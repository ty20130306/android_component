package com.vanchu.libs.upgrade;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import com.vanchu.libs.common.SwitchLogger;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;

public class UpgradeManager extends ProgressDialog {
	
	private static final String LOG_TAG = UpgradeManager.class.getSimpleName();
	
	private static final int DOWNLOAD_BUFFER_SIZE		= 2048;
	
	private static final int CONNECT_TIMEOUT		= 5000; // millisecond
	private static final int READ_TIMEOUT			= 5000; // millisecond
	private static final int EXIT_DELAY_DURATION	= 2000; // millisecond
	
	private static final int TIMEOUT_RETRY_MAX		= 3;
	
	private static final int DOWNLOAD_STORAGE_TYPE_SDCARD		= 1;
	private static final int DOWNLOAD_STORAGE_TYPE_DEVICE_MEM	= 2;
	
	private static final int DOWNLOAD_DONE					= 0;
	private static final int DOWNLOAD_PROGRESS				= 1;
	private static final int DOWNLOAD_ERR_IO				= 2;
	private static final int DOWNLOAD_ERR_URL				= 3;
	private static final int DOWNLOAD_ERR_SPACE_NOT_ENOUGH	= 4;
	private static final int DOWNLOAD_ERR_SOCKET_TIMEOUT	= 5;
	
	private UpgradeParam		_param;
	private UpgradeCallback		_callback;
	private Context				_context;
	
	private int					_upgradeType;
	
	private int 				_downloadStorageType;
	private String				_downloadPath;
	private String				_downloadDir;
	private String				_tmpDownloadPath;
	
	private int 				_timeoutRetryCnt;
	
	private Handler	_handler = new Handler(){
		
		@Override
		public void handleMessage(Message msg){

			switch (msg.what) {
			case DOWNLOAD_PROGRESS:
				updateProgress(msg);
				break;
				
			case DOWNLOAD_ERR_SOCKET_TIMEOUT:
				handleSocketTimeout();
				break;
				
			case DOWNLOAD_ERR_IO:
				handleIoError();
				break;
				
			case DOWNLOAD_ERR_SPACE_NOT_ENOUGH:
				handleSpaceNotEnough(msg);
				break;
				
			case DOWNLOAD_ERR_URL:
				handleUrlError();
				break;
				
			case DOWNLOAD_DONE:
				install();
				break;
			}
		}
	};	
	
	public UpgradeManager (Context context, UpgradeParam param, UpgradeCallback callback){
		super(context);
		
		_context	= context;
		_param		= param;
		_callback	= callback;
		
		_upgradeType			= _param.getUpgradeType();
		_downloadStorageType	= DOWNLOAD_STORAGE_TYPE_SDCARD;
		
		_timeoutRetryCnt		= 0;
		
		initDownloadStorage(false);
		initProgressDialog();
	}
	
	public void check(){
		if(UpgradeParam.UPGRADE_TYPE_LATEST == _upgradeType){
			deleteInstalledApkFile();
			_callback.onLatestVersion();
			_callback.onComplete(UpgradeResult.RESULT_LATEST_VERSION);
			return ;
		}
		
		upgrade();
	}
	
	private void install(){
		SwitchLogger.d(LOG_TAG, "download complete, begin to install");
		UpgradeUtil.rename(_tmpDownloadPath, _downloadPath);
		UpgradeUtil.chmod(_downloadPath, "777");
		doInstall();
		dismiss();
		_callback.onInstallStarted();
		_callback.onComplete(UpgradeResult.RESULT_INSTALL_STARTED);
		
		if(UpgradeParam.UPGRADE_TYPE_FORCE == _param.getUpgradeType()){
			exitApp();
		}
	}
	
	private void handleUrlError(){
		_callback.onUrlError();
		errorCommonHandler();
	}
	
