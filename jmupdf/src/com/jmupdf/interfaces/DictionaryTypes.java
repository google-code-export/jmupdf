/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.interfaces;

public interface DictionaryTypes {

	/*
	 * Predefined key values for "Info"
	 * 	
	 * 	See: http://www.verypdf.com/pdfinfoeditor/pdf-date-format.htm
	 * 	Format:  "D:YYYYMMDDHHMMSSxxxxxxx"
	 * 	Example: "D:20091222171933-05'00'" 
	 * 
	 */
	public static final String INFO_CREATION_DATE = "CreationDate"; 
	public static final String INFO_MODIFIED_DATE = "ModDate";
	public static final String INFO_TITLE = "Title";
	public static final String INFO_AUTHOR = "Author";
	public static final String INFO_SUBJECT = "Subject";
	public static final String INFO_KEYWORDS = "Keywords";
	public static final String INFO_CREATOR = "Creator";
	public static final String INFO_PRODUCER = "Producer";
	public static final String INFO_TRAPPED = "Trapped";
	
}
