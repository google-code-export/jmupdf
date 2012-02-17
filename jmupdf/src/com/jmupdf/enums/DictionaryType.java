/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.enums;

/**
 * DictionaryType enum
 * 
 * @author Pedro J Rivera
 *
 */
public enum DictionaryType {

	INFO_CREATION_DATE("CreationDate"), 
	INFO_MODIFIED_DATE("ModDate"),
	INFO_TITLE("Title"),
	INFO_AUTHOR("Author"),
	INFO_SUBJECT("Subject"),
	INFO_KEYWORDS("Keywords"),
	INFO_CREATOR("Creator"),
	INFO_PRODUCER("Producer"),
	INFO_TRAPPED("Trapped");
	
	private String type;
	
	private DictionaryType(String type) {
		this.type = type;
	}
	
	public String getStrValue() {
		return type;
	}
	
}
