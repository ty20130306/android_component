package com.vanchu.sample;

import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.music.MusicService;
import com.vanchu.libs.music.MusicService.MusicBinder;
import com.vanchu.libs.music.MusicServiceCallback;
import com.vanchu.module.music.MusicData;
import com.vanchu.module.music.VanchuMusicService;
import com.vanchu.test.R;
import com.vanchu.test.R.layout;
import com.vanchu.test.R.menu;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.View;

public class MusicServiceActivity extends Activity {

	private static final String LOG_TAG	= MusicServiceActivity.class.getSimpleName();
	
	private VanchuMusicService	_service		= null;
	private boolean				_serviceBound	= false;
	
	private MediaPlayer		_mediaPlayer		= null;
	
	private ServiceConnection	_connection	= new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			_service	= null;
			_serviceBound	= false;
			SwitchLogger.d(LOG_TAG, "onServiceDisconnected");
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MusicBinder binder	= (MusicBinder)service;
			_serviceBound	= true;
			_service	= (VanchuMusicService)binder.getService();
			_mediaPlayer	= _service.getMediaPlayer();
			_service.setRequestUrl("http://kadovat.dev.time.bangyouxi.com/widgets/music/list.json");
			_service.setMusicServiceCallback(new VanchuMusicServiceCallback());
			SwitchLogger.d(LOG_TAG, "onServiceConnected");
		}
	};
	
	private class VanchuMusicServiceCallback extends MusicServiceCallback {
		
		private boolean prepared	= false;
		
		@Override
		public void onMusicPrepared(MediaPlayer mp) {
			super.onMusicPrepared(mp);
			prepared	= true;
			SwitchLogger.d(LOG_TAG, "VanchuMusicServiceCallback.onMusicPrepared");
			MusicData md	= _service.getCurrentMusicData();
			SwitchLogger.d(LOG_TAG, "music data, name="+md.getName()+",player mode="+md.getPlayerMode()
									+",audio url="+md.getAudioUrl()+",audio path="+md.getAudioPath()
									+",lyric url="+md.getLyricUrl()+",lyric path="+md.getLyricPath()
									+",img url="+md.getImgUrl());
		}
		
		@Override
		public void onMusicBuffering(MediaPlayer mp, int percent) {
			super.onMusicBuffering(mp, percent);
			if(100 != percent) {
				SwitchLogger.d(LOG_TAG, "VanchuMusicServiceCallback.onMusicBuffering, percent="+percent);
			}
		}
		
		@Override
		public void onMusicPlaying(MediaPlayer mp) {
			super.onMusicPlaying(mp);
			//SwitchLogger.d(LOG_TAG, "VanchuMusicServiceCallback.onMusicPlaying");
		}
		
		@Override
		public void onMusicCompletion(MediaPlayer mp) {
			super.onMusicCompletion(mp);
			prepared	= false;
			SwitchLogger.d(LOG_TAG, "VanchuMusicServiceCallback.onMusicCompletion");
			boolean isMusicAvailable	= _service.isMusicAvailable();
			if(isMusicAvailable) {
				SwitchLogger.d(LOG_TAG, "music available");
				_service.nextSmartMusic();
			} else {
				SwitchLogger.d(LOG_TAG, "music not available");
			}
		}
		
		@Override
		public void onPlayerModeChange(int currentPlayerMode) {
			if(MusicService.PLAYER_MODE_ONLINE == currentPlayerMode) {
				SwitchLogger.d(LOG_TAG, "player mode change to online" );
			} else {
				SwitchLogger.d(LOG_TAG, "player mode change to offline" );
			}
		}
		
		@Override
		public void onPlayerDetailModeChange(int currentPlayerDetailMode) {
			super.onPlayerDetailModeChange(currentPlayerDetailMode);
			switch (currentPlayerDetailMode) {
			case MusicService.PLAYER_DETAIL_MODE_WIFI:
				SwitchLogger.d(LOG_TAG, "player detail mode change to wifi" );
				break;
			case MusicService.PLAYER_DETAIL_MODE_2G3G:
				SwitchLogger.d(LOG_TAG, "player detail mode change to 2G/3G" );
				break;
			case MusicService.PLAYER_DETAIL_MODE_OFFLINE:
				SwitchLogger.d(LOG_TAG, "player detail mode change to offline" );
				break;
			default:
				break;
			}
			
		}
		
		@Override
		public void onError(int errCode) {
			switch (errCode) {
			case VanchuMusicService.ERR_NO_OFFLINE_MUSIC:
				Tip.show(MusicServiceActivity.this, "还没有缓存过离线歌曲，请连接网络在线听歌");
				break;
				
			case VanchuMusicService.ERR_REQUEST_LIST_FAIL:
				Tip.show(MusicServiceActivity.this, "请求歌曲列表失败");
				break;
				
			case VanchuMusicService.ERR_REQUEST_URL_NOT_SET:
				Tip.show(MusicServiceActivity.this, "请设置请求歌曲列表URL");
				break;
				
			default:
				break;
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_service);
		
		init();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(_serviceBound && null != _service) {
			_service.cleanUp();
			unbindMusicService();
		}
	}
	
	private void init() {
		startMusicService();
		bindMusicService();
	}
	
	private void startMusicService() {
		SwitchLogger.d(LOG_TAG, "start music service");
		Intent intent	= new Intent(this, VanchuMusicService.class);
		startService(intent);
	}
	
	private void stopMusicService() {
		SwitchLogger.d(LOG_TAG, "stop music service" );
		Intent intent	= new Intent(this, VanchuMusicService.class);
		stopService(intent);
	}
	
	private void bindMusicService() {
		SwitchLogger.d(LOG_TAG, "bind music service");
		_serviceBound	= true;
		Intent intent	= new Intent(this, VanchuMusicService.class);
		bindService(intent, _connection, Context.BIND_AUTO_CREATE);
	}

	private void unbindMusicService() {
		SwitchLogger.d(LOG_TAG, "unbind music service");
		_serviceBound	= false;
		unbindService(_connection);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.music, menu);
		return true;
	}

	public void pause(View v) {
		_service.pauseMusic();
	}
	
	public void play(View v) {
		_service.playSmartMusic();
	}
	
	public void next(View v) {
		_service.nextSmartMusic();
	}
	
	public void onlinePlay(View v) {
		_service.playOnlineMusic();
	}
	
	public void onlineNext(View v) {
		_service.nextOnlineMusic();
	}
	
	public void offlinePlay(View v) {
		_service.playOfflineMusic();
	}
	
	public void offlineNext(View v) {
		_service.nextOfflineMusic();
	}

}
