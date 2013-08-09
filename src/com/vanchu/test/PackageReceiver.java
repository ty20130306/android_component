package com.vanchu.test;

import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.SwitchLogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PackageReceiver extends BroadcastReceiver {
	private static final String		LOG_TAG		= PackageReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action	= intent.getAction();
		
		SwitchLogger.e(LOG_TAG, "PackageReceiver receive action = " + action);
		if(Intent.ACTION_PACKAGE_ADDED.equals(action)){
			SwitchLogger.e(LOG_TAG, "有应用被添加");
        	Tip.show(context, "有应用被添加");
		} else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())){  
			SwitchLogger.e(LOG_TAG, "有应用被删除");
        	Tip.show(context, "有应用被删除");
		} else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
			SwitchLogger.e(LOG_TAG, "有应用被替换");
			Tip.show(context, "有应用被替换");
		}

	}

}
