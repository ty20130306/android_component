package com.vanchu.libs.music;

import java.io.IOException;
import com.vanchu.libs.common.util.NetUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.common.util.ThreadUtil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class MusicService extends Service {

	private static final String LOG_TAG	= MusicService.class.getSimpleName();
	
	public static final int PLAY_SUCC				= 0;
	public static final int PLAY_FAIL_NO_NETOWRK	= 1;
	public static final int PLAY_FAIL_WRONG_PARAM	= 2;
	public static final int PLAY_FAIL_PREPARING		= 3;
	
	public static final int PLAYER_STATE_STOPPED	= 1;
	public static final int PLAYER_STATE_PLAYING	= 2;
	public static final int PLAYER_STATE_PAUSED		= 3;
	public static final int PLAYER_STATE_PREPARING	= 4;
	
	public static final int PLAYER_MODE_NONE		= 0;
	public static final int PLAYER_MODE_ONLINE		= 1;
	public static final int PLAYER_MODE_OFFLINE		= 2;
	
	public static final int PLAYER_DETAIL_MODE_NONE		= 0;
	public static final int PLAYER_DETAIL_MODE_WIFI		= 1;
	public static final int PLAYER_DETAIL_MODE_2G3G		= 2;
	public static final int PLAYER_DETAIL_MODE_OFFLINE	= 3;
	
	private MusicBinder	_binder			= null;
	private WakeLock	_wakeLock		= null;
	private WifiLock	_wifiLock		= null;
	
	private String		_currentMusicUrl	= null;
	private String 		_currentMusicPath	= null;
	
	private boolean		_positionThreadRunning	= true;
	private MediaPlayer	_mediaPlayer	= null;
	
	private int			_playerState		= PLAYER_STATE_STOPPED;
	private int			_playerMode			= PLAYER_MODE_NONE;
	private int			_playerDetailMode	= PLAYER_DETAIL_MODE_NONE;
	
	private boolean		_pausedByLossOfAudioFocus	= false;
	private MusicAudioFocusChangeListener	_audioFocusChangeListener	= null;
	
	protected MusicServiceCallback	_callback	= null;
	
	@Override
	public void onCreate() {
		SwitchLogger.d(LOG_TAG, "onCreate");
		
		initMediaPlayer();
		requestAudioFocus();
		startPositionPublishThread();
	}
	
	private void requestAudioFocus() {
		_audioFocusChangeListener	= new MusicAudioFocusChangeListener();
		AudioManager audioManager	= (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		int result	= audioManager.requestAudioFocus(_audioFocusChangeListener,
												AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		if(result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			SwitchLogger.d(LOG_TAG, "==========audio request granted=============");
		} else {
			SwitchLogger.d(LOG_TAG, "==========audio request fail================");
		}
	}
	
	private void abandonAudioRequest() {
		AudioManager audioManager	= (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		int result	= audioManager.abandonAudioFocus(_audioFocusChangeListener);
		if(result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			SwitchLogger.d(LOG_TAG, "==========audio abandon granted=============");
		} else {
			SwitchLogger.d(LOG_TAG, "==========audio abandon fail================");
		}
	}
	
	private void startPositionPublishThread() {
		new Thread() {
			public void run() {
				while (_positionThreadRunning) {
					if(null != _mediaPlayer && _mediaPlayer.isPlaying()) {
						onMusicPlaying(_mediaPlayer);
						if(null != _callback) {
							_callback.onMusicPlaying(_mediaPlayer);
						}
					}
					ThreadUtil.sleep(900);
				}
			}
		}.start();
	}

	private void initMediaPlayer() {
		if(null == _mediaPlayer) {
			SwitchLogger.d(LOG_TAG, "media player not inited, init media player");
			_mediaPlayer	= new MediaPlayer();
			_playerState	= PLAYER_STATE_STOPPED;
			_mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					onMusicPrepared(mp);
					if(null != _callback) {
						_callback.onMusicPrepared(mp);
					}
					
					if(PLAYER_STATE_PREPARING == _playerState) {
						_playerState	= PLAYER_STATE_PLAYING;
						mp.start();
					}
				}
			});
			
			_mediaPlayer.setOnBufferingUpdateListener(new MusicBufferingUpdateListener());
			_mediaPlayer.setOnCompletionListener(new MusicCompletionListener());
			_mediaPlayer.setOnErrorListener(new MusicErrorListener());
		} else {
			SwitchLogger.d(LOG_TAG, "media player inited");
		}
	}
	
	private int getNewPlayerDetailMode(int newPlayerMode) {
		if(PLAYER_MODE_ONLINE == newPlayerMode) {
			int networkType	= NetUtil.getNetworkType(this);
			if(NetUtil.NETWORK_TYPE_WIFI == networkType) {
				return PLAYER_DETAIL_MODE_WIFI;
			} else {
				return PLAYER_DETAIL_MODE_2G3G;
			}
		} else {
			return PLAYER_DETAIL_MODE_OFFLINE;
		}
	}
	
	
	private void updateDetailModeIfNeed(int newPlayerMode) {
		int newPlayerDetailMode	= getNewPlayerDetailMode(newPlayerMode);
		if(newPlayerDetailMode != _playerDetailMode) {
			_playerDetailMode	= newPlayerDetailMode;
			if(null != _callback) {
				_callback.onPlayerDetailModeChange(_playerDetailMode);
			}
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SwitchLogger.d(LOG_TAG, "onStartCommand");
		
		return START_REDELIVER_INTENT;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		SwitchLogger.d(LOG_TAG, "onStart");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		SwitchLogger.d(LOG_TAG, "onBind");

		if(null == _binder) {
			_binder	= new MusicBinder();
		}
		
		return _binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		SwitchLogger.d(LOG_TAG, "onUnbind");

		return false;
	}
	
	@Override
	public void onDestroy() {
		SwitchLogger.d(LOG_TAG, "onDestroy");
		
		// clean up things here
		_positionThreadRunning	= false;
		releaseLock();
		stopMusic();
		abandonAudioRequest();
		_mediaPlayer.release();
	}
	
	public class MusicBinder extends Binder {
		public MusicService getService() {
			SwitchLogger.d(LOG_TAG, "get service instance");
			return MusicService.this;
		}
	}
	
	public void setMusicServiceCallback(MusicServiceCallback callback){
		_callback	= callback;
	}
	
	public String getCurrentMusicUrl() {
		return _currentMusicUrl;
	}
	
	public String getCurrentMusicPath() {
		return _currentMusicPath;
	}
	
	/********************** 音频焦点监听器 *************************/
	private class MusicAudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
		@Override
		public void onAudioFocusChange(int focusChange) {
			SwitchLogger.d(LOG_TAG, "============onAudioFocusChange, focusChange:" + focusChange + "===============");
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
				SwitchLogger.d(LOG_TAG, "==========audio focus change to gain================");
				if(PLAYER_STATE_PAUSED == _playerState && _pausedByLossOfAudioFocus) {
					// 短暂丢失焦点后重新获得焦点，继续播放
					_pausedByLossOfAudioFocus	= false;
					playMusic();
				}
				break;
			
			case AudioManager.AUDIOFOCUS_LOSS:
				SwitchLogger.d(LOG_TAG, "==========audio focus change to loss================");
				// 长时间焦点丢失，再次重新获得时间不能确定，获得焦点后尝试继续播放
				if(PLAYER_STATE_PLAYING == _playerState) {
					_pausedByLossOfAudioFocus	= true;
					pauseMusic();
				}
				break;
				
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				SwitchLogger.d(LOG_TAG, "==========audio focus change to transient loss================");
				// 短暂丢失焦点，获得焦点后继续播放
				if(PLAYER_STATE_PLAYING == _playerState) {
					_pausedByLossOfAudioFocus	= true;
					pauseMusic();
				}
			default:
				break;
			}
		}
	}
	
	/********************** 音乐错误监听器 *****************************/
	private class MusicErrorListener implements MediaPlayer.OnErrorListener {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			SwitchLogger.e(LOG_TAG, "music error occur, what=" + what + ", extra=" + extra 
									+ ", _playerState=" + _playerState);
			
			stopMusic();
			if(PLAYER_STATE_PLAYING == _playerState) {
				playMusic();
			}
			return true;
		}
	}
	
	
	/********************** 音乐下载缓存监听器 *************************/
	private class MusicBufferingUpdateListener implements MediaPlayer.OnBufferingUpdateListener {
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			//SwitchLogger.d(LOG_TAG, "buffering music " + percent + "%");
			onMusicBuffering(mp, percent);
			if(null != _callback) {
				_callback.onMusicBuffering(mp, percent);
			}
		}
	}
	
	/********************** 音乐播放结束监听器 *************************/
	private class MusicCompletionListener implements MediaPlayer.OnCompletionListener {
		@Override
		public void onCompletion(MediaPlayer mp) {
			SwitchLogger.d(LOG_TAG, "MusicCompletionListener.onCompletion called");
			stopMusic();
			onMusicCompletion(mp);
			if(null != _callback) {
				_callback.onMusicCompletion(mp);
			}
		}
	}
	
	/********************** 音乐播放内部控制相关函数 ***********************/
	
	private int playMusic(){
		if(PLAYER_MODE_ONLINE == _playerMode) {
			return playOnlineMusic(_currentMusicUrl);
		} else if(PLAYER_MODE_OFFLINE == _playerMode) {
			return playOfflineMusic(_currentMusicPath);
		}
		
		return PLAY_SUCC;
	}
	
	private void prepareMusic(String source) throws IOException {
		_playerState	= PLAYER_STATE_PREPARING;
		_mediaPlayer.reset();
		_mediaPlayer.setDataSource(source);
		_mediaPlayer.prepareAsync();
	}
	
	/********************** 音乐外部播放控制相关函数 ***********************/
	public MediaPlayer getMediaPlayer() {
		return _mediaPlayer;
	}
	
	public int getPlayerState() {
		return _playerState;
	}
	
	public int getPlayerMode() {
		return _playerMode;
	}
	
	public int getPlayerDetailMode() {
		return _playerDetailMode;
	}
	
	public int playOfflineMusic(String musicPath) {
		acquireLock();
		
		if(PLAYER_MODE_OFFLINE != _playerMode) {
			_playerMode	= PLAYER_MODE_OFFLINE;
			stopMusic();
			if(null != _callback) {
				_callback.onPlayerModeChange(_playerMode);
			}
		}
		
		updateDetailModeIfNeed(PLAYER_MODE_OFFLINE);
		
		if(_playerState == PLAYER_STATE_PLAYING && null != musicPath && musicPath.equals(_currentMusicPath)) {
			SwitchLogger.d(LOG_TAG, "media player is playing the offline music, no need to start");
			return PLAY_SUCC;
		} else if(_playerState == PLAYER_STATE_PAUSED && null != musicPath && musicPath.equals(_currentMusicPath)) {
			SwitchLogger.d(LOG_TAG, "media player is paused, continue to play the offline music");
			_playerState	= PLAYER_STATE_PLAYING;
			_mediaPlayer.start();
			return PLAY_SUCC;
		} else {
			if(PLAYER_STATE_PREPARING == _playerState) {
				SwitchLogger.d(LOG_TAG, "music is preparing, just wait for the offline music");
				return PLAY_SUCC;
			}
			
			SwitchLogger.d(LOG_TAG, "media player is stopped, start to play the offline music");
			SwitchLogger.d(LOG_TAG, "old music path = " + _currentMusicPath);
			SwitchLogger.d(LOG_TAG, "new music path = " + musicPath);
			_currentMusicPath	= musicPath;
			
			if(null == _currentMusicPath) {
				SwitchLogger.e(LOG_TAG, "music path is null, can not start to play the offline music");
				return PLAY_FAIL_WRONG_PARAM;
			}
			
			if(_playerState == PLAYER_STATE_PLAYING || _playerState == PLAYER_STATE_PAUSED ){
				stopMusic();
			}
			
			try {
				prepareMusic(_currentMusicPath);
			} catch (IOException e) {
				SwitchLogger.e(e);
			}
			
			return PLAY_SUCC;
		}
	}
	
	public int playOnlineMusic(String musicUrl) {
		acquireLock();
		
		if( ! NetUtil.isConnected(this) && PLAYER_STATE_PAUSED != _playerState) {
			return PLAY_FAIL_NO_NETOWRK;
		}
		
		if(PLAYER_MODE_ONLINE != _playerMode) {
			_playerMode	= PLAYER_MODE_ONLINE;
			stopMusic();
			if(null != _callback) {
				_callback.onPlayerModeChange(_playerMode);
			}
		}
		
		updateDetailModeIfNeed(PLAYER_MODE_ONLINE);
		
		if(_playerState == PLAYER_STATE_PLAYING && null != musicUrl && musicUrl.equals(_currentMusicUrl)) {
			SwitchLogger.d(LOG_TAG, "media player is playing the online music, no need to start");
			return PLAY_SUCC;
		} else if(_playerState == PLAYER_STATE_PAUSED && null != musicUrl && musicUrl.equals(_currentMusicUrl)) {
			SwitchLogger.d(LOG_TAG, "media player is paused, continue to play the online music");
			_playerState	= PLAYER_STATE_PLAYING;
			_mediaPlayer.start();
			return PLAY_SUCC;
		} else {
			if(PLAYER_STATE_PREPARING == _playerState) {
				SwitchLogger.d(LOG_TAG, "music is preparing, just wait for the online music");
				return PLAY_FAIL_PREPARING;
			}
			
			SwitchLogger.d(LOG_TAG, "media player is stopped, start to play the online music");
			SwitchLogger.d(LOG_TAG, "old music url = " + _currentMusicUrl);
			SwitchLogger.d(LOG_TAG, "new music url = " + musicUrl);
			_currentMusicUrl	= musicUrl;
			
			if(null == _currentMusicUrl) {
				SwitchLogger.e(LOG_TAG, "music url is null, can not start to play the online music");
				return PLAY_FAIL_WRONG_PARAM;
			}
			
			if(_playerState == PLAYER_STATE_PLAYING || _playerState == PLAYER_STATE_PAUSED ){
				stopMusic();
			}
			
			try {
				prepareMusic(_currentMusicUrl);
			} catch (IOException e) {
				SwitchLogger.e(e);
			}
			
			return PLAY_SUCC;
		}
	}
	
	public void stopMusic() {
		if(_playerState != PLAYER_STATE_STOPPED) {
			_playerState	= PLAYER_STATE_STOPPED;
			_mediaPlayer.stop();
		}
		_mediaPlayer.reset();
	}
	
	public void cleanUp() {
		_callback	= null;
	}
	
	public void pauseMusic() {
		switch (_playerState) {
		case PLAYER_STATE_PLAYING :
			_playerState	= PLAYER_STATE_PAUSED;
			_mediaPlayer.pause();
			releaseLock();
			break;
		
		case PLAYER_STATE_PREPARING :
			_playerState	= PLAYER_STATE_PAUSED;
			releaseLock();
			break;
			
		default:
		}
	}
	
	/********************** 锁获取相关函数 ***********************/
	// 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
	private void acquireWakeLock() {
		if (null == _wakeLock) {
			PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
			_wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "MusicServiceWakeLock");
			if (null != _wakeLock) {
				_wakeLock.acquire();
			}
		}
	}

	// 释放设备电源锁
	private void releaseWakeLock() {
		if (null != _wakeLock) {
			_wakeLock.release();
			_wakeLock = null;
		}
	}
	
	// 获取WIFI锁，保持该服务在屏幕熄灭时仍然获取WIFI，保持联网
	private void acquireWifiLock() {
		if(null == _wifiLock) {
			_wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "MusicServiceWifiLock");
			_wifiLock.acquire();
		}
	}
	
	// 释放WIFI锁
	private void releaseWifiLock() {
		if(null != _wifiLock) {
			_wifiLock.release();
			_wifiLock	= null;
		}
	}
	
	private void acquireLock() {
		SwitchLogger.d(LOG_TAG, "----------------acquire lock" );
		acquireWakeLock();
		acquireWifiLock();
	}
	
	private void releaseLock() {
		SwitchLogger.d(LOG_TAG, "----------------release lock" );
		releaseWakeLock();
		releaseWifiLock();
	}
	
	/****************供子类使用函数*****************/
	final protected boolean continueToPlay() {
		if(PLAYER_STATE_PAUSED == _playerState && PLAYER_MODE_NONE != _playerMode) {
			if(PLAYER_MODE_ONLINE == _playerMode) {
				playOnlineMusic(_currentMusicUrl);
				return true;
			} else if(PLAYER_MODE_OFFLINE == getPlayerMode()) {
				playOfflineMusic(_currentMusicPath);
				return true;
			}
			
			return false;
		}
		
		return false;
	}
	
	/****************子类可扩展函数*****************/
	protected void onMusicPrepared(MediaPlayer mp) {
		//SwitchLogger.d(LOG_TAG, "onMusicPrepared" );
	}
	
	protected void onMusicBuffering(MediaPlayer mp, int percent) {
		//SwitchLogger.d(LOG_TAG, "onMusicBuffering, percent="+percent );
	}
	
	protected void onMusicPlaying(MediaPlayer mp) {
		//SwitchLogger.d(LOG_TAG, "onMusicPlaying" );
	}

	protected void onMusicCompletion(MediaPlayer mp) {
		//SwitchLogger.d(LOG_TAG, "onMusicCompletion" );
	}
}
