package com.vanchu.sample;

import android.media.MediaPlayer;

import com.vanchu.libs.common.util.SwitchLogger;

public class MusicServiceCallback {

	private static final String	LOG_TAG		= MusicServiceCallback.class.getSimpleName();
	
	public void onMusicPrepared(MediaPlayer mp) {
		SwitchLogger.d(LOG_TAG, "onMusicPrepared" );
	}
	
	public void onMusicBuffering(MediaPlayer mp, int percent) {
		//SwitchLogger.d(LOG_TAG, "onMusicBuffering" );
	}
	
	public void onMusicPlaying(MediaPlayer mp) {
		//SwitchLogger.d(LOG_TAG, "onMusicPlaying" );
	}

	public void onMusicCompletion(MediaPlayer mp) {
		SwitchLogger.d(LOG_TAG, "onMusicCompletion" );
	}

}
