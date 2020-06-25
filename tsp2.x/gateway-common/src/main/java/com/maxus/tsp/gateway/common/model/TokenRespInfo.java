package com.maxus.tsp.gateway.common.model;

/**
 * @ClassName TokenRespInfo
 * @Description 获取token出错时返回给RVM的JSON
 * @Author zijhm
 * @Date 2019/1/15 16:59
 * @Version 1.0
 **/
public class TokenRespInfo {
    private String sn;
    private String status;
    private String date;
    private String errDesc;
    private String seqNo;
    private long eventTime;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrDesc() {
        return errDesc;
    }

    public void setErrDesc(String errDesc) {
        this.errDesc = errDesc;
    }

}
