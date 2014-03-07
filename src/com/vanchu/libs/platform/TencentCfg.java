package com.vanchu.libs.platform;

public class TencentCfg implements IPlatformCfg {
	
	private String _appid;
	private String _scope;
	
	public TencentCfg(String appId, String scope) {
		_appid	= appId;
		_scope	= scope;
	}
	
	public String getAppId() {
		return _appid;
	}
	
	public String getScope() {
		return _scope;
	}
}
