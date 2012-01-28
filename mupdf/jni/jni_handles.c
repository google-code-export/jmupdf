#include "jmupdf.h"

/**
 * Create a new jni_doc_handle
 */
jni_doc_handle *jni_new_doc_handle(int max_store)
{
	fz_context *ctx = fz_new_context(NULL, max_store);

	if (!ctx)
	{
		return NULL;
	}

	jni_doc_handle *hdoc = fz_malloc_no_throw(ctx, sizeof(jni_doc_handle));

	if (!hdoc)
	{
		fz_free_context(ctx);
		return NULL;
	}

	hdoc->ctx = ctx;
	hdoc->handle = jni_ptr_to_jlong(hdoc);
	hdoc->anti_alias_level = fz_get_aa_level(hdoc->ctx);
	hdoc->page_number = 0;
	hdoc->pdf = NULL;
	hdoc->xps = NULL;
	hdoc->xps_page = NULL;
	hdoc->pdf_page = NULL;
	hdoc->page_list = NULL;

	return hdoc;
}

/**
 * Free handle resources and make handle reusable
 */
int jni_free_doc_handle(jlong handle)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if (!hdoc)
	{
		return -1;
	}

	fz_context *ctx = hdoc->ctx;

	if (!ctx)
	{
		return -1;
	}

	jni_free_page(hdoc);

	if (hdoc->pdf)
	{
		pdf_close_document(hdoc->pdf);
	}
	if (hdoc->xps)
	{
		xps_close_document(hdoc->xps);
	}

	fz_free(ctx, hdoc);
	fz_free_context(ctx);

	return 0;
}

/*
 * Get jni_doc_handle
 */
jni_doc_handle *jni_get_doc_handle(jlong handle)
{
	return (jni_doc_handle *)jni_jlong_to_ptr(handle);
}
