package com.vanchu.test;

import java.io.Serializable;

class MyItem implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String _url;
	private String _id;
	private String _name;
	
	public MyItem(String url, String id, String name) {
		_url	= url;
		_id		= id;
		_name	= name;
	}

	public String get_url() {
		return _url;
	}

	public void set_url(String _url) {
		this._url = _url;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		
		if(o == null) {
			return false;
		}
		
		if(this.getClass() != o.getClass()){
			return false;
		}
		
		MyItem another	= (MyItem)o;
		if(null != _id && _id.equals(another.get_id())) {
			return true;
		} else {
			return false;
		}
	}
}