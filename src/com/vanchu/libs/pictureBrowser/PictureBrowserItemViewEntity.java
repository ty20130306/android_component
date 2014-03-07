package com.vanchu.libs.pictureBrowser;

import java.io.Serializable;

public class PictureBrowserItemViewEntity implements Serializable{
	
	private static final long serialVersionUID = 1L;

	/**
	 * Layout File id
	 */
	private int layoutResource = -1;

	/**
	 * ImageView id
	 */
	private int imageviewId = -1;

	/**
	 * ProgressBar id
	 */
	private int progressbarId = -1;

	/**
	 * TextView Show Progress Value
	 */
	private int textProgressId = -1;
	
	private int defImageViewId = -1;

	public PictureBrowserItemViewEntity(int layoutResource, int imageviewId,
			int progressbarId, int textProgressId,int defImageViewId) {
		this.layoutResource = layoutResource;
		this.imageviewId = imageviewId;
		this.progressbarId = progressbarId;
		this.textProgressId = textProgressId;
		this.defImageViewId = defImageViewId;
	}

	public int getLayoutResource() {
		return layoutResource;
	}

	public int getImageviewId() {
		return imageviewId;
	}

	public int getProgressbarId() {
		return progressbarId;
	}

	public int getTextProgressId() {
		return textProgressId;
	}
	
	public int getDefImageViewId() {
		return defImageViewId;
	}
}
