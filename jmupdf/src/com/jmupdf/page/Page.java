/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.page;

import java.nio.ByteBuffer;

import com.jmupdf.JmuPdf;
import com.jmupdf.document.Document;
import com.jmupdf.enums.ImageType;
import com.jmupdf.enums.TifCompression;
import com.jmupdf.enums.TifMode;
import com.jmupdf.exceptions.PageException;

/**
 * Page class.
 * 
 * @author Pedro J Rivera
 *
 */
public class Page extends JmuPdf {
	private Document document;
	private PageRect boundBox;
	private PageLinks[] pageLinks;
	private long handle;
	private int pageNumber;
	private int pageRotate;
	private int antiAliasLevel;
	
	public static final int PAGE_ROTATE_AUTO = -1;
	public static final int PAGE_ROTATE_NONE = 0;
	public static final int PAGE_ROTATE_90 = 90;
	public static final int PAGE_ROTATE_180 = 180;
	public static final int PAGE_ROTATE_270 = 270;
	public static final int PAGE_ROTATE_360 = 360;

	/**
	 * Create a new page object
	 * 
	 * @param doc
	 * @param page 
	 */
	public Page(Document doc, int page) throws PageException {
		this.document = doc;
		this.pageNumber = page;
		this.boundBox = new PageRect();
		this.pageRotate = 0;
		this.antiAliasLevel = 0;
		this.handle = newPage(doc.getHandle(), page);
		if (handle > 0) {
			if (!loadPageInfo()) {
				throw new PageException("Error: Page could not be loaded.");
			}
		} else {
			throw new PageException("Error: Page could not be created.");
		}
	}
	
	/**
	 * Get page number
	 * @return
	 */
	public int getPageNumber() {
		return pageNumber;
	}

	/**
	 * Get page bound box.
	 * @return
	 */
	public PageRect getBoundBox() {
		return boundBox;
	}

	/**
	 * Get page x
	 * @return
	 */
	public int getX() {
		return getBoundBox().getX();
	}
	
	/**
	 * Get page y
	 * @return
	 */
	public int getY() {
		return getBoundBox().getY();
	}
	
	/**
	 * Get page width
	 * @return
	 */
	public int getWidth() {
		return getBoundBox().getWidth();
	}

	/**
	 * Get page height
	 * @return
	 */
	public int getHeight() {
		return getBoundBox().getHeight();
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
		if (handle > 0) {
			return getText(null, getX(), getY(), getWidth(), getHeight());
		}
		return null;
	}
	
