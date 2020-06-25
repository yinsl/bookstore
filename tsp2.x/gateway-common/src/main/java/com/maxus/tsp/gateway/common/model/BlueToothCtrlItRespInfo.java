package com.maxus.tsp.gateway.common.model;

/**
 * @ClassName: BlueToothCtrlItRespInfo.java
 * @Description: 蓝牙控制执行后返回it的结果封装类
 * @author: zijhm
 * @date: 2018/12/12 9:00
 * @version: 1.0
 */
public class BlueToothCtrlItRespInfo {
    private String sn;
    private String status;
    //data的存值情况：BlueToothCtrlRespInfo
    private Object data;
    private String description;
    private String seqNo;
    private long eventTime;

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

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }
}