	private void handleSpaceNotEnough(Message msg){
		if(_downloadStorageType == DOWNLOAD_STORAGE_TYPE_SDCARD){
			SwitchLogger.d(LOG_TAG, "sdcard space not enough, try device mem");
			initDownloadStorage(true);
			download();
		} else {
			DownloadProgress progress	= (DownloadProgress)msg.obj;
			_callback.onStorageNotEnough(progress.total);
			errorCommonHandler();
		}
	}
	
	private void handleIoError(){
		if(_downloadStorageType == DOWNLOAD_STORAGE_TYPE_SDCARD){
			SwitchLogger.d(LOG_TAG, "download to sdcard fail, try device mem");
			initDownloadStorage(true);
			download();
		} else {
			_callback.onIoError();
			errorCommonHandler();
		}
	}
	
	private void handleSocketTimeout(){
		if(_timeoutRetryCnt < TIMEOUT_RETRY_MAX){
			_timeoutRetryCnt++;
			SwitchLogger.d(LOG_TAG, 
							"socket timeout, retry it, timeout retry count now: " + _timeoutRetryCnt);
			download();
		} else {
			_callback.onSocketTimeout();
			errorCommonHandler();
		}
	}
	
	private void errorCommonHandler(){
		dismiss();
		if(UpgradeParam.UPGRADE_TYPE_FORCE == _param.getUpgradeType()){
			_callback.onComplete(UpgradeResult.RESULT_ERROR_FATAL);
			exitApp();
		} else {
			_callback.onComplete(UpgradeResult.RESULT_ERROR_SKIPPABLE);
		}
	}
	
	private void updateProgress(Message msg){
		DownloadProgress progress = (DownloadProgress)msg.obj;
		setProgress((int)(progress.hasRead * 100 / progress.total));
		String tip	= String.format("正在下载安装包...\n已下载: %d K\n总大小: %d K",
									(int)(progress.hasRead / 1024), (int)(progress.total / 1024) );
		
		setMessage(tip);
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
		SwitchLogger.d(LOG_TAG, "install path: "+_downloadPath);
		
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(new File(_downloadPath));
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		_context.startActivity(intent);
		dismiss();
	}
	
	private void initProgressDialog(){
		setCancelable(false);
		setProgressStyle(STYLE_HORIZONTAL);
		setMax(100);
		setTitle("下载进度");
		setMessage("正在准备下载安装包");
	}
	
	private void initDownloadStorage(boolean useDeviceMem){
		File file	= null;
		
		if( ! useDeviceMem && UpgradeUtil.isSDCardReady()){
			_downloadStorageType	= DOWNLOAD_STORAGE_TYPE_SDCARD;
			file			= Environment.getExternalStorageDirectory();
		} else {
			_downloadStorageType	= DOWNLOAD_STORAGE_TYPE_DEVICE_MEM;
			file			= _context.getFilesDir();
		}
		
		_downloadDir		= file.getAbsolutePath();
		_downloadPath		= _downloadDir + "/" + _param.getApkFileName();
		_tmpDownloadPath	= _downloadPath + ".tmp";
	}
	
	private void deleteInstalledApkFile(){
		File apkFile	= new File(_downloadPath);
		if(apkFile.exists()){
			SwitchLogger.d(LOG_TAG, "delete installed apk file: " + _downloadPath);
			apkFile.delete();
		}
	}
	
	private void upgrade(){
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
		dialog.show();
	}
	
	private class DownloadProgress {
		public long total;
		public long hasRead;
		
		public DownloadProgress(long total, long hasRead){
			this.total		= total;
			this.hasRead	= hasRead;
		}
	}
	
	private void download(){
		if(apkDownloaded()){
			SwitchLogger.d(LOG_TAG, _downloadPath + " already downloaded");
			install();
			return;
		}
		
		SwitchLogger.d(LOG_TAG, "try to download, path: " + _downloadPath);
		setProgress(0);
		show();
		
		new Thread(){
			public void run(){
				doDownload();
			}
		}.start();
	}
	
