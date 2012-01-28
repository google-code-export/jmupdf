#ifdef WIN32
#include <windows.h>
#define TIFF_OPEN(f,m) TIFFOpenW(f,m)
#define SLEEP(x) Sleep(x)
#else
#include <unistd.h>
#define TIFF_OPEN(f,m) TIFFOpen(f,m)
#define SLEEP(x) usleep(x)
#endif

#include "jmupdf.h"
#include "tiffio.h"

/**
 * RGBA
 *
 * This format is an extension of BI_BITFIELDS where a fourth bitmask is used to define bits
 * in the pixel which correspond to an alpha channel. When displayed on top of other images,
 * RGBA pixels are blended with the background pixel according to the value of this alpha component.
 *
 * For example, a 32bpp RGBA image would likely use the top 8 bits of each u_int32 to store the
 * alpha component (the unused byte in normal 32bpp RGB). In this case, the masks reported would be:
 *
 * Red 		0x00FF0000
 * Green 	0x0000FF00
 * Blue 	0x000000FF
 * Alpha 	0xFF000000
 *
 * giving 256 levels of blending per pixel (8 bits of alpha data).In general, the masks used for
 * this format are passed using a means other than a BITMAPINFOHEADER (for example, in DirectDraw,
 * the DDPIXELFORMAT structure contains fields specifically for R,G,B and Alpha masks) but I have
 * also heard that it is acceptable to append 4 u_int32s to the end of the BITMAPINFOHEADER structure
 * containing the mask information.
 *
 */

/**
 * Create a new single page or multi-page TIF image
 *
 */
static int write_tif(unsigned char *pixels, const char *file, int mode,
		     int quality, int compression, int photometric,
		     float resolution, int bitspersample, int samplesperpixel,
		     size_t size, int w, int h)
{

	TIFF *image = NULL;
	char *cmode;

	if (mode == 0)
	{
		cmode = "w";
	}
	else
	{
		cmode = "a";
	}

	/*
	 * Suppress messages
	 */
	TIFFSetWarningHandler(NULL);
	TIFFSetErrorHandler(NULL);

	/*
	 * Open TIF file
	 *  Modes:
	 *   a = Open or create file for writing; append data to end of file.
	 *   w = Open file for writing; discard existing contents
	 *
	 *   Retry for a few minutes, then bail out.
	 *     TODO: Should probably make this a flag user can control
	 */
	int r = 0;
	while (r < 500)
	{
		image = TIFF_OPEN(file, cmode);
		if (image) 	break;
		else		++r;
		SLEEP(1000);
	}

	if (!image)
	{
		return -1;
	}

	TIFFSetField(image, TIFFTAG_ORIENTATION, ORIENTATION_TOPLEFT);
	TIFFSetField(image, TIFFTAG_PHOTOMETRIC, photometric);
	TIFFSetField(image, TIFFTAG_PLANARCONFIG, PLANARCONFIG_CONTIG);
	TIFFSetField(image, TIFFTAG_BITSPERSAMPLE, bitspersample);
	TIFFSetField(image, TIFFTAG_IMAGEWIDTH, w);
	TIFFSetField(image, TIFFTAG_IMAGELENGTH, h);
	TIFFSetField(image, TIFFTAG_SAMPLESPERPIXEL, samplesperpixel);
	TIFFSetField(image, TIFFTAG_ROWSPERSTRIP, h);
	TIFFSetField(image, TIFFTAG_COMPRESSION, compression);
	TIFFSetField(image, TIFFTAG_XRESOLUTION, resolution);
	TIFFSetField(image, TIFFTAG_YRESOLUTION, resolution);
	TIFFSetField(image, TIFFTAG_FILLORDER, FILLORDER_MSB2LSB);
	TIFFSetField(image, TIFFTAG_RESOLUTIONUNIT, RESUNIT_INCH);

	// Note: quality level is on the IJG 0-100 scale.
	// Default value is 75
	if (compression == COMPRESSION_JPEG)
	{
		TIFFSetField(image, TIFFTAG_JPEGQUALITY, quality);
	}

	// Note: quality level is on the ZLIB 1-9 scale.
	// Default value is 6
	if (compression == COMPRESSION_ADOBE_DEFLATE)
	{
		TIFFSetField(image, TIFFTAG_ZIPQUALITY, quality);
	}

	TIFFSetField(image, TIFFTAG_ARTIST, "Created by JMuPdf");

	// Write the information to the file
	tsize_t rc = TIFFWriteEncodedStrip(image, 0, pixels, size);

	// Close the file
	TIFFClose(image);

	if (rc < 0)
	{
		return -2;
	}
	else
	{
		return 0;
	}
}


