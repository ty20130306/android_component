package com.vanchu.sample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.WeiboParameters;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.LogoutAPI;
import com.sina.weibo.sdk.openapi.UpdateAPI;
import com.sina.weibo.sdk.openapi.UploadUrlTextAPI;
import com.sina.weibo.sdk.utils.LogUtil;
import com.sina.weibo.sdk.utils.UIUtils;
import com.vanchu.libs.common.ui.Tip;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.platform.IPlatformListener;
import com.vanchu.libs.platform.PlatformFacotry;
import com.vanchu.libs.platform.PlatformSina;
import com.vanchu.libs.platform.SinaCfg;
import com.vanchu.libs.platform.SinaToken;
import com.vanchu.libs.platform.SinaTokenKeeper;
import com.vanchu.libs.platform.TencentToken;
import com.vanchu.libs.platform.TencentTokenKeeper;
import com.vanchu.test.R;
import com.vanchu.test.R.layout;
import com.vanchu.test.R.menu;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SinaSdkSampleActivity extends Activity {

	private static final String LOG_TAG	= SinaSdkSampleActivity.class.getSimpleName();
	
	private static final String APP_KEY	= "348238800";
	private static final String APP_SECRET	= "e2f09231a3f836c329dd14496554c025";
	private static final String REDIRECT_URL	= "http://app100670476.qzone.qzoneapp.com/mobi/login/weibo.ngi";
	
	// 闺蜜圈H5版所有scope
	private static final String	SCOPE	= "email,invitation_write,follow_app_official_microblog,friendships_groups_read";
	
	 /** 通过 code 获取 Token 的 URL */
    private static final String OAUTH2_ACCESS_TOKEN_URL = "https://open.weibo.cn/oauth2/access_token";
	
	/** 获取 Token 成功或失败的消息 */
    private static final int MSG_FETCH_TOKEN_SUCCESS = 1;
    private static final int MSG_FETCH_TOKEN_FAILED  = 2;
	
	private WeiboAuth			_weiboAuth			= null;
	private Oauth2AccessToken	_weiboAccessToken	= null;
	
	private String _weiboAuthCode;
	
	
	private PlatformSina	_platformSina;
	
	/**
     * 该 Handler 配合 {@link RequestListener} 对应的回调来更新 UI。
     */
    private Handler _handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_FETCH_TOKEN_SUCCESS:
                // 显示 Token
                String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                        new java.util.Date(_weiboAccessToken.getExpiresTime()));
                
                String showMsg	= "Token："+_weiboAccessToken.getToken()+",有效期："+date+",uid="
                				+_weiboAccessToken.getUid();
                SwitchLogger.d(LOG_TAG ,showMsg);
                Tip.show(SinaSdkSampleActivity.this, showMsg);
                SwitchLogger.d(LOG_TAG, "save token to AccessTokenKeeper" );
                SinaAccessTokenKeeper.writeAccessToken(SinaSdkSampleActivity.this, _weiboAccessToken);
                break;
                
            case MSG_FETCH_TOKEN_FAILED:
            	Tip.show(SinaSdkSampleActivity.this, "获取token失败" );
                break;
                
            default:
                break;
            }
        };
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sina_sdk_sample);
		
		_weiboAuth	= new WeiboAuth(this, APP_KEY, REDIRECT_URL, SCOPE);
		_weiboAccessToken	= SinaAccessTokenKeeper.readAccessToken(this);
		
		SinaCfg sinaCfg	= new SinaCfg(APP_KEY, APP_SECRET, REDIRECT_URL, SCOPE);
		_platformSina	= (PlatformSina)PlatformFacotry.createPlatform(this, PlatformFacotry.PLATFORM_TYPE_SINA, sinaCfg);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sina_sdk_sample, menu);
		return true;
	}

	public void libLogin(View v) {
		_platformSina.login(this, new IPlatformListener() {
			
			@Override
			public void onError() {
				SwitchLogger.e(LOG_TAG, "libLogin, onError");
			}
			
			@Override
			public void onComplete(JSONObject data) {
				SwitchLogger.d(LOG_TAG, "libLogin, onComplete");
				SinaToken sinaToken	= SinaTokenKeeper.fetch(SinaSdkSampleActivity.this);
				SwitchLogger.d(LOG_TAG, "libLogin, uid="+sinaToken.getUid()+",token="+sinaToken.getAccessToken());
			}
			
			@Override
			public void onCancel() {
				SwitchLogger.d(LOG_TAG, "libLogin, onCancel");
			}
		});
	}
	
	public void libLogout(View v) {
		_platformSina.logout();
	}
	
	public void libValidate(View v) {
		SinaToken sinaToken	= SinaTokenKeeper.fetch(SinaSdkSampleActivity.this);
		if(sinaToken.isTokenValid()) {
			SwitchLogger.d(LOG_TAG, "libValidate, token is valid, uid="+sinaToken.getUid()+",access_token="+sinaToken.getAccessToken());
		} else {
			SwitchLogger.d(LOG_TAG, "libValidate, token is not valid");
		}
	}
	
	public void login(View v) {
		 _weiboAuth.authorize(new AuthListener(), WeiboAuth.OBTAIN_AUTH_CODE);
	}

	private boolean isLogon() {
		return (null != _weiboAccessToken && _weiboAccessToken.isSessionValid());
	}
	
	public void logout(View v) {
		if(isLogon()) {
			new LogoutAPI(_weiboAccessToken).logout(new LogoutRequestListener());
		} else {
			String msg	= "未登陆或登陆态过期，请先登录";
			SwitchLogger.d(LOG_TAG, msg);
			Tip.show(this, msg);
		}
	}
	
	public void validate(View v) {
		String msg;
		if(isLogon()) {
			msg	= "已登录,uid="+_weiboAccessToken.getUid()+", token="+_weiboAccessToken.getToken();
			SwitchLogger.d(LOG_TAG, msg);
			Tip.show(this, msg);
		} else {
			msg	= "未登陆或登陆态过期，请先登录";
			SwitchLogger.d(LOG_TAG, msg);
			Tip.show(this, msg);
		}
	}
	
	private String getMsg() {
		EditText et	= (EditText)findViewById(R.id.msg);
		String msg	= et.getText().toString();
		if(msg.equals("")) {
			msg	= "测试微博<script>alert('xxx')</script>";
		}
		
		return msg;
	}
	
	public void libUpdate(View v) {
		String msg	= getMsg();
		_platformSina.share(msg, new IPlatformListener() {
			
			@Override
			public void onError() {
				SwitchLogger.e(LOG_TAG, "_platformSina.share text onError");
			}
			
			@Override
			public void onComplete(JSONObject data) {
				SwitchLogger.d(LOG_TAG, "_platformSina.share text onComplete");
			}
			
			@Override
			public void onCancel() {
				SwitchLogger.d(LOG_TAG, "_platformSina.share text onCancel");
			}
		});
	}
	
	public void update(View v) {
		String msg	= getMsg();
		if(isLogon()) {
			new UpdateAPI(_weiboAccessToken).publish(msg, new UpdateRequestListener());
		} else {
			String tip	= "token 失效，请先登录";
			Log.e("", tip);
			Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
			
			login(null);
		}
	}
	
	public void libUpload(View v) {
		String msg	= getMsg();
		_platformSina.share(msg, "http://app100670476.qzone.qzoneapp.com/images/header.jpg?ver=5811825", new IPlatformListener() {
			
			@Override
			public void onError() {
				SwitchLogger.e(LOG_TAG, "_platformSina.share text and url onError");
			}
			
			@Override
			public void onComplete(JSONObject data) {
				SwitchLogger.d(LOG_TAG, "_platformSina.share text and url onComplete");
			}
			
			@Override
			public void onCancel() {
				SwitchLogger.d(LOG_TAG, "_platformSina.share text and url onCancel");
			}
		});
	}
	
	public void upload(View v) {
		String msg	= getMsg();
		if(isLogon()) {
			new UploadUrlTextAPI(_weiboAccessToken).publish(msg, 
								"http://app100670476.qzone.qzoneapp.com/images/header.jpg?ver=5811825", 
								new UploadRequestListener());
		} else {
			String tip	= "token 失效，请先登录";
			Log.e("", tip);
			Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
			login(null);
		}
	}

	 /**
     * 发布图文微博请求的监听器（API请求结果的监听器）
     */
    private class UploadRequestListener implements RequestListener {

        @Override
        public void onComplete(String response) {
            LogUtil.d("", "upload Response: " + response);

            if (TextUtils.isEmpty(response) || response.contains("error_code")) {
                try {
                    JSONObject obj = new JSONObject(response);
                    String errorMsg = obj.getString("error");
                    String errorCode = obj.getString("error_code");
                    String message = "error_code: " + errorCode + "error_message: " + errorMsg;
                    LogUtil.e("", "upload Failed: " + message);
                    Toast.makeText(SinaSdkSampleActivity.this, message, Toast.LENGTH_SHORT).show();
                    
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
            	LogUtil.e("", "发布图文微博成功");
            	Toast.makeText(SinaSdkSampleActivity.this, "发布图文微博成功", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onComplete4binary(ByteArrayOutputStream responseOS) {
            // Do nothing
        }

        @Override
        public void onIOException(IOException e) {
            LogUtil.e("", e.getMessage());
        }

        @Override
        public void onError(WeiboException e) {
            LogUtil.e("", e.getMessage());
        }
    }

	 /**
     * 发布文字微博请求的监听器（API请求结果的监听器）
     */
    private class UpdateRequestListener implements RequestListener {

        @Override
        public void onComplete(String response) {
            LogUtil.d("", "update Response: " + response);

            if (TextUtils.isEmpty(response) || response.contains("error_code")) {
                try {
                    JSONObject obj = new JSONObject(response);
                    String errorMsg = obj.getString("error");
                    String errorCode = obj.getString("error_code");
                    String message = "error_code: " + errorCode + "error_message: " + errorMsg;
                    LogUtil.e("", "update Failed: " + message);
                    Toast.makeText(SinaSdkSampleActivity.this, message, Toast.LENGTH_SHORT).show();
                    
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
            	LogUtil.e("", "发布微博成功");
            	Toast.makeText(SinaSdkSampleActivity.this, "发布微博成功", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onComplete4binary(ByteArrayOutputStream responseOS) {
            // Do nothing
        }

        @Override
        public void onIOException(IOException e) {
            LogUtil.e("", e.getMessage());
        }

        @Override
        public void onError(WeiboException e) {
            LogUtil.e("", e.getMessage());
        }
    }
	
	 /**
     * 注销的监听器，接收注销处理结果。（API请求结果的监听器）
     */
    private class LogoutRequestListener implements RequestListener {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                try {
                    JSONObject obj = new JSONObject(response);
                    String value = obj.getString("result");
                    
                    if ("true".equalsIgnoreCase(value)) {
                        SinaAccessTokenKeeper.clear(SinaSdkSampleActivity.this);
                        _weiboAccessToken	= null;
                    }
                    
                   String msg	= "注销成功";
                   SwitchLogger.d(LOG_TAG, msg);
                   Tip.show(SinaSdkSampleActivity.this, msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        
        @Override
        public void onComplete4binary(ByteArrayOutputStream responseOS) {
        	SwitchLogger.e(LOG_TAG, "LogoutRequestListener.onComplete4binary");
        } 
        
        @Override
        public void onIOException(IOException e) {
        	SwitchLogger.e(LOG_TAG, "LogoutRequestListener.onIOException");
        	SwitchLogger.e(e);
        }

        @Override
        public void onError(WeiboException e) {
        	SwitchLogger.e(LOG_TAG, "LogoutRequestListener.onError");
        	SwitchLogger.e(e);
        }
    }
	
	 /**
     * 微博认证授权回调类。
     */
    private class AuthListener implements WeiboAuthListener {
        
        @Override
        public void onComplete(Bundle values) {
            if (null == values) {
            	SwitchLogger.e(LOG_TAG,  "obtain code failed");
                Toast.makeText(SinaSdkSampleActivity.this, "obtain code failed", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String code = values.getString("code");
            if (TextUtils.isEmpty(code)) {
            	SwitchLogger.e(LOG_TAG,  "obtain code failed");
                Toast.makeText(SinaSdkSampleActivity.this, "obtain code failed", Toast.LENGTH_SHORT).show();
                return;
            }
            
            _weiboAuthCode = code;
            SwitchLogger.d(LOG_TAG, "fetch auth code succ, code="+code);
            Toast.makeText(SinaSdkSampleActivity.this, "fetch auth code succ, code="+code, Toast.LENGTH_SHORT).show();
            
            fetchTokenAsync(_weiboAuthCode, APP_SECRET);
        }

        @Override
        public void onCancel() {
        	SwitchLogger.e(LOG_TAG,  "onCancel, fetch auth code cancelled");
            Toast.makeText(SinaSdkSampleActivity.this, 
            		"onCancel, fetch auth code cancelled", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
        	SwitchLogger.e(LOG_TAG,  "onCancel, fetch auth code exception occur");
            UIUtils.showToast(SinaSdkSampleActivity.this, 
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }
    
    /**
     * 异步获取 Token。
     * 
     * @param authCode  授权 Code，该 Code 是一次性的，只能被获取一次 Token
     * @param appSecret 应用程序的 APP_SECRET，请务必妥善保管好自己的 APP_SECRET，
     *                  不要直接暴露在程序中，此处仅作为一个DEMO来演示。
     */
    public void fetchTokenAsync(String authCode, String appSecret) {
        WeiboParameters requestParams = new WeiboParameters();
        requestParams.add(WBConstants.AUTH_PARAMS_CLIENT_ID,     APP_KEY);
        requestParams.add(WBConstants.AUTH_PARAMS_CLIENT_SECRET, appSecret);
        requestParams.add(WBConstants.AUTH_PARAMS_GRANT_TYPE,    "authorization_code");
        requestParams.add(WBConstants.AUTH_PARAMS_CODE,          authCode);
        requestParams.add(WBConstants.AUTH_PARAMS_REDIRECT_URL,  REDIRECT_URL);
    
        /**
         * 请注意：
         * {@link RequestListener} 对应的回调是运行在后台线程中的，
         * 因此，需要使用 Handler 来配合更新 UI。
         */
        AsyncWeiboRunner.request(OAUTH2_ACCESS_TOKEN_URL, requestParams, "POST", new RequestListener() {
            @Override
            public void onComplete(String response) {
                LogUtil.d(LOG_TAG, "Response: " + response);
                
                // 获取 Token 成功
                Oauth2AccessToken token = Oauth2AccessToken.parseAccessToken(response);
                if (token != null && token.isSessionValid()) {
                    LogUtil.d(LOG_TAG, "Success! " + token.toString());
                    
                    _weiboAccessToken = token;
                    _handler.obtainMessage(MSG_FETCH_TOKEN_SUCCESS).sendToTarget();
                } else {
                    LogUtil.d(LOG_TAG, "Failed to receive access token");
                }
            }
    
            @Override
            public void onComplete4binary(ByteArrayOutputStream responseOS) {
                LogUtil.e(LOG_TAG, "onComplete4binary...");
                _handler.obtainMessage(MSG_FETCH_TOKEN_FAILED).sendToTarget();
            }
    
            @Override
            public void onIOException(IOException e) {
                LogUtil.e(LOG_TAG, "onIOException： " + e.getMessage());
                _handler.obtainMessage(MSG_FETCH_TOKEN_FAILED).sendToTarget();
            }
    
            @Override
            public void onError(WeiboException e) {
                LogUtil.e(LOG_TAG, "WeiboException： " + e.getMessage());
                _handler.obtainMessage(MSG_FETCH_TOKEN_FAILED).sendToTarget();
            }
        });
    }
}
