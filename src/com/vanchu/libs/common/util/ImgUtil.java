package com.vanchu.libs.common.util;

import java.io.File;
import java.io.FileInputStream;

import com.vanchu.libs.common.task.AsyncImageLoader;
import com.vanchu.libs.common.task.AsyncImageLoader.ImageCallback;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * 图片工具类
 */
public class ImgUtil {
	
	private static final String LOG_TAG	= ImgUtil.class.getSimpleName();
	
	public static final String IMG_TYPE_PNG		= "PNG";
	public static final String IMG_TYPE_JPG		= "JPG";
	public static final String IMG_TYPE_GIF		= "GIF";
	
	private static String LOADED_IMG_DIR	= "loaded_img";
	private AsyncImageLoader _asyncImageLoader;
	private Context _context;
	private String 	_path;
	
	public ImgUtil(Context context) {
		_context = context;
		
		_path = _context.getDir(LOADED_IMG_DIR, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE) + "/";
		_asyncImageLoader = new AsyncImageLoader(_path);
	}
	
	public static Bitmap getSuitableBitmap(File file) {
		Bitmap bitmap = null;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			FileInputStream fis	= new FileInputStream(file);
			bitmap	= BitmapFactory.decodeStream(fis, null, options);
			
			int sum = options.outWidth * options.outHeight;
			SwitchLogger.d(LOG_TAG, "=============before, width="+options.outWidth+",height="+options.outHeight);
			if (sum > 640000 || options.outWidth > 2048 || options.outHeight > 2048) { // 图片太大
				float scale = (float) Math.sqrt(sum / 500000.0f);
				float extScale	= 0.0f;	
				if(options.outWidth > 2048) {
					extScale	= (float)((float)(options.outWidth) / (float)(1024+256));
				}
				if(options.outHeight > 2048) {
					float tmpScale	= (float)((float)(options.outHeight) / (float)(1024+256));
					if(tmpScale > extScale) {
						extScale = tmpScale;
					}
				}
				float totalScale	= scale + extScale;
				options.outWidth /= totalScale;
				options.outHeight /= totalScale;
				options.inSampleSize		= (int)(Math.ceil(totalScale));
				//options.inPreferredConfig	= Bitmap.Config.ARGB_4444;
				//options.inPreferredConfig	= Bitmap.Config.RGB_565;
				options.inDither			= false;	//Disable Dithering mode
				options.inPurgeable			= true;		//Tell to gc that whether it needs free memory, the Bitmap can be cleared
				options.inInputShareable	= true;		//Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
				SwitchLogger.d(LOG_TAG, "=============scale="+scale+", extScale="+extScale+",totalScale="+totalScale+",inSampleSize="+options.inSampleSize);
			}
			SwitchLogger.d(LOG_TAG, "=============after, width="+options.outWidth+",height="+options.outHeight);
			options.inJustDecodeBounds	= false;

			fis	= new FileInputStream(file);
			bitmap = BitmapFactory.decodeStream(fis, null, options);
		} catch (Exception e) {
			SwitchLogger.e(e);
			return null;
		}
		return bitmap;
	}

	public static String getImgTypeFromFile(File file) {
		String type = IMG_TYPE_JPG;
		try {
			FileInputStream fis	= new FileInputStream(file);
			byte[] head = new byte[10];  
			fis.read(head);
			
			// Png test:  
			if (head[1] == 'P' && head[2] == 'N' && head[3] == 'G') {  
				type = IMG_TYPE_PNG;  
			} else if (head[0] == 'G' && head[1] == 'I' && head[2] == 'F') {  
				type = IMG_TYPE_GIF;  
			} else if (head[6] == 'J' && head[7] == 'F' && head[8] == 'I' && head[9] == 'F') {  
				type = IMG_TYPE_JPG;   
			} else {
				type = IMG_TYPE_JPG;
				SwitchLogger.d(LOG_TAG, "unknown imgae type, take it as JPG");
			}
			
			fis.close();
		} catch(Exception e) {
			SwitchLogger.e(e);
		}
		
		return type;
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
			imageView.setImageDrawable(BitmapUtil.toRoundCorner(_context, defaultDrawable, 5));
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
				if (null != imageDrawable && null != imageView) {
					imageView.setImageDrawable(imageDrawable);
				}
			}
		});
		
		if (null != cachedImage && null != imageView) {
			imageView.setImageDrawable(cachedImage);
		}
		
		return _path + _asyncImageLoader.getImgName(url);
	}
}
