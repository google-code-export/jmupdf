#include "jmupdf.h"

// Document type constants
static const int DOC_PDF = 0;
static const int DOC_XPS = 1;

/**
 * Open a PDF document
 *
 */
static int jni_open_pdf(jni_doc_handle *hdoc, const char *file, char *password)
{
	fz_stream *stm = NULL;
	int rc = 0;

	fz_try(hdoc->ctx)
	{
		stm = fz_open_file(hdoc->ctx, file);
		hdoc->pdf = pdf_open_document_with_stream(stm);
	}
	fz_always(hdoc->ctx)
	{
		fz_close(stm);
	}
	fz_catch(hdoc->ctx)
	{
		if (!stm)
		{
			rc = -1;
		}
		else if (!hdoc->pdf)
		{
			rc = -2;
		}
	}

	if (hdoc->pdf)
	{
		if (pdf_needs_password(hdoc->pdf))
		{
			if(!pdf_authenticate_password(hdoc->pdf, password))
			{
				rc = -3;
			}
		}
	}

	return rc;
}

/**
 * Open an XPS document
 *
 */
static int jni_open_xps(jni_doc_handle *hdoc, const char *file)
{
	int rc = 0;

	fz_try(hdoc->ctx)
	{
		hdoc->xps = xps_open_document(hdoc->ctx, (char*)file);
	}
	fz_catch(hdoc->ctx)
	{
		rc = -1;
	}

	return rc;
}

/**
 * Load outline to PdfOutline object structure
 *
 */
static void jni_load_outline(JNIEnv *env, jclass cls, jobject obj,
		                     jmethodID add_next, jmethodID add_child,
		                     jmethodID set_page, jmethodID set_title,
		                     jni_doc_handle *hdoc, fz_outline *outline)
{
	while (outline)
	{
		if (outline->title)
		{
			(*env)->CallVoidMethod(env, obj, set_title, (*env)->NewStringUTF(env, outline->title));
		}
		if (outline->dest.kind == FZ_LINK_GOTO)
		{
			(*env)->CallVoidMethod(env, obj, set_page, outline->dest.ld.gotor.page+1);
		}
		if (outline->down)
		{
			jni_load_outline(env, cls, (*env)->CallObjectMethod(env, obj, add_child),
					         add_next, add_child, set_page, set_title, hdoc, outline->down);
		}
		outline = outline->next;
		if (outline)
		{
			obj = (*env)->CallObjectMethod(env, obj, add_next);
		}
	}
}

/**
 * Open a PDF document
 *
 */
JNIEXPORT jlong JNICALL Java_com_jmupdf_JmuPdf_open(JNIEnv *env, jclass obj, jint type, jstring document, jstring password, jint max_store)
{
	jni_doc_handle *hdoc = jni_new_doc_handle(max_store);

	if (!hdoc)
	{
		return -1;
	}

	const char *file = (*env)->GetStringUTFChars(env, document, 0);
	char *pass = (char*)(*env)->GetStringUTFChars(env, password, 0);
	int rc = 0;

	if (type == DOC_PDF)
	{
		rc = jni_open_pdf(hdoc, file, pass);
	}
	else if (type == DOC_XPS)
	{
		rc = jni_open_xps(hdoc, file);
	}
	else
	{
		rc = -1;
	}

	(*env)->ReleaseStringUTFChars(env, document, file);
	(*env)->ReleaseStringUTFChars(env, password, pass);

	if (rc != 0)
	{
		jni_free_doc_handle(hdoc->handle);
		return -1;
	}

	return hdoc->handle;
}

/**
 * Close a document and free resources
 *
 */
JNIEXPORT jint JNICALL Java_com_jmupdf_JmuPdf_close(JNIEnv *env, jclass obj, jlong handle)
{
	return jni_free_doc_handle(handle);
}

/**
 * Get document version
 *
 */
JNIEXPORT jint JNICALL Java_com_jmupdf_JmuPdf_getVersion(JNIEnv *env, jclass obj, jlong handle)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);
	int v = 0;

	if (hdoc->pdf)
	{
		v = hdoc->pdf->version;
	}

	return v;
}

/**
 * Get an array that has the outline of the document
 *
 */
JNIEXPORT jobject JNICALL Java_com_jmupdf_JmuPdf_getOutline(JNIEnv *env, jclass obj, jlong handle)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);
	if (!hdoc)
	{
		return NULL;
	}

	jclass cls = (*env)->FindClass(env, "com/jmupdf/document/Outline");
	if (!cls)
	{
		return NULL;
	}

	jmethodID init      = (*env)->GetMethodID(env, cls, "<init>",   "()V");
	jmethodID add_next  = (*env)->GetMethodID(env, cls, "addNext",  "()Lcom/jmupdf/document/Outline;");
	jmethodID add_child = (*env)->GetMethodID(env, cls, "addChild", "()Lcom/jmupdf/document/Outline;");
	jmethodID set_page  = (*env)->GetMethodID(env, cls, "setPage",  "(I)V");
	jmethodID set_title = (*env)->GetMethodID(env, cls, "setTitle", "(Ljava/lang/String;)V");

	fz_outline *outline = NULL;
	jobject out = NULL;

	if(init > 0 && add_next > 0 && add_child > 0 && set_page > 0 && set_title > 0)
	{
		if(hdoc->pdf)
		{
			outline = pdf_load_outline(hdoc->pdf);
		}
		else if(hdoc->xps)
		{
			outline = xps_load_outline(hdoc->xps);
		}
		if (outline)
		{
			out = (*env)->NewObject(env, cls, init);
			if (out)
			{
				jni_load_outline(env, cls, out, add_next, add_child, set_page, set_title, hdoc, outline);
			}
		}
	}

	if (cls)
	{
		(*env)->DeleteLocalRef(env, cls);
	}
	if (outline && hdoc->pdf)
	{
		fz_free_outline(outline);
	}

	return out;
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

	fz_obj *info = fz_dict_gets(hdoc->pdf->trailer, "Info");
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

	if (!hdoc->pdf)
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

	data[1]  = pdf_has_permission(hdoc->pdf, PDF_PERM_PRINT); 				// print
	data[2]  = pdf_has_permission(hdoc->pdf, PDF_PERM_CHANGE); 			// modify
	data[3]  = pdf_has_permission(hdoc->pdf, PDF_PERM_COPY);				// copy
	data[4]  = pdf_has_permission(hdoc->pdf, PDF_PERM_NOTES);				// annotate
	data[5]  = pdf_has_permission(hdoc->pdf, PDF_PERM_FILL_FORM);			// Fill form fields
	data[6]  = pdf_has_permission(hdoc->pdf, PDF_PERM_ACCESSIBILITY);		// Extract text and graphics
	data[7]  = pdf_has_permission(hdoc->pdf, PDF_PERM_ASSEMBLE);			// Document assembly
	data[8]  = pdf_has_permission(hdoc->pdf, PDF_PERM_HIGH_RES_PRINT);		// Print quality
	data[9]  = pdf_get_crypt_revision(hdoc->pdf);							// Revision
	data[10] = pdf_get_crypt_length(hdoc->pdf);							// Length

	char *method = pdf_get_crypt_method(hdoc->pdf);						// Method

	if (strcmp(method, "RC4") == 0)  			data[11] = 1;
	else if (strcmp(method, "AES") == 0)  		data[11] = 2;
	else if (strcmp(method, "Unknown") == 0) 	data[11] = 3;
	else 										data[11] = 0;

	data[0] = data[11] > 0;													// Is encrypted

	(*env)->ReleaseIntArrayElements(env, dataarray, data, 0);

	return dataarray;
}
