#include "jmupdf.h"

enum jni_image_formats
{
    FORMAT_PNG = 1,
    FORMAT_PBM,
    FORMAT_PNM,
    FORMAT_JPG,
    FORMAT_BMP,
    FORMAT_PAM,
    FORMAT_TIF,
    FORMAT_BUFFERED_IMAGE,
};

struct jni_options_s
{
	jint imageFormat;
	jint imageType;
	jint rotate;
	jint quality;
	jint compression;
	jint mode;
	jint antiAlias;
	jfloat zoom;
	jfloat gamma;
	jfloat x0;
	jfloat y0;
	jfloat x1;
	jfloat y1;
};

/**
 * Create new options object
 */
jni_options * jni_new_options(fz_context *ctx)
{
	jni_options *o = fz_malloc_no_throw(ctx, sizeof(jni_options));
	return o;
}

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
static fz_rect jni_normalize_rect(jni_page *page)
{
	fz_rect rect = fz_empty_rect;
	if (page->options->x0 == 0 && page->options->y0 == 0 &&
		page->options->x1 == 0 && page->options->y1 == 0)
	{
		rect.x0 = page->bbox.x0;
		rect.y0 = page->bbox.y0;
		rect.x1 = page->bbox.x1;
		rect.y1 = page->bbox.y1;
	}
	else
	{
		rect.x0 = MAX(page->options->x0, page->bbox.x0);
		rect.y0 = MAX(page->options->y0, page->bbox.y0);
		rect.x1 = MIN(page->options->x1, page->bbox.x1);
		rect.y1 = MIN(page->options->y1, page->bbox.y1);
	}
	return rect;
}

/**
 * Set anti alias level
 */
static void jni_set_aa_level(jni_page *page)
{
	if (fz_aa_level(page->ctx) != page->options->antiAlias)
	{
		fz_drop_glyph_cache_context(page->ctx);
		fz_set_aa_level(page->ctx, page->options->antiAlias);
		fz_new_glyph_cache_context(page->ctx);
	}
}

/**
 * Get an RGB, ARGB, Gray scale pixel data
 */
static fz_pixmap *jni_get_pixmap(jni_page *page)
{
	fz_pixmap *pix = NULL;
	fz_device *dev = NULL;
	fz_matrix ctm;
	fz_bbox bbox;

	// Try to get pixel buffer
	fz_try(page->ctx)
	{
		jni_set_aa_level(page);
		ctm = jni_get_view_ctm(page->options->zoom, page->options->rotate);
		bbox = fz_round_rect(fz_transform_rect(ctm, jni_normalize_rect(page)));
		pix = fz_new_pixmap_with_bbox(page->ctx, jni_get_color_space(page->options->imageType), bbox);
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
	if (jni_save_alpha(page->options->imageType))
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
		dev = fz_new_draw_device(page->ctx, pix);
		fz_run_display_list(page->list, dev, ctm, bbox, NULL);
		if (page->options->gamma != 1 && page->options->gamma > 0)
		{
			fz_gamma_pixmap(page->ctx, pix, page->options->gamma);
		}
		if (page->options->imageType != COLOR_ARGB_PRE)
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
Java_com_jmupdf_JmuPdf_getByteBuffer(JNIEnv *env, jclass obj, jlong handle, jintArray bbox)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return NULL;
	}

	fz_pixmap *pix = jni_get_pixmap(page);

	if (!pix)
	{
		return NULL;
	}

	jobject pixarray = jni_get_packed_pixels(env, page->ctx, pix, page->options->imageType);

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
 * Create an image file from a given page
 */
JNIEXPORT jint JNICALL
Java_com_jmupdf_JmuPdf_saveAsFile(JNIEnv *env, jobject obj, jlong handle, jbyteArray out)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return -1;
	}

	fz_pixmap *pix = jni_get_pixmap(page);

	if (!pix)
	{
		return -2;
	}

	char * file = jni_jbyte_to_char(env, page->ctx, out);
	int rc = 0;

	fz_try(page->ctx)
	{
		switch (page->options->imageFormat)
		{
		case FORMAT_PNG:
			jni_write_png(NULL, page->ctx, pix, (const char*)file, jni_save_alpha(page->options->imageType), page->options->zoom);
			break;
		case FORMAT_JPG:
			jni_write_jpg(NULL, page->ctx, pix, (const char*)file, page->options->zoom, page->options->imageType, page->options->quality);
			break;
		case FORMAT_TIF:
			rc = jni_write_tif(page->ctx, pix, (const char*)file, page->options->zoom, page->options->compression, page->options->imageType, page->options->mode, page->options->quality);
			break;
		case FORMAT_PNM:
			fz_write_pnm(page->ctx, pix, file);
			break;
		case FORMAT_PAM:
			fz_write_pam(page->ctx, pix, file, jni_save_alpha(page->options->imageType));
			break;
		case FORMAT_PBM:
		{
			fz_halftone *ht = fz_default_halftone(page->ctx, 1);
			fz_bitmap *bit = NULL;
			if (ht)
			{
				bit = fz_halftone_pixmap(page->ctx, pix, ht);
			}
			if (bit)
			{
				fz_write_pbm(page->ctx, bit, (char*)file);
			}
			break;
		}
		case FORMAT_BMP:
			rc = jni_write_bmp(page->ctx, pix, (const char*)file, page->options->zoom, page->options->imageType);
			break;
		default:
			fprintf(stderr,"Image Format not supported : %i\n", (int)page->options->imageFormat);
			rc = -4;
			break;
		}
	}
	fz_catch(page->ctx)
	{
		rc = -3;
	}

	fz_free(page->ctx, file);
	fz_drop_pixmap(page->ctx, pix);

	return rc;
}

JNIEXPORT jbyteArray JNICALL
Java_com_jmupdf_JmuPdf_saveAsByte(JNIEnv *env, jobject obj, jlong handle)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return NULL;
	}

	fz_pixmap *pix = jni_get_pixmap(page);

	if (!pix)
	{
		return NULL;
	}

	jbyteArray buffer = NULL;

	fz_try(page->ctx)
	{
		switch (page->options->imageFormat)
		{
		case FORMAT_PNG:
			buffer = (jbyteArray)jni_write_png(env, page->ctx, pix, NULL, jni_save_alpha(page->options->imageType), page->options->zoom);
			break;
		case FORMAT_JPG:
			buffer = (jbyteArray)jni_write_jpg(env, page->ctx, pix, NULL, page->options->zoom, page->options->imageType, page->options->quality);
			break;
		default:
			fprintf(stderr,"Image Format not supported : %i\n", (int)page->options->imageFormat);
			break;
		}
	}
	fz_catch(page->ctx) {}

	fz_drop_pixmap(page->ctx, pix);

	return buffer;
}

/**
 * Get pointer to page options structure
 */
JNIEXPORT jobject JNICALL
Java_com_jmupdf_JmuPdf_getPageOptionsStruct(JNIEnv *env, jclass obj, jlong handle)
{
	jni_page *page = jni_get_page(handle);

	if (!page)
	{
		return NULL;
	}

	return jni_new_buffer_direct((void *)page->options, sizeof(*page->options));
}
