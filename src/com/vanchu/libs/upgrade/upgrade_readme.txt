
功能特性：
1：下载位置支持sdcard和手机内存
2：下载前支持提前判断空间是否足够
3：支持断点续传
4：网络超时重试
5：非强制性更新失败不影响老版本的app的正常进行

--------------------------------------------------------------------------------------

使用说明：
一：使用升级组件需要的权限（请注意：一定要加上下边所有权限，否则在某些机器上（比如小米1,1s）会导致升级失败）
	<uses-permission android:name="android.permission.INTERNET" />
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
	<uses-permission android:name="android.permission.RESTART_PACKAGES" />
	<uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES"/>
	

三：调用接口使用说明
注意：当前版本的获取是从AndroidManifest.xml的android:versionName中获取，请按具体版本设置该属性的值

1：UpgradeManager 使用举例

	public void checkUpgradeManager(View v){
		VanchuLogger.setPrintLog(true);
		VanchuLogger.d(LOG_TAG, "checkUpgradeManager");
		
		UpgradeParam param	= new UpgradeParam(
			UpgradeUtil.getCurrentVersionName(this), 
			"1.0.3",
			"1.0.7", 
			"http://pesiwang.devel.rabbit.oa.com/component.1.0.4.apk",
			"升级详细内容"
		);
		
		new UpgradeManager(this, param, new UpgradeCallback(this)).check();

	}

2：UpgradeProxy使用举例

	public void upgradeProxy(View view){
		VanchuLogger.setPrintLog(true);
		VanchuLogger.d(LOG_TAG, "checkUpgradeProxy");

		new UpgradeProxy(
				this,
				"http://pesiwang.devel.rabbit.oa.com/t.php",
				new UpgradeCallback(this)).check();
		
	}

3：自定义UpgradeCallback使用举例

	public void upgradeProxy(View view){
		VanchuLogger.setPrintLog(true);
		VanchuLogger.d(LOG_TAG, "checkUpgradeProxy");
		
		class MyCallback extends UpgradeCallback {
			
			public MyCallback(Context context){
				super(context);
			}
			
			@Override
			public UpgradeParam onUpgradeInfoResponse(String response){
				try {
					JSONObject jsonResponse	= new JSONObject(response);
					String lowest	= jsonResponse.getString("lowest");
					String highest	= jsonResponse.getString("highest");
					String url		= jsonResponse.getString("apkUrl");
					String detail	= jsonResponse.getString("detail");
					
					VanchuLogger.d(LOG_TAG, "receive info, lowest version:" + lowest + ", highest version: " + highest);
					VanchuLogger.d(LOG_TAG, "receive info, apkUrl: " + url + ", detail: " + detail);
					
					String current	= UpgradeUtil.getCurrentVersionName(getContext());
					VanchuLogger.d(LOG_TAG, "current version: " + current);
					
					return new UpgradeParam(current, lowest, highest, url, detail);
				} catch(JSONException e){
					if(VanchuLogger.isPrintLog()){
						VanchuLogger.e(e);
					}
					return null;
				}
			}
		}
		
		new UpgradeProxy(
				this,
				"http://pesiwang.devel.rabbit.oa.com/t.php",
				new MyCallback(this)).check();
		
	}


--------------------------------------------------------------------------------------

自测检查：
1：测试sdcard和手机内存存储选择的代码是否删除
2：测试断点续传代码是否删除
3：测试空间不够的代码是否删除
4：测试网络超时的代码是否删除


