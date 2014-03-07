package com.vanchu.libs.platform;

public class SinaCfg implements IPlatformCfg {

	private String _appKey;
	private String _appSecret;
	private String _redirectUrl;
	private String _scope;
	
	public SinaCfg(String appKey, String appSecret, String redirectUrl, String scope) {
		_appKey		= appKey;
		_appSecret	= appSecret;
		_redirectUrl	= redirectUrl;
		_scope			= scope;
	}
	
	public String getAppKey() {
		return _appKey;
	}
	
	public String getAppSecret() {
		return _appSecret;
	}
	
	public String getRedirectUrl() {
		return _redirectUrl;
	}
	
	public String getScope() {
		return _scope;
	}
}
