package com.vanchu.libs.upgrade;

import java.io.File;

import com.vanchu.libs.common.task.Downloader;
import com.vanchu.libs.common.task.Downloader.IDownloadListener;
import com.vanchu.libs.common.util.SwitchLogger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;

public class UpgradeManager {
	
	private static final String LOG_TAG = UpgradeManager.class.getSimpleName();
	
	private UpgradeParam		_param;
	private UpgradeCallback		_callback;
	private Context				_context;
	
	private int					_upgradeType;
	private String				_downloadPath;
	
	private Dialog				_detailDialog;
	
	public UpgradeManager (Context context, UpgradeParam param, UpgradeCallback callback){
		_context	= context;
		_param		= param;
		_callback	= callback;
		
		_upgradeType		= _param.getUpgradeType();
		
		_detailDialog		= createDetailDialog();
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
			_callback.exitApp();
		}
	}
	
	private void errorCommonHandler(){
		if(UpgradeParam.UPGRADE_TYPE_FORCE == _param.getUpgradeType()){
			_callback.onComplete(UpgradeResult.RESULT_ERROR_FATAL);
			_callback.exitApp();
		} else {
			_callback.onComplete(UpgradeResult.RESULT_ERROR_SKIPPABLE);
		}
	}
	
	private void publishDownloadProgress(long downloaded, long total){
		_callback.onDownloadProgress(downloaded, total);
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
			_callback.onDownloadStarted();
		}
		
		@Override
		public void onProgress(long downloaded, long total) {
			publishDownloadProgress(downloaded, total);
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
		
		@Override
		public void onPause() {
			
		}
	}
	
	private void download(){
		new Downloader(_context, _param.getUpgradeApkUrl(), "upgrade", new UpgradeDownloadListener(), true).run();
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
