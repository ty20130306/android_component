package com.vanchu.test;


import com.vanchu.libs.common.util.CropImgUtil;
import com.vanchu.libs.common.util.SwitchLogger;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;

import android.view.Display;
import android.view.View;
import android.widget.ImageView;

public class CropActivity extends Activity {
	private static final String	LOG_TAG	= CropActivity.class.getSimpleName();
	
	Bitmap bp=null;  
	
	private ImageView _imageView;
	private boolean		_filled;
	
	float scaleWidth;
	float scaleHeight;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crop);
		SwitchLogger.setPrintLog(true);
		
		Display display=getWindowManager().getDefaultDisplay();  
        bp=BitmapFactory.decodeResource(getResources(),R.drawable.beauty);  
        int width=bp.getWidth();  
        int height=bp.getHeight();  
        int w=display.getWidth();  
        int h=display.getHeight();  
        scaleWidth=((float)w)/width;  
        scaleHeight=((float)h)/height; 
        
        SwitchLogger.d(LOG_TAG, "width:"+width+",height="+height+",dw="+w+",dh="+h+",scaleWidth="+scaleWidth+",scaleHeight="+scaleHeight);
        _imageView	= (ImageView)findViewById(R.id.pic_1);
		
		_filled	= false;
		_imageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if( ! _filled) {
					Matrix matrix=new Matrix();  
                    matrix.postScale(1.0f,1.0f);  
                    Bitmap newBitmap=Bitmap.createBitmap(bp, 0, 0, bp.getWidth(), bp.getHeight(), matrix, true);  
                    _imageView.setImageBitmap(newBitmap);  
                    _filled=true;
                    
				} else {
					Matrix matrix=new Matrix();  
                    matrix.postScale(scaleWidth,scaleHeight);  
                      
                    Bitmap newBitmap=Bitmap.createBitmap(bp, 0, 0, bp.getWidth(), bp.getHeight(), matrix, true);  
                    _imageView.setImageBitmap(newBitmap);  
                    _filled=false; 
				}
			}
		});
	}

	private boolean needCropHeight(float showWidth, float showHeight, float imgWidth, float imgHeight) {
		if((showHeight *1.0 / showWidth) < (imgHeight * 1.0) / imgWidth) {
			return true;
		} else {
			return false;
		}
	}

	private int calNewHeight(int showWidth, int showHeight, int imgWidth) {
		return (int)(showHeight * 1.0 * imgWidth / showWidth);
	}
	
	private int calNewWidth(int showWidth, int showHeight, int imgHeight) {
		return (int)(showWidth * 1.0 * imgHeight / showHeight);
	}
	
	public void cropStart(View v) {
		Bitmap bmp	= BitmapFactory.decodeResource(getResources(), R.drawable.beauty);
		
		Bitmap resizedbitmap	= CropImgUtil.scaleCrop(bmp, 400, 100, CropImgUtil.CROP_TYPE_START_SIDE);
		ImageView beauty	= (ImageView)findViewById(R.id.pic_1);
		beauty.setImageBitmap(resizedbitmap);
	}
	
	public void cropEnd(View v) {
		Bitmap bmp	= BitmapFactory.decodeResource(getResources(), R.drawable.beauty);
		
		Bitmap resizedbitmap	= CropImgUtil.scaleCrop(bmp, 400, 100, CropImgUtil.CROP_TYPE_END_SIDE);
		ImageView beauty	= (ImageView)findViewById(R.id.pic_1);
		beauty.setImageBitmap(resizedbitmap);
	}

	public void cropBoth(View v) {
		Bitmap bmp	= BitmapFactory.decodeResource(getResources(), R.drawable.beauty);
		
		Bitmap resizedbitmap	= CropImgUtil.scaleCrop(bmp, 400, 100, CropImgUtil.CROP_TYPE_BOTH_SIDE);
		ImageView beauty	= (ImageView)findViewById(R.id.pic_1);
		beauty.setImageBitmap(resizedbitmap);
	}
	
	public void crop(View v) {
		DisplayMetrics metric = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(metric);  
        int screenWidth = metric.widthPixels;     // 屏幕宽度（像素）  
        int screenHeight = metric.heightPixels;   // 屏幕高度（像素）  
        
        Bitmap bmp=BitmapFactory.decodeResource(getResources(), R.drawable.beauty);
        int imgWidth=bmp.getWidth();
        int imgHeight=bmp.getHeight();
        
//        int showWidth	= screenWidth;
//        //int showHeight	= (int)(screenHeight * 1.0 / 6);
//        int showHeight	= (int)(400 * metric.density);
        
	      int showWidth		= 100;
	      int showHeight	= 400;
        
        int newImgHeight	= imgHeight;
        int newImgWidth		= imgWidth;
        SwitchLogger.e("*******", "before,showWidth:"+showWidth+",showHeight:"+showHeight
        		+",imgWidth:"+imgWidth+",imgHeight"+imgHeight);
        
        Bitmap resizedbitmap;
        int begin;
        if(needCropHeight(showWidth,showHeight, imgWidth,  imgHeight)) {
        	newImgHeight	= calNewHeight(showWidth,showHeight,  imgWidth);
        	begin	= (int)(((imgHeight - newImgHeight) * 1.0) / 2);
        	resizedbitmap=Bitmap.createBitmap(bmp,0,begin, newImgWidth, newImgHeight-begin);
        	//resizedbitmap=Bitmap.createScaledBitmap(bmp,newImgWidth, newImgHeight-begin*2, false);
        	SwitchLogger.e("*******", "crop height, new pic height:"+newImgHeight);
        } else {
        	newImgWidth		= calNewWidth(showWidth,showHeight,  imgHeight);
        	begin	= (int)(((imgWidth - newImgWidth) * 1.0) / 2);
        	
        	//resizedbitmap=Bitmap.createBitmap(bmp,begin,0, newImgWidth-begin, newImgHeight);
        	resizedbitmap=Bitmap.createBitmap(bmp,begin,0, newImgWidth, newImgHeight);
        	//resizedbitmap=Bitmap.createBitmap(bmp,newImgWidth-begin*2, newImgHeight, false);
        	SwitchLogger.e("*******", "crop width,begin:"+begin+" new pic width:"+newImgWidth);
        }

        
        SwitchLogger.e("*******", "resizedbitmap width:"+resizedbitmap.getWidth()+",height:" + resizedbitmap.getHeight());
        ImageView beauty	= (ImageView)findViewById(R.id.pic_1);
        beauty.setImageBitmap(resizedbitmap);
        
	}

}

