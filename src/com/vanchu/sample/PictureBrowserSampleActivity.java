package com.vanchu.sample;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.BitmapUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.pictureBrowser.PictureBrowser;
import com.vanchu.libs.pictureBrowser.PictureBrowserDataEntity;
import com.vanchu.libs.pictureBrowser.PictureBrowserItemClickLinstener;
import com.vanchu.libs.pictureBrowser.PictureBrowserItemViewEntity;
import com.vanchu.libs.pictureBrowser.PictureBrowserViewEntity;
import com.vanchu.libs.webCache.WebCache;
import com.vanchu.libs.webCache.WebCache.GetCallback;
import com.vanchu.test.R;

public class PictureBrowserSampleActivity extends Activity{

	private final String TAG = PictureBrowserSampleActivity.class.getSimpleName();
	
	private ViewPager viewPager = null;
	private LinearLayout layoutDots = null;
	private PictureBrowser pagerLogic = null;
	private ImageView imgBrowser = null;
	
	private WebCache webCache  = null;
	private final String WEBCACHE_DEFAULT_PIC = "webcache_default_pic";

	private boolean isClick = true;
	private int count = 0;
	private String[] defImageUrl = new String[]{
			"http://www.wmpic.me/wp-content/uploads/2013/09/2013093012344046-215x185.jpg",
			"http://www.wmpic.me/wp-content/uploads/2013/09/20130912215349204-215x185.jpg",
			"http://www.wmpic.me/wp-content/uploads/2013/09/20130912214127552-215x185.jpg",
			"http://www.wmpic.me/wp-content/uploads/2013/10/20131029224449996-215x185.jpg",
			"http://www.wmpic.me/wp-content/uploads/2013/11/2013111823092895-215x185.jpg",
			"http://www.wmpic.me/wp-content/uploads/2013/07/20130730123501421-215x185.jpg"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SwitchLogger.setPrintLog(true);
		setContentView(R.layout.activity_picture_browser_sample);
		imgBrowser = (ImageView)findViewById(R.id.img_browser);
		viewPager = (ViewPager)findViewById(R.id.viewpager_picutre);
		layoutDots = (LinearLayout)findViewById(R.id.layout_picture_pager_dot);
		
		initWebCache();
		
		btnBrowserSinglePic(null);
	}
	
	private void initWebCache(){
		webCache =WebCache.getInstance(this, WEBCACHE_DEFAULT_PIC);
		WebCache.Settings settings = new WebCache.Settings();
		webCache.setup(settings);
	}
	
