/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.page;

import com.jmupdf.interfaces.Page;

/**
 * PageRect Class
 * 
 * Object is reusable.
 * 
 * @author Pedro J Rivera
 *
 */
public class PageRect {
    private float p1x;
    private float p1y;
    private float p2x;
    private float p2y;

    private int x;
    private int y;
    private int w;
    private int h;

	/**
	 * Cartesian or Java2D representation of a rectangle.
	 */
	public PageRect() {
		this(0, 0, 0, 0);
	}
	
    /**
     * Cartesian or Java2D representation of a rectangle.
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     */
	public PageRect(float x0, float y0, float x1, float y1) {
		setRect(x0, y0, x1, y1);
	}

	/**
	 * Cartesian or Java2D representation of a rectangle.
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public PageRect(int x, int y, int w, int h) {
		setRect(x, y, w, h);
	}
	
	/**
	 * Set rectangle 
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	public void setRect(float x0, float y0, float x1, float y1) {
		this.p1x = x0;
		this.p1y = y0;
		this.p2x = x1;
		this.p2y = y1;
		normalizeCoordinates(p1x, p1y, p2x, p2y);
	}

	/**
	 * Set rectangle
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public void setRect(int x, int y, int w, int h) {
		setRect((float)x, (float)y, (float)x+w, (float)y+h);
	}
	
	/**
	 * Get X coordinate
	 * @return
	 */
    public int getX() {
    	return x;    	
    }
    
    /**
     * Get Y coordinate
     * @return
     */
    public int getY() {
    	return y;
    }
    
    /**
     * Get width
     * @return
     */
    public int getWidth() {
    	return w;
    }
    
    /**
     * Get height
     * @return
     */
    public int getHeight() {
    	return h;
    }

    /**
     * Get point one x
     * @return
     */
    public float getX0() {
    	return p1x;
    }
    
    /**
     * Get point one y
     * @return
     */
    public float getY0() {
    	return p1y;
    }
    
    /**
     * Get point two x
     * @return
     */
    public float getX1() {
    	return p2x;
    }
    
    /**
     * Get point two y
     * @return
     */
    public float getY1() {
    	return p2y;
    }

    /**
     * Scale rectangle
     * @param zoom
     * @return
     */
    public PageRect scale(float zoom) {
    	return new PageRect(p1x*zoom, p1y*zoom, p2x*zoom, p2y*zoom);
    }
    
    /**
     * Rotate rectangle 
     * @param boundBox
     * @param rotation
     * @return rotated rectangle
     */
    public PageRect rotate(PageRect boundBox, int rotation) {
    	
    	// Assure a positive rotation
    	rotation = rotate360(rotation);

    	// Exit if rotation = 0
    	if (rotation == Page.PAGE_ROTATE_NONE || rotation == Page.PAGE_ROTATE_360) {
   			return new PageRect(p1x, p1y, p2x, p2y);
    	}
    	
    	// Get radians
		float rad = (float)Math.toRadians(rotation);
		
        // Get center point of rotation
		float cx = (boundBox.getX() + boundBox.getWidth())/2;
		float cy = (boundBox.getY() + boundBox.getHeight())/2;

        // Get new rotated media-box points
		float[] r1 = rotate(boundBox, rad, cx, cy);
        float p_mbx1 = r1[0];
        float p_mby1 = r1[1];
        float p_mbx2 = r1[2];
        float p_mby2 = r1[3];
        
        // Get new rotated region points
        float[] r2 = rotate(this, rad, cx, cy);
        float p_x1 = r2[0];
        float p_y1 = r2[1];
        float p_x2 = r2[2];
        float p_y2 = r2[3];
        float p_w  = Math.abs(p_x2 - p_x1);
        float p_h  = Math.abs(p_y2 - p_y1);
        
        // New rotated points
        float mbx1 = 0;
        float mby1 = 0;
        float x0 = 0;
        float y0 = 0;
        float x1 = 0;
        float y1 = 0;
        
        // Adjust rotated points to x0, y0 (upper left) coordinates 
        if (rotation == Page.PAGE_ROTATE_90) {
            mbx1 = Math.max(p_mbx2, p_mby2) + Math.min(p_mbx1, p_mby1);
            mby1 = Math.max(p_mbx2, p_mby2) - Math.min(p_mbx1, p_mby1);
            x0 = mbx1 - Math.abs(p_mbx1 - p_x1) - p_w;
            y0 = Math.abs(p_mby1 - p_y1);
        }
        else if (rotation == Page.PAGE_ROTATE_180) {
            mbx1 = p_mbx1;
            mby1 = p_mby1;
            x0 = mbx1 - Math.abs(p_mbx1 - p_x1) - p_w;
            y0 = mby1 - Math.abs(p_mby1 - p_y1) - p_h;
        }
        else if (rotation == Page.PAGE_ROTATE_270) {
            mbx1 = Math.max(p_mbx2, p_mby2) - Math.min(p_mbx1, p_mby1);
            mby1 = Math.max(p_mbx2, p_mby2) + Math.min(p_mbx1, p_mby1);
            x0 = Math.abs(p_mbx1 - p_x1);
            y0 = mby1 - Math.abs(p_mby1 - p_y1) - p_h;
        }
        
        // Set x1, y1 coordinates
        x1 = x0 + p_w;
        y1 = y0 + p_h;
        
        return new PageRect(x0, y0, x1, y1);   
    }
    
