package com.vanchu.libs.kvDb;

import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class SqlDbHelper extends SQLiteOpenHelper {
	
	public static final String	TABLE_NAME		= "nosql";
	
	public static final String	COLUMN_KEY		= "key";
	public static final String	COLUMN_VALUE	= "value";
	public static final String	COLUMN_EXPIRE	= "expire";
	public static final String	COLUMN_TOUCH_TIME	= "touch_time";
	public static final String	COLUMN_UPDATE_TIME	= "update_time";
	public static final String	COLUMN_CREATE_TIME	= "create_time";
	
	private static final String	LOG_TAG		= SqlDbHelper.class.getSimpleName();
	
	private static final int	DB_VERSION	= 1;
	
	public SqlDbHelper(Context context, String dbName, CursorFactory factory, int version) {
		super(context, dbName, factory, version);
	}
	
	public SqlDbHelper(Context context, String dbName) {
		this(context, dbName, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		SwitchLogger.d(LOG_TAG, "onCreate() called");
		
		String sql	= "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
						+ " (" + COLUMN_KEY + " TEXT PRIMARY KEY NOT NULL UNIQUE, "
						+ COLUMN_VALUE + " TEXT, "
						+ COLUMN_EXPIRE + " INTEGER, "
						+ COLUMN_TOUCH_TIME + " INTEGER, "
						+ COLUMN_UPDATE_TIME + " INTEGER, "
						+ COLUMN_CREATE_TIME + " INTEGER)";
		
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		SwitchLogger.d(LOG_TAG, "onUpgrade() called, oldVersion=" + oldVersion + ", newVersion=" + newVersion);
	}
}
