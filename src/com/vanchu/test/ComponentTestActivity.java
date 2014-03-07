package com.vanchu.test;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vanchu.libs.webCache.WebCache;
import com.vanchu.libs.addressBook.AddressBookActivity;
import com.vanchu.libs.common.container.DeadList;
import com.vanchu.libs.common.container.DeadList.DeadListCallback;
import com.vanchu.libs.common.container.SolidQueue;
import com.vanchu.libs.common.container.SolidQueue.SolidQueueCallback;
import com.vanchu.libs.common.task.Downloader;
import com.vanchu.libs.common.task.Downloader.IDownloadListener;
import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.ActivityUtil;
import com.vanchu.libs.common.util.NetUtil;
import com.vanchu.libs.common.util.SharedPrefsUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.common.util.TimeUtil;
import com.vanchu.libs.push.PushParam;
import com.vanchu.libs.push.PushRobot;
import com.vanchu.libs.upgrade.UpgradeCallback;
import com.vanchu.libs.upgrade.UpgradeManager;
import com.vanchu.libs.upgrade.UpgradeParam;
import com.vanchu.libs.upgrade.UpgradeProxy;
import com.vanchu.module.music.MusicSolidQueueElement;
import com.vanchu.module.music.VanchuMusicService;
import com.vanchu.sample.AccountSystemActivity;
import com.vanchu.sample.AddressActivity;
import com.vanchu.sample.AnimationActivity;
import com.vanchu.sample.DbActivity;
import com.vanchu.sample.GestureLockSampleActivity;
import com.vanchu.sample.ImageLoadSampleActivity;
import com.vanchu.sample.LocationActivity;
import com.vanchu.sample.MusicSceneServiceActivity;
import com.vanchu.sample.MusicServiceActivity;
import com.vanchu.sample.PictureBrowserSampleActivity;
import com.vanchu.sample.QqSdkSampleActivity;
import com.vanchu.sample.ScrollListViewSampleActivity;
import com.vanchu.sample.SinaSdkSampleActivity;
import com.vanchu.sample.SmileSampleActivity;
import com.vanchu.sample.VanchuTabSampleActivity;
import com.vanchu.sample.WebViewActivity;
import com.vanchu.sample.slipping.GuimiSlippingActivity;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ComponentTestActivity extends Activity {
	public static final String SONG_PACKAGE_NAME = "com.vanchu.apps.bangyouxi.plugins.song";
	public static final String SONG_INDEX_ACTIVITY = "com.vanchu.apps.bangyouxi.plugins.song.SongActivity";

	private static final String LOG_TAG = ComponentTestActivity.class
			.getSimpleName();

	private ProgressDialog _progressDialog;
	Downloader _downloader = null;
	private long lastBackKeyPressedTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.component_test);
		SwitchLogger.setPrintLog(true);
		Log.d(LOG_TAG,
				"current version name="
						+ ActivityUtil.getCurrentVersionName(this));

		// testWebView(null);
		// testAnimation(null);
		// testNetwork(null);
		// testMediaPlayer(null);
		// testMusicService(null);
		// testMusicSceneService(null);
		// testPushService(null);
		// testWebCache(null);
		// testSqlLite(null);
		// testPictureBrowser(null);
		// testCachedImageLoader(null);
		
		//another(null);
	}
	
	public void another(View v) {
		gotoActivity(AnotherTestActivity.class);
	}
	
	/**
	  * 将4字节的byte数组转成一个int值
	  * @param b
	  * @return
	  */
	public static int byteArray2int(byte[] b){
	    byte[] a = new byte[4];
	    int i = a.length - 1,j = b.length - 1;
	    for (; i >= 0; i--,j--) {//从b的尾部(即int值的低位)开始copy数据
	        if(j >= 0)
	            a[i] = b[j];
	        else
	            a[i] = 0;//如果b.length不足4,则将高位补0
	    }
	    int v0 = (a[0] & 0xff) << 24;//&0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位
	    int v1 = (a[1] & 0xff) << 16;
	    int v2 = (a[2] & 0xff) << 8;
	    int v3 = (a[3] & 0xff) ;
	    return v0 + v1 + v2 + v3;
	}
	
	public void testByteBuffer(View v) {
		long start = System.currentTimeMillis();
		//for(int c = 0; c <= 10000; ++c) {
		ByteBuffer bb	= ByteBuffer.allocate(36);
		byte[] byteArr	= bb.array();
		SwitchLogger.d(LOG_TAG, "byte arr length="+byteArr.length+",bb.position="+bb.position()
				+",bb.remaining="+bb.remaining()+",bb.limit="+bb.limit());
		bb.putInt(15);
		SwitchLogger.d(LOG_TAG, "byte arr length="+byteArr.length+",bb.position="+bb.position()
				+",bb.remaining="+bb.remaining()+",bb.limit="+bb.limit());

		byte[] data	= new byte[4];
		bb.get(data);
		int result	= byteArray2int(data);
		SwitchLogger.d(LOG_TAG, "byte arr length="+byteArr.length+",bb.position="+bb.position()
				+",bb.remaining="+bb.remaining()+",bb.limit="+bb.limit()+",result="+result);

		bb.position(0);
		bb.get(data);
		result	= byteArray2int(data);
		SwitchLogger.d(LOG_TAG, "byte arr length="+byteArr.length+",bb.position="+bb.position()
				+",bb.remaining="+bb.remaining()+",bb.limit="+bb.limit()+",result="+result);

		bb.position(4);
		SwitchLogger.d(LOG_TAG, "byte arr length="+byteArr.length+",bb.position="+bb.position()
				+",bb.remaining="+bb.remaining()+",bb.limit="+bb.limit()+",result="+result);
		//}
		long end = System.currentTimeMillis();
		SwitchLogger.d(LOG_TAG, "use "+(end-start));
	}
	
	public void testGestureLock(View v) {
		gotoActivity(GestureLockSampleActivity.class);
	}
	
	public void testCachedImageLoader(View v) {
		gotoActivity(TestCachedImageLoaderActivity.class);
	}
	
	public void getPhoneInfo(View v) {
		SwitchLogger.d(LOG_TAG, "机型:"+Build.MODEL+",SDK版本："+Build.VERSION.SDK_INT
								+",系统版本："+Build.VERSION.RELEASE);
		
		Display display	= getWindowManager().getDefaultDisplay();
		int screenHeight	= display.getHeight();
		int screenWidth		= display.getWidth();
		SwitchLogger.d(LOG_TAG, "屏幕高度:"+screenHeight+",屏幕宽度："+screenWidth);
	}

	public void testImageLoad(View v) {
		gotoActivity(ImageLoadSampleActivity.class);
	}

	public void testScrollListView(View v) {
		gotoActivity(ScrollListViewSampleActivity.class);
	}

	public void testSmile(View v) {
		gotoActivity(SmileSampleActivity.class);
	}

	public void testSlipping(View v) {
		gotoActivity(GuimiSlippingActivity.class);
	}

	public void testPictureBrowser(View v) {
		gotoActivity(PictureBrowserSampleActivity.class);
	}

	public void testAccountSystem(View v) {
		gotoActivity(AccountSystemActivity.class);
	}

	public void testSinaSdk(View v) {
		gotoActivity(SinaSdkSampleActivity.class);
	}

	public void testQqSdk(View v) {
		gotoActivity(QqSdkSampleActivity.class);
	}

	private void gotoActivity(Class<?> cls) {
		Intent intent = new Intent(this, cls);
		startActivity(intent);
	}

	public void testVanchuTab(View v) {
		Intent intent = new Intent(this, VanchuTabSampleActivity.class);
		startActivity(intent);
	}

	public void testEventCenter(View v) {
		Intent intent = new Intent(this, TestEventCenterActivity.class);
		startActivity(intent);
	}

	public void testObjCmp(View v) {
		Father f = new Father(1);
		Father ff = new Father(2);
		Father f2 = new Father(2);

		Father fr = f;

		SwitchLogger.d(LOG_TAG, "f == f is " + cmp(f, f));
		SwitchLogger.d(LOG_TAG, "f == f2 is " + cmp(f, f2));
		SwitchLogger.d(LOG_TAG, "f == fr is " + cmp(f, fr));
		SwitchLogger.d(LOG_TAG, "f == ff is " + cmp(f, ff));
	}

	private boolean cmp(Object a, Object b) {
		return (a == b);
	}

	public void testTime(View v) {
		long time = System.currentTimeMillis();
		SwitchLogger.d(LOG_TAG, "System.currentTimeMillis()=" + time);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		SwitchLogger.d(LOG_TAG, "get from calendar," + year + "-" + (month + 1)
				+ "-" + day + " " + hour + ":" + minute + ":" + second);
		GregorianCalendar gcalenar = new GregorianCalendar(year, month, day,
				hour, minute, second);

		SwitchLogger.d(LOG_TAG,
				"gcalenar.getTimeInMillis()=" + gcalenar.getTimeInMillis());

		SwitchLogger
				.d(LOG_TAG, "tomorrow morning=============================");
		GregorianCalendar gcalenar2 = new GregorianCalendar(year, month,
				day + 1, 0, 0, 0);
		SwitchLogger.d(LOG_TAG,
				"gcalenar2.getTimeInMillis()=" + gcalenar2.getTimeInMillis());

		calendar.setTimeInMillis(gcalenar2.getTimeInMillis());
		int year2 = calendar.get(Calendar.YEAR);
		int month2 = calendar.get(Calendar.MONTH);
		int day2 = calendar.get(Calendar.DAY_OF_MONTH);
		int hour2 = calendar.get(Calendar.HOUR_OF_DAY);
		int minute2 = calendar.get(Calendar.MINUTE);
		int second2 = calendar.get(Calendar.SECOND);
		SwitchLogger.d(LOG_TAG, "get from calendar2," + year2 + "-"
				+ (month2 + 1) + "-" + day2 + " " + hour2 + ":" + minute2 + ":"
				+ second2);

		SwitchLogger.d(LOG_TAG, "TimeUtil====================================");
		long ndts = TimeUtil.nextDayTimestamp();
		SwitchLogger.d(LOG_TAG, "nextDayTimestamp=" + ndts);
		SwitchLogger.d(LOG_TAG,
				"next date=" + TimeUtil.timestampToDateStr(ndts));
	}

	public void testKvDb(View v) {
		Intent intent = new Intent(this, TestKvDbActivity.class);
		startActivity(intent);
	}

	public void testListRemove(View v) {
		ArrayList<String> list = new ArrayList<String>();
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("d");
		list.add("e");

		String r = list.remove(0);
		SwitchLogger.d(LOG_TAG, r + " is be removed");

		r = list.remove(0);
		SwitchLogger.d(LOG_TAG, r + " is be removed");

		r = list.remove(0);
		SwitchLogger.d(LOG_TAG, r + " is be removed");

	}

	class Father {
		private int type = 1;

		public Father(int t) {
			type = t;
		}

		public void setType(int t) {
			type = t;
		}

		public int getType() {
			return type;
		}

		public void preload() {
			SwitchLogger.d(LOG_TAG, "father.preload");
		}

		// @Override
		// public boolean equals(Object obj) {
		// return false;
		// }
	}

	class Son extends Father {
		public Son(int t) {
			super(t);
		}

		public void preload() {
			SwitchLogger.d(LOG_TAG, "son.preload");
		}
	}

	public void testExtend(View v) {
		Father f = new Son(2);
		SwitchLogger.d(LOG_TAG, "f.getType=" + f.getType());
		f.preload();
	}

	class A {
		private int _i;
		private String _s;

		public A(int i, String s) {
			_i = i;
			_s = s;
		}

		public String getS() {
			return _s;
		}

		public int getI() {
			return _i;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}

			if (o == null) {
				return false;
			}

			if (this.getClass() != o.getClass()) {
				return false;
			}

			A another = (A) o;
			if (_s.equals(another.getS()) && _i == another.getI()) {
				return true;
			} else {
				return false;
			}
		}
	}

	public void testHashMap(View v) {
		A a = new A(1, "a");
		A a2 = new A(2, "aa");
		Map<String, A> map = new HashMap<String, ComponentTestActivity.A>();
		map.put("a", a);
		map.put("aa", a2);
		A aa = new A(2, "aa");
		if (map.containsValue(aa)) {
			SwitchLogger.d(LOG_TAG, "a2 == aa");
		} else {
			SwitchLogger.d(LOG_TAG, "a2 != aa");
		}
	}

	public void testMusicSceneMgr(View v) {
		Intent intent = new Intent(this, TestMusicSceneMgrActivity.class);
		startActivity(intent);
	}

	public void testCfgCenter(View v) {
		Intent intent = new Intent(this, TestCfgMgrActivity.class);
		startActivity(intent);
	}

	private void printJsonObj(JSONObject obj) throws JSONException {
		String query = obj.getString("query");
		SwitchLogger.d(LOG_TAG, "query=" + query);

		JSONArray arr = obj.getJSONArray("locations");
		for (int i = 0; i < arr.length(); ++i) {
			SwitchLogger.d(LOG_TAG, "locations[" + i + "]=" + arr.getInt(i));
		}
	}

	public void testSerializeJson(View v) {
		try {
			String str = "{\"query\":\"Pizza\",\"locations\":[94043,90210]}";
			JSONObject obj = new JSONObject(str);
			printJsonObj(obj);

			SwitchLogger.d(LOG_TAG, "to string-->to JSONObject");

			String newStr = obj.toString();
			SwitchLogger.d(LOG_TAG, "new string=" + newStr);
			JSONObject newObj = new JSONObject(newStr);
			printJsonObj(newObj);
		} catch (JSONException e) {
			SwitchLogger.e(e);
		}
	}

	public void testSqlLite(View v) {
		Intent intent = new Intent(this, DbActivity.class);
		startActivity(intent);
	}

	public void testMusicService(View v) {
		Intent intent = new Intent(this, MusicServiceActivity.class);
		startActivity(intent);
	}

	public void testMusicSceneService(View v) {
		Intent intent = new Intent(this, MusicSceneServiceActivity.class);
		startActivity(intent);
	}

	public void testNetwork(View v) {
		if (NetUtil.isConnected(this)) {
			SwitchLogger.d(LOG_TAG, "----------network is connected");
		} else {
			SwitchLogger.d(LOG_TAG, "----------network is not connected");
		}

		if (NetUtil.isFastNetwork(this)) {
			SwitchLogger.d(LOG_TAG, "----------network is fast");
		} else {
			SwitchLogger.d(LOG_TAG, "----------network is not fast");
		}

		SwitchLogger.d(LOG_TAG,
				"----------network type is:" + NetUtil.getNetworkType(this));

		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		SwitchLogger.d(LOG_TAG, "----------TelephonyManager network type is:"
				+ telephonyManager.getNetworkType());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:

			long currTime = System.currentTimeMillis();
			if (currTime - lastBackKeyPressedTime > 2000) {
				Tip.show(this, "再次按返回键退出游戏");
				lastBackKeyPressedTime = currTime;
			} else {
				SwitchLogger.d(LOG_TAG, "stop music service");
				Intent intent = new Intent(this, VanchuMusicService.class);
				stopService(intent);
				finish();
			}
			return true;

		default:
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void testWebView(View v) {
		Intent intent = new Intent(this, WebViewActivity.class);
		startActivity(intent);
	}

	public void testScaleCrop(View v) {
		Intent intent = new Intent(this, CropActivity.class);
		startActivity(intent);
	}

	private void initProgressDialog() {
		_progressDialog = new ProgressDialog(this);
		_progressDialog.setCancelable(false);
		_progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		_progressDialog.setMax(100);
		_progressDialog.setTitle("下载进度");
		_progressDialog.setMessage("正在准备下载安装包");
	}

	private void printsq(LinkedList<String> sq) {
		for (int i = 0; i < sq.size(); ++i) {
			SwitchLogger.d(LOG_TAG, "sq[" + i + "]=" + sq.get(i));
		}
	}

	// private void testStringSolidQueue() {
	// SolidQueue<String> sq = new SolidQueue<String>(this, "test", 10);
	//
	// SwitchLogger.d(LOG_TAG, "1 from file --------------------------");
	// printsq(sq.getQueue());
	// new Thread(){
	// public void run() {
	// SolidQueue<String> sq2 = new
	// SolidQueue<String>(ComponentTestActivity.this, "test", 10);
	// sq2.enqueue("aa");
	// sq2.enqueue("bb");
	// sq2.enqueue("cc");
	// sq2.enqueue("dd");
	// sq2.enqueue("ee");
	// sq2.enqueue("ff");
	// }
	// }.start();
	//
	// sq.enqueue("a");
	// sq.enqueue("b");
	// sq.enqueue("c");
	// sq.enqueue("e");
	// sq.enqueue("d");
	// sq.enqueue("f");
	//
	// SwitchLogger.d(LOG_TAG, "2 from memory --------------------------");
	// printsq(sq.getQueue());
	//
	// sq = new SolidQueue<String>(this, "test", 10);
	// SwitchLogger.d(LOG_TAG, "3 from file --------------------------");
	// printsq(sq.getQueue());
	// }
	//

	private void printMysq(LinkedList<MyItem> sq) {
		if (sq.size() == 0) {
			SwitchLogger.d(LOG_TAG, "sq is empty");
			return;
		}

		for (int i = 0; i < sq.size(); ++i) {
			MyItem item = sq.get(i);
			SwitchLogger.d(LOG_TAG, "sq[" + i + "]:" + item.get_id() + ","
					+ item.get_name() + "," + item.get_url());
		}
	}

	private void testMySolidQueue() {
		SolidQueueCallback<MyItem> callback = new SolidQueueCallback<MyItem>() {
			public void onAdd(MyItem element) {
				SwitchLogger.e(LOG_TAG, "onAdd, id=" + element.get_id());
			}

			public void onRemove(MyItem element) {
				SwitchLogger.e(LOG_TAG, "onRemove, id=" + element.get_id());
			}
		};

		SolidQueue<MyItem> sq = new SolidQueue<MyItem>(this, "my_queue", 5,
				callback);

		SwitchLogger.d(LOG_TAG, "1 from file --------------------------");
		printMysq(sq.getQueue());
//		SwitchLogger.d(LOG_TAG, "1 from file, reverse ------------------");
//		printMysq(sq.getReverseQueue());

		// new Thread(){
		// public void run() {
		// SolidQueue<MyItem> sq2 = new
		// SolidQueue<MyItem>(ComponentTestActivity.this, "test", 10);
		// sq2.enqueue("aa");
		// sq2.enqueue("bb");
		// sq2.enqueue("cc");
		// sq2.enqueue("dd");
		// sq2.enqueue("ee");
		// sq2.enqueue("ff");
		// sq2.solidify();
		// }
		// }.start();

		sq.enqueue(new MyItem("http://", "a", "pesi"));
		sq.enqueue(new MyItem("http://", "b", "pesi"));
		
//		
//		LinkedList<MyItem> linkList	= new LinkedList<MyItem>();
//		linkList.add(new MyItem("http://", "linked list 0", "pesi"));
//		linkList.add(new MyItem("http://", "linked list 1", "pesi"));
//		sq.addAll(linkList, true);
		
//		SwitchLogger.d(LOG_TAG, "2 after clear --------------------------");
//		printMysq(sq.getQueue());
//		
//		sq.enqueue(new MyItem("http://", "c", "pesi"));
//		sq.enqueue(new MyItem("http://", "d", "pesi"));
//		sq.enqueue(new MyItem("http://", "e", "pesi"));
//		sq.enqueue(new MyItem("http://", "f", "pesi"));
//		sq.enqueue(new MyItem("http://", "g", "pesi"));
//		sq.enqueue(new MyItem("http://", "h", "pesi"));

		SwitchLogger.d(LOG_TAG, "3 from memory --------------------------");
		printMysq(sq.getQueue());
		
		sq.destroy();
		SwitchLogger.d(LOG_TAG, "3 from memory, reverse ------------------");
		printMysq(sq.getReverseQueue());
		
		sq = new SolidQueue<MyItem>(this, "my_queue", 5, callback);
		SwitchLogger.d(LOG_TAG, "4 from file --------------------------");
		printMysq(sq.getQueue());
		
//		ArrayList<MyItem> arrList	= new ArrayList<MyItem>();
//		arrList.add(new MyItem("http://", "array list 0", "pesi"));
//		arrList.add(new MyItem("http://", "array list 1", "pesi"));
//		sq.addAll(arrList, false);
	}

	private void printMyList(LinkedList<MyItem> sl) {
		if (sl.size() == 0) {
			SwitchLogger.d(LOG_TAG, "solid list is empty");
			return;
		}

		for (int i = 0; i < sl.size(); ++i) {
			MyItem item = sl.get(i);
			SwitchLogger.d(LOG_TAG, "sq[" + i + "]:" + item.get_id() + ","
					+ item.get_name() + "," + item.get_url());
		}
	}

	private void testMyDeadList() {
		DeadListCallback<MyItem> callback = new DeadListCallback<MyItem>() {
			public void onAdd(MyItem element) {
				SwitchLogger.e(LOG_TAG, "onAdd, id=" + element.get_id());
			}

			public void onRemove(MyItem element) {
				SwitchLogger.e(LOG_TAG, "onRemove, id=" + element.get_id());
			}
		};

		DeadList<MyItem> dl = new DeadList<MyItem>(this, "my_list", 5, callback);
		
		SwitchLogger.d(LOG_TAG, "1 from file --------------------------");
		printMyList(dl.getList());
//		dl.clear();
		
//		dl.add(new MyItem("http://", "a", "pesi"));
//		dl.add(new MyItem("http://", "b", "pesi"));
//		dl.add(new MyItem("http://", "c", "pesi"));

//		dl.add(new MyItem("http://", "d", "pesi"));
//		dl.add(new MyItem("http://", "e", "pesi"));
//		dl.add(new MyItem("http://", "f", "pesi"));
		
		ArrayList<MyItem> tmpList	= new ArrayList<MyItem>();
		tmpList.add(new MyItem("http://", "d", "pesi"));
		tmpList.add(new MyItem("http://", "e", "pesi"));
		tmpList.add(new MyItem("http://", "f", "pesi"));
		dl.addAll(tmpList);
		dl.addAll(tmpList);
		dl.add(new MyItem("http://", "a", "pesi"));
		
		SwitchLogger.d(LOG_TAG, "2 from memory --------------------------");
		printMyList(dl.getList());
		
		dl.destroy();
		SwitchLogger.d(LOG_TAG, "2 from memory --------------------------");
		printMyList(dl.getList());
//
//		dl.remove(new MyItem("http://", "c", "pesi"));
//		SwitchLogger.d(LOG_TAG, "3 after remove(c) ---------------");
//		printMyList(dl.getList());
//		
//		dl.remove(3);
//		SwitchLogger.d(LOG_TAG, "4 after remove(3) ---------------");
//		printMyList(dl.getList());
//		
//		dl.clear();
		dl = new DeadList<MyItem>(this, "my_list", 2, callback);
		SwitchLogger.d(LOG_TAG, "5 from file --------------------------");
		printMysq(dl.getList());
//		
//		dl.add(new MyItem("http://", "a", "pesi"));
//		dl.add(new MyItem("http://", "b", "pesi"));
//		dl.add(new MyItem("http://", "c", "pesi"));
//		printMysq(dl.getList());
	}

	public void testAnimation(View v) {
		Intent intent = new Intent(this, AnimationActivity.class);
		startActivity(intent);
	}

	public void testMediaPlayer(View v) {
		Intent intent = new Intent(this, MediaPlayerActivity.class);
		startActivity(intent);
	}

	public void testSolidQueue(View v) {
		// testStringSolidQueue();
		testMySolidQueue();
	}

	public void testDeadList(View v) {
		// testStringSolidQueue();
		testMyDeadList();
	}

	class TestDownloadListener implements IDownloadListener {

		@Override
		public void onStart() {

		}

		@Override
		public void onProgress(long downloaded, long total) {
			ProgressBar progressBar = (ProgressBar) findViewById(R.id.downloader_progress_bar);
			progressBar.setProgress((int) (downloaded * 100 / total));

			SwitchLogger.d(LOG_TAG, "downloaded " + downloaded);
		}

		@Override
		public void onSuccess(String downloadFile) {
			SwitchLogger.d(LOG_TAG, "download " + downloadFile + " complete");
			_downloader = null;
		}

		@Override
		public void onError(int errCode) {
			SwitchLogger.d(LOG_TAG, "download error, errCode = " + errCode);
			_downloader = null;
		}

		@Override
		public void onPause() {
			SwitchLogger.d(LOG_TAG, "download paused");
		}
	}

	public void testDownloaderRun(View v) {
		SwitchLogger.setPrintLog(true);

		if (null == _downloader) {
			SwitchLogger.d(LOG_TAG, "------------------start to download");
			_downloader = new Downloader(this,
					"http://pesiwang.devel.rabbit.oa.com/study.apk", "test",
					new TestDownloadListener());
			_downloader.run();
		} else {
			SwitchLogger.d(LOG_TAG, "------------------continue to download");
			_downloader.run();
		}
	}

	public void testDownloaderPause(View v) {
		SwitchLogger.d(LOG_TAG, "------------------testDownloaderPause");
		_downloader.pause();
	}

	public void testPluginSystem(View v) {
		SwitchLogger.setPrintLog(true);

		Intent intent = new Intent(this, TestPluginSystemActivity.class);
		startActivity(intent);
	}

	public void testWebCache(View v) {
		Intent intent = new Intent(this, WebCacheActivity.class);
		startActivity(intent);
	}

	public void startApp(View v) {
		SwitchLogger.setPrintLog(true);
		ActivityUtil.startApp(this, SONG_PACKAGE_NAME);
	}

	public void startApp2(View v) {
		SwitchLogger.setPrintLog(true);
		ActivityUtil.startApp(this, SONG_PACKAGE_NAME, SONG_INDEX_ACTIVITY);
	}

	public void goToSecond(View v) {
		Intent intent = new Intent(this, SecondActivity.class);
		startActivity(intent);

		Map<String, String> param = new HashMap<String, String>();
		param.put("a", "b");
		param.put("a", "c");
		param.put("a", "d");

		SwitchLogger.d(LOG_TAG, "result:" + (String) param.get("a"));
	}

	public void testPushService(View v) {
		SwitchLogger.d(LOG_TAG, "testPushService()");

		PushParam pushParam = PushRobot.getPushParam(this);

		String msgUrl = "http://pesiwang.devel.rabbit.oa.com/test_push_msg.php";
		pushParam.setMsgUrl(msgUrl);

		Map<String, String> msgUrlParam = pushParam.getMsgUrlParam();
		msgUrlParam.put("name", "wolf");
		pushParam.setMsgUrlParam(msgUrlParam);

		pushParam.setIgnoreIntervalLimit(true);
		pushParam.setMsgInterval(3000);
		pushParam.setNotifyWhenRunning(true);
		pushParam.setDefaults(Notification.DEFAULT_ALL);

		PushRobot.run(this, TestPushService.class, pushParam);
	}

	public void testPutStringMap(View v) {
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "testPutStringMap()");
		HashMap<String, String> testMap = new HashMap<String, String>();
		testMap.put("name", "wolf");
		testMap.put("age", "27");
		SharedPrefsUtil.putStringMap(this, "test", testMap);
		HashMap<String, String> m = SharedPrefsUtil.getStringMap(this, "test");

		SwitchLogger.d(LOG_TAG, "size:" + m.size() + "," + m.toString());

	}

	public void feedback(View v) {
		Intent intent = new Intent(this, TestFeedbackActivity.class);
		startActivity(intent);
	}

	public void addressBookClick(View v) {
		Intent intent = new Intent(this, AddressActivity.class);
		startActivity(intent);
	}
	
	public void locationClick(View v) {
		Intent intent = new Intent(this, LocationActivity.class);
		startActivity(intent);
	}
	
	
	public void checkUpgradeUIManager(View v) {
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "checkUpgradeManager");

		UpgradeParam param = new UpgradeParam(
				ActivityUtil.getCurrentVersionName(this), "1.0.3", "1.0.7",
				"http://pesiwang.devel.rabbit.oa.com/component.1.0.4.apk",
				"升级详细内容");

		class MyCallback extends UpgradeCallback {

			public MyCallback(Context context) {
				super(context);
			}

			@Override
			public void onDownloadStarted() {
				initProgressDialog();
			}

			@Override
			public void onDownloadProgress(long downloaded, long total) {
				_progressDialog.setProgress((int) (downloaded * 100 / total));
				String tip = String.format("正在下载安装包...\n已下载: %d K\n总大小: %d K",
						(int) (downloaded / 1024), (int) (total / 1024));

				_progressDialog.setMessage(tip);
			}

			@Override
			public void onComplete(int result) {
				_progressDialog.dismiss();
			}

			public void exitApp() {
				SwitchLogger.d(LOG_TAG, "implement exitApp");
			}
		}

		class MyUpgradeManager extends UpgradeManager {
			public MyUpgradeManager(Context context, UpgradeParam param,
					UpgradeCallback callback) {
				super(context, param, callback);
			}

			@Override
			protected Dialog createDetailDialog() {
				Dialog dialog = new Dialog(getContext());
				View view = LayoutInflater.from(getContext()).inflate(
						R.layout.dialog, null);
				Button yes = (Button) view.findViewById(R.id.dialog_upgrade);

				yes.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MyUpgradeManager.this.chooseToUpgrade();
					}
				});

				Button no = (Button) view.findViewById(R.id.dialog_ignore);
				no.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MyUpgradeManager.this.choosetToSkip();
					}
				});

				TextView text = (TextView) view.findViewById(R.id.dialog_text);
				UpgradeParam param = getParam();
				SwitchLogger.e(LOG_TAG, text + "  xxxxxxxxxxxxxxxxxxxxxx   "
						+ param);
				text.setText(param.getCurrentVersionName() + ","
						+ param.getHighestVersionName() + ","
						+ param.getUpgradeDetail());
				SwitchLogger.e(LOG_TAG, "yyyyyyyyyyyyyyyyyyyyyyyyyy");
				dialog.setContentView(view);

				return dialog;
			}
		}

		new MyUpgradeManager(this, param, new MyCallback(this)).check();

	}

	public void checkUpgradeManager(View v) {
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "checkUpgradeManager");

		UpgradeParam param = new UpgradeParam(
				ActivityUtil.getCurrentVersionName(this), "1.0.3", "1.0.7",
				"http://pesiwang.devel.rabbit.oa.com/component.1.0.4.apk",
				"升级详细内容");

		initProgressDialog();

		class MyCallback extends UpgradeCallback {

			public MyCallback(Context context) {
				super(context);
			}

			@Override
			public void onDownloadStarted() {
				_progressDialog.show();
			}

			@Override
			public void onDownloadProgress(long downloaded, long total) {
				_progressDialog.setProgress((int) (downloaded * 100 / total));
				String tip = String.format("正在下载安装包...\n已下载: %d K\n总大小: %d K",
						(int) (downloaded / 1024), (int) (total / 1024));

				_progressDialog.setMessage(tip);
			}

			@Override
			public void onComplete(int result) {
				_progressDialog.dismiss();
			}

			public void exitApp() {
				SwitchLogger.d(LOG_TAG, "implement exitApp");
			}
		}

		new UpgradeManager(this, param, new MyCallback(this)).check();

	}

	public void upgradeUIProxy(View view) {
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "checkUpgradeProxy");

		class MyCallback extends UpgradeCallback {

			public MyCallback(Context context) {
				super(context);
			}

			@Override
			public void onDownloadStarted() {
				initProgressDialog();
			}

			@Override
			public void onDownloadProgress(long downloaded, long total) {
				_progressDialog.setProgress((int) (downloaded * 100 / total));
				String tip = String.format("正在下载安装包...\n已下载: %d K\n总大小: %d K",
						(int) (downloaded / 1024), (int) (total / 1024));

				_progressDialog.setMessage(tip);
			}

			@Override
			public void onComplete(int result) {
				_progressDialog.dismiss();
			}

			public void exitApp() {
				SwitchLogger.d(LOG_TAG, "implement exitApp");
			}

			@Override
			public UpgradeParam onUpgradeInfoResponse(String response) {
				try {
					JSONObject jsonResponse = new JSONObject(response);
					String lowest = jsonResponse.getString("lowest");
					String highest = jsonResponse.getString("highest");
					String url = jsonResponse.getString("apkUrl");
					String detail = jsonResponse.getString("detail");

					SwitchLogger.d(LOG_TAG, "receive info, lowest version:"
							+ lowest + ", highest version: " + highest);
					SwitchLogger.d(LOG_TAG, "receive info, apkUrl: " + url
							+ ", detail: " + detail);

					String current = ActivityUtil
							.getCurrentVersionName(getContext());
					SwitchLogger.d(LOG_TAG, "current version: " + current);

					return new UpgradeParam(current, lowest, highest, url,
							detail);
				} catch (JSONException e) {
					SwitchLogger.e(e);

					return null;
				}
			}
		}

		class MyUpgradeManager extends UpgradeManager {
			public MyUpgradeManager(Context context, UpgradeParam param,
					UpgradeCallback callback) {
				super(context, param, callback);
			}

			@Override
			protected Dialog createDetailDialog() {
				Dialog dialog = new Dialog(getContext());
				View view = LayoutInflater.from(getContext()).inflate(
						R.layout.dialog, null);
				Button yes = (Button) view.findViewById(R.id.dialog_upgrade);

				yes.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MyUpgradeManager.this.chooseToUpgrade();
					}
				});

				Button no = (Button) view.findViewById(R.id.dialog_ignore);
				no.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MyUpgradeManager.this.choosetToSkip();
					}
				});

				TextView text = (TextView) view.findViewById(R.id.dialog_text);
				UpgradeParam param = getParam();
				SwitchLogger.e(LOG_TAG, text + "  xxxxxxxxxxxxxxxxxxxxxx   "
						+ param);
				text.setText(param.getCurrentVersionName() + ","
						+ param.getHighestVersionName() + ","
						+ param.getUpgradeDetail());
				SwitchLogger.e(LOG_TAG, "yyyyyyyyyyyyyyyyyyyyyyyyyy");
				dialog.setContentView(view);

				return dialog;
			}
		}

		class MyUpgradeProxy extends UpgradeProxy {
			public MyUpgradeProxy(Context context, String upgradeInfoUrl,
					UpgradeCallback callback) {
				super(context, upgradeInfoUrl, callback);
			}

			@Override
			protected UpgradeManager createUpgradeManager(Context context,
					UpgradeParam param, UpgradeCallback callback) {
				return new MyUpgradeManager(context, param, callback);
			}
		}

		new MyUpgradeProxy(this, "http://pesiwang.devel.rabbit.oa.com/t.php",
				new MyCallback(this)).check();

	}

	public void upgradeProxy(View view) {

		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "checkUpgradeProxy");
		initProgressDialog();
		class MyCallback extends UpgradeCallback {

			public MyCallback(Context context) {
				super(context);
			}

			@Override
			public void onDownloadStarted() {
				_progressDialog.show();
			}

			public void onLatestVersion() {
				super.onLatestVersion();
				Tip.show(getContext(), "当前已经是最新版本");
			}

			@Override
			public void onDownloadProgress(long downloaded, long total) {
				_progressDialog.setProgress((int) (downloaded * 100 / total));
				String tip = String.format("正在下载安装包...\n已下载: %d K\n总大小: %d K",
						(int) (downloaded / 1024), (int) (total / 1024));

				_progressDialog.setMessage(tip);
			}

			@Override
			public void onComplete(int result) {
				_progressDialog.dismiss();
			}

			public void exitApp() {
				SwitchLogger.d(LOG_TAG, "implement exitApp");
				((Activity) getContext()).finish();
			}

			@Override
			public UpgradeParam onUpgradeInfoResponse(String response) {
				try {
					JSONObject jsonResponse = (new JSONObject(response))
							.getJSONObject("data");

					String lowest = jsonResponse.getString("lowest");
					String highest = jsonResponse.getString("highest");
					String url = jsonResponse.getString("apkUrl");
					String detail = jsonResponse.getString("detail");

					SwitchLogger.d(LOG_TAG, "receive info, lowest version:"
							+ lowest + ", highest version: " + highest);
					SwitchLogger.d(LOG_TAG, "receive info, apkUrl: " + url
							+ ", detail: " + detail);

					String current = ActivityUtil
							.getCurrentVersionName(getContext());
					SwitchLogger.d(LOG_TAG, "current version: " + current);

					return new UpgradeParam(current, lowest, highest, url,
							detail);
				} catch (JSONException e) {
					if (SwitchLogger.isPrintLog()) {
						SwitchLogger.e(e);
					}
					return null;
				}
			}
		}

		new UpgradeProxy(this, "http://pesiwang.devel.rabbit.oa.com/t.php",
				new MyCallback(this)).check();

	}
}
