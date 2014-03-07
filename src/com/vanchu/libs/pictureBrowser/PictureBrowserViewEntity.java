package com.vanchu.libs.pictureBrowser;

import java.io.Serializable;

import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;

public class PictureBrowserViewEntity implements Serializable{

	private static final long serialVersionUID = 1L;


	/**
	 * 
	 */
	private ViewPager viewPager = null;
	
	private LinearLayout layoutDots = null;


	public PictureBrowserViewEntity(ViewPager viewPager, LinearLayout layoutDots) {
		this.viewPager = viewPager;
		this.layoutDots = layoutDots;
	}
	
	public LinearLayout getLayoutDots() {
		return layoutDots;
	}

	public ViewPager getViewPager() {
		return viewPager;
	}

}
