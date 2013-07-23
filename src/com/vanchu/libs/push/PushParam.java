package com.vanchu.libs.push;

import java.util.HashMap;
import java.util.Map;


public class PushParam {

	public static final int MIN_MSG_INTERVAL		= 300000;	// milliseconds = 5 minutes
	public static final int DEFAULT_MSG_INTERVAL	= 600000;	// milliseconds = 10 minutes
	public static final int MAX_MSG_INTERVAL		= 3600000;	// milliseconds = 1 hour
	
	public static final int MIN_CFG_INTERVAL		= 1800000;	// milliseconds = 30 minutes
	public static final int DEFAULT_CFG_INTERVAL	= 3600000;	// milliseconds = 1 hour
	public static final int MAX_CFG_INTERVAL		= 86400000;	// milliseconds = 24 hours
	
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
	 * 请求推送配置的url
	 */
	private String					_cfgUrl;
	
	/**
	 * 请求推送配置的url参数
	 */
	private Map<String, String>		_cfgUrlParam;
	
	/**
	 * 请求推送配置的间隔，单位是毫秒
	 */
	private int						_cfgInterval;
	
	/**
	 * app运行时是否弹出推送消息
	 */
	private boolean					_notifyWhenRunning;
	
	public PushParam(int msgInterval,
					String msgUrl, 
					Map<String, String> msgUrlParam,
					int cfgInterval,
					String cfgUrl,
					Map<String, String> cfgUrlParam,
					boolean notifyWhenRunning)
	{
		setMsgInterval(msgInterval);
		_msgUrl			= msgUrl;
		_msgUrlParam	= msgUrlParam;
		if(_msgUrlParam == null){
			_msgUrlParam	= new HashMap<String, String>();
		}
		
		setCfgInterval(cfgInterval);
		_cfgUrl			= cfgUrl;
		_cfgUrlParam	= cfgUrlParam;
		if(_cfgUrlParam == null){
			_cfgUrlParam	= new HashMap<String, String>();
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
	
	public String getCfgUrl(){
		return _cfgUrl;
	}
	
	public Map<String, String> getCfgUrlParam(){
		return _cfgUrlParam;
	}

	public int getCfgInterval(){
		return _cfgInterval;
	}
	
	public void setCfgInterval(int cfgInterval){
		cfgInterval		= adjustCfgInterval(cfgInterval);
		_cfgInterval	= cfgInterval;
	}
	
	public boolean getNotifyWhenRunning(){
		return _notifyWhenRunning;
	}
	
	public boolean isMsgUrlValid(){
		return (_msgUrl != "");
	}
	
	public boolean isCfgUrlValid(){
		return (_cfgUrl != "");
	}
	
	private int adjustMsgInterval(int msgInterval){
		if(msgInterval < MIN_MSG_INTERVAL){
			msgInterval	= MIN_MSG_INTERVAL;
		} else if(msgInterval > MAX_MSG_INTERVAL){
			msgInterval	= MAX_MSG_INTERVAL;
		}
		
		return msgInterval;
	}
	
	private int adjustCfgInterval(int cfgInterval){
		if(cfgInterval < MIN_CFG_INTERVAL){
			cfgInterval	= MIN_CFG_INTERVAL;
		} else if(cfgInterval > MAX_CFG_INTERVAL){
			cfgInterval	= MAX_CFG_INTERVAL;
		}
		
		return cfgInterval;
	}
}
