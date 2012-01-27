#include "jmupdf.h"

/**
 * Get Current Transformation Matrix
 */
fz_matrix jni_get_view_ctm(jni_doc_handle *hdoc, float zoom, int rotate)
{
	fz_matrix ctm = fz_identity;
	float z = zoom;

	if (hdoc->xps)
	{
		z = jni_to_72_dpi(zoom);
	}

	ctm = fz_scale(z, z);
	ctm = fz_concat(ctm, fz_rotate(rotate));

	return ctm;
}

/**
 * Normalize rectangle bounds
 *
 */
static fz_rect jni_normalize_rect(jni_doc_handle *hdoc, float x0, float y0, float x1, float y1)
{
	fz_rect rect;
	if (hdoc->pdf_page || hdoc->xps_page)
	{
		if (x0==0 && y0==0 && x1==0 && y1==0)
		{
			rect.x0 = hdoc->pdf_page ? hdoc->page_bbox.x0 : jni_to_96_dpi(hdoc->page_bbox.x0);
			rect.y0 = hdoc->pdf_page ? hdoc->page_bbox.y0 : jni_to_96_dpi(hdoc->page_bbox.y0);
			rect.x1 = hdoc->pdf_page ? hdoc->page_bbox.x1 : jni_to_96_dpi(hdoc->page_bbox.x1);
			rect.y1 = hdoc->pdf_page ? hdoc->page_bbox.y1 : jni_to_96_dpi(hdoc->page_bbox.y1);
		}
		else
		{
			rect.x0 = MAX(hdoc->pdf_page ? x0 : jni_to_96_dpi(x0),
						  hdoc->pdf_page ? hdoc->page_bbox.x0 : jni_to_96_dpi(hdoc->page_bbox.x0));
			rect.y0 = MAX(hdoc->pdf_page ? y0 : jni_to_96_dpi(y0),
						  hdoc->pdf_page ? hdoc->page_bbox.y0 : jni_to_96_dpi(hdoc->page_bbox.y0));
			rect.x1 = MIN(hdoc->pdf_page ? x1 : jni_to_96_dpi(x1),
						  hdoc->pdf_page ? hdoc->page_bbox.x1 : jni_to_96_dpi(hdoc->page_bbox.x1));
			rect.y1 = MIN(hdoc->pdf_page ? y1 : jni_to_96_dpi(y1),
						  hdoc->pdf_page ? hdoc->page_bbox.y1 : jni_to_96_dpi(hdoc->page_bbox.y1));
		}
	}
	return rect;
}

/**
 * Get an RGB, ARGB, Gray scale pixel data
 *
 */
