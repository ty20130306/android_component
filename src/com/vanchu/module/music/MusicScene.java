package com.vanchu.module.music;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;

import com.vanchu.libs.common.container.SolidQueue;
import com.vanchu.libs.common.task.Downloader;
import com.vanchu.libs.common.util.NetUtil;
import com.vanchu.libs.common.util.SwitchLogger;

public class MusicScene {
	
	private static final String LOG_TAG	= MusicScene.class.getSimpleName();
	
	public static final int DOWNLOAD_STATUS_STOPPED		= 1;
	public static final int DOWNLOAD_STATUS_RUNNING		= 2;
	public static final int DOWNLOAD_STATUS_PAUSED		= 3;
	
	private static final String PREFS_MUSIC_SCENE		= "music_scene";
	private static final String	MUSIC_QUEUE_NAME		= "music_queue_scene";

	protected static final int INDEX_NONE		= -1;
	
	private static final int REQUEST_MUSIC_INFO_SUCC	= 0;
	private static final int REQUEST_MUSIC_INFO_FAIL	= 1;
	
	private static final int DEFAULT_EACH_FETCH_NUM		= 10;
	
	private static final int MUSIC_INFO_LIST_MAX_SIZE	= 20;
	
	protected int	_currentInfoIndex			= INDEX_NONE;
	protected List<MusicInfo> _musicInfoList	= new ArrayList<MusicInfo>();
	
	protected SolidQueue<MusicSolidQueueElement>	_musicSolidQueue	= null;
	
	protected int	_eachFetchNum		= DEFAULT_EACH_FETCH_NUM;
	
	private int	_downloadStatus			= DOWNLOAD_STATUS_STOPPED;
	private Downloader	_audioDownloader	= null;
	protected List<MusicInfo> _downloadList	= new ArrayList<MusicInfo>();
	
	private boolean	_enableUpdate		= true;
	
	private MusicSceneCallback	_callback	= null;
	
	private int _offlineListenedNum;
	
	private SharedPreferences _prefs;
	
	protected Context	_context;
	private int		_sceneType;
	private String	_requestUrl;
	private int		_maxQueueSize;
	private String	_musicDownloadDir;
	
	public MusicScene(Context context, int sceneType, String requestUrl, int maxQueueSize) {
		_context	= context;
		_sceneType	= sceneType;
		_requestUrl	= requestUrl;
		_maxQueueSize	= maxQueueSize;
		
		_musicDownloadDir	= "music_scene_" + String.valueOf(_sceneType); 
				
		String musicQueueName	= MUSIC_QUEUE_NAME + "_" + String.valueOf(_sceneType);
		_musicSolidQueue	= new SolidQueue<MusicSolidQueueElement>(_context, 
										musicQueueName, _maxQueueSize, new MusicSolidQueueCallback());
		
		_prefs	= _context.getSharedPreferences(PREFS_MUSIC_SCENE, Context.MODE_PRIVATE);
		
		_offlineListenedNum	= _prefs.getInt(getOfflineListenedNumKey(), 0);
	}
	
	public int getSceneType() {
		return _sceneType;
	}
	
	public void clearCache() {
		Editor editor	= _prefs.edit();
		editor.remove(getNewbieFlagKey());
		editor.remove(getOfflineListenedNumKey());
		editor.commit();
		
		int oldQueueSize	= getQueueSize();
		_musicSolidQueue.setMaxSize(0);
		_musicSolidQueue.solidify();
		int newQueueSize	= getQueueSize();
		if(oldQueueSize != newQueueSize && null != _callback) {
			_callback.onQueueSizeChanged(this, newQueueSize);
		}
	}
	
	private String getOfflineListenedNumKey() {
		return "music_offline_listened_num_" + String.valueOf(_sceneType);
	}

	private String getNewbieFlagKey() {
		return "music_newbie_" + String.valueOf(_sceneType);
	}
	
	private String getAndUpdateNewbieFlag() {
		String key	= getNewbieFlagKey();
		String newbieFlag	= _prefs.getString(key, "1");
		_prefs.edit().putString(key, "0").commit();
		
		return newbieFlag;
	}
	
