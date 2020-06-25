package com.maxus.tsp.gateway.common.model;

/**
 * @ClassName RmtGroupRequestInfo
 * @Description 远控组合请求入参model
 * @Author zijhm
 * @Date 2019/1/31 16:26
 * @Version 1.0
 **/
public class RmtGroupRequestInfo {
    private String sn;
    private String otaType;
    
    //otaType为REMOTECTRL时的参数
    private String comd;
    private String value;
    private String temperature;
    //otaType为REMOTECTRL_EXT时的参数
    private Integer paramSize;
    private String param;
    
    private String seqNo;
    private long eventTime;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getOtaType() {
        return otaType;
    }

    public void setOtaType(String otaType) {
        this.otaType = otaType;
    }

    public String getComd() {
        return comd;
    }

    public void setComd(String comd) {
        this.comd = comd;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
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

	public Integer getParamSize() {
		return paramSize;
	}

	public void setParamSize(Integer paramSize) {
		this.paramSize = paramSize;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}
}
