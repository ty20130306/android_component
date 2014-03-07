package com.vanchu.libs.platform;

import android.content.Context;
import android.content.SharedPreferences;

public class TencentTokenKeeper {
	private static final String PREFS_TENCENT_TOKEN_KEEPER	= "com_vanchu_libs_platform_tencent_token_keeper";

	private static final String KEY_PF		= "pf";
    private static final String KEY_PF_KEY	= "pf_key";
    private static final String KEY_APP_ID	= "app_id";
    private static final String KEY_OPEN_ID	= "open_id";
    private static final String KEY_ACCESS_TOKEN	= "access_token";
    private static final String KEY_PAY_TOKEN		= "pay_token";
    private static final String KEY_EXPIRE_IN		= "expire_in";
    
	public static synchronized void save(Context context, TencentToken tencentToken) {
		SharedPreferences prefs	= context.getSharedPreferences(PREFS_TENCENT_TOKEN_KEEPER, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor	= prefs.edit();
		editor.putString(KEY_PF, tencentToken.getPf());
		editor.putString(KEY_PF_KEY, tencentToken.getPfKey());
		editor.putString(KEY_APP_ID, tencentToken.getAppId());
		editor.putString(KEY_OPEN_ID, tencentToken.getOpenId());
		editor.putString(KEY_ACCESS_TOKEN, tencentToken.getAccessToken());
		editor.putString(KEY_PAY_TOKEN, tencentToken.getPayToken());
		editor.putString(KEY_EXPIRE_IN, tencentToken.getExpireIn());
		editor.commit();
	}
	
	public static synchronized TencentToken fetch(Context context) {
		SharedPreferences prefs	= context.getSharedPreferences(PREFS_TENCENT_TOKEN_KEEPER, Context.MODE_PRIVATE);
		String pf		= prefs.getString(KEY_PF, "");
		String pfKey	= prefs.getString(KEY_PF_KEY, "");
		String appId	= prefs.getString(KEY_APP_ID, "");
		String openId	= prefs.getString(KEY_OPEN_ID, "");
		String accessToken	= prefs.getString(KEY_ACCESS_TOKEN, "");
		String payToken		= prefs.getString(KEY_PAY_TOKEN, "");
		String expireIn		= prefs.getString(KEY_EXPIRE_IN, "");
		
		return new TencentToken(pf, pfKey, appId, openId, accessToken, payToken, expireIn);
	}
	
	public static synchronized void clear(Context context) {
		SharedPreferences prefs	= context.getSharedPreferences(PREFS_TENCENT_TOKEN_KEEPER, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor	= prefs.edit();
		editor.clear();
		editor.commit();
	}
}

