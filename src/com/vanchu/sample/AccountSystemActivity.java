package com.vanchu.sample;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.vanchu.libs.accountSystem.Account;
import com.vanchu.libs.accountSystem.AccountKeeper;
import com.vanchu.libs.accountSystem.AccountSystem;
import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.NetUtil;
import com.vanchu.libs.common.util.StringUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.test.R;
import com.vanchu.test.R.layout;
import com.vanchu.test.R.menu;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class AccountSystemActivity extends Activity {

	private static final String LOG_TAG	= AccountSystemActivity.class.getSimpleName();
	
	
	private static final String HOST	= "http://test.gmq.apps.vanchu.cn";
	private static final int REGISTER_SUCC	= 1;
	private static final int REGISTER_FAIL	= 2;
	private static final int LOGIN_SUCC		= 3;
	private static final int LOGIN_FAIL		= 4;
	
	
	private String _uid;
	private String _auth;
	private String _pauth;
	
	private AccountSystem	_accountSystem;
	
	private Handler _handler	= new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REGISTER_SUCC:
				SwitchLogger.d(LOG_TAG, "register succ, uid="+_uid+",auth="+_auth+",pauth="+_pauth);
				break;
			case REGISTER_FAIL:
				SwitchLogger.e(LOG_TAG, "register fail");
				break;
			case LOGIN_SUCC:
				SwitchLogger.d(LOG_TAG, "login succ, uid="+_uid+",auth="+_auth+",pauth="+_pauth);
				break;
			case LOGIN_FAIL:
				SwitchLogger.e(LOG_TAG, "login fail");
				break;

			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_system);
		_accountSystem	= new AccountSystem(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.account_system, menu);
		return true;
	}

	private String getInput(int id) {
		EditText et		= (EditText)findViewById(id);
		String input	= et.getText().toString();
		return input;
	}
	
	private String getNicknameFromMail(String email) {
		String nameAddr[]	= email.split("@");
		return nameAddr[0];
	}
	
	private boolean isLegalPassword(String password) {
		if(password.length() < 6) {
			return false;
		} else {
			return true;
		}
	}
	
	public void validateEmail(View v) {
		String email	= getInput(R.id.email);
		if(StringUtil.isLegalEmail(email)) {
			SwitchLogger.d(LOG_TAG, "email legal, email="+email);
			String nickname	= getNicknameFromMail(email);
			SwitchLogger.d(LOG_TAG, "nickname from email="+nickname);
		} else {
			SwitchLogger.d(LOG_TAG, "email not legal, email="+email);
		}
	}
	
	public void validatePhoneNumber(View v) {
		String phoneNumber	= getInput(R.id.phone_number);
		if(StringUtil.isLegalPhoneNumber(phoneNumber)) {
			SwitchLogger.d(LOG_TAG, "phone number legal, phone number="+phoneNumber);
		} else {
			SwitchLogger.d(LOG_TAG, "phone number not legal, phone number="+phoneNumber);
		}
	}
	
	public void register(View v) {
		String email	= getInput(R.id.email);
		String password	= getInput(R.id.password);
		
		if(! StringUtil.isLegalEmail(email) || ! isLegalPassword(password)) {
			SwitchLogger.e(LOG_TAG, "input invalid,email="+email+",password="+password );
			Tip.show(this, "输入无效");
			return ;
		}
		
		String nickname	= getNicknameFromMail(email);
		final HashMap<String, String> params	= new HashMap<String, String>();
		params.put("email", email);
		params.put("nickname", nickname);
		params.put("password", password);
		new Thread(){
			public void run() {
				String response = NetUtil.httpPostRequest(HOST+"/mobi/account/register.json", params, 1);
				if(null == response) {
					SwitchLogger.e(LOG_TAG, "register response is null");
					_handler.sendEmptyMessage(REGISTER_FAIL);
					return;
				}
				
				SwitchLogger.d(LOG_TAG, "register response="+response);
				try {
					JSONObject data	= new JSONObject(response);
					int ret	= data.getInt("ret");
					if(0 != ret) {
						SwitchLogger.d(LOG_TAG, "register fail, ret = " + ret);
						_handler.sendEmptyMessage(REGISTER_FAIL);
						return ;
					}
					_uid	= data.getString("userid");
					_auth	= data.getString("auth");
					_pauth	= data.getString("pauth");
					
					_handler.sendEmptyMessage(REGISTER_SUCC);
				} catch (JSONException e) {
					SwitchLogger.e(e);
					_handler.sendEmptyMessage(REGISTER_FAIL);
				}
			}
		}.start();
		
	}
	
	public void login(View v) {
		String email	= getInput(R.id.email);
		String password	= getInput(R.id.password);
		
		if(email.equals("") || password.equals("") || ! StringUtil.isLegalEmail(email) || ! isLegalPassword(password)) {
			SwitchLogger.e(LOG_TAG, "input invalid,email="+email+",password="+password );
			Tip.show(this, "输入无效");
			return ;
		}

		final HashMap<String, String> params	= new HashMap<String, String>();
		params.put("email", email);
		params.put("password", password);
		new Thread(){
			public void run() {
				String response = NetUtil.httpPostRequest(HOST+"/mobi/login/native.json", params, 1);
				if(null == response) {
					SwitchLogger.e(LOG_TAG, "login response is null");
					_handler.sendEmptyMessage(LOGIN_FAIL);
					return;
				}
				
				SwitchLogger.d(LOG_TAG, "login response="+response);
				try {
					JSONObject data	= new JSONObject(response);
					int ret	= data.getInt("ret");
					if(0 != ret) {
						SwitchLogger.d(LOG_TAG, "login fail, ret = " + ret);
						_handler.sendEmptyMessage(LOGIN_FAIL);
						return ;
					}
					_uid	= data.getString("userid");
					_auth	= data.getString("auth");
					_pauth	= data.getString("pauth");
					
					_handler.sendEmptyMessage(LOGIN_SUCC);
				} catch (JSONException e) {
					SwitchLogger.e(e);
					_handler.sendEmptyMessage(LOGIN_FAIL);
				}
			}
		}.start();
	}
	
	public void registerLib(View v) {
		String email	= getInput(R.id.email);
		String password	= getInput(R.id.password);
		
		if(! StringUtil.isLegalEmail(email) || ! isLegalPassword(password)) {
			SwitchLogger.e(LOG_TAG, "input invalid,email="+email+",password="+password );
			Tip.show(this, "输入无效");
			return ;
		}
		
		String nickname	= getNicknameFromMail(email);
		final HashMap<String, String> params	= new HashMap<String, String>();
		params.put("email", email);
		params.put("nickname", nickname);
		params.put("password", password);
		_accountSystem.register(HOST+"/mobi/account/register.json", params, new AccountSystem.Callback() {
			
			@Override
			public void onError(int ret) {
				SwitchLogger.e(LOG_TAG, "lib register failed");
			}
			
			@Override
			public void onComplete(JSONObject responseJson) {
				SwitchLogger.d(LOG_TAG, "onComplete, lib register succ,responseJson="+responseJson.toString());
				Account account	= _accountSystem.getAccount();
				SwitchLogger.d(LOG_TAG, "uid="+account.getUid()+",auth="+account.getAuth()+",pauth="+account.getPauth());
			}
		});
	}

	public void loginLib(View v) {
		String email	= getInput(R.id.email);
		String password	= getInput(R.id.password);
		
		if(email.equals("") || password.equals("") || ! StringUtil.isLegalEmail(email) || ! isLegalPassword(password)) {
			SwitchLogger.e(LOG_TAG, "input invalid,email="+email+",password="+password );
			Tip.show(this, "输入无效");
			return ;
		}

		final HashMap<String, String> params	= new HashMap<String, String>();
		params.put("email", email);
		params.put("password", password);
		
		
		_accountSystem.login(HOST+"/mobi/login/native.json", params, new AccountSystem.Callback() {
			
			@Override
			public void onError(int ret) {
				SwitchLogger.e(LOG_TAG, "lib login failed");
			}
			
			@Override
			public void onComplete(JSONObject responseJson) {
				SwitchLogger.d(LOG_TAG, "onComplete, lib login succ,responseJson="+responseJson.toString());
				Account account	= _accountSystem.getAccount();
				SwitchLogger.d(LOG_TAG, "uid="+account.getUid()+",auth="+account.getAuth()+",pauth="+account.getPauth());
			}
		});
	}
	
	public void logoutLib(View v) {
		_accountSystem.logout();
	}
	
	public void validateLib(View v) {
		Account account	= _accountSystem.getAccount();
		if(account.isLogon()) {
			SwitchLogger.d(LOG_TAG, "has logon, uid="+account.getUid()+",auth="+account.getAuth()+",pauth="+account.getPauth());
		} else {
			SwitchLogger.d(LOG_TAG, "not logon");
		}
	}
	
	public void getInfoLib(View v) {
		final Account account	= _accountSystem.getAccount();
		new Thread(){
			public void run() {
				final HashMap<String, String> params	= new HashMap<String, String>();
				params.put("auth", account.getAuth());
				params.put("pauth", account.getPauth());
					
				String response = NetUtil.httpPostRequest(HOST+"/mobi/v1/entry.json", params, 1);
				if(null == response) {
					SwitchLogger.e(LOG_TAG, "getInfo response is null");
					return;
				}
				
				SwitchLogger.d(LOG_TAG, "getInfo response="+response);
				try {
					JSONObject data	= new JSONObject(response);
					int ret	= data.getInt("ret");
					if(0 != ret) {
						SwitchLogger.e(LOG_TAG, "getInfo fail, ret = " + ret);
						return ;
					}
				} catch (JSONException e) {
					SwitchLogger.e(e);
				}
			}
		}.start();
	}
}
