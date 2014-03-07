package com.vanchu.module.music;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.vanchu.libs.common.container.SolidQueue;
import com.vanchu.libs.common.task.CfgMgr;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.music.MusicService;

public class MusicSceneMgr {
	
	private static final String LOG_TAG	= MusicSceneMgr.class.getSimpleName();
	
	public static final int SCENE_TYPE_NONE			= -1;
	public static final int SCENE_TYPE_NEWEST		= 1;
	public static final int SCENE_TYPE_DEFAULT		= 2;
	public static final int SCENE_TYPE_FAVORITE		= 3;
	
	public static final int DEFAULT_SCENE_MIN_QUEUE_SIZE	= 10;
	
	private static final String PREFS_MUSIC_SCENE_MGR		= "music_scene_mgr";
	private static final String PREFS_KEY_CURRENT_SCENE_TYPE	= "current_scene_type";
	private static final String PREFS_KEY_MAX_QUEUE_SIZE		= "max_queue_size";
	
	
	private MusicSceneMgrCallback		_callback	= null;
	
	private Context	_context;
	private String	_requestUrl;
	
	private CfgMgr	_cfgMgr;
	private Map<Integer, MusicSceneCfg>	_typeCfgMap;
	private Map<Integer, MusicScene>	_typeSceneMap;
	private Map<Integer, Integer>		_typeMaxQueueSizeMap;
	private LinkedList<MusicScene>		_downloadMusicSceneList;
	private SharedPreferences			_prefs;
	private int		_currentSceneType;
	
	public MusicSceneMgr(Context context, String requestUrl) {
		_context		= context;
		_requestUrl		= requestUrl;
		
		_cfgMgr		= CfgMgr.getInstance(_context.getApplicationContext());
		_typeCfgMap		= new HashMap<Integer, MusicSceneCfg>();
		_typeSceneMap	= new LinkedHashMap<Integer, MusicScene>();
		_typeMaxQueueSizeMap	= new HashMap<Integer, Integer>();
		_downloadMusicSceneList	= new LinkedList<MusicScene>();
		
		_prefs	= _context.getSharedPreferences(PREFS_MUSIC_SCENE_MGR, Context.MODE_PRIVATE);
		_currentSceneType	= _prefs.getInt(PREFS_KEY_CURRENT_SCENE_TYPE, SCENE_TYPE_DEFAULT);
	}
	
