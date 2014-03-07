package com.vanchu.libs.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Bitmap工具类

 */
public class BitmapUtil {
	
	private static final String LOG_TAG	= BitmapUtil.class.getSimpleName();
	
	/**
	 * 获取合适的bitmap，避免发生OOM
	 * @param file
	 * @return
	 */
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

	/**
	 * 从文件获取bitmap
	 * @param filePath
	 * @return
	 */
	public static Bitmap getBitmapFromFile(String filePath) {
		try {
			File file	= new File(filePath);
			FileInputStream fis	= new FileInputStream(file);
			Bitmap bitmap	= BitmapFactory.decodeStream(fis, null, null);
			
			return bitmap;
		} catch (Exception e) {
			SwitchLogger.e(e);
			return null;
		}
	}
	
	/**
	 * 按最大大小不超过maxSize压缩
	 * @param originalBitmap
	 * @param maxSize 单位byte
	 */
	public static Bitmap compressBitmapBySize(Bitmap originalBitmap, long maxSize) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		originalBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		if(baos.toByteArray().length <= maxSize) {
			return originalBitmap;
		}
		
		int options = 100;
		while ( baos.toByteArray().length > maxSize && options > 40) { 
			baos.reset();//重置baos即清空baos  
			options -= 20;
			originalBitmap.compress(Bitmap.CompressFormat.PNG, options, baos);
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Bitmap resultBitmap = BitmapFactory.decodeStream(bais, null, null);
		return resultBitmap;	
	}
	
	/**
	 * 按像素按比例压缩bitmap
	 * @param originalBitmap
	 * @param dstWidth
	 * @param dstHeight
	 * @return
	 */
	public static Bitmap compressBitmapByResolution(Bitmap originalBitmap, int dstWidth, int dstHeight) {
		try {
			if(dstWidth <= 0 || dstHeight <= 0) {
				return originalBitmap;
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			originalBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			baos.close();
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			Bitmap resultBitmap	= BitmapFactory.decodeStream(bais, null, options);
			
			int srcWidth	= options.outWidth;
			int srcHeight	= options.outHeight;
			float widthRatio	= (float)((float)srcWidth / (float)dstWidth);
			float heightRatio	= (float)((float)srcHeight / (float)dstHeight);
			float ratio	= widthRatio;
			if(heightRatio > ratio) {
				ratio	= heightRatio;
			}
			float extRatio	= 0.0f;	
			if(options.outWidth > 2048) {
				extRatio	= (float)((float)(options.outWidth) / (float)(1024+256));
			}
			if(options.outHeight > 2048) {
				float tmpRatio	= (float)((float)(options.outHeight) / (float)(1024+256));
				if(tmpRatio > extRatio) {
					extRatio = tmpRatio;
				}
			}
			SwitchLogger.d(LOG_TAG, "=============src width="+options.outWidth+", src height="+options.outHeight);
			SwitchLogger.d(LOG_TAG, "=============dst width="+dstWidth+", dst height="+dstHeight);
			
			float totalRatio	= ratio + extRatio;
			options.outWidth /= totalRatio;
			options.outHeight /= totalRatio;
			options.inSampleSize		= (int)(Math.ceil(totalRatio));
			//options.inPreferredConfig	= Bitmap.Config.ARGB_4444;
			//options.inPreferredConfig	= Bitmap.Config.RGB_565;
			options.inDither			= false;	//Disable Dithering mode
			options.inPurgeable			= true;		//Tell to gc that whether it needs free memory, the Bitmap can be cleared
			options.inInputShareable	= true;		//Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
			SwitchLogger.d(LOG_TAG, "=============ratio="+ratio+", extRatio="+extRatio+",totalRatio="+totalRatio+",inSampleSize="+options.inSampleSize);

			SwitchLogger.d(LOG_TAG, "=============in fact, real dst width="+options.outWidth+",real dst height="+options.outHeight);
			options.inJustDecodeBounds	= false;
			
			bais.reset();
			resultBitmap = BitmapFactory.decodeStream(bais, null, options);
			return resultBitmap;
		} catch (Exception e) {
			SwitchLogger.e(e);
			return null;
		}
	}
	
	/**
	 * 保存bitmap到文件
	 * @param bitmap
	 * @param path
	 * @return
	 */
	public static boolean saveBitmapToFile(Bitmap bitmap, String path) {
		try {
			File file	= new File(path);
			if( ! file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fos	= new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			SwitchLogger.e(e);
			return false;
		}
		
		return true;
		
	}
	
	/**
	 * Bitmap转成byte[]
	 * 
	 * @param bitmap
	 * @return
	 */
	public static byte[] getBytesFromBitmap(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (bitmap == null) {
			return null;
		}
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * 等宽切割一维数字图片
	 * 
	 * @param context
	 *            上下文
	 * @param imgResId
	 *            资源图片
	 * @param count
	 *            切割数目
	 * @param num
	 *            返回的数字
	 * @return
	 */
	public static Bitmap getNumBitmap(Context context, int imgResId, int count, int num) {
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imgResId);
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		return Bitmap.createBitmap(bitmap, w * num / count, 0, w / count, h);
	}

	/**
	 * 绘制圆角ImageView
	 * 
	 * @param context
	 *            上下文
	 * @param imgResId
	 *            资源图片
	 * @param pixels
	 *            圆角半径
	 * @return
	 */
	public static Drawable toRoundCorner(Context context, int imgResId, int pixels) {
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imgResId);
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return new BitmapDrawable(output);
	}

	/**
	 * 绘制圆角ImageView
	 * 
	 * @param drawable
	 *            资源图片
	 * @param pixels
	 *            圆角半径
	 * @return
	 */
	public static Drawable toRoundCorner(Drawable drawable, int pixels) {
		BitmapDrawable b = (BitmapDrawable) drawable;
		Bitmap bitmap = b.getBitmap();
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;
		paint.setAntiAlias(true);// 反走样
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));// 设置图片相交时的模式
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return new BitmapDrawable(output);
	}

}
