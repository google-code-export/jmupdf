/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.page;

import com.jmupdf.document.Document;

/**
 * Page class.
 * 
 * @author Pedro J Rivera
 *
 */
public class Page {
	private Document document;
	private PageRect origBoundBox;
	private PageRect normBoundBox;
	private PageLinks[] pageLinks;
	private int pageNumber;
	private int pageRotate;		

	public static final int PAGE_ROTATE_AUTO = -1;
	public static final int PAGE_ROTATE_NONE = 0;
	public static final int PAGE_ROTATE_90 = 90;
	public static final int PAGE_ROTATE_180 = 180;
	public static final int PAGE_ROTATE_270 = 270;
	public static final int PAGE_ROTATE_360 = 360;
	
	/**
	 * Create a new page object
	 * @param doc
	 * @param page
	 */
	public Page(Document doc, int page) {
		this(doc, doc.getPageInfo(page), page);
	}

	/**
	 * Create a new page object
	 * @param doc
	 * @param pageInfo
	 * @param page
	 */
	public Page(Document doc, float[] pageInfo, int page) {
		this.document = doc;
		this.pageNumber = page;
		loadPageInfo(pageInfo);
	}
	
	/**
	 * Get page number
	 * @return
	 */
	public int getPageNumber() {
		return pageNumber;
	}

	/**
	 * Get page bound box. This is the page's original
	 * coordinates without rotation.
	 * @return
	 */
	public PageRect getBoundBox() {
		return origBoundBox;
	}

	/**
	 * Get page x
	 * @return
	 */
	public int getX() {
		return normBoundBox.getX();
	}
	
	/**
	 * Get page y
	 * @return
	 */
	public int getY() {
		return normBoundBox.getY();
	}
	
	/**
	 * Get page width
	 * @return
	 */
	public int getWidth() {
		return normBoundBox.getWidth();
	}

	/**
	 * Get page height
	 * @return
	 */
	public int getHeight() {
		return normBoundBox.getHeight();
	}

	/**
	 * Get original page rotation.
	 * This is the rotation as it is saved in the document
	 * @return
	 */
	public int getRotation() {
		return pageRotate;
	}

	/**
	 * Get document this page belongs to
	 * @return
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Get text from page
	 * @return
	 */
	public String getText() {
		return getText(null, getX(), getY(), getWidth(), getHeight());
	}
	
	/**
	 * Get text from page. </br>
	 * Optionally pass in a PageRenderer object to determine how to extract text. </br>
	 * @param pagePixels : can be null
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public String getText(PagePixels pagePixels, int x, int y, int w, int h) {
		String text = "";

		PageText[] pdfTextSpan = getTextSpan(pagePixels, x, y, w, h);
		
		if (pdfTextSpan == null) {
			return text;
		}
		
		int len;
		
		for(int i=0; i<pdfTextSpan.length; i++) {
			text += pdfTextSpan[i].getText();
			if (pdfTextSpan[i].isEndOfLine()) {
				if (i == pdfTextSpan.length-1) {
					text += "\n";
				} else {
					 if ((pdfTextSpan[i].getY0() == pdfTextSpan[i+1].getY0())) {
						 len = pdfTextSpan[i+1].getX1() - pdfTextSpan[i].getX1();
						 if (len > 1) {
							 text += " ";
						 }
					 } else {
						 text += "\n";
					 }
				}
			}	
		}

		return text;
	}
	
	/**
	 * Get TextSpan Array Object. </br>
	 * Optionally pass in a PagePixel object to determine how to extract text. </br>
	 * @param pagePixels : can be null
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public PageText[] getTextSpan(PagePixels pagePixels, int x, int y, int w, int h) {

		// If no rendering object available then use defaults
		if (pagePixels == null) {
			return getDocument().getPageText(getPageNumber(), 1f, getRotation(),  x, y, x+w, y+h);
		}
		
		// Get rotation
		int rotate = pagePixels.getRotation();

		// Rotate bound box to current rotation
		PageRect m = getBoundBox().scale(pagePixels.getZoom());
		m = m.rotate(m, rotate);

		// Rotate to default page rotation
		PageRect r = new PageRect(x, y, w, h);		
		r = r.rotate(m, -(rotate-getRotation()));
		
		return getDocument().getPageText(getPageNumber(), pagePixels.getZoom(), getRotation(), (int)r.getX0(), (int)r.getY0(), (int)r.getX1(), (int)r.getY1());
	}

	/**
	 * Get PageLinks Array Object </br>
	 * Optionally pass in a PagePixel object to determine how to extract links. </br>
	 * @param pagePixels : can be null
	 * @return
	 */
	public PageLinks[] getLinks(PagePixels pagePixels) {
		if (pageLinks == null) {
			pageLinks = getDocument().getPageLinks(getPageNumber());
			if (pageLinks == null) {
				pageLinks = new PageLinks[1];
				pageLinks[0] = new PageLinks(0, 0, 0, 0, 0, "");
			} else {
				if (pagePixels != null) {
					int rotate = pagePixels.getRotation();
					PageRect rect = new PageRect();
					for (int i=0; i<pageLinks.length; i++) {						
						rect.setRect(pageLinks[i].getX0(), pageLinks[i].getY0(), 
									 pageLinks[i].getX1(), pageLinks[i].getY1());
						rect = rect.rotate(getBoundBox(), rotate);
						rect = rect.scale(pagePixels.getZoom());
						pageLinks[i].setX0(rect.getX0());
						pageLinks[i].setY0(rect.getY0());
						pageLinks[i].setX1(rect.getX1());
						pageLinks[i].setY1(rect.getY1());
					}
					rect = null;
				}
			}
		}
		return pageLinks;
	}

	/**
	 * Load page information
	 * @param pageInfo
	 */
	private void loadPageInfo(float[] pageInfo) {
		if (pageInfo != null) {
			// Save original bound box coordinates without rotation 
			origBoundBox = new PageRect(pageInfo[0], pageInfo[1], pageInfo[2], pageInfo[3]);

			// Set default page rotation
			pageRotate = (int)pageInfo[4];

			// Normalize initial page rotation
			if (pageRotate == PAGE_ROTATE_90 || pageRotate == PAGE_ROTATE_270)  {
				normBoundBox = origBoundBox.rotate(origBoundBox, pageRotate);
			} else {
				normBoundBox = origBoundBox.rotate(origBoundBox, 0);
			}
		}
	}

}
