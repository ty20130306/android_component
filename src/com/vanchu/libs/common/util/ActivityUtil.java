package com.vanchu.libs.common.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;

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
	
	/**
	 * 判断app是否在运行（包括后台运行和前台运行）
	 * @param context
	 * @return 
	 */
	public static boolean isAppRunning(Context context) {
		boolean isRunning = false;
		
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> list = am.getRunningTasks(100);
		if (list.size() <= 0) {
			return false;
		}
			
		String packageName = context.getPackageName();
		for (RunningTaskInfo info : list) {
			if (info.topActivity.getPackageName().equals(packageName)) {
				isRunning = true;
				break;
			}
		}
		
		return isRunning;
	}
	
	/**
	 * 判断app是否在前台运行
	 * @param context
	 * @return
	 */
	public static boolean isAppRunningTop(Context context) {
		
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> list = am.getRunningTasks(1);
		if (list.size() > 0) {
			String packageName = context.getPackageName();
			RunningTaskInfo topRunningTaskinfo	= list.get(0);
			if (topRunningTaskinfo.topActivity.getPackageName().equals(packageName)) {
				return true;
			}
		}

		return false;
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
			
			if(isAppRunning(context)){
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
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
	 * 用于启动隐藏icon或启动指定activity的app
	 * @param context
	 * @param packageName
	 * @param className
	 * @param intent data
	 * @return succ: true, fail: false
	 */
	public static boolean startApp(Context context, String packageName, String className, Map<String, String> data){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(packageName, className);
		
		if(data != null) {
			Iterator<Entry<String, String>> iter	= data.entrySet().iterator();
			while(iter.hasNext()) {
				Entry<String, String> entry	= iter.next();
				intent.putExtra(entry.getKey(), entry.getValue());
			}
		}
		
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
	
	public static void restartSelf() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	public static Object getMetaValue(Context context, String metaKey) {
		Bundle metaData		= null;
        Object metaValue	= null;
        if (context == null || metaKey == null) {
        	SwitchLogger.e(LOG_TAG, "context or metaKey is null");
            return null;
        }
        
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    				context.getPackageName(), PackageManager.GET_META_DATA);
            
            if (null == ai) {
            	SwitchLogger.e(LOG_TAG, "ai is null");
            	return null;
            }
            metaData = ai.metaData;
            
            if (null == metaData) {
            	SwitchLogger.e(LOG_TAG, "ai.metaData is null");
            	return null;
            }
            
            metaValue = metaData.get(metaKey);
        } catch (NameNotFoundException e) {
        	SwitchLogger.e(e);
        }
        
        return metaValue;
    }
}
