package com.ndcf.spider.crawler.executor.siteconfig;

/**
 * 单例，后期配置在配置文件或者数据库中；
 */
public class CralwerExample1Config extends CralwerConfig {

    // 需要初始化的参数
    public static String URL = "http://www.example1.com/";
    public static String CHARSET = "UTF-8";
//    public static String regUrlConStr = "\\S*(?:" + SITE_NAME + ")((?!css|js|:void)\\S)*";
    public static int MAX_TIMES = 2000;
    public static int THIS_TIMES_CNT = 20;
    public static Boolean proxyWhether = false;
    public static int timeOut = 10000;




    protected CralwerExample1Config(String URL, String regUrlConStr , int MAX_TIMES, boolean proxyWhether, long THIS_TIMES_CNT) {
        super(URL, regUrlConStr, MAX_TIMES, proxyWhether, THIS_TIMES_CNT);
        super.init();
    }

//    @Override
//    String handleUrlOnFind(String url ) {
//

//
//        return url;
//    }


    public static CralwerConfig getSingleCralwerConfig() {
        return Inner.INSTACE;
    }


    private final static class Inner {
        private final static CralwerConfig INSTACE = new CralwerExample1Config( URL,null,MAX_TIMES ,proxyWhether,THIS_TIMES_CNT );

    }


}