	private boolean apkDownloaded() {
		File apkFile	= new File(_downloadPath);
		if(apkFile.exists()){
			return true;
		} else {
			return false;
		}
	}

	private long getDownloadedSize(File tmpApkFile){
		long currentSize	= UpgradeUtil.getFileSize(tmpApkFile);
		if(currentSize > 0){
			/**
			 * 如果安装包临时文件已经存在并且已经下载完整，
			 * currentSize 会与文件大小相等，如果将Range的起始下载字节位置)设置为文件大小
			 * 将会导致服务端返回416错误--Requested Range Not Satisfiable错误
			 * 为了避免这种情况发生，对currentSize进行减1，保证Range的起始下载字节位置是正确的
			 */
			currentSize -= 1;
		}

		return currentSize;
	}
	
	private void doDownload(){
		try {
			File tmpApkFile		= new File(_tmpDownloadPath);
			long currentSize	= getDownloadedSize(tmpApkFile);

			SwitchLogger.d(LOG_TAG, _tmpDownloadPath + ", current size: " + currentSize);
			
			URL	url	= new URL(_param.getUpgradeApkUrl());
			HttpURLConnection httpUrlConnection	= (HttpURLConnection)url.openConnection();
			httpUrlConnection.setConnectTimeout(CONNECT_TIMEOUT);
			httpUrlConnection.setReadTimeout(READ_TIMEOUT);
			
			SwitchLogger.d(LOG_TAG, "connect time out="+httpUrlConnection.getConnectTimeout()
							+",read time out="+httpUrlConnection.getReadTimeout());
			
			httpUrlConnection.setRequestProperty("Range", "bytes=" + currentSize + "-");
			
			InputStream inputStream			= httpUrlConnection.getInputStream();
			RandomAccessFile outputStream	= new RandomAccessFile(tmpApkFile, "rw");
			outputStream.seek(currentSize);
			
			DownloadProgress downloadProgress = new DownloadProgress(currentSize + httpUrlConnection.getContentLength(), currentSize);
			SwitchLogger.d(LOG_TAG, _tmpDownloadPath + ", total size: " + downloadProgress.total);
			if( ! UpgradeUtil.spaceEnough(_downloadDir, downloadProgress.total)){
				_handler.obtainMessage(UpgradeManager.DOWNLOAD_ERR_SPACE_NOT_ENOUGH, downloadProgress).sendToTarget();
				inputStream.close();
				outputStream.close();
				return;
			}
			
			_handler.obtainMessage(UpgradeManager.DOWNLOAD_PROGRESS, downloadProgress).sendToTarget();
			
			byte[] buffer	= new byte[DOWNLOAD_BUFFER_SIZE];
			int len = 0;
			while((len = inputStream.read(buffer)) != -1){
				outputStream.write(buffer, 0, len);
				downloadProgress.hasRead	+= len;
				_handler.obtainMessage(UpgradeManager.DOWNLOAD_PROGRESS, downloadProgress).sendToTarget();
			}
			
			inputStream.close();
			outputStream.close();
			_handler.obtainMessage(UpgradeManager.DOWNLOAD_DONE).sendToTarget();
			
		} catch(MalformedURLException e){
			SwitchLogger.e(e);
			_handler.obtainMessage(UpgradeManager.DOWNLOAD_ERR_URL).sendToTarget();
			
		} catch(SocketTimeoutException e){
			SwitchLogger.d(LOG_TAG, "receive socket timeout exception occur");
			SwitchLogger.e(e);
			_handler.obtainMessage(UpgradeManager.DOWNLOAD_ERR_SOCKET_TIMEOUT).sendToTarget();
		
		} catch(IOException e){
			SwitchLogger.e(e);
			_handler.obtainMessage(UpgradeManager.DOWNLOAD_ERR_IO).sendToTarget();
		}
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
