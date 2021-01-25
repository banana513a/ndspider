package com.ndcf.spider.crawler.bo;

import com.ndcf.spider.crawler.common.enumeration.EnumDict;
import com.ndcf.spider.crawler.common.util.DateTimeUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * hash(url)
 */

@Getter
@Setter
public class UrlNode {
    private String  url;
    private String parentUrl;
    private String originalUrl;
    private String type;
    private String tag;
    private String status;
    private String insertDate;
    private String updateDate;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlNode urlNode = (UrlNode) o;
        return Objects.equals(url, urlNode.url);
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(url);
    }

    private UrlNode() {

    }


    public UrlNode(String url, String parentUrl,String type,String tag, String originalUrl) {
        this.url = url;
        this.parentUrl = parentUrl==null?url:parentUrl;
        this.type = type;
        this.tag = tag;
        this.originalUrl = originalUrl;

        //
        this.status= EnumDict.WAIT.getCode();
        this.insertDate = DateTimeUtil.getCurrentDateTimeMsStr();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("UrlNode{");
        sb.append("url='").append(url).append('\'');
        sb.append(", parentUrl='").append(parentUrl).append('\'');
        sb.append(", originalUrl='").append(originalUrl).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", tag='").append(tag).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", insertDate='").append(insertDate).append('\'');
        sb.append(", updateDate='").append(updateDate).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
