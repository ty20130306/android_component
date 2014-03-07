package com.vanchu.sample;

import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.gestureLock.GestureView;
import com.vanchu.libs.gestureLock.GestureViewConfig;
import com.vanchu.test.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class GestureLockSampleActivity extends Activity {

	private GestureView lockView;
	private TextView textView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gesture_lock);
		lockView = (GestureView) findViewById(R.id.gestureView1);
		textView = (TextView) findViewById(R.id.textView1);
		((Button) findViewById(R.id.button1))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						int value = lockView.getRandomValue();
						lockView.showGesture(value);
						textView.setText("手势密码：" + value);
					}
				});
		GestureViewConfig config = new GestureViewConfig(this, 300,
				R.drawable.gesture_big, R.drawable.gesture_big_touch,
				R.drawable.gesture_small, R.drawable.gesture_small_touch);
		GestureView.CallBack callBack = new GestureView.CallBack() {

			@Override
			public void onActionOver(int gestureValue) {
				textView.setText("手势密码：" + gestureValue);
			}
		};
		lockView.init(callBack, config);
	}

	@Override
	public void finish() {
		if (lockView != null) {
			lockView.recycle();
		}
		super.finish();
	}

}
