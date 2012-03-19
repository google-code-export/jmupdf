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

	/*
	 * Open DLL dependency
	 */
	static {
		loadDll();
	}

	/*
	 * PDF/XPS Common Functions 
	 */
	
	/* document level */
	protected native long open(int type, byte[] pdf, byte[] password, int maxStore);
	protected native void close(long handle);
	protected native int getVersion(long handle);
	protected native DocumentOutline getOutline(long handle);
	protected native int getPageCount(long handle);
	protected native int getAntiAliasLevel(long handle);
	protected native int setAntiAliasLevel(long handle, int antiAliasLevel);
	
	/* page level */
	protected native long newPage(long handle, int page);
	protected native long freePage(long handle);
	protected native float[] getPageInfo(long handle);
	protected native PageText[] getPageText(long handle, float threshold, float x0, float y0, float x1, float y1);
	protected native PageLinks[] getPageLinks(long handle);
	
	/*
	 * PDF Specific Functions (document level)
	 */
	protected native String pdfInfo(long handle, String key);
	protected native int[] pdfEncryptInfo(long handle);

	/*
	 * Rendering Functions (all page level)
	 */
	protected native ByteBuffer getByteBuffer(long handle, float zoom, int rotate, int color, float gamma, int[] bbox, float x0, float y0, float x1, float y1);
	protected native void freeByteBuffer(long handle, ByteBuffer buffer);
	
	protected native int writePbm(long handle, int rotate, float zoom, float gamma, byte[] file);
	protected native int writePnm(long handle, int rotate, float zoom, int color, float gamma, byte[] file);
	protected native int writePam(long handle, int rotate, float zoom, int color, float gamma, byte[] file);
	protected native int writePng(long handle, int rotate, float zoom, int color, float gamma, byte[] file);
	protected native int writeTif(long handle, int rotate, float zoom, int color, float gamma, byte[] file, int compression, int mode, int quality);
	protected native int writeJPeg(long handle,int rotate, float zoom, int color, float gamma, byte[] file, int quality);
	protected native int writeBmp(long handle, int rotate, float zoom, int color, float gamma, byte[] file);

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
	 * is64Bit()
	 *
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
     * Print test messages
     * @param text
     */
    protected static void log(String text) {
    	System.out.println(text);
    }

}