static fz_pixmap *jni_get_pixmap(jni_doc_handle *hdoc, int pagen, float zoom, int rotate, int color, float gamma, float x0, float y0, float x1, float y1)
{
	fz_matrix ctm;
	fz_bbox bbox;
	fz_colorspace *colorspace;
	fz_pixmap *pix = NULL;
	fz_device *dev = NULL;

	//  Set anti alias level
	fz_set_aa_level(hdoc->ctx, hdoc->anti_alias_level);

	// Get page object
	jni_get_doc_page(hdoc, pagen);

	if (!hdoc->pdf_page && !hdoc->xps_page)
	{
		return NULL;
	}

	// Get Current Transformation Matrix
	if (rotate == -1)
	{
		rotate = 0;
		if (hdoc->xref)
		{
			rotate = hdoc->pdf_page->rotate;
		}
	}
	ctm = jni_get_view_ctm(hdoc, zoom, rotate);

	// Create new bounding box for page
	bbox = fz_round_rect(fz_transform_rect(ctm, jni_normalize_rect(hdoc, x0, y0, x1, y1)));

	// Set color space
	if (color == COLOR_RGB || color == COLOR_ARGB)
	{
		colorspace = fz_device_rgb;
	}
	else
	{
		colorspace = fz_device_gray;
	}

	// Try to get a pixmap buffer
	fz_try(hdoc->ctx)
	{
		pix = fz_new_pixmap_with_rect(hdoc->ctx, colorspace, bbox);
	}
	fz_catch(hdoc->ctx)
	{
		fz_drop_pixmap(hdoc->ctx, pix);
	}

	if (!pix)
	{
		return NULL;
	}
	if (!pix->samples)
	{
		fz_drop_pixmap(hdoc->ctx, pix);
		return NULL;
	}

	// Set background color
	if (color == COLOR_ARGB)
	{
		fz_clear_pixmap(pix);
	}
	else
	{
		fz_clear_pixmap_with_color(pix, 255);
	}

	// Render image
	fz_try(hdoc->ctx)
	{
		if (!hdoc->ctx->glyph_cache)
		{
			fz_new_glyph_cache_context(hdoc->ctx);
		}
		dev = fz_new_draw_device(hdoc->ctx, pix);
		fz_execute_display_list(hdoc->page_list, dev, ctm, bbox, NULL);
	}
	fz_always(hdoc->ctx)
	{
		fz_free_device(dev);
	}
	fz_catch(hdoc->ctx)
	{
		fz_drop_pixmap(hdoc->ctx, pix);
		pix = NULL;
	}

	// Set gamma correction
	if (gamma != 1 && gamma > 0)
	{
		fz_gamma_pixmap(pix, gamma);
	}

	// Set alpha
	if (color == COLOR_ARGB)
	{
		fz_unmultiply_pixmap(pix);
	}

	return pix;
}

/**
 * Convert pixels to black and white image
 * with optional dithering.
 */
int jni_pix_to_black_white(fz_context *ctx, fz_pixmap * pix, int dither, unsigned char * trgbuf)
{
	int size = pix->w * pix->h;
	unsigned char *pixbuf = (unsigned char*)fz_malloc_no_throw(ctx, (size_t)size);

	if (!pixbuf)
	{
		return -1;
	}

	unsigned char *srcbuf = pixbuf;
	unsigned char *ptrsrc = pixbuf;
	unsigned char *ptrstr = pixbuf;
	unsigned char *pixels = pix->samples;
	float value, qerror;
	int threshold = 128;
	int stride, x, y;

	// Create a gray scale image
	for (x = 0; x < size; x++)
	{
		*srcbuf++ = jni_get_r(*pixels) |
				    jni_get_g(*pixels) |
				    jni_get_b(*pixels);
		pixels += pix->n;
	}

	for (y = 0; y < pix->h; y++)
	{
		for (x = 0; x < pix->w; x++)
		{
			//  Get gray value
			value = *ptrsrc++;

			// Threshold value
			*trgbuf++ = value < threshold ? 0 : 255;

			// Spread error amongst neighboring pixels
			// Based on Floyd-Steinberg Dithering
			// http://en.wikipedia.org/wiki/Floyd-Steinberg_dithering
			if (dither)
			{
				if((x > 0) && (y > 0) && (x < (pix->w-1)) && (y < (pix->h-1)))
				{
					// Compute quantization error
					qerror = value < threshold ? value : (value-255);

					stride = y * pix->w;

					// 7/16 = 0.4375f
					srcbuf = ptrstr + x + 1 + stride;
					value = *srcbuf;
					*srcbuf = CLAMP(roundf(value + 0.4375f * qerror), 0, 255);

					// 3/16 = 0.1875f
					srcbuf = ptrstr + x - 1 + stride + pix->w;
					value = *srcbuf;
					*srcbuf = CLAMP(roundf(value + 0.1875f * qerror), 0, 255);

					// 5/16 = 0.3125f
					srcbuf = ptrstr + x + stride + pix->w;
					value = *srcbuf;
					*srcbuf = CLAMP(roundf(value + 0.3125f * qerror), 0, 255);

					// 1/16 = 0.0625f
					srcbuf = ptrstr + x + 1 + stride + pix->w;
					value = *srcbuf;
					*srcbuf = CLAMP(roundf(value + 0.0625f * qerror), 0, 255);
				}
			}
		}
	}
	fz_free(ctx, pixbuf);
	return 0;
}

