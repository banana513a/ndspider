package com.ndcf.spider.crawler.htmlunit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.ndcf.spider.crawler.common.Constant.HtmlUnitConstant.PATH_TEST;

public class FileUtil {
    public static void toFile (String content) {
        File file = null;
        FileWriter fw = null;
        file = new File(PATH_TEST+"aaa.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file,true);
            fw.write(content);//向文件中复制内容
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(fw != null){
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}