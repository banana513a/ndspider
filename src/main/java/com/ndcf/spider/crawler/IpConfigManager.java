package com.ndcf.spider.crawler;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.ndcf.spider.crawler.bo.IpNode;
import com.ndcf.spider.crawler.common.enumeration.EnumDict;
import com.ndcf.spider.crawler.common.enumeration.EnumDict;
import com.ndcf.spider.crawler.common.util.Util;
import com.ndcf.spider.crawler.executor.RefreshIpListener;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.ndcf.spider.crawler.common.Constant.HtmlUnitConstant.*;
import static com.ndcf.spider.crawler.common.util.Util.strToObject;

/**
 * 单例（线程安全）
 */
public class IpConfigManager {

     public static ConcurrentHashMap<String, IpNode> allIpNodes = new ConcurrentHashMap<>();
     public static LinkedList<RefreshIpListener> refreshIpListeners = new LinkedList<>();
     public static HashSet<IpNode> idleIpNodes = new HashSet<>();
     public static HashSet<IpNode> unAvailableIpNodes = new HashSet<>();
    //    private static HashSet<IpNode> ipNodeProxyHashSet;
//    public static ConcurrentHashMap<IpNode> idleIpNodeQueue = new ConcurrentLinkedQueue<>();
//    public static ConcurrentLinkedQueue<IpNode> unAvailableIpNodeQueue = new ConcurrentLinkedQueue<>();
    private static IpNode ipNode;

    //        // 设置认证头部
    public final static String userName = "nq1h3diad5o1" ;
    public final static String password = "t6mmto4ype1" ;


    static {
        try {
            List<String> ipNodesAll = FileUtils.readLines(new File(PATH_ALL_IPNODES), CHARSET);
            for (String str : ipNodesAll) {
                IpNode ipNode = (IpNode)strToObject(str,OBJPATH_HTMLUNIT);
                System.out.println("init IpConfigManager.allIpNodes:"+ipNode.getIpport());
                allIpNodes.putIfAbsent(ipNode.getIpport(), ipNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        addProxyIpOnStatic();
        addIdleIpNodeQueue();
    }

    public static boolean registerRefreshIpListener(RefreshIpListener ipListener) {
        boolean status=false;
        synchronized (IpConfigManager.class) {
            refreshIpListeners.add(ipListener);
            status = true;
        }
        return status;
    }




    static void refreshIpNotify() {
        Iterator<RefreshIpListener> iterator = refreshIpListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().onRefreshIpCompelete();
        }

    }


    static void addProxyIpOnStatic() {
        IpNode ipNode;

        ipNode = new IpNode("116.196.105.144", "23128");
        allIpNodes.putIfAbsent(ipNode.getIpport(), ipNode);

        // localhost
        ipNode = new IpNode("127.0.0.1", "9999");
        ipNode.setType(EnumDict.IPTYPE_LOCAL.getCode());
        allIpNodes.putIfAbsent(ipNode.getIpport(), ipNode);

        saveAllIpNodes(allIpNodes);

    }

    static void addIdleIpNodeQueue() {
         IpNode ipNode;
        for (Map.Entry<String, IpNode> entry : allIpNodes.entrySet()) {
            ipNode=entry.getValue();
            if ( EnumDict.SUCESS.getCode().equals(ipNode.getStatus())) {
                idleIpNodes.add(ipNode);
            }
        }
    }

    private IpConfigManager() {

    }

    public static Boolean saveAllIpNodes(ConcurrentHashMap<String, IpNode> allIpNodes) {
        Boolean status=false;
        StringBuffer stringBuffer = new StringBuffer();
        if (null == allIpNodes || allIpNodes.isEmpty()) {
            return status;
        }
        try {
            for (Map.Entry<String,IpNode> entry: allIpNodes.entrySet()) {
                stringBuffer.append(entry.getValue().toString()).append("\n");
            }
            //覆盖模式
            FileUtils.write(new File(PATH_ALL_IPNODES), stringBuffer, CHARSET, false);
            status = true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return status;
    }

    private static final class IpManagerThread {
        private static final Thread INSTANCE=new Thread(new Inner(),"IpManagerThread");
    }

    public static Thread getIpManagerThread() {
        return IpManagerThread.INSTANCE;
    }


    private static class Inner implements Runnable {

        //10 分钟
        long sleepMs=10*60*1000;
//        long sleepMs=3*1000;

        boolean isNotify = false;
        IpNode ipNode=null;


        @Override
        public void run() {

            while (true) {
                try {
                    Thread.sleep(sleepMs);

//                    ipNode = new IpNode("127.0.0.1", Integer.toString((int)(Math.random()*1000)));
                    ipNode = new IpNode("127.0.0.1", "1");
                    ipNode.setType(EnumDict.IPTYPE_LOCAL.getCode());


                    ipNode = allIpNodes.putIfAbsent(ipNode.getIpport(), ipNode);

                    //如果有新的IP才通知,注意更新频率控制，全部IP变更后再全部通知
                    if (null == ipNode ) {
                        idleIpNodes.add(ipNode);
                    }
                    // 定时触发refreshIpNotify();
                    isNotify = true;
                    if (true == isNotify && sleepMs>5000) {
                        refreshIpNotify();
                    }
                    Util.saveTestFile();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


    }





    public boolean httpIsConnect(String URL) throws InterruptedException {
        boolean status = false;
        String url=URL;
        System.out.println("Thread.currentThread().getName():" + Thread.currentThread().getName() + " Id: " + Thread.currentThread().getId());

        // 随机睡眠 1-2s
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("stopwatch-httpConnect");
        Thread.sleep((long) (1 * 1000 + Math.random() * 1000));

        BrowserVersion[] versions = {BrowserVersion.BEST_SUPPORTED, BrowserVersion.CHROME, BrowserVersion.EDGE, BrowserVersion.FIREFOX_78};
        WebClient client = new WebClient(versions[(int)(versions.length * Math.random())]);

        Page page ;
        try {
            page = client.getPage(url);
            WebResponse response = page.getWebResponse();

            //4.判断响应状态为200，进行处理
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                String html = response.getContentAsString();

                status=true;
                System.out.println(html.length());
            } else {
                //如果返回状态不是200，比如404（页面不存在）等，根据情况做处理，这里略
                System.out.println("返回状态不是200");
                System.out.println(response.getContentAsString());
            }

        } catch (Exception e) {
            System.out.println("Exception:");
            e.printStackTrace();
        }finally {
            client.close();
        }

        stopWatch.stop();

        return status;
    }


    public static void main(String[] args) {
        System.out.println("11");
        getIpManagerThread().start();

    }

}
