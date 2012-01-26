#include "jmupdf.h"

/*
 * Load a pdf page
 */
static void jni_load_pdf_page(jni_doc_handle *hdoc, int pagen)
{
	if (pagen == hdoc->page_number)
	{
		if (hdoc->pdf_page)
		{
			return;
		}
	}

	fz_try(hdoc->ctx)
	{
		jni_free_page(hdoc);
		hdoc->pdf_page = pdf_load_page(hdoc->xref, pagen-1);
		hdoc->page_bbox = pdf_bound_page(hdoc->xref, hdoc->pdf_page);
		hdoc->page_number = pagen;
	}
	fz_catch(hdoc->ctx)
	{
		hdoc->pdf_page = NULL;
	}
}

/**
 * Load a pdf page and parse it
 */
static void jni_load_pdf_page_for_view(jni_doc_handle *hdoc, int pagen)
{
	fz_device *dev = NULL;

	fz_try(hdoc->ctx)
	{
		jni_load_pdf_page(hdoc, pagen);
		if (!hdoc->page_list)
		{
			hdoc->page_list = fz_new_display_list(hdoc->ctx);
			dev = fz_new_list_device(hdoc->ctx, hdoc->page_list);
			pdf_run_page(hdoc->xref, hdoc->pdf_page, dev, fz_identity, NULL);
		}
	}
	fz_always(hdoc->ctx)
	{
		fz_free_device(dev);
	}
	fz_catch(hdoc->ctx)
	{
		jni_free_page(hdoc);
	}
}

/*
 * Load an xps page
 */
static void jni_load_xps_page(jni_doc_handle *hdoc, int pagen)
{
	if (pagen == hdoc->page_number)
	{
		if (hdoc->xps_page)
		{
			return;
		}
	}

	jni_free_page(hdoc);

	fz_try(hdoc->ctx)
	{
		hdoc->xps_page = xps_load_page(hdoc->xps, pagen-1);
		hdoc->page_bbox = xps_bound_page(hdoc->xps, hdoc->xps_page);
		hdoc->page_number = pagen;
	}
	fz_catch(hdoc->ctx) {}

	return;
}

/**
 * Create a new XPS page object and cache it
 */
static void jni_load_xps_page_for_view(jni_doc_handle *hdoc, int pagen)
{
	jni_load_xps_page(hdoc, pagen);

	if (!hdoc->xps_page)
	{
		return;
	}

	// If page_list already exists, then exit
	if (hdoc->page_list)
	{
		return;
	}

	fz_device *dev = NULL;

	fz_try(hdoc->ctx)
	{
		// Create display list
		hdoc->page_list = fz_new_display_list(hdoc->ctx);
		dev = fz_new_list_device(hdoc->ctx, hdoc->page_list);
		hdoc->xps->dev = dev;
	}
	fz_catch(hdoc->ctx) {}

	if (dev)
	{
		fz_try(hdoc->ctx)
		{
			// Parse page object
			xps_parse_fixed_page(hdoc->xps, fz_identity, hdoc->xps_page);
		}
		fz_catch(hdoc->ctx) {}

		fz_free_device(dev);
		hdoc->xps->dev = NULL;
	}

	return;
}

/**
 * Create a new page object
 *
 */
void jni_get_doc_page(jni_doc_handle *hdoc, int pagen)
{
	if (hdoc->xref)
	{
		jni_load_pdf_page_for_view(hdoc, pagen);
	}
	else if (hdoc->xps)
	{
		jni_load_xps_page_for_view(hdoc, pagen);
	}
}

/*
 * Free page
 */
void jni_free_page(jni_doc_handle *hdoc)
{
	if (hdoc->xps_page)
	{
		xps_free_page(hdoc->xps, hdoc->xps_page);
	}
	if (hdoc->page_list)
	{
		fz_free_display_list(hdoc->ctx, hdoc->page_list);
	}
	if (hdoc->pdf_page)
	{
		pdf_free_page(hdoc->ctx, hdoc->pdf_page);
	}

	hdoc->page_list = NULL;
	hdoc->pdf_page = NULL;
	hdoc->xps_page = NULL;
	hdoc->page_number = 0;

	fz_flush_warnings(hdoc->ctx);
}

/**
 * Get page count
 *
 */
JNIEXPORT jint JNICALL Java_com_jmupdf_JmuPdf_getPageCount(JNIEnv *env, jclass obj, jlong handle)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if (!hdoc)
	{
		return -1;
	}

	int rc = -2;

	fz_try(hdoc->ctx)
	{
		if (hdoc->xref)
		{
			rc = pdf_count_pages(hdoc->xref);
		}
		else if (hdoc->xps)
		{
			rc = xps_count_pages(hdoc->xps);
		}
	}
	fz_catch(hdoc->ctx) {}

	return rc;
}

/**
 * Get page dimensions
 *
 */
