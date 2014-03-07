package com.vanchu.test.wxapi;

import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.vanchu.libs.common.util.SwitchLogger;
import com.vanchu.test.R;
import com.vanchu.test.R.layout;
import com.vanchu.test.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

	private IWXAPI _api;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String WX_APP_ID = "wxb8b030415c4db030";
		_api = WXAPIFactory.createWXAPI(this, WX_APP_ID, false);
		_api.handleIntent(getIntent(), this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wxentry, menu);
		return true;
	}

	@Override
	public void onReq(BaseReq req) {
		SwitchLogger.d("WXEntryActivity", "onReq transaction:" + req.transaction);
	}
	
	@Override
	public void onResp(BaseResp resp) {
		SwitchLogger.d("WXEntryActivity", "onResp errCode:" + resp.errCode + " errStr:" + resp.errStr + " transaction:" + resp.transaction);
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			SwitchLogger.d("WXEntryActivity", "----------weixin share succ"); // 微信分享成功
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			SwitchLogger.d("WXEntryActivity", "----------weixin share cancel"); // 微信分享取消
			break;
		default:
			SwitchLogger.d("WXEntryActivity", "----------weixin share fail"); // 微信分享失败
			break;
		}
		finish();
	}
}
