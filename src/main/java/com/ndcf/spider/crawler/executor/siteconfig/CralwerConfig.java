package com.ndcf.spider.crawler.executor.siteconfig;

import com.gargoylesoftware.htmlunit.*;
import com.ndcf.spider.crawler.IpConfigManager;
import com.ndcf.spider.crawler.bo.*;
import com.ndcf.spider.crawler.common.Constant.HtmlUnitConstant;
import com.ndcf.spider.crawler.common.enumeration.EnumDict;
import com.ndcf.spider.crawler.common.enumeration.ResultCodeEnum;
import com.ndcf.spider.crawler.common.exceptions.HtmlUnitExecption;
import com.ndcf.spider.crawler.common.util.DateTimeUtil;
import com.ndcf.spider.crawler.dto.DTOHttpConnect;
import com.ndcf.spider.crawler.executor.RefreshIpListener;
import com.ndcf.spider.crawler.executor.ResponseChainHandleStep;
import com.ndcf.spider.crawler.httpclient.HttpClientHelper;
import com.ndcf.spider.crawler.httpclient.HttpResult;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ndcf.spider.crawler.common.Constant.HtmlUnitConstant.OBJPATH_HTMLUNIT;
import static com.ndcf.spider.crawler.common.util.HttpUtil.urldecode;
import static com.ndcf.spider.crawler.common.util.Util.strToObject;


/**
 * 多例模式，但每个SITE_NAME是单例；protected 构造方法
 * IP访问控制：图片类型和href 分别控制次数，图片类型为html的10倍 thisTimeCnt *= 10;,
 * 执行思路：一、以站点为主来轮询IP， 二、以IP为主来轮询站点，
 */
@Getter
@Setter
public abstract class CralwerConfig implements RefreshIpListener {

    private static Logger logger = LoggerFactory.getLogger(CralwerConfig.class);


    public LinkedList<ResponseChainHandleStep> chainResStepList = new LinkedList<>();
    //    ConcurrentLinkedQueue<String> threadQueue = new ConcurrentLinkedQueue<>();
    public ConcurrentHashMap<String, UrlNode> allLinks = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, UrlNode> linksUsed = new ConcurrentHashMap<>();
    //    public ConcurrentHashMap<String,HtmlFileInfoBo> linksUsed = new ConcurrentHashMap<>();
    public ConcurrentLinkedQueue<UrlNode> urlQueue = new ConcurrentLinkedQueue<>();
    // 初始化注意先加载linksUsed，然后再加载allLinks，urlQueue；
    ArrayList<Node> nodeArrayList = new ArrayList<>();
    AtomicLong atomicLong = new AtomicLong(0);
    String varUrl;
    //        // 设置认证头部
    protected String userName;
    protected String password;
    protected Pattern patternRegFilePath;
    protected Pattern patternRegUrlConStr;
    protected Pattern patternReg;
    protected Pattern patternRegTag;
    // 需要初始化的参数
    protected String SITE_NAME; //重要，用于正则匹配和限流等， 域名或者二级域名 ;
    protected String DOMAIN_NAME; // "www.xxx.com";
    protected String WEBSITE; // "http://www.xxx.com";
    protected String URL; // "http://www.xxx.com";
    protected String CHARSET ="UTF-8"; // 爬网页会再动态获取
    // 递归访问特定条件的URL
    protected String filePathReg;// = "(?<="+SITE_NAME+"/)\\S*";
    protected String regUrlConStr;// = "\\S*(?:"+SITE_NAME+")((?!css|js|:void)\\S)*";
    protected long MAX_TIMES = HtmlUnitConstant.MAX_TIMES;
    protected long THIS_TIMES_CNT = HtmlUnitConstant.THIS_TIMES_CNT;
    // 保存访问页面中的所有符合条件的连接handleUrlToQueue:
    // protected String reg = "href=['\"](\\S*"+SITE_NAME+"\\S*|(?!http|//)\\S*)(?<!css|js|:void|\\(0\\))['\"]";
    // 先保存所有包含站点的url，在connect再做筛选
    protected String reg; // = "href=['\"](\\S*"+SITE_NAME+"\\S*|(?!http|//)\\S*)['\"]";
    protected String regTag; // = "href=['\"](\\S*"+SITE_NAME+"\\S*|(?!http|//)\\S*)['\"]";
    // 线程相关信息
    protected String threadName;
    protected int threadNums = 1;
    protected int priority = 5;
    //文件路径
    protected String PATH = HtmlUnitConstant.PATH;
    protected String PATH_FILE;
    protected String pathMapping;
    protected String pathAllLinks;
    //
    protected String pathAllIpNodeSites;
    protected int MAX_DEPTH = 9999;
    protected boolean proxyWhether = true;

    //原htmlUnit
    protected int timeOut = 10000;
    // 失败重试
    protected int errorCnt = 1;
    // 失败指定次数后切换IP
    protected int errorCntIp = 100;
    protected ConcurrentHashMap<String, IpNodeSiteDate> allIpNodeSites;
    protected HashSet<IpNode> idleIpNodes;
    protected HashSet<IpNode> unAvailableIpNodes;
    protected String localhostIpPortSite;

    //

    protected CralwerConfig() {

    }