/**
 * Convert pixels to packed binary image
 * with optional dithering.
 */
int jni_pix_to_binary(fz_context *ctx, fz_pixmap * pix, int dither, unsigned char * trgbuf)
{
	int size = pix->w * pix->h;
	unsigned char *pixbuf = (unsigned char*)fz_malloc_no_throw(ctx, (size_t)size);

	if (!pixbuf)
	{
		return -1;
	}

	unsigned char *srcbuf = pixbuf;
	unsigned char *ptrsrc = pixbuf;
	unsigned char *ptrstr = pixbuf;
	unsigned char *pixels = pix->samples;
	unsigned char bitpack = 0;
	float value, qerror;
	int threshold = 128;
	int bitcnt = 7;
	int stride, x, y;

	// Create a gray scale image
	for (x = 0; x < size; x++)
	{
		*srcbuf++ = jni_get_r(*pixels) |
				    jni_get_g(*pixels) |
				    jni_get_b(*pixels);
		pixels += pix->n;
	}

	for (y = 0; y < pix->h; y++)
	{
		for (x = 0; x < pix->w; x++)
		{
			 // Grab gray value
			value = *ptrsrc++;

			// Convert to binary and Pack bits
			bitpack |= (value < threshold) << bitcnt; //(7-(bitcnt%8));
			if (bitcnt-- == 0) {
				*trgbuf++ = bitpack;
				bitpack = 0;
				bitcnt = 7;
			}

			// Spread error amongst neighboring pixels
			// Based on Floyd-Steinberg Dithering
			// http://en.wikipedia.org/wiki/Floyd-Steinberg_dithering
			if (dither == 1)
			{
				if((x > 0) && (y > 0) && (x < (pix->w-1)) && (y < (pix->h-1)))
				{
					// Compute quantization error
					qerror = value < threshold ? value : (value-255);

					stride = y * pix->w;

					// 7/16 = 0.4375f
					srcbuf = ptrstr + x + 1 + stride;
					value = *srcbuf;
					*srcbuf = CLAMP(roundf(value + 0.4375f * qerror), 0, 255);

					// 3/16 = 0.1875f
					srcbuf = ptrstr + x - 1 + stride + pix->w;
					value = *srcbuf;
					*srcbuf = CLAMP(roundf(value + 0.1875f * qerror), 0, 255);

					// 5/16 = 0.3125f
					srcbuf = ptrstr + x + stride + pix->w;
					value = *srcbuf;
					*srcbuf = CLAMP(roundf(value + 0.3125f * qerror), 0, 255);

					// 1/16 = 0.0625f
					srcbuf = ptrstr + x + 1 + stride + pix->w;
					value = *srcbuf;
					*srcbuf = CLAMP(roundf(value + 0.0625f * qerror), 0, 255);
				}
			}
		}

		// Pad bit pack if needed
		if (bitcnt < 7)
		{
			while (bitcnt >= 0)
			{
				bitpack |= 0 << bitcnt--;
			}
			*trgbuf++ = bitpack;
			bitpack = 0;
			bitcnt = 7;
		}

	}
	fz_free(ctx, pixbuf);
	return 0;
}

/**
 * Get an RGB, Gray or Binary pixels
 *
 */
