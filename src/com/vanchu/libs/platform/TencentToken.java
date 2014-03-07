package com.vanchu.libs.platform;


public class TencentToken implements IToken {
	private String _pf;
	private String _pfKey;
	private String _appId;
	private String _openId;
	private String _accessToken;
	private String _payToken;
	private String _expireIn;
	
	private long _expireAt;
	
	public TencentToken(String pf, String pfKey, String appId, String openId, 
							String accessToken, String payToken, String expireIn) {
	
		_pf		= pf;
		_pfKey	= pfKey;
		_appId	= appId;
		_openId	= openId;
		_accessToken	= accessToken;
		_payToken		= payToken;
		_expireIn		= expireIn;
		
		if(null == _expireIn || _expireIn.equals("")) {
			_expireAt	= 0;
		} else {
			_expireAt	= System.currentTimeMillis() + Long.parseLong(_expireIn) * 1000;
		}
	}
	
	public String getPf() {
		return _pf;
	}
	
	public String getPfKey() {
		return _pfKey;
	}
	
	public String getAppId() {
		return _appId;
	}
	
	public String getOpenId() {
		return _openId;
	}
	
	public String getAccessToken() {
		return _accessToken;
	}
	
	public String getPayToken() {
		return _payToken;
	}
	
	public String getExpireIn() {
		return String.valueOf((_expireAt - System.currentTimeMillis()) / 1000);
	}
	
	public long getExpireAt() {
		return _expireAt;
	}
	
 	@Override
	public boolean isTokenValid() {
		return (System.currentTimeMillis() < _expireAt 
				&& null != _openId && (!_openId.equals("")) );
	}
}
