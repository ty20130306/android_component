package com.vanchu.libs.pluginSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;
import android.content.pm.PackageInfo;

public class PluginInfoManager {

	private static final String		LOG_TAG		= PluginInfoManager.class.getSimpleName();
	
	private static final int		NO_FLAG		= 0;
	
	private Context					_context;
	private Map<String, PluginCfg>	_pluginCfgMap;
	private Map<String, String>		_currentVersionMap;
	
	private List<PluginInfo>		_pluginInfoList;
	
	public PluginInfoManager(Context context, Map<String, PluginCfg> pluginCfgMap) {
		_context		= context;
		_pluginCfgMap	= pluginCfgMap;
		_currentVersionMap	= getInstalledPluginVersion();
		initPluginInfoList();
	}
	
	public void updateInfoList() {
		_currentVersionMap	= getInstalledPluginVersion();
		initPluginInfoList();
	}
	
	private Map<String, String> getInstalledPluginVersion() {
		List<PackageInfo> packageList	= _context.getPackageManager().getInstalledPackages(NO_FLAG);
		HashMap<String, String>	versionMap	= new HashMap<String, String>();
		for(int i = 0; i < packageList.size(); ++i) {
			PackageInfo pi	= packageList.get(i);
			if(pi.versionName == null || pi.versionName == "") {
				continue;
			}
			versionMap.put(pi.packageName, pi.versionName);
		}
		
		return versionMap;
	}
	
	private void initPluginInfoList() {
		_pluginInfoList	= new ArrayList<PluginInfo>();
		Iterator<Entry<String, PluginCfg>> iter	= _pluginCfgMap.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, PluginCfg> entry	= iter.next();
			String pluginId			= entry.getKey();
			PluginCfg pluginCfg		= entry.getValue();
			String currentVersionName	= PluginVersion.NO_VERSION_NAME;
			if(_currentVersionMap.containsKey(pluginId)) {
				currentVersionName	= _currentVersionMap.get(pluginId);
			}
			
			_pluginInfoList.add(new PluginInfo(pluginCfg, currentVersionName));
		}
		SwitchLogger.d(LOG_TAG, "-------------------before sort--------------------");
		printInfoList();
		Collections.sort(_pluginInfoList);
		SwitchLogger.d(LOG_TAG, "-------------------after sort--------------------");
		printInfoList();
	}

	private void printInfoList() {
		for(int i = 0; i < _pluginInfoList.size(); ++i) {
			PluginCfg pc	= _pluginInfoList.get(i).getPluginCfg();
			SwitchLogger.d(LOG_TAG, "name="+pc.getName()+"priority="+pc.getPriority()+",order="+pc.getOrder());
		}
	}
	
	public Map<String, PluginCfg> getPluginCfgMap() {
		return _pluginCfgMap;
	}
	
	public List<PluginInfo> getPluginInfoList() {
		return _pluginInfoList;
	}
	
	public List<PluginInfo> getPriorPluginInfoList() {
		ArrayList<PluginInfo> priorList	= new ArrayList<PluginInfo>();
		
		for(int i = 0; i < _pluginInfoList.size(); ++i) {
			PluginInfo pi	= _pluginInfoList.get(i);
			if(pi.getPluginCfg().getPriority() > 0) {
				priorList.add(pi);
			}
		}
		
		return priorList;
	}
	
	public List<PluginInfo> getOrderPluginInfoList() {
		ArrayList<PluginInfo> orderList	= new ArrayList<PluginInfo>();
		
		for(int i = 0; i < _pluginInfoList.size(); ++i) {
			PluginInfo pi	= _pluginInfoList.get(i);
			if(pi.getPluginCfg().getPriority() <= 0) {
				orderList.add(pi);
			}
		}
		
		return orderList;
	}
	
//	public List<String> getListByPriorityAndOrder() {
//		
//	}
//	
//	public List<String> getListByPriority() {
//		ArrayList<String> list	= new ArrayList<String>();
//		
//	}
//	
//	public List<String> getListByOrder() {
//		
//	}
}
