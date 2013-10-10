package com.vanchu.module.music;

import java.io.Serializable;

public class MusicSolidQueueElement implements Serializable  {
	private static final long serialVersionUID = 0L;
	
	private String _id;
	private String _name;
	private String _audio;
	private String _img;
	private String _artist;
	private String _album;
	private String _lyric;
	private String _audioPath;
	private String _lyricPath	= "";
	
	public MusicSolidQueueElement(String id, String name, String audio, String img, 
						String artist, String album, String lyric, String audioPath) {
		_id		= id;
		_name	= name;
		_audio	= audio;
		_img	= img;
		_artist	= artist;
		_album	= album;
		_lyric	= lyric;
		_audioPath	= audioPath;
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
	
	public String getAudioPath() {
		return _audioPath;
	}
	
	public String getLyricPath() {
		return _lyricPath;
	}

	public void setLyricPath(String lyricPath) {
		_lyricPath	= lyricPath;
	}
	/* 
	 * 用于SolidQueue比较元素是否相等
	 * 当SolidQueue出队的时候，需要比较出队元素的值是否与还在队列中的值相等，
	 * 如果有相等，回调函数的onRemove不会被调用
	 */
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
		
		MusicSolidQueueElement another	= (MusicSolidQueueElement)o;
		if(_id.equals(another.getId())) {
			return true;
		} else {
			return false;
		}
	}
}
