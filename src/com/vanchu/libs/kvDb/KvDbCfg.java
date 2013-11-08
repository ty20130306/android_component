package com.vanchu.libs.kvDb;

public class KvDbCfg {
	
	public static final int CAPACITY_NOT_LIMIT		= -1;
	public static final int LRU_DEFAULT_THRESHOLD	= 10;
	
	private int		_capacity		= CAPACITY_NOT_LIMIT;
	private int 	_lruThreshold	= LRU_DEFAULT_THRESHOLD;
	
	/**
	 * 设置db容量，无限制请用宏 CAPACITY_NOT_LIMIT
	 * @param capacity
	 */
	public KvDbCfg setCapacity(int capacity) {
		if(CAPACITY_NOT_LIMIT != capacity && capacity <= 0) {
			capacity	= 0;
		}
		
		_capacity	= capacity;
		
		return this;
	}
	
	/**
	 * 获取db容量，判断是否无限制请用宏 CAPACITY_NOT_LIMIT
	 * @return
	 */
	public int getCapacity() {
		return _capacity;
	}
	
	public int getLruThreshold() {
		return _lruThreshold;
	}
	
	public KvDbCfg setLruThreshold(int lruThreshold) {
		if(lruThreshold < 1) {
			lruThreshold	= 1;
		}
		_lruThreshold	= lruThreshold;
		
		return this;
	}
}