JNIEXPORT jobject JNICALL Java_com_jmupdf_JmuPdf_getPixMap(JNIEnv *env, jclass obj, jlong handle, jint pagen, jfloat zoom, jint rotate, jint color, jfloat gamma, jintArray bbox, jfloat x0, jfloat y0, jfloat x1, jfloat y1)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if (!hdoc)
	{
		return NULL;
	}

	fz_pixmap *pix = jni_get_pixmap(hdoc, pagen, zoom, rotate, color, gamma, x0, y0, x1, y1);

	if (!pix)
	{
		return NULL;
	}

	// Create new array of either int's or bytes'.
	// Binary data require pixels to be in byte[] array and
	// RGB/Gray data require pixels to be in int[] array.
	int size = pix->w * pix->h;
	jobject pixarray;

	if (color == COLOR_BLACK_WHITE ||
	    color == COLOR_BLACK_WHITE_DITHER)
	{
		pixarray = (*env)->NewByteArray(env, size);
	}
	else
	{
		pixarray = (*env)->NewIntArray(env, size);
	}

	if (!pixarray)
	{
		fz_drop_pixmap(hdoc->ctx, pix);
		return NULL;
	}

	// Get pointer to array
	jint *pixint = NULL;
	jbyte *pixbyte = NULL;

	if (color == COLOR_BLACK_WHITE ||
		color == COLOR_BLACK_WHITE_DITHER)
	{
		pixbyte = (*env)->GetPrimitiveArrayCritical(env, pixarray, 0);
	}
	else
	{
		pixint = (*env)->GetPrimitiveArrayCritical(env, pixarray, 0);
	}

	if (!pixbyte && !pixint) {
		fz_drop_pixmap(hdoc->ctx, pix);
		return NULL;
	}

	// Get a pointer to pixel data
	unsigned char *pixels = pix->samples;
	jint *ptr_pixint = pixint;
	jbyte *ptr_pixbyte = pixbyte;

	int i = 0;
	int rc = 0;

	if (color == COLOR_RGB || color == COLOR_ARGB)
	{
		for (i=0; i<size; i++)
		{
			*ptr_pixint++ = jni_get_a(pixels[3]) |
							jni_get_r(pixels[0]) |
							jni_get_g(pixels[1]) |
							jni_get_b(pixels[2]);
			pixels += pix->n;
		}
	}
	else if (color == COLOR_GRAY_SCALE)
	{
		for (i=0; i<size; i++)
		{
			*ptr_pixint++ = jni_get_a(pixels[1]) |
							jni_get_r(pixels[0]) |
							jni_get_g(pixels[0]) |
							jni_get_b(pixels[0]);
			pixels += pix->n;
		}
	}
	else if (color == COLOR_BLACK_WHITE || color == COLOR_BLACK_WHITE_DITHER)
	{
		int dither = (color == COLOR_BLACK_WHITE_DITHER);
		rc = jni_pix_to_black_white(hdoc->ctx, pix, dither, pixels);
		if (rc == 0)
		{
			for (i=0; i<size; i++)
				*ptr_pixbyte++ = (jbyte)*pixels++;
		}
	}

	// Set array region
	if (color == COLOR_BLACK_WHITE || color == COLOR_BLACK_WHITE_DITHER)
	{
		(*env)->ReleasePrimitiveArrayCritical(env, pixarray, pixbyte, 0);
		pixbyte = NULL;
	}
	else
	{
		(*env)->ReleasePrimitiveArrayCritical(env, pixarray, pixint, 0);
		pixint = NULL;
	}

	if (rc == 0)
	{
		// Populate bbox with image information
		jint *data1 = (*env)->GetIntArrayElements(env, bbox, 0);
		data1[0] = 0;
		data1[1] = 0;
		data1[2] = ABS(pix->w);
		data1[3] = ABS(pix->h);
		(*env)->ReleaseIntArrayElements(env, bbox, data1, 0);
	}

	// Cleanup
	fz_drop_pixmap(hdoc->ctx, pix);

	if (rc == 0)
	{
		return pixarray;
	}
	else
	{
		return NULL;
	}
}

/**
 * Set Default Anti Alias Level
 */
