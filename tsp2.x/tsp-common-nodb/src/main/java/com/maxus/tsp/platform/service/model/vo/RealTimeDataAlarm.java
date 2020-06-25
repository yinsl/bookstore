package com.maxus.tsp.platform.service.model.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.maxus.tsp.common.constant.OTARealTimeConstants;
import com.maxus.tsp.common.enums.AlertLevelEnum;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;

/**
 * RealTimeDataAlarm数据
 *
 * @author lzgea
 */
public class RealTimeDataAlarm implements Serializable {

    private static final long serialVersionUID = -4751720908824968044L;
    private static Logger logger = LogManager.getLogger(RealTimeDataAlarm.class);
    @JSONField(serialize = false)
    private int byteDatalength = 5;//报警类报文基础长度
    @JSONField(serialize = false)
    private byte[] oriAlarmInfoBytes;//原始报文
    @JSONField(serialize = false)
    private byte[] gbDataAlarm;//国标标准报警报文
    @JSONField(serialize = false)
    private boolean dataCorrect = true;//解析是否成功标志
    @JSONField(serialize = false)
    private boolean isDataFitGB = false;//原始报文是否符合国标

    private long GeneralAlarmFlag;//通用报警标志

    private boolean Alarm_DifferTemp = false;//	bit0：温度差异报警
    private boolean Alarm_CellHighTemp = false;//	bit1：电池高温报警
    private boolean Alarm_EngineContainerOverPressure = false;//	bit2: 车载储能装置类型过压报警
    private boolean Alarm_EngineContainerLowPressure = false;//	bit3: 车载储能装置类型欠压报警

    private boolean Alarm_lowSOC = false;//	bit4: SOC低报警.
    private boolean Alarm_SingleCellHighVoltage = false;//	bit5：单体电池过压报警
    private boolean Alarm_SingleCellLowVoltage = false;//	bit6: 单体电池欠压报警
    private boolean Alarm_HighSOC = false;//	bit7: SOC过高报警.

    private boolean Alarm_UnstableSOC = false;//	bit8: SOC 跳变报警.
    private boolean Alarm_UnmatchableCellContainer = false;//	bit9：可充电储能系统不匹配报警.
    private boolean Alarm_SingleCellDiffer = false;//	bit10 电池单体一致性差报警.
    private boolean Alarm_CellInsulation = false;//	bit11 绝缘报警.

    private boolean Alarm_DCDCTemp = false;//	bit12 DCDC 温度报警
    private boolean Alarm_BrakeSystem = false;//	bit13 制动系统报警.
    private boolean Alarm_DCDCStatus = false;//	bit14  DCDC 状态报警
    private boolean Alarm_DriveMotorContrlTemp = false;//	bit15  驱动电机控制器温度报警

    private boolean Alarm_LockedByHighVoltage = false;//	bit16  高压互锁状态报警
    private boolean Alarm_DriveMotorTemp = false;//	bit17  驱动电机温度报警
    private boolean Alarm_EngineContainerOverCharge = false;//	bit18  车载储能装置类型过充


    private int ChargeDevFaultCnt;//可充电储能装置故障总数
    private long[] ChargeDevFaultList;//可充电储能装置故障列表
    private int MotorFaultCnt;//驱动电机故障总数
    private long[] MotorFaultList;//驱动电机故障列表
    private int EngineFaultCnt;//发动机故障总数
    private long[] EngineFaultList;//发动机故障总数列表
    private int OtherFaultCnt;//其它故障总数
    private long[] OtherFaultList;//其它故障总数列表
    private int MaxAlarmLevel;//最高报警等级
    @JSONField(serialize = false)
    private List<CodeValue> realTimeDataTempAlarmlist;

    @JSONField(serialize = false)
    public String getRealTimeDataAlarmJSONInfo() {
        if (isAnalysisOk()) {
            return JSONObject.toJSONString(realTimeDataTempAlarmlist, SerializerFeature.WriteMapNullValue);
        }
        return OTARealTimeConstants.INVALID_JSNINFO;
    }

    @JSONField(serialize = false)
    public byte[] getGBdatagramBytes() {
        if (isDataFitGB) {
            return gbDataAlarm;
        } else {
            return null;
        }
    }

    public RealTimeDataAlarm() {
        dataCorrect = false;
    }

