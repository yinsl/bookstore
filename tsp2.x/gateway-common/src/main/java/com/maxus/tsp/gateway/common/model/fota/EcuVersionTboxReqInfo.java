package com.maxus.tsp.gateway.common.model.fota;

public class EcuVersionTboxReqInfo {
    //请求Sn
    private String sn = "";
    //请求指令
    private String cmd = "";
    //数据长度
    private int dataSize;
    //数据
    private String data = "";
    //请求序列号
    private String seqNo = "";
    //请求发生时间
    private long eventTime;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public int getDataSize() {
        return dataSize;
    }

    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
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

    public String getCmd() {

        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public  EcuVersionTboxReqInfo(){
        super();
    }

    public EcuVersionTboxReqInfo(String sn, String cmd, int dataSize, String data) {
        super();
        this.sn = sn;
        this.cmd = cmd;
        this.dataSize = dataSize;
        this.data = data;
    }
}
