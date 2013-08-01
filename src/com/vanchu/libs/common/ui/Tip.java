package com.vanchu.libs.common.ui;

import android.content.Context;
import android.widget.Toast;

/**
 * 飘字提示工具类
 */
public class Tip {

	/**
	 * 飘字提示
	 * 
	 * @param context
	 * @param msg
	 */
	public static void show(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 飘字提示
	 * 
	 * @param context
	 * @param resId
	 */
	public static void show(Context context, int resId) {
		Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
	}

}
