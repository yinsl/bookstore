package com.maxus.tsp.gateway.common.model;

/**
 * 用来封装除获取蓝牙回复结果以外的结果
 */
public class BlueToothCtrlRespCommonInfo {
    private String cmd;
    private Integer result;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }
}
