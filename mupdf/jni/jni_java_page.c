#include "jmupdf.h"

/**
 * Create new page object
 */
static jni_page *jni_new_page(jni_document *doc, int pagen)
{
	jni_page *page = fz_malloc_no_throw(doc->ctx, sizeof(jni_page));

	if (!page)
	{
		return NULL;
	}

	page->doc = doc;
	page->page = NULL;
	page->list = NULL;
	page->page_number = pagen;
	page->ctx = fz_clone_context(doc->ctx);
	page->anti_alias = fz_get_aa_level(page->ctx);
	page->gamma = 1.0f;

	return page;
}

/**
 * Free page object
 */
static void jni_free_page(jni_page *page)
{
	if (!page)
	{
		return;
	}

	fz_context *ctx = page->ctx;

	if (page->list)
	{
		fz_free_display_list(ctx, page->list);
	}

	if (page->page)
	{
		fz_free_page(page->doc->doc, page->page);
	}

	fz_flush_warnings(ctx);

	page->page = NULL;
	page->list = NULL;
	page->page_number = 0;

	fz_free(ctx, page);
	fz_free_context(ctx);
}

/**
 * Load a page
 */
static void jni_load_page(jni_page *page)
{
	fz_try(page->ctx)
	{
		page->page = fz_load_page(page->doc->doc, page->page_number-1);
		page->bbox = fz_bound_page(page->doc->doc, page->page);
	}
	fz_catch(page->ctx)
	{
		page->page = NULL;
	}
}

/**
 * Draw a page
 */
static void jni_draw_page(jni_page *page)
{
	fz_device *dev = NULL;
	fz_try(page->ctx)
	{
		page->list = fz_new_display_list(page->ctx);
		dev = fz_new_list_device(page->ctx, page->list);
		fz_run_page(page->doc->doc, page->page, dev, fz_identity, NULL);
	}
	fz_catch(page->ctx) {}
	fz_free_device(dev);
}

/**
 * Load page text
 */
static fz_text_span * jni_load_text(jni_page *page)
{
	fz_matrix ctm = jni_get_view_ctm(1, 0);

	fz_text_span *page_text = NULL;
	fz_device *dev = NULL;

	fz_try(page->ctx)
	{
		page_text = fz_new_text_span(page->ctx);
		dev = fz_new_text_device(page->ctx, page_text);
		fz_run_display_list(page->list, dev, ctm, fz_infinite_bbox, NULL);
	}
	fz_always(page->ctx)
	{
		fz_free_device(dev);
	}
	fz_catch(page->ctx)
	{
		if(page_text)
		{
			fz_free_text_span(page->ctx, page_text);
			page_text = NULL;
		}
	}

	return page_text;
}

/**
 * Count text span objects within given coordinates
 */
static int jni_count_text_span(fz_text_span *page_text, int x0, int y0, int x1, int y1)
{
	fz_text_span *span;
	fz_bbox *hitbox;
	int i = 0;
	int totspan = 0;
	for (span = page_text; span; span = span->next)
	{
		for (i = 0; i < span->len; i++)
		{
			hitbox = &span->text[i].bbox;
			if (hitbox->x1 >= x0 && hitbox->x0 <= x1 && hitbox->y1 >= y0 && hitbox->y0 <= y1)
			{
				++totspan;
				break;
			}
		}
	}
	return totspan;
}

/**
 * Get page from pointer
 */
jni_page *jni_get_page(jlong handle)
{
	return (jni_page *)jni_jlong_to_ptr(handle);
}

/**
 * Get page dimensions
 */
JNIEXPORT jfloatArray JNICALL
Java_com_jmupdf_JmuPdf_loadPage(JNIEnv *env, jclass obj, jlong handle)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return NULL;
	}

	fz_lock(page->ctx, FZ_LOCK_FILE);
	{
	 jni_load_page(page);
	}
	fz_unlock(page->ctx, FZ_LOCK_FILE);

	if(!page->page)
	{
		return NULL;
	}

	fz_lock(page->ctx, FZ_LOCK_FILE);
	{
	  jni_draw_page(page);
	}
	fz_unlock(page->ctx, FZ_LOCK_FILE);

	if(!page->list)
	{
		return NULL;
	}

	jfloatArray dataarray = jni_new_float_array(5);

	if (!dataarray)
	{
		return NULL;
	}

	jfloat *data = jni_get_float_array(dataarray);

	data[0] = page->bbox.x0;
	data[1] = page->bbox.y0;
	data[2] = page->bbox.x1;
	data[3] = page->bbox.y1;

	if (page->doc->type == DOC_PDF)
	{
		data[4] = ((pdf_page*)page->page)->rotate;
	}

	jni_release_float_array(dataarray, data);

	return dataarray;
}

/**
 * Get Page Text
 *
 * Coordinates are assumed to reflect a zoom factor of 1f and 0 rotation
 */
