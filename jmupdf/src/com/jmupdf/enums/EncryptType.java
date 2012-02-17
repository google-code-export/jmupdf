/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.enums;

/**
 * EncryptType enum
 * 
 * @author Pedro J Rivera
 *
 */
public enum EncryptType {
	NONE(0),
	RC4(1),
	AES(2),
	UNKNOWN(-1);

	private int type;
	
	private EncryptType(int type) {
		this.type = type;
	}
	
	public int getIntValue() {
		return type;
	}
	
	public static EncryptType setType(int type) {
		EncryptType et;
		switch (type) {
		case 0:
			et = NONE;
			break;
		case 1:
			et = RC4;
			break;
		case 2:
			et = AES;
			break;
		case 3:
			et = NONE;
			break;
		default:
			et = UNKNOWN;
			break;
		}
		return et;
	}
	
}
