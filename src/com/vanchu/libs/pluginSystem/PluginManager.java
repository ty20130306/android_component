package com.vanchu.libs.pluginSystem;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;

import com.vanchu.libs.common.task.Downloader;
import com.vanchu.libs.common.task.Downloader.IDownloadListener;
import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.ActivityUtil;
import com.vanchu.libs.common.util.SwitchLogger;


public class PluginManager {
	
	public static final int	RESULT_SKIP_UPGRADE			= 1;
	public static final int	RESULT_LATEST_VERSION		= 2;
	public static final int	RESULT_INSTALL_STARTED		= 3;
	
	private static final String 	LOG_TAG	= PluginManager.class.getSimpleName();
	
	private Context					_context;
	private PluginManagerCallback	_callback;
	
	private PluginInfo	_pluginInfo;
	private PluginCfg	_pluginCfg;
	
	private Dialog		_detailDialog;
	private ProgressDialog	_progressDialog;
	
	public PluginManager(Context context, PluginInfo pluginInfo, PluginManagerCallback callback) {
		_context		= context;
		_pluginInfo		= pluginInfo;
		_pluginCfg		= _pluginInfo.getPluginCfg();
		
		_callback		= callback;
		_detailDialog	= createDetailDialog();
		
		_progressDialog	= createProgressDialog();
		
	}
	
	public void start() {
		SwitchLogger.d(LOG_TAG, "onClick");
		
		if(_pluginInfo.isInstalled()) {
			if(_pluginInfo.getUpgradeType() == PluginVersion.UPGRADE_TYPE_FORCE) {
				Tip.show(_context, "请先更新" + _pluginCfg.getName()+"插件");
			} else {
				ActivityUtil.startApp(_context, _pluginCfg.getPackageName(), _pluginCfg.getClassName());
			}
		} else {
			Tip.show(_context, "请先安装" + _pluginCfg.getName()+"插件");
		}
	}
	
	public void upgrade() {
		if(_pluginInfo.getUpgradeType() == PluginVersion.UPGRADE_TYPE_LATEST) {
			_callback.onComplete(RESULT_LATEST_VERSION);
			return ;
		}
		
		if(_pluginInfo.isInstalled()) {
			_detailDialog.show();
		} else {
			download();
		}
	}
	
	public int getUpgradeType() {
		return _pluginInfo.getUpgradeType();
	}
	
	private void download() {
		new Downloader(_context, _pluginCfg.getPluginVersion().getApkUrl(), new PluginDownloadListener()).run();
	}
	
	protected Dialog createDetailDialog(){
		AlertDialog.Builder dialog = new AlertDialog.Builder(_context) {
			@Override
			public AlertDialog create() {
				setCancelable(false);
				return super.create();
			}
		};
		
		PluginOnClickListener listener = new PluginOnClickListener();
		dialog.setTitle("检测到新版本");
		String tip = String.format("当前版本: %s<br />更新版本: %s<br />更新内容: <br />%s", 
							_pluginInfo.getCurrentVersionName(),
							_pluginInfo.getPluginCfg().getPluginVersion().getHighestName(),
							_pluginInfo.getPluginCfg().getPluginVersion().getUpgradeDetail());
		
		Spanned span	= Html.fromHtml(tip);
		tip	= span.toString();
		
		dialog.setMessage(tip);
		dialog.setPositiveButton("立即更新", listener);
		if (_pluginInfo.getUpgradeType() == PluginVersion.UPGRADE_TYPE_OPTIONAL) {
			dialog.setNeutralButton("以后再说", listener);
		}
		
		return dialog.create();
	}
	
	private class PluginOnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int which){
			dialog.dismiss();
			
			switch (_pluginInfo.getUpgradeType()) {
				case PluginVersion.UPGRADE_TYPE_FORCE:
					SwitchLogger.d(LOG_TAG, "force to upgrade");
					download();
					break;
					
				case PluginVersion.UPGRADE_TYPE_OPTIONAL:
					switch(which){
						case DialogInterface.BUTTON_POSITIVE:
							SwitchLogger.d(LOG_TAG, "optional to upgrade, choose to upgrade");
							download();
							break;
							
						case DialogInterface.BUTTON_NEUTRAL:
							SwitchLogger.d(LOG_TAG, "optional to upgrade, choose to ignore");
							_callback.onComplete(PluginManager.RESULT_SKIP_UPGRADE);
							break;
					}
					break;
					
				default:
					break;
			}
		}
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
	
	private ProgressDialog createProgressDialog(){
		ProgressDialog progressDialog	= new ProgressDialog(_context);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMax(100);
		progressDialog.setTitle("下载进度");
		progressDialog.setMessage("正在准备下载安装包");
		
		return progressDialog;
	}
	
	private class PluginDownloadListener implements IDownloadListener {
		
		@Override
		public void onStart() {
			_progressDialog.show();
		}
		
		@Override
		public void onProgress(long downloaded, long total) {
			_progressDialog.setProgress((int)(downloaded * 100 / total));
			String tip	= String.format("正在下载安装包...\n已下载: %d K\n总大小: %d K",
										(int)(downloaded / 1024), (int)(total / 1024) );
			
			_progressDialog.setMessage(tip);
		}

		@Override
		public void onSuccess(String downloadFile) {
			_progressDialog.hide();
			SwitchLogger.d(LOG_TAG, downloadFile + " downloaded");
			install(downloadFile);
		}

		@Override
		public void onError(int errCode) {
			switch (errCode) {
			case Downloader.DOWNLOAD_ERR_SOCKET_TIMEOUT:
				break;
				
			case Downloader.DOWNLOAD_ERR_IO:

				break;
				
			case Downloader.DOWNLOAD_ERR_SPACE_NOT_ENOUGH:

				break;
				
			case Downloader.DOWNLOAD_ERR_URL:

				break;
				
			default:
				break;
			}
		}
	}
}
