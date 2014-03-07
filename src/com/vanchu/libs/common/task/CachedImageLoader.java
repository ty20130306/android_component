package com.vanchu.libs.common.task;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.vanchu.libs.common.util.BitmapUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.webCache.WebCache;

import android.graphics.Bitmap;
import android.os.Handler;

public class CachedImageLoader {
	private static final String LOG_TAG = CachedImageLoader.class.getSimpleName();
	
	public static final int		DEFAULT_MAX_CAPACITY			= 10;// 一级缓存的最大空间
	public static final long	DEFAULT_DELAY_BEFORE_PURGE		= 10 * 1000;// 定时清理缓存

	private int		mMaxCapacity		= DEFAULT_MAX_CAPACITY;
	private long	mDelayBeforePurge	= DEFAULT_DELAY_BEFORE_PURGE;
	
	private HashMap<String, Bitmap> mFirstLevelCache	= null;
	private ConcurrentHashMap<String, SoftReference<Bitmap>> mSecondLevelCache		= null;
	private Runnable mClearCache	= null;
	private Handler mPurgeHandler	= null;

	/**
	 * 构造函数
	 */
	public CachedImageLoader() {
		init();
	}
	
	public CachedImageLoader(int maxCapacity, long delayBeforePurge) {
		mMaxCapacity		= maxCapacity;
		mDelayBeforePurge	= delayBeforePurge;
		init();
	}
	
	private void init() {
		mPurgeHandler = new Handler();
		
		mClearCache = new Runnable() {
			@Override
			public void run() {
				clear();
			}
		};
		
		initFirstLevelCache();
		initSecondLevelCache();
	}
	
	private void initSecondLevelCache() {
		// 采用的是软应用，只有在内存吃紧的时候软应用才会被回收，有效的避免了oom
		mSecondLevelCache	= new ConcurrentHashMap<String, SoftReference<Bitmap>>(mMaxCapacity / 2);
	}
	
	private void initFirstLevelCache() {
		// 0.75是加载因子为经验值，true则表示按照最近访问量的高低排序，false则表示按照插入顺序排序
		mFirstLevelCache = new LinkedHashMap<String, Bitmap>(mMaxCapacity / 2, 0.75f, true) {
			
			private static final long serialVersionUID = 1L;

			protected boolean removeEldestEntry(Entry<String, Bitmap> eldest) {
				if (size() > mMaxCapacity) {
					// 当超过一级缓存阈值的时候，将老的值从一级缓存搬到二级缓存
					mSecondLevelCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
					return true;
				}

				return false;
			}
		};
	}
	
	/**
	 *  重置缓存清理的timer
	 */
	private void resetPurgeTimer() {
		mPurgeHandler.removeCallbacks(mClearCache);
		mPurgeHandler.postDelayed(mClearCache, mDelayBeforePurge);
	}

	/**
	 * 清理缓存
	 */
	private void clear() {
		synchronized (mFirstLevelCache) {
			mFirstLevelCache.clear();
		}
		mSecondLevelCache.clear();
	}

	/**
	 * 返回缓存，如果没有则返回null
	 * 
	 * @param url
	 * @return
	 */
	private Bitmap getBitmapFromCache(String url) {
		//SwitchLogger.d(LOG_TAG, "mFirstLevelCache.size="+mFirstLevelCache.size());
		//SwitchLogger.d(LOG_TAG, "mSecondLevelCache.size="+mSecondLevelCache.size());
		
		Bitmap bitmap = null;
		bitmap = getFromFirstLevelCache(url);// 从一级缓存中拿
		if (bitmap != null) {
			//SwitchLogger.d(LOG_TAG, "firstLevelcache load succ");
			return bitmap;
		}
		
		bitmap = getFromSecondLevelCache(url);// 从二级缓存中拿
		if (bitmap != null) {
			//SwitchLogger.d(LOG_TAG, "secondLevelcache load succ");
			return bitmap;
		}
		
		return bitmap;
	}

	/**
	 * 从二级缓存中拿
	 * 
	 * @param url
	 * @return
	 */
	private Bitmap getFromSecondLevelCache(String url) {
		Bitmap bitmap = null;
		SoftReference<Bitmap> softReference = mSecondLevelCache.get(url);
		if (softReference != null) {
			bitmap = softReference.get();
			if (bitmap == null) {// 由于内存吃紧，软引用已经被gc回收了
				mSecondLevelCache.remove(url);
			}
		}
		
		return bitmap;
	}

	/**
	 * 从一级缓存中拿
	 * 
	 * @param url
	 * @return
	 */
	private Bitmap getFromFirstLevelCache(String url) {
		Bitmap bitmap = null;
		synchronized (mFirstLevelCache) {
			bitmap = mFirstLevelCache.get(url);
			if (bitmap != null) {// 将最近访问的元素放到链的头部，提高下一次访问该元素的检索速度（LRU算法）
				mFirstLevelCache.remove(url);
				mFirstLevelCache.put(url, bitmap);
			}
		}
		return bitmap;
	}

	/**
	 * 加载图片，如果缓存中有就直接从缓存中拿，缓存中没有就下载
	 * @param url 图片url
	 * @param webCache 用于下载图片的webCache
	 * @param callback 回调
	 */
	public void loadImage(final String url, final WebCache webCache, final Callback callback) {
		resetPurgeTimer();
		Bitmap cachedBitmap = getBitmapFromCache(url);
		if (cachedBitmap != null) {
			callback.onSucc(url, cachedBitmap);
			return;
		}
		
		webCache.get(url, new WebCache.GetCallback() {
			
			@Override
			public void onProgress(String url, int progress, Object param) {
				callback.onProgress(url, progress);
			}
			
			@Override
			public void onFail(String url, int reason, Object param) {
				callback.onFail(url);
				
			}
			
			@Override
			public void onDone(String url, File file, Object param) {
				Bitmap newBitmap	= BitmapUtil.getSuitableBitmap(file);
				if(null == newBitmap) {
					callback.onFail(url);
					return ;
				}
				
				addImage2Cache(url, newBitmap);
				callback.onSucc(url, newBitmap);
				
			}
		}, null, false);
	}

	/**
	 * 放入缓存
	 * 
	 * @param url
	 * @param bitmap
	 */
	private void addImage2Cache(String url, Bitmap bitmap) {
		if (bitmap == null || url == null) {
			return;
		}
		synchronized (mFirstLevelCache) {
			mFirstLevelCache.put(url, bitmap);
		}
	}
	
	public interface Callback {
		public void onProgress(String url, int progress);
		public void onSucc(String url, Bitmap bitmap);
		public void onFail(String url);
	}
}
