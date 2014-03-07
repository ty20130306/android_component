package com.vanchu.libs.accountSystem;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanchu.libs.common.util.NetUtil;
import com.vanchu.libs.common.util.SwitchLogger;

import android.content.Context;
import android.os.Handler;

public class AccountSystem {

	private static final String LOG_TAG	= AccountSystem.class.getSimpleName();
	
	private static final int REGISTER_SUCC	= 1;
	private static final int REGISTER_FAIL	= 2;
	private static final int LOGIN_SUCC		= 3;
	private static final int LOGIN_FAIL		= 4;
	
	private Context		_context;
	
	public AccountSystem(Context context) {
		_context	= context;
	}
	
	public void register(final String url, final Map<String, String> params, final Callback callback) {
		final Handler handler	= new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case REGISTER_SUCC:
					callback.onComplete((JSONObject)msg.obj);
					break;
				case REGISTER_FAIL:
					callback.onError(msg.arg1);
					break;
				default:
					break;
				}
			}
		};

		new Thread(){
			public void run() {
				String response = NetUtil.httpPostRequest(url, params, 1);
				if(null == response) {
					SwitchLogger.e(LOG_TAG, "register response is null");
					handler.obtainMessage(REGISTER_FAIL, -1, -1).sendToTarget();
					return;
				}
				
				SwitchLogger.d(LOG_TAG, "register response="+response);
				try {
					JSONObject data	= new JSONObject(response);
					int ret	= data.getInt("ret");
					if(0 != ret) {
						SwitchLogger.e(LOG_TAG, "register fail, ret = " + ret);
						handler.obtainMessage(REGISTER_FAIL, ret, 0).sendToTarget();
						return ;
					}
					String uid	= data.getString("userid");
					String auth	= data.getString("auth");
					String pauth= data.getString("pauth");
					SwitchLogger.d(LOG_TAG, "register succ, uid="+uid+",auth="+auth+",pauth="+pauth);
					Account account	= new Account(uid, auth, pauth, Account.LOGIN_TYPE_VANCHU);
					AccountKeeper.saveAccount(_context, account);
					handler.obtainMessage(REGISTER_SUCC, data).sendToTarget();
				} catch (JSONException e) {
					SwitchLogger.e(e);
					handler.obtainMessage(REGISTER_FAIL, -1, -1).sendToTarget();
				}
			}
		}.start();
	}
	
	
	public void login(final String url,  final Map<String, String> params, final Callback callback) {
		final Handler handler	= new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case LOGIN_SUCC:
					callback.onComplete((JSONObject)msg.obj);
					break;
				case LOGIN_FAIL:
					callback.onError(msg.arg1);
					break;
				default:
					break;
				}
			}
		};
		
		new Thread(){
			public void run() {
				String response = NetUtil.httpPostRequest(url, params, 1);
				if(null == response) {
					SwitchLogger.e(LOG_TAG, "login response is null");
					handler.obtainMessage(LOGIN_FAIL, -1, -1).sendToTarget();
					return;
				}
				
				SwitchLogger.d(LOG_TAG, "login response="+response);
				try {
					JSONObject data	= new JSONObject(response);
					int ret	= data.getInt("ret");
					if(0 != ret) {
						SwitchLogger.e(LOG_TAG, "login fail, ret = " + ret);
						handler.obtainMessage(LOGIN_FAIL, ret, -1).sendToTarget();
						return ;
					}
					String uid	= data.getString("userid");
					String auth	= data.getString("auth");
					String pauth= data.getString("pauth");
					SwitchLogger.d(LOG_TAG, "login succ, uid="+uid+",auth="+auth+",pauth="+pauth);
					Account account	= new Account(uid, auth, pauth, Account.LOGIN_TYPE_VANCHU);
					AccountKeeper.saveAccount(_context, account);
					handler.obtainMessage(LOGIN_SUCC, data).sendToTarget();
				} catch (JSONException e) {
					SwitchLogger.e(e);
					handler.obtainMessage(LOGIN_FAIL, -1, -1).sendToTarget();
				}
			}
		}.start();
	}
	
	public void logout() {
		Account account	= new Account("", "", "", Account.LOGIN_TYPE_VANCHU);
		AccountKeeper.saveAccount(_context, account);
	}
	
	public boolean isLogon() {
		Account account	= AccountKeeper.fetchAccount(_context);
		if(account.isLogon()) {
			return true;
		} else {
			return false;
		}
	}
	
	public Account getAccount() {
		return AccountKeeper.fetchAccount(_context);
	}
	
	public interface Callback {
		public void onComplete(JSONObject responseJson);
		public void onError(int ret);
	}
}
