package com.examples.pdf.general;

import com.jmupdf.enums.DictionaryType;
import com.jmupdf.enums.ImageType;
import com.jmupdf.enums.TifCompression;
import com.jmupdf.enums.TifMode;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.page.PageRenderer;
import com.jmupdf.pdf.PdfDocument;
import com.jmupdf.pdf.PdfEncrypt;

/**
 * Example JMuPDF uses
 * 
 * @author Pedro J Rivera
 *
 */
public class GeneralTest {

	public static void main(String[] args) {
		
		try {
			String doc = "c:\\tmp\\myreport.pdf";
			String doc2 = "c:\\tmp\\2.pdf";
			String png = "c:\\tmp\\2.png";
			String tif = "c:\\tmp\\2.tif";
			float zoom = 1f;
			int antiAliasLevel = 8;
			
			/*
			 * Open document
			 */
			PdfDocument pdfDoc = new PdfDocument(doc, "p123");
			PdfDocument pdfDoc2 = new PdfDocument(doc2, "p123");

			/*
			 * Set anti alias level
			 */
			pdfDoc.setAntiAliasLevel(antiAliasLevel);
			pdfDoc2.setAntiAliasLevel(antiAliasLevel);

			log("-----------------");
			log("Total Pages : " + pdfDoc.getPageCount());
			log("Version : " + pdfDoc.getInformation().getVersion());
			
			/*
			 * Get some info
			 */
			String value;
			value = pdfDoc.getInfo(DictionaryType.INFO_TITLE);			
			log("Title: " + value);
			value = pdfDoc.getInfo(DictionaryType.INFO_AUTHOR);			
			log("Author: " + value);
			value = pdfDoc.getInfo(DictionaryType.INFO_MODIFIED_DATE);			
			log("Mod Date: " + value);
			value = pdfDoc.getInfo(DictionaryType.INFO_CREATION_DATE);			
			log("Creation Date: " + value);			
			value = pdfDoc.getInfo(DictionaryType.INFO_PRODUCER);			
			log("Producer: " + value);
			value = pdfDoc.getInfo(DictionaryType.INFO_CREATOR);
			log("Creator: " + value);
			value = pdfDoc.getInfo(DictionaryType.INFO_KEYWORDS);
			log("Keywords: " + value);
			value = pdfDoc.getInfo(DictionaryType.INFO_SUBJECT);
			log("Subject: " + value);

			log("-----------------");
			
			/*
			 * get encryption info
			 */
			PdfEncrypt e = pdfDoc.getEncryptInfo();
	
			if(e.isEncrypted()) {
				log("Document is encrypted");	
			} else {
				log("Document NOT is encrypted");
			}			
			if (e.getCanPrint()){
				if(e.getCanPrintQuality()) {
					log("Printing: Fully Allowed");						
				} else {
					log("Printing: Low Resolution");
				}
			} else {
				log("Printing: Not Allowed");
			}
			log("Changing the document: " + (e.getCanModify()?"Allow":"Not Allowed"));
			log("Content copying or extraction: " + (e.getCanCopy()?"Allow":"Not Allowed"));				
			log("Authoring comments and form fields: " + (e.getCanNotes()?"Allow":"Not Allowed"));
			log("Form field fill-in or signing = " + (e.getCanFillForm()?"Allow":"Not Allowed"));
			log("Content Accessibility enabled = " + (e.getCanAccessibility()?"Allow":"Not Allowed"));
			log("Document assembly = " + (e.getCanAssemble()?"Allow":"Not Allowed"));
			log("-----------------");

			/*
			 * export page #1 as png file
			 */
			log("Begin PNG write...");
			int pageNumber = 1;
			if (pdfDoc.saveAsPng(
					pageNumber, 
					png, 
					zoom, 
					ImageType.IMAGE_TYPE_RGB))
				log("PNG file created!");
			else
				log("PNG file not created");
			
			/*
			 * export page #1 as tif file
			 */
			log("Begin TIF write...");
			if (pdfDoc.saveAsTif(
					pageNumber, 
					tif, 
					zoom, 
					ImageType.IMAGE_TYPE_RGB, 
					TifCompression.TIF_COMPRESSION_ZLIB, 
					TifMode.TIF_DATA_DISCARD,
					0))
				log("TIF file created!");
			else
				log("TIF file not created");
			
			/*
			 * Load a page object from pdf for viewing or whatever 
			 */
			log("Begin create page object...");
			Page p1 = pdfDoc.getPage(1);
			
			if(p1 != null) {
				log("Page width: " + p1.getWidth());
				log("Page height: " + p1.getHeight());
			}
			
			/*
			 * Create a rendering object
			 */
			PageRenderer render = new PageRenderer(p1, 1f, Page.PAGE_ROTATE_AUTO, ImageType.IMAGE_TYPE_RGB);
			
			log("-----------------");
			log("Creating image...");
			
			render.render(true);
			
			log("Image width: " + render.getWidth());			
			log("Image height: " + render.getHeight());
			log("Resolution: " + render.getResolution());
			log("Zoom factor: " + render.getZoom());
			log("Done.");
			
			render.dispose();
			
			log("Rendering object disposed!");
			log("-----------------");
			
			
			pdfDoc2.dispose();
			log("Document2 disposed!");
			pdfDoc.dispose();
			log("Document disposed!");
			
		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void log(String msg) {
		System.out.println(msg);
	}
	
}
