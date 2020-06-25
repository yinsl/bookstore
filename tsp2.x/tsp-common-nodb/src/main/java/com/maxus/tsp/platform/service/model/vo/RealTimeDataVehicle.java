package com.maxus.tsp.platform.service.model.vo;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.maxus.tsp.common.util.MathUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.maxus.tsp.common.constant.OTARealTimeConstants;
import com.maxus.tsp.common.enums.ChargingStatusEnum;
import com.maxus.tsp.common.enums.DCDCStatusEnum;
import com.maxus.tsp.common.enums.GearPosEnum;
import com.maxus.tsp.common.enums.RunningModelEnum;
import com.maxus.tsp.common.enums.VehicleEngineStatusEnum;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;

/**
 * @author lzgea
 * @Description: 整车报文数据(CMD_UP_REALTIME_DATA_VEHICLE)
 */
public class RealTimeDataVehicle implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2306092968360440538L;
    private static Logger logger = LogManager.getLogger(RealTimeDataVehicle.class);
    @JSONField(serialize = false)
    public int byteDatalength = 20;// VehicleInfo信息长度为20字节
    @JSONField(serialize = false)
    private byte[] oriVehicleInfoBytes = new byte[byteDatalength];
    @JSONField(serialize = false)
    private byte[] gbDataVehicle = new byte[OTARealTimeConstants.GB_DATA_VEHICLE_LENTH + 1];
    private boolean dataCorrect = true;
    private float vehicleBaseNumber = 0.1f;// 单位
    private float totalCurrentOffSet = 1000.0f;//电流偏移量

    private int vehicleStatus;                //引擎状态/车辆状态
    private int chargeStatus;                //充电状态
    private int powerType;                    //运行模式
    private float vehicleSpeed;                //车辆速度
    private double odo;                        //累计里程
    private float totalVoltage;                //总电压
    private float totalCurrent;                //总电流
    private int soc;                        //SOC
    private int dCDCStatus;                    //DC-DC状态
    private int gearPos;//档位值
    private int gearPosBrake;//有无制动值
    private int gearPosDrive;//有无驱动值
    private long insulateResist;            //绝缘电阻
    private int accePedal;                    //加速踏板行程值
    private int brakePedal;                    //制动踏板状态值
    private String gear;                    //档位／变速器状态
    private boolean hasDriveForce;//有无驱动力
    private boolean hasBrakingForce;//有无制动力
    //判断是否符合国标
    @JSONField(serialize = false)
    private boolean isDataFitGB = false;
    @JSONField(serialize = false)
    private List<Object> realTimeDataVehicleList;

    @JSONField(serialize = false)
    public String getRealTimeDataVehicleJSONInfo() {
        //if (isAnalysisOk()) {
        return JSONObject.toJSONString(realTimeDataVehicleList, SerializerFeature.WriteMapNullValue);
        //}
        //return OTARealTimeConstants.INVALID_JSNINFO;
    }

    public RealTimeDataVehicle() {
        dataCorrect = false;
    }

    @JSONField(serialize = false)
    public byte[] getGBdatagramBytes() {
        if (isDataFitGB) {
            return gbDataVehicle;
        } else {
            return null;
        }
    }

    public RealTimeDataVehicle(byte[] datagramBytes, String tboxsn) {
        isDataFitGB = false;
        if (datagramBytes.length == byteDatalength) {

            //长度正确直接封装国标数据
            isDataFitGB = true;
            oriVehicleInfoBytes = datagramBytes;
            System.arraycopy(oriVehicleInfoBytes, 0, gbDataVehicle, 1, OTARealTimeConstants.GB_DATA_VEHICLE_LENTH);
            gbDataVehicle[0] = OTARealTimeConstants.GB_DATA_VEHICLE_MARK;

            // 报文长度正确，尝试提取数据
            byte[] _VehicleStatus = new byte[2];
            byte[] _ChargeStatus = new byte[2];
            byte[] _PowerType = new byte[2];
            byte[] _VehicleSpeed = new byte[2];
            byte[] _Odo = new byte[4];
            byte[] _TotalVoltage = new byte[2];
            byte[] _TotalCurrent = new byte[2];
            byte[] _SOC = new byte[2];
            byte[] _DCDCStatus = new byte[2];
            byte[] _GearPos = new byte[1];
            byte[] _InsulateResist = new byte[2];
            byte[] _AccePedal = new byte[2];
            byte[] _BrakePedal = new byte[2];
            DecimalFormat df = new DecimalFormat("#.0");
            realTimeDataVehicleList = new ArrayList<>();

            try {
                // 提取并判断VehicleStatus是否无效
                System.arraycopy(oriVehicleInfoBytes, 0, _VehicleStatus, 1, OTARealTimeConstants.UINT8_OFFSET);
                if (_VehicleStatus[1] == OTARealTimeConstants.VEHICLE_STATUS_INVALID) {
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.VEHICLE_STATUS,
                            VehicleEngineStatusEnum.INVALID.getValue()));
                } else {
                    vehicleStatus = ByteUtil.getUnsignedInt(_VehicleStatus);
                    switch (vehicleStatus) {
                        case OTARealTimeConstants.VEHICLE_STATUS_STARTED:
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.VEHICLE_STATUS,
                                    VehicleEngineStatusEnum.STARTED.getValue()));
                            break;
                        case OTARealTimeConstants.VEHICLE_STATUS_STOPPED:
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.VEHICLE_STATUS,
                                    VehicleEngineStatusEnum.STOPPED.getValue()));
                            break;
                        case OTARealTimeConstants.VEHICLE_STATUS_OTHER:
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.VEHICLE_STATUS,
                                    VehicleEngineStatusEnum.OTHER.getValue()));
                            break;
                        case OTARealTimeConstants.INVALID_BYTE_ERROR:
                            logger.debug("TBox(SN:{}):国标整车数据中车辆状态为{}", tboxsn, VehicleEngineStatusEnum.ERROR.getValue());
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.VEHICLE_STATUS,
                                    VehicleEngineStatusEnum.ERROR.getValue()));
                            break;
                        default:
                            //取值范围外，原值上传
                            logger.debug("TBox(SN:{}):国标整车数据中车辆状态为{}", tboxsn, vehicleStatus);
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.VEHICLE_STATUS, vehicleStatus));
                            break;
                    }
                }

                // 判断ChargeStatus
                System.arraycopy(oriVehicleInfoBytes, 1, _ChargeStatus, 1, OTARealTimeConstants.UINT8_OFFSET);
                if (_ChargeStatus[1] == OTARealTimeConstants.CHARGE_STATUS_CHARGING_INVALID) {
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.CHARGE_STATUS,
                            ChargingStatusEnum.CHARGING_INVALID.getValue()));
                } else {
                    chargeStatus = ByteUtil.getUnsignedInt(_ChargeStatus);
                    switch (chargeStatus) {
                        case OTARealTimeConstants.CHARGE_STATUS_CHARGING_STOPPED:
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.CHARGE_STATUS,
                                    ChargingStatusEnum.CHARGING_STOPPED.getValue()));
                            break;
                        case OTARealTimeConstants.CHARGE_STATUS_CHARGING_DRIVING:
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.CHARGE_STATUS,
                                    ChargingStatusEnum.CHARGING_DRIVING.getValue()));
                            break;
                        case OTARealTimeConstants.CHARGE_STATUS_NO_CHARGING:
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.CHARGE_STATUS,
                                    ChargingStatusEnum.NO_CHARGING.getValue()));
                            break;
                        case OTARealTimeConstants.CHARGE_STATUS_CHARGING_FINISH:
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.CHARGE_STATUS,
                                    ChargingStatusEnum.CHARGING_FINISH.getValue()));
                            break;
                        case OTARealTimeConstants.INVALID_BYTE_ERROR:
                            logger.debug("TBox(SN:{}):国标整车数据中充电状态为{}", tboxsn, ChargingStatusEnum.CHARGING_ERROR.getValue());
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.CHARGE_STATUS,
                                    ChargingStatusEnum.CHARGING_ERROR.getValue()));
                            break;
                        default:
                            //国标取值外，原值上传
                            logger.debug("TBox(SN:{}):国标整车数据中充电状态为{}", tboxsn, chargeStatus);
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.CHARGE_STATUS, chargeStatus));
                            break;
                    }
                }

                //_PowerType
                System.arraycopy(oriVehicleInfoBytes, 2, _PowerType, 1, OTARealTimeConstants.UINT8_OFFSET);
                if (_PowerType[1] == OTARealTimeConstants.CHARGE_STATUS_CHARGING_INVALID) {
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.POWER_TYPE,
                            RunningModelEnum.INVALID.getValue()));
                } else {
                    powerType = ByteUtil.getUnsignedInt(_PowerType);
                    switch (powerType) {
                        case OTARealTimeConstants.RUNNING_MODEL_EV:
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.POWER_TYPE,
                                    RunningModelEnum.EV.getValue()));
                            break;
                        case OTARealTimeConstants.RUNNING_MODEL_PHEV:
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.POWER_TYPE,
                                    RunningModelEnum.PHEV.getValue()));
                            break;
                        case OTARealTimeConstants.RUNNING_MODEL_FV:
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.POWER_TYPE,
                                    RunningModelEnum.FV.getValue()));
                            break;
                        case OTARealTimeConstants.INVALID_BYTE_ERROR:
                            logger.debug("TBox(SN:{}):国标整车数据中运行模式为{}", tboxsn, RunningModelEnum.ERROR.getValue());
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.POWER_TYPE,
                                    RunningModelEnum.ERROR.getValue()));
                            break;
                        default:
                            //国标取值范围外，原值上传
                            logger.debug("TBox(SN:{}):国标整车数据中运行模式为{}", tboxsn, powerType);
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.POWER_TYPE, powerType));
                            break;
                    }
                }

                // 判断_VehicleSpeed 0.1km/h
                System.arraycopy(oriVehicleInfoBytes, 3, _VehicleSpeed, 0, OTARealTimeConstants.UINT16_OFFSET);
                logger.debug("TBox(SN:{}):国标整车数据中车速为{}", tboxsn, ByteUtil.getUnsignedInt(_VehicleSpeed));
                if (ByteUtil.getUnsignedInt(_VehicleSpeed) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.VEHICLE_SPEED, OTARealTimeConstants.INVALID_UINT16_STRING));
                } else if (ByteUtil.getUnsignedInt(_VehicleSpeed) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.VEHICLE_SPEED, OTARealTimeConstants.INVALID_UINT16_ERROR));
                } else {
                    vehicleSpeed = Float
                            .parseFloat(df.format(ByteUtil.getUnsignedInt(_VehicleSpeed) * vehicleBaseNumber));
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.VEHICLE_SPEED, vehicleSpeed));
                }

                // 判断Odo 0.1km
                System.arraycopy(oriVehicleInfoBytes, 5, _Odo, 0, OTARealTimeConstants.UINT32_OFFSET);
                logger.debug("TBox(SN:{}):国标整车数据中累计里程为{}", tboxsn, ByteUtil.getUnsignedLong(_Odo));
                if (ByteUtil.getUnsignedLong(_Odo) == OTARealTimeConstants.INVALID_UINT32_VALUE) {
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.ODO, OTARealTimeConstants.INVALID_UINT32_STRING));
                } else if (ByteUtil.getUnsignedLong(_Odo) == OTARealTimeConstants.INVALID_UINT32_ERROR) {
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.ODO, OTARealTimeConstants.INVALID_UINT32_ERROR));
                } else {
                    double mul = MathUtil.mul(ByteUtil.getUnsignedLong(_Odo), 0.1);
                    String format = df.format(mul);
                    odo = Double.parseDouble(format);
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.ODO, odo));
                }

                // 判断_TotalVoltage 0.1v
                System.arraycopy(oriVehicleInfoBytes, 9, _TotalVoltage, 0, OTARealTimeConstants.UINT16_OFFSET);
                logger.debug("TBox(SN:{}):国标整车数据中总电压为{}", tboxsn, ByteUtil.getUnsignedInt(_TotalVoltage));
                if (ByteUtil.getUnsignedInt(_TotalVoltage) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.TOTAL_VOLTAGE, OTARealTimeConstants.INVALID_UINT16_STRING));
                } else if (ByteUtil.getUnsignedInt(_TotalVoltage) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.TOTAL_VOLTAGE, OTARealTimeConstants.INVALID_UINT16_ERROR));
                } else {
                    totalVoltage = Float
                            .parseFloat(df.format(ByteUtil.getUnsignedInt(_TotalVoltage) * vehicleBaseNumber));
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.TOTAL_VOLTAGE, totalVoltage));
                }

                // 判断_TotalCurrent 1000A
                System.arraycopy(oriVehicleInfoBytes, 11, _TotalCurrent, 0, OTARealTimeConstants.UINT16_OFFSET);
                logger.debug("TBox(SN:{}):国标整车数据中总电流为{}", tboxsn, ByteUtil.getUnsignedInt(_TotalCurrent));
                if (ByteUtil.getUnsignedInt(_TotalCurrent) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.TOTAL_CURRENT, OTARealTimeConstants.INVALID_UINT16_STRING));
                } else if (ByteUtil.getUnsignedInt(_TotalCurrent) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.TOTAL_CURRENT, OTARealTimeConstants.INVALID_UINT16_ERROR));
                } else {
                    totalCurrent = Float.parseFloat(df.format(
                            (ByteUtil.getUnsignedInt(_TotalCurrent)) * vehicleBaseNumber - totalCurrentOffSet));
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.TOTAL_CURRENT, totalCurrent));
                }

                // _SOC
                System.arraycopy(oriVehicleInfoBytes, 13, _SOC, 1, OTARealTimeConstants.UINT8_OFFSET);
                logger.debug("TBox(SN:{}):国标整车数据中SOC为{}", tboxsn, Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_SOC[1])));
                if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_SOC[1])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.SOC, OTARealTimeConstants.INVALID_BYTE_STRING));
                    //test true
                } else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_SOC[1])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.SOC, OTARealTimeConstants.INVALID_BYTE_ERROR));
                } else {
                    soc = ByteUtil.getUnsignedInt(_SOC);
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.SOC, soc));
                }

                // _DCDCStatus
                System.arraycopy(oriVehicleInfoBytes, 14, _DCDCStatus, 1, OTARealTimeConstants.UINT8_OFFSET);
                if (_DCDCStatus[1] == OTARealTimeConstants.DCDC_STATUS_INVALID) {
                    realTimeDataVehicleList
                            .add(new CodeValue(OTARealTimeConstants.DCDC_STATUS, DCDCStatusEnum.INVALID.getValue()));
                } else {
                    dCDCStatus = ByteUtil.getUnsignedInt(_DCDCStatus);
                    switch (dCDCStatus) {
                        case OTARealTimeConstants.DCDC_STATUS_OFF:
                            realTimeDataVehicleList
                                    .add(new CodeValue(OTARealTimeConstants.DCDC_STATUS, DCDCStatusEnum.OFF.getValue()));
                            break;
                        case OTARealTimeConstants.DCDC_STATUS_NORMAL:
                            realTimeDataVehicleList
                                    .add(new CodeValue(OTARealTimeConstants.DCDC_STATUS, DCDCStatusEnum.NORMAL.getValue()));
                            break;
                        case OTARealTimeConstants.INVALID_BYTE_ERROR:
                            logger.debug("TBox(SN:{}):国标整车数据中DC-DC状态为{}", tboxsn, DCDCStatusEnum.ERROR.getValue());
                            realTimeDataVehicleList
                                    .add(new CodeValue(OTARealTimeConstants.DCDC_STATUS, DCDCStatusEnum.ERROR.getValue()));
                            break;
                        default:
                            //国标取值范围外，原值上传
                            logger.debug("TBox(SN:{}):国标整车数据中DC-DC状态为{}", tboxsn, dCDCStatus);
                            realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.DCDC_STATUS, dCDCStatus));
                            break;
                    }
                }

                // gearPos
                String gearStatus = null;
                System.arraycopy(oriVehicleInfoBytes, 15, _GearPos, 0, OTARealTimeConstants.UINT8_OFFSET);
                gearPos = _GearPos[0] & 0x0F;
                gearPosBrake = _GearPos[0] & (byte) 0x10;
                gearPosDrive = _GearPos[0] & (byte) 0x20;
                for (GearPosEnum gearpos : GearPosEnum.values()) {
                    if ((int) gearpos.getCode() == gearPos) {
                        gear = gearpos.getValue();
                        gearStatus = gear;
                    }
                }
                if (gearStatus == null) {
                    gearStatus = String.valueOf(gearPos);
                }
                hasBrakingForce = (gearPosBrake != 0);
                hasDriveForce = (gearPosDrive != 0);
                gearStatus = gearStatus + "," + hasDriveForce + "," + hasBrakingForce;
                realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.GEAR_POS, gearStatus));

                // _InsulateResist
                System.arraycopy(oriVehicleInfoBytes, 16, _InsulateResist, 0, OTARealTimeConstants.UINT16_OFFSET);
                if (ByteUtil.getUnsignedInt(_InsulateResist) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
                    realTimeDataVehicleList
                            .add(new CodeValue(OTARealTimeConstants.INSULATE_RESIST, OTARealTimeConstants.INVALID_UINT16_STRING));
                } else {
                    insulateResist = ByteUtil.getUnsignedInt(_InsulateResist);
                    realTimeDataVehicleList
                            .add(new CodeValue(OTARealTimeConstants.INSULATE_RESIST, insulateResist));
                }

                // _AccePedal
                System.arraycopy(oriVehicleInfoBytes, 18, _AccePedal, 1, OTARealTimeConstants.UINT8_OFFSET);
                if (_AccePedal[1] == OTARealTimeConstants.INVALID_BYTE_VALUE) {
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.ACC_PEDAL, OTARealTimeConstants.INVALID_BYTE_STRING));
                } else {
                    accePedal = ByteUtil.getUnsignedInt(_AccePedal);
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.ACC_PEDAL, accePedal));
                }

                // _BrakePedal
                System.arraycopy(oriVehicleInfoBytes, 19, _BrakePedal, 1, OTARealTimeConstants.UINT8_OFFSET);
                if (_BrakePedal[1] == OTARealTimeConstants.INVALID_BYTE_VALUE) {
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.BRAKE_PEDAL, OTARealTimeConstants.INVALID_BYTE_STRING));
                } else {
                    brakePedal = ByteUtil.getUnsignedInt(_BrakePedal);
                    realTimeDataVehicleList.add(new CodeValue(OTARealTimeConstants.BRAKE_PEDAL, brakePedal));
                }
            } catch (Exception e) {
                logger.error("TBox(SN:{}):RealTimeDataVehicle 解析报文出错，原因:{}", tboxsn,
                        ThrowableUtil.getErrorInfoFromThrowable(e));
                dataCorrect = false;
            }
        } else {
            dataCorrect = false;
            logger.warn("TBox(SN:{}):RealTimeDataVehicle 报文长度错误", tboxsn);
        }
    }

    public void setVehicleStatus(int vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
    }

    public int getdCDCStatus() {
        return dCDCStatus;
    }

    public void setdCDCStatus(int dCDCStatus) {
        this.dCDCStatus = dCDCStatus;
    }

    public void setChargeStatus(int chargeStatus) {
        this.chargeStatus = chargeStatus;
    }

    public void setPowerType(int powerType) {
        this.powerType = powerType;
    }

    public void setVehicleSpeed(float vehicleSpeed) {
        this.vehicleSpeed = vehicleSpeed;
    }

    public void setOdo(double odo) {
        this.odo = odo;
    }

    public void setTotalVoltage(float totalVoltage) {
        this.totalVoltage = totalVoltage;
    }

    public void setTotalCurrent(float totalCurrent) {
        this.totalCurrent = totalCurrent;
    }

    public void setSoc(int soc) {
        this.soc = soc;
    }

    public void setGearPos(int gearPos) {
        this.gearPos = gearPos;
    }

    public void setGearPosBrake(int gearPosBrake) {
        this.gearPosBrake = gearPosBrake;
    }

    public void setGearPosDrive(int gearPosDrive) {
        this.gearPosDrive = gearPosDrive;
    }

    public void setInsulateResist(long insulateResist) {
        this.insulateResist = insulateResist;
    }

    public void setAccePedal(int accePedal) {
        this.accePedal = accePedal;
    }

    public void setBrakePedal(int brakePedal) {
        this.brakePedal = brakePedal;
    }

    public int getChargeStatus() {
        return chargeStatus;
    }

    public int getPowerType() {
        return powerType;
    }

    public float getVehicleSpeed() {
        return vehicleSpeed;
    }

    public double getOdo() {
        return odo;
    }

    public float getTotalVoltage() {
        return totalVoltage;
    }

    public float getTotalCurrent() {
        return totalCurrent;
    }

    public int getSoc() {
        return soc;
    }

    public int getGearPos() {
        return gearPos;
    }

    public int getGearPosBrake() {
        return gearPosBrake;
    }

    public int getGearPosDrive() {
        return gearPosDrive;
    }

    public long getInsulateResist() {
        return insulateResist;
    }

    public int getAccePedal() {
        return accePedal;
    }

    public int getBrakePedal() {
        return brakePedal;
    }

    public int getVehicleStatus() {
        return vehicleStatus;
    }

    @JSONField(serialize = false)
    public boolean isAnalysisOk() {
        return dataCorrect;
    }

    public String getGear() {
        return gear;
    }

    public void setGear(String gear) {
        this.gear = gear;
    }

    public boolean isHasDriveForce() {
        return hasDriveForce;
    }

    public void setHasDriveForce(boolean hasDriveForce) {
        this.hasDriveForce = hasDriveForce;
    }

    public boolean isHasBrakingForce() {
        return hasBrakingForce;
    }

    public void setHasBrakingForce(boolean hasBrakingForce) {
        this.hasBrakingForce = hasBrakingForce;
    }

}
