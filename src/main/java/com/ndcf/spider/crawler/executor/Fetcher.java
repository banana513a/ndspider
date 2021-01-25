package com.ndcf.spider.crawler.executor;


import com.ndcf.spider.crawler.executor.siteconfig.CralwerConfig;

import java.util.concurrent.atomic.AtomicLong;


/**
 * @author zsmj
 * @version 1.0.0
 * @ClassName Fetcher.java
 * @Description 解耦，不递归访问，原型
 * @createTime 2020年12月06日 20:45:00
 */
public  class Fetcher implements Runnable {
    public CralwerConfig cralwerConfig;

    String varUrl;
    AtomicLong atomicLong = new AtomicLong(0);

    private Fetcher() {

    }


    @Override
    public void run() {
        cralwerConfig.startCrawler();
    }


    public Fetcher(CralwerConfig cralwerConfig) {
        this.cralwerConfig = cralwerConfig;
    }



//
//    class DefaultResponseChainHandleStep implements ResponseChainHandleStep {
//
//        @Override
//        public DTOHttpConnect handle(DTOHttpConnect dtoHttpConnect) {
//            return dtoHttpConnect;
//        }
//    }



}
