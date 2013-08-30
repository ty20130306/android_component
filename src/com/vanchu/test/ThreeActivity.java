package com.vanchu.test;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.ActivityUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.upgrade.UpgradeCallback;
import com.vanchu.libs.upgrade.UpgradeParam;
import com.vanchu.libs.upgrade.UpgradeProxy;

import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class ThreeActivity extends Activity {
	private static final String LOG_TAG	= SecondActivity.class.getSimpleName();
	
	private ProgressDialog	_progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_three);
		SwitchLogger.setPrintLog(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.three, menu);
		return true;
	}
	
	public void testExitApp(View v) {
		//android.os.Process.killProcess(android.os.Process.myPid());
		SwitchLogger.d(LOG_TAG, "testExitApp");
		ActivityManager am	= (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		am.killBackgroundProcesses(getPackageName());
	}
	
	public void home(View v) {
	     Intent intent = new Intent();
	     intent.setAction(Intent.ACTION_MAIN);
	     intent.addCategory(Intent.CATEGORY_HOME);           
	     startActivity(intent);
	}
	
	private void initProgressDialog(){
		_progressDialog	= new ProgressDialog(this);
		_progressDialog.setCancelable(false);
		_progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		_progressDialog.setMax(100);
		_progressDialog.setTitle("下载进度");
		_progressDialog.setMessage("正在准备下载安装包");
	}

	public void upgradeProxy(View view){

		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "checkUpgradeProxy");
		initProgressDialog();
		class MyCallback extends UpgradeCallback {
			
			public MyCallback(Context context){
				super(context);
			}
			
			@Override
			public void onDownloadStarted() {
				_progressDialog.show();
			}
			
			public void onLatestVersion(){
				super.onLatestVersion();
				Tip.show(getContext(), "当前已经是最新版本");
			}
			
			@Override
			public void onDownloadProgress(long downloaded, long total) {
				_progressDialog.setProgress((int)(downloaded * 100 / total));
				String tip	= String.format("正在下载安装包...\n已下载: %d K\n总大小: %d K",
											(int)(downloaded / 1024), (int)(total / 1024) );
				
				_progressDialog.setMessage(tip);
			}
			
			@Override
			public void onComplete(int result) {
				_progressDialog.dismiss();
			}
			
			public void exitApp() {
				SwitchLogger.d(LOG_TAG, "implement exitApp");
				((Activity)getContext()).finish();
			}
			
			@Override
			public UpgradeParam onUpgradeInfoResponse(String response){
				try {
					JSONObject jsonResponse	= new JSONObject(response);
					String lowest	= jsonResponse.getString("lowest");
					String highest	= jsonResponse.getString("highest");
					String url		= jsonResponse.getString("apkUrl");
					String detail	= jsonResponse.getString("detail");
					
					SwitchLogger.d(LOG_TAG, "receive info, lowest version:" + lowest + ", highest version: " + highest);
					SwitchLogger.d(LOG_TAG, "receive info, apkUrl: " + url + ", detail: " + detail);
					
					String current	= ActivityUtil.getCurrentVersionName(getContext());
					SwitchLogger.d(LOG_TAG, "current version: " + current);
					
					return new UpgradeParam(current, lowest, highest, url, detail);
				} catch(JSONException e){
					if(SwitchLogger.isPrintLog()){
						SwitchLogger.e(e);
					}
					return null;
				}
			}
		}
		
		new UpgradeProxy(
				this,
				"http://pesiwang.devel.rabbit.oa.com/t.php",
				new MyCallback(this)).check();

		
	}
}
