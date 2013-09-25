package com.vanchu.test;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.MediaController.MediaPlayerControl;

import java.io.IOException;

import com.vanchu.libs.common.util.SwitchLogger;

public class MediaPlayerActivity extends Activity implements MediaPlayer.OnBufferingUpdateListener {
	
	private static final String	LOG_TAG	= MediaPlayerActivity.class.getSimpleName();
	
	private static final int	STATE_PLAYING	= 1;
	private static final int	STATE_PAUSED	= 2;
	private static final int	STATE_STOPPED	= 3;
	
	protected MediaPlayer _mp = null;
	protected int _state	= STATE_STOPPED;
	protected SeekBar _seekBar;
	protected ProgressBar _progressBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_media_player);

		final TextView tv = (TextView)findViewById(R.id.textview);
		_seekBar	= (SeekBar)findViewById(R.id.seek_bar);
		_seekBar.setMax(100);
		_seekBar.setSecondaryProgress(50);
		_progressBar	= (ProgressBar)findViewById(R.id.progress_bar);
		
		MediaPlayerActivity.this._mp = new MediaPlayer();
		_mp.setOnBufferingUpdateListener(this);
		MediaPlayerActivity.this._mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				_seekBar.setMax(mp.getDuration());
				_progressBar.setVisibility(View.GONE);
				mp.start();
				MediaPlayerActivity.this._state = STATE_PLAYING;
			}
		});

		Button btnPlay = (Button)findViewById(R.id.button_play);
		btnPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(_state == STATE_PLAYING) {
					SwitchLogger.d(LOG_TAG, "mp is playing, no need to start");
					return;
				} else if(_state == STATE_PAUSED) {
					_state	= STATE_PLAYING;
					SwitchLogger.d(LOG_TAG, "mp is paused, just start");
					MediaPlayerActivity.this._mp.start();
				} else {
					_progressBar.setVisibility(View.VISIBLE);
					_state	= STATE_PLAYING;
					try {
						MediaPlayerActivity.this._mp.setDataSource("http://m1.file.xiami.com/826/14826/128121/1410562_96662_l.mp3");
						MediaPlayerActivity.this._mp.prepareAsync();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		Button btnPause = (Button)findViewById(R.id.button_pause);
		btnPause.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(MediaPlayerActivity.this._state == STATE_PLAYING) {
					MediaPlayerActivity.this._state	= STATE_PAUSED;
					MediaPlayerActivity.this._mp.pause();
				}
			}
		});
		
		Button btnStop = (Button)findViewById(R.id.button_stop);
		btnStop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MediaPlayerActivity.this._state	= STATE_STOPPED;
				MediaPlayerActivity.this._mp.stop();
				MediaPlayerActivity.this._mp.reset();
				_seekBar.setProgress(0);
			}
		});

		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if((MediaPlayerActivity.this._mp != null) && (MediaPlayerActivity.this._state == STATE_PLAYING)){
					int pos = MediaPlayerActivity.this._mp.getCurrentPosition();
					int min = (pos / 60000);
					int sec = (pos / 1000 - 60 * min);
					tv.setText(String.format("%02d:%02d", min, sec));
					_seekBar.setProgress(pos);
					_seekBar.setSecondaryProgress(pos  * 2);
				}
			}
		};

		new Thread(new Runnable(){
			@Override
			public void run() {
				while (true) {
					try {
						handler.sendMessage(new Message());
						Thread.sleep(900);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		SwitchLogger.d(LOG_TAG, "onBufferingUpdate:"+percent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
}