	private List<MusicInfo> parseResponse(JSONObject response) {
		List<MusicInfo> infoList	= new ArrayList<MusicInfo>();
		
		try {
			JSONArray dataList	= response.getJSONObject("musics").getJSONArray("data");
			
			for(int i = 0; i < dataList.length(); ++i ) {
				JSONObject	info	= dataList.getJSONObject(i);
				String id		= info.getString("id");
				String name		= info.getString("name");
				String audio	= info.getString("audio");
				String img		= info.getString("img");
				String artist	= info.getString("artist");
				String album	= info.getString("album");
				String lyric	= info.getString("lyric");
				MusicInfo mi	= new MusicInfo(id, name, audio, img, artist, album, lyric);
				infoList.add(mi);
			}
		} catch( JSONException e) {
			SwitchLogger.e(e);
		}
		
		return infoList;
	}
	
	public void enableUpdate(boolean enabled) {
		_enableUpdate	= enabled;
	}
	
	public void setMaxQueueSize(int maxSize) {
		int oldQueueSize	= getQueueSize();
		_maxQueueSize	= maxSize;
		_musicSolidQueue.setMaxSize(_maxQueueSize);
		_musicSolidQueue.solidify();
		int newQueueSize	= getQueueSize();
		if(oldQueueSize != newQueueSize && null != _callback) {
			_callback.onQueueSizeChanged(this, newQueueSize);
		}
	}
	
	public int getMaxQueueSize() {
		return _maxQueueSize;
	}
	
	public int getQueueSize() {
		return _musicSolidQueue.size();
	}
	
	public SolidQueue<MusicSolidQueueElement> getSolidQueue() {
		return _musicSolidQueue;
	}
	
	public void setMusicSceneCallback(MusicSceneCallback callback) {
		_callback	= callback;
	}
	
	public MusicSolidQueueElement nextOfflineMusicElement() {
		if(_musicSolidQueue.size() <= 0) {
			return null;
		}
		
		LinkedList<MusicSolidQueueElement>	list	= _musicSolidQueue.getQueue();
		int listSize	= list.size();
		MusicSolidQueueElement element	= null;
		SwitchLogger.d(LOG_TAG, "nextOfflineMusicElement, listSize=" + listSize
								+ ",_offlineListenedNum=" + _offlineListenedNum);
		if(listSize <= _offlineListenedNum) {
			Random rand	= new Random();
			int i	= rand.nextInt(listSize);
			element	= list.get(i);
		} else {
			int j	= listSize - _offlineListenedNum - 1;
			element	= list.get(j);
			if(_enableUpdate) {
				_offlineListenedNum += 1;
				SwitchLogger.d(LOG_TAG, "_offlineListenedNum += 1, now _offlineListenedNum="+_offlineListenedNum);
				_prefs.edit().putInt(getOfflineListenedNumKey(), _offlineListenedNum).commit();
			}
		}
		
		return element;
	}
	
	public static String downloadStatusStr(int status) {
		switch (status) {
		case DOWNLOAD_STATUS_PAUSED:
			return "paused";

		case DOWNLOAD_STATUS_RUNNING:
			return "running";
			
		case DOWNLOAD_STATUS_STOPPED:
			return "stopped";
			
		default:
			break;
		}
		
		return "unknown";
	}
	
	private void changeDownloadStatus(int newStatus) {
		SwitchLogger.d(LOG_TAG, "download status change from " + downloadStatusStr(_downloadStatus) 
								+ " to " + downloadStatusStr(newStatus) );
		_downloadStatus	= newStatus;
		if(null != _callback) {
			_callback.onPreloadStatusChanged(this, _downloadStatus);
		}
	}
	
	private void downloadAudio() {
		SwitchLogger.d(LOG_TAG, "begin to download audio");
		
		MusicInfo info	= null;
		synchronized (_downloadList) {
			int fetchNum	= getFetchNum();
			if(fetchNum <= 0) {
				SwitchLogger.d(LOG_TAG, "nothing more to preload, preload finished");
				changeDownloadStatus(DOWNLOAD_STATUS_STOPPED);
				return ;
			}

			if(DOWNLOAD_STATUS_PAUSED == _downloadStatus) {
				SwitchLogger.d(LOG_TAG, "preload paused");
				_audioDownloader	= null;
				return ;
			}
			
			if(_downloadList.size() <= 0) {
				SwitchLogger.d(LOG_TAG, " _downloadList is empty now, need to get download list again");
				getDownloadListFromSource(fetchNum, new GetDownloadListCallback());
				return ;
			}
			
			info	= _downloadList.get(0);
			_downloadList.remove(0);
		}
		
		String audioUrl	= info.getAudio();
		SwitchLogger.d(LOG_TAG, "downloading audio, url="+audioUrl);
		_audioDownloader	= new Downloader(_context, audioUrl, _musicDownloadDir, new AudioDownloadListener(this, info));
		_audioDownloader.run();
	}
	
