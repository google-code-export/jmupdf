package com.examples.pdf.tiling;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.page.PageRenderer;
import com.jmupdf.pdf.PdfDocument;
import com.jmupdf.tiles.TileCache;
import com.jmupdf.tiles.TiledImage;

/**
 * Example of how to create image tiles of a page
 * 
 * @author Pedro Rivera
 *
 */
public class Tiling {
	
	public static void main(String[] args) {
		try {

			// Open document
			PdfDocument pdfDoc = new PdfDocument("E:\\tmp\\itextinaction.pdf", 10);

			// Get page object
			Page page = pdfDoc.getPage(12);

			// setup zoom, rotation, color, and tile info
			float zoom = 3f;
			int rotate = Page.PAGE_ROTATE_90_CW;
			int color = PageRenderer.IMAGE_TYPE_RGB;
			int tilew = 512;
			int tileh = 512;

			// Create tile cache object
			TileCache c = new TileCache(page, color, rotate, zoom, tilew, tileh);

			// Loop through tiles and save
			for (TiledImage t : c.getTiles()) {	
				t.render();
				ImageIO.write(t.getImage(), "PNG", new File("e:\\tmp\\img\\test1_" + t.getTileY() + "_" + t.getTileX() + ".png"));
				t.dispose();
			}

			log("done!");

			// Dispose
			c.dispose();
			pdfDoc.dispose();

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
