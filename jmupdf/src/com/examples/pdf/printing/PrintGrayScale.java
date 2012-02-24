package com.examples.pdf.printing;

import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrintQuality;

import com.jmupdf.exceptions.DocException;
import com.jmupdf.exceptions.DocSecurityException;
import com.jmupdf.print.PrintServices;

/**
 * How to print black & white
 *
 */
public class PrintGrayScale {

	public static void main(String[] args) {
		int copies = 1;
		int startPage = 1;
		int endPage = 3;
		boolean showDialog = false;

		try {

			// Create new print object
			PrintServices p = new PrintServices("C:\\Users\\Pedro\\Downloads\\test-2.pdf", "");

			// Override default resolution
			p.setCustomResolution(150);
			p.setStretching(false);
			p.setAutoRotate(true);
			
			// Print it
			p.print("Test job", 
					"HP LaserJet 1150", 
					copies, 
					startPage, 
					endPage, 
					MediaSizeName.NA_LETTER,
					PrintQuality.NORMAL,  
					Chromaticity.MONOCHROME,  
					OrientationRequested.LANDSCAPE,  
					showDialog);

			// Wait here until printing is done
			p.waitForPrintJobDone();
			
		} catch (DocException e) {
			e.printStackTrace();
		} catch (DocSecurityException e) {
			e.printStackTrace();
		}
		
	}

}
