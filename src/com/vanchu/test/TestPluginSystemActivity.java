package com.vanchu.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

import com.vanchu.sample.DbManager;

import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.content.Context;
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
	
	private ViewPager	_viewPager;
	private List<View>	_pageViewList;
	private List<GridViewAdapter>	_gridViewAdapterList;
	private List<View>	_dotList;
	private	int	_lastPagePosition;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_plugin_system);
		
		_pluginData	= null;
		_viewPager	= (ViewPager)findViewById(R.id.view_pager);
		_lastPagePosition	= 0;
		
		initDotList();
		_pageViewList			= new ArrayList<View>();
		_gridViewAdapterList	= new ArrayList<GridViewAdapter>(); 
		
		_ps	= new PluginSystem(this, 
				"http://pesiwang.devel.rabbit.oa.com/test_plugin_system.php", 
				new MyPluginSystemCallback());
		
		_ps.run();
	}
	
	private void initDotList() {
		_dotList	= new ArrayList<View>();
		_dotList.add(findViewById(R.id.page_dot_0));
		_dotList.add(findViewById(R.id.page_dot_1));
		_dotList.add(findViewById(R.id.page_dot_2));
		_dotList.add(findViewById(R.id.page_dot_3));
		_dotList.add(findViewById(R.id.page_dot_4));
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
									+pi.getCurrentVersionName());
		}
	}
	
	public void testSQLite(View v) {
		SwitchLogger.setPrintLog(true);
		SwitchLogger.d(LOG_TAG, "test sqlite");
		
		DbManager	_dbMananger	= new DbManager(this);
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
	
	private void showDotList() {
		for(int i = 0; i < _pluginData.size() && i < _dotList.size(); ++i) {
			_dotList.get(i).setVisibility(View.VISIBLE);
		}
	}

	private class MyPluginSystemCallback extends PluginSystemCallback {
		
		@Override
		public void onPluginInfoReady(PluginInfoManager pluginInfoManager) {
			List<PluginInfo> pluginInfolist	= pluginInfoManager.getPluginInfoList();
			
			printInfoList(pluginInfolist);
			
			setPlguinData(pluginInfolist);
			showDotList();
			
			LayoutInflater inflater	= TestPluginSystemActivity.this.getLayoutInflater();
			for(int i = 0; i < _pluginData.size(); ++i) {
				List<PluginInfo> pagePluginInfoList	= _pluginData.get(i);

				View pageView	= inflater.inflate(R.layout.page_view_layout, null);
				GridView gv		= (GridView)pageView.findViewById(R.id.page_grid_view);
				GridViewAdapter gvd	= new GridViewAdapter(TestPluginSystemActivity.this, pagePluginInfoList, i);
				SwitchLogger.d(LOG_TAG, "gv="+gv+"gvd="+gvd);
				gv.setAdapter(gvd);
				
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
	
	private void changeToEditing() {
		for(int i = 0; i < _pluginData.size(); ++i) {
			for(int j = 0; j < _pluginData.get(i).size(); ++j) {
				if( ! _pluginData.get(i).get(j).getPluginCfg().isSticky()){
					_pluginData.get(i).get(j).setEditing(true);
				}
			}
		}
	}
	
	private void changeToNormal() {
		for(int i = 0; i < _pluginData.size(); ++i) {
			for(int j = 0; j < _pluginData.get(i).size(); ++j) {
				_pluginData.get(i).get(j).setEditing(false);
			}
		}
	}

	private class GridItemLongClickListener implements View.OnLongClickListener {
		
		@Override
		public boolean onLongClick(View v) {
			SwitchLogger.d(LOG_TAG, "OnGridItemLongClickListener");
			changeToEditing();
			View bg	= TestPluginSystemActivity.this.findViewById(R.id.plugin_system_bg);
			bg.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					changeToNormal();
					for(int i = 0; i < _gridViewAdapterList.size(); ++i) {
						_gridViewAdapterList.get(i).setPagePluginInfoList(_pluginData.get(i));
						_gridViewAdapterList.get(i).notifyDataSetChanged();
					}
				}
			});
			
			for(int i = 0; i < _gridViewAdapterList.size(); ++i) {
				_gridViewAdapterList.get(i).setPagePluginInfoList(_pluginData.get(i));
				_gridViewAdapterList.get(i).notifyDataSetChanged();
			}
			
			return true;
		}
	}
	
	public class MyPagerChangeListener implements OnPageChangeListener {

		public void onPageScrollStateChanged(int state) {

		}

		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

		}

		public void onPageSelected(int position) {
			if(position < _dotList.size()) {
				((ImageView)_dotList.get(position)).setImageResource(R.drawable.ps_page_selected);
				((ImageView)_dotList.get(_lastPagePosition)).setImageResource(R.drawable.ps_page_unselected);
				_lastPagePosition = position;
			}
			
			Log.d("MyPagerChangeListener", "onPageSelected, position="+position);
		}
    }

	/**********************以下类都是可以独立出来单独成为一个文件的*****************/
	
	public class GridItemClickListener	implements View.OnClickListener {
		
		Context _context;
		int		_pageIndex;
		int		_itemIndex;
		
		public GridItemClickListener(Context context, int pageIndex, int itemIndex) {
			_context	= context;
			_pageIndex	= pageIndex;
			_itemIndex	= itemIndex;
		}
		
		@Override
		public void onClick(View v) {
			SwitchLogger.d(LOG_TAG, "onItemClick, page index:" + _pageIndex + ",item index="+_itemIndex);  

			ProgressBar	pb	= (ProgressBar)v.findViewById(R.id.progressBar);
			TextView	pt	= (TextView)v.findViewById(R.id.progressText);
			TextView	nt	= (TextView)v.findViewById(R.id.textView);
			ImageView	installIcon	= (ImageView)v.findViewById(R.id.installIcon);
			PluginInfo	pi	= _pluginData.get(_pageIndex).get(_itemIndex);
			PluginManager pluginManager	= new PluginManager(_context, pi, new BYXPluginManagerCallback(pb, pt, nt, 
																					installIcon, pi.getPluginCfg().getName()));
			
			if(pi.isInstalled()) {
				if(pi.isEditing()) {
					pluginManager.uninstall();
				} else {
					pluginManager.start();
				}
			} else {
				pluginManager.install();
			}
		}
	}
	
	public class BYXPluginManagerCallback extends PluginManagerCallback {
		
		private ProgressBar	_progressBar;
		private TextView	_progressText;
		private TextView	_pluginNameText;
		private ImageView	_installIcon;
		private String		_pluginName;
		
		public BYXPluginManagerCallback(ProgressBar progressBar, TextView progressText, 
										TextView pluginNameText, ImageView installIcon, String pluginName) {
			_progressBar	= progressBar;
			_progressText	= progressText;
			_pluginNameText	= pluginNameText;
			_installIcon	= installIcon;
			_pluginName		= pluginName;
		}
		
		@Override
		public void onComplete(int result) {
			_progressBar.setVisibility(View.INVISIBLE);
			_pluginNameText.setText(_pluginName);
		}

		@Override
		public void onDownloadStart() {
			_progressBar.setVisibility(View.VISIBLE);
			_progressText.setVisibility(View.VISIBLE);
			_pluginNameText.setText("下载中");
			_installIcon.setVisibility(View.GONE);
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
			_pluginNameText.setText(_pluginName);
			_installIcon.setVisibility(View.VISIBLE);
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
			view	= null;
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

	public class GridViewAdapter extends BaseAdapter {
		Context _context;
		int		_pageIndex;
		List<PluginInfo>	_pagePluginInfoList;
		
		public GridViewAdapter(Context context, List<PluginInfo> pagePluginInfoList, int pageIndex) {
			super();
			_context			= context;
			_pagePluginInfoList	= pagePluginInfoList;
			_pageIndex			= pageIndex;
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
	        PluginInfo pi	= _pagePluginInfoList.get(position);
	        String name		= pi.getPluginCfg().getName();
			
			LayoutInflater inflater = ((Activity) _context).getLayoutInflater();
	        View itemView = inflater.inflate(R.layout.grid_view_item, null);  
	        
	        ImageView iamgeView		= (ImageView) itemView.findViewById(R.id.imageView);  
	        TextView textView		= (TextView) itemView.findViewById(R.id.textView);
	        textView.setText(name);
	        ImageView installIcon	= (ImageView) itemView.findViewById(R.id.installIcon);
	        ImageView uninstallIcon	= (ImageView) itemView.findViewById(R.id.uninstallIcon);
	        
	        (new ImgUtil((Activity)_context)).asyncSetImg(iamgeView, pi.getPluginCfg().getIconUrl(), R.drawable.icon);
	        
	        if( ! pi.isInstalled()) {
	        	uninstallIcon.setVisibility(View.GONE);
	        	installIcon.setVisibility(View.VISIBLE);
	        } else {
	        	if(pi.isEditing()) {
	        		uninstallIcon.setVisibility(View.VISIBLE);
	        		installIcon.setVisibility(View.GONE);
	        	} else {
	        		uninstallIcon.setVisibility(View.GONE);
	        		installIcon.setVisibility(View.GONE);
	        	}
	        }
	       
	        itemView.setOnClickListener(new GridItemClickListener(_context, _pageIndex, position));
	        itemView.setOnLongClickListener(new GridItemLongClickListener());
	        return itemView; 
		}
	}
	
	/************************可以独立出来的类结束 ********************************/

}