JNIEXPORT jfloatArray JNICALL Java_com_jmupdf_JmuPdf_loadPage(JNIEnv *env, jclass obj, jlong handle, jint pagen)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if (!hdoc)
	{
		return NULL;
	}

	jfloatArray dataarray = (*env)->NewFloatArray(env, 5);

	if (!dataarray)
	{
		return NULL;
	}

	jfloat *data = (*env)->GetFloatArrayElements(env, dataarray, 0);

	data[0] = 0;
	data[1] = 0;
	data[2] = 0;
	data[3] = 0;
	data[4] = 0;

	if (hdoc->xref)
	{
		jni_load_pdf_page(hdoc, pagen);
		data[4] = hdoc->pdf_page->rotate;
	}
	else if (hdoc->xps)
	{
		jni_load_xps_page(hdoc, pagen);
	}

	if(hdoc->pdf_page || hdoc->xps_page)
	{
		data[0] = hdoc->page_bbox.x0;
		data[1] = hdoc->page_bbox.y0;
		data[2] = hdoc->page_bbox.x1;
		data[3] = hdoc->page_bbox.y1;
	}

	(*env)->ReleaseFloatArrayElements(env, dataarray, data, 0);

	return dataarray;
}

/**
 * Get Page Text
 *
 */
JNIEXPORT jobjectArray JNICALL Java_com_jmupdf_JmuPdf_getPageText(JNIEnv *env, jclass obj, jlong handle, jint pagen, jfloat zoom, jint rotate, jint x0, jint y0, jint x1, jint y1)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if (!hdoc)
	{
		return NULL;
	}

	// Load page object
	jni_get_doc_page(hdoc, pagen);

	// Get Current Transformation Matrix
	fz_matrix ctm = jni_get_view_ctm(hdoc, zoom, rotate);

	fz_text_span *page_text = NULL;
	fz_device *dev = NULL;

	// Extract text
	fz_try(hdoc->ctx)
	{
		page_text = fz_new_text_span(hdoc->ctx);
		dev = fz_new_text_device(hdoc->ctx, page_text);
		fz_execute_display_list(hdoc->page_list, dev, ctm, fz_infinite_bbox, NULL);
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

	if (!page_text)
	{
		return NULL;
	}

	// Count total span objects
	fz_text_span *span;
	fz_bbox *hitbox;
	int i = 0;
	int seen = 0;
	int totspan = 0;
	for (span = page_text; span; span = span->next)
	{
		seen = 0;
		for (i = 0; i < span->len; i++)
		{
			hitbox = &span->text[i].bbox;
			if (hitbox->x1 >= x0 && hitbox->x0 <= x1 && hitbox->y1 >= y0 && hitbox->y0 <= y1)
			{
				seen = 1;
			}
		}
		if (seen)
		{
			++totspan;
		}
	}

	jobjectArray joa = NULL;
	jclass cls = (*env)->FindClass(env, "com/jmupdf/page/PageText");
	jmethodID mid = (*env)->GetMethodID(env, cls, "<init>", "(IIIII[I)V");

	// Load up span objects with text
	if (totspan > 0)
	{
		joa = (*env)->NewObjectArray(env, totspan, cls, NULL);
		if (joa != NULL)
		{
			int e = 0;
			int p = 0;
			jintArray txtarr = NULL;
			jint *txtptr = NULL;
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
							txtarr = (*env)->NewIntArray(env, span->len);
							txtptr = (*env)->GetIntArrayElements(env, txtarr, 0);
						}
						txtptr[p++] = span->text[i].c;
						seen = 1;
					}
				}
				if (seen == 1)
				{
					(*env)->SetIntArrayRegion(env, txtarr, 0, span->len, txtptr);
					(*env)->ReleaseIntArrayElements(env, txtarr, txtptr, 0);
					(*env)->SetObjectArrayElement(env, joa, e++,
							(*env)->NewObject(env, cls, mid, span->text[0].bbox.x0,
															 span->text[0].bbox.y0,
															 hitbox->x1,
															 hitbox->y1,
															 span->eol,
															 txtarr));
				}
			}
		}
	}

	// Free resources
	(*env)->DeleteLocalRef(env, cls);
	fz_free_text_span(hdoc->ctx, page_text);

	return joa;
}

/**
 * Get Page Links
 *
 * TODO : Get page links for xps documents
 *
 */
JNIEXPORT jobjectArray JNICALL Java_com_jmupdf_JmuPdf_getPageLinks(JNIEnv *env, jclass obj, jlong handle, jint pagen)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if (!hdoc)
	{
		return NULL;
	}

	jni_get_doc_page(hdoc, pagen);

	if(!hdoc->pdf_page)
	{
		return NULL;
	}

	jobjectArray joa = NULL;
	jclass cls = (*env)->FindClass(env, "com/jmupdf/page/PageLinks");
	jmethodID mid = (*env)->GetMethodID(env, cls, "<init>", "(FFFFILjava/lang/String;)V");

	// Count up total links
	int totlinks = 0;
	fz_link *link;

	for (link = hdoc->pdf_page->links; link; link = link->next)
	{
		if (link->dest.kind == FZ_LINK_URI || link->dest.kind == FZ_LINK_GOTO)
		{
			totlinks++;
		}
	}

	// Store link data in object array
	if (totlinks > 0)
	{
		joa = (*env)->NewObjectArray(env, totlinks, cls, NULL);
		if (joa)
		{
			int e = 0;
			int seen;
			int type;
			char *buf;
			for (link = hdoc->pdf_page->links; link; link = link->next)
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
					(*env)->SetObjectArrayElement(env, joa, e++,
							(*env)->NewObject(env, cls, mid,
									link->rect.x0,
									link->rect.y0,
									link->rect.x1,
									link->rect.y1,
									type,
									(*env)->NewStringUTF(env, buf)));
					if (type == 0)
						fz_free(hdoc->ctx, buf);
				}
			}
		}
	}

	// Free resources
	(*env)->DeleteLocalRef(env, cls);

	return joa;
}
