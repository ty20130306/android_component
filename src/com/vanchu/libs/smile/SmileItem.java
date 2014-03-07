package com.vanchu.libs.smile;

public class SmileItem {

	private String faceName;
	private int sourseId;

	public SmileItem(String key, int sourseId) {
		this.faceName = key;
		this.sourseId = sourseId;
	}

	public String getFaceName() {
		return faceName;
	}

	public int getSourseId() {
		return sourseId;
	}

}