package com.vanchu.module.music;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;

import com.vanchu.libs.common.container.SolidQueue;
import com.vanchu.libs.common.task.Downloader;
import com.vanchu.libs.common.util.NetUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.music.MusicService;

public class VanchuMusicService extends MusicService {
	private static final String LOG_TAG	= VanchuMusicService.class.getSimpleName();
	
	public static final int	ERR_NO_OFFLINE_MUSIC		= 1;
	public static final int	ERR_REQUEST_LIST_FAIL		= 2;
	public static final int	ERR_REQUEST_URL_NOT_SET		= 3;
	
	private static final String PREFS_VANCHU_MUSIC_SERVICE		= "vanchu_music_services";
	
	private static final int INDEX_NONE		= -1;
	
	private static final int	DEFAULT_MUSIC_QUEUE_SIZE	= 10;
	private static final String DEFAULT_EACH_FETCH_NUM		= "10";
	
	private static final int REQUEST_MUSIC_INFO_SUCC	= 0;
	private static final int REQUEST_MUSIC_INFO_FAIL	= 1;
	
	private static final String	MUSIC_DOWNLOAD_DIR_NAME	= "music";
	private static final String	MUSIC_QUEUE_NAME		= "music";
	
	private int	_musicQueueSize			= DEFAULT_MUSIC_QUEUE_SIZE;
	private String _eachFetchNum		= DEFAULT_EACH_FETCH_NUM;
	
	private String _requestUrl			= null;
	
	private int				_currentInfoIndex	= INDEX_NONE;
	private List<MusicInfo> _musicInfoList		= new LinkedList<MusicInfo>();
	
	private SolidQueue<MusicSolidQueueElement>	_musicSolidQueue	= null;
	private Map<String, Boolean>	_musicDownloadingMap			= null;
	
	private Handler	_handler	= new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REQUEST_MUSIC_INFO_SUCC :
				parseInfoList((JSONObject)msg.obj);
				break;

