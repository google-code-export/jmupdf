/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.tiles;

import java.util.ArrayList;

import com.jmupdf.enums.ImageType;
import com.jmupdf.interfaces.Page;
import com.jmupdf.interfaces.PagePixels;
import com.jmupdf.page.PageRect;

/**
 * TileCache Class
 * 
 * Maintains a list of TiledImage objects to be rendered later.
 * 
 * @author Pedro J Rivera
 * 
 */
public class TileCache {
	private int tilew;
	private int tileh;
	private ArrayList<TiledImage> tiles = new ArrayList<TiledImage>();
	
	/**
	 * TileCache Class
	 * @param page
	 * @param color
	 * @param rotate
	 * @param zoom
	 * @param tilew
	 * @param tileh
	 */
	public TileCache(Page page, ImageType color, int rotate, float zoom, int tilew, int tileh) {
		this.tilew = tilew;
		this.tileh = tileh;
		
		PagePixels pagePixels = page.getPagePixels();
		pagePixels.getOptions().setZoom(zoom);
		pagePixels.getOptions().setRotate(rotate);
		pagePixels.getOptions().setImageType(color);
		
		// Rotate page as we want to display it
		PageRect m = page.getBoundBox().scale(zoom);
		m = m.rotate(m, rotate);

		// Calculate tiles based on rotation
		int w = m.getWidth();
		int h = m.getHeight();
		int tilesx = w / tilew;
		int tilesy = h / tileh;
		
		// Determine if we need additional x, y tiles		
		if (w % tilew > 0) {
			tilesx++;
		}
		if (h % tileh > 0) {
			tilesy++;
		}

		// Create TiledImage objects
		// Images are not rendered here. I am just establishing tile data.
		for (int y=0; y<tilesy; y++) {
			for (int x=0; x<tilesx; x++) {
				tiles.add(new TiledImage(pagePixels, x, y, tilew, tileh));				
			}
		}
		
		pagePixels.dispose();
	}

	/**
	 * Get array list of tiles
	 * @return
	 */
	public ArrayList<TiledImage> getTiles() {
		return tiles;
	}

	/**
	 * Get image width
	 * @return
	 */
	public int getTilew() {
		return tilew;
	}

	/**
	 * Get image height
	 * @return
	 */
	public int getTileh() {
		return tileh;
	}

	/**
	 * Dispose of tiled images
	 */
	public void dispose() {
		for (TiledImage tile : tiles) {
			tile.dispose();			
		}
		tiles.removeAll(tiles);
		tiles = null;
	}

    /**
     * Print test messages
     * @param text
     */
    protected void log(String text) {
    	System.out.println(text);
    }
    
}
