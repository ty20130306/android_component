package com.vanchu.test;

import com.vanchu.libs.common.ui.LoadingDialog;
import com.vanchu.libs.common.util.ActivityUtil;
import com.vanchu.libs.common.util.ThreadUtil;
import com.vanchu.libs.upgrade.UpgradeCallback;
import com.vanchu.libs.upgrade.UpgradeManager;
import com.vanchu.libs.upgrade.UpgradeParam;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class SecondActivity extends Activity {

	private static final String LOG_TAG	= SecondActivity.class.getSimpleName();
	private ProgressBar _pBar;
	
	private Handler _handler	= new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				LoadingDialog.cancel();
				break;

			case 1:
				Log.d(LOG_TAG, "progress bar test");
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second);
		
		Log.d(LOG_TAG, "current version name="+ActivityUtil.getCurrentVersionName(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.second, menu);
		return true;
	}
	
	public void testLoadingDialog(View v){
		LoadingDialog.createWithAnim(this, R.style.dialogCenterAnimation);
		new Thread(){
			public void run() {
				ThreadUtil.sleep(2000);
				_handler.sendEmptyMessage(0);
			}
		}.start();
	}

	public void testLoadingDialog2(View v){
		LoadingDialog.create(this, "正在努力加载中");
		new Thread(){
			public void run() {
				ThreadUtil.sleep(2000);
				_handler.sendEmptyMessage(0);
			}
		}.start();
	}
	
	public void testLoadingDialog3(View v){
		LoadingDialog.create(this);
		new Thread(){
			public void run() {
				ThreadUtil.sleep(2000);
				_handler.sendEmptyMessage(0);
			}
		}.start();
	}
	
	public void testProgressBar(View v){
		Dialog dialog	= new Dialog(this);
		RelativeLayout rl	= new RelativeLayout(this);
		rl.setBackgroundColor(Color.TRANSPARENT);
		
		_pBar	= new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
		_pBar.setIndeterminate(true);
		RelativeLayout.LayoutParams rlp	= new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
		
		rl.setLayoutParams(rlp);
		rl.addView(_pBar);

		//_pBar.setLayoutParams(rlp);
	
		dialog.setContentView(rl);
		dialog.show();
		
		new Thread(){
			public void run() {
				ThreadUtil.sleep(2000);
				_handler.sendEmptyMessage(1);
			}
		}.start();
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
