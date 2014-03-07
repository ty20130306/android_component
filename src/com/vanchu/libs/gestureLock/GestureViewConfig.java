package com.vanchu.libs.gestureLock;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

public class GestureViewConfig {

	public static final int TYPE_COLOR = 1;
	public static final int TYPE_IMAGE = 2;

	public int paintType;// 种类
	public int cube;// 方格边长
	public int roundR;// 大圆半径
	public int touchR;// 触摸半径
	public int smallRoundR;// 小圆半径
	public int lineWidth;// 直线宽度

	public int lineColor;

	public int unchooseColor;
	public int chooseColor;
	public int unchooseCenterCircleColor;
	public int chooseCenterCircleColor;

	public Bitmap unchooseImage;
	public Bitmap chooseImage;
	public Bitmap unchooseCenterCircleImage;
	public Bitmap chooseCenterCircleImage;

	public GestureViewConfig(int cubeLength) {
		paintType = TYPE_COLOR;
		cube = cubeLength / 3;
		setDefaultColor();
		changeSize(0.6, 0.6, 0.2, 0.1);
	}

	public GestureViewConfig(Activity context, int cubeLength,
			int unchooseImageID, int chooseImageID,
			int unchooseCenterCircleImageID, int chooseCenterCircleImageID) {
		paintType = TYPE_IMAGE;
		float density = context.getResources().getDisplayMetrics().density;
		cubeLength = (int) (cubeLength * density);
		cube = cubeLength / 3;
		this.unchooseImage = BitmapFactory.decodeResource(
				context.getResources(), unchooseImageID);
		this.chooseImage = BitmapFactory.decodeResource(context.getResources(),
				chooseImageID);
		this.unchooseCenterCircleImage = BitmapFactory.decodeResource(
				context.getResources(), unchooseCenterCircleImageID);
		this.chooseCenterCircleImage = BitmapFactory.decodeResource(
				context.getResources(), chooseCenterCircleImageID);
		setDefaultColor();
		changeSize(0.6, 0.6, 0.2, 0.1);
	}

	private void setDefaultColor() {
		unchooseColor = Color.GRAY;
		chooseColor = Color.GREEN;
		unchooseCenterCircleColor = Color.YELLOW;
		chooseCenterCircleColor = Color.BLACK;
		lineColor = 0xff8496c8;
	}

	public void changeSize(double touchSize, double roundSize,
			double smallRoundSize, double lineSize) {
		touchR = (int) (touchSize * cube / 2);
		roundR = (int) (roundSize * cube / 2);
		smallRoundR = (int) (smallRoundSize * cube / 2);
		lineWidth = (int) (lineSize * cube);
		if (paintType == TYPE_IMAGE) {
			if (unchooseImage != null) {
				unchooseImage = Bitmap.createScaledBitmap(unchooseImage,
						roundR * 2, roundR * 2, true);
			}
			if (chooseImage != null) {
				chooseImage = Bitmap.createScaledBitmap(chooseImage,
						roundR * 2, roundR * 2, true);
			}
			if (unchooseCenterCircleImage != null) {
				unchooseCenterCircleImage = Bitmap.createScaledBitmap(
						unchooseCenterCircleImage, smallRoundR * 2,
						smallRoundR * 2, true);
			}
			if (chooseCenterCircleImage != null) {
				chooseCenterCircleImage = Bitmap.createScaledBitmap(
						chooseCenterCircleImage, smallRoundR * 2,
						smallRoundR * 2, true);
			}
		}
	}

	public void recycle() {
		if (unchooseImage != null) {
			unchooseImage.recycle();
			unchooseImage = null;
		}
		if (chooseImage != null) {
			chooseImage.recycle();
			chooseImage = null;
		}
		if (unchooseCenterCircleImage != null) {
			unchooseCenterCircleImage.recycle();
			unchooseCenterCircleImage = null;
		}
		if (chooseCenterCircleImage != null) {
			chooseCenterCircleImage.recycle();
			chooseCenterCircleImage = null;
		}
	}

}
