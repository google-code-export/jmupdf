#ifndef JMuPDF_H_
#define JMuPDF_H_

#include <stdio.h>
#include <stdint.h>

#include "fitz.h"
#include "mupdf.h"
#include "muxps.h"
#include "jni.h"

// Define JMuPdf internal version
#define JMUPDF_VERSION "0.3.1-beta"

// Document handle
typedef struct jni_doc_handle_s jni_doc_handle;
struct jni_doc_handle_s {
	jlong handle;
	fz_context *ctx;

	pdf_document *pdf;
	xps_document *xps;

	pdf_page *pdf_page;
	xps_page *xps_page;

	fz_rect page_bbox;
	fz_display_list *page_list;

	int page_number;
	int anti_alias_level;
};

// Pointer conversions
#define jni_jlong_to_ptr(a) ((void *)(uintptr_t)(a))
#define jni_ptr_to_jlong(a) ((jlong)(uintptr_t)(a))

// Color constants
static const int COLOR_RGB = 1;
static const int COLOR_ARGB = 2;
static const int COLOR_GRAY_SCALE = 10;
static const int COLOR_BLACK_WHITE = 12;
static const int COLOR_BLACK_WHITE_DITHER = 121;

// Default DPI
static const int DEFAULT_DPI = 72;

// ARGB macros
#define jni_get_a(P) ((P & 0xff) << 24)
#define jni_get_r(P) ((P & 0xff) << 16)
#define jni_get_g(P) ((P & 0xff) <<  8)
#define jni_get_b(P) ((P & 0xff))

// DPI conversion macros
#define jni_to_96_dpi(P) (P*1.3334)
#define jni_to_72_dpi(P) (P*.75)

// Calculate resolution based on zoom factor
#define jni_resolution(Z) (Z*DEFAULT_DPI)

// jni_handles.c
jni_doc_handle *jni_new_doc_handle(int);
jni_doc_handle *jni_get_doc_handle(jlong);
int jni_free_doc_handle(jlong);

// jni_java_page.c
void jni_get_doc_page(jni_doc_handle*, int);
void jni_free_page(jni_doc_handle*);

// jni_java_pixmap.c
fz_matrix jni_get_view_ctm(jni_doc_handle*, float, int);
int jni_pix_to_black_white(fz_context*, fz_pixmap*, int, unsigned char* );
int jni_pix_to_binary(fz_context*, fz_pixmap*, int, unsigned char*);

// jni_write_xxx.c
int jni_write_png(fz_context*, fz_pixmap*, const char*, float, int);
int jni_write_tif(fz_context*, fz_pixmap*, const char*, float, int, int, int, int);
int jni_write_jpg(fz_context*, fz_pixmap*, const char*, float, int, int);
int jni_write_bmp(fz_context*, fz_pixmap*, const char*, float, int);

// JNI Strong Typing
#define jni_new_char(str) (*env)->GetStringUTFChars(env, str, 0);
#define jni_free_char(str, chars) (*env)->ReleaseStringUTFChars(env, str, chars);
#define jni_start_array_critical(array) (*env)->GetPrimitiveArrayCritical(env, array, 0);
#define jni_end_array_critical(array, carray) (*env)->ReleasePrimitiveArrayCritical(env, array, carray, 0);
#define jni_new_string(chars) (*env)->NewStringUTF(env, chars);
#define jni_new_byte_array(size) (*env)->NewByteArray(env, size);
#define jni_new_int_array(size) (*env)->NewIntArray(env, size);
#define jni_new_float_array(size) (*env)->NewFloatArray(env, size);
#define jni_new_object_array(size, cls) (*env)->NewObjectArray(env, size, cls, NULL);
#define jni_set_object_array_el(array, idx, obj) (*env)->SetObjectArrayElement(env, array, idx, obj);
#define jni_free_ref(cls) (*env)->DeleteLocalRef(env, cls);

// Outline class and methods: Strong Typing
#define jni_new_outline_class() (*env)->FindClass(env, "com/jmupdf/document/Outline");
#define jni_new_outline_obj(cls, method) (*env)->NewObject(env, cls, method);
#define jni_get_outline_init(cls) (*env)->GetMethodID(env, cls, "<init>",   "()V");
#define jni_get_outline_add_next(cls) (*env)->GetMethodID(env, cls, "addNext",  "()Lcom/jmupdf/document/Outline;");
#define jni_get_outline_add_child(cls) (*env)->GetMethodID(env, cls, "addChild", "()Lcom/jmupdf/document/Outline;");
#define jni_get_outline_set_page(cls) (*env)->GetMethodID(env, cls, "setPage",  "(I)V");
#define jni_get_outline_set_title(cls) (*env)->GetMethodID(env, cls, "setTitle", "(Ljava/lang/String;)V");
#define jni_outline_set_title_call(obj, method, string) (*env)->CallVoidMethod(env, obj, method, string);
#define jni_outline_set_page_call(obj, method, page) (*env)->CallVoidMethod(env, obj, method, page);
#define jni_outline_add_child_call(obj, method) (*env)->CallObjectMethod(env, obj, method);
#define jni_outline_add_next_call(obj, method) (*env)->CallObjectMethod(env, obj, method);

// Page class and methods: Strong Typing
#define jni_new_page_text_class() (*env)->FindClass(env, "com/jmupdf/page/PageText");
#define jni_new_page_text_obj(cls, method, x0, y0, x1, y1, eol, text) (*env)->NewObject(env, cls, method, x0, y0, x1, y1, eol, text);
#define jni_get_page_text_init(cls) (*env)->GetMethodID(env, cls, "<init>", "(IIIII[I)V");

// Page links and methods: Strong Typing
#define jni_new_page_links_class() (*env)->FindClass(env, "com/jmupdf/page/PageLinks");
#define jni_new_page_links_obj(cls, method, x0, y0, x1, y1, eol, text) (*env)->NewObject(env, cls, method, x0, y0, x1, y1, type, text);
#define jni_get_page_links_init(cls) (*env)->GetMethodID(env, cls, "<init>", "(FFFFILjava/lang/String;)V");

#endif
