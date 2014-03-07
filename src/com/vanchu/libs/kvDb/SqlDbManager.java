package com.vanchu.libs.kvDb;


import com.vanchu.libs.common.util.SwitchLogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class SqlDbManager {
	private SqlDbHelper		_dbHelper;
	private SQLiteDatabase	_db;
	
	public SqlDbManager(Context context, String dbName) {
		_dbHelper	= new SqlDbHelper(context, dbName);
		_db			= _dbHelper.getWritableDatabase();
	}
	
	public boolean updateTouchTime(String key) {
		ContentValues cv	= new ContentValues();
		cv.put(SqlDbHelper.COLUMN_TOUCH_TIME, new Long(System.currentTimeMillis()));
		int num	= _db.update(SqlDbHelper.TABLE_NAME, cv, SqlDbHelper.COLUMN_KEY+"=?", new String[]{key});
		if(1 == num) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean updateExpireAndTouchTime(String key, long expire) {
		ContentValues cv	= new ContentValues();
		cv.put(SqlDbHelper.COLUMN_EXPIRE, new Long(expire));
		cv.put(SqlDbHelper.COLUMN_TOUCH_TIME, new Long(System.currentTimeMillis()));
		
		int num	= _db.update(SqlDbHelper.TABLE_NAME, cv, SqlDbHelper.COLUMN_KEY+"=?", new String[]{key});
		if(1 == num) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean set(MetaData md) {
		try {
			long currentTime	= System.currentTimeMillis();
			MetaData oldMd		= get(md.getKey());
			long createTime		= currentTime;
			if(oldMd.exist()) {
				createTime	= oldMd.getCreateTime();
			}
			_db.execSQL("REPLACE INTO " + SqlDbHelper.TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?)", 
						new Object[] {md.getKey(), md.getValue(), md.getExpire(), currentTime,
										currentTime, createTime});
			
			return true;
		} catch (SQLException e) {
			SwitchLogger.e(e);
			return false;
		}
	}
	
	public MetaData get(String key) {
		MetaData md		= new MetaData(key);
		
		try {
			Cursor c	= _db.rawQuery("SELECT * FROM " + SqlDbHelper.TABLE_NAME
										+ " WHERE " + SqlDbHelper.COLUMN_KEY + " = ?",
										new String[] {key});
			
			if(c.moveToFirst()){
				String value	= c.getString(c.getColumnIndex(SqlDbHelper.COLUMN_VALUE));
				md.setValue(value);
				
				long expire		= c.getLong(c.getColumnIndex(SqlDbHelper.COLUMN_EXPIRE));
				md.setExpire(expire);
				
				long touchTime	= c.getLong(c.getColumnIndex(SqlDbHelper.COLUMN_TOUCH_TIME));
				md.setTouchTime(touchTime);
				
				long updateTime	= c.getLong(c.getColumnIndex(SqlDbHelper.COLUMN_UPDATE_TIME));
				md.setUpdateTime(updateTime);
				
				long createTime	= c.getLong(c.getColumnIndex(SqlDbHelper.COLUMN_CREATE_TIME));
				md.setCreateTime(createTime);
				
				md.setExist(true);
			}
			
			if( ! c.isClosed()){
				c.close();
			}
		} catch (SQLException e) {
			SwitchLogger.e(e);
		}
		
		return md;
	}
	
	public boolean delete(String key) {
		int result = _db.delete(SqlDbHelper.TABLE_NAME, SqlDbHelper.COLUMN_KEY + " = ?", new String[] {key});

		if(1 == result) {
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteAll() {
		 _db.delete(SqlDbHelper.TABLE_NAME, null, null);
	}
	
	public int getDbSize() {
		try {
			String sql	= "SELECT COUNT(*) FROM " + SqlDbHelper.TABLE_NAME;
			SQLiteStatement statement	= _db.compileStatement(sql);
			long dbSize	= statement.simpleQueryForLong();
			return (int)(dbSize);
		} catch (Exception e) {
			SwitchLogger.e(e);
			return 0;
		}
	}
	
	public void deleteLruKey(int num) {
		try {	
			String sql	= "DELETE FROM " + SqlDbHelper.TABLE_NAME
							+ " WHERE " + SqlDbHelper.COLUMN_KEY + " IN("
							+ " SELECT " + SqlDbHelper.COLUMN_KEY + " FROM " + SqlDbHelper.TABLE_NAME
							+ " ORDER BY " + SqlDbHelper.COLUMN_TOUCH_TIME + " ASC"
							+ " LIMIT " + String.valueOf(num) + " )";
							
			SQLiteStatement statement	= _db.compileStatement(sql);
			statement.execute();
		} catch (Exception e) {
			SwitchLogger.e(e);
		}
	}
	
	public void close() {
		_db.close();
		_dbHelper.close();
	}
	
}
