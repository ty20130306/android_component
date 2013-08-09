package com.vanchu.libs.common.util;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

/**
 * @author wolf
 *
 */
public class ActivityUtil {
	
	private static final int	NO_FLAG	= 0;
	
	private static final String LOG_TAG	= ActivityUtil.class.getSimpleName();
	
	public static String getCurrentVersionName(Context context) {
		String currentVersionName = "";
		
		try {
			PackageManager pm	= context.getPackageManager();
			PackageInfo pi		= pm.getPackageInfo(context.getPackageName(), 0);
			currentVersionName	= pi.versionName;
			if (currentVersionName == null || currentVersionName.length() <= 0) {
				currentVersionName	= "1.0.0";
			}
		} catch (Exception e) {
			currentVersionName	= "1.0.0";
		}
		
		return currentVersionName;
	}
	
	public static void uninstallApp(Context context, String packageName){
		Uri uri	= Uri.parse("package:"+packageName);
		Intent uninstallIntent	= new Intent(Intent.ACTION_DELETE, uri);
		context.startActivity(uninstallIntent);
	}
	
	public static boolean isAppInstalled(Context context, String packageName){
		if(packageName == null || packageName.equals("")){
			return false;
		}
		try {
			PackageManager	pm	= context.getPackageManager();
			PackageInfo		pi	= pm.getPackageInfo(packageName, NO_FLAG);
			if(pi == null){
				return false;
			}
		} catch ( NameNotFoundException e) {
			return false;
		}
		
		return true;
	}
	
	public static boolean isAppRuning(Context context) {
		boolean isRunning = false;
		
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> list = am.getRunningTasks(100);
		if (list.size() <= 0) {
			return false;
		}
			
		String packageName = context.getPackageName();
		for (RunningTaskInfo info : list) {
			if (info.topActivity.getPackageName().equals(packageName) 
				|| info.baseActivity.getPackageName().equals(packageName)) 
			{
				isRunning = true;
				break;
			}
		}
		
		return isRunning;
	}

	
	/**
	 * 用于启动自身app
	 * @param context
	 * @param launcherActivityClass
	 * @return succ: true, fail: false
	 */
	public static boolean startApp(Context context, Class<?> launcherActivityClass){
		try {
			Intent	intent;
			
			if(isAppRuning(context)){
				intent = new Intent(context, launcherActivityClass);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			} else {
				String packageName	= context.getPackageName();
				intent		= context.getPackageManager().getLaunchIntentForPackage(packageName);
				if(intent != null){
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setAction(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					context.startActivity(intent);
				} else {
					SwitchLogger.e(LOG_TAG, "can not find launch intent for package " + packageName);
					return false;
				}
			}
		} catch (ActivityNotFoundException e){
			SwitchLogger.e(e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 用于启动隐藏icon或启动指定activity的app
	 * @param context
	 * @param packageName
	 * @param className
	 * @return succ: true, fail: false
	 */
	public static boolean startApp(Context context, String packageName, String className){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setClassName(packageName, className);
		
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			SwitchLogger.e(e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 启动正常的app
	 * @param context
	 * @param packageName
	 * @return succ: true, fail: false
	 */
	public static boolean startApp(Context context, String packageName){
		try {
			Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
			if(intent != null){
		        intent.setAction(Intent.ACTION_MAIN);
		        intent.addCategory(Intent.CATEGORY_LAUNCHER);
	        	context.startActivity(intent);
			} else {
				SwitchLogger.e(LOG_TAG, "can not find launch intent for package " + packageName);
				return false;
			}
        } catch(ActivityNotFoundException e){
        	SwitchLogger.e(e);
        	return false;
        }
		
		return true;
	}
}
