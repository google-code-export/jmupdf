/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.enums;

/**
 * TifCompression enum
 * 
 * @author Pedro J Rivera
 *
 */
public enum TifCompression {

	TIF_COMPRESSION_NONE(1), 
	TIF_COMPRESSION_CCITT_RLE(2), 
	TIF_COMPRESSION_CCITT_T_4(3), 
	TIF_COMPRESSION_CCITT_T_6(4), 
	TIF_COMPRESSION_LZW(5),
	TIF_COMPRESSION_JPEG(7), 
	TIF_COMPRESSION_ZLIB(8), 
	TIF_COMPRESSION_PACKBITS(32773), 
	TIF_COMPRESSION_DEFLATE(32946);
	
	private int compression;
	
	private TifCompression(int compression) {
		this.compression = compression;
	}
	
	public int getIntValue() {
		return compression;
	}
	
}
