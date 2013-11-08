package com.vanchu.libs.kvDb;

public class MetaData {
	
	public static final long NEVER_EXPIRE		= -1;
	
	private String	_key		= null;
	private String	_value		= null;
	private long	_expire		= NEVER_EXPIRE;
	private long	_touchTime	= System.currentTimeMillis();
	private boolean	_exist		= false;
	
	public MetaData(String key) {
		_key	= key;
		_exist	= false;
	}
	
	public MetaData(String key, String value, long expire, long touchTime) {
		_key	= key;
		_value	= value;
		_expire	= expire;
		_touchTime	= touchTime;
		_exist		= true;
	}
	
	/**************getter*******************/
	public boolean exist() {
		return _exist;
	}
	
	public String getKey() {
		return _key;
	}

	public String getValue() {
		return _value;
	}

	public long getExpire() {
		return _expire;
	}

	public long getTouchTime() {
		return _touchTime;
	}

	/**************setter*********************/
	public void setExist(boolean exist) {
		_exist	= exist;
	}
	
	public void setKey(String key) {
		_key	= key;
	}
	
	public void setValue(String value) {
		_value	= value;
	}
	
	public void setExpire(long expire) {
		_expire	= expire;
	}
	
	public void setTouchTime(long touchTime) {
		_touchTime	= touchTime;
	}
	
}
