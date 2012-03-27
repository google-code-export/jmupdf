/*
 *  
 * See copyright file
 * 
 */

package com.jmupdf;

import java.nio.ByteBuffer;

import com.jmupdf.document.DocumentOutline;
import com.jmupdf.page.PageLinks;
import com.jmupdf.page.PageText;

/**
 *
 * Abstract native interface to MuPdf library
 *
 * @author Pedro J Rivera
 *
 */
public abstract class JmuPdf {
	private static final String jmupdf_version = "0.5.0-beta";

	/* Open DLL dependency */
	static { loadDll(); }

	/* PDF, XPS, CBZ common document functions (document level) */
	protected native long open(int type, byte[] pdf, byte[] password, int maxStore);
	protected native void close(long handle);
	protected native int getVersion(long handle);
	protected native int getPageCount(long handle);
	protected native DocumentOutline getOutline(long handle);
	
	/* PDF Specific Functions (document level) */
	protected native String pdfInfo(long handle, String key);
	protected native int[] pdfEncryptInfo(long handle);

	/* PDF, XPS, CBZ common page functions (page level) */
	protected native long newPage(long handle, int page, float[] info);
	protected native long freePage(long handle);
	protected native PageText[] getPageText(long handle, float threshold, float x0, float y0, float x1, float y1);
	protected native PageLinks[] getPageLinks(long handle);
	
	/* PDF, XPS, CBZ common rendering functions (page level) */
	protected native ByteBuffer getByteBuffer(long handle, int[] bbox);
	protected native void freeByteBuffer(long handle, ByteBuffer buffer);
	protected native int saveAsFile(long handle, byte[] file);
	protected native byte[] saveAsByte(long handle);
	
	/* Get pointer to page rendering options data structure */
	protected native ByteBuffer getPageOptionsStruct(long handle);

	/**
	 * Get library version
	 * @return
	 */
	public static String getLibVersion() {
		return jmupdf_version;
	}
	
	/**
	 * Load native resource file
	 */
	private static void loadDll() {
		try {
			if (is64bit()) {
				System.loadLibrary("jmupdf64");
			} else {
				System.loadLibrary("jmupdf32");
			}
		} catch (Exception e) {
			System.out.println("Native library could not be loaded.");
		}
	}

	/**
	 * Determine if this is a 64 bit environment
	 */
	private static boolean is64bit() {
		String val = System.getProperty("sun.arch.data.model");
		boolean is64bit = false;
		if (val.equals("64")) {
			is64bit = true;
		}
		return is64bit;
	}

    /**
     * Print error messages
     * @param text
     */
    protected static void log(String text) {
    	System.err.println(text);
    }

}
