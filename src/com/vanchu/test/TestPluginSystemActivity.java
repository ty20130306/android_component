package com.vanchu.test;

import java.util.ArrayList;
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
import com.vanchu.libs.pluginSystem.PluginVersion;

import com.vanchu.sample.PluginSystemDbManager;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TestPluginSystemActivity extends Activity {
	private static final String 	LOG_TAG	= TestPluginSystemActivity.class.getSimpleName();
	
	private List<PluginInfo> _list = new ArrayList<PluginInfo>();
	private GridView _gv;
	private GridViewAdapter _gvAdapater;
	private ImgUtil			_imgUtil;
	private PluginSystem _ps;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_plugin_system);
		
		initImgUtil();
		_gv	= (GridView) findViewById(R.id.gridView);
		_gv.setVisibility(View.GONE);
		
		_gv.setOnItemClickListener(new OnItemClickListener() {  
			 
            public void onItemClick(AdapterView<?> parent, View view, int position,  long id) {  
                SwitchLogger.d(LOG_TAG, ((TextView) view.findViewById(R.id.textView1))  
                                .getText().toString() + " position:" + position + ",id="+id);  
                
                
                PluginManager pluginManager	= new PluginManager(TestPluginSystemActivity.this, _list.get(position), new PluginManagerCallback());
                if(pluginManager.getUpgradeType() == PluginVersion.UPGRADE_TYPE_NONE
                		|| pluginManager.getUpgradeType() == PluginVersion.UPGRADE_TYPE_FORCE) 
                {
                	pluginManager.upgrade();
                } else {
                	pluginManager.start();
                }
            }
        });  
		
		
		_ps	= new PluginSystem(this, 
				"http://pesiwang.devel.rabbit.oa.com/test_plugin_system.php", 
				new MyPluginSystemCallback());
		
		_ps.run();
	}
	
	private void initImgUtil() {
		_imgUtil	= new ImgUtil(this);
	}
	
	private class GridViewAdapter extends BaseAdapter {
		Context _context;
		
		public GridViewAdapter(Context context) {
			super();
			_context	= context;
		}

		@Override
		public int getCount() {
			return _list.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = ((Activity) _context).getLayoutInflater();  
	        View twoEditTextLayoutRef = inflater.inflate(R.layout.grid_view_item,  
	                null);  
	        ImageView ivRef = (ImageView) twoEditTextLayoutRef  
	                .findViewById(R.id.imageView1);  
	        TextView tvRef = (TextView) twoEditTextLayoutRef  
	                .findViewById(R.id.textView1);  
	 
	       
	        String name	= _list.get(position).getPluginCfg().getName();
	        PluginInfo pi	= _list.get(position);
	        
	        //ivRef.setImageResource(R.drawable.icon);  
	        _imgUtil.asyncSetImg(ivRef, pi.getPluginCfg().getIconUrl(), R.drawable.icon);
	        
	        if(pi.isInstalled()) {
	        	name += "(已安装";
		        if(pi.getUpgradeType() == PluginVersion.UPGRADE_TYPE_LATEST) {
		        	name += "，最新)";
		        } else if(pi.getUpgradeType() == PluginVersion.UPGRADE_TYPE_FORCE) {
		        	name += "，需强升)";
		        } else {
		        	name += "，可升)";
		        }
	        } else {
	        	name += "(未安装)";
	        }
	        
	        
	        tvRef.setText(name);  
	        return twoEditTextLayoutRef;  
		}
	}
	
	private class MyPluginSystemCallback extends PluginSystemCallback {
		
		@Override
		public void onPluginInfoReady(PluginInfoManager pluginInfoManager) {
			_list	= pluginInfoManager.getPluginInfoList();
			
			printInfoList(_list);
			_gvAdapater	= new GridViewAdapter(TestPluginSystemActivity.this);
			_gv.setAdapter(_gvAdapater);
			_gv.setVisibility(View.VISIBLE);
		}

		@Override
		public void onPluginInfoChange(PluginInfoManager pluginInfoManager) {
			SwitchLogger.d(LOG_TAG, "onPluginInfoChange called");
			_list	= pluginInfoManager.getPluginInfoList();
			printInfoList(_list);
			_gvAdapater.notifyDataSetChanged();
		}
		
		@Override
		public void onError(int errCode) {
			super.onError(errCode);
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
}