	private void fetchTypeMaxQueueSizeMap() {
		Iterator<Entry<Integer, MusicSceneCfg>>	iter	= _typeCfgMap.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<Integer, MusicSceneCfg> entry	= iter.next();
			Integer typeObj	= entry.getKey();
			if(null == typeObj) {
				continue;
			}
			
			int maxQueueSize	= _prefs.getInt(PREFS_KEY_MAX_QUEUE_SIZE+"_"+typeObj.toString(), 0);
			_typeMaxQueueSizeMap.put(typeObj, new Integer(maxQueueSize) );
			SwitchLogger.d(LOG_TAG, "fetch, type " + typeObj.toString() + " max queue size is " + maxQueueSize);
		}
	}
	
	private void saveTypeMaxQueueSizeMap() {
		Iterator<Entry<Integer, Integer>> iter	= _typeMaxQueueSizeMap.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<Integer, Integer> entry	= iter.next();
			Integer typeObj		= entry.getKey();
			if(null == typeObj) {
				continue;
			}
			
			Integer valueObj	= entry.getValue();
			if(null == valueObj) {
				continue;
			}
			
			int maxQueueSize	= valueObj.intValue();
			_prefs.edit().putInt(PREFS_KEY_MAX_QUEUE_SIZE+"_"+typeObj.toString(), maxQueueSize).commit();
			
			SwitchLogger.d(LOG_TAG, "save, type " + typeObj.toString() + " max queue size is " + maxQueueSize);
		}
	}
 	
	public void init(String sceneCfgUrl, final InitCallback initCallback) {
		_cfgMgr.get(sceneCfgUrl, new CfgMgr.GetCallback() {
			
			@Override
			public JSONObject onResponse(String url, String response) {
				try {
					SwitchLogger.d(LOG_TAG, "CfgCenter.GetCallback.onResponse, response="+response);
					JSONObject jsonObj	= new JSONObject(response);
					return jsonObj;
				} catch (JSONException e) {
					SwitchLogger.e(e);
					return null;
				}
			}
			
			@Override
			public void onSucc(String url, boolean latest, JSONObject cfg, JSONObject oldCfg) {
				try {
					SwitchLogger.d(LOG_TAG, "CfgCenter.GetCallback.onSucc, cfg="+cfg.toString());
					
					JSONArray	data	= cfg.getJSONArray("data");
					for(int i = 0; i < data.length(); ++i) {
						JSONObject s	= data.getJSONObject(i);
						int type	= s.getInt("type");
						String name	= s.getString("name");
						MusicSceneCfg msc	= new MusicSceneCfg(type, name);	
						_typeCfgMap.put(new Integer(type), msc);
					}
					
					if(latest) {
						handleCfgUpdate(oldCfg);
					}
					fetchTypeMaxQueueSizeMap();
					initMusicScene();
					initCallback.onSucc();
				} catch (JSONException e) {
					SwitchLogger.e(e);
					initCallback.onFail();
				}
			}
			
			@Override
			public void onFail(String url) {
				SwitchLogger.d(LOG_TAG, "CfgCenter.GetCallback.onFail, url="+url);
				initCallback.onFail();
			}
		});
	}
	
	private boolean inDownloadMusicSceneList(MusicScene ms) {
		for(int i = 0; i < _downloadMusicSceneList.size(); ++i) {
			if(ms.getSceneType() == _downloadMusicSceneList.get(i).getSceneType()) {
				return true;
			}
		}
		
		return false;
	}
	
	public List<MusicSceneInfo> getMusicSceneInfoList() {
		synchronized (_downloadMusicSceneList) {
			List<MusicSceneInfo> list	= new ArrayList<MusicSceneInfo>();
			Iterator<Entry<Integer, MusicSceneCfg>>	iter	= _typeCfgMap.entrySet().iterator();
			while(iter.hasNext()) {
				Entry<Integer, MusicSceneCfg> entry	= iter.next();
				Integer typeObj	= entry.getKey();
				if(null == typeObj) {
					continue;
				}

				MusicScene	musicScene	= _typeSceneMap.get(typeObj);
				if(null == musicScene) {
					continue;
				}
				int maxQueueSize	= musicScene.getMaxQueueSize();

				boolean isPreloading	= false;
				if(inDownloadMusicSceneList(musicScene)) {
					isPreloading	= true;
				}
				SwitchLogger.d(LOG_TAG, "type " + musicScene.getSceneType() + ", isPreloading="+isPreloading);
				MusicSceneCfg cfg	= entry.getValue();
				int type	= cfg.getType();
				String name	= cfg.getName();

				int queueSize	= musicScene.getQueueSize();
				MusicSceneInfo	info	= new MusicSceneInfo(type, name, queueSize, maxQueueSize, isPreloading);
				list.add(info);
			}

			return list;
		}
	}
	
	public void setMusicSceneMgrCallback(MusicSceneMgrCallback callback) {
		_callback	= callback;
	}
	
	private void initMusicScene() {
		Iterator<Entry<Integer, MusicSceneCfg>>	iter	= _typeCfgMap.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<Integer, MusicSceneCfg> entry	= iter.next();
			Integer typeObj	= entry.getKey();
			if(null == typeObj) {
				continue;
			}
			
			if(_typeSceneMap.containsKey(typeObj)) {
				continue;
			}
			
			int sceneType	= typeObj.intValue();
			
			Integer maxSizeObj	= _typeMaxQueueSizeMap.get(typeObj);
			if(null == maxSizeObj) {
				continue;
			}
			int maxQueueSize	= maxSizeObj.intValue();
			if(SCENE_TYPE_DEFAULT == sceneType && maxQueueSize < DEFAULT_SCENE_MIN_QUEUE_SIZE) {
				maxQueueSize	= DEFAULT_SCENE_MIN_QUEUE_SIZE;
			}
			
			MusicScene ms	= null;
			if(SCENE_TYPE_FAVORITE == sceneType) {
				ms	= new MusicFavoriteScene(_context, sceneType, maxQueueSize);
			} else {
				ms	= new MusicScene(_context, sceneType, _requestUrl, maxQueueSize);
			}
			ms.setMusicSceneCallback(new MusicSceneCallback(){
				@Override
				public void onPreloadStatusChanged(MusicScene ms, int currentStatus) {
					synchronized (_downloadMusicSceneList) {
						if(MusicScene.DOWNLOAD_STATUS_STOPPED == currentStatus) {
							if(_downloadMusicSceneList.size() > 0) {
								MusicScene lastMusicScene	= _downloadMusicSceneList.getLast();
								if(lastMusicScene.getSceneType() == ms.getSceneType()) {
									_downloadMusicSceneList.removeLast();
								}
							}
							
							if(_downloadMusicSceneList.size() > 0) {
								MusicScene nextMusicScene	= _downloadMusicSceneList.getLast();
								nextMusicScene.preload();
								SwitchLogger.d(LOG_TAG, "musicScene of type " + ms.getSceneType()
										+ " download finished, start next one, next type="
										+ nextMusicScene.getSceneType());
							} else {
								SwitchLogger.d(LOG_TAG, "all music scene download finished");
							}
						}
						
						if(null != _callback) {
							_callback.onPreloadStatusChanged(ms, currentStatus);
						}
					}
				}
				
				@Override
				public void onQueueSizeChanged(MusicScene ms, int currentQueueSize) {
					if(null != _callback) {
						_callback.onQueueSizeChanged(ms, currentQueueSize);
					}
				}
			});
			_typeSceneMap.put(typeObj, ms);
		}
	}
	
	private void handleCfgUpdate(JSONObject oldCfg) {
		Map<Integer, MusicSceneCfg>	oldTypeCfgMap	= new HashMap<Integer, MusicSceneCfg>();
		
		if(null == oldCfg) {
			return ;
		}
		
		try {
			JSONArray data	= oldCfg.getJSONArray("data");
			for(int i = 0; i < data.length(); ++i) {
				JSONObject s	= data.getJSONObject(i);
				int type	= s.getInt("type");
				String name	= s.getString("name");
				MusicSceneCfg msc	= new MusicSceneCfg(type, name);	
				oldTypeCfgMap.put(new Integer(type), msc);
			}
		} catch (JSONException e) {
			SwitchLogger.e(e);
			return ;
		}
		
		Iterator<Entry<Integer, MusicSceneCfg>>	iter	= oldTypeCfgMap.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<Integer, MusicSceneCfg> entry	= iter.next();
			Integer key	= entry.getKey();
			if(null != key && ! _typeCfgMap.containsKey(key)) {
				MusicScene	ms	= new MusicScene(_context, key.intValue(), "", 0);
				ms.clearCache();
			}
		}
	}
	
	public int getCurrentSceneType() {
		return _currentSceneType;
	}
	
	public void setCurrentSceneType(int sceneType) {
		SwitchLogger.d(LOG_TAG, "change current type from " + _currentSceneType + " to " + sceneType);
		_currentSceneType	= sceneType;
		_prefs.edit().putInt(PREFS_KEY_CURRENT_SCENE_TYPE, _currentSceneType).commit();
	}
	
	private void setMaxQueueSize(int sceneType, int maxSize) {
		MusicScene ms	= _typeSceneMap.get(new Integer(sceneType));
		if(null == ms) {
			return ;
		}
		
		if(SCENE_TYPE_DEFAULT == sceneType && maxSize < DEFAULT_SCENE_MIN_QUEUE_SIZE) {
			maxSize	= DEFAULT_SCENE_MIN_QUEUE_SIZE;
		}
		
		ms.setMaxQueueSize(maxSize);
		_typeMaxQueueSizeMap.put(new Integer(sceneType), new Integer(maxSize));
		saveTypeMaxQueueSizeMap();
		
		SwitchLogger.d(LOG_TAG, "set type " + sceneType + " max queue size to " + maxSize);
	}
	
	public int getCurrentPreloadingType() {
		if(_downloadMusicSceneList.size() <= 0) {
			return MusicSceneMgr.SCENE_TYPE_NONE;
		}
	
		return _downloadMusicSceneList.getLast().getSceneType();
	}
	
	public int getMaxQueueSize(int sceneType) {
		MusicScene ms	= _typeSceneMap.get(new Integer(sceneType));
		if(null == ms) {
			return 0;
		}
		
		return ms.getMaxQueueSize();
	}
	
	public int getCurrentQueueSize() {
		return getQueueSize(_currentSceneType);
	}
	
	public SolidQueue<MusicSolidQueueElement> getCurrentSolidQueue() {
		MusicScene ms	= _typeSceneMap.get(new Integer(_currentSceneType));
		if(null == ms) {
			return null;
		}
		
		return ms.getSolidQueue();
	}
	
	public int getQueueSize(int sceneType) {
		MusicScene ms	= _typeSceneMap.get(new Integer(sceneType));
		if(null == ms) {
			return 0;
		}
		
		return ms.getQueueSize();
	}
	
	public static interface InitCallback {
		public void onSucc();
		public void onFail();
	}
	
	public void nextOnlineMusicData(final NextOnlineCallback callback) {
		MusicScene ms	= _typeSceneMap.get(new Integer(_currentSceneType));
		if(null == ms) {
			/**
			 * 当场景配置出现变化，导致某种类型的配置被删除，
			 * 且刚好当前场景处于此配置时将会出现此类情况，尝试自动切换到默认场景
			 */
			SwitchLogger.d(LOG_TAG, "get music scene of " + _currentSceneType + " fail, try default scene type" );
			setCurrentSceneType(SCENE_TYPE_DEFAULT);
			ms	= _typeSceneMap.get(new Integer(_currentSceneType));
			if(null == ms) {
				SwitchLogger.e(LOG_TAG, "get music scene of " + _currentSceneType + " fail");
				callback.onDone(null);
				return ;
			}
		}
		
		ms.nextOnlineMusicInfo(new MusicScene.OnlineCallback() {
			
			@Override
			public void onDone(MusicInfo mi) {
				if(null == mi) {
					callback.onDone(null);
					return ;
				}

				String id		= mi.getId();
				String name		= mi.getName();
				String artist	= mi.getArtist();
				String album	= mi.getAlbum();
				String audioUrl	= mi.getAudio();
				String lyricUrl	= mi.getLyric();
				String imgUrl	= mi.getImg();
				MusicData md	= new MusicData(id, name, artist, album, MusicService.PLAYER_MODE_ONLINE, audioUrl, "", lyricUrl, "", imgUrl);
				callback.onDone(md);
			}
		});
	}
	
	/**
	 * @return offline music data, null if no one or error happens
	 */
	public MusicData nextOfflineMusicData() {
		MusicScene ms	= _typeSceneMap.get(new Integer(_currentSceneType));
		if(null == ms) {
			/**
			 * 当场景配置出现变化，导致某种类型的配置被删除，
			 * 且刚好当前场景处于此配置时将会出现此类情况，尝试自动切换到默认场景
			 */
			SwitchLogger.d(LOG_TAG, "get music scene of " + _currentSceneType + " fail, try default scene type" );
			setCurrentSceneType(SCENE_TYPE_DEFAULT);
			ms	= _typeSceneMap.get(new Integer(_currentSceneType));
			if(null == ms) {
				SwitchLogger.e(LOG_TAG, "get music scene of " + _currentSceneType + " fail");
				return null;
			}
		}
		
		MusicSolidQueueElement msqe	= ms.nextOfflineMusicElement();
		if(null == msqe) {
			return null;
		}
		
		String id			= msqe.getId();
		String name			= msqe.getName();
		String artist		= msqe.getArtist();
		String album		= msqe.getAlbum();
		String audio		= msqe.getAudio();
		String audioPath	= msqe.getAudioPath();
		String lyric		= msqe.getLyric();
		String lyricPath	= msqe.getLyricPath();
		String imgUrl		= msqe.getImg();
		return new MusicData(id, name, artist, album, MusicService.PLAYER_MODE_OFFLINE, audio, audioPath, lyric, lyricPath, imgUrl);
	}
	
	public interface NextOnlineCallback {
		
		/**
		 * @param data music data, null if fail
		 */
		public void onDone(MusicData data);
	}
	
	public void preload(int sceneType, int num) {
		SwitchLogger.d(LOG_TAG, "preload music, scene type=" + sceneType + ",num="+num );
		synchronized (_downloadMusicSceneList) {
			MusicScene ms	= _typeSceneMap.get(new Integer(sceneType));
			if(null == ms) {
				SwitchLogger.e(LOG_TAG, "get music scene fail, null return, scene type="+sceneType);
				return ;
			}
			
			// stop preloading
			if(num <= 0) {
				setMaxQueueSize(sceneType, 0);
				ms.stopPreloading();
				SwitchLogger.e(LOG_TAG, "num <= 0, clear preloading music, scene type="+sceneType+",num="+num);
				return ;
			}
			
			setMaxQueueSize(sceneType, num);
			if( ! inDownloadMusicSceneList(ms) ) {
				_downloadMusicSceneList.addFirst(ms);
			}

			if(_downloadMusicSceneList.size() >= 2) {
				MusicScene lastMusicScene	= _downloadMusicSceneList.getLast();
				SwitchLogger.e(LOG_TAG, "scene type " + lastMusicScene.getSceneType() + " is preloading, wait for it");
			} else {
				ms.preload();
			}
		}
	}
	
	public boolean pausePreloading() {
		synchronized (_downloadMusicSceneList) {
			if(_downloadMusicSceneList.size() > 0) {
				MusicScene ms	= _downloadMusicSceneList.getLast();
				ms.pausePreloading();
				
				return true;
			}
			
			return false;
		}
	}
	
	public boolean continueToPreload() {
		synchronized (_downloadMusicSceneList) {
			if(_downloadMusicSceneList.size() > 0) {
				MusicScene ms	= _downloadMusicSceneList.getLast();
				ms.preload();
				
				return true;
			}
			
			return false;
		}
	}
}
