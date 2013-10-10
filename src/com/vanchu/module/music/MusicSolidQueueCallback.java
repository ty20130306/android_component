package com.vanchu.module.music;

import java.io.File;

import com.vanchu.libs.common.container.SolidQueue.SolidQueueCallback;
import com.vanchu.libs.common.util.SwitchLogger;

public class MusicSolidQueueCallback implements SolidQueueCallback<MusicSolidQueueElement> {
	
	private static final String	LOG_TAG	= MusicSolidQueueCallback.class.getSimpleName();
	
	@Override
	public void onAdd(MusicSolidQueueElement element) {
		SwitchLogger.d(LOG_TAG, "MusicSolidQueueElement.onAdd, audio=" + element.getAudio() 
								+ ", audio path=" + element.getAudioPath()
								+ ", lyric=" + element.getLyric()
								+ ", lyric path=" + element.getLyricPath());
	}
	
	@Override
	public void onRemove(MusicSolidQueueElement element) {
		SwitchLogger.d(LOG_TAG, "MusicSolidQueueElement.onAdd, audio=" + element.getAudio() 
				+ ", audio path=" + element.getAudioPath()
				+ ", lyric=" + element.getLyric()
				+ ", lyric path=" + element.getLyricPath());
		
		String audioPath	= element.getAudioPath();
		if( ! audioPath.equals("") ) {
			File audioFile	= new File(audioPath);
			if(audioFile.exists()) {
				audioFile.delete();
			}
		}
		
		String lyricPath	= element.getLyricPath();
		if( ! lyricPath.equals("") ) {
			File lyricFile	= new File(lyricPath);
			if(lyricFile.exists()) {
				lyricFile.delete();
			}
		}
	}
}
