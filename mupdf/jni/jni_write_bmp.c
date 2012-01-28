#include "jmupdf.h"
#include "bmpfile.h"

int jni_write_bmp(fz_context *ctx, fz_pixmap *pix, const char *file, float zoom, int color)
{
	bmpfile_t *bmp;
	rgb_pixel_t pixel;
	uint32_t depth;
	int i, j;
	unsigned char *pixels = pix->samples;
	int rc = 0;

	if (color == COLOR_RGB)
	{
		depth = 24;
	}
	else if (color == COLOR_GRAY_SCALE)
	{
		depth = 8;
	}
	else
	{
		depth = 1;
	}

	bmp = bmp_create(pix->w, pix->h, depth);

	if (bmp == NULL)
	{
		return -1;
	}

	bmp_set_dpi(bmp, jni_resolution(zoom), jni_resolution(zoom));

	if (color == COLOR_RGB)
	{
		for (i=0; i<pix->h; i++)
		{
			for (j=0; j<pix->w; j++)
			{
				pixel.red = pixels[0];
				pixel.green = pixels[1];
				pixel.blue = pixels[2];
				bmp_set_pixel(bmp, j, i, pixel);
				pixels += pix->n;
			}
		}
	}

	else if (color == COLOR_GRAY_SCALE)
	{
		for (i=0; i<pix->h; i++)
		{
			for (j=0; j<pix->w; j++)
			{
				pixel.red = pixels[0];
				pixel.green = pixels[0];
				pixel.blue = pixels[0];
				bmp_set_pixel(bmp, j, i, pixel);
				pixels += pix->n;
			}
		}
	}

	else if (color == COLOR_BLACK_WHITE || color == COLOR_BLACK_WHITE_DITHER)
	{
		unsigned char *trgbuf = (unsigned char*)fz_malloc_no_throw(ctx, pix->w*pix->h);
		if (trgbuf)
		{
			unsigned char *ptrbuf = trgbuf;
			int dither = (color == COLOR_BLACK_WHITE_DITHER);
			rc = jni_pix_to_black_white(ctx, pix, dither, ptrbuf);
			if (rc == 0)
			{
				ptrbuf = trgbuf;
				for (i=0; i<pix->h; i++)
				{
					for (j=0; j<pix->w; j++)
					{
						pixel.red = ptrbuf[0];
						pixel.green = ptrbuf[0];
						pixel.blue = ptrbuf[0];
						bmp_set_pixel(bmp, j, i, pixel);
						ptrbuf++;
					}
				}
			}
			fz_free(ctx, trgbuf);
		}
	}

	bmp_save(bmp, file);
	bmp_destroy(bmp);

	return rc;
}
