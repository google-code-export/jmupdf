/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.enums;

/**
 * ImageType enum
 * 
 * @author Pedro J Rivera
 *
 */
public enum ImageType {
	
	IMAGE_TYPE_RGB(1), 
	IMAGE_TYPE_ARGB(2),
	IMAGE_TYPE_ARGB_PRE(3),
	IMAGE_TYPE_BGR(4),
	IMAGE_TYPE_GRAY(10), 
	IMAGE_TYPE_BINARY(12), 
	IMAGE_TYPE_BINARY_DITHER(121);
	
	private int type;
	
	private ImageType(int type) {
		this.type = type;
	}
	
	public int getIntValue() {
		return type;
	}

}
