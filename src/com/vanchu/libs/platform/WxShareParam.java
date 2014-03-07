package com.vanchu.libs.platform;

import android.graphics.Bitmap;

public class WxShareParam {

	private String	_appId;
	private String	_title;
	private String	_desc;
	private String	_targetUrl;
	private Bitmap	_pic;
	private boolean	_toCircle;
	
	public WxShareParam(String appId, String title, String desc, String targetUrl, Bitmap pic, boolean toCircle) {
		_appId	= appId;
		_title	= title;
		_desc	= desc;
		_targetUrl	= targetUrl;
		_pic		= pic;
		_toCircle	= toCircle;
	}
	
	public String getAppId() {
		return _appId;
	}
	
	public String getTitle() {
		return _title;
	}
	
	public String getDesc() {
		return _desc;
	}
	
	public String getTargetUrl() {
		return _targetUrl;
	}
	
	public Bitmap getPic() {
		return _pic;
	}
	
	public boolean isToCircle() {
		return _toCircle;
	}
}
