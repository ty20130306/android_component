package com.vanchu.test;

import com.vanchu.libs.common.ui.FeedbackActivity;
import com.vanchu.libs.common.ui.FilterInputTextWatcher;
import com.vanchu.libs.common.ui.MaxInputTextWatcher;
import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.kvDb.KvDb;
import com.vanchu.libs.kvDb.KvDbCfg;
import com.vanchu.libs.kvDb.MetaData;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class TestKvDbActivity extends Activity {

	private static final String LOG_TAG	= TestKvDbActivity.class.getSimpleName();
	
	private KvDb _kvDb	= null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_kv_db);
		
		EditText et	= (EditText)findViewById(R.id.db_input);
		et.addTextChangedListener(new MaxInputTextWatcher(et, 5, new MaxInputTextWatcher.Callback() {
			
			@Override
			public void onMaxInputReached(int maxLen) {
				Tip.show(TestKvDbActivity.this, "你只能输入"+maxLen+"个字");
			}
			
			@Override
			public void onTextChanged(String currentStr, int maxLen) {
				SwitchLogger.d(LOG_TAG, "current str="+currentStr+",len="+currentStr.length()+",maxLen="+maxLen);
			}
		}));
		
		et.addTextChangedListener(new FilterInputTextWatcher(et, "12 ", new FilterInputTextWatcher.Callback() {
			
			@Override
			public void onFiltered(String filteredStr) {
				Tip.show(TestKvDbActivity.this, "不能输入 "+filteredStr+" 字符");
			}
		}));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_kv_db, menu);
		return true;
	}
	
	private String getInput(int id) {
		EditText et	= (EditText)findViewById(id);
		return et.getText().toString();
	}

	public void selectDb(View v) {
		if(null != _kvDb) {
			_kvDb.close();
		}
		
		String dbName	= getInput(R.id.db_input);
		_kvDb	= new KvDb(this, dbName, new KvDbCfg());
		
		String msg	= "select db " + dbName;
		SwitchLogger.d(LOG_TAG, msg);
		Tip.show(this, msg);
	}
	
	public void getDbSize(View v) {
		if(null == _kvDb) {
			Tip.show(this, "请先选择db" );
			return ;
		}
		
		int dbSize	= _kvDb.getDbSize();
		String msg	= "db " + _kvDb.getDbName() + " size = " + dbSize;
		SwitchLogger.d(LOG_TAG, msg);
		Tip.show(this, msg);
	}
	
	public void setCfg(View v) {
		if(null == _kvDb) {
			Tip.show(this, "请先选择db" );
			return ;
		}
		
		int capacity	= KvDbCfg.CAPACITY_NOT_LIMIT;
		int threshold	= KvDbCfg.LRU_DEFAULT_THRESHOLD;
		String str	= getInput(R.id.capacity_input);
		if( ! str.equals("") ) {
			capacity	= Integer.parseInt(str);
		}
		str	= getInput(R.id.threshold_input);
		if( ! str.equals("") ) {
			threshold	= Integer.parseInt(str);
		}
	
		KvDbCfg cfg	= _kvDb.getCfg();
		cfg.setCapacity(capacity).setLruThreshold(threshold);
		String msg	= "set db cfg, capacity=" + capacity + ",threshold="+threshold;
		SwitchLogger.d(LOG_TAG, msg);
		
		KvDbCfg newCfg	= _kvDb.getCfg();
		msg	= "now db cfg, capacity=" + newCfg.getCapacity() + ",threshold="+newCfg.getLruThreshold();
		SwitchLogger.d(LOG_TAG, msg);
		Tip.show(this, msg);
	}
	
	public void getCfg(View v) {
		if(null == _kvDb) {
			Tip.show(this, "请先选择db" );
			return ;
		}
	
		KvDbCfg cfg	= _kvDb.getCfg();
		String msg	= "get db cfg, capacity=" + cfg.getCapacity() + ",threshold="+cfg.getLruThreshold();
		SwitchLogger.d(LOG_TAG, msg);
		Tip.show(this, msg);
	}
	
	public void ttl(View v) {
		if(null == _kvDb) {
			Tip.show(this, "请先选择db" );
			return ;
		}
		
		String key		= getInput(R.id.key_input);
		
		long timeToLive	= _kvDb.ttl(key);
		
		String msg = "";
		if(MetaData.NEVER_EXPIRE == timeToLive) {
			msg	= "ttl, key=" + key + ", never expire";
		} else {
			msg	= "ttl, key=" + key + ",timeToLive="+timeToLive+" ms";
		}
		
		SwitchLogger.d(LOG_TAG,  msg);
		Tip.show(this, msg);
	}
	
	public void expire(View v) {
		if(null == _kvDb) {
			Tip.show(this, "请先选择db" );
			return ;
		}
		
		String key		= getInput(R.id.key_input);
		String expireStr	= getInput(R.id.expire_input);
		long expire	= MetaData.NEVER_EXPIRE;
		if( ! expireStr.equals("")) {
			expire	= Integer.parseInt(expireStr);
			expire	*= 1000;
		}
		
		_kvDb.expire(key, expire);
		
		String msg	= "expire key=" + key + ",expire="+expire;
		SwitchLogger.d(LOG_TAG,  msg);
		Tip.show(this, msg);
	}
	
	public void set(View v) {
		if(null == _kvDb) {
			Tip.show(this, "请先选择db" );
			return ;
		}
		
		String key		= getInput(R.id.key_input);
		String value	= getInput(R.id.value_input);
		String expireStr	= getInput(R.id.expire_input);
		long expire	= MetaData.NEVER_EXPIRE;
		if( ! expireStr.equals("")) {
			expire	= Integer.parseInt(expireStr);
			expire	*= 1000;
		}
		
		_kvDb.set(key, value, expire);
		
		String msg	= "set key=" + key +",value="+value+",expire="+expire;
		SwitchLogger.d(LOG_TAG,  msg);
		Tip.show(this, msg);
	}
	
	public void setValueNull(View v) {
		if(null == _kvDb) {
			Tip.show(this, "请先选择db" );
			return ;
		}
		
		String key		= getInput(R.id.key_input);
		String expireStr	= getInput(R.id.expire_input);
		long expire	= MetaData.NEVER_EXPIRE;
		if( ! expireStr.equals("")) {
			expire	= Integer.parseInt(expireStr);
			expire	*= 1000;
		}
		
		_kvDb.set(key, null, expire);
		
		String msg	= "set key=" + key +",value=null,expire="+expire;
		SwitchLogger.d(LOG_TAG,  msg);
		Tip.show(this, msg);
	}

	public void get(View v) {
		if(null == _kvDb) {
			Tip.show(this, "请先选择db" );
			return ;
		}
		
		String key		= getInput(R.id.key_input);
		String value	= _kvDb.get(key);
		
		if(null == value) {
			SwitchLogger.e(LOG_TAG, "value is null");
		}
		
		String msg	= "get key=" + key +",value="+value;
		SwitchLogger.d(LOG_TAG, msg );
		Tip.show(this, msg);
	}

	public void delete(View v) {
		if(null == _kvDb) {
			Tip.show(this, "请先选择db" );
			return ;
		}
		
		String key		= getInput(R.id.key_input);
		_kvDb.delete(key);
		
		String msg	=  "delete key=" + key;
		SwitchLogger.d(LOG_TAG, msg);
		Tip.show(this, msg);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_BACK:
			setResult(RESULT_OK);
			finish();
			break;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
