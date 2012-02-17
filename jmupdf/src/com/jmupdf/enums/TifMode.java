/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.enums;

/**
 * TifMode enum
 * 
 * @author Pedro J Rivera
 *
 */
public enum TifMode {

	TIF_DATA_DISCARD(0),
	TIF_DATA_APPEND(1);
	
	private int mode;
	
	private TifMode(int mode) {
		this.mode = mode;
	}
	
	public int getIntValue() {
		return mode;
	}
	
}
