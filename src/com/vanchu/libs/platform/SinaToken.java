package com.vanchu.libs.platform;

public class SinaToken implements IToken {
	
	private String _uid;
	private String _accessToken;
	private long _expireTime;
	
	public SinaToken(String uid, String accessToken, long expireTime) {
		_uid			= uid;
		_accessToken	= accessToken;
		_expireTime		= expireTime;
	}
	
	public String getUid() {
		return _uid;
	}
	
	public String getAccessToken() {
		return _accessToken;
	}
	
	public long getExpireTime() {
		return _expireTime;
	}
	
	@Override
	public boolean isTokenValid() {
		return (System.currentTimeMillis() < _expireTime 
				&& null != _uid && ! _uid.equals("") );
	}
}
 