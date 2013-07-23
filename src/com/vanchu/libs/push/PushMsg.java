package com.vanchu.libs.push;

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
	
	public PushMsg(JSONObject msg){
		_show	= true;
		_extra	= new Bundle();
		
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
				while(iterator.hasNext()){
					
					String key	= (String)iterator.next();
					_extra.putString(key, extra.getString(key));
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
	
	public Bundle getExtra(){
		return _extra;
	}
}
