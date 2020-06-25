package com.maxus.tsp.gateway.ota.process;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.GB6BEmission;
import com.maxus.tsp.gateway.common.constant.KafkaOtaDataCommand;
import com.maxus.tsp.gateway.common.constant.OTAConstant;
import com.maxus.tsp.gateway.common.model.Kafka_OTAData;
import com.maxus.tsp.gateway.common.ota.OTAMessage;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.service.TboxService;
import com.maxus.tsp.platform.service.model.vo.CodeValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 汪亚军
 * @Title ProcGB6B
 * @Description 为支持国六B数据上报，增加指令处理
 * @date 2018年11月20日
 */
public class ProcGB6B extends BaseOtaProc {

    // 日志
    private static Logger logger = LogManager.getLogger(ProcGB6B.class);

    //网关接收到报文的时间
    private long gatewayTimeIn;

    public ProcGB6B(KafkaService kafkaService, TboxService tboxService, long receivetime) {
        super(kafkaService, tboxService);
        this.gatewayTimeIn = receivetime;
    }

    /**
     * @param requestMsg
     * @return
     * @Title: checkGBEmission
     * @Description: 国六B排放数据上行指令处理
     */
    public byte[] checkGBEmission(OTAMessage requestMsg) {
        return procGBEmission(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param requestMsg
     * @return
     * @Title: checkGBLoginEmission
     * @Description:国六B功能的登入上行指令处理
     */
    public byte[] checkGBLoginEmission(OTAMessage requestMsg) {
        return procGBLoginEmission(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param requestMsg
     * @return
     * @Title: checkGBLogoutEmission
     * @Description:国六B功能的登出上行指令处理
     */
    public byte[] checkGBLogoutEmission(OTAMessage requestMsg) {
        return procGBLogoutEmission(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param param
     * @param serialNumber
     * @return outData
     * @Title: procGBEmission
     * @Description:国六B排放数据处理
     */
    private byte[] procGBEmission(byte[] param, String serialNumber) {
        logger.info("Tbox(SN:{}):国六B排放数据解密后报文为:{}", serialNumber, ByteUtil.byteToHex(param));
        byte[] outData = {OTAConstant.COMMON_RESULT_SUCCESS};
        Kafka_OTAData emissionDataInfo = new Kafka_OTAData();
        emissionDataInfo.setSn(serialNumber);
        //封装网关接受报文时间
        emissionDataInfo.setGatewayTimeIn(gatewayTimeIn);
        try {
            Map<String, Object> retMap = new HashMap<>();

            // 获取obdInfoCnt
            int obdInfoCnt = param[OTAConstant.OBD_INFO_CNT_INDEX];
            // 判断obdInfoCnt是否在范围内，不在范围内，原值上传，后取0运算
            retMap.put(GB6BEmission.OBD_INFO_CNT.getCode(), ByteUtil.byteToHex(param[OTAConstant.OBD_INFO_CNT_INDEX]));
            if (obdInfoCnt < OTAConstant.OBD_INFO_CNT_MIN || obdInfoCnt > OTAConstant.OBD_INFO_CNT_MAX) {
                logger.warn("Tbox(SN:{}):国六B排放数据obdInfoCnt为无效值:{}", serialNumber, obdInfoCnt);
                obdInfoCnt = 0;
            }

            //用于计算长度
            int faultCnt = 0;
            // 用于上传给RVM
            String faultCntString = null;
            // 定义范围外的faultCnt
            int illegalFaultCnt = 0;
            if (obdInfoCnt == 1) {
                // 获取faultCnt
                faultCnt = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(param[OTAConstant.FAULT_CNT_INDEX]));
                faultCntString = ByteUtil.byteToHex(param[OTAConstant.FAULT_CNT_INDEX]);
            }

            // obdInfo的长度
            int obdInfoLength = 0;
            if (faultCnt < OTAConstant.FAULT_CNT_MIN || faultCnt > OTAConstant.FAULT_CNT_MAX) {
                logger.warn("TBox(SN:{}):国六B排放数据faultCnt为无效值:{}", serialNumber, faultCnt);
                faultCnt = 0;
                obdInfoLength = ((OTAConstant.FAULT_LIST_OFFSET * illegalFaultCnt) + OTAConstant.EXTRA_OBD_INFO_OFFSET) * obdInfoCnt;
            } else {
                obdInfoLength = ((OTAConstant.FAULT_LIST_OFFSET * faultCnt) + OTAConstant.EXTRA_OBD_INFO_OFFSET) * obdInfoCnt;
            }

            // 获取dataStreamCnt
            int dataStreamCnt = param[obdInfoLength + OTAConstant.PREFIXAL_OFFSET];
            retMap.put(GB6BEmission.DATA_STREAM_CNT.getCode(), ByteUtil.byteToHex(param[obdInfoLength + OTAConstant.PREFIXAL_OFFSET]));
            if (dataStreamCnt < 0 || dataStreamCnt > 1) {
                logger.warn("TBox(SN:{}):国六B排放数据dataStreamCnt为无效值:{}", serialNumber, dataStreamCnt);
                dataStreamCnt = 0;
            }

            // dataStreamInfo的长度
            int dataStreamInfoLength = OTAConstant.DATA_STREAM_INFO_OFFSET * dataStreamCnt;

            // 报文总长度
            int gb_emission_msg_offset = OTAConstant.PREFIXAL_OFFSET + obdInfoLength + OTAConstant.DATA_STREAM_CNT_OFFSET + dataStreamInfoLength;

            if (param.length != gb_emission_msg_offset) {
                logger.warn("Tbox(SN:{}):国六B排放数据信息长度不一致,报文丢弃!", serialNumber);
                outData[0] = OTAConstant.COMMON_RESULT_FAILED;
                return outData;
            } else {
                // IsRealtime
                //ps:判断此项数据决定ota_data的command是否为实时数据
                byte[] _isRealTime = new byte[OTAConstant.UINT8_OFFSET];
                System.arraycopy(param, 0, _isRealTime, 0, OTAConstant.UINT8_OFFSET);
                int isRealTimeType = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_isRealTime[0]));

                // CollectTime
                byte[] _collectTime = new byte[OTAConstant.DATETIME_BYTES_SIZE];
                System.arraycopy(param, 1, _collectTime, 0, OTAConstant.DATETIME_BYTES_SIZE);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(ByteUtil.bytesToDataTime(_collectTime)));
                emissionDataInfo.setCollectTime(calendar.getTimeInMillis());
                // SoftwareVersion
                byte[] _softwareVersion = new byte[OTAConstant.UINT8_OFFSET];
                System.arraycopy(param, 8, _softwareVersion, 0, OTAConstant.UINT8_OFFSET);
                retMap.put(GB6BEmission.SOFTWARE_VERSION.getCode(), ByteUtil.byteToHex(_softwareVersion));

                // ObdInfo
                CodeValue[] obdInfoList;
                if (obdInfoCnt == 1) {
                    int srcPos = 10;
                    byte[] _obdInfo = new byte[(OTAConstant.FAULT_LIST_OFFSET * faultCnt) + OTAConstant.EXTRA_OBD_INFO_OFFSET];
                    System.arraycopy(param, srcPos, _obdInfo, 0, (OTAConstant.FAULT_LIST_OFFSET * faultCnt) + OTAConstant.EXTRA_OBD_INFO_OFFSET);
                    obdInfoList = procGBEmissionObdInfo(serialNumber, _obdInfo, faultCnt, emissionDataInfo, faultCntString);
                } else {
                    obdInfoList = new CodeValue[0];
                }
                retMap.put(GB6BEmission.OBD_INFO.getCode(), obdInfoList);

                // DataStreamInfo
                CodeValue[] dataStreamInfo;
                if (dataStreamCnt == 1) {
                    int srcPos = obdInfoLength + OTAConstant.EXTRA_OFFSET;
                    byte[] _dataStreamInfo = new byte[OTAConstant.DATA_STREAM_INFO_OFFSET];
                    System.arraycopy(param, srcPos, _dataStreamInfo, 0, OTAConstant.DATA_STREAM_INFO_OFFSET);
                    dataStreamInfo = procGBEmissionDataStreamInfo(serialNumber, _dataStreamInfo);
                } else {
                    dataStreamInfo = new CodeValue[0];
                }
                retMap.put(GB6BEmission.DATA_STREAM_INFO.getCode(), dataStreamInfo);

                //将retMap的值存入对应的CodeValue数组
                CodeValue[] result = new CodeValue[retMap.size()];
                int index = 0;
                for (Map.Entry<String, Object> entry :
                        retMap.entrySet()) {
                    result[index++] = new CodeValue(entry.getKey(), entry.getValue());
                }
                emissionDataInfo.setItems(result);

                //网关发送时间
                long gatewayTimeOut = System.currentTimeMillis();
                emissionDataInfo.setGatewayTimeOut(gatewayTimeOut);

                // kafka发送消息
                //判断是否是实时数据
                if (isRealTimeType == 0) {
                    emissionDataInfo.setCommand(KafkaOtaDataCommand.KAFKA_PARAM_RE_EMISSION_DATA);
                } else {
                    emissionDataInfo.setCommand(KafkaOtaDataCommand.KAFKA_PARAM_EMISSION_DATA);
                }

                logger.info("TBox(SN: {})国六B排放数据上行指令回复为：{} ", serialNumber,
                        JSONObject.toJSONString(emissionDataInfo));
                kafkaService.transferGBEmissionResponse(emissionDataInfo, KafkaMsgConstant.TOPIC_OTA_DATA);

                return outData;
            }
        } catch (Exception e) {
            logger.error("TBox(SN: {})解析CMD_UP_GB_EMISSION出错,当前报文:{},原因:{}", serialNumber,
                    ByteUtil.byteToHex(param), ThrowableUtil.getErrorInfoFromThrowable(e));
            outData[0] = OTAConstant.COMMON_RESULT_FAILED;
            return outData;
        }
    }

    /**
     * @param param
     * @param serialNumber
     * @return
     * @Title: procGBLoginEmission
     * @Description: 国六B功能的登入数据处理
     */
    private byte[] procGBLoginEmission(byte[] param, String serialNumber) {
        logger.debug("TBox(SN:{})国六B登入解密后报文:{}", serialNumber, ByteUtil.byteToHex(param));
        byte[] outData = {OTAConstant.COMMON_RESULT_SUCCESS};
        Kafka_OTAData emissionLoginInfo = new Kafka_OTAData();
        emissionLoginInfo.setCommand(KafkaOtaDataCommand.KAFKA_PARAM_GB_LOGIN_EMSN);
        emissionLoginInfo.setSn(serialNumber);
        //网关的接收报文的时间
        emissionLoginInfo.setGatewayTimeIn(gatewayTimeIn);
        try {
            if (param.length != OTAConstant.DATETIME_BYTES_SIZE + OTAConstant.OTA_SOFT_VERSION_OFFSET) {
                logger.warn("Tbox(SN:{}):国六B功能的登入数据信息长度不一致,报文丢弃!", serialNumber);
                outData[0] = OTAConstant.COMMON_RESULT_FAILED;
                return outData;
            } else {
                String vin = tboxService.getVINForTbox(serialNumber);
//                if (vin == null) {
//                    logger.warn("TBox(SN: {}): 从Redis获取VIN号异常", serialNumber);
//                    vin = "";
//                }
                emissionLoginInfo.setVin(vin);

                // currentTime
                byte[] _currentTime = new byte[OTAConstant.DATETIME_BYTES_SIZE];
                System.arraycopy(param, 0, _currentTime, 0, OTAConstant.DATETIME_BYTES_SIZE);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(ByteUtil.bytesToDataTime(_currentTime)));
                emissionLoginInfo.setCollectTime(calendar.getTimeInMillis());

                CodeValue[] items = new CodeValue[1];
                //softWareVersion
                int softWareVersion = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(param[7]));
                logger.info("TBox(SN:{})当前国六登录报文终端软件版本号为:{}", serialNumber, softWareVersion);
                byte[] _softWareVersion = new byte[1];
                System.arraycopy(param, 7, _softWareVersion, 0, 1);
                items[0] = new CodeValue(GB6BEmission.SOFTWARE_VERSION.getCode(), ByteUtil.byteToHex(_softWareVersion));
                emissionLoginInfo.setItems(items);

                //网关发送时间
                long gatewayTimeOut = System.currentTimeMillis();
                emissionLoginInfo.setGatewayTimeOut(gatewayTimeOut);

                logger.debug("TBox(SN: {})国六B功能的登入数据上行指令回复为：{} ", serialNumber,
                        JSONObject.toJSONString(emissionLoginInfo));

                //网关发送kafka
                kafkaService.transferGBLoginEmissionResponse(emissionLoginInfo, KafkaMsgConstant.TOPIC_OTA_DATA);

                return outData;
            }
        } catch (Exception e) {
            logger.error("Tbox(SN: {})解析CMD_UP_GB_LOGIN_EMSN出错,当前报文:{},原因:{}", serialNumber,
                    ByteUtil.byteToHex(param), ThrowableUtil.getErrorInfoFromThrowable(e));
            outData[0] = OTAConstant.COMMON_RESULT_FAILED;
            return outData;
        }
    }

