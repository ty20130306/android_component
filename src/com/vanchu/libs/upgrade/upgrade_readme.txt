
使用特别注意：
1：android:versionName 的值必须是含有2个小数点的值：x.y.z ；y,z都是一位数，满9向前进1 ;
例如：	当前版本是1.0.9 ，下一版本是1.1.0；
		当前版本是1.9.9，下一个版本就是2.0.0；
		当前版本是9.9.9 下一个版本就是10.0.0 
		以此类推。。
versioncode的值为：将versionName小数点换成0，例如版本是1.0.0,versionCode = "10000"
风险：后两个小数点后必须是1位数，否则有可能高版本会比低版本低的情况，比如1.1.0 (10100)与 1.0.10(101010)
这样的话就会导致更新不成功了

2：每个版本的apk的下载url的最后边的文件名必须是唯一的，即带有版本号标示的文件，否则会导致
在短点续传和免重复下载的功能中，导致下载文件错乱或者无法下载成功
正确：http://cdn.bangyouxi.com/app1101073753/apk/112/guimiquan_1.0.2_1001001.apk
错误：http://d.guimi.vanchu.com/download.php?channel=1001001
风险：每个版本的apk的下载url的最后边的文件名必须是唯一，否则断点续传中，如果上次a版本下载到一半，
下次下载b版本，url一样，会导致使用a版本的头，b版本文件的尾，导致apk文件错乱；或者不会真正下载，因为url
相同，导致保存到本地的文件的命名相同，导致以为是下载过的

--------------------------------------------------------------------------------------

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


