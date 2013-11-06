package com.vanchu.module.music;

import java.util.List;
import android.media.MediaPlayer;
import com.vanchu.libs.common.util.NetUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.music.MusicService;
import com.vanchu.libs.music.MusicServiceCallback;

public class SceneMusicService extends MusicService {
	private static final String LOG_TAG	= SceneMusicService.class.getSimpleName();
	
	public static final int	ERR_NO_OFFLINE_MUSIC				= 1;
	public static final int	ERR_REQUEST_LIST_FAIL				= 2;
	public static final int	ERR_REQUEST_URL_NOT_SET				= 3;
	public static final int	ERR_FETCH_ONLINE_MUSIC_DATA_FAIL	= 4;
	public static final int	ERR_INIT_NOT_SUCC					= 5;

	private static final int MUSIC_BUFFERING		= 1;
	private static final int MUSIC_BUFFER_DONE		= 2;
	private int	_bufferStatus				= MUSIC_BUFFER_DONE;
	
	private String _requestUrl			= null;
	private MusicSceneMgr	_musicSceneMgr	= null;
	
	private MusicData	_currentMusicData	= null;
	private boolean		_initSucc			= false;

	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	public void setRequestUrl(String requestUrl) {
		_requestUrl		= requestUrl;
	}
	
	public void setMusicServiceCallback(MusicServiceCallback callback) {
		_callback	= callback;
	}
	
	public void setCurrentSceneType(int sceneType) {
		_musicSceneMgr.setCurrentSceneType(sceneType);
	}
	
	public int getCurrentSceneType() {
		return _musicSceneMgr.getCurrentSceneType();
	}
	
	public interface InitMusicSceneMgrCallback {
		public void onMusicSceneInit(boolean succ, List<MusicSceneInfo> list);
		public void onPreloadStatusChanged(MusicScene ms, int currentStatus);
		public void onQueueSizeChanged(MusicScene ms, int currentQueueSize);
	}
	
	public void initMusicSceneMgr(String sceneCfgUrl, final InitMusicSceneMgrCallback initSceneMgrCallback) {
		_musicSceneMgr	= new MusicSceneMgr(this, _requestUrl);
		_musicSceneMgr.init(sceneCfgUrl, new MusicSceneMgr.InitCallback() {
			
			@Override
			public void onSucc() {
				_initSucc	= true;
				_musicSceneMgr.setMusicSceneMgrCallback(new MusicSceneMgrCallback() {
					@Override
					public void onPreloadStatusChanged(MusicScene ms, int currentStatus) {
						if(null != initSceneMgrCallback) {
							initSceneMgrCallback.onPreloadStatusChanged(ms, currentStatus);
						}
					}
					
					@Override
					public void onQueueSizeChanged(MusicScene ms, int currentQueueSize) {
						if(null != initSceneMgrCallback) {
							initSceneMgrCallback.onQueueSizeChanged(ms, currentQueueSize);
						}
					}
				});
				
				if(null != initSceneMgrCallback) {
					List<MusicSceneInfo> list	= _musicSceneMgr.getMusicSceneInfoList();
					initSceneMgrCallback.onMusicSceneInit(true, list);
				}
			}
			
			@Override
			public void onFail() {
				_initSucc	= false;
				if(null != initSceneMgrCallback) {
					initSceneMgrCallback.onMusicSceneInit(false, null);
				}
			}
		});
	}
	
	private void doPlaySmartMusic() {
		int networkType	= NetUtil.getNetworkType(this);
		if(NetUtil.NETWORK_TYPE_WIFI == networkType) {
			checkAndPlayOnlineMusic();
		} else if (NetUtil.NETWORK_TYPE_INVALID == networkType) {
			checkAndPlayOfflineMusic();
		} else {
			if(_musicSceneMgr.getCurrentQueueSize() > 0 
				|| _musicSceneMgr.getQueueSize(MusicSceneMgr.SCENE_TYPE_DEFAULT) > 0) 
			{
				checkAndPlayOfflineMusic();
			} else {
				checkAndPlayOnlineMusic();
			}
		}
	}
	
	public void playSmartMusic() {
		if(continueToPlay()) {
			return ;
		}
		
		doPlaySmartMusic();
	}
	
	public void nextSmartMusic() {
		doPlaySmartMusic();
	}
	
	public void playOnlineMusic() {
		checkAndPlayOnlineMusic();
	}
	
	public void nextOnlineMusic() {
		checkAndPlayOnlineMusic();
	}
	
	public void playOfflineMusic() {
		checkAndPlayOfflineMusic();
	}
	
	public void nextOfflineMusic() {
		checkAndPlayOfflineMusic();
	}
	
	public List<MusicSceneInfo> getMusicSceneList() {
		if( ! _initSucc) {
			return null;
		}
		
		List<MusicSceneInfo> list	= _musicSceneMgr.getMusicSceneInfoList();
		return list;
	}
	
	public void likeMusic() {
		if(null == _currentMusicData) {
			SwitchLogger.e(LOG_TAG, "current music data is null");
			return ;
		}
		
		MusicDbManager dbManager	= new MusicDbManager(this);
		dbManager.setMusicData(_currentMusicData);
		dbManager.close();
	}
	
	public boolean isFavoriteMusic() {
		if(null == _currentMusicData) {
			SwitchLogger.e(LOG_TAG, "current music data is null");
			return false;
		}
		
		MusicDbManager dbManager	= new MusicDbManager(this);
		boolean exist	= dbManager.existMusicData(_currentMusicData.getId());
		dbManager.close();
		
		return exist;
	}
	
