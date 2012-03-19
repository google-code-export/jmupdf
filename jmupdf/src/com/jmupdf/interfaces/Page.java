/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.interfaces;

import com.jmupdf.enums.ImageType;
import com.jmupdf.enums.TifCompression;
import com.jmupdf.enums.TifMode;
import com.jmupdf.page.PageLinks;
import com.jmupdf.page.PagePixels;
import com.jmupdf.page.PageRect;
import com.jmupdf.page.PageText;

/**
 * Page class.
 * 
 * @author Pedro J Rivera
 *
 */
public interface Page {
	
	public static final int PAGE_ROTATE_AUTO = -1;
	public static final int PAGE_ROTATE_NONE = 0;
	public static final int PAGE_ROTATE_90 = 90;
	public static final int PAGE_ROTATE_180 = 180;
	public static final int PAGE_ROTATE_270 = 270;
	public static final int PAGE_ROTATE_360 = 360;
	
	/**
	 * Get document handle
	 * 
	 * @return
	 */
	long getHandle();
	
	/**
	 * Get page number
	 * @return
	 */
	int getPageNumber();

	/**
	 * Get page bound box.
	 * @return
	 */
	PageRect getBoundBox();

	/**
	 * Get page x
	 * @return
	 */
	int getX();
	
	/**
	 * Get page y
	 * @return
	 */
	int getY();
	
	/**
	 * Get page width
	 * @return
	 */
	int getWidth();

	/**
	 * Get page height
	 * @return
	 */
	int getHeight();

	/**
	 * Get original page rotation.
	 * This is the rotation as it is saved in the document
	 * @return
	 */
	int getRotation();

	/**
	 * Get document this page belongs to
	 * @return
	 */
	Document getDocument();

	/**
	 * Get TextSpan Array Object. </br></br>
	 * 
	 * All coordinates are assumed to be in 1f zoom and 0 rotation. </br>
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	PageText[] getTextSpan(PageRect rect);

	/**
	 * Get PageLinks Array Object </br>
	 * Optionally pass in a PagePixel object to determine how to extract links. </br>
	 * 
	 * @param pagePixels : can be null
	 * @return
	 */
	PageLinks[] getLinks(PagePixels pagePixels);
	
	/**
	 * Dispose of page resources
	 */
	void dispose();

	
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/* saveAsXXX() methods.                      */
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

	
	/**
	 * Save a page to a file in PNG format
	 * 
	 * @param file
	 * @param rotate
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @return
	 */
	boolean saveAsPng(String file, int rotate, float zoom, ImageType color, float gamma, int aa);
	
	/**
	 * Save a page to a file in PBM format</br>
	 * PBM is the portable bitmap format, a lowest common denominator monochrome file format. 
	 * @param file
	 * @param rotate
	 * @param zoom
	 * @param gamma
	 * @return
	 */
	boolean saveAsPbm(String file, int rotate, float zoom, float gamma, int aa);
	
	/**
	 * Save a page to a file in PNM format
	 * 
	 * @param file
	 * @param rotate
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @return
	 */
	boolean saveAsPnm(String file, int rotate, float zoom, ImageType color, float gamma, int aa);

	/**
	 * Save a page to a file in JPEG format
	 * 
	 * @param file
	 * @param rotate
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @param quality : quality levels are in the range 0-100 with a default value of 75.
	 * @return
	 */
	boolean saveAsJPeg(String file, int rotate, float zoom, ImageType color, float gamma, int aa, int quality);
	
	/**
	 * Save a page to a BMP format
	 * 
	 * @param file
	 * @param rotate
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @return
	 */
	boolean saveAsBmp(String file, int rotate, float zoom, ImageType color, float gamma, int aa);
	
	/**
	 * Save a page to a file in PAM format</br>
	 * The name "PAM" is an acronym derived from "Portable Arbitrary Map"
	 * 
	 * @param file
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @return
	 */
	boolean saveAsPam(String file, int rotate, float zoom, ImageType color, float gamma, int aa);
	
	/**
	 * Save a page to a TIF format
	 * 
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @param compression
	 * @param mode
	 * @param quality <br/><br/>
	 *        
	 *        <blockquote>
	 *        <strong>When compression == TIF_COMPRESSION_ZLIB <br/></strong> 
	 *          Control  the  compression  technique used by the Deflate codec.  <br/> 
	 *          Quality levels are in the range 1-9 with larger numbers yielding <br/> 
	 *          better compression at the cost of more computation. The default  <br/> 
	 *          quality level is 6 which yields a good time-space tradeoff.      <br/><br/>
	 *          
	 *        <strong>When compression == TIF_COMPRESSION_JPEG <br/></strong>
	 *          Control the compression quality level used in the baseline algo- <br/>
	 *          rithm. Note that quality levels are in the range 0-100 with a    <br/>
	 *          default value of 75. <br/><br/>
	 *        </blockquote>  
	 * @return
	 */
	boolean saveAsTif(String file, int rotate, float zoom, ImageType color, float gamma, int aa, TifCompression compression, TifMode mode, int quality);
		
}
