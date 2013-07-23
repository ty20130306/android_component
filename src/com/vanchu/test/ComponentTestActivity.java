package com.vanchu.test;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanchu.libs.common.SharedPrefsUtil;
import com.vanchu.libs.common.SwitchLogger;
import com.vanchu.libs.push.PushParam;
import com.vanchu.libs.push.PushRobot;
import com.vanchu.libs.upgrade.UpgradeCallback;
import com.vanchu.libs.upgrade.UpgradeManager;
import com.vanchu.libs.upgrade.UpgradeParam;
import com.vanchu.libs.upgrade.UpgradeProxy;
import com.vanchu.libs.upgrade.UpgradeUtil;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class ComponentTestActivity extends Activity {
	private static final String	LOG_TAG	= ComponentTestActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.component_test);
		
		Log.d(LOG_TAG, "current version name="+UpgradeUtil.getCurrentVersionName(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void goToSecond(View v){
		Toast.makeText(this, 
				String.format("更新失败，存储空间不足, 需要 %s M空间", "545515"), 
				Toast.LENGTH_LONG).show();
		
		
		Intent intent	= new Intent(this, SecondActivity.class);
		startActivity(intent);
	}

	public void testPushService(View v){
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "testPushService()");
		
		//int msgInterval = PushParam.DEFAULT_MSG_INTERVAL;
		String msgUrl = "http://pesiwang.devel.rabbit.oa.com/test_push_msg.php";
		HashMap<String, String> msgUrlParam = new HashMap<String, String>();
		msgUrlParam.put("name", "wolf");
		
		//int cfgInterval = PushParam.DEFAULT_CFG_INTERVAL;
		String cfgUrl = "http://pesiwang.devel.rabbit.oa.com/test_push_cfg.php";
		HashMap<String, String> cfgUrlParam = new HashMap<String, String>();
		cfgUrlParam.put("age", "24");
		
		PushParam pushParam	= new PushParam(3000, msgUrl, msgUrlParam, 5000, cfgUrl, cfgUrlParam, true);
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
	
	public void checkUpgradeUIManager(View v){
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "checkUpgradeManager");
		
		UpgradeParam param	= new UpgradeParam(
			UpgradeUtil.getCurrentVersionName(this), 
			"1.0.3",
			"1.0.7", 
			"http://pesiwang.devel.rabbit.oa.com/component.1.0.4.apk",
			"升级详细内容"
		);

		class MyUpgradeManager extends UpgradeManager{
			public MyUpgradeManager(Context context, UpgradeParam param, UpgradeCallback callback){
				super(context, param, callback);
			}
			
			@Override
			protected Dialog createDialog(){
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
		
		new MyUpgradeManager(this, param, new UpgradeCallback(this)).check();

	}
	
	public void checkUpgradeManager(View v){
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "checkUpgradeManager");
		
		UpgradeParam param	= new UpgradeParam(
			UpgradeUtil.getCurrentVersionName(this), 
			"1.0.3",
			"1.0.7", 
			"http://pesiwang.devel.rabbit.oa.com/component.1.0.4.apk",
			"升级详细内容"
		);
		
		new UpgradeManager(this, param, new UpgradeCallback(this)).check();

	}
	
	public void upgradeUIProxy(View view){
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "checkUpgradeProxy");
		
		class MyCallback extends UpgradeCallback {
			
			public MyCallback(Context context){
				super(context);
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
					
					String current	= UpgradeUtil.getCurrentVersionName(getContext());
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
		
		class MyUpgradeManager extends UpgradeManager{
			public MyUpgradeManager(Context context, UpgradeParam param, UpgradeCallback callback){
				super(context, param, callback);
			}
			
			@Override
			protected Dialog createDialog(){
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
		
		class MyCallback extends UpgradeCallback {
			
			public MyCallback(Context context){
				super(context);
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
					
					String current	= UpgradeUtil.getCurrentVersionName(getContext());
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
