package com.vanchu.libs.common.util;

import com.vanchu.libs.common.task.AsyncImageLoader;
import com.vanchu.libs.common.task.AsyncImageLoader.ImageCallback;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * 图片工具类
 */
public class ImgUtil {
	
	private static String LOADED_IMG_DIR	= "loaded_img";
	private AsyncImageLoader _asyncImageLoader;
	private Activity _activity;
	private String 	_path;
	
	public ImgUtil(Activity activity) {
		_activity = activity;
		
		_path = _activity.getDir(LOADED_IMG_DIR, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE) + "/";
		_asyncImageLoader = new AsyncImageLoader(_path);
	}

	/**
	 * 异步设置图片 圆角图片
	 * 
	 * @param imageView
	 * @param url
	 * 
	 * @return 返回本地存储的图片位置
	 */
	public String asyncSetRoundImg(final ImageView imageView, String url, int defaultDrawable) {
		Drawable cachedImage = _asyncImageLoader.loadDrawable(url, new ImageCallback() {
			@Override
			public void imageLoaded(Drawable imageDrawable, String imageUrl) {
				if (imageDrawable != null) {
					imageView.setImageDrawable(BitmapUtil.toRoundCorner(imageDrawable, 5));
				}
			}
		});
		
		if (cachedImage != null) {
			imageView.setImageDrawable(BitmapUtil.toRoundCorner(cachedImage, 5));
		} else {
			imageView.setImageDrawable(BitmapUtil.toRoundCorner(_activity, defaultDrawable, 5));
		}
		
		return _path + _asyncImageLoader.getImgName(url);
	}

	/**
	 * 异步设置图片
	 * 
	 * @param imageView
	 * @param url
	 * 
	 * @return 返回本地存储的图片位置
	 */
	public String asyncSetImg(final ImageView imageView, String url, int defaultDrawable) {
		Drawable cachedImage = _asyncImageLoader.loadDrawable(url, new ImageCallback() {
			@Override
			public void imageLoaded(Drawable imageDrawable, String imageUrl) {
				if (imageDrawable != null) {
					imageView.setImageDrawable(imageDrawable);
				}
			}
		});
		
		if (cachedImage != null) {
			imageView.setImageDrawable(cachedImage);
		} else {
			imageView.setImageResource(defaultDrawable);
		}
		
		return _path + _asyncImageLoader.getImgName(url);
	}

	/**
	 * 异步设置图片(无默认图片)
	 * 
	 * @param imageView
	 * @param url
	 * 
	 * @return 返回本地存储的图片位置
	 */
	public String asyncSetImg(final ImageView imageView, String url) {
		Drawable cachedImage = _asyncImageLoader.loadDrawable(url, new ImageCallback() {
			@Override
			public void imageLoaded(Drawable imageDrawable, String imageUrl) {
				if (imageDrawable != null) {
					imageView.setImageDrawable(imageDrawable);
				}
			}
		});
		
		if (cachedImage != null) {
			imageView.setImageDrawable(cachedImage);
		}
		
		return _path + _asyncImageLoader.getImgName(url);
	}
}
