/*
 * Source copied from res_pixmap.c
 *
 * Modifications
 * =============
 *
 *   Pedro J Rivera
 *   Francois Barre
 *
 */

#include "includes/jmupdf.h"
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

static int chunksize(int rawsize)
{
	return 4 + 4 + rawsize + 4;
}

static inline void put32_buffer(jbyte *buf, unsigned int offset, unsigned int v)
{
	buf[0 + offset] = (v >> 24) & 0xff;
	buf[1 + offset] = (v >> 16) & 0xff;
	buf[2 + offset] = (v >> 8) & 0xff;
	buf[3 + offset] = (v) & 0xff;
}

static int putchunk_buffer(char *tag, unsigned char *data, int size, jbyte *buf, int offset)
{
	unsigned int sum;
	put32_buffer(buf, offset, size);
	offset += 4;
	memcpy(&(buf[offset]), tag, 4);
	offset += 4;
	memcpy(&(buf[offset]), data, size);
	offset += size;

	sum = crc32(0, NULL, 0);
	sum = crc32(sum, (unsigned char*)tag, 4);
	sum = crc32(sum, data, size);
	put32_buffer(buf, offset, sum);
	offset += 4;

	return chunksize(size);
}

/**
 *
 * Create a PNG image format and save to file or byte buffer
 *
 * When *env is passed in we are assuming creation of a byte buffer.
 *
 * To improve performance I am using GetPrimitiveArrayCritical(). Later on we could change this to a
 * ByteBuffer and avoid getting in the way of the GC due to array pinning.
 *
 */
void * jni_write_png(JNIEnv *env, fz_context *ctx, fz_pixmap *pixmap, const char *filename, int savealpha, float zoom)
{
	static const unsigned char pngsig[8] = { 137, 80, 78, 71, 13, 10, 26, 10 };
	FILE *fp = NULL;
	unsigned char head[13];
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
		fz_throw(ctx, "Could not allocate memory");
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
		fz_throw(ctx, "cannot compress image data");
	}

	jbyteArray buf = NULL;
	jbyte *ptrbuf = NULL;

	if (env)
	{
		int size = 8 + chunksize(13) + chunksize(9) + chunksize(csize) + chunksize(0);
		buf = jni_new_byte_array(size);
		if (!buf)
		{
			fz_free(ctx, udata);
			fz_free(ctx, cdata);
			fz_throw(ctx, "could not create buffer");
		}
		ptrbuf = jni_start_array_critical(buf);
		if (!ptrbuf)
		{
			fz_free(ctx, udata);
			fz_free(ctx, cdata);
			fz_throw(ctx, "could not create buffer");
		}
	}
	else
	{
		fp = fopen(filename, "wb");
		if (!fp)
		{
			fz_free(ctx, udata);
			fz_free(ctx, cdata);
			fz_throw(ctx, "cannot open file '%s': %s", filename, strerror(errno));
		}
	}

	big32(head+0, pixmap->w);
	big32(head+4, pixmap->h);
	head[8] = 8;  /* depth */
	head[9] = color;
	head[10] = 0; /* compression */
	head[11] = 0; /* filter */
	head[12] = 0; /* interlace */

	int offset = 0;

	if (env)
	{
		memcpy(ptrbuf, pngsig, 8);
		offset += 8;
		offset += putchunk_buffer("IHDR", head, 13, ptrbuf, offset);
	}
	else
	{
		fwrite(pngsig, 1, 8, fp);
		putchunk("IHDR", head, 13, fp);
	}

	if (zoom > 0)
	{
		unsigned char phys[9];
		float factor = 0.0254; 	// <= 1 inch = 0.0254 meters
		float dpi = jni_resolution(zoom);
		float px = dpi / factor;
		big32(phys+0, px);		// PixelsPerUnitX
		big32(phys+4, px);		// PixelsPerUnitY
		phys[8] = 1;			// PixelUnits 1 = Meters
		if (env)
			offset += putchunk_buffer("pHYs", phys, 9, ptrbuf, offset);
		else
			putchunk("pHYs", phys, 9, fp);
	}

	if (env)
	{
		offset += putchunk_buffer("IDAT", cdata, csize, ptrbuf, offset);
		offset += putchunk_buffer("IEND", head, 0, ptrbuf, offset);
		jni_end_array_critical(buf, ptrbuf);
	}
	else
	{
		putchunk("IDAT", cdata, csize, fp);
		putchunk("IEND", head, 0, fp);
		fclose(fp);
	}

	fz_free(ctx, udata);
	fz_free(ctx, cdata);

	if (env)
	{
		return buf;
	}
	return NULL;
}
