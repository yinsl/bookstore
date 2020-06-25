package com.maxus.tsp.gateway.common.model;

import java.util.List;

/**
 * @ClassName RmtGroupRespInfo
 * @Description 组合远控回包内容的封装model
 * @Author zijhm
 * @Date 2019/2/1 9:05
 * @Version 1.0
 **/
public class RmtGroupRespInfo {
    private String errorCode;
    private List<RmtGroupResultInfo> result;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public List<RmtGroupResultInfo> getResult() {
        return result;
    }

    public void setResult(List<RmtGroupResultInfo> result) {
        this.result = result;
    }
}
