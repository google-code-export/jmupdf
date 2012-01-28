#include "jmupdf.h"
#include "jpeglib.h"

int jni_write_jpg(fz_context *ctx, fz_pixmap *pix, const char *file, float zoom, int color, int quality)
{
	struct jpeg_compress_struct cinfo;
	struct jpeg_error_mgr jerr;
	FILE * fp;
	int rc = 0;

	/*
	 * Step 1: allocate and initialize JPEG compression object
	 */
	cinfo.err = jpeg_std_error(&jerr);
	jpeg_create_compress(&cinfo);

	/*
	 * Step 2: specify data destination
	 */
	fp = fopen(file, "wb");
	if (!fp) {
		jpeg_destroy_compress(&cinfo);
		return -1;
	}

	jpeg_stdio_dest(&cinfo, fp);

	/*
	 * Step 3: set parameters for compression
	 */
	cinfo.image_width = pix->w;
	cinfo.image_height = pix->h;
	cinfo.input_components = pix->n - 1;

	if (color == COLOR_RGB || color == COLOR_ARGB)
	{
		cinfo.in_color_space = JCS_RGB;
	}
	else
	{
		cinfo.in_color_space = JCS_GRAYSCALE;
	}

	jpeg_set_defaults(&cinfo);
	jpeg_set_quality(&cinfo, quality, TRUE);

	cinfo.X_density = jni_resolution(zoom);
	cinfo.Y_density = jni_resolution(zoom);;
	cinfo.density_unit = 1;

	/*
	 * Step 4: Start compressor
	 */
	jpeg_start_compress(&cinfo, TRUE);

	/*
	 * Step 5: Remove alpha from original pixels
	 */
	int stride = pix->w * (pix->n - 1);
	JSAMPLE * trgbuf = (JSAMPLE*)fz_malloc_no_throw(ctx, pix->h*stride);
	if (!trgbuf)
	{
		rc = -2;
		goto cleanup;
	}

	JSAMPLE * ptrbuf = trgbuf;
	JSAMPLE * pixels = pix->samples;
	int i;
	int size = pix->w*pix->h;

	if (color == COLOR_RGB || color == COLOR_ARGB)
	{
		for (i=0; i<size; i++)
		{
			*ptrbuf++ = pixels[0];
			*ptrbuf++ = pixels[1];
			*ptrbuf++ = pixels[2];
			pixels += pix->n;
		}
	}
	else if (color == COLOR_GRAY_SCALE)
	{
		for (i=0; i<size; i++)
		{
			*ptrbuf++ = pixels[0];
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
	fclose(fp);

	/*
	 * Step 8: release JPEG compression object
	 */
	jpeg_destroy_compress(&cinfo);
	fz_free(ctx, trgbuf);

	return rc;
}