	private class AudioDownloadListener implements Downloader.IDownloadListener {
		
		private MusicScene ms;
		private MusicInfo mi;

		public AudioDownloadListener(MusicScene musicScene, MusicInfo musicInfo) {
			ms	= musicScene;
			mi	= musicInfo;
		}
		
		@Override
		public void onStart() {
			SwitchLogger.d(LOG_TAG, "download audio from network started" );
		}
		
		@Override
		public void onProgress(long downloaded, long total) {
			//SwitchLogger.d(LOG_TAG, "download audio progress:" + downloaded + "/" + total);
		}
		
		@Override
		public void onSuccess(String downloadFile) {
			SwitchLogger.d(LOG_TAG, "download audio succ, downloadFile:" + downloadFile);
			String lyric	= mi.getLyric();
			MusicSolidQueueElement element	= new MusicSolidQueueElement(mi.getId(), mi.getName(), 
															mi.getAudio(), mi.getImg(), mi.getArtist(), 
															mi.getAlbum(), lyric, downloadFile);
			_musicSolidQueue.enqueue(element);
			if(_enableUpdate) {
				if(_musicSolidQueue.size() >= _maxQueueSize && _offlineListenedNum > 0) {
					_offlineListenedNum	-= 1;
					SwitchLogger.d(LOG_TAG, "_offlineListenedNum -= 1, now _offlineListenedNum="
											+ _offlineListenedNum);
					_prefs.edit().putInt(getOfflineListenedNumKey(), _offlineListenedNum).commit();
				}
			}
			
			if(null != _callback) {
				_callback.onQueueSizeChanged(ms, ms.getQueueSize());
			}
			
			if(null != lyric && ! lyric.equals("")) {
				new Downloader(_context, lyric, _musicDownloadDir, new LyricDownloadListener(mi.getId())).run();
			} else {
				downloadAudio();
			}
		}
		
		@Override
		public void onError(int errCode) {
			SwitchLogger.e(LOG_TAG, "download audio fail, errCode="+errCode);
			
			// if fail, continue to try next one
			downloadAudio();
		}
		
		@Override
		public void onPause() {
			
		}
	}
	
	private class LyricDownloadListener implements Downloader.IDownloadListener {
		
		private String musicId;
		
		public LyricDownloadListener(String id) {
			musicId		= id;
		}
		
		@Override
		public void onStart() {
			SwitchLogger.d(LOG_TAG, "download lyric from network started" );
		}
		
		@Override
		public void onProgress(long downloaded, long total) {
			//SwitchLogger.d(LOG_TAG, "download lyric progress:" + downloaded + "/" + total);
		}
		
		private void updateLyricPath(String id, String lyricPath) {
			LinkedList<MusicSolidQueueElement>	list	= _musicSolidQueue.getQueue();
			for(int i = 0; i < list.size(); ++i) {
				MusicSolidQueueElement msqe	= list.get(i);
				if(null != id && id.equals(msqe.getId())) {
					msqe.setLyricPath(lyricPath);
					_musicSolidQueue.solidify();
					SwitchLogger.d(LOG_TAG, "update lyric path succ,id="+id+",path="+lyricPath);
					return ;
				}
			}
			
			SwitchLogger.d(LOG_TAG, "update lyric path fail, can not find id="+id+",path="+lyricPath);
		}
		
		@Override
		public void onSuccess(String downloadFile) {
			SwitchLogger.d(LOG_TAG, "download lyric succ, downloadFile:" + downloadFile);
			updateLyricPath(musicId, downloadFile);
			downloadAudio();
		}
		
		@Override
		public void onError(int errCode) {
			SwitchLogger.e(LOG_TAG, "download lyric fail, errCode="+errCode);
			
			// if fail, continue to try next one
			downloadAudio();
		}
		
		@Override
		public void onPause() {
			
		}
	}
	
