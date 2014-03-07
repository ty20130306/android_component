package com.vanchu.libs.slipping;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SlippingFragmentPagerAdapter extends FragmentPagerAdapter{

	private ArrayList<Fragment> fragments = null;
	public SlippingFragmentPagerAdapter(FragmentManager fm,ArrayList<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int arg0) {
		if(null != fragments && fragments.size()>0){
			return fragments.get(arg0);
		}else{
			return null;
		}
	}

	@Override
	public int getCount() {
		if(fragments != null){
			return fragments.size();
		}
		return 0;
	}
}
