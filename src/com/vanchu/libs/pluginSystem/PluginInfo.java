package com.vanchu.libs.pluginSystem;

public class PluginInfo implements Comparable<PluginInfo> {
	
	public static final String	NO_VERSION_NAME		= "";
	public static final int		NO_VERSION_CODE		= 0;
	
	private PluginCfg	_pluginCfg;
	private String		_currentVersionName;
	private int			_currentVersionCode;
	
	private boolean		_installed;
	private boolean		_editing;
	
	public PluginInfo(PluginCfg pluginCfg, String currentVersionName) {
		_pluginCfg			= pluginCfg;
		
		if(currentVersionName == NO_VERSION_NAME) {
			_installed	= false;
			_currentVersionName		= NO_VERSION_NAME;
			_currentVersionCode		= NO_VERSION_CODE;
		} else {
			_installed	= true;
			_currentVersionName		= currentVersionName;
			_currentVersionCode		= versionNameToCode(_currentVersionName);
		}
		
		_editing	= false;
	}
	
	public static int versionNameToCode(String versionName) {
		return Integer.parseInt(versionName.replace(".", "0"));
	}
	
	public boolean isInstalled() {
		return _installed;
	}
	
	public boolean isEditing(){
		return _editing;
	}
	
	public void setEditing(boolean editing) {
		_editing	= editing;
	}
	
	public String getCurrentVersionName() {
		return _currentVersionName;
	}
	
	public int getCurrentVersionCode() {
		return _currentVersionCode;
	}
	
	public PluginCfg getPluginCfg() {
		return _pluginCfg;
	}
	
	private int sortByPriorityAndOrder(PluginInfo other) {
		if(_pluginCfg.getPriority() > other.getPluginCfg().getPriority()) {
			return -1;
		} else if(_pluginCfg.getPriority() < other.getPluginCfg().getPriority()) {
			return 1;
		} else {
			if(_pluginCfg.getOrder() > other.getPluginCfg().getOrder()) {
				return 1;
			} else if (_pluginCfg.getOrder() < other.getPluginCfg().getOrder()) {
				return -1;
			} else {
				return 0;
			}
		}
	}
	
	@Override
	public int compareTo(PluginInfo other) {
		if((_installed && other.isInstalled()) || (! _installed && ! other.isInstalled())) {	
			return sortByPriorityAndOrder(other);
		} else if (_installed && ! other.isInstalled()) {
			return -1;
		} else { // ! _installed && other.isInstalled()
			return 1;
		}
	}
}
