
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
	/**
	 * 根据消息类型返回icon id
	 * @param msgType	消息类型
	 * @return
	 */
	abstract protected int getNotificationIcon(int msgType);
	
	/**
	 * 根据消息类型实现推送消息的点击动作
	 * @param msgType	消息类型
	 */
	abstract protected void onNotificationClick(int msgType, Bundle msgExtra);

2：继承PushBroadcastReceiver，根据具体业务实现下边抽象函数
	abstract protected Class<?> getServiceClass();	// 返回具体实现的service的类

3：在代码启动的地方调用启动代码
	eg：PushRobot.run(this, TestPushService.class, pushParam);

四：推送消息字段解析
数据字段：
data	=> array(
	type	=> 推送消息类型(整型)
	ticker	=> 手机顶栏提示文字(字符串)
	title	=> 消息标题(字符串)
	text	=> 消息正文(字符串)
	show	=> 收到推送是否提示(0/1)
	extra	=> 消息额外需要字段 (json对象)
)

配置字段：
cfg		=> array(
	interval	=> 获取推送消息间隔，单位为毫秒
	delay		=> 上次退出应用后，至少经过了多少时间才允许显示推送消息，单位为毫秒
	after		=> 上次显示推送消息后，至少经过了多少时间才允许显示推送消息，单位为毫秒
	avaiStartTime	=> 每日允许推送开始时间（格式为：hhmm，比如早上8点表示为 0800）
	avaiEndTime		=> 每日允许推送开始时间（格式为：hhmm，比如晚上10点表示为 2200）
)