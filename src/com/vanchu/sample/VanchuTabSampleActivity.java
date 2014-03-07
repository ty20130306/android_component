package com.vanchu.sample;

import java.util.ArrayList;
import java.util.List;

import com.vanchu.libs.common.ui.DialogFactory;
import com.vanchu.libs.common.ui.VanchuTabActivity;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.test.R;
import com.vanchu.test.SecondActivity;
import com.vanchu.test.TestFeedbackActivity;
import com.vanchu.test.TestKvDbActivity;
import com.vanchu.test.R.layout;
import com.vanchu.test.R.menu;
import com.vanchu.test.ThreeActivity;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;

public class VanchuTabSampleActivity extends VanchuTabActivity {

	private static final String LOG_TAG		= VanchuTabSampleActivity.class.getSimpleName();

	@Override
	protected void beforeViewInited() {
		List<TabCfg> tabCfgList	= new ArrayList<TabCfg>();
		tabCfgList.add(new TabCfg(R.id.navigation_tab1, "tab1", new Intent(this, SecondActivity.class) ));
		tabCfgList.add(new TabCfg(R.id.navigation_tab2, "tab2", new Intent(this, ThreeActivity.class) ));
		tabCfgList.add(new TabCfg(R.id.navigation_tab3, "tab3", null));
		tabCfgList.add(new TabCfg(R.id.navigation_tab4, "tab4", new Intent(this, TestFeedbackActivity.class) ));
		tabCfgList.add(new TabCfg(R.id.navigation_tab5, "tab5", new Intent(this, TestKvDbActivity.class) ));
		
		initCfg(R.layout.activity_vanchu_tab_sample, R.id.navigation_tab_group, tabCfgList);
	}
	
	private void showDialog() {
		View view	= getLayoutInflater().inflate(R.layout.dialog, null);
		int y	= getRadioGroup().getMeasuredHeight() + 20;
		Dialog dialog	= DialogFactory.createCenterDialog(this, view, R.style.customDialog, LayoutParams.WRAP_CONTENT, y, 0.5f, true);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				selectTab(getLastTabId());
			}
		});
		dialog.show();
	}
	
	@Override
	protected boolean beforeIntent(int tabId) {
		switch (tabId) {
		case R.id.navigation_tab1:
			return true;
		case R.id.navigation_tab2:
			return false;
			
		case R.id.navigation_tab4:
		case R.id.navigation_tab5:
			selectTab(getLastTabId());
			return false;
			
		case R.id.navigation_tab3:
			showDialog();
			return false;
		
		default:
			return true;
		}
	}
	
	@Override
	protected void onTabClick(int tabId) {
		switch (tabId) {
		case R.id.navigation_tab1:
		case R.id.navigation_tab4:
		case R.id.navigation_tab5:
			break;
			
		case R.id.navigation_tab2:
			Intent intent	= new Intent(this, TestKvDbActivity.class);
			startActivityForResult(intent, 0);
			break;
		case R.id.navigation_tab3:
			showDialog();
			break;
		
		default:
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		SwitchLogger.d(LOG_TAG, "onDestroy");
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 0 && resultCode == RESULT_OK) {
			selectTab(getLastTabId());
			return ;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
}
