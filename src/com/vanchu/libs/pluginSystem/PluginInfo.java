package com.vanchu.libs.pluginSystem;

public class PluginInfo implements Comparable<PluginInfo> {
	
	private PluginCfg	_pluginCfg;
	private String		_currentVersionName;
	private int			_currentVersionCode;
	
	private boolean		_installed;
	private int			_upgradeType;
	
	public PluginInfo(PluginCfg pluginCfg, String currentVersionName) {
		_pluginCfg			= pluginCfg;
		
		if(currentVersionName == PluginVersion.NO_VERSION_NAME) {
			_installed	= false;
			_currentVersionName		= PluginVersion.NO_VERSION_NAME;
			_currentVersionCode		= PluginVersion.NO_VERSION_CODE;
			_upgradeType			= PluginVersion.UPGRADE_TYPE_NONE;
		} else {
			_installed	= true;
			_currentVersionName		= currentVersionName;
			_currentVersionCode		= PluginVersion.versionNameToCode(_currentVersionName);
			_upgradeType			= pluginCfg.getPluginVersion().getUpgradeType(_currentVersionCode);
		}
	}
	
	public boolean isInstalled() {
		return _installed;
	}
	
	public int getUpgradeType() {
		return _upgradeType;
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
	
	@Override
	public int compareTo(PluginInfo other) {
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
}
