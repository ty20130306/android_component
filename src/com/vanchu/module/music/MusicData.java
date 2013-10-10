package com.vanchu.module.music;

public class MusicData {

	private String	_name;
	private int		_playerMode;
	private String	_audioUrl;
	private String	_audioPath;
	private String	_lyricUrl;
	private String	_lyricPath;
	private String	_imgUrl;
	
	public MusicData(String name, int playerMode, String audioUrl, String audioPath, 
					String lyricUrl, String lyricPath, String imgUrl) 
	{
		_name		= name;
		_playerMode	= playerMode;
		_audioUrl	= audioUrl;
		_audioPath	= audioPath;
		_lyricUrl	= lyricUrl;
		_lyricPath	= lyricPath;
		_imgUrl		= imgUrl;
	}
	
	public String getName() {
		return _name;
	}
	
	public int getPlayerMode() {
		return _playerMode;
	}
	
	public String getAudioUrl() {
		return _audioUrl;
	}
	
	public String getAudioPath() {
		return _audioPath;
	}
	
	public String getLyricUrl() {
		return _lyricUrl;
	}
	
	public String getLyricPath() {
		return _lyricPath;
	}
	
	public String getImgUrl() {
		return _imgUrl;
	}
}
