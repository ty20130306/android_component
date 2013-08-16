package com.vanchu.libs.pluginSystem;

import com.vanchu.libs.common.util.SwitchLogger;

public class PluginManagerCallback {
	
	private static final String 	LOG_TAG	= PluginManagerCallback.class.getSimpleName();
	
	public void onComplete(int result) {
		SwitchLogger.d(LOG_TAG, "onComplete, result = " + result);
	}
	
	public void onDownloadStart() {
		SwitchLogger.d(LOG_TAG, "onDownloadStart");
	}
	
	public void onDownloadProgress(long downloaded, long total) {
		SwitchLogger.d(LOG_TAG, "onDownloadProgress");
	}
	
	public void onDownloadEnd() {
		SwitchLogger.d(LOG_TAG, "onDownloadEnd");
	}
}
