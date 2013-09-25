package com.vanchu.sample;

import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.sample.MusicService.MusicBinder;
import com.vanchu.test.R;
import com.vanchu.test.R.layout;
import com.vanchu.test.R.menu;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class TestMusicServiceActivity extends Activity {

	private static final String 	LOG_TAG		= TestMusicServiceActivity.class.getSimpleName();
	
	private static final String		MUSIC_1		= "http://m1.file.xiami.com/1/133/29133/189619/2304423_119094_l.mp3";
//	private static final String		MUSIC_2		= "http://m1.file.xiami.com/826/14826/128121/1410562_96662_l.mp3";
	
//	private static final String		MUSIC_1		= "http://small.cdn.baidupcs.com/file/0237474bec99f635331263b64ad99b38?xcode=a6a7397d559883576e73797a190ec140a8eacd2d77a4d25d&fid=3895617211-250528-1262336179&time=1380076042&sign=FDTAXER-DCb740ccc5511e5e8fedcff06b081203-raU8z5D3v%2Bdk%2FpZqs8JZAsxTvVI%3D&to=sc&fm=B,B,T,bs&expires=8h&rt=sh&r=951206849&logid=2652064266&sh=1&fn=%E9%92%A2%E7%90%B4%20-%20%E5%BE%88%E5%A5%BD%E5%90%AC%E7%9A%84%E4%B8%80%E6%AE%B5%E9%9F%A9%E5%9B%BD%E7%BA%AF%E9%9F%B3%E4%B9%90%E7%89%87%E6%AE%B5.mp3";
	private static final String		MUSIC_2		= "http://zhangmenshiting.baidu.com/data2/music/33186104/14716050151200128.mp3?xcode=79edc4f45ea71f4c72c44d84857ca433d65bab0ba5f92970";

	
	private MusicService		_service;
	private boolean			_serviceBound;
	private String			_lastMusicUrl	= MUSIC_1;
	private MediaPlayer		_mediaPlayer	= null;
	protected SeekBar 		_seekBar;
	private float			_leftVolume		= 0.0f;
	private float			_rightVolume	= 0.0f;
	private TextView		_volumeText		= null;
	
	private ServiceConnection	_connection	= new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			_service	= null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MusicBinder binder	= (MusicBinder)service;
			_serviceBound	= true;
			_service	= binder.getService();
			_mediaPlayer	= _service.getMediaPlayer();
			_service.setMusicServiceCallback(new TestMusicServiceCallback());
			if(MusicService.PLAYER_STATE_STOPPED != _service.getPlayerState()) {
				_seekBar.setProgress(_mediaPlayer.getCurrentPosition());
				_seekBar.setMax(_mediaPlayer.getDuration());
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_music_service);
		_service		= null;
		_serviceBound	= false;
		startMusicService();
		bindMusicService();
		
		_seekBar	= (SeekBar)findViewById(R.id.seek_bar);
		_volumeText	= (TextView)findViewById(R.id.volume_text);
		_volumeText.setText("暂未获取到音量");
		//init();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(_serviceBound) {
			unbindMusicService();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		return true;
	}
	
	private void startMusicService() {
		SwitchLogger.d(LOG_TAG, "start service");
		Intent intent	= new Intent(TestMusicServiceActivity.this, MusicService.class);
		TestMusicServiceActivity.this.startService(intent);
	}
	
	private void stopMusicService() {
		SwitchLogger.d(LOG_TAG, "stop service" );
		Intent intent	= new Intent(TestMusicServiceActivity.this, MusicService.class);
		TestMusicServiceActivity.this.stopService(intent);
	}
	
	private void bindMusicService() {
		SwitchLogger.d(LOG_TAG, "bind service");
		_serviceBound	= true;
		Intent intent	= new Intent(TestMusicServiceActivity.this, MusicService.class);
		TestMusicServiceActivity.this.bindService(intent, _connection, Context.BIND_AUTO_CREATE);
	}

	private void unbindMusicService() {
		SwitchLogger.d(LOG_TAG, "unbind service");
		_serviceBound	= false;
		TestMusicServiceActivity.this.unbindService(_connection);
	}
	
	private void init() {
		Button start	= (Button)findViewById(R.id.start);
		start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startMusicService();
			}
		});
		
		Button stop	= (Button)findViewById(R.id.stop);
		stop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				stopMusicService();
			}
		});
		
		Button bind	= (Button)findViewById(R.id.bind);
		bind.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				bindMusicService();
			}
		});
		
		Button unbind	= (Button)findViewById(R.id.unbind);
		unbind.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				unbindMusicService();
			}
		});
		
		Button communicate	= (Button)findViewById(R.id.communicate);
		communicate.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SwitchLogger.d(LOG_TAG, "communicate with service" );
				if(_serviceBound) {
					SwitchLogger.d(LOG_TAG, "service bound, player state is " + _service.getPlayerState());
				} else {
					SwitchLogger.d(LOG_TAG, "service not inited or bound, communicate fail");
				}
			}
		});
	}

	public void increaseVolume(View v) {
		_leftVolume += 0.1;
		if(_leftVolume > 1.0) {
			_leftVolume = 1.0f;
		}
		_rightVolume += 0.1;
		if(_rightVolume > 1.0) {
			_rightVolume = 1.0f;
		}
		_mediaPlayer.setVolume(_leftVolume, _rightVolume);
		_volumeText.setText(_leftVolume + "," + _rightVolume);
	}

	public void decreaseVolume(View v) {
		_leftVolume -= 0.1;
		if(_leftVolume < 0) {
			_leftVolume = 0f;
		}
		_rightVolume -= 0.1;
		if(_rightVolume < 0) {
			_rightVolume = 0f;
		}
		_mediaPlayer.setVolume(_leftVolume, _rightVolume);
		_volumeText.setText(_leftVolume + "," + _rightVolume);
	}
	
	public void playMusic(View v) {
		_service.playMusic(MUSIC_1);
		_lastMusicUrl	= MUSIC_1;
	}
	
	public void stopMusic(View v) {
		_service.stopMusic();
	}
	
	public void pauseMusic(View v) {
		_service.pauseMusic();
	}
	
	public void nextMusic(View v) {
		if(_lastMusicUrl == MUSIC_1) {
			_lastMusicUrl	= MUSIC_2;
			_service.playMusic(MUSIC_2);
		} else {
			_lastMusicUrl	= MUSIC_1;
			_service.playMusic(MUSIC_1);
		}
	}
	
	private class TestMusicServiceCallback extends MusicServiceCallback {
		
		private boolean prepared	= false;
		
		@Override
		public void onMusicPrepared(MediaPlayer mp) {
			super.onMusicPrepared(mp);
			_seekBar.setMax(mp.getDuration());
			_seekBar.setProgress(0);
			_seekBar.setSecondaryProgress(0);
			prepared	= true;
		}
		
		@Override
		public void onMusicBuffering(MediaPlayer mp, int percent) {
			super.onMusicBuffering(mp, percent);
			if(prepared) {
				int sp	= (int)((float)percent * 0.01 * mp.getDuration());
				_seekBar.setSecondaryProgress(sp);
			}
		}
		
		@Override
		public void onMusicPlaying(MediaPlayer mp) {
			super.onMusicPlaying(mp);
			_seekBar.setProgress(mp.getCurrentPosition());
		}
		
		@Override
		public void onMusicCompletion(MediaPlayer mp) {
			super.onMusicCompletion(mp);
			prepared	= false;
			nextMusic(null);
		}
	}

}
