package com.vanchu.libs.common.task;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.vanchu.libs.common.util.NetUtil;
import com.vanchu.libs.common.util.StringUtil;
import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class CfgMgr {
	private static final String LOG_TAG		= CfgMgr.class.getSimpleName();
	
	private static final String CFG_CENTER_DIR		= "cfg_center";
	private static final String CFG_INDEX_FILE_NAME	= "index";
	
	private static final int GET_NETWORK_CFG_SUCC		= 1;
	private static final int GET_NETWORK_CFG_FAIL		= 2;
	
	private static CfgMgr		_instance	= null;
	
	private Map<String, Object>	_lockMap	= new HashMap<String, Object>();
	
	private Context			_context;
	private List<String>	_index;
	private String			_dir;
	
	private CfgMgr(Context context) {
		_context	= context;
		_dir		= _context.getDir(CFG_CENTER_DIR, Context.MODE_PRIVATE).getAbsolutePath();
		_index	= new ArrayList<String>();
		
		loadIndex();
	}
	
	public static CfgMgr getInstance(Context context){
		if(null == _instance) {
			_instance	= new CfgMgr(context);
		}
		
		return _instance;
	}
	
	private String createCfgFileName(String url) {
		return StringUtil.md5sum(url);
	}
	
	private Object getLock(String url) {
		if( ! _lockMap.containsKey(url)){
			_lockMap.put(url, new Object());
		}
		
		return _lockMap.get(url);
	}
	
	private synchronized void loadIndex() {
		String indexFilePath	= _dir + "/" + CFG_INDEX_FILE_NAME;
		File f	= new File(indexFilePath);
		if( ! f.exists()) {
			SwitchLogger.d(LOG_TAG, "file not exists, file path=" + indexFilePath);
			return ;
		}
		
		RandomAccessFile file = null;
		try{
			file = new RandomAccessFile(indexFilePath, "r");
			int total = file.readInt();
			SwitchLogger.d(LOG_TAG, "load index, total num =" + total);
			for(int i = 0; i < total; ++i){
				int urlLen		= file.readInt();
				if(urlLen <= 0) {
					throw new Exception("bad url length, url len="+urlLen);
				}

				byte[] urlBytes	= new byte[urlLen];
				int hasRead	= 0;
				int tmp		= 0;
				while(-1 != (tmp = file.read(urlBytes, hasRead, urlLen - hasRead)) && hasRead < urlLen) {
					hasRead	+= tmp;
				}

				if(hasRead < urlLen) {
					throw new Exception("bad file format, read url fail");
				}

				String url	= new String(urlBytes);
				_index.add(url);
				file.close();
				file = null;
			}
		} catch (Exception e){
			SwitchLogger.e(e);			
		}

		try {
			if(file != null) {
				file.close();
			}
		} catch(Exception e) {
			SwitchLogger.e(e);
		}
	}
	
	private synchronized boolean saveIndex() {
		RandomAccessFile file = null;
		try{
			int newLength	= 0;
			file = new RandomAccessFile(_dir + "/" + CFG_INDEX_FILE_NAME, "rw");

			file.writeInt(_index.size());
			newLength	+= 4;
			for(int i = 0; i < _index.size(); ++i){
				String url	= _index.get(i);
				file.writeInt(url.length());
				newLength	+= 4;
				file.writeBytes(url);
				newLength	+= url.length();
			}
			
			file.setLength(newLength);
		} catch (Exception e){
			SwitchLogger.e(e);
			return false;
		}

		try {
			if(null != file){
				file.close();
			}
		} catch (Exception e){
			SwitchLogger.e(e);
			return false;
		}

		return true;
	}
	
	/**
	 * @param url cfg url
	 */
	public void removeLocal(String url) {
		Object lock	= getLock(url);
		synchronized(lock) {
			if(_index.contains(url)) {
				_index.remove(url);
			}

			String filePath	= createCfgFileName(url);
			File f	= new File(filePath);
			if(f.exists()) {
				f.delete();
			}
		}
	}
	
	/**
	 * @param url cfg url
	 * @return cfg data, null if not exits
	 */
	public JSONObject getLocal(String url) {
		Object lock	= getLock(url);
		synchronized (lock) {
			if( ! _index.contains(url)) {
				return null;
			}

			String cfgFileName	= createCfgFileName(url);
			String filePath		= _dir + "/" + cfgFileName;
			File f	= new File(filePath);
			if( ! f.exists()) {
				return null;
			}

			RandomAccessFile file = null;
			String jsonStr	= null;
			try{
				file = new RandomAccessFile(filePath, "r");
				int fileLen	= (int)(file.length());
				if(fileLen <= 0) {
					file.close();
					throw new Exception("file is empty, file=" + filePath);
				}

				byte[] buffer	= new byte[fileLen];
				int hasRead	= 0;
				int tmp		= 0;
				while(-1 != (tmp = file.read(buffer, hasRead, fileLen - hasRead))) {
					hasRead	+= tmp;
					if(hasRead >= fileLen) {
						break;
					}
				}

				if(hasRead < fileLen) {
					file.close();
					throw new Exception("bad file format, read cfg fail, url="+url);
				}

				jsonStr	= new String(buffer);
			} catch (Exception e){
				SwitchLogger.e(e);
				return null;
			}

			try {
				if(null != file){
					file.close();
				}
				return new JSONObject(jsonStr);
			} catch (Exception e){
				SwitchLogger.e(e);
				return null;
			}
		}
	}
	
	private boolean saveLocal(String url, JSONObject cfg) {
		Object lock	= getLock(url);
		synchronized (lock) {
			String cfgFileName	= createCfgFileName(url);
			String filePath		= _dir + "/" + cfgFileName;

			RandomAccessFile file = null;
			String jsonStr	= cfg.toString();
			try{
				int newLength	= 0;
				file = new RandomAccessFile(filePath, "rw");
				byte[] bytes	= jsonStr.getBytes(); 
				file.write(bytes);
				newLength	+= bytes.length;
				file.setLength(newLength);
			} catch (Exception e){
				SwitchLogger.e(e);
				return false;
			}

			try {
				if(null != file){
					file.close();
				}
			} catch (Exception e){
				SwitchLogger.e(e);
				return false;
			}

			return true;
		}
	}
	
	public void get(final String url, final GetCallback callback) {
		Object lock	= getLock(url);
		synchronized (lock) {
			final JSONObject localCfg	= getLocal(url);
			final Handler handler	= new Handler(){
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case GET_NETWORK_CFG_SUCC:
						JSONObject latestCfg	= callback.onResponse(url, (String)msg.obj);
						if(null != latestCfg) {
							if(saveLocal(url, latestCfg)) {
								if( ! _index.contains(url)){
									_index.add(url);
								}

								saveIndex();
							}
							callback.onSucc(url, true, latestCfg, localCfg);
						} else {
							if(null != localCfg) {
								callback.onSucc(url, false, localCfg, localCfg);
							} else {
								callback.onFail(url);
							}
						}
						break;

					case GET_NETWORK_CFG_FAIL:
						if(null != localCfg) {
							callback.onSucc(url, false, localCfg, localCfg);
						} else {
							callback.onFail(url);
						}
						break;

					default:
						break;
					}
				}
			};

			new Thread(){
				public void run() {
					String response	= NetUtil.httpGetRequest(url, null, 1);
					if(null == response) {
						handler.sendEmptyMessage(GET_NETWORK_CFG_FAIL);
					} else {
						handler.obtainMessage(GET_NETWORK_CFG_SUCC, response).sendToTarget();
					}
				}
			}.start();
		}
	}
	
	public interface GetCallback {
		/**
		 * @param url is cfg url
		 * @param response
		 * @return cfg data json object, null if errors happen
		 */
		public JSONObject onResponse(String url, String response);
		
		public void onSucc(String url, boolean cfgUpdated, JSONObject latestCfg, JSONObject oldCfg);
		public void onFail(String url);
	}
}
