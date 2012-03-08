/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.document;

import com.jmupdf.enums.LinkType;

/**
 * Outline Class
 * 
 * This class returns a data structure of entries that can be used to build a
 * tree view for navigating a document.
 * 
 * @author Pedro J Rivera
 *
 */
public class Outline {
	private Outline next;
	private Outline child;
	private LinkType type;
	private String destination;
	private String title;	
	private float x0;
	private float y0;
	private float x1;
	private float y1;
	private int page;
	
	/**
	 * Constructor
	 * @param title
	 * @param page
	 * 
	 */
	public Outline(int type, String title, String destination) {
		this.next = null;
		this.child = null;
		this.type = LinkType.setType(type);
		this.destination = destination;
		this.title = title;
		this.x0 = 0;
		this.y0 = 0;
		this.x1 = 0;
		this.y1 = 0;
		this.page = 0;
		if (getType() == LinkType.LINK_GOTO) {
			this.page = Integer.valueOf(destination);
		}
	}

	/**
	 * Constructor
	 * 
	 */
	public Outline() {
		this(0, "", "");
	}
	
	public Outline addChild() {		
		child = new Outline(0, "", "");
		return child;
	}

	public Outline getChild() {		
		return child;
	}
	
	public Outline addNext() {		
		next = new Outline(0, "", "");
		return next;
	}
	
	public Outline getNext() {		
		return next;
	}

	/**
	 * Set link type
	 * @param type
	 */
	public void setType(int type) {
		this.type = LinkType.setType(type);
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
	 * Set title of outline item
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Get title of outline item
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set page number
	 * @param page
	 */
	public void setPage(int page) {
		this.page = page;
		this.destination = "" + page;
	}

	/**
	 * Get page number
	 * @return
	 */
	public int getPage() {
		return page;
	}

	/**
	 * Set rectangle of link. This only applies for LINK_GOTO.
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	public void setRect(float x0, float y0, float x1, float y1) {
		this.x0 = x0;
		this.y0 = y0;
		this.x1 = x1;
		this.y1 = y1;
	}

	/**
	 * Get x0 coordinate of link
	 * @return
	 */
	public float getX0() {
		return x0;
	}
	
	/**
	 * Get y0 coordinate of link
	 * @return
	 */
	public float getY0() {
		return y0;
	}

	/**
	 * Get x1 coordinate of link
	 * @return
	 */
	public float getX1() {
		return x1;
	}

	/**
	 * Get y1 coordinate of link
	 * @return
	 */
	public float getY1() {
		return y1;
	}
	
	/**
	 * Set destination.
	 * @param destination
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	/**
	 * Get link destination. This could be a URL or a page number.
	 * @return
	 */
	public String getDestination() {
		return destination;
	}
	
}
