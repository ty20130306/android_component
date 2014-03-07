package com.vanchu.libs.common.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost.TabSpec;

/**
 * @author wolf
 *
 */
public class VanchuTabActivity extends TabActivity implements OnClickListener {

	protected static final int ID_NULL	= -1; 
	
	private int		_layoutId;
	private int		_radioGroupId;
	private List<TabCfg>	_tabCfgList;
	
	private TabHost		_tabHost;
	private RadioGroup	_radioGroup;
	private Map<Integer, RadioButton>	_radioButtonMap	= new HashMap<Integer, RadioButton>();
	
	private int _currentTabId	= ID_NULL;
	private int _lastTabId		= ID_NULL;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		beforeViewInited();
		initView();
		afterViewInited();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	/******************** 需重载的函数开始 *************************/
	protected void beforeViewInited() {
		
	}
	
	protected void afterViewInited() {
		
	}
	
	protected void onTabClick(int tabId) {
		
	}
	/******************** 需重载的函数结束 *************************/
	
	/******************** 供子类调用的函数开始 *************************/
	protected int getLastTabId() {
		return _lastTabId;
	}
	
	protected int getCurrentTabId() {
		return _currentTabId;
	}
	
	protected void initCurrentTabId(int tabId) {
		_currentTabId	= tabId;
	}
	
	protected void initCfg(int layoutId, int radioGroupId, List<TabCfg> tabCfgList) {
		_layoutId		= layoutId;
		_radioGroupId	= radioGroupId;
		_tabCfgList		= tabCfgList;
	}
	
	protected RadioGroup getRadioGroup() {
		return _radioGroup;
	}
	
	protected void selectTab(int id) {
		if(id == _currentTabId) {
			return ;
		}
		
		RadioButton btn	= _radioButtonMap.get(new Integer(id));
		if(null == btn) {
			return ;
		}
		
		btn.setChecked(true);
	}
	/******************** 供子类调用的函数结束*************************/
	
	private void updateCurrentTabId(int newId) {
		_lastTabId		= _currentTabId;
		_currentTabId	= newId; 
	}
	
	private void initTab() {
		for(int i = 0, len = _tabCfgList.size(); i < len; i++) {
			TabCfg tabCfg = _tabCfgList.get(i);
			if(null == tabCfg) {
				continue;
			}
			
			if(ID_NULL == _currentTabId) {
				_currentTabId	= tabCfg.getId();
			}
			
			RadioButton btn	= (RadioButton)findViewById(tabCfg.getId());
			if(null == btn) {
				continue;
			}
			btn.setOnClickListener(this);
			_radioButtonMap.put(new Integer(tabCfg.getId()), btn);
			
			if(null != tabCfg.getIntent()) {
				TabSpec tabSpec	= _tabHost.newTabSpec(tabCfg.getTag()).setIndicator(tabCfg.getTag()).setContent(tabCfg.getIntent());
				_tabHost.addTab(tabSpec);
			}
		}
		_radioGroup.setOnCheckedChangeListener(new RadioCheckedChangeListener());
	}
	
	private void initView() {
		setContentView(_layoutId);
		_tabHost	= getTabHost();
		_radioGroup	= (RadioGroup)findViewById(_radioGroupId);
		_radioGroup.measure(0, 0);
		
		initTab();
	}
	
	private TabCfg getTabCfgById(int id) {
		for(int i = 0; i < _tabCfgList.size(); ++i) {
			TabCfg tc	= _tabCfgList.get(i);
			if(null != tc && tc.getId() == id) {
				return tc;
			}
		}
		
		return null;
	}
	
	
	/**
	 * @param msgId
	 * @return true for execute intent, false for ignore
	 */
	protected boolean beforeIntent(int tabId) {
		return true;
	}
	
	private class RadioCheckedChangeListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(RadioGroup group, int id) {
			if(id == _currentTabId) {
				return ;
			}
			updateCurrentTabId(id);
			TabCfg tc	= getTabCfgById(id);
			if(null != tc && null != tc.getIntent() && beforeIntent(id)) {
				_tabHost.setCurrentTabByTag(tc.getTag());
			}
		}
	}

	@Override
	public void onClick(View v) {
		onTabClick(v.getId());
	}
	
	public static class TabCfg {
		private int id;
		private String tag;
		private Intent intent;
		
		public TabCfg(int id, String tag, Intent intent) {
			this.id		= id;
			this.tag	= tag;
			this.intent	= intent;
		}
		
		public int getId() {
			return id;
		}
		
		public String getTag() {
			return tag;
		}
		
		public Intent getIntent() {
			return intent;
		}
	}
}