JNIEXPORT jint JNICALL Java_com_jmupdf_JmuPdf_setAntiAliasLevel(JNIEnv *env, jclass obj, jlong handle, jint anti_alias_level)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if (!hdoc)
	{
		return -1;
	}

	if (hdoc->anti_alias_level != anti_alias_level)
	{
		hdoc->anti_alias_level = anti_alias_level;
		if (hdoc->ctx->glyph_cache)
		{
			fz_free_glyph_cache_context(hdoc->ctx);
			hdoc->ctx->glyph_cache = NULL;
		}
	}

	return 0;
}

/**
 * Get Default Anti Alias Level
 */
JNIEXPORT jint JNICALL Java_com_jmupdf_JmuPdf_getAntiAliasLevel(JNIEnv *env, jclass obj, jlong handle)
{
	return jni_get_doc_handle(handle)->anti_alias_level;
}

/**
 * Create a PNG file
 *
 */
JNIEXPORT jint JNICALL Java_com_jmupdf_JmuPdf_writePng(JNIEnv *env, jclass obj, jlong handle, jint page, jfloat zoom, jint color, jfloat gamma, jstring pngFile)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if (!hdoc)
	{
		return -1;
	}

	fz_pixmap *pix = jni_get_pixmap(hdoc, page, zoom, -1, color, gamma, 0, 0, 0, 0);

	if (!pix)
	{
		return -2;
	}

	const char *file = (*env)->GetStringUTFChars(env, pngFile, 0);
	int rc = jni_write_png(hdoc->ctx, pix, (char*)file, (color==COLOR_ARGB), zoom);
	(*env)->ReleaseStringUTFChars(env, pngFile, file);
	fz_drop_pixmap(hdoc->ctx, pix);

	return rc==0?0:-3;
}

/**
 * Create a single or multi-page TIF file
 *
 */
JNIEXPORT jint JNICALL Java_com_jmupdf_JmuPdf_writeTif(JNIEnv *env, jclass obj, jlong handle, jint page, jfloat zoom, jint color, jfloat gamma, jstring tifFile, jint compression, jint mode, jint quality)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if (!hdoc)
	{
		return -1;
	}

	fz_pixmap *pix = jni_get_pixmap(hdoc, page, zoom, -1, color, gamma, 0, 0, 0, 0);

	if (!pix)
	{
		return -2;
	}

	const char *file = (*env)->GetStringUTFChars(env, tifFile, 0);
	int rc = jni_write_tif(hdoc->ctx, pix, (char*)file, zoom, compression, color, mode, quality);
	(*env)->ReleaseStringUTFChars(env, tifFile, file);

	fz_drop_pixmap(hdoc->ctx, pix);

	return rc==0?0:-3;
}

/**
 * Create a JPEG file
 *
 */
JNIEXPORT jint JNICALL Java_com_jmupdf_JmuPdf_writeJPeg(JNIEnv *env, jclass obj, jlong handle, jint page, jfloat zoom, jint color, jfloat gamma, jstring jpegFile, jint quality)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if (!hdoc)
	{
		return -1;
	}

	fz_pixmap *pix = jni_get_pixmap(hdoc, page, zoom, -1, color, gamma, 0, 0, 0, 0);

	if (!pix)
	{
		return -2;
	}

	const char *file = (*env)->GetStringUTFChars(env, jpegFile, 0);
	int rc = jni_write_jpeg(hdoc->ctx, pix, (char*)file, zoom, color, quality);
	(*env)->ReleaseStringUTFChars(env, jpegFile, file);

	fz_drop_pixmap(hdoc->ctx, pix);

	return rc==0?0:-3;
}

/**
 * Create a PNM file
 */
