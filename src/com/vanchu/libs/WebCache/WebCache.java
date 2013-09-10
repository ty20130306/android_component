package com.vanchu.libs.webCache;


import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by ray on 9/5/13.
 */
public class WebCache {
    static public class Settings{
        public int capacity = 100; /* items to hold */
        public int timeout = 10000; /* timeout in milliseconds */
    }

    public interface GetCallback{
        public void onDone(String url, File file, Object param);
        public void onFail(String url, int reason, Object param);
    }

    public static final int SUCC = 0x00;
    public static final int REASON_STORAGE_FAILED = 0x01;
    public static final int REASON_NETWORK_FAILED = 0x02;

    private static Map<String, WebCache> _instances = new TreeMap<String, WebCache>();
    private Storage _storage;
    private Network _network;

    private WebCache(Context ctx, String type){
        this._storage = new Storage(ctx, type);
        this._network = new Network();
    }

    public void setup(Settings settings){
        this._storage.setup(settings.capacity);
        this._network.setup(settings.timeout);
    }

    public final static WebCache getInstance(Context context, String type){
        synchronized (_instances){
            WebCache cache = _instances.get(type);
            if(cache == null){
                cache = new WebCache(context, type);
                _instances.put(type, cache);
            }
            return cache;
        }
    }

    public void get(final String url, final GetCallback getCallback, final Object param, final boolean skipCache){
        //step 1. setup handler
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
            switch (msg.what){
                case REASON_NETWORK_FAILED:
                    getCallback.onFail(url, REASON_NETWORK_FAILED, param);
                    break;
                case REASON_STORAGE_FAILED:
                    getCallback.onFail(url, REASON_NETWORK_FAILED, param);
                    break;
                case SUCC:
                    getCallback.onDone(url, (File)(msg.obj), param);
                    break;
                }
            }
        };

        //step 2. invoke a thread
        new Thread(){
            public void run(){
                //check storage first
            	if(!skipCache){
            		File file = WebCache.this._storage.get(url);
            		if(file != null){
            			handler.obtainMessage(SUCC, file).sendToTarget();
            			return;
            		}
            	}
                
                // try network
                InputStream inputStream = WebCache.this._network.get(url);
                if(inputStream == null){
                    handler.sendEmptyMessage(REASON_NETWORK_FAILED);
                    return;
                }
                File file = WebCache.this._storage.set(url, inputStream);
                if(file == null){
                    handler.sendEmptyMessage(REASON_STORAGE_FAILED);
                    return;
                }
                handler.obtainMessage(SUCC, file).sendToTarget();
            }
        }.start();
    }
}
