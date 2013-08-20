package com.vanchu.sample;

import java.util.HashMap;
import java.util.Map;

import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DbManager {
	
	private static final String	LOG_TAG	= DbManager.class.getSimpleName();
	
	private DbHelper	_dbHelper;
	private SQLiteDatabase			_db;
	
	public DbManager(Context context) {
		_dbHelper	= new DbHelper(context);
		_db			= _dbHelper.getWritableDatabase();
	}
	
	public void setPluginVersion(String pluginId, String version) {
		SwitchLogger.d(LOG_TAG, "call setPluginVersion with pluginId=" + pluginId + ", version=" + version);
		
		try {
			_db.execSQL("REPLACE INTO " + DbHelper.TABLE_PLUGIN_VERSION + " VALUES (?, ?)", 
						new Object[] {pluginId, version});
			
		} catch (SQLException e) {
			SwitchLogger.e(e);
		}
	}
	
	public String getPluginVersion(String pluginId) {
		SwitchLogger.d(LOG_TAG, "call getPluginVersion with pluginId=" + pluginId);
		String	version	= null;
		
		try {
			Cursor c	= _db.rawQuery("SELECT * FROM " + DbHelper.TABLE_PLUGIN_VERSION 
						+ " WHERE " + DbHelper.TABLE_PLUGIN_VERSION_COLUMN_ID + " = ?",
						new String[] {pluginId});
			
			if(c.moveToFirst()){
				version	= c.getString(c.getColumnIndex(DbHelper.TABLE_PLUGIN_VERSION_COLUMN_VERSION));
			}
			
			if( ! c.isClosed()){
				c.close();
			}
		} catch (SQLException e) {
			SwitchLogger.e(e);
		}
		
		return version;
	}
	
	public Map<String, String> getAllPluginVersion() {
		SwitchLogger.d(LOG_TAG, "call getPluginVersion with pluginIdList");
		
		HashMap<String, String>	versionMap	= new HashMap<String, String>();
		
		try {
			Cursor c	= _db.rawQuery("SELECT * FROM " + DbHelper.TABLE_PLUGIN_VERSION, null);
			if(c.moveToFirst()){
				do {
					String id		= c.getString(c.getColumnIndex(DbHelper.TABLE_PLUGIN_VERSION_COLUMN_ID));
					String version	= c.getString(c.getColumnIndex(DbHelper.TABLE_PLUGIN_VERSION_COLUMN_VERSION));
					SwitchLogger.d(LOG_TAG, "get " + id + ", " + version);
					versionMap.put(id, version);
				} while(c.moveToNext());
			}
			
			if( ! c.isClosed()){
				c.close();
			}
		} catch (SQLException e) {
			SwitchLogger.e(e);
		}
		
		return versionMap;
	}
}
