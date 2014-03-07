package com.vanchu.sample;

import java.io.File;

import com.vanchu.libs.common.util.BitmapUtil;
import com.vanchu.libs.imageLoader.ImageLoader;
import com.vanchu.libs.imageLoader.ImageLoader.ImageLoadListener;
import com.vanchu.test.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class ImageLoadSampleActivity extends Activity implements
		OnClickListener {

	private ImageLoader pictureLoad;
	private int count = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_imageload);
		((Button) findViewById(R.id.btn_1)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_2)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_3)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_4)).setOnClickListener(this);
		((ImageView) findViewById(R.id.iv_0)).setImageResource(getResources()
				.getIdentifier("e001", "drawable", "com.alex.apps.widget"));
		pictureLoad = new ImageLoader(this);
		pictureLoad.setListener(new ImageLoadListener() {

			@Override
			public void onSuccess(File file) {
				Bitmap bitmap = BitmapUtil.getSuitableBitmap(file);
				switch (count) {
				case 0:
					((ImageView) findViewById(R.id.iv_1))
							.setImageBitmap(bitmap);
					break;
				case 1:
					((ImageView) findViewById(R.id.iv_2))
							.setImageBitmap(bitmap);
					break;
				case 2:
					((ImageView) findViewById(R.id.iv_3))
							.setImageBitmap(bitmap);
					break;
				case 3:
					((ImageView) findViewById(R.id.iv_4))
							.setImageBitmap(bitmap);
					break;
				case 4:
					((ImageView) findViewById(R.id.iv_5))
							.setImageBitmap(bitmap);
					break;
				case 5:
					((ImageView) findViewById(R.id.iv_6))
							.setImageBitmap(bitmap);
					break;
				case 6:
					((ImageView) findViewById(R.id.iv_7))
							.setImageBitmap(bitmap);
					break;
				case 7:
					((ImageView) findViewById(R.id.iv_8))
							.setImageBitmap(bitmap);
					break;
				case 8:
					((ImageView) findViewById(R.id.iv_9))
							.setImageBitmap(bitmap);
					break;
				case 9:
					((ImageView) findViewById(R.id.iv_10))
							.setImageBitmap(bitmap);
					break;
				case 10:
					((ImageView) findViewById(R.id.iv_11))
							.setImageBitmap(bitmap);
					break;
				case 11:
					((ImageView) findViewById(R.id.iv_12))
							.setImageBitmap(bitmap);
					count = -1;
					break;
				}
				count++;
			}

			@Override
			public void onFailed(String msg) {
				Log.e("MyLog", msg);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		pictureLoad.onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_1:
			pictureLoad.choosePhotoFromLocal();
			break;
		case R.id.btn_2:
			pictureLoad.chooseAndCutPhotoFromLocal(1, 2);
			break;
		case R.id.btn_3:
			pictureLoad.takePhotoFromCamera();
			break;
		case R.id.btn_4:
			pictureLoad.takeAndCutPhotoFromCamera(true);
			break;
		default:
			break;
		}
	}
}
