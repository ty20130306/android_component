package com.vanchu.module.music;

public class MusicSceneInfo {

	private int 	_type;
	private String	_name;
	private int		_queueSize;
	private int		_maxQueueSize;
	private boolean	_isPreloading;
	
	public MusicSceneInfo(int type, String name, int queueSize, int maxQueueSize, boolean isPreloading) {
		_type	= type;
		_name	= name;
		_queueSize		= queueSize;
		_maxQueueSize	= maxQueueSize;
		_isPreloading	= isPreloading;
	}
	
	public int getType() {
		return _type;
	}
	
	public String getName() {
		return _name;
	}
	
	public int getQueueSize() {
		return _queueSize;
	}
	 
	public int getMaxQueueSize() {
		return _maxQueueSize;
	}
	
	public boolean isPreloading() {
		return _isPreloading;
	}
}
