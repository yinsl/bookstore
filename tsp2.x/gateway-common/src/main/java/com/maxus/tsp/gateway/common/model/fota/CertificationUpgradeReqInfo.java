package com.maxus.tsp.gateway.common.model.fota;

/**
 * @ClassName CertificationUpgradeReqInfo
 * @Description 平台请求网关证书更新主题入参model
 * @Author ssh
 * @Date 2019/1/31 9:51
 * @Version 1.0
 **/
public class CertificationUpgradeReqInfo {
    private String sn;
    private String cmd;
    private Integer type;
    private Integer size;
    private String certification;
    private String seqNo;
    private long eventTime;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
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
}
