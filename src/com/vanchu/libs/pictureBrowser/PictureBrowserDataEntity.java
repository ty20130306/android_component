package com.vanchu.libs.pictureBrowser;

import java.io.Serializable;

import android.graphics.drawable.Drawable;

import com.vanchu.libs.webCache.WebCache;

public class PictureBrowserDataEntity implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private WebCache webCache  = null;
	private String defImgUrl = null;
	private Drawable defDrawable = null;
	private String detailUrl = null;

	/***
	 * 可传递图片drawable
	 * @param defDrawable 默认图片
	 * @param detailUrl   大图url
	 */
	public PictureBrowserDataEntity(Drawable defDrawable, String detailUrl) {
		this.defDrawable = defDrawable;
		this.detailUrl = detailUrl;
	}

	/***
	 * 传递图片的url
	 * @param defImgUrl 默认url
	 * @param detailUrl 大图url
	 * @param webCache 默认图片的url缓存
	 */
	public PictureBrowserDataEntity(String defImgUrl, String detailUrl,WebCache webCache) {
		this.defImgUrl = defImgUrl;
		this.detailUrl = detailUrl;
		this.webCache = webCache;
	}

	public WebCache getWebCache() {
		return webCache;
	}
	
	public String getDefImgUrl() {
		return defImgUrl;
	}
	
	public Drawable getDefDrawable() {
		return defDrawable;
	}

	public String getDetailUrl() {
		return detailUrl;
	}
}
