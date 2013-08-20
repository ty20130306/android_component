package com.vanchu.libs.pluginSystem;

import java.io.File;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.vanchu.libs.common.task.Downloader;
import com.vanchu.libs.common.task.Downloader.IDownloadListener;
import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.ActivityUtil;
import com.vanchu.libs.common.util.SwitchLogger;

public class PluginManager {
	
	public static final int	RESULT_SKIP_UPGRADE			= 1;
	public static final int	RESULT_LATEST_VERSION		= 2;
	public static final int	RESULT_INSTALL_STARTED		= 3;
	public static final int RESULT_ERR_DOWNLOAD_FAIL	= 4;
	
	private static final String 	LOG_TAG	= PluginManager.class.getSimpleName();
	
	private Context					_context;
	private PluginManagerCallback	_callback;
	
	private PluginInfo	_pluginInfo;
	private PluginCfg	_pluginCfg;
	
	public PluginManager(Context context, PluginInfo pluginInfo, PluginManagerCallback callback) {
		_context		= context;
		_pluginInfo		= pluginInfo;
		_pluginCfg		= _pluginInfo.getPluginCfg();
		
		_callback		= callback;
	}
	
	public void start() {
		SwitchLogger.d(LOG_TAG, "onClick");
		
		if(_pluginInfo.isInstalled()) {
			ActivityUtil.startApp(_context, _pluginCfg.getPackageName(), _pluginCfg.getClassName());
		} else {
			Tip.show(_context, "请先安装" + _pluginCfg.getName()+"插件");
		}
	}
	
	public void install() {
		if(_pluginInfo.isInstalled()) {
			Tip.show(_context, "已安装" + _pluginCfg.getName()+"插件");
			return ;
		}
		
		download();
	}
	
	public void uninstall(){
		
		if(_pluginInfo.isInstalled()) {
			ActivityUtil.uninstallApp(_context, _pluginInfo.getPluginCfg().getPackageName());
		}
	}
	
	private void download() {
		new Downloader(_context, _pluginCfg.getApkUrl(), new PluginDownloadListener()).run();
	}
	
	private void install(String downloadFile){
		SwitchLogger.d(LOG_TAG, "download complete, begin to install");

		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(new File(downloadFile));
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		_context.startActivity(intent);
		
		_callback.onComplete(RESULT_INSTALL_STARTED);
	}
	
	private class PluginDownloadListener implements IDownloadListener {
		
		@Override
		public void onStart() {
			_callback.onDownloadStart();
		}
		
		@Override
		public void onProgress(long downloaded, long total) {
			_callback.onDownloadProgress(downloaded, total);
		}

		@Override
		public void onSuccess(String downloadFile) {
			_callback.onDownloadEnd();
			SwitchLogger.d(LOG_TAG, downloadFile + " downloaded");
			install(downloadFile);
		}

		@Override
		public void onError(int errCode) {
			switch (errCode) {
			case Downloader.DOWNLOAD_ERR_SOCKET_TIMEOUT:
			case Downloader.DOWNLOAD_ERR_IO:
			case Downloader.DOWNLOAD_ERR_SPACE_NOT_ENOUGH:
			case Downloader.DOWNLOAD_ERR_URL:
				_callback.onComplete(RESULT_ERR_DOWNLOAD_FAIL);
				break;

			default:
				break;
			}
		}
	}
}