			case REQUEST_MUSIC_INFO_FAIL :
				if(null != _callback) {
					_callback.onError(ERR_REQUEST_LIST_FAIL);
				}
				SwitchLogger.e(LOG_TAG, "request music info list fail" );
				break;
			default:
				break;
			}
		}
	};

	private void parseInfoList(JSONObject response) {
		try {
			JSONArray list	= response.getJSONObject("musics").getJSONArray("data");
			
			for(int i = 0; i < list.length(); ++i ) {
				JSONObject	info	= list.getJSONObject(i);
				String id		= info.getString("id");
				String name		= info.getString("name");
				String audio	= info.getString("audio");
				String img		= info.getString("img");
				String artist	= info.getString("artist");
				String album	= info.getString("album");
				String lyric	= info.getString("lyric");
				MusicInfo mi	= new MusicInfo(id, name, audio, img, artist, album, lyric);
				_musicInfoList.add(mi);
			}
		} catch( JSONException e) {
			SwitchLogger.e(e);
			if(null != _callback) {
				_callback.onError(ERR_REQUEST_LIST_FAIL);
			}
		}
		
		_currentInfoIndex += 1;
		SwitchLogger.d(LOG_TAG, "parseInfoList *********** _currentInfoIndex += 1, now=" + _currentInfoIndex);
		if(_currentInfoIndex >= _musicInfoList.size()) {
			if(_musicInfoList.size() <= 0) {
				SwitchLogger.e(LOG_TAG, "no music info, play fail");
				return ;
			}

			_currentInfoIndex	= 0;
		}
		playOnlineMusic(_musicInfoList.get(_currentInfoIndex).getAudio());
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		init();
	}
	
	private void init() {
		_musicSolidQueue	= new SolidQueue<MusicSolidQueueElement>(this, MUSIC_QUEUE_NAME, 
														_musicQueueSize, new MusicSolidQueueCallback());
		
		_musicDownloadingMap	= new HashMap<String, Boolean>();
	}
	
	private void doPlaySmartMusic() {
		int networkType	= NetUtil.getNetworkType(this);
		if(NetUtil.NETWORK_TYPE_WIFI == networkType) {
			checkAndPlayOnlineMusic();
		} else if (NetUtil.NETWORK_TYPE_INVALID == networkType) {
			checkAndPlayOfflineMusic();
		} else {
			if(_musicSolidQueue.size() > 0) {
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
	
	public MusicData getCurrentMusicData() {
		int playerMode	= getPlayerMode();
		if(PLAYER_MODE_ONLINE == playerMode) {
			if(_currentInfoIndex < 0 || _currentInfoIndex >= _musicInfoList.size()) {
				return null;
			}
			
			MusicInfo mi	= _musicInfoList.get(_currentInfoIndex);
			String id		= mi.getId();
			String name		= mi.getName();
			String artist	= mi.getArtist();
			String audioUrl	= mi.getAudio();
			String lyricUrl	= mi.getLyric();
			String imgUrl	= mi.getImg();
			return new MusicData(id, name, artist, playerMode, audioUrl, "", lyricUrl, "", imgUrl);
		} else if(PLAYER_MODE_OFFLINE == playerMode) {
			MusicSolidQueueElement msqe	= getCurrentMusicSolidQueueElement();
			if(null == msqe) {
				return null;
			}
			
			String id			= msqe.getId();
			String name			= msqe.getName();
			String artist		= msqe.getArtist();
			String audioPath	= msqe.getAudioPath();
			String lyricPath	= msqe.getLyricPath();
			String imgUrl		= msqe.getImg();
			return new MusicData(id, name, artist, playerMode, "", audioPath, "", lyricPath, imgUrl);
		} else {
			return null;
		}
	}
	
	public boolean isMusicAvailable() {
		if(NetUtil.NETWORK_TYPE_INVALID != NetUtil.getNetworkType(this) ) {
			return true;
		}
		
		if(_musicSolidQueue.size() > 0) {
			return true;
		}
		
		return false;
	}
	
	public void setMusicQueueSize(int size) {
		if(size < 0) {
			return ;
		}
		
		_musicQueueSize	= size;
		_musicSolidQueue.setMaxSize(_musicQueueSize);
	}
	
	public void setRequestUrl(String requestUrl) {
		_requestUrl	= requestUrl;
	}
	
	private MusicSolidQueueElement getCurrentMusicSolidQueueElement() {
		String currentAudioPath	= getCurrentMusicPath();
		if(null == currentAudioPath) {
			return null;
		}
		
		LinkedList<MusicSolidQueueElement> list	= _musicSolidQueue.getQueue();
		MusicSolidQueueElement msqe;
		for(int i = 0; i < list.size(); ++i) {
			msqe	= list.get(i);
			if(currentAudioPath.equals(msqe.getAudioPath())) {
				return msqe;
			}
		}
		
		return null;
	}
	
	private boolean needUpdateMusicInfoList() {
		if(_musicInfoList.size() <= 0) {
			return true;
		}
		
		if(_currentInfoIndex == _musicInfoList.size() - 1) {
			return true;
		}
		
		return false;
	}
	
	private String getAndUpdateNewbieFlag() {
		SharedPreferences prefs		= getSharedPreferences(PREFS_VANCHU_MUSIC_SERVICE, Context.MODE_PRIVATE);
		String newbieFlag	= prefs.getString("music_newbie", "1");
		prefs.edit().putString("music_newbie", "0").commit();
		
		return newbieFlag;
	}
	
	private void updateMusicInfoList() {
		if(null == _requestUrl) {
			if(null != _callback) {
				_callback.onError(ERR_REQUEST_URL_NOT_SET);
			}
			SwitchLogger.e(LOG_TAG, "request url is null, please set it first" );
			return ;
		}
		
		new Thread(){
			@Override
			public void run() {
				HashMap<String, String> params	= new HashMap<String, String>();
				params.put("newbie", getAndUpdateNewbieFlag());
				params.put("num", _eachFetchNum);
				
				String response	= NetUtil.httpPostRequest(_requestUrl, params, 3);
				SwitchLogger.d(LOG_TAG, "response="+response);

				if(response == null){
					_handler.sendEmptyMessage(REQUEST_MUSIC_INFO_FAIL);
					return ;
				}
				
				JSONObject responseJson	= null;
				try {
					responseJson	= new JSONObject(response);
				} catch( JSONException e) {
					SwitchLogger.e(e);
					_handler.sendEmptyMessage(REQUEST_MUSIC_INFO_FAIL);
					return ;
				}
				
				_handler.obtainMessage(REQUEST_MUSIC_INFO_SUCC, responseJson).sendToTarget();
			}
		}.start();
	}
	
	private void checkAndPlayOnlineMusic() {
		if(needUpdateMusicInfoList()) {
			updateMusicInfoList();
			
			return ;
		}
		
		int result	= playOnlineMusic(_musicInfoList.get(_currentInfoIndex + 1).getAudio());
		if(VanchuMusicService.PLAY_FAIL_PREPARING == result) {
			return ;
		}
		
		_currentInfoIndex += 1;
		SwitchLogger.d(LOG_TAG, "checkAndPlayOnlineMusic *********** _currentInfoIndex += 1, now=" + _currentInfoIndex);
		
	}
	
	private void checkAndPlayOfflineMusic() {
		if(_musicSolidQueue.size() <= 0) {
			if(null != _callback) {
				_callback.onError(ERR_NO_OFFLINE_MUSIC);
			}
			return ;
		}
		
		int retryMax	= 3;
		int retryCnt	= 0;
		String currentPath	= getCurrentMusicPath();
		String next		= null;
		LinkedList<MusicSolidQueueElement>	list	= _musicSolidQueue.getQueue();
		Random rand	= new Random();
		do {
			int i	= rand.nextInt(list.size());
			MusicSolidQueueElement element	= list.get(i);
			next	= element.getAudioPath();
		} while(null != currentPath && currentPath.equals(next) && retryCnt < retryMax);
		
		currentPath	= next;
		SwitchLogger.d(LOG_TAG, "current offline music path change, now="+currentPath);
		playOfflineMusic(currentPath);
	}

	
	/************* 音乐下载相关开始 ******************************/
	private boolean audioInQueue(String url) {
		LinkedList<MusicSolidQueueElement> list	= _musicSolidQueue.getQueue();
		for(int i = 0; i < list.size(); ++i) {
			if(null != url && url.equals(list.get(i).getAudio())) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean audioNeedDownload() {
		String currentUrl	= getCurrentMusicUrl();
		Boolean isDownloadingObj	= _musicDownloadingMap.get(currentUrl);
		if(null != isDownloadingObj && true == isDownloadingObj.booleanValue()) {
			//SwitchLogger.d(LOG_TAG, "music " + _currentOnlineMusicUrl + " is downloading" );
			return false;
		}
		
		Iterator<Entry<String, Boolean> > iter	= _musicDownloadingMap.entrySet().iterator();
		int downloadingCnt	= 0;
		while(iter.hasNext()) {
			Entry<String, Boolean> entry	= iter.next();
			isDownloadingObj	= entry.getValue();
			if(true == isDownloadingObj.booleanValue()) {
				downloadingCnt++;
			}
		}
		if(downloadingCnt >= 2) {
			//SwitchLogger.d(LOG_TAG, "more than 2 audio is downloading" );
			return false;
		}
		
		if(audioInQueue(currentUrl)) {
			//SwitchLogger.d(LOG_TAG, "music " + _currentOnlineMusicUrl + " is in queue, no need to download" );
			return false;
		}
		
		if(NetUtil.NETWORK_TYPE_WIFI != NetUtil.getNetworkType(this) && _musicSolidQueue.size() > 0) {
			//SwitchLogger.d(LOG_TAG, "network type not wifi && has already cached one, no need to download" );
			return false;
		}

		
		return true;
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
		
		@Override
		public void onSuccess(String downloadFile) {
			SwitchLogger.d(LOG_TAG, "download lyric succ, downloadFile:" + downloadFile);
			updateLyricPath(musicId, downloadFile);
		}
		
		@Override
		public void onError(int errCode) {
			SwitchLogger.e(LOG_TAG, "download lyric fail, errCode="+errCode);
		}
		
		@Override
		public void onPause() {
			
		}
	}
	
	private void updateLyricPath(String id, String lyricPath) {
		LinkedList<MusicSolidQueueElement>	list	= _musicSolidQueue.getQueue();
		for(int i = 0; i < list.size(); ++i) {
			MusicSolidQueueElement msqe	= list.get(i);
			if(null != id && id.equals(msqe.getId())) {
				msqe.setLyricPath(lyricPath);
				_musicSolidQueue.solidify();
				return ;
			}
		}
	}
	
	private class AudioDownloadListener implements Downloader.IDownloadListener {
		
		private String audioUrl;
		private String musicId;
		
		public AudioDownloadListener(String id, String url) {
			musicId		= id;
			audioUrl	= url;
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
			MusicInfo mi	= getMusicInfo(musicId);
			if(null == mi) {
				return ;
			}
			String lyric	= mi.getLyric();
			MusicSolidQueueElement element	= new MusicSolidQueueElement(musicId, mi.getName(), 
															mi.getAudio(), mi.getImg(), mi.getArtist(), 
															mi.getAlbum(), lyric, downloadFile);
			_musicSolidQueue.enqueue(element);
			_musicDownloadingMap.put(audioUrl, new Boolean(false) );
			
			if(null != lyric && ! lyric.equals("")) {
				new Downloader(VanchuMusicService.this, lyric, MUSIC_DOWNLOAD_DIR_NAME, new LyricDownloadListener(mi.getId())).run();
			}
		}
		
		@Override
		public void onError(int errCode) {
			SwitchLogger.e(LOG_TAG, "download audio fail, errCode="+errCode);
			_musicDownloadingMap.put(audioUrl, new Boolean(false) );
		}
		
		@Override
		public void onPause() {
			
		}
	}
	
	private MusicInfo getMusicInfo(String id) {
		for(int i = 0; i < _musicInfoList.size(); ++i) {
			MusicInfo mi	= _musicInfoList.get(i);
			if(null != id && id.equals(mi.getId())) {
				return mi;
			}
		}
		
		return null;
	}

	private String getIdByMusicUrl(String url) {
		for(int i = 0; i < _musicInfoList.size(); ++i) {
			MusicInfo mi	= _musicInfoList.get(i);
			if(null != url && url.equals(mi.getAudio()) ) {
				return mi.getId();
			}
		}
		
		return null;
	}
	
	private void downloadAudio() {
		String current	= getCurrentMusicUrl();
		String id		= getIdByMusicUrl(current);
		if(null == id) {
			return ;
		}
		_musicDownloadingMap.put(current, new Boolean(true) );
		new Downloader(this, current, MUSIC_DOWNLOAD_DIR_NAME, new AudioDownloadListener(id, current)).run();
	}
	
	@Override
	protected void onMusicBuffering(MediaPlayer mp, int percent) {
		if(100 == percent && audioNeedDownload()) {
			SwitchLogger.d(LOG_TAG, "need to download music, begin to download" );
			downloadAudio();
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
