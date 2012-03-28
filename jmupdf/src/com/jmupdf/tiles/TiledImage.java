/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.tiles;

import java.awt.image.BufferedImage;

import com.jmupdf.interfaces.Page;
import com.jmupdf.interfaces.PagePixels;
import com.jmupdf.page.PageRect;

/**
 * TiledImage Class
 * 
 * Represents a single tiled image
 * 
 * @author Pedro J Rivera
 *
 */
public class TiledImage {
	private PagePixels pagePixels;
	private PageRect tileRect;
	private PageRect pixRect;
	private int tilex;
	private int tiley;
	
	/**
	 * TiledImage Class
	 * @param tilex
	 * @param tiley
	 * @param tilew
	 * @param tileh
	 * @param pagePixels
	 */
	public TiledImage(PagePixels pagePixels, int tilex, int tiley, int tilew, int tileh) {
		this.tilex = tilex;
		this.tiley = tiley;
		this.tileRect = new PageRect(tilex * tilew, tiley * tileh, tilew, tileh);
		this.pixRect = new PageRect();		
		this.pagePixels = pagePixels.clone();
		normalize();
	}

	/**
	 * Get image
	 * @return
	 */
	public BufferedImage getImage() {
		return getPagePixels().getImage();
	}
	
	/**
	 * Render tile image
	 */
	public void render() {
		getPagePixels().drawPage(
				 null,
				 pixRect.getX0() / getPagePixels().getOptions().getZoom(), 
				 pixRect.getY0() / getPagePixels().getOptions().getZoom(), 
				 pixRect.getX1() / getPagePixels().getOptions().getZoom(), 
				 pixRect.getY1() / getPagePixels().getOptions().getZoom() );
	}

	/**
	 * Get tile x position
	 * @return
	 */
	public int getTileX() {
		return tilex;
	}

	/**
	 * Get tile y position
	 * @return
	 */
	public int getTileY() {
		return tiley;
	}

	/**
	 * Get tile x coordinate
	 * @return
	 */
	public int getX() {
		return tileRect.getX();
	}

	/**
	 * Get tile y coordinate
	 * @return
	 */
	public int getY() {
		return tileRect.getY();
	}

	/**
	 * Get tile width
	 * @return
	 */
	public int getWidth() {
		return tileRect.getWidth();
	}

	/**
	 * Get tile height
	 * @return
	 */
	public int getHeight() {
		return tileRect.getHeight();
	}

	/**
	 * Dispose of resources
	 */
	public void dispose() {
		tileRect = null;
		pixRect = null;
		pagePixels.getPage().dispose();
		pagePixels.dispose();
	}

	/**
	 * Normalize rendering coordinates
	 */
	private void normalize() {
		
		// Rotate bound box to actual rotation
		PageRect bb = getPage().getBoundBox().scale(getPagePixels().getOptions().getZoom());
		PageRect rb = bb.rotate(bb, getPagePixels().getOptions().getRotate());

		// Adjust width of tile
		int x1 = rb.getWidth();
		int y1 = rb.getHeight();
		int x2 = getX() + getWidth();
		int y2 = getY() + getHeight();		
		tileRect.setRect(tileRect.getX(), tileRect.getY(), (Math.min(x1, x2) - getX()), (Math.min(y1, y2) - getY()));

		// Rotate to default page rotation
		pixRect.setRect(getX(), getY(), getWidth(), getHeight());
		pixRect = pixRect.rotate(bb, getPagePixels().getOptions().getRotate(), Page.PAGE_ROTATE_NONE);
	}

	/**
	 * Get page object
	 * @return
	 */
	private Page getPage() {
		return getPagePixels().getPage();
	}
	
	/**
	 * Get page renderer
	 * @return
	 */
	private PagePixels getPagePixels() {
		return pagePixels;
	}
	
    /**
     * Print test messages
     * @param text
     */
    protected void log(String text) {
    	System.out.println(text);
    }
    
}
