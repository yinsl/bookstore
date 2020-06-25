package com.maxus.tsp.platform.service.model.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.maxus.tsp.common.constant.OTARealTimeConstants;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;

/**
 * RealTimeDataTemp数据CMD_UP_REALTIME_DATA_TEMP
 *
 * @author lzgea
 */
public class RealTimeDataTemp implements Serializable {

    private static final long serialVersionUID = 5858607276358459792L;
    private static Logger logger = LogManager.getLogger(RealTimeDataTemp.class);
    @JSONField(serialize = false)
    private int byteDatalength = 3;//报文基础字节数
    @JSONField(serialize = false)
    private byte[] oriTempInfoBytes;
    @JSONField(serialize = false)
    private boolean dataCorrect = true;
    private int temparaturesBaseNum = 40;//温度偏移量

    private int subSystemId;//可充电储能子系统号
    private int tempCnt;//温度探针个数
    private int[] temparatures;//温度值列表
    @JSONField(serialize = false)
    private Map<String, Object> realTimeDataTempMap;

    public int getSubSystemId() {
        return subSystemId;
    }

    public void setSubSystemId(int subSystemId) {
        this.subSystemId = subSystemId;
    }

    public int getTempCnt() {
        return tempCnt;
    }

    public void setTempCnt(int tempCnt) {
        this.tempCnt = tempCnt;
    }

    public int[] getTemparatures() {
        return temparatures;
    }

    public void setTemparatures(int[] temparatures) {
        this.temparatures = temparatures;
    }

    @JSONField(serialize = false)
    public String getRealTimeDataTempJSONInfo() {
        //if (isAnalysisOk()) {
        return JSONObject.toJSONString(realTimeDataTempMap, SerializerFeature.WriteMapNullValue);
        //}
        //return OTARealTimeConstants.INVALID_JSNINFO;
    }

    public RealTimeDataTemp() {
        dataCorrect = false;
    }

    public RealTimeDataTemp(byte[] datagramBytes, String tboxsn) {
        // check length
        if (datagramBytes.length < byteDatalength) {
            logger.warn("TBox(SN:{}):RealTimeDataTemp 报文长度错误", tboxsn);
            dataCorrect = false;
        } else {
            try {
                byte[] _TempCnt = new byte[2];
                realTimeDataTempMap = new HashMap<>();

                //_SubSystemId
                byte[] _SubSystemId = new byte[2];
                System.arraycopy(datagramBytes, 0, _SubSystemId, 1, OTARealTimeConstants.UINT8_OFFSET);
                subSystemId = ByteUtil.getUnsignedInt(_SubSystemId);
                if (subSystemId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
                    //0无效值
                    logger.debug("TBox(SN:{}):国标可充电储能装置温度数据中可充电储能子系统号为{}", tboxsn, 0);
                    realTimeDataTempMap.put(OTARealTimeConstants.SUB_SYSTEMID, OTARealTimeConstants.INVALID_BYTE_STRING);
                } else {
                    realTimeDataTempMap.put(OTARealTimeConstants.SUB_SYSTEMID, subSystemId);
                }

                //_TempCnt
                System.arraycopy(datagramBytes, 1, _TempCnt, 0, OTARealTimeConstants.UINT16_OFFSET);
                logger.debug("TBox(SN:{}):国标可充电储能装置温度数据中可充电储能温度探针个数为{}", tboxsn, ByteUtil.getUnsignedInt(_TempCnt));
                if (ByteUtil.getUnsignedInt(_TempCnt) == OTARealTimeConstants.INVALID_UINT16_STRING) {
                    tempCnt = (int) OTARealTimeConstants.INVALID_UINT16_STRING;
                } else if (ByteUtil.getUnsignedInt(_TempCnt) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
                    tempCnt = (int) OTARealTimeConstants.INVALID_UINT16_ERROR;
                } else {
                    tempCnt = ByteUtil.getUnsignedInt(_TempCnt);
                    byteDatalength += tempCnt;
                }
                realTimeDataTempMap.put(OTARealTimeConstants.TEMP_CNT, tempCnt);

                //_Temparatures
                if (tempCnt != 65535 && tempCnt != 65534) {
                    //_Temparatures
                    byte[] _Temparatures = new byte[tempCnt];
                    System.arraycopy(datagramBytes, 3, _Temparatures, 0, tempCnt);
                    temparatures = new int[tempCnt];
                    for (int i = 0; i < tempCnt; i++) {
                        int temp = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_Temparatures[i]));
                        if (temp == OTARealTimeConstants.INVALID_BYTE_STRING || temp == OTARealTimeConstants.INVALID_BYTE_ERROR) {
                            temparatures[i] = temp;
                        } else {
                            temparatures[i] = temp - temparaturesBaseNum;
                        }
                    }
                    realTimeDataTempMap.put(OTARealTimeConstants.TEMPARATURES, temparatures);
                } else {
                    dataCorrect = false;
                    logger.warn("TBox(SN:{}):RealTimeDataTemp 报文长度错误", tboxsn);
                    temparatures = new int[0];
                    realTimeDataTempMap.put(OTARealTimeConstants.TEMPARATURES, temparatures);
                }


            } catch (Exception e) {
                logger.error("TBox(SN:{}):RealTimeDataTemp 解析报文出错，原因:{}", tboxsn,
                        ThrowableUtil.getErrorInfoFromThrowable(e));
                dataCorrect = false;
            }
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
        return converdata;
    }

    @JSONField(serialize = false)
    public boolean isAnalysisOk() {
        return dataCorrect;
    }

}
