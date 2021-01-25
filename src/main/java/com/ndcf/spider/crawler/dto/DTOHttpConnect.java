package com.ndcf.spider.crawler.dto;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.ndcf.spider.crawler.bo.IpNodeSiteDate;
import com.ndcf.spider.crawler.bo.UrlNode;
import com.ndcf.spider.crawler.common.enumeration.EnumDict;
import com.ndcf.spider.crawler.httpclient.HttpClientHelper;
import com.ndcf.spider.crawler.httpclient.HttpResult;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class DTOHttpConnect {
    private IpNodeSiteDate ipNodeSiteDate;
    private UrlNode urlNode; // allLinks 引用同一对象，赋值影响
    private String exectorType;

    private WebClient webClient;
    private Page page;


    private HttpClientHelper httpClientHelper;
    private HttpResult httpResult;


    private String httpStatus;
    private String charset;
    private String contentType;
    private String html;
    private byte[] response; //  Arrays.copyOf(this.response, this.response.length);

    private EnumDict runStsEnum = EnumDict.RUN;


    public byte[] getResponse() {
        return Arrays.copyOf(this.response, this.response.length);
    }

    public void setResponse(byte[] response) {
        this.response = response;
    }

    public DTOHttpConnect(IpNodeSiteDate ipNodeSiteDate, WebClient webClient) {
        this.ipNodeSiteDate = ipNodeSiteDate;
        this.webClient = webClient;

        //
//        this.exectorType = EnumDict.EXECTOR_TYPE_HTMLUNIT.getCode();

    }


    public void clean() {
        this.httpStatus = null;
        this.html = null;
        this.response = null;
        this.httpResult = null;
        this.contentType = null;
        this.charset = null;
    }

}