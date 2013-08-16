package com.vanchu.libs.pluginSystem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanchu.libs.common.util.NetUtil;
import com.vanchu.libs.common.util.SwitchLogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

public class PluginSystem {
	
	public static final int		SUCC							= 0;
	public static final int		ERR_NETWORK_NOT_CONNECTED		= 1;
	public static final int		ERR_HTTP_REQUEST_FAILED			= 2;
	public static final int		ERR_HTTP_RESPONSE_ERROR			= 3;
	
	private static final String LOG_TAG	= PluginSystem.class.getSimpleName();
	
	private Context					_context;
	private String					_pluginCfgUrl;
	private Map<String, PluginCfg>	_pluginCfgMap;
	private PluginSystemCallback	_callback;
	private PluginInfoManager		_pluginInfoManager;
	private PluginSystemReceiver	_pluginSystemReceiver;
	
	private Handler	_handler	= new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SUCC:
				init();
				break;
				
			case ERR_NETWORK_NOT_CONNECTED :
			case ERR_HTTP_REQUEST_FAILED :
			case ERR_HTTP_RESPONSE_ERROR :
				_callback.onError(msg.what);
				break;
			
			default:
				break;
			}
		}
	};
	
	public PluginSystem(Context context, String pluginCfgUrl, PluginSystemCallback callback) {
		_context		= context;
		_pluginCfgUrl	= pluginCfgUrl;
		_callback		= callback;
		_pluginCfgMap	= null;
		_pluginInfoManager		= null;
		_pluginSystemReceiver	= null;
	}
	
	public void run(){
		getPluginCfg();
	}
	
	public void stop() {
		if(_pluginSystemReceiver != null) {
			_context.unregisterReceiver(_pluginSystemReceiver);
		}
	}
	
	private void init() {
		_pluginInfoManager		= new PluginInfoManager(_context, _pluginCfgMap);
		registerReceiver();
		_callback.onPluginInfoReady(_pluginInfoManager);
	}
	
	private void registerReceiver() {
		_pluginSystemReceiver	= new PluginSystemReceiver();
		
		IntentFilter intentFilter	= new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		
		//This line is very important. Otherwise, broadcast can't be received.
		intentFilter.addDataScheme("package"); 
		
		_context.registerReceiver(_pluginSystemReceiver, intentFilter);
		
		SwitchLogger.d(LOG_TAG, "register receiver done");
	}

	private class PluginSystemReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action	= intent.getAction();
			
			if(Intent.ACTION_PACKAGE_ADDED.endsWith(action) || Intent.ACTION_PACKAGE_REMOVED.endsWith(action)) {
				_pluginInfoManager.updateInfoList();
            	_callback.onPluginInfoChange(_pluginInfoManager);
			}
		}
	}
	
	private void doGetPluginCfg(){
		if( ! NetUtil.isConnected(_context)){
			_handler.obtainMessage(ERR_NETWORK_NOT_CONNECTED).sendToTarget();
			return ;
		}
		
		String response = NetUtil.httpGetRequest(_pluginCfgUrl, null, 3);
		if(response == null){
			_handler.obtainMessage(ERR_HTTP_REQUEST_FAILED).sendToTarget();
			return ;
		}
		
		SwitchLogger.d(LOG_TAG, "receive http response = " + response);
		
		_pluginCfgMap	= parseCfgResponse(response);
		if(_pluginCfgMap == null){
			_handler.obtainMessage(ERR_HTTP_RESPONSE_ERROR).sendToTarget();
		} else {
			_handler.obtainMessage(SUCC).sendToTarget();
		}
	}
	
	private Map<String, PluginCfg> parseCfgResponse(String response) {
		Map<String, PluginCfg> pluginCfgMap	= new HashMap<String, PluginCfg>();
		
		try {
			JSONObject	responseJson	= new JSONObject(response);
			Iterator<?> iterator		= responseJson.keys();
			while(iterator.hasNext()) {
				String		pluginId		= (String)iterator.next();
				JSONObject	pluginCfgJson	= responseJson.getJSONObject(pluginId);
				PluginCfg	pluginCfg		= parsePluginCfg(pluginId, pluginCfgJson);
				pluginCfgMap.put(pluginId, pluginCfg);
			}
		} catch (JSONException e) {
			return null;
		}
		
		return pluginCfgMap;
	}
	
	private boolean intToBoolean(int value){
		if(value == 0) {
			return false;
		} else {
			return true;
		}
	}

	private PluginCfg parsePluginCfg(String pluginId, JSONObject pluginCfgJson) throws JSONException {
		String name		= pluginCfgJson.getString("name");
		String iconUrl	= pluginCfgJson.getString("icon");
		boolean show	= intToBoolean(pluginCfgJson.getInt("show"));
		int priority	= pluginCfgJson.getInt("priority");
		int order		= pluginCfgJson.getInt("order");
		String packageName	= pluginId;
		String className	= pluginCfgJson.getString("className");
		String apkUrl		= pluginCfgJson.getString("apkUrl");
		return new PluginCfg(pluginId, name, iconUrl, show, priority, order, packageName, className, apkUrl);
	}

	private void getPluginCfg() {
		new Thread(){
			public void run() {
				doGetPluginCfg();
			};
		}.start();
	}
}
