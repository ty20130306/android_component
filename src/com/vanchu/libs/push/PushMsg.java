package com.vanchu.libs.push;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;


import android.os.Bundle;

public class PushMsg {
	
	public static final int MSG_TYPE_NONE		= 0;
	
	private boolean	_show;
	private	int		_type;
	private	String	_title;
	private	String	_text;
	private	String	_ticker;
	private	Bundle	_extra;
	
	private HashMap<String, String> _cfg;
	
	public PushMsg(JSONObject msg){
		_show	= false;
		_extra	= new Bundle();
		_cfg	= new HashMap<String, String>();
		
		try {
			parseCfg(msg);
		} catch (JSONException e){
			// ignore the exception
		}
		
		try {
			parseData(msg);
		} catch(JSONException e){
			_show	= false;
			_type	= MSG_TYPE_NONE;
			_title	= "";
			_text	= "";
			_ticker	= "";
		}
		
	}
	
	public boolean isShow(){
		return _show;
	}
	
	public int getType(){
		return _type;
	}
	
	public String getTicker(){
		return _ticker;
	}
	
	public String getTitle(){
		return _title;
	}
	
	public String getText(){
		return _text;
	}
	
	public Bundle getExtra(){
		return _extra;
	}
	
	public HashMap<String, String> getCfg(){
		return _cfg;
	}
	
	private void parseExtra(JSONObject data) throws JSONException {
		if(data.has("extra")){
			JSONObject extra		= data.getJSONObject("extra");
			Iterator<?> iterator	= extra.keys();
			while(iterator.hasNext()){
				String key	= (String)iterator.next();
				_extra.putString(key, extra.getString(key));
			}
		}
	}
	
	private void parseShow(JSONObject data) throws JSONException {
		if(data.has("show")){
			int s	= Integer.parseInt(data.getString("show"));
			if(s == 0){
				_show	= false;
			} else {
				_show	= true;
			}
		}
	}
	
	private void parseTicker(JSONObject data) throws JSONException {
		if(data.has("ticker")){
			_ticker	= data.getString("ticker");
		} else {
			_ticker	= _text;
		}
	}
	
	private void parseCfg(JSONObject msg) throws JSONException {
		if(msg.has("cfg")){
			JSONObject extra		= msg.getJSONObject("cfg");
			Iterator<?> iterator	= extra.keys();
			while(iterator.hasNext()){
				String key	= (String)iterator.next();
				_cfg.put(key, extra.getString(key));
			}
		}
	}
	
	private void parseData(JSONObject msg) throws JSONException {
		if(msg.has("data")){
			JSONObject	data	= msg.getJSONObject("data");
			_type	= Integer.parseInt(data.getString("type"));
			_title	= data.getString("title");
			_text	= data.getString("text");
			
			parseShow(data);
			parseTicker(data);
			parseExtra(data);
		} else {
			_show	= false;
			_type	= MSG_TYPE_NONE;
			_title	= "";
			_text	= "";
			_ticker	= "";
		}
	}
}
