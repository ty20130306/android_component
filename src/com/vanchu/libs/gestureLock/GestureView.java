package com.vanchu.libs.gestureLock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;

public class GestureView extends View {

	public GestureView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public GestureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public GestureView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	private List<GestureViewPoint> items;
	private int[] number;
	private CallBack callBack;
	private GestureViewConfig config;
	private boolean touchable = true;

	public void init(CallBack callBack, int line) {
		config = new GestureViewConfig(line);
		init(callBack, config);
	}

	public void init(CallBack callBack, GestureViewConfig config) {
		this.callBack = callBack;
		this.config = config;
		items = new ArrayList<GestureViewPoint>();
		items.add(new GestureViewPoint(1, new PointF(config.cube / 2,
				config.cube / 2)));
		items.add(new GestureViewPoint(2, new PointF(config.cube / 2 * 3,
				config.cube / 2)));
		items.add(new GestureViewPoint(3, new PointF(config.cube / 2 * 5,
				config.cube / 2)));
		items.add(new GestureViewPoint(4, new PointF(config.cube / 2,
				config.cube / 2 * 3)));
		items.add(new GestureViewPoint(5, new PointF(config.cube / 2 * 3,
				config.cube / 2 * 3)));
		items.add(new GestureViewPoint(6, new PointF(config.cube / 2 * 5,
				config.cube / 2 * 3)));
		items.add(new GestureViewPoint(7, new PointF(config.cube / 2,
				config.cube / 2 * 5)));
		items.add(new GestureViewPoint(8, new PointF(config.cube / 2 * 3,
				config.cube / 2 * 5)));
		items.add(new GestureViewPoint(9, new PointF(config.cube / 2 * 5,
				config.cube / 2 * 5)));
		reset();
	}

	public void setConfig(GestureViewConfig config) {
		this.config = config;
	}

	public void reset() {
		touchable = true;
		number = new int[items.size()];
		for (GestureViewPoint item : items) {
			item.hasTouched = false;
		}
		postInvalidate();
	}

	public void showGesture(int value) {
		char[] chars = (value + "").toCharArray();
		touchable = false;
		number = new int[items.size()];
		for (GestureViewPoint item : items) {
			item.hasTouched = false;
		}
		for (int i = 0; i < chars.length; i++) {
			number[i] = chars[i] - '0';
			if (number[i] > 0) {
				items.get(number[i] - 1).hasTouched = true;
			}
		}
		postInvalidate();
	}

	public int getRandomValue() {
		Random r = new Random();
		int k = 0;
		while (true) {
			k = r.nextInt(1000000000);
			if ((k + "").contains("0")) {
			} else {
				boolean needReChoose = false;
				char[] chars = (k + "").toCharArray();
				for (int i = 0; i < chars.length; i++) {
					for (int j = i + 1; j < chars.length; j++) {
						if ((int) chars[i] == (int) chars[j]) {
							needReChoose = true;
							break;
						}
					}
					if (needReChoose) {
						break;
					}
				}
				if (!needReChoose) {
					break;
				}
			}
		}
		return k;
	}

	public void recycle() {
		if (config != null) {
			config.recycle();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!touchable) {
			return super.onTouchEvent(event);
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			check(new PointF(event.getX(), event.getY()));
			break;
		case MotionEvent.ACTION_UP:
			int key = 0;
			String s = "";
			for (int i = 0; i < number.length; i++) {
				s += number[i];
			}
			key = Integer.parseInt(s);
			if (callBack != null && key != 0) {
				callBack.onActionOver(key);
			}
			reset();
			break;
		case MotionEvent.ACTION_MOVE:
			check(new PointF(event.getX(), event.getY()));
			break;
		default:
			break;
		}
		postInvalidate();
		return true;
	}

	private void check(PointF pointF) {
		for (GestureViewPoint item : items) {
			if (!item.hasTouched && length(pointF, item.center) < config.touchR) {
				for (int i = 0; i < number.length; i++) {
					if (number[i] == 0) {
						number[i] = item.value;
						break;
					}
				}
				item.hasTouched = true;
			}
		}
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		if (items == null) {
			return;
		}
		Paint paint = new Paint();
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setAntiAlias(true);

		paint.setStrokeWidth(1);
		for (GestureViewPoint item : items) {
			item.drawSelf(config, canvas, paint);
		}

		paint.setStrokeWidth(config.lineWidth);
		drawLine(canvas, paint);

		paint.setStrokeWidth(1);
		for (GestureViewPoint item : items) {
			item.drawCenterCircle(config, canvas, paint);
		}
	}

	private void drawLine(Canvas canvas, Paint paint) {
		paint.setColor(config.lineColor);
		// paint.setStrokeCap(Paint.Cap.SQUARE);
		for (int i = 1; i < number.length; i++) {
			if (number[i] > 0) {
				PointF start = items.get(number[i - 1] - 1).center;
				PointF end = items.get(number[i] - 1).center;
				canvas.drawLine(start.x, start.y, end.x, end.y, paint);
			}
		}
	}

	private float length(PointF pointF, PointF center) {
		float x = Math.abs(pointF.x - center.x);
		float y = Math.abs(pointF.y - center.y);
		return FloatMath.sqrt(x * x + y * y);
	}

	public interface CallBack {

		public void onActionOver(int gestureValue);
	}

}
