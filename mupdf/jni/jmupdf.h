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

	pdf_xref *xref;
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

// jni_write_tiff.c
int jni_write_tif(fz_context*, fz_pixmap*, char*, float, int, int, int, int);

// jni_write_jpeg.c
int jni_write_jpeg(fz_context*, fz_pixmap*, char*, float, int, int);

// jni_write_png.c
int jni_write_png(fz_context*, fz_pixmap*, char*, int, float);

// jni_write_bmp.c
int jni_write_bmp(fz_context*, fz_pixmap*, char*, int, float);

#endif
