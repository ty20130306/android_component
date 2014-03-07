package com.vanchu.libs.slipping;

import java.util.ArrayList;

import android.support.v4.view.ViewPager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SlippingViewEntity {

	
	private ViewPager viewPager= null;
	private ArrayList<RadioButton> radioButtons = null;
	private RadioGroup radioGroup = null;
	public SlippingViewEntity(ViewPager viewPager, RadioGroup radioGroup,ArrayList<RadioButton> radioButtons) {
		this.radioGroup = radioGroup;
		this.viewPager = viewPager;
		this.radioButtons = radioButtons;
	}
	public ViewPager getViewPager() {
		return viewPager;
	}
	public ArrayList<RadioButton> getRadioButtons() {
		return radioButtons;
	}
	
	public RadioGroup getRadioGroup() {
		return radioGroup;
	}
}
