package com.vanchu.libs.common.util;

import java.io.FileInputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class CropImgUtil {
	
	public static final int		CROP_TYPE_START_SIDE	= 1;
	public static final int		CROP_TYPE_END_SIDE		= 2;
	public static final int		CROP_TYPE_BOTH_SIDE		= 3;
	
	private static boolean needCropHeight(float showWidth, float showHeight, float imgWidth, float imgHeight) {
		if((showHeight *1.0 / showWidth) < (imgHeight * 1.0) / imgWidth) {
			return true;
		} else {
			return false;
		}
	}

	private static int calNewHeight(int showWidth, int showHeight, int imgWidth) {
		return (int)(showHeight * 1.0 * imgWidth / showWidth);
	}
	
	private static int calNewWidth(int showWidth, int showHeight, int imgHeight) {
		return (int)(showWidth * 1.0 * imgHeight / showHeight);
	}
	
	public static Bitmap scaleCrop(Context context, int resId, int showWidth, int showHeight, int cropType) {
		Bitmap bmp	= BitmapFactory.decodeResource(context.getResources(), resId);
		return scaleCrop(bmp, showWidth, showHeight, cropType);
	}
	
	public static Bitmap scaleCrop(String path, int showWidth, int showHeight, int cropType) {
		try {
			FileInputStream fis	= new FileInputStream(path);
			Bitmap bmp	= BitmapFactory.decodeStream(fis);
			return scaleCrop(bmp, showWidth, showHeight, cropType);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Bitmap scaleCrop(Bitmap bmp, int showWidth, int showHeight, int cropType) {
		int imgWidth	= bmp.getWidth();
        int imgHeight	= bmp.getHeight();

        int newImgHeight	= imgHeight;
        int newImgWidth		= imgWidth;
        int x			= 0;
        int y			= 0;
        
       // SwitchLogger.d(LOG_TAG, "showWidth:"+showWidth+",showHeight:"+showHeight+",imgWidth:"+imgWidth+",imgHeight:"+imgHeight);
        
        if(needCropHeight(showWidth, showHeight, imgWidth, imgHeight)) {
        	newImgHeight	= calNewHeight(showWidth, showHeight, imgWidth);
        	switch (cropType) {
			case CROP_TYPE_START_SIDE:
				y	= imgHeight - newImgHeight;
				break;
			
			case CROP_TYPE_END_SIDE:
				y	= 0;
				break;
			default:
				y	= (int)(((imgHeight - newImgHeight) * 1.0) / 2);
				break;
			}
        } else {
        	newImgWidth	= calNewWidth(showWidth, showHeight, imgHeight);
        	switch (cropType) {
			case CROP_TYPE_START_SIDE:
				x	= imgWidth - newImgWidth;
				break;
			
			case CROP_TYPE_END_SIDE:
				x	= 0;
				break;
			default:
				x	= (int)(((imgWidth - newImgWidth) * 1.0) / 2);
				break;
			}
        }
        
       // SwitchLogger.d(LOG_TAG, "x:"+x+",y:"+y+",newImgWidth:"+newImgWidth+",newImgHeight:"+newImgHeight);
        
        return Bitmap.createBitmap(bmp, x, y, newImgWidth, newImgHeight);
	}
}
