package com.vanchu.libs.common.task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.vanchu.libs.common.util.FileUtil;
import com.vanchu.libs.common.util.StringUtil;
import com.vanchu.libs.common.util.SwitchLogger;

public class Downloader {
	
	public static final int DOWNLOAD_SUCC					= 0;
	public static final int DOWNLOAD_PROGRESS				= 1;
	public static final int DOWNLOAD_ERR_IO					= 2;
	public static final int DOWNLOAD_ERR_URL				= 3;
	public static final int DOWNLOAD_ERR_SPACE_NOT_ENOUGH	= 4;
	public static final int DOWNLOAD_ERR_SOCKET_TIMEOUT		= 5;
	
	private static final String DOWNLOAD_ROOT_DIR_NAME	= "vanchu_download";
	
	private static final String LOG_TAG	= Downloader.class.getSimpleName();
	
	private static final int DOWNLOAD_STORAGE_TYPE_SDCARD		= 1;
	private static final int DOWNLOAD_STORAGE_TYPE_DEVICE_MEM	= 2;
	
	private static final int CONNECT_TIMEOUT		= 10000; // millisecond
	private static final int READ_TIMEOUT			= 10000; // millisecond
	
	private static final int DOWNLOAD_BUFFER_SIZE		= 2048;
	private static final int TIMEOUT_RETRY_MAX			= 3;

	private Context	_context;
	private String	_downloadUrl;
	private String	_dirName;
	private IDownloadListener	_downloadListener;
	
	private String	_downloadFileName;
	private int		_downloadStorageType;
	private String	_downloadDir;
	private String	_downloadPath;
	private String	_tmpDownloadPath;
	
	private int 	_timeoutRetryCnt;
	
	private Handler	_handler = new Handler(){
		
		@Override
		public void handleMessage(Message msg){

			switch (msg.what) {
			case DOWNLOAD_PROGRESS:
				publishProgress(msg);
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
				
			case DOWNLOAD_SUCC:
				downloadSucc();
				break;
			}
		}
	};	
	
	public Downloader(Context context, String downloadUrl, String dirName, IDownloadListener downloadListener){
		_context			= context;
		_downloadUrl		= downloadUrl;
		_dirName			= dirName;
		_downloadListener	= downloadListener;
		
		_timeoutRetryCnt	= 0;
		
		splitFileNameFromUrl();
		initDownloadStorage(false);
	}
	
	public interface IDownloadListener {
		
		public void onStart();
		
		public void onProgress(long downloaded, long total);
		
		public void onSuccess(String downloadFile);
		
		public void onError(int errCode);
		
	}
	
	private void publishProgress(Message msg){
		DownloadProgress progress = (DownloadProgress)msg.obj;
		long downloaded	= progress.hasRead;
		long total		= progress.total;
		_downloadListener.onProgress(downloaded, total);
	}
	
	private void downloadSucc(){
		SwitchLogger.d(LOG_TAG, "download complete, rename tmp to official");
		FileUtil.rename(_tmpDownloadPath, _downloadPath);
		FileUtil.chmod(_downloadPath, "777");
		_downloadListener.onSuccess(_downloadPath);
	}
	
	private void handleUrlError(){
		_downloadListener.onError(DOWNLOAD_ERR_URL);
	}
	
	private void handleIoError(){
		if(_downloadStorageType == DOWNLOAD_STORAGE_TYPE_SDCARD){
			SwitchLogger.d(LOG_TAG, "download to sdcard fail, try device mem");
			initDownloadStorage(true);
			download();
		} else {
			_downloadListener.onError(DOWNLOAD_ERR_IO);
		}
	}
	
	private void handleSocketTimeout(){
		if(_timeoutRetryCnt < TIMEOUT_RETRY_MAX){
			_timeoutRetryCnt++;
			SwitchLogger.d(LOG_TAG, "socket timeout, retry it, timeout retry count now: " + _timeoutRetryCnt);
			download();
		} else {
			_downloadListener.onError(DOWNLOAD_ERR_SPACE_NOT_ENOUGH);
		}
	}
	
	private void handleSpaceNotEnough(Message msg){
		if(_downloadStorageType == DOWNLOAD_STORAGE_TYPE_SDCARD){
			SwitchLogger.d(LOG_TAG, "sdcard space not enough, try device mem");
			initDownloadStorage(true);
			download();
		} else {
			DownloadProgress progress	= (DownloadProgress)msg.obj;
			_downloadListener.onError(DOWNLOAD_ERR_SPACE_NOT_ENOUGH);
			SwitchLogger.d(LOG_TAG, "device mem space not enough, need " + progress.total);
		}
	}
	
