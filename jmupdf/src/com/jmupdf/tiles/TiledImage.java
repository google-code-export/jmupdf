/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.tiles;

import java.awt.image.BufferedImage;

import com.jmupdf.page.Page;
import com.jmupdf.page.PageRect;
import com.jmupdf.page.PageRenderer;

/**
 * TiledImage
 * 
 * Represents a single tiled image
 * 
 * @author Pedro J Rivera
 *
 */
public class TiledImage {
	private PageRenderer renderer;
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
	 * @param renderer
	 */
	public TiledImage(int tilex, int tiley, int tilew, int tileh, PageRenderer renderer) {
		this.tilex = tilex;
		this.tiley = tiley;
		this.w = tilew;
		this.h = tileh;
		this.x = tilex*(tilew);
		this.y = tiley*(tileh);
		this.renderer = renderer;
		normalize();
	}

	/**
	 * Get image
	 * @return
	 */
	public BufferedImage getImage() {
		return getRenderer().getImage();
	}

	/**
	 * Determine if image is already rendered
	 * @return
	 */
	public boolean isTileRendered() {
		if (getRenderer() == null) {
			return false;
		} 
		return getRenderer().isPageRendered();
	}
	
	/**
	 * Determine if image is rendering
	 * @return
	 */
	public boolean isTileRendering() {
		if (getRenderer() == null) {
			return false;
		} 
		return getRenderer().isPageRendering();
	}
	
	/**
	 * Render tile image
	 */
	public void render() {
		if (!getRenderer().isPageRendered() && !getRenderer().isPageRendering()) {
			getRenderer().setCroppingArea(
					 this.rx/getRenderer().getZoom(), 
					 this.ry/getRenderer().getZoom(), 
					(this.rx+this.rw)/getRenderer().getZoom(), 
					(this.ry+this.rh)/getRenderer().getZoom());
			getRenderer().render(true);
		}
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
		if (getRenderer() != null) {
			getRenderer().needsRendering();
		}
	}

	/**
	 * Normalize rendering coordinates
	 */
	private void normalize() {
		
		// Rotate mediabox to actual rotation
		PageRect m = getPage().getMediaBox().scale(getRenderer().getZoom());
		m = m.rotate(m, getRenderer().getNormalizedRotation());

		// Adjust width of tile
		int x1 = m.getWidth();
		int y1 = m.getHeight();
		int x2 = getX() + getWidth();
		int y2 = getY() + getHeight();
		this.w = Math.min(x1, x2) - getX();
		this.h = Math.min(y1, y2) - getY();

		// Rotate to default page rotation
		PageRect r = new PageRect(getX(), getY(), getX()+getWidth(), getY()+getHeight());
		r = r.rotate(m, -(getRenderer().getNormalizedRotation()-getPage().getRotation()));
		m = m.rotate(m, -(getRenderer().getNormalizedRotation()-getPage().getRotation()));

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
		return getRenderer().getPage();
	}
	
	/**
	 * Get page renderer
	 * @return
	 */
	private PageRenderer getRenderer() {
		return renderer;
	}
	
    /**
     * Print test messages
     * @param text
     */
    protected void log(String text) {
    	System.out.println(text);
    }
    
}
