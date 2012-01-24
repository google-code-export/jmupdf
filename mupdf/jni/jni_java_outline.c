#include "jmupdf.h"

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
		if(hdoc->xref)
		{
			outline = pdf_load_outline(hdoc->xref);
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
	if (outline && hdoc->xref)
	{
		fz_free_outline(outline);
	}

	return out;
}
