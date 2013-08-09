package com.vanchu.libs.pluginSystem;

public class PluginVersion {
	
	public static final	String	NO_VERSION_NAME		= "";
	public static final int		NO_VERSION_CODE		= 0;	
	
	public static final int UPGRADE_TYPE_NONE		= -1;
	public static final int UPGRADE_TYPE_FORCE		= 0;
	public static final int UPGRADE_TYPE_OPTIONAL	= 1;
	public static final int UPGRADE_TYPE_LATEST		= 2;
	
	private	String	_lowestName;
	private String	_highestName;
	private String	_apkUrl;
	private String	_upgradeDetail;
	
	private int		_lowestCode;
	private int		_highestCode;
	
	public PluginVersion(String lowestName, String highestName, String apkUrl, String upgradeDetail){
		_lowestName		= lowestName;
		_highestName	= highestName;
		_apkUrl			= apkUrl;
		_upgradeDetail	= upgradeDetail;
		
		_lowestCode		= versionNameToCode(_lowestName);
		_highestCode	= versionNameToCode(_highestName);
	}
	
	public static int versionNameToCode(String versionName) {
		return Integer.parseInt(versionName.replace(".", "0"));
	}
	
	public String getLowestName() {
		return _lowestName;
	}
	
	public String getHighestName() {
		return _highestName;
	}
	
	public String getApkUrl() {
		return _apkUrl;
	}
	
	public String getUpgradeDetail() {
		return _upgradeDetail;
	}
	
	public int getLowestCode() {
		return _lowestCode;
	}
	
	public int getHighestCode() {
		return _highestCode;
	}
	
	public int getUpgradeType(String currentName){
		int currentCode		= versionNameToCode(currentName);
		return getUpgradeType(currentCode);
	}
	
	public int getUpgradeType(int currentCode){
		if(currentCode >= _highestCode){
			return UPGRADE_TYPE_LATEST;
		} else if(currentCode < _lowestCode){
			return UPGRADE_TYPE_FORCE;
		} else {
			return UPGRADE_TYPE_OPTIONAL;
		}
	}
}
