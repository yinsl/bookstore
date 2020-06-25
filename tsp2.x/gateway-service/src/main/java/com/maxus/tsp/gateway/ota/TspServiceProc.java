/**
 * TspServiceProc.java Create on 2017年7月17日
 * Copyright (c) 2017年7月17日 by 上汽集团商用车技术中心
 *
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>
 * @version 1.0
 */
package com.maxus.tsp.gateway.ota;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.maxus.tsp.common.constant.DatePatternConstant;
import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.constant.MqttConstant;
import com.maxus.tsp.common.constant.OTARealTimeConstants;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.FaultDataEnum;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.enums.TransferWarningTypeEnum;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.DateUtil;
import com.maxus.tsp.common.util.MD5;
import com.maxus.tsp.common.util.MajorMinorUtil;
import com.maxus.tsp.common.util.MinioClientUtils;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.GetVehicleStatusAirConditionEnum;
import com.maxus.tsp.gateway.common.constant.KafkaOtaDataCommand;
import com.maxus.tsp.gateway.common.constant.OTACommand;
import com.maxus.tsp.gateway.common.constant.OTAConstant;
import com.maxus.tsp.gateway.common.constant.OTAForwardCommand;
import com.maxus.tsp.gateway.common.constant.OTAForwardMessagePartSize;
import com.maxus.tsp.gateway.common.constant.OTARemoteCommand;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.constant.OtaVersionFeature;
import com.maxus.tsp.gateway.common.constant.WarningContentConstant;
import com.maxus.tsp.gateway.common.constant.WarningTypeConstant;
import com.maxus.tsp.gateway.common.model.BaseRmtCtrlItReq;
import com.maxus.tsp.gateway.common.model.CameraRespInfo;
import com.maxus.tsp.gateway.common.model.CarWindowStatus;
import com.maxus.tsp.gateway.common.model.EarlyWarningInfo;
import com.maxus.tsp.gateway.common.model.HomeCtrlResultMo;
import com.maxus.tsp.gateway.common.model.Kafka_GPSData;
import com.maxus.tsp.gateway.common.model.Kafka_LocationData;
import com.maxus.tsp.gateway.common.model.Kafka_OTAData;
import com.maxus.tsp.gateway.common.model.Kafka_RegisterData;
import com.maxus.tsp.gateway.common.model.PoiRespInfo;
import com.maxus.tsp.gateway.common.model.RemoteConfigParam;
import com.maxus.tsp.gateway.common.model.RemoteCtrlItResponse;
import com.maxus.tsp.gateway.common.model.RemoteCtrlResponseData;
import com.maxus.tsp.gateway.common.model.RmtGroupRequestInfo;
import com.maxus.tsp.gateway.common.model.RmtGroupRespInfo;
import com.maxus.tsp.gateway.common.model.RmtGroupResultInfo;
import com.maxus.tsp.gateway.common.model.TakePhotoRespInfo;
import com.maxus.tsp.gateway.common.model.TboxFileLoadStatus;
import com.maxus.tsp.gateway.common.model.TboxUpdateRvmResponse;
import com.maxus.tsp.gateway.common.model.TokenRespInfo;
import com.maxus.tsp.gateway.common.model.VehicleStatusDataMo;
import com.maxus.tsp.gateway.common.model.WarningInfo;
import com.maxus.tsp.gateway.common.ota.GlobalSessionChannel;
import com.maxus.tsp.gateway.common.ota.OTAForwardMessage;
import com.maxus.tsp.gateway.common.ota.OTAMessage;
import com.maxus.tsp.gateway.common.ota.OtaVersion;
import com.maxus.tsp.gateway.common.ota.RemoteCtrlInfo;
import com.maxus.tsp.gateway.common.ota.RmtGroupData;
import com.maxus.tsp.gateway.common.ota.TokenData;
import com.maxus.tsp.gateway.conf.MqttConfiguration.MyGateway;
import com.maxus.tsp.gateway.service.DataProcessing;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.service.TboxService;
import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
import com.maxus.tsp.gateway.timer.RmtGroupTask;
import com.maxus.tsp.platform.service.model.AppJsonResult;
import com.maxus.tsp.platform.service.model.vo.CodeValue;
import com.maxus.tsp.platform.service.model.vo.ItRedisInfo;
import com.maxus.tsp.platform.service.model.vo.ReportAlarm;
import com.maxus.tsp.platform.service.model.vo.ReportPos;
import com.maxus.tsp.platform.service.model.vo.TboxVo;

@Component
public class TspServiceProc {

    public static final int INT = 0x40;
    // 日志
    private static Logger logger = LogManager.getLogger(TspServiceProc.class);
    // 数据库服务接口
    @Autowired
    private TspPlatformClient tspPlatformClient;
    
    @Autowired
    private RedisAPI redisAPI;

	@Autowired
	private MyGateway myGateway;
    
    @Autowired
    private OtaVersion otaVersion;
    
    // kafka服务
    @Autowired
    private KafkaService kafkaService;
    // Tbox服务
    @Autowired
    private TboxService tboxService;
    
    @Autowired
    private DataProcessing dataProcessing;
    
    @Autowired
    private MinioClientUtils minioClientUtils;
    // stream-task投递服务
    // private StreamSender streamSender;
    // 线程池Executor服务
    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(OperationConstant.FIX_POOL_NUMBER);
    // 登陆时可能具备的电池信息
    private static Hashtable<String, List<String>> batteryInfo = new Hashtable<String, List<String>>();
    // 登陆时可能具备的车辆信息VIN
    public static Hashtable<String, String> loginCarInfo = new Hashtable<String, String>();
    // 登陆时可能具备的tbox的iccid
    public static Hashtable<String, String> loginICCIDInfo = new Hashtable<String, String>();
    // 记录需要注册确认的tbox
    private static Hashtable<String, TspServiceProc> registedCheckList = new Hashtable<String, TspServiceProc>();
    // 网关接收报文时间
    private long gatewayTimeIn;

    /**
     * @return ExecutorService
     * @Title: getThreadPool
     * @Description: 获取单例线程Executor服务对象, 提供网关所有的线程池管理
     */
    public static ExecutorService getThreadPool() {
        return fixedThreadPool;
    }

    public static TspServiceProc getRegistProc(String tboxSn) {
        return registedCheckList.get(tboxSn);
    }

    /**
     * @param requestMsg
     * @param ctx
     * @param tboxSNForRecord
     * @return
     * @Title: checkDataLogin
     * @Description: 解析登陆报文
     */
    public byte[] checkDataLogin(OTAMessage requestMsg, String tboxSNForRecord) {
        return inDataLogin(requestMsg.getParam(), requestMsg.getSerialNumber(), tboxSNForRecord);
    }

