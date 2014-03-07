package com.vanchu.sample;

import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.tencent.tauth.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.vanchu.libs.common.util.BitmapUtil;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.libs.platform.IPlatformListener;
import com.vanchu.libs.platform.PlatformFacotry;
import com.vanchu.libs.platform.PlatformTencent;
import com.vanchu.libs.platform.TencentCfg;
import com.vanchu.libs.platform.TencentSendStoryParam;
import com.vanchu.libs.platform.TencentShareToQqParam;
import com.vanchu.libs.platform.TencentToken;
import com.vanchu.libs.platform.TencentTokenKeeper;
import com.vanchu.libs.platform.WxShareParam;
import com.vanchu.test.R;
import com.vanchu.test.R.layout;
import com.vanchu.test.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class QqSdkSampleActivity extends Activity {

	private static final String LOG_TAG	= QqSdkSampleActivity.class.getSimpleName();
	
	private static final String APP_ID	= "100645243";
	/**
	 * 猜歌王使用的scope
	 */
	private final static String SCOPE = "get_app_friends,get_simple_userinfo,get_clip,add_pic_t,upload_pic,add_topic";
	
	private Tencent _tencent	= null;
	private TencentSession	_tencentSession	= null;
	
	
	private PlatformTencent _platformTencent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qq_sdk_sample);
		
		_tencent	= Tencent.createInstance(APP_ID, getApplicationContext());
		TencentCfg tencentCfg	= new TencentCfg(APP_ID, SCOPE);
		_platformTencent	= (PlatformTencent)PlatformFacotry.createPlatform(this, PlatformFacotry.PLATFORM_TYPE_TENCENT, tencentCfg);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.qq_sdk_sample, menu);
		return true;
	}
	
	private void saveTencentSession(JSONObject values) {
		SwitchLogger.d(LOG_TAG, "saveTencentSession, values="+values.toString());
        try {
			String pf		= values.getString("pf");
			String pfKey	= values.getString("pfkey");
			String appId	= values.getString("appid");
			String openId	= values.getString("openid");
			String accessToken	= values.getString("access_token");
			String payToken		= values.getString("pay_token");
			String expireIn		= values.getString("expires_in");
			_tencentSession	= new TencentSession(pf, pfKey, appId, openId, accessToken, payToken, expireIn);
			SwitchLogger.d(LOG_TAG, "save tencent session done" );
		} catch (JSONException e) {
			SwitchLogger.e(e);
		}
	}
	
	public void login(View v) {
		IUiListener listener = new BaseUiListener() {
            @Override
            protected void doComplete(JSONObject values) {
            	SwitchLogger.d(LOG_TAG, "login, doComplete, values="+values.toString());
            	saveTencentSession(values);
            }
        };
        _tencent.login(this, SCOPE, listener);
	}
	
	public void logout(View v) {
		_tencent.logout(this);
		_tencentSession	= null;
	}
	
	private boolean ready() {
        boolean ready = _tencent.isSessionValid()
                && _tencent.getOpenId() != null;
        if (!ready)
            Toast.makeText(this, "login and get openId first, please!",
                    Toast.LENGTH_SHORT).show();
        return ready;
    }

	private boolean tencentSessionValid() {
		return (null !=_tencentSession && ! _tencentSession.hasExpired());
	}
	
	public void validate(View v) {
		if( ! tencentSessionValid()) {
			SwitchLogger.d(LOG_TAG, "no valid tencent session, try to login ");
			login(null);
			return;
		}
		
		IUiListener listener = new BaseUiListener() {
			@Override
			protected void doComplete(JSONObject values) {
				SwitchLogger.d(LOG_TAG, "validate, doComplete, values="+values.toString());
				try {
					String msg	= values.getString("msg");
					if(msg.equals("") ) {
						SwitchLogger.d(LOG_TAG, "session still valid" );
					} else {
						SwitchLogger.d(LOG_TAG, "session not valid" );
						saveTencentSession(values);
					}
				} catch (JSONException e) {
					SwitchLogger.e(e);
				}
			}
		};
		_tencent.setOpenId(_tencentSession.getOpenId());
		_tencent.setAccessToken(_tencentSession.getAccessToken(), _tencentSession.getExpireIn());
		_tencent.login(this, SCOPE, listener);
	}

	public void shareToQq(View v) {
		Bundle params	= new Bundle();
		params.putString(Constants.PARAM_TITLE, "磨时光--无聊神器");
		params.putString(Constants.PARAM_IMAGE_URL, "http://m.bangyouxi.com/images/m1_top.jpg?v=201310141");
		params.putString(Constants.PARAM_TARGET_URL, "http://m.bangyouxi.com");
		params.putString(Constants.PARAM_SUMMARY, "坐公交，坐地铁，上厕所必备神器");
		params.putString(Constants.PARAM_APP_SOURCE, "冷兔大学堂"+APP_ID);
		params.putString(Constants.PARAM_APPNAME, "冷兔大学堂");
        
		_tencent.shareToQQ(this, params, new BaseUiListener(){
			protected void doComplete(JSONObject values) {
				SwitchLogger.d(LOG_TAG, "shareToQQ, onComplete");
			}

			@Override
			public void onError(UiError e) {
				SwitchLogger.e(LOG_TAG, "shareToQQ, onError code:" + e.errorCode + ", msg:"
						+ e.errorMessage + ", detail:" + e.errorDetail);
			}
			
			@Override
			public void onCancel() {
				SwitchLogger.d(LOG_TAG, "shareToQQ, onCancel");
			}
		}); 
	}
	
	public void invite(View v) {
		if(ready()) {
			 Bundle params = new Bundle();
             params.putString(Constants.PARAM_APP_ICON, "http://imgcache.qq.com/qzone/space_item/pre/0/66768.gif");
             params.putString(Constants.PARAM_APP_DESC, "AndroidSdk_1_3: invite description!");
             params.putString(Constants.PARAM_ACT, "进入应用");
             _tencent.invite(this, params, new BaseUiListener(){
            	 @Override
            	protected void doComplete(JSONObject values) {
            		SwitchLogger.d(LOG_TAG, "invite, doComplete" );
            	}
             });
		}
	}

	public void sendStory(View v) {
		 Bundle params = new Bundle();

         params.putString(Constants.PARAM_TITLE, "AndroidSdk_1_3:UiStory title");
         params.putString(Constants.PARAM_COMMENT, "AndroidSdk_1_3: UiStory comment");
         params.putString(Constants.PARAM_IMAGE, "http://imgcache.qq.com/qzone/space_item/pre/0/66768.gif");
         params.putString(Constants.PARAM_SUMMARY, "AndroidSdk_1_3: UiStory summary");
         params.putString( Constants.PARAM_PLAY_URL,
                 		"http://player.youku.com/player.php/Type/Folder/Fid/15442464/Ob/1/Pt/0/sid/XMzA0NDM2NTUy/v.swf");
         params.putString(Constants.PARAM_ACT, "进入应用");
         String[] receiver = {
                 "121345674896845AGHIHOGVJOASJ", "GISFHOPGJOEJUGO4513587422"
         };
         params.putStringArray(Constants.PARAM_RECEIVER, receiver);
         _tencent.story(this, params, new BaseUiListener(){
        	 @Override
        	protected void doComplete(JSONObject values) {
        		 SwitchLogger.d(LOG_TAG, "sendStory, doComplete" );
        	}
         });
	}
	
	private int getScene(IWXAPI api) {
		if (api.getWXAppSupportAPI() >= 0x21020001) {// ,0x21020001及以上支持发送朋友圈
			return SendMessageToWX.Req.WXSceneTimeline;// 消息会发送至朋友圈
		} else {
			return SendMessageToWX.Req.WXSceneSession;// 消息会发送至微信的会话内
		}
	}
	
	private void shareToWx(boolean toCircle) {
		String WX_APP_ID = "wxb8b030415c4db030";
		IWXAPI api	= WXAPIFactory.createWXAPI(this, WX_APP_ID, true);
		api.registerApp(WX_APP_ID);
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = "http://qcf.vanchu.com";
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = "标题：七彩坊";
		msg.description = "描述：开始疯狂填字之旅吧";
		Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		msg.thumbData = BitmapUtil.getBytesFromBitmap(thumb);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		req.scene = toCircle ? getScene(api) : SendMessageToWX.Req.WXSceneSession;
		boolean isOK = api.sendReq(req);
		if (isOK) {
			SwitchLogger.d(LOG_TAG, "sendReq成功！");
		} else {
			SwitchLogger.d(LOG_TAG, "sendReq失败！");
		}
	}
	
	public void shareToWxFriend(View v) {
		shareToWx(false);
	}
	
	public void shareToWxCircle(View v) {
		shareToWx(true);
	}
	
	
	public void libShareToWxFriend(View v) {
		Bitmap bm	= BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		WxShareParam param	= new WxShareParam("wxb8b030415c4db030", "标题：七彩坊", 
												"描述：开始疯狂填字之旅吧", 
												"http://qcf.vanchu.com", 
												bm, 
												false);
		PlatformTencent.shareToWx(this, param);
	}
	
	public void libShareToWxCircle(View v) {
		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		WxShareParam param	= new WxShareParam("wxb8b030415c4db030", "标题：七彩坊", 
				"描述：开始疯狂填字之旅吧", 
				"http://qcf.vanchu.com", 
				bm, 
				true);
		PlatformTencent.shareToWx(this, param);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 

		SwitchLogger.d(LOG_TAG, "onActivityResult,requestCode:" + requestCode + ", resultCode:"+resultCode);
		// must call mTencent.onActivityResult.
		if (_tencent == null) {
			return;
		}
		_tencent.onActivityResult(requestCode, resultCode, data);
		
		_platformTencent.onActivityResult(requestCode, resultCode, data);
	}
	
	
	public void libLogin(View v) {
		_platformTencent.login(this, new IPlatformListener() {
			
			@Override
			public void onError() {
				SwitchLogger.e(LOG_TAG, "lib login error");
			}
			
			@Override
			public void onComplete(JSONObject data) {
				TencentToken ttoken	= TencentTokenKeeper.fetch(QqSdkSampleActivity.this);
				SwitchLogger.d(LOG_TAG, "lib login succ, openId="+ttoken.getOpenId()+",access_token="+ttoken.getAccessToken());
			}
			
			@Override
			public void onCancel() {
				SwitchLogger.d(LOG_TAG, "lib login cancel");
			}
		});
	}
	
	public void libLogout(View v) {
		_platformTencent.logout();
		TencentTokenKeeper.clear(this);
	}
	
	public void libValidate(View v) {
		TencentToken ttoken	= TencentTokenKeeper.fetch(QqSdkSampleActivity.this);
		if(ttoken.isTokenValid()) {
			SwitchLogger.d(LOG_TAG, "libValidate, token is valid, openId="+ttoken.getOpenId()+",access_token="+ttoken.getAccessToken());
		} else {
			SwitchLogger.d(LOG_TAG, "libValidate, token is not valid");
		}
	}
	
	public void libShareToQq(View v) {
		TencentShareToQqParam param	= new TencentShareToQqParam("http://qcf.vanchu.com/", "七彩坊", "http://app100670476.qzone.qzoneapp.com/images/header.jpg?ver=5811825");
		_platformTencent.shareToQq(this, param, new IPlatformListener() {
			
			@Override
			public void onError() {
				SwitchLogger.e(LOG_TAG, "libShareToQq error");
			}
			
			@Override
			public void onComplete(JSONObject data) {
				SwitchLogger.d(LOG_TAG, "libShareToQq succ");
			}
			
			@Override
			public void onCancel() {
				SwitchLogger.d(LOG_TAG, "libShareToQq cancel");
			}
		});
	}
	
	public void libSendStory(View v) {
		TencentSendStoryParam param	= new TencentSendStoryParam("七彩坊", "http://app100670476.qzone.qzoneapp.com/images/header.jpg?ver=5811825");
		_platformTencent.sendStory(this, param, new IPlatformListener() {
			
			@Override
			public void onError() {
				SwitchLogger.e(LOG_TAG, "libSendStory error");
			}
			
			@Override
			public void onComplete(JSONObject data) {
				SwitchLogger.d(LOG_TAG, "libSendStory succ");
			}
			
			@Override
			public void onCancel() {
				SwitchLogger.d(LOG_TAG, "libSendStory cancel");
			}
		});
	}
	
	private class BaseUiListener implements IUiListener {

		@Override
		public void onComplete(JSONObject response) {
			SwitchLogger.d(LOG_TAG, "login, onComplete, response="+response);
			doComplete(response);
		}

		protected void doComplete(JSONObject values) {

		}

		@Override
		public void onError(UiError e) {
			SwitchLogger.e(LOG_TAG, "login, onError, msg="+e.errorMessage+",code="+e.errorCode+",detail="+e.errorDetail);
		}

		@Override
		public void onCancel() {
			SwitchLogger.d(LOG_TAG, "login, onCancel");
		}
	}
	
	private class TencentSession {
		private String pf;
		private String pfKey;
		private String appId;
		private String openId;
		private String accessToken;
		private String payToken;
		private String expireIn;
		private long expireAt;
		
		public TencentSession(String pf, String pfKey, String appId, String openId, 
								String accessToken, String payToken, String expireIn) {
		
			this.pf		= pf;
			this.pfKey	= pfKey;
			this.appId	= appId;
			this.openId	= openId;
			this.accessToken	= accessToken;
			this.payToken		= payToken;
			this.expireIn		= expireIn;
			
			this.expireAt	= System.currentTimeMillis() + Long.parseLong(expireIn) * 1000;
		}
		
		public String getPf() {
			return pf;
		}
		
		public String getPfKey() {
			return pfKey;
		}
		
		public String getAppId() {
			return appId;
		}
		
		public String getOpenId() {
			return openId;
		}
		
		public String getAccessToken() {
			return accessToken;
		}
		
		public String getPayToken() {
			return payToken;
		}
		
		public String getExpireIn() {
			return expireIn;
		}
		
		public boolean hasExpired() {
			return (System.currentTimeMillis() > expireAt);
		}
	}
}
