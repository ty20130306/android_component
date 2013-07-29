
功能特性：
1：支持自定义推送消息
2：支持后端调整配置推送消息轮询间隔，间隔限制如下
	public static final int MIN_MSG_INTERVAL		= 300000;	// milliseconds = 5 minutes
	public static final int DEFAULT_MSG_INTERVAL	= 3600000;	// milliseconds = 1 hour
	public static final int MAX_MSG_INTERVAL		= 86400000;	// milliseconds = 1 day

3：监听ACTION_USER_PRESENT，手机解锁自动拉起推送服务


--------------------------------------------------------------------------------------

使用说明：
一：使用升级组件需要的权限
	<uses-permission android:name="android.permission.INTERNET" />
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	<uses-permission android:name="android.permission.GET_TASKS"/>
	<uses-permission android:name="android.permission.WAKE_LOCK" />
	<uses-permission android:name="android.permission.VIBRATE" />

二：修改Manifest.xml 文件中增加receiver节点和service节点
eg：
<!-- 推送模块 -->
<service android:name="com.vanchu.test.TestPushService" />
<receiver android:name="com.vanchu.test.TestPushBroadcastReceiver" >
	<intent-filter >
		<action android:name="android.intent.action.USER_PRESENT" />
	</intent-filter>
</receiver>

三：代码实现
1：继承PushService，根据具体业务实现下边抽象函数
	abstract protected int getNotificationIcon(int msgType);	// 根据消息类型返回icon id
	abstract protected void onNotificationClick(int msgType);	// 根据消息类型处理推送点击动作

2：继承PushBroadcastReceiver，根据具体业务实现下边抽象函数
	abstract protected Class<?> getServiceClass();	// 返回具体实现的service的类

3：在代码启动的地方调用启动代码
	eg：PushRobot.run(this, TestPushService.class, pushParam);

