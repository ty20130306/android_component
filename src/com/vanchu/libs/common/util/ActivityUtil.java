package com.vanchu.libs.common.util;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class ActivityUtil {
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

	public static void startApp(Context context, Class<?> launcherActivityClass){
		Intent	intent;
		
		if(isAppRuning(context)){
			intent = new Intent(context, launcherActivityClass);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} else {
			String packageName	= context.getPackageName();
			intent		= context.getPackageManager().getLaunchIntentForPackage(packageName);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			context.startActivity(intent);
		}
	}
}
