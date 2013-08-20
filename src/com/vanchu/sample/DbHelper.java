package com.vanchu.sample;

import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DbHelper extends SQLiteOpenHelper {
	
	public static final String	TABLE_PLUGIN_VERSION				= "plugin_version";
	public static final String	TABLE_PLUGIN_VERSION_COLUMN_ID		= "id";
	public static final String	TABLE_PLUGIN_VERSION_COLUMN_VERSION	= "version";
	
	private static final String	LOG_TAG		= DbHelper.class.getSimpleName();
	
	private static final int	DB_VERSION			= 1;
	private static final String	DB_NAME				= "db_plugin_system";	
	
	public DbHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
	
	public DbHelper(Context context) {
		this(context, DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		SwitchLogger.d(LOG_TAG, "onCreate() called");
		
		// 创建playlist
		String	sql = "CREATE TABLE IF NOT EXISTS "
						+ TABLE_PLUGIN_VERSION
						+ "(id TEXT PRIMARY KEY NOT NULL UNIQUE, version TEXT)";
		
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		SwitchLogger.d(LOG_TAG, "onUpgrade() called, oldVersion=" + oldVersion + ", newVersion=" + newVersion);
	}
}
