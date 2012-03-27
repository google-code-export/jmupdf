/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.interfaces;

import java.awt.image.BufferedImage;

/**
 * PagePixels Interface
 * 
 * This interface represents pixel/image data for a given page.
 * 
 * @author Pedro J Rivera
 *
 */
public interface PagePixels {
    
	/**
	 * Get page object
	 * @return
	 */
	public Page getPage();
	
	/**
	 * Get page rendering options object
	 * @return
	 */
	public PageRendererOptions getOptions();

	/**
	 * Get buffered image
	 * @return
	 */
	public BufferedImage getImage();

	/**
	 * Get pixel data
	 * @return
	 */
	public Object getPixels();

	/**
	 * Draw page image. </br></br>
	 * 
	 * If PageRendererOptions object is null then all coordinates are assumed to be in </br>
	 * 1f zoom and 0 rotation. Otherwise the coordinates passed in must reflect </br>
	 * the zoom factor and rotation of the PageRendererOptions object passed in.</br></br>
	 * 
	 * @param pagePixels Optional 
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	public void drawPage(PageRendererOptions options, float x0, float y0, float x1, float y1);
	
	/**
	 * Returns a new copy of PagePixels object. </br></br>
	 * This also clones the page object therefore the page object also needs to be disposed.</br></br>
	 * 
	 * Example code:
	 * <blockquote>
	 * PagePixels pix = pagePixels.clone(); </br>
	 * pix.getPage().dispose(); </br>
	 * pix.dispose(); </br>
	 * </blockquote>
	 */
	public PagePixels clone();

	/**
	 * Dispose of resources
	 */
	public void dispose();

}
