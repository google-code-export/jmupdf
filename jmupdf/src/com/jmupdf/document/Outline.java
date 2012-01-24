/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.document;

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
	private int page;
	private String title;
	private Outline next;
	private Outline child;
	
	/**
	 * Constructor
	 * 
	 */
	public Outline() {
		this("", 0);
	}
	
	/**
	 * Constructor
	 * @param title
	 * @param page
	 * 
	 */
	public Outline(String title, int page) {
		this.page = page;
		this.title = title;
		next = null;
		child = null;
	}
		
	public Outline addChild() {		
		child = new Outline("", 0);
		return child;
	}

	public Outline getChild() {		
		return child;
	}
	
	public Outline addNext() {		
		next = new Outline("", 0);
		return next;
	}
	
	public Outline getNext() {		
		return next;
	}

	public void setPage(int page) {
		this.page = page;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public int getPage() {
		return page;
	}
		
}
