package com.vanchu.libs.pluginSystem;

import com.vanchu.libs.common.util.SwitchLogger;

public class PluginSystemCallback {

	private static final String		LOG_TAG		= PluginSystemCallback.class.getSimpleName();
	
	public void onPluginInfoReady(PluginInfoManager pluginInfoManager) {
		SwitchLogger.d(LOG_TAG, "onPluginInfoReady called");
	}
	
	public void onPluginInfoChange(PluginInfoManager pluginInfoManager) {
		SwitchLogger.d(LOG_TAG, "onPluginInfoChange called");
	}
	
	
	public void onError(int errCode) {
		SwitchLogger.d(LOG_TAG, "onError called, errCode = " + errCode);
	}
}
