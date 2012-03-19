/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.pdf;

import com.jmupdf.enums.EncryptType;

/**
 * PDF encryption information
 * 
 * @author Pedro J Rivera
 *
 */
class PdfEncrypt {
	private boolean isEncrypted;
	private boolean canPrint;
	private boolean canModify;	
	private boolean canCopy;
	private boolean canNotes;
	private boolean canFillForm;
	private boolean canAccessibility;
	private boolean canAssemble;
	private boolean canPrintQuality;
	private int revision;	
	private int length;
	private EncryptType method;
	
	/**
	 * PDF encryption information
	 * 
	 * @param data
	 */
	public PdfEncrypt(int[] data) {
		isEncrypted = data[0] > 0;
		canPrint = data[1] > 0;
		canModify = data[2] > 0;		
		canCopy = data[3] > 0;
		canNotes = data[4] > 0;
		canFillForm = data[5] > 0;
		canAccessibility = data[6] > 0;
		canAssemble = data[7] > 0;
		canPrintQuality = data[8] > 0;
		revision = data[9];			
		length = data[10];
		method = EncryptType.setType(data[11]);
	}

	public boolean isEncrypted() {
		return isEncrypted;
	}
	
	public int getRevision() {
		return revision;
	}
	
	public int getLength() {
		return length;
	}

	public EncryptType getMethod() {
		return method;
	}

	public boolean getCanPrint() {
		return canPrint;
	}

	public boolean getCanModify() {
		return canModify;
	}

	public boolean getCanNotes() {
		return canNotes;
	}

	public boolean getCanCopy() {
		return canCopy;
	}

	public boolean getCanFillForm() {
		return canFillForm;
	}

	public boolean getCanAccessibility() {
		return canAccessibility;
	}

	public boolean getCanAssemble() {
		return canAssemble;
	}

	public boolean getCanPrintQuality() {
		return canPrintQuality;
	}

}
