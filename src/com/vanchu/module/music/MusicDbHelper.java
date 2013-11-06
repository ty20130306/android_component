package com.vanchu.module.music;

import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class MusicDbHelper extends SQLiteOpenHelper {
	private static final String	LOG_TAG		= MusicDbHelper.class.getSimpleName();
	
	public static final String	TABLE_FAVORITE					= "favorite";
	public static final String	TABLE_FAVORITE_COLUMN_ID		= "id";
	public static final String	TABLE_FAVORITE_COLUMN_NAME		= "name";
	public static final String	TABLE_FAVORITE_COLUMN_ARTIST	= "artist";
	public static final String	TABLE_FAVORITE_COLUMN_ALBUM		= "album";
	public static final String	TABLE_FAVORITE_COLUMN_PLAYER_MODE	= "player_mode";
	public static final String	TABLE_FAVORITE_COLUMN_AUDIO_URL		= "audio_url";
	public static final String	TABLE_FAVORITE_COLUMN_AUDIO_PATH	= "audio_path";
	public static final String	TABLE_FAVORITE_COLUMN_LYRIC_URL		= "lyric_url";
	public static final String	TABLE_FAVORITE_COLUMN_LYRIC_PATH	= "lyric_path";
	public static final String	TABLE_FAVORITE_COLUMN_IMG			= "img";
	public static final String	TABLE_FAVORITE_COLUMN_UPDATE_TIME	= "update_time";
	
	private static final int	DB_VERSION			= 1;
	private static final String	DB_NAME				= "db_music";	
	
	public MusicDbHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
	
	public MusicDbHelper(Context context) {
		this(context, DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		SwitchLogger.d(LOG_TAG, "onCreate() called");
		
		String	sql = "CREATE TABLE IF NOT EXISTS "
						+ TABLE_FAVORITE
						+ "(" + TABLE_FAVORITE_COLUMN_ID + " TEXT PRIMARY KEY NOT NULL UNIQUE, "
						+ TABLE_FAVORITE_COLUMN_NAME + " TEXT, "
						+ TABLE_FAVORITE_COLUMN_ARTIST + " TEXT, "
						+ TABLE_FAVORITE_COLUMN_ALBUM + " TEXT, "
						+ TABLE_FAVORITE_COLUMN_PLAYER_MODE + " INTEGER, "
						+ TABLE_FAVORITE_COLUMN_AUDIO_URL + " TEXT, "
						+ TABLE_FAVORITE_COLUMN_AUDIO_PATH + " TEXT, "
						+ TABLE_FAVORITE_COLUMN_LYRIC_URL + " TEXT, "
						+ TABLE_FAVORITE_COLUMN_LYRIC_PATH + " TEXT, "
						+ TABLE_FAVORITE_COLUMN_IMG + " TEXT, "
						+ TABLE_FAVORITE_COLUMN_UPDATE_TIME + " INTEGER)";

		db.execSQL(sql);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		SwitchLogger.d(LOG_TAG, "onUpgrade() called, oldVersion=" + oldVersion + ", newVersion=" + newVersion);
	}
}
