#include "jmupdf.h"

/**
 * Convert jbyte array to char array.
 */
char * jni_jbyte_to_char(JNIEnv *env, fz_context *ctx, jbyteArray ba)
{
	jbyte *jb = jni_get_byte_array(ba);
	jsize len = jni_get_array_len(ba);

	char * buf = fz_malloc_no_throw(ctx, len + 1);
	int i = 0;

	for (i = 0; i < len; i++) {
		buf[i] = jb[i];
	}

	buf[len] = '\0';

	jni_release_byte_array(ba, jb);

	return buf;
}

/**
 * Get Current Transformation Matrix
 */
fz_matrix jni_get_view_ctm(float zoom, int rotate)
{
	fz_matrix ctm = fz_identity;
	float z = zoom;

	ctm = fz_scale(z, z);
	ctm = fz_concat(ctm, fz_rotate(rotate));

	return ctm;
}

/**
 * Determine if alpha value should be saved based on color type
 */
static int jni_save_alpha(int color)
{
	if (color == COLOR_ARGB ||
		color == COLOR_ARGB_PRE)
	{
		return 1;
	}
	return 0;
}

/**
 * Get color space
 */
static fz_colorspace * jni_get_color_space(int color)
{
	fz_colorspace *colorspace;
	switch (color)
	{
		case COLOR_RGB:
		case COLOR_ARGB:
		case COLOR_ARGB_PRE:
			colorspace = fz_device_rgb;
			break;
		case COLOR_BGR:
			colorspace = fz_device_bgr;
			break;
		case COLOR_GRAY_SCALE:
		case COLOR_BLACK_WHITE:
		case COLOR_BLACK_WHITE_DITHER:
			colorspace = fz_device_gray;
			break;
		default:
			colorspace = fz_device_rgb;
			break;
	}
	return colorspace;
}

/**
 * Normalize rectangle bounds
 */
static fz_rect jni_normalize_rect(jni_page *page, float x0, float y0, float x1, float y1)
{
	fz_rect rect = fz_empty_rect;
	if (page->page)
	{
		if (x0==0 && y0==0 && x1==0 && y1==0)
		{
			rect.x0 = page->bbox.x0;
			rect.y0 = page->bbox.y0;
			rect.x1 = page->bbox.x1;
			rect.y1 = page->bbox.y1;
		}
		else
		{
			rect.x0 = MAX(x0, page->bbox.x0);
			rect.y0 = MAX(y0, page->bbox.y0);
			rect.x1 = MIN(x1, page->bbox.x1);
			rect.y1 = MIN(y1, page->bbox.y1);
		}
	}
	return rect;
}

/**
 * Get an RGB, ARGB, Gray scale pixel data
 */
static fz_pixmap *jni_get_pixmap(jni_page *page, float zoom, int rotate, int color, float gamma, float x0, float y0, float x1, float y1)
{
	fz_pixmap *pix = NULL;
	fz_device *dev = NULL;

	//  Set anti alias level
	// TODO: Move this out
	// TODO: Fix, this is now broken, yikes!
	if (fz_get_aa_level(page->ctx) != page->anti_alias)
	{
		fz_set_aa_level(page->ctx, page->anti_alias);
	}

	// Get Current Transformation Matrix
	fz_matrix ctm = jni_get_view_ctm(zoom, rotate);

	// Create new bounding box for page
	fz_bbox bbox = fz_round_rect(fz_transform_rect(ctm, jni_normalize_rect(page, x0, y0, x1, y1)));

	// Try to get pixel buffer
	fz_try(page->ctx)
	{
		pix = fz_new_pixmap_with_rect(page->ctx, jni_get_color_space(color), bbox);
	}
	fz_catch(page->ctx)
	{
		fz_drop_pixmap(page->ctx, pix);
	}

	if (!pix)
	{
		return NULL;
	}
	if (!pix->samples)
	{
		fz_drop_pixmap(page->ctx, pix);
		return NULL;
	}

	// Set background color
	if (jni_save_alpha(color))
	{
		fz_clear_pixmap(page->ctx, pix);
	}
	else
	{
		fz_clear_pixmap_with_value(page->ctx, pix, 255);
	}

	// Render image
	fz_try(page->ctx)
	{
		//if (!page->doc->ctx->glyph_cache)
		//{
		//	fz_new_glyph_cache_context(page->doc->ctx);
		//	page->ctx->glyph_cache = fz_keep_glyph_cache(page->doc->ctx);
		//}
		dev = fz_new_draw_device(page->ctx, pix);
		fz_run_display_list(page->list, dev, ctm, bbox, NULL);
		if (gamma != 1 && gamma > 0)
		{
			fz_gamma_pixmap(page->ctx, pix, gamma);
		}
		if (color != COLOR_ARGB_PRE)
		{
			fz_unmultiply_pixmap(page->ctx, pix);
		}
	}
	fz_always(page->ctx)
	{
		fz_free_device(dev);
	}
	fz_catch(page->ctx)
	{
		fz_drop_pixmap(page->ctx, pix);
		pix = NULL;
	}

	return pix;
}

