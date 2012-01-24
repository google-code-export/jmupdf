#include "jmupdf.h"

/**
 * Get Page Links
 *
 */
JNIEXPORT jobjectArray JNICALL Java_com_jmupdf_JmuPdf_pdfGetPageLinks(JNIEnv *env, jclass obj, jlong handle, jint pagen)
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
