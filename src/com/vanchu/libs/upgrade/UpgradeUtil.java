package com.vanchu.libs.upgrade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.vanchu.libs.common.SwitchLogger;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;

public class UpgradeUtil {
	private static final String LOG_TAG = UpgradeUtil.class.getSimpleName();
	
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
	
	public static boolean rename(String oldPath, String newPath){
		File tmpApkFile		= new File(oldPath);
		boolean ret			= tmpApkFile.renameTo(new File(newPath));
		
		if(ret == true){
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean chmod(String path, String mode){
		String cmd	= "chmod " + mode + " " + path;
		return executeCmd(cmd);
	}
	
	public static boolean rm(String path, String option){
		String cmd	= "rm " + option + " " + path;
		return executeCmd(cmd);
	}
	
	public static boolean isSDCardReady(){
		String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	    	SwitchLogger.d(LOG_TAG, "SDCard ready");
	    	return true;
	    	
	    } else {
	    	SwitchLogger.d(LOG_TAG, "SDCard not ready");
	    	return false;
	    }
	}
	
	public static boolean spaceEnough(String dirPath, long need){
		StatFs sf				= new StatFs(dirPath); 
		int blockSize			= sf.getBlockSize(); 
		int availableBlocks		= sf.getAvailableBlocks();
		
		/**
		 * 注意：availableBlocks 与 blockSize相乘之前一定要进行double转换，
		 * 即在前面加强制转换符(double)，否则计算结果会溢出，导致最后乘积变为负数，
		 * 即使计算结果的存储为double类型也会这样
		 */
		double availableSpace	= (double)availableBlocks * (double)blockSize;
		
		SwitchLogger.d(LOG_TAG, dirPath + ", block size: " + blockSize + ", available blocks:" + availableBlocks);
		SwitchLogger.d(LOG_TAG, dirPath + ", available space: " + (availableSpace  / (1024 * 1024)) + " M");
		
		if(availableSpace > need){
			return true;
		} else {
			return false;
		}
	}
	
	public static long getFileSize(String path){
		File file	= new File(path);
		return UpgradeUtil.getFileSize(file);
	}
	
	public static long getFileSize(File file){
		long size	= 0;
		
		try {
			if(file.exists()){
				FileInputStream fis	= new FileInputStream(file);
				size	= fis.available();
				fis.close();
			}
		} catch (IOException e){
			SwitchLogger.e(e);
			size	= 0;
		}
		
		return size;
	}
	
	public static void sleep(long millis){
		try{
			Thread.sleep(millis);
		} catch (InterruptedException e){
			SwitchLogger.e(e);
		}
	}
	
	public static boolean copy(String srcPath, String dstPath){
		try{
			FileInputStream srcFile		= new FileInputStream(srcPath);
			FileOutputStream dstFile	= new FileOutputStream(dstPath);
			
			byte buffer[] = new byte[4096];
			int len;
			while ((len = srcFile.read(buffer)) != -1) {
				dstFile.write(buffer, 0, len);
			}
			
			srcFile.close();
			dstFile.close();
			return true;
			
		} catch(IOException e){
			SwitchLogger.e(e);
			return false;
		}
	}
	
	private static boolean executeCmd(String cmd){
		try {
			Runtime.getRuntime().exec(cmd);
			SwitchLogger.d(LOG_TAG, "execute cmd " + cmd + " succ");
			return true;
			
		} catch (IOException e){
			SwitchLogger.e(e);
			return false;
		}
	}
	
}
