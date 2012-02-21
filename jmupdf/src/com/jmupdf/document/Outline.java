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
	private int page;
	
	/**
	 * Constructor
	 * 
	 */
	public Outline() {
		this(0, "", "");
	}
	
	/**
	 * Constructor
	 * @param title
	 * @param page
	 * 
	 */
	public Outline(int type, String title, String destination) {
		this.type = LinkType.setType(type);
		this.destination = destination;
		this.page = 0;
		if (getType() == LinkType.LINK_GOTO) {
			this.page = Integer.valueOf(destination);
		}
		this.title = title;
		next = null;
		child = null;
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
