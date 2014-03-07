package com.vanchu.libs.imageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.vanchu.libs.common.util.SwitchLogger;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.widget.Toast;

public class ImageLoader {

	private final static String TAG = ImageLoader.class.getSimpleName();

	private final int code_choose = 190;
	private final int code_photo = 191;
	private final int code_cut = 192;
	private final String toastNoSDcard = "请插入SD卡";
	private final String toastOpenCameraError = "打开照相机失败";

	private File file;
	private String foldPath;
	private boolean isCut;
	private int width;
	private int height;
	private Activity activity;
	private ImageLoadListener listener;

	// prepare

	/** default path： SDcard => pictures */
	public ImageLoader(Activity activity) {
		init(activity, "pictures");
	}

	public ImageLoader(Activity activity, String foldName) {
		init(activity, foldName);
	}

	private void init(Activity activity, String foldName) {
		this.activity = activity;
		String SDcardRoot = getSDcardRootPath();
		this.foldPath = SDcardRoot + "/" + foldName + "/";
		if (isOK()) {
			File fold = new File(foldPath);
			if (!fold.exists()) {
				fold.mkdirs();
			}
		}
	};

	public void setListener(ImageLoadListener listener) {
		this.listener = listener;
	}

	// function

	public void choosePhotoFromLocal() {
		if (isOK()) {
			this.isCut = false;
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			activity.startActivityForResult(intent, code_choose);
		}
	}

	public void chooseAndCutPhotoFromLocal(boolean isSquare) {
		if (isSquare) {
			chooseAndCutPhotoFromLocal(1, 1);
		} else {
			chooseAndCutPhotoFromLocal(0, 0);
		}
	}

	public void chooseAndCutPhotoFromLocal(int width, int height) {
		if (isOK()) {
			this.isCut = true;
			this.width = width;
			this.height = height;
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			activity.startActivityForResult(intent, code_choose);
		}
	}

	public void takePhotoFromCamera() {
		if (isOK()) {
			try {
				this.isCut = false;
				Intent intent = openCamera();
				activity.startActivityForResult(intent, code_photo);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(activity, toastOpenCameraError,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void takeAndCutPhotoFromCamera(boolean isSquare) {
		if (isSquare) {
			takeAndCutPhotoFromCamera(1, 1);
		} else {
			takeAndCutPhotoFromCamera(0, 0);
		}
	}

	public void takeAndCutPhotoFromCamera(int width, int height) {
		if (isOK()) {
			try {
				this.isCut = true;
				this.width = width;
				this.height = height;
				Intent intent = openCamera();
				activity.startActivityForResult(intent, code_photo);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(activity, toastOpenCameraError,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	// onActivityResult

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		SwitchLogger.d(TAG, "");
		if (requestCode == code_choose || requestCode == code_photo
				|| requestCode == code_cut) {
			if (resultCode != Activity.RESULT_OK) {
				onFailed("result code error,maybe canceled");
				return;
			}
			switch (requestCode) {
			case code_choose:
				result_choose(data);
				break;
			case code_photo:
				result_photo();
				break;
			case code_cut:
				result_cut();
				break;
			}
		}
	}

	private void result_choose(Intent data) {
		if (data == null || data.getData() == null) {
			onFailed("result data is null");
			return;
		}
		Uri uri = data.getData();
		if (isCut) {
			file = new File(foldPath + System.currentTimeMillis() + ".cut");
			Intent intent = cropUri(uri, Uri.fromFile(file));
			activity.startActivityForResult(intent, code_cut);
		} else {
			file = getFile(uri);
			if (file != null && file.exists()) {
				onSuccess(file);
			} else {
				onFailed("file not exists");
			}
		}
	}

	private void result_photo() {
		if (isCut) {
			Uri photoUri = Uri.fromFile(file);
			file = new File(foldPath + System.currentTimeMillis() + ".cut");
			Uri outPutUri = Uri.fromFile(file);
			Intent intent = cropUri(photoUri, outPutUri);
			activity.startActivityForResult(intent, code_cut);
		} else {
			if (file != null && file.exists()) {
				onSuccess(file);
			} else {
				onFailed("file not exists");
			}
		}
	}

	private void result_cut() {
		if (file != null && file.exists()) {
			onSuccess(file);
		} else {
			onFailed("file not exists");
		}
	}

	// call back

	private void onSuccess(File file) {
		if (listener != null) {
			listener.onSuccess(file);
		}
	}

	private void onFailed(String string) {
		if (listener != null) {
			listener.onFailed(string);
		}
	}

	// private common function

	private boolean isOK() {
		boolean hasSDcard = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (hasSDcard) {
			return true;
		} else {
			Toast.makeText(activity, toastNoSDcard, Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	private Intent openCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		String strImgPath = foldPath + System.currentTimeMillis() + ".jpg";
		file = new File(strImgPath);
		Uri uri = Uri.fromFile(file);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		return intent;
	}

	private Intent cropUri(Uri uri, Uri outPutUri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		if (width != 0 && height != 0) {
			intent.putExtra("aspectX", width);
			intent.putExtra("aspectY", height);
			intent.putExtra("outputX", 128 * width);
			intent.putExtra("outputY", 128 * height);
		}
		intent.putExtra("scale", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
		intent.putExtra("return_data", true);
		return intent;
	}

	private String getSDcardRootPath() {
		String SDcardRootPath = null;
		File file = null;
		try {
			file = Environment.getExternalStorageDirectory();// ��ȡSD����Ŀ¼
			SDcardRootPath = file.getAbsolutePath();
		} catch (Exception e) {
			SDcardRootPath = null;
		}
		return SDcardRootPath;
	}

	private File getFile(Uri uri) {
		SwitchLogger.e("MyLog", "URI = " + uri.toString());
		File file = getFile1(uri);
		if (file == null) {
			file = getFile2(uri);
		}
		if (file == null) {
			file = getFile3(uri);
		}
		if (file == null) {
			file = getFile4(uri);
		}
		return file;
	}

	private File getFile1(Uri uri) {
		File file = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = activity.managedQuery(uri, proj, null, null, null);

			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			String path = cursor.getString(column_index);
			file = new File(path);
		} catch (Exception e) {
			SwitchLogger.e("MyLog", "getFile1Error");
			file = null;
		}
		return file;
	}

	private File getFile2(Uri uri) {
		File file = null;
		try {
			String[] projection = { MediaStore.Images.Media.DATA };
			CursorLoader loader = new CursorLoader(activity, uri, projection,
					null, null, null);
			Cursor cursor = loader.loadInBackground();
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			String path = cursor.getString(column_index);
			file = new File(path);
		} catch (Exception e) {
			SwitchLogger.e("MyLog", "getFile2Error");
			file = null;
		}
		return file;
	}

	private File getFile3(Uri uri) {
		File file = null;
		String uriString = uri.toString();
		String rootPath = getSDCardPath(activity);
		if (rootPath == null) {
			SwitchLogger.e("MyLog", "getFile3Error");
			return null;
		}
		int index = uriString.indexOf(rootPath);
		if (index == -1) {
			SwitchLogger.e("MyLog", "getFile3Error");
			return null;
		}
		String filePath = uriString.substring(index);
		file = new File(filePath);
		return file;
	}

	private File getFile4(Uri uri) {
		File file = null;
		file = new File(foldPath + System.currentTimeMillis() + ".jpg");
		try {
			InputStream is = activity.getContentResolver().openInputStream(uri);
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
			out.close();
			is.close();
		} catch (FileNotFoundException e) {
			SwitchLogger.e("MyLog", "getFile4Error");
			return null;
		} catch (IOException e) {
			SwitchLogger.e("MyLog", "getFile4Error");
			return null;
		}
		return file;
	}

	private String getSDCardPath(Context context) {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return null;
		}
		File file = Environment.getExternalStorageDirectory();
		return file.getAbsolutePath() + "/";
	}

	// listener

	public interface ImageLoadListener {

		public void onSuccess(File file);

		public void onFailed(String msg);

	}

}
