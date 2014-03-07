package com.vanchu.libs.accountSystem;

import android.content.Context;
import android.content.SharedPreferences;

public class AccountKeeper {
	private static final String PREFS_ACCOUNT_KEEPER	= "com_vanchu_libs_account_system_account_keeper";
	
	private static final String KEY_UID		= "uid";
    private static final String KEY_AUTH	= "auth";
    private static final String KEY_PAUTH	= "pauth";
    private static final String KEY_LOGIN_TYPE	= "login_type";
	
	public static void saveAccount(Context context, Account account) {
		SharedPreferences prefs	= context.getSharedPreferences(PREFS_ACCOUNT_KEEPER, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor	= prefs.edit();
		editor.putString(KEY_UID, account.getUid());
		editor.putString(KEY_AUTH, account.getAuth());
		editor.putString(KEY_PAUTH, account.getPauth());
		editor.putInt(KEY_LOGIN_TYPE, account.getLoginType());
		editor.commit();
	}
	
	public static Account fetchAccount(Context context) {
		SharedPreferences prefs	= context.getSharedPreferences(PREFS_ACCOUNT_KEEPER, Context.MODE_PRIVATE);
		String uid		= prefs.getString(KEY_UID, "");
		String auth		= prefs.getString(KEY_AUTH, "");
		String pauth	= prefs.getString(KEY_PAUTH, "");
		int loginType	= prefs.getInt(KEY_LOGIN_TYPE, Account.LOGIN_TYPE_VANCHU);
		return new Account(uid, auth, pauth, loginType);
	}
}
