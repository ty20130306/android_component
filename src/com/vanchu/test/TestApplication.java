package com.vanchu.test;

import com.vanchu.libs.common.task.CustomUEH;
import com.vanchu.libs.common.util.SwitchLogger;

import android.app.Application;

public class TestApplication extends Application {
	
	private static final String LOG_TAG		= TestApplication.class.getSimpleName();
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "onCreate, init CustomUEH");
		CustomUEH.instance().init(getApplicationContext());
	}
}