    /**
     * @param datagramBytes
     * @param tboxSN
     * @param ctx
     * @param tboxSNForRecord
     * @return outData
     * @Title: inDataLogin
     * @Description: 登陆报文解析
     */
    private byte[] inDataLogin(byte[] datagramBytes, String tboxSN, String tboxSNForRecord) {
        byte[] outData = null;
        logger.info("TBox(sn:{})原始解密后报文:{}", tboxSN, ByteUtil.byteToHex(datagramBytes));
        final int bitAnd = 0xff;
        try {
            if (datagramBytes.length < OTAConstant.OTA_LOGIN_MSG_LENGTH_MIN) { // 报文长度不对，直接回复空
                logger.warn("登录报文长度错误！");
                return null;
            }
            // 获取当前AES并保存
            byte[] curAESKey = new byte[OTAConstant.AES_KEY_LENGTH];
            System.arraycopy(datagramBytes, OTAConstant.ZERO, curAESKey, OTAConstant.ZERO, OTAConstant.AES_KEY_LENGTH);
            redisAPI.setHash(RedisConstant.SECURITY_KEY, tboxSN, ByteUtil.byteToHex(curAESKey));
            // 调用方法获取tbox信息是否合法
            logger.info("dataProcessing is null? " + (dataProcessing == null));
            if (!dataProcessing.existTboxInfo(tboxSN)) {
                logger.warn("TBox(SN:{}): 当前TBox信息在数据库中不存在", tboxSN);
                outData = ByteUtil.int2Byte(OTAConstant.LOGIN_RESULT_FAILED_FOR_TBOX_SN_NOT_EXIST);
            } else if (!dataProcessing.loginVarible(tboxSN)) {// 未注册
                logger.warn("TBox(SN:{}): 当前TBox未注册", tboxSN);
                outData = ByteUtil.int2Byte(OTAConstant.LOGIN_RESULT_FAILED_FOR_OTHER_REASON);
            } else {
                // 检查版本号是否正确
                byte[] curVersion = new byte[OTAConstant.OTA_VERSION_LENGTH];
                System.arraycopy(datagramBytes, OTAConstant.OTA_VERSION_OFFSET, curVersion, OTAConstant.ZERO,
                        OTAConstant.OTA_VERSION_LENGTH);
                if (!otaVersion.isVersionSupported(ByteUtil.byteToHex(curVersion))) {
                    logger.warn("TBox(SN:{}): 当前OTA版本号不支持,支持版本数量: {},输入为:{}.{}", tboxSN, otaVersion.getSupportedNumber(),curVersion[0] & bitAnd,
                            curVersion[1] & bitAnd);
                    outData = ByteUtil.int2Byte(OTAConstant.LOGIN_RESULT_FAILED_FOR_VERSION_IS_NOT_SUPPORT);
                } else {
                    logger.info("TBox(SN:{}): 当前OTA版本号为:{}.{}", tboxSN, curVersion[0] & bitAnd, curVersion[1] & bitAnd);
                    OtaVersionFeature.setVersion(tboxSN, ByteUtil.byteToHex(curVersion));
                    boolean extendAnalysis = true;
                    batteryInfo.remove(tboxSN);
                    loginCarInfo.remove(tboxSN);
                    loginICCIDInfo.remove(tboxSN);
                    // 确认登陆信息中是否具有vin，电池信息，如果有，则保存，每次登陆覆盖
                    if (datagramBytes.length > OTAConstant.OTA_LOGIN_MSG_LENGTH_MIN) {
                        if (datagramBytes.length < OTAConstant.OTA_LOGIN_MSG_LENGTH_MIN_WITH_EXTEND_INFO) {
                            logger.warn("TBox(SN:{}): 当前TBox的扩展信息长度不正确", tboxSN);
                            extendAnalysis = false;
                        } else {
                            byte[] tboxLoginVin = new byte[OperationConstant.VIN_LENGTH];
                            System.arraycopy(datagramBytes, OTAConstant.OTA_LOGIN_MSG_EXTEND_VIN_OFFSET, tboxLoginVin,
                                    OTAConstant.ZERO, OperationConstant.VIN_LENGTH);
                            // 获取当前vin
                            logger.info("TBox(sn:{})原始vin报文:{} ", tboxSN, ByteUtil.byteToHex(tboxLoginVin));
                            String tboxLoginVinString = ByteUtil.bytesToString(tboxLoginVin);
                            String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{17}$";
                            if (!tboxLoginVinString.matches(regex) && !StringUtils.isBlank(tboxLoginVinString.trim())) {
                                logger.warn("TBox(SN:{})vin号不符合字母+数字的组合或乱码 。错误VIN:{}", tboxSN, tboxLoginVinString);
                                extendAnalysis = false;
                                outData = ByteUtil.int2Byte(OTAConstant.LOGIN_RESULT_FAILED_FOR_WRONG_VIN);
                            } else {
                                if (!StringUtils.isBlank(tboxLoginVinString.trim())) {
                                    loginCarInfo.put(tboxSN, tboxLoginVinString);
                                }
                                byte[] tboxLoginICCID = new byte[OperationConstant.TBOX_ICCID_LENGTH];
                                System.arraycopy(datagramBytes, OTAConstant.OTA_LOGIN_MSG_EXTEND_ICCID_OFFSET,
                                        tboxLoginICCID, OTAConstant.ZERO, OperationConstant.TBOX_ICCID_LENGTH);
                                String curICCID = ByteUtil.bytesToString(tboxLoginICCID);
                                if (!StringUtils.isBlank(curICCID.trim())) {
                                    loginICCIDInfo.put(tboxSN, curICCID);
                                }
                                logger.info("TBox(SN:{}): 当前TBox上报的ICCID是{}", tboxSN, curICCID);
                                int batteryNum = datagramBytes[OTAConstant.OTA_LOGIN_MSG_EXTEND_BATTERY_OFFSET];
                                int batteryCodeAnalysisOffset = OTAConstant.OTA_LOGIN_MSG_EXTEND_BATTERY_OFFSET + 1;
                                List<String> codeList = new ArrayList<String>();
                                while (batteryNum > 0) {
                                    byte[] batteryCodeSize = new byte[OTAConstant.OTA_STRING_TYPE_BYTE_SIZE];
                                    if (datagramBytes.length < batteryCodeAnalysisOffset
                                            + OTAConstant.OTA_STRING_TYPE_BYTE_SIZE) {
                                        extendAnalysis = false;
                                        break;
                                    }
                                    System.arraycopy(datagramBytes, batteryCodeAnalysisOffset, batteryCodeSize,
                                            OTAConstant.ZERO, OTAConstant.OTA_STRING_TYPE_BYTE_SIZE);
                                    int curBatteryCodeSize = ByteUtil.byte2Short(batteryCodeSize);
                                    batteryCodeAnalysisOffset = batteryCodeAnalysisOffset
                                            + OTAConstant.OTA_STRING_TYPE_BYTE_SIZE;
                                    byte[] batteryCode = new byte[curBatteryCodeSize];
                                    if (datagramBytes.length < batteryCodeAnalysisOffset + curBatteryCodeSize) {
                                        extendAnalysis = false;
                                        break;
                                    }
                                    System.arraycopy(datagramBytes, batteryCodeAnalysisOffset, batteryCode,
                                            OTAConstant.ZERO, curBatteryCodeSize);
                                    codeList.add(ByteUtil.parseAscii(batteryCode));
                                    batteryCodeAnalysisOffset = batteryCodeAnalysisOffset + curBatteryCodeSize;
                                    batteryNum--;
                                }
                                batteryInfo.put(tboxSN, codeList);
                            }
                        }
                    }
                    if (!extendAnalysis) {
                        batteryInfo.remove(tboxSN);
                        if (null == outData) {
                            outData = ByteUtil.int2Byte(OTAConstant.LOGIN_RESULT_FAILED_FOR_OTHER_REASON);
                        }
                    } else {
                        outData = ByteUtil.int2Byte(OTAConstant.COMMON_RESULT_SUCCESS);
                        if (tboxSNForRecord == null) {
                            logger.info("TBox(SN:{}): 当前TBox登陆成功", tboxSN);
                        } else {
                            logger.info("TBox(SN:{}): 当前TBox已经登陆", tboxSN);
                        }
                        // 记录当前登陆时间
                        dataProcessing.onlineTboxAdd(tboxSN);
                        // 记录当前TBox对应绑定车辆车架号信息
                        initialVINforSN(tboxSN);
                        // 执行组合唤醒指令
                        String reqInfo = redisAPI.getValue(RedisConstant.RMT_GROUP_CTRL_REQ);
                        if (StringUtils.isNotEmpty(reqInfo)){
                        	RmtGroupRequestInfo rmtGroupRequestInfo = JSONObject.parseObject(reqInfo, RmtGroupRequestInfo.class);
                            doWhenTBoxOnline(rmtGroupRequestInfo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("当前TBox(SN:{})登陆失败, 当前登陆报文:{}, 原因:{}", tboxSN, ByteUtil.byteToHex(datagramBytes),
                    ThrowableUtil.getErrorInfoFromThrowable(e));
            outData = ByteUtil.int2Byte(OTAConstant.LOGIN_RESULT_FAILED_FOR_OTHER_REASON);
        }
        return outData;
    }
    
    /**
	 * @Description TBox在线或唤醒后执行当前的远程控制指令
	 * @Date 2019/2/4 16:02
	 * @Param [rmtGroupRequestInfo]
	 * @return com.maxus.tsp.platform.service.model.AppJsonResult
	 **/
	private void doWhenTBoxOnline(RmtGroupRequestInfo rmtGroupRequestInfo) {
		String serialNumber = rmtGroupRequestInfo.getSn();
		// 在线时的正常处理业务流程
		try {
			logger.info("TBox(SN:{})确定当前TBox在线, 将本次控制指令存储在Redis中!", serialNumber);
			addCommandSend(serialNumber, RedisConstant.RM_GROUP_CTRL);
			// 当前TBox在线, 判断redis中是否存在唤醒指令
			if (redisAPI.hasKey(RedisConstant.WAKE_UP + "_" + serialNumber)) {
				logger.info("TBox(SN:{})在线并且redis中存在唤醒指令, 清除唤醒指令!", serialNumber);
				redisAPI.delete(RedisConstant.WAKE_UP + "_" + serialNumber);
			}
			// 通过网关kafka发送一条消息, 用作内部通信
			// 默认下发时间
			String downTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
			// 将远程组合控制的网关下发topic存入到redis
			if (rmtGroupRequestInfo.getOtaType().equals("REMOTECTRL")) {
				redisAPI.setValue(OperationConstant.RM_GROUP_CTRL + serialNumber,
				        KafkaMsgConstant.TOPIC_SELF_RM_GROUP_DOWN);
			} else if (rmtGroupRequestInfo.getOtaType().equals("REMOTECTRL_EXT")) {
				redisAPI.setValue(OperationConstant.RM_GROUP_CTRL + serialNumber,
				        KafkaMsgConstant.TOPIC_SELF_RM_EXT_DOWN);
			}

			String[] cmd = { serialNumber, JSONObject.toJSONString(rmtGroupRequestInfo), downTime };

			// 开始组装下发报文
			OTAMessage sendOTAMsg = RmtGroupData.getSendDataForRmtGroup(cmd);
			if (sendOTAMsg != null) {
				if (sendOTAMsg.curMessage != null && sendOTAMsg.curMessage.length > 0) {
					logger.info("TBox(SN:{})开始下发组合远控EXT报文:{}", serialNumber, ByteUtil.byteToHex(sendOTAMsg.curMessage));
					// 写入报文
					String curVer = redisAPI.getHash(RedisConstant.CUR_OTA_VERSION, serialNumber);
					String replyTopic = curVer + MqttConstant.MQTT_GW + serialNumber;
//					myGateway.sendToMqtt(Asn1Util.encodeSeq(sendOTAMsg.curMessage), replyTopic, 1);
					logger.info("响应报文：" + String.valueOf(sendOTAMsg.curMessage));
					myGateway.sendToMqtt(sendOTAMsg.curMessage, replyTopic, 1);
					// 开启超时定时器，到房车指令下发回包，给定10秒，并设置redis全局标志位
					BaseRmtCtrlItReq rmtRequest = new BaseRmtCtrlItReq();
					rmtRequest.setSn(serialNumber);
					rmtRequest.setEventTime(rmtGroupRequestInfo.getEventTime());
					rmtRequest.setSeqNo(rmtGroupRequestInfo.getSeqNo());
					new RmtGroupTask(kafkaService, rmtRequest, redisAPI).start();
				} else {
					logger.warn("TBox(SN:{})组合远控EXT指令下发失败:{}", serialNumber, cmd[0] + "_" + cmd[1] + "_" + cmd[2]);
				}
			} else {
				logger.warn("TBox(SN:{})组装组合远控EXT报文出错, 组装后的报文为空!", serialNumber);
			}
		} catch (Exception e) {
			logger.error("TBox(SN:{})组合远控操作因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
			replayRmtGroupToIt(serialNumber, new AppJsonResult(ResultStatus.OPT_FAILED_BY_EXCEPTION, ""),
			        rmtGroupRequestInfo);
		}
	}
	
	private void replayRmtGroupToIt(String serialNumber, AppJsonResult appJsonResult,
	        RmtGroupRequestInfo rmtGroupRequestInfo) {
		// 封转组合远控的结果返回给RVM
		RemoteCtrlItResponse rmtGroupItResp = new RemoteCtrlItResponse();
		rmtGroupItResp.setSn(serialNumber);
		rmtGroupItResp.setStatus(appJsonResult.getStatus());
		rmtGroupItResp.setData(appJsonResult.getData());
		rmtGroupItResp.setDescription(appJsonResult.getDescription());
		rmtGroupItResp.setSeqNo(rmtGroupRequestInfo.getSeqNo());
		rmtGroupItResp.setEventTime(rmtGroupRequestInfo.getEventTime());
		logger.debug("TBox(SN:{})组合远控返回结果为:{}", serialNumber, JSONObject.toJSONString(rmtGroupItResp));
		kafkaService.sndMesToITForTemplate(KafkaMsgConstant.TOPIC_IT_RM_GROUP_RESP, rmtGroupItResp, serialNumber);
	}
	
	/**
	 * @method addCommandSend
	 * @description 添加一条正在下发的指令
	 * @param serialNumber
	 * @param value
	 * @return
	 * @author zhuna
	 * @date 2019/2/15 15:49
	 */
	private boolean addCommandSend(String serialNumber, String value) {
			Date date = new Date();
			String currentTime = DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
			return redisAPI.setValueWithEspireTime(RedisConstant.COMMAND_SEND + "_" + serialNumber, value + "_"
			        + currentTime, OperationConstant.REMOTECONTROL_RESP_EXPIRED_TIME, TimeUnit.SECONDS);
	}

    /**
     * @throws @author fogmk
     * @Title: CheckDataRegisterUp
     * @Description: check register data and save data into Redis
     * @param: @param  requestMsg
     * @param: @return
     * @return: byte[]
     * @Date 2017年7月18日 上午9:30:02
     */
    public byte[] checkDataRegisterUp(OTAMessage requestMsg) {
        return register(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @throws @author fogmk
     * @Title: Register
     * @Description: TBox下线
     * @param: @param  commd
     * @param: @param  tboxSN
     * @param: @return
     * @return: byte[]
     * @Date 2017年7月17日 下午4:38:17
     */
    public byte[] register(byte[] datagramBytes, String tboxSn) {
        byte[] outData = { OTAConstant.COMMON_RESULT_SUCCESS };
        byte[] keySize = new byte[2];
        // 计算当前Tbox的UUID+TBox公钥+SN的MD5值
        String uuid = "";
        int status = OTAConstant.TBOX_RIGISTER_UNLOCKED;
        try {
            // 获取公钥长度
            System.arraycopy(datagramBytes, 0, keySize, 0, 2);
            int keyLength = ByteUtil.byte2Short(keySize);

            if ((keyLength + OTAConstant.OTA_REGISTER_MSG_LENGTH_FOR_LEN_AND_MS5) != datagramBytes.length) {
                outData[0] = OTAConstant.REGISTER_RESULT_FAILED_FOR_OTHER_REASON;
                logger.warn("TBox(SN:{}): 注册失败。原因为报文参数异常   当前报文:{}", tboxSn, ByteUtil.byteToHex(datagramBytes));
                return outData; // 如果数据长度不正确，设为其他原因失败
            }
            // 获取公钥
            byte[] publicKey = new byte[keyLength];
            System.arraycopy(datagramBytes, 2, publicKey, 0, keyLength);
            // 获取MD5值
            byte[] md5Value = new byte[OperationConstant.MD5_SIZE];
            System.arraycopy(datagramBytes, (2 + keyLength), md5Value, 0, OperationConstant.MD5_SIZE);

            TboxVo tboxVo = dataProcessing.getTboxInfo(tboxSn);
            if (tboxVo == null) {
                // 失败，错误的SN号
                outData[0] = OTAConstant.REGISTER_RESULT_FAILED_FOR_WRONG_SN_NUMBER;
                logger.warn("TBox(SN:{}): 注册失败，原因为错误的SN号", tboxSn);
            } else {
                uuid = tboxVo.getUuid();
                status = tboxVo.getStatus();
            }
            // 判断status如果是锁定，则不能进行注册
            if (status == OTAConstant.TBOX_RIGISTER_LOCKED) {
                // 失败，Tbox已经被锁定
                outData[0] = OTAConstant.REGISTER_RESULT_FAILED_FOR_TBOX_HAS_BEEN_LOCKED;
                logger.warn("TBox(SN:{}): 注册失败，设备已锁定", tboxSn);
            } else if (outData[0] == OTAConstant.COMMON_RESULT_SUCCESS) {
                // 计算UUID+TBox公钥+SN的MD5值
                String curPublicKey = ByteUtil.parseAscii(publicKey);
                String curMD5InputData = uuid + curPublicKey + tboxSn;
                String curMD5Output = MD5.encode(curMD5InputData);
                // 比较报文中的MD5与计算得出的MD5是否相等
                if (ByteUtil.parseAscii(md5Value).equalsIgnoreCase(curMD5Output)) {
                    // 核对成功，缓存public key至Redis，等CMD_UP_QCPASSED的报文获得后才会将public
                    // key和锁定值写入数据库
                    setTboxPubKey(tboxSn, curPublicKey);
                    logger.info("TBox(SN:{}): 核对MD5值成功，缓存公钥至Redis中", tboxSn);
                } else {
                    // 失败，错误的MD5值
                    outData[0] = OTAConstant.REGISTER_RESULT_FAILED_FOR_WRONG_MD5_NUMBER;
                    logger.info("TBox(SN:{}): 注册失败，错误的MD5值", tboxSn);
                }
            }
        } catch (Exception ex) {
            // 失败，其他原因
            outData[0] = OTAConstant.REGISTER_RESULT_FAILED_FOR_OTHER_REASON;
            logger.error("TBox(SN:{}): 注册失败    原因:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
        return outData;
    }
    
    /**
     * @Title: setTboxPubKey
     * @Description: 记录tbox 公钥
     * @param: @param
     *             tboxsn
     * @param: @param
     *             tboxPubKey
     * @return: void
     * @throws @author
     *             余佶
     * @Date 2017年8月12日 下午1:52:37
     */
    public void setTboxPubKey(String tboxsn, String tboxPubKey) {
        try {
            redisAPI.setHash(RedisConstant.TBOX_PUBLIC_KEY_TEMP, tboxsn, tboxPubKey);
        } catch (Exception ex) {
            // logger.error("Redis connection error, can't do setTboxPubKey:
            // "+tboxsn);
            logger.error("Redis connection error, TBox({}) can't do setTboxPubKey:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }

    /**
     * @throws @author fogmk
     * @Title: RegisterCert
     * @Description: TBox下线 OTA升级版本
     * @param: @param  commd
     * @param: @param  tboxSN
     * @param: @return
     * @return: byte[]
     * @Date 2017年7月17日 下午4:38:17
     */
    public byte[] checkDataRegisterUpCert(OTAMessage requestMsg) {
        byte[] outData = { OTAConstant.COMMON_RESULT_SUCCESS };
        byte[] keySize = new byte[2];
        // 计算当前Tbox的UUID+TBox公钥+SN的MD5值
        String uuid = "";
        int status = OTAConstant.TBOX_RIGISTER_UNLOCKED;
        byte[] datagramBytes = requestMsg.getParam();
        String tboxSn = requestMsg.getSerialNumber();
        try {
            // 获取TBox证书长度
            System.arraycopy(datagramBytes, 0, keySize, 0, 2);
            int keyLength = ByteUtil.byte2Short(keySize);

            if ((keyLength + OTAConstant.OTA_REGISTER_MSG_LENGTH_FOR_LEN_AND_MS5) != datagramBytes.length) {
                outData[0] = OTAConstant.REGISTER_RESULT_FAILED_FOR_OTHER_REASON;
                logger.warn("TBox(SN:{}): 注册失败。原因为报文参数异常   当前报文:{}", tboxSn, ByteUtil.byteToHex(datagramBytes));
                return outData; // 如果数据长度不正确，设为其他原因失败
            }
            // 获取TBox证书
            byte[] certification = new byte[keyLength];
            System.arraycopy(datagramBytes, 2, certification, 0, keyLength);
            // 获取MD5值
            byte[] md5Value = new byte[OperationConstant.MD5_SIZE];
            System.arraycopy(datagramBytes, (2 + keyLength), md5Value, 0, OperationConstant.MD5_SIZE);

            TboxVo tboxVo = dataProcessing.getTboxInfo(tboxSn);
            if (tboxVo == null) {
                // 失败，错误的SN号
                outData[0] = OTAConstant.REGISTER_RESULT_FAILED_FOR_WRONG_SN_NUMBER;
                logger.warn("TBox(SN:{}): 注册失败，原因为错误的SN号", tboxSn);
            } else {
                uuid = tboxVo.getUuid();
                status = tboxVo.getStatus();
            }
            // 判断status如果是锁定，则不能进行注册
            if (status == OTAConstant.TBOX_RIGISTER_LOCKED) {
                // 失败，Tbox已经被锁定
                outData[0] = OTAConstant.REGISTER_RESULT_FAILED_FOR_TBOX_HAS_BEEN_LOCKED;
                logger.warn("TBox(SN:{}): 注册失败，原因为设备已锁定", tboxSn);
            } else if (outData[0] == OTAConstant.COMMON_RESULT_SUCCESS) {
                // 计算UUID+TBox证书+SN的MD5值
                String curPublicKey = ByteUtil.parseAscii(certification);
                String curMD5InputData = uuid + curPublicKey + tboxSn;
                String curMD5Output = MD5.encode(curMD5InputData);
                // 比较报文中的MD5与计算得出的MD5是否相等
                if (ByteUtil.parseAscii(md5Value).equalsIgnoreCase(curMD5Output)) {
                    // 核对成功，缓存public key至Redis，等CMD_UP_QCPASSED的报文获得后才会将public
                    // key和锁定值写入数据库
                    // GlobalSessionChannel.setTboxPubKey(tboxSn, curPublicKey);
                    // ******** fota版本注册 证书存储在证书服务器 ********
                    // 判定证书服务器目录是否存在
                    minioClientUtils.checkBucket();
                    // 证书存储到服务器
                    minioClientUtils.putBase64CertData(tboxSn, new String(Base64.encodeBase64(certification)));
                    // fota标记 缓存至redis
                    redisAPI.setHash(RedisConstant.TBOX_WITH_FOTA, tboxSn, "fota");
                    logger.info("TBox(SN:{}): 核对MD5值成功，TBox证书传递给服务平台", tboxSn);
                } else {
                    // 失败，错误的MD5值
                    outData[0] = OTAConstant.REGISTER_RESULT_FAILED_FOR_WRONG_MD5_NUMBER;
                    logger.info("TBox(SN:{}): 注册失败，错误的MD5值", tboxSn);
                }
            }
        } catch (Exception ex) {
            // 失败，其他原因
            outData[0] = OTAConstant.REGISTER_RESULT_FAILED_FOR_OTHER_REASON;
            logger.error("TBox(SN:{}): 注册失败    原因:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
        return outData;
    }

    /**
     * @param requestMsg
     * @return byte[]
     * @Title: checkDataUpdatePubKey
     * @Description: 公钥升级结果解析
     */
    public byte[] checkDataUpdatePubKey(OTAMessage requestMsg) {

        String sn = requestMsg.getSerialNumber();
        try {
            // 获取结果,=0成功，=1失败
            int pubkeyUpdateResult = requestMsg.getParam()[0];
            if (pubkeyUpdateResult == 0) {
                pubkeyUpdateResult = 2;
            } else {
                pubkeyUpdateResult = 0;
            }
            // 将结果更新至数据库
            if (this.tspPlatformClient.updatePkeyStatus(sn, pubkeyUpdateResult)) {
                // serial number存在时，更新数据库,修改更新时间
                logger.info("TBox(SN:{}): 更新当前TBox数据库信息,修改更新时间", sn);
            } else {
                // serial number可能不存在表中
                logger.info("TBox(SN:{}): 在基本信息表中未找到当前TBox信息", sn);
            }
        } catch (Exception ex) {
            logger.error("TBox(SN:{}): TSP公钥更新异常，原因:{}", sn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
        return new byte[] {};
    }

    /**
     * @param requestMsg
     * @return
     * @Title: checkDataQCPassedUp
     * @Description: 注册确认报文解析
     */
    public byte[] checkDataQCPassedUp(OTAMessage requestMsg) {
        return qcPassed(requestMsg.getSerialNumber());
    }
    
    /**
     * @Title: getTboxPubKey
     * @Description: 获取公钥信息
     * @param: @param
     *             tboxsn
     * @return: String
     * @throws @author
     *             余佶
     * @Date 2017年8月12日 下午1:52:37
     */
    public String getTboxPubKey(String tboxsn) {
        try {
            if (redisAPI.hasKey(RedisConstant.TBOX_PUBLIC_KEY_TEMP, tboxsn)) {
                return (String) redisAPI.getHash(RedisConstant.TBOX_PUBLIC_KEY_TEMP, tboxsn);
            } else {
                return null;
            }
        } catch (Exception ex) {
            // logger.error("Redis connection error, can't do getTboxPubKey:
            // "+tboxsn);
            logger.error("Redis connection error, TBox({}) can't do getTboxPubKey:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return null;
        }
    }
    
    /**
     * @throws @author fogmk
     * @Title: QCPassed
     * @Description: get register data from redis and save register data into
     *               database then set register lock for current register tbox
     * @param: @param  tboxSN
     * @param: @return
     * @return: byte[]
     * @Date 2017年7月20日 下午2:12:01
     */
    private byte[] qcPassed(String tboxSn) {
        byte[] outData = { OTAConstant.COMMON_RESULT_SUCCESS };
        // 判断tbox是否正确，判断设备状态是否未锁定
        int status = OTAConstant.TBOX_RIGISTER_UNLOCKED;
        try {
            String curPublicKey = getTboxPubKey(tboxSn);
            String tboxWithFota = redisAPI.getHash(RedisConstant.TBOX_WITH_FOTA, tboxSn);
            // 从Tbox基本信息表中，获取Tbox的uuid
            TboxVo tbox = dataProcessing.getTboxInfo(tboxSn);
            if (tbox == null) {
                // 失败，错误的SN号
                outData[0] = OTAConstant.QCPASSED_RESULT_FAILED_FOR_WRONG_SN_NUMBER;
                logger.warn("TBox(SN:{})注册状态检查报文失败: 在基本信息表中未找到当前TBox信息", tboxSn);
            } else {
                status = tbox.getStatus();
            }
            // 判断status如果已经是锁定，则不能进行存储公钥和状态锁定
            if (status == OTAConstant.TBOX_RIGISTER_LOCKED) {
                // 失败，Tbox已经被锁定
                outData[0] = OTAConstant.QCPASSED_RESULT_FAILED_FOR_TBOX_HAS_BEEN_LOCKED;
                logger.warn("TBox(SN:{}): 注册状态检查报文失败，TBox已经被锁定", tboxSn);
            } else if (outData[0] == OTAConstant.COMMON_RESULT_SUCCESS) {
                MajorMinorUtil majorMinor = new MajorMinorUtil(tboxSn);
                boolean flag = false;
                // 注册是fota版本，即为证书操作
                if (StringUtils.isNotBlank(tboxWithFota)) {
                    // 根据tboxSn从证书服务器中获取证书信息
                    String certData = minioClientUtils.getBase64CertData(tboxSn);
                    if (StringUtils.isNotBlank(certData)) {
                        flag = true;
                        curPublicKey = "";
                    } else {
                        // 从证书服务器中未找不到tbox的证书
                        outData[0] = OTAConstant.QCPASSED_RESULT_FAILED_FOR_OTHER_REASON;
                        logger.warn("TBox(SN:{}): 注册状态验证失败  原因：从证书服务器中未找不到tbox的证书", tboxSn);
                    }
                } else { // 非fota版本，公钥操作
                         // Redis中临时公钥存在
                    if (curPublicKey != null && curPublicKey.length() > 0) {
                        flag = true;
                    } else {
                        // 找不到，或者是公钥数据为空，为Null
                        outData[0] = OTAConstant.QCPASSED_RESULT_FAILED_FOR_OTHER_REASON;
                        logger.warn("TBox(SN:{}): 注册状态验证失败  原因：Redis中公钥找不到，或者是公钥数据为空，或为Null", tboxSn);
                    }
                }
                // 证书或公钥没有问题可用时
                if (flag) {
                    // 如果tbox在 it redis中有，则使用kafka通知it激活信息
                    // 将Redis中的临时公钥(为证书操作时，公钥为"")，major，minor更新数据库中，并且锁定本TBox
                    if (dataProcessing.lockTbox(curPublicKey, tboxSn, majorMinor.getMajor(), majorMinor.getMinor())) {
                        // 数据库初步锁定执行成功,可能需要将激活信息投递给rvm
                        if (registerInfoTransferCheck(tboxSn)) {
                            // 传递成功，更新锁定及公钥同步至redis
                            tbox.setStatus(OperationConstant.REGISTER_COMPLETE_SUCCESS);
                            tbox.setPkey(curPublicKey);
                            tbox.setMajor(majorMinor.getMajor());
                            tbox.setMinor(majorMinor.getMinor());
                            redisAPI.setHash(RedisConstant.TBOX_INFO, tbox.getSn(),
                                    JSONObject.toJSONString(tbox, SerializerFeature.WriteMapNullValue));
                            outData[0] = OTAConstant.COMMON_RESULT_SUCCESS;
                            logger.info("TBox(SN:{}): 注册状态验证成功", tboxSn);
                            // 删除Redis中临时公钥
                            removeTboxPubKey(tboxSn);
                        } else {
                            // 向rvm投递tbox激活信息
                            // 数据库锁定执行不成功
                            outData[0] = OTAConstant.QCPASSED_RESULT_FAILED_FOR_OTHER_REASON;
                            logger.warn("TBox(SN:{}): 注册状态检验由于RVM激活失败而失败", tboxSn);
                        }
                    } else {
                        // 数据库锁定执行不成功
                        outData[0] = OTAConstant.QCPASSED_RESULT_FAILED_FOR_OTHER_REASON;
                        logger.warn("TBox(SN:{}): 注册状态检验由于注册信息写入数据库失败而失败", tboxSn);
                    }
                }
            }
        } catch (Exception ex) {
            // 失败，其他原因
            outData[0] = OTAConstant.QCPASSED_RESULT_FAILED_FOR_OTHER_REASON;
            logger.error("TBox(SN:{}): 注册状态验证失败  原因:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
        // 删除当前存储的公钥信息
        // OpRedisRecord.removePkey(TBOX_PUBLIC_KEY_HEADER + tboxSN);
        return outData;
    }
    
    /**
     * @Title: removeTboxPubKey
     * @Description: 清除tbox公钥信息
     * @param: @param
     *             tboxsn
     * @return: void
     * @throws @author
     *             余佶
     * @Date 2017年8月12日 下午1:52:37
     */
    public void removeTboxPubKey(String tboxsn) {
        try {
            redisAPI.removeHash(RedisConstant.TBOX_PUBLIC_KEY_TEMP, tboxsn);
        } catch (Exception ex) {
            // logger.error("Redis connection error, can't do removeTboxPubKey:
            // "+tboxsn);
            logger.error("Redis connection error, TBox({}) can't do removeTboxPubKey:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }

    /**
     * @param tboxSn
     * @return
     * @Title: registerInfoTransferCheck
     * @Description: 向RVM传递注册信息
     */
    private boolean registerInfoTransferCheck(String tboxSn) {
        boolean result = true;
        try {
            // 当发现设备需要传递，并且it redis显示并没注册
            if (dataProcessing.isTBoxDataNeedTransfer(tboxSn)) {
                if (!OperationConstant.DEVICE_LOCK_SUCCESS
                        .equals(dataProcessing.getITTboxInfo(tboxSn).getDeviceLockStatus())) {
                    // 如果rvm的状态是非激活，则尝试传递激活信息
                    Kafka_RegisterData registData = new Kafka_RegisterData();
                    registData.setSn(tboxSn);
                    // 有的tbox，由于OTA版本问题，没有传递vin
                    String loginVin = loginCarInfo.get(tboxSn);
                    if (loginVin != null && loginVin.length() > 0) {
                        registData.setVin(loginVin);
                    }
                    result = kafkaService.transferRegisterData(registData, tboxSn);
                    // 如果传递成功，则等待结果
                    if (result) {
                        registedCheckList.put(tboxSn, TspServiceProc.this);
                        long startTime = System.currentTimeMillis();
                        logger.info("TBox(SN:{})的注册信息已经投递给RVM，等待确认响应。 ", tboxSn);
                        // 等待it通知激活成功后，再回复给tbox，超时等待10s,如果超时，则不再锁定tbox
                        synchronized (TspServiceProc.this) {
                            TspServiceProc.this.wait(OperationConstant.REGISTER_DATA_RESPONSE_WAIT_TIME);
                        }
                        // 如果等待超时，直接通知tbox，激活失败
                        long endTime = System.currentTimeMillis();
                        if (endTime - startTime >= OperationConstant.REGISTER_DATA_RESPONSE_WAIT_TIME) {
                            logger.warn("TBox(SN:{})的RVM激活失败。 ", tboxSn);
                            result = false;
                        } else {
                            logger.info("TBox(SN:{})的RVM激活确认成功。Time:{} ", tboxSn, endTime - startTime);
                            dataProcessing.lockTbox(tboxSn);
                            result = true;
                        }
                    }
                } else {
                    // 状态已经是激活
                    logger.info("Tbox {}已经在RVM激活过。", tboxSn);
                    dataProcessing.lockTbox(tboxSn);
                }
            }
        } catch (Exception ex) {
            result = false;
        } finally {
            registedCheckList.remove(tboxSn);
        }
        return result;
    }

    /**
     * @param requestMsg
     * @return
     * @Title: checkDataRemoteControl
     * @Description: 解析远程控制回复结果
     */
    public byte[] checkDataRemoteControl(OTAMessage requestMsg) {
        // 回包的待加密数据段，含CRC校验字节，初始化为0x00
        return teleControl(requestMsg.getParam(), requestMsg.getSerialNumber());

    }

    /**
     * @throws @author fogmk
     * @Title: TeleControl
     * @Description: 远程控制
     * @param: @param  datagramBytes
     * @param: @param  tboxSn
     * @param: @return
     * @return: byte[]
     * @Date 2017年7月18日 上午11:30:43
     */
    private byte[] teleControl(byte[] datagramBytes, String tboxSn) {
        byte[] outData = new byte[] {};
        boolean whistleFLG = false;
        boolean flashFlg = false;
        boolean controlDoorFlg = false;
        boolean controlACFlg = false;
        boolean controlTempFlg = false;
        boolean warmSeatFlg = false;
        boolean limitSpeedFlg = false;
        boolean ret = false;
        String operation = "";
        // initialize remote control status.
        String remoteCtrolState = ResultStatus.REMOTE_CONTROL_CANNOT_DONE_FOR_OTHER_REASONS.getCode();
        try {
            logger.info("TBox(SN:{}): 当前远程控制TBox上行回复报文数据：{}", tboxSn, ByteUtil.byteToHex(datagramBytes));
            if ((datagramBytes[1] & OTAConstant.WHISTLE_SUCCESS_BIT) == OTAConstant.WHISTLE_SUCCESS_BIT) {
                whistleFLG = true; // bit 0: 1：鸣笛成功；0：鸣笛失败
            }
            if ((datagramBytes[1] & OTAConstant.FLASH_SUCCESS_BIT) == OTAConstant.FLASH_SUCCESS_BIT) {
                flashFlg = true; // bit 1: 1：闪灯成功；0：闪灯失败
            }
            if ((datagramBytes[1] & OTAConstant.CONTROL_DOOR_SUCCESS_BIT) == OTAConstant.CONTROL_DOOR_SUCCESS_BIT) {
                controlDoorFlg = true; // bit 2: 1：上锁解锁成功；0：上锁解锁失败
            }
            if ((datagramBytes[1] & OTAConstant.CONTROL_AC_SUCCESS_BIT) == OTAConstant.CONTROL_AC_SUCCESS_BIT) {
                controlACFlg = true; // bit 3: 1：开关空调成功；0：开关空调失败
            }
            if ((datagramBytes[1]
                    & OTAConstant.CONTROL_AC_TEMP_SUCCESS_BIT) == OTAConstant.CONTROL_AC_TEMP_SUCCESS_BIT) {
                controlTempFlg = true; // bit 4: 1：设置空调温度成功；0：设置空调温度失败
            }
            if ((datagramBytes[1] & OTAConstant.WARM_SEAT_SUCCESS_BIT) == OTAConstant.WARM_SEAT_SUCCESS_BIT) {
                warmSeatFlg = true; // bit 5: 1：座椅加热成功；0：座椅加热失败
            }
            if ((datagramBytes[1]
                    & OTAConstant.LIMIT_SPEED_SEAT_SUCCESS_BIT) == OTAConstant.LIMIT_SPEED_SEAT_SUCCESS_BIT) {
                limitSpeedFlg = true; // bit 6: 1：限速或解除限速成功；0：限速或解除限速失败
            }
            // 当redis可用时，需要确认目前进行的是什么控制，否则不去考虑具体对应的操作
            // if (GlobalSessionChannel.isRedisValid()) {
            RemoteCtrlInfo rectInfo = GlobalSessionChannel.getRemoteCtrl(tboxSn);
            if (rectInfo != null) {
                OTARemoteCommand comd = rectInfo.remoteCmd;
                operation = comd.name();
                logger.info("TBox(SN:{}): 当前远程控制指令:{}", tboxSn, comd);
                // 判断控制结果
                switch (comd) {
                case Search:
                    ret = (whistleFLG || flashFlg);
                    break;
                case ControlDoor:
                    ret = controlDoorFlg;
                    break;
                case AirConditioning:
                    ret = controlACFlg;
                    break;
                case TemperatureSetting:
                    ret = controlTempFlg;
                    break;
                case SeatheatingFrontRight:
                case SeatheatingFrontLeft:
                case SeatheatingRearRight:
                case SeatheatingRearLeft:
                    ret = warmSeatFlg;
                    break;
                case LimitSpeed:
                    ret = limitSpeedFlg;
                    break;
                default:
                    ret = false;
                    break;
                }
            }
            // } else {
            // // redis不可用，根据内存获取操作名称
            // operation = GlobalSessionChannel.getRmtCtrlCmd(tboxSn);
            // ret = whistleFLG || flashFlg || controlDoorFlg || controlACFlg ||
            // controlTempFlg || warmSeatFlg
            // || limitSpeedFlg;
            // }
            byte[] errorCodeBytes = new byte[2];
            System.arraycopy(datagramBytes, 2, errorCodeBytes, 0, 2);
            if (!ret) {
                // 如果失败，需要判断后续两个字节ErrorCode
                short errorCodeShort = ByteUtil.byte2Short(errorCodeBytes);
                String remoteCtrlErrorReason = "";
                switch (errorCodeShort) {
                case OTAConstant.REMOTE_CONTROL_ERR_CODE_POWER_MODE_IS_NOT_OFF:
                    // remoteCtrolState =
                    // ResultStatus.REMOTE_CONTROL_CANNOT_DONE_FOR_DRIVE_MODE.getCode();
                    remoteCtrolState = ResultStatus.RM_REMOTE_CONTROL_CANNOT_DONE_FOR_DRIVE_MODE.getCode();
                    // 0x01= Power mode != OFF
                    remoteCtrlErrorReason = "Power mode 未处于OFF状态";
                    break;
                case OTAConstant.REMOTE_CONTROL_ERR_CODE_OTHER_CMD_IN_PROGRESS:
                    // remoteCtrolState =
                    // ResultStatus.REMOTE_CONTROL_CANNOT_DONE_FOR_CAR_IS_DOING_ANOTHER_OP.getCode();
                    remoteCtrolState = ResultStatus.RM_REMOTE_CONTROL_CANNOT_DONE_FOR_CAR_IS_DOING_ANOTHER_OP.getCode();
                    // 0x03= Other Command in progress =您有其他车辆操作正在执行，请稍候执行
                    remoteCtrlErrorReason = "您有其他车辆操作正在执行，请稍候执行";
                    break;
                case OTAConstant.REMOTE_CONTROL_ERR_CODE_DOOR_OPEN:
                    // remoteCtrolState =
                    // ResultStatus.REMOTE_CONTROL_CANNOT_DONE_FOR_CAR_DOOR_UNCLOSED.getCode();
                    remoteCtrolState = ResultStatus.RM_REMOTE_CONTROL_CANNOT_DONE_FOR_CAR_DOOR_UNCLOSED.getCode();
                    // 0x15= Door Open =车门未关，执行失败
                    remoteCtrlErrorReason = "车门未关";
                    break;
                case OTAConstant.REMOTE_CONTROL_ERR_CODE_DOOR_IS_UNLOCKED:
                    // remoteCtrolState =
                    // ResultStatus.REMOTE_CONTROL_CANNOT_DONE_FOR_CAR_DOOR_UNLOCKED.getCode();
                    remoteCtrolState = ResultStatus.RM_REMOTE_CONTROL_CANNOT_DONE_FOR_CAR_DOOR_UNLOCKED.getCode();
                    // 0x19= Door is unlocked =车门未锁，执行失败
                    remoteCtrlErrorReason = "车门未锁";
                    break;
                default:// 其他错误
                    // remoteCtrolState =
                    // ResultStatus.REMOTE_CONTROL_CANNOT_DONE_FOR_OTHER_REASONS.getCode();
                    remoteCtrolState = ResultStatus.RM_REMOTE_CONTROL_CANNOT_DONE_FOR_OTHER_REASONS.getCode();
                    remoteCtrlErrorReason = "其他错误";
                    // logger.warn("TBox(SN:{}): 当前远程控制失败 原因： 其他错误 错误码(十进制):{}", tboxSn,
                    // errorCodeShort);
                    break;
                }
                logger.warn("TBox(SN: {}): 当前远程控制失败    原因：{}  错误码(十进制)：{}", tboxSn, remoteCtrlErrorReason,
                        errorCodeShort);
            } else {
                // 成功
                remoteCtrolState = ResultStatus.SUCCESS.getCode();
            }
            logger.info(("TBox(SN:{}): 当前远程返回结果:{}"), tboxSn, ret);
            // 12-14增加上行报文消息产生时间
            // ----------12-14 增加上行报文消息产生时间------//
            Date curTime = new Date();
            String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
            // error Code,非成功的操作，需要记录错误码，成功的则错误码为00,即none
            String errCode = "";
            if (!ResultStatus.SUCCESS.getCode().equals(remoteCtrolState)) {
                errCode = ByteUtil.byteToHex(errorCodeBytes[1]);
            } else {
                errCode = "00";
            }
            // --------------------------------------------//
            kafkaService.sndRmtCtrlRep(
                    tboxSn + "_" + operation + "_" + remoteCtrolState + "_" + eventTime + "_" + errCode, tboxSn);

        } catch (Exception ex) {
            logger.error("TBox(SN:{}): 当前远程控制回包处理异常，原因:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
        return outData;
    }

    /**
     * @param requestMsg
     * @return outData
     * @Title: checkDataForward
     * @Description: 透传报文
     */
    public byte[] checkDataForward(OTAMessage requestMsg) {
        try {
            // 获取透传的报文信息
            byte[] outData = null;
            byte[] forwardMsg = requestMsg.getParam();
            if (forwardMsg.length <= OTAConstant.OTA_INVALID_LENGTH_MIN) {
                logger.warn("透传报文信息格式错误，当前报文信息:{}", ByteUtil.byteToHex(requestMsg.curMessage));
                return outData;
            }
            OTAForwardMessage requestForwardMsg = new OTAForwardMessage(forwardMsg);
            if (requestForwardMsg.isAnalysed()) {
                OTAForwardCommand command = OTAForwardCommand.getByCode(requestForwardMsg.getCommand());
                logger.info("当前透传控制指令:{}", command);
                switch (command) {
                case CMD_UP_TAKE_PHOTO: // 拍照结果
                    inDataForwardTakePhoto(requestForwardMsg.getParam(), requestMsg.getSerialNumber());
                    break;
                case CMD_UP_POI: // 下发结果
                    inDataForwardPoi(requestForwardMsg.getParam(), requestMsg.getSerialNumber());
                    break;
                case CMD_UP_TOKEN: // 请求TOKEN
                    outData = inDataForwardToken(requestForwardMsg.getParam(), requestMsg.getSerialNumber());
                    break;
                default:
                    outData = null;
                    break;
                }
            } else {
                logger.warn("TBox(SN:{}): 透传报文信息解析错误，当前报文信息:{}", requestMsg.getSerialNumber(),
                        ByteUtil.byteToHex(requestMsg.curMessage));
            }
            return outData;
        } catch (Exception e) {
            logger.error("透传报文解析因异常失败，原因:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
            return null;
        }
    }

    /**
     * 解析请求TOKEN透传报文,查询token并返回TBox
     *
     * @param datagramBytes
     * @param serialNumber
     */
    private byte[] inDataForwardToken(byte[] datagramBytes, String serialNumber) {
        byte[] outData;
        String ret = "";
        String token = "";
        try {
            logger.info("TBox(SN:{}请求token时间为:{}", serialNumber, ByteUtil.bytesToDataTime(datagramBytes));
            // 从RVM Redis获取ItRedisInfo信息
            ItRedisInfo tBoxInfo = redisAPI.getItValue(serialNumber);
            logger.info("TBox(SN:{})获取TBox对象TBoxInfo:{}", serialNumber, JSONObject.toJSONString(tBoxInfo));
            if (tBoxInfo == null) {
                logger.warn("TBox(SN:{})信息在RVM Redis中不存在!", serialNumber);
                ret = ResultStatus.TOKEN_TBOX_INFO_NULL.getCode();
            } else {
                token = tBoxInfo.getToken();
                if (StringUtils.isBlank(token) || !token.matches("[a-zA-Z0-9]+")) {
                    logger.warn("TBox(SN:{})获取token不存在或不符合要求!", serialNumber);
                    ret = ResultStatus.TOKEN_TBOX_INFO_TOKEN_MSG_NULL.getCode();
                } else {
                    logger.info("TBox(SN:{})获取token为:{}", serialNumber, token);
                }
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})在解析报文，并查询token过程中发生异常:{}", serialNumber,
                    ThrowableUtil.getErrorInfoFromThrowable(e));
            ret = ResultStatus.TOKEN_GET_NULL.getCode();
        }
        outData = new TokenData().getTokenData(ret, token, serialNumber);
        if (!StringUtils.isBlank(ret)) {
            TokenRespInfo tokenRespInfo = new TokenRespInfo();
            tokenRespInfo.setSn(serialNumber);
            tokenRespInfo.setStatus(ret);
            Date currentDate = new Date();
            tokenRespInfo.setDate(DateFormatUtils.format(currentDate, "yyyy-MM-dd"));
            tokenRespInfo.setSeqNo(DateFormatUtils.format(currentDate, "yyyyMMddHHmmssSSS") + "69504");
            tokenRespInfo.setEventTime(currentDate.getTime());
            switch (ret) {
            case "14001":
                tokenRespInfo.setErrDesc(ResultStatus.TOKEN_TBOX_INFO_NULL.getDescription());
                break;
            case "14002":
                tokenRespInfo.setErrDesc(ResultStatus.TOKEN_TBOX_INFO_TOKEN_MSG_NULL.getDescription());
                break;
            case "14003":
                tokenRespInfo.setErrDesc(ResultStatus.TOKEN_GET_NULL.getDescription());
                break;
            default:
                logger.warn("token获取发生其他错误，错误码:{}", ret);
                break;
            }
            kafkaService.sndMesToITForTemplate(KafkaMsgConstant.TOPIC_GET_TOKEN_ERROR, tokenRespInfo, serialNumber);
        }
        return outData;
    }

    /**
     * @param datagramBytes
     * @param tboxSn
     * @Title: inDataForwardPoi
     * @Description: 解析具体透传信息(Poi)
     */
    private void inDataForwardPoi(byte[] datagramBytes, String tboxSn) {
        // 解析具体透传信息
        try {
            if (datagramBytes.length != 1) {
                logger.warn("TBox(SN:{}): 透传报文信息格式错误，当前报文信息:{}", tboxSn, ByteUtil.byteToHex(datagramBytes));
                return;
            }
            PoiRespInfo poiRespInfo = new PoiRespInfo();
            if (datagramBytes[0] == 0) {
                poiRespInfo.setData(0);
            } else {
                poiRespInfo.setData(1);
            }
            Date curTime = new Date();
            String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
            // 生产kakfa消息
            logger.debug("TBox(SN:{}):本次SendPOI请求回复报文已被解析，将产生kafka消息", tboxSn);
            kafkaService.sndForwardRep(tboxSn + "_" + OperationConstant.FORWARD_POI + "_"
                    + JSONObject.toJSONString(poiRespInfo) + "_" + eventTime + "_" + poiRespInfo.getData(), tboxSn);
            return;
        } catch (Exception ex) {
            logger.error("本次poi下发失败，原因:{} ", ThrowableUtil.getErrorInfoFromThrowable(ex));
            return;
        }
    }

    /**
     * @param datagramBytes
     * @param tboxSn
     * @Title: inDataForwardTakePhoto
     * @Description: 解析具体透传信息(TakePhoto)
     */
    private void inDataForwardTakePhoto(byte[] datagramBytes, String tboxSn) {
        // 解析具体透传的信息
        try {
            if (datagramBytes.length <= 2) {
                logger.warn("TBox(SN:{}): 透传报文信息格式错误，当前报文信息:{}", tboxSn, ByteUtil.byteToHex(datagramBytes));
                return;
            }
            // 解析摄像头个数
            int cameraNum = datagramBytes[0];
            if (cameraNum <= 0
                    || (cameraNum * OTAForwardMessagePartSize.CAMERA_RESULT_SIZE.value() + 2 != datagramBytes.length)) {
                // 报文参数长度不正确
                logger.warn("TBox(SN:{}): 摄像头数量错误，当前报文信息:{}", tboxSn, ByteUtil.byteToHex(datagramBytes));
                return;
            }
            // 获取当前正在执行的拍摄序列号，如果不存在，则丢弃当前报文
            String vin = tboxService.getVINForTbox(tboxSn);
            // 解析电源模式
            int powerMode = datagramBytes[datagramBytes.length - 1];
            TakePhotoRespInfo takePhotoResultData = new TakePhotoRespInfo();
            takePhotoResultData.setPowerMode(Integer.toString(powerMode));
            CameraRespInfo[] cameraRespInfos = new CameraRespInfo[cameraNum];
            // 解析摄像头信息
            int errNum = OTAConstant.ZERO;
            int resultFalseNum = OTAConstant.ZERO;
            int carCaptureStatus = OperationConstant.TAKEPHOTO_ORIGIN_STATUS;
            for (int i = 0; i < cameraNum; i++) {
                CameraRespInfo cameraRespInfoData = new CameraRespInfo();
                // 解析各摄像头抓拍成功与否，写入数据库
                int cameraResultOffset = 1 + i * OTAForwardMessagePartSize.CAMERA_RESULT_SIZE.value();
                int cameraResultSize = OTAForwardMessagePartSize.CAMERA_RESULT_SIZE.value();
                byte[] takePhotoResult = new byte[cameraResultSize];
                System.arraycopy(datagramBytes, cameraResultOffset, takePhotoResult, OTAConstant.ARRAY_OFFSET_ZERO,
                        cameraResultSize);
                byte[] takePhotoTime = new byte[OTAConstant.DATETIME_BYTES_SIZE];
                System.arraycopy(datagramBytes, cameraResultOffset, takePhotoTime, OTAConstant.ARRAY_OFFSET_ZERO,
                        OTAConstant.DATETIME_BYTES_SIZE);
                String takePhotoTimeStr = ByteUtil.bytesToDataTime(takePhotoTime);

                int cameraNo = takePhotoResult[OTAConstant.TAKEPHOTO_UP_MSG_CAMERANO_OFFSET];
                int result = takePhotoResult[OTAConstant.TAKEPHOTO_UP_MSG_CAMERA_RESULT_OFFSET];
                if (result == 1) {
                    resultFalseNum++;
                }
                if (cameraNo < Integer.parseInt(OperationConstant.SUPPORTED_CAMERA_NO_MIN)
                        || cameraNo > Integer.parseInt(OperationConstant.SUPPORTED_CAMERA_NO_MAX)
                        || dataProcessing.checkCamera(vin, tboxSn, cameraNo) || (result != 1 && result != 0)) {
                    logger.warn("TBox(SN:{}): 无摄像头或者拍摄错误    摄像头编号:{}   拍摄结果:{}, 拍照时间:{}", tboxSn, cameraNo, result,
                            takePhotoTimeStr);
                    errNum++;
                } else {
                    cameraRespInfoData.setCameraID(Integer.toString(cameraNo));
                    cameraRespInfoData.setResult(Integer.toString(result));
                    cameraRespInfos[i] = cameraRespInfoData;
                }
            }
            if (errNum == cameraNum) {
                // 更新数据库，将数据库的状态更新为
                carCaptureStatus = OperationConstant.TAKEPHOTO_FAILED_FOR_OTHER_REASON;
                logger.warn("TBox(SN:{}): 本次由于其他原因拍摄失败 ", tboxSn);
                return;
            }
            takePhotoResultData.setCameraResult(cameraRespInfos);
            if (resultFalseNum == OTAConstant.ZERO) {
                // 完全成功
                carCaptureStatus = OperationConstant.TAKEPHOTO_COMPLETE_SUCCESS;
            } else if (resultFalseNum == cameraNum) {
                // 完全失败
                carCaptureStatus = OperationConstant.TAKEPHOTO_FAILED_FOR_OTHER_REASON;
            } else {
                // 部分失败
                carCaptureStatus = OperationConstant.TAKEPHOTO_PARTLY_SUCCESS;
            }
            logger.debug("TBox(SN:{}): 本次拍摄结果已被解析，将产生kafka消息    拍摄结果:{} ", tboxSn,
                    JSONObject.toJSONString(takePhotoResultData));
            // 12-14增加上行报文消息产生时间处理
            Date curTime = new Date();
            String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
            kafkaService.sndForwardRep(
                    tboxSn + "_" + OperationConstant.FORWARD_TAKEPHOTO + "_"
                            + JSONObject.toJSONString(takePhotoResultData) + "_" + eventTime + "_" + carCaptureStatus,
                    tboxSn);
            return;
        } catch (Exception ex) {
            logger.error("TBox(SN:{})本次拍摄失败，原因:{} ", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return;
        }
    }

    /**
     * @param requestMsg
     * @return
     * @Title: checkDataReportPos
     * @Description: 解析GPS信息
     */
    public byte[] checkDataReportPos(OTAMessage requestMsg) {
        // 回包的待加密数据段，含CRC校验字节，初始化为0x00
        return inDataGPSGroup(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @throws @author fogmk
     * @Title: inDataGPSGroup
     * @Description: 上报GPS信息
     * @param: @param  datagramBytes
     * @param: @param  serialNumber
     * @param: @return
     * @return: byte[]
     * @Date 2017年7月18日 下午2:25:21
     */
    private byte[] inDataGPSGroup(byte[] datagramBytes, String tboxSn) {

        byte[] outData = {};
        // if (currentVIN == null) {
        // logger.warn("TBox(SN:{}): 从Redis获取VIN号异常", tboxSn);
        // currentVIN = "";
        // }
        try {
            short databyteLength = (short) datagramBytes[0];
            int offset = 1;
            int signledataLen = ReportPos.byteDataLength;
            int errorNumber = 0;
            Kafka_LocationData locData = new Kafka_LocationData();
            locData.setSn(tboxSn);
            List<Kafka_GPSData> gpsData = new ArrayList<Kafka_GPSData>();

            if ((databyteLength * signledataLen + 1) == datagramBytes.length) {
                // 数据长度正确
                ReportPos[] poses = new ReportPos[databyteLength];
                for (int i = 0; i < databyteLength; i++) {
                    // 获取当前数据组
                    byte[] curPosData = new byte[signledataLen];

                    System.arraycopy(datagramBytes, offset, curPosData, 0, signledataLen);
                    offset += signledataLen;

                    ReportPos rpPosData = new ReportPos(curPosData);
                    Kafka_GPSData curKafkaGpsData = new Kafka_GPSData();
                    if (rpPosData.isAnalysisOk()) {
                        poses[i] = rpPosData;
                        BigDecimal labd = new BigDecimal(
                                Math.abs(rpPosData.getLatitude()) / OperationConstant.GPS_PRECISION);
                        BigDecimal lgbd = new BigDecimal(
                                Math.abs(rpPosData.getLongitude()) / OperationConstant.GPS_PRECISION);
                        curKafkaGpsData.setLatitude(
                                labd.setScale(OperationConstant.GPS_PRECISION_SCALE, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue());
                        curKafkaGpsData.setLongitude(
                                lgbd.setScale(OperationConstant.GPS_PRECISION_SCALE, BigDecimal.ROUND_HALF_UP)
                                        .doubleValue());
                        curKafkaGpsData.setCollectTime(rpPosData.getCollectTimeStamp());
                        StringBuilder curStatus = new StringBuilder();
                        String validStatus = OperationConstant.INVALID; // 数据有效性
                        if (rpPosData.getValidStatus() > 0) {
                            validStatus = OperationConstant.VALID;
                        }
                        String latitude = OperationConstant.SOUTH_LATITUDE; // 南北纬
                        if (rpPosData.getLatitude() > 0) {
                            latitude = OperationConstant.NORTH_LATITUDE;
                        }
                        String logitude = OperationConstant.WEST_LONGITUDE; // 东西经
                        if (rpPosData.getLongitude() > 0) {
                            logitude = OperationConstant.EAST_LONGITUDE;
                        }
                        curStatus.append(validStatus).append(OperationConstant.COMMA).append(latitude)
                                .append(OperationConstant.COMMA).append(logitude);
                        curKafkaGpsData.setPosStatus(curStatus.toString());
                        gpsData.add(curKafkaGpsData);
                    } else {
                        logger.warn("TBox(SN:{}): GPS数据异常    错误数据:{}", tboxSn, ByteUtil.byteToHex(curPosData));
                        errorNumber++;
                    }
                }
                if (errorNumber != databyteLength) {
                    locData.setItems(gpsData);
                    if (dataProcessing.isTBoxDataNeedTransfer(tboxSn)) {
                        kafkaService.transferLocationData(locData, tboxSn);
                    }
                    // // TODO:当融合项目切换完成，确认不再向原APP服务时，停止想stream-task发送数据
                    // this.streamSender.sendGps(JSONObject.toJSONString(gpsStream,
                    // SerializerFeature.WriteMapNullValue));
                }
            } else {
                logger.warn("TBox(SN:{}): 上传GPS数据异常    原因：参数错误", tboxSn);
            }
        } catch (Exception ex) {
            logger.error("TBox(SN:{}): 上传GPS数据异常    原因:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }

        return outData;
    }

    /**
     * @param requestMsg
     * @return
     * @Title: checkDataGetPos
     * @Description: 向tbox请求获取GPS后，tbox上传的GPS
     */
    public byte[] checkDataGetPos(OTAMessage requestMsg) {

        return inDataGPS(requestMsg.getParam(), requestMsg.getSerialNumber());

    }

    /**
     * @param datagramBytes
     * @param tboxSn
     * @return outData
     * @Title: inDataGPS
     * @Description: 请求上传GPS
     */
    public byte[] inDataGPS(byte[] datagramBytes, String tboxSn) {
        byte[] outData = new byte[] {};
        try {
            ReportPos reportPos = new ReportPos(datagramBytes);
            if (reportPos.isAnalysisOk()) {
                logger.info("TBox(SN:{}): 获取实时GPS数据    Latitude:{} Longitude:{}", tboxSn, reportPos.getLatitude(),
                        reportPos.getLongitude());
                // 调用Option下的类，将获取的GPS信息写入Redis中
                String runtimeGPS = "{\"latitude\":\"" + reportPos.getLatitude() + "\",\"longitude\":\""
                        + reportPos.getLongitude() + "\",\"precision\":\"" + "1000000" + "\",\"collectDate\":\""
                        + reportPos.getCollectDate() + "\"}";
                addGpsPos(tboxSn, runtimeGPS);
            } else {
                logger.warn("TBox(SN:{}): 上传GPS数据异常，原因：参数错误", tboxSn);
            }
        } catch (Exception ex) {
            logger.error("TBox(SN:{}): 上传GPS数据异常，原因:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }

        return outData;
    }
    
    /**
     * 添加实时位置信息
     *
     * @Title: addGpsPos
     * @Description: 添加实时位置信息
     * @param: @param
     *             tboxsn
     * @param: @param
     *             reportPos
     * @return: void
     * @throws @author
     *             fogmk
     * @Date 2017年7月18日 下午3:04:49
     */
    public void addGpsPos(String tboxsn, String currentGPS) {
        try {
            redisAPI.setHash(RedisConstant.CAR_CUR_POS, tboxsn, currentGPS);
        } catch (Exception ex) {
            logger.error("Redis connection error,TBox({}) can't do addGpsPos:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }
    
    /**
     * 根据SN获取对应的远程升级值
     * @param tboxSn
     * @return
     */
    public String getRemoteUpdate(String tboxSn) {
        try {
            return redisAPI.getHash(RedisConstant.ON_REMOTE_UPDATE_OTA, tboxSn);
        } catch (Exception ex) {
            logger.error("Redis connection error,TBox({}) can't do getRemoteUpdate:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
        return null;
    }
    
    /**
     * 添加远程升级记录
     * @param tboxSn
     * @param updateInfo
     */
    public void addRemoteUpdate(String tboxSn, String updateInfo) {
        try {
            redisAPI.setHash(RedisConstant.ON_REMOTE_UPDATE_OTA, tboxSn, updateInfo);
        } catch (Exception ex) {
            logger.error("Redis connection error,TBox({}) can't do addRemoteUpdate:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }

    /**
     * @param requestMsg
     * @return
     * @Title: checkDataRemoteUpdate
     * @Description: 远程升级解析
     */
    public byte[] checkDataRemoteUpdate(OTAMessage requestMsg) {
        String sn = requestMsg.getSerialNumber();
        try {
            // 获取结果
            int remoteUpdateResult = requestMsg.getParam()[0];

            if (getRemoteUpdate(sn) != null) {
                TboxFileLoadStatus updateInfo = JSONObject.parseObject(getRemoteUpdate(sn),
                        TboxFileLoadStatus.class);
                updateInfo.setResult(Integer.toString(remoteUpdateResult));
                addRemoteUpdate(sn, JSONObject.toJSONString(updateInfo));
                // 发送kfk消息通知IT
                TboxUpdateRvmResponse tboxUpdateRvmResponse = new TboxUpdateRvmResponse();
                tboxUpdateRvmResponse.setSn(sn);
                tboxUpdateRvmResponse.setData(remoteUpdateResult);
                tboxUpdateRvmResponse.setSeqNo(updateInfo.getSeqNo());
                tboxUpdateRvmResponse.setEventTime(updateInfo.getEventTime());
                // status description
                // todo
                switch (remoteUpdateResult) {
                case 0:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.REMOTE_UPDATE_SUCCESS.getCode());
                    tboxUpdateRvmResponse.setDescription(ResultStatus.REMOTE_UPDATE_SUCCESS.getDescription());
                    break;
                case 1:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.REMOTE_UPDATE_DOWNLOADING.getCode());
                    tboxUpdateRvmResponse.setDescription(ResultStatus.REMOTE_UPDATE_DOWNLOADING.getDescription());
                    break;
                case 2:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.REMOTE_UPDATE_FINISH_DOWNLOAD.getCode());
                    tboxUpdateRvmResponse.setDescription(ResultStatus.REMOTE_UPDATE_FINISH_DOWNLOAD.getDescription());
                    break;
                case 3:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.REMOTE_UPDATE_DOWNLOAD_FAILED.getCode());
                    tboxUpdateRvmResponse.setDescription(ResultStatus.REMOTE_UPDATE_DOWNLOAD_FAILED.getDescription());
                    break;
                case 4:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.REMOTE_UPDATE_CHECK_FAILED.getCode());
                    tboxUpdateRvmResponse.setDescription(ResultStatus.REMOTE_UPDATE_CHECK_FAILED.getDescription());
                    break;
                case 5:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.REMOTE_UPDATE_OTHERREASON_FAILED.getCode());
                    tboxUpdateRvmResponse
                            .setDescription(ResultStatus.REMOTE_UPDATE_OTHERREASON_FAILED.getDescription());
                    break;
                case 6:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.REMOTE_UPDATING.getCode());
                    tboxUpdateRvmResponse.setDescription(ResultStatus.REMOTE_UPDATING.getDescription());
                    break;
                default:
                    break;
                }
                logger.debug("TBox(sn:{})远程升级返回结果:{}", sn, JSONObject.toJSONString(tboxUpdateRvmResponse));
                kafkaService.transferTboxFOTAResponse(tboxUpdateRvmResponse);

            }

            if (this.tspPlatformClient.updateVerStatus(sn, remoteUpdateResult)) {
                // serial number存在时，更新数据库,修改更新时间
                logger.info("TBox(SN:{}): 更新数据库信息成功    Status:{}", sn, remoteUpdateResult);
            } else {
                // serial number可能不存在表中
                logger.warn("TBox(SN:{}): 当前TBox信息不存在    Status:{}", sn, remoteUpdateResult);
            }

        } catch (Exception ex) {
            logger.error("TBox(SN:{}): 当前TBox远程升级失败    原因:{}", sn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
        return new byte[] {};
    }

    /**
     * @param requestMsg
     * @return
     * @Title: checkDataAlarm
     * @Description: 解析报警报文
     */
    public byte[] checkDataAlarm(OTAMessage requestMsg) {

        return inDataAlarm(requestMsg.getParam(), requestMsg.getSerialNumber());

    }

    /**
     * @param datagramBytes
     * @param tboxSn
     * @return outData
     * @Title: inDataAlarm
     * @Description: 详细解析报警报文
     */
    public byte[] inDataAlarm(byte[] datagramBytes, String tboxSn) {
        byte[] outData = { OTAConstant.COMMON_RESULT_SUCCESS }; // 初始化为 成功
        String currentVIN = tboxService.getVINForTbox(tboxSn);
        WarningInfo curwarnInfo = new WarningInfo();
        // OwnerBusNichNamesVO ownerBusInfo =
        // this.tspPlatformClient.getCarInfoBytboxSN(tboxSn);
        // this.tspPlatformClient.getOwnerInfoBySN(tboxSn);
        String warnHeader = "";
        // if (ownerBusInfo == null || ownerInfo == null) {
        // logger.warn(("TBox(SN: {}): 无法在数据库中找到与当前报警信息相符的车辆或车主",
        // tboxSn));
        // } else {
        // curwarnInfo.setOwnerBusInfo(ownerBusInfo);
        // curwarnInfo.setOwnerInfo(ownerInfo);
        // String carName = ownerBusInfo.getCarName();
        // String carType = ownerBusInfo.getCarType();
        // warnHeader = "您的爱车（" + carName + "）" + carType;
        // }
        // if (ownerBusInfo != null && ownerBusInfo.getCarType() == "G10") {
        // logger.warn(("TBox(SN: {}): 收到车辆报警信息，但当前车辆无车辆报警功能",
        // tboxSn));
        // return outData;
        // }
        curwarnInfo.setCurrentVIN(currentVIN);
        curwarnInfo.setTboxsn(tboxSn);
        analyseWarningInfo(datagramBytes, tboxSn, curwarnInfo, currentVIN, warnHeader);
        return outData;
    }

    private byte[] analyseWarningInfo(byte[] datagramBytes, String tboxSn, WarningInfo curwarnInfo, String currentVIN,
            String warnHeader) {
        byte[] outData = { OTAConstant.COMMON_RESULT_SUCCESS }; // 初始化为 成功
        String happenTime = "";
        // 初始化报警信息
        CodeValue[] transferwarningInfo = new CodeValue[TransferWarningTypeEnum.values().length];
        int index = 0;
        for (TransferWarningTypeEnum warning : TransferWarningTypeEnum.values()) {
            transferwarningInfo[index] = new CodeValue(warning.getCode(), warning.getValue());
            ++index;
        }
        try {
            if (datagramBytes.length == OTAConstant.OTA_WARNING_MSG_LENGTH) {
                // check alarm position
                byte[] warningDetail = new byte[OTAConstant.OTA_WARNING_DETAIL_LENGTH];
                System.arraycopy(datagramBytes, OTAConstant.OTA_WARNING_DETAIL_OFFSET, warningDetail, OTAConstant.ZERO,
                        OTAConstant.OTA_WARNING_DETAIL_LENGTH);
                byte[] curPosData = new byte[ReportPos.byteDataLength];
                System.arraycopy(datagramBytes, OTAConstant.OTA_WARNING_GPS_OFFSET, curPosData, OTAConstant.ZERO,
                        ReportPos.byteDataLength);
                ReportPos rpPosData = new ReportPos(curPosData);
                happenTime = rpPosData.getCollectDate();
                if (happenTime.compareTo(OperationConstant.WARNING_VALID_COLLECT_TIME) < 0) {
                    happenTime = DateFormatUtils.format(new Date(), DatePatternConstant.SECOND);
                }
                // check warning type
                curwarnInfo.setRpPosData(rpPosData);
                curwarnInfo.setHappenTime(happenTime);
                curwarnInfo.setWarnTitle("车辆异常报警提醒[发生时间" + curwarnInfo.getHappenTime() + "]");
                curwarnInfo.setWarnType(datagramBytes[0]);
                curwarnInfo.setWarnDetail(ByteUtil.getUnsignedInt(warningDetail));
                Date collectDate;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    collectDate = sdf.parse(happenTime);
                } catch (Exception ex) {
                    logger.error("TBox(SN:{}): 当前车辆报警信息收集时间有误，原因:{}", tboxSn,
                            ThrowableUtil.getErrorInfoFromThrowable(ex));
                    outData[0] = OTAConstant.COMMON_RESULT_FAILED;
                    return outData;
                }
                ReportAlarm reportAlarm = new ReportAlarm();
                reportAlarm.setCreateTime(collectDate);
                reportAlarm.setVin(currentVIN);
                reportAlarm.setSn(tboxSn);
                reportAlarm.setLatitude((int) rpPosData.getLatitude());
                reportAlarm.setLongitude((int) rpPosData.getLongitude());
                switch (datagramBytes[0]) {
                case OTAConstant.WARNING_FOR_OPENING_CARDOORS_ILLEGALLY: // 异常进入情况
                    curwarnInfo.setSmsWarningText(WarningTypeConstant.ABNORMAL_ENTRY.getSmsDescription());
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_DETAIL_FOR_OPENING_FL_DOOR_ILLEGALLY) == OTAConstant.WARNING_DETAIL_FOR_OPENING_FL_DOOR_ILLEGALLY) { // 驾驶座车门打开
                        curwarnInfo.setWarnContent(warnHeader + WarningContentConstant.DRIVER_DOOR_OPEN.getMessage());
                        transferwarningInfo[TransferWarningTypeEnum.OPENING_FL_DOOR_ILLEGALLY.getIndex()]
                                .setValue(true);
                        // asyncCheckPushThreading(curwarnInfo);

                    }
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_DETAIL_FOR_OPENING_FR_DOOR_ILLEGALLY) == OTAConstant.WARNING_DETAIL_FOR_OPENING_FR_DOOR_ILLEGALLY) { // 副驾驶座车门打开
                        transferwarningInfo[TransferWarningTypeEnum.OPENING_FR_DOOR_ILLEGALLY.getIndex()]
                                .setValue(true);
                        curwarnInfo
                                .setWarnContent(warnHeader + WarningContentConstant.CO_DRIVER_DOOR_OPEN.getMessage());
                        // asyncCheckPushThreading(curwarnInfo);

                    }
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_DETAIL_FOR_OPENING_BR_DOOR_ILLEGALLY) == OTAConstant.WARNING_DETAIL_FOR_OPENING_BR_DOOR_ILLEGALLY) { // 右后座车门打开
                        transferwarningInfo[TransferWarningTypeEnum.OPENING_BR_DOOR_ILLEGALLY.getIndex()]
                                .setValue(true);
                        curwarnInfo
                                .setWarnContent(warnHeader + WarningContentConstant.RIGHT_REAR_DOOR_OPEN.getMessage());
                        // asyncCheckPushThreading(curwarnInfo);

                    }
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_DETAIL_FOR_OPENING_BL_DOOR_ILLEGALLY) == OTAConstant.WARNING_DETAIL_FOR_OPENING_BL_DOOR_ILLEGALLY) { // 左后座车门打开
                        transferwarningInfo[TransferWarningTypeEnum.OPENING_BL_DOOR_ILLEGALLY.getIndex()]
                                .setValue(true);
                        curwarnInfo
                                .setWarnContent(warnHeader + WarningContentConstant.LEFT_REAR_DOOR_OPEN.getMessage());
                        // asyncCheckPushThreading(curwarnInfo);

                    }
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_DETAIL_FOR_OPENING_TAIL_DOOR_ILLEGALLY) == OTAConstant.WARNING_DETAIL_FOR_OPENING_TAIL_DOOR_ILLEGALLY) { // 尾门打开
                        transferwarningInfo[TransferWarningTypeEnum.OPENING_TAIL_DOOR_ILLEGALLY.getIndex()]
                                .setValue(true);
                        curwarnInfo.setWarnContent(warnHeader + WarningContentConstant.TAILGATE_OPEN.getMessage());
                        // asyncCheckPushThreading(curwarnInfo);

                    }
                    // asyncPushAlarmForSubscription(reportAlarm,
                    // AlarmTypeConstant.ABNORMAL_ENTRY);
                    break;
                case OTAConstant.WARNING_FOR_MOVING_VEHICLE_ILLEGALLY: // 非法移动
                    curwarnInfo.setSmsWarningText(WarningTypeConstant.INVALID_MOVEMENT.getSmsDescription());
                    curwarnInfo.setWarnContent(warnHeader + WarningContentConstant.INVALID_MOVEMENT.getMessage());
                    transferwarningInfo[TransferWarningTypeEnum.MOVING_VEHICLE_ILLEGALLY.getIndex()].setValue(true);
                    // asyncCheckPushThreading(curwarnInfo);
                    break;
                case OTAConstant.WARNING_FOR_OPENING_HOOD_ILLEGALLY: // 引擎盖异常打开
                    curwarnInfo.setSmsWarningText(WarningTypeConstant.OPENING_HOOD.getSmsDescription());
                    curwarnInfo.setWarnContent(warnHeader + WarningContentConstant.OPENING_HOOD_ILLEGALLY.getMessage());
                    transferwarningInfo[TransferWarningTypeEnum.OPENING_HOOD_ILLEGALLY.getIndex()].setValue(true);
                    // asyncCheckPushThreading(curwarnInfo);
                    break;
                case OTAConstant.WARNING_FOR_DISASSEMBLING_TIRES_ILLEGALLY: // 轮胎非法拆卸
                    curwarnInfo.setSmsWarningText(WarningTypeConstant.DISASSEMBLING_TIRES.getSmsDescription());
                    curwarnInfo.setWarnContent(
                            warnHeader + WarningContentConstant.DISASSEMBLING_TIRES_ILLEGALLY.getMessage());
                    transferwarningInfo[TransferWarningTypeEnum.DISASSEMBLING_TIRES_ILLEGALLY.getIndex()]
                            .setValue(true);
                    // asyncCheckPushThreading(curwarnInfo);
                    break;
                case OTAConstant.WARNING_FOR_VEHICLE_COLLISION: // 车辆碰撞
                    curwarnInfo.setSmsWarningText(WarningTypeConstant.VEHICLE_COLLISION.getSmsDescription());
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_FOR_VEHICLE_FRONTSIDE_COLLISION) == OTAConstant.WARNING_FOR_VEHICLE_FRONTSIDE_COLLISION) { // 前部碰撞
                        curwarnInfo.setWarnContent(
                                warnHeader + WarningContentConstant.VEHICLE_FRONTSIDE_COLLISION.getMessage());
                        transferwarningInfo[TransferWarningTypeEnum.VEHICLE_FRONTSIDE_COLLISION.getIndex()]
                                .setValue(true);
                        // asyncCheckPushThreading(curwarnInfo);
                    }
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_FOR_VEHICLE_BACKSIDE_COLLISION) == OTAConstant.WARNING_FOR_VEHICLE_BACKSIDE_COLLISION) { // 后部碰撞
                        curwarnInfo.setWarnContent(
                                warnHeader + WarningContentConstant.VEHICLE_BACKSIDE_COLLISION.getMessage());
                        transferwarningInfo[TransferWarningTypeEnum.VEHICLE_BACKSIDE_COLLISION.getIndex()]
                                .setValue(true);
                        // asyncCheckPushThreading(curwarnInfo);
                    }
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_FOR_VEHICLE_LEFTSIDE_COLLISION) == OTAConstant.WARNING_FOR_VEHICLE_LEFTSIDE_COLLISION) { // 做不碰撞
                        curwarnInfo.setWarnContent(
                                warnHeader + WarningContentConstant.VEHICLE_LEFTSIDE_COLLISION.getMessage());
                        transferwarningInfo[TransferWarningTypeEnum.VEHICLE_LEFTSIDE_COLLISION.getIndex()]
                                .setValue(true);
                        // asyncCheckPushThreading(curwarnInfo);
                    }
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_FOR_VEHICLE_RIGHTSIDE_COLLISION) == OTAConstant.WARNING_FOR_VEHICLE_RIGHTSIDE_COLLISION) { // 右部碰撞
                        curwarnInfo.setWarnContent(
                                warnHeader + WarningContentConstant.VEHICLE_RIGHTSIDE_COLLISION.getMessage());
                        transferwarningInfo[TransferWarningTypeEnum.VEHICLE_RIGHTSIDE_COLLISION.getIndex()]
                                .setValue(true);
                        // asyncCheckPushThreading(curwarnInfo);
                    }
                    break;
                case OTAConstant.WARNING_FOR_FATIGUE_DRIVING: // 疲劳驾驶
                    curwarnInfo.setSmsWarningText(WarningTypeConstant.FATIGUE_DRIVING.getSmsDescription());
                    curwarnInfo.setWarnContent(warnHeader + WarningContentConstant.FATIGUE_DRIVING.getMessage());
                    transferwarningInfo[TransferWarningTypeEnum.FATIGUE_DRIVING.getIndex()].setValue(true);
                    // asyncCheckPushThreading(curwarnInfo);
                    break;
                case OTAConstant.WARNING_FOR_CAN_DATA_INTERRUPTION: // CAN数据中断
                    curwarnInfo.setWarnTitle(WarningTypeConstant.CAN_INTERRUPT.getDescription());
                    curwarnInfo.setSmsWarningText(WarningTypeConstant.CAN_INTERRUPT.getSmsDescription());
                    curwarnInfo.setWarnContent(warnHeader + WarningContentConstant.CAN_DATA_INTERRUPTION.getMessage());
                    transferwarningInfo[TransferWarningTypeEnum.CAN_DATA_INTERRUPTION.getIndex()].setValue(true);
                    // asyncCheckPushThreading(curwarnInfo);
                    break;
                case OTAConstant.WARNING_FOR_VEHICLE_MULFUNCTION_UPDATED: // 车辆故障状态更新,这部分转发需要使用FAULT格式
                    List<Integer> fault = new ArrayList<>();
                    // logger.warn(("TBox(SN: {}):
                    // 车辆上传了故障状态更新报警信息。", tboxSn));
                    curwarnInfo.setWarnTitle(WarningTypeConstant.DTC_CODE_UPDATE.getDescription());
                    curwarnInfo.setSmsWarningText(WarningTypeConstant.DTC_CODE_UPDATE.getSmsDescription());
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_DETAIL_FOR_ESP_MULFUNCTION) == OTAConstant.WARNING_DETAIL_FOR_ESP_MULFUNCTION) { // ESP故障
                        curwarnInfo.setWarnContent(warnHeader + WarningContentConstant.ESP_MULFUNCTION.getMessage());
                        fault.add(Integer.valueOf(FaultDataEnum.ESP_FAULT.getNumber()));
                    }
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_DETAIL_FOR_ABS_MULFUNCTION) == OTAConstant.WARNING_DETAIL_FOR_ABS_MULFUNCTION) { // ABS故障
                        curwarnInfo.setWarnContent(warnHeader + WarningContentConstant.ABS_MULFUNCTION.getMessage());
                        fault.add(Integer.valueOf(FaultDataEnum.ABS_FAULT.getNumber()));
                        // ABS故障需要推送给外部平台
                        // asyncPushAlarmForSubscription(reportAlarm,
                        // AlarmTypeConstant.ABS_MULFUNCTION);
                    }
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_DETAIL_FOR_EMISSION_MULFUNCTION) == OTAConstant.WARNING_DETAIL_FOR_EMISSION_MULFUNCTION) { // 排放故障
                        curwarnInfo
                                .setWarnContent(warnHeader + WarningContentConstant.EMISSION_MULFUNCTION.getMessage());
                        fault.add(Integer.valueOf(FaultDataEnum.EMISSION_FAULT.getNumber()));
                    }
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_DETAIL_FOR_ENGINE_MULFUNCTION) == OTAConstant.WARNING_DETAIL_FOR_ENGINE_MULFUNCTION) { // 发动机故障
                        curwarnInfo.setWarnContent(warnHeader + WarningContentConstant.ENGINE_MULFUNCTION.getMessage());
                        fault.add(Integer.valueOf(FaultDataEnum.ENGINE_FAULT.getNumber()));
                        // 发动机故障需要推送给外部平台
                        // asyncPushAlarmForSubscription(reportAlarm,
                        // AlarmTypeConstant.ENGINE_MULFUNCTION);
                    }
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_DETAIL_FOR_AIRBAG_MULFUNCTION) == OTAConstant.WARNING_DETAIL_FOR_AIRBAG_MULFUNCTION) { // 安全气囊故障
                        curwarnInfo.setWarnContent(warnHeader + WarningContentConstant.AIRBAG_MULFUNCTION.getMessage());
                        fault.add(Integer.valueOf(FaultDataEnum.SAFETY_AIR_BAG_FAULT.getNumber()));
                    }
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_DETAIL_FOR_EBD_MULFUNCTION) == OTAConstant.WARNING_DETAIL_FOR_EBD_MULFUNCTION) { // EBD故障
                        curwarnInfo.setWarnContent(warnHeader + WarningContentConstant.EBD_MULFUNCTION.getMessage());
                        fault.add(Integer.valueOf(FaultDataEnum.EBD_FAULT.getNumber()));
                        // EBD故障需要推送给外部平台
                        // asyncPushAlarmForSubscription(reportAlarm,
                        // AlarmTypeConstant.EBD_MULFUNCTION);
                    }
                    curwarnInfo.setFault(fault);
                    pushFauluToIt(curwarnInfo);
                    break;
                case OTAConstant.WARNING_FOR_GETTING_GPSPOS_FAILURE: // GPS获取故障
                    curwarnInfo.setWarnTitle(WarningTypeConstant.GETTING_GPSPOS_FAILURE.getDescription());
                    curwarnInfo.setSmsWarningText(WarningTypeConstant.GETTING_GPSPOS_FAILURE.getSmsDescription());
                    curwarnInfo.setWarnContent(warnHeader + WarningContentConstant.GETTING_GPSPOS_FAILURE.getMessage());
                    transferwarningInfo[TransferWarningTypeEnum.GETTING_GPSPOS_FAILURE.getIndex()].setValue(true);
                    // asyncCheckPushThreading(curwarnInfo);
                    break;
                case OTAConstant.WARNING_FOR_DRIVING_LANE_DEPARTURE: // 车道偏离
                    curwarnInfo.setWarnTitle(WarningTypeConstant.DRIVING_LANE_DEPARTURE.getDescription());
                    curwarnInfo.setSmsWarningText(WarningTypeConstant.DRIVING_LANE_DEPARTURE.getSmsDescription());
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_FOR_DRIVING_LEFTLANE_DEPARTURE) == OTAConstant.WARNING_FOR_DRIVING_LEFTLANE_DEPARTURE) { // 车道向左偏移
                        curwarnInfo.setWarnContent(
                                warnHeader + WarningContentConstant.DRIVING_LEFTLANE_DEPARTURE.getMessage());
                        transferwarningInfo[TransferWarningTypeEnum.DRIVING_LEFTLANE_DEPARTURE.getIndex()]
                                .setValue(true);
                        // asyncCheckPushThreading(curwarnInfo);
                    }
                    if ((warningDetail[1]
                            & OTAConstant.WARNING_FOR_DRIVING_RIGHTLANE_DEPARTURE) == OTAConstant.WARNING_FOR_DRIVING_RIGHTLANE_DEPARTURE) { // 车道向右偏移
                        curwarnInfo.setWarnContent(
                                warnHeader + WarningContentConstant.DRIVING_RIGHTLANE_DEPARTURE.getMessage());
                        transferwarningInfo[TransferWarningTypeEnum.DRIVING_RIGHTLANE_DEPARTURE.getIndex()]
                                .setValue(true);
                        // asyncCheckPushThreading(curwarnInfo);
                    }
                    break;
                default:
                    break;
                }
                if (curwarnInfo.getWarnType() != OTAConstant.WARNING_FOR_VEHICLE_MULFUNCTION_UPDATED) {
                    // 如果type非故障，按warning格式上报
                    transferWarningInfo(transferwarningInfo, curwarnInfo);
                }
            } else {
                logger.warn("TBox(SN:{}): 车辆报警信息异常，原因：参数错误", tboxSn);
                outData[0] = OTAConstant.COMMON_RESULT_FAILED;
            }
        } catch (Exception ex) {
            outData[0] = OTAConstant.COMMON_RESULT_FAILED;
            logger.error("TBox(SN:{}): 车辆报警信息异常，原因:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
        return outData;
    }

    /**
     * @param transferwarningInfos
     * @param curwarnInfo
     * @Title: transferWarningInfo
     * @Description: 将报警信息发送给RVM
     */
    private void transferWarningInfo(CodeValue[] transferwarningInfos, WarningInfo curwarnInfo) {
        // 给IT发送报警信息
        try {
            logger.debug("collectData:{}", curwarnInfo.getHappenTime() + JSONObject.toJSON(transferwarningInfos));
            kafkaService.transferOTAData(curwarnInfo.getTboxsn(), KafkaOtaDataCommand.KAFKA_PARAM_WARNING,
                    DateUtil.stringToLong(curwarnInfo.getHappenTime(), "yyyy-MM-dd HH:mm:ss"), transferwarningInfos,
                    gatewayTimeIn);
        } catch (Exception e) {
            logger.error("格式化日期信息错误，错误原因:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
        }
    }

    /**
     * @param tboxSn
     * @Title: initialVINforSN
     * @Description: 异步判断tbox的绑定VIN号
     */
    public void initialVINforSN(String tboxSn) {
        final String curTboxSn = tboxSn;
        fixedThreadPool.execute(() -> {
            // 记录当前TBox对应绑定车辆车架号信息
            // 从数据库获取vin码
            String curVin = tboxService.getVINForTbox(curTboxSn);
            Date collectTime = new Date();
            if (dataProcessing.isTBoxDataNeedTransfer(curTboxSn)) {
                // 如果是batteryNumber不为0,设计
                Kafka_OTAData otaData = new Kafka_OTAData();
                otaData.setSn(curTboxSn);
                if (!StringUtils.isEmpty(curVin)) {
                    otaData.setVin(curVin);
                }
                otaData.setCollectTime(collectTime.getTime());
                otaData.setCommand(KafkaOtaDataCommand.KAFKA_PARAM_LOGIN);
                // loginCarInfo.get(curTboxSn);
                List<String> curBatteryInfo = batteryInfo.get(curTboxSn);
                CodeValue[] batteryItem = new CodeValue[2];
                int batteryNo = 0;
                if (curBatteryInfo != null) {
                    batteryNo = curBatteryInfo.size();
                } else {
                    curBatteryInfo = new ArrayList<String>();
                }
                batteryItem[0] = new CodeValue(OTARealTimeConstants.BATTERY_NUM, batteryNo);
                batteryItem[1] = new CodeValue(OTARealTimeConstants.BATTERY_CODE, curBatteryInfo);
                otaData.setItems(batteryItem);
                otaData.setGatewayTimeIn(gatewayTimeIn);
                otaData.setGatewayTimeOut(System.currentTimeMillis());
                kafkaService.transferOTAData(otaData, KafkaOtaDataCommand.KAFKA_PARAM_LOGIN, collectTime.getTime());
            }
        });
    }
    
    /**
     * @Title: 根据操作指令确认是否有该指令引起的唤醒操作
     * @param tboxSn
     * @return
     * @author zhuna
     * @date 2018年12月4日
     */
    public boolean existWakeUp(String tboxSn) {
        return redisAPI.hasKey(RedisConstant.WAKE_UP + "_" + tboxSn);
    }

    /**
     * @param tboxSn
     * @Title: checkWakeUpNotify
     * @Description: 异步生产kafka唤醒通知，由每个网关节点的kafka消费者判断在该节点上是否存在阻塞的控制指令（由于tbox不在线需要进行唤醒的阻塞）
     */
    public void checkWakeUpNotify(String tboxSn) {
        final String curTboxSn = tboxSn;
        fixedThreadPool.execute(() -> {
            try {
                if (curTboxSn != null && curTboxSn != "") {
                    // 如果是唤醒登陆，去除唤醒Redis记录，以及通知唤醒线程成功
                    Thread.sleep(OperationConstant.OTA_DOWN_MESSAGE_SEND_DELAY_TIME);
                    if (existWakeUp(curTboxSn)) {
                        // 如果Redis可用，删除该数据，
                        logger.info("TBox(SN:{}): 存在远程指令唤醒记录，将当前TBox唤醒记录移除Redis数据库", curTboxSn);
                        redisAPI.delete(RedisConstant.WAKE_UP + "_" + curTboxSn);
                    }
                    Date curTime = new Date();
                    String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
                    // 这里得通过kafka生产一个唤醒上传
                    kafkaService.sndWakeUp(eventTime, curTboxSn);
                }
            } catch (Exception ex) {
                logger.error("TBox(SN:{}): 检查当前TBox唤醒信息异常，原因:{}", curTboxSn,
                        ThrowableUtil.getErrorInfoFromThrowable(ex));
            }
        });
    }

    /**
     * @param requestMsg
     * @return outData
     * @Title: checkDataRealTime
     * @Description: 检查实时数据上报流程
     */
    public byte[] checkDataRealTime(OTAMessage requestMsg) {
        byte[] outData = new TspRealTimeDataServiceProc(this.kafkaService, this.gatewayTimeIn)
                .checkRealTimeData(requestMsg.getParam(), requestMsg.getSerialNumber());
        return outData;
    }

    /**
     * @param requestMsg
     * @param commandByCode
     * @return outData
     * @Title: checkDataGBloginlogout
     * @Description: 检查国标登录流程
     */
    public byte[] checkDataGBloginlogout(OTAMessage requestMsg, OTACommand commandByCode) {
        byte[] outData = null;
        if (requestMsg.getParam().length != OTAConstant.DATETIME_BYTES_SIZE) {
            outData = new byte[] { OTAConstant.COMMON_RESULT_FAILED };
        } else {
            try {
                String datetime = ByteUtil.bytesToDataTime(requestMsg.getParam());
                logger.info("TBox(SN:{})国标指令{}时间为{}", requestMsg.getSerialNumber(), commandByCode, datetime);
                outData = new byte[] { OTAConstant.COMMON_RESULT_SUCCESS };
            } catch (Exception ex) {
                logger.error("TBox(SN:{})检查国标登录流程发生异常:{}", requestMsg.getSerialNumber(),
                        ThrowableUtil.getErrorInfoFromThrowable(ex));
                outData = new byte[] { OTAConstant.COMMON_RESULT_FAILED };
            }
        }
        return outData;
    }

    /**
     * @param requestMsg
     * @return
     * @Title: checkDataFault
     * @Description: 故障报文解析
     */
    public byte[] checkDataFault(OTAMessage requestMsg) {
        return inDataFault(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param datagramBytes
     * @param tboxSn
     * @return
     * @Title: inDataFault
     * @Description: 故障报文解析
     */
    private byte[] inDataFault(byte[] datagramBytes, String tboxSn) {
        // 初始化为 成功
        byte[] outData = { OTAConstant.COMMON_RESULT_SUCCESS };
        String currentVIN = tboxService.getVINForTbox(tboxSn);
        WarningInfo curwarnInfo = new WarningInfo();
        // OwnerBusNichNamesVO ownerBusInfo =
        // this.tspPlatformClient.getCarInfoBytboxSN(tboxSn);
        // this.tspPlatformClient.getOwnerInfoBySN(tboxSn);
        String warnHeader = "";
        // if (ownerBusInfo == null || ownerInfo == null) {
        // logger.warn(("TBox(SN: {}): 无法在数据库中找到与当前故障提醒信息相符的车辆或车主",
        // tboxSn));
        // } else {
        // curwarnInfo.setOwnerBusInfo(ownerBusInfo);
        // curwarnInfo.setOwnerInfo(ownerInfo);
        // String carName = ownerBusInfo.getCarName();
        // String carType = ownerBusInfo.getCarType();
        // warnHeader = "您的爱车(" + carName + ")" + carType + "目前遇到一则故障报警，";
        // }
        // if (ownerBusInfo != null && ownerBusInfo.getCarType() == "G10") {
        // logger.warn(("TBox(SN: {}): 收到车辆故障提醒信息，但当前车辆无车辆故障提醒功能",
        // tboxSn));
        // return outData;
        // }
        curwarnInfo.setCurrentVIN(currentVIN);
        curwarnInfo.setTboxsn(tboxSn);
        outData = analyseFaultInfo(datagramBytes, tboxSn, curwarnInfo, currentVIN, warnHeader);
        return outData;
    }

    /**
     * @param datagramBytes
     * @param tboxSn
     * @param curwarnInfo
     * @param currentVIN
     * @param warnHeader
     * @return
     * @Title: analyseFaultInfo
     * @Description: 故障信息分析
     */
    private byte[] analyseFaultInfo(byte[] datagramBytes, String tboxSn, WarningInfo curwarnInfo, String currentVIN,
            String warnHeader) {
        // 初始化为 成功
        logger.debug("TBox(SN:{})当前上传故障报文解密后参数部分为:{}", tboxSn, ByteUtil.byteToHex(datagramBytes));
        byte[] outData = { OTAConstant.COMMON_RESULT_SUCCESS };
        String happenTime = "";

        // 新增 故障个数
        byte[] _FaultCnt = new byte[1];
        System.arraycopy(datagramBytes, 0, _FaultCnt, 0, 1);
        int faultCnt = _FaultCnt[0];
        if (faultCnt > OperationConstant.MAX_FAULT_NO_SUPPORTED) {
            logger.warn("TBox(SN:{}): 车辆故障提醒信息异常，原因：TBox上传故障数超过{}个", tboxSn, OperationConstant.MAX_FAULT_NO_SUPPORTED);
            outData[0] = OTAConstant.COMMON_RESULT_FAILED;
        } else {
            try {
                // 新增 校验报文参数长度
                if (datagramBytes.length == OperationConstant.FIX_FAULT_MSG_BYTES + faultCnt) {
                    // 更改 报警日期&定位
                    byte[] curPosData = new byte[ReportPos.byteDataLength];
                    System.arraycopy(datagramBytes, 1 + faultCnt, curPosData, OTAConstant.ZERO,
                            ReportPos.byteDataLength);
                    ReportPos rpPosData = new ReportPos(curPosData);
                    // 新增 faultList
                    byte[] _FaultList = new byte[faultCnt];
                    System.arraycopy(datagramBytes, 1, _FaultList, 0, faultCnt);
                    int[] faultList = new int[faultCnt];
                    happenTime = rpPosData.getCollectDate();
                    // 若为2018年之前的数据，将发生时间置为当前时间 原61需求，弃用
                    /*
                     * if (happenTime.compareTo(OperationConstant.WARNING_VALID_COLLECT_TIME) < 0) {
                     * happenTime = DateFormatUtils.format(new Date(), DatePatternConstant.MINUTE);
                     * }
                     */
                    // check warning type

                    curwarnInfo.setRpPosData(rpPosData);
                    curwarnInfo.setHappenTime(happenTime);
                    curwarnInfo.setWarnType(8);
                    // 车辆故障提醒
                    curwarnInfo.setWarnTitle("车辆故障提醒");
                    curwarnInfo.setAppPush(warnHeader + "点击查看");
                    curwarnInfo.setSmsWarningText(warnHeader + "请尽快到经销商进行检查，以免影响您的行车安全。上汽大通");
                    List<Integer> fault = new ArrayList<>();
                    for (int i = 0; i < faultCnt; i++) {
                        faultList[i] = _FaultList[i];
                        fault.add(faultList[i]);
                    }
                    curwarnInfo.setFault(fault);
                    // 向RVM推送故障信息
                    pushFauluToIt(curwarnInfo);
                } else {
                    logger.warn("TBox(SN:{}): 车辆故障提醒信息异常，原因：参数错误", tboxSn);
                    outData[0] = OTAConstant.COMMON_RESULT_FAILED;
                }
            } catch (Exception ex) {
                outData[0] = OTAConstant.COMMON_RESULT_FAILED;
                logger.error("TBox(SN:{}): 车辆故障提醒信息异常，原因:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            }
        }
        return outData;
    }

    /**
     * @param checkwarnInfo
     * @return result
     * @Title: pushFauluToIt
     * @Description: 向RVM推送故障信息
     */
    private boolean pushFauluToIt(WarningInfo checkwarnInfo) {
        boolean result = false;
        int faultListLen = FaultDataEnum.values().length;
        CodeValue[] transferWarningInfoStream = new CodeValue[faultListLen];
        // 给IT发送报警信息
        try {
            for (int j = 0; j < faultListLen; j++) {
                CodeValue codeValue = new CodeValue();
                codeValue.setCode(FaultDataEnum.values()[j].getCode());
                if (!checkwarnInfo.getFault().contains(j + 1)) {
                    codeValue.setValue(false);
                } else {
                    codeValue.setValue(true);
                }
                transferWarningInfoStream[j] = codeValue;
            }
            logger.debug("TBox(SN:{})当前故障上报发送给IT内容为:{}", checkwarnInfo.getTboxsn(),
                    JSONObject.toJSONString(transferWarningInfoStream));
            kafkaService.transferOTAData(checkwarnInfo.getTboxsn(), KafkaOtaDataCommand.KAFKA_PARAM_FAULT,
                    DateUtil.stringToLong(checkwarnInfo.getHappenTime(), "yyyy-MM-dd HH:mm:ss"),
                    transferWarningInfoStream, gatewayTimeIn);
            logger.debug("故障收集时间collectData:{}",
                    checkwarnInfo.getHappenTime() + JSONObject.toJSON(transferWarningInfoStream));
            result = true;

        } catch (Exception e) {
            logger.error("转投it错误，错误原因:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
        }
        return result;
    }
    
    public boolean existChannelInfo(String tboxsn) {
        boolean result = redisAPI.hasKey(RedisConstant.TBOX_CHANNEL, tboxsn);
        return result;
    }

    // 解析远程配置结果

    /**
     * @param datagramBytes
     * @param tboxSn
     * @Title: byteToHex
     * @Description: 解析远程配置结果
     */
    private void inDataRmtConfig(byte[] datagramBytes, String tboxSn) {
        //
        logger.info("解析远程配置结果参数:{}", ByteUtil.byteToHex(datagramBytes));
        int rmtConfigResult = 0;
        RemoteCtrlResponseData rmtData = new RemoteCtrlResponseData();
        // 获取结果
        try {
            if (datagramBytes.length == 3) {
                byte[] tboxResult = new byte[1];
                byte[] tboxErrorCode = new byte[2];
                System.arraycopy(datagramBytes, 0, tboxResult, 0, 1);
                System.arraycopy(datagramBytes, 1, tboxErrorCode, 0, 2);
                if (datagramBytes[0] == OTAConstant.REMOTE_CONFIG_SUCCESS) {
                    rmtConfigResult = OTAConstant.REMOTE_CONFIG_SUCCESS;
                    logger.info("TBox(SN:{})远程配置成功", tboxSn);
                } else if (datagramBytes[0] == 1) {
                    // rmtConfigResult = datagramBytes[0];
                    logger.info("TBox(SN:{})远程配置失败", tboxSn);
                    rmtConfigResult = Integer
                            .parseUnsignedInt(ResultStatus.REMOTE_CONTROL_FAILED_FOR_EXCEPTION.getCode());
                } else {
                    rmtConfigResult = Integer
                            .parseUnsignedInt(ResultStatus.RM_REMOTE_CONTROL_CANNOT_DONE_FOR_OTHER_REASONS.getCode());
                    logger.warn("TBox(SN:{})远程配置失败: result:{}  ,error code :{}", tboxSn, tboxResult[0],
                            ByteUtil.byteToHex(tboxErrorCode));
                }
                rmtData.setErrCode(ByteUtil.byteToHex(tboxErrorCode));

                if (existChannelInfo(tboxSn)) {
                    addRemoteCtrlRes(tboxSn, "status: " + rmtConfigResult);
                }

                Date curTime = new Date();
                String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");

                String dataValue = JSONObject.toJSONString(rmtData);
                logger.debug("TBox(SN:{})获取的远程配置结果为:{}", dataValue);
                kafkaService.sndRmtConfigRep(tboxSn + "_" + rmtConfigResult + "_" + dataValue + "_" + eventTime,
                        tboxSn);
            } else {
                logger.warn("TBox(SN:{}): 当前远程配置报文数据参数长度错误。", tboxSn);
            }
        } catch (Exception ex) {
            logger.error("TBox(SN:{}): 当前远程配置回包处理异常，原因:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }

    }
    
    /**
     * @Title: addRemoteCtrlRes
     * @Description: 添加一条远程控制返回结果
     * @param: @param
     *             tboxsn
     * @param: @param
     *             remoteCtrlCommand
     * @return: void
     * @throws @author
     *             yuji
     * @Date 2017年7月18日 上午11:22:43
     */
    public boolean addRemoteCtrlRes(String tboxsn, String remoteCtrlResp) {
        try {
            redisAPI.setHash(RedisConstant.CAR_REMOTE_CTRL_RESP, tboxsn, remoteCtrlResp);
            return true;
        } catch (Exception ex) {
            logger.error("Redis connection error, TBox({}) can't do addRemoteCtrlRes:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return false;
        }
    }

    /**
     * @param datagramBytes
     * @param tboxSn
     * @Title: inGetRmtConfig
     * @Description: 解析Tbox上传的远程配置信息
     */
    private void inGetRmtConfig(byte[] datagramBytes, String tboxSn) {
        logger.info("获取的TBox远程配置报文为:{}", ByteUtil.byteToHex(datagramBytes));
        int result = 0;
        int paramSize = 0;
        int idCode = 0;
        long value = 0;
        RemoteCtrlResponseData rmtData = new RemoteCtrlResponseData();
        List<RemoteConfigParam> paramlist = new ArrayList<>();

        // 获取结果00 00 0A 00 02 00 05 00 03 00 00 00 04
        try {
            byte[] tboxResult = new byte[1];
            byte[] tboxidCode = new byte[2];
            byte[] tboxParamSize = new byte[2];
            System.arraycopy(datagramBytes, 1, tboxParamSize, 0, 2);
            paramSize = ByteUtil.getUnsignedInt(tboxParamSize);
            if (datagramBytes.length == (paramSize + 3)) {
                // 处理结果
                System.arraycopy(datagramBytes, 0, tboxResult, 0, 1);
                result = Integer.parseInt(ByteUtil.getUnsignedChar(tboxResult[0]));
                // idcode
                int paramIndex = 0;
                byte[] tboxValue4 = new byte[4];
                byte[] tboxValue2 = new byte[2];
                while (paramIndex < paramSize) {
                    RemoteConfigParam configParam = new RemoteConfigParam();
                    System.arraycopy(datagramBytes, 3 + paramIndex, tboxidCode, 0, 2);
                    idCode = ByteUtil.getUnsignedInt(tboxidCode);
                    if (idCode == 3) {
                        System.arraycopy(datagramBytes, 3 + paramIndex + 2, tboxValue4, 0, 4);
                        value = ByteUtil.getUnsignedLong(tboxValue4);
                        paramIndex = paramIndex + 6;
                    } else {
                        System.arraycopy(datagramBytes, 3 + paramIndex + 2, tboxValue2, 0, 2);
                        value = ByteUtil.getUnsignedInt(tboxValue2);
                        paramIndex = paramIndex + 4;
                    }
                    configParam.setConfigID(idCode);
                    configParam.setValue(String.valueOf(value));
                    paramlist.add(configParam);
                }

                RemoteConfigParam[] paramArray = new RemoteConfigParam[paramlist.size()];
                paramIndex = 0;
                for (RemoteConfigParam param : paramlist) {
                    paramArray[paramIndex++] = param;
                }

                rmtData.setParam(paramArray);
                String dataValue = JSONObject.toJSONString(rmtData);
                logger.debug("获取的远程配置结果为:{}", dataValue);

                if (existChannelInfo(tboxSn)) {
                    addRemoteCtrlRes(tboxSn, "status: " + result);
                }

                Date curTime = new Date();
                String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");

                kafkaService.sndGetRmtConfigRep(tboxSn + "_" + result + "_" + dataValue + "_" + eventTime, tboxSn);

            } else {
                logger.warn("TBox(SN:{}): 从TBox获取的远程配置报文数据参数长度错误。", tboxSn);
            }

        } catch (Exception ex) {
            logger.error("TBox(SN:{}): 解析从TBox获取的远程配置报文异常，原因:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }

    }

    /**
     * @param requestMsg
     * @Title: checkDataRmtConfig
     * @Description: 远程配置TBOX
     */
    public void checkDataRmtConfig(OTAMessage requestMsg) {
        inDataRmtConfig(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param requestMsg
     * @Title: checkDataRmtConfig
     * @Description: 远程获取Tbox配置
     */
    public void checkGetRmtConfig(OTAMessage requestMsg) {
        inGetRmtConfig(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param requestMsg
     * @return outData
     * @Title: checkDataRmtConfig
     * @Description: 大数据分析
     */
    public byte[] checkDataBigData(OTAMessage requestMsg) {

        return inDataBigData(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param datagramBytes
     * @param tboxSn
     * @return
     * @Title: checkDataRmtConfig
     * @Description: 大数据透传解析
     */
    private byte[] inDataBigData(byte[] datagramBytes, String tboxSn) {
        byte[] outData = new byte[] { OTAConstant.COMMON_RESULT_SUCCESS };
        if (datagramBytes.length < OTAConstant.BIG_DATA_COMPRESS_TYPE_SIZE + OTAConstant.BIG_DATA_CONTENT_SIZE
                + OTAConstant.DATETIME_BYTES_SIZE + OTAConstant.OTA_VERSION_LENGTH) {
            logger.warn("TBox(SN:{}):上传的大数据数据不完整.", tboxSn);
            outData[0] = OTAConstant.COMMON_RESULT_FAILED;
        } else {
            // 解析压缩模式
            int compressionMode = datagramBytes[OTAConstant.BIG_DATA_COMPRESS_TYPE_OFFSET];
            if (compressionMode != 0 && compressionMode != 1) {
                logger.warn("TBox(SN:{}):上传的大数据压缩模式错误 ，值为:{}", tboxSn, compressionMode);
                outData[0] = OTAConstant.COMMON_RESULT_FAILED;
            } else {
                // 解析大数据长度
                byte[] bigDataSizeByte = new byte[OTAConstant.BIG_DATA_CONTENT_SIZE];
                System.arraycopy(datagramBytes, OTAConstant.BIG_DATA_CONTENT_SIZE_OFFSET, bigDataSizeByte, 0,
                        OTAConstant.BIG_DATA_CONTENT_SIZE);
                int bigDataSize = ByteUtil.getUnsignedInt(bigDataSizeByte);
                if (datagramBytes.length != (OTAConstant.BIG_DATA_COMPRESS_TYPE_SIZE + OTAConstant.BIG_DATA_CONTENT_SIZE
                        + bigDataSize + OTAConstant.DATETIME_BYTES_SIZE + OTAConstant.OTA_VERSION_LENGTH)) {
                    logger.warn("TBox(SN:{}):上传的大数据数据长度错误 ，值为:{}", tboxSn, bigDataSize);
                    outData[0] = OTAConstant.COMMON_RESULT_FAILED;
                } else {
                    byte[] bigDataByte = new byte[bigDataSize];
                    System.arraycopy(datagramBytes, OTAConstant.BIG_DATA_CONTENT_OFFSET, bigDataByte, 0, bigDataSize);
                    // 检查收集时间
                    byte[] dataTime = new byte[OTAConstant.DATETIME_BYTES_SIZE];
                    System.arraycopy(datagramBytes, OTAConstant.BIG_DATA_CONTENT_OFFSET + bigDataSize, dataTime, 0,
                            OTAConstant.DATETIME_BYTES_SIZE);
                    String collectTime = ByteUtil.bytesToDataTime(dataTime);
                    if (collectTime.compareTo(OperationConstant.WARNING_VALID_COLLECT_TIME) < 0) {
                        collectTime = DateFormatUtils.format(new Date(), DatePatternConstant.SECOND);
                    }
                    logger.debug("TBox(SN:{}):上传的大数据,压缩类型为:{},收集时间为:{},数据为:{}", tboxSn, compressionMode, collectTime,
                            ByteUtil.byteToHex(bigDataByte));
                    // 给IT发送大数据内容
                    try {
                        kafkaService.transferOTAData(tboxSn, DateUtil.stringToLong(collectTime, "yyyy-MM-dd HH:mm:ss"),
                                compressionMode, bigDataByte);
                    } catch (Exception e) {
                        logger.error("TBox(SN:{})大数据格式化日期信息错误，错误原因:{}", tboxSn,
                                ThrowableUtil.getErrorInfoFromThrowable(e));
                        ;
                    }
                }
            }
        }

        return outData;
    }

    /**
     * @param requestMsg
     * @Title: checkDataStartDrtRep
     * @Description: 开始往国家平台发数据 指令执行结果解析
     */
    public void checkDataStartDrtRep(OTAMessage requestMsg) {
        inDataStartDrtRep(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param param
     * @param serialNumber
     * @Title: inDataStartDrtRep
     * @Description: 开始往国家平台发数据 指令执行结果解析
     */
    private void inDataStartDrtRep(byte[] param, String serialNumber) {
        try {
            String status = ResultStatus.DIRECT_REPORT_START_FAILED_FOR_OTHER_REASON.getCode();
            if (param.length != 1) {
                logger.warn("TBox(SN:{}): 开始往国家平台发数据的指令执行结果解析失败，原因：param长度错误", serialNumber);
            } else {
                switch (param[0]) {
                case OTAConstant.START_DIRECT_REPORT_SUCCESS:
                    logger.info("TBox(SN:{}): 开始往国家平台发数据的指令执行结果:成功", serialNumber);
                    status = ResultStatus.SUCCESS.getCode();
                    break;
                case OTAConstant.START_DIRECT_REPORT_SERVER_CONNECTRING_FAILED:
                    logger.info("TBox(SN:{}): 开始往国家平台发数据的指令执行结果:失败，原因：连接服务器失败", serialNumber);
                    status = ResultStatus.DIRECT_REPORT_START_FAILED_FOR_CONNECT_SERVICE_ERROR.getCode();
                    break;
                case OTAConstant.START_DIRECT_REPORT_LOGIN_FAILED:
                    logger.info("TBox(SN:{}): 开始往国家平台发数据的指令执行结果:失败，原因：登入失败", serialNumber);
                    status = ResultStatus.DIRECT_REPORT_START_FAILED_FOR_LOGIN_SERVICE_ERROR.getCode();
                    break;
                case OTAConstant.START_DIRECT_REPORT_OTHER_FAILED:
                    logger.info("TBox(SN:{}): 开始往国家平台发数据的指令执行结果:失败，原因：其他", serialNumber);
                    break;
                default:
                    logger.warn("TBox(SN:{}): 开始往国家平台发数据的指令执行结果解析失败，原因：param内容错误", serialNumber);
                    break;
                }
            }
            Date curTime = new Date();
            String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
            kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_TBOX_DIRECT_REPORT_RES,
                    serialNumber + "_" + status + "_" + eventTime, serialNumber);
        } catch (Exception ex) {
            logger.error("TBox(SN:{}): 停止往国家平台发数据的指令执行结果解析发生异常:{}", serialNumber,
                    ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }

    /**
     * @param requestMsg
     * @Title: checkDataStopDrtRep
     * @Description: 停止往国家平台发数据 指令执行结果解析
     */
    public void checkDataStopDrtRep(OTAMessage requestMsg) {
        inDataStopDrtRep(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param param
     * @param serialNumber
     * @Title: inDataStopDrtRep
     * @Description: 停止往国家平台发数据 指令执行结果解析
     */
    private void inDataStopDrtRep(byte[] param, String serialNumber) {
        try {
            String status = ResultStatus.DIRECT_REPORT_STOP_FAILED.getCode();
            if (param.length != 1) {
                logger.warn("TBox(SN:{}): 停止往国家平台发数据的指令执行结果解析失败，原因：param长度错误", serialNumber);
            } else {
                switch (param[0]) {
                case OTAConstant.STOP_DIRECT_REPORT_SUCCESS:
                    logger.info("TBox(SN:{}): 停止往国家平台发数据的指令执行结果:成功", serialNumber);
                    status = ResultStatus.SUCCESS.getCode();
                    break;
                case OTAConstant.STOP_DIRECT_REPORT_FAILED:
                    logger.info("TBox(SN:{}): 停止往国家平台发数据的指令执行结果:失败", serialNumber);
                    break;
                default:
                    logger.warn("TBox(SN:{}): 停止往国家平台发数据的指令执行结果解析失败，原因：param内容错误", serialNumber);
                    break;
                }
            }
            Date curTime = new Date();
            String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
            kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_TBOX_DIRECT_REPORT_RES,
                    serialNumber + "_" + status + "_" + eventTime, serialNumber);
        } catch (Exception ex) {
            logger.error("TBox(SN:{}): 停止往国家平台发数据的指令执行结果解析发生异常:{}", serialNumber,
                    ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }

    /**
     * @param requestMsg
     * @Title: checkGetVehicleStatus
     * @Description: 处理获取远程车况
     */
    public void checkGetVehicleStatus(OTAMessage requestMsg) {
        inGetVehicleStatusRep(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param param
     * @param serialNumber
     * @Title: inGetVehicleStatusRep
     * @Description: 处理获取远程车况
     */
    private void inGetVehicleStatusRep(byte[] param, String serialNumber) {
        // 00 07 E1 0A 13 13 2F 31 00 03 00 01 03
        try {
            byte[] resultByte = new byte[1];
            byte[] eventTime = new byte[7];
            byte[] paramSize = new byte[2];

            byte[] id = new byte[2];
            // byte[] type = new byte[1];

            System.arraycopy(param, 1, eventTime, 0, 7);
            System.arraycopy(param, 8, paramSize, 0, 2);
            resultByte[0] = param[0];
            // 处理结果
            String result = Integer.toString(resultByte[0]);
            // 获取车况时间
            String getStatusTime = ByteUtil.bytesToDataTime(eventTime);
            Short paramSizeS = ByteUtil.byte2Short(paramSize);
            if (10 + paramSizeS != param.length) {
                logger.warn("TBox({})获取车况状态报文解析,报文参数长度不正确", serialNumber);
                return;
            }
            RemoteCtrlResponseData rmtData = new RemoteCtrlResponseData();

            List<VehicleStatusDataMo> dataList = new ArrayList<>();
            // 获取车况返回结果成功，否则不处理后续参数报文
            if (resultByte[0] == OTAConstant.COMMON_RESULT_SUCCESS) {
                rmtData.setErrCode("0");
                byte[] paramContent = new byte[paramSizeS];
                int key;
                int index = 0;
                byte[] numU8 = new byte[1];
                byte[] numU16 = new byte[2];
                // 获取参数报文
                System.arraycopy(param, 10, paramContent, 0, paramSizeS);
                CarWindowStatus win = new CarWindowStatus();
                while (index < paramSizeS) {
                    System.arraycopy(paramContent, index, id, 0, 2);
                    key = ByteUtil.getUnsignedInt(id);
                    // 字段id偏移
                    index += 2;
                    // 空调状态
                    if (1 == key) {
                        VehicleStatusDataMo data = new VehicleStatusDataMo();
                        System.arraycopy(paramContent, index, numU8, 0, 1);
                        // 空调状态结果偏移
                        index += 1;
                        data.setId(key);
                        // 有效空调状态值0-4
                        if (null != GetVehicleStatusAirConditionEnum.getValue(numU8[0])) {
                            data.setType(GetVehicleStatusAirConditionEnum.getValue(numU8[0]).getType());
                        } else {
                            data.setType(GetVehicleStatusAirConditionEnum.AIR_CONDITION_INVALID.getType());
                        }
                        data.setValue((int) numU8[0]);
                        dataList.add(data);
                    } else if (2 == key) {// 座椅加热状态
                        VehicleStatusDataMo data = new VehicleStatusDataMo();
                        System.arraycopy(paramContent, index, numU16, 0, 2);
                        // 座椅加热结果偏移
                        index += 2;
                        data.setId(key);
                        data.setType("");
                        data.setValue(getSeatHeatResult(numU16));
                        dataList.add(data);
                    } else if (3 <= key && 7 >= key) {// 车窗状态
                        System.arraycopy(paramContent, index, numU8, 0, 1);
                        // 车窗状态结果偏移
                        index += 1;
                        getCarWindowsResult(numU8, key, win);
                        // data.setId(3);
                        // data.setType("");
                        // data.setValue(win);
                    } else {
                        logger.warn("TBox(sn:{})字段id不正确，为{}：", serialNumber, key);
                    }

                }
                VehicleStatusDataMo data = new VehicleStatusDataMo();
                data.setId(3);
                data.setType("");
                data.setValue(win);
                dataList.add(data);
            } else {
                String errorCode = String.valueOf((int) resultByte[0]);
                rmtData.setErrCode(errorCode);
                logger.warn("TBox(sn:{})获取车辆状态返回结果处理不成功，为{}：", serialNumber, errorCode);
            }
            VehicleStatusDataMo[] vehicleData = null;
            if (dataList.size() > 0) {
                vehicleData = new VehicleStatusDataMo[dataList.size()];
                for (int index = 0; index < dataList.size(); index++) {
                    vehicleData[index] = dataList.get(index);
                }
            }

            rmtData.setVehicleStatus(vehicleData);
            Date curTime = new Date();
            String dealEventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
            String value = result + "@" + getStatusTime + "@" + JSONObject.toJSONString(rmtData);
            logger.debug("TBox(sn:{})获取车况内容:{}; ", serialNumber, value);
            kafkaService.sndMesForTemplate(KafkaMsgConstant.GET_VEHICLE_STATUS_UP_CTRL,
                    serialNumber + "_" + value + "_" + dealEventTime, serialNumber);
        } catch (Exception ex) {
            logger.error("TBox(sn:{})解析获取的车况内容失败;原因:{} ", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }

    }

    /**
     * @param
     * @return
     * @method 解析座椅加热结果
     * @description
     * @author zhuna
     * @date 2019/2/21 18:46
     */
    public List<Map<String, Object>> getSeatHeatResult(byte[] numU16) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        // ********** 左前座椅 ******
        Map<String, Object> map = new HashMap<>();
        map.put("seat", "leftFront");
        if ((numU16[1] & (byte) 0x01) == (byte) 0x01 && (numU16[1] & (byte) 0x02) != (byte) 0x02) {// ON
            // map.put("value", 1);
            map.put("description", "On");
        } else if ((numU16[1] & (byte) 0x01) != (byte) 0x01 && (numU16[1] & (byte) 0x02) != (byte) 0x02) {// OFF
            // map.put("value", 0);
            map.put("description", "Off");
        } else if ((numU16[1] & (byte) 0x01) != (byte) 0x01 && (numU16[1] & (byte) 0x02) == (byte) 0x02) {// 无效
            // map.put("value", 2);
            map.put("description", "Invalid");
        } else if ((numU16[1] & (byte) 0x01) == (byte) 0x01 && (numU16[1] & (byte) 0x02) == (byte) 0x02) {// 无效
            // map.put("value", 3);
            map.put("description", "Invalid");
        }
        mapList.add(map);

        // ********** 右前座椅 ******
        map = new HashMap<>();
        map.put("seat", "rightFront");
        if ((numU16[1] & (byte) 0x04) == (byte) 0x04 && (numU16[1] & (byte) 0x08) != (byte) 0x08) {// ON
            // map.put("value", 1);
            map.put("description", "On");
        } else if ((numU16[1] & (byte) 0x04) != (byte) 0x04 && (numU16[1] & (byte) 0x08) != (byte) 0x08) {// OFF
            // map.put("value", 0);
            map.put("description", "Off");
        } else if ((numU16[1] & (byte) 0x04) != (byte) 0x04 && (numU16[1] & (byte) 0x08) == (byte) 0x08) {// 无效
            // map.put("value", 2);
            map.put("description", "Invalid");
        } else if ((numU16[1] & (byte) 0x04) == (byte) 0x04 && (numU16[1] & (byte) 0x08) == (byte) 0x08) {// 无效
            // map.put("value", 3);
            map.put("description", "Invalid");
        }
        mapList.add(map);

        // ********** 左后座椅 ******
        map = new HashMap<>();
        map.put("seat", "leftRear");
        if ((numU16[1] & (byte) 0x10) == (byte) 0x10 && (numU16[1] & (byte) 0x20) != (byte) 0x20) {// ON
            // map.put("value", 1);
            map.put("description", "On");
        } else if ((numU16[1] & (byte) 0x10) != (byte) 0x10 && (numU16[1] & (byte) 0x20) != (byte) 0x20) {// OFF
            // map.put("value", 0);
            map.put("description", "Off");
        } else if ((numU16[1] & (byte) 0x10) != (byte) 0x10 && (numU16[1] & (byte) 0x20) == (byte) 0x20) {// 无效
            // map.put("value", 2);
            map.put("description", "Invalid");
        } else if ((numU16[1] & (byte) 0x10) == (byte) 0x10 && (numU16[1] & (byte) 0x20) == (byte) 0x20) {// 无效
            // map.put("value", 3);
            map.put("description", "Invalid");
        }
        mapList.add(map);

        // ********** 右后座椅 ******
        map = new HashMap<>();
        map.put("seat", "rightRear");
        if ((numU16[1] & (byte) 0x40) == (byte) 0x40 && (numU16[1] & (byte) 0x80) != (byte) 0x80) {// ON
            // map.put("value", 1);
            map.put("description", "On");
        } else if ((numU16[1] & (byte) 0x40) != (byte) 0x40 && (numU16[1] & (byte) 0x80) != (byte) 0x80) {// OFF
            // map.put("value", 0);
            map.put("description", "Off");
        } else if ((numU16[1] & (byte) 0x40) != (byte) 0x40 && (numU16[1] & (byte) 0x80) == (byte) 0x80) {// 无效
            // map.put("value", 2);
            map.put("description", "Invalid");
        } else if ((numU16[1] & (byte) 0x40) == (byte) 0x40 && (numU16[1] & (byte) 0x80) == (byte) 0x80) {// 无效
            // map.put("value", 3);
            map.put("description", "Invalid");
        }
        mapList.add(map);
        return mapList;
    }

    /**
     * @param
     * @return
     * @method 解析车窗开合度结果
     * @description
     * @author ssh
     * @date 2019/3/6 13:34
     */
    public void getCarWindowsResult(byte[] numU8, int key, CarWindowStatus win) {
        // **********天窗************
        if (3 == key) {
            if ((int) numU8[0] > 100 || (int) numU8[0] < 0) {// 无效
                win.setTop("invalid");
            } else if ((int) numU8[0] >= 0 && (int) numU8[0] <= 100) {// 正常
                String valid = (int) numU8[0] + "";
                win.setTop(valid);
            }
        }

        // **********左后车窗************
        if (4 == key) {
            if ((int) numU8[0] > 100 || (int) numU8[0] < 0) {// 无效
                win.setLeftRear("invalid");
            } else if ((int) numU8[0] >= 0 && (int) numU8[0] <= 100) {// 正常
                String valid = (int) numU8[0] + "";
                win.setLeftRear(valid);
            }
        }
        // **********右后车窗************
        if (5 == key) {
            if ((int) numU8[0] > 100 || (int) numU8[0] < 0) {// 无效
                win.setRightRear("invalid");
            } else if ((int) numU8[0] >= 0 && (int) numU8[0] <= 100) {// 正常
                String valid = (int) numU8[0] + "";
                win.setRightRear(valid);
            }
        }
        // **********主驾车窗************
        if (6 == key) {
            if ((int) numU8[0] > 100 || (int) numU8[0] < 0) {// 无效
                win.setMainDriver("invalid");
            } else if ((int) numU8[0] >= 0 && (int) numU8[0] <= 100) {// 正常
                String valid = (int) numU8[0] + "";
                win.setMainDriver(valid);
            }
        }
        // **********附驾车窗************
        if (7 == key) {
            if ((int) numU8[0] > 100 || (int) numU8[0] < 0) {// 无效
                win.setDeputyDriver("invalid");
            } else if ((int) numU8[0] >= 0 && (int) numU8[0] <= 100) {// 正常
                String valid = (int) numU8[0] + "";
                win.setDeputyDriver(valid);
            }
        }
    }

    /**
     * @param requestMsg
     * @Title: inGetVehicleStatusRep
     * @Description: 处理TBox上传的工程数据
     */
    public byte[] checkUpEngineData(OTAMessage requestMsg) {
        return inUpEngineData(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param param
     * @param tboxSn
     * @return outData
     * @Title: inUpEngineData
     * @Description: 处理TBox上传的工程数据
     */
    private byte[] inUpEngineData(byte[] param, String tboxSn) {
        byte[] outData = new byte[] { OTAConstant.COMMON_RESULT_SUCCESS };
        if (param.length < OTAConstant.ENG_DATA_COMPRESS_TYPE_SIZE + OTAConstant.ENG_DATA_CONTENT_SIZE
                + OTAConstant.DATETIME_BYTES_SIZE + OTAConstant.OTA_VERSION_LENGTH) {
            logger.warn("TBox(SN:{}):上传的工程数据不完整.", tboxSn);
            outData[0] = OTAConstant.COMMON_RESULT_FAILED;
            return outData;
        } else {
            // 解析压缩模式
            int compressionMode = param[OTAConstant.BIG_DATA_COMPRESS_TYPE_OFFSET];
            if (compressionMode != 0 && compressionMode != 1) {
                logger.warn("TBox(SN:{}):上传的工程数据压缩模式错误 ，值为:{}", tboxSn, compressionMode);
                outData[0] = OTAConstant.COMMON_RESULT_FAILED;
                return outData;
            } else {
                // 解析工程数据内容
                byte[] engDataSize = new byte[OTAConstant.ENG_DATA_CONTENT_SIZE];
                System.arraycopy(param, OTAConstant.ENG_DATA_CONTENT_SIZE_OFFSET, engDataSize, 0,
                        OTAConstant.ENG_DATA_CONTENT_SIZE);
                int engDataContentLength = ByteUtil.getUnsignedInt(engDataSize);
                if (param.length != OTAConstant.ENG_DATA_COMPRESS_TYPE_SIZE + OTAConstant.ENG_DATA_CONTENT_SIZE
                        + OTAConstant.DATETIME_BYTES_SIZE + OTAConstant.OTA_VERSION_LENGTH + engDataContentLength) {
                    logger.warn("TBox(SN:{}):工程数据内容长度不一致！", tboxSn);
                    outData[0] = OTAConstant.COMMON_RESULT_FAILED;
                    return outData;
                } else {
                    byte[] engdataContent = new byte[engDataContentLength];
                    System.arraycopy(param, OTAConstant.ENG_DATA_CONTENT_OFFSET, engdataContent, 0,
                            engDataContentLength);
                    byte[] engdataCollectionTime = new byte[OTAConstant.DATETIME_BYTES_SIZE];
                    System.arraycopy(param, OTAConstant.ENG_DATA_CONTENT_OFFSET + engDataContentLength,
                            engdataCollectionTime, 0, OTAConstant.DATETIME_BYTES_SIZE);
                    String collectTime = ByteUtil.bytesToDataTime(engdataCollectionTime);
                    if (collectTime.compareTo(OperationConstant.WARNING_VALID_COLLECT_TIME) < 0) {
                        collectTime = DateFormatUtils.format(new Date(), DatePatternConstant.SECOND);
                    }
                    logger.debug("TBox(SN:{})：上传工程数据:{}，收集时间:{},压缩类型:{}", tboxSn, ByteUtil.byteToHex(engdataContent),
                            collectTime, compressionMode);
                    try {

                        // todo将工程数据传递给IT
                        kafkaService.transferENGData(tboxSn, DateUtil.stringToLong(collectTime, "yyyy-MM-dd HH:mm:ss"),
                                compressionMode, engdataContent);

                    } catch (Exception ex) {
                        logger.error("TBox(sn:{}) 转投RVM工程数据出错:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
                    }
                }
            }
        }
        return outData;
    }

    /**
     * @param requestMsg
     * @Title: checkDownLoadFile
     * @Description: 处理文件下载指令的回复
     */
    public void checkDownLoadFile(OTAMessage requestMsg) {
        inDownLoadFile(requestMsg.getParam(), requestMsg.getSerialNumber());
    }
    
    public boolean setTboxDownLoadFile(String sn, String downLoadFileInfo) {
        try {
            return redisAPI.setHash(RedisConstant.TBOX_DOWNLOAD_FILE_INFO, sn, downLoadFileInfo);
        } catch (Exception ex) {
            logger.error("Tbox(sn: {})插入-Redis对应Tbox文件下载记录失败！原因：{}", sn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return false;
        }
    }
    
    public String existTboxDownLoadFile(String sn) {
        try {
            return redisAPI.getHash(RedisConstant.TBOX_DOWNLOAD_FILE_INFO, sn);
        } catch (Exception ex) {
            logger.error("Tbox(sn: {})获取-Redis是否存在Tbox文件下载记录失败！原因：{}", sn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return null;
        }
    }

    /**
     * @param param
     * @param serialNumber
     * @Title: inDownLoadFile
     * @Description: 处理文件下载指令的回复
     */
    private void inDownLoadFile(byte[] param, String serialNumber) {
        try {
            int result = param[0];
            if (existTboxDownLoadFile(serialNumber) != null) {
                TboxFileLoadStatus updateInfo = JSONObject.parseObject(
                        existTboxDownLoadFile(serialNumber), TboxFileLoadStatus.class);
                updateInfo.setResult(Integer.toString(result));
                setTboxDownLoadFile(serialNumber, JSONObject.toJSONString(updateInfo));

                // 发送kfk消息通知IT
                TboxUpdateRvmResponse tboxUpdateRvmResponse = new TboxUpdateRvmResponse();
                tboxUpdateRvmResponse.setSn(serialNumber);
                tboxUpdateRvmResponse.setData(result);
                tboxUpdateRvmResponse.setSeqNo(updateInfo.getSeqNo());
                tboxUpdateRvmResponse.setEventTime(updateInfo.getEventTime());
                switch (result) {
                case 0:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.DOWNLOAD_FILE_SUCCESS.getCode());
                    tboxUpdateRvmResponse.setDescription(ResultStatus.DOWNLOAD_FILE_SUCCESS.getDescription());
                    break;
                case 1:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.DOWNLOAD_FILE_BEGIN_DOWNLOADING.getCode());
                    tboxUpdateRvmResponse.setDescription(ResultStatus.DOWNLOAD_FILE_BEGIN_DOWNLOADING.getDescription());
                    break;
                case 2:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.DOWNLOAD_FILE_DOWNLOADED.getCode());
                    tboxUpdateRvmResponse.setDescription(ResultStatus.DOWNLOAD_FILE_DOWNLOADED.getDescription());
                    break;
                case 3:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.DOWNLOAD_FILE_DOWNLOAD_FAILED.getCode());
                    tboxUpdateRvmResponse.setDescription(ResultStatus.DOWNLOAD_FILE_DOWNLOAD_FAILED.getDescription());
                    break;
                case 4:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.DOWNLOAD_FILE_CHECK_FAILED.getCode());
                    tboxUpdateRvmResponse.setDescription(ResultStatus.DOWNLOAD_FILE_CHECK_FAILED.getDescription());
                    break;
                case 5:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.DOWNLOAD_FILE_FAILED.getCode());
                    tboxUpdateRvmResponse.setDescription(ResultStatus.DOWNLOAD_FILE_FAILED.getDescription());
                    break;
                default:
                    break;
                }
                logger.debug("TBox(sn:{})本次文件下载指令回复为:{}", serialNumber, JSONObject.toJSONString(tboxUpdateRvmResponse));
                kafkaService.transferTboxFileLoadResponse(tboxUpdateRvmResponse,
                        KafkaMsgConstant.TOPIC_IT_DOWNLOAD_FILE_RESPONSE);
            }
        } catch (Exception ex) {
            logger.error("TBox(sn:{} )解析CMD_UP_DOWNLOAD_FILE出错，原因:{}", serialNumber,
                    ThrowableUtil.getErrorInfoFromThrowable(ex));
        }

    }

    /**
     * @param requestMsg
     * @Title: checkUpLoadFile
     * @Description: 处理文件上传指令的回复
     */
    public void checkUpLoadFile(OTAMessage requestMsg) {
        inUpLoadFile(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    public boolean setTboxUpLoadFile(String sn, String upLoadFileInfo) {
        try {
            return redisAPI.setHash(RedisConstant.TBOX_UPLOAD_FILE_INFO, sn, upLoadFileInfo);
        } catch (Exception ex) {
            logger.error("Tbox(sn: {})插入-Redis对应Tbox文件上传记录失败！原因：{}", sn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return false;
        }
    }
    
    public String existTboxUpLoadFile(String sn) {
        try {
            return redisAPI.getHash(RedisConstant.TBOX_UPLOAD_FILE_INFO, sn);
        } catch (Exception ex) {
            logger.error("Tbox(sn: {})获取-Redis是否存在Tbox文件上传记录失败！原因：{}", sn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return null;
        }
    }
    
    /**
     * @param param
     * @param serialNumber
     * @Title: inUpLoadFile
     * @Description: 处理文件上传指令的回复
     */
    private void inUpLoadFile(byte[] param, String serialNumber) {
        try {
            int result = param[0];
            if (existTboxUpLoadFile(serialNumber) != null) {

                TboxFileLoadStatus updateInfo = JSONObject
                        .parseObject(existTboxUpLoadFile(serialNumber), TboxFileLoadStatus.class);
                updateInfo.setResult(Integer.toString(result));
                setTboxUpLoadFile(serialNumber, JSONObject.toJSONString(updateInfo));
                // 发送kfk消息通知IT
                TboxUpdateRvmResponse tboxUpdateRvmResponse = new TboxUpdateRvmResponse();
                tboxUpdateRvmResponse.setSn(serialNumber);
                tboxUpdateRvmResponse.setData(result);
                tboxUpdateRvmResponse.setSeqNo(updateInfo.getSeqNo());
                tboxUpdateRvmResponse.setEventTime(updateInfo.getEventTime());
                switch (result) {
                case 0:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.UPLOAD_FILE_SUCCESS.getCode());
                    tboxUpdateRvmResponse.setDescription(ResultStatus.UPLOAD_FILE_SUCCESS.getDescription());
                    break;
                case 1:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.UPLOAD_FILE_UPLOADING.getCode());
                    tboxUpdateRvmResponse.setDescription(ResultStatus.UPLOAD_FILE_UPLOADING.getDescription());
                    break;
                case 2:
                    tboxUpdateRvmResponse.setStatus(ResultStatus.UPLOAD_FILE_FAILED.getCode());
                    tboxUpdateRvmResponse.setDescription(ResultStatus.UPLOAD_FILE_FAILED.getDescription());
                    break;
                default:
                    break;
                }
                logger.debug("TBox(sn:{})本次文件上传指令回复为:{}", serialNumber, JSONObject.toJSONString(tboxUpdateRvmResponse));
                kafkaService.transferTboxFileLoadResponse(tboxUpdateRvmResponse,
                        KafkaMsgConstant.TOPIC_IT_UPLOAD_FILE_RESPONSE);
            }
        } catch (Exception ex) {
            logger.error("TBox(sn:{})解析CMD_UP_UPLOAD_FILE出错，原因:{}", serialNumber,
                    ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }

    /**
     * @param requestMsg
     * @return
     * @Title: checkEarlyyWarning
     * @Description: 预警信息指令处理
     */
    public byte[] checkEarlyWarning(OTAMessage requestMsg) {
        return inEarlyWarning(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @param param
     * @param serialNumber
     * @return outData
     * @Title: inEarlyWarning
     * @Description: 预警信息指令处理
     */
    private byte[] inEarlyWarning(byte[] param, String serialNumber) {
        logger.info("TBox(SN:{}):解密后报文为:{}", serialNumber, ByteUtil.byteToHex(param));
        byte[] outData = { OTAConstant.COMMON_RESULT_SUCCESS };
        EarlyWarningInfo earlyWarningInfo = new EarlyWarningInfo();
        earlyWarningInfo.setSn(serialNumber);
        earlyWarningInfo.setCommand(KafkaOtaDataCommand.KAFKA_PARAM_EARLY_WARNING);
        try {
            if (param.length != OTAConstant.OTA_EARLYWARNING_MSG_OFFSET) {
                logger.warn("TBox(SN:{}):危险行为预警信息长度不一致,报文丢弃!", serialNumber);
                outData[0] = OTAConstant.COMMON_RESULT_FAILED;
                return outData;
            } else {
                ItRedisInfo itTboxInfo = dataProcessing.getITTboxInfo(serialNumber);
                String vin;
                if (itTboxInfo == null) {
                    logger.warn("TBox(SN:{}): 不存在该车辆的信息", serialNumber);
                    vin = "";
                } else {
                    vin = itTboxInfo.getVin();
                    if (vin == null) {
                        logger.warn("TBox(SN:{}): 从Redis获取VIN号异常", serialNumber);
                        vin = "";
                    }
                }
                earlyWarningInfo.setVin(vin);

                // WarningType
                int warningType = param[0];
                earlyWarningInfo.setWarningType(warningType);

                // 开始时间
                byte[] _startTime = new byte[OTAConstant.DATETIME_BYTES_SIZE];
                System.arraycopy(param, 1, _startTime, 0, OTAConstant.DATETIME_BYTES_SIZE);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(ByteUtil.bytesToDataTime(_startTime)));
                earlyWarningInfo.setStartTime(calendar.getTimeInMillis());

                // 结束时间
                byte[] _endTime = new byte[OTAConstant.DATETIME_BYTES_SIZE];
                System.arraycopy(param, 8, _endTime, 0, OTAConstant.DATETIME_BYTES_SIZE);
                calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(ByteUtil.bytesToDataTime(_endTime)));
                earlyWarningInfo.setEndTime(calendar.getTimeInMillis());

                // 位置状态
                byte[] _posStatus = new byte[OTAConstant.OTA_EARLYWARNING_POS_STATUS_OFFSET];
                System.arraycopy(param, 15, _posStatus, 0, OTAConstant.OTA_EARLYWARNING_POS_STATUS_OFFSET);
                String posStatus = "";
                // 判断位置有效性，0位: 0 有效 1无效
                if ((_posStatus[0] & 0x01) == 0x01) {
                    posStatus = posStatus + 1 + ",";// 无效
                } else {
                    posStatus = posStatus + 0 + ",";// 有效
                }
                // 判断南北纬，1位: 0 北纬 1 南纬
                if ((_posStatus[0] & 0x02) == 0x02) {
                    posStatus = posStatus + 1 + ",";// 南纬
                } else {
                    posStatus = posStatus + 0 + ",";// 北纬
                }
                // 判断东西经，2位: 0 东经 1 西经
                if ((_posStatus[0] & 0x04) == 0x04) {
                    posStatus = posStatus + 1;// 西经
                } else {
                    posStatus = posStatus + 0;// 东经
                }
                earlyWarningInfo.setPosStatus(posStatus);

                // 纬度
                byte[] _latitude = new byte[OTAConstant.OTA_EARLYWARNING_LATITUDE_OFFSET];
                System.arraycopy(param, 16, _latitude, 0, OTAConstant.OTA_EARLYWARNING_LATITUDE_OFFSET);
                earlyWarningInfo.setLatitude(ByteUtil.getUnsignedLong(_latitude));

                // 经度
                byte[] _longitude = new byte[OTAConstant.OTA_EARLYWARNING_LONGITUDE_OFFSET];
                System.arraycopy(param, 20, _longitude, 0, OTAConstant.OTA_EARLYWARNING_LONGITUDE_OFFSET);
                earlyWarningInfo.setLongitude(ByteUtil.getUnsignedLong(_longitude));

                // 车速
                byte[] _vehicleSpeed = new byte[OTAConstant.OTA_EARLYWARNING_VEHICLE_SPEED_OFFSET];
                System.arraycopy(param, 24, _vehicleSpeed, 0, OTAConstant.OTA_EARLYWARNING_VEHICLE_SPEED_OFFSET);
                earlyWarningInfo.setSpeed(ByteUtil.getUnsignedInt(_vehicleSpeed));

                earlyWarningInfo.setGatewayTimeIn(gatewayTimeIn);
                earlyWarningInfo.setGatewayTimeOut(System.currentTimeMillis());

                logger.debug("TBox(SN:{})本次预警信息指令回复为:{}", serialNumber, JSONObject.toJSONString(earlyWarningInfo));
                kafkaService.transferEarlyWarningResponse(earlyWarningInfo, KafkaMsgConstant.TOPIC_OTA_DATA);

                return outData;

            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})解析CMD_UP_EARLY_WARNING出错，原因:{}", serialNumber,
                    ThrowableUtil.getErrorInfoFromThrowable(e));
            ;
            outData[0] = OTAConstant.COMMON_RESULT_FAILED;
            return outData;
        }
    }

    /**
     * 智能家居控制
     *
     * @param requestMsg
     * @return
     */
    public void checkHomeCtrl(OTAMessage requestMsg) {
        inHomeCtrl(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    private void inHomeCtrl(byte[] param, String serialNumber) {
        // 获取参数长度
        byte[] _paramSize = new byte[2];
        List<HomeCtrlResultMo> ctrlResultList = new ArrayList<>();
        try {
            System.arraycopy(param, 0, _paramSize, 0, OTAConstant.HOME_CTRL_UP_PARAMSIZE_LENGTH);
            int paramSize = ByteUtil.getUnsignedInt(_paramSize);
            if (paramSize > 0) {
                int index = 2;
                byte[] _id = new byte[2];
                byte[] _result = new byte[2];
                while (index < paramSize) {
                    System.arraycopy(param, index, _id, 0, OTAConstant.HOME_CTRL_UP_ID_OFFSET);
                    index += 2;
                    System.arraycopy(param, index, _result, 0, OTAConstant.HOME_CTRL_UP_RESULT_OFFSET);
                    index += 2;
                    HomeCtrlResultMo result = new HomeCtrlResultMo(ByteUtil.getUnsignedInt(_id),
                            ByteUtil.getUnsignedInt(_result));
                    ctrlResultList.add(result);
                }
            }

            if (!ctrlResultList.isEmpty()) {
                Date curTime = new Date();
                String eventTime = DateFormatUtils.format(curTime, "yyyy-MM-dd HH:mm:ss");
                logger.info("TBox(sn:{})智能家居控制上行回复时间:{}, 结果:{}", serialNumber, eventTime,
                        JSONObject.toJSONString(ctrlResultList));
                // ToDo
                // 给网关自身的kafka，发送房车家居远控上行消息。
                kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_UP_HOME_CTRL,
                        serialNumber + "_" + JSONObject.toJSONString(ctrlResultList) + "_" + eventTime, serialNumber);
            }
        } catch (Exception ex) {
            logger.error("TBox(sn:{})解析CMD_UP_HOME_CTRL指令失败，原因:{} ", serialNumber,
                    ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }

    /**
     * @return void
     * @Description 远程组合控制回复报文上行
     * @Date 2019/1/29 23:16
     * @Param [requestMsg]
     **/
    public void checkRmtGroupControl(OTAMessage requestMsg) {
        procRmtGroupControl(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @return void
     * @Description 处理远程组合控制回复报文
     * @Date 2019/1/29 23:18
     * @Param [param, serialNumber]
     */
    private void procRmtGroupControl(byte[] param, String serialNumber) {
        logger.debug("TBox(SN:{})远程组合控制回复报文解密后为:{}", serialNumber, ByteUtil.byteToHex(param));
        try {
            // logger.info("param length:{}", param.length);
            if (param.length == OTAConstant.RMT_GROUP_RESP_OFFSET) {
                RmtGroupRespInfo rmtGroupRespInfo = new RmtGroupRespInfo();
                // result
                byte[] _result = new byte[2];
                System.arraycopy(param, 0, _result, 0, 2);
                List<RmtGroupResultInfo> rmtGroupResultInfos = new ArrayList<>();
                // bit 0
                if ((_result[1] & 0x01) == 0x01) {
                    rmtGroupResultInfos.add(new RmtGroupResultInfo(0, 1));
                } else {
                    rmtGroupResultInfos.add(new RmtGroupResultInfo(0, 0));
                }
                // bit 1
                if ((_result[1] & 0x02) == 0x02) {
                    rmtGroupResultInfos.add(new RmtGroupResultInfo(1, 1));
                } else {
                    rmtGroupResultInfos.add(new RmtGroupResultInfo(1, 0));
                }
                // bit 2
                if ((_result[1] & 0x04) == 0x04) {
                    rmtGroupResultInfos.add(new RmtGroupResultInfo(2, 1));
                } else {
                    rmtGroupResultInfos.add(new RmtGroupResultInfo(2, 0));
                }
                // bit 3
                if ((_result[1] & 0x08) == 0x08) {
                    rmtGroupResultInfos.add(new RmtGroupResultInfo(3, 1));
                } else {
                    rmtGroupResultInfos.add(new RmtGroupResultInfo(3, 0));
                }
                // bit 4
                if ((_result[1] & 0x10) == 0x10) {
                    rmtGroupResultInfos.add(new RmtGroupResultInfo(4, 1));
                } else {
                    rmtGroupResultInfos.add(new RmtGroupResultInfo(4, 0));
                }
                // bit 5
                if ((_result[1] & 0x20) == 0x20) {
                    rmtGroupResultInfos.add(new RmtGroupResultInfo(5, 1));
                } else {
                    rmtGroupResultInfos.add(new RmtGroupResultInfo(5, 0));
                }
                // bit 6
                if ((_result[1] & 0x40) == 0x40) {
                    rmtGroupResultInfos.add(new RmtGroupResultInfo(6, 1));
                } else {
                    rmtGroupResultInfos.add(new RmtGroupResultInfo(6, 0));
                }
                if (OtaVersionFeature.RMTGROUP_ENGINE_AND_TRUNK.isSupported(serialNumber)) {
                    // OTA版本为0.38之后才支持此功能
                    // bit 7
                    if ((_result[1] & 0x80) == 0x80) {
                        rmtGroupResultInfos.add(new RmtGroupResultInfo(7, 1));
                    } else {
                        rmtGroupResultInfos.add(new RmtGroupResultInfo(7, 0));
                    }
                    // bit 8
                    if ((_result[0] & 0x01) == 0x01) {
                        rmtGroupResultInfos.add(new RmtGroupResultInfo(8, 1));
                    } else {
                        rmtGroupResultInfos.add(new RmtGroupResultInfo(8, 0));
                    }
                }
                rmtGroupRespInfo.setResult(rmtGroupResultInfos);

                // errorCode
                byte[] _errorCode = new byte[2];
                System.arraycopy(param, 2, _errorCode, 0, 2);
                rmtGroupRespInfo.setErrorCode(ByteUtil.byteToHex(_errorCode));

                String respValue = JSONObject.toJSONString(rmtGroupRespInfo);
                logger.info("TBox(SN:{})回复远程组合控制消息:{}", serialNumber, respValue);

                // dateTime
                String dateTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_SELF_RM_GROUP_UP,
                        serialNumber + "_" + respValue + "_" + dateTime, serialNumber);
                
                String homeCtrlReq = redisAPI.getValue(RedisConstant.CAR_HOME_CTRL_REQ);
                
                RmtGroupRequestInfo rmtGroupRequestInfo = JSONObject.parseObject(homeCtrlReq, RmtGroupRequestInfo.class);
                
                AppJsonResult appJsonResult = new AppJsonResult(ResultStatus.SUCCESS, rmtGroupRespInfo);
                //封转组合远控的结果返回给RVM
                RemoteCtrlItResponse rmtGroupItResp = new RemoteCtrlItResponse();
                rmtGroupItResp.setSn(serialNumber);
                rmtGroupItResp.setStatus(appJsonResult.getStatus());
                rmtGroupItResp.setData(rmtGroupRespInfo.getResult());
                rmtGroupItResp.setDescription(appJsonResult.getDescription());
                rmtGroupItResp.setSeqNo(rmtGroupRequestInfo.getSeqNo());
                rmtGroupItResp.setEventTime(rmtGroupRequestInfo.getEventTime());
                logger.debug("TBox(SN:{})组合远控返回结果为:{}", serialNumber, JSONObject.toJSONString(rmtGroupItResp));
                kafkaService.sndMesToITForTemplate(KafkaMsgConstant.TOPIC_IT_RM_GROUP_RESP, rmtGroupItResp, serialNumber);
                
            } else {
                logger.warn("TBox(SN:{})远程组合控制回复报文长度错误, 报文丢弃!", serialNumber);
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})解析CMD_UP_REMOTECTRL因发生异常失败, 异常原因:{}", serialNumber,
                    ThrowableUtil.getErrorInfoFromThrowable(e));
        }
    }

    /**
     * @Description 远程控制指令（Ext）报文解析
     * @Data 2019年3月6日上午10:33:26
     * @param requestMsg
     */
    public void checkRmtGroupControlExt(OTAMessage requestMsg) {
        procRmtGroupControlExt(requestMsg.getParam(), requestMsg.getSerialNumber());
    }

    /**
     * @Description 远程控制指令（Ext）报文具体解析类
     * @Data 2019年3月6日上午10:34:34
     * @param param
     * @param serialNumber
     */
    private void procRmtGroupControlExt(byte[] param, String serialNumber) {
        try {
            RmtGroupRespInfo rmtGroupRespInfo = new RmtGroupRespInfo();
            if (param.length > 2) {
                byte[] _paramSize = new byte[OTAConstant.RMT_GROUP_EXT_PARAMSIZE_OFFSET];
                System.arraycopy(param, 0, _paramSize, 0, OTAConstant.RMT_GROUP_EXT_PARAMSIZE_OFFSET);
                int extParamSize = ByteUtil.getUnsignedInt(_paramSize);
                List<RmtGroupResultInfo> rmtGroupResultInfos = new ArrayList<>();
                if (param.length == OTAConstant.RMT_GROUP_EXT_PARAMSIZE_OFFSET + extParamSize
                        && extParamSize % 3 == 0) {
                    byte[] _cmd = new byte[2];
                    byte[] _result = new byte[1];
                    int index = 2;
                    int resultInfo = 0;
                    for (int i = 0; i < extParamSize / 3; i++) {
                        // command
                        System.arraycopy(param, index, _cmd, 0, 2);
                        // result
                        System.arraycopy(param, index + 2, _result, 0, 1);
                        resultInfo = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_result[0]));
                        rmtGroupResultInfos.add(new RmtGroupResultInfo(ByteUtil.getUnsignedInt(_cmd), resultInfo));
                        index = index + 3;
                    }

                    rmtGroupRespInfo.setResult(rmtGroupResultInfos);

                    String respValue = JSONObject.toJSONString(rmtGroupRespInfo);
                    logger.info("TBox(SN:{})回复远程组合控制消息:{}", serialNumber, respValue);
                    // dateTime
                    String dateTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                    kafkaService.sndMesForTemplate(KafkaMsgConstant.TOPIC_SELF_RM_GROUP_UP,
                            serialNumber + "_" + respValue + "_" + dateTime, serialNumber);
                } else {
                    logger.warn("TBox(SN:{})解析CMD_UP_REMOTECTRL_EXT时上传的列表（远程车控指令+结果）的长度错误，原始报文丢弃:{}", serialNumber,
                            ByteUtil.byteToHex(param));
                }
            } else {
                logger.warn("TBox(SN:{})解析CMD_UP_REMOTECTRL_EXT的指令参数不符合要求或为空,原始报文丢弃:{}", serialNumber,
                        ByteUtil.byteToHex(param));
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{})解析CMD_UP_REMOTECTRL_EXT 因发生异常失败, 异常原因:{}", serialNumber,
                    ThrowableUtil.getErrorInfoFromThrowable(e));
        }
    }
}
