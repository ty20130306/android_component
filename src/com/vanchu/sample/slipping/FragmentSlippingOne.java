package com.vanchu.sample.slipping;

import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.test.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentSlippingOne extends Fragment{
	private final String TAG = FragmentSlippingOne.class.getSimpleName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_slipping_one, container,false);
		SwitchLogger.d(TAG, TAG +" onCreateView");
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		SwitchLogger.d(TAG, TAG +" onCreate");
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onDestroy() {
		SwitchLogger.d(TAG, TAG +" onDestroy");
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	 
	@Override
	public void onPause() {
		SwitchLogger.d(TAG, TAG +"  onPause");
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	public void onStop() {
		SwitchLogger.d(TAG, TAG +" onStop");
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	@Override
	public void onResume() {
		SwitchLogger.d(TAG, TAG +"  onResume");
		super.onResume();
	}

}
