package com.maxus.tsp.gateway.common.model;

/**
 * @ClassName BaseFotaCtrlItResp
 * @Description Fota控制指令的返回基本格式
 * @Author zijhm
 * @Date 2019/1/23 8:59
 * @Version 1.0
 **/
public class BaseFotaCtrlItResp {
    //TBox序列号
    private String sn;
    //返回码
    private String status;
    //TBox返回结果
    private Object data;
    //返回码对应描述
    private String description;
    //请求主题中解析得到的序列号，平台可以用于确认时序
    private String seqNo;
    //请求发起时间，格式为时间戳
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