    /**
     * Rotate rectangle </br>
     * Get new coordinates by rotating current coordinates from "fromRotation" to "toRotation"  
     * @param boundBox : Original bounding box from getPage().getBoundBox()
     * @param fromRotation
     * @param toRotation
     * @return
     */
    public PageRect rotate(PageRect boundBox, int fromRotation, int toRotation) {
    	PageRect b = boundBox.rotate(boundBox, fromRotation);
    	PageRect c = new PageRect(getX0(), getY0(), getX1(), getY1());
    	return c.rotate(b, -(fromRotation-toRotation));
    }
    
    /**
     * Convert a negative rotation to a positive rotation. 
     * @param rotate
     * @return
     */
    public static int rotate360(int rotate) {
       	int rotation = rotate % 360;
    	if (rotation < 0) {
    		rotation += 360;
    	}
    	return rotation;
    }
    
    /**
     * Return new rotated coordinates
     * @param rect
     * @param rad
     * @param cx
     * @param cy
     * @return
     */
    private float[] rotate(PageRect rect, float rad, float cx, float cy) {
    	float[] cor = new float[4];

    	float x0 = rect.getX0();
    	float y0 = rect.getY0();
    	float x1 = rect.getX1();
    	float y1 = rect.getY1();
        
        cor[0] = (float)((x0-cx) * Math.cos(rad) - (y0-cy) * Math.sin(rad) + cx);
        cor[1] = (float)((x0-cx) * Math.sin(rad) + (y0-cy) * Math.cos(rad) + cy);
        cor[2] = (float)((x1-cx) * Math.cos(rad) - (y1-cy) * Math.sin(rad) + cx);
        cor[3] = (float)((x1-cx) * Math.sin(rad) + (y1-cy) * Math.cos(rad) + cy);
    	
    	return cor;
    }
    
	/**
     * Normalize the given coordinates so that the rectangle is created with the
     * proper dimensions.
     * @param x1 
     * @param y1 
     * @param x2 
     * @param y2 
     */
    private void normalizeCoordinates(float x1, float y1, float x2, float y2) {
    	this.x = (int)x1;
    	this.y = (int)y1;
    	this.w = (int)Math.abs(x2 - x1);
    	this.h = (int)Math.abs(y2 - y1);
    }
	
    /**
     * Print test messages
     * @param text
     */
    protected void log(String text) {
    	System.out.println(text);
    }

}