	/**
	 * Get text from page. </br></br>
	 * 
	 * If PagePixel object is null then all coordinates are assumed to be in </br>
	 * 1f zoom and 0 rotation. Otherwise the coordinates passed in must reflect </br>
	 * the zoom factor and rotation of the PagePixel object passed in.</br></br>
	 * 
	 * @param pagePixels Optional
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public String getText(PagePixels pagePixels, int x, int y, int w, int h) {
		if (handle <= 0) {
			return null;
		}
		
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
	 * Get TextSpan Array Object. </br></br>
	 * 
	 * If PagePixel object is null then all coordinates are assumed to be in </br>
	 * 1f zoom and 0 rotation. Otherwise the coordinates passed in must reflect </br>
	 * the zoom factor and rotation of the PagePixel object passed in.</br></br>
	 * 
	 * @param pagePixels Optional.
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public PageText[] getTextSpan(PagePixels pagePixels, int x, int y, int w, int h) {
		if (handle <= 0) {
			return null;
		}
		
		PageRect pr = new PageRect();
		int rotate = getRotation();
		float zoom = 1f;

		if (pagePixels != null) {
			zoom = pagePixels.getZoom();
			rotate = pagePixels.getRotation();
		}

		pr.setRect(x/zoom, y/zoom, (x+w)/zoom, (y+h)/zoom);		
		pr = pr.rotate(getBoundBox(), rotate, PAGE_ROTATE_NONE);

		return getPageText((int)pr.getX0(), (int)pr.getY0(), (int)pr.getX1(), (int)pr.getY1());
	}

	/**
	 * Get PageLinks Array Object </br>
	 * Optionally pass in a PagePixel object to determine how to extract links. </br>
	 * 
	 * @param pagePixels : can be null
	 * @return
	 */
	public PageLinks[] getLinks(PagePixels pagePixels) {
		if (handle <= 0) {
			return null;
		}
		if (pageLinks == null) {
			pageLinks = getPageLinks();
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
	 * Get a page as a byte buffer
	 * 
	 * @param zoom
	 * @param rotate
	 * @param color
	 * @param gamma
	 * @param bbox
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return
	 */
	public ByteBuffer getByteBuffer(float zoom, int rotate, ImageType color, float gamma, int[] bbox, float x0, float y0, float x1, float y1) {
		if (handle > 0) {
			return getByteBuffer(handle, zoom, rotate, color.getIntValue(), gamma, bbox, x0, y0, x1, y1);
		}
		return null;
	}

	/**
	 * Free a byte buffer resource
	 * 
	 * @param buffer
	 */
	public void freeByteBuffer(ByteBuffer buffer) {
		if (handle > 0) {
			if (buffer != null) {
				if (buffer.isDirect()) {
					buffer.clear();
					freeByteBuffer(handle, buffer);
				}
			}
		}
	}
	
	/**
	 * Get page text
	 * 
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return
	 */
	private PageText[] getPageText(int x0, int y0, int x1, int y1) {
		if (handle > 0) {
			return getPageText(handle, x0, y0, x1, y1);
		}
		return null;
	}
	
	/**
	 * Get page links
	 * 
	 * @return
	 */
	private PageLinks[] getPageLinks() {
		if (handle > 0) {
			return getPageLinks(handle);
		}
		return null;
	}
	
	/**
	 * Load page information
	 */
	private boolean loadPageInfo() {
		float[] pageInfo = loadPage(handle);
		if (pageInfo == null) {
			dispose();
			return false;
		}
		antiAliasLevel = getAntiAliasLevel(handle);
		boundBox = new PageRect(pageInfo[0], pageInfo[1], pageInfo[2], pageInfo[3]);
		pageRotate = (int)pageInfo[4];
		return true;
	}

	/**
	 * Dispose of page resources
	 */
	public void dispose() {
		if (handle > 0) {
			freePage(handle);
			handle = 0;
		}
	}

	/* */
	/* */
	
	/**
	 * Set default anti-alias level to be used when rendering pages
	 * 
	 * @param antiAliasLevel
	 */
	public void setAntiAliasLevel(int antiAliasLevel) {
		if (handle > 0) {
			antiAliasLevel = validateAntiAliasLevel(antiAliasLevel);
			if (antiAliasLevel != this.antiAliasLevel) {
				this.antiAliasLevel = antiAliasLevel;
				setAntiAliasLevel(handle, antiAliasLevel);
			}
		}
	}

	/**
	 * Get default anti-alias level used when rendering pages
	 * @return
	 */
	public int getAntiAliasLevel() {
		if (handle > 0) {
			return antiAliasLevel;
		}
		return -1;
	}

	/**
	 * Validate Anti-alias level
	 */
	private static int validateAntiAliasLevel(int level) {
		if (level < 0) {
			return 0;
		}
		else if (level > 8) {
			return 8;
		}
		return level;
	}
	
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/* saveAsXXX() methods.                      */
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	
	/**
	 * Save a page to a file in PNG format
	 * 
	 * @param file
	 * @param rotate
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @return
	 */
	public boolean saveAsPng(String file, int rotate, float zoom, ImageType color, float gamma) {
		if (handle > 0) {
			if (color == ImageType.IMAGE_TYPE_RGB  	   || 
				color == ImageType.IMAGE_TYPE_ARGB 	   ||
				color == ImageType.IMAGE_TYPE_ARGB_PRE ||
				color == ImageType.IMAGE_TYPE_GRAY) {
				if (rotate == Page.PAGE_ROTATE_AUTO) {
					rotate = Page.PAGE_ROTATE_NONE;
				}
				return writePng(handle, rotate, zoom, color.getIntValue(), gamma, file.getBytes()) == 0;
			}
		}
		return false;
	}
	
	/**
	 * Save a page to a file in PBM format</br>
	 * PBM is the portable bitmap format, a lowest common denominator monochrome file format. 
	 * @param file
	 * @param rotate
	 * @param zoom
	 * @param gamma
	 * @return
	 */
	public boolean saveAsPbm(String file, int rotate, float zoom, float gamma) {
		if (handle > 0) {
			if (rotate == Page.PAGE_ROTATE_AUTO) {
				rotate = Page.PAGE_ROTATE_NONE;
			}
			return writePbm(handle, rotate, zoom, gamma, file.getBytes()) == 0;
		}
		return false;
	}
	
	/**
	 * Save a page to a file in PNM format
	 * 
	 * @param file
	 * @param rotate
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @return
	 */
	public boolean saveAsPnm(String file, int rotate, float zoom, ImageType color, float gamma) {
		if (handle > 0) {
			if (color == ImageType.IMAGE_TYPE_RGB || 
				color == ImageType.IMAGE_TYPE_GRAY) {
				if (rotate == Page.PAGE_ROTATE_AUTO) {
					rotate = Page.PAGE_ROTATE_NONE;
				}
				return writePnm(handle, rotate, zoom, color.getIntValue(), gamma, file.getBytes()) == 0;
			}
		}
		return false;
	}

	/**
	 * Save a page to a file in JPEG format
	 * 
	 * @param file
	 * @param rotate
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @param quality : quality levels are in the range 0-100 with a default value of 75.
	 * @return
	 */
	public boolean saveAsJPeg(String file, int rotate, float zoom, ImageType color, float gamma, int quality) {
		if (handle > 0) {
			if (color == ImageType.IMAGE_TYPE_RGB || 
				color == ImageType.IMAGE_TYPE_GRAY) {
				if (!(quality >= 0 && quality <= 100)) {
					quality = 75;
				}
				if (rotate == Page.PAGE_ROTATE_AUTO) {
					rotate = Page.PAGE_ROTATE_NONE;
				}
				return writeJPeg(handle, rotate, zoom, color.getIntValue(), gamma, file.getBytes(), quality) == 0;
			}
		}
		return false;
	}
	
	/**
	 * Save a page to a BMP format
	 * 
	 * @param file
	 * @param rotate
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @return
	 */
	public boolean saveAsBmp(String file, int rotate, float zoom, ImageType color, float gamma) {
		if (handle > 0) {
			if (color == ImageType.IMAGE_TYPE_RGB    || 
				color == ImageType.IMAGE_TYPE_GRAY   ||
				color == ImageType.IMAGE_TYPE_BINARY ||
				color == ImageType.IMAGE_TYPE_BINARY_DITHER) {
				if (rotate == Page.PAGE_ROTATE_AUTO) {
					rotate = Page.PAGE_ROTATE_NONE;
				}
				return writeBmp(handle, rotate, zoom, color.getIntValue(), gamma, file.getBytes()) == 0;
			}
		}
		return false;
	}
	
	/**
	 * Save a page to a file in PAM format</br>
	 * The name "PAM" is an acronym derived from "Portable Arbitrary Map"
	 * 
	 * @param file
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @return
	 */
	public boolean saveAsPam(String file, int rotate, float zoom, ImageType color, float gamma) {
		if (handle > 0) {
			if (color == ImageType.IMAGE_TYPE_RGB  	 || 
				color == ImageType.IMAGE_TYPE_ARGB 	 || 
				color == ImageType.IMAGE_TYPE_ARGB_PRE ||
				color == ImageType.IMAGE_TYPE_GRAY) {
				if (rotate == Page.PAGE_ROTATE_AUTO) {
					rotate = Page.PAGE_ROTATE_NONE;
				}
				return writePam(handle, rotate, zoom, color.getIntValue(), gamma, file.getBytes()) == 0;
			}
		}
		return false;
	}
	
	/**
	 * Save a page to a TIF format
	 * 
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @param gamma
	 * @param compression
	 * @param mode
	 * @param quality <br/><br/>
	 *        
	 *        <blockquote>
	 *        <strong>When compression == TIF_COMPRESSION_ZLIB <br/></strong> 
	 *          Control  the  compression  technique used by the Deflate codec.  <br/> 
	 *          Quality levels are in the range 1-9 with larger numbers yielding <br/> 
	 *          better compression at the cost of more computation. The default  <br/> 
	 *          quality level is 6 which yields a good time-space tradeoff.      <br/><br/>
	 *          
	 *        <strong>When compression == TIF_COMPRESSION_JPEG <br/></strong>
	 *          Control the compression quality level used in the baseline algo- <br/>
	 *          rithm. Note that quality levels are in the range 0-100 with a    <br/>
	 *          default value of 75. <br/><br/>
	 *        </blockquote>  
	 * @return
	 */
	public boolean saveAsTif(String file, int rotate, float zoom, ImageType color, float gamma, TifCompression compression, TifMode mode, int quality) {
		if (handle > 0) {
			
			if (!(color == ImageType.IMAGE_TYPE_RGB      || 
				  color == ImageType.IMAGE_TYPE_ARGB     ||
				  color == ImageType.IMAGE_TYPE_ARGB_PRE ||
				  color == ImageType.IMAGE_TYPE_GRAY     ||
				  color == ImageType.IMAGE_TYPE_BINARY   || 
				  color == ImageType.IMAGE_TYPE_BINARY_DITHER)) {
				log("Invalid color type specified.");
				return false;
			}
			
			if (!(mode == TifMode.TIF_DATA_APPEND || 
				  mode == TifMode.TIF_DATA_DISCARD)) {
				log("Invalid mode value specified.");
				return false;
			}

			if (compression == TifCompression.TIF_COMPRESSION_CCITT_RLE || 
				compression == TifCompression.TIF_COMPRESSION_CCITT_T_4 ||
				compression == TifCompression.TIF_COMPRESSION_CCITT_T_6) {
				if (!(color == ImageType.IMAGE_TYPE_BINARY || 
					  color == ImageType.IMAGE_TYPE_BINARY_DITHER)) {
					log("When using CCITT compression, color must be type binary");
					return false;
				}
				if (color == ImageType.IMAGE_TYPE_ARGB || 
					color == ImageType.IMAGE_TYPE_ARGB_PRE) {
					log("When using CCITT compression, color cannot be type of ARGB");
					return false;
				}
			}

			if (compression == TifCompression.TIF_COMPRESSION_JPEG) {
				if (!(quality >= 1 && quality <= 100)) {
					quality = 75;
				}
			}

			if (compression == TifCompression.TIF_COMPRESSION_ZLIB) {
				if (!(quality >= 1 && quality <= 9)) {
					quality = 6;
				}
			}

			if (rotate == Page.PAGE_ROTATE_AUTO) {
				rotate = Page.PAGE_ROTATE_NONE;
			}
			
			return writeTif(handle, rotate, zoom, color.getIntValue(), gamma, file.getBytes(), compression.getIntValue(), mode.getIntValue(), quality) == 0;
		}

		return false;
	}
		
}
