package com.examples.swing.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.util.ArrayList;

import com.jmupdf.page.Page;
import com.jmupdf.page.PageRect;
import com.jmupdf.page.PageRenderer;

public class TileCache {
	private Page page;
	private int rotate;
	private int color;
	private float zoom;

	private ArrayList<TiledImage> tiles = new ArrayList<TiledImage>();
	
	private final int tilew = 512;
	private final int tileh = 512;
	
	public TileCache(Page page, int color, int rotate, float zoom) {
		this.page = page;
		this.color = color;
		this.rotate = rotate;
		this.zoom = zoom;
		
		// Create temporary renderer
		PageRenderer pr = new PageRenderer(page, zoom, rotate, color);
		
		// Rotate page as we want to display it
		PageRect m = page.getBoundBox().scale(zoom);
		//m = m.rotate(m, pr.getNormalizedRotation());

		// Calculate tiles based on rotation
		int w = (int)(m.getWidth());
		int h = (int)(m.getHeight());
		int tilesx = w/tilew;
		int tilesy = h/tileh;
		
		// Determine if we need additional x, y tiles		
		if (w%tilew > 0)
			tilesx++;
		if (h%tileh > 0)
			tilesy++;

		// Create tile objects
		for (int y=0; y<tilesy; y++) {
			for (int x=0; x<tilesx; x++) {
				TiledImage ti = new TiledImage(x, y, tilew, tileh);
				ti.setData(page, zoom, rotate, color);
				tiles.add(ti);				
			}
		}
		
		pr.dispose();
		pr = null;
		
	}

	public ArrayList<TiledImage> getTiles() {
		return tiles;
	}

	public Page getPage() {
		return page;
	}

	public int getRotate() {
		return rotate;
	}

	public int getColor() {
		return color;
	}

	public float getZoom() {
		return zoom;
	}

	public int getTilew() {
		return tilew;
	}

	public int getTileh() {
		return tileh;
	}

	public void paint(Graphics g, int x, int y, Rectangle r, ImageObserver io) {

		Graphics2D g2 = (Graphics2D) g;

		int rx1, ry1, rx2, ry2;
		int tx1, ty1, tx2, ty2;

		for (TiledImage tile : tiles) {
			rx1 = (int)r.getX() - getTileh();
			ry1 = (int)r.getY() - getTilew();
			rx2 = (int)(r.getX() + r.getWidth() + getTilew());
			ry2 = (int)(r.getY() + r.getHeight() + getTileh());
			
			tx1 = (int)tile.getX();
			ty1 = (int)tile.getY();
			tx2 = (int)(tile.getX() + tile.getWidth());
			ty2 = (int)(tile.getY() + tile.getHeight());

			if (tx1 >= rx1 && tx2 <= rx2 && ty1 >= ry1 && ty2 <= ry2) {  
				tile.render();
				g2.drawImage(tile.getImage(), x+tile.getX(), y+tile.getY(), io);
			}

		}

	}

	public void dispose() {
		for (TiledImage tile : tiles) {
			tile.dispose();
		}
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
