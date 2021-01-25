package com.ndcf.spider.cron;


import com.ndcf.spider.crawler.executor.SingleHtmlUnitConfigFactory;
import com.ndcf.spider.crawler.executor.siteconfig.CralwerConfig;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SaveCrawlerConfigCron {


    private int count=0;

    // 每5分钟保存config
    @Scheduled(cron="0 0/5 * * * ?")
    private void process() {
        System.out.println("this is scheduler task runing  "+(count++));

        for (Map.Entry<String, CralwerConfig> configEntry : SingleHtmlUnitConfigFactory.CRAWLER_CONFIG_MAP.entrySet()) {
            configEntry.getValue().saveAllIpNodeSites();
            configEntry.getValue().saveAllLinks();
        }

        System.out.println("saveCralwerConfig:");


    }



}