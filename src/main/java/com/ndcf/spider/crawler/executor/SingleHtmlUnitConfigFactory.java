package com.ndcf.spider.crawler.executor;

import com.ndcf.spider.crawler.executor.siteconfig.*;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zsmj
 * @version 1.0.0
 * @ClassName SingleHtmlUnitConfigFactory.java
 * @Description HtmlUnitConfig多例，但每个SITE_NAME是单例的，每个SITE_NAME只有一个对应的config类；
 * @createTime 2020年12月13日 20:34:00
 */
public class SingleHtmlUnitConfigFactory {
    public final static ConcurrentHashMap<String, CralwerConfig> CRAWLER_CONFIG_MAP = new ConcurrentHashMap<>();

    //
    static {
        CRAWLER_CONFIG_MAP.put(CralwerExample1Config.getSingleCralwerConfig().getSITE_NAME(), CralwerExample1Config.getSingleCralwerConfig());
        CRAWLER_CONFIG_MAP.put(CralwerExample2Config.getSingleCralwerConfig().getSITE_NAME(), CralwerExample2Config.getSingleCralwerConfig());

    }





}
