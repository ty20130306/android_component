package com.vanchu.libs.upgrade;

import com.vanchu.libs.common.SwitchLogger;


public class UpgradeParam {
	private static final String LOG_TAG = UpgradeManager.class.getSimpleName();
	
	public static final int UPGRADE_TYPE_FORCE		= 0;
	public static final int UPGRADE_TYPE_OPTIONAL	= 1;
	public static final int UPGRADE_TYPE_LATEST		= 2;
	
	private String _highestVersionName;
	private String _lowestVersionName;
	private String _currentVersionName;
	private String _upgradeApkUrl;
	private String _upgradeDetail;
	
	private int _highestVersionCode;
	private int _lowestVersionCode;
	private int _currentVersionCode;
	
	private String		_apkFileName;
	
	public UpgradeParam(String currentVersionName, 
						String lowestVersionName, 
						String highestVersionName, 
						String upgradeApkUrl,
						String upgradeDetail )
	{
		init(currentVersionName, 
			lowestVersionName, 
			highestVersionName, 
			upgradeApkUrl,
			upgradeDetail);
	}

	
	private void init(String currentVersionName, 
					String lowestVersionName,
					String highestVersionName, 
					String upgradeApkUrl,
					String upgradeDetail)
	{
		_currentVersionName		= currentVersionName;
		_highestVersionName		= highestVersionName;
		_lowestVersionName		= lowestVersionName;
		
		_upgradeApkUrl			= upgradeApkUrl;
		_upgradeDetail			= upgradeDetail;
		
		_currentVersionCode		= Integer.parseInt(_currentVersionName.replace(".", "0"));
		_lowestVersionCode		= Integer.parseInt(_lowestVersionName.replace(".", "0"));
		_highestVersionCode		= Integer.parseInt(_highestVersionName.replace(".", "0"));
		
		splitApkFileNameFromUrl();
		checkInputVersion();
	}
	
	private void checkInputVersion() {
		if(_lowestVersionCode > _highestVersionCode){
			SwitchLogger.e(LOG_TAG, "lowest version is higher than highest version, " +
							"lowest = " + _lowestVersionName + ", highest = " + _highestVersionName );
			
			
			SwitchLogger.w(LOG_TAG, "wrong input, swap lowest with highest");
			swapVersion();
		}
	}

	private void swapVersion() {
		String tmpName		= _lowestVersionName;
		_lowestVersionName	= _highestVersionName;
		_highestVersionName	= tmpName;
		
		int tmpCode			= _lowestVersionCode;
		_lowestVersionCode	= _highestVersionCode;
		_highestVersionCode	= tmpCode;
	}

	private void splitApkFileNameFromUrl(){
		String[] urlElements	= _upgradeApkUrl.split("/");
		
		_apkFileName	= urlElements[urlElements.length - 1];
		
		if(_apkFileName.length() == 0){
			_apkFileName	= "vanchu.apk";
		}
		
		if( ! _apkFileName.contains(".apk")){
			_apkFileName	+= ".apk";
		}
		
		SwitchLogger.d(LOG_TAG, "apk file name: "+_apkFileName);
	}
	
	public String getApkFileName(){
		return _apkFileName;
	}
	
	public String getHighestVersionName(){
		return _highestVersionName;
	}
	
	public String getLowestVersionName(){
		return _lowestVersionName;
	}
	
	public String getCurrentVersionName(){
		return _currentVersionName;
	}
	
	public String getUpgradeApkUrl(){
		return _upgradeApkUrl;
	}
	
	public String getUpgradeDetail(){
		return _upgradeDetail;
	}
	
	public int getUpgradeType(){
		if(_currentVersionCode >= _highestVersionCode){
			return UPGRADE_TYPE_LATEST;
		} else if(_currentVersionCode < _lowestVersionCode){
			return UPGRADE_TYPE_FORCE;
		} else {
			return UPGRADE_TYPE_OPTIONAL;
		}
	}
}

