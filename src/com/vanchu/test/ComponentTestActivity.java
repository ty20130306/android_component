package com.vanchu.test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanchu.libs.webCache.WebCache;
import com.vanchu.libs.common.container.SolidQueue;
import com.vanchu.libs.common.container.SolidQueue.SolidQueueCallback;
import com.vanchu.libs.common.task.Downloader;
import com.vanchu.libs.common.task.Downloader.IDownloadListener;
import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.ActivityUtil;
import com.vanchu.libs.common.util.SharedPrefsUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.push.PushParam;
import com.vanchu.libs.push.PushRobot;
import com.vanchu.libs.upgrade.UpgradeCallback;
import com.vanchu.libs.upgrade.UpgradeManager;
import com.vanchu.libs.upgrade.UpgradeParam;
import com.vanchu.libs.upgrade.UpgradeProxy;
import com.vanchu.sample.WebViewActivity;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ComponentTestActivity extends Activity {
	public static final String	SONG_PACKAGE_NAME	=	"com.vanchu.apps.bangyouxi.plugins.song";
	public static final String	SONG_INDEX_ACTIVITY	=	"com.vanchu.apps.bangyouxi.plugins.song.SongActivity";
	
	private static final String	LOG_TAG	= ComponentTestActivity.class.getSimpleName();
	
	private ProgressDialog	_progressDialog;
	
	private long lastBackKeyPressedTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.component_test);
		SwitchLogger.setPrintLog(true);
		Log.d(LOG_TAG, "current version name="+ActivityUtil.getCurrentVersionName(this));
		
		//testWebView(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.menu_exit:
			finish();
			break;

		default:
			break;
		}
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			
			long currTime	= System.currentTimeMillis();
			if(currTime - lastBackKeyPressedTime > 2000 ) {
				Tip.show(this, "再次按返回键退出游戏");
				lastBackKeyPressedTime	= currTime;
			} else {
				finish();
			}
			return true;

		default:
			break;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	public void testWebView(View v) {
		Intent intent	= new Intent(this, WebViewActivity.class);
		startActivity(intent);
	}
	
	public void testScaleCrop(View v) {
		Intent intent	= new Intent(this, CropActivity.class);
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
	
	private void printsq(LinkedList<String> sq) {
		for(int i = 0; i < sq.size(); ++i) {
			SwitchLogger.d(LOG_TAG, "sq["+i+"]="+sq.get(i));
		}
	}
	
//	private void testStringSolidQueue() {
//		SolidQueue<String> sq	= new SolidQueue<String>(this, "test", 10);
//		
//		SwitchLogger.d(LOG_TAG, "1 from file --------------------------");
//		printsq(sq.getQueue());
//		new Thread(){
//			public void run() {
//				SolidQueue<String> sq2	= new SolidQueue<String>(ComponentTestActivity.this, "test", 10);
//				sq2.enqueue("aa");
//				sq2.enqueue("bb");
//				sq2.enqueue("cc");
//				sq2.enqueue("dd");
//				sq2.enqueue("ee");
//				sq2.enqueue("ff");
//			}
//		}.start();
//		
//		sq.enqueue("a");
//		sq.enqueue("b");
//		sq.enqueue("c");
//		sq.enqueue("e");
//		sq.enqueue("d");
//		sq.enqueue("f");
//		
//		SwitchLogger.d(LOG_TAG, "2 from memory --------------------------");
//		printsq(sq.getQueue());
//		
//		sq	= new SolidQueue<String>(this, "test", 10);
//		SwitchLogger.d(LOG_TAG, "3 from file --------------------------");
//		printsq(sq.getQueue());
//	}
//	
	
	
	private void printMysq(LinkedList<MyItem> sq) {
		if(sq.size() == 0) {
			SwitchLogger.d(LOG_TAG, "sq is empty");
			return;
		}
		
		for(int i = 0; i < sq.size(); ++i) {
			MyItem item	= sq.get(i);
			SwitchLogger.d(LOG_TAG, "sq["+i+"]:"+item.get_id()+","+item.get_name()+","+item.get_url());
		}
	}
	
	private void testMySolidQueue() {
		SolidQueueCallback<MyItem> callback	= new SolidQueueCallback<MyItem> (){
			public void onAdd(MyItem element) {
				SwitchLogger.e(LOG_TAG, "onAdd, id="+element.get_id());
			}
			
			public void onRemove(MyItem element) {
				SwitchLogger.e(LOG_TAG, "onRemove, id="+element.get_id());
			}
		};
		
		SolidQueue<MyItem> sq	= new SolidQueue<MyItem>(this, "my_queue", 10, callback);
		
		SwitchLogger.d(LOG_TAG, "1 from file --------------------------");
		printMysq(sq.getQueue());
		
//		new Thread(){
//			public void run() {
//				SolidQueue<MyItem> sq2	= new SolidQueue<MyItem>(ComponentTestActivity.this, "test", 10);
//				sq2.enqueue("aa");
//				sq2.enqueue("bb");
//				sq2.enqueue("cc");
//				sq2.enqueue("dd");
//				sq2.enqueue("ee");
//				sq2.enqueue("ff");
//				sq2.solidify();
//			}
//		}.start();
		

		sq.enqueue(new MyItem("http://", "a", "pesi"));
		sq.enqueue(new MyItem("http://", "b", "pesi"));
		sq.enqueue(new MyItem("http://", "c", "pesi"));
		
		SwitchLogger.d(LOG_TAG, "2 from memory --------------------------");
		printMysq(sq.getQueue());
		
		sq	= new SolidQueue<MyItem>(this, "my_queue", 10, callback);
		SwitchLogger.d(LOG_TAG, "3 from file --------------------------");
		printMysq(sq.getQueue());
	}
	
	public void testSolidQueue(View v){
		// testStringSolidQueue();
		testMySolidQueue();
	}
	
	public void testDownloader(View v){
		SwitchLogger.setPrintLog(true);
		
		class TestDownloadListener implements IDownloadListener {

			@Override
			public void onStart() {
				initProgressDialog();
			}
			
			@Override
			public void onProgress(long downloaded, long total) {
				_progressDialog.setProgress((int)(downloaded * 100 / total));
				String tip	= String.format("正在下载安装包...\n已下载: %d K\n总大小: %d K",
											(int)(downloaded / 1024), (int)(total / 1024) );
				
				_progressDialog.setMessage(tip);
				
				SwitchLogger.d(LOG_TAG, "downloaded " + downloaded);
			}

			@Override
			public void onSuccess(String downloadFile) {
				SwitchLogger.d(LOG_TAG, "download " + downloadFile + " complete");
				_progressDialog.dismiss();
				SwitchLogger.d(LOG_TAG, "_progressDialog.dismiss() called");
			}

			@Override
			public void onError(int errCode) {
				SwitchLogger.d(LOG_TAG, "download error, errCode = " + errCode);
			}
		}

		new Downloader(this, "http://pesiwang.devel.rabbit.oa.com/song.apk", "test", new TestDownloadListener()).run();
	}
	
	public void testPluginSystem(View v){
		SwitchLogger.setPrintLog(true);
	
		Intent intent	= new Intent(this, TestPluginSystemActivity.class);
		startActivity(intent);
	}
	
	public void testWebCache(View v){
		Intent intent	= new Intent(this, WebCacheActivity.class);
		startActivity(intent);
	}
	
	public void startApp(View v){
		SwitchLogger.setPrintLog(true);
		ActivityUtil.startApp(this, SONG_PACKAGE_NAME);
	}
	
	public void startApp2(View v){
		SwitchLogger.setPrintLog(true);
		ActivityUtil.startApp(this, SONG_PACKAGE_NAME, SONG_INDEX_ACTIVITY);
	}
	
	public void goToSecond(View v){		
		Intent intent	= new Intent(this, SecondActivity.class);
		startActivity(intent);
		
		Map<String, String> param	= new HashMap<String, String>();
		param.put("a", "b");
		param.put("a", "c");
		param.put("a", "d");
		
		SwitchLogger.d(LOG_TAG, "result:" + (String)param.get("a"));
	}

	public void testPushService(View v){
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "testPushService()");
		
		//int msgInterval = PushParam.DEFAULT_MSG_INTERVAL;
		String msgUrl = "http://pesiwang.devel.rabbit.oa.com/test_push_msg.php";
		HashMap<String, String> msgUrlParam = new HashMap<String, String>();
		msgUrlParam.put("name", "wolf");
		
		PushParam pushParam	= new PushParam(3000, msgUrl, msgUrlParam);
		pushParam.setIgnoreIntervalLimit(true);
		pushParam.setMsgInterval(3000);
		pushParam.setNotifyWhenRunning(true);
		pushParam.setDefaults(Notification.DEFAULT_ALL);
		
		PushRobot.run(this, TestPushService.class, pushParam);
	}
	
	public void testPutStringMap(View v){
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "testPutStringMap()");
		HashMap<String, String> testMap	= new HashMap<String, String>();
		testMap.put("name", "wolf");
		testMap.put("age", "27");
		SharedPrefsUtil.putStringMap(this, "test", testMap);
		HashMap<String, String> m = SharedPrefsUtil.getStringMap(this, "test");
		
		SwitchLogger.d(LOG_TAG, "size:"+m.size()+","+m.toString());
		
	}
	
	public void feedback(View v){
		Intent intent	= new Intent(this, TestFeedbackActivity.class);
		startActivity(intent);
	}
	
	public void checkUpgradeUIManager(View v){
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "checkUpgradeManager");
		
		UpgradeParam param	= new UpgradeParam(
			ActivityUtil.getCurrentVersionName(this), 
			"1.0.3",
			"1.0.7", 
			"http://pesiwang.devel.rabbit.oa.com/component.1.0.4.apk",
			"升级详细内容"
		);
		

		class MyCallback extends UpgradeCallback {
			
			public MyCallback(Context context){
				super(context);
			}
			
			@Override
			public void onDownloadStarted() {
				initProgressDialog();
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
			}
		}

		class MyUpgradeManager extends UpgradeManager{
			public MyUpgradeManager(Context context, UpgradeParam param, UpgradeCallback callback){
				super(context, param, callback);
			}
			
			@Override
			protected Dialog createDetailDialog(){
				Dialog	dialog	= new Dialog(getContext());
				View view	= LayoutInflater.from(getContext()).inflate(R.layout.dialog, null);
				Button yes	= (Button)view.findViewById(R.id.dialog_upgrade);
				
				yes.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MyUpgradeManager.this.chooseToUpgrade();
					}
				});
				
				Button no	= (Button)view.findViewById(R.id.dialog_ignore);
				no.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MyUpgradeManager.this.choosetToSkip();
					}
				});
				
				TextView text	= (TextView)view.findViewById(R.id.dialog_text);
				UpgradeParam param	= getParam();
				SwitchLogger.e(LOG_TAG, text+"  xxxxxxxxxxxxxxxxxxxxxx   "+param);
				text.setText(param.getCurrentVersionName() +","+param.getHighestVersionName()+","+param.getUpgradeDetail());
				SwitchLogger.e(LOG_TAG, "yyyyyyyyyyyyyyyyyyyyyyyyyy");
				dialog.setContentView(view);
				
				return dialog;
			}
		}
		
		new MyUpgradeManager(this, param, new MyCallback(this)).check();

	}
	
	public void checkUpgradeManager(View v){
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "checkUpgradeManager");
		
		UpgradeParam param	= new UpgradeParam(
			ActivityUtil.getCurrentVersionName(this), 
			"1.0.3",
			"1.0.7", 
			"http://pesiwang.devel.rabbit.oa.com/component.1.0.4.apk",
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
			
			@Override
			public void onComplete(int result) {
				_progressDialog.dismiss();
			}
			
			public void exitApp() {
				SwitchLogger.d(LOG_TAG, "implement exitApp");
			}
		}
		
		new UpgradeManager(this, param, new MyCallback(this)).check();

	}
	
	public void upgradeUIProxy(View view){
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "checkUpgradeProxy");
		
		class MyCallback extends UpgradeCallback {
			
			public MyCallback(Context context){
				super(context);
			}
			
			@Override
			public void onDownloadStarted() {
				initProgressDialog();
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
					SwitchLogger.e(e);

					return null;
				}
			}
		}
		
		class MyUpgradeManager extends UpgradeManager{
			public MyUpgradeManager(Context context, UpgradeParam param, UpgradeCallback callback){
				super(context, param, callback);
			}
			
			@Override
			protected Dialog createDetailDialog(){
				Dialog	dialog	= new Dialog(getContext());
				View view	= LayoutInflater.from(getContext()).inflate(R.layout.dialog, null);
				Button yes	= (Button)view.findViewById(R.id.dialog_upgrade);
				
				yes.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MyUpgradeManager.this.chooseToUpgrade();
					}
				});
				
				Button no	= (Button)view.findViewById(R.id.dialog_ignore);
				no.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MyUpgradeManager.this.choosetToSkip();
					}
				});
				
				TextView text	= (TextView)view.findViewById(R.id.dialog_text);
				UpgradeParam param	= getParam();
				SwitchLogger.e(LOG_TAG, text+"  xxxxxxxxxxxxxxxxxxxxxx   "+param);
				text.setText(param.getCurrentVersionName() +","+param.getHighestVersionName()+","+param.getUpgradeDetail());
				SwitchLogger.e(LOG_TAG, "yyyyyyyyyyyyyyyyyyyyyyyyyy");
				dialog.setContentView(view);
				
				return dialog;
			}
		}
		
		class MyUpgradeProxy extends UpgradeProxy{
			public MyUpgradeProxy(Context context, String upgradeInfoUrl, UpgradeCallback callback){
				super(context, upgradeInfoUrl, callback);
			}
			
			@Override
			protected UpgradeManager createUpgradeManager(Context context, UpgradeParam param, UpgradeCallback callback){
				return new MyUpgradeManager(context, param, callback);
			}
		}
		
		new MyUpgradeProxy(
				this,
				"http://pesiwang.devel.rabbit.oa.com/t.php",
				new MyCallback(this)).check();

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
