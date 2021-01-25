package com.ndcf.spider.crawler.common.util;

public class ThreadUtil {
    private ThreadUtil() {

    }

    public static String getCurThreadInfo() {
        return "[线程组:" +Thread.currentThread().getThreadGroup()+
                ",当前线程id:" + Thread.currentThread().getId() +
                ",当前线程名称:" + Thread.currentThread().getName()+
                ",优先级:" + Thread.currentThread().getPriority()+
                "]";
    }


}