	public int getDownloadStatus() {
		return _downloadStatus;
	}
	
	public void pausePreloading() {
		SwitchLogger.d(LOG_TAG, "begin to pause preloading" );
		synchronized (_downloadList) {
			changeDownloadStatus(DOWNLOAD_STATUS_PAUSED);
			if(null != _audioDownloader && Downloader.DOWNLOAD_STATUS_RUNNING == _audioDownloader.getStatus()) {
				_audioDownloader.pause();
			}
		}
	}
	
	public void stopPreloading() {
		SwitchLogger.d(LOG_TAG, "stop preloading" );
		synchronized (_downloadList) {
			if(null != _audioDownloader && Downloader.DOWNLOAD_STATUS_RUNNING == _audioDownloader.getStatus()) {
				_audioDownloader.pause();
				_audioDownloader	= null;
			}
			
			_downloadList	= new ArrayList<MusicInfo>();
			changeDownloadStatus(DOWNLOAD_STATUS_STOPPED);
		}
	}
	
	protected void getDownloadListFromSource(final int fetchNum, final GetDownloadListCallback callback) {
		final Handler handler	= new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case REQUEST_MUSIC_INFO_SUCC :
					callback.onSucc();
					break;

				case REQUEST_MUSIC_INFO_FAIL :
					callback.onFail();
					break;
				default:
					break;
				}
			}
		};
		
		new Thread(){
			@Override
			public void run() {
				HashMap<String, String> params	= new HashMap<String, String>();
				params.put("newbie", getAndUpdateNewbieFlag());
				params.put("num", String.valueOf(fetchNum));
				params.put("type", String.valueOf(_sceneType));
				
				String response	= NetUtil.httpPostRequest(_requestUrl, params, 3);
				SwitchLogger.d(LOG_TAG, "response=" + response);

				if(null == response){
					handler.sendEmptyMessage(REQUEST_MUSIC_INFO_FAIL);
					return ;
				}
				
				JSONObject responseJson	= null;
				try {
					responseJson	= new JSONObject(response);
				} catch( JSONException e) {
					SwitchLogger.e(e);
					handler.sendEmptyMessage(REQUEST_MUSIC_INFO_FAIL);
					return ;
				}
				
				List<MusicInfo> list	= parseResponse(responseJson);
				if(list.size() <= 0) {
					handler.sendEmptyMessage(REQUEST_MUSIC_INFO_FAIL);
				} else {
					synchronized (_downloadList) {
						for(int i = 0; i < list.size(); ++i) {
							_downloadList.add(list.get(i));
						}
					}
					
					handler.sendEmptyMessage(REQUEST_MUSIC_INFO_SUCC);
				}
			}
		}.start();
	}
	
	protected class GetDownloadListCallback {
		public void onSucc() {
			SwitchLogger.d(LOG_TAG, "get download list succ");
			downloadAudio();
		}
		
		public void onFail() {
			SwitchLogger.d(LOG_TAG, "get download list fail");
			synchronized (_downloadList) {
				changeDownloadStatus(DOWNLOAD_STATUS_STOPPED);
			}
		}
	}
	
	private int getFetchNum() {
		int fetchNum	= _maxQueueSize - _musicSolidQueue.size() + _offlineListenedNum;
		SwitchLogger.d(LOG_TAG, "_maxQueueSize="+_maxQueueSize+",solidQueueSize="+_musicSolidQueue.size()
								+",_offlineListenedNum="+_offlineListenedNum+",fetchNum="+fetchNum);
		
		return fetchNum;
	}
	
	public void preload() {
		int fetchNum	= 0;
		synchronized (_downloadList) {
			if(DOWNLOAD_STATUS_PAUSED == _downloadStatus && null != _audioDownloader) {
				SwitchLogger.d(LOG_TAG, "preload paused, continue to download");
				changeDownloadStatus(DOWNLOAD_STATUS_RUNNING);
				_audioDownloader.run();
				
				return ;
			}
			
			if(DOWNLOAD_STATUS_RUNNING == _downloadStatus) {
				SwitchLogger.d(LOG_TAG, "preload downloading");
				return ;
			}

			fetchNum	= getFetchNum();
			if(fetchNum <= 0) { 
				// 减少预加载数量的情况
				SwitchLogger.d(LOG_TAG, "no need to preload, _maxQueueSize=" + _maxQueueSize 
										+ ", solid queue size=" + _musicSolidQueue.size()
										+ ",_offlineListenedNum=" + _offlineListenedNum);
				
				/*
				 * 需要通知music scene mgr从预加载队列中移除这个场景，
				 * 如果队列不为空，启动下一个音乐场景的预加载 
				 */
				changeDownloadStatus(DOWNLOAD_STATUS_STOPPED);
				return ;
			}
			
			changeDownloadStatus(DOWNLOAD_STATUS_RUNNING);
		}

		getDownloadListFromSource(fetchNum, new GetDownloadListCallback());
	}
	
	protected void slimMusicInfoList() {
		synchronized (_musicInfoList) {
			int cnt	= 0;
			while(_musicInfoList.size() > MUSIC_INFO_LIST_MAX_SIZE) {
				_musicInfoList.remove(0);
				++cnt;
			}
			
			_currentInfoIndex	-=	cnt;
			if(_currentInfoIndex < 0) {
				_currentInfoIndex	= INDEX_NONE;
			}
			
			SwitchLogger.d(LOG_TAG, "slim music info list, reduce " + cnt 
									+ ", now list size=" + _musicInfoList.size()
									+ ", current index=" + _currentInfoIndex);
		}
	}
	
	protected void getInfoListFromSource(final OnlineCallback callback) {
		// get from network
		if(null == _requestUrl) {
			if(null != callback) {
				callback.onDone(null);
			}
			SwitchLogger.e(LOG_TAG, "request url is null, please set it first" );
			return ;
		}

		final Handler handler	= new Handler(){
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case REQUEST_MUSIC_INFO_SUCC :
					synchronized (_musicInfoList) {
						List<MusicInfo> infoList	= parseResponse((JSONObject)msg.obj);
						for(int i = 0; i < infoList.size(); ++i) {
							_musicInfoList.add(infoList.get(i));
						}
						
						slimMusicInfoList();
						
						MusicInfo info	= null;
						if(0 <= _currentInfoIndex + 1 && _currentInfoIndex + 1 < _musicInfoList.size()) {
							_currentInfoIndex += 1;
							info	= _musicInfoList.get(_currentInfoIndex);
						}

						if(null != callback) {
							callback.onDone(info);
						}
					}
					break;

				case REQUEST_MUSIC_INFO_FAIL :
					SwitchLogger.e(LOG_TAG, "request music info list fail" );
					if(null != callback) {
						callback.onDone(null);
					}
					break;
				default:
					break;
				}
			}
		};

		new Thread(){
			@Override
			public void run() {
				HashMap<String, String> params	= new HashMap<String, String>();
				params.put("newbie", getAndUpdateNewbieFlag());
				params.put("num", String.valueOf(_eachFetchNum));
				params.put("sceneType", String.valueOf(_sceneType));
				
				String response	= NetUtil.httpPostRequest(_requestUrl, params, 3);
				SwitchLogger.d(LOG_TAG, "response=" + response);

				if(response == null){
					handler.sendEmptyMessage(REQUEST_MUSIC_INFO_FAIL);
					return ;
				}

				JSONObject responseJson	= null;
				try {
					responseJson	= new JSONObject(response);
				} catch( JSONException e) {
					SwitchLogger.e(e);
					handler.sendEmptyMessage(REQUEST_MUSIC_INFO_FAIL);
					return ;
				}

				handler.obtainMessage(REQUEST_MUSIC_INFO_SUCC, responseJson).sendToTarget();
			}
		}.start();	
	}
	
	public void nextOnlineMusicInfo(final OnlineCallback callback) {
		
		synchronized (_musicInfoList) {
			// get from memory
			if(0 <= _currentInfoIndex + 1 && _currentInfoIndex + 1 < _musicInfoList.size()) {
				if(null != callback) {
					_currentInfoIndex += 1;
					callback.onDone(_musicInfoList.get(_currentInfoIndex));
				}
				
				return ;
			}
		}
		
		getInfoListFromSource(callback);
	}
	
	public String getRequestUrl() {
		return _requestUrl;
	}
	
	public interface OnlineCallback {
		
		/**
		 * @param info music info, null if fail
		 */
		public void onDone(MusicInfo info);
	}
}
