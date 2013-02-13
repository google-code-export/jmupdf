#include "includes/jmupdf.h"
#include "jpeglib.h"

/**
 *
 * Create a JPEG image format and save to file or byte buffer
 *
 * When *env is passed in we are assuming creation of a byte buffer.
 *
 * To improve performance I am using GetPrimitiveArrayCritical(). Later on we could change this to a
 * ByteBuffer and avoid getting in the way of the GC due to array pinning.
 *
 */
void * jni_write_jpg(JNIEnv *env, fz_context *ctx, fz_pixmap *pix, const char *file, float zoom, int color, int quality)
{
	struct jpeg_compress_struct cinfo;
	struct jpeg_error_mgr jerr;

	FILE *fp = NULL;
	unsigned char *outbuffer = NULL;
	long unsigned int outlen = 4096;

	JSAMPLE *trgbuf = NULL;
	int stride = pix->w * (pix->n - 1);
	int size = pix->w * pix->h;
	int i = 0;

	/*
	 * Step 1: allocate and initialize JPEG compression object
	 */
	cinfo.err = jpeg_std_error(&jerr);
	jpeg_create_compress(&cinfo);

	/*
	 * Step 2: specify data destination
	 */
	if (env)
	{
		outbuffer = malloc(outlen);
		if (!outbuffer)
			goto cleanup;
		jpeg_mem_dest(&cinfo, &outbuffer, &outlen);
	}
	else
	{
		fp = fopen(file, "wb");
		if (!fp)
			goto cleanup;
		jpeg_stdio_dest(&cinfo, fp);
	}

	/*
	 * Step 3: set parameters for compression
	 */
	cinfo.image_width = pix->w;
	cinfo.image_height = pix->h;
	cinfo.input_components = pix->n - 1;

	if (color == COLOR_GRAY_SCALE)
	{
		cinfo.in_color_space = JCS_GRAYSCALE;
	}
	else
	{
		cinfo.in_color_space = JCS_RGB;
	}

	jpeg_set_defaults(&cinfo);
	jpeg_set_quality(&cinfo, quality, TRUE);

	cinfo.X_density = jni_resolution(zoom);
	cinfo.Y_density = jni_resolution(zoom);;
	cinfo.density_unit = 1;

	/*
	 * Step 4: Compression initialization
	 */
	jpeg_start_compress(&cinfo, TRUE);

	/*
	 * Step 5: Remove alpha from original pixels
	 */
	trgbuf = (JSAMPLE*)fz_malloc_no_throw(ctx, pix->h*stride);

	if (!trgbuf)
	{
		goto cleanup;
	}

	JSAMPLE * ptrbuf = trgbuf;
	JSAMPLE * pixels = pix->samples;

	if (color == COLOR_GRAY_SCALE)
	{
		for (i=0; i<size; i++)
		{
			*ptrbuf++ = pixels[0];
			pixels += pix->n;
		}
	}
	else
	{
		for (i=0; i<size; i++)
		{
			*ptrbuf++ = pixels[0];
			*ptrbuf++ = pixels[1];
			*ptrbuf++ = pixels[2];
			pixels += pix->n;
		}
	}

	/*
	 * Step 6: while (scan lines remain to be written)
	 */
	JSAMPROW row_pointer[1];
	while (cinfo.next_scanline < cinfo.image_height)
	{
		row_pointer[0] = &trgbuf[cinfo.next_scanline * stride];
		jpeg_write_scanlines(&cinfo, row_pointer, 1);
	}

cleanup:

	/*
	 * Step 7: Finish compression
	 */
	jpeg_finish_compress(&cinfo);

	jbyteArray ba = NULL;

	if (env)
	{
		ba = jni_new_byte_array(outlen);
		if (ba)
		{
			jbyte *pa = jni_start_array_critical(ba);
			if (pa)
			{
				JOCTET *pbuf = outbuffer;
				for (i=0; i<outlen; i++)
					*pa++ = (jbyte)*pbuf++;
				jni_end_array_critical(ba, pa);
			}
		}
		free(outbuffer);
	}
	else
	{
		if (fp)
			fclose(fp);
	}

	/*
	 * Step 8: release JPEG compression object
	 */
	jpeg_destroy_compress(&cinfo);

	if (trgbuf)
		fz_free(ctx, trgbuf);

	if (env)
	{
		return ba;
	}
	return NULL;
}
