package com.vanchu.sample.slipping;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.slipping.SlippingFragment;
import com.vanchu.libs.slipping.SlippingViewEntity;
import com.vanchu.test.R;

@SuppressLint("NewApi")
public class GuimiSlippingActivity extends FragmentActivity  {

	private final String TAG = GuimiSlippingActivity.class.getSimpleName();
	private ViewPager viewPager = null;
	private RadioGroup radioGroup = null;
	private ArrayList<Fragment> fragments = new ArrayList<Fragment>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SwitchLogger.setPrintLog(true);
		setContentView(R.layout.guimi_slipping);
		fragments.add(new FragmentSlippingOne());
		fragments.add(new FragmentSlippingTwo());
		fragments.add(new FragmentSlippingThree());
		radioGroup = (RadioGroup) findViewById(R.id.radiogroup_slipping);
		viewPager = (ViewPager) findViewById(R.id.viewpager_slipping);
//		init();
		init2();
	}

	private void init() {
		ArrayList<RadioButton> radioButtons = new ArrayList<RadioButton>();
		int[] radioIds = new int[] { R.id.radio_random_look,
				R.id.radio_newest, R.id.radio_newest_talk };
		for(int index = 0;index <radioIds.length;index++){
			radioButtons.add((RadioButton)findViewById(radioIds[index]));
		}
		SlippingViewEntity entity = new SlippingViewEntity(viewPager,radioGroup,radioButtons);
		SlippingFragment slippingFragment = new SlippingFragment(this,
				fragments);
		slippingFragment.initFragment(entity);
	}
	
	private void init2(){
		SlippingFragment slippingFragment = new SlippingFragment(this,
				fragments);
		slippingFragment.initFragment(viewPager, radioGroup);
	}
}
