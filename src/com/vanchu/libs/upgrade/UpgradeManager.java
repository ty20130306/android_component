package com.vanchu.libs.upgrade;

import java.io.File;

import com.vanchu.libs.common.task.Downloader;
import com.vanchu.libs.common.task.Downloader.IDownloadListener;
import com.vanchu.libs.common.util.SwitchLogger;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;

public class UpgradeManager {
	
	private static final String LOG_TAG = UpgradeManager.class.getSimpleName();

	private static final int EXIT_DELAY_DURATION	= 2000; // millisecond
	
	private UpgradeParam		_param;
	private UpgradeCallback		_callback;
	private Context				_context;
	
	private int					_upgradeType;
	private String				_downloadPath;
	
	private Dialog				_detailDialog;
	private Handler				_handler;
	private boolean				_downloadStarted;
	
	public UpgradeManager (Context context, UpgradeParam param, UpgradeCallback callback){
		_context	= context;
		_param		= param;
		_callback	= callback;
		
		_upgradeType		= _param.getUpgradeType();
		
		_detailDialog		= createDetailDialog();
		_handler			= new Handler();
		_downloadStarted	= false;
	}
	
	public Context getContext(){
		return _context;
	}
	
	public void check(){
		if(UpgradeParam.UPGRADE_TYPE_LATEST == _upgradeType){
			_callback.onLatestVersion();
			_callback.onComplete(UpgradeResult.RESULT_LATEST_VERSION);
			return ;
		}
		
		upgrade();
	}
	
	protected UpgradeParam getParam(){
		return _param;
	}
	
	protected Dialog createDetailDialog(){
		AlertDialog.Builder dialog = new AlertDialog.Builder(_context) {
			@Override
			public AlertDialog create() {
				setCancelable(false);
				return super.create();
			}
		};
		
		UpgradeOnClickListener listener = new UpgradeOnClickListener();
		dialog.setTitle("检测到新版本");
		String tip = String.format("当前版本: %s<br />更新版本: %s<br />更新内容: <br />%s", 
							_param.getCurrentVersionName(),
							_param.getHighestVersionName(),
							_param.getUpgradeDetail());
		Spanned span	= Html.fromHtml(tip);
		tip	= span.toString();
		
		dialog.setMessage(tip);
		dialog.setPositiveButton("立即更新", listener);
		if (_upgradeType == UpgradeParam.UPGRADE_TYPE_OPTIONAL) {
			dialog.setNeutralButton("以后再说", listener);
		}
		
		return dialog.create();
	}
	
	public void chooseToUpgrade(){
		SwitchLogger.d(LOG_TAG, "chooseToUpgrade");
		
		if(_detailDialog != null){
			_detailDialog.dismiss();
		}
		
		download();
	}
	
	public void choosetToSkip(){
		SwitchLogger.d(LOG_TAG, "choosetToSkip");
		
		if(_detailDialog != null){
			_detailDialog.dismiss();
		}
	}
	
	private void install(){
		SwitchLogger.d(LOG_TAG, "download complete, begin to install");
		doInstall();
		_callback.onInstallStarted();
		_callback.onComplete(UpgradeResult.RESULT_INSTALL_STARTED);
		
		if(UpgradeParam.UPGRADE_TYPE_FORCE == _param.getUpgradeType()){
			exitApp();
		}
	}
	
	private void errorCommonHandler(){
		if(UpgradeParam.UPGRADE_TYPE_FORCE == _param.getUpgradeType()){
			_callback.onComplete(UpgradeResult.RESULT_ERROR_FATAL);
			exitApp();
		} else {
			_callback.onComplete(UpgradeResult.RESULT_ERROR_SKIPPABLE);
		}
	}
	
	private void publishProgress(long downloaded, long total){
		_callback.onProgress(downloaded, total);
	}

	private void exitApp(){
		_handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				((Activity)_context).finish();
				
				SwitchLogger.d(LOG_TAG, "exiting app " + _context.getPackageName());
				ActivityManager activityManager	= (ActivityManager)_context.getSystemService(Context.ACTIVITY_SERVICE);
				activityManager.restartPackage(_context.getPackageName());
				
				System.exit(0);
			}
		}, EXIT_DELAY_DURATION);
		
	}
	
	private void doInstall(){
		SwitchLogger.d(LOG_TAG, "install path: " + _downloadPath);
		
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(new File(_downloadPath));
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		_context.startActivity(intent);
	}
	
	private void upgrade(){
		_detailDialog.show();
	}

	private class UpgradeDownloadListener implements IDownloadListener {

		@Override
		public void onStart() {
			// do nothing
		}
		
		@Override
		public void onProgress(long downloaded, long total) {
			if( ! _downloadStarted) {
				_downloadStarted	= true;
				_callback.onDownloadStarted();
			}
			publishProgress(downloaded, total);
		}

		@Override
		public void onSuccess(String downloadFile) {
			_downloadPath	= downloadFile;
			install();
		}

		@Override
		public void onError(int errCode) {
			switch (errCode) {
			case Downloader.DOWNLOAD_ERR_SOCKET_TIMEOUT:
				_callback.onSocketTimeout();
				break;
				
			case Downloader.DOWNLOAD_ERR_IO:
				_callback.onIoError();
				break;
				
			case Downloader.DOWNLOAD_ERR_SPACE_NOT_ENOUGH:
				_callback.onStorageNotEnough();
				break;
				
			case Downloader.DOWNLOAD_ERR_URL:
				_callback.onUrlError();
				break;
				
			default:
				break;
			}
			
			errorCommonHandler();
		}
	}
	
	private void download(){
		new Downloader(_context, _param.getUpgradeApkUrl(), new UpgradeDownloadListener()).run();
	}
	
	private class UpgradeOnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int which){
			dialog.dismiss();
			
			switch (_upgradeType) {
				case UpgradeParam.UPGRADE_TYPE_FORCE:
					SwitchLogger.d(LOG_TAG, "force to upgrade");
					download();
					break;
					
				case UpgradeParam.UPGRADE_TYPE_OPTIONAL:
					switch(which){
						case DialogInterface.BUTTON_POSITIVE:
							SwitchLogger.d(LOG_TAG, "optional to upgrade, choose to upgrade");
							download();
							break;
							
						case DialogInterface.BUTTON_NEUTRAL:
							SwitchLogger.d(LOG_TAG, "optional to upgrade, choose to ignore");
							_callback.onSkipUpgrade();
							_callback.onComplete(UpgradeResult.RESULT_SKIP_UPGRADE);
							break;
					}
					break;
					
				default:
					break;
			}
		}
	}
}