    /**
     * @param param
     * @param serialNumber
     * @return
     * @Title: procGBLogoutEmission
     * @Description: 国六B功能的登出数据处理
     */
    private byte[] procGBLogoutEmission(byte[] param, String serialNumber) {
        logger.debug("TBox(SN:{})国六B登出解密后报文:{}", serialNumber, ByteUtil.byteToHex(param));
        byte[] outData = {OTAConstant.COMMON_RESULT_SUCCESS};
        Kafka_OTAData emissionLogoutInfo = new Kafka_OTAData();
        emissionLogoutInfo.setCommand(KafkaOtaDataCommand.KAFKA_PARAM_GB_LOGINOUT_EMSN);
        emissionLogoutInfo.setSn(serialNumber);
        //网关接受时间
        emissionLogoutInfo.setGatewayTimeIn(gatewayTimeIn);
        try {
            if (param.length != OTAConstant.DATETIME_BYTES_SIZE + OTAConstant.OTA_SOFT_VERSION_OFFSET) {
                logger.warn("Tbox(SN:{}):国六B功能的登出数据信息长度不一致,报文丢弃!", serialNumber);
                outData[0] = OTAConstant.COMMON_RESULT_FAILED;
                return outData;
            } else {
                String vin = tboxService.getVINForTbox(serialNumber);
//                if (vin == null) {
//                    logger.warn("TBox(SN: {}): 从Redis获取VIN号异常", serialNumber);
//                    vin = "";
//                }
                emissionLogoutInfo.setVin(vin);

                // currentTime
                byte[] _currentTime = new byte[OTAConstant.DATETIME_BYTES_SIZE];
                System.arraycopy(param, 0, _currentTime, 0, OTAConstant.DATETIME_BYTES_SIZE);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(ByteUtil.bytesToDataTime(_currentTime)));
                emissionLogoutInfo.setCollectTime(calendar.getTimeInMillis());

                CodeValue[] items = new CodeValue[1];
                //softWareVersion
                int softWareVersion = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(param[7]));
                logger.info("TBox(SN:{})当前国六登出报文终端软件版本号为:{}", serialNumber, softWareVersion);
                byte[] _softWareVersion = new byte[1];
                System.arraycopy(param, 7, _softWareVersion, 0, 1);
                items[0] = new CodeValue(GB6BEmission.SOFTWARE_VERSION.getCode(), ByteUtil.byteToHex(_softWareVersion));
                emissionLogoutInfo.setItems(items);

                //网关发送时间
                emissionLogoutInfo.setGatewayTimeOut(System.currentTimeMillis());

                logger.info("TBox(SN: {})国六B功能的登出数据上行指令回复为：{} ", serialNumber,
                        JSONObject.toJSONString(emissionLogoutInfo));

                //网关发送kafka消息
                kafkaService.transferGBLogoutEmissionResponse(emissionLogoutInfo, KafkaMsgConstant.TOPIC_OTA_DATA);
                return outData;
            }
        } catch (Exception e) {
            logger.error("TBox(SN: {})解析CMD_UP_GB_LOGOUT_EMSN出错,当前报文:{},原因:{}", serialNumber,
                    ByteUtil.byteToHex(param), ThrowableUtil.getErrorInfoFromThrowable(e));
            outData[0] = OTAConstant.COMMON_RESULT_FAILED;
            return outData;
        }
    }

    /**
     * @param serialNumber
     * @param _obdInfo
     * @param emissionData
     * @return
     * @Title: procGBEmissionObdInfo
     * @Description: 处理ObdInfo数据
     */
    private CodeValue[] procGBEmissionObdInfo(String serialNumber, byte[] _obdInfo, int faultCnt, Kafka_OTAData emissionData, String faultCntString) {
        logger.debug("TBox(SN:{})排放数据处理ObdInfo数据原始报文为:{}", serialNumber, ByteUtil.byteToHex(_obdInfo));
        CodeValue[] obdInfo;
        if (faultCnt == 0) {
            obdInfo = new CodeValue[OTAConstant.OBD_INFO_NUM - 1];
        } else {
            obdInfo = new CodeValue[OTAConstant.OBD_INFO_NUM];
        }
        int index = 0;

        // DiagProtocol
        obdInfo[index++] = new CodeValue(GB6BEmission.DIAG_PROTOCOL.getCode(), ByteUtil.byteToHex(_obdInfo[0]));

        // MILStatus
        obdInfo[index++] = new CodeValue(GB6BEmission.MIL_STATUS.getCode(), ByteUtil.byteToHex(_obdInfo[1]));

        // DiagSupport
        byte[] _diagSupport = new byte[OTAConstant.UINT16_OFFSET];
        System.arraycopy(_obdInfo, 2, _diagSupport, 0, OTAConstant.UINT16_OFFSET);
        String diagSupport = ByteUtil.byteToHex(_diagSupport);
        obdInfo[index++] = new CodeValue(GB6BEmission.DIAG_SUPPORT.getCode(), diagSupport);

        // DiagReady
        byte[] _diagReady = new byte[OTAConstant.UINT16_OFFSET];
        System.arraycopy(_obdInfo, 4, _diagReady, 0, OTAConstant.UINT16_OFFSET);
        String diagReady = ByteUtil.byteToHex(_diagReady);
        obdInfo[index++] = new CodeValue(GB6BEmission.DIAG_READY.getCode(), diagReady);

        // Vin
        byte[] _vin = new byte[OTAConstant.UINT8_OFFSET * 17];
        System.arraycopy(_obdInfo, 6, _vin, 0, OTAConstant.UINT8_OFFSET * 17);
        emissionData.setVin(ByteUtil.byteToHex(_vin));

        // SoftwareCalId
        byte[] _softwareCalId = new byte[OTAConstant.UINT8_OFFSET * 18];
        System.arraycopy(_obdInfo, 23, _softwareCalId, 0, OTAConstant.UINT8_OFFSET * 18);
        obdInfo[index++] = new CodeValue(GB6BEmission.SOFTWARE_CAL_ID.getCode(), ByteUtil.byteToHex(_softwareCalId));

        // CVN
        byte[] _cvn = new byte[OTAConstant.UINT8_OFFSET * 18];
        System.arraycopy(_obdInfo, 41, _cvn, 0, OTAConstant.UINT8_OFFSET * 18);
        obdInfo[index++] = new CodeValue(GB6BEmission.CVN.getCode(), ByteUtil.byteToHex(_cvn));

        // IUPR
        byte[] _iupr = new byte[OTAConstant.UINT8_OFFSET * 36];
        System.arraycopy(_obdInfo, 59, _iupr, 0, OTAConstant.UINT8_OFFSET * 36);
        obdInfo[index++] = new CodeValue(GB6BEmission.IUPR.getCode(), ByteUtil.byteToHex(_iupr));

        // FaultCnt
        if (faultCntString != null) {
            obdInfo[index++] = new CodeValue(GB6BEmission.FAULT_CNT.getCode(), faultCntString);
        }
        if (faultCnt > 0 && faultCnt <= 253) {
            // FaultList
            byte[] _faultList = new byte[OTAConstant.UINT32_OFFSET * faultCnt];
            System.arraycopy(_obdInfo, 96, _faultList, 0, OTAConstant.UINT32_OFFSET * faultCnt);
            obdInfo[index++] = new CodeValue(GB6BEmission.FAULT_LIST.getCode(), ByteUtil.byteToHex(_faultList));
        }
        return obdInfo;
    }

    /**
     * @param serialNumber
     * @param _dataStreamInfo
     * @return
     * @Title: procGBEmissionDataStreamInfo
     * @Description: 处理_dataStreamInfo
     */
    private CodeValue[] procGBEmissionDataStreamInfo(String serialNumber, byte[] _dataStreamInfo) {
        logger.debug("TBox(SN:{}):排放数据处理dataStreamInfo原始报文为:{}", serialNumber, ByteUtil.byteToHex(_dataStreamInfo));
        CodeValue[] dataStreamInfo = new CodeValue[OTAConstant.DATA_STREAM_INFO_NUM];
        int index = 0;

        // VehicleSpeed
        byte[] _vehicleSpeed = new byte[OTAConstant.UINT16_OFFSET];
        System.arraycopy(_dataStreamInfo, 0, _vehicleSpeed, 0, OTAConstant.UINT16_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.VEHICLE_SPEED.getCode(), ByteUtil.byteToHex(_vehicleSpeed));

        // AirPressure
        byte[] _airPressure = new byte[OTAConstant.UINT8_OFFSET];
        System.arraycopy(_dataStreamInfo, 2, _airPressure, 0, OTAConstant.UINT8_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.AIR_PRESSURE.getCode(), ByteUtil.byteToHex(_airPressure));

        // NetTorque
        byte[] _netTorque = new byte[OTAConstant.UINT8_OFFSET];
        System.arraycopy(_dataStreamInfo, 3, _netTorque, 0, OTAConstant.UINT8_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.NET_TORQUE.getCode(), ByteUtil.byteToHex(_netTorque));

        // FrictionTorque
        byte[] _frictionTorque = new byte[OTAConstant.UINT8_OFFSET];
        System.arraycopy(_dataStreamInfo, 4, _frictionTorque, 0, OTAConstant.UINT8_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.FRICTION_TORQUE.getCode(), ByteUtil.byteToHex(_frictionTorque));

        // EngineSpeed
        byte[] _engineSpeed = new byte[OTAConstant.UINT16_OFFSET];
        System.arraycopy(_dataStreamInfo, 5, _engineSpeed, 0, OTAConstant.UINT16_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.ENGINE_SPEED.getCode(), ByteUtil.byteToHex(_engineSpeed));

        // FuelFlowRate
        byte[] _fuelFlowRate = new byte[OTAConstant.UINT16_OFFSET];
        System.arraycopy(_dataStreamInfo, 7, _fuelFlowRate, 0, OTAConstant.UINT16_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.FUEL_FLOW_RATE.getCode(), ByteUtil.byteToHex(_fuelFlowRate));

        // NoxUp
        byte[] _noxUp = new byte[OTAConstant.UINT16_OFFSET];
        System.arraycopy(_dataStreamInfo, 9, _noxUp, 0, OTAConstant.UINT16_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.NOX_UP.getCode(), ByteUtil.byteToHex(_noxUp));

        // NoxDown
        byte[] _noxDown = new byte[OTAConstant.UINT16_OFFSET];
        System.arraycopy(_dataStreamInfo, 11, _noxDown, 0, OTAConstant.UINT16_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.NOX_DOWN.getCode(), ByteUtil.byteToHex(_noxDown));

        // Reactant
        byte[] _reactant = new byte[OTAConstant.UINT8_OFFSET];
        System.arraycopy(_dataStreamInfo, 13, _reactant, 0, OTAConstant.UINT8_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.REACTANT.getCode(), ByteUtil.byteToHex(_reactant));

        // AirInput
        byte[] _airInput = new byte[OTAConstant.UINT16_OFFSET];
        System.arraycopy(_dataStreamInfo, 14, _airInput, 0, OTAConstant.UINT16_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.AIR_INPUT.getCode(), ByteUtil.byteToHex(_airInput));

        // TempEntrance
        byte[] _tempEntrance = new byte[OTAConstant.UINT16_OFFSET];
        System.arraycopy(_dataStreamInfo, 16, _tempEntrance, 0, OTAConstant.UINT16_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.TEMP_ENTRANCE.getCode(), ByteUtil.byteToHex(_tempEntrance));

        // TempExit
        byte[] _tempExit = new byte[OTAConstant.UINT16_OFFSET];
        System.arraycopy(_dataStreamInfo, 18, _tempExit, 0, OTAConstant.UINT16_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.TEMP_EXIT.getCode(), ByteUtil.byteToHex(_tempExit));

        // DPF
        byte[] _dpf = new byte[OTAConstant.UINT16_OFFSET];
        System.arraycopy(_dataStreamInfo, 20, _dpf, 0, OTAConstant.UINT16_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.DPF.getCode(), ByteUtil.byteToHex(_dpf));

        // TempCoolant
        byte[] _tempCoolant = new byte[OTAConstant.UINT8_OFFSET];
        System.arraycopy(_dataStreamInfo, 22, _tempCoolant, 0, OTAConstant.UINT8_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.TEMP_COOLANT.getCode(), ByteUtil.byteToHex(_tempCoolant));

        // FuelLevel
        byte[] _fuelLevel = new byte[OTAConstant.UINT8_OFFSET];
        System.arraycopy(_dataStreamInfo, 23, _fuelLevel, 0, OTAConstant.UINT8_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.FUEL_LEVEL.getCode(), ByteUtil.byteToHex(_fuelLevel));

        // PosStatus
        // 此处不做按位解析处理，上传16进制报文
        byte[] _posStatus = new byte[OTAConstant.UINT8_OFFSET];
        System.arraycopy(_dataStreamInfo, 24, _posStatus, 0, OTAConstant.UINT8_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.POS_STATUS.getCode(), ByteUtil.byteToHex(_posStatus));

        // longitude
        byte[] _longitude = new byte[OTAConstant.UINT32_OFFSET];
        System.arraycopy(_dataStreamInfo, 25, _longitude, 0, OTAConstant.UINT32_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.LONGITUDE.getCode(), ByteUtil.byteToHex(_longitude));

        // latitude
        byte[] _latitude = new byte[OTAConstant.UINT32_OFFSET];
        System.arraycopy(_dataStreamInfo, 29, _latitude, 0, OTAConstant.UINT32_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.LATITUDE.getCode(), ByteUtil.byteToHex(_latitude));

        // ODO
        byte[] _odo = new byte[OTAConstant.UINT32_OFFSET];
        System.arraycopy(_dataStreamInfo, 33, _odo, 0, OTAConstant.UINT32_OFFSET);
        dataStreamInfo[index++] = new CodeValue(GB6BEmission.ODO.getCode(), ByteUtil.byteToHex(_odo));

        return dataStreamInfo;
    }
}
