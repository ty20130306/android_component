package com.vanchu.test;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.vanchu.libs.common.util.NetUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.push.PushService;

public class TestPushService extends PushService {
	private static final String LOG_TAG	= TestPushService.class.getSimpleName();
	
	@Override
	protected int getNotificationIcon(int msgType) {
		//SwitchLogger.d(LOG_TAG, "getNotificationIcon, msg type=" + msgType);
		switch (msgType) {
		case 1:
			return R.drawable.ic_launcher;

		case 2:
			return R.drawable.icon;
			
		default:
			return R.drawable.ic_launcher;
		}
	}
	
	@Override
	protected JSONObject parseMsgResponse(String response) throws JSONException {
		JSONObject msg	= new JSONObject(response);
		
		return msg.getJSONObject("notification");
	}

	@Override
	protected void onNotificationClick(int msgType, Bundle msgExtra) {
		SwitchLogger.e(LOG_TAG, "onNotificationClick, msg type=" + msgType);
		SwitchLogger.e(LOG_TAG, "onNotificationClick, extra string name=" + msgExtra.getString("name"));
		int num	= Integer.parseInt(msgExtra.getString("num"));
		SwitchLogger.e(LOG_TAG, "onNotificationClick, extra int num=" + num);
		SwitchLogger.e(LOG_TAG, "onNotificationClick, extra string age=" + msgExtra.getString("age"));
		
		switch(msgType){
		case 1:
			//ActivityUtil.startApp(this, ComponentTestActivity.class);
			putMsgUrlParam("name", msgExtra.getString("name") + ", from client");
			SwitchLogger.d(LOG_TAG, "receive msg type = " + msgType);
			break;
			
		case 2:
			NetUtil.openUrl(this, "http://www.baidu.com");
			break;
			
		default:
			
			break;
		}
	}
}
