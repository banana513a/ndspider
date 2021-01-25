package com.ndcf.spider.crawler.executor.siteconfig;

/**
 * 单例，后期配置在配置文件或者数据库中；
 */
public class CralwerExample2Config extends CralwerConfig {

    // 需要初始化的参数
    public static String URL = "http://www.example2.com/";
    public static String CHARSET = "UTF-8";
//    public static String regUrlConStr = "\\S*(?:" + SITE_NAME + ")((?!css|js|:void)\\S)*";
    public static int MAX_TIMES = 2000;
    public static int THIS_TIMES_CNT = 20;
    public static Boolean proxyWhether = false;
    public static int timeOut = 10000;

    private static volatile CralwerExample2Config instance = null;



    protected CralwerExample2Config(String URL, String regUrlConStr , int MAX_TIMES, boolean proxyWhether, long THIS_TIMES_CNT) {
        super(URL, regUrlConStr, MAX_TIMES, proxyWhether, THIS_TIMES_CNT);
        super.init();
    }


    private CralwerExample2Config() {

    }

    public static CralwerConfig getSingleCralwerConfig() {
        if (instance == null) {
            synchronized (CralwerExample2Config.class) {
                if (instance == null) {
                    instance=new CralwerExample2Config( URL,null,MAX_TIMES ,proxyWhether,THIS_TIMES_CNT );
                }
            }
        }
        return instance;
    }





}
