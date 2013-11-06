package com.vanchu.module.music;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import android.content.Context;
import com.vanchu.libs.common.util.SwitchLogger;

public class MusicFavoriteScene extends MusicScene {
	
	private static final String LOG_TAG	= MusicFavoriteScene.class.getSimpleName();
	
	public MusicFavoriteScene(Context context, int sceneType, int maxQueueSize) {
		super(context, sceneType, null, maxQueueSize);
	}
	
	private MusicInfo createMusicInfoFromData(MusicData md) {
		String id		= md.getId();
		String name		= md.getName();
		String artist	= md.getArtist();
		String album	= md.getAlbum();
		String audioUrl	= md.getAudioUrl();
		String lyricUrl	= md.getLyricUrl();
		String imgUrl	= md.getImgUrl();
		
		SwitchLogger.d(LOG_TAG, "createMusicInfoFromData with id="+md.getId()
				+",name="+md.getName()+",player mode="+md.getPlayerMode()
				+",audio url="+md.getAudioUrl()+",audio path="+md.getAudioPath()
				+",lyric url="+md.getLyricUrl()+",lyric path="+md.getLyricPath()
				+",img url="+md.getImgUrl());
		return new MusicInfo(id, name, audioUrl, imgUrl, artist, album, lyricUrl);
	}
	
	@Override
	protected void slimMusicInfoList() {
		MusicDbManager dbManager	= new MusicDbManager(_context);
		synchronized (_musicInfoList) {
			int i = 0;
			while(i < _musicInfoList.size()) {
				MusicInfo info	= _musicInfoList.get(i);
				if(dbManager.existMusicData(info.getId())) {
					++i;
				} else {
					_musicInfoList.remove(i);
				}
			}
			
			_currentInfoIndex	= INDEX_NONE;
		}
		dbManager.close();
		
		SwitchLogger.d(LOG_TAG, "sync music info list, now list size=" + _musicInfoList.size()
								+ ", _currentInfoIndex=" + _currentInfoIndex);
	}
	
	@Override
	public void nextOnlineMusicInfo(OnlineCallback callback) {
		MusicDbManager dbManager	= new MusicDbManager(_context);
		synchronized (_musicInfoList) {
			// get from memory
			while(0 <= _currentInfoIndex + 1 && _currentInfoIndex + 1 < _musicInfoList.size()) {
				_currentInfoIndex += 1;
				MusicInfo info	= _musicInfoList.get(_currentInfoIndex);
				if(dbManager.existMusicData(info.getId())) {
					if(null != callback) {
						callback.onDone(info);
					}
					
					dbManager.close();
					return ;
				}
			}
		}
		
		dbManager.close();
		
		getInfoListFromSource(callback);
	}
	
	@Override
	protected void getInfoListFromSource(final OnlineCallback callback) {
		MusicDbManager dbManager	= new MusicDbManager(_context);
		//int limit	= _eachFetchNum;
		int limit	= 2;
		int offset	= _currentInfoIndex;
		if(offset < 0) {
			offset	= 0;
		}
		
		List<MusicData> musicDataList	= dbManager.getMusicDataList(limit, offset);
		if(musicDataList.size() <= 0) {
			SwitchLogger.d(LOG_TAG, "for info, get data list from sqlite, list is empty, limit="+limit+",offset="+offset);
			slimMusicInfoList();
			MusicInfo info	= null;
			synchronized (_musicInfoList) {
				if(0 <= _currentInfoIndex + 1 && _currentInfoIndex + 1 < _musicInfoList.size()) {
					_currentInfoIndex	+= 1;
					info	= _musicInfoList.get(_currentInfoIndex);
				}
			}
			
			if(null != callback) {
				callback.onDone(info);
			}
			
			dbManager.close();
			return ;
		}
		
		synchronized (_musicInfoList) {
			MusicInfo info	= null;
			SwitchLogger.d(LOG_TAG, "for info, get data from sqlite,size="+musicDataList.size()+"-------------------");
			for(int i = 0; i < musicDataList.size(); ++i) {
				info	= createMusicInfoFromData(musicDataList.get(i));
				_musicInfoList.add(info);
			}
			
			info	= null;
			if(0 <= _currentInfoIndex + 1 && _currentInfoIndex + 1 < _musicInfoList.size()) {
				_currentInfoIndex += 1;
				info	= _musicInfoList.get(_currentInfoIndex);
			}

			if(null != callback) {
				callback.onDone(info);
			}
		}
		
		dbManager.close();
	}
	
	private List<String> createIdListFromSolidQueue() {
		List<String> idList	= new ArrayList<String>();
		
		LinkedList<MusicSolidQueueElement> queue	= _musicSolidQueue.getQueue();
		for(int i = 0; i < queue.size(); ++i) {
			idList.add(queue.get(i).getId());
		}
		
		return idList;
	}
	
	@Override
	protected void getDownloadListFromSource(final int fetchNum, final GetDownloadListCallback callback) {
		MusicDbManager dbManager	= new MusicDbManager(_context);

		List<MusicData> musicDataList	= dbManager.getAllMusicData();
		if(musicDataList.size() <= 0) {
			SwitchLogger.d(LOG_TAG, "for download, get all music data list from sqlite, list is empty");
			
			if(null != callback) {
				callback.onFail();
			}
			
			dbManager.close();
			return ;
		}
		
		synchronized (_downloadList) {
			int size	= musicDataList.size();
			List<String> downloadedIdList	= createIdListFromSolidQueue();
			SwitchLogger.d(LOG_TAG, "for download, get data from sqlite,size="+size+"-------------------");
			int cnt		= 0;
			for(int i = 0; i < size; ++i) {
				MusicData data	= musicDataList.get(i);
				if( ! downloadedIdList.contains(data.getId())) {
					MusicInfo info	= createMusicInfoFromData(data);
					_downloadList.add(info);
					++cnt;
					if(cnt >= fetchNum) {
						break;
					}
				}
			}
			
			if(null != callback) {
				if(cnt > 0) {
					callback.onSucc();
				} else {
					callback.onFail();
				}
			}
		}
		
		dbManager.close();
	}
}
