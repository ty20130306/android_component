
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

