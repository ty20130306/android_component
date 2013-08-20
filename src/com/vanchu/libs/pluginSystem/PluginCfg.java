package com.vanchu.libs.pluginSystem;

public class PluginCfg {
	
	private String	_id;
	private String	_name;
	private String	_iconUrl;
	private boolean	_show;
	private int		_priority;
	private int		_order;
	private boolean	_sticky;
	private String	_packageName;
	private String	_className;
	private String	_apkUrl;
	
	public PluginCfg(String id, String name, String iconUrl, boolean show, int priority, 
					int order, boolean sticky, String packageName, String className, String apkUrl) 
	{
		_id			= id;
		_name		= name;
		_iconUrl	= iconUrl;
		_show		= show;
		_priority	= priority;
		_order		= order;
		_sticky		= sticky;
		_packageName	= packageName;
		_className		= className;
		_apkUrl			= apkUrl;
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
	
	public boolean isSticky() {
		return _sticky;
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
	
	public String getApkUrl() {
		return _apkUrl;
	}
}



