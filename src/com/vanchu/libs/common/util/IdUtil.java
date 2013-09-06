package com.vanchu.libs.common.util;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

public class IdUtil {
	
	private static final String PREF_NAME_UNIQUE_ID	= "unique_id";
	private static final String PREF_KEY_ID			= "id";
	
	public static String getDeviceId(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}
	
	public static String getUUID() {
		return UUID.randomUUID().toString();
	}
	
	public static String getDeviceUniqueId(Context context) {
		SharedPreferences pref	= context.getSharedPreferences(PREF_NAME_UNIQUE_ID, Context.MODE_PRIVATE);
		String uniqueId	= pref.getString(PREF_KEY_ID, "");
		if(uniqueId == "") {
			uniqueId	= getDeviceId(context);
			if(uniqueId == null) {
				uniqueId	= getUUID();
			}
			
			pref.edit().putString(PREF_KEY_ID, uniqueId).commit();
		}
		
		return uniqueId;
	}
}
