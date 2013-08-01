package com.vanchu.libs.push;

import com.vanchu.libs.common.util.SwitchLogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

abstract public class PushBroadcastReceiver extends BroadcastReceiver {
	private static final String	LOG_TAG	= PushBroadcastReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_USER_PRESENT.endsWith(action)) {
			SwitchLogger.setPrintLog(true);
			SwitchLogger.d(LOG_TAG, "receive action=" + Intent.ACTION_USER_PRESENT + ", begin to check push service");
			PushRobot.check(context, getServiceClass());
		}
	}
	
	/**
	 * 返回继承PushService实现的类
	 * @return
	 */
	abstract protected Class<?> getServiceClass();
}
