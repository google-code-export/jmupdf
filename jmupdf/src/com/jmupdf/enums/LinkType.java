/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.enums;

/**
 * LinkType enum
 * 
 * @author Pedro J Rivera
 *
 */
public enum LinkType {

	LINK_NONE(0),
	LINK_GOTO(1), 
	LINK_URL(2), 
	LINK_LAUNCH(3), 
	LINK_NAMED(4),
	LINK_GOTOR(5);

	private int type;

	private LinkType(int type) {
		this.type = type;
	}
	
	public int getIntValue() {
		return type;
	}
	
	public static LinkType setType(int type) {
		LinkType lt;
		switch (type) {
		case 1:
			lt = LINK_GOTO;
			break;
		case 2:
			lt = LINK_URL;
			break;
		case 3:
			lt = LINK_LAUNCH;
			break;
		case 4:
			lt = LINK_NAMED;
			break;
		case 5:
			lt = LINK_GOTOR;
			break;
		default:
			lt = LINK_NONE;
			break;
		}
		return lt;
	}

}