	private void initDownloadStorage(boolean useDeviceMem){
		File file	= null;
		
		if( ! useDeviceMem && FileUtil.isSDCardReady()){
			_downloadStorageType	= DOWNLOAD_STORAGE_TYPE_SDCARD;
			file			= Environment.getExternalStorageDirectory();
		} else {
			_downloadStorageType	= DOWNLOAD_STORAGE_TYPE_DEVICE_MEM;
			file			= _context.getFilesDir();
		}
		
		_downloadDir	= file.getAbsolutePath() + "/" + DOWNLOAD_ROOT_DIR_NAME;
		if(_dirName != null && _dirName != "") {
			_downloadDir	+= "/" + _dirName;
		}
		
		File dir		= new File(_downloadDir);
		if( ! dir.exists()) {
			dir.mkdirs();
		}
		FileUtil.chmod(_downloadDir, "777");
		
		_downloadPath		= _downloadDir + "/" + _downloadFileName;
		_tmpDownloadPath	= _downloadPath + ".tmp";
		
		SwitchLogger.d(LOG_TAG, "url:" + _downloadUrl + ", file name:" + _downloadFileName
								+ ", path:" + _downloadPath + ", tmp path:" + _tmpDownloadPath);
	}
	
	private void splitFileNameFromUrl(){
		String[] urlElements	= _downloadUrl.split(File.separator);
		
		_downloadFileName	= urlElements[urlElements.length - 1];
		
		if(_downloadFileName.length() == 0){
			String currentDateStr	= StringUtil.currentDateToString("yyyyMMDD_HHmmss");
			_downloadFileName	= "downloader_" + currentDateStr;
		}
	}
	
	public void run(){
		_downloadListener.onStart();
		download();
	}
	
	private void download(){
		if(fileDownloaded()){
			SwitchLogger.d(LOG_TAG, _downloadPath + " already downloaded");
			downloadSucc();
			return;
		}

		new Thread(){
			public void run(){
				doDownload();
			}
		}.start();
	}
	
	private boolean fileDownloaded() {
		File file	= new File(_downloadPath);
		if(file.exists()){
			return true;
		} else {
			return false;
		}
	}
	
	private void doDownload(){
		try {
			File tmpFile		= new File(_tmpDownloadPath);
			long currentSize	= getDownloadedSize(tmpFile);

			SwitchLogger.d(LOG_TAG, _tmpDownloadPath + ", current size: " + currentSize);
			
			URL	url	= new URL(_downloadUrl);
			HttpURLConnection httpUrlConnection	= (HttpURLConnection)url.openConnection();
			httpUrlConnection.setConnectTimeout(CONNECT_TIMEOUT);
			httpUrlConnection.setReadTimeout(READ_TIMEOUT);
			
			SwitchLogger.d(LOG_TAG, "connect time out=" + httpUrlConnection.getConnectTimeout());
			SwitchLogger.d(LOG_TAG, "read time out=" + httpUrlConnection.getReadTimeout());
			
			httpUrlConnection.setRequestProperty("Range", "bytes=" + currentSize + "-");
			
			InputStream inputStream			= httpUrlConnection.getInputStream();
			RandomAccessFile outputStream	= new RandomAccessFile(tmpFile, "rw");
			outputStream.seek(currentSize);
			
			DownloadProgress downloadProgress = new DownloadProgress(currentSize + httpUrlConnection.getContentLength(), currentSize);
			SwitchLogger.d(LOG_TAG, _tmpDownloadPath + ", total size: " + downloadProgress.total);
			if( ! FileUtil.spaceEnough(_downloadDir, downloadProgress.total)){
				_handler.obtainMessage(DOWNLOAD_ERR_SPACE_NOT_ENOUGH, downloadProgress).sendToTarget();
				inputStream.close();
				outputStream.close();
				return;
			}
			
			_handler.obtainMessage(DOWNLOAD_PROGRESS, downloadProgress).sendToTarget();
			
			byte[] buffer	= new byte[DOWNLOAD_BUFFER_SIZE];
			int len = 0;
			while((len = inputStream.read(buffer)) != -1){
				outputStream.write(buffer, 0, len);
				downloadProgress.hasRead	+= len;
				_handler.obtainMessage(DOWNLOAD_PROGRESS, downloadProgress).sendToTarget();
			}
			
			inputStream.close();
			outputStream.close();
			_handler.obtainMessage(DOWNLOAD_SUCC).sendToTarget();
			
		} catch(MalformedURLException e){
			SwitchLogger.e(e);
			_handler.obtainMessage(DOWNLOAD_ERR_URL).sendToTarget();
			
		} catch(SocketTimeoutException e){
			SwitchLogger.d(LOG_TAG, "receive socket timeout exception occur");
			SwitchLogger.e(e);
			_handler.obtainMessage(DOWNLOAD_ERR_SOCKET_TIMEOUT).sendToTarget();
		
		} catch(IOException e){
			SwitchLogger.e(e);
			_handler.obtainMessage(DOWNLOAD_ERR_IO).sendToTarget();
		}
	}
	
	private long getDownloadedSize(File tmpFile){
		long currentSize	= FileUtil.getFileSize(tmpFile);
		if(currentSize > 0){
			/**
			 * 如果临时文件已经存在并且已经下载完整，
			 * currentSize 会与文件大小相等，如果将Range的起始下载字节位置)设置为文件大小
			 * 将会导致服务端返回416错误--Requested Range Not Satisfiable错误
			 * 为了避免这种情况发生，对currentSize进行减1，保证Range的起始下载字节位置是正确的
			 */
			currentSize -= 1;
		}

		return currentSize;
	}
	
	private class DownloadProgress {
		public long total;
		public long hasRead;
		
		public DownloadProgress(long total, long hasRead){
			this.total		= total;
			this.hasRead	= hasRead;
		}
	}
}
