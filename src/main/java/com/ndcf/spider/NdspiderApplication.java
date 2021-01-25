package com.ndcf.spider;

import com.ndcf.spider.crawler.IpConfigManager;
import com.ndcf.spider.crawler.executor.Fetcher;
import com.ndcf.spider.crawler.executor.SingleHtmlUnitConfigFactory;
import com.ndcf.spider.crawler.executor.siteconfig.CralwerConfig;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.out;

@SpringBootApplication
@EnableScheduling
public class NdspiderApplication {

    public static void main(String[] args) {
        SpringApplication.run(NdspiderApplication.class, args);

        run();


    }


    static void run() {

        // 启动IpConfigManager
        IpConfigManager.getIpManagerThread().start();

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);


        for (Map.Entry<String, CralwerConfig> configEntry : SingleHtmlUnitConfigFactory.CRAWLER_CONFIG_MAP.entrySet()) {
            out.println(configEntry.getKey());
            out.println(configEntry.getValue().getURL());
            fixedThreadPool.submit(new Fetcher(configEntry.getValue()));

        }
        fixedThreadPool.execute(()->out.println("线程名称：" + Thread.currentThread().getName() + "，执行"));


    }
}
