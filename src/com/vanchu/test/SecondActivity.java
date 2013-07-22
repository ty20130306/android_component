package com.vanchu.test;

import com.vanchu.libs.upgrade.UpgradeCallback;
import com.vanchu.libs.upgrade.UpgradeManager;
import com.vanchu.libs.upgrade.UpgradeParam;
import com.vanchu.libs.upgrade.UpgradeUtil;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class SecondActivity extends Activity {

	private static final String LOG_TAG	= SecondActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second);
		
		Log.d(LOG_TAG, "current version name="+UpgradeUtil.getCurrentVersionName(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.second, menu);
		return true;
	}

	public void checkUpgrade(View v){
		UpgradeParam param	= new UpgradeParam(
			"1.0.3",
			"1.0.7", 
			"1.0.5", 
			"http://pesiwang.devel.rabbit.oa.com/component.apk",
			"升级详细内容"
		);
		
		new UpgradeManager(
			this, 
			param,
			new UpgradeCallback(this)).check();

	}
}
