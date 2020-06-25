package com.maxus.tsp.gateway.common.model.fota;

import java.util.Random;

/**
 * @ClassName AgreeUpgradeReqInfo
 * @Description 平台请求网关车主AVN端是否同意升级主题入参model
 * @Author ssh
 * @Date 2019/2/14 14:18
 * @Version 1.0
 **/
public class AgreeUpgradeReqInfo {
    private String sn;

    //升级任务ID
    private long id;

    //车主选择结果(0:车主拒绝升级,1:车主同意升级,2:车主选择稍后再试,其他值:无效)
    private Integer data;

    private String seqNo;
    private long eventTime;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getData() {
        return data;
    }

    public void setData(Integer data) {
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
        this.seqNo = System.currentTimeMillis() + res.toString();
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }
}
