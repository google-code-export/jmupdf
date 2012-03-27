/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.enums;

/**
 * ImageFormat enum
 * 
 * @author Francois Barre
 *
 */
public enum ImageFormat {

	FORMAT_PNG(1),
    FORMAT_PBM(2),
    FORMAT_PNM(3),
    FORMAT_JPG(4),
    FORMAT_BMP(5),
    FORMAT_PAM(6),
    FORMAT_TIF(7),
    FORMAT_BUFFERED_IMAGE(8);

    private int format;
    
    ImageFormat(int format) {
        this.format = format;
    }
    
    public int getIntValue() {
        return format;
    }
    
    public static ImageFormat setImageFormat(int format) {
    	ImageFormat fmt;
    	switch (format) {
		case 1:
			fmt = FORMAT_PNG;
			break;
		case 2:
			fmt = FORMAT_PBM;
			break;
		case 3:
			fmt = FORMAT_PNM;
			break;
		case 4:
			fmt = FORMAT_JPG;
			break;
		case 5:
			fmt = FORMAT_BMP;
			break;
		case 6:
			fmt = FORMAT_PAM;
			break;
		case 7:
			fmt = FORMAT_TIF;
			break;
		case 8:
			fmt = FORMAT_BUFFERED_IMAGE;
			break;
		default:
			fmt = FORMAT_PNG;
			break;
		}
    	return fmt;
    }

}
