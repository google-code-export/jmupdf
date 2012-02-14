/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.tiles;

import java.awt.image.BufferedImage;

import com.jmupdf.page.Page;
import com.jmupdf.page.PagePixels;
import com.jmupdf.page.PageRect;

/**
 * TiledImage
 * 
 * Represents a single tiled image
 * 
 * @author Pedro J Rivera
 *
 */
public class TiledImage {
	private PagePixels pagePixels;
	private int tilex;
	private int tiley;
	private int x;
	private int y;
	private int w;
	private int h;
	
	private int rx, ry, rw, rh;
	
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
		this.w = tilew;
		this.h = tileh;
		this.x = tilex*(tilew);
		this.y = tiley*(tileh);		
		this.pagePixels = (PagePixels)pagePixels.clone();
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
				 this.rx/getPagePixels().getZoom(), 
				 this.ry/getPagePixels().getZoom(), 
				(this.rx+this.rw)/getPagePixels().getZoom(), 
				(this.ry+this.rh)/getPagePixels().getZoom());
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
		return x;
	}

	/**
	 * Get tile y coordinate
	 * @return
	 */
	public int getY() {
		return y;
	}

	/**
	 * Get tile width
	 * @return
	 */
	public int getWidth() {
		return w;
	}

	/**
	 * Get tile height
	 * @return
	 */
	public int getHeight() {
		return h;
	}

	/**
	 * Dispose of resources
	 */
	public void dispose() {
		if (getPagePixels() != null) {
			getPagePixels().dispose();
		}
	}

	/**
	 * Normalize rendering coordinates
	 */
	private void normalize() {
		
		// Rotate bound box to actual rotation
		PageRect bb = getPage().getBoundBox().scale(getPagePixels().getZoom());
		PageRect rb = bb.rotate(bb, getPagePixels().getRotation());

		// Adjust width of tile
		int x1 = rb.getWidth();
		int y1 = rb.getHeight();
		int x2 = getX() + getWidth();
		int y2 = getY() + getHeight();
		this.w = Math.min(x1, x2) - getX();
		this.h = Math.min(y1, y2) - getY();

		// Rotate to default page rotation
		PageRect r = new PageRect(getX(), getY(), getWidth(), getHeight());
		r = r.rotateTo(bb, getPagePixels().getRotation(), getPage().getRotation());

		// Assign new rendering coordinates
		this.rx = r.getX();
		this.ry = r.getY();
		this.rw = r.getWidth();
		this.rh = r.getHeight();
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
