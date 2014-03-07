package com.vanchu.libs.platform;

import org.json.JSONObject;

public interface IPlatformListener {
	public void onComplete(JSONObject data);
	public void onError();
	public void onCancel();
}
