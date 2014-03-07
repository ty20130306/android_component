package com.vanchu.libs.platform;

import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.vanchu.libs.common.util.BitmapUtil;
import com.vanchu.libs.common.util.SwitchLogger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

public class PlatformTencent implements IPlatformBase {
	
	private static final String LOG_TAG	= PlatformTencent.class.getSimpleName();
	
	private Context		_context;
	private TencentCfg	_tencentCfg;
	
	private Tencent		_tencent;
	
	public PlatformTencent(Context context, TencentCfg tencentCfg) {
		_context	= context;
		_tencentCfg	= tencentCfg;
		
		_tencent	= Tencent.createInstance(_tencentCfg.getAppId(), _context.getApplicationContext());
		TencentToken tencenToken	= TencentTokenKeeper.fetch(_context);
		if(tencenToken.isTokenValid()) {
			_tencent.setOpenId(tencenToken.getOpenId());
			_tencent.setAccessToken(tencenToken.getAccessToken(), tencenToken.getExpireIn());
		}
	}
	
	private TencentToken parseLoginResponse(JSONObject response) throws JSONException {
		String pf		= response.getString("pf");
		String pfKey	= response.getString("pfkey");
		String appId	= "";
		if(response.has("appid")) {
			appId	= response.getString("appid");
		}
		String openId	= response.getString("openid");
		String accessToken	= response.getString("access_token");
		String payToken		= response.getString("pay_token");
		String expireIn		= response.getString("expires_in");
		TencentToken tencentToken	= new TencentToken(pf, pfKey, appId, openId, accessToken, payToken, expireIn);
		
		return tencentToken;
	}
	
	@Override
	public void login(Activity activity, final IPlatformListener listener) {
		IUiListener uiListener = new BaseUiListener() {
			@Override
			public void onComplete(JSONObject response) {
				super.onComplete(response);
		        try {
		        	int ret	= response.getInt("ret");
		        	if(0 != ret) {
		        		listener.onError();
		        		return ;
		        	}
		        	
		        	/**
		        	 *  成功返回
		        	 *  {"ret":"0","pay_token":"71FDB3CA39809FE065371E98544A6780",
		        	 * 			"pf":"openmobile_android","sendinstall":"0","appid":"100645243",
		        	 * 			"openid":"FA02A93F7B075BB621D4455AA7FC8663","expires_in":"7776000",
		        	 * 			"pfkey":"dab779a9e8d641e675fcc3e14a5b599a",
		        	 * 			"access_token":"C5DC236900B05FD6A5428EF9F4F6660D"}
		        	 */
		        	TencentToken tencentToken	= parseLoginResponse(response);
		        	TencentTokenKeeper.save(_context, tencentToken);
		        	listener.onComplete(null);
		        	return ;
				} catch (JSONException e) {
					SwitchLogger.e(e);
					listener.onError();
				}
			}
			
			@Override
			public void onError(UiError err) {
				super.onError(err);
				listener.onError();
			}
			
			@Override
			public void onCancel() {
				super.onCancel();
				listener.onCancel();
			}
        };
        
        _tencent.login(activity, _tencentCfg.getScope(), uiListener);
	}
	
	public void logout() {
		_tencent.logout(_context);
		TencentTokenKeeper.clear(_context);
	}
	
	public void shareToQq(Activity activity, TencentShareToQqParam param, final IPlatformListener listener) {
		Bundle paramBundle	= param.getParamBundle();
		_tencent.shareToQQ(activity, paramBundle, new BaseUiListener(){
			@Override
			public void onComplete(JSONObject response) {
				SwitchLogger.d(LOG_TAG, "shareToQQ, onComplete");
				listener.onComplete(response);
			}

			@Override
			public void onError(UiError e) {
				SwitchLogger.e(LOG_TAG, "shareToQQ, onError code:" + e.errorCode + ", msg:"
						+ e.errorMessage + ", detail:" + e.errorDetail);
				listener.onError();
			}
			
			@Override
			public void onCancel() {
				SwitchLogger.d(LOG_TAG, "shareToQQ, onCancel");
				listener.onCancel();
			}
		}); 
	}

	private void doSendStory(Activity activity, TencentSendStoryParam param, final IPlatformListener listener) {
		Bundle paramBundle	= param.getParamBundle();
		_tencent.story(activity, paramBundle, new BaseUiListener(){
			@Override
			public void onComplete(JSONObject response) {
				SwitchLogger.d(LOG_TAG, "sendStory, onComplete");
				listener.onComplete(response);
			}

			@Override
			public void onError(UiError e) {
				SwitchLogger.e(LOG_TAG, "sendStory, onError code:" + e.errorCode + ", msg:"
						+ e.errorMessage + ", detail:" + e.errorDetail);
				listener.onError();
			}
			
			@Override
			public void onCancel() {
				SwitchLogger.d(LOG_TAG, "sendStory, onCancel");
				listener.onCancel();
			}
        });
	}
	
	public void sendStory(final Activity activity, final TencentSendStoryParam param, final IPlatformListener listener) {
		TencentToken tencenToken	= TencentTokenKeeper.fetch(_context);
		if(tencenToken.isTokenValid()) {
			doSendStory(activity, param, listener);
		} else {
			login(activity, new IPlatformListener() {
				
				@Override
				public void onError() {
					listener.onError();
				}
				
				@Override
				public void onComplete(JSONObject data) {
					doSendStory(activity, param, listener);
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
		return TencentTokenKeeper.fetch(_context);
	}
	
	private static int getWxScene(IWXAPI api) {
		if (api.getWXAppSupportAPI() >= 0x21020001) {// ,0x21020001及以上支持发送朋友圈
			return SendMessageToWX.Req.WXSceneTimeline;// 消息会发送至朋友圈
		} else {
			return SendMessageToWX.Req.WXSceneSession;// 消息会发送至微信的会话内
		}
	}
	
	public static boolean shareToWx(Context context, WxShareParam param) {
		IWXAPI api	= WXAPIFactory.createWXAPI(context, param.getAppId(), true);
		api.registerApp(param.getAppId());
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = param.getTargetUrl();
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = param.getTitle();
		msg.description = param.getDesc();
		Bitmap pic	= param.getPic();
		if(null != pic) {
			msg.thumbData	= BitmapUtil.getBytesFromBitmap(pic);
		}
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		req.scene = param.isToCircle() ? getWxScene(api) : SendMessageToWX.Req.WXSceneSession;
		boolean isOk = api.sendReq(req);
		if (isOk) {
			SwitchLogger.d(LOG_TAG, "wx sendReq succ");
			return true;
		} else {
			SwitchLogger.d(LOG_TAG, "wx sendReq fail");
			return false;
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		_tencent.onActivityResult(requestCode, resultCode, data);
	}
	
	private class BaseUiListener implements IUiListener {

		@Override
		public void onComplete(JSONObject response) {
			SwitchLogger.d(LOG_TAG, "onComplete, response="+response.toString());
		}

		@Override
		public void onError(UiError e) {
			SwitchLogger.e(LOG_TAG, "onError, msg="+e.errorMessage+",code="+e.errorCode+",detail="+e.errorDetail);
		}

		@Override
		public void onCancel() {
			SwitchLogger.d(LOG_TAG, "onCancel");
		}
	}
}
