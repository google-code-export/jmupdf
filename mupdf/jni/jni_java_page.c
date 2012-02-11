#include "jmupdf.h"

/**
 * Load a page
 */
static void jni_load_page(jni_document *hdoc, int pagen)
{
	fz_try(hdoc->ctx)
	{
		hdoc->page = fz_load_page(hdoc->doc, pagen-1);
		hdoc->page_bbox = fz_bound_page(hdoc->doc, hdoc->page);
		hdoc->page_number = pagen;
	}
	fz_catch(hdoc->ctx)
	{
		hdoc->page = NULL;
	}
}

/**
 * Draw a page
 */
static void jni_draw_page(jni_document *hdoc, int pagen)
{
	fz_device *dev = NULL;
	fz_try(hdoc->ctx)
	{
		hdoc->page_list = fz_new_display_list(hdoc->ctx);
		dev = fz_new_list_device(hdoc->ctx, hdoc->page_list);
		fz_run_page(hdoc->doc, hdoc->page, dev, fz_identity, NULL);
	}
	fz_catch(hdoc->ctx)
	{
		jni_free_page(hdoc);
	}
	fz_free_device(dev);
}

/**
 * Load page text
 */
static fz_text_span * jni_load_page_text(jni_document *hdoc, int pagen, float zoom, int rotate)
{
	fz_matrix ctm = jni_get_view_ctm(hdoc, zoom, rotate);

	fz_text_span *page_text = NULL;
	fz_device *dev = NULL;

	fz_try(hdoc->ctx)
	{
		page_text = fz_new_text_span(hdoc->ctx);
		dev = fz_new_text_device(hdoc->ctx, page_text);
		fz_run_display_list(hdoc->page_list, dev, ctm, fz_infinite_bbox, NULL);
	}
	fz_always(hdoc->ctx)
	{
		fz_free_device(dev);
	}
	fz_catch(hdoc->ctx)
	{
		if(page_text)
		{
			fz_free_text_span(hdoc->ctx, page_text);
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
 * Get a page
 */
void jni_get_page(jni_document *hdoc, int pagen)
{
	if (pagen != hdoc->page_number)
	{
		jni_free_page(hdoc);
	}

	if (!hdoc->page)
	{
		jni_load_page(hdoc, pagen);
	}

	if (hdoc->page && !hdoc->page_list)
	{
		jni_draw_page(hdoc, pagen);
	}
}

/**
 * Free page and resources
 */
void jni_free_page(jni_document *hdoc)
{
	if (hdoc->page_list)
	{
		fz_free_display_list(hdoc->ctx, hdoc->page_list);
	}

	if (hdoc->page)
	{
		fz_free_page(hdoc->doc, hdoc->page);
	}

	hdoc->page = NULL;
	hdoc->page_list = NULL;
	hdoc->page_number = 0;

	fz_flush_warnings(hdoc->ctx);
}

/**
 * Get page count
 */
JNIEXPORT jint JNICALL
Java_com_jmupdf_JmuPdf_getPageCount(JNIEnv *env, jclass obj, jlong handle)
{
	jni_document *hdoc = jni_get_document(handle);

	if (!hdoc)
	{
		return -1;
	}

	int rc = -2;

	fz_try(hdoc->ctx)
	{
		rc = fz_count_pages(hdoc->doc);
	}
	fz_catch(hdoc->ctx) {}

	return rc;
}

/**
 * Get page dimensions
 */
JNIEXPORT jfloatArray JNICALL
Java_com_jmupdf_JmuPdf_loadPage(JNIEnv *env, jclass obj, jlong handle, jint pagen)
{
	jni_document *hdoc = jni_get_document(handle);

	if (!hdoc)
	{
		return NULL;
	}

	jni_get_page(hdoc, pagen);

	if(!hdoc->page)
	{
		return NULL;
	}

	jfloatArray dataarray = jni_new_float_array(5);

	if (!dataarray)
	{
		return NULL;
	}

	jfloat *data = jni_get_float_array(dataarray);

	data[0] = hdoc->page_bbox.x0;
	data[1] = hdoc->page_bbox.y0;
	data[2] = hdoc->page_bbox.x1;
	data[3] = hdoc->page_bbox.y1;

	if (hdoc->doc_type == DOC_PDF)
	{
		data[4] = ((pdf_page*)hdoc->page)->rotate;
	}

	jni_release_float_array(dataarray, data);

	return dataarray;
}

/**
 * Get Page Text
 */
JNIEXPORT jobjectArray JNICALL
Java_com_jmupdf_JmuPdf_getPageText(JNIEnv *env, jclass obj, jlong handle, jint pagen, jfloat zoom, jint rotate, jint x0, jint y0, jint x1, jint y1)
{
	jni_document *hdoc = jni_get_document(handle);

	if (!hdoc)
	{
		return NULL;
	}

	jni_get_page(hdoc, pagen);

	if(!hdoc->page)
	{
		return NULL;
	}

	fz_text_span *page_text = jni_load_page_text(hdoc, pagen, zoom, rotate);

	if (!page_text)
	{
		return NULL;
	}

	jclass cls = jni_new_page_text_class();

	if (!cls)
	{
		fz_free_text_span(hdoc->ctx, page_text);
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
	fz_free_text_span(hdoc->ctx, page_text);

	return page_text_arr;
}

/**
 * Get Page Links
 */
JNIEXPORT jobjectArray JNICALL
Java_com_jmupdf_JmuPdf_getPageLinks(JNIEnv *env, jclass obj, jlong handle, jint pagen)
{
	jni_document *hdoc = jni_get_document(handle);

	if (!hdoc)
	{
		return NULL;
	}

	jni_get_page(hdoc, pagen);

	if (!hdoc->page)
	{
		return NULL;
	}

	fz_link *page_links = fz_load_links(hdoc->doc, hdoc->page);

	if (!page_links)
	{
		return NULL;
	}

	jclass cls = jni_new_page_links_class();

	if (!cls)
	{
		fz_drop_link(hdoc->ctx, page_links);
		return NULL;
	}

	jmethodID mid = jni_get_page_links_init(cls);
	jobjectArray page_links_arr = NULL;

	// Count up total links
	int totlinks = 0;
	fz_link *link;

	for (link = page_links; link; link = link->next)
	{
		if (link->dest.kind == FZ_LINK_URI || link->dest.kind == FZ_LINK_GOTO)
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
				if (link->dest.kind == FZ_LINK_URI)
				{
					seen = 1;
					type = 1;
					buf = link->dest.ld.uri.uri;
				}
				else if (link->dest.kind == FZ_LINK_GOTO)
				{
					seen = 1;
					type = 0;
					buf = fz_malloc_no_throw(hdoc->ctx, 1);
					if (buf)
					{
						sprintf(buf, "%d", link->dest.ld.gotor.page);
					}
				}
				if (seen == 1)
				{
					text = jni_new_string(buf);
					new_page_links = jni_new_page_links_obj(
							cls, mid,
							link->rect.x0, link->rect.y0,
							link->rect.x1, link->rect.y1, type, text);
					jni_set_object_array_el(page_links_arr, e++, new_page_links);
					if (type == 0)
					{
						fz_free(hdoc->ctx, buf);
					}
				}
			}
		}
	}

	// Free resources
	jni_free_ref(cls);
	fz_drop_link(hdoc->ctx, page_links);

	return page_links_arr;
}
