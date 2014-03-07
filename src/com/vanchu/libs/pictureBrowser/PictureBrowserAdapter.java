package com.vanchu.libs.pictureBrowser;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vanchu.libs.common.imgZoom.GestureImageView;
import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.BitmapUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.webCache.WebCache;
import com.vanchu.libs.webCache.WebCache.GetCallback;

public class PictureBrowserAdapter extends PagerAdapter {

	private final String TAG = PictureBrowserAdapter.class.getSimpleName();
	public Context context = null;
	private List<PictureBrowserDataEntity> listEntity = null;
	private final String saveCachePath = "vanchu_browser_guimi_cacheImage";
	private WebCache webCache = null;
	private WebCache.GetCallback webcacheCallback = null;
	private PictureBrowserItemViewEntity itemEntity = null;
	private GestureImageView currentImageView = null;
	private PictureBrowserItemClickLinstener itemClickLinstener = null;

	public void setItemClickLinstener(
			PictureBrowserItemClickLinstener itemClickLinstener) {
		this.itemClickLinstener = itemClickLinstener;
	}

	public PictureBrowserAdapter(Context ctx,
			List<PictureBrowserDataEntity> list,
			PictureBrowserItemViewEntity itemEntity) {
		this.context = ctx;
		this.listEntity = list;
		this.itemEntity = itemEntity;
		initWebcache();
	}

	/**
	 * 初始化Webcache
	 */
	private void initWebcache() {
		webCache = WebCache.getInstance(context, saveCachePath);
		WebCache.Settings settings = new WebCache.Settings();
		settings.capacity = 100;
		settings.timeout = 10000; // millisecond
		webCache.setup(settings);
		webcacheCallback = new WebCache.GetCallback() {
			@Override
			public void onFail(String url, int reason, Object param) {
				if (null != itemEntity) {
					if (null != param) {
						View view = (View) param;
						ImageView detailImage = (ImageView) view
								.findViewById(itemEntity.getImageviewId());
						ProgressBar progressBar = (ProgressBar) view
								.findViewById(itemEntity.getProgressbarId());
						TextView textView = (TextView) view
								.findViewById(itemEntity.getTextProgressId());
						progressBar.setVisibility(View.GONE);
						textView.setVisibility(View.GONE);
						detailImage.setVisibility(View.GONE);
						Tip.show(context, "加载大图失败");
					}
				}
			}

			@Override
			public void onDone(String url, File file, Object param) {
				if (null != param) {
					View view = (View) param;
					if (null != itemEntity) {
						GestureImageView detailImage = (GestureImageView) view
								.findViewById(itemEntity.getImageviewId());
						ImageView defImageView = (ImageView) view
								.findViewById(itemEntity.getDefImageViewId());
						ProgressBar progressBar = (ProgressBar) view
								.findViewById(itemEntity.getProgressbarId());
						TextView textView = (TextView) view
								.findViewById(itemEntity.getTextProgressId());
						Bitmap bitmap = null;
						if (file != null) {
							bitmap = BitmapUtil.getSuitableBitmap(file);
							if (bitmap != null) {
								detailImage.setImageBitmap(bitmap);
								detailImage.setVisibility(View.VISIBLE);
								defImageView.setVisibility(View.GONE);
							}
						}
						progressBar.setVisibility(View.GONE);
						textView.setVisibility(View.GONE);
					}
				}
			}

			@Override
			public void onProgress(String url, int progress, Object param) {
				View view = (View) param;
				if (null != itemEntity) {
					TextView textView = (TextView) view.findViewById(itemEntity
							.getTextProgressId());
					textView.setText(String.valueOf(progress) + "%");
					textView.invalidate();
				}
			}
		};
	}

