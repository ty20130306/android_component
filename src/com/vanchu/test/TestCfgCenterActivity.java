package com.vanchu.test;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanchu.libs.cfgCenter.CfgCenter;
import com.vanchu.libs.common.util.SwitchLogger;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class TestCfgCenterActivity extends Activity {

	private static final String LOG_TAG	= TestCfgCenterActivity.class.getSimpleName();
	
	private CfgCenter	_cfgCenter	= null;
	private static final String TEST_CFG_URL	= "http://pesiwang.devel.rabbit.oa.com/test_cfg_center.php";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_cfg_center);
		_cfgCenter	= new CfgCenter(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_cfg_center, menu);
		return true;
	}
	
	public void testGet(View v) {
		SwitchLogger.d(LOG_TAG, "CfgCenter testGet");
		_cfgCenter.get(TEST_CFG_URL, new CfgCenter.GetCallback() {
			
			@Override
			public JSONObject onResponse(String url, String response) {
				SwitchLogger.d(LOG_TAG, "CfgCenter.GetCallback.onResponse called, url="+url);
				if(null == response) {
					SwitchLogger.d(LOG_TAG, "cfg response is null");
					return null;
				}
				
				SwitchLogger.d(LOG_TAG, "cfg response =" + response);
				try {
					return new JSONObject(response);
				} catch (JSONException e) {
					SwitchLogger.e(e);
					return null;
				}
			}
			
			@Override
			public void onSucc(String url, boolean latest, JSONObject cfg, JSONObject oldCfg) {
				SwitchLogger.d(LOG_TAG, "CfgCenter.GetCallback.onSucc called,url="+url);
				if(latest) {
					SwitchLogger.d(LOG_TAG, "cfg is latest");
					SwitchLogger.d(LOG_TAG, "cfg =" + cfg.toString());
					if(null != oldCfg) {
						SwitchLogger.d(LOG_TAG, "old cfg =" + cfg.toString());
					} else {
						SwitchLogger.d(LOG_TAG, "old cfg is null");
					}
				} else {
					SwitchLogger.d(LOG_TAG, "cfg is old");
					SwitchLogger.d(LOG_TAG, "old cfg =" + cfg.toString());
				}
			}
			
			@Override
			public void onFail(String url) {
				SwitchLogger.d(LOG_TAG, "CfgCenter.GetCallback.onFail called,url=" + url);
			}
		});
	}

	public void testGetLocal(View v) {
		SwitchLogger.d(LOG_TAG, "CfgCenter testGetLocal");
		JSONObject localCfg	= _cfgCenter.getLocal(TEST_CFG_URL);
		if(null == localCfg) {
			SwitchLogger.d(LOG_TAG, "CfgCenter.getLocal return null");
			return ;
		}
		
		SwitchLogger.d(LOG_TAG, "local cfg =" + localCfg.toString());
	}
	
	public void testRemoveLocal(View v) {
		SwitchLogger.d(LOG_TAG, "CfgCenter testRemoveLocal");
		_cfgCenter.removeLocal(TEST_CFG_URL);
	}
}
