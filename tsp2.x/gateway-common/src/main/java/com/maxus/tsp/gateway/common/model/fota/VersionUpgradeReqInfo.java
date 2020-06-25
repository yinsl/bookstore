package com.maxus.tsp.gateway.common.model.fota;

/**
 * @ClassName VersionUpgradeReqInfo
 * @Description 平台请求网关车主答复下发主题入参model
 * @Author zijhm
 * @Date 2019/1/24 16:36
 * @Version 1.0
 **/
public class VersionUpgradeReqInfo{
    private String sn;
    private Integer id;
    private Integer operate;
    private String seqNo;
    private long eventTime;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOperate() {
        return operate;
    }

    public void setOperate(Integer operate) {
        this.operate = operate;
    }
}
