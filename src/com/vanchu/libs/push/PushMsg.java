package com.vanchu.libs.push;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class PushMsg {
	public static final int MSG_TYPE_NONE		= 0;
	
	private boolean	_show;
	private	int		_type;
	private	String	_title;
	private	String	_text;
	private	String	_ticker;
	private	Map<String, String>	_extra;
	
	public PushMsg(JSONObject msg){
		_show	= true;
		_extra	= new HashMap<String, String>();
		
		try {
			_type	= msg.getInt("type");
			_title	= msg.getString("title");
			_text	= msg.getString("text");
			
			if(msg.has("show")){
				int s	= msg.getInt("show");
				if(s == 0){
					_show	= false;
				} else {
					_show	= true;
				}
			}
			
			if(msg.has("ticker")){
				_ticker	= msg.getString("ticker");
			} else {
				_ticker	= _text;
			}
			
			if(msg.has("extra")){
				JSONObject extra		= msg.getJSONObject("extra");
				Iterator<?> iterator	= extra.keys();
				if(iterator.hasNext()){
					String key	= (String)iterator.next();
					_extra.put(key, extra.getString(key));
				}
			}
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
	
	public String getExtraString(String key, String defValue){
		if(_extra.containsKey(key)){
			return _extra.get(key);
		} else {
			return defValue;
		}
	}
	
	public int getExtraInt(String key, int defValue){
		try {
			if(_extra.containsKey(key)){
				return Integer.parseInt(_extra.get(key));
			} else {
				return defValue;
			}
		} catch(NumberFormatException e){
			return defValue;
		}
	}
}
