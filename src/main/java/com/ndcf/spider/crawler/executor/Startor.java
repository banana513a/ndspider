package com.ndcf.spider.crawler.executor;

import com.ndcf.spider.crawler.IpConfigManager;
import com.ndcf.spider.crawler.executor.siteconfig.CralwerConfig;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.out;

public class Startor {


    public static void main(String[] args) {

        // 启动IpConfigManager
        IpConfigManager.getIpManagerThread().start();

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);


        for (Map.Entry<String, CralwerConfig> configEntry : SingleHtmlUnitConfigFactory.CRAWLER_CONFIG_MAP.entrySet()) {
            out.println(configEntry.getKey());
            out.println(configEntry.getValue().getURL());
            fixedThreadPool.submit(new Fetcher(configEntry.getValue()));

        }
        fixedThreadPool.execute(()->out.println("线程名称：" + Thread.currentThread().getName() + "，执行"));



//        Thread thread1 = new Thread(new HtmlUnit(SingleHtmlUnitConfigFactory.htmlUnitConfigMap.get(CralwerJianshuConfig.SITE_NAME)));
//        thread1.start();



    }
}
