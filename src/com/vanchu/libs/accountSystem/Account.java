package com.vanchu.libs.accountSystem;

public class Account {
	
	public static final int	LOGIN_TYPE_VANCHU	= 1;
	public static final int	LOGIN_TYPE_TENCENT	= 2;
	public static final int	LOGIN_TYPE_SINA		= 3;
	
	private String 	_uid;
	private String 	_auth;
	private String 	_pauth;
	private int		_loginType;
	
	public Account(String uid, String auth, String pauth, int loginType) {
		_uid	= uid;
		_auth	= auth;
		_pauth	= pauth;
		_loginType	= loginType;
	}
	
	public String getUid() {
		return _uid;
	}
	
	public String getAuth() {
		return _auth;
	}
	
	public String getPauth() {
		return _pauth;
	}
	
	public int getLoginType() {
		return _loginType;
	}
	
	public boolean isLogon() {
		if(_uid.equals("") || _auth.equals("") || _pauth.equals("") ) {
			return false;
		} else {
			return true;
		}
	}
}
