#include "jmupdf.h"

/**
 * Free document resources
 */
static void jni_free_document(jni_document *doc)
{
	if (!doc)
	{
		return;
	}

	fz_context *ctx = doc->ctx;

	if (!ctx)
	{
		return;
	}

	if (doc->doc)
	{
		fz_close_document(doc->doc);
	}

	fz_locks_context *locks = ctx->locks;

	fz_free(ctx, doc);
	fz_free_context(ctx);
	jni_free_locks(locks);

	return;
}

/**
 * Create a new document
 */
static jni_document *jni_new_document(int max_store, jni_doc_type type)
{
	fz_locks_context *locks = jni_new_locks();

	if (!locks)
	{
		return NULL;
	}

	fz_context *ctx = fz_new_context(NULL, locks, max_store);

	if (!ctx)
	{
		jni_free_locks(locks);
		return NULL;
	}

	jni_document *doc = fz_malloc_no_throw(ctx, sizeof(jni_document));

	if (!doc)
	{
		fz_free_context(ctx);
		jni_free_locks(locks);
		return NULL;
	}

	doc->ctx = ctx;
	doc->doc = NULL;
	doc->type = type;

	return doc;
}

/**
 * Open a document
 */
static int jni_open_document(jni_document *doc, const char *file, char *password)
{
	fz_stream *stm = NULL;
	int rc = 0;

	fz_try(doc->ctx)
	{
		stm = fz_open_file(doc->ctx, file);
		if (doc->type == DOC_PDF)
		{
			doc->doc = (fz_document*)pdf_open_document_with_stream(stm);
		}
		else if (doc->type == DOC_XPS)
		{
			doc->doc = (fz_document*)xps_open_document_with_stream(stm);
		}
		else if (doc->type == DOC_CBZ)
		{
			doc->doc = (fz_document*)cbz_open_document_with_stream(stm);
		}
	}
	fz_always(doc->ctx)
	{
		fz_close(stm);
	}
	fz_catch(doc->ctx)
	{
		if (!stm)
		{
			rc = -1;
		}
		else if (!doc->doc)
		{
			rc = -2;
		}
	}

	if (doc->doc)
	{
		if (fz_needs_password(doc->doc))
		{
			if(!fz_authenticate_password(doc->doc, password))
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
		                     jmethodID add_next, jmethodID add_child, jmethodID set_page,
		                     jmethodID set_rect, jmethodID set_dest, jmethodID set_title,
		                     jmethodID set_type, jni_document *doc, fz_outline *outline)
{
	jstring text;
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
				float x0 = outline->dest.ld.gotor.lt.x;
				float y0 = outline->dest.ld.gotor.lt.y;
				float x1 = outline->dest.ld.gotor.rb.x;
				float y1 = outline->dest.ld.gotor.rb.y;
				jni_outline_set_page_call(obj, set_page, page);
				jni_outline_set_rect_call(obj, set_rect, x0, y0, x1, y1);
			}
			else
			{
				text = jni_new_string(buf);
				if (buf)
					jni_outline_set_destination_call(obj, set_dest, text);
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
			jni_load_outline(env, cls, new_child, add_next, add_child, set_page, set_rect, set_dest, set_title, set_type, doc, outline->down);
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
	if (handle > 0)
	{
		return (jni_document *)jni_jlong_to_ptr(handle);
	}
	return NULL;
}

/**
 * Open a document
 */
JNIEXPORT jlong JNICALL
Java_com_jmupdf_JmuPdf_open(JNIEnv *env, jclass obj, jint type, jbyteArray document, jbyteArray password, jint max_store)
{
    jni_document *doc = jni_new_document(max_store, type);

    if (!doc)
    {
    	return -1;
    }

    char * file = jni_jbyte_to_char(env, doc->ctx, document);
    char * pass = jni_jbyte_to_char(env, doc->ctx, password);

    int rc = jni_open_document(doc, (const char*)file, pass);

    fz_free(doc->ctx, file);
    fz_free(doc->ctx, pass);

    if (rc != 0)
    {
    	jni_free_document(doc);
    	return rc;
    }

    return jni_ptr_to_jlong(doc);
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
 * Get page count
 */
JNIEXPORT jint JNICALL
Java_com_jmupdf_JmuPdf_getPageCount(JNIEnv *env, jclass obj, jlong handle)
{
	jni_document *doc = jni_get_document(handle);

	if (!doc)
	{
		return -1;
	}

	int rc = -2;

	fz_try(doc->ctx)
	{
		rc = fz_count_pages(doc->doc);
	}
	fz_catch(doc->ctx) {}

	return rc;
}

/**
 * Get document version
 */
JNIEXPORT jint JNICALL
Java_com_jmupdf_JmuPdf_getVersion(JNIEnv *env, jclass obj, jlong handle)
{
	jni_document *doc = jni_get_document(handle);
	int v = 0;

	if (doc->doc && doc->type == DOC_PDF)
	{
		v = ((pdf_document*)doc->doc)->version;
	}

	return v;
}

/**
 * Get an array that has the outline of the document
 */
JNIEXPORT jobject JNICALL
Java_com_jmupdf_JmuPdf_getOutline(JNIEnv *env, jclass obj, jlong handle)
{
	jni_document *doc = jni_get_document(handle);

	if (!doc)
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
	jmethodID set_rect  = jni_get_outline_set_rect(cls);
	jmethodID set_dest  = jni_get_outline_set_destination(cls);
	jmethodID set_title = jni_get_outline_set_title(cls);
	jmethodID set_type  = jni_get_outline_set_type(cls);

	fz_outline *outline = NULL;
	jobject out = NULL;

	if(init > 0 && add_next > 0 && add_child > 0 && set_page > 0 && set_rect > 0 && set_title > 0 && set_dest > 0 && set_type > 0)
	{
		outline = fz_load_outline(doc->doc);
		if (outline)
		{
			out = jni_new_outline_obj(cls, init);
			if (out)
			{
				jni_load_outline(env, cls, out, add_next, add_child, set_page, set_rect, set_dest, set_title, set_type, doc, outline);
			}
		}
	}

	if (cls)
	{
		jni_free_ref(cls);
	}

	if (outline)
	{
		fz_free_outline(doc->ctx, outline);
	}

	return out;
}

/**
 * Get PDF information from dictionary.
 */
JNIEXPORT jstring JNICALL
Java_com_jmupdf_JmuPdf_pdfInfo(JNIEnv *env, jclass obj, jlong handle, jstring key)
{
	jni_document *doc = jni_get_document(handle);

	if (!doc)
	{
		return NULL;
	}

	if (!doc->doc)
	{
		return NULL;
	}

	pdf_obj *info = pdf_dict_gets(((pdf_document*)doc->doc)->trailer, "Info");
	char *text = NULL;

	if (info)
	{
		const char *dictkey = jni_new_char(key);
		pdf_obj *obj = pdf_dict_gets(info, (char*)dictkey);
		jni_free_char(key, dictkey);
		if (!obj)
		{
			return NULL;
		}
		text = pdf_to_utf8(doc->ctx, obj);
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
	jni_document *doc = jni_get_document(handle);

	if (!doc)
	{
		return NULL;
	}

	if (!doc->doc)
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

	data[1]  = pdf_has_permission(((pdf_document*)doc->doc), PDF_PERM_PRINT); 			// print
	data[2]  = pdf_has_permission(((pdf_document*)doc->doc), PDF_PERM_CHANGE); 			// modify
	data[3]  = pdf_has_permission(((pdf_document*)doc->doc), PDF_PERM_COPY);			// copy
	data[4]  = pdf_has_permission(((pdf_document*)doc->doc), PDF_PERM_NOTES);			// annotate
	data[5]  = pdf_has_permission(((pdf_document*)doc->doc), PDF_PERM_FILL_FORM);		// Fill form fields
	data[6]  = pdf_has_permission(((pdf_document*)doc->doc), PDF_PERM_ACCESSIBILITY);	// Extract text and graphics
	data[7]  = pdf_has_permission(((pdf_document*)doc->doc), PDF_PERM_ASSEMBLE);		// Document assembly
	data[8]  = pdf_has_permission(((pdf_document*)doc->doc), PDF_PERM_HIGH_RES_PRINT);	// Print quality
	data[9]  = pdf_crypt_revision(((pdf_document*)doc->doc));							// Revision
	data[10] = pdf_crypt_length(((pdf_document*)doc->doc));								// Length

	char *method = pdf_crypt_method(((pdf_document*)doc->doc));							// Method

	if (strcmp(method, "RC4") == 0)  			data[11] = 1;
	else if (strcmp(method, "AES") == 0)  		data[11] = 2;
	else if (strcmp(method, "Unknown") == 0) 	data[11] = 3;
	else 										data[11] = 0;

	data[0] = data[11] > 0;																// Is encrypted

	jni_release_int_array(dataarray, data);

	return dataarray;
}
