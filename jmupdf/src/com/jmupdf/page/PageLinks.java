/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.page;

import com.jmupdf.enums.LinkType;

/**
 * Links Class
 * 
 * @author Pedro J Rivera
 *
 */
public class PageLinks {
	private float x0;
	private float y0;
	private float x1;
	private float y1;
	private LinkType type;
	private String destination;

	/**
	 * Create a new page links instance
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param type
	 * @param destination
	 */
	public PageLinks(float x0, float y0, float x1, float y1, int type, String destination) {
		this.x0 = x0;
		this.y0 = y0;
		this.x1 = x1;
		this.y1 = y1;
		this.type = LinkType.setType(type);
		this.destination = destination;
	}

	/**
	 * Get x0 coordinate of link
	 * @return
	 */
	public float getX0() {
		return x0;
	}
	
	/**
	 * Set x0 coordinate of link
	 * @param x
	 */
	public void setX0(float x) {
		this.x0 = x;
	}
	
	/**
	 * Get y0 coordinate of link
	 * @return
	 */
	public float getY0() {
		return y0;
	}

	/**
	 * Set y0 coordinate of link
	 * @param y
	 */
	public void setY0(float y) {
		this.y0 = y;
	}
	
	/**
	 * Get x1 coordinate of link
	 * @return
	 */
	public float getX1() {
		return x1;
	}

	/**
	 * Set x1 coordinate of link
	 * @param x
	 */
	public void setX1(float x) {
		this.x1 = x;
	}

	/**
	 * Get y1 coordinate of link
	 * @return
	 */
	public float getY1() {
		return y1;
	}

	/**
	 * Set y1 coordinate of link
	 * @param y
	 */
	public void setY1(float y) {
		this.y1 = y;
	}

	/**
	 * Get link type.
	 * @see LinkTypes
	 * @return
	 */
	public LinkType getType() {
		return type;
	}

	/**
	 * Get link destination. This could be a URL or a page number.
	 * @return
	 */
	public String getDestination() {
		return destination;
	}

    /**
     * Print test messages
     * @param text
     */
    protected void log(String text) {
    	System.out.println(text);
    }
}
