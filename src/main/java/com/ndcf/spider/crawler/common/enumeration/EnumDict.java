package com.ndcf.spider.crawler.common.enumeration;

import lombok.Getter;
import lombok.Setter;

/**
 * CD XX XXX,
 */
@Getter
public enum EnumDict {

    // 0-10 状态
    SUCESS("0", "SUCESS"),
    FAILURE("4", "FAILURE"),
    WAIT("3", "WAIT"),
    UNAVAILABLE("2", "暂不可用"),

    URL_HTML("200", "网页"),
    URL_MEDIA("201", "MEDIA"),
    URL_UNKNOW("209", "URL类型未知"),
    URL_TYPE_HREF("210", "HREF"),
    URL_TYPE_TAG("211", "URL_TAG"),

    //
    IPTYPE_PROXY("0001000","代理IP"),
    IPTYPE_LOCAL("0001001","本机"),

    RUN("0002000","可运行"),
    UNRUN("0002001","不可运行"),


    EXECTOR_TYPE_HTMLUNIT("0003001", "HTMLUNIT"),
    EXECTOR_TYPE_HTTPCLIENT("0003002", "HTTPCLIENT"),



    ;

    private String code;
    private String name;

    EnumDict(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