	public void dislikeMusic() {
		if(null == _currentMusicData) {
			SwitchLogger.e(LOG_TAG, "current music data is null");
			return ;
		}
		
		MusicDbManager dbManager	= new MusicDbManager(this);
		dbManager.deleteMusicData(_currentMusicData.getId());
		dbManager.close();
	}
	
	public MusicData getCurrentMusicData() {
		return _currentMusicData;
	}
	
	public boolean isMusicAvailable() {
		if(NetUtil.NETWORK_TYPE_INVALID != NetUtil.getNetworkType(this) ) {
			return true;
		}
		
		if(_musicSceneMgr.getCurrentQueueSize() > 0) {
			return true;
		}
		
		return false;
	}
	
	public void preload(int sceneType, int num) {
		_musicSceneMgr.preload(sceneType, num);
	}

	public int getMaxQueueSize(int sceneType) {
		return _musicSceneMgr.getMaxQueueSize(sceneType);
	}
	
	public int getQueueSize(int sceneType) {
		return _musicSceneMgr.getQueueSize(sceneType);
	}
	
	private void checkAndPlayOnlineMusic() {
		if( ! _initSucc) {
			if(null != _callback) {
				_callback.onError(ERR_INIT_NOT_SUCC);
			}
			
			SwitchLogger.e(LOG_TAG, "init not succ, check it" );
			return;
		}
		
		_musicSceneMgr.nextOnlineMusicData(new MusicSceneMgr.NextOnlineCallback() {
			
			@Override
			public void onDone(MusicData data) {
				if(null == data) {
					if(null != _callback) {
						_callback.onError(ERR_FETCH_ONLINE_MUSIC_DATA_FAIL);
					}
					SwitchLogger.e(LOG_TAG, "nextOnlineMusicData fetch fail, data is null" );
					return ;
				}
				
				SwitchLogger.d(LOG_TAG, "get next online music data succ, name="+data.getName()
										+ ",id="+data.getId());
				_currentMusicData	= data;
				int result	= playOnlineMusic(_currentMusicData.getAudioUrl());
				if(PLAY_FAIL_PREPARING == result) {
					SwitchLogger.e(LOG_TAG, "online music is preparing, play failed");
					return ;
				}
			}
		});
	}
	
	private void checkAndPlayOfflineMusic() {
		// check if offline music available
		if(_musicSceneMgr.getCurrentQueueSize() <= 0) {
			SwitchLogger.d(LOG_TAG, "current scene type " + _musicSceneMgr.getCurrentSceneType() 
									+ " do not have offline music, try default scene");
			_musicSceneMgr.setCurrentSceneType(MusicSceneMgr.SCENE_TYPE_DEFAULT);
			if(_musicSceneMgr.getCurrentQueueSize() <= 0) {
				if(null != _callback) {
					SwitchLogger.d(LOG_TAG, "no next offline music data" );
					_callback.onError(ERR_NO_OFFLINE_MUSIC);
				}
				return ;
			}
		}
		
		// offline music available, rand it
		int retryMax	= 3;
		int retryCnt	= 0;
		String currentPath	= getCurrentMusicPath();
		String nextPath		= null;
		MusicData musicData	= null;
		do {
			musicData	= _musicSceneMgr.nextOfflineMusicData();
			if(null == musicData) {
				SwitchLogger.d(LOG_TAG, "no next offline music data" );
				_callback.onError(ERR_NO_OFFLINE_MUSIC);
				return ;
			}
			nextPath	= musicData.getAudioPath();
			++retryCnt;
		} while(null != currentPath && currentPath.equals(nextPath) && retryCnt < retryMax);
		
		_currentMusicData	= musicData;
		currentPath	= nextPath;
		SwitchLogger.d(LOG_TAG, "current offline music path change, now="+currentPath);
		playOfflineMusic(currentPath);
	}
	
	@Override
	protected void onMusicBuffering(MediaPlayer mp, int percent) {
		if(100 == percent) {
			if(MUSIC_BUFFERING == _bufferStatus) {
				SwitchLogger.d(LOG_TAG, "buffering music done" );
				_bufferStatus	= MUSIC_BUFFER_DONE;
				
				if( ! _musicSceneMgr.continueToPreload()) {
					// 如果当前没有正在预加载，尝试检查当前场景是否需要更新缓存数据
					int currentSceneType	= _musicSceneMgr.getCurrentSceneType();
					_musicSceneMgr.preload(currentSceneType, _musicSceneMgr.getMaxQueueSize(currentSceneType));
				}
			}
		} else {
			SwitchLogger.d(LOG_TAG, "music buffering, percent="+percent);
			_bufferStatus	= MUSIC_BUFFERING;
			_musicSceneMgr.pausePreloading();
		}
	}
	
	@Override
	protected void onMusicPrepared(MediaPlayer mp) {
		
	}
	
	@Override
	protected void onMusicCompletion(MediaPlayer mp) {
		SwitchLogger.d(LOG_TAG, "onMusicCompletion");
		int networkType	= NetUtil.getNetworkType(this);
		
		if((NetUtil.NETWORK_TYPE_2G == networkType || NetUtil.NETWORK_TYPE_3G == networkType) 
			&& MusicService.PLAYER_DETAIL_MODE_2G3G == getPlayerDetailMode()) 
		{
			playOnlineMusic();
		} else {
			playSmartMusic();
		}
	}
}
