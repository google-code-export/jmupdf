package com.examples.pdf.tiling;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.pdf.PdfDocument;
import com.jmupdf.tiles.TileCache;
import com.jmupdf.tiles.TiledImage;

/**
 * Example of how to create image tiles of a page
 * 
 * @author Pedro Rivera
 *
 */
public class TilingTest {
	
	public static void main(String[] args) {
		try {

			// Open document
			PdfDocument doc = new PdfDocument("d:\\tmp\\itextinaction.pdf", 10);

			// Get page object
			Page page = new Page(doc, 1);

			// setup zoom, rotation, color, and tile info
			float zoom = 3f;
			int rotate = Page.PAGE_ROTATE_90;
			ImageType color = ImageType.IMAGE_TYPE_RGB;
			int tilew = 512;
			int tileh = 512;

			// Create tile cache object
			TileCache cache = new TileCache(page, color, rotate, zoom, tilew, tileh);

			// Loop through tiles and save
			for (TiledImage t : cache.getTiles()) {	
				t.render();
				ImageIO.write(t.getImage(), "PNG", new File("d:\\tmp\\img\\test1_" + t.getTileY() + "_" + t.getTileX() + ".png"));
				t.dispose();
			}

			log("done!");

			// Dispose
			page.dispose();
			cache.dispose();
			doc.dispose();

		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
    /**
     * Print test messages
     * @param text
     */
    protected static void log(String text) {
    	System.out.println(text);
    }
}
