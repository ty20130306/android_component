package com.vanchu.libs.push;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanchu.libs.common.CommonUtil;
import com.vanchu.libs.common.NetUtil;
import com.vanchu.libs.common.SwitchLogger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

abstract public class PushService extends Service {
	private static final String LOG_TAG	= PushService.class.getSimpleName();
	
	public static String START_TYPE		= "START_TYPE";
	public static String MSG_TYPE		= "MSG_TYPE";
	
	public static final int START_TYPE_INIT				= 1;
	public static final int START_TYPE_NOTIFICATION		= 2;
	
	private WakeLock	_wakeLock		= null;
	private PushParam	_pushParam		= null;
	
	private Timer 		_msgTimer		= null;
	private TimerTask	_msgTimerTask	= null;
	
	private Timer		_cfgTimer		= null;
	private TimerTask	_cfgTimerTask	= null;
	
	
	/**
	 * 根据消息类型返回icon id
	 * @param msgType	消息类型
	 * @return
	 */
	abstract protected int getNotificationIcon(int msgType);
	
	/**
	 * 根据消息类型实现推送消息的点击动作
	 * @param msgType	消息类型
	 */
	abstract protected void onNotificationClick(int msgType, Bundle msgExtra);
	
	private void showNotification(PushMsg pushMsg) {
		//SwitchLogger.d(LOG_TAG, "ticker="+pushMsg.getTicker()+",type="+pushMsg.getType()+",title="+pushMsg.getTitle()+",text="+pushMsg.getText());
		
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
		notification.defaults = Notification.DEFAULT_ALL;
		
		NotificationManager	notificationManager	= (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(pushMsg.getType(), notification);
	}
	
	private void onPushMsgResponse(String response){
		//SwitchLogger.d(LOG_TAG, response);
		
		try {
			JSONObject msg	= new JSONObject(response);
			PushMsg pushMsg	= new PushMsg(msg);
			if(pushMsg.isShow()){
				showNotification(pushMsg);
			}
		} catch(JSONException e){
			SwitchLogger.e(e);
		}
	}
	
	private void onPushCfgResponse(String response) {
		//SwitchLogger.d(LOG_TAG, response);
		try {
			JSONObject cfg	= new JSONObject(response);
			int msgInterval	= cfg.getInt("msgInterval");
			int cfgInterval	= cfg.getInt("cfgInterval");
			SwitchLogger.d(LOG_TAG, "received msgInterval="+msgInterval+",cfgInterval="+cfgInterval);
			resetPushInterval(msgInterval, cfgInterval);
		} catch(JSONException e){
			SwitchLogger.e(e);
		}
	}
	
	private void resetPushInterval(int msgInterval, int cfgInterval) {
		_pushParam.setMsgInterval(msgInterval);
		_pushParam.setCfgInterval(cfgInterval);
		setPushParam();
		stopTimerTask();
		initTimerTask();
	}
	
	private void stopTimerTask() {
		_msgTimerTask.cancel();
		_msgTimer.cancel();
		_msgTimerTask	= null;
		_msgTimer		= null;
		
		_cfgTimerTask.cancel();
		_cfgTimer.cancel();
		_cfgTimerTask	= null;
		_cfgTimer		= null;
	}
	
	@Override
	public IBinder onBind(Intent intent){
		
		return null;
	}

	@Override
	public void onCreate(){
		SwitchLogger.d(LOG_TAG, "onCreate()");
		acquireWakeLock();
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
				initTimerTask();
				break;
	
			case START_TYPE_NOTIFICATION :
				onNotificationClick(intent.getIntExtra(MSG_TYPE, PushMsg.MSG_TYPE_NONE), intent.getExtras());
				break;
				
			default:
				break;
			}
		} else {
			initTimerTask();
		}
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy(){
		SwitchLogger.d(LOG_TAG, "onDestroy()");
		releaseWakeLock();
		super.onDestroy();
	}
	
	private void getPushParam(){
		_pushParam	= PushRobot.getPushParam(this);
	}
	
	private void setPushParam(){
		PushRobot.setPushParam(this, _pushParam);
	}
	
	private void initTimerTask(){
		startGetMsgTimer();
		startGetCfgTimer();
	}

	private void getPushMsg() {
		getPushParam();
		
		boolean notifyWhenRunning	= _pushParam.getNotifyWhenRunning();
		if( ! notifyWhenRunning && CommonUtil.isAppRuning(this)){
			return;
		}
		
		if(_pushParam.isMsgUrlValid()){
			String response	= NetUtil.httpPostRequest(_pushParam.getMsgUrl(), _pushParam.getMsgUrlParam(), 3);
			if(response == null){
				SwitchLogger.e(LOG_TAG, "request push msg fail");
				return ;
			}
			
			onPushMsgResponse(response);
		} else {
			SwitchLogger.d(LOG_TAG, "msg url not valid, url=" + _pushParam.getMsgUrl());
		}
	}
	
	private void startGetMsgTimer() {
		SwitchLogger.d(LOG_TAG, "startGetMsgTimer()");
		
		if(_msgTimerTask == null){
			SwitchLogger.d(LOG_TAG, "_msgTimerTask is null");
			if(_msgTimer == null){
				SwitchLogger.d(LOG_TAG, "_msgTimer is null");
				_msgTimer	= new Timer(true);
			}
			
			_msgTimerTask	= new TimerTask() {
				
				@Override
				public void run() {
					getPushMsg();
				}
			};
			
			_msgTimer.schedule(_msgTimerTask, _pushParam.getMsgInterval(), _pushParam.getMsgInterval());
		} else {
			SwitchLogger.d(LOG_TAG, "_msgTimerTask already inited");
		}
	}
	
	private void getPushCfg() {
		getPushParam();
		
		if(_pushParam.isCfgUrlValid()){
			String response	= NetUtil.httpPostRequest(_pushParam.getCfgUrl(), _pushParam.getCfgUrlParam(), 3);
			if(response == null){
				SwitchLogger.e(LOG_TAG, "request push cfg fail");
				return ;
			}
			onPushCfgResponse(response);
		} else {
			SwitchLogger.d(LOG_TAG, "cfg url is not valid, disable cfg request");
		}
	}

	private void startGetCfgTimer() {
		SwitchLogger.d(LOG_TAG, "startGetCfgTimer()");
		
		if(_cfgTimerTask == null){
			SwitchLogger.d(LOG_TAG, "_cfgTimerTask is null");
			if(_cfgTimer == null){
				SwitchLogger.d(LOG_TAG, "_cfgTimer is null");
				_cfgTimer	= new Timer(true);
			}
			
			_cfgTimerTask	= new TimerTask() {
				@Override
				public void run() {
					getPushCfg();
				}
			};
			
			_cfgTimer.schedule(_cfgTimerTask, _pushParam.getCfgInterval(), _pushParam.getCfgInterval());
		} else {
			SwitchLogger.d(LOG_TAG, "_cfgTimerTask already inited");
		}
	}
	
	// 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
	private void acquireWakeLock() {
		if (null == _wakeLock) {
			PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
			_wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "PushService");
			if (null != _wakeLock) {
				_wakeLock.acquire();
			}
		}
	}

	// 释放设备电源锁
	private void releaseWakeLock() {
		if (null != _wakeLock) {
			_wakeLock.release();
			_wakeLock = null;
		}
	}
	
}
