#include "jmupdf.h"

/**
 * Create a new document
 */
static jni_document *jni_new_document(int max_store, jni_doc_type type)
{
	fz_context *ctx = fz_new_context(NULL, NULL, max_store);

	if (!ctx)
	{
		return NULL;
	}

	jni_document *hdoc = fz_malloc_no_throw(ctx, sizeof(jni_document));

	if (!hdoc)
	{
		fz_free_context(ctx);
		return NULL;
	}

	hdoc->ctx = ctx;
	hdoc->doc = NULL;
	hdoc->page = NULL;
	hdoc->page_list = NULL;
	hdoc->page_number = 0;
	hdoc->doc_type = type;

	hdoc->anti_alias_level = fz_get_aa_level(hdoc->ctx);

	return hdoc;
}

/**
 * Free document resources
 */
static void jni_free_document(jni_document *hdoc)
{
	if (!hdoc)
	{
		return;
	}

	fz_context *ctx = hdoc->ctx;

	if (!ctx)
	{
		return;
	}

	jni_free_page(hdoc);

	if (hdoc->ctx->glyph_cache)
	{
		fz_drop_glyph_cache_context(hdoc->ctx);
	}

	if (hdoc->doc)
	{
		fz_close_document(hdoc->doc);
	}

	fz_free(ctx, hdoc);
	fz_free_context(ctx);

	return;
}

/**
 * Open a document
 */
static int jni_open_document(jni_document *hdoc, const char *file, char *password)
{
	fz_stream *stm = NULL;
	int rc = 0;

	fz_try(hdoc->ctx)
	{
		stm = fz_open_file(hdoc->ctx, file);
		if (hdoc->doc_type == DOC_PDF)
		{
			hdoc->doc = (fz_document*)pdf_open_document_with_stream(stm);
		}
		else if (hdoc->doc_type == DOC_XPS)
		{
			hdoc->doc = (fz_document*)xps_open_document_with_stream(stm);
		}
		else if (hdoc->doc_type == DOC_CBZ)
		{
			hdoc->doc = (fz_document*)cbz_open_document_with_stream(stm);
		}
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
		else if (!hdoc->doc)
		{
			rc = -2;
		}
	}

	if (hdoc->doc)
	{
		if (fz_needs_password(hdoc->doc))
		{
			if(!fz_authenticate_password(hdoc->doc, password))
			{
				rc = -3;
			}
		}
	}

	return rc;
}

/**
 * Load outline to PdfOutline object structure
 */
static void jni_load_outline(JNIEnv *env, jclass cls, jobject obj,
		                     jmethodID add_next, jmethodID add_child,
		                     jmethodID set_page, jmethodID set_dest, jmethodID set_title,
		                     jmethodID set_type, jni_document *hdoc, fz_outline *outline)
{
	char *buf;
	int type;
	int page;
	while (outline)
	{
		switch (outline->dest.kind) {
			case FZ_LINK_GOTO:
				type = 1;
				page = outline->dest.ld.gotor.page + 1;
				break;
			case FZ_LINK_URI:
				type = 2;
				buf = outline->dest.ld.uri.uri;
				break;
			case FZ_LINK_LAUNCH:
				type = 3;
				buf = outline->dest.ld.launch.file_spec;
				break;
			case FZ_LINK_NAMED:
				type = 4;
				buf = outline->dest.ld.named.named;
				break;
			case FZ_LINK_GOTOR:
				type = 5;
				buf = outline->dest.ld.gotor.file_spec;
				break;
			default:
				type = 0;
				break;
		}
		if (type > 0)
		{
			jni_outline_set_type_call(obj, set_type, type);
			if (type == 1)
			{
				jni_outline_set_page_call(obj, set_page, page);
			}
			else
			{
				jni_outline_set_destination_call(obj, set_dest, buf);
			}
		}
		if (outline->title)
		{
			jstring title = jni_new_string(outline->title);
			jni_outline_set_title_call(obj, set_title, title);
		}
		if (outline->down)
		{
			jobject new_child = jni_outline_add_child_call(obj, add_child);
			jni_load_outline(env, cls, new_child, add_next, add_child, set_page, set_dest, set_title, set_type, hdoc, outline->down);
		}
		outline = outline->next;
		if (outline)
		{
			obj = jni_outline_add_next_call(obj, add_next);
		}
	}
}

/**
 * Get document from pointer
 */
jni_document *jni_get_document(jlong handle)
{
	return (jni_document *)jni_jlong_to_ptr(handle);
}

/**
 * Open a document
 */
JNIEXPORT jlong JNICALL
Java_com_jmupdf_JmuPdf_open(JNIEnv *env, jclass obj, jint type, jbyteArray document, jbyteArray password, jint max_store)
{
    jni_document *hdoc = jni_new_document(max_store, type);

    if (!hdoc)
    {
            return -1;
    }

    char * file = jni_jbyte_to_char(env, hdoc, document);
    char * pass = jni_jbyte_to_char(env, hdoc, password);

    int rc = jni_open_document(hdoc, (const char*)file, pass);

    fz_free(hdoc->ctx, file);
    fz_free(hdoc->ctx, pass);

    if (rc != 0)
    {
            jni_free_document(hdoc);
            return rc;
    }

    return jni_ptr_to_jlong(hdoc);
}

/**
 * Close a document and free resources
 */
