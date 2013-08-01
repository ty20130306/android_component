package com.vanchu.libs.common.util;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsUtil {
	
	private static String getPrefsName(String key){
		return key + "_string_map";
	}
	
	public static void putStringMap(Context context, String key, Map<String, String> map){
		if(map == null){
			return;
		}
		
		SharedPreferences prefs			= context.getSharedPreferences(getPrefsName(key), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor	= prefs.edit();
		
		for(String k : map.keySet()){
			editor.putString(k, map.get(k));
		}
		
		editor.commit();
	}
	
	public static HashMap<String, String> getStringMap(Context context, String key){
		SharedPreferences prefs		= context.getSharedPreferences(getPrefsName(key), Context.MODE_PRIVATE);
		
		HashMap<String, String> resultMap	= new HashMap<String, String>();
		Map<String, ?> prefsMap	= prefs.getAll();
		for(Map.Entry<String, ?> entry : prefsMap.entrySet()){
			resultMap.put(entry.getKey(), entry.getValue().toString());
		}
		
		return resultMap;
	}
}
