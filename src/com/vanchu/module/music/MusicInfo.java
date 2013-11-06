package com.vanchu.module.music;


public class MusicInfo {

	private String _id;
	private String _name;
	private String _audio;
	private String _img;
	private String _artist;
	private String _album;
	private String _lyric;
	
	public MusicInfo(String id, String name, String audio, String img, 
						String artist, String album, String lyric) {
		_id		= id;
		_name	= name;
		_audio	= audio;
		_img	= img;
		_artist	= artist;
		_album	= album;
		_lyric	= lyric;
	}
	
	public String getId() {
		return _id;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getAudio() {
		return _audio;
	}
	
	public String getImg() {
		return _img;
	}
	
	public String getArtist() {
		return _artist;
	}
	
	public String getAlbum() {
		return _album;
	}
	
	public String getLyric() {
		return _lyric;
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
		
		MusicInfo another	= (MusicInfo)o;
		if(_id.equals(another.getId())) {
			return true;
		} else {
			return false;
		}
	}
}
