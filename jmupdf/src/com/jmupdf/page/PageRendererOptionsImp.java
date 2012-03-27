/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.page;

import java.nio.ByteBuffer;

import com.jmupdf.enums.ImageFormat;
import com.jmupdf.enums.ImageType;
import com.jmupdf.enums.TifCompression;
import com.jmupdf.enums.TifMode;
import com.jmupdf.interfaces.Page;
import com.jmupdf.interfaces.PageRendererOptions;

/**
 * Page rendering descriptor
 * 
 * @author Francois Barre
 * @author Pedro J Rivera
 *
 */
abstract class PageRendererOptionsImp implements PageRendererOptions {
	
	protected ImageFormat imageFormat;
	protected ImageType imageType;
	protected int rotation;
	protected int antiAlias;
	protected float gamma;
	protected float zoom;
	protected int quality;
	protected TifCompression compression;
	protected TifMode mode;
	protected PageRect bbox;    
    protected ByteBuffer pageStruct;
    protected boolean isDisposed; 
    
    protected static final int IDX_IMAGE_FORMAT = 0;
    protected static final int IDX_IMAGE_TYPE = 4;
    protected static final int IDX_ROTATE = 8;
    protected static final int IDX_QUALITY = 12;
    protected static final int IDX_COMPRESSION = 16;
    protected static final int IDX_MODE = 20;
    protected static final int IDX_ANTIALIAS = 24;
    protected static final int IDX_ZOOM = 28;
    protected static final int IDX_GAMMA = 32;
    protected static final int IDX_X0 = 36;
    protected static final int IDX_Y0 = 40;
    protected static final int IDX_X1 = 44;
    protected static final int IDX_Y1 = 48;

	protected static final int DEFAULT_RESOLUTION = 72;
	
    /**
     * Load default options
     */
    protected void loadDefaults() {
    	setImageFormat(ImageFormat.FORMAT_PNG);
    	setImageType(ImageType.IMAGE_TYPE_RGB);
    	setRotate(Page.PAGE_ROTATE_NONE);
    	setAntiAlias(8);
    	setGamma(1.0f);
    	setZoom(1.0f);
    	setQuality(0);
    	setCompression(TifCompression.TIF_COMPRESSION_ZLIB);
    	setMode(TifMode.TIF_DATA_APPEND);
    	setBoundBox(new PageRect());
    	isDisposed = false;
    }
    
    /**
     * Get options data structure
     * @return
     */
    private ByteBuffer getOptionsStruct() {
    	return pageStruct;
    }
    
    /* */
    public ImageFormat getImageFormat() {
        return imageFormat;
    }

    /* */
    public void setImageFormat(ImageFormat imageFormat) {
    	if (isDisposed) {
    		return;
    	}
        this.imageFormat = imageFormat;
        getOptionsStruct().putInt(IDX_IMAGE_FORMAT, imageFormat.getIntValue());
    }

    /* */
    public ImageType getImageType() {
        return imageType;
    }

    /* */
    public void setImageType(ImageType imageType) {
    	if (isDisposed) {
    		return;
    	}
        this.imageType = imageType;
        getOptionsStruct().putInt(IDX_IMAGE_TYPE, imageType.getIntValue());
    }

    /* */
    public int getRotate() {
        return rotation;
    }

    /* */
    public void setRotate(int rotate) {
    	if (isDisposed) {
    		return;
    	}
		if (rotate == Page.PAGE_ROTATE_AUTO) {
			rotate = Page.PAGE_ROTATE_NONE;
		}
        this.rotation = PageRect.rotate360(rotate);
        getOptionsStruct().putInt(IDX_ROTATE, rotate);
    }

    /* */
    public float getZoom() {
        return zoom;
    }

    /* */
    public void setZoom(float zoom) {
    	if (isDisposed) {
    		return;
    	}
    	if (zoom <= 0 ) {
    		zoom = 1f;
    	}
        this.zoom = zoom;
        getOptionsStruct().putFloat(IDX_ZOOM, zoom);
    }

