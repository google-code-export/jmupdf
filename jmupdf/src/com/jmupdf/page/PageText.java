/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.page;

/**
 * PageText class 
 * 
 * @author Pedro J Rivera
 *
 */
public class PageText {
	private int x0;
	private int y0;
	private int x1;
	private int y1;
	private boolean endOfLine; 
	private String text;

	/**
	 * Create text span instance
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param eol
	 * @param text
	 */
	public PageText(int x0, int y0, int x1, int y1, int eol, int[] text) {
		this.x0 = x0;
		this.y0 = y0;
		this.x1 = x1;
		this.y1 = y1;
		this.endOfLine = eol == 1;
		this.text = "";
		toString(text);
	}

	/**
	 * Get x0 coordinate of text
	 * @return
	 */
	public int getX0() {
		return x0;
	}

	/**
	 * Get y0 coordinate of text
	 * @return
	 */
	public int getY0() {
		return y0;
	}

	/**
	 * Get x1 coordinate of text
	 * @return
	 */
	public int getX1() {
		return x1;
	}

	/**
	 * Get y1 coordinate of text
	 * @return
	 */
	public int getY1() {
		return y1;
	}

	/**
	 * Determine if this is the end of line for text
	 * @return
	 */
	public boolean isEndOfLine() {
		return endOfLine;
	}
	
	/**
	 * Get text
	 * @return
	 */
	public String getText() {
		return text;
	}

	/**
	 * Convert int array to string
	 * @param text
	 */
	private void toString(int[] text) {
		for (int i=0; i<text.length; i++) {
			if (text[i] == 0) {
				break;
			}
			if (text[i] < 32) {				
				this.text += "?";
			} else {
				this.text += (char)text[i];
			}
		}
	}
	
    /**
     * Print test messages
     * @param text
     */
    protected static void log(String text) {
    	System.out.println(text);
    }
    
}
