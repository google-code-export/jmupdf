/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.enums;

/**
 * DocumentType enum
 * 
 * @author Pedro J Rivera
 *
 */
public enum DocumentType {

	DOC_PDF(0), 
	DOC_XPS(1), 
	DOC_CBZ(2);
	
	private int type;
	
	private DocumentType(int type) {
		this.type = type;
	}
	
	public int getIntValue() {
		return type;
	}
	
}
