package com.vanchu.libs.pluginSystem;

public class PluginCfg {
	
	private String	_id;
	private String	_name;
	private String	_iconUrl;
	private boolean	_show;
	private int		_priority;
	private int		_order;
	private String	_packageName;
	private String	_className;
	
	private PluginVersion	_pluginVersion;
	
	public PluginCfg(String id, String name, String iconUrl, boolean show, 
					int priority, int order, String packageName, String className,
					PluginVersion pluginVersion) 
	{
		_id			= id;
		_name		= name;
		_iconUrl	= iconUrl;
		_show		= show;
		_priority	= priority;
		_order		= order;
		_packageName	= packageName;
		_className		= className;
		
		_pluginVersion	= pluginVersion;
	}
	
	public String getId() {
		return _id;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getIconUrl() {
		return _iconUrl;
	}
	
	public boolean isShow() {
		return _show;
	}
	
	public int getPriority() {
		return _priority;
	}
	
	public int getOrder() {
		return _order;
	}
	
	public String getPackageName() {
		return _packageName;
	}
	
	public String getClassName() {
		return _className;
	}
	
	public PluginVersion getPluginVersion() {
		return _pluginVersion;
	}
}



