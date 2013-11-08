package com.vanchu.libs.eventCenter;

public class Event {
	
	private int		_type;
	private Object	_data;
	
	public Event(int type, Object data) {
		_type	= type;
		_data	= data;
	}
	
	public int getType() {
		return _type;
	}
	
	public Object getData() {
		return _data;
	}
}
