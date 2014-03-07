package com.vanchu.libs.kvDb;

import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;

public class KvDb {
	
	private static final String	LOG_TAG	= SqlDbManager.class.getSimpleName();
	
	private String 			_dbName;
	private SqlDbManager	_sqlDbManager;
	private KvDbCfg			_kvDbCfg;
	
	public KvDb(Context context, String dbName, KvDbCfg kvDbCfg) {
		_dbName			= dbName;	
		_sqlDbManager	= new SqlDbManager(context, dbName);
		_kvDbCfg		= kvDbCfg;
	}
	
	private void lru() {
		int capacity	= _kvDbCfg.getCapacity();
		if(KvDbCfg.CAPACITY_NOT_LIMIT == capacity) {
			return ;
		}
		
		if(capacity <= 0) {
			_sqlDbManager.deleteAll();
			 return ;
		}
		
		int dbSize	= _sqlDbManager.getDbSize();
		int toDeleteNum	= dbSize - capacity;
		if(toDeleteNum < _kvDbCfg.getLruThreshold()) {
			return ;
		}
		
		_sqlDbManager.deleteLruKey(_kvDbCfg.getLruThreshold());
	}
	
	private boolean hasExpired(long expire, long currentTime) {
		if(MetaData.NEVER_EXPIRE != expire && expire < currentTime) {
			return true;
		} else {
			return false;
		}
	}
	
	/*********************************** public methods **************************/
	public String getDbName() {
		return _dbName;
	}
	
	/**
	 * 获取db里的key的数量（包含暂时还没有淘汰掉的）
	 * @return
	 */
	public int getDbSize() {
		return _sqlDbManager.getDbSize();
	}
	
	/**
	 * 获取KvDb配置
	 * @return
	 */
	public KvDbCfg getCfg() {
		return _kvDbCfg;
	}
	
	/**
	 * 获取key的存活时间
	 * @param key
	 * @return 正常情况返回存活时间，单位毫秒；出错，key不存在或已过期，返回0
	 */
	public long ttl(String key) {
		if(null == key) {
			SwitchLogger.e(LOG_TAG, "ttl fail, key is null");
			return 0;
		}
		
		// key not found
		MetaData md		= _sqlDbManager.get(key);
		if( ! md.exist()) {
			return 0;
		}
		
		// key has expired
		long currentTime	= System.currentTimeMillis();
		if(hasExpired(md.getExpire(), currentTime)) {
			_sqlDbManager.delete(key);
			return 0;
		}
		
		// update touch time & get ttl
		_sqlDbManager.updateTouchTime(key);
		if(MetaData.NEVER_EXPIRE == md.getExpire()) {
			return MetaData.NEVER_EXPIRE;
		} else {
			return md.getExpire() - currentTime;
		}
	}
	
	/**
	 * 设置key的过期时间
	 * @param key
	 * @param millis 存活时间，单位毫秒，永不过期使用宏MetaData.NEVER_EXPIRE
	 * @return 成功返回true，失败返回false
	 */
	public boolean expire(String key, long millis) {
		if(null == key) {
			SwitchLogger.e(LOG_TAG, "expire fail, key is null");
			return false;
		}
		
		MetaData md	= _sqlDbManager.get(key);
		if( ! md.exist()) {
			SwitchLogger.e(LOG_TAG, "expire fail, key not found" );
			return false;
		}
		
		long currentTime	= System.currentTimeMillis();
		if(MetaData.NEVER_EXPIRE != md.getExpire() && md.getExpire() < currentTime) {
			_sqlDbManager.delete(key);
			return false;
		}
		
		long expire		= currentTime + millis;
		if(MetaData.NEVER_EXPIRE == millis){
			expire	= MetaData.NEVER_EXPIRE;
		}
		
		return _sqlDbManager.updateExpireAndTouchTime(key, expire);
	}
	
	/**
	 * 保存数据
	 * @param key
	 * @param value
	 * @param expire 存活时间，单位毫秒，永不过期使用宏MetaData.NEVER_EXPIRE
	 * @return 成功返回true，失败返回false
	 */
	public boolean set(String key, String value, long expire) {
		SwitchLogger.d(LOG_TAG, "call set with key="+key+", value="+value+", expire="+expire);
		
		if(null == key) {
			SwitchLogger.e(LOG_TAG, "set fail, key is null");
			return false;
		}
		
		long currentTime	= System.currentTimeMillis();
		MetaData md	= null;
		if(MetaData.NEVER_EXPIRE == expire) {
			md	= new MetaData(key, value, expire);
		} else {
			md	= new MetaData(key, value, currentTime + expire);
		}
		
		lru();
		
		return _sqlDbManager.set(md);
	}
	
	/**
	 * 保存数据，数据永不过期
	 * @param key
	 * @param value
	 * @return 成功返回true，失败返回false
	 */
	public boolean set(String key, String value) {
		return set(key, value, MetaData.NEVER_EXPIRE);
	}
	
	/**
	 * 获取数据
	 * @param key
	 * @return 该key对应的值，出错,key不存在或已经过期，返回null
	 */
	public String get(String key) {
		SwitchLogger.d(LOG_TAG, "call get with key=" + key);
		
		if(null == key) {
			SwitchLogger.e(LOG_TAG, "get fail, key is null");
			return null;
		}
		
		MetaData md		= _sqlDbManager.get(key);
		if( ! md.exist()) {
			return null;
		}
		
		long currentTime	= System.currentTimeMillis();
		if(MetaData.NEVER_EXPIRE != md.getExpire() && md.getExpire() < currentTime) {
			_sqlDbManager.delete(key);
			return null;
		} else {
			_sqlDbManager.updateTouchTime(key);
			return md.getValue();
		}
	}
	
	/**
	 * 删除数据
	 * @param key
	 * @return 成功返回true，出错或失败返回false（删除已过期的key也返回true）
	 */
	public boolean delete(String key) {
		SwitchLogger.d(LOG_TAG, "call delete with key=" + key);
		if(null == key) {
			SwitchLogger.e(LOG_TAG, "delete fail, key is null");
			return false;
		}
		
		return _sqlDbManager.delete(key);
	}
	
	public void close() {
		_sqlDbManager.close();
	}
}
