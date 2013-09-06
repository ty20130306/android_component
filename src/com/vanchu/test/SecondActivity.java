package com.vanchu.test;

import com.vanchu.libs.common.ui.LoadingDialog;
import com.vanchu.libs.common.util.ActivityUtil;
import com.vanchu.libs.common.util.IdUtil;
import com.vanchu.libs.common.util.ImgUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.common.util.ThreadUtil;
import com.vanchu.libs.upgrade.UpgradeCallback;
import com.vanchu.libs.upgrade.UpgradeManager;
import com.vanchu.libs.upgrade.UpgradeParam;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class SecondActivity extends Activity {

	private static final String LOG_TAG	= SecondActivity.class.getSimpleName();
	private ProgressBar _pBar;
	private ProgressDialog	_progressDialog;
	
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
		
		SwitchLogger.setPrintLog(true);
		Log.d(LOG_TAG, "current version name="+ActivityUtil.getCurrentVersionName(this));
		
		String deviceId	= IdUtil.getDeviceId(this);
		String uuid		= IdUtil.getUUID();
		String uniqueId	= IdUtil.getDeviceUniqueId(this);
		
		SwitchLogger.d(LOG_TAG, "device id="+deviceId+",uuid="+uuid+",uniqueId="+uniqueId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.second, menu);
		return true;
	}
	
	public void testAysncImgaeLoader(View v) {
		
		
		ImgUtil imgUtil	= new ImgUtil(this);
		ImageView imageView	= (ImageView) findViewById(R.id.aysnc_img);
		imgUtil.asyncSetImg(imageView, "http://pesiwang.devel.rabbit.oa.com/icon.png", R.drawable.icon);
	}
	
	public void testMyProgressDialog(View v){
		new MyProgressDialog(this).show();
	}
	
	public void goToThree(View v){
		Intent intent	= new Intent(this, ThreeActivity.class);
		startActivity(intent);
	}
	
	public void testExitApp(View v) {
		android.os.Process.killProcess(android.os.Process.myPid());
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
	
	private void initProgressDialog(){
		_progressDialog	= new ProgressDialog(this);
		_progressDialog.setCancelable(false);
		_progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		_progressDialog.setMax(100);
		_progressDialog.setTitle("下载进度");
		_progressDialog.setMessage("正在准备下载安装包");
	}
	
	public void checkUpgrade(View v){
		UpgradeParam param	= new UpgradeParam(
			"1.0.3",
			"1.0.7", 
			"1.0.5", 
			"http://pesiwang.devel.rabbit.oa.com/component.apk",
			"升级详细内容"
		);
		initProgressDialog();
		
		class MyCallback extends UpgradeCallback {
			
			public MyCallback(Context context){
				super(context);
			}
			
			@Override
			public void onDownloadStarted() {
				_progressDialog.show();
			}
			
			@Override
			public void onDownloadProgress(long downloaded, long total) {
				_progressDialog.setProgress((int)(downloaded * 100 / total));
				String tip	= String.format("正在下载安装包...\n已下载: %d K\n总大小: %d K",
											(int)(downloaded / 1024), (int)(total / 1024) );
				
				_progressDialog.setMessage(tip);
			}
			
			public void onComplete(int result) {
				_progressDialog.dismiss();
			}
			
			public void exitApp() {
				android.os.Process.killProcess(android.os.Process.myPid());
				SwitchLogger.d(LOG_TAG, "implement exitApp");
			}
		}
		
		new UpgradeManager(
			this, 
			param,
			new MyCallback(this)).check();

	}
}
