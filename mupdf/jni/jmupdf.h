#ifndef JMuPDF_H_
#define JMuPDF_H_

#include <stdio.h>
#include <stdint.h>

#include "fitz.h"
#include "mupdf.h"
#include "muxps.h"
#include "mucbz.h"
#include "jni.h"

// Define JMuPdf internal version
#define JMUPDF_VERSION "0.3.1-beta"

// Pointer conversions for x86 and x64
#define jni_jlong_to_ptr(a) ((void *)(uintptr_t)(a))
#define jni_ptr_to_jlong(a) ((jlong)(uintptr_t)(a))

// Color types
typedef enum jni_color_types
{
	COLOR_RGB = 1,
	COLOR_ARGB = 2,
	COLOR_ARGB_PRE = 3,
	COLOR_BGR = 4,
	COLOR_GRAY_SCALE = 10,
	COLOR_BLACK_WHITE = 12,
	COLOR_BLACK_WHITE_DITHER = 121
} jni_color_type;

// Document types
typedef enum jni_doc_types
{
	DOC_PDF = 0,
	DOC_XPS = 1,
	DOC_CBZ = 2
} jni_doc_type;

// Document Structure
typedef struct jni_document_s jni_document;
struct jni_document_s {
	fz_context *ctx;
	fz_document *doc;

	fz_page *page;
	fz_display_list *page_list;
	fz_rect page_bbox;

	int page_number;
	int anti_alias_level;
	jni_doc_type doc_type;
};

// Default DPI
static const int DEFAULT_DPI = 72;

// RGB macros
#define jni_get_rgb_a(P) ((P & 0xff) << 24)
#define jni_get_rgb_r(P) ((P & 0xff) << 16)
#define jni_get_rgb_g(P) ((P & 0xff) <<  8)
#define jni_get_rgb_b(P) ((P & 0xff))

// BGR macros
#define jni_get_bgr_b(P) ((P & 0xff) << 16)
#define jni_get_bgr_g(P) ((P & 0xff) <<  8)
#define jni_get_bgr_r(P) ((P & 0xff))

// Calculate resolution based on zoom factor
#define jni_resolution(Z) (Z*DEFAULT_DPI)

// jni_java_document.c
jni_document *jni_get_document(jlong);

// jni_java_page.c
void jni_get_page(jni_document*, int);
void jni_free_page(jni_document*);

// jni_java_pixmap.c
char * jni_jbyte_to_char(JNIEnv*, jni_document*, jbyteArray);
fz_matrix jni_get_view_ctm(jni_document*, float, int);
int jni_pix_to_black_white(fz_context*, fz_pixmap*, int, unsigned char* );
int jni_pix_to_binary(fz_context*, fz_pixmap*, int, unsigned char*);

// jni_write_xxx.c
int jni_write_png(fz_context*, fz_pixmap*, const char*, float, int);
int jni_write_tif(fz_context*, fz_pixmap*, const char*, float, int, int, int, int);
int jni_write_jpg(fz_context*, fz_pixmap*, const char*, float, int, int);
int jni_write_bmp(fz_context*, fz_pixmap*, const char*, float, int);

// JNI String
#define jni_new_char(str) (*env)->GetStringUTFChars(env, str, 0);
#define jni_free_char(str, chars) (*env)->ReleaseStringUTFChars(env, str, chars);

// JNI Get/ReleaseXXXArrayElements()
#define jni_get_int_array(array) (*env)->GetIntArrayElements(env, array, 0);
#define jni_release_int_array(array, elem) (*env)->ReleaseIntArrayElements(env, array, elem, 0);
#define jni_get_float_array(array) (*env)->GetFloatArrayElements(env, array, 0);
#define jni_release_float_array(array, elem) (*env)->ReleaseFloatArrayElements(env, array, elem, 0);
#define jni_get_char_array(array) (*env)->GetCharArrayElements(env, array, 0);
#define jni_release_char_array(array, elem) (*env)->ReleaseCharArrayElements(env, array, elem, 0);
#define jni_get_byte_array(array) (*env)->GetByteArrayElements(env, array, 0);
#define jni_release_byte_array(array, elem) (*env)->ReleaseByteArrayElements(env, array, elem, 0);
#define jni_get_array_len(array) (*env)->GetArrayLength(env, array);

// JNI GET/ReleasePrimitiveArrayCritical() <== Not good for GC!!
#define jni_start_array_critical(array) (*env)->GetPrimitiveArrayCritical(env, array, 0);
#define jni_end_array_critical(array, carray) (*env)->ReleasePrimitiveArrayCritical(env, array, carray, 0);

// JNI NewXXXArray()
#define jni_new_byte_array(size) (*env)->NewByteArray(env, size);
#define jni_new_int_array(size) (*env)->NewIntArray(env, size);
#define jni_new_float_array(size) (*env)->NewFloatArray(env, size);
#define jni_new_object_array(size, cls) (*env)->NewObjectArray(env, size, cls, NULL);
#define jni_new_string(chars) (*env)->NewStringUTF(env, chars);

#define jni_set_object_array_el(array, idx, obj) (*env)->SetObjectArrayElement(env, array, idx, obj);
#define jni_free_ref(cls) (*env)->DeleteLocalRef(env, cls);

// JNI ByteBuffer
#define jni_new_buffer_direct(mem, len) (*env)->NewDirectByteBuffer(env, mem, len)
#define jni_get_buffer_address(buf) (*env)->GetDirectBufferAddress(env, buf)
#define jni_get_buffer_capacity(buf) (*env)->GetDirectBufferCatpacity(env, buf)

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
