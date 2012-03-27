/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.interfaces;

import com.jmupdf.page.PageLinks;
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
	 * Get original page rotation. </br>
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
	 * All coordinates are assumed to be in 1f zoom and 0 rotation. </br>
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	PageText[] getTextSpan(PageRect rect);

	/**
	 * Get PageLinks Array Object </br>
	 * Optionally pass in a PageRendererOptions object to determine how to extract links. </br>
	 * @param pagePixels : can be null
	 * @return
	 */
	PageLinks[] getLinks(PageRendererOptions options);
	
	/**
	 * Get PagePixels object
	 * @return
	 */
	PagePixels getPagePixels();
	
	/**
	 * Save page as an image file 
	 * @param file the file to save to 
	 * @param options the complete description of the image to generate
	 * @return true upon success, false otherwise
	 */
	boolean saveAsImage(String file, PageRendererOptions options);

	/**
	 * Save page as a byte array
	 * @param options
	 * @return
	 */
	byte[] saveAsImage(PageRendererOptions options);

	/**
	 * Get a page rendering object
	 * @return
	 */
	PageRendererOptions getRenderingOptions();
	
	/**
	 * Dispose of page resources
	 */
	void dispose();

}
