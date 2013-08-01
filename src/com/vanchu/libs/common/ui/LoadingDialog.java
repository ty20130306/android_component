package com.vanchu.libs.common.ui;


import android.app.ProgressDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;

/**
 * 加载提示框
 */
public class LoadingDialog {

	private static boolean _isStart = false; // 标记提示框是否启动
	private static ProgressDialog _pDialog = null; // 声明进度条对话框

	/**
	 * 创建提示框
	 * 
	 * @param context 上下文环境
	 */
	public static void create(Context context) {
		cancel(); // 停止当前提示框
		_pDialog = new ProgressDialog(context); // 实例化进度条对话框
		_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条风格（圆形、旋转）
		_pDialog.setMessage("loading...");// 设置内容
		_pDialog.setIndeterminate(false);// 设置为不明确进度条
		_pDialog.setCancelable(false);// 设置不可以按退回键取消提示框
		_pDialog.show(); // 启动对话框
		_isStart = true; // 启动标记设置为true
	}
	
	/**
	 * 创建提示框
	 * 
	 * @param context 上下文环境
	 * @param msg 提示框内容
	 */
	public static void create(Context context, String msg) {
		cancel(); // 停止当前提示框
		_pDialog = new ProgressDialog(context); // 实例化进度条对话框
		_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条风格（圆形、旋转）
		_pDialog.setMessage(msg);// 设置内容
		_pDialog.setIndeterminate(false);// 设置为不明确进度条
		_pDialog.setCancelable(false);// 设置不可以按退回键取消提示框
		_pDialog.show(); // 启动对话框
		_isStart = true; // 启动标记设置为true
	}

	/**
	 * 创建提示框
	 * 
	 * @param context 上下文环境
	 * @param msgResId 提示框内容资源ID
	 */
	public static void create(Context context, int msgResId) {
		cancel(); // 停止当前提示框
		_pDialog = new ProgressDialog(context); // 实例化进度条对话框
		_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条风格（圆形、旋转）
		_pDialog.setMessage(context.getString(msgResId));// 设置内容
		_pDialog.setIndeterminate(false);// 设置为不明确进度条
		_pDialog.setCancelable(false);// 设置不可以按退回键取消提示框
		_pDialog.setCanceledOnTouchOutside(false); // 设置点击提示框外边不可以取消提示框
		_pDialog.show(); // 启动对话框
		_isStart = true; // 启动标记设置为true
	}

	/**
	 * 创建提示框
	 * 
	 * @param context 上下文环境
	 * @param animResId 窗口弹出和消失资源动画id
	 */
	public static void createWithAnim(Context context, int animResId) {
		cancel(); // 停止当前提示框
		_pDialog = new ProgressDialog(context); // 实例化进度条对话框
		_pDialog.setMessage("");
		_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条风格（圆形、旋转）
		_pDialog.setIndeterminate(false);// 设置为不明确进度条
		_pDialog.setCancelable(false);// 设置不可以按退回键取消提示框
		Window window = _pDialog.getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		window.setGravity(Gravity.CENTER); // 对话框显示方位
		window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 背景变暗
		window.setWindowAnimations(animResId); // 出现和消失动画
		params.x = 0; // 基于显示方位的水平偏移量
		params.y = 0; // 基于显示方位的垂直偏移量
		params.width = LayoutParams.WRAP_CONTENT; // 水平填充效果
		params.height = LayoutParams.WRAP_CONTENT; // 垂直填充效果
		params.dimAmount = 0.0f; // 背景黑暗度
		window.setAttributes(params);
		_pDialog.show(); // 启动对话框
		_isStart = true; // 启动标记设置为true
	}

	/**
	 * 取消提示框
	 */
	public static void cancel() {
		if (_isStart) {
			_isStart = false;
			_pDialog.cancel();
		}
	}
}
