package com.vanchu.libs.pluginSystem;

import com.vanchu.libs.common.util.SwitchLogger;

public class PluginManagerCallback {
	
	private static final String 	LOG_TAG	= PluginManager.class.getSimpleName();
	
	public void onComplete(int result) {
		SwitchLogger.d(LOG_TAG, "result = " + result);
	}
	
}