    /* */
    public float getResolution() {
    	return getZoom() * DEFAULT_RESOLUTION;
    }

    /* */
    public float getGamma() {
        return gamma;
    }

    /* */
    public void setGamma(float gamma) {
    	if (isDisposed) {
    		return;
    	}
		if (gamma <= 0) {
			gamma = 1f;
		} else if (gamma > 2) {
			gamma = 2f;
		}
        this.gamma = gamma;
        getOptionsStruct().putFloat(IDX_GAMMA, gamma);
    }

    /* */
    public int getQuality() {
        return quality;
    }

    /* */
    public void setQuality(int quality) {
    	if (isDisposed) {
    		return;
    	}
        this.quality = quality;
        getOptionsStruct().putInt(IDX_QUALITY, quality);
    }

    /* */
    public TifCompression getCompression() {
        return compression;
    }

    /* */
    public void setCompression(TifCompression compression) {
    	if (isDisposed) {
    		return;
    	}
        this.compression = compression;
        getOptionsStruct().putInt(IDX_COMPRESSION, compression.getIntValue());
    }

    /* */
    public TifMode getMode() {
        return mode;
    }

    /* */
    public void setMode(TifMode mode) {
    	if (isDisposed) {
    		return;
    	}
        this.mode = mode;
        getOptionsStruct().putInt(IDX_MODE, mode.getIntValue());
    }

    /* */
    public PageRect getBoundBox() {
        return bbox;
    }

    /* */
    public void setBoundBox(PageRect bbox) {
    	if (isDisposed) {
    		return;
    	}
        this.bbox = bbox;
        getOptionsStruct().putFloat(IDX_X0, bbox.getX0());
        getOptionsStruct().putFloat(IDX_Y0, bbox.getY0());
        getOptionsStruct().putFloat(IDX_X1, bbox.getX1());
        getOptionsStruct().putFloat(IDX_Y1, bbox.getY1());
    }

    /* */
    public int getAntiAlias() {
        return antiAlias;
    }

    /* */
    public void setAntiAlias(int antiAlias) {
    	if (isDisposed) {
    		return;
    	}
		if (antiAlias < 0) {
			antiAlias = 0;
		} else if (antiAlias > 8) {
			antiAlias = 8;
		}
        this.antiAlias = antiAlias;
        getOptionsStruct().putInt(IDX_ANTIALIAS, antiAlias);
    }

