package com.maxus.tsp.gateway.common.model.fota;

/**
 * @ClassName ProgressItRespInfo
 * @Description 进度上报结果返回model
 * @Author zijhm
 * @Date 2019/2/18 15:01
 * @Version 1.0
 **/
public class ProgressItRespInfo {
    private String sn;
    //上报指令
    private String cmd;
    //进度上报结果
    private int result;
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

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
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