/**
 * Create a new single page or multi-page TIF image
 *
 */
int jni_write_tif(fz_context *ctx, fz_pixmap *pix, const char *file, float zoom, int compression, int color, int mode, int quality)
{
	int ccitt = 0;
	int samplesperpixel = 1;
	int bitspersample = 1;
	int photometric = 0;
	int length = pix->w * pix->h;
	int savealpha = (color == COLOR_ARGB);
	float resolution = jni_resolution(zoom);
	size_t size;

 	if (compression == COMPRESSION_CCITTRLE || compression == COMPRESSION_CCITTRLEW ||
	    compression == COMPRESSION_CCITT_T4 || compression == COMPRESSION_CCITT_T6)
	{
		ccitt = 1;
		samplesperpixel = 1;
		photometric = PHOTOMETRIC_MINISWHITE;
		size = ((pix->w + 7) / 8) * pix->h;
		bitspersample = 1;
	}
	else
	{
		if (color == COLOR_RGB || color == COLOR_ARGB)
		{
			samplesperpixel = pix->n;
			photometric = PHOTOMETRIC_RGB;
			if (!savealpha)
			{
				--samplesperpixel;
			}
		}
		else
		{
			samplesperpixel = 1;
			photometric = PHOTOMETRIC_MINISBLACK;
		}
		size = length * samplesperpixel;
		bitspersample = 8;
	}

	//
	// --------------
	//

	unsigned char *pixels = pix->samples;

	if (color == COLOR_ARGB)
	{
		return write_tif(pixels, file, mode, quality,
				        compression, photometric, resolution, bitspersample,
				        samplesperpixel, size, pix->w, pix->h);
	}

	//
	// --------------
	//

	int i = 0;
	int rc = 0;
	unsigned char *trgbuf = (unsigned char*)fz_malloc_no_throw(ctx, size);

	if (!trgbuf)
	{
		return -3;
	}

	unsigned char *ptrbuf = trgbuf;

	if (color == COLOR_RGB)
	{
		for (i=0; i<length; i++)
		{
			*ptrbuf++ = pixels[0];
			*ptrbuf++ = pixels[1];
			*ptrbuf++ = pixels[2];
			pixels += pix->n;
		}
	}

	else if (color == COLOR_GRAY_SCALE)
	{
		for (i=0; i<length; i++)
		{
			*ptrbuf++ = pixels[0];
			pixels += pix->n;
		}
	}

	else if (color == COLOR_BLACK_WHITE ||
			 color == COLOR_BLACK_WHITE_DITHER)
	{
		int dither = (color == COLOR_BLACK_WHITE_DITHER);
		if (ccitt == 1)
		{
			rc = jni_pix_to_binary(ctx, pix, dither, ptrbuf);
		}
		else
		{
			rc = jni_pix_to_black_white(ctx, pix, dither, ptrbuf);
		}
	}

	if (rc == 0)
	{
		rc = write_tif(trgbuf, file, mode, quality,
					  compression, photometric, resolution, bitspersample,
					  samplesperpixel, size, pix->w, pix->h);
	}

	fz_free(ctx, trgbuf);

	return rc;
}
