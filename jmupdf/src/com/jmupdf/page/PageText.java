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
	private float x0;
	private float y0;
	private float x1;
	private float y1;
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
	public PageText(float x0, float y0, float x1, float y1, int eol, int[] text) {
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
	public float getX0() {
		return x0;
	}

	/**
	 * Get y0 coordinate of text
	 * @return
	 */
	public float getY0() {
		return y0;
	}

	/**
	 * Get x1 coordinate of text
	 * @return
	 */
	public float getX1() {
		return x1;
	}

	/**
	 * Get y1 coordinate of text
	 * @return
	 */
	public float getY1() {
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
	
	/* */
	/* */
	
	/**
	 * Helper method to return a single string from PageText array
	 * 
	 * @param textArr
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return
	 */
	public static String getStringFromArray(PageText[] textArr) {
		String text = "";
		
		if (textArr == null) {
			return text;
		}
		
		float len;
		
		for(int i=0; i<textArr.length; i++) {
			text += textArr[i].getText();
			if (textArr[i].isEndOfLine()) {
				if (i == textArr.length-1) {
					text += "\n";
				} else {
					 if ((textArr[i].getY0() == textArr[i+1].getY0())) {
						 len = textArr[i+1].getX1() - textArr[i].getX1();
						 if (len > 1) {
							 text += " ";
						 }
					 } else {
						 text += "\n";
					 }
				}
			}	
		}

		return text;
	}
	
    /**
     * Print test messages
     * @param text
     */
    protected static void log(String text) {
    	System.out.println(text);
    }
    
}
