package com.vanchu.libs.push;

import java.util.HashMap;
import java.util.Map;


public class PushParam {

	public static final int MIN_MSG_INTERVAL		= 300000;	// milliseconds = 5 minutes
	public static final int DEFAULT_MSG_INTERVAL	= 3600000;	// milliseconds = 1 hour
	public static final int MAX_MSG_INTERVAL		= 86400000;	// milliseconds = 1 day

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
	
	public PushParam(int msgInterval,
					String msgUrl, 
					Map<String, String> msgUrlParam,
					boolean notifyWhenRunning)
	{
		setMsgInterval(msgInterval);
		_msgUrl			= msgUrl;
		_msgUrlParam	= msgUrlParam;
		if(_msgUrlParam == null){
			_msgUrlParam	= new HashMap<String, String>();
		}
		
		_notifyWhenRunning	= notifyWhenRunning;
	}
	
	public String getMsgUrl(){
		return _msgUrl;
	}

	public Map<String, String> getMsgUrlParam(){
		return _msgUrlParam;
	}
	
	public int getMsgInterval(){
		return _msgInterval;
	}
	
	public void setMsgInterval(int msgInterval){
		msgInterval		= adjustMsgInterval(msgInterval);
		_msgInterval	= msgInterval;
	}
	
	public boolean getNotifyWhenRunning(){
		return _notifyWhenRunning;
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