    public RealTimeDataAlarm(byte[] datagramBytes, String tboxsn) {
    	
        //check length
        isDataFitGB = false;
        if (datagramBytes.length < byteDatalength) {
            dataCorrect = false;
            logger.warn("TBox(SN:{})RealTimeDataAlarm 报文长度错误", tboxsn);
        } else {

            try {
                realTimeDataTempAlarmlist = new ArrayList<>();
                oriAlarmInfoBytes = datagramBytes;
                byte[] _ChargeDevFaultCnt = new byte[2];
                byte[] _MotorFaultCnt = new byte[2];
                byte[] _EngineFaultCnt = new byte[2];
                byte[] _OtherFaultCnt = new byte[2];
                byte[] _MaxAlarmLevel = new byte[2];
                byte[] _GeneralAlarmFlag = new byte[4];

                //解析ChargeDevFaultCnt
                System.arraycopy(oriAlarmInfoBytes, 5, _ChargeDevFaultCnt, 1, OTARealTimeConstants.UINT8_OFFSET);
                logger.debug("TBox(SN:{}):国标报警数据中可充电储能装置故障总数为{}", tboxsn,ByteUtil.byteToHex(_ChargeDevFaultCnt[1]));
                if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_ChargeDevFaultCnt[1])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.CHARGEDEV_FAULT_CNT, OTARealTimeConstants.INVALID_BYTE_STRING));
                } else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_ChargeDevFaultCnt[1])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.CHARGEDEV_FAULT_CNT, OTARealTimeConstants.INVALID_BYTE_ERROR));
                } else {
                    ChargeDevFaultCnt = ByteUtil.getUnsignedInt(_ChargeDevFaultCnt);
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.CHARGEDEV_FAULT_CNT, ChargeDevFaultCnt));
                }

                //解析MotorFaultCnt
                System.arraycopy(oriAlarmInfoBytes, (6 + ChargeDevFaultCnt * 4), _MotorFaultCnt,
                        1, OTARealTimeConstants.UINT8_OFFSET);
                logger.debug("TBox(SN:{}):国标报警数据中驱动电机故障总数为{}", tboxsn,ByteUtil.byteToHex(_MotorFaultCnt[1]));
                if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MotorFaultCnt[1])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.MOTOR_FAULT_CNT, OTARealTimeConstants.INVALID_BYTE_STRING));
                } else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MotorFaultCnt[1])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.MOTOR_FAULT_CNT, OTARealTimeConstants.INVALID_BYTE_ERROR));
                } else {
                    MotorFaultCnt = ByteUtil.getUnsignedInt(_MotorFaultCnt);
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.MOTOR_FAULT_CNT, MotorFaultCnt));
                }

                //解析EngineFaultCnt
                System.arraycopy(oriAlarmInfoBytes, (7 + ChargeDevFaultCnt * 4 +
                        MotorFaultCnt * 4), _EngineFaultCnt, 1, OTARealTimeConstants.UINT8_OFFSET);
                logger.debug("TBox(SN:{}):国标报警数据中发动机故障总数为{}", tboxsn,ByteUtil.byteToHex(_EngineFaultCnt[1]));
                if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_EngineFaultCnt[1])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ENGINE_FAULT_CNT, OTARealTimeConstants.INVALID_BYTE_STRING));
                } else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_EngineFaultCnt[1])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ENGINE_FAULT_CNT, OTARealTimeConstants.INVALID_BYTE_ERROR));
                } else {
                    EngineFaultCnt = ByteUtil.getUnsignedInt(_EngineFaultCnt);
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ENGINE_FAULT_CNT, EngineFaultCnt));
                }

                //解析OtherFaultCnt
                System.arraycopy(oriAlarmInfoBytes, (8 + ChargeDevFaultCnt * 4 +
                        MotorFaultCnt * 4 + EngineFaultCnt * 4), _OtherFaultCnt, 1, OTARealTimeConstants.UINT8_OFFSET);
                logger.debug("TBox(SN:{}):国标报警数据中其他故障总数为{}", tboxsn,ByteUtil.byteToHex(_OtherFaultCnt[1]));
                if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_OtherFaultCnt[1])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.OTHER_FAULT_CNT, OTARealTimeConstants.INVALID_BYTE_STRING));
                } else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_OtherFaultCnt[1])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.OTHER_FAULT_CNT, OTARealTimeConstants.INVALID_BYTE_ERROR));
                } else {
                    OtherFaultCnt = ByteUtil.getUnsignedInt(_OtherFaultCnt);
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.OTHER_FAULT_CNT, OtherFaultCnt));
                }


                byteDatalength = 9 + (ChargeDevFaultCnt + MotorFaultCnt + EngineFaultCnt
                        + OtherFaultCnt) * 4;
                if (byteDatalength == oriAlarmInfoBytes.length) {
                    //长度正确开始封装国标数据
                    isDataFitGB = true;
                    gbDataAlarm = new byte[byteDatalength + 1];
                    gbDataAlarm[0] = OTARealTimeConstants.GB_DATA_ALARM_MARK;
                    System.arraycopy(oriAlarmInfoBytes, 0, gbDataAlarm, 1, byteDatalength);

                    //解析MaxAlarmLevel
                    System.arraycopy(oriAlarmInfoBytes, 0, _MaxAlarmLevel, 1, OTARealTimeConstants.UINT8_OFFSET);
                    if (_MaxAlarmLevel[1] == OTARealTimeConstants.INVALID_BYTE_VALUE) {
                        realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.MAX_ALARM_LEVEL,
                                AlertLevelEnum.INVALID.getValue()));
                    } else {
                        MaxAlarmLevel = ByteUtil.getUnsignedInt(_MaxAlarmLevel);
                        
                        switch (MaxAlarmLevel) {
                            case OTARealTimeConstants.ALERT_LEVEL_0:
                                realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.MAX_ALARM_LEVEL,
                                        AlertLevelEnum.LEVEL_0.getValue()));
                                break;
                            case OTARealTimeConstants.ALERT_LEVEL_1:
                                realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.MAX_ALARM_LEVEL,
                                        AlertLevelEnum.LEVEL_1.getValue()));
                                break;
                            case OTARealTimeConstants.ALERT_LEVEL_2:
                                realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.MAX_ALARM_LEVEL,
                                        AlertLevelEnum.LEVEL_2.getValue()));
                                break;
                            case OTARealTimeConstants.ALERT_LEVEL_3:
                                realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.MAX_ALARM_LEVEL,
                                        AlertLevelEnum.LEVEL_3.getValue()));
                                break;
                            case OTARealTimeConstants.INVALID_BYTE_ERROR:
                            	logger.debug("TBox(SN:{}):国标报警数据中最高报警等级为{}", tboxsn, AlertLevelEnum.ERROR.getValue());
                                realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.MAX_ALARM_LEVEL,
                                        AlertLevelEnum.ERROR.getValue()));
                                break;
                            default:
                                //在国标范围之外的数值，原值上传
                            	logger.debug("TBox(SN:{}):国标报警数据中最高报警等级为{}", tboxsn, MaxAlarmLevel);
                                realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.MAX_ALARM_LEVEL, MaxAlarmLevel));
                                break;
                        }
                    }

                    //解析通用报警标志位
                    System.arraycopy(oriAlarmInfoBytes, 1, _GeneralAlarmFlag, 0, OTARealTimeConstants.UINT32_OFFSET);
                    if ((_GeneralAlarmFlag[3] & 0x01) == 0x01)
                        Alarm_DifferTemp = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_DIFFERTEMP, Alarm_DifferTemp));

                    if ((_GeneralAlarmFlag[3] & 0x02) == 0x02)
                        Alarm_CellHighTemp = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_CELLHIGHTEMP, Alarm_CellHighTemp));

                    if ((_GeneralAlarmFlag[3] & 0x04) == 0x04)
                        Alarm_EngineContainerOverPressure = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_ENGINECONTAINER_OVER_PRESSURE,
                            Alarm_EngineContainerOverPressure));

                    if ((_GeneralAlarmFlag[3] & 0x08) == 0x08)
                        Alarm_EngineContainerLowPressure = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_ENGINECONTAINER_LOW_PRESSURE,
                            Alarm_EngineContainerLowPressure));


                    if ((_GeneralAlarmFlag[3] & 0x10) == 0x10)
                        Alarm_lowSOC = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_LOWSOC,
                            Alarm_lowSOC));

                    if ((_GeneralAlarmFlag[3] & 0x20) == 0x20)
                        Alarm_SingleCellHighVoltage = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_SINGLECELL_HIGHVOTAGE,
                            Alarm_SingleCellHighVoltage));

                    if ((_GeneralAlarmFlag[3] & 0x40) == 0x40)
                        Alarm_SingleCellLowVoltage = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_SINGLECELL_LOWVOTAGE,
                            Alarm_SingleCellLowVoltage));

                    if ((_GeneralAlarmFlag[3] & 0x80) == 0x80)
                        Alarm_HighSOC = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_HIGHSOC,
                            Alarm_HighSOC));


                    if ((_GeneralAlarmFlag[2] & 0x01) == 0x01)
                        Alarm_UnstableSOC = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_UNSTABLESOC,
                            Alarm_UnstableSOC));

                    if ((_GeneralAlarmFlag[2] & 0x02) == 0x02)
                        Alarm_UnmatchableCellContainer = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_UNMATCHABLE_CELLCONTAINER,
                            Alarm_UnmatchableCellContainer));

                    if ((_GeneralAlarmFlag[2] & 0x04) == 0x04)
                        Alarm_SingleCellDiffer = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_SINGLECELLDIFFER,
                            Alarm_SingleCellDiffer));

                    if ((_GeneralAlarmFlag[2] & 0x08) == 0x08)
                        Alarm_CellInsulation = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_CELLINSULATION,
                            Alarm_CellInsulation));


                    if ((_GeneralAlarmFlag[2] & 0x10) == 0x10)
                        Alarm_DCDCTemp = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_DCDCTEMP, Alarm_DCDCTemp));

                    if ((_GeneralAlarmFlag[2] & 0x20) == 0x20)
                        Alarm_BrakeSystem = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_BRAKESYSTEM, Alarm_BrakeSystem));

                    if ((_GeneralAlarmFlag[2] & 0x40) == 0x40)
                        Alarm_DCDCStatus = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_DCDCSTATUS, Alarm_DCDCStatus));

                    if ((_GeneralAlarmFlag[2] & 0x80) == 0x80)
                        Alarm_DriveMotorContrlTemp = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_DRIVEMOTORCTRLTEMP, Alarm_DriveMotorContrlTemp));


                    if ((_GeneralAlarmFlag[1] & 0x01) == 0x01)
                        Alarm_LockedByHighVoltage = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_LOCKEDBYHIGNVOLTAGE, Alarm_LockedByHighVoltage));


                    if ((_GeneralAlarmFlag[1] & 0x02) == 0x02)
                        Alarm_DriveMotorTemp = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_DRIVEMOTORTEMP, Alarm_DriveMotorTemp));


                    if ((_GeneralAlarmFlag[1] & 0x04) == 0x04)
                        Alarm_EngineContainerOverCharge = true;
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ALARM_OVERCHARGE, Alarm_EngineContainerOverCharge));


                    byte[] _ChargeDevFaultList = new byte[ChargeDevFaultCnt * 4];
                    byte[] _MotorFaultList = new byte[MotorFaultCnt * 4];
                    byte[] _EngineFaultList = new byte[EngineFaultCnt * 4];
                    byte[] _OtherFaultList = new byte[OtherFaultCnt * 4];
                    byte[] _temp = new byte[4];
                    ChargeDevFaultList = new long[ChargeDevFaultCnt];
                    MotorFaultList = new long[MotorFaultCnt];
                    EngineFaultList = new long[EngineFaultCnt];
                    OtherFaultList = new long[OtherFaultCnt];

                    //解析ChargeDevFaultList
                    if (ChargeDevFaultCnt != 0) {
                        System.arraycopy(oriAlarmInfoBytes, 6, _ChargeDevFaultList, 0, ChargeDevFaultCnt * 4);
                        for (int i = 0; i < ChargeDevFaultCnt * 4; i += 4) {
                            for (int j = 0; j < 4; j++) {
                                _temp[j] = _ChargeDevFaultList[i + j];
                            }
                            ChargeDevFaultList[i / 4] = ByteUtil.getUnsignedLong(_temp);
                        }
                    }
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.CHARGEDEV_FAULT_LIST,
                            ChargeDevFaultList));

                    //解析MotorFaultList
                    if (MotorFaultCnt != 0) {
                        System.arraycopy(oriAlarmInfoBytes, (7 + ChargeDevFaultCnt * 4), _MotorFaultList, 0, MotorFaultCnt * 4);
                        for (int i = 0; i < MotorFaultCnt * 4; i += 4) {
                            for (int j = 0; j < 4; j++) {
                                _temp[j] = _MotorFaultList[i + j];
                            }
                            MotorFaultList[i / 4] = ByteUtil.getUnsignedLong(_temp);
                        }
                    }
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.MOTOR_FAULT_LIST,
                            MotorFaultList));

                    //解析EngineFaultList
                    if (EngineFaultCnt != 0) {
                        System.arraycopy(oriAlarmInfoBytes, (8 + ChargeDevFaultCnt * 4 + MotorFaultCnt * 4),
                                _EngineFaultList, 0, EngineFaultCnt * 4);
                        for (int i = 0; i < EngineFaultCnt * 4; i += 4) {
                            for (int j = 0; j < 4; j++) {
                                _temp[j] = _EngineFaultList[i + j];
                            }
                            EngineFaultList[i / 4] = ByteUtil.getUnsignedLong(_temp);
                        }
                    }
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.ENGINE_FAULT_LIST,
                            EngineFaultList));

                    //解析OtherFaultList
                    if (OtherFaultCnt != 0) {
                        System.arraycopy(oriAlarmInfoBytes, (9 + ChargeDevFaultCnt * 4 + MotorFaultCnt * 4 + EngineFaultCnt * 4),
                                _OtherFaultList, 0, OtherFaultCnt * 4);
                        for (int i = 0; i < OtherFaultCnt * 4; i += 4) {
                            for (int j = 0; j < 4; j++) {
                                _temp[j] = _OtherFaultList[i + j];
                            }
                            OtherFaultList[i / 4] = ByteUtil.getUnsignedLong(_temp);
                        }
                    }
                    realTimeDataTempAlarmlist.add(new CodeValue(OTARealTimeConstants.OTHER_FAULT_LIST,
                            OtherFaultList));
                    

                } else {
                    dataCorrect = false;
                }
            } catch (Exception e) {
                logger.error("TBox(SN:{}):RealTimeDataAlarm 解析报文出错，原因:{}", tboxsn,
                        ThrowableUtil.getErrorInfoFromThrowable(e));
                dataCorrect = false;
            }
        }
    }

    @JSONField(serialize = false)
    public String converToString(long[] data) {
        StringBuilder converdata = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            if (i == 0) {
                converdata.append(data[i]);
            } else {
                converdata.append(",").append(data[i]);
            }
        }
        return converdata.toString();
    }

    public boolean isAlarm_DifferTemp() {
        return Alarm_DifferTemp;
    }

    public void setAlarm_DifferTemp(boolean alarm_DifferTemp) {
        Alarm_DifferTemp = alarm_DifferTemp;
    }

    public boolean isAlarm_CellHighTemp() {
        return Alarm_CellHighTemp;
    }

    public void setAlarm_CellHighTemp(boolean alarm_CellHighTemp) {
        Alarm_CellHighTemp = alarm_CellHighTemp;
    }

    public boolean isAlarm_EngineContainerOverPressure() {
        return Alarm_EngineContainerOverPressure;
    }

    public void setAlarm_EngineContainerOverPressure(boolean alarm_EngineContainerOverPressure) {
        Alarm_EngineContainerOverPressure = alarm_EngineContainerOverPressure;
    }

    public boolean isAlarm_EngineContainerLowPressure() {
        return Alarm_EngineContainerLowPressure;
    }

    public void setAlarm_EngineContainerLowPressure(boolean alarm_EngineContainerLowPressure) {
        Alarm_EngineContainerLowPressure = alarm_EngineContainerLowPressure;
    }

    public boolean isAlarm_lowSOC() {
        return Alarm_lowSOC;
    }

    public void setAlarm_lowSOC(boolean alarm_lowSOC) {
        Alarm_lowSOC = alarm_lowSOC;
    }

    public boolean isAlarm_SingleCellHighVoltage() {
        return Alarm_SingleCellHighVoltage;
    }

    public void setAlarm_SingleCellHighVoltage(boolean alarm_SingleCellHighVoltage) {
        Alarm_SingleCellHighVoltage = alarm_SingleCellHighVoltage;
    }

    public boolean isAlarm_SingleCellLowVoltage() {
        return Alarm_SingleCellLowVoltage;
    }

    public void setAlarm_SingleCellLowVoltage(boolean alarm_SingleCellLowVoltage) {
        Alarm_SingleCellLowVoltage = alarm_SingleCellLowVoltage;
    }

    public boolean isAlarm_HighSOC() {
        return Alarm_HighSOC;
    }

    public void setAlarm_HighSOC(boolean alarm_HighSOC) {
        Alarm_HighSOC = alarm_HighSOC;
    }

    public boolean isAlarm_UnstableSOC() {
        return Alarm_UnstableSOC;
    }

    public void setAlarm_UnstableSOC(boolean alarm_UnstableSOC) {
        Alarm_UnstableSOC = alarm_UnstableSOC;
    }

    public boolean isAlarm_UnmatchableCellContainer() {
        return Alarm_UnmatchableCellContainer;
    }

    public void setAlarm_UnmatchableCellContainer(boolean alarm_UnmatchableCellContainer) {
        Alarm_UnmatchableCellContainer = alarm_UnmatchableCellContainer;
    }

    public boolean isAlarm_SingleCellDiffer() {
        return Alarm_SingleCellDiffer;
    }

    public void setAlarm_SingleCellDiffer(boolean alarm_SingleCellDiffer) {
        Alarm_SingleCellDiffer = alarm_SingleCellDiffer;
    }

    public boolean isAlarm_CellInsulation() {
        return Alarm_CellInsulation;
    }

    public void setAlarm_CellInsulation(boolean alarm_CellInsulation) {
        Alarm_CellInsulation = alarm_CellInsulation;
    }

    public boolean isAlarm_DCDCTemp() {
        return Alarm_DCDCTemp;
    }

    public void setAlarm_DCDCTemp(boolean alarm_DCDCTemp) {
        Alarm_DCDCTemp = alarm_DCDCTemp;
    }

    public boolean isAlarm_BrakeSystem() {
        return Alarm_BrakeSystem;
    }

    public void setAlarm_BrakeSystem(boolean alarm_BrakeSystem) {
        Alarm_BrakeSystem = alarm_BrakeSystem;
    }

    public boolean isAlarm_DCDCStatus() {
        return Alarm_DCDCStatus;
    }

    public void setAlarm_DCDCStatus(boolean alarm_DCDCStatus) {
        Alarm_DCDCStatus = alarm_DCDCStatus;
    }

    public boolean isAlarm_DriveMotorContrlTemp() {
        return Alarm_DriveMotorContrlTemp;
    }

    public void setAlarm_DriveMotorContrlTemp(boolean alarm_DriveMotorContrlTemp) {
        Alarm_DriveMotorContrlTemp = alarm_DriveMotorContrlTemp;
    }

    public boolean isAlarm_LockedByHighVoltage() {
        return Alarm_LockedByHighVoltage;
    }

    public void setAlarm_LockedByHighVoltage(boolean alarm_LockedByHighVoltage) {
        Alarm_LockedByHighVoltage = alarm_LockedByHighVoltage;
    }

    public boolean isAlarm_DriveMotorTemp() {
        return Alarm_DriveMotorTemp;
    }

    public void setAlarm_DriveMotorTemp(boolean alarm_DriveMotorTemp) {
        Alarm_DriveMotorTemp = alarm_DriveMotorTemp;
    }

    public boolean isAlarm_EngineContainerOverCharge() {
        return Alarm_EngineContainerOverCharge;
    }

    public void setAlarm_EngineContainerOverCharge(boolean alarm_EngineContainerOverCharge) {
        Alarm_EngineContainerOverCharge = alarm_EngineContainerOverCharge;
    }

    public int getChargeDevFaultCnt() {
        return ChargeDevFaultCnt;
    }

    public void setChargeDevFaultCnt(int chargeDevFaultCnt) {
        ChargeDevFaultCnt = chargeDevFaultCnt;
    }

    public long[] getChargeDevFaultList() {
        return ChargeDevFaultList;
    }

    public void setChargeDevFaultList(long[] chargeDevFaultList) {
        ChargeDevFaultList = chargeDevFaultList;
    }

    public int getMotorFaultCnt() {
        return MotorFaultCnt;
    }

    public void setMotorFaultCnt(int motorFaultCnt) {
        MotorFaultCnt = motorFaultCnt;
    }

    public long[] getMotorFaultList() {
        return MotorFaultList;
    }

    public void setMotorFaultList(long[] motorFaultList) {
        MotorFaultList = motorFaultList;
    }

    public int getEngineFaultCnt() {
        return EngineFaultCnt;
    }

    public void setEngineFaultCnt(int engineFaultCnt) {
        EngineFaultCnt = engineFaultCnt;
    }

    public long[] getEngineFaultList() {
        return EngineFaultList;
    }

    public void setEngineFaultList(long[] engineFaultList) {
        EngineFaultList = engineFaultList;
    }

    public int getOtherFaultCnt() {
        return OtherFaultCnt;
    }

    public void setOtherFaultCnt(int otherFaultCnt) {
        OtherFaultCnt = otherFaultCnt;
    }

    public long[] getOtherFaultList() {
        return OtherFaultList;
    }

    public void setOtherFaultList(long[] otherFaultList) {
        OtherFaultList = otherFaultList;
    }

    public int getMaxAlarmLevel() {
        return MaxAlarmLevel;
    }

    public void setMaxAlarmLevel(int maxAlarmLevel) {
        MaxAlarmLevel = maxAlarmLevel;
    }

    @JSONField(serialize = false)
    public boolean isAnalysisOk() {
        return dataCorrect;
    }

    public long getGeneralAlarmFlag() {
        return GeneralAlarmFlag;
    }

    public void setGeneralAlarmFlag(long generalAlarmFlag) {
        GeneralAlarmFlag = generalAlarmFlag;
    }

