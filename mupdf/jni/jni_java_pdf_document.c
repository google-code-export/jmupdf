#include "jmupdf.h"

/**
 * Open a PDF document
 *
 */
JNIEXPORT jlong JNICALL Java_com_jmupdf_JmuPdf_pdfOpen(JNIEnv *env, jclass obj, jstring docname, jstring password, jint max_store)
{
	jni_doc_handle *hdoc = jni_new_doc_handle(max_store);

	if (!hdoc)
	{
		return -1;
	}

	// Open PDF and load xref table
	int rc = 0;
	const char *pdfDoc = (*env)->GetStringUTFChars(env, docname, 0);
	fz_stream *file = NULL;

	fz_try(hdoc->ctx)
	{
		file = fz_open_file(hdoc->ctx, pdfDoc);
		hdoc->xref = pdf_open_xref_with_stream(file);
	}
	fz_always(hdoc->ctx)
	{
		fz_close(file);
		(*env)->ReleaseStringUTFChars(env, docname, pdfDoc);
	}
	fz_catch(hdoc->ctx)
	{
		if (!file)
		{
			rc = -2;
		}
		else if (!hdoc->xref)
		{
			rc = -3;
		}
		jni_free_doc_handle(hdoc->handle);
	}

	if (rc != 0)
	{
		return rc;
	}

	// Handle encrypted PDF file
	if (pdf_needs_password(hdoc->xref))
	{
		if (password == NULL)
		{
			jni_free_doc_handle(hdoc->handle);
			return -4;
		}
		char *pass = (char*)(*env)->GetStringUTFChars(env, password, 0);
		int ok = pdf_authenticate_password(hdoc->xref, pass);
		(*env)->ReleaseStringUTFChars(env, password, pass);
		if(!ok)
		{
			jni_free_doc_handle(hdoc->handle);
			return -5;
		}
	}

	return hdoc->handle;
}

/**
 * Close a PDF document and free resources
 *
 */
JNIEXPORT jint JNICALL Java_com_jmupdf_JmuPdf_pdfClose(JNIEnv *env, jclass obj, jlong handle)
{
	return jni_free_doc_handle(handle);
}

/**
 * Get PDF version
 *
 */
JNIEXPORT jint JNICALL Java_com_jmupdf_JmuPdf_pdfVersion(JNIEnv *env, jclass obj, jlong handle)
{
	return jni_get_doc_handle(handle)->xref->version;
}

/**
 * Get PDF information from dictionary.
 *
 */
JNIEXPORT jstring JNICALL Java_com_jmupdf_JmuPdf_pdfInfo(JNIEnv *env, jclass obj, jlong handle, jstring key)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if (!hdoc)
	{
		return NULL;
	}

	fz_obj *info = fz_dict_gets(hdoc->xref->trailer, "Info");
	char *text = NULL;

	if (info)
	{
		const char *dictkey = (*env)->GetStringUTFChars(env, key, 0);
		fz_obj *obj = fz_dict_gets(info, (char*)dictkey);
		(*env)->ReleaseStringUTFChars(env, key, dictkey);
		if (!obj)
		{
			return NULL;
		}
		text = pdf_to_utf8(hdoc->ctx, obj);
	}

	return (*env)->NewStringUTF(env, text);
}

/**
 * Get encryption information
 *
 */
JNIEXPORT jintArray JNICALL Java_com_jmupdf_JmuPdf_pdfEncryptInfo(JNIEnv *env, jclass obj, jlong handle)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if (!hdoc)
	{
		return NULL;
	}

	if (!hdoc->xref)
	{
		return NULL;
	}

	int sizeofarray = 12;

	jintArray dataarray = (*env)->NewIntArray(env, sizeofarray);

	if (!dataarray)
	{
		return NULL;
	}

	jint *data = (*env)->GetIntArrayElements(env, dataarray, 0);

	data[1]  = pdf_has_permission(hdoc->xref, PDF_PERM_PRINT); 				// print
	data[2]  = pdf_has_permission(hdoc->xref, PDF_PERM_CHANGE); 			// modify
	data[3]  = pdf_has_permission(hdoc->xref, PDF_PERM_COPY);				// copy
	data[4]  = pdf_has_permission(hdoc->xref, PDF_PERM_NOTES);				// annotate
	data[5]  = pdf_has_permission(hdoc->xref, PDF_PERM_FILL_FORM);			// Fill form fields
	data[6]  = pdf_has_permission(hdoc->xref, PDF_PERM_ACCESSIBILITY);		// Extract text and graphics
	data[7]  = pdf_has_permission(hdoc->xref, PDF_PERM_ASSEMBLE);			// Document assembly
	data[8]  = pdf_has_permission(hdoc->xref, PDF_PERM_HIGH_RES_PRINT);		// Print quality
	data[9]  = pdf_get_crypt_revision(hdoc->xref);							// Revision
	data[10] = pdf_get_crypt_length(hdoc->xref);							// Length

	char *method = pdf_get_crypt_method(hdoc->xref);						// Method

	if (strcmp(method, "RC4") == 0)  			data[11] = 1;
	else if (strcmp(method, "AES") == 0)  		data[11] = 2;
	else if (strcmp(method, "Unknown") == 0) 	data[11] = 3;
	else 										data[11] = 0;

	data[0] = data[11] > 0;													// Is encrypted

	(*env)->ReleaseIntArrayElements(env, dataarray, data, 0);

	return dataarray;
}
