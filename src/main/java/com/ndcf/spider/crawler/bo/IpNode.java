package com.ndcf.spider.crawler.bo;

import com.ndcf.spider.crawler.common.enumeration.EnumDict;
import com.ndcf.spider.crawler.common.enumeration.EnumDict;
import com.ndcf.spider.crawler.common.util.DateTimeUtil;

// hash(ip,port)
public class IpNode {
    private String ipport;
    private String ip;
    private String port;

    // EnumDict
    private String type= EnumDict.IPTYPE_PROXY.getCode();;
    // EnumDict
    private String status = EnumDict.SUCESS.getCode();
    private String successCnt = "0";
    private String errorCnt = "0";
    private String cnt = "0";
    private String allSuccessCnt = "0";
    private String allErrorCnt = "0";
    private String allCnt = "0";

    private String note;
    private String insertDate;
    private String updateDate;

    private IpNode() {
    }
    public IpNode(String ip, String port) {
        this.ip = ip;
        this.port = port;
        //
        this.ipport = ip + ":" + port;

        //
        this.insertDate = DateTimeUtil.getCurrentDateTimeMsStr();
    }

    @Override
    public int hashCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(ip);
        sb.append(port);
        char[] charArr = sb.toString().toCharArray();
        int hash = 0;
        for (char c : charArr) {
            hash = hash * 131 + c;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof IpNode) {
            if (((IpNode) obj).ip.equals(this.ip)
                    && ((IpNode) obj).port.equals(this.port)) {
                return true;
            }
        }

        return false;
    }

    public String getIpport() {
        return ipport;
    }

    public void setIpport(String ipport) {
        this.ipport = ipport;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSuccessCnt() {
        return successCnt;
    }

    public void setSuccessCnt(String successCnt) {
        this.successCnt = successCnt;
    }

    public String getErrorCnt() {
        return errorCnt;
    }

    public void setErrorCnt(String errorCnt) {
        this.errorCnt = errorCnt;
    }

    public String getCnt() {
        return cnt;
    }

    public void setCnt(String cnt) {
        this.cnt = cnt;
    }

    public String getAllSuccessCnt() {
        return allSuccessCnt;
    }

    public void setAllSuccessCnt(String allSuccessCnt) {
        this.allSuccessCnt = allSuccessCnt;
    }

    public String getAllErrorCnt() {
        return allErrorCnt;
    }

    public void setAllErrorCnt(String allErrorCnt) {
        this.allErrorCnt = allErrorCnt;
    }

    public String getAllCnt() {
        return allCnt;
    }

    public void setAllCnt(String allCnt) {
        this.allCnt = allCnt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(String insertDate) {
        this.insertDate = insertDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("IpNode{");
        sb.append("ipport='").append(ipport).append('\'');
        sb.append(", ip='").append(ip).append('\'');
        sb.append(", port='").append(port).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", successCnt='").append(successCnt).append('\'');
        sb.append(", errorCnt='").append(errorCnt).append('\'');
        sb.append(", cnt='").append(cnt).append('\'');
        sb.append(", allSuccessCnt='").append(allSuccessCnt).append('\'');
        sb.append(", allErrorCnt='").append(allErrorCnt).append('\'');
        sb.append(", allCnt='").append(allCnt).append('\'');
        sb.append(", note='").append(note).append('\'');
        sb.append(", insertDate='").append(insertDate).append('\'');
        sb.append(", updateDate='").append(updateDate).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
