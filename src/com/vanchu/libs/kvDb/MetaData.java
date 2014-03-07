package com.vanchu.libs.kvDb;

public class MetaData {
	
	public static final long NEVER_EXPIRE		= -1;
	
	private String	_key		= null;
	private String	_value		= null;
	private long	_expire		= NEVER_EXPIRE;
	private long	_touchTime	= System.currentTimeMillis();
	private long	_updateTime	= System.currentTimeMillis();
	private long	_createTime	= System.currentTimeMillis();
	private boolean	_exist		= false;
	
	public MetaData(String key) {
		_key	= key;
	}
	
	public MetaData(String key, String value, long expire) {
		_key	= key;
		_value	= value;
		_expire	= expire;
		_exist		= true;
	}
	
	/**************getter*******************/
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
	
	public long getUpdateTime() {
		return _updateTime;
	}
	
	public long getCreateTime() {
		return _createTime;
	}
	
	public boolean exist() {
		return _exist;
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
	
	public void setUpdateTime(long updateTime) {
		_updateTime	= updateTime;
	}
	
	public void setCreateTime(long createTime) {
		_createTime	= createTime;
	}
	
	public void setTouchTime(long touchTime) {
		_touchTime	= touchTime;
	}
	
}
