package com.ndcf.spider.crawler.httpclient;


import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ndcf.spider.crawler.common.util.HttpUtil.getHeaders;

/**
 * HttpClient 封装
 *
 * @author bangis.wangdf
 */
public class HttpClientHelper {

    private static Logger LOG = LoggerFactory.getLogger(HttpClientHelper.class);
    private CloseableHttpClient httpclient;


    private HttpContext localContext = new BasicHttpContext();
    private BasicCookieStore basicCookieStore = new BasicCookieStore();                          // cookie存储用来完成登录后记录相关信息

    private int TIME_OUT = 3;                                              // 连接超时时间

//    public HttpClientHelper() {
//        instance();
//    }

    /**
     * 启用cookie存储
     */
    private void instance(HttpHost httpHost) {
        HttpClientBuilder httpClientBuilder = null;
        try {
            httpClientBuilder = HttpClients.custom()
                    .setConnectionTimeToLive(10, TimeUnit.SECONDS)
                    .setHostnameVerifier(new AllowAllHostnameVerifier())
                        .setSslcontext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                            public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                                return true;
                            }
                        }).build());

            if (httpHost != null) {
                httpClientBuilder.setProxy(httpHost);
            }
            httpclient = httpClientBuilder.build();


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }



//        try {
//            httpclient = HttpClients.custom()
//                    .setHostnameVerifier(new AllowAllHostnameVerifier())
//                    .setSslcontext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
//                        public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
//                            return true;
//                        }
//                    }).build()).build();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        }

        localContext.setAttribute("http.cookie-store", basicCookieStore);// Cookie存储
//        httpGet = new HttpGet();
    }

    public Boolean setProxy() {

//        httpGet


//        HttpHost httpHost = new HttpHost();
//        httpclient.exe
        return false;

    }

    /**
     *  ssl boolean=true 支持https网址，false同默认构造
     */
    public HttpClientHelper(HttpHost httpHost) {
        boolean ssl = true;
        instance(httpHost);
//        if (ssl) {
//            try {
//                X509TrustManager tm = new X509TrustManager() {
//
//                    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
//                    }
//
//                    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
//                    }
//
//                    public X509Certificate[] getAcceptedIssuers() {
//                        return null;
//                    }
//                };
//                SSLContext ctx = SSLContext.getInstance("TLS");
//                ctx.init(null, new TrustManager[]{tm}, null);
//                SSLSocketFactory ssf = new SSLSocketFactory(ctx);
//                ClientConnectionManager ccm = httpclient.getConnectionManager();
//                SchemeRegistry sr = ccm.getSchemeRegistry();
//                sr.register(new Scheme("https", ssf, 443));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * @param url
     * @param headers 指定headers
     * @return
     */
    public HttpResult get(String url, Header... headers) {
        HttpResponse response;
        HttpGet httpGet = new HttpGet(url);
//        httpGet.setURI(URI.create(url));
        if (headers != null) {
            for (Header h : headers) {
                httpGet.addHeader(h);
            }
        } else {// 如不指定则使用默认
            Header header = new BasicHeader(
                    "User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1;  .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; InfoPath.2)");
            httpGet.addHeader(header);
        }
        HttpResult httpResult = HttpResult.empty();
        try {
            response = httpclient.execute(httpGet, localContext);
            getHeaders(response.getAllHeaders());
            httpResult = new HttpResult(localContext, response);
        } catch (IOException e) {
            LOG.error(" get ", e);
            httpGet.abort();
        }
        return httpResult;
    }

    public HttpResult post(String url, Map<String, String> data, Header... headers) {
        HttpResponse response;
        HttpPost httppost = new HttpPost(url);
        String contentType = null;
        if (headers != null) {
            int size = headers.length;
            for (int i = 0; i < size; ++i) {
                Header h = (Header) headers[i];
                if (!(h.getName().startsWith("$x-param"))) {
                    httppost.addHeader(h);
                }
                if ("Content-Type".equalsIgnoreCase(h.getName())) {
                    contentType = h.getValue();
                }
            }

        }
        if (contentType != null) {
            httppost.setHeader("Content-Type", contentType);
        } else if (data != null) {
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        }

        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        for (String key : data.keySet()) {
            formParams.add(new BasicNameValuePair(key, (String) data.get(key)));
        }
        HttpResult httpResult = HttpResult.empty();
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, "UTF-8");
            httppost.setEntity(entity);
            response = httpclient.execute(httppost, localContext);
            httpResult = new HttpResult(localContext, response);
        } catch (IOException e) {
            LOG.error(" post ", e);
            httppost.abort();
        } finally {
        }
        return httpResult;
    }

    public String getCookie(String name, String... domain) {
        String dm = "";
        if (domain != null && domain.length >= 1) {
            dm = domain[0];
        }
        for (Cookie c : basicCookieStore.getCookies()) {
            if (StringUtils.equals(name, c.getName()) && StringUtils.equals(dm, c.getDomain())) {
                return c.getValue();
            }
        }
        return null;
    }

    public void pringCookieAll() {
        for (Cookie c : basicCookieStore.getCookies()) {
            System.out.println(c);
        }
    }
}