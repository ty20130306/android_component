package com.vanchu.libs.common.util;

import java.io.ByteArrayOutputStream;

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
