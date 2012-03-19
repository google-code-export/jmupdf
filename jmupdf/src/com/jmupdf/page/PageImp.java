/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.page;

import com.jmupdf.JmuPdf;
import com.jmupdf.enums.ImageType;
import com.jmupdf.enums.TifCompression;
import com.jmupdf.enums.TifMode;
import com.jmupdf.interfaces.Document;
import com.jmupdf.interfaces.Page;

/**
 * Page class.
 * 
 * @author Pedro J Rivera
 *
 */
public abstract class PageImp extends JmuPdf implements Page {
	protected Document document;
	protected PageRect boundBox = new PageRect();
	protected PageLinks[] links;
	protected long handle = 0;
	protected int pageNumber = 0;
	protected int rotation = 0;
	
	/* */
	public synchronized long getHandle() {
		return handle;
	}
	
	/* */
	public int getPageNumber() {
		return pageNumber;
	}

	/* */
	public PageRect getBoundBox() {
		return boundBox;
	}

	/* */
	public int getX() {
		return getBoundBox().getX();
	}
	
	/* */
	public int getY() {
		return getBoundBox().getY();
	}
	
	/* */
	public int getWidth() {
		return getBoundBox().getWidth();
	}

	/* */
	public int getHeight() {
		return getBoundBox().getHeight();
	}

	/* */
	public int getRotation() {
		return rotation;
	}

	/* */	
	public Document getDocument() {
		return document;
	}

	/* */
	public synchronized PageText[] getTextSpan(PageRect rect) {
		if (getHandle() > 0) {
			return getPageText(getHandle(), 0.45f, rect.getX0(), rect.getY0(), rect.getX1(), rect.getY1());			
		}
		return null;
	}

	/* */
	public synchronized PageLinks[] getLinks(PagePixels pagePixels) {
		if (getHandle() <= 0) {
			return null;
		}
		if (links == null) {
			links = getPageLinks(getHandle());
			if (links == null) {
				links = new PageLinks[1];
				links[0] = new PageLinks(0, 0, 0, 0, 0, "");
			} else {
				if (pagePixels != null) {
					int rotate = pagePixels.getRotation();
					PageRect rect = new PageRect();
					for (int i=0; i<links.length; i++) {						
						rect.setRect(links[i].getX0(), links[i].getY0(), 
									 links[i].getX1(), links[i].getY1());
						rect = rect.rotate(getBoundBox(), rotate);
						rect = rect.scale(pagePixels.getZoom());
						links[i].setX0(rect.getX0());
						links[i].setY0(rect.getY0());
						links[i].setX1(rect.getX1());
						links[i].setY1(rect.getY1());
					}
					rect = null;
				}
			}
		}
		return links;
	}

	/* */
	public synchronized void dispose() {
		if (getHandle() > 0) {
			freePage(getHandle());
			handle = 0;
		}
	}
	
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/* saveAsXXX() methods.                      */
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	
	/* */
	public synchronized boolean saveAsPng(String file, int rotate, float zoom, ImageType color, float gamma, int aa) {
		if (getHandle() > 0) {
			if (color == ImageType.IMAGE_TYPE_RGB  	   || 
				color == ImageType.IMAGE_TYPE_ARGB 	   ||
				color == ImageType.IMAGE_TYPE_ARGB_PRE ||
				color == ImageType.IMAGE_TYPE_GRAY) {
				if (rotate == PageImp.PAGE_ROTATE_AUTO) {
					rotate = PageImp.PAGE_ROTATE_NONE;
				}
				return writePng(getHandle(), rotate, zoom, color.getIntValue(), gamma, aa, file.getBytes()) == 0;
			}
		}
		return false;
	}
	
	/* */
	public synchronized boolean saveAsPbm(String file, int rotate, float zoom, float gamma, int aa) {
		if (getHandle() > 0) {
			if (rotate == PageImp.PAGE_ROTATE_AUTO) {
				rotate = PageImp.PAGE_ROTATE_NONE;
			}
			return writePbm(getHandle(), rotate, zoom, gamma, aa, file.getBytes()) == 0;
		}
		return false;
	}
	
	/* */
	public synchronized boolean saveAsPnm(String file, int rotate, float zoom, ImageType color, float gamma, int aa) {
		if (getHandle() > 0) {
			if (color == ImageType.IMAGE_TYPE_RGB || 
				color == ImageType.IMAGE_TYPE_GRAY) {
				if (rotate == PageImp.PAGE_ROTATE_AUTO) {
					rotate = PageImp.PAGE_ROTATE_NONE;
				}
				return writePnm(getHandle(), rotate, zoom, color.getIntValue(), gamma, aa, file.getBytes()) == 0;
			}
		}
		return false;
	}

	/* */
	public synchronized boolean saveAsJPeg(String file, int rotate, float zoom, ImageType color, float gamma, int aa, int quality) {
		if (getHandle() > 0) {
			if (color == ImageType.IMAGE_TYPE_RGB || 
				color == ImageType.IMAGE_TYPE_GRAY) {
				if (!(quality >= 0 && quality <= 100)) {
					quality = 75;
				}
				if (rotate == PageImp.PAGE_ROTATE_AUTO) {
					rotate = PageImp.PAGE_ROTATE_NONE;
				}
				return writeJPG(getHandle(), rotate, zoom, color.getIntValue(), gamma, aa, file.getBytes(), quality) == 0;
			}
		}
		return false;
	}
	
	/* */
	public synchronized boolean saveAsBmp(String file, int rotate, float zoom, ImageType color, float gamma, int aa) {
		if (getHandle() > 0) {
			if (color == ImageType.IMAGE_TYPE_RGB    || 
				color == ImageType.IMAGE_TYPE_GRAY   ||
				color == ImageType.IMAGE_TYPE_BINARY ||
				color == ImageType.IMAGE_TYPE_BINARY_DITHER) {
				if (rotate == PageImp.PAGE_ROTATE_AUTO) {
					rotate = PageImp.PAGE_ROTATE_NONE;
				}
				return writeBmp(getHandle(), rotate, zoom, color.getIntValue(), gamma, aa, file.getBytes()) == 0;
			}
		}
		return false;
	}
	
	/* */
	public synchronized boolean saveAsPam(String file, int rotate, float zoom, ImageType color, float gamma, int aa) {
		if (getHandle() > 0) {
			if (color == ImageType.IMAGE_TYPE_RGB  	 || 
				color == ImageType.IMAGE_TYPE_ARGB 	 || 
				color == ImageType.IMAGE_TYPE_ARGB_PRE ||
				color == ImageType.IMAGE_TYPE_GRAY) {
				if (rotate == PageImp.PAGE_ROTATE_AUTO) {
					rotate = PageImp.PAGE_ROTATE_NONE;
				}
				return writePam(getHandle(), rotate, zoom, color.getIntValue(), gamma, aa, file.getBytes()) == 0;
			}
		}
		return false;
	}
	
	/* */
	public synchronized boolean saveAsTif(String file, int rotate, float zoom, ImageType color, float gamma, int aa, TifCompression compression, TifMode mode, int quality) {
		if (getHandle() > 0) {
			
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

			if (rotate == PageImp.PAGE_ROTATE_AUTO) {
				rotate = PageImp.PAGE_ROTATE_NONE;
			}
			
			return writeTif(getHandle(), rotate, zoom, color.getIntValue(), gamma, aa, file.getBytes(), compression.getIntValue(), mode.getIntValue(), quality) == 0;
		}

		return false;
	}
		
}