    protected CralwerConfig(String URL, String regUrlConStr , int MAX_TIMES, boolean proxyWhether, long THIS_TIMES_CNT) {
        // url为域名要以为/结尾
        this.URL = URL.indexOf("/", 8) < 0 ? URL + "/" : URL;
        this.MAX_TIMES = MAX_TIMES;
        this.proxyWhether = proxyWhether;
        this.THIS_TIMES_CNT = THIS_TIMES_CNT;

        // 注意是入参还是this.URL
        this.DOMAIN_NAME = URL.substring(this.URL.indexOf("//") + 2, this.URL.indexOf("/", 8));

        String[] strings = this.DOMAIN_NAME.split("\\.");
        if (strings.length >= 3) {
            this.SITE_NAME = strings[1];
            // 后期考虑分类
//            if ("www".equals(strings[0])) {
//                this.SITE_NAME = strings[1];
//            } else {
//                this.SITE_NAME = strings[0]+ strings[1] ;
//            }
        } else {
            this.SITE_NAME = strings[0];
        }


        this.regUrlConStr = regUrlConStr == null ? this.SITE_NAME : regUrlConStr;

    }


    void initVars() {


        this.filePathReg = "(?<=" + SITE_NAME + "/)\\S*";

        this.WEBSITE = this.URL.substring(0, this.SITE_NAME.indexOf("/", 8) + 1);


//        this.reg = "href=['\"](\\S*" + SITE_NAME + "\\S*|(?!http|//)\\S*)['\"]";
        this.reg = "href=['\"]\\S*['\"]";
        this.regTag = "src=['\"]\\S*['\"]";
        // 如果regUrlConStr是空的使用默认的正则
//        this.regUrlConStr = "\\S*(?:" + SITE_NAME + ")((?!css|js|:void)\\S)*";
        this.patternReg = Pattern.compile(reg);
        this.patternRegTag = Pattern.compile(regTag);


        this.patternRegUrlConStr = Pattern.compile(regUrlConStr);
        this.patternRegFilePath = Pattern.compile(filePathReg);

        //文件路径
        this.PATH_FILE = PATH + SITE_NAME + "\\files\\";
        this.pathMapping = PATH + SITE_NAME + "\\siteconfig\\mapping.txt";
        this.pathAllLinks = PATH + SITE_NAME + "\\siteconfig\\all_links.txt";

        this.pathAllIpNodeSites = HtmlUnitConstant.PATH_COMMON + "allIpNodeSites_" + SITE_NAME + ".txt";

        this.localhostIpPortSite = "127.0.0.1:9999:" + SITE_NAME;

        //
    }

    /**
     * 外部Builder构造完再手工执行此初始化方法，不要放在构造方法里；后续待优化用Builder模式
     */
    public void init() {
        initVars();
        readConfigFile();
        onRefreshIpCompelete();
        initUrlConfig();
        // 注册listener ，
        IpConfigManager.registerRefreshIpListener(this);
    }

