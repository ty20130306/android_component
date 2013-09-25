package com.vanchu.sample;

import java.io.IOException;
import java.util.Random;

import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.common.util.ThreadUtil;
import com.vanchu.test.MediaPlayerActivity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;

public class MusicService extends Service {

	private static final String LOG_TAG	= MusicService.class.getSimpleName();
	
	public static final int	PLAYER_STATE_STOPPED	= 1;
	public static final int	PLAYER_STATE_PLAYING	= 2;
	public static final int	PLAYER_STATE_PAUSED		= 3;
	
	private MusicBinder	_binder			= null;
	private WakeLock	_wakeLock		= null;
	private WifiLock	_wifiLock		= null;
	
	private String		_musicUrl		= null;
	private MediaPlayer	_mediaPlayer	= null;
	
	private int			_playerState	= PLAYER_STATE_STOPPED;
	
	private MusicServiceCallback	_callback	= null;
	
	private boolean		_pausedByLossOfAudioFocus	= false;
	private MusicAudioFocusChangeListener	_audioFocusChangeListener	= null;
	
	@Override
	public void onCreate() {
		SwitchLogger.d(LOG_TAG, "onCreate");
		
		acquireWakeLock();
		acquireWifiLock();
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
				while (true) {
					if(null != _callback && null != _mediaPlayer
						&& PLAYER_STATE_PLAYING == _playerState) 
					{
						_callback.onMusicPlaying(_mediaPlayer);
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
			_mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					if(null != _callback) {
						_callback.onMusicPrepared(mp);
					}
					_playerState	= PLAYER_STATE_PLAYING;
					mp.start();
				}
			});
			
			_mediaPlayer.setOnBufferingUpdateListener(new MusicBufferingUpdateListener());
			_mediaPlayer.setOnCompletionListener(new MusicCompletionListener());
		} else {
			SwitchLogger.d(LOG_TAG, "media player inited");
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
		releaseWakeLock();
		releaseWifiLock();
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
					playMusic(_musicUrl);
				}
				break;
			
			case AudioManager.AUDIOFOCUS_LOSS:
				SwitchLogger.d(LOG_TAG, "==========audio focus change to loss================");
				// 长时间焦点丢失，再次重新获得时间不能确定，获得焦点后不要再继续播放
				if(PLAYER_STATE_PLAYING == _playerState) {
					_pausedByLossOfAudioFocus	= false;
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
	
	
	/********************** 音乐下载缓存监听器 *************************/
	private class MusicBufferingUpdateListener implements MediaPlayer.OnBufferingUpdateListener {
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			//SwitchLogger.d(LOG_TAG, "buffering music " + percent + "%");
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
			if(null != _callback) {
				_callback.onMusicCompletion(mp);
			}
		}
	}
	
	/********************** 音乐播放控制相关函数 ***********************/
	public MediaPlayer getMediaPlayer() {
		return _mediaPlayer;
	}
	
	public int getPlayerState() {
		return _playerState;
	}
	
	public void playMusic(String musicUrl) {
		if(_playerState == PLAYER_STATE_PLAYING && musicUrl == _musicUrl) {
			SwitchLogger.d(LOG_TAG, "media player is playing, no need to start");
			return;
		} else if(_playerState == PLAYER_STATE_PAUSED && musicUrl == _musicUrl) {
			SwitchLogger.d(LOG_TAG, "media player is paused, continue to play");
			_mediaPlayer.start();
			_playerState	= PLAYER_STATE_PLAYING;
		} else {
			SwitchLogger.d(LOG_TAG, "media player is stopped, start to play");
			SwitchLogger.d(LOG_TAG, "old music url = " + _musicUrl);
			SwitchLogger.d(LOG_TAG, "new music url = " + musicUrl);
			_musicUrl	= musicUrl;
			
			if(null == _musicUrl) {
				SwitchLogger.e(LOG_TAG, "music url is null, can not start to play");
				return;
			}
			
			if(_playerState == PLAYER_STATE_PLAYING || _playerState == PLAYER_STATE_PAUSED ){
				_mediaPlayer.stop();
				_mediaPlayer.reset();
			}
			
			try {
				_mediaPlayer.setDataSource(_musicUrl);
				_mediaPlayer.prepareAsync();
			} catch (IOException e) {
				SwitchLogger.e(e);
			}
		}
	}
	
	public void stopMusic() {
		if(_playerState != PLAYER_STATE_STOPPED) {
			SwitchLogger.d(LOG_TAG, "media player stop");
			_playerState	= PLAYER_STATE_STOPPED;
			_mediaPlayer.stop();
			_mediaPlayer.reset();
		}
	}
	
	public void pauseMusic() {
		if(_playerState == PLAYER_STATE_PLAYING) {
			_playerState	= PLAYER_STATE_PAUSED;
			_mediaPlayer.pause();
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
}
