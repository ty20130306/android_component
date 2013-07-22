package com.vanchu.test;

import com.vanchu.libs.push.PushBroadcastReceiver;

public class TestPushBroadcastReceiver extends PushBroadcastReceiver {
	
	protected Class<?> getServiceClass(){
		return TestPushService.class;
	}
}
