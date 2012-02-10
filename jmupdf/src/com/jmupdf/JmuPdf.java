/*
 *  
 * See copyright file
 * 
 */

package com.jmupdf;

import java.nio.ByteBuffer;

import com.jmupdf.document.Outline;
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
	private static final String jmupdf_version = "0.3.1-beta";

	/*
	 * Open DLL dependency
	 */
	static {
		loadDll();
	}

	/*
	 * PDF/XPS Common Functions 
	 */
	protected native synchronized long open(int type, String pdf, String password, int maxStore);
	protected native synchronized void close(long handle);
	protected native synchronized int getVersion(long handle);
	protected native synchronized int getPageCount(long handle);
	protected native synchronized float[] loadPage(long handle, int page);
	protected native synchronized PageText[] getPageText(long handle, int page, float zoom, int rotate, int x0, int y0, int x1, int y1);
	protected native synchronized Outline getOutline(long handle);
	protected native synchronized PageLinks[] getPageLinks(long handle, int page);
	
	/*
	 * PDF Specific Functions
	 */
	protected native synchronized String pdfInfo(long handle, String key);
	protected native synchronized int[] pdfEncryptInfo(long handle);

	/*
	 * Rendering Functions 
	 */
	protected native synchronized Object getPixMap(long handle, int page, float zoom, int rotate, int color, float gamma, int[] bbox, float x0, float y0, float x1, float y1);
	protected native synchronized ByteBuffer getByteBuffer(long handle, int page, float zoom, int rotate, int color, float gamma, int[] bbox, float x0, float y0, float x1, float y1);
	protected native synchronized void freeByteBuffer(long handle, ByteBuffer buffer);
	
	protected native synchronized int getAntiAliasLevel(long handle);
	protected native synchronized int setAntiAliasLevel(long handle, int antiAliasLevel);
	
	protected native synchronized int writePbm(long handle, int page, float zoom, float gamma, String file);
	protected native synchronized int writePnm(long handle, int page, float zoom, int color, float gamma, String file);
	protected native synchronized int writePam(long handle, int page, float zoom, int color, float gamma, String file);
	protected native synchronized int writePng(long handle, int page, float zoom, int color, float gamma, String file);
	protected native synchronized int writeTif(long handle, int page, float zoom, int color, float gamma, String file, int compression, int mode, int quality);
	protected native synchronized int writeJPeg(long handle, int page, float zoom, int color, float gamma, String file, int quality);
	protected native synchronized int writeBmp(long handle, int page, float zoom, int color, float gamma, String file);
	
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
