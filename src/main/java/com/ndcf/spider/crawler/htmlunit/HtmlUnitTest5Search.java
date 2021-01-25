package com.ndcf.spider.crawler.htmlunit;



import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * @author 网上搜索整理
 * @version 1.0.0
 * @ClassName HtmlUnitTest1Baidu.java
 * @Description TODO
 * @createTime 2020年12月29日 22:06:00
 */
public class HtmlUnitTest5Search {

    public static void main(String[] args) {
        WebClient webClient=new WebClient(BrowserVersion.FIREFOX_78); // 实例化Web客户端
        try {
            HtmlPage page=webClient.getPage("http://blog.java1234.com/index.html"); // 解析获取页面
//            HtmlForm form=page.getFormByName("myform"); // 得到搜索Form
            HtmlForm form= (HtmlForm) page.getDocumentElement().getElementsByAttribute("form","class","navbar-form navbar-right").get(0); // 得到搜索Form
//            HtmlTextInput textField=form.getInputByName("q"); // 获取查询文本框
            HtmlTextInput textField=form.getInputByName("q"); // 获取查询文本框
            HtmlButton button = (HtmlButton) form.getElementsByAttribute("button", "class", "btn btn-default").get(0); // 获取提交按钮
            textField.setValueAttribute("java"); // 文本框“填入”数据

            HtmlPage page2=button.click(); // 模拟点击
            webClient.waitForBackgroundJavaScript(5 * 1000);
            System.out.println("222ttttttttttttttttttttt");
            System.out.println(page2.asXml());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            webClient.close(); // 关闭客户端，释放内存
        }
    }
}