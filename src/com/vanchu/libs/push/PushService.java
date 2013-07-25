package com.vanchu.libs.push;

import java.util.HashMap;
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
		getPushParam();
		notification.defaults = _pushParam.getDefaults();
		
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
			
			updatePushCfgIfNeed(pushMsg);
		} catch(JSONException e){
			SwitchLogger.e(e);
		}
	}
	
	private void updatePushCfgIfNeed(PushMsg pushMsg) {
		HashMap<String, String> cfg	= pushMsg.getCfg();
		if(cfg.containsKey("interval")){
			int interval	= Integer.parseInt(cfg.get("interval"));
			SwitchLogger.d(LOG_TAG, "received interval="+interval);
			resetPushInterval(interval);
		}
	}
	
	private void resetPushInterval(int interval) {
		getPushParam();
		if(interval != _pushParam.getMsgInterval()){
			_pushParam.setMsgInterval(interval);
			setPushParam();
			stopTimerTask();
			initTimerTask();
			return ;
		} else {
			SwitchLogger.d(LOG_TAG, "same interval, no need to update");
		}

	}
	
	private void stopTimerTask() {
		if(_msgTimerTask != null){
			_msgTimerTask.cancel();
			_msgTimerTask	= null;
		}
		if(_msgTimer != null){
			_msgTimer.cancel();
			_msgTimer		= null;
		}
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
			SwitchLogger.d(LOG_TAG, "_msgTimerTask schedule succ, interval="+_pushParam.getMsgInterval());
		} else {
			SwitchLogger.d(LOG_TAG, "_msgTimerTask already inited");
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
