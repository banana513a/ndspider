package com.ndcf.spider.crawler.bo;

import com.ndcf.spider.crawler.common.enumeration.EnumDict;
import com.ndcf.spider.crawler.common.util.DateTimeUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class HtmlFileInfoBo {
    private String url;
    private String fileName;
    private String fileNameSeqNo;
    private String md5Hex;
    private String status = EnumDict.SUCESS.getCode();

    private String insertDate;
    private String updateDate;


    public HtmlFileInfoBo(String url, String fileName, String md5Hex) {
        this.url = url;
        this.fileName = fileName;
        this.md5Hex = md5Hex;

        //
        this.insertDate = DateTimeUtil.getCurrentDateTimeMsStr();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HtmlFileInfoBo htmlFileInfoBo = (HtmlFileInfoBo) o;

        return Objects.equals(url, htmlFileInfoBo.url);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(url);
    }



    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("HtmlFileInfoBo{");
        sb.append("url='").append(url).append('\'');
        sb.append(", fileName='").append(fileName).append('\'');
        sb.append(", md5Hex='").append(md5Hex).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", insertDate='").append(insertDate).append('\'');
        sb.append(", updateDate='").append(updateDate).append('\'');
        sb.append('}');
        return sb.toString();
    }

    private HtmlFileInfoBo() {
    }
}
