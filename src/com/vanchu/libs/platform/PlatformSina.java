package com.vanchu.libs.platform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
import com.vanchu.libs.common.util.SwitchLogger;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

public class PlatformSina implements IPlatformBase {
	
	private static final String LOG_TAG	= PlatformSina.class.getSimpleName();
	
	private static final String OAUTH2_ACCESS_TOKEN_URL = "https://open.weibo.cn/oauth2/access_token";
	
	/** 获取 Token 成功或失败的消息 */
    private static final int FETCH_TOKEN_SUCC		= 1;
    private static final int FETCH_TOKEN_FAIL		= 2;
	
	
	private Context		_context;
	private SinaCfg		_sinaCfg;
	
	public PlatformSina(Context context, SinaCfg sinaCfg) {
		_context	= context;
		_sinaCfg	= sinaCfg;
	}
	
	 /**
     * 异步获取 Token。
     * 
     * @param authCode  授权 Code，该 Code 是一次性的，只能被获取一次 Token
     * @param appSecret 应用程序的 APP_SECRET，请务必妥善保管好自己的 APP_SECRET，
     *                  不要直接暴露在程序中，此处仅作为一个DEMO来演示。
     */
    private void fetchTokenAsync(String authCode, String appSecret, final IPlatformListener listener) {
        WeiboParameters requestParams = new WeiboParameters();
        requestParams.add(WBConstants.AUTH_PARAMS_CLIENT_ID,     _sinaCfg.getAppKey());
        requestParams.add(WBConstants.AUTH_PARAMS_CLIENT_SECRET, appSecret);
        requestParams.add(WBConstants.AUTH_PARAMS_GRANT_TYPE,    "authorization_code");
        requestParams.add(WBConstants.AUTH_PARAMS_CODE,          authCode);
        requestParams.add(WBConstants.AUTH_PARAMS_REDIRECT_URL,  _sinaCfg.getRedirectUrl());
    
        final Handler handler	= new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		switch (msg.what) {
				case FETCH_TOKEN_SUCC:
					listener.onComplete(null);
					break;

				case FETCH_TOKEN_FAIL:
					listener.onError();
					break;
				default:
					break;
				}
        	}
        };
        
        /**
         * 请注意：
         * {@link RequestListener} 对应的回调是运行在后台线程中的，
         * 因此，需要使用 Handler 来配合更新 UI。
         */
        AsyncWeiboRunner.request(OAUTH2_ACCESS_TOKEN_URL, requestParams, "POST", new RequestListener() {
            @Override
            public void onComplete(String response) {
                SwitchLogger.d(LOG_TAG, "fetch oauth token Response: " + response);
                
                // 获取 Token 成功
                Oauth2AccessToken token = Oauth2AccessToken.parseAccessToken(response);
                if (token != null && token.isSessionValid()) {
                    SwitchLogger.d(LOG_TAG, "fetch oauth token success,token=" + token.toString());
                    
                    String uid	= token.getUid();
                    String accessToken	= token.getToken();
                    long expireTime		= token.getExpiresTime();
                    SwitchLogger.d(LOG_TAG, "save SinaToken to keeper,uid="+uid+",accessToken="+accessToken
                    						+",expireTime="+expireTime);
                    SinaToken sinaToken	= new SinaToken(uid, accessToken, expireTime);
                	SinaTokenKeeper.save(_context, sinaToken);
                	
                    handler.obtainMessage(FETCH_TOKEN_SUCC).sendToTarget();
                } else {
                    SwitchLogger.d(LOG_TAG, "failed to parse oauth token");
                    handler.obtainMessage(FETCH_TOKEN_FAIL).sendToTarget();
                }
            }
    
            @Override
            public void onComplete4binary(ByteArrayOutputStream responseOS) {
                SwitchLogger.e(LOG_TAG, "onComplete4binary...");
                handler.obtainMessage(FETCH_TOKEN_FAIL).sendToTarget();
            }
    
            @Override
            public void onIOException(IOException e) {
                SwitchLogger.e(e);
                handler.obtainMessage(FETCH_TOKEN_FAIL).sendToTarget();
            }
    
            @Override
            public void onError(WeiboException e) {
                SwitchLogger.e(e);
                handler.obtainMessage(FETCH_TOKEN_FAIL).sendToTarget();
            }
        });
    }

	@Override
	public void login(Activity activity, final IPlatformListener listener) {
		WeiboAuth weiboAuth	= new WeiboAuth(_context, _sinaCfg.getAppKey(), 
											_sinaCfg.getRedirectUrl(), _sinaCfg.getScope());

		WeiboAuthListener weiboAuthListener	= new WeiboAuthListener() {
			@Override
			public void onComplete(Bundle values) {
				if (null == values) {
					SwitchLogger.e(LOG_TAG,  "obtain sina auth code failed");
					listener.onError();
					return;
				}
				
				String authCode = values.getString("code");
				if (TextUtils.isEmpty(authCode)) {
					SwitchLogger.e(LOG_TAG,  "obtain sina auth code failed, auth code is empty");
					listener.onError();
					return;
				}
				
				SwitchLogger.d(LOG_TAG, "fetch sina auth code succ, auth code="+authCode);
				fetchTokenAsync(authCode, _sinaCfg.getAppSecret(), listener);
			}

			@Override
			public void onCancel() {
				SwitchLogger.d(LOG_TAG,  "onCancel, fetch auth code cancelled");
				listener.onCancel();
			}

			@Override
			public void onWeiboException(WeiboException e) {
				SwitchLogger.e(LOG_TAG,  "onWeiboException, fetch auth code exception occur");
				listener.onError();
			}
		};
		
		weiboAuth.authorize(weiboAuthListener, WeiboAuth.OBTAIN_AUTH_CODE);
	}
	
	@Override
	public void logout() {
		Oauth2AccessToken oauth2AccessToken	= SinaTokenKeeper.getOauth2AccessToken(_context);
		if(oauth2AccessToken.isSessionValid()) {
			new LogoutAPI(oauth2AccessToken).logout(new BaseRequestListener(null){
				@Override
				public void doComplete(String response) {
					SwitchLogger.d(LOG_TAG, "LogoutRequestListener.onComplete");
		        	
		            if (!TextUtils.isEmpty(response)) {
		                try {
		                    JSONObject obj = new JSONObject(response);
		                    String value = obj.getString("result");
		                    
		                    if ("true".equalsIgnoreCase(value)) {
		                    	SwitchLogger.d(LOG_TAG, "sina logout succ");
		                    } else {
		                    	SwitchLogger.d(LOG_TAG, "sina logout fail");
		                    }
		                } catch (JSONException e) {
		                	SwitchLogger.e(e);
		                }
		            }
				}
			});
		} 
		
		SinaTokenKeeper.clear(_context);
	}
	
	private void doShare(Oauth2AccessToken token, String msg, final IPlatformListener listener) {
		new UpdateAPI(token).publish(msg, new BaseRequestListener(listener) {
			@Override
			public void doComplete(String response) {
				SwitchLogger.d(LOG_TAG, "share text(update), response: " + response);

				if (TextUtils.isEmpty(response) || response.contains("error_code")) {
					try {
						JSONObject obj = new JSONObject(response);
						String errorMsg = obj.getString("error");
						String errorCode = obj.getString("error_code");
						String message = "error_code: " + errorCode + "error_message: " + errorMsg;
						SwitchLogger.e(LOG_TAG, "share text(update) failed: " + message);
						listener.onError();
					} catch (JSONException e) {
						SwitchLogger.e(e);
						listener.onError();
					}
				} else {
					SwitchLogger.d(LOG_TAG, "share text(update) succ");
					listener.onComplete(null);
				}
			}
		});
	}
	
	public void share(final String msg, final IPlatformListener listener) {
		Oauth2AccessToken token	= SinaTokenKeeper.getOauth2AccessToken(_context);
		if(token.isSessionValid()) {
			doShare(token, msg, listener);
		} else {
			login(null, new IPlatformListener() {
				
				@Override
				public void onError() {
					listener.onError();
				}
				
				@Override
				public void onComplete(JSONObject data) {
					Oauth2AccessToken newtoken	= SinaTokenKeeper.getOauth2AccessToken(_context);
					doShare(newtoken, msg, listener);
				}
				
				@Override
				public void onCancel() {
					listener.onCancel();
				}
			});
		}
	}
	
	private void doShare(Oauth2AccessToken token, String msg, String imgUrl, final IPlatformListener listener) {
		new UploadUrlTextAPI(token).publish(msg, imgUrl, new BaseRequestListener(listener) {
			@Override
			public void doComplete(String response) {
				SwitchLogger.d(LOG_TAG, "share text(update), response: " + response);

				if (TextUtils.isEmpty(response) || response.contains("error_code")) {
					try {
						JSONObject obj = new JSONObject(response);
						String errorMsg = obj.getString("error");
						String errorCode = obj.getString("error_code");
						String message = "error_code: " + errorCode + "error_message: " + errorMsg;
						SwitchLogger.e(LOG_TAG, "share text and img url(upload_url_text) failed: " + message);
						listener.onError();
					} catch (JSONException e) {
						SwitchLogger.e(e);
						listener.onError();
					}
				} else {
					SwitchLogger.d(LOG_TAG, "share text and img url(upload_url_text) succ");
					listener.onComplete(null);
				}
			}
		});
	}
	
	public void share(final String msg, final String imgUrl, final IPlatformListener listener) {
		Oauth2AccessToken token	= SinaTokenKeeper.getOauth2AccessToken(_context);
		if(token.isSessionValid()) {
			doShare(token, msg, listener);
		} else {
			login(null, new IPlatformListener() {
				
				@Override
				public void onError() {
					listener.onError();
				}
				
				@Override
				public void onComplete(JSONObject data) {
					Oauth2AccessToken newtoken	= SinaTokenKeeper.getOauth2AccessToken(_context);
					doShare(newtoken, msg, imgUrl, listener);
				}
				
				@Override
				public void onCancel() {
					listener.onCancel();
				}
			});
		}
	}
	
	@Override
	public IToken getToken() {
		return SinaTokenKeeper.fetch(_context);
	}
	
	 /**
     * API请求结果的监听器基类
     */
    private class BaseRequestListener implements RequestListener {
    	
    	private IPlatformListener listener	= null;
    	
    	public BaseRequestListener(IPlatformListener listener) {
    		this.listener	= listener;
    	}
    	
    	public void doComplete(String response) {
    		
    	}
    	
        @Override
        public void onComplete(String response) {
        	SwitchLogger.d(LOG_TAG, "RequestListener.onComplete");
        	doComplete(response);
        }
        
        @Override
        public void onComplete4binary(ByteArrayOutputStream responseOS) {
        	SwitchLogger.d(LOG_TAG, "RequestListener.onComplete4binary");
        	if(null != listener) {
        		listener.onError();
        	}
        } 
        
        @Override
        public void onIOException(IOException e) {
        	SwitchLogger.d(LOG_TAG, "RequestListener.onIOException");
        	SwitchLogger.e(e);
        	if(null != listener) {
        		listener.onError();
        	}
        }

        @Override
        public void onError(WeiboException e) {
        	SwitchLogger.d(LOG_TAG, "RequestListener.onError");
        	SwitchLogger.e(e);
        	if(null != listener) {
        		listener.onError();
        	}
        }
    }
	
}
