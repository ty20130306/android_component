package com.vanchu.test;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.ActivityUtil;
import com.vanchu.libs.common.util.ImgUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.pluginSystem.PluginCfg;
import com.vanchu.libs.pluginSystem.PluginInfo;
import com.vanchu.libs.pluginSystem.PluginInfoManager;
import com.vanchu.libs.pluginSystem.PluginManager;
import com.vanchu.libs.pluginSystem.PluginManagerCallback;
import com.vanchu.libs.pluginSystem.PluginSystem;
import com.vanchu.libs.pluginSystem.PluginSystemCallback;
import com.vanchu.libs.pluginSystem.PluginVersion;

import com.vanchu.sample.PluginSystemDbManager;

import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TestPluginSystemActivity extends Activity {
	private static final String 	LOG_TAG	= TestPluginSystemActivity.class.getSimpleName();
	
	private List<List<PluginInfo>>	_pluginData;
	private PluginSystem	_ps;
	
	private ViewPager _viewPager;
	private List<View> _pageViewList;
	private List<GridViewAdapter> _gridViewAdapterList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_plugin_system);
		
		_pluginData	= null;
		_viewPager	= (ViewPager)findViewById(R.id.view_pager);
		
		_pageViewList			= new ArrayList<View>();
		_gridViewAdapterList	= new ArrayList<GridViewAdapter>(); 

		_ps	= new PluginSystem(this, 
				"http://pesiwang.devel.rabbit.oa.com/test_plugin_system.php", 
				new MyPluginSystemCallback());
		
		_ps.run();
	}
	
	private void setPlguinData(List<PluginInfo> pluginInfoList) {
		_pluginData	= new ArrayList<List<PluginInfo>>();
		
		int pageNum	= (int)(Math.ceil((double)pluginInfoList.size() / 3));
		for(int i = 0; i < pageNum; ++i) {
			List<PluginInfo> pagePluginInfoList	= new ArrayList<PluginInfo>();
			for(int j = i * 3; j < (i + 1) * 3 && j < pluginInfoList.size(); ++j) {
				pagePluginInfoList.add(pluginInfoList.get(j));
			}
			
			_pluginData.add(pagePluginInfoList);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_plugin_system, menu);
		
		return true;
	}
	
	private void printInfoList(List<PluginInfo> list) {
		for(int i = 0; i < list.size(); ++i) {
			PluginInfo pi	= list.get(i);
			PluginCfg pc	= pi.getPluginCfg();
			SwitchLogger.d(LOG_TAG, "id="+pc.getId()+",name="+pc.getName()+",priority="+pc.getPriority()+",order="+pc.getOrder()
									+",show="+pc.isShow()+",installed="+pi.isInstalled()+",currentVersionName="
									+pi.getCurrentVersionName()+",upgradeType="+pi.getUpgradeType());
		}
	}
	
	public void testSQLite(View v) {
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "test sqlite");
		
		PluginSystemDbManager	_dbMananger	= new PluginSystemDbManager(this);
		_dbMananger.setPluginVersion("test", "1.0.4");
		_dbMananger.setPluginVersion("song", "1.0.3");
		SwitchLogger.d(LOG_TAG, "get test version =" + _dbMananger.getPluginVersion("test"));
		_dbMananger.getAllPluginVersion();
	}
	
	public void testInstall(View v){
		SwitchLogger.setPrintLog(true);
		if(ActivityUtil.isAppInstalled(this, ComponentTestActivity.SONG_PACKAGE_NAME)){
			Tip.show(this, ComponentTestActivity.SONG_PACKAGE_NAME + " installed, start it");
			ActivityUtil.startApp(this, ComponentTestActivity.SONG_PACKAGE_NAME);
		} else {
			Tip.show(this, ComponentTestActivity.SONG_PACKAGE_NAME + " not installed");
		}
	}

	public void testUninstall(View v){
		SwitchLogger.setPrintLog(true);
		ActivityUtil.uninstallApp(this, ComponentTestActivity.SONG_PACKAGE_NAME);
	}
	
	@Override
	protected void onDestroy() {
		_ps.stop();
		super.onDestroy();
	}

	private class MyPluginSystemCallback extends PluginSystemCallback {
		
		@Override
		public void onPluginInfoReady(PluginInfoManager pluginInfoManager) {
			List<PluginInfo> pluginInfolist	= pluginInfoManager.getPluginInfoList();
			
			printInfoList(pluginInfolist);
			
			setPlguinData(pluginInfolist);

			LayoutInflater inflater	= TestPluginSystemActivity.this.getLayoutInflater();
			for(int i = 0; i < _pluginData.size(); ++i) {
				List<PluginInfo> pagePluginInfoList	= _pluginData.get(i);

				View pageView	= inflater.inflate(R.layout.page_view_layout, null);
				GridView gv		= (GridView)pageView.findViewById(R.id.page_grid_view);
				GridViewAdapter gvd	= new GridViewAdapter(TestPluginSystemActivity.this, pagePluginInfoList);
				SwitchLogger.d(LOG_TAG, "gv="+gv+"gvd="+gvd);
				gv.setAdapter(gvd);
				gv.setOnItemClickListener(new OnPluginClickListener(pagePluginInfoList)); 
				
				_pageViewList.add(pageView);
				_gridViewAdapterList.add(gvd);
			}
			
			_viewPager.setAdapter(new MyPagerAdapter(_pageViewList));
			_viewPager.setOnPageChangeListener(new MyPagerChangeListener());
		}

		@Override
		public void onPluginInfoChange(PluginInfoManager pluginInfoManager) {
			SwitchLogger.d(LOG_TAG, "onPluginInfoChange called");
			List<PluginInfo> pluginInfolist	= pluginInfoManager.getPluginInfoList();
			printInfoList(pluginInfolist);
			setPlguinData(pluginInfolist);
			
			for(int i = 0; i < _gridViewAdapterList.size(); ++i) {
				_gridViewAdapterList.get(i).setPagePluginInfoList(_pluginData.get(i));
				_gridViewAdapterList.get(i).notifyDataSetChanged();
			}
		}
		
		@Override
		public void onError(int errCode) {
			super.onError(errCode);
		}
	}
	
	/**********************以下类都是可以独立出来单独成为一个文件的*****************/
	
	public class OnPluginClickListener implements OnItemClickListener {
		
		
		List<PluginInfo> _pagePluginInfoList;
		
		public OnPluginClickListener(List<PluginInfo> pagePluginInfoList) {
			_pagePluginInfoList	= pagePluginInfoList;
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,  long id) {
			
			 SwitchLogger.d(LOG_TAG, ((TextView) view.findViewById(R.id.textView))  
                     .getText().toString() + " position:" + position + ",id="+id);  
     
			 ProgressBar	pb	= (ProgressBar)view.findViewById(R.id.progressBar);
			 TextView		tv	= (TextView)view.findViewById(R.id.progressText);
		     PluginManager pluginManager	= new PluginManager(TestPluginSystemActivity.this, 
													    		_pagePluginInfoList.get(position), 
													    		new BYXPluginManagerCallback(pb, tv));
		     
		     if(_pagePluginInfoList.get(position).isInstalled()) {
		    	 pluginManager.start();
		     } else {
		    	 pluginManager.install();
		     }
		}
	}
	
	public class BYXPluginManagerCallback extends PluginManagerCallback {
		
		private ProgressBar	_progressBar;
		private TextView	_progressText;

		public BYXPluginManagerCallback(ProgressBar progressBar, TextView progressText) {
			_progressBar	= progressBar;
			_progressText	= progressText;
		}
		
		@Override
		public void onComplete(int result) {
			_progressBar.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onDownloadStart() {
			_progressBar.setVisibility(View.VISIBLE);
			_progressText.setVisibility(View.VISIBLE);
		}

		@Override
		public void onDownloadProgress(long downloaded, long total) {
			int progress	= (int)((double)downloaded/total * 100);
			_progressText.setText(progress + "%");
		}

		@Override
		public void onDownloadEnd() {
			_progressBar.setVisibility(View.GONE);
			_progressText.setVisibility(View.GONE);
		}
	}
	
	public class MyPagerAdapter extends PagerAdapter {
    	
    	private List<View> list;
    	
    	public MyPagerAdapter(List<View> list) {
    		this.list = list;
		}
    	
		@Override
		public void destroyItem(View view, int index, Object arg2) {
			// TODO Auto-generated method stub
			((ViewPager)view).removeView(list.get(index));
			
			Log.d("MyPagerAdapter", "destroyItem");
		}

		@Override
		public void finishUpdate(View arg0) {
			// TODO Auto-generated method stub
			Log.d("MyPagerAdapter", "finishUpdate");
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
//			Log.d("MyPagerAdapter", "getCount");
			return list.size();
		}

		@Override
		public Object instantiateItem(View view, int index) {
			((ViewPager)view).addView(list.get(index), 0);
			Log.d("MyPagerAdapter", "instantiateItem, index=" + index);
			return list.get(index);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			// TODO Auto-generated method stub
			Log.d("MyPagerAdapter", "isViewFromObject");
			return view == (object);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
			// TODO Auto-generated method stub
			Log.d("MyPagerAdapter", "restoreState");
		}

		@Override
		public Parcelable saveState() {
			// TODO Auto-generated method stub
			Log.d("MyPagerAdapter", "saveState");
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
			// TODO Auto-generated method stub
		}
    }

	private class GridViewAdapter extends BaseAdapter {
		Context _context;
		List<PluginInfo>	_pagePluginInfoList;
		public GridViewAdapter(Context context, List<PluginInfo> pagePluginInfoList) {
			super();
			_context			= context;
			_pagePluginInfoList	= pagePluginInfoList;
		}

		@Override
		public int getCount() {
			return _pagePluginInfoList.size();
		}

		public void setPagePluginInfoList(List<PluginInfo> pagePluginInfoList) {
			_pagePluginInfoList	= pagePluginInfoList;
		}
		
		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = ((Activity) _context).getLayoutInflater();  
	        View twoEditTextLayoutRef = inflater.inflate(R.layout.grid_view_item,  
	                null);  
	        ImageView ivRef = (ImageView) twoEditTextLayoutRef.findViewById(R.id.imageView);  
	        TextView tvRef = (TextView) twoEditTextLayoutRef.findViewById(R.id.textView);  
	       
	        String name	= _pagePluginInfoList.get(position).getPluginCfg().getName();
	        PluginInfo pi	= _pagePluginInfoList.get(position);
	        
	        //ivRef.setImageResource(R.drawable.icon);  
	        (new ImgUtil((Activity)_context)).asyncSetImg(ivRef, pi.getPluginCfg().getIconUrl(), R.drawable.icon);
	        
	        if(pi.isInstalled()) {
	        	name += "(已安装";
//		        if(pi.getUpgradeType() == PluginVersion.UPGRADE_TYPE_LATEST) {
//		        	name += "，最新)";
//		        } else if(pi.getUpgradeType() == PluginVersion.UPGRADE_TYPE_FORCE) {
//		        	name += "，需强升)";
//		        } else {
//		        	name += "，可升)";
//		        }
	        } else {
	        	name += "(未安装)";
	        }
	        
	        tvRef.setText(name);
	        return twoEditTextLayoutRef; 
		}
	}
	
	public class MyPagerChangeListener implements OnPageChangeListener {

		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
//			Log.d("MyPagerChangeListener", "onPageScrollStateChanged");
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
//			Log.d("MyPagerChangeListener", "onPageScrolled");
		}

		public void onPageSelected(int position) {
//			((View)listDots.get(position)).setBackgroundResource(R.drawable.dot_focused);
//			((View)listDots.get(oldPosition)).setBackgroundResource(R.drawable.dot_normal);
//			oldPosition = position;
			
			Log.d("MyPagerChangeListener", "onPageSelected");
		}
    }
	
	/************************可以独立出来的类结束 ********************************/
}
