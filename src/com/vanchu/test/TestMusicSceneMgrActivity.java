package com.vanchu.test;

import java.util.List;

import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.music.MusicService;
import com.vanchu.libs.music.MusicService.MusicBinder;
import com.vanchu.libs.music.MusicServiceCallback;
import com.vanchu.module.music.MusicData;
import com.vanchu.module.music.MusicScene;
import com.vanchu.module.music.MusicSceneInfo;
import com.vanchu.module.music.MusicSceneMgr;
import com.vanchu.module.music.MusicSceneMgrCallback;
import com.vanchu.module.music.SceneMusicService;
import com.vanchu.test.R;

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
import android.widget.EditText;

public class TestMusicSceneMgrActivity extends Activity {

	private static final String LOG_TAG	= TestMusicSceneMgrActivity.class.getSimpleName();
	
	private MusicSceneMgr	_musicSceneMgr		= null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_music_scene_mgr);
		
		init();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void init() {
		String requestUrl	= "http://kadovat.dev.time.bangyouxi.com/widgets/music/list.json";
		_musicSceneMgr	= new MusicSceneMgr(this, requestUrl);
		_musicSceneMgr.setMusicSceneMgrCallback(new MusicSceneMgrCallback(){
			@Override
			public void onPreloadStatusChanged(MusicScene ms, int currentStatus) {
				SwitchLogger.d(LOG_TAG, "music scene preload status changed, type="
										+ ms.getSceneType() + ", current status = " 
										+ MusicScene.downloadStatusStr(currentStatus));
			}
		});
		
		initMusicSceneMgr(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.music, menu);
		return true;
	}

	/******************音乐场景测试****************/
	public void initMusicSceneMgr(View v) {
		String sceneCfgUrl	= "http://pesiwang.devel.rabbit.oa.com/test_music_scene_cfg.php";
		_musicSceneMgr.init(sceneCfgUrl, new MusicSceneMgr.InitCallback() {
			
			@Override
			public void onSucc() {
				SwitchLogger.d(LOG_TAG, "MusicSceneMgr.InitCallback.onSucc");
			}
			
			@Override
			public void onFail() {
				SwitchLogger.d(LOG_TAG, "MusicSceneMgr.InitCallback.onFail");
			}
		});
	}
	
	public void getCurrSceneType(View v) {
		int type	= _musicSceneMgr.getCurrentSceneType();
		String msg	= "current scene type is " + type;
		SwitchLogger.d(LOG_TAG, msg);
		Tip.show(this, msg);
	}
	
	public void setCurrSceneType(View v) {
		EditText et	= (EditText)findViewById(R.id.scene_type_input);
		int inputType	= Integer.parseInt(et.getText().toString());
		SwitchLogger.d(LOG_TAG, "input scene type is " + inputType);
		_musicSceneMgr.setCurrentSceneType(inputType);
		
		String msg	= "sest current scene type, now type = " + _musicSceneMgr.getCurrentSceneType();
		SwitchLogger.d(LOG_TAG, msg);
		Tip.show(this, msg);
	}
	
	public void nextOfflineMusicData(View v) {
		SwitchLogger.d(LOG_TAG, "nextOfflineMusicData");
		MusicData md	= _musicSceneMgr.nextOfflineMusicData();
		if(null == md) {
			SwitchLogger.d(LOG_TAG, "nextOfflineMusicData return null");
			return;
		}
		
		SwitchLogger.d(LOG_TAG, "offline music data, name="+md.getName()+",player mode="+md.getPlayerMode()
				+",audio url="+md.getAudioUrl()+",audio path="+md.getAudioPath()
				+",lyric url="+md.getLyricUrl()+",lyric path="+md.getLyricPath()
				+",img url="+md.getImgUrl());
	}
	
	public void nextOnlineMusicData(View v) {
		SwitchLogger.d(LOG_TAG, "nextOnlineMusicData");
		_musicSceneMgr.nextOnlineMusicData(new MusicSceneMgr.NextOnlineCallback() {
			
			@Override
			public void onDone(MusicData md) {
				if(null == md) {
					SwitchLogger.d(LOG_TAG, "nextOnlineMusicData return null");
					return;
				}
				
				SwitchLogger.d(LOG_TAG, "online music data, name="+md.getName()+",player mode="+md.getPlayerMode()
						+",audio url="+md.getAudioUrl()+",audio path="+md.getAudioPath()
						+",lyric url="+md.getLyricUrl()+",lyric path="+md.getLyricPath()
						+",img url="+md.getImgUrl());
			}
		});
	}
	
	public void preloadDefaultMusic(View v) {
		_musicSceneMgr.preload(MusicSceneMgr.SCENE_TYPE_DEFAULT, 2);
	}
	
	public void preload2Music(View v) {
		_musicSceneMgr.preload(2, 2);
	}
	
	public void preload3Music(View v) {
		_musicSceneMgr.preload(3, 2);
	}
	
	public void preload4Music(View v) {
		_musicSceneMgr.preload(4, 2);
	}
	
	public void pausePreloadingMusic(View v) {
		_musicSceneMgr.pausePreloading();
	}
	
	public void getSceneInfo(View v) {
		List<MusicSceneInfo> list = _musicSceneMgr.getMusicSceneInfoList();
		for(int i = 0; i < list.size(); ++i) {
			MusicSceneInfo info	= list.get(i);
			SwitchLogger.d(LOG_TAG, "music scene info, type="+info.getType()
									+ ",name="+info.getName()
									+ ",max queue size="+info.getMaxQueueSize()
									+ ",isPreloading="+info.isPreloading());
		}
	}
	
	
}
