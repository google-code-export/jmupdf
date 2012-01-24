/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.interfaces;

public interface TifTypes {

	/*
	 * TIF file modes
	 */
	public static final int TIF_DATA_DISCARD = 0;
	public static final int TIF_DATA_APPEND = 1;

	/*
	 * Supported TIF compression modes
	 */
	public static final int TIF_COMPRESSION_NONE = 1; 
	public static final int TIF_COMPRESSION_CCITT_RLE = 2; 
	public static final int TIF_COMPRESSION_CCITT_T_4 = 3; 
	public static final int TIF_COMPRESSION_CCITT_T_6 = 4; 
	public static final int TIF_COMPRESSION_LZW = 5;
	public static final int TIF_COMPRESSION_JPEG = 7; 
	public static final int TIF_COMPRESSION_ZLIB = 8; 
	public static final int TIF_COMPRESSION_PACKBITS = 32773; 
	public static final int TIF_COMPRESSION_DEFLATE = 32946;

}
