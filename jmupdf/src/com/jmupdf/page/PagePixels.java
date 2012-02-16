package com.jmupdf.page;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.jmupdf.interfaces.DocumentTypes;
import com.jmupdf.interfaces.ImageTypes;

/**
 * PagePixels Class
 * 
 * This class represents pixel/image data for a given page.
 * 
 * @author Pedro J Rivera
 *
 */
public class PagePixels implements ImageTypes, DocumentTypes {
	private Page page;
	private PageRect boundBox;
	private BufferedImage image;	
	private ByteBuffer buffer;
	private Object pixels;

	private float gamma;
	private float zoom;
	private float resolution;
	private int rotate;
	private int rotation;
	private int color;
	private boolean isDirty;
	private static int default_resolution = 72;
	
	/**
	 * Create new page pixel object
	 */
	public PagePixels(Page page) {
		this.page = page;
		this.boundBox = new PageRect();
		this.gamma = 1f;
		this.zoom = 1f;
		this.rotate = 0;
		this.resolution = default_resolution;
		this.color = IMAGE_TYPE_RGB; 
		this.pixels = null;
		this.image = null;
		setRotation();
		this.isDirty = true;
	}

	/**
	 * Get X coordinate
	 * @return
	 */
	public int getX() {
		return getBoundBox().getX();
	}
	
	/**
	 * Get Y coordinate
	 * @return
	 */
	public int getY() {
		return getBoundBox().getY();
	}
	
	/**
	 * Get width
	 * @return
	 */
	public int getWidth() {
		return getBoundBox().getWidth();
	}

	/**
	 * Get height
	 * @return
	 */
	public int getHeight() {
		return getBoundBox().getHeight();
	}
	
    /**
     * Get point one x
     * @return
     */
    public float getX0() {
    	return getBoundBox().getX0();
    }
    
    /**
     * Get point one y
     * @return
     */
    public float getY0() {
    	return getBoundBox().getY0();
    }
    
    /**
     * Get point two x
     * @return
     */
    public float getX1() {
    	return getBoundBox().getX1();
    }

    /**
     * Get point two y
     * @return
     */
    public float getY1() {
    	return getBoundBox().getY1();
    }
    
	/**
	 * Get page object
	 * @return
	 */
	public Page getPage() {
		return page;
	}
	
	/**
	 * Get gamma correction
	 * @return
	 */
	public float getGamma() {
		return gamma;
	}

	/**
	 * Set gamma correction </br></br>
	 * 
	 * Gamma correct the output image. </br>
	 * Some typical values are 0.7 or 1.4 to thin or darken text rendering. </br>
	 * Default value is 1f.
	 * 
	 * @param gamma
	 */
	public void setGamma(float gamma) {
		if (getGamma() == gamma) {
			return;
		}
		if (gamma <= 0) {
			gamma = 1f;
		}
		this.gamma = gamma;
		setDirty(true);
	}

	/**
	 * Get anti-alias level.
	 * @return
	 */
	public int getAntiAliasLevel() {
		return getPage().getDocument().getAntiAliasLevel();
	}
	
	/**
	 * Set anti-alias level.</br>
	 * This value is used to determine what bit level is used when </br> 
	 * applying anti-aliasing while rendering page images.</br>
	 * A value of zero turns off anti-aliasing. Maximum value is 8. </br>
	 * @param level
	 */
	public void setAntiAliasLevel(int level) {
		if (getAntiAliasLevel() == level) {
			return;
		}
		getPage().getDocument().setAntiAliasLevel(level);
		setDirty(true);
	}

	/**
	 * Get resolution
	 * @return
	 */
	public float getResolution() {
		return resolution;
	}
	
	/**
	 * Set resolution
	 * @param zoom
	 */
	private void setResolution() {
		resolution = default_resolution * getZoom();
	}
	
	/**
	 * Get zoom level
	 * @return
	 */
	public float getZoom() {
		return zoom;
	}

	/**
	 * Set zoom level, Default value is 1f.
	 * @param zoom
	 */
	public void setZoom(float zoom) {
		if (getZoom() == zoom) {
			return;
		}
		this.zoom = zoom;
		setResolution();
		setDirty(true);
	}

	/**
	 * Get image rotation.
	 * @return
	 */
	public int getRotate() {
		return rotate;
	}
	
	/**
	 * Set image rotation. Default value is 0.
	 * @param rotate
	 */
	public void setRotate(int rotate) {
		if (getRotate() == rotate) {
			return;
		}
		this.rotate = rotate;
		setRotation();
		setDirty(true);
	}

	/**
	 * Get actual rotation
	 * This will return whatever rotation is being requested </br> 
	 * plus any default page rotations stored in the page. </br>
	 * In other words, this will be the actual rotation performed on the image </br>
	 * which is not necessarily the getRotate() value.
	 * @return
	 */
	public int getRotation() {
		return rotation;
	}

	/**
	 * Set actual rotation
	 */
	private void setRotation() {
		int rotate = getRotate();
		
		if (rotate == Page.PAGE_ROTATE_AUTO) {
			rotate = getPage().getRotation();
		} else {
			if (rotate != Page.PAGE_ROTATE_NONE) {
				rotate += getPage().getRotation();
			}
		}
	    
		rotate = rotate % 360;
	    
		if (rotate < 0) { 
	    	rotate = rotate + 360;
		}

		rotation = rotate;
	}
	
