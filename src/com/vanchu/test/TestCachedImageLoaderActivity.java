package com.vanchu.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.vanchu.libs.common.task.CachedImageLoader;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.webCache.WebCache;

import android.os.Bundle;
import android.app.Activity;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class TestCachedImageLoaderActivity extends Activity {

	private static final String LOG_TAG	= TestCachedImageLoaderActivity.class.getSimpleName();
	
	private ListView		_listView;
	private List<String>	_urlList;
	private CachedImageLoader _imgLoader;
	private WebCache		_webCache;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_cached_image_loader);
		_listView	= (ListView)findViewById(R.id.list_view);
		_listView.setAdapter(new ListAdapter());
		_imgLoader	= new CachedImageLoader();
		_webCache	= WebCache.getInstance(this, "test_cached_image_loader" );
		
		initUrlList();
	}
	
	private void initUrlList() {
		_urlList	= new ArrayList<String>();
		_urlList.add("http://t100.qpic.cn/mblogpic/c848b3ab3fac196835e0/460");
		_urlList.add("http://t100.qpic.cn/mblogpic/744a6496549d0754fde2/460");
		_urlList.add("http://t100.qpic.cn/mblogpic/7a194230232516186cd4/460");
		_urlList.add("http://t100.qpic.cn/mblogpic/b29c5f758d049001c086/460");
		_urlList.add("http://t100.qpic.cn/mblogpic/c848b3ab3fac196835e0/460");
		
		_urlList.add("http://image.tianjimedia.com/uploadImages/2013/249/4SVA1SY108DU.jpg");
		_urlList.add("http://image.tianjimedia.com/uploadImages/2013/249/Z4E7SE5SCF3W.jpg");
		_urlList.add("http://image.tianjimedia.com/uploadImages/2013/249/BFA7M8G37YD8.jpg");
		_urlList.add("http://image.tianjimedia.com/uploadImages/2013/249/14Y0RS7P2Q7H.jpg");
		_urlList.add("http://image.tianjimedia.com/uploadImages/2013/249/RBWFU6888EUR.jpg");
		
		_urlList.add("http://image.tianjimedia.com/uploadImages/2013/249/DMY9430829S2.jpg");
		_urlList.add("http://image.tianjimedia.com/uploadImages/2013/249/7X8UE5S4WB8U.jpg");
		_urlList.add("http://image.tianjimedia.com/uploadImages/2013/249/Y5353PFU0U2N.jpg");
		_urlList.add("http://image.tianjimedia.com/uploadImages/2013/249/7QA3K10W247E.jpg");
		_urlList.add("http://image.tianjimedia.com/uploadImages/2013/249/98NU2851J072.jpg");
		
		_urlList.add("http://t100.qpic.cn/mblogpic/c848b3ab3fac196835e0/460");
		_urlList.add("http://t100.qpic.cn/mblogpic/744a6496549d0754fde2/460");
	}
	
	private String getUrl(int position) {
		return _urlList.get(position);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_cached_image_loader, menu);
		return true;
	}

	private class ListAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			
			return 15;
		}
		
		@Override
		public Object getItem(int position) {
			
			return null;
		}
		
		@Override
		public long getItemId(int position) {
			
			return 0;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.item_list_view, null);
            final ImageView imgSmall = (ImageView)convertView.findViewById(R.id.img_small);
            _imgLoader.loadImage(getUrl(position), _webCache, new CachedImageLoader.Callback() {
				@Override
				public void onSucc(String url, Bitmap bitmap) {
					SwitchLogger.d(LOG_TAG, "imgSmall loadImage.onSucc");
					imgSmall.setImageBitmap(bitmap);
				}
				@Override
				public void onProgress(String url, int progress) {
					
				}
				@Override
				public void onFail(String url) {
					SwitchLogger.e(LOG_TAG, "imgSmall loadImage.onFail");
				}
			});
            
            final ImageView imgBig = (ImageView)convertView.findViewById(R.id.img_big); 
            _imgLoader.loadImage(getUrl(position+1), _webCache, new CachedImageLoader.Callback() {
				@Override
				public void onSucc(String url, Bitmap bitmap) {
					SwitchLogger.d(LOG_TAG, "imgBig loadImage.onSucc");
					imgBig.setImageBitmap(bitmap);
				}
				@Override
				public void onProgress(String url, int progress) {
					
				}
				@Override
				public void onFail(String url) {
					SwitchLogger.e(LOG_TAG, "imgBig loadImage.onFail");
				}
			});
            return convertView;
		}
	};
}
