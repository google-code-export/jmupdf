#ifndef JMuPDF_H_
#define JMuPDF_H_

#include <stdio.h>
#include <stdint.h>

#include "fitz-internal.h"
#include "mupdf-internal.h"
#include "muxps-internal.h"
#include "mucbz.h"

#ifdef __linux__
#include "linux/jni.h"
#elif __APPLE__
#include "mac/jni.h"
#elif __WIN32__
#include "win/jni.h"
#endif

// Define JMuPdf internal version
#define JMUPDF_VERSION "0.5.0"

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

// Page rendering options
typedef struct jni_options_s jni_options;

// Document structure
typedef struct jni_document_s jni_document;
struct jni_document_s
{
	fz_context *ctx;
	fz_document *doc;
	jni_doc_type type;
};

// Page structure
typedef struct jni_page_s jni_page;
struct jni_page_s
{
	fz_context *ctx;
	jni_document *doc;
	jni_options *options;
	fz_page *page;
	fz_display_list *list;
	fz_rect bbox;
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

// jni_concurrent.c
fz_locks_context * jni_new_locks();
void jni_free_locks(fz_locks_context*);
void jni_lock(fz_context*);
void jni_unlock(fz_context*);

// jni_java_document.c
jni_document *jni_get_document(jlong);

// jni_java_page.c
jni_page *jni_get_page(jlong);

// jni_java_pixmap.c
jni_options * jni_new_options(fz_context*);
char * jni_jbyte_to_char(JNIEnv*, fz_context*, jbyteArray);
fz_matrix jni_get_view_ctm(float, int);
int jni_pix_to_black_white(fz_context*, fz_pixmap*, int, unsigned char* );
int jni_pix_to_binary(fz_context*, fz_pixmap*, int, unsigned char*);

// jni_write_xxx.c
void * jni_write_png(JNIEnv*, fz_context*, fz_pixmap*, const char*, int, float);
void * jni_write_jpg(JNIEnv*, fz_context*, fz_pixmap*, const char*, float, int, int);
int jni_write_tif(fz_context*, fz_pixmap*, const char*, float, int, int, int, int);
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

// DocumentOutline class and methods: Strong Typing
#define jni_new_outline_class() (*env)->FindClass(env, "com/jmupdf/document/DocumentOutline");
#define jni_new_outline_obj(cls, method) (*env)->NewObject(env, cls, method);
#define jni_get_outline_init(cls) (*env)->GetMethodID(env, cls, "<init>",   "()V");
#define jni_get_outline_add_next(cls) (*env)->GetMethodID(env, cls, "addNext",  "()Lcom/jmupdf/document/DocumentOutline;");
#define jni_get_outline_add_child(cls) (*env)->GetMethodID(env, cls, "addChild", "()Lcom/jmupdf/document/DocumentOutline;");
#define jni_get_outline_set_page(cls) (*env)->GetMethodID(env, cls, "setPage",  "(I)V");
#define jni_get_outline_set_rect(cls) (*env)->GetMethodID(env, cls, "setRect",  "(FFFF)V");
#define jni_get_outline_set_title(cls) (*env)->GetMethodID(env, cls, "setTitle", "(Ljava/lang/String;)V");
#define jni_get_outline_set_type(cls) (*env)->GetMethodID(env, cls, "setType", "(I)V");
#define jni_get_outline_set_destination(cls) (*env)->GetMethodID(env, cls, "setDestination", "(Ljava/lang/String;)V");
#define jni_outline_set_title_call(obj, method, string) (*env)->CallVoidMethod(env, obj, method, string);
#define jni_outline_set_type_call(obj, method, type) (*env)->CallVoidMethod(env, obj, method, type);
#define jni_outline_set_page_call(obj, method, page) (*env)->CallVoidMethod(env, obj, method, page);
#define jni_outline_set_rect_call(obj, method, x0, y0, x1, y1) (*env)->CallVoidMethod(env, obj, method, x0, y0, x1, y1);
#define jni_outline_set_destination_call(obj, method, dest) (*env)->CallVoidMethod(env, obj, method, dest);
#define jni_outline_add_child_call(obj, method) (*env)->CallObjectMethod(env, obj, method);
#define jni_outline_add_next_call(obj, method) (*env)->CallObjectMethod(env, obj, method);

// PageText class and methods: Strong Typing
#define jni_new_page_text_class() (*env)->FindClass(env, "com/jmupdf/page/PageText");
#define jni_new_page_text_obj(cls, method, x0, y0, x1, y1, eol, text) (*env)->NewObject(env, cls, method, x0, y0, x1, y1, eol, text);
#define jni_get_page_text_init(cls) (*env)->GetMethodID(env, cls, "<init>", "(FFFFI[I)V");

// Page links and methods: Strong Typing
#define jni_new_page_links_class() (*env)->FindClass(env, "com/jmupdf/page/PageLinks");
#define jni_new_page_links_obj(cls, method, x0, y0, x1, y1, eol, text) (*env)->NewObject(env, cls, method, x0, y0, x1, y1, type, text);
#define jni_get_page_links_init(cls) (*env)->GetMethodID(env, cls, "<init>", "(FFFFILjava/lang/String;)V");

#endif
