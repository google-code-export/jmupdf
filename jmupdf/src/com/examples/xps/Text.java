package com.examples.xps;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.page.Page;
import com.jmupdf.xps.XpsDocument;

/**
 * Sample get page text
 * 
 * @author Pedro J Rivera
 *
 */
public class Text {

	public static void main(String[] args) {

		try {
			
			/*
			 * Open document
			 */
			XpsDocument xps = new XpsDocument("d:\\test01.xps");
			
			/*
			 * Get page
			 */
			Page page = xps.getPage(1);

			/*
			 * Get text, if any
			 */
			String text = page.getText();
			
			/*
			 * Display out data
			 */
			System.out.println(text);
			
			/*
			 * Dispose
			 */
			xps.dispose();
			
		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		}
		
	}
	
}
