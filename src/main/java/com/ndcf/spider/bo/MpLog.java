package com.ndcf.spider.bo;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class MpLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String username;

    private String code;

    // @Column(name = "create_time",insertable = false,updatable = false,columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    //@Column(columnDefinition = "TIMESTAMP DEFAULT SYSTIMESTAMP")
    @Column(insertable = false,updatable = false,columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createTime;

    private String status;

    private String ip;

    private String type;

    private String note;

    private String sessionid;


    //


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }


    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    //
    public MpLog() {

    }


    public MpLog(String username, Timestamp createTime, String ip, String sessionid, String status, String note) {
        this.username = username;
        this.createTime = createTime;
        this.ip = ip;
        this.sessionid=sessionid;
        this.status=status;
        this.note=note;
    }
}
