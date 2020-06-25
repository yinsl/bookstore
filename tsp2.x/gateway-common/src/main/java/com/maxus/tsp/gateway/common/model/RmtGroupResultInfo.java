package com.maxus.tsp.gateway.common.model;

/**
 * @ClassName RmtGroupResultInfo
 * @Description 组合远控回包内容result的封装model
 * @Author zijhm
 * @Date 2019/2/1 9:06
 * @Version 1.0
 **/
public class RmtGroupResultInfo {
    private int cmd;
    private int result;

    public RmtGroupResultInfo(int cmd, int result) {
        this.cmd = cmd;
        this.result = result;
    }

    public RmtGroupResultInfo() {
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
