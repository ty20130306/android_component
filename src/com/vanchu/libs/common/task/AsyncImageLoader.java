package com.vanchu.libs.common.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.vanchu.libs.common.util.SwitchLogger;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

/**
 * 异步加载图片
 */
public class AsyncImageLoader {

	private static final String LOG_TAG		= AsyncImageLoader.class.getSimpleName();
	
	private String _path; // 本地缓存路径

	private HashMap<String, SoftReference<Drawable>> _imageCachePool; // 内存对象缓存池

	public AsyncImageLoader(String path) {
		_path = path;
		_imageCachePool = new HashMap<String, SoftReference<Drawable>>();
	}

	/**
	 * 从缓存获取图片
	 * 
	 * @param imageUrl
	 * @param imageCallback
	 * @return
	 */
	public Drawable loadDrawable(final String imageUrl, final ImageCallback imageCallback) {
		if (imageUrl == null || "".equals(imageUrl)) {
			return null;
		}
		
		// 1、从内存加载图片
		if (_imageCachePool.containsKey(imageUrl)) {
			SoftReference<Drawable> softReference = _imageCachePool.get(imageUrl);
			Drawable drawable = softReference.get();
			if (drawable != null) {
				SwitchLogger.d(LOG_TAG, "get from cache pool");
				return drawable;
			}
		}
		
		// 2、从本地加载图片，并存入内存
		String imgName = getImgName(imageUrl);
		Bitmap bitmap = BitmapFactory.decodeFile(_path + imgName);
		if (bitmap != null) {
			Drawable drawable = new BitmapDrawable(bitmap);
			_imageCachePool.put(imageUrl, new SoftReference<Drawable>(drawable)); // 图片存入内存缓存
			SwitchLogger.d(LOG_TAG, "get from storage");
			return drawable;
		}
		
		// 3、从网络下载
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Drawable) message.obj, imageUrl);
				SwitchLogger.d(LOG_TAG, "get from network");
			}
		};
		new Thread() {
			@Override
			public void run() {
				Drawable drawable = loadImageFromUrl(imageUrl); // 网络加载图片
				Message message = handler.obtainMessage(0, drawable);
				handler.sendMessage(message);
				_imageCachePool.put(imageUrl, new SoftReference<Drawable>(drawable)); // 图片存入内存缓存
				String imgName = getImgName(imageUrl);
				saveImgFile(drawable, _path, imgName);// 图片存入本地
			}
		}.start();
		
		return null;
	}

	/**
	 * 网络下载图片
	 * 
	 * @param spec
	 * @return
	 */
	public static Drawable loadImageFromUrl(String spec) {
		URL url;
		InputStream input = null;
		
		try {
			url = new URL(spec);
			input = (InputStream) url.getContent();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Drawable d = Drawable.createFromStream(input, "src");
		return d;
	}

	/**
	 * 回调接口
	 */
	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, String imageUrl);
	}

	/**
	 * 保存图片文件
	 * 
	 * @param drawable
	 * @param path
	 * @param imgName
	 */
	private void saveImgFile(Drawable drawable, String path, String imgName) {
		File dirFile = new File(path);
		if ( ! dirFile.exists()) {
			dirFile.mkdir();
		}
		
		File imgFile = new File(dirFile, imgName);
		try {
			imgFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(imgFile);
			BitmapDrawable bd = (BitmapDrawable) drawable;
			Bitmap bm = bd.getBitmap();
			if (imgName.endsWith(".jpg")) {
				bm.compress(CompressFormat.JPEG, 50, fos);
			} else {
				bm.compress(CompressFormat.PNG, 50, fos);
			}
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获得文件名，以倒数第二个“/”截取
	 * 
	 * @param url
	 *            链接
	 * @return
	 */
	private String getImgName(String url) {
		if (url == null || "".equals(url)) {// 网址为空
			return "pic" + System.currentTimeMillis() + ".jpg";// 直接返回时间字符串
		}
		
		if (url.charAt(url.length() - 1) == '/') {// 若最后一位是“/”，去掉
			url = url.substring(0, url.length() - 1);
		}
		
		String imgName = "";
		String fileName = url.substring(url.lastIndexOf("/") + 1);// 取“/”最后一位
		if (url.lastIndexOf("/") == -1) {// 只有一个“/”
			imgName	= fileName;
		} else {
			url = url.substring(0, url.lastIndexOf("/"));
			String lastDirName = url.substring(url.lastIndexOf("/") + 1);// 取“/”倒数第二位
			imgName = lastDirName + "_" + fileName;
		}
		
		if (imgName.indexOf(".jpg") == -1 && imgName.indexOf(".png") == -1) {
			imgName += ".jpg";
		}
		
		return imgName;
	}
}