JNIEXPORT jint JNICALL Java_com_jmupdf_JmuPdf_writePnm(JNIEnv *env, jclass obj, jlong handle, jint page, jfloat zoom, jint color, jfloat gamma, jstring pnmFile)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if (!hdoc)
	{
		return -1;
	}

	fz_pixmap *pix = jni_get_pixmap(hdoc, page, zoom, -1, color, gamma, 0, 0, 0, 0);

	if (!pix)
	{
		return -2;
	}

	const char *file = (*env)->GetStringUTFChars(env, pnmFile, 0);
	int rc = -3;

	fz_try(hdoc->ctx)
	{
		fz_write_pnm(hdoc->ctx, pix, (char*)file);
		rc = 0;
	}
	fz_catch(hdoc->ctx) {}

	(*env)->ReleaseStringUTFChars(env, pnmFile, file);
	fz_drop_pixmap(hdoc->ctx, pix);

	return rc;
}

/**
 * Create a PAM file
 */
JNIEXPORT jint JNICALL Java_com_jmupdf_JmuPdf_writePam(JNIEnv *env, jclass obj, jlong handle, jint page, jfloat zoom, jint color, jfloat gamma, jstring pamFile)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if(!hdoc)
	{
		return -1;
	}

	fz_pixmap *pix = jni_get_pixmap(hdoc, page, zoom, -1, color, gamma, 0, 0, 0, 0);

	if (!pix)
	{
		return -2;
	}

	const char *file = (*env)->GetStringUTFChars(env, pamFile, 0);
	int rc = -3;

	fz_try(hdoc->ctx)
	{
		fz_write_pam(hdoc->ctx, pix, (char*)file, (color==COLOR_ARGB));
		rc = 0;
	}
	fz_catch(hdoc->ctx) {}

	(*env)->ReleaseStringUTFChars(env, pamFile, file);
	fz_drop_pixmap(hdoc->ctx, pix);

	return rc;
}

/**
 * Create a PBM file
 */
JNIEXPORT jint JNICALL Java_com_jmupdf_JmuPdf_writePbm(JNIEnv *env, jclass obj, jlong handle, jint page, jfloat zoom, jfloat gamma, jstring pbmFile)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if (!hdoc)
	{
		return -1;
	}

	fz_pixmap *pix = jni_get_pixmap(hdoc, page, zoom, -1, COLOR_GRAY_SCALE, gamma, 0, 0, 0, 0);

	if (!pix)
	{
		return -2;
	}

	const char *file = (*env)->GetStringUTFChars(env, pbmFile, 0);
	int rc = -3;

	fz_halftone *ht = fz_get_default_halftone(hdoc->ctx, 1);
	fz_bitmap *bit = NULL;

	if (ht)
	{
		bit = fz_halftone_pixmap(hdoc->ctx, pix, ht);
	}
	if (bit)
	{
		fz_try(hdoc->ctx)
		{
			fz_write_pbm(hdoc->ctx, bit, (char*)file);
			rc = 0;
		}
		fz_catch(hdoc->ctx){}
	}

	(*env)->ReleaseStringUTFChars(env, pbmFile, file);
	fz_drop_bitmap(hdoc->ctx, bit);
	fz_drop_halftone(hdoc->ctx, ht);
	fz_drop_pixmap(hdoc->ctx, pix);

	return rc;
}

/**
 * Create a BMP file
 */
JNIEXPORT jint JNICALL Java_com_jmupdf_JmuPdf_writeBmp(JNIEnv *env, jclass obj, jlong handle, jint page, jfloat zoom, jint color, jfloat gamma, jstring bmpFile)
{
	jni_doc_handle *hdoc = jni_get_doc_handle(handle);

	if (!hdoc)
	{
		return -1;
	}

	fz_pixmap *pix = jni_get_pixmap(hdoc, page, zoom, -1, color, gamma, 0, 0, 0, 0);

	if (!pix)
	{
		return -2;
	}

	const char *file = (*env)->GetStringUTFChars(env, bmpFile, 0);
	int rc = jni_write_bmp(hdoc->ctx, pix, (char*)file, color, zoom);
	(*env)->ReleaseStringUTFChars(env, bmpFile, file);

	fz_drop_pixmap(hdoc->ctx, pix);

	return rc==0?0:-3;
}
