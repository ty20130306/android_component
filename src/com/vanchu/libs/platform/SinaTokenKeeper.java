package com.vanchu.libs.platform;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class SinaTokenKeeper {
	private static final String PREFS_SINA_TOKEN_KEEPER = "com_vanchu_libs_platform_sina_token_keeper";

	private static final String KEY_UID				= "uid";
	private static final String KEY_ACCESS_TOKEN	= "access_token";
	private static final String KEY_EXPIRE_TIME		= "expire_time";

	public static synchronized void save(Context context, SinaToken sinaToken) {
		if (null == context || null == sinaToken) {
			return;
		}

		SharedPreferences prefs = context.getSharedPreferences(PREFS_SINA_TOKEN_KEEPER, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(KEY_UID, sinaToken.getUid());
		editor.putString(KEY_ACCESS_TOKEN, sinaToken.getAccessToken());
		editor.putLong(KEY_EXPIRE_TIME, sinaToken.getExpireTime());
		editor.commit();
	}

	public static synchronized SinaToken fetch(Context context) {
		if (null == context) {
			return null;
		}

		SharedPreferences prefs = context.getSharedPreferences(PREFS_SINA_TOKEN_KEEPER, Context.MODE_PRIVATE);
		String uid	= prefs.getString(KEY_UID, "");
		String accessToken	= prefs.getString(KEY_ACCESS_TOKEN, "");
		long expireTime		= prefs.getLong(KEY_EXPIRE_TIME, 0);

		return new SinaToken(uid, accessToken, expireTime);
	}

	public static synchronized void clear(Context context) {
		if (null == context) {
			return;
		}

		SharedPreferences prefs = context.getSharedPreferences(PREFS_SINA_TOKEN_KEEPER, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.clear();
		editor.commit();
	}
	
	public static synchronized Oauth2AccessToken getOauth2AccessToken(Context context) {
		SinaToken sinaToken	= fetch(context);
		Oauth2AccessToken oauth2AccessToken	= new Oauth2AccessToken();
		oauth2AccessToken.setUid(sinaToken.getUid());
		oauth2AccessToken.setToken(sinaToken.getAccessToken());
		oauth2AccessToken.setExpiresTime(sinaToken.getExpireTime());
		
		return oauth2AccessToken;
	}
}
