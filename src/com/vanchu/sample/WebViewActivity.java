package com.vanchu.sample;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanchu.libs.common.ui.DialogFactory;
import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.FileUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.test.R;
import com.vanchu.test.R.layout;
import com.vanchu.test.R.menu;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.RenderPriority;
import android.widget.ImageView;

public class WebViewActivity extends Activity {

	private static final String LOG_TAG	= WebViewActivity.class.getSimpleName();
	private static final int SOURCE_TYPE_SDCARD		= 1;
	private static final int SOURCE_TYPE_APP_DIR	= 2;
	
	private ImageView	_imageView;
	private WebView		_webView;
	private JavascriptConnector _javascriptConnector;
	private int _sourceType;
	
	private String _sdcardFileName;
	private String _appFileName;
	
	private Handler	_handler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_view);
		
		_imageView	= (ImageView)findViewById(R.id.pic);
		_imageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				showWebView();
			}
		});
		_javascriptConnector	= new JavascriptConnector();
		
		saveFile(null);
		readSdcardFile(null);
		//readAppFile(null);
		
	}
	
	public class JavascriptConnector {
		/**
		 * 看图返回接口
		 * @param msg
		 */
		public void loadImg(){
			if(_sourceType == SOURCE_TYPE_SDCARD) {
				_handler.post(new Runnable() {
					
					@Override
					public void run() {
						_webView.loadUrl("javascript:App.setImg('"+_sdcardFileName+"')");//javascript
						SwitchLogger.d(LOG_TAG, "javascript call loadImg, SOURCE_TYPE_SDCARD, file="+_sdcardFileName );
					}
				});
				
			} else {
				_handler.post(new Runnable() {
					
					@Override
					public void run() {
						_webView.loadUrl("javascript:App.setImg('"+_appFileName+"')");//javascript
						SwitchLogger.d(LOG_TAG, "javascript call loadImg, SOURCE_TYPE_APP_DIR, file="+_appFileName );
					}
				});
				
			}
		}
	}

	public void readSdcardFile(View v) {
		_sourceType	= SOURCE_TYPE_SDCARD;
		showDialog();
	}

	private void showDialog() {
		View view	= getLayoutInflater().inflate(R.layout.dialog_web_view, null);
		_webView	= (WebView)view.findViewById(R.id.web_view);
		initWebview();
		Dialog dialog	= DialogFactory.createCenterDialog(this, view, R.style.customDialog, LayoutParams.MATCH_PARENT, 1.0f, true);
		dialog.show();
	}

	public void readAppFile(View v) {
		_sourceType	= SOURCE_TYPE_APP_DIR;
		showDialog();
	}

	private void saveFileToSdcard() {
		if( ! FileUtil.isSDCardReady()){
			Tip.show(this, "sdcard 不可用");
			return ;
		}
		File dir		= null;
		
		File storageDir			= Environment.getExternalStorageDirectory();
		String storageDirName	= storageDir.getAbsolutePath() + "/a_test_web_view";
		SwitchLogger.d(LOG_TAG, "storageDirName="+storageDirName);
		dir	= new File(storageDirName);
		if( ! dir.exists()) {
			dir.mkdir();
		}
		FileUtil.chmod(storageDirName, "777");
		
		AssetManager am	= getAssets();
		try {
			writeFile(am.open("beauty.jpg"), storageDirName+"/beauty.jpg");
			writeFile(am.open("girl.jpg"), storageDirName+"/girl.jpg");
		} catch (Exception e) {
			SwitchLogger.e(e);
		}
		
		_sdcardFileName	= storageDirName+"/girl.jpg";
		FileUtil.chmod(_sdcardFileName, "777");
	}
	
	private void saveToAppDir() {
		File dir		= null;
		File appDir			= getFilesDir();
		String appDirName	= appDir.getAbsolutePath() + "/a_test_web_view";
		SwitchLogger.d(LOG_TAG, "appDirName="+appDirName);
		dir	= new File(appDirName);
		if( ! dir.exists()) {
			dir.mkdir();
		}
		FileUtil.chmod(appDirName, "777");
		
		AssetManager am	= getAssets();
		try {
			writeFile(am.open("beauty.jpg"), appDirName+"/beauty.jpg");
			writeFile(am.open("girl.jpg"), appDirName+"/girl.jpg");
		} catch (Exception e) {
			SwitchLogger.e(e);
		}
		
		_appFileName	= appDirName+"/girl.jpg";
		FileUtil.chmod(_appFileName, "777");
	}
	
	public void saveFile(View v) {
		saveFileToSdcard();
		saveToAppDir();
	}
	
	private void deleteFromSdcard(){
		if( ! FileUtil.isSDCardReady()){
			Tip.show(this, "sdcard 不可用");
			return ;
		}
		
		File storageDir			= Environment.getExternalStorageDirectory();
		String storageDirName	= storageDir.getAbsolutePath() + "/a_test_web_view";
		File file	= new File(storageDirName + "/beauty.jpg");
		if(file.exists()) {
			file.delete();
			SwitchLogger.d(LOG_TAG, "delete file " + storageDirName + "/beauty.jpg succ");
		} else {
			SwitchLogger.d(LOG_TAG, "file " + storageDirName + "/beauty.jpg not exist");
		}
		
		file	= new File(storageDirName + "/girl.jpg");
		if(file.exists()) {
			file.delete();
			SwitchLogger.d(LOG_TAG, "delete file " + storageDirName + "/girl.jpg succ");
		} else {
			SwitchLogger.d(LOG_TAG, "file " + storageDirName + "/girl.jpg not exist");
		}
	}
	
	private void deleteFromAppDir() {
		File appDir			= getFilesDir();
		String appDirName	= appDir.getAbsolutePath() + "/a_test_web_view";
		File file	= new File(appDirName + "/beauty.jpg");
		if(file.exists()) {
			file.delete();
			SwitchLogger.d(LOG_TAG, "delete file " + appDirName + "/beauty.jpg succ");
		} else {
			SwitchLogger.d(LOG_TAG, "file " + appDirName + "/beauty.jpg not exist");
		}
		
		file	= new File(appDirName + "/girl.jpg");
		if(file.exists()) {
			file.delete();
			SwitchLogger.d(LOG_TAG, "delete file " + appDirName + "/girl.jpg succ");
		} else {
			SwitchLogger.d(LOG_TAG, "file " + appDirName + "/girl.jpg not exist");
		}
	}
	
	public void deleteFile(View v) {
		deleteFromSdcard();
		deleteFromAppDir();
	}
 
	private void writeFile(InputStream is, String fileName) {
		try {
			OutputStream os = new BufferedOutputStream(new FileOutputStream(fileName));
			
	        byte[] buffer = new byte[8192];
	        int byteread;
	        while ((byteread = is.read(buffer)) != -1) {
	            os.write(buffer, 0, byteread);
	        }
	        is.close();
	        os.close();
		} catch (Exception e) {
			SwitchLogger.e(e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_view, menu);
		return true;
	}
	
	private void showWebView() {
		View view	= getLayoutInflater().inflate(R.layout.dialog_web_view, null);
		_webView	= (WebView)view.findViewById(R.id.web_view);
		initWebview();
		Dialog dialog	= DialogFactory.createCenterDialog(this, view, R.style.customDialog, LayoutParams.MATCH_PARENT, 1.0f, true);
		dialog.show();
	}
	
	private void initWebview(){
		_webView.setScrollBarStyle(0);
		final WebSettings settings = _webView.getSettings();
		
		settings.setRenderPriority(RenderPriority.HIGH);
		
		// WebView启用javascript脚本执行
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		// 启用数据库
		settings.setDatabaseEnabled(true);
		String dir = this.getApplicationContext()
				.getDir("database", Context.MODE_PRIVATE).getPath();
		// 设置数据库路径
		settings.setDatabasePath(dir);
		// 使用localStorage则必须打开
		settings.setDomStorageEnabled(true);
		// 设置允许访问文件数据
		settings.setAllowFileAccess(true);
		int screenDensity = getResources().getDisplayMetrics().densityDpi;
		WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.MEDIUM;
		switch (screenDensity) {
		case DisplayMetrics.DENSITY_LOW:
			zoomDensity = WebSettings.ZoomDensity.CLOSE;
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			zoomDensity = WebSettings.ZoomDensity.MEDIUM;
			break;
		case DisplayMetrics.DENSITY_HIGH:
			zoomDensity = WebSettings.ZoomDensity.FAR;
			break;
		}
		settings.setDefaultZoom(zoomDensity);
		_webView.addJavascriptInterface(_javascriptConnector, "Shell");
		initWebViewClient();
		webViewLoadingData();
	}
	
	public void webViewLoadingData(){
		_webView.loadUrl("file:///android_asset/t.html");
	}
	
	
	private void initWebViewClient(){
		_webView.setWebChromeClient(new WebChromeClient(){
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
			}
		});
		
		_webView.setWebViewClient(new WebViewClient(){
			@Override
			public void onLoadResource(WebView view, String url) {
				super.onLoadResource(view, url);
			}
		});
	}
}
