package com.vanchu.libs.common.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

public class CustomUEH implements UncaughtExceptionHandler {
	
	public static CustomUEH	_instance		= null;
	
	private static final String	LOG_TAG		= CustomUEH.class.getSimpleName();
	private static final String	DIR_NAME	= "crash";
	
	private Map<String, String> _exceptionInfo	= new HashMap<String, String>();	 
			
	private UncaughtExceptionHandler	_defaultUEH		= null;
	private Context	_context	= null;
	private String	_packageName	= "vanchu";
	
	public static CustomUEH instance() {
		if(null == _instance) {
			_instance	= new CustomUEH();
			_instance.initDefaultUEH();
		}
		
		return _instance;
	}
	
	private void initDefaultUEH() {
		_defaultUEH	= Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	
	public void init(Context context) {
		SwitchLogger.d(LOG_TAG, "init CustomUEH");
		
		_context		= context;
		if(null != _context) {
			_packageName	= _context.getPackageName();
		}
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		SwitchLogger.d(LOG_TAG, "uncaught exception occurred");
		
		if(null != throwable) {
			SwitchLogger.d(LOG_TAG, "save exception info to file");
			_exceptionInfo.clear();
			saveExceptionInfo(throwable);
			
		}
		
		if(null != _defaultUEH) {
			SwitchLogger.d(LOG_TAG, "default UEH process");
			_defaultUEH.uncaughtException(thread, throwable);
		}
	}
	
	private void saveExceptionInfo(Throwable throwable) {
		_exceptionInfo.clear();
		collectAppInfo();
		collectDeviceInfo();
		String exceptiongInfo = getExceptionInfo(throwable);
		
		saveToFile(exceptiongInfo);
	}
	
	private void collectAppInfo() {
		if(null != _context) {
			try {
				PackageManager pm = _context.getPackageManager();
				String packageName	= _context.getPackageName();
				_exceptionInfo.put("packageName", packageName);
				PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
				if (null != pi) {
					String versionName = null == pi.versionName ? "null" : pi.versionName;
					_exceptionInfo.put("versionName", versionName);
				}
			} catch (Exception e) {
				SwitchLogger.e(e);
			}
		}
	}
	
	private void collectDeviceInfo() {
		_exceptionInfo.put("phone_model", Build.MODEL);
		_exceptionInfo.put("system_version", Build.VERSION.RELEASE);
	}
	
	private String getExceptionInfo(Throwable throwable) {
		StringBuffer buffer = new StringBuffer();
		for (Map.Entry<String, String> entry : _exceptionInfo.entrySet()) {
			buffer.append(entry.getKey() + " = " + entry.getValue() + "\n");
		}
		
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		throwable.printStackTrace(printWriter);
		Throwable cause = throwable.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		
		String result = writer.toString();
		buffer.append(result);
		
		return buffer.toString();
	}
	
	private void saveToFile(String exceptionInfo) {
		try {
			if( ! Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
	           return ;
	        }
			File dir = new File(Environment.getExternalStorageDirectory(), "data/"+_packageName+"/"+DIR_NAME);
			if( ! dir.exists()) {
				dir.mkdirs();
			}
			String filePath	= dir.getAbsolutePath();

			long now	= System.currentTimeMillis();
			String date	= new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(now));
			String fileName = "crash-" + date + ".txt";
			
			FileOutputStream fos = new FileOutputStream(filePath + File.separator + fileName);
			fos.write(exceptionInfo.getBytes());
			
			fos.flush();
			fos.close();
		} catch (Exception e) {
			SwitchLogger.e(e);
		}
	}
}
