package com.vanchu.libs.upgrade;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanchu.libs.common.util.ActivityUtil;
import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;

abstract public class UpgradeCallback {
	private static final String LOG_TAG = UpgradeCallback.class.getSimpleName();
	
	private Context _context;
	
	public UpgradeCallback(Context context){
		_context	= context;
	}
	
	public Context getContext(){
		return _context;
	}
	
	public void onDownloadStarted(){
		SwitchLogger.d(LOG_TAG, "onStart called");
	}
	
	public void onDownloadProgress(long downloaded, long total){
		
	}

	public UpgradeParam onUpgradeInfoResponse(String response){
		SwitchLogger.d(LOG_TAG, "onUpgradeInfoResponse called");
		
		try {
			JSONObject jsonResponse	= new JSONObject(response);
			String lowest	= jsonResponse.getString("lowest");
			String highest	= jsonResponse.getString("highest");
			String url		= jsonResponse.getString("apkUrl");
			String detail	= jsonResponse.getString("detail");
			
			SwitchLogger.d(LOG_TAG, "receive info, lowest version:" + lowest + ", highest version: " + highest);
			SwitchLogger.d(LOG_TAG, "receive info, apkUrl: " + url + ", detail: " + detail);
			
			String current	= ActivityUtil.getCurrentVersionName(getContext());
			SwitchLogger.d(LOG_TAG, "current version: " + current);
			
			return new UpgradeParam(current, lowest, highest, url, detail);
		} catch(JSONException e){
			if(SwitchLogger.isPrintLog()){
				SwitchLogger.e(e);
			}
			return null;
		}
	}
	
	public void onInstallStarted(){
		SwitchLogger.d(LOG_TAG, "onInstallStarted called");
	}
	
	public void onLatestVersion(){
		SwitchLogger.d(LOG_TAG, "onLatestVersion called");
	}
	
	public void onSkipUpgrade(){
		SwitchLogger.d(LOG_TAG, "onSkipUpgrade called");
	}
	
	public void onIoError(){
		SwitchLogger.e(LOG_TAG, "onIoError called");
	}
	
	public void onUrlError(){
		SwitchLogger.e(LOG_TAG, "onUrlError called");
	}
	
	public void onNetworkNotConnected(){
		SwitchLogger.e(LOG_TAG, "onNetworkNotConnected called");
	}
	
	public void onStorageNotEnough(){
		SwitchLogger.e(LOG_TAG, "onStorageNotEnough called");
	}
	
	public void onSocketTimeout(){
		SwitchLogger.e(LOG_TAG, "onSocketTimeout called");
	}
	
	public void exitApp(){
		ActivityUtil.restartSelf();
	}
	
	abstract public void onComplete(int result);
}
