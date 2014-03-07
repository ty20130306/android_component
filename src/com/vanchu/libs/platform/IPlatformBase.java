package com.vanchu.libs.platform;

import android.app.Activity;

public interface IPlatformBase {
	public void login(Activity activity, IPlatformListener listener);
	public void logout();
	public IToken getToken();
}