/**
 * Get a new direct byte buffer that wraps packed pixel data
 */
static jobject jni_get_packed_pixels(JNIEnv *env, fz_context *ctx, fz_pixmap *pix, jint color)
{
	int usebyte = (color == COLOR_BLACK_WHITE || color == COLOR_BLACK_WHITE_DITHER || color == COLOR_GRAY_SCALE);
	int size = pix->w * pix->h;
	int memsize = usebyte ? (size*sizeof(jbyte)) : (size*sizeof(jint));

	jobject pixarray = fz_malloc_no_throw(ctx, memsize);

	if (!pixarray)
	{
		return NULL;
	}

	jint *ptr_pixint = (jint*)pixarray;
	jbyte *ptr_pixbyte = (jbyte*)pixarray;
	unsigned char *pixels = pix->samples;
	int i = 0;
	int rc = 0;
	int dither = (color == COLOR_BLACK_WHITE_DITHER);

	// Set color space
	switch (color)
	{
		case COLOR_RGB:
			for (i=0; i<size; i++)
			{
				*ptr_pixint++ = jni_get_rgb_r(pixels[0]) |
								jni_get_rgb_g(pixels[1]) |
								jni_get_rgb_b(pixels[2]);
				pixels += pix->n;
			}
			break;
		case COLOR_ARGB:
		case COLOR_ARGB_PRE:
			for (i=0; i<size; i++)
			{
				*ptr_pixint++ = jni_get_rgb_a(pixels[3]) |
								jni_get_rgb_r(pixels[0]) |
								jni_get_rgb_g(pixels[1]) |
								jni_get_rgb_b(pixels[2]);
				pixels += pix->n;
			}
			break;
		case COLOR_BGR:
			for (i=0; i<size; i++)
			{
				*ptr_pixint++ = jni_get_bgr_b(pixels[0]) |
								jni_get_bgr_g(pixels[1]) |
								jni_get_bgr_r(pixels[2]);
				pixels += pix->n;
			}
			break;
		case COLOR_GRAY_SCALE:
			for (i=0; i<size; i++)
			{
				*ptr_pixbyte++ = jni_get_rgb_r(pixels[0]) |
							 	 jni_get_rgb_g(pixels[0]) |
								 jni_get_rgb_b(pixels[0]);
				pixels += pix->n;
			}
			break;
		case COLOR_BLACK_WHITE:
		case COLOR_BLACK_WHITE_DITHER:
			rc = jni_pix_to_black_white(ctx, pix, dither, (unsigned char *)ptr_pixbyte);
			break;
		default:
			break;
	}

	if (rc != 0)
	{
		fz_free(ctx, pixarray);
		return NULL;
	}

	return jni_new_buffer_direct(pixarray, memsize);
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

	// Create a packed gray scale image
	for (x = 0; x < size; x++)
	{
		*srcbuf++ = jni_get_rgb_r(*pixels) |
				    jni_get_rgb_g(*pixels) |
				    jni_get_rgb_b(*pixels);
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

	// Create a packed gray scale image
	for (x = 0; x < size; x++)
	{
		*srcbuf++ = jni_get_rgb_r(*pixels) |
				    jni_get_rgb_g(*pixels) |
				    jni_get_rgb_b(*pixels);
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
 * Get an packed RGB, Gray or Binary pixels
 * Returns a DirectByteBuffer
 */
JNIEXPORT jobject JNICALL
Java_com_jmupdf_JmuPdf_getByteBuffer(JNIEnv *env, jclass obj, jlong handle, jfloat zoom, jint rotate, jint color, jfloat gamma, jintArray bbox, jfloat x0, jfloat y0, jfloat x1, jfloat y1)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return NULL;
	}

	fz_pixmap *pix = jni_get_pixmap(page, zoom, rotate, color, gamma, x0, y0, x1, y1);

	if (!pix)
	{
		return NULL;
	}

	jobject pixarray = jni_get_packed_pixels(env, page->ctx, pix, color);

	if (!pixarray)
	{
		fz_drop_pixmap(page->ctx, pix);
		return NULL;
	}

	jint *ae = jni_get_int_array(bbox);

	if (ae)
	{
		ae[0] = 0;
		ae[1] = 0;
		ae[2] = ABS(pix->w);
		ae[3] = ABS(pix->h);
	}

	jni_release_int_array(bbox, ae);
	fz_drop_pixmap(page->ctx, pix);

	return pixarray;
}

/**
 * Free a ByteBuffer resource
 */
JNIEXPORT void JNICALL
Java_com_jmupdf_JmuPdf_freeByteBuffer(JNIEnv *env, jclass obj, jlong handle, jobject buffer)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return;
	}

	void *pixmap = jni_get_buffer_address(buffer);

	fz_free(page->ctx, pixmap);
}

