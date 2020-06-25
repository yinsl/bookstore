package com.maxus.tsp.platform.service.model.vo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.maxus.tsp.common.constant.OTARealTimeConstants;
import com.maxus.tsp.common.enums.DCStatusEnum;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * RealTimeDataFuelCell数据
 *
 * @author lzgea
 */
public class RealTimeDataFuelCell implements Serializable {

    private static final long serialVersionUID = -8811355227246776848L;
    private static Logger logger = LogManager.getLogger(RealTimeDataFuelCell.class);
    public int byteDatalength = 18;// 字节长度为17
    @JSONField(serialize = false)
    private byte[] oriFuelCellInfoBytes;
    @JSONField(serialize = false)
    private byte[] gbDataFuelCell;
    @JSONField(serialize = false)
    private boolean dataCorrect = true;
    private int probeTempBaseNum = 40;//温度偏移量
    private int hydrogenMaxBaseTemp = 40;//温度偏移量
    private float fuelConsumePercentBaseNum = 0.01f;//燃料消耗率单位
    private float floatNum = 0.1f;//单位

    private float fuelCellVolt;                                //燃料电池电压
    private float fuelCellCurrent;                            //燃料电池电流
    private float fuelConsumePercent;                        //燃料消耗率
    private int tempProbeCnt;                                //燃料电池温度探针总数
    private int[] probeTempList;                            //燃料电池温度探针探针温度值
    private float hydrogenMaxTemp;                            //氢系统中最高温度
    private int hydrogenMaxTempProbe;                        //氢系统中最高温度探针代号
    private int hydrogenMaxConcentrate;                        //氢气最高浓度
    private int hydrogenMaxConcentrateId;                    //氢气最高浓度传感器代号
    private float hydrogenMaxPressure;                        //氢气最高压力
    private int hydrogenMaxPressureId;                        //氢气最高压力传感器代号
    private int hVDCDC;                                        //高压DC/DC状态
    @JSONField(serialize = false)
    private List<CodeValue> realTimeDataFuelCellList;
    //判断是否符合国标
    @JSONField(serialize = false)
    private boolean isDataFitGB = false;

    @JSONField(serialize = false)
    public String getRealTimeDataFuelCellJSONInfo() {
        //if (isAnalysisOk()) {
        return JSONObject.toJSONString(realTimeDataFuelCellList, SerializerFeature.WriteMapNullValue);
        //}
        //return OTARealTimeConstants.INVALID_JSNINFO;
    }

    @JSONField(serialize = false)
    public byte[] getGBdatagramBytes() {
        if (isDataFitGB) {
            return gbDataFuelCell;
        } else {
            return null;
        }
    }

    public RealTimeDataFuelCell() {
        dataCorrect = false;
    }

