package com.vanchu.libs.push;

import java.util.HashMap;

import com.vanchu.libs.common.util.SharedPrefsUtil;
import com.vanchu.libs.common.util.SwitchLogger;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class PushRobot {
	private static final String LOG_TAG		= PushRobot.class.getSimpleName();
	private static final String PREFS_PUSH_PARAM		= "push_param";
	private static final String PREFS_PUSH_SERVICE_MSG_URL_PARAM	= "push_service_msg_url_param";
	
	public static void run(Context context, Class<?> pushServiceClass, PushParam pushParam){
		setPushParam(context, pushParam);
		
		Intent intent	= new Intent(context, pushServiceClass);
		intent.putExtra(PushService.START_TYPE, PushService.START_TYPE_INIT);
		context.startService(intent);
	}
	
	public static void check(Context context, Class<?> pushServiceClass){
		Intent intent	= new Intent(context, pushServiceClass);
		intent.putExtra(PushService.START_TYPE, PushService.START_TYPE_INIT);
		context.startService(intent);
	}
	
	public synchronized static PushParam getPushParam(Context context){
		SharedPreferences prefs		= context.getSharedPreferences(PREFS_PUSH_PARAM, Context.MODE_PRIVATE);
		int		msgInterval			= prefs.getInt("msgInterval", PushParam.DEFAULT_MSG_INTERVAL);
		String	msgUrl				= prefs.getString("msgUrl", "");
		boolean	notifyWhenRunning	= prefs.getBoolean("notifyWhenRunning", false);
		int 	defaults			= prefs.getInt("defaults", Notification.DEFAULT_LIGHTS);
		boolean	ignoreIntervalLimit	= prefs.getBoolean("ignoreIntervalLimit", false);
		int		delay				= prefs.getInt("delay", PushParam.DEFAULT_DELAY);
		int 	avaiStartTime		= prefs.getInt("avaiStartTime", PushParam.DEFAULT_AVAI_START_TIME);
		int 	avaiEndTime			= prefs.getInt("avaiEndTime", PushParam.DEFAULT_AVAI_END_TIME);
		int		after				= prefs.getInt("after", PushParam.DEFAULT_AFTER);
		
		HashMap<String, String> msgUrlParam	= SharedPrefsUtil.getStringMap(context, PREFS_PUSH_SERVICE_MSG_URL_PARAM);
		
		PushParam pushParam	= new PushParam();
		pushParam.setMsgUrl(msgUrl);
		pushParam.setMsgUrlParam(msgUrlParam);
		pushParam.setIgnoreIntervalLimit(ignoreIntervalLimit);
		pushParam.setMsgInterval(msgInterval);
		
		pushParam.setNotifyWhenRunning(notifyWhenRunning);
		pushParam.setDefaults(defaults);
		pushParam.setDelay(delay);
		pushParam.setAvaiTime(avaiStartTime, avaiEndTime);
		pushParam.setAfter(after);
		
		SwitchLogger.d(LOG_TAG, "prefs get push param,url="+msgUrl+",delay="+delay
								+",after="+after+",msgInterval="+msgInterval
								+",avaiStartTime="+avaiStartTime
								+",avaiEndTime="+avaiEndTime);
		
		return pushParam;
	}
	
	public synchronized static void setPushParam(Context context, PushParam pushParam){
		SharedPreferences prefs			= context.getSharedPreferences(PREFS_PUSH_PARAM, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor	= prefs.edit();
		editor.putInt("msgInterval", pushParam.getMsgInterval());
		editor.putString("msgUrl", pushParam.getMsgUrl());
		editor.putBoolean("notifyWhenRunning", pushParam.getNotifyWhenRunning());
		editor.putInt("defaults", pushParam.getDefaults());
		editor.putBoolean("ignoreIntervalLimit", pushParam.isIgnoreIntervalLimit());
		editor.putInt("delay", pushParam.getDelay());
		editor.putInt("avaiStartTime", pushParam.getAvaiStartTime());
		editor.putInt("avaiEndTime", pushParam.getAvaiEndTime());
		editor.putInt("after", pushParam.getAfter());
		boolean succ	= editor.commit();
		if(succ) {
			SwitchLogger.d(LOG_TAG, "prefs commit push param succ,url="+pushParam.getMsgUrl()
									+",delay="+pushParam.getDelay()
									+",after="+pushParam.getAfter()+",msgInterval="+pushParam.getMsgInterval()
									+",avaiStartTime="+pushParam.getAvaiStartTime()
									+",avaiEndTime="+pushParam.getAvaiEndTime());
		} else {
			SwitchLogger.e(LOG_TAG, "commit push param fail");
		}
		
		SharedPrefsUtil.putStringMap(context, PREFS_PUSH_SERVICE_MSG_URL_PARAM, pushParam.getMsgUrlParam());
	}
}
