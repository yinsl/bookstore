package com.maxus.tsp.gateway.common.model.fota;

/**
 * @ClassName VersionUpgradeRespInfo
 * @Description TBox返回Fota收到车主答复后答复报文结果的model
 * @Author zijhm
 * @Date 2019/1/28 13:30
 * @Version 1.0
 **/
public class VersionUpgradeRespInfo {
    private long id;
    private int result;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
