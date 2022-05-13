# FlowLite

最新版下载链接：https://vkceyugu.cdn.bspapp.com/VKCEYUGU-02c40445-b5cc-470a-8632-1d38855dcc18/8cf86fd3-4e92-4d69-afc5-b9dd4b1738ed.apk

#### 介绍
简单的使用原生实现联通流量监控查询
因为内置抓包模块涉及部分商用代码暂不公开，公开代码中屏蔽了这个功能。
需要的同学可以去我仓库：https://github.com/nongchengqi/NetWorkPacketCapture 查看抓包核心代码

不使用任何三方库
2022.5.13 增加悬浮窗ip和更新时间展示，分别使用[IP]和[时]格式表示
2022.4.12 重构更新，基本支持套餐识别。

支持悬浮窗、支持小组件

#### 软件架构
软件架构说明


#### 目录说明

1.  float  悬浮窗代码
2.  ui  用户界面
3.  utils 工具类，获取ip和流量包解析的等都在这
4.  wiget 桌面小组件 

#### 使用说明

1.  套餐识别不正确怎么办？

所有套餐都是按照联通官方进行默认分组，首次个性化分组需要长按对应流量包移动

2.  移动流量包后显示跳点负数怎么办？

首次移动流量包会出现跳点数据负数的情况，请到配置页点击一次清除（不是点击每日清除开关），刷新即可

3.  悬浮窗怎么设置

需要在配置页打开使用悬浮窗开关，然后具体配置看配置页


#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
