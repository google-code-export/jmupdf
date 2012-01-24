/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.page;

import com.jmupdf.document.Document;
import com.jmupdf.interfaces.PageTypes;

/**
 * Page class.
 * 
 * @author Pedro J Rivera
 *
 */
public class Page implements PageTypes {
	private Document document;
	private PageRect mediabox;
	private PageLinks[] pageLinks;
	private int pn;
	private int px;
	private int py;
	private int pw;
	private int ph;
	private int pr;		

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
		this.pn = page;
		loadPageInfo(pageInfo);
	}
	
	/**
	 * Get page number
	 * @return
	 */
	public int getPageNumber() {
		return pn;
	}

	/**
	 * Get page media box. This is the page's original
	 * Cartesian coordinates without rotation.
	 * @return
	 */
	public PageRect getMediaBox() {
		return mediabox;
	}

	/**
	 * Get page x
	 * @return
	 */
	public int getX() {
		return px;
	}
	
	/**
	 * Get page y
	 * @return
	 */
	public int getY() {
		return py;
	}
	
	/**
	 * Get page width
	 * @return
	 */
	public int getWidth() {
		return pw;
	}

	/**
	 * Get page height
	 * @return
	 */
	public int getHeight() {
		return ph;
	}

	/**
	 * Get original page rotation.
	 * This is the rotation as it is saved in the document
	 * @return
	 */
	public int getRotation() {
		return pr;
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
	 * @param renderer : can be null
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public String getText(PageRenderer renderer, int x, int y, int w, int h) {
		String text = "";

		PageText[] pdfTextSpan = getTextSpan(renderer, x, y, w, h);
		
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
	 * Optionally pass in a PageRenderer object to determine how to extract text. </br>
	 * @param renderer : can be null
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public PageText[] getTextSpan(PageRenderer renderer, int x, int y, int w, int h) {

		// If no rendering object available then use defaults
		if (renderer == null) {
			return getDocument().getPageText(getPageNumber(), 1f, getRotation(),  x, y, x+w, y+h);
		}
		
		// Get rotation
		int rotate = renderer.getNormalizedRotation();

		// Rotate mediabox to current rotation
		PageRect m = getMediaBox().scale(renderer.getZoom());
		m = m.rotate(m, rotate);

		// Rotate to default page rotation
		PageRect r = new PageRect(x, y, x+w, y+h);		
		r = r.rotate(m, -(rotate-getRotation()));

		// Set new coordinates
		x = r.getX();
		y = r.getY();
		w = r.getWidth();
		h = r.getHeight();

		return getDocument().getPageText(getPageNumber(), renderer.getZoom(), getRotation(), x, y, x+w, y+h);
	}

	/**
	 * Get PageLinks Array Object </br>
	 * Optionally pass in a PageRenderer object to determine how to extract links. </br>
	 * @param renderer : can be null
	 * @return
	 */
	public PageLinks[] getLinks(PageRenderer renderer) {		
		if (pageLinks == null) {
			if (getDocument().getType() == Document.DOC_PDF) {
				pageLinks = getDocument().getPageLinks(getPageNumber());
			} else if (getDocument().getType() == Document.DOC_XPS) {
				pageLinks = null;
			}
			if (pageLinks == null) {
				pageLinks = new PageLinks[1];
				pageLinks[0] = new PageLinks(0, 0, 0, 0, 0, "");
			} else {
				if (renderer != null) {
					int rotate = renderer.getNormalizedRotation();
					PageRect rect = new PageRect();
					for (int i=0; i<pageLinks.length; i++) {						
						rect.setRect(pageLinks[i].getX0(), pageLinks[i].getY0(), 
									 pageLinks[i].getX1(), pageLinks[i].getY1());
						rect = rect.rotate(getMediaBox(), rotate);
						rect = rect.scale(renderer.getZoom());
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

		// Set default values
		px = 0;
		py = 0;
		pw = 0;
		ph = 0;
		pr = 0;

		if (pageInfo != null) {

			// Save original media box coordinates without rotation 
			mediabox = new PageRect(pageInfo[0], pageInfo[1], pageInfo[2], pageInfo[3]);

			// Set page coordinates
			px = mediabox.getX();
			py = mediabox.getY();
			pw = mediabox.getWidth();
			ph = mediabox.getHeight();
			
			// Set default page rotation
			pr = (int)pageInfo[4];

			// Normalize initial page rotation
			if (pr == PAGE_ROTATE_90_CW || pr == PAGE_ROTATE_270)  {
				PageRect r = mediabox.rotate(mediabox, pr);
				px = r.getX();
				py = r.getY();
				pw = r.getWidth();
				ph = r.getHeight();
			}
		
		}

	}

}