    public void initUrlConfig() {


        // 初始化先addAllLinks，然后LinksUsed，最后UrlQueue

        try {

            // 自动创建目录和文件，注意是追加模式
            FileUtils.write(new File(pathAllLinks), null, CHARSET, true);
            FileUtils.write(new File(pathMapping), null, CHARSET, true);


//            logger.info("pathMapping:" + pathMapping);
            List<String> urlNodeAllLinksStrList = FileUtils.readLines(new File(pathAllLinks), CHARSET);
            UrlNode urlNode;
            for (int i = 0; i < urlNodeAllLinksStrList.size(); i++) {
                try {
                    urlNode = (UrlNode) strToObject(urlNodeAllLinksStrList.get(i), OBJPATH_HTMLUNIT);
//                    logger.info("initConfig-urlNode:" + urlNode.toString());
                    varUrl = urlNode.getUrl();
                    allLinks.putIfAbsent(urlNode.getUrl(), urlNode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


//            logger.info("pathMapping:" + pathMapping);
            List<String> fileInfoStrList = FileUtils.readLines(new File(pathMapping), CHARSET);

            HtmlFileInfoBo htmlFileInfoBo;
            for (int i = 0; i < fileInfoStrList.size(); i++) {

                try {
                    htmlFileInfoBo = (HtmlFileInfoBo) strToObject(fileInfoStrList.get(i), OBJPATH_HTMLUNIT);
                    // url
                    varUrl = htmlFileInfoBo.getUrl();
                    if (varUrl != null && !"".equals(varUrl)) {
                        linksUsed.putIfAbsent(varUrl, allLinks.get(varUrl));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // fileName
            }

            // 初始URL
            UrlNode rootUrlNode = new UrlNode(URL, null, EnumDict.URL_HTML.getCode(), EnumDict.URL_TYPE_HREF.getCode(), URL);
            if (null == allLinks.putIfAbsent(URL, rootUrlNode)) {
                FileUtils.write(new File(pathAllLinks), rootUrlNode.toString() + "\n", CHARSET, true);
            }
            urlQueue.add(rootUrlNode);


            initAddUrlQueue();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void initAddUrlQueue() {
        for (Map.Entry<String, UrlNode> entry : allLinks.entrySet()) {
            addUrlQueue(entry.getValue());
        }

    }


    boolean addUrlQueue(UrlNode urlNode) {
        String url = handleUrlToQueue(urlNode);

        if (null != url) {
            url = handleUrlOnFind(url);

        }

        if (null == url
                || "".equals(url)
                || linksUsed.contains(urlNode)
                || urlNode.getStatus().equals(EnumDict.FAILURE.getCode())
                || urlNode.getStatus().equals(EnumDict.UNAVAILABLE.getCode())
        ) {
//            logger.info("addUrlQueue already exist or unavailable");
            return false;
        }
        urlQueue.add(urlNode);
        return true;
    }

    public void readConfigFile() {
//        logger.info("init ipNodeSites-readConfigFile:");

        allIpNodeSites = new ConcurrentHashMap<>();
        try {
            if (new File(pathAllIpNodeSites).exists()) {
                List<String> stringList = FileUtils.readLines(new File(pathAllIpNodeSites), CHARSET);
                for (String str : stringList) {
                    IpNodeSiteDate ipNodeSiteDate = (IpNodeSiteDate) strToObject(str, OBJPATH_HTMLUNIT);
//                logger.info("init ipNodeSites-readConfigFile:" + (i++) + "  " + ipNodeSiteDate.toString());
                    if (DateTimeUtil.getCurrentDateStr().equals(ipNodeSiteDate.getDate())) {
                        allIpNodeSites.putIfAbsent(ipNodeSiteDate.getIpPortSiteDate(), ipNodeSiteDate);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    boolean addLinksUsed(UrlNode urlNode) {
        //去掉synchronized
//        synchronized (CralwerConfig.class) {
        String url = urlNode.getUrl();

        if (null == url || "".equals(url) || linksUsed.contains(urlNode)) {
            return false;
        } else {
            if (null == linksUsed.putIfAbsent(url, urlNode)) {
                return true;
            } else {
                return false;
            }
        }
    }

    // 过滤url到urlQueue
    String handleUrlToQueue(UrlNode urlNode) {
        String retUrl = null;
        Pattern pattern = patternRegUrlConStr;
        String url = urlNode.getUrl();
        String parentUrl = urlNode.getParentUrl() == null ? url : urlNode.getParentUrl();
        Matcher matcher = pattern.matcher(url);
        Matcher matcher2 = pattern.matcher(parentUrl);

        if (matcher.find() || matcher2.find() || url.equals(URL)) {
            retUrl = url;
        }

        return retUrl;
    }


    // Override
    String handleUrlOnFind(String url ) {
        return url;
    }


//    String handleUrlToQueue(UrlNode urlNode) {
//        String retUrl = null;
//        Pattern pattern = patternRegUrlConStr;
//        String url = urlNode.getUrl();
//        String parentUrl = urlNode.getParentUrl() == null ? url : urlNode.getParentUrl();
//        Matcher matcher = pattern.matcher(url);
//        Matcher matcher2 = pattern.matcher(parentUrl);
//
//        if (!url.substring(url.lastIndexOf("/") + 1).contains(".")
//                || url.contains(".html")
//                ) {
//            if (matcher.find() || matcher2.find() || url.equals(URL)) {
//                retUrl = url;
//            }
//        } else {
//            retUrl = url;
//        }
//
//        return retUrl;
//    }


    public void findAndSaveLinks(DTOHttpConnect dtoHttpConnect) throws IOException {

        String html = dtoHttpConnect.getHtml();
        String webUrl = dtoHttpConnect.getUrlNode().getUrl();
        String localWebSite = webUrl.substring(0, webUrl.indexOf("/", 8));

        UrlNode urlNode;
        String link;
        String src;

        Matcher matcher = patternReg.matcher(html);
        Matcher matcherTag = patternRegTag.matcher(html);

        int i = 0;
        HashSet<UrlNode> hashSet = new LinkedHashSet<>();

/*


        Document doc = Jsoup.parse(html.toString());
        Elements elementsImg = doc.select("img[src]");//获取到的值为所有的<img src="...">
        Elements elementsScript = doc.select("script[src]");//获取到的值为所有的<script src="...">
        HashSet<UrlNode> hashSet = new LinkedHashSet<>();
        int i = 0;
        for (Element element : elementsImg) {
            src = element.attr("src");//获取到src的值
            if (src.indexOf("#") > 0) {
                hashSet.add(new UrlNode(src.substring(0, src.indexOf("#")), dtoHttpConnect.getUrlNode().getUrl(), EnumDict.URL_UNKNOW.getCode(), EnumDict.URL_TYPE_TAG.getCode(), src));
            } else {
                hashSet.add(new UrlNode(src, dtoHttpConnect.getUrlNode().getUrl(), EnumDict.URL_UNKNOW.getCode(), EnumDict.URL_TYPE_TAG.getCode(), src));
            }
            i++;
        }

        for (Element element : elementsScript) {
            src = element.attr("src");//获取到src的值
            if (src.indexOf("#") > 0) {
                hashSet.add(new UrlNode(src.substring(0, src.indexOf("#")), dtoHttpConnect.getUrlNode().getUrl(), EnumDict.URL_UNKNOW.getCode(), EnumDict.URL_TYPE_TAG.getCode(), src));
            } else {
                hashSet.add(new UrlNode(src, dtoHttpConnect.getUrlNode().getUrl(), EnumDict.URL_UNKNOW.getCode(), EnumDict.URL_TYPE_TAG.getCode(), src));
            }
            i++;
        }

*/


        while (matcher.find()) {
            src = matcher.group(0);
            if (src.indexOf("='") >= 0 && src.indexOf("='") < 8) {
                src = src.substring(src.indexOf("='") + 2, src.length() - 1);
            } else {
                src = src.substring(src.indexOf("=\"") + 2, src.length() - 1);
            }

            if (src.length() == 0) {
                continue;
            }
            if (src.indexOf("#") >= 0) {
                hashSet.add(new UrlNode(src.substring(0, src.indexOf("#")), dtoHttpConnect.getUrlNode().getUrl(), EnumDict.URL_UNKNOW.getCode(), EnumDict.URL_TYPE_HREF.getCode(), src));
            } else {
                hashSet.add(new UrlNode(src, dtoHttpConnect.getUrlNode().getUrl(), EnumDict.URL_UNKNOW.getCode(), EnumDict.URL_TYPE_HREF.getCode(), src));
            }
            i++;

        }

        while (matcherTag.find()) {
            src = matcherTag.group(0);
            if (src.indexOf("='") >= 0 && src.indexOf("='") < 8) {
                src = src.substring(src.indexOf("='") + 2, src.length() - 1);
            } else {
                src = src.substring(src.indexOf("=\"") + 2, src.length() - 1);
            }

            if (src.length() == 0) {
                continue;
            }
            if (src.indexOf("#") >= 0) {
                hashSet.add(new UrlNode(src.substring(0, src.indexOf("#")), dtoHttpConnect.getUrlNode().getUrl(), EnumDict.URL_UNKNOW.getCode(), EnumDict.URL_TYPE_TAG.getCode(), src));
            } else if (src.indexOf(".jpg!") >= 0) {
                hashSet.add(new UrlNode(src.substring(0, src.indexOf(".jpg!") + 4), dtoHttpConnect.getUrlNode().getUrl(), EnumDict.URL_UNKNOW.getCode(), EnumDict.URL_TYPE_TAG.getCode(), src));
            } else if (src.indexOf(".png!") >= 0) {
                hashSet.add(new UrlNode(src.substring(0, src.indexOf(".png!") + 4), dtoHttpConnect.getUrlNode().getUrl(), EnumDict.URL_UNKNOW.getCode(), EnumDict.URL_TYPE_TAG.getCode(), src));
            } else {
                hashSet.add(new UrlNode(src, dtoHttpConnect.getUrlNode().getUrl(), EnumDict.URL_UNKNOW.getCode(), EnumDict.URL_TYPE_TAG.getCode(), src));
            }
            i++;

        }


        // 用ConCurrentHashMap去掉synchronized
        Iterator iterator = hashSet.iterator();
        while (iterator.hasNext()) {
            urlNode = (UrlNode) iterator.next();
            link = urlNode.getUrl();

            if (link.length() == 0) {
                continue;
            }


            if (link.indexOf(".jpg?") > 0) {
                link = link.substring(0, link.indexOf(".jpg?") + 4);
            } else if (link.indexOf(".jpeg?") > 0) {
                link = link.substring(0, link.indexOf(".jpeg?") + 5);
            } else if (link.indexOf(".svg?") > 0) {
                link = link.substring(0, link.indexOf(".svg?") + 4);
            } else if (link.indexOf(".png?") > 0) {
                link = link.substring(0, link.indexOf(".png?") + 4);
            } else if (link.indexOf(".svg?") > 0) {
                link = link.substring(0, link.indexOf(".svg?") + 4);
            } else if (link.indexOf(".gif?") > 0) {
                link = link.substring(0, link.indexOf(".gif?") + 4);
            } else if (link.indexOf(".pdf?") > 0) {
                link = link.substring(0, link.indexOf(".pdf?") + 4);
            }


            if (link.length() >= 2 && "//".equals(link.substring(0, 2))) {
                link = webUrl.substring(0, webUrl.indexOf("//")) + link;
            } else if (link.substring(0, 1).equals("/")) {
                link = localWebSite + link;
            } else if (link.length() >= 4 && !link.substring(0, 4).equals("http")) { // 没有http开头的
                link = localWebSite + "/" + link;
            }

            // 单独处理，比如originalUrl='//cdn2.xxx.io'
            if (link.indexOf("/", 8) < 0) {
                link = link + "/";
            }


            link = handleUrlOnFind(link);

            urlNode.setUrl(link);



/*

                // url /
                // contentType 判断类型
                if (link.lastIndexOf("/",8) > 0) {
                    urlTmp = link.substring(link.lastIndexOf("/"));

                    if (EnumDict.URL_TYPE_HREF.getCode().equals(urlNode.getTag())) {
                        // .htm,.html,xhtml
                        if (  urlTmp.contains(".png")
                                ||urlTmp.contains(".jpg")
                                ||urlTmp.contains(".svg")

                        ) {
                            urlNode.setType(EnumDict.URL_MEDIA.getCode());
                        } else {
                            urlNode.setType(EnumDict.URL_HTML.getCode());
                        }
                    }else {
                        // .htm,.html,xhtml
                        if (urlTmp.contains(".htm")
                                ||urlTmp.contains(".xhtml")
                                ||urlTmp.contains(".xml")
                                ||urlTmp.contains(".xsl")
                                ||urlTmp.contains(".shtm")
                                ||urlTmp.contains(".dhtml")
                                ||urlTmp.contains(".css")
                                ||urlTmp.contains(".js")
                        ) {
                            urlNode.setType(EnumDict.URL_HTML.getCode());
                        } else {
                            urlNode.setType(EnumDict.URL_MEDIA.getCode());
                        }
                    }
                } else {
                    // UrlNode{url='https://kb.cnblogs.com', parentUrl='http://www.cnblogs.com/', originalUrl='https://kb.cnblogs.com', type='201', tag='211', status='3', insertDate='2020-12-17 20:03:09.092', updateDate='null'}
                    urlNode.setUrl(link+"/");
                    urlNode.setType(EnumDict.URL_HTML.getCode());
                }

*/


            if (urlNode.getUrl() != null && null == allLinks.putIfAbsent(urlNode.getUrl(), urlNode)) {
//                logger.info("matcher.find:" + urlNode.toString());
                if (addUrlQueue(urlNode)) {
//                    FileUtils.write(new File(pathAllLinks), urlNode.toString() + "\n", CHARSET, true);
                }
            }
        }

        return;
    }



    public String genFilePath(DTOHttpConnect dtoHttpConnect) {


        String filePath = null;
        String fileName = null;
        String urlTmp= null;
        String webUrl = dtoHttpConnect.getUrlNode().getUrl();
        UrlNode urlNode = dtoHttpConnect.getUrlNode();
        webUrl = webUrl.indexOf("/", 8) < 0 ? webUrl + "/" : webUrl;
        String localDomainName = webUrl.substring(webUrl.indexOf("//") + 2, webUrl.indexOf("/", 8));
        if (dtoHttpConnect.getCharset() != null) {
            urlTmp= urldecode(webUrl, dtoHttpConnect.getCharset());
        } else {
            urlTmp = urldecode(webUrl);
        }
        String urlTmpFirstName = null;
        String urlTmpLastName = null;
        String str1 = webUrl.substring(0, webUrl.lastIndexOf("/"));




        if (urlTmp.indexOf("?") > 6) {
            if (".".equals(urlTmp.substring(urlTmp.indexOf("?") - 4, urlTmp.indexOf("?") - 3))
                && (urlTmp.substring(urlTmp.indexOf("?") - 4, urlTmp.indexOf("?")).contains(".png")
                    || urlTmp.substring(urlTmp.indexOf("?") - 4, urlTmp.indexOf("?")).contains(".svg")
                    || urlTmp.substring(urlTmp.indexOf("?") - 4, urlTmp.indexOf("?")).contains(".jpg")
                    || urlTmp.substring(urlTmp.indexOf("?") - 4, urlTmp.indexOf("?")).contains(".gif")
                    )
//                    && !urlTmp.substring(urlTmp.indexOf("?") - 4, urlTmp.indexOf("?")).contains(".htm")
                    ) {
                urlTmp = urlTmp.substring(0, urlTmp.indexOf("?"));
            }else if (".".equals(urlTmp.substring(urlTmp.indexOf("?") - 5, urlTmp.indexOf("?") - 4))
                    && (urlTmp.substring(urlTmp.indexOf("?") - 5, urlTmp.indexOf("?")).contains(".jpeg")
            )
//                    && !urlTmp.substring(urlTmp.indexOf("?") - 4, urlTmp.indexOf("?")).contains(".htm")
            ) {
                urlTmp = urlTmp.substring(0, urlTmp.indexOf("?"));
            }
        }






//                urlTmp = urlTmp.replace("*", "%3a");
        // 没有后缀名的处理,注意 lastIndexOf
        urlTmpLastName = urlTmp.substring(urlTmp.lastIndexOf("/"));
        urlTmpLastName = urlTmpLastName.length() >= 2 ? urlTmpLastName.substring(1) : urlTmpLastName;


        // 从第八位indexOf
        int var1 = urlTmp.indexOf("/", 12);
        if (var1 < 0 || "/".equals(urlTmp.substring(var1))) {
            fileName = localDomainName;
        } else {
            if ("/".equals(urlTmp.substring(urlTmp.length() - 1))) {
                fileName = urlTmp.substring(var1 + 1) + localDomainName;
            } else {
                fileName = urlTmp.substring(var1 + 1);

//                        if (urlTmpLastName.contains(".") && urlTmpLastName.contains("%3f")) {
//                            fileName = urlTmp.substring(var1 + 1, urlTmp.lastIndexOf("%3f"));
//                        } else {
//                            fileName = urlTmp.substring(var1 + 1);
//                        }
            }
        }


        // 注意顺序和位置
        fileName = fileName.replace("?", "%3f");
        fileName = fileName.replace("#", "%23");
        fileName = fileName.replace("|", "%7c");
        fileName = fileName.replace("<", "%3c");
        fileName = fileName.replace(">", "%3e");
        fileName = fileName.replace(">", "%3e");
        fileName = fileName.replace(":", "%3a");
        fileName = fileName.replace("\"", "%22");




        if ( urlTmpLastName.length()>5 && !urlTmpLastName.substring(urlTmpLastName.length()-5).contains(".")) {
            if (EnumDict.URL_HTML.getCode().equals(urlNode.getType())) {
                filePath = PATH_FILE + localDomainName + "/" + fileName + ".html";
            } else {
                filePath = PATH_FILE + localDomainName + "/" + fileName + ".jpg";
            }
        } else {

            filePath = PATH_FILE + localDomainName + "/" + fileName;
        }


        // windows 文件路径以/
        filePath = filePath.replace("/", "\\");




//        logger.info("filePath:" + filePath.toString());


        return filePath;


    }


    // 保存图片和href待分开处理
    public void findUrlAndStoreData(DTOHttpConnect dtoHttpConnect) throws HtmlUnitExecption, IOException {
        String html = dtoHttpConnect.getHtml();
        UrlNode urlNode = dtoHttpConnect.getUrlNode();
        String webUrl = dtoHttpConnect.getUrlNode().getUrl();

        //

        String md5Hex = DigestUtils.md5Hex(html.getBytes());
        HtmlFileInfoBo htmlFileInfoBo;
        InputStream inputStream = null;
        OutputStream outputStream = null;


        String charset = dtoHttpConnect.getCharset();
        String contentType = dtoHttpConnect.getContentType();
        
        ArrayList<String> links;
        Boolean isSave;

        // 保存网页内的url
        if (contentType.contains("text")) {
            urlNode.setType(EnumDict.URL_HTML.getCode());
            findAndSaveLinks(dtoHttpConnect);
        } else {
            urlNode.setType(EnumDict.URL_MEDIA.getCode());
        }

        isSave = addLinksUsed(urlNode);
        try {

            if (isSave == true) {

                String filePath =genFilePath(dtoHttpConnect);

                htmlFileInfoBo = new HtmlFileInfoBo(webUrl, filePath, md5Hex);


                Node node = new Node(webUrl, filePath);
                nodeArrayList.add(node);
                // html覆盖模式
                if (EnumDict.URL_HTML.getCode().equals(urlNode.getType())) {
                    FileUtils.write(new File(filePath), html, charset, false);
                } else {
//                    inputStream = dtoHttpConnect.getPage().getWebResponse().getContentAsStream();
                    File file = new File(filePath.substring(0, filePath.lastIndexOf("\\") + 1));
                    file.mkdirs();
                    outputStream = new FileOutputStream(filePath, false);
                    IOUtils.write(dtoHttpConnect.getResponse(), outputStream);
                }

                // 配置文件注意是追加模式
                FileUtils.write(new File(pathMapping), htmlFileInfoBo.toString() + "\n", CHARSET, true);

            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 待确认是否自动关闭流，是否内存泄漏？
            if (!EnumDict.URL_HTML.getCode().equals(urlNode.getType())) {
                try {
                    outputStream.close();
//                    inputStream.close();
                    IOUtils.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return;
    }



    public boolean saveAll(String filePath,ConcurrentHashMap<String, ?> allNodes) {
        boolean status = false;
        StringBuffer stringBuffer = new StringBuffer();
        if (null == allNodes || allNodes.isEmpty()) {
            return status;
        }
        try {
            for (Map.Entry<String, ?> entry : allNodes.entrySet()) {
                stringBuffer.append(entry.getValue().toString()).append("\n");
            }
            //覆盖模式
            FileUtils.write(new File(filePath), stringBuffer, CHARSET, false);
            status = true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return status;
    }


    public  boolean saveAllLinks() {
        return saveAll(pathAllLinks, allLinks);
    }

    public boolean saveAllIpNodeSites() {
        return saveAll(pathAllIpNodeSites, allIpNodeSites);
    }



    public WebClient genWebClient() {
        WebClient webClient;
        BrowserVersion[] versions = {BrowserVersion.BEST_SUPPORTED, BrowserVersion.CHROME, BrowserVersion.EDGE, BrowserVersion.FIREFOX_78};
        webClient = new WebClient(versions[(int) (versions.length * Math.random())]);
        webClient.getOptions().setTimeout(timeOut);
        webClient.getOptions().setThrowExceptionOnScriptError(false);//当JS执行出错的时候是否抛出异常, 这里选择不需要
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);//当HTTP的状态非200时是否抛出异常, 这里选择不需要
        webClient.getOptions().setActiveXNative(false);
        webClient.getOptions().setCssEnabled(false);//是否启用CSS, 因为不需要展现页面, 所以不需要启用
        // 默认设置js false 提高效率
        webClient.getOptions().setJavaScriptEnabled(true); //很重要，启用JS
//            webClient.getOptions().setUseInsecureSSL(false); //
        webClient.getOptions().setUseInsecureSSL(true);
//            webClient.getOptions().setDownloadImages(true);
        webClient.getCache().setMaxSize(100);

        webClient.setAjaxController(new NicelyResynchronizingAjaxController());//很重要，设置支持AJAX

        if (true == proxyWhether) {
            DefaultCredentialsProvider creds = new DefaultCredentialsProvider();
            creds.addCredentials(IpConfigManager.userName, IpConfigManager.password);
            webClient.setCredentialsProvider(creds);
        }


        CookieManager cookieManager = new CookieManager() {
            protected int getPort(java.net.URL url) {
                // or deduct it from url.getProtocol()
                return 80;
            }
        };
        webClient.setCookieManager(cookieManager);


        return webClient;
    }

    public boolean setClient(DTOHttpConnect dtoHttpConnect) throws Exception {
        IpNodeSiteDate ipNodeSiteDateTmp = null;
        String ipPortSite = null;
        boolean status = false;


        // 默认 EXECTOR_TYPE_HTTPCLIENT
        dtoHttpConnect.setExectorType(EnumDict.EXECTOR_TYPE_HTTPCLIENT.getCode());
        
        WebClient webClient = dtoHttpConnect.getWebClient()==null?genWebClient():dtoHttpConnect.getWebClient();
        
        // 默认 EXECTOR_TYPE_HTTPCLIENT
        dtoHttpConnect.setExectorType(EnumDict.EXECTOR_TYPE_HTTPCLIENT.getCode());

        if (true == proxyWhether) {
            for (Map.Entry<String, IpNodeSiteDate> entry : allIpNodeSites.entrySet()) {
//                logger.info("setClient:" + entry.getValue().toString());
                if ("0".equals(entry.getValue().getType())
                        && "0".equals(entry.getValue().getStatus())
                        && DateTimeUtil.getCurrentDateStr().equals(entry.getValue().getDate())
                        ) {
                    ipNodeSiteDateTmp = entry.getValue();
                    break;
                }
            }
            ipPortSite = ipNodeSiteDateTmp.getIpPortSiteDate();
            if (ipPortSite != null) {
                ProxyConfig proxyConfig = new ProxyConfig((ipPortSite.split(",")[0]).split(":")[0], Integer.parseInt((ipPortSite.split(",")[0]).split(":")[1]));
                webClient.getOptions().setProxyConfig(proxyConfig); // 此处设置代理IP
                HttpHost httpHost = new HttpHost((ipPortSite.split(",")[0]).split(":")[0], Integer.parseInt((ipPortSite.split(",")[0]).split(":")[1]));
                dtoHttpConnect.setHttpClientHelper(new HttpClientHelper(httpHost));
                status = true;
            }
//            logger.info("setClient:ipNodeSite-" + ipNodeSiteDateTmp.toString());

        } else {
            System.out.print("本机访问:");
            ipNodeSiteDateTmp = allIpNodeSites.get(localhostIpPortSite+":" + DateTimeUtil.getCurrentDateStr());
//            logger.info(ipNodeSiteDateTmp.toString());
            status = true;

            if ((!"0".equals(ipNodeSiteDateTmp.getStatus()))
                    || (!DateTimeUtil.getCurrentDateStr().equals(ipNodeSiteDateTmp.getDate()))
                    ) {
                ipNodeSiteDateTmp = null;
                status = false;
            }


        }
        // 不能对同IP重复设置代理
        if (status != true || ipNodeSiteDateTmp.equals(dtoHttpConnect.getIpNodeSiteDate()))  {
            dtoHttpConnect.setWebClient(null);
            throw new HtmlUnitExecption(ResultCodeEnum.HTTP_SET_PROXY_ERROR);
        } else {
            dtoHttpConnect.setIpNodeSiteDate(ipNodeSiteDateTmp);
            dtoHttpConnect.setWebClient(webClient);
            dtoHttpConnect.setHttpClientHelper(new HttpClientHelper(null));
        }

        return status;
    }

    public boolean setAfterStatusStep(DTOHttpConnect dtoHttpConnect) throws Exception {


        boolean status = false;
        int ivar;
        IpNodeSiteDate ipNodeSiteDate = dtoHttpConnect.getIpNodeSiteDate();
        // 后期待该按域名站点统计，比如跨站以及CDN的图片资源应该分类，



        if ( null != dtoHttpConnect.getHttpStatus() && Integer.valueOf(dtoHttpConnect.getHttpStatus()) == HttpStatus.SC_OK) {
            ivar = Integer.parseInt(ipNodeSiteDate.getSuccessCnt()) + 1;
            ipNodeSiteDate.setSuccessCnt(String.valueOf(ivar));

            //切换IP
            if (ivar > MAX_TIMES) {
                // setClient
                status = setClient(dtoHttpConnect);
            } else {
                status = true;
            }

        } else {
            // 每个url失败算一次失败连接，对象的getErrorCnt+1
            ivar = Integer.parseInt(ipNodeSiteDate.getErrorCnt()) + 1;
            ipNodeSiteDate.setErrorCnt(String.valueOf(ivar));

            dtoHttpConnect.getUrlNode().setStatus(EnumDict.UNAVAILABLE.getCode());
            dtoHttpConnect.getUrlNode().setUpdateDate(DateTimeUtil.getCurrentDateTimeMsStr());
            //切换IP
            if (ivar >= errorCntIp) {
                ipNodeSiteDate.setStatus("4");
                // setClient
                status = setClient(dtoHttpConnect);
            } else {
                status = true;
            }
        }

        if (status != true) {
            throw new HtmlUnitExecption(ResultCodeEnum.HTTP_SET_PROXY_ERROR);
        }
        return status;
    }

//
//    public Boolean setErrorStep(DTOHttpConnect dtoHttpConnect) throws Exception {
//    }

    
    @Override
    public boolean onRefreshIpCompelete() {
        IpNode ipNode;
        IpNodeSiteDate ipNodeSiteDate;
        for (Map.Entry<String, IpNode> entry : IpConfigManager.allIpNodes.entrySet()) {
            ipNode = entry.getValue();
            if ("0".equals(ipNode.getStatus())) {
                ipNodeSiteDate = new IpNodeSiteDate(ipNode.getIp(), ipNode.getPort(), SITE_NAME, ipNode.getType());
                allIpNodeSites.putIfAbsent(ipNodeSiteDate.getIpPortSiteDate(), ipNodeSiteDate);
//                logger.info("init ipNodeSites:" + ipNodeSiteDate.toString());
            }
        }

        return false;
    }




    public boolean htmlUnitGet(DTOHttpConnect dtoHttpConnect) throws Exception {

        Page page = dtoHttpConnect.getWebClient().getPage(dtoHttpConnect.getUrlNode().getUrl());
        // set
        dtoHttpConnect.setPage(page);

        WebResponse response = page.getWebResponse();


        response.getResponseHeaders();

        // set
        dtoHttpConnect.setHttpStatus(String.valueOf(response.getStatusCode()));

        //4.判断响应状态为200，进行处理
        if (response.getStatusCode() == HttpStatus.SC_OK) {

            String charset = String.valueOf(page.getWebResponse().getContentCharset());
            String contentType = String.valueOf(page.getWebResponse().getContentType());


            page.getWebResponse().getResponseHeaders();




            InputStream in = page.getWebResponse().getContentAsStream();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = in.read(buffer)) > 0){
                os.write(buffer, 0, count);
            }

            // set
            dtoHttpConnect.setResponse(os.toByteArray());
            os.close();

            // set
            dtoHttpConnect.setHtml(response.getContentAsString());
            dtoHttpConnect.setCharset(charset);
            dtoHttpConnect.setContentType(contentType);

            findUrlAndStoreData(dtoHttpConnect);



            logger.info(Integer.toString(dtoHttpConnect.getHtml().length()));

            return true;
        } else {
            //如果返回状态不是200，比如404（页面不存在）等，根据情况做处理，这里略
            logger.info("返回状态不是200");
            logger.info(response.getContentAsString());
        }
        // 如果没有抛异常都暂时认为是此url可以访问的，因为有许多异步加载js等

        return false;

    }



    public boolean httpClientGet(DTOHttpConnect dtoHttpConnect) throws Exception {
        HttpClientHelper httpClientHelper = dtoHttpConnect.getHttpClientHelper();
        String url = dtoHttpConnect.getUrlNode().getUrl();
        url = url.replace("|", "%7c");
        url = url.replace("{", "%7b");
        url = url.replace("}", "%7d");

        HttpResult httpResult = httpClientHelper.get(url);

        // set
        dtoHttpConnect.setHttpStatus(String.valueOf(httpResult.getStatuCode()));
        dtoHttpConnect.setHttpResult(httpResult);


        //4.判断响应状态为200，进行处理
        if (httpResult.getStatuCode() == HttpStatus.SC_OK) {
            dtoHttpConnect.setHtml(httpResult.getHtml());
            dtoHttpConnect.setResponse(httpResult.getResponse());
            dtoHttpConnect.setCharset(httpResult.getHeaderCharset());
            dtoHttpConnect.setContentType(httpResult.getHeaderContentType());

            findUrlAndStoreData(dtoHttpConnect);

        } else {
            //如果返回状态不是200，比如404（页面不存在）等，根据情况做处理，这里略
            logger.info("返回状态不是200");
        }




        return true;
    }

    public boolean crawlerProcess(DTOHttpConnect dtoHttpConnect) throws Exception {
        boolean status = false;
        if (EnumDict.EXECTOR_TYPE_HTMLUNIT.getCode().equals(dtoHttpConnect.getExectorType())) {
            status = htmlUnitGet(dtoHttpConnect);
        } else if (EnumDict.EXECTOR_TYPE_HTTPCLIENT.getCode().equals(dtoHttpConnect.getExectorType())) {
            status = httpClientGet(dtoHttpConnect);
        } else { //  默认
            status = htmlUnitGet(dtoHttpConnect);
        }



        return status;
    }


    public void startCrawler() {

        DTOHttpConnect dtoHttpConnect = new DTOHttpConnect(null, null);
//        dtoHttpConnect.setUrl(URL);
        dtoHttpConnect.setUrlNode(urlQueue.poll());
        long errCnt = 0;
        long allMaxCnt = MAX_TIMES * IpConfigManager.idleIpNodes.size()+1;
        long thisTimeCnt = THIS_TIMES_CNT+1;

        logger.info("Thread.currentThread().getName():" + Thread.currentThread().getName() + " Id: " + Thread.currentThread().getId());
        logger.info("httpConnect-" + atomicLong.get() + " start:" + Instant.now() + " url:" + dtoHttpConnect.getUrlNode().getUrl());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("stopwatch-httpConnect-" + atomicLong.get());

        atomicLong.addAndGet(1);
        logger.info("atomicLong:" + atomicLong.get());


        try {
            // setClient
            setClient(dtoHttpConnect);



            logger.info("setClient-dtoHttpConnect.getIpNodeSiteDate():" + dtoHttpConnect.getIpNodeSiteDate().toString());

            int cnt = 0;
            thisTimeCnt *= 10;
            while (cnt < allMaxCnt && cnt < thisTimeCnt) {
                if (EnumDict.URL_HTML.getCode().equals(allLinks.get(dtoHttpConnect.getUrlNode().getUrl()).getType())) {
                    cnt = cnt + 10;
                }else {
                    cnt = cnt + 1;
                }

                // 随机睡眠 10-20s
                if (THIS_TIMES_CNT >= 50) {
                    // 3-10s
                    Thread.sleep((long) (3 * 1000 + Math.random() * 10000));
                } else {
                    //1-5s
                    Thread.sleep((long) (1 * 1000 + Math.random() * 4000));
                }

                for (errCnt = 0; errCnt < errorCnt; errCnt++) {

                    // 默认1-2s
                    Thread.sleep((long) (1 * 1000 + Math.random() * 1000));

                    logger.info("connect url same frequency times:" + "cnt:" + cnt + ";errcnt:" + errCnt + "; url:" + dtoHttpConnect.getUrlNode().getUrl());
                    try {
                        boolean status=false;

                        // 测试模式指定url
//                        dtoHttpConnect.setUrl("xxx"); // 测试模式指定url



                        status = crawlerProcess(dtoHttpConnect);


                        if (status) {
                            break;
                        }
                        errCnt++;
                    } catch (Exception e) {
                        logger.error("Exception--------: getPage");
                        e.printStackTrace();
                    }finally {

                    }
                }


                setAfterStatusStep(dtoHttpConnect);




                dtoHttpConnect.clean();


                if (urlQueue.isEmpty()) {
                    logger.info("ttttt2222222-getUrlQueue().isEmpty():");
                    break;
                } else {
                    dtoHttpConnect.setUrlNode(urlQueue.poll());
                }

            }
        } catch (Exception e) {
            logger.info("Exception-tttttt222:");
            e.printStackTrace();
        } finally {
            dtoHttpConnect.getWebClient().close();

            saveAllIpNodeSites();
            saveAllLinks();
//            cralwerConfig.saveAllIpNodes(cralwerConfig.getAllIpNodeSites());
            if (null != dtoHttpConnect.getPage()) {
                dtoHttpConnect.getPage().cleanUp();
            }
        }

        stopWatch.stop();

    }





}
