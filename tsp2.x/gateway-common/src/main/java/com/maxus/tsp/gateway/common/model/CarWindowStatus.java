package com.maxus.tsp.gateway.common.model;

/**
 * 封装车窗状态
 */
public class CarWindowStatus {
    private String top;
    private String leftRear;
    private String rightRear;
    private String mainDriver;
    private String deputyDriver;

    public String getTop() {
        return top;
    }

    public void setTop(String top) {
        this.top = top;
    }

    public String getLeftRear() {
        return leftRear;
    }

    public void setLeftRear(String leftRear) {
        this.leftRear = leftRear;
    }

    public String getRightRear() {
        return rightRear;
    }

    public void setRightRear(String rightRear) {
        this.rightRear = rightRear;
    }

    public String getMainDriver() {
        return mainDriver;
    }

    public void setMainDriver(String mainDriver) {
        this.mainDriver = mainDriver;
    }

    public String getDeputyDriver() {
        return deputyDriver;
    }

    public void setDeputyDriver(String deputyDriver) {
        this.deputyDriver = deputyDriver;
    }
}
