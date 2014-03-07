package com.vanchu.module.music;

import java.util.ArrayList;
import java.util.List;

import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;


public class MusicDbManager {
	private static final String	LOG_TAG	= MusicDbManager.class.getSimpleName();
	
	private MusicDbHelper	_dbHelper;
	private SQLiteDatabase	_db;
	
	public MusicDbManager(Context context) {
		_dbHelper	= new MusicDbHelper(context);
		_db			= _dbHelper.getWritableDatabase();
	}
	
	public boolean setMusicData(MusicData md) {
		SwitchLogger.d(LOG_TAG, "call setMusicData with id="+md.getId()
				+",name="+md.getName()+",player mode="+md.getPlayerMode()
				+",audio url="+md.getAudioUrl()+",audio path="+md.getAudioPath()
				+",lyric url="+md.getLyricUrl()+",lyric path="+md.getLyricPath()
				+",img url="+md.getImgUrl());
		
		try {
			long updateTime	= (long)(System.currentTimeMillis() / 1000);
			_db.execSQL("REPLACE INTO " + MusicDbHelper.TABLE_FAVORITE + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 
						new Object[] {md.getId(), md.getName(), md.getArtist(), md.getAlbum(), md.getPlayerMode(), 
										md.getAudioUrl(), md.getAudioPath(), md.getLyricUrl(),
										md.getLyricPath(), md.getImgUrl(), updateTime});
			
			SwitchLogger.d(LOG_TAG, "set update time = " + updateTime);
			return true;
		} catch (SQLException e) {
			SwitchLogger.e(e);
			return false;
		}
	}
	
	private MusicData createMusicDataFromCursor(Cursor c) {
		String id		= c.getString(c.getColumnIndex(MusicDbHelper.TABLE_FAVORITE_COLUMN_ID));
		String name		= c.getString(c.getColumnIndex(MusicDbHelper.TABLE_FAVORITE_COLUMN_NAME));
		String artist	= c.getString(c.getColumnIndex(MusicDbHelper.TABLE_FAVORITE_COLUMN_ARTIST));
		String album	= c.getString(c.getColumnIndex(MusicDbHelper.TABLE_FAVORITE_COLUMN_ALBUM));
		int playerMode	= c.getInt(c.getColumnIndex(MusicDbHelper.TABLE_FAVORITE_COLUMN_PLAYER_MODE));
		String audioUrl	= c.getString(c.getColumnIndex(MusicDbHelper.TABLE_FAVORITE_COLUMN_AUDIO_URL));
		String audioPath	= c.getString(c.getColumnIndex(MusicDbHelper.TABLE_FAVORITE_COLUMN_AUDIO_PATH));
		String lyricUrl		= c.getString(c.getColumnIndex(MusicDbHelper.TABLE_FAVORITE_COLUMN_LYRIC_URL));
		String lyricPath	= c.getString(c.getColumnIndex(MusicDbHelper.TABLE_FAVORITE_COLUMN_LYRIC_PATH));
		String imgUrl		= c.getString(c.getColumnIndex(MusicDbHelper.TABLE_FAVORITE_COLUMN_IMG));
		
		MusicData md	= new MusicData(id, name, artist, album, playerMode, audioUrl, audioPath, lyricUrl, lyricPath, imgUrl);
		return md;
	}
	
	public boolean existMusicData(String id) {
		SwitchLogger.d(LOG_TAG, "call existMusicData with id=" + id);
		
		MusicData md	= getMusicData(id);
		if(null == md) {
			return false;
		} else {
			return true;
		}
	}
	
	public MusicData getMusicData(String id) {
		SwitchLogger.d(LOG_TAG, "call getMusicData with id=" + id);
		
		MusicData md	= null;
		
		try {
			Cursor c	= _db.rawQuery("SELECT * FROM " + MusicDbHelper.TABLE_FAVORITE
						+ " WHERE " + MusicDbHelper.TABLE_FAVORITE_COLUMN_ID + " = ?",
						new String[] {id});
			
			if(c.moveToFirst()){
				md	= createMusicDataFromCursor(c);
			}
			
			if( ! c.isClosed()){
				c.close();
			}
		} catch (SQLException e) {
			SwitchLogger.e(e);
		}
		
		return md;
	}
	
	public boolean deleteMusicData(String id) {
		SwitchLogger.d(LOG_TAG, "call deleteMusicData with id=" + id);
		int result = _db.delete(MusicDbHelper.TABLE_FAVORITE,
								MusicDbHelper.TABLE_FAVORITE_COLUMN_ID + " = ?", new String[] {id});
		
		if(1 == result) {
			return true;
		} else {
			return false;
		}
	}
	
	public List<MusicData> getMusicDataList(int limit, int offset) {
		SwitchLogger.d(LOG_TAG, "call getMusicDataList with limit=" + limit + ",offset=" + offset);
		List<MusicData>	list	= new ArrayList<MusicData>();
		if(limit <= 0 || offset < 0) {
			return list;
		}
		
		try {
			Cursor c	= _db.rawQuery("SELECT * FROM " + MusicDbHelper.TABLE_FAVORITE + " order by " 
										+ MusicDbHelper.TABLE_FAVORITE_COLUMN_UPDATE_TIME + " ASC "
										+ "LIMIT " + limit + " OFFSET " + offset, null);
			if(c.moveToFirst()){
				do {
					MusicData md	= createMusicDataFromCursor(c);
					list.add(md);
				} while(c.moveToNext());
			}
			
			if( ! c.isClosed()){
				c.close();
			}
		} catch (SQLException e) {
			SwitchLogger.e(e);
		}
		
		return list;
	}
	
	public int getDbSize() {
		try {
			String sql	= "SELECT COUNT(*) FROM " + MusicDbHelper.TABLE_FAVORITE;
			SQLiteStatement statement	= _db.compileStatement(sql);
			long dbSize	= statement.simpleQueryForLong();
			return (int)(dbSize);
		} catch (Exception e) {
			SwitchLogger.e(e);
			return 0;
		}
	}
	
	public List<MusicData> getAllMusicData() {
		SwitchLogger.d(LOG_TAG, "call getAllMusicData");
		
		List<MusicData>	list	= new ArrayList<MusicData>();
		try {
			Cursor c	= _db.rawQuery("SELECT * FROM " + MusicDbHelper.TABLE_FAVORITE + " order by " 
										+ MusicDbHelper.TABLE_FAVORITE_COLUMN_UPDATE_TIME + " DESC", null);
			if(c.moveToFirst()){
				do {
					MusicData md	= createMusicDataFromCursor(c);
					list.add(md);
					long updateTime		= c.getLong(c.getColumnIndex(MusicDbHelper.TABLE_FAVORITE_COLUMN_UPDATE_TIME));
					SwitchLogger.d(LOG_TAG, "get update time = " + updateTime);
				} while(c.moveToNext());
			}
			
			if( ! c.isClosed()){
				c.close();
			}
		} catch (SQLException e) {
			SwitchLogger.e(e);
		}
		
		return list;
	}
	
	public void close() {
		_db.close();
		_dbHelper.close();
	}
}
