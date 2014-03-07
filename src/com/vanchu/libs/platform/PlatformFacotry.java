package com.vanchu.libs.platform;

import android.content.Context;

public class PlatformFacotry {

	public static final int PLATFORM_TYPE_TENCENT	= 1;
	public static final int PLATFORM_TYPE_SINA		= 2;
	
	public static IPlatformBase createPlatform(Context context, int platformType, IPlatformCfg platformCfg) {
		switch (platformType) {
		case PLATFORM_TYPE_TENCENT:
			return new PlatformTencent(context, (TencentCfg)platformCfg);
		case PLATFORM_TYPE_SINA:
			return new PlatformSina(context, (SinaCfg)platformCfg);
		default:
			return null;
		}
	}
}