	/**
	 * Get color type
	 * @return
	 */
	public int getColor() {
		return color;
	}

	/**
	 * Set color type. Default value is IMAGE_TYPE_RGB.
	 * @param color
	 */
	public void setColor(int color) {
		if (getColor() == color) {
			return;
		}
		this.color = color;
		setDirty(true);
	}

	/**
	 * Return is dirty flag
	 * @return
	 */
	public boolean isDirty() {
		return isDirty;
	}
	
	/**
	 * Set dirty flag
	 * @param dirty
	 */
	private void setDirty(boolean dirty) {
		isDirty = dirty;
	}
	
	/**
	 * Get pixel data bounding box
	 * @return
	 */
	private PageRect getBoundBox() {
		return boundBox;
	}
	
	/**
	 * Get buffered image
	 * @return
	 */
	public BufferedImage getImage() {
		if (!isDirty()) {
			if (image == null) {
				createBufferedImage();
			}
		}
		return image;
	}

	/**
	 * Get pixel data
	 * @return
	 */
	public Object getPixels() {
		if (!isDirty()) {
			if (pixels == null) { 
				if (buffer != null) {			
					if (isByteData()) {
						pixels = new byte[buffer.order(ByteOrder.nativeOrder()).capacity()];
						buffer.order(ByteOrder.nativeOrder()).get((byte[])pixels);
					} else {
						pixels = new int[buffer.order(ByteOrder.nativeOrder()).asIntBuffer().capacity()];
						buffer.order(ByteOrder.nativeOrder()).asIntBuffer().get((int[])pixels);
					}
					getPage().getDocument().freeByteBuffer(buffer);
					buffer = null;
				}
			}
		}
		return pixels;
	}

	/**
	 * Draw page image. </br>
	 * The coordinates passed in is the region to render </br>
	 * The coordinates should be based on the pages default rotation and a zoom level of 1f. </br> 
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	public void drawPage(float x0, float y0, float x1, float y1) {
		
		if (!isDirty()) {
			return;
		}
		
		getBoundBox().setRect(x0, y0, x1, y1);
		PageRect c = getBoundBox().rotateBack(getPage().getBoundBox(), getPage().getRotation());
		
		int[] bbox = new int[4];
		
		buffer = getPage().getDocument().getPageByteBuffer(
				 getPage().getPageNumber(), 
				 getZoom(), 
				 getRotation(), 
				 getColor(),
				 getGamma(),
				 bbox, 
				 c.getX0(), 
				 c.getY0(), 
				 c.getX1(), 
				 c.getY1());

		if (buffer != null || pixels != null) {
			getBoundBox().setRect(bbox[0], bbox[1], bbox[2], bbox[3]);			
			setDirty(false);
		} else {
			System.gc();
		}

	}

	/**
	 * Create a buffered image from packed pixel data
	 * @param pixels
	 */
	private void createBufferedImage() {
		Object p;
		try {
			p = getPixels();
			if (p != null) {
				image = new BufferedImage(getWidth(), getHeight(), getBufferedImageType());
			    if (image != null) {
			    	WritableRaster raster = image.getRaster();
			    	raster.setDataElements(getX(), getY(), getWidth(), getHeight(), p);
			    }			    
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			if (image != null) {
				image.flush();
				image = null;	
			}
    		System.gc();
		}
	}

	/**
	 * Get buffered image type
	 * @return
	 */
	private int getBufferedImageType() {
		int type;
		switch (getColor()) {
			case IMAGE_TYPE_BINARY:
			case IMAGE_TYPE_BINARY_DITHER:
				type = BufferedImage.TYPE_BYTE_BINARY;
				break;
			case IMAGE_TYPE_GRAY:
				type = BufferedImage.TYPE_BYTE_GRAY;
				break;
			case IMAGE_TYPE_RGB:
				type = BufferedImage.TYPE_INT_RGB;
				break;
			case IMAGE_TYPE_ARGB:
				type = BufferedImage.TYPE_INT_ARGB;
				break;
			case IMAGE_TYPE_ARGB_PRE:
				type = BufferedImage.TYPE_INT_ARGB_PRE;
				break;
			case IMAGE_TYPE_BGR:
				type = BufferedImage.TYPE_INT_BGR;
				break;
			default:
				type = BufferedImage.TYPE_INT_RGB;
				break;
		}
		return type;
	}

	/**
	 * Determine if color type is a byte type.
	 * @return
	 */
	private boolean isByteData() {
		return (getColor() == IMAGE_TYPE_BINARY        || 
		        getColor() == IMAGE_TYPE_BINARY_DITHER ||
		        getColor() == IMAGE_TYPE_GRAY);
	}

	/**
	 * Return a new copy of PagePixels object
	 */
	public Object clone() {
		PagePixels p = new PagePixels(getPage());
		p.setAntiAliasLevel(getAntiAliasLevel());
		p.setColor(getColor());
		p.setGamma(getGamma());
		p.setRotate(getRotate());
		p.setZoom(getZoom());
		return p;
	}

	/**
	 * Dispose of resources
	 */
	public void dispose() {
		if (image != null) {
			image.flush();
		}
		pixels = null;
		buffer = null;
		boundBox = null;
	}
	
}
