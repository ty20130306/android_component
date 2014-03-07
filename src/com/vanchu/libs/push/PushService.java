package com.vanchu.libs.push;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanchu.libs.common.util.ActivityUtil;
import com.vanchu.libs.common.util.NetUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;

abstract public class PushService extends Service {
	private static final String LOG_TAG				= PushService.class.getSimpleName();
	
	private static final String PREFS_PUSH_SERVICE		= "push_service";
	
	private static final int NO_EXIT_TIME			= -1;
	private static final int NO_POP_UP_TIME			= -1;
	
	public static String START_TYPE		= "START_TYPE";
	public static String MSG_TYPE		= "MSG_TYPE";
	
	public static final int START_TYPE_INIT				= 1;
	public static final int START_TYPE_NOTIFICATION		= 2;
	public static final int START_TYPE_REQUEST_PUSH_MSG	= 3;
	
	private PushParam	_pushParam		= null;

	private AlarmManager	_alarmManager	= null;
	private PendingIntent	_alarmIntent	= null;
	
	private SharedPreferences	_prefs	= null;
	private long 		_lastExitTime	= NO_EXIT_TIME;
	private long		_lastPopUpTime	= NO_POP_UP_TIME;
	
	/**
	 * 根据消息类型返回icon id
	 * @param msgType	消息类型
	 * @return
	 */
	abstract protected int getNotificationIcon(int msgType);
	
	/**
	 * 根据消息类型实现推送消息的点击动作
	 * @param msgType	消息类型
	 * @param msgExtra	消息的extra数据
	 */
	abstract protected void onNotificationClick(int msgType, Bundle msgExtra);
	
	protected void onPopUp() {
		SwitchLogger.d(LOG_TAG, "notification pop up" );
	}
	
	protected void putMsgUrlParam(String key, String value) {
		Map<String, String> msgUrlParam	= _pushParam.getMsgUrlParam();
		msgUrlParam.put(key, value);
		synchronized (PushService.this) {
			_pushParam.setMsgUrlParam(msgUrlParam);
		}
		setPushParam();
	}
	
