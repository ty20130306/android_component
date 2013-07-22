package com.vanchu.libs.upgrade;

public class UpgradeResult {
	public static final int RESULT_LATEST_VERSION		= 0;
	public static final int RESULT_SKIP_UPGRADE			= 1;
	public static final int RESULT_INSTALL_STARTED		= 2;
	
	public static final int RESULT_ERROR_TRY_IT			= 3;
	public static final int RESULT_ERROR_SKIPPABLE		= 4;
	public static final int RESULT_ERROR_FATAL			= 5;
}