JNIEXPORT jobjectArray JNICALL
Java_com_jmupdf_JmuPdf_getPageText(JNIEnv *env, jclass obj, jlong handle, jint x0, jint y0, jint x1, jint y1)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return NULL;
	}

	fz_text_span *page_text = jni_load_text(page);

	if (!page_text)
	{
		return NULL;
	}

	jclass cls = jni_new_page_text_class();

	if (!cls)
	{
		fz_free_text_span(page->ctx, page_text);
		return NULL;
	}

	jmethodID init = jni_get_page_text_init(cls);
	jobjectArray page_text_arr = NULL;

	int totspan = jni_count_text_span(page_text, x0, y0, x1, y1);

	if (totspan > 0)
	{
		page_text_arr = jni_new_object_array(totspan, cls);

		if (page_text_arr)
		{
			int e = 0;
			int p = 0;
			int i = 0;
			int seen = 0;
			fz_text_span *span;
			fz_bbox *hitbox;
			jintArray txtarr = NULL;
			jint *txtptr = NULL;
			jobject new_page;

			for (span = page_text; span; span = span->next)
			{
				seen = 0;
				p = 0;
				for (i = 0; i < span->len; i++)
				{
					hitbox = &span->text[i].bbox;
					if (hitbox->x1 >= x0 && hitbox->x0 <= x1 && hitbox->y1 >= y0 && hitbox->y0 <= y1)
					{
						if (seen == 0)
						{
							txtarr = jni_new_int_array(span->len);
							txtptr = jni_get_int_array(txtarr);
							seen = 1;
						}
						txtptr[p++] = span->text[i].c;
					}
				}
				if (seen == 1)
				{
					jni_release_int_array(txtarr, txtptr);
					new_page = jni_new_page_text_obj(
						               cls, init,
						               span->text[0].bbox.x0, span->text[0].bbox.y0,
						               hitbox->x1, hitbox->y1, span->eol, txtarr);
					jni_set_object_array_el(page_text_arr, e++, new_page);
				}
			}
		}
	}

	jni_free_ref(cls);
	fz_free_text_span(page->ctx, page_text);

	return page_text_arr;
}

/**
 * Get Page Links
 */
JNIEXPORT jobjectArray JNICALL
Java_com_jmupdf_JmuPdf_getPageLinks(JNIEnv *env, jclass obj, jlong handle)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return NULL;
	}

	fz_link *page_links = fz_load_links(page->doc->doc, page->page);

	if (!page_links)
	{
		return NULL;
	}

	jclass cls = jni_new_page_links_class();

	if (!cls)
	{
		fz_drop_link(page->ctx, page_links);
		return NULL;
	}

	jmethodID mid = jni_get_page_links_init(cls);
	jobjectArray page_links_arr = NULL;

	// Count up total links
	int totlinks = 0;
	fz_link *link;

	for (link = page_links; link; link = link->next)
	{
		if (link->dest.kind)
		{
			totlinks++;
		}
	}

	// Store link data in object array
	if (totlinks > 0)
	{
		page_links_arr = jni_new_object_array(totlinks, cls);
		if (page_links_arr)
		{
			int e = 0;
			int seen;
			int type;
			char *buf;
			jobject new_page_links;
			jstring text;

			for (link = page_links; link; link = link->next)
			{
				seen = 0;
				switch (link->dest.kind) {
					case FZ_LINK_GOTO:
						buf = fz_malloc_no_throw(page->ctx, 1);
						if (buf)
						{
							seen = 1;
							type = 1;
							sprintf(buf, "%d", link->dest.ld.gotor.page + 1);
						}
						break;
					case FZ_LINK_URI:
						seen = 1;
						type = 2;
						buf = link->dest.ld.uri.uri;
						break;
					case FZ_LINK_LAUNCH:
						seen = 1;
						type = 3;
						buf = link->dest.ld.launch.file_spec;
						break;
					case FZ_LINK_NAMED:
						seen = 1;
						type = 4;
						buf = link->dest.ld.named.named;
						break;
					case FZ_LINK_GOTOR:
						seen = 1;
						type = 5;
						buf = link->dest.ld.gotor.file_spec;
						break;
					default:
						break;
				}
				if (seen == 1)
				{
					text = jni_new_string(buf);
					new_page_links = jni_new_page_links_obj(
							cls, mid,
							link->rect.x0, link->rect.y0,
							link->rect.x1, link->rect.y1, type, text);
					jni_set_object_array_el(page_links_arr, e++, new_page_links);
					if (type == 1)
					{
						fz_free(page->ctx, buf);
					}
				}
			}
		}
	}

	// Free resources
	jni_free_ref(cls);
	fz_drop_link(page->ctx, page_links);

	return page_links_arr;
}

/**
 * Create new page object
 */
JNIEXPORT jlong JNICALL
Java_com_jmupdf_JmuPdf_newPage(JNIEnv *env, jclass obj, jlong handle, jint pagen)
{
	jni_document *doc = jni_get_document(handle);

	if (!doc)
	{
		return -1;
	}

	jni_page *page = jni_new_page(doc, pagen);

	if (!page)
	{
		return -2;
	}

	return jni_ptr_to_jlong(page);
}

/**
 * Free page resources
 */
JNIEXPORT void JNICALL
Java_com_jmupdf_JmuPdf_freePage(JNIEnv *env, jclass obj, jlong handle)
{
	jni_free_page(jni_get_page(handle));
}
