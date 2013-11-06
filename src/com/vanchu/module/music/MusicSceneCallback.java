package com.vanchu.module.music;

import com.vanchu.libs.common.util.SwitchLogger;

public class MusicSceneCallback {

	private static final String LOG_TAG	= MusicSceneCallback.class.getSimpleName();
	
	public void onPreloadStatusChanged(MusicScene ms, int currentStatus) {
		SwitchLogger.d(LOG_TAG, "onPreloadStatusChange,type="+ms.getSceneType()+",current status="+currentStatus );
	}
	
	public void onQueueSizeChanged(MusicScene ms, int currentQueueSize) {
		SwitchLogger.d(LOG_TAG, "onQueueSizeChanged,type="+ms.getSceneType()+", current queue size="+currentQueueSize );
	}
}
