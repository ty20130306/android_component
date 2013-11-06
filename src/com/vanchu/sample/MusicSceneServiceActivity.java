package com.vanchu.sample;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.music.MusicService;
import com.vanchu.libs.music.MusicService.MusicBinder;
import com.vanchu.libs.music.MusicServiceCallback;
import com.vanchu.module.music.MusicData;
import com.vanchu.module.music.MusicDbManager;
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
import android.text.Editable;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MusicSceneServiceActivity extends Activity {

	private static final String LOG_TAG	= MusicSceneServiceActivity.class.getSimpleName();
	
	private SceneMusicService	_service		= null;
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
			_service		= (SceneMusicService)binder.getService();
			_mediaPlayer	= _service.getMediaPlayer();
			
			String	sceneCfgUrl	= "http://pesiwang.devel.rabbit.oa.com/test_music_scene_cfg.php";
			String	requestUrl	= "http://kadovat.dev.time.bangyouxi.com/widgets/music/list.json";
			_service.setRequestUrl(requestUrl);
			_service.setMusicServiceCallback(new SceneMusicServiceCallback());
			_service.initMusicSceneMgr(sceneCfgUrl, new SceneMusicService.InitMusicSceneMgrCallback() {
				
				@Override
				public void onMusicSceneInit(boolean succ, List<MusicSceneInfo> list) {
					if(succ) {
						SwitchLogger.d(LOG_TAG, "music scene init succ");
						printSceneInfo(list);
					} else {
						SwitchLogger.d(LOG_TAG, "music scene init fail");
					}
				}
				
				@Override
				public void onPreloadStatusChanged(MusicScene ms, int currentStatus) {
					SwitchLogger.d(LOG_TAG, "onPreloadStatusChanged,type="
											+ms.getSceneType()+",status="+currentStatus);
				}
				
				public void onQueueSizeChanged(MusicScene ms, int currentQueueSize) {
					SwitchLogger.d(LOG_TAG, "onQueueSizeChanged,type="
							+ms.getSceneType()+",currentQueueSize="+currentQueueSize);
				}
			});
			SwitchLogger.d(LOG_TAG, "onServiceConnected");
		}
	};
	
	private void printSceneInfo(List<MusicSceneInfo> list) {
		for(int i = 0; i < list.size(); ++i) {
			MusicSceneInfo info	= list.get(i);
			SwitchLogger.d(LOG_TAG, "music scene info, type="+info.getType()+",name="+info.getName()+",queueSize="+info.getQueueSize()
									+",max queue size="+info.getMaxQueueSize()+",is preloading="+info.isPreloading());
		}
	}
	
	private class SceneMusicServiceCallback extends MusicServiceCallback {
		
		private boolean prepared	= false;
		
		@Override
		public void onMusicPrepared(MediaPlayer mp) {
			super.onMusicPrepared(mp);
			prepared	= true;
			SwitchLogger.d(LOG_TAG, "SceneMusicServiceCallback.onMusicPrepared");
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
				//SwitchLogger.d(LOG_TAG, "SceneMusicServiceCallback.onMusicBuffering, percent="+percent);
			}
		}
		
		@Override
		public void onMusicPlaying(MediaPlayer mp) {
			super.onMusicPlaying(mp);
			//SwitchLogger.d(LOG_TAG, "SceneMusicServiceCallback.onMusicPlaying");
		}
		
		@Override
		public void onMusicCompletion(MediaPlayer mp) {
			super.onMusicCompletion(mp);
			prepared	= false;
			SwitchLogger.d(LOG_TAG, "SceneMusicServiceCallback.onMusicCompletion");
			boolean isMusicAvailable	= _service.isMusicAvailable();
			if(isMusicAvailable) {
				SwitchLogger.d(LOG_TAG, "music available");
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
			case SceneMusicService.ERR_NO_OFFLINE_MUSIC:
				Tip.show(MusicSceneServiceActivity.this, "还没有缓存过离线歌曲，请连接网络在线听歌");
				break;
				
			case SceneMusicService.ERR_REQUEST_LIST_FAIL:
				Tip.show(MusicSceneServiceActivity.this, "请求歌曲列表失败");
				break;
				
			case SceneMusicService.ERR_REQUEST_URL_NOT_SET:
				Tip.show(MusicSceneServiceActivity.this, "请设置请求歌曲列表URL");
				break;
				
			default:
				break;
			}
		}

	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_scene_service);
		
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
		Intent intent	= new Intent(this, SceneMusicService.class);
		startService(intent);
	}
	
	private void stopMusicService() {
		SwitchLogger.d(LOG_TAG, "stop music service" );
		Intent intent	= new Intent(this, SceneMusicService.class);
		stopService(intent);
	}
	
	private void bindMusicService() {
		SwitchLogger.d(LOG_TAG, "bind music service");
		_serviceBound	= true;
		Intent intent	= new Intent(this, SceneMusicService.class);
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
	
	public void dislikeIt(View v) {
		_service.dislikeMusic();
	}
	
	public void likeIt(View v) {
		_service.likeMusic();
	}
	
	public void getFavoriteList(View v) {
		MusicDbManager dbManager	= new MusicDbManager(this);
		List<MusicData> list	= dbManager.getAllMusicData();
		
		SwitchLogger.d(LOG_TAG, "get all music data, total="+list.size()+"-------------------------");
		for(int i = list.size() - 1; i >= 0; --i) {
			MusicData md	= list.get(i);
			SwitchLogger.d(LOG_TAG, i+" music data, id="+md.getId()
					+",name="+md.getName()+",player mode="+md.getPlayerMode()
					+",audio url="+md.getAudioUrl()+",audio path="+md.getAudioPath()
					+",lyric url="+md.getLyricUrl()+",lyric path="+md.getLyricPath()
					+",img url="+md.getImgUrl());
		}
		
		dbManager.close();
	}
	
	public void setCurrentSceneType(View v) {
		EditText et	= (EditText)findViewById(R.id.scene_type_input);
		String s	= et.getText().toString();
		if(s.equals("")) {
			Tip.show(this, "请输入目标scene type");
			return ;
		}
		int sceneType	= Integer.parseInt(s);
		SwitchLogger.d(LOG_TAG, "set current scene type to " + sceneType);
		_service.setCurrentSceneType(sceneType);
		Tip.show(this, "set current scene type to " + sceneType);
	}
	
	public void getMaxQueueSize(View v) {
		EditText et	= (EditText)findViewById(R.id.scene_type_input);
		String s	= et.getText().toString();
		if(s.equals("")) {
			Tip.show(this, "请输入目标scene type");
			return ;
		}
		int sceneType	= Integer.parseInt(s);
		int maxQueueSize = _service.getMaxQueueSize(sceneType);
		SwitchLogger.d(LOG_TAG, "get max queue size, scene type="+sceneType+", maxQueueSize="+maxQueueSize);
		Tip.show(this, "get max queue size, scene type="+sceneType + ", maxQueueSize="+maxQueueSize);
	}
	
	public void getQueueSize(View v) {
		EditText et	= (EditText)findViewById(R.id.scene_type_input);
		String s	= et.getText().toString();
		if(s.equals("")) {
			Tip.show(this, "请输入目标scene type");
			return ;
		}
		int sceneType	= Integer.parseInt(s);
		int queueSize = _service.getQueueSize(sceneType);
		SwitchLogger.d(LOG_TAG, "get queue size, scene type="+sceneType+", queueSize="+queueSize);
		Tip.show(this, "get queue size, scene type="+sceneType + ", queueSize="+queueSize);
	}
	
	public void getCurrentSceneType(View v) {
		int sceneType	= _service.getCurrentSceneType();
		SwitchLogger.d(LOG_TAG, "current scene type is " + sceneType);
		Tip.show(this, "current scene type is " + sceneType);
	}
	
	public void preload(View v) {
		EditText et	= (EditText)findViewById(R.id.preload_num_input);
		String s	= et.getText().toString();
		if(s.equals("")) {
			Tip.show(this, "请输入需要预加载的数量");
			return ;
		}
		
		int num	= Integer.parseInt(s);
		int sceneType	= _service.getCurrentSceneType();
		SwitchLogger.d(LOG_TAG, "start to preload scene type " + sceneType);
		Tip.show(this, "start to preload scene type " + sceneType);
		_service.preload(sceneType, num);
	}
	
}
