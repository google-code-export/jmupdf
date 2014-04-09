/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.page;

import java.nio.ByteOrder;

import com.jmupdf.JmuPdf;
import com.jmupdf.enums.ImageFormat;
import com.jmupdf.interfaces.Document;
import com.jmupdf.interfaces.Page;
import com.jmupdf.interfaces.PagePixels;
import com.jmupdf.interfaces.PageRendererOptions;

/**
 * Page Class Implementation
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
	protected PageRendererOptions options = null;
	
	/* */
	public long getHandle() {
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
	public PageText[] getTextSpan(PageRect rect) {
		if (getHandle() > 0) {
			return getPageText(getHandle(), 0.45f, rect.getX0(), rect.getY0(), rect.getX1(), rect.getY1());			
		}
		return null;
	}

	/* */
	public PageLinks[] getLinks(PageRendererOptions options) {
		if (getHandle() <= 0) {
			return null;
		}
		synchronized (this) {
			if (links == null) {
				links = getPageLinks(getHandle());
				if (links == null) {
					links = new PageLinks[1];
					links[0] = new PageLinks(0, 0, 0, 0, 0, "");
				} else {
					if (options != null) {
						int rotate = options.getRotate();
						PageRect rect = new PageRect();
						for (int i=0; i<links.length; i++) {						
							rect.setRect(links[i].getX0(), links[i].getY0(), 
										 links[i].getX1(), links[i].getY1());
							rect = rect.rotate(getBoundBox(), rotate);
							rect = rect.scale(options.getZoom());
							links[i].setX0(rect.getX0());
							links[i].setY0(rect.getY0());
							links[i].setX1(rect.getX1());
							links[i].setY1(rect.getY1());
						}
						rect = null;
					}
				}
			}			
		}
		return links;
	}

	/* */
	public synchronized void dispose() {
		if (getHandle() > 0) {
			freePage(getHandle());
			if (options != null) {
				((PageRendererOptionsImp)options).dispose();
			}
			handle = 0;
		}
	}

    /* */
    public boolean saveAsImage(String file, PageRendererOptions options) {
        if (getHandle() > 0) {
        	if (!options.getImageFormat().equals(ImageFormat.FORMAT_BUFFERED_IMAGE)) {
            	if (options.isValid()) {
            		return saveAsFile(getHandle(), file.getBytes()) == 0;
            	}
        	} else {
        		log("Image format cannot be FORMAT_BUFFERED_IMAGE when saving to a file.");
        	}
        }
        return false;
    }

    /* */
    public byte[] saveAsImage(PageRendererOptions options) {
        if (getHandle() > 0) {
            if (options.getImageFormat() == ImageFormat.FORMAT_PNG ||
            	options.getImageFormat() == ImageFormat.FORMAT_JPG) {
            	if (options.isValid()) {
            		return saveAsByte(getHandle());
            	}
            } else {
            	log("Currently only PNG and JPEG formats are supported when creating a byte array.");
            }
        }
        return null;
    }

    /* */
    public PagePixels getPagePixels() {
    	if (getHandle() > 0) {
    		return new PagePixelsFactory(this);
    	}
    	return null;
    };

    /* */
    public PageRendererOptions getRenderingOptions() {
    	if (getHandle() > 0) {
    		synchronized (this) {
        		if (options == null) {
        			options = new PageRendererOptionsFactory(this);
    	    	}				
			}
    		return options;
    	}
    	return null;
    }

    /**
     * PagePixelsFactory class
     */
    class PagePixelsFactory extends PagePixelsImp {

        public PagePixelsFactory(Page page) {
    		this.page = page;
    		options = page.getRenderingOptions();
    		options.setImageFormat(ImageFormat.FORMAT_BUFFERED_IMAGE);
    	}
        
    }
    
    /**
     * PageRendererOptionsFactory class
     */
    class PageRendererOptionsFactory extends PageRendererOptionsImp {

        public PageRendererOptionsFactory(Page page) {
    		pageStruct = getPageOptionsStruct(page.getHandle()).order(ByteOrder.nativeOrder());
    		loadDefaults();
    	}
        
    }

}