	private void showNotification(PushMsg pushMsg) {
		SwitchLogger.d(LOG_TAG, "ticker="+pushMsg.getTicker()+",type="+pushMsg.getType()+",title="+pushMsg.getTitle()+",text="+pushMsg.getText());
		
		Intent	intent		= new Intent(Intent.ACTION_RUN);
		intent.setClass(this, this.getClass());
		intent.putExtra(START_TYPE, START_TYPE_NOTIFICATION);
		intent.putExtra(MSG_TYPE, pushMsg.getType());
		intent.putExtras(pushMsg.getExtra());
		PendingIntent	pIntent	= PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		Notification notification = new Notification();
		notification.icon = getNotificationIcon(pushMsg.getType());
		notification.tickerText = pushMsg.getTicker();
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(this, pushMsg.getTitle(), pushMsg.getText(), pIntent);
		notification.defaults = _pushParam.getDefaults();
		
		SwitchLogger.d(LOG_TAG, "notification.defaults = " + notification.defaults);
		NotificationManager	notificationManager	= (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(pushMsg.getType(), notification);
	}
	
	protected JSONObject parseMsgResponse(String response) throws JSONException {
		JSONObject msg	= new JSONObject(response);
		
		return msg;
	}
	
	private void onPushMsgResponse(String response){
		//SwitchLogger.d(LOG_TAG, response);
		
		try {
			JSONObject msg	= parseMsgResponse(response);
			PushMsg pushMsg	= new PushMsg(msg);
			if(needPopUp(pushMsg)){
				showNotification(pushMsg);
				onPopUp();
			}
			
			updatePushCfgIfNeed(pushMsg);
		} catch(JSONException e){
			SwitchLogger.e(e);
		}
	}
	
	private boolean needPopUp(PushMsg pushMsg) {
		// update should come first
		updateExitTimeIfNeed();
		
		// if force, force to pop up no matter what other conditons
		long currTime	= System.currentTimeMillis();
		if(pushMsg.isForce()) {
			SwitchLogger.d(LOG_TAG, "force set, force to pop up " );
			_lastPopUpTime	= currTime;
			_prefs.edit().putLong("last_pop_up_time", _lastPopUpTime).commit();
			return true;
		}
		
		// check whehter should pop up
		if(currTime - _lastExitTime < _pushParam.getDelay()) {
			SwitchLogger.d(LOG_TAG, "do not reach delay, delay = " + _pushParam.getDelay() + ", passed = " + (currTime - _lastExitTime));
			return false;
		}
		
		int currHourAndMinute	= getCurrHourAndMinute();
		if(currHourAndMinute < _pushParam.getAvaiStartTime() || currHourAndMinute > _pushParam.getAvaiEndTime()) {
			SwitchLogger.d(LOG_TAG, "not in available duration, start = " + _pushParam.getAvaiStartTime() 
									+ ", end = " + _pushParam.getAvaiEndTime() + ", now = " + currHourAndMinute);
			
			return false;
		}
		
		boolean notifyWhenRunning	= _pushParam.getNotifyWhenRunning();
		if( ! notifyWhenRunning && ActivityUtil.isAppRunningTop(this)){
			SwitchLogger.d(LOG_TAG, "do not notify when running");
			return false;
		}
		
		if( ! pushMsg.isShow()){
			SwitchLogger.d(LOG_TAG, "push msg show field is false");
			return false;
		}
		
		if(currTime - _lastPopUpTime < _pushParam.getAfter()) {
			SwitchLogger.d(LOG_TAG, "do not reach after, after = " + _pushParam.getAfter() + ", passed = " + (currTime - _lastPopUpTime));
			return false;
		}
		
		SwitchLogger.d(LOG_TAG, "condition satisfied, pop up,  _lastPopUpTime="
								+_lastPopUpTime+",_lastExitTime="+_lastExitTime
								+",current time="+currTime);
		
		_lastPopUpTime	= currTime;
		_prefs.edit().putLong("last_pop_up_time", _lastPopUpTime).commit();
		
		return true;
	}

	private void updatePushCfgIfNeed(PushMsg pushMsg) {
		synchronized (PushService.this) {

			boolean		needRegisterAlarm	= false;
			boolean		needSetPushParam	= false;

			HashMap<String, String> cfg	= pushMsg.getCfg();
			if(cfg.containsKey("interval")){
				int interval	= Integer.parseInt(cfg.get("interval"));
				SwitchLogger.d(LOG_TAG, "received interval="+interval);
				if(interval != _pushParam.getMsgInterval()){
					SwitchLogger.d(LOG_TAG, "old interval="+_pushParam.getMsgInterval());
					_pushParam.setMsgInterval(interval);
					needSetPushParam	= true;
					needRegisterAlarm	= true;
				}
			}

			if(cfg.containsKey("delay")) {
				int delay	= Integer.parseInt(cfg.get("delay"));
				SwitchLogger.d(LOG_TAG, "received delay="+delay);
				if(delay != _pushParam.getDelay()){
					SwitchLogger.d(LOG_TAG, "old delay="+_pushParam.getDelay());
					_pushParam.setDelay(delay);
					needSetPushParam	= true;
				}
			}

			if(cfg.containsKey("avaiStartTime") && cfg.containsKey("avaiEndTime")) {
				int avaiStartTime	= Integer.parseInt(cfg.get("avaiStartTime"));
				int avaiEndTime		= Integer.parseInt(cfg.get("avaiEndTime"));
				SwitchLogger.d(LOG_TAG, "received avaiStartTime=" + avaiStartTime + ",avaiEndTime=" + avaiEndTime);

				if(avaiStartTime != _pushParam.getAvaiStartTime() || avaiEndTime != _pushParam.getAvaiEndTime()) {
					SwitchLogger.d(LOG_TAG, "old avai time,start="+_pushParam.getAvaiStartTime()
							+",end="+_pushParam.getAvaiEndTime());
					_pushParam.setAvaiTime(avaiStartTime, avaiEndTime);
					needSetPushParam	= true;
				}
			}

			if(cfg.containsKey("after")) {
				int after	= Integer.parseInt(cfg.get("after"));
				SwitchLogger.d(LOG_TAG, "received after="+after);
				if(after != _pushParam.getAfter()){
					SwitchLogger.d(LOG_TAG, "old after ,after="+_pushParam.getAfter());
					_pushParam.setAfter(after);
					needSetPushParam	= true;
				}
			}

			if(needSetPushParam) {
				SwitchLogger.d(LOG_TAG, "***************cfg change, need update push param");
				setPushParam();
			}

			if(needRegisterAlarm) {
				SwitchLogger.d(LOG_TAG, "***************cfg change, need register alarm");
				cancelAlarm();
				setAlarm();
			}

			SwitchLogger.d(LOG_TAG, "_lastPopUpTime="+_lastPopUpTime+",_lastExitTime="+_lastExitTime);
		}
	}
	
	@Override
	public IBinder onBind(Intent intent){
		
		return null;
	}

	@Override
	public void onCreate(){
		SwitchLogger.d(LOG_TAG, "onCreate()");
		_prefs	= getSharedPreferences(PREFS_PUSH_SERVICE, Context.MODE_PRIVATE);
		
		getPushParam();
		getPushStatus();
		
		super.onCreate();
	}
	
	@Override
	public void onStart(Intent intent, int startId){
		SwitchLogger.d(LOG_TAG, "onStart()");
		
		super.onStart(intent, startId);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		SwitchLogger.d(LOG_TAG, "onStartCommand()");
		getPushParam();
		
		if(intent != null){
			int startType	= intent.getIntExtra(START_TYPE, START_TYPE_INIT);
			SwitchLogger.d(LOG_TAG, "start type = " + startType);
			
			switch (startType) {
			case START_TYPE_INIT :
				setAlarm();
				break;
	
			case START_TYPE_NOTIFICATION :
				onNotificationClick(intent.getIntExtra(MSG_TYPE, PushMsg.MSG_TYPE_NONE), intent.getExtras());
				break;
			
			case START_TYPE_REQUEST_PUSH_MSG :
				getPushMsg();
				break;
				
			default:
				break;
			}
		} else {
			setAlarm();
		}
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy(){
		SwitchLogger.d(LOG_TAG, "onDestroy()");
		cancelAlarm();
		setPushParam();
		super.onDestroy();
	}
	
	private void getPushStatus() {
		long currTime	= System.currentTimeMillis();
		_lastPopUpTime	= _prefs.getLong("last_pop_up_time", currTime);
		_lastExitTime	= _prefs.getLong("last_exit_time", currTime);
	}
	
	private void getPushParam(){
		synchronized (PushService.this) {
			_pushParam	= PushRobot.getPushParam(this);
		}
	}
	
	private void setPushParam(){
		synchronized (PushService.this) {
			PushRobot.setPushParam(this, _pushParam);
		}
	}
	
	private void setAlarm() {
		if (null == _alarmManager) {
			SwitchLogger.d(LOG_TAG, "_alarmManager is null, register alarm");
			_alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			Intent intent = new Intent(this, this.getClass());
			intent.putExtra(START_TYPE, START_TYPE_REQUEST_PUSH_MSG);
			_alarmIntent	= PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			int interval	= _pushParam.getMsgInterval();
			SwitchLogger.d(LOG_TAG, "get msg interval is " + interval);
			_alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 
										System.currentTimeMillis() + 10 * 1000, 
										interval, _alarmIntent);
		} else {
			SwitchLogger.d(LOG_TAG, "alarm has been registered");
		}
	}
	
	private void cancelAlarm() {
		if(null != _alarmManager && null != _alarmIntent) {
			_alarmManager.cancel(_alarmIntent);
			_alarmManager	= null;
			_alarmIntent	= null;
		}
	}
	
	private void updateExitTimeIfNeed() {
		if(ActivityUtil.isAppRunningTop(this)) {
			_lastExitTime	= NO_EXIT_TIME;
		} else {
			if(_lastExitTime == NO_EXIT_TIME) {
				_lastExitTime	= System.currentTimeMillis();
			}
		}
		
		_prefs.edit().putLong("last_exit_time", _lastExitTime).commit();
	}
	
	private int getCurrHourAndMinute() {
		Calendar calendar	= Calendar.getInstance();
		int hour	= calendar.get(Calendar.HOUR_OF_DAY);
		int minute	= calendar.get(Calendar.MINUTE);
		
		return hour * 100 + minute;
	}
	
	private void getPushMsg() {
		getPushParam();
		new Thread() {
			public void run() {
				if(_pushParam.isMsgUrlValid()){
					String response	= NetUtil.httpPostRequest(_pushParam.getMsgUrl(), _pushParam.getMsgUrlParam(), 1);
					if(response == null){
						SwitchLogger.e(LOG_TAG, "request push msg fail");
						return ;
					}
					SwitchLogger.d(LOG_TAG, "response = " + response);
					onPushMsgResponse(response);
				} else {
					SwitchLogger.e(LOG_TAG, "msg url not valid, url=" + _pushParam.getMsgUrl());
				}
			}
		}.start();
	}
}
