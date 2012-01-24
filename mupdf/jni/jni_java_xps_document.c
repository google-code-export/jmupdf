#include "jmupdf.h"

/**
 * Open an XPS document
 *
 */
JNIEXPORT jlong JNICALL Java_com_jmupdf_JmuPdf_xpsOpen(JNIEnv *env, jclass obj, jstring docname, jint max_store)
{
	jni_doc_handle *hdoc = jni_new_doc_handle(max_store);

	if (!hdoc)
	{
		return -1;
	}

	const char *xpsdoc = (*env)->GetStringUTFChars(env, docname, 0);
	int rc = 0;

	fz_try(hdoc->ctx)
	{
		hdoc->xps = xps_open_file(hdoc->ctx, (char*)xpsdoc);
	}
	fz_always(hdoc->ctx)
	{
		(*env)->ReleaseStringUTFChars(env, docname, xpsdoc);
	}
	fz_catch(hdoc->ctx)
	{
		jni_free_doc_handle(hdoc->handle);
		rc = -2;
	}

	if (rc == 0)
	{
		return hdoc->handle;
	}
	else
	{
		return rc;
	}
}

/**
 * Close an XPS document and free resources
 *
 */
JNIEXPORT jint JNICALL Java_com_jmupdf_JmuPdf_xpsClose(JNIEnv *env, jclass obj, jlong handle)
{
	return jni_free_doc_handle(handle);
}
