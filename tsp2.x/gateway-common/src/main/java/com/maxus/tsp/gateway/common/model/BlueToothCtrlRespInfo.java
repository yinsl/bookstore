package com.maxus.tsp.gateway.common.model;

/**
 * @ClassName: BlueToothCtrlRespInfo.java
 * @Description: Tbox返回结果的封装类
 *                  用来封装获取蓝牙钥匙结果
 * @author: zijhm
 * @date: 2018/12/12 9:02
 * @version: 1.0
 */
public class BlueToothCtrlRespInfo {
    private String cmd;
    private Integer result;
    private Integer BtKeyNum;
    private long[] BtKeyList;
    private Integer BtKeyMax;

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

    public Integer getBtKeyNum() {
        return BtKeyNum;
    }

    public void setBtKeyNum(Integer btKeyNum) {
        BtKeyNum = btKeyNum;
    }

    public long[] getBtKeyList() {
        return BtKeyList;
    }

    public void setBtKeyList(long[] btKeyList) {
        BtKeyList = btKeyList;
    }

    public Integer getBtKeyMax() {
        return BtKeyMax;
    }

    public void setBtKeyMax(Integer btKeyMax) {
        BtKeyMax = btKeyMax;
    }
}
