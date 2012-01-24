/*
 * Source copied from res_pixmap.c
 */

#include "jmupdf.h"
#include "zlib.h"

static inline void big32(unsigned char *buf, unsigned int v)
{
	buf[0] = (v >> 24) & 0xff;
	buf[1] = (v >> 16) & 0xff;
	buf[2] = (v >> 8) & 0xff;
	buf[3] = (v) & 0xff;
}

static inline void put32(unsigned int v, FILE *fp)
{
	putc(v >> 24, fp);
	putc(v >> 16, fp);
	putc(v >> 8, fp);
	putc(v, fp);
}

static void putchunk(char *tag, unsigned char *data, int size, FILE *fp)
{
	unsigned int sum;
	put32(size, fp);
	fwrite(tag, 1, 4, fp);
	fwrite(data, 1, size, fp);
	sum = crc32(0, NULL, 0);
	sum = crc32(sum, (unsigned char*)tag, 4);
	sum = crc32(sum, data, size);
	put32(sum, fp);
}

int jni_write_png(fz_context *ctx, fz_pixmap *pixmap, char *filename, int savealpha, float zoom)
{
	static const unsigned char pngsig[8] = { 137, 80, 78, 71, 13, 10, 26, 10 };
	FILE *fp;
	unsigned char head[13];
	unsigned char phys[9];	// Added by PJR
	unsigned char *udata = NULL;
	unsigned char *cdata = NULL;
	unsigned char *sp, *dp;
	uLong usize, csize;
	int y, x, k, sn, dn;
	int color;
	int err;

	fz_var(udata);
	fz_var(cdata);

	if (pixmap->n != 1 && pixmap->n != 2 && pixmap->n != 4)
		fz_throw(ctx, "pixmap must be grayscale or rgb to write as png");

	sn = pixmap->n;
	dn = pixmap->n;
	if (!savealpha && dn > 1)
		dn--;

	switch (dn)
	{
	default:
	case 1: color = 0; break;
	case 2: color = 4; break;
	case 3: color = 2; break;
	case 4: color = 6; break;
	}

	usize = (pixmap->w * dn + 1) * pixmap->h;
	csize = compressBound(usize);
	fz_try(ctx)
	{
		udata = fz_malloc(ctx, usize);
		cdata = fz_malloc(ctx, csize);
	}
	fz_catch(ctx)
	{
		fz_free(ctx, udata);
		fz_free(ctx, cdata);
		//fz_rethrow(ctx);
	}

	if (!udata || !cdata)
	{
		return -1;
	}

	sp = pixmap->samples;
	dp = udata;
	for (y = 0; y < pixmap->h; y++)
	{
		*dp++ = 1; /* sub prediction filter */
		for (x = 0; x < pixmap->w; x++)
		{
			for (k = 0; k < dn; k++)
			{
				if (x == 0)
					dp[k] = sp[k];
				else
					dp[k] = sp[k] - sp[k-sn];
			}
			sp += sn;
			dp += dn;
		}
	}

	err = compress(cdata, &csize, udata, usize);
	if (err != Z_OK)
	{
		fz_free(ctx, udata);
		fz_free(ctx, cdata);
		//fz_throw(ctx, "cannot compress image data");
		return -1;
	}

	fp = fopen(filename, "wb");
	if (!fp)
	{
		fz_free(ctx, udata);
		fz_free(ctx, cdata);
		//fz_throw(ctx, "cannot open file '%s': %s", filename, strerror(errno));
		return -1;
	}

	big32(head+0, pixmap->w);
	big32(head+4, pixmap->h);
	head[8] = 8; /* depth */
	head[9] = color;
	head[10] = 0; /* compression */
	head[11] = 0; /* filter */
	head[12] = 0; /* interlace */

	fwrite(pngsig, 1, 8, fp);
	putchunk("IHDR", head, 13, fp);

	// ~~~~~~~~~~~~~~~~~~~~~~~
	// ~~~ Begin: Added by PJR
	if (zoom > 0)
	{
		float factor = 0.0254; 	// <= 1 inch = 0.0254 meters
		float dpi = jni_resolution(zoom);
		float px = dpi / factor;
		big32(phys+0, px);		// PixelsPerUnitX
		big32(phys+4, px);		// PixelsPerUnitY
		phys[8] = 1;			// PixelUnits 1 = Meters
		putchunk("pHYs", phys, 9, fp);
	}
	// ~~~~ End: Added by PJR
	// ~~~~~~~~~~~~~~~~~~~~~~~

	putchunk("IDAT", cdata, csize, fp);
	putchunk("IEND", head, 0, fp);
	fclose(fp);

	fz_free(ctx, udata);
	fz_free(ctx, cdata);

	return 0;
}
