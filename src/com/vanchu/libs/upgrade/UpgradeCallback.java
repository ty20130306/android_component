package com.vanchu.libs.upgrade;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanchu.libs.common.SwitchLogger;

import android.content.Context;
import android.widget.Toast;

public class UpgradeCallback {
	private static final String LOG_TAG = UpgradeCallback.class.getSimpleName();
	
	private Context _context;
	
	public UpgradeCallback(Context context){
		_context	= context;
	}
	
	public Context getContext(){
		return _context;
	}
	
	public void onComplete(int result){
		SwitchLogger.d(LOG_TAG, "onComplete called, result = " + result);
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
			
			String current	= UpgradeUtil.getCurrentVersionName(getContext());
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
		Toast.makeText(_context, "更新失败，请检查您的SD卡", Toast.LENGTH_LONG).show();
	}
	
	public void onUrlError(){
		Toast.makeText(_context, "更新失败，服务器升级中", Toast.LENGTH_LONG).show();
	}
	
	public void onNetworkNotConnected(){
		SwitchLogger.d(LOG_TAG, "onNetworkNotConnected called");
		Toast.makeText(_context, "请打开您的网络连接", Toast.LENGTH_LONG).show();
	}
	
	public void onStorageNotEnough(long needBytes){
		SwitchLogger.e(LOG_TAG, "space not enough to download, need " + needBytes + " bytes");
		String tip	= String.format("更新失败，存储空间不足, 需要 %d M空间", (int)(needBytes / (1024 * 1024) + 1));
		Toast.makeText(_context, tip, Toast.LENGTH_LONG).show();
	}
	
	public void onSocketTimeout(){
		SwitchLogger.e(LOG_TAG, "socket time out");
		Toast.makeText(_context, "更新失败，网络超时", Toast.LENGTH_LONG).show();
	}
}
