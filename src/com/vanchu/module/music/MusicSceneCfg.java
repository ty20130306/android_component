package com.vanchu.module.music;

public class MusicSceneCfg {
	
	private int		_type;
	private String	_name;
	
	public MusicSceneCfg(int type, String name) {
		_type	= type;
		_name	= name;
	}
	
	public String getName() {
		return _name;
	}
	
	public int getType() {
		return _type;
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
		
		MusicSceneCfg another	= (MusicSceneCfg)o;
		if(_name.equals(another.getName()) && _type == another.getType()) {
			return true;
		} else {
			return false;
		}
	}
}
