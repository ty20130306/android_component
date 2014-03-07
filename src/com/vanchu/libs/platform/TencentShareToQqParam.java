package com.vanchu.libs.platform;

import com.tencent.tauth.Constants;

import android.os.Bundle;

public class TencentShareToQqParam {

	/**
	 * 必选, 这条分享消息被好友点击后的跳转URL。
	 */
	private String	_targetUrl	= null;
	
	/**
	 * 可选, 分享的标题。
	 * 注：PARAM_TITLE、PARAM_IMAGE_URL、PARAM_SUMMARY不能全为空，最少必须有一个是有值的。
	 */
	private String	_title	= null;
	
	/**
	 * 可选, 分享的图片URL。
	 */
	private String	_imageUrl	= null;
	
	/**
	 * 可选, 分享的消息摘要，最长50个字
	 */
	private String	_summary	= null;
	
	/**
	 * 可选, 标识该消息的来源应用，值为应用名称+AppId。
	 */
	private String	_appSource	= null;
	
	/**
	 * 可选, 手Q客户端顶部，替换“返回”按钮文字，如果为空，用返回代替
	 */
	private String	_appName	= null;
	
	public TencentShareToQqParam(String targetUrl, String title, String imageUrl) {
		_targetUrl	= targetUrl;
		_title		= title;
		_imageUrl	= imageUrl;
	}
	
	public void setSummary(String summary) {
		_summary	= summary;
	}
	
	public void setAppSource(String appSource) {
		_appSource	= appSource;
	}
	
	public void setAppName(String appName) {
		_appName	= appName;
	}

	public Bundle getParamBundle() {
		Bundle bundle	= new Bundle();
		bundle.putString(Constants.PARAM_TARGET_URL, _targetUrl);
		bundle.putString(Constants.PARAM_TITLE, _title);
		bundle.putString(Constants.PARAM_IMAGE_URL, _imageUrl);
		
		if(null != _summary) {
			bundle.putString(Constants.PARAM_SUMMARY, _summary);
		}
		
		if(null != _appSource) {
			bundle.putString(Constants.PARAM_APP_SOURCE, _appSource);
		}
		
		if(null != _appName) {
			bundle.putString(Constants.PARAM_APPNAME, _appName);
		}
		
		return bundle;
	}
}