/**
 * Set Default Anti Alias Level
 */
JNIEXPORT jint JNICALL
Java_com_jmupdf_JmuPdf_setAntiAliasLevel(JNIEnv *env, jclass obj, jlong handle, jint anti_alias_level)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return -1;
	}

	//if (page->anti_alias != anti_alias_level)
	//{
		page->anti_alias = anti_alias_level;
		//if (page->doc->ctx->glyph_cache)
		//{
		//	fz_drop_glyph_cache_context(page->doc->ctx);
		//}
	//}

	return 0;
}

/**
 * Get Default Anti Alias Level
 */
JNIEXPORT jint JNICALL
Java_com_jmupdf_JmuPdf_getAntiAliasLevel(JNIEnv *env, jclass obj, jlong handle)
{
	return jni_get_page(handle)->anti_alias;
}

/**
 * Create a PNG file
 */
JNIEXPORT jint JNICALL
Java_com_jmupdf_JmuPdf_writePng(JNIEnv *env, jclass obj, jlong handle, jint rotate, jfloat zoom, jint color, jfloat gamma, jbyteArray out)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return -1;
	}

	fz_pixmap *pix = jni_get_pixmap(page, zoom, rotate, color, gamma, 0, 0, 0, 0);

	if (!pix)
	{
		return -2;
	}

	char * file = jni_jbyte_to_char(env, page->ctx, out);

	int rc = jni_write_png(page->ctx, pix, (const char*)file, jni_save_alpha(color), zoom);

	fz_free(page->ctx, file);
	fz_drop_pixmap(page->ctx, pix);

	return rc==0?0:-3;
}

/**
 * Create a single or multi-page TIF file
 */
JNIEXPORT jint JNICALL
Java_com_jmupdf_JmuPdf_writeTif(JNIEnv *env, jclass obj, jlong handle, jint rotate, jfloat zoom, jint color, jfloat gamma, jbyteArray out, jint compression, jint mode, jint quality)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return -1;
	}

	fz_pixmap *pix = jni_get_pixmap(page, zoom, rotate, color, gamma, 0, 0, 0, 0);

	if (!pix)
	{
		return -2;
	}

	char * file = jni_jbyte_to_char(env, page->ctx, out);

	int rc = jni_write_tif(page->ctx, pix, (const char*)file, zoom, compression, color, mode, quality);

	fz_free(page->ctx, file);
	fz_drop_pixmap(page->ctx, pix);

	return rc==0?0:-3;
}

/**
 * Create a JPEG file
 */
