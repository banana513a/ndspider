package com.ndcf.spider.crawler.common.util;

import com.ndcf.spider.crawler.common.Constant.HtmlUnitConstant;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.HashMap;

import static com.ndcf.spider.crawler.common.Constant.HtmlUnitConstant.OBJPATH_HTMLUNIT;

/**
 * // 如果字符串含有=则有问题，数据问题，就好用特殊字符或者自定义的字符；
 * 使用前要分析是否含有,
 */
public class Util {
    static int seq2 = 0;

    public static String getSequence() {
        synchronized (Util.class) {
            return String.valueOf(Instant.now().toEpochMilli()) + String.valueOf(++seq2);
        }
    }

    // toString转对象 ,后续改注解扫描或者spring 容器
    public static Object strToObject(String str,String objPath) {
        Object obj=null;
        if (null == str || "".equals(str)) {
            return null;
        }

        // IpNode{ip='127.0.0.1', port='80', ipport='127.0.0.1:80', type='null', cnt='null', errorCnt='null'}
        HashMap<String, String> fieldsMap = new HashMap<>();
        String className = str.substring(0, str.indexOf("{"));
        String[] tmpFields = str.substring(str.indexOf("{") + 1, str.length() - 1).split(", ");
        String key;
        String value;
        for (int i = 0; i < tmpFields.length; i++) {
            // 如果字符串含有=则有问题，数据问题，就好用特殊字符或者自定义的字符；
            String[] tmpFields2=tmpFields[i].split("='");
            key = tmpFields2[0];
            value = tmpFields2[1].substring(0, tmpFields2[1].length() - 1);
            fieldsMap.put(key, value.toString());
        }
        try {
            Class<?> clz = Class.forName(objPath + "." + className);
            Constructor c0 = clz.getDeclaredConstructor();
            c0.setAccessible(true);
            obj = c0.newInstance();

            Field[] fields = clz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
//                fields[i].getType()
                fields[i].setAccessible(true);
                fields[i].set(obj, fieldsMap.get(fields[i].getName()));
            }
        } catch (Exception e) {
            System.out.println("Exception:"+str);
            e.printStackTrace();
        }

        return obj;
    }

    public static void saveTestFile() {
        try {
            String data = ThreadUtil.getCurThreadInfo() + " " + DateTimeUtil.getCurrentDateTimeMsStr();
            System.out.println(data);
            FileUtils.write(new File(HtmlUnitConstant.TEST_FILE), data+"\n", HtmlUnitConstant.CHARSET, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public static void main(String[] args) throws Exception {
        String str = "UrlNode{url='http://cwzx.shdjt.com/cwcx.asp?gdmc=%CF%E3%B8%DB%D6%D0%D1%EB%BD%E1%CB%E3%D3%D0%CF%DE%B9%AB%CB%BE', parentUrl='http://cwzx.shdjt.com/top500.asp', originalUrl='cwcx.asp?gdmc=%CF%E3%B8%DB%D6%D0%D1%EB%BD%E1%CB%E3%D3%D0%CF%DE%B9%AB%CB%BE', type='209', tag='210', status='3', insertDate='2021-01-01 16:08:55.420', updateDate='null'}";
        System.out.println(str.indexOf("{"));
        System.out.println(str.substring(0,str.indexOf("{")));
        System.out.println(str.substring(str.indexOf("{")+1,str.length()-1));

        System.out.println(strToObject(str,OBJPATH_HTMLUNIT).toString());


        System.out.println(DateTimeUtil.getCurrentDateTimeStr());
        System.out.println(DateTimeUtil.getCurrentDateTimeMsStr());

        System.out.println(Thread.currentThread().getName());
        System.out.println(Thread.currentThread().getId());
        System.out.println(Thread.currentThread().getThreadGroup());
        System.out.println(Thread.currentThread().toString());

        System.out.println(ThreadUtil.getCurThreadInfo());
        saveTestFile();

    }

}
