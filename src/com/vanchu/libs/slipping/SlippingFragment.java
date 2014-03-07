package com.vanchu.libs.slipping;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.vanchu.libs.common.util.SwitchLogger;

public class SlippingFragment implements OnPageChangeListener{

	private final String TAG = SlippingFragment.class.getSimpleName();
	private ArrayList<Fragment> fragments = null;

	private ViewPager viewPager = null;
	private RadioGroup radioGroup = null;
	private FragmentActivity activity = null;
	private int defSelectedId = 0;
	private List<RadioButton> radioButtons  = null;
	
	
	public SlippingFragment(FragmentActivity activity,ArrayList<Fragment> fragments) {
		this.activity = activity;
		this.fragments = fragments;
	}
	
	public void setDefSelectedId(int defSelectedId) {
		this.defSelectedId = defSelectedId;
	}
	
	/**
	 * RadioGroup中包含有其他组件
	 * @param entity
	 */
	public void initFragment(SlippingViewEntity entity){
		viewPager = entity.getViewPager();
		radioGroup = entity.getRadioGroup();
		radioButtons = entity.getRadioButtons();
		if(radioButtons != null&& radioButtons.size() >0){
			for(int i = 0;i<radioButtons.size();i++){
				if(defSelectedId == i){
					radioButtons.get(i).setChecked(true);
				}
				radioButtons.get(i).setOnClickListener(new OnRadioClick(i));
			}
		}
		
		SlippingFragmentPagerAdapter pagerAdapter = new SlippingFragmentPagerAdapter(activity.getSupportFragmentManager(), fragments);
//		if (Build.VERSION.SDK_INT >= 9) {
//			viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
//		}
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(defSelectedId);
		viewPager.setOffscreenPageLimit(0);
		viewPager.setOnPageChangeListener(this);
	}
	
	/**
	 * RadioGroupz
	 * @param viewPager
	 * @param radioGroup
	 */
	public void initFragment(ViewPager viewPager,RadioGroup radioGroup){
		this.viewPager = viewPager;
		this.radioGroup = radioGroup;
		SlippingFragmentPagerAdapter pagerAdapter = new SlippingFragmentPagerAdapter(activity.getSupportFragmentManager(), fragments);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(defSelectedId);
		viewPager.setOffscreenPageLimit(0);
		viewPager.setOnPageChangeListener(this);
//		if (Build.VERSION.SDK_INT >= 9) {
//			viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
//		}
		int count =  radioGroup.getChildCount();
		SwitchLogger.d(TAG,TAG +" count:" +count);
		for(int i = 0;i<count;i++){
			if(defSelectedId ==i){
				((RadioButton)radioGroup.getChildAt(i)).setChecked(true);
			}
			radioGroup.getChildAt(i).setOnClickListener(new OnRadioClick(i));
		}
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
	
	public void setView(int position){
		viewPager.setCurrentItem(position,false);
		if(radioButtons == null){
			if(position < radioGroup.getChildCount()){
				((RadioButton)radioGroup.getChildAt(position)).setChecked(true);
			}
		}else{
			if(radioButtons.size() > 0){
				if(position < radioButtons.size()){
					radioButtons.get(position).setChecked(true);
				}
			}
		}
	}
	
	@Override
	public void onPageSelected(int position) {
		SwitchLogger.d(TAG, TAG+"onPageSelected  arg0:"+position);
	    setView(position);
	}
	
	private class OnRadioClick implements OnClickListener{
		private int position = 0;
		public OnRadioClick(int position){
			this.position = position;
		}
		
		@Override
		public void onClick(View v) {
			SwitchLogger.d(TAG, TAG +"-OnRadioCheckedChange- onCheckedChanged-id:" +v.getId());
			setView(position);
		}
	}
}
