package com.vanchu.libs.pictureBrowser;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.vanchu.libs.common.imgZoom.GestureImageView;
import com.vanchu.libs.common.util.SwitchLogger;

public class PictureBrowser implements OnPageChangeListener {

	private final String TAG = PictureBrowser.class.getSimpleName();
	private Context context = null;
	private ViewPager viewPager = null;
	private LinearLayout dotlayout = null;
	private List<PictureBrowserDataEntity> listEntity = new ArrayList<PictureBrowserDataEntity>();
	private ImageView[] dots = null;
	private int currentIndex = -1; // 当前的位置
	private PictureBrowserAdapter pagerAdapter = null;
	private PictureBrowserItemViewEntity browserItemEntity = null;

	/**
	 * Settting ViewPager Cache limit
	 */
	private int cacheLimit = -1;

	private PictureBrowserItemClickLinstener itemClickLinstener = null;

	/***
	 * Settting  ViewPager Item click event
	 * @param itemClickLinstener
	 */
	public void setItemClickLinstener(
			PictureBrowserItemClickLinstener itemClickLinstener) {
		this.itemClickLinstener = itemClickLinstener;
	}
	
	public void setCacheLimit(int cacheLimit) {
		this.cacheLimit = cacheLimit;
	}
	
	public PictureBrowser(Context context, PictureBrowserViewEntity browserEntity,
			List<PictureBrowserDataEntity> lists, int currentIndex,
			PictureBrowserItemViewEntity browserItemEntity) {
		this.context = context;
		this.viewPager = browserEntity.getViewPager();
		this.dotlayout = browserEntity.getLayoutDots();
		this.listEntity = lists;
		this.currentIndex = currentIndex;
		this.browserItemEntity = browserItemEntity;
	}

	public void initpager() {
		initDots(currentIndex);
		this.pagerAdapter = new PictureBrowserAdapter(context, listEntity,
				browserItemEntity);
		viewPager.setOffscreenPageLimit(cacheLimit);
		pagerAdapter.setItemClickLinstener(itemClickLinstener);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(this);
		viewPager.setCurrentItem(currentIndex);
	}
	
	private void initDots(int currentIndex) {
		if (dotlayout == null) {
			return;
		}
		if (listEntity != null && listEntity.size() != 0) {
			dots = new ImageView[listEntity.size()];
			for (int i = 0; i < listEntity.size(); i++) {
				if (i < dots.length) {
					dots[i] = (ImageView) dotlayout.getChildAt(i);
					dots[i].setEnabled(true);
					dots[i].setTag(i);
					dots[i].setVisibility(View.VISIBLE);
				}
			}
			/* 不可点击代表当前是选中状态，可点击代表没有选中 */
			if (currentIndex < dots.length) {
				dots[currentIndex].setEnabled(false);
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
//		SwitchLogger.d(TAG, TAG + " onPageScrollStateChanged arg0 :" + arg0);
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
//		SwitchLogger.d(TAG, TAG + " onPageScrolled arg0 :" + arg0 + ",arg1:"
//				+ arg1 + ",arg2:" + arg2);
	}

	@Override
	public void onPageSelected(int arg0) {
		SwitchLogger.d(TAG, TAG + " onPageSelected arg0 :" + arg0);
		setCurrentDot(arg0);
	}

	public void setCurrentDot(int position) {
		SwitchLogger.d(TAG, TAG + " setCurrentDot position:" + position);
		if (position < 0 || position > listEntity.size() - 1
				|| currentIndex == position) {
			return;
		}
		if(dots != null){
			dots[position].setEnabled(false);
			dots[currentIndex].setEnabled(true);
		}
		currentIndex = position;
	}
}
