# JAVA爬虫（可配置站点快速爬取，支持自动切换代理IP，支持htmlunit、httpclient等切换）
#  https://github.com/banana513a/ndspider
## 1、 遍历网页内所有的url并筛选符合条件的url放入队列。
## 2、 频率控制，次数及时间，每天每个IP访问次数限制在100次内，且设置cookie和随机睡眠；每个IP不要多线程并发爬；爬虫增加IP代理、伪装UA、AI识别;数据抓取的过程中使用伪造device_id绕过服务器的身份校验，使用伪造UA及IP绕过服务器的访问频率限制;
## 3、 代理线程池，（更新代理IpConfigManager.idleIpNodes ，然后通知注册的监听方法RefreshIpListener.onRefreshIpCompelete；）;代理IP自动切换；每个IP每天达到设定的指定次数或者失败指定次数自动切换。
## 4、 可配置Config,快速爬取通用的网站（公开不需要登陆验证等）。
## 5、 纯Java，没有依赖数据库（后期考虑用Mysql、Mongodb、redis）；


# 启动说明：
##1、配置
###	1.1、站点配置：

	src/main/java/com/ndcf/spider/crawler/executor/siteconfig/CralwerExample1Config.java （继承src/main/java/com/ndcf/spider/crawler/executor/siteconfig/CralwerConfig.java）
```
	    // 继承CralwerConfig，需要初始化的参数
	    public static String URL = "http://www.example1.com/";  // 设置需要爬取的站点url，（遍历网页内所有的url并筛选符合条件的url放入队列。）
	    //    public static String regUrlConStr = "\\S*(?:" + SITE_NAME + ")((?!css|js|:void)\\S)*";   //遍历网页内所有的url的过滤正则,不填则不过滤
	    public static String CHARSET = "UTF-8";  // 默认编码，实际会根据爬取网页的http header返回的编码自动设置编码格式；
	    public static int MAX_TIMES = 2000; // 当天该站点最大次数
	    public static int THIS_TIMES_CNT = 20; // 本次启动爬取的次数（默认乘10），因为涉及所有url，包括css,js,图片，所以，thisTimeCnt *= 10;,
	    public static Boolean proxyWhether = false;  // 是否启用代理，不启用默认用本地IP：IpNode("127.0.0.1", "9999")，
	    public static int timeOut = 10000;  // 超时时间(单位：毫秒),
```
	src/main/java/com/ndcf/spider/crawler/executor/SingleHtmlUnitConfigFactory.java （在类变量CRAWLER_CONFIG_MAP注册对应的站点配置CralwerConfig继承类比如CralwerExample1Config.java）
###	1.2、保存文件夹路径以及反射包路径配置:
	src/main/java/com/ndcf/spider/crawler/common/Constant/HtmlUnitConstant.java
###	1.3、代理IP设置
	src/main/java/com/ndcf/spider/crawler/IpConfigManager.java中初始化或动态添加；
	初始化： addProxyIpOnStatic() 中添加初始化的代理IP，注意默认需添加new IpNode("127.0.0.1", "9999")，这里把本地IP也当作代理IP便于与代理IP统一处理;
	
## 2、启动
启动类：src/main/java/com/ndcf/spider/NdspiderApplication.java 或者 src/main/java/com/ndcf/spider/crawler/executor/Startor.java

## 保存的资源路径：
G:\202011\sitename\files -- 爬取站点的资源路径
G:\202011\sitename\siteconfig -- 爬取站点的url相关信息
G:\202011\conmonConfig -- 站点的代理IP统计信息


# 待

## 1、日志，
## 2、数据一致性以及宕机处理；
## 3、短信异常通知，
## 4、后期考虑用Mysql、Mongodb、redis，目前是把站点所有url加载到内存中，爬取大站点后期可能比较吃内存。
## 5、保存路径自动区分windows和linux文件路径，
## 6、切日的测试，相关站点次数和代理IP的；
#
