package com.vanchu.libs.gestureLock;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class GestureViewPoint {

	public PointF center;
	public boolean hasTouched;
	public int value;

	public GestureViewPoint(int value, PointF center) {
		super();
		this.value = value;
		this.center = center;
		this.hasTouched = false;
	}

	public void drawSelf(GestureViewConfig config, Canvas canvas, Paint paint) {
		switch (config.paintType) {
		case GestureViewConfig.TYPE_COLOR:
			if (hasTouched) {
				paint.setColor(config.chooseColor);
				canvas.drawCircle(center.x, center.y, config.roundR, paint);
			} else {
				paint.setColor(config.unchooseColor);
				canvas.drawCircle(center.x, center.y, config.roundR, paint);
			}
			break;
		case GestureViewConfig.TYPE_IMAGE:
			if (hasTouched) {
				Bitmap bitmap = config.chooseImage;
				if (bitmap != null) {
					canvas.drawBitmap(bitmap, center.x - bitmap.getWidth() / 2,
							center.y - bitmap.getHeight() / 2, paint);
				}
			} else {
				Bitmap bitmap = config.unchooseImage;
				if (bitmap != null) {
					canvas.drawBitmap(bitmap, center.x - bitmap.getWidth() / 2,
							center.y - bitmap.getHeight() / 2, paint);
				}
			}
			break;
		default:
			break;
		}
	}

	public void drawCenterCircle(GestureViewConfig config, Canvas canvas,
			Paint paint) {
		switch (config.paintType) {
		case GestureViewConfig.TYPE_COLOR:
			if (hasTouched) {
				paint.setColor(config.chooseCenterCircleColor);
				canvas.drawCircle(center.x, center.y, config.smallRoundR, paint);
			} else {
				paint.setColor(config.unchooseCenterCircleColor);
				canvas.drawCircle(center.x, center.y, config.smallRoundR, paint);
			}
			break;
		case GestureViewConfig.TYPE_IMAGE:
			if (hasTouched) {
				Bitmap bitmap = config.chooseCenterCircleImage;
				if (bitmap != null) {
					canvas.drawBitmap(bitmap, center.x - bitmap.getWidth() / 2,
							center.y - bitmap.getHeight() / 2, paint);
				}
			} else {
				Bitmap bitmap = config.unchooseCenterCircleImage;
				if (bitmap != null) {
					canvas.drawBitmap(bitmap, center.x - bitmap.getWidth() / 2,
							center.y - bitmap.getHeight() / 2, paint);
				}
			}
			break;
		default:
			break;
		}
	}

}
