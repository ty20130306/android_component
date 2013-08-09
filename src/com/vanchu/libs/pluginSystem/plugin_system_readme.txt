
功能特性：
1：自动判断插件是否安装，未安装则提示下载安装
2：自动判断插件是否需要升级，若需要升级，则自动提示升级
3：插件显示可配置
4：插件显示UI剥离

--------------------------------------------------------------------------------------



使用说明：

一：插件编写注意

1：在manifest.xml的入口activity属性中增加android:exported="true"，否则插件系统没有权限启动插件
关于 android:exported, http://developer.android.com/guide/topics/manifest/activity-element.html
android:exported
Whether or not the activity can be launched by components of other applications — "true" if it can be, 
and "false" if not. If "false", the activity can be launched only by components of the same application or 
applications with the same user ID.
The default value depends on whether the activity contains intent filters. The absence of any filters 
means that the activity can be invoked only by specifying its exact class name. 
This implies that the activity is intended only for application-internal use 
(since others would not know the class name). So in this case, the default value is "false". 
On the other hand, the presence of at least one filter implies that the activity is intended 
for external use, so the default value is "true".

This attribute is not the only way to limit an activity's exposure to other applications. 
You can also use a permission to limit the external entities that can invoke the activity 
(see the permission attribute).

2：注释掉入口activity的启动属性，保证插件安装后不显示图标
<action android:name="android.intent.action.MAIN" />
<category android:name="android.intent.category.LAUNCHER" />

二：代码接入
1：PluginSystem，插件系统的入口
使用举例如下

启动插件系统：
_ps	= new PluginSystem(this, 
				"http://pesiwang.devel.rabbit.oa.com/test_plugin_system.php", 
				new MyPluginSystemCallback());
		
_ps.run();

停止插件系统（注意，一定要调用stop停止插件系统，否则会内存泄露）：
_ps.stop();

2：PluginSystemCallback，插件系统的回调
主要回调函数解释：
public void onPluginInfoReady(PluginInfoManager pluginInfoManager)
当插件信息可获取的时候触发，在这个回调中可以实现UI的初始化

public void onPluginInfoChange(PluginInfoManager pluginInfoManager)
当插件信息有变化的时候触发，在这个回调中可以实现UI的更新
比如：比如新安装有插件时，插件状态要从未安装到已安装

3：PluginManager，插件管理器，用于启动插件，卸载插件，更新插件等等

4：PluginManagerCallback，插件管理器的回调，主要用于异步下载安装的回调