	@Override
	public int getCount() {
		if (listEntity != null) {
			return listEntity.size();
		}
		return 0;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return (arg0 == arg1);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if (listEntity != null) {
			View v = (View) object;
			((ViewPager) container).removeView(v);
			v = null;
		}
	}

	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View view = createItemView(position);
		((ViewPager) container).addView(view, 0);
		return view;
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);
		View view = (View) object;
		currentImageView = (GestureImageView) view.findViewById(itemEntity
				.getImageviewId());
	}

	public GestureImageView getItemView() {
		return currentImageView;
	}

	private View createItemView(int position) {

		View view = null;
		if (listEntity != null) {
			if (position < listEntity.size()) {
				PictureBrowserDataEntity entity = listEntity.get(position);
				if (null != itemEntity) {
					view = LayoutInflater.from(context).inflate(
							itemEntity.getLayoutResource(), null);
					GestureImageView detailImgView = (GestureImageView) view
							.findViewById(itemEntity.getImageviewId());
					ImageView defImageView = (ImageView) view
							.findViewById(itemEntity.getDefImageViewId());

					if (itemClickLinstener != null) {
						detailImgView
								.setOnTouchListener(new ItemImageOnTouchListener(
										detailImgView.getContext()));
						view.setOnClickListener(new ItemClick());
						view.setOnTouchListener(new DefImageOnTouchListener());
					}
					if (entity.getDefDrawable() != null) {
						defImageView.setImageDrawable(entity.getDefDrawable());
					}

					if (null != entity.getDefImgUrl()
							&& entity.getWebCache() != null) {
						defImageView.setVisibility(View.VISIBLE);
						detailImgView.setVisibility(View.GONE);
						String url = entity.getDefImgUrl();
						SwitchLogger
								.d(TAG, TAG + ",get default img url=" + url);
						entity.getWebCache().get(
								url,
								new DefImageListener(entity.getDetailUrl(),
										view), defImageView, false);
						return view;
					}
					defImageView.setVisibility(View.GONE);
					detailImgView.setVisibility(View.VISIBLE);
					SwitchLogger.d(TAG,
							TAG + " getDefDrawable:" + entity.getDefDrawable());
					webCache.get(entity.getDetailUrl(), webcacheCallback, view,
							false);
				}
			}
		}
		return view;
	}

	private class DefImageListener implements GetCallback {

		private String detailUrl = null;
		private View view = null;

		public DefImageListener(String detailUrl, View view) {
			this.detailUrl = detailUrl;
			this.view = view;
		}

		@Override
		public void onProgress(String url, int progress, Object param) {

		}

		@Override
		public void onFail(String url, int reason, Object param) {
			SwitchLogger.e(TAG, "get default img fail, url=" + url);
		}

		@Override
		public void onDone(String url, File file, Object param) {
			if (null != param) {
				ImageView imgView = (ImageView) param;
				if (null != file) {
					Bitmap bitmap = BitmapUtil.getSuitableBitmap(file);
					if (null != bitmap) {
						imgView.setImageBitmap(bitmap);
					}
				}
			}

			webCache.get(detailUrl, webcacheCallback, view, false);
		}
	}

	private class ItemImageOnTouchListener implements OnTouchListener {
		private GestureDetector detector = null;
		public ItemImageOnTouchListener(Context context) {
			detector = new GestureDetector(context,
					new SimpleOnGestureListener() {
						@Override
						public boolean onSingleTapConfirmed(MotionEvent e) {
							itemClickLinstener.onClick();
							return true;
						}
					});
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (detector.onTouchEvent(event)) {
				return true;
			}
			return false;
		}
	}
	
	private class DefImageOnTouchListener implements OnTouchListener {
		private GestureDetector detector = new GestureDetector(context,
				new SimpleOnGestureListener() {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						itemClickLinstener.onClick();
						return true;
					}
				});

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (detector.onTouchEvent(event)) {
				return true;
			}

			float defX = 0f;
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				defX = event.getX();
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				float newX = event.getX();
				float result = newX - defX;
				if (Math.abs(result) < 5) {
					itemClickLinstener.onClick();
					return true;
				}
			}
			return false;
		}
	}
	
	private class ItemClick implements OnClickListener{

		@Override
		public void onClick(View v) {
			
		}
	}
	
}