JNIEXPORT jint JNICALL
Java_com_jmupdf_JmuPdf_writeJPeg(JNIEnv *env, jclass obj, jlong handle, jint rotate, jfloat zoom, jint color, jfloat gamma, jbyteArray out, jint quality)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return -1;
	}

	fz_pixmap *pix = jni_get_pixmap(page, zoom, rotate, color, gamma, 0, 0, 0, 0);

	if (!pix)
	{
		return -2;
	}

	char * file = jni_jbyte_to_char(env, page->ctx, out);

	int rc = jni_write_jpg(page->ctx, pix, (const char*)file, zoom, color, quality);

	fz_free(page->ctx, file);
	fz_drop_pixmap(page->ctx, pix);

	return rc==0?0:-3;
}

/**
 * Create a PNM file
 */
JNIEXPORT jint JNICALL
Java_com_jmupdf_JmuPdf_writePnm(JNIEnv *env, jclass obj, jlong handle, jint rotate, jfloat zoom, jint color, jfloat gamma, jbyteArray out)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return -1;
	}

	fz_pixmap *pix = jni_get_pixmap(page, zoom, rotate, color, gamma, 0, 0, 0, 0);

	if (!pix)
	{
		return -2;
	}

	char * file = jni_jbyte_to_char(env, page->ctx, out);
	int rc = -3;

	fz_try(page->ctx)
	{
		fz_write_pnm(page->ctx, pix, file);
		rc = 0;
	}
	fz_catch(page->ctx) {}

	fz_free(page->ctx, file);
	fz_drop_pixmap(page->ctx, pix);

	return rc;
}

/**
 * Create a PAM file
 */
JNIEXPORT jint JNICALL
Java_com_jmupdf_JmuPdf_writePam(JNIEnv *env, jclass obj, jlong handle, jint rotate, jfloat zoom, jint color, jfloat gamma, jbyteArray out)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return -1;
	}

	fz_pixmap *pix = jni_get_pixmap(page, zoom, rotate, color, gamma, 0, 0, 0, 0);

	if (!pix)
	{
		return -2;
	}

	char * file = jni_jbyte_to_char(env, page->ctx, out);
	int rc = -3;

	fz_try(page->ctx)
	{
		fz_write_pam(page->ctx, pix, file, jni_save_alpha(color));
		rc = 0;
	}
	fz_catch(page->ctx) {}

	fz_free(page->ctx, file);
	fz_drop_pixmap(page->ctx, pix);

	return rc;
}

/**
 * Create a PBM file
 */
JNIEXPORT jint JNICALL
Java_com_jmupdf_JmuPdf_writePbm(JNIEnv *env, jclass obj, jlong handle, jint rotate, jfloat zoom, jfloat gamma, jbyteArray out)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return -1;
	}

	fz_pixmap *pix = jni_get_pixmap(page, zoom, rotate, COLOR_GRAY_SCALE, gamma, 0, 0, 0, 0);

	if (!pix)
	{
		return -2;
	}

	char * file = jni_jbyte_to_char(env, page->ctx, out);
	int rc = -3;

	fz_halftone *ht = fz_get_default_halftone(page->ctx, 1);
	fz_bitmap *bit = NULL;

	if (ht)
	{
		bit = fz_halftone_pixmap(page->ctx, pix, ht);
	}
	if (bit)
	{
		fz_try(page->ctx)
		{
			fz_write_pbm(page->ctx, bit, (char*)file);
			rc = 0;
		}
		fz_catch(page->ctx){}
	}

	fz_free(page->ctx, file);
	fz_drop_bitmap(page->ctx, bit);
	fz_drop_halftone(page->ctx, ht);
	fz_drop_pixmap(page->ctx, pix);

	return rc;
}

/**
 * Create a BMP file
 */
JNIEXPORT jint JNICALL
Java_com_jmupdf_JmuPdf_writeBmp(JNIEnv *env, jclass obj, jlong handle, jint rotate, jfloat zoom, jint color, jfloat gamma, jbyteArray out)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return -1;
	}

	fz_pixmap *pix = jni_get_pixmap(page, zoom, rotate, color, gamma, 0, 0, 0, 0);

	if (!pix)
	{
		return -2;
	}

	char * file = jni_jbyte_to_char(env, page->ctx, out);

	int rc = jni_write_bmp(page->ctx, pix, (const char*)file, zoom, color);

	fz_free(page->ctx, file);
	fz_drop_pixmap(page->ctx, pix);

	return rc==0?0:-3;
}
