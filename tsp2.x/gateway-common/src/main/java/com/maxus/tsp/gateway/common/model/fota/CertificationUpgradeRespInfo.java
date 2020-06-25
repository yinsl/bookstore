package com.maxus.tsp.gateway.common.model.fota;

/**
 * @ClassName CertificationUpgradeRespInfo
 * @Description 平台请求网关证书更新返回主题的model
 * @Author ssh
 * @Date 2019/2/2 14:48
 * @Version 1.0
 **/
public class CertificationUpgradeRespInfo {
    private String sn;
    private String status;
    private Object data;
    private String description;
    private String seqNo;
    private Long eventTime;

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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    public Long getEventTime() {
        return eventTime;
    }

    public void setEventTime(Long eventTime) {
        this.eventTime = eventTime;
    }
}
