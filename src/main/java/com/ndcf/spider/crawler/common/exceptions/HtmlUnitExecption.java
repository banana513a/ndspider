package com.ndcf.spider.crawler.common.exceptions;

import com.ndcf.spider.crawler.common.enumeration.ResultCodeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * @author zsmj
 * @version 1.0.0
 * @ClassName HtmlUnitExecption.java
 * @Description TODO
 * @createTime 2020年12月13日 21:08:00
 */
@Data
@Slf4j
public class HtmlUnitExecption extends Exception implements Serializable{

    private static final long serialVersionUID = -6874690919953768457L;


    private ResultCodeEnum resultCodeEnum;

    private String code;

    private String message;


    public HtmlUnitExecption(String code,String message){
        super(message);
        this.code = code;
        this.message = message;
    }

    public HtmlUnitExecption(ResultCodeEnum resultCodeEnum){
        super(resultCodeEnum.message);
        this.resultCodeEnum = resultCodeEnum;
    }


    public HtmlUnitExecption(String code,String message,Throwable throwable){
        super(message,throwable);
        this.code = code;
        this.message = message;
    }

    public HtmlUnitExecption(ResultCodeEnum resultCodeEnum,Throwable throwable){
        super(resultCodeEnum.message,throwable);
        this.resultCodeEnum = resultCodeEnum;
    }


    /**
     * @description TODO 重写Throwable中printStackTrace方法，打印异常信息
     * @return void
     * @date 2019/8/21 下午7:57
     * @author flyingkid
     */
//    @Override
//    public void printStackTrace(){
//        if (resultCodeEnum != null){
//            log.info("异常代码: {}, 异常信息: {}",resultCodeEnum.code,resultCodeEnum.message);
//            return;
//        }
//        log.info("异常代码: {}, 异常信息: {}",code,message);
//    }

}



