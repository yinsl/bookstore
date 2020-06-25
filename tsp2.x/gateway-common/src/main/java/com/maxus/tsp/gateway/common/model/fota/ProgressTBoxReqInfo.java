package com.maxus.tsp.gateway.common.model.fota;

import java.util.Random;

/**
 * @ClassName DownloadProgressTBoxReqInfo
 * @Description 解析上报进度报文封装json给RVM
 * @Author zijhm
 * @Date 2019/2/15 16:14
 * @Version 1.0
 **/
public class ProgressTBoxReqInfo {
    private String sn;
    //上报指令
    private String cmd;
    private long id;
    //data中报文的长度
    private Integer dataSize;
    //存放16进制报文
    private String data;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getDataSize() {
        return dataSize;
    }

    public void setDataSize(Integer dataSize) {
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

    public void setSeqNo() {
        Random random = new Random();
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            res.append(random.nextInt(10));
        }
        this.seqNo = System.currentTimeMillis()+ res.toString();
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }
}
