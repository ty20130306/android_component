package com.vanchu.libs.push;

import java.util.HashMap;
import java.util.Map;

import android.app.Notification;


/**
 * @author wolf
 *
 */
public class PushParam {

	public static final int MIN_MSG_INTERVAL		= 5 * 60 * 1000; // milliseconds = 5 minutes
	public static final int DEFAULT_MSG_INTERVAL	= 60 * 60 * 1000;	// milliseconds = 1 hour
	public static final int MAX_MSG_INTERVAL		= 24 * 60 * 60 * 1000;	// milliseconds = 1 day

	public static final int DEFAULT_DELAY			= 4 * 60 * 60 * 1000;	// milliseconds = 4 hours
	
	public static final int DEFAULT_AVAI_START_TIME	= 800;
	public static final int DEFAULT_AVAI_END_TIME	= 2400;
	
	public static final int MIN_AVAI_START_TIME		= 0;
	public static final int MAX_AVAI_END_TIME		= 2400;
	
	public static final int DEFAULT_AFTER			= 4 * 60 * 60 * 1000;	// milliseconds = 4 hours
	
	
	/**
	 * 请求推送消息的url
	 */
	private String					_msgUrl;
	
	/**
	 * 请求推送消息的url参数
	 */
	private Map<String, String>		_msgUrlParam;
	
	/**
	 * 请求推送消息的间隔，单位是毫秒
	 */
	private int						_msgInterval;
	
	/**
	 * app运行时是否弹出推送消息
	 */
	private boolean					_notifyWhenRunning;
	
	
	/**
	 * 接受到通知时的表现
	 */
	private int						_defaults;
	
	
	
	/**
	 * 忽略推送间隔限制
	 */
	private boolean					_ignoreIntervalLimit;
	
	
	/**
	 * 退出app后延迟推送的时间
	 */
	private int						_delay;
	
	
	/**
	 * 每天开始允许推送开始时间, 早上8点时间表示为：0800
	 */
	private int						_avaiStartTime;
	
	/**
	 * 每天开始允许推送结束时间, 晚上8点时间表示为：2000
	 */
	private int						_avaiEndTime;
	
	
	/**
	 * 成功弹出过推送后经过_after毫秒后才能再弹出
	 */
	private int 					_after;
	
	public PushParam() {
		setMsgInterval(DEFAULT_MSG_INTERVAL);
		_msgUrl			= "";
		_msgUrlParam	= new HashMap<String, String>();
		
		_notifyWhenRunning	= false;
		_defaults			= Notification.DEFAULT_LIGHTS;
		_ignoreIntervalLimit	= false;
		
		_delay				= DEFAULT_DELAY;
		_avaiStartTime		= DEFAULT_AVAI_START_TIME;
		_avaiEndTime		= DEFAULT_AVAI_END_TIME;
		_after				= DEFAULT_AFTER;
	}
	
	public int getAfter() {
		return _after;
	}
	
	public void setAfter(int after) {
		_after	= after;
	}
	
	public int getAvaiStartTime() {
		return _avaiStartTime;
	}
	
	public void setAvaiTime(int avaiStartTime, int avaiEndTime) {
		if(avaiStartTime < MIN_AVAI_START_TIME || avaiStartTime > MAX_AVAI_END_TIME) {
			avaiStartTime	= MIN_AVAI_START_TIME;
		}
		_avaiStartTime	= avaiStartTime;
		
		if(avaiEndTime < MIN_AVAI_START_TIME || avaiEndTime > MAX_AVAI_END_TIME) {
			avaiEndTime	= MAX_AVAI_END_TIME;
		}
		_avaiEndTime	= avaiEndTime;
		
		if(_avaiStartTime > _avaiEndTime) {
			int tmp			= _avaiStartTime;
			_avaiStartTime	= _avaiEndTime;
			_avaiEndTime	= tmp;
		}
	}
	
	public int getAvaiEndTime() {
		return _avaiEndTime;
	}
	
	public void setAvaiEndTime(int avaiEndTime) {
		if(avaiEndTime < MIN_AVAI_START_TIME || avaiEndTime > MAX_AVAI_END_TIME) {
			avaiEndTime	= MAX_AVAI_END_TIME;
		}
		_avaiEndTime	= avaiEndTime;
	}
	
	public int getDelay() {
		return _delay;
	}
	
	public void setDelay(int delay) {
		_delay	= delay;
	}
	
	public void setIgnoreIntervalLimit(boolean ignoreIntervalLimit) {
		_ignoreIntervalLimit	= ignoreIntervalLimit;
	}
	
	public boolean isIgnoreIntervalLimit() {
		return _ignoreIntervalLimit;
	}
	
	public void setMsgUrl(String msgUrl) {
		_msgUrl	= msgUrl;
	}
	
	public String getMsgUrl(){
		return _msgUrl;
	}

	public void setMsgUrlParam(Map<String, String> msgUrlParam) {
		_msgUrlParam	= msgUrlParam;
	}
	
	public Map<String, String> getMsgUrlParam(){
		return _msgUrlParam;
	}
	
	public int getMsgInterval(){
		return _msgInterval;
	}
	
	public void setMsgInterval(int msgInterval){
		if( ! _ignoreIntervalLimit) {
			msgInterval		= adjustMsgInterval(msgInterval);
		}
		
		_msgInterval	= msgInterval;
	}
	
	public boolean getNotifyWhenRunning(){
		return _notifyWhenRunning;
	}
	
	public void setNotifyWhenRunning(boolean notifyWhenRunning){
		_notifyWhenRunning	= notifyWhenRunning;
	}
	
	public int getDefaults(){
		return _defaults;
	}
	
	public void setDefaults(int defaults){
		_defaults	= defaults;
	}
	
	public boolean isMsgUrlValid(){
		return (_msgUrl != "");
	}
	
	private int adjustMsgInterval(int msgInterval){
		if(msgInterval < MIN_MSG_INTERVAL){
			msgInterval	= MIN_MSG_INTERVAL;
		} else if(msgInterval > MAX_MSG_INTERVAL){
			msgInterval	= MAX_MSG_INTERVAL;
		}
		
		return msgInterval;
	}

}