JNIEXPORT void JNICALL
Java_com_jmupdf_JmuPdf_close(JNIEnv *env, jclass obj, jlong handle)
{
	jni_free_document(jni_get_document(handle));
}

/**
 * Get document version
 */
JNIEXPORT jint JNICALL
Java_com_jmupdf_JmuPdf_getVersion(JNIEnv *env, jclass obj, jlong handle)
{
	jni_document *hdoc = jni_get_document(handle);
	int v = 0;

	if (hdoc->doc && hdoc->doc_type == DOC_PDF)
	{
		v = ((pdf_document*)hdoc->doc)->version;
	}

	return v;
}

/**
 * Get an array that has the outline of the document
 */
JNIEXPORT jobject JNICALL
Java_com_jmupdf_JmuPdf_getOutline(JNIEnv *env, jclass obj, jlong handle)
{
	jni_document *hdoc = jni_get_document(handle);

	if (!hdoc)
	{
		return NULL;
	}

	jclass cls = jni_new_outline_class();

	if (!cls)
	{
		return NULL;
	}

	jmethodID init      = jni_get_outline_init(cls);
	jmethodID add_next  = jni_get_outline_add_next(cls);
	jmethodID add_child = jni_get_outline_add_child(cls);
	jmethodID set_page  = jni_get_outline_set_page(cls);
	jmethodID set_dest  = jni_get_outline_set_destination(cls);
	jmethodID set_title = jni_get_outline_set_title(cls);
	jmethodID set_type  = jni_get_outline_set_type(cls);

	fz_outline *outline = NULL;
	jobject out = NULL;

	if(init > 0 && add_next > 0 && add_child > 0 && set_page > 0 && set_title > 0 && set_dest > 0 && set_type > 0)
	{
		outline = fz_load_outline(hdoc->doc);
		if (outline)
		{
			out = jni_new_outline_obj(cls, init);
			if (out)
			{
				jni_load_outline(env, cls, out, add_next, add_child, set_page, set_dest, set_title, set_type, hdoc, outline);
			}
		}
	}

	if (cls)
	{
		jni_free_ref(cls);
	}

	if (outline)
	{
		fz_free_outline(hdoc->ctx, outline);
	}

	return out;
}

/**
 * Get PDF information from dictionary.
 */
JNIEXPORT jstring JNICALL
Java_com_jmupdf_JmuPdf_pdfInfo(JNIEnv *env, jclass obj, jlong handle, jstring key)
{
	jni_document *hdoc = jni_get_document(handle);

	if (!hdoc)
	{
		return NULL;
	}

	if (!hdoc->doc)
	{
		return NULL;
	}

	fz_obj *info = fz_dict_gets(((pdf_document*)hdoc->doc)->trailer, "Info");
	char *text = NULL;

	if (info)
	{
		const char *dictkey = jni_new_char(key);
		fz_obj *obj = fz_dict_gets(info, (char*)dictkey);
		jni_free_char(key, dictkey);
		if (!obj)
		{
			return NULL;
		}
		text = pdf_to_utf8(hdoc->ctx, obj);
	}

	jstring str = jni_new_string(text);

	return str;
}

/**
 * Get PDF encryption information
 */
JNIEXPORT jintArray JNICALL
Java_com_jmupdf_JmuPdf_pdfEncryptInfo(JNIEnv *env, jclass obj, jlong handle)
{
	jni_document *hdoc = jni_get_document(handle);

	if (!hdoc)
	{
		return NULL;
	}

	if (!hdoc->doc)
	{
		return NULL;
	}

	int sizeofarray = 12;

	jintArray dataarray = jni_new_int_array(sizeofarray);

	if (!dataarray)
	{
		return NULL;
	}

	jint *data = jni_get_int_array(dataarray);

	data[1]  = pdf_has_permission(((pdf_document*)hdoc->doc), PDF_PERM_PRINT); 			// print
	data[2]  = pdf_has_permission(((pdf_document*)hdoc->doc), PDF_PERM_CHANGE); 		// modify
	data[3]  = pdf_has_permission(((pdf_document*)hdoc->doc), PDF_PERM_COPY);			// copy
	data[4]  = pdf_has_permission(((pdf_document*)hdoc->doc), PDF_PERM_NOTES);			// annotate
	data[5]  = pdf_has_permission(((pdf_document*)hdoc->doc), PDF_PERM_FILL_FORM);		// Fill form fields
	data[6]  = pdf_has_permission(((pdf_document*)hdoc->doc), PDF_PERM_ACCESSIBILITY);	// Extract text and graphics
	data[7]  = pdf_has_permission(((pdf_document*)hdoc->doc), PDF_PERM_ASSEMBLE);		// Document assembly
	data[8]  = pdf_has_permission(((pdf_document*)hdoc->doc), PDF_PERM_HIGH_RES_PRINT);	// Print quality
	data[9]  = pdf_get_crypt_revision(((pdf_document*)hdoc->doc));						// Revision
	data[10] = pdf_get_crypt_length(((pdf_document*)hdoc->doc));						// Length

	char *method = pdf_get_crypt_method(((pdf_document*)hdoc->doc));					// Method

	if (strcmp(method, "RC4") == 0)  			data[11] = 1;
	else if (strcmp(method, "AES") == 0)  		data[11] = 2;
	else if (strcmp(method, "Unknown") == 0) 	data[11] = 3;
	else 										data[11] = 0;

	data[0] = data[11] > 0;																// Is encrypted

	jni_release_int_array(dataarray, data);

	return dataarray;
}