    /* */
    public boolean isValid() {
    	boolean retval = true;
        ImageType imageType = getImageType();
        
        switch (getImageFormat()) {
        	case FORMAT_BUFFERED_IMAGE:
        		break;
        		
	        case FORMAT_PNG:
	            if (!(imageType == ImageType.IMAGE_TYPE_RGB      || 
	                  imageType == ImageType.IMAGE_TYPE_ARGB     ||
	                  imageType == ImageType.IMAGE_TYPE_ARGB_PRE || 
	                  imageType == ImageType.IMAGE_TYPE_GRAY)) {
	            	log("Invalid ImageType=" + imageType); 
	            	retval = false;
	            }
	            break;
	        
	        case FORMAT_PNM:
	            if (!(imageType == ImageType.IMAGE_TYPE_RGB || 
	                  imageType == ImageType.IMAGE_TYPE_GRAY)) {
	            	log("Invalid ImageType=" + imageType);
	            	retval = false;
	            }
	            break;
	        
	        case FORMAT_PBM:
	            if (!(imageType == ImageType.IMAGE_TYPE_GRAY)) {
	                log("Invalid ImageType=" + imageType);
	                retval = false;
	            }
	            break;
	            
	        case FORMAT_JPG:
	            if (!(imageType == ImageType.IMAGE_TYPE_RGB ||
	                  imageType == ImageType.IMAGE_TYPE_GRAY)) {
	            	log("Invalid ImageType=" + imageType);
	            	retval = false;
	            }
	            if (!(getQuality() >= 0 && 
	            	  getQuality() <= 100)) {
	                setQuality(75);
	            }
	            break;
	        
	        case FORMAT_BMP:
	            if (!(imageType == ImageType.IMAGE_TYPE_RGB    || 
	                  imageType == ImageType.IMAGE_TYPE_GRAY   || 
	                  imageType == ImageType.IMAGE_TYPE_BINARY || 
	                  imageType == ImageType.IMAGE_TYPE_BINARY_DITHER)) {
	            	log("Invalid ImageType=" + imageType);
	            	retval = false;
	            }
	            break;
	            
	        case FORMAT_PAM:
	            if (!(imageType == ImageType.IMAGE_TYPE_RGB      || 
	                  imageType == ImageType.IMAGE_TYPE_ARGB     || 
	                  imageType == ImageType.IMAGE_TYPE_ARGB_PRE || 
	                  imageType == ImageType.IMAGE_TYPE_GRAY)) {
	            	log("Invalid ImageType=" + imageType);
	            	retval = false;
	            }
	            break;
	        
	        case FORMAT_TIF:
	            if (!(imageType == ImageType.IMAGE_TYPE_RGB      || 
	                  imageType == ImageType.IMAGE_TYPE_ARGB     || 
	                  imageType == ImageType.IMAGE_TYPE_ARGB_PRE || 
	                  imageType == ImageType.IMAGE_TYPE_GRAY     || 
	                  imageType == ImageType.IMAGE_TYPE_BINARY   || 
	                  imageType == ImageType.IMAGE_TYPE_BINARY_DITHER)) {
	                log("Invalid color type specified.");
	                retval = false;
	            }
	
	            if (!(getMode() == TifMode.TIF_DATA_APPEND || 
	            	  getMode() == TifMode.TIF_DATA_DISCARD)) {
	            	log("Invalid mode value specified.");
	            	retval = false;
	            }
	
	            if (getCompression() == TifCompression.TIF_COMPRESSION_CCITT_RLE  || 
	            	getCompression() == TifCompression.TIF_COMPRESSION_CCITT_T_4  || 
	            	getCompression() == TifCompression.TIF_COMPRESSION_CCITT_T_6) {
	                if (!(imageType == ImageType.IMAGE_TYPE_BINARY || 
	                	  imageType == ImageType.IMAGE_TYPE_BINARY_DITHER)) {
	                	log("When using CCITT compression, color must be type binary.");
	                	retval = false;
	                }
	                if (imageType == ImageType.IMAGE_TYPE_ARGB || 
	                	imageType == ImageType.IMAGE_TYPE_ARGB_PRE) {
	                	log("When using CCITT compression, color cannot be type of ARGB.");
	                	retval = false;
	                }
	            }
	
	            if (getCompression() == TifCompression.TIF_COMPRESSION_JPEG) {
	                if (!(getQuality() >= 1 && 
	                	  getQuality() <= 100)) {
	                    setQuality(75);
	                }
	            }

	            if (getCompression() == TifCompression.TIF_COMPRESSION_ZLIB) {
	                if (!(getQuality() >= 1 && 
	                	  getQuality() <= 9)) {
	                    setQuality(6);
	                }
	            }
	            break;
	            
	        default:
	        	log("Unsupported image type requested.");
	        	retval = false;
        }
        return retval;
    }

    /**
     * Inform object that page has been disposed and disable </br>
     * any access to change options. </br></br>
     * 
     * This avoids accessing a data structure that no longer exists. </br>
     * This is called from Page.dispose()
     */
   protected void dispose() {
    	isDisposed = true;
    }
    
    /**
     * Print error messages
     * @param text
     */
    protected static void log(String text) {
    	System.err.println(text);
    }

}
