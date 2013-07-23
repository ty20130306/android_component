package com.vanchu.libs.upgrade;

import com.vanchu.libs.common.NetUtil;
import com.vanchu.libs.common.SwitchLogger;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class UpgradeProxy {
	private static final String LOG_TAG = UpgradeProxy.class.getSimpleName();
	
	private static final int SUCC							= 0;
	private static final int ERR_NETWORK_NOT_CONNECTED		= 1;
	private static final int ERR_HTTP_REQUEST_FAILED		= 2;
	private static final int ERR_HTTP_RESPONSE_ERROR		= 3;
	
	private Context				_context;
	private String				_upgradeInfoUrl;
	private UpgradeCallback		_callback;
	
	private Handler _handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what) {
			case SUCC:
				UpgradeParam param		= (UpgradeParam)msg.obj;
				UpgradeManager manager	= createUpgradeManager(_context, param, _callback);
				manager.check();
				break;
			
			case ERR_NETWORK_NOT_CONNECTED:
				_callback.onNetworkNotConnected();
				_callback.onComplete(UpgradeResult.RESULT_ERROR_TRY_IT);
				break;
			
			case ERR_HTTP_REQUEST_FAILED:
				SwitchLogger.e(LOG_TAG, "http get request for upgrade info fail");
				_callback.onComplete(UpgradeResult.RESULT_ERROR_TRY_IT);
				break;
				
			case ERR_HTTP_RESPONSE_ERROR:
				SwitchLogger.e(LOG_TAG, "upgrade info response error");
				_callback.onComplete(UpgradeResult.RESULT_ERROR_TRY_IT);
				break;
			
			default:
				break;
			}
		}
	};
	
	public UpgradeProxy(Context context, String upgradeInfoUrl, UpgradeCallback callback) {
		_context		= context;
		_upgradeInfoUrl	= upgradeInfoUrl;
		_callback		= callback;
	}
	
	protected UpgradeManager createUpgradeManager(Context context, UpgradeParam param, UpgradeCallback callback){
		return new UpgradeManager(context, param, callback);
	}
	
	private void doCheck(){
		if( ! NetUtil.isConnected(_context)){
			_handler.obtainMessage(ERR_NETWORK_NOT_CONNECTED).sendToTarget();
			return ;
		}
		
		String response = NetUtil.httpGetRequest(_upgradeInfoUrl, null, 3);
		if(response == null){
			_handler.obtainMessage(ERR_HTTP_REQUEST_FAILED).sendToTarget();
			return ;
		}
		
		SwitchLogger.d(LOG_TAG, "receive http response = " + response);
		
		UpgradeParam param	= _callback.onUpgradeInfoResponse(response);
		if(param == null){
			_handler.obtainMessage(ERR_HTTP_REQUEST_FAILED).sendToTarget();
		} else {
			_handler.obtainMessage(SUCC, param).sendToTarget();
		}
	}
	
	public void check(){
		new Thread(){
			public void run(){
				doCheck();
			}
		}.start();
	}
}
