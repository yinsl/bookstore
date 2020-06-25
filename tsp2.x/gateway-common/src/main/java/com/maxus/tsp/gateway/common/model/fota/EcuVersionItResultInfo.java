package com.maxus.tsp.gateway.common.model.fota;

public class EcuVersionItResultInfo {

    //请求Sn
    private String sn = "";
    //请求指令
    private String cmd = "";

    private int value;
    //参数长度
    private int paramSize;
    //参数
    private String param;
    //序列号
    private String seqNo;
    //时间戳
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

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getParamSize() {
        return paramSize;
    }

    public void setParamSize(int paramSize) {
        this.paramSize = paramSize;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
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
