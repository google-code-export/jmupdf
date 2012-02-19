package com.examples.pdf.crop;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jmupdf.enums.ImageType;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.page.PageRenderer;
import com.jmupdf.pdf.PdfDocument;

/**
 * Cropping example
 * 
 */
public class Cropping {

	public static void main(String[] args) {

		try {
			
			/*
			 * Open document
			 */
			PdfDocument pdfDoc = new PdfDocument("E:\\development\\indigo\\workspace_jmupdf-old\\pdf_tests\\test05.pdf", "");

			/*
			 * Get page object
			 */
			Page page = pdfDoc.getPage(1);

			/*
			 * Get rendering object
			 */
			PageRenderer render = new PageRenderer(page, 1f, Page.PAGE_ROTATE_90, ImageType.IMAGE_TYPE_RGB);
			render.setCroppingArea(560f,50f,755f,100f);
			//render.setCroppingArea(40f,500f,218f,564f);
			//512,575,752,563
			//78.0, 287.0, 146.0, 306.0
			//146.0, 289.0, 474.0, 305.0
			render.render(true);
			
			/*
			 * Save to disk for review
			 */
			ImageIO.write(render.getImage(), "PNG", new File("e:\\tmp\\test1.png"));

			/*
			 * Dispose
			 */
			render.dispose();
			pdfDoc.dispose();

		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
}
