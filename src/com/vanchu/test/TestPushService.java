package com.vanchu.test;

import com.vanchu.libs.common.CommonUtil;
import com.vanchu.libs.common.NetUtil;
import com.vanchu.libs.common.SwitchLogger;
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
	protected void onNotificationClick(int msgType) {
		SwitchLogger.d(LOG_TAG, "onNotificationClick, msg type=" + msgType);
		
		switch(msgType){
		case 1:
			CommonUtil.startApp(this, ComponentTestActivity.class);
			break;
			
		case 2:
			NetUtil.openUrl(this, "http://www.baidu.com");
			break;
			
		default:
			
			break;
		}
	}
}
