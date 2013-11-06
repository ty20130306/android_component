package com.vanchu.sample;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.test.R;
import com.vanchu.test.R.layout;
import com.vanchu.test.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class DbActivity extends Activity {

	private static final String	LOG_TAG	= DbActivity.class.getSimpleName();
	
	private EditText	_idEditText;
	private EditText	_versionEditText;
	private TextView	_dbView;
	private DbManager	_dbMananger;
	
	private String		_id;
	private String		_version;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_db);
		
		_idEditText	= (EditText)findViewById(R.id.id);
		_versionEditText	= (EditText)findViewById(R.id.version);
		_dbView	= (TextView)findViewById(R.id.db_view);
		_dbMananger	= new DbManager(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.db, menu);
		return true;
	}
	
	private void getInput() {
		_id			= _idEditText.getText().toString();
		_version	= _versionEditText.getText().toString();
	}

	private void updateOutput() {
		Map<String, String> idVersionMap	= _dbMananger.getAllPluginVersion();
		String result	= "";
		Iterator<Entry<String, String>>	iter	= idVersionMap.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, String> entry	= iter.next();
			result	+= entry.getKey() + ", " + entry.getValue() + "\n";
		}
		_dbView.setText(result);
	}
	
	public void insert(View v) {
		getInput();		
		_dbMananger.setPluginVersion(_id, _version);
		SwitchLogger.d(LOG_TAG, "insert id="+_id+",version="+_version);
		updateOutput();
	}
	
	public void delete(View v) {
		getInput();
		boolean result	= _dbMananger.deletePluginVersion(_id);
		SwitchLogger.d(LOG_TAG, "delete id="+_id+",result="+String.valueOf(result));
		updateOutput();
	}
	
	public void select(View v) {
		getInput();
		String version	= _dbMananger.getPluginVersion(_id);
		SwitchLogger.d(LOG_TAG, "select id="+_id+",version="+version);
		updateOutput();
	}
	
	public void update(View v) {
		getInput();
		String version	= _dbMananger.getPluginVersion(_id);
		SwitchLogger.d(LOG_TAG, "old id="+_id+",version="+version);
		_dbMananger.setPluginVersion(_id, _version);
		version	= _dbMananger.getPluginVersion(_id);
		SwitchLogger.d(LOG_TAG, "new id="+_id+",version="+version);
		updateOutput();
	}
	
	public void show(View v) {
		updateOutput();
	}
}
