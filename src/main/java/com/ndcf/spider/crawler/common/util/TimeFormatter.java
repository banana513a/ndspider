package com.ndcf.spider.crawler.common.util;

public class TimeFormatter {

    //构造方法私有化:该类本身地方除外的其他地方无法实例化该类对象
    private TimeFormatter() {
    }

    public static final String DATETIME_FORMATTER = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_FORMATTER_SSS = "yyyy-MM-dd HH:mm:ss.SSS";

    public static final String DATE_FORMATTER = "yyyy-MM-dd";

    public static final String TIME_FORMATTER = "HH:mm:ss";

    public static final String DATETIME_T_FORMATTER = "yyyy-MM-dd'T'HH:mm:ss";

}