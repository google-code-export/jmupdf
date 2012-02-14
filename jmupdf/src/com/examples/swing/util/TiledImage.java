package com.examples.swing.util;

import java.awt.image.BufferedImage;

import com.jmupdf.page.Page;
import com.jmupdf.page.PageRect;
import com.jmupdf.page.PageRenderer;

public class TiledImage {
	private PageRenderer renderer;
	private int tilex;
	private int tiley;
	private int x;
	private int y;
	private int w;
	private int h;
	private float currentZoom;
	
	private int rx, ry, rw, rh;
	
	public TiledImage(int tilex, int tiley, int tilew, int tileh) {
		this.tilex = tilex;
		this.tiley = tiley;
		this.w = tilew;
		this.h = tileh;
		this.x = tilex*(tileh);
		this.y = tiley*(tilew);
		this.currentZoom = 1f;
	}

	public BufferedImage getImage() {
		return getRenderer().getImage();
	}

	public PageRenderer getRenderer() {
		return renderer;
	}

	public boolean isTileRendered() {
		if (getRenderer() == null) {
			return false;
		} 
		return getRenderer().isPageRendered();
	}
	
	public boolean isTileRendering() {
		if (getRenderer() == null) {
			return false;
		} 
		return getRenderer().isPageRendering();
	}
	
	public void setData(Page page, float zoom, int rotate, int color) {
		
		if (currentZoom != zoom || renderer == null) {
			
			if (this.renderer != null) {
				this.renderer.dispose();
			}

			this.currentZoom = zoom;
			this.renderer = new PageRenderer(page, zoom, rotate, color);
			
			// Rotate mediabox to actual rotation
			PageRect m = page.getBoundBox().scale(zoom);
			//m = m.rotate(m, renderer.getNormalizedRotation());

			// Adjust width of tile
			int x1 = (int)(m.getWidth());
			int y1 = (int)(m.getHeight());
			int x2 = getX() + getWidth();
			int y2 = getY() + getHeight();
			this.w = Math.min(x1, x2) - getX();
			this.h = Math.min(y1, y2) - getY();

			// Rotate to default page rotation
			PageRect r = new PageRect(getX(), getY(), getX()+getWidth(), getY()+getHeight());
			//r = r.rotate(m, -(this.renderer.getNormalizedRotation()-page.getRotation()));
			//m = m.rotate(m, -(this.renderer.getNormalizedRotation()-page.getRotation()));

			// Assign new rendering coordinates
			this.rx = (int)r.getX();
			this.ry = (int)r.getY();
			this.rw = (int)r.getWidth();
			this.rh = (int)r.getHeight();
			
		}
	}
	
	public void render() {
		if (!getRenderer().isPageRendered() && !getRenderer().isPageRendering()) {
			this.renderer.setCroppingArea(
					 this.rx/this.renderer.getZoom(), 
					 this.ry/this.renderer.getZoom(), 
					(this.rx+this.rw)/this.renderer.getZoom(), 
					(this.ry+this.rh)/this.renderer.getZoom());
			this.renderer.render(true);
		}
	}

	public int getTileX() {
		return tilex;
	}

	public int getTileY() {
		return tiley;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return w;
	}

	public int getHeight() {
		return h;
	}
	
	public void dispose() {
		if (getRenderer() != null) {
			renderer.dispose();
			renderer = null;
		}
	}

    /**
     * Print test messages
     * @param text
     */
    protected void log(String text) {
    	System.out.println(text);
    }
    
}
