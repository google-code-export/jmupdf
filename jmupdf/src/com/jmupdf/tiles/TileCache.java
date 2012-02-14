/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.tiles;

import java.util.ArrayList;

import com.jmupdf.page.Page;
import com.jmupdf.page.PagePixels;
import com.jmupdf.page.PageRect;

/**
 * TileCache 
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
	public TileCache(Page page, int color, int rotate, float zoom, int tilew, int tileh) {
		this.tilew = tilew;
		this.tileh = tileh;
		
		PagePixels pagePixels = new PagePixels(page);
		pagePixels.setZoom(zoom);
		pagePixels.setRotate(rotate);
		pagePixels.setColor(color);
		
		// Rotate page as we want to display it
		PageRect m = page.getBoundBox().scale(zoom);
		m = m.rotate(m, pagePixels.getRotation());

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

    
    
//	public void paint(Graphics g, int x, int y, Rectangle r, ImageObserver io) {
//
//		Graphics2D g2 = (Graphics2D) g;
//
//		int rx1, ry1, rx2, ry2;
//		int tx1, ty1, tx2, ty2;
//
//		for (TiledImage tile : tiles) {
//			rx1 = (int)r.getX() - getTileh();
//			ry1 = (int)r.getY() - getTilew();
//			rx2 = (int)(r.getX() + r.getWidth() + getTilew());
//			ry2 = (int)(r.getY() + r.getHeight() + getTileh());
//			
//			tx1 = (int)tile.getX();
//			ty1 = (int)tile.getY();
//			tx2 = (int)(tile.getX() + tile.getWidth());
//			ty2 = (int)(tile.getY() + tile.getHeight());
//
//			if (tx1 >= rx1 && tx2 <= rx2 && ty1 >= ry1 && ty2 <= ry2) {  
//				tile.render();
//				g2.drawImage(tile.getImage(), x+tile.getX(), y+tile.getY(), io);
//			}
//
//		}
//
//	}
    
}