    public RealTimeDataFuelCell(byte[] datagramBytes, String tboxsn) {
        realTimeDataFuelCellList = new ArrayList<>();
        isDataFitGB = false;
        if (datagramBytes.length < byteDatalength) {
            dataCorrect = false;
            logger.warn("TBox(SN:{})RealTimeDataFuelCell 报文长度错误", tboxsn);
        }

        //解析tempProbeCnt
        try {
            // 获取探针数量
            byte[] _tempProbeCnt = new byte[2];
            System.arraycopy(datagramBytes, 6, _tempProbeCnt, 0, OTARealTimeConstants.UINT16_OFFSET);
            logger.debug("TBox(SN:{}):国标燃料电池数据中燃料电池温度探针总数为{}", tboxsn, ByteUtil.getUnsignedInt(_tempProbeCnt));
            if (ByteUtil.getUnsignedInt(_tempProbeCnt) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
                tempProbeCnt = 0;
                realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.TEMP_PROBE_CNT, OTARealTimeConstants.INVALID_UINT16_STRING));
            } else if (ByteUtil.getUnsignedInt(_tempProbeCnt) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
                tempProbeCnt = 0;
                realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.TEMP_PROBE_CNT, OTARealTimeConstants.INVALID_UINT16_ERROR));
            } else {
                tempProbeCnt = ByteUtil.getUnsignedInt(_tempProbeCnt);
                realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.TEMP_PROBE_CNT, tempProbeCnt));
            }
            byteDatalength += tempProbeCnt;
            oriFuelCellInfoBytes = new byte[byteDatalength];
            oriFuelCellInfoBytes = datagramBytes;

            // 检查整体字节数
            if (datagramBytes.length == byteDatalength) {
                //长度符合，先封装国标数据
                isDataFitGB = true;
                gbDataFuelCell = new byte[byteDatalength + 1];
                System.arraycopy(oriFuelCellInfoBytes, 0, gbDataFuelCell, 1, byteDatalength);
                gbDataFuelCell[0] = OTARealTimeConstants.GB_DATA_FUEL_CELL_MARK;

                byte[] _FuelCellVolt = new byte[2];
                byte[] _FuelCellCurrent = new byte[2];
                byte[] _FuelConsumePercent = new byte[2];
                byte[] _ProbeTempList = new byte[tempProbeCnt];
                byte[] _HydrogenMaxTemp = new byte[2];
                byte[] _HydrogenMaxTempProbe = new byte[2];
                byte[] _HydrogenMaxConcentrate = new byte[2];
                byte[] _HydrogenMaxConcentrateId = new byte[2];
                byte[] _HydrogenMaxPressure = new byte[2];
                byte[] _HydrogenMaxPressureId = new byte[2];
                byte[] _HVDCDC = new byte[2];
                DecimalFormat df1 = new DecimalFormat("#.0");
                DecimalFormat df4 = new DecimalFormat("#.0000");
//                realTimeDataFuelCellList = new ArrayList<>();

                //解析FuelCellVolt
                System.arraycopy(oriFuelCellInfoBytes, 0, _FuelCellVolt, 0, OTARealTimeConstants.UINT16_OFFSET);
                logger.debug("TBox(SN:{}):国标燃料电池数据中燃料电池电压为{}", tboxsn, ByteUtil.getUnsignedInt(_FuelCellVolt));
                if (ByteUtil.getUnsignedInt(_FuelCellVolt) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.FUEL_CELL_VOLT, OTARealTimeConstants.INVALID_UINT16_STRING));
                } else if (ByteUtil.getUnsignedInt(_FuelCellVolt) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.FUEL_CELL_VOLT, OTARealTimeConstants.INVALID_UINT16_ERROR));
                } else {
                    fuelCellVolt = Float.parseFloat(df1.format(ByteUtil.getUnsignedInt(_FuelCellVolt) * floatNum));
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.FUEL_CELL_VOLT,
                            fuelCellVolt));
                }

                //解析FuelCellCurrent
                System.arraycopy(oriFuelCellInfoBytes, 2, _FuelCellCurrent, 0, OTARealTimeConstants.UINT16_OFFSET);
                logger.debug("TBox(SN:{}):国标燃料电池数据中燃料电池电流为{}", tboxsn, ByteUtil.getUnsignedInt(_FuelCellCurrent));
                if (ByteUtil.getUnsignedInt(_FuelCellCurrent) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.FUEL_CELL_CURRENT, OTARealTimeConstants.INVALID_UINT16_STRING));
                } else if (ByteUtil.getUnsignedInt(_FuelCellCurrent) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.FUEL_CELL_CURRENT, OTARealTimeConstants.INVALID_UINT16_ERROR));
                } else {
                    fuelCellCurrent = Float
                            .parseFloat(df1.format(ByteUtil.getUnsignedInt(_FuelCellCurrent) * floatNum));
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.FUEL_CELL_CURRENT,
                            fuelCellCurrent));
                }

                //解析FuelConsumePercent
                System.arraycopy(oriFuelCellInfoBytes, 4, _FuelConsumePercent, 0, OTARealTimeConstants.UINT16_OFFSET);
                logger.debug("TBox(SN:{}):国标燃料电池数据中燃料消耗率为{}", tboxsn, ByteUtil.getUnsignedInt(_FuelConsumePercent));
                if (ByteUtil.getUnsignedInt(_FuelConsumePercent) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.FUEL_CONSUMEPERCENT, OTARealTimeConstants.INVALID_UINT16_STRING));
                } else if (ByteUtil.getUnsignedInt(_FuelConsumePercent) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.FUEL_CONSUMEPERCENT, OTARealTimeConstants.INVALID_UINT16_ERROR));
                } else {
                    fuelConsumePercent = Float.parseFloat(
                            df4.format(ByteUtil.getUnsignedInt(_FuelConsumePercent))) * fuelConsumePercentBaseNum;
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.FUEL_CONSUMEPERCENT,
                            fuelConsumePercent));
                }

                //解析ProbeTempList(tempProbeCnt=0 -> ProbeTempList=[])
                if (tempProbeCnt != 0) {
                    System.arraycopy(oriFuelCellInfoBytes, 8, _ProbeTempList, 0, tempProbeCnt);
//                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.TEMP_PROBE_CNT,
//                            tempProbeCnt));
                    probeTempList = new int[tempProbeCnt];
                    for (int i = 0; i < tempProbeCnt; i++) {
                        probeTempList[i] = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_ProbeTempList[i])) - probeTempBaseNum;
                    }
                } else {
                    probeTempList = new int[tempProbeCnt];
                }
                realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.PROB_TEMP_LIST,
                        probeTempList));

                //解析HydrogenMaxTemp
                System.arraycopy(oriFuelCellInfoBytes, (8 + tempProbeCnt), _HydrogenMaxTemp, 0,
                        OTARealTimeConstants.UINT16_OFFSET);
                logger.debug("TBox(SN:{}):国标燃料电池数据中氢系统中最高温度为{}", tboxsn, ByteUtil.getUnsignedInt(_HydrogenMaxTemp));
                if (ByteUtil.getUnsignedInt(_HydrogenMaxTemp) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_TEMP, OTARealTimeConstants.INVALID_UINT16_STRING));
                } else if (ByteUtil.getUnsignedInt(_HydrogenMaxTemp) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_TEMP, OTARealTimeConstants.INVALID_UINT16_ERROR));
                } else {
                    hydrogenMaxTemp = Float.parseFloat(
                            df1.format(ByteUtil.getUnsignedInt(_HydrogenMaxTemp) * floatNum - hydrogenMaxBaseTemp));
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_TEMP,
                            hydrogenMaxTemp));
                }

                //解析HydrogenMaxTempProbe
                System.arraycopy(oriFuelCellInfoBytes, (10 + tempProbeCnt), _HydrogenMaxTempProbe, 1,
                        OTARealTimeConstants.UINT8_OFFSET);
                logger.debug("TBox(SN:{}):国标燃料电池数据中氢系统中最高温度探针代号为{}", tboxsn, ByteUtil.byteToHex(_HydrogenMaxTempProbe[1]));
                ;
                hydrogenMaxTempProbe = ByteUtil.getUnsignedInt(_HydrogenMaxTempProbe);
                if (hydrogenMaxTempProbe == OTARealTimeConstants.INVALID_ZERO_VALUE) {
                    //0为无效值
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_TEMP_PROBE, OTARealTimeConstants.INVALID_BYTE_STRING));
                } else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_HydrogenMaxTempProbe[1])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_TEMP_PROBE, OTARealTimeConstants.INVALID_BYTE_STRING));
                } else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_HydrogenMaxTempProbe[1])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_TEMP_PROBE, OTARealTimeConstants.INVALID_BYTE_ERROR));
                } else {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_TEMP_PROBE,
                            hydrogenMaxTempProbe));
                }

                //解析HydrogenMaxConcentrate
                System.arraycopy(oriFuelCellInfoBytes, (11 + tempProbeCnt), _HydrogenMaxConcentrate, 0,
                        OTARealTimeConstants.UINT16_OFFSET);
                logger.debug("TBox(SN:{}):国标燃料电池数据中氢气最高浓度为{}", tboxsn, ByteUtil.getUnsignedInt(_HydrogenMaxConcentrate));
                ;
                if (ByteUtil.getUnsignedInt(_HydrogenMaxConcentrate) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_CONCENTRATE, OTARealTimeConstants.INVALID_UINT16_STRING));
                } else if (ByteUtil.getUnsignedInt(_HydrogenMaxConcentrate) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_CONCENTRATE, OTARealTimeConstants.INVALID_UINT16_ERROR));
                } else {
                    hydrogenMaxConcentrate = ByteUtil.getUnsignedInt(_HydrogenMaxConcentrate);
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_CONCENTRATE,
                            hydrogenMaxConcentrate));
                }

                //解析HydrogenMaxConcentrateId
                System.arraycopy(oriFuelCellInfoBytes, (13 + tempProbeCnt), _HydrogenMaxConcentrateId, 1,
                        OTARealTimeConstants.UINT8_OFFSET);
                logger.debug("TBox(SN:{}):国标燃料电池数据中氢气最高浓度传感器代号为{}", tboxsn, ByteUtil.byteToHex(_HydrogenMaxConcentrateId[1]));
                ;
                hydrogenMaxConcentrateId = ByteUtil.getUnsignedInt(_HydrogenMaxConcentrateId);
                if (hydrogenMaxConcentrateId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
                    //0为无效值
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_CONCENTRATEID, OTARealTimeConstants.INVALID_BYTE_STRING));
                } else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_HydrogenMaxConcentrateId[1])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_CONCENTRATEID, OTARealTimeConstants.INVALID_BYTE_STRING));
                } else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_HydrogenMaxConcentrateId[1])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_CONCENTRATEID, OTARealTimeConstants.INVALID_BYTE_ERROR));
                } else {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_CONCENTRATEID,
                            hydrogenMaxConcentrateId));
                }

                //解析HydrogenMaxPressure
                System.arraycopy(oriFuelCellInfoBytes, (14 + tempProbeCnt), _HydrogenMaxPressure, 0,
                        OTARealTimeConstants.UINT16_OFFSET);
                hydrogenMaxPressure = Float
                        .parseFloat(df1.format(ByteUtil.getUnsignedInt(_HydrogenMaxPressure) * floatNum));
                realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_PRESSURE,
                        hydrogenMaxPressure));

                //解析HydrogenMaxPressureId
                System.arraycopy(oriFuelCellInfoBytes, (16 + tempProbeCnt), _HydrogenMaxPressureId, 1,
                        OTARealTimeConstants.UINT8_OFFSET);
                logger.debug("TBox(SN:{}):国标燃料电池数据中氢气最高压力传感器代号为{}", tboxsn, ByteUtil.byteToHex(_HydrogenMaxPressureId[1]));                ;
                hydrogenMaxPressureId = ByteUtil.getUnsignedInt(_HydrogenMaxPressureId);
                if (hydrogenMaxPressureId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
                    //0为无效值
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_PRESSUREID, OTARealTimeConstants.INVALID_BYTE_STRING));
                } else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_HydrogenMaxPressureId[1])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_PRESSUREID, OTARealTimeConstants.INVALID_BYTE_STRING));
                } else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_HydrogenMaxPressureId[1])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_PRESSUREID, OTARealTimeConstants.INVALID_BYTE_ERROR));
                } else {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HYDROGEN_MAX_PRESSUREID,
                            hydrogenMaxPressureId));
                }

                //解析HVDCDC
                System.arraycopy(oriFuelCellInfoBytes, (17 + tempProbeCnt), _HVDCDC, 1,
                        OTARealTimeConstants.UINT8_OFFSET);
                if (_HVDCDC[1] == OTARealTimeConstants.DCSTATUS_INVALID) {
                    realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HVDCDC,
                            DCStatusEnum.INVALID.getValue()));
                } else {
                    hVDCDC = ByteUtil.getUnsignedInt(_HVDCDC);
                    switch (hVDCDC) {
                        case OTARealTimeConstants.RUNNING:
                            realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HVDCDC,
                                    DCStatusEnum.RUNNING.getValue()));
                            break;
                        case OTARealTimeConstants.DISCONNECTED:
                            realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HVDCDC,
                                    DCStatusEnum.DISCONNECTED.getValue()));
                            break;
                        case OTARealTimeConstants.INVALID_BYTE_ERROR:
                            logger.debug("TBox(SN:{}):国标燃料电池数据中高压DC/DC状态为{}", tboxsn, DCStatusEnum.ERROR.getValue());
                            realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HVDCDC,
                                    DCStatusEnum.ERROR.getValue()));
                            break;
                        default:
                            //取值范围外的其他值,原值上传
                            logger.debug("TBox(SN:{}):国标燃料电池数据中高压DC/DC状态为{}", tboxsn, hVDCDC);
                            realTimeDataFuelCellList.add(new CodeValue(OTARealTimeConstants.HVDCDC, hVDCDC));
                            break;
                    }
                }
            } else {
                dataCorrect = false;
                logger.warn("TBox(SN:{}):RealTimeDataFuelCell 报文长度错误", tboxsn);
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{}): RealTimeDataFuelCell 解析报文出错，原因:{}", tboxsn,
                    ThrowableUtil.getErrorInfoFromThrowable(e));
            dataCorrect = false;
        }
    }

    public String converToString(int[] data) {
        String converdata = "";
        for (int i = 0; i < data.length; i++) {
            if (i == 0) {
                converdata += data[i];
            } else {
                converdata += ("," + data[i]);
            }
        }
        // System.out.println("---------"+converdata);
        return converdata;
    }

    public float getFuelCellVolt() {
        return fuelCellVolt;
    }

    public void setFuelCellVolt(float fuelCellVolt) {
        this.fuelCellVolt = fuelCellVolt;
    }

    public float getFuelCellCurrent() {
        return fuelCellCurrent;
    }

    public void setFuelCellCurrent(float fuelCellCurrent) {
        this.fuelCellCurrent = fuelCellCurrent;
    }

    public float getFuelConsumePercent() {
        return fuelConsumePercent;
    }

    public void setFuelConsumePercent(float fuelConsumePercent) {
        this.fuelConsumePercent = fuelConsumePercent;
    }

    public int getTempProbeCnt() {
        return tempProbeCnt;
    }

    public void setTempProbeCnt(int tempProbeCnt) {
        this.tempProbeCnt = tempProbeCnt;
    }

    public int[] getProbeTempList() {
        return probeTempList;
    }

    public void setProbeTempList(int[] probeTempList) {
        this.probeTempList = probeTempList;
    }

    public float getHydrogenMaxTemp() {
        return hydrogenMaxTemp;
    }

    public void setHydrogenMaxTemp(float hydrogenMaxTemp) {
        this.hydrogenMaxTemp = hydrogenMaxTemp;
    }

    public int getHydrogenMaxTempProbe() {
        return hydrogenMaxTempProbe;
    }

    public void setHydrogenMaxTempProbe(int hydrogenMaxTempProbe) {
        this.hydrogenMaxTempProbe = hydrogenMaxTempProbe;
    }

    public int getHydrogenMaxConcentrate() {
        return hydrogenMaxConcentrate;
    }

    public void setHydrogenMaxConcentrate(int hydrogenMaxConcentrate) {
        this.hydrogenMaxConcentrate = hydrogenMaxConcentrate;
    }

    public int getHydrogenMaxConcentrateId() {
        return hydrogenMaxConcentrateId;
    }

    public void setHydrogenMaxConcentrateId(int hydrogenMaxConcentrateId) {
        this.hydrogenMaxConcentrateId = hydrogenMaxConcentrateId;
    }

    public float getHydrogenMaxPressure() {
        return hydrogenMaxPressure;
    }

    public void setHydrogenMaxPressure(float hydrogenMaxPressure) {
        this.hydrogenMaxPressure = hydrogenMaxPressure;
    }

    public int getHydrogenMaxPressureId() {
        return hydrogenMaxPressureId;
    }

    public void setHydrogenMaxPressureId(int hydrogenMaxPressureId) {
        this.hydrogenMaxPressureId = hydrogenMaxPressureId;
    }

    public int gethVDCDC() {
        return hVDCDC;
    }

    public void sethVDCDC(int hVDCDC) {
        this.hVDCDC = hVDCDC;
    }

    @JSONField(serialize = false)
    public boolean isAnalysisOk() {
        return dataCorrect;
    }

    // public String getInfo () {
    //
    // String info= "---fuelCellVolt: "+ getFuelCellCurrent() +"\n"+
    // "---fuelCellCurrent: "+getFuelCellCurrent()+"\n"+
    // "---fuelConsumePercent: "+getFuelConsumePercent()+"\n"+
    // "---tempProbeCnt: "+getTempProbeCnt()+"\n"+
    // "---probeTempList: "+probeTempList[0]+"\n"+
    // "---hydrogenMaxTemp: "+getHydrogenMaxTemp()+"\n"+
    // "---hydrogenMaxTempProbe: "+getHydrogenMaxTempProbe()
    // +"\n"+"---hydrogenMaxConcentrate:"+hydrogenMaxConcentrate+"\n"+
    // "---hydrogenMaxConcentrateId:"+hydrogenMaxConcentrateId+"\n"+
    // "---hydrogenMaxPressure: "+hydrogenMaxPressure+"\n"
    // +"---hydrogenMaxPressureId: "+hydrogenMaxPressureId+"\n"+
    // "---hVDCDC: "+hVDCDC;
    // return info;
    // }

}