//	public String getInfo () {
//		String info= "MaxAlarmLevel: "+ MaxAlarmLevel +"\n"+
//				"GeneralAlarmFlag: "+ GeneralAlarmFlag +"\n"+
//				"ChargeDevFaultCnt: "+ ChargeDevFaultCnt +"\n"+
//				"ChargeDevFault: "+ ChargeDevFaultList +"\n"+
//				"MotorFaultCnt: "+ MotorFaultCnt +"\n"+
//				"MotorFault: "+ MotorFaultList +"\n"+
//				"EngineFaultCnt: "+ EngineFaultCnt +"\n"+
//				"EngineFault: "+ EngineFaultList[0] +"\n"+
//				"OtherFaultCnt: "+ OtherFaultCnt +"\n"+
//				"OtherFault: "+ OtherFaultList[0] +"\n"+
//				"Alarm_DifferTemp: "+ Alarm_DifferTemp +"\n"+
//				"Alarm_CellHighTemp: "+ Alarm_CellHighTemp +"\n"+
//				"Alarm_EngineContainerOverPressure: "+ Alarm_EngineContainerOverPressure +"\n"+
//				"Alarm_EngineContainerLowPressure: "+ Alarm_EngineContainerLowPressure +"\n"+
//				"Alarm_lowSOC: "+ Alarm_lowSOC +"\n"+
//				"Alarm_SingleCellHighVoltage: "+ Alarm_SingleCellHighVoltage +"\n"+
//				"Alarm_SingleCellLowVoltage: "+ Alarm_SingleCellLowVoltage +"\n"+
//				"Alarm_HighSOC: "+ Alarm_HighSOC +"\n"+
//				"Alarm_UnstableSOC: "+ Alarm_UnstableSOC +"\n"+
//				"Alarm_UnmatchableCellContainer: "+ Alarm_UnmatchableCellContainer +"\n"+
//				"Alarm_SingleCellDiffer: "+ Alarm_SingleCellDiffer +"\n"+
//				"Alarm_CellInsulation: "+ Alarm_CellInsulation +"\n"+
//				"Alarm_DCDCTemp: "+ Alarm_DCDCTemp +"\n"+
//				"Alarm_BrakeSystem: "+ Alarm_BrakeSystem +"\n"+
//				"Alarm_DCDCStatus: "+ Alarm_DCDCStatus +"\n"+
//				"Alarm_DriveMotorContrlTemp: "+ Alarm_DriveMotorContrlTemp +"\n"+
//				"Alarm_LockedByHighVoltage: "+ Alarm_LockedByHighVoltage +"\n"+
//				"Alarm_DriveMotorTemp: "+ Alarm_DriveMotorTemp +"\n"+
//				"Alarm_EngineContainerOverCharge: "+ Alarm_EngineContainerOverCharge +"\n";
//		return info;
//	}


}
