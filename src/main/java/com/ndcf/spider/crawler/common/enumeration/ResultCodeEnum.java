package com.ndcf.spider.crawler.common.enumeration;

import lombok.Getter;

/**
 * @author zsmj
 * @version 1.0.0
 * @ClassName ResultCodeEnum.java
 * @Description TODO
 * @createTime 2020年12月13日 21:35:00
 */
@Getter
public enum ResultCodeEnum {
    SUCCESS(true,20000,"成功"),
    UNKNOWN_ERROR(false,20001,"未知错误"),
    PARAM_ERROR(false,20002,"参数错误"),
    NULL_POINT(false,20003,"NULL_POINT"),
    HTTP_CLIENT_ERROR(false,20004,"HTTP_CLIENT_ERROR"),
    HTTP_SET_PROXY_ERROR(false,20005,"设置代理失败"),
    ;





    // 响应是否成功
    public Boolean success;
    // 响应状态码
    public Integer code;
    // 响应信息
    public String message;

    ResultCodeEnum(boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }



}