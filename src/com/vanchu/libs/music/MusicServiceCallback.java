package com.vanchu.libs.music;

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
	
	public void onPlayerModeChange(int currentPlayerMode) {
		SwitchLogger.d(LOG_TAG, "onPlayerModeChange, current player mode = " + currentPlayerMode );
	}
	
	public void onPlayerDetailModeChange(int currentPlayerDetailMode) {
		SwitchLogger.d(LOG_TAG, "onPlayerDetailModeChange, current player detail mode = " + currentPlayerDetailMode );
	}
	
	public void onError(int errCode) {
		SwitchLogger.e(LOG_TAG, "onError, errCode = " + errCode );
	}
}