	public void btnBrowserSinglePic(View view){
		if(isClick){
			if(count >= defImageUrl.length){
				Tip.show(this, "已经全部展示");
				return;
			}
			Tip.show(this, "當前展示的圖片是："+count);
			imgBrowser.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					PictureBrowserSampleActivity.this.finish();
				}
			});
			webCache.get(defImageUrl[count], new CacheListener(), imgBrowser, false);
			isClick = false;
			count ++;
		}
	}
	
	/**
	 * 通过drawable显示默认图片
	 * @param view
	 */
	public void btnBrowserDrawablePic(View view){
		PictureBrowserViewEntity browserEntity =  new PictureBrowserViewEntity(viewPager, layoutDots);
		PictureBrowserItemViewEntity itemEntity = new PictureBrowserItemViewEntity(R.layout.picture_browser_item, R.id.picture_browser_detail_img, R.id.picture_browser_detail_progress_bar, R.id.picture_browser_detail_progress_text,R.id.picture_browser_detail_def_img);
		List<PictureBrowserDataEntity>  lists = initDrawable();
		if(lists.size() >0){
			pagerLogic = new PictureBrowser(this, browserEntity,lists , 1,itemEntity);
			pagerLogic.setItemClickLinstener(itemClickLinstener);
			pagerLogic.initpager();
		}
	}
	
	
	/**
	 * 通过URL
	 * @param view
	 */
	public void btnBrowserURLPic(View view){
		PictureBrowserViewEntity browserEntity =  new PictureBrowserViewEntity(viewPager, layoutDots);
		PictureBrowserItemViewEntity itemEntity = new PictureBrowserItemViewEntity(R.layout.picture_browser_item, R.id.picture_browser_detail_img, R.id.picture_browser_detail_progress_bar, R.id.picture_browser_detail_progress_text,R.id.picture_browser_detail_def_img);
		List<PictureBrowserDataEntity>  lists = initDrawableURl();
		if(lists.size() >0){
			pagerLogic = new PictureBrowser(this, browserEntity, lists, 3,itemEntity);
			pagerLogic.setItemClickLinstener(itemClickLinstener);
			pagerLogic.initpager();
		}
	}
	
	/**
	 * 通过默认图片通过url来展示
	 * @return
	 */
	private List<PictureBrowserDataEntity> initDrawableURl(){
		List<PictureBrowserDataEntity> lists = new ArrayList<PictureBrowserDataEntity>();
		 String[] strUrl = {"http://pic.58pic.com/58pic/11/00/02/23F58PICMkI.jpg",
				 "http://pic.58pic.com/58pic/11/24/02/57B58PICASv.jpg",
				 "http://pic.58pic.com/58pic/11/45/29/95j58PICe4Y.jpg",
				 "http://pic.58pic.com/58pic/11/30/26/05h58PICjz3.jpg",
				 "http://pic.58pic.com/10/80/53/44bOOOPIC11.jpg",
				 "http://pic.58pic.com/58pic/11/01/48/85858PICIPY.jpg"};
		 if(strUrl.length != defImageUrl.length){
			 SwitchLogger.e(TAG,TAG +"--数据长度不一样----");
			 return lists;
		 }
		for(int index = 0;index <strUrl.length;index ++){
			PictureBrowserDataEntity entity = new PictureBrowserDataEntity(defImageUrl[index], strUrl[index],webCache);
			lists.add(entity);
		}
		SwitchLogger.d(TAG, TAG +" drawables:"+lists.size());
		return lists;
	}
	
	private List<PictureBrowserDataEntity> initDrawable(){
		List<PictureBrowserDataEntity> lists = new ArrayList<PictureBrowserDataEntity>();
		 String[] strUrl = {"http://pic.58pic.com/58pic/11/00/02/23F58PICMkI.jpg",
				 "http://pic.58pic.com/58pic/11/24/02/57B58PICASv.jpg",
				 "http://pic.58pic.com/58pic/11/45/29/95j58PICe4Y.jpg",
				 "http://pic.58pic.com/58pic/11/30/26/05h58PICjz3.jpg",
				 "http://pic.58pic.com/10/80/53/44bOOOPIC11.jpg",
				 "http://pic.58pic.com/58pic/11/01/48/85858PICIPY.jpg"};
		 
		int[] ids = {R.drawable.hao0001,
				     R.drawable.hao0002,
				     R.drawable.hao0003,
				     R.drawable.hao0004,
				     R.drawable.hao0005,
				     R.drawable.hao0006};
		for(int index = 0;index <ids.length;index ++){
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), ids[index]);		
			PictureBrowserDataEntity entity = new PictureBrowserDataEntity(new BitmapDrawable(getResources(), bitmap), strUrl[index]);
			lists.add(entity);
		}
		SwitchLogger.d(TAG, TAG +" drawables:"+lists.size());
		return lists;
	}
	
	
	private class CacheListener implements GetCallback{

		@Override
		public void onDone(String url, File file, Object param) {
			isClick = true;
			SwitchLogger.d(TAG, TAG +"---webcache--onDone..." +url);
			if(param != null){
				if(file != null){
					ImageView imgView = (ImageView)param;
					Bitmap bm = BitmapUtil.getSuitableBitmap(file);
					imgView.setImageBitmap(bm);
				}
			}
		}

		@Override
		public void onFail(String url, int reason, Object param) {
			SwitchLogger.d(TAG, TAG +"---webcache--onFail ..."+url);
			isClick = true;
		}

		@Override
		public void onProgress(String url, int progress, Object param) {
			SwitchLogger.d(TAG, TAG +"---webcache--onProgress ..."+progress);
		}
	}
	
	private PictureBrowserItemClickLinstener itemClickLinstener = new PictureBrowserItemClickLinstener() {
		
		@Override
		public void onClick() {
			PictureBrowserSampleActivity.this.finish();
		}
	};
}

