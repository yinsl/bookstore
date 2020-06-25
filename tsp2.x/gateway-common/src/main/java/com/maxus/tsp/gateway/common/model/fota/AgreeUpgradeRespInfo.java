package com.maxus.tsp.gateway.common.model.fota;
/**
 * @ClassName AgreeUpgradeRespInfo
 * @Description 平台请求车主avn端是否同意升级的model
 * @Author ssh
 * @Date 2019/2/20 8:54
 * @Version 1.0
 **/
public class AgreeUpgradeRespInfo {
    private String sn;

    //升级任务ID
    private Long id;

    //结果(0:成功，1:失败)
    private Integer result;

    private String seqNo;
    private long eventTime;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
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
