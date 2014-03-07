package com.vanchu.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.vanchu.libs.scrollListview.*;
import com.vanchu.libs.scrollListview.ScrollListView.ScrollListViewListener;
import com.vanchu.libs.scrollListview.ScrollListViewAdd.AddViewListener;
import com.vanchu.test.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ScrollListViewSampleActivity extends Activity {

	private final int refreshOK = -1;
	private final int refreshFailed = -2;
	private final int loadmoreOK = -3;
	private final int loadmoreFailed = -4;

	private final int itemNum = 10;
	private ScrollListView listView;
	private SimpleAdapter adapter;
	private ArrayList<HashMap<String, String>> listData;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case refreshOK:
				listData.clear();
				listData.addAll((ArrayList<HashMap<String, String>>) msg.obj);
				listView.refresh(listData.size());
				Toast.makeText(ScrollListViewSampleActivity.this, "刷新成功",
						Toast.LENGTH_SHORT).show();
				break;
			case refreshFailed:
				listView.refresh();
				Toast.makeText(ScrollListViewSampleActivity.this, "刷新失败！！！",
						Toast.LENGTH_SHORT).show();
				break;
			case loadmoreOK:
				ArrayList<HashMap<String, String>> adds = (ArrayList<HashMap<String, String>>) msg.obj;
				listData.addAll(adds);
				listView.refresh(adds.size());
				Toast.makeText(ScrollListViewSampleActivity.this, "加载成功",
						Toast.LENGTH_SHORT).show();
				break;
			case loadmoreFailed:
				listView.refresh();
				Toast.makeText(ScrollListViewSampleActivity.this, "加载失败！！！",
						Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_listview);

		listData = new ArrayList<HashMap<String, String>>();
		adapter = new SimpleAdapter(this, listData,
				R.layout.activity_test_listview_item,
				new String[] { "itemText" }, new int[] { R.id.tv_item });

		final ScrollListViewAdd headView = (ScrollListViewAdd) getLayoutInflater()
				.inflate(R.layout.activity_test_listview_load, null);
		final ScrollListViewAdd footView = (ScrollListViewAdd) getLayoutInflater()
				.inflate(R.layout.activity_test_listview_load, null);
		headView.setListener(new AddViewListener() {

			@Override
			public void beforeLoading(ScrollListViewAdd myAddView) {
				headView.setVisibility(View.VISIBLE);
				((TextView) myAddView.findViewById(R.id.tv_loadMore))
						.setText("beforeLoading");
			}

			@Override
			public void onLoading(ScrollListViewAdd myAddView) {
				headView.setVisibility(View.VISIBLE);
				((TextView) myAddView.findViewById(R.id.tv_loadMore))
						.setText("onLoading");
			}

			@Override
			public void afterLoading(ScrollListViewAdd myAddView) {
				headView.setVisibility(View.GONE);
				((TextView) myAddView.findViewById(R.id.tv_loadMore))
						.setText("afterLoading");
			}

			@Override
			public void noMoreData(ScrollListViewAdd myAddView) {
				headView.setVisibility(View.VISIBLE);
				((TextView) myAddView.findViewById(R.id.tv_loadMore))
						.setText("noMoreData");
			}
		});
		footView.setListener(new AddViewListener() {

			@Override
			public void beforeLoading(ScrollListViewAdd myAddView) {
				((TextView) myAddView.findViewById(R.id.tv_loadMore))
						.setText("beforeLoading");
			}

			@Override
			public void onLoading(ScrollListViewAdd myAddView) {
				((TextView) myAddView.findViewById(R.id.tv_loadMore))
						.setText("onLoading");
			}

			@Override
			public void afterLoading(ScrollListViewAdd myAddView) {
				((TextView) myAddView.findViewById(R.id.tv_loadMore))
						.setText("afterLoading");
			}

			@Override
			public void noMoreData(ScrollListViewAdd myAddView) {
				((TextView) myAddView.findViewById(R.id.tv_loadMore))
						.setText("noMoreData");
			}
		});

		listView = (ScrollListView) findViewById(R.id.slv);
		listView.init(adapter, headView, footView,
				new ScrollListViewListener() {

					@Override
					public void onTopAction() {
						reloadData();
					}

					@Override
					public void onBottomAction() {
						loadMoreData();
					}
				}, 1);

		reloadData();
	}

	private void reloadData() {
		new Thread() {
			public void run() {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (new Random().nextInt(10) < 7) {
					ArrayList<HashMap<String, String>> newData = new ArrayList<HashMap<String, String>>();
					for (int i = 0; i < itemNum; i++) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("itemText", "test" + i);
						newData.add(map);
					}
					Message msg = new Message();
					msg.what = refreshOK;
					msg.obj = newData;
					handler.sendMessage(msg);
				} else {
					handler.sendEmptyMessage(refreshFailed);
				}
			};
		}.start();
	}

	private void loadMoreData() {
		new Thread() {
			public void run() {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (new Random().nextInt(10) < 6) {
					ArrayList<HashMap<String, String>> newData = new ArrayList<HashMap<String, String>>();
					for (int i = 0; i < itemNum; i++) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("itemText", "test" + i);
						newData.add(map);
					}
					Message msg = new Message();
					msg.what = loadmoreOK;
					msg.obj = newData;
					handler.sendMessage(msg);
				} else if (new Random().nextInt(10) < 6) {
					ArrayList<HashMap<String, String>> newData = new ArrayList<HashMap<String, String>>();
					for (int i = 0; i < 0; i++) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("itemText", "test" + i);
						newData.add(map);
					}
					Message msg = new Message();
					msg.what = loadmoreOK;
					msg.obj = newData;
					handler.sendMessage(msg);
				} else {
					handler.sendEmptyMessage(loadmoreFailed);
				}
			};
		}.start();
	}

}
