package com.maxus.tsp.gateway.server;

import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.MinioClientUtils;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.FOTAConstant;
import com.maxus.tsp.gateway.common.constant.KafkaOtaDataCommand;
import com.maxus.tsp.gateway.common.constant.OTACommand;
import com.maxus.tsp.gateway.common.constant.OTAConstant;
import com.maxus.tsp.gateway.common.constant.OTAEncrptMode;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.constant.OtaVersionFeature;
import com.maxus.tsp.gateway.common.ota.OTAMessage;
import com.maxus.tsp.gateway.common.ota.TimeLogUtil;
import com.maxus.tsp.gateway.conf.MqttProperties;
import com.maxus.tsp.gateway.ota.TspServiceProc;
import com.maxus.tsp.gateway.ota.process.ProcAgreeUpgrade;
import com.maxus.tsp.gateway.ota.process.ProcBlueTooth;
import com.maxus.tsp.gateway.ota.process.ProcGB6B;
import com.maxus.tsp.gateway.ota.process.ProcGetEcu;
import com.maxus.tsp.gateway.ota.process.ProcProgressReport;
import com.maxus.tsp.gateway.ota.process.ProcReportCan;
import com.maxus.tsp.gateway.ota.process.ProcReportVersion;
import com.maxus.tsp.gateway.ota.process.ProcVersionQuery;
import com.maxus.tsp.gateway.ota.process.ProcVersionUpgrade;
import com.maxus.tsp.gateway.service.DataProcessing;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.service.MqttService;
import com.maxus.tsp.gateway.service.TboxService;
import com.maxus.tsp.platform.service.model.vo.TboxVo;

import io.netty.util.internal.StringUtil;

@Component
@EnableConfigurationProperties(MqttProperties.class)
public class OTAResolveServerHandler {

    private static Logger logger = LogManager.getLogger(OTAResolveServerHandler.class);

    @Autowired
    private ProcReportCan procReportCan;
    
    // tbox序列号,登陆成功后，使用该序列号处理后续的业务报文
    private String tboxSn = null;
    // 用于表示国标是否已经登录
    private boolean isGBLogined = false;
    // 用于表示国标是否已经登录
    private boolean isGB6BLogined = false;
    
    @Autowired
    private DataProcessing dataProcessing;
    
    @Autowired
    private TspServiceProc tspServiceProc;
    
    @Autowired
    private RedisAPI redisAPI;
    // kafka服务
    private KafkaService kafkaService;
    // tbox服务
    private TboxService tboxService;
    //网关接收报文时间
    private long receivetime;
    
    @Autowired
    private MinioClientUtils minioClientUtils;

    @Autowired
	private MqttService mqttService;
    
    @Autowired
    private OTAMessage otaMessage;
    
    //@Autowired
    //private OTAMessage requestMsg;

	public void resolve(Message<?> message) {
		try {
			String logName ="OTAResolveServerHandler.resolve: ";
			TimeLogUtil.log(logName+"start");
			
			MessageHeaders headers = message.getHeaders();
			StringBuffer headerStr = new StringBuffer();
			headers.forEach((key, value) -> headerStr.append("[" + key + " : " + value + "] "));
			String topic = (String) headers.get("mqtt_receivedTopic");

//		byte[] msgOta = Asn1Util.decodeSeq((byte[]) message.getPayload());
			byte[] msgOta = (byte[]) message.getPayload();
			logger.info("开始处理 message header: {} \nTopic: [{}], payload length: [{}],payload: [{}]",
					headerStr.toString(), topic, msgOta.length, ByteUtil.byteToHex(msgOta));

			long receivetime = System.currentTimeMillis(); // 初始化收到报文时间
			byte[] procOTAret = null;
			OTAMessage requestMsg = new OTAMessage(msgOta);
			
			TimeLogUtil.log(logName+"new OTAMessage");
			
			if (requestMsg.isAnalysed()) {
				String currentTboxSN = requestMsg.getSerialNumber();
				// 根据加密方式进行进一步操作
				switch (requestMsg.getEncryptType()) {
				case RSA:
					// 登录报文，需要获取Tbox公钥进行解密
					// 获得tbox的锁定状态
					String pkey = "";
					TboxVo tboxVo = dataProcessing.getTboxInfo(currentTboxSN);
					if (tboxVo != null) {
						if (tboxVo.getStatus() == OperationConstant.REGISTER_TBOXSTATUS_INITIAL) {
							logger.info("TBox(SN:{})处于正在注册中", currentTboxSN);
							pkey = getTboxPubKey(currentTboxSN);// 取出redis临时信息中的公钥
						} else if (tboxVo.getStatus() == OperationConstant.REGISTER_COMPLETE_SUCCESS) {
							logger.info("TBox(SN:{})处于正常登录中", currentTboxSN);
							pkey = tboxVo.getPkey();// 取出正式储存的公钥
						} else {
							logger.warn("TBox(SN: {}处于其他状态 {}", currentTboxSN, tboxVo.getStatus());
							if (tboxVo.getStatus() == OperationConstant.REGISTER_GATEWAY_SUCCESS) {
								logger.info("TBox(SN: {})曾经注册未通过RVM检验，正在重新注册登陆阶段中", currentTboxSN);
								pkey = getTboxPubKey(currentTboxSN);
							}
						}
						if (pkey != null && !pkey.isEmpty()) {
							byte[] decryptBytesRSA = requestMsg.DecryptBytesRSA(pkey, currentTboxSN);
							if (decryptBytesRSA.length > 0) {
								requestMsg.analysisCommand(decryptBytesRSA);// 明文解析
							} else {
								requestMsg.analysisCommand(null);
							}
						} else {
							logger.warn("TBox(SN: {}): RSA解密失败    原因：Redis数据库不存在对应公钥", currentTboxSN);
							requestMsg.analysisCommand(null);
						}
					} else {
						logger.warn("TBox(SN: {}): RSA解密失败    原因：Redis数据库不存在对应TBox信息", currentTboxSN);
						requestMsg.analysisCommand(null);
					}
					break;
				case AES:// AES解密
					if (this.tboxSn != null) {
						String currentAESKey = redisAPI.getHash(RedisConstant.SECURITY_KEY, this.tboxSn);// 获取AES密钥
						byte[] decryptBytesAES = requestMsg.DecryptBytesAES(currentAESKey, tboxSn);
						requestMsg.analysisCommand(decryptBytesAES);
					} else {
						requestMsg.analysisCommand(null);
					}
					break;
				case RC4:// NOT IMPLEMENTS
					// break;
				case PKI:// PKI/CA解密
					TboxVo tbox = dataProcessing.getTboxInfo(currentTboxSN);
					if (tbox != null) {
						logger.info("TBox(SN:{})当前状态:{}", currentTboxSN, tbox.getStatus());
						// 获取服务器证书
						String certification = null;
						try {
							certification = minioClientUtils.getBase64CertData(currentTboxSN);
						} catch (Exception e) {
							e.printStackTrace();
						}
						logger.debug("TBox(SN:{})从服务器获取证书为：{}", currentTboxSN, certification);
						if (certification != null && !certification.isEmpty()) {
							// 解密 验签
							byte[] decryptBytesPKICA = requestMsg.DecryptBytesPKICA(certification, currentTboxSN);
							if (decryptBytesPKICA.length > 0) {
								requestMsg.analysisCommand(decryptBytesPKICA);// PKI/CA解密、验签成功，开始明文解析
							} else {
								requestMsg.analysisCommand(null);
							}
						} else {
							logger.warn("TBox(SN: {}): PKI/CA解密失败    原因：证书服务器中未获取到对应的TBox证书", currentTboxSN);
							requestMsg.analysisCommand(null);
						}
					} else {
						logger.warn("TBox(SN: {}): PKI/CA解密失败    原因：Redis数据库不存在sn对应的TBox信息", currentTboxSN);
						requestMsg.analysisCommand(null);
					}
					break;
				default:
					requestMsg.analysisCommand(null);
					break;
				}
				TimeLogUtil.log(logName+"analysisCommand 完成");
				if (requestMsg.isAnalysed()) {
					logger.debug("TBox(SN:{})当前报文通用部分分析成功, 开始进行报文内容解析!", currentTboxSN);
					procOTAret = procOTA(requestMsg); // 如果分析成功，才进行回复
				} else {
					logger.warn("TBox(SN:{})当前报文通用部分分析失败, 不进行后续报文内容解析!", currentTboxSN);
				}
				TimeLogUtil.log(logName+"procOTA 结束");
			}
			// 如果是null就丢弃
			if (procOTAret != null) {
				TimeLogUtil.log(logName+"response 开始");
				String replyTopic = mqttService.getSendTopics()[0].replaceAll("#", requestMsg.getSerialNumber());
				String msgReply = "This is a response: " + StringUtil.toHexString(procOTAret);
				logger.info("reply topic::: " + replyTopic + ", reply message: " + msgReply);
				logger.info("响应报文：" + StringUtil.toHexString(procOTAret));
				TimeLogUtil.log(logName+"response 结束");
				
				mqttService.sendWithRetry(procOTAret, replyTopic);
				TimeLogUtil.log(logName+"mqtt send back");
				long replyTime = System.currentTimeMillis() - receivetime;
				if (logger.isInfoEnabled()) {
					logger.info("TBox(SN: {}): TSP回复报文：{}  本次响应时间：{} 毫秒", requestMsg.getSerialNumber(),
							ByteUtil.byteToHex(procOTAret), replyTime);
				}
				if (logger.isWarnEnabled()) {
					if (replyTime > OperationConstant.OTA_REPLAY_TIME_MAX_FLAG) {
						// 如果回复超过5s，打印错误
						logger.warn("TBox(SN: {}): 回复指令:{}超过5秒", requestMsg.getSerialNumber(),
								OTACommand.getByCode(requestMsg.getCommand()));
					}
				}
			} else {
				if (logger.isWarnEnabled()) {
					logger.warn("本次报文不需要响应. 原始报文为：{}", ByteUtil.byteToHex(msgOta));
					if (System.currentTimeMillis() - receivetime > OperationConstant.OTA_REPLAY_TIME_MAX_FLAG) {
						// 如果回复超过5s，打印错误
						logger.warn("本次指令不需要响应，解析指令超过5秒");
					}
				}
			}
			TimeLogUtil.log(logName+"结束");
			if(TimeLogUtil.enable) {
				logger.info(TimeLogUtil.printLog());
			}
		}catch (Exception e) {
			logger.error("处理ota报文异常",e);
		} 
	}

    private void tboxLoginInfoRemove(String serialNumber) {
    	redisAPI.removeHash(RedisConstant.SECURITY_KEY, serialNumber);
        dataProcessing.onlineTboxDelete(serialNumber);
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
            }
        } catch (Exception ex) {
            logger.error("Redis connection error, TBox({}) can't do getTboxPubKey:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
        return null;
    }

    /**
     * @Title: procOTA
     * @Description: 指令解析
     * @param: @param requestMsg,ctx
     * @param: @return
     * @return: retHex
     */
    private byte[] procOTA(OTAMessage requestMsg) {
    	String logName ="OTAResolveServerHandler.resolve -> procOTA: ";
    	TimeLogUtil.log(logName+"start");
        boolean fotaMessage = false;
        byte[] fotaRespParam = null;
        byte[] outData = ByteUtil.short2Byte((short) 1);// outData决定是否回包
        String tboxsn = requestMsg.getSerialNumber();
        short command = requestMsg.getCommand();
        OTACommand commandByCode = OTACommand.getByCode(command);
        logger.info("TBox(SN: {}): 当前TBox指令码：{}", tboxsn, commandByCode);
        // 没有登录，同时指令不是登录指令，強制断开连接
		boolean isTboxOnline = redisAPI.hasKey(RedisConstant.ONLINE_TBOX, tboxsn);
		logger.info("redis tbox(" + tboxsn + ") online? " + isTboxOnline);
		TimeLogUtil.log(logName + "redis判断sn online");
		try {
			logger.info("OTACommand == " + commandByCode.getMessage());
		}catch(Exception e){
			logger.error("TBox(SN: {}): command:{}  重新获取command:{}", tboxsn,command,requestMsg.getCommand() );
			return null;
		}
		
		if (!isTboxOnline && (commandByCode != OTACommand.CMD_UP_LOGIN) && (OTACommand.CMD_UP_REGISTER != commandByCode)
				&& (OTACommand.CMD_UP_REGISTER_CERT != commandByCode)) {
			logger.info("TBox(SN: {}): 当前TBox需要先登录或注册", tboxsn);
			return null;
		}
        switch (commandByCode) {
            case CMD_UP_LOGIN: // 登录报文
                if (!(requestMsg.getEncryptType().equals(OTAEncrptMode.RSA) || requestMsg.getEncryptType().equals(OTAEncrptMode.PKI))) {
                    logger.warn("TBox({})登录报文没有使用密文!", tboxsn);
                }
                outData = tspServiceProc.checkDataLogin(requestMsg, tboxSn);
                if (outData != null && outData[0] == OTAConstant.COMMON_RESULT_SUCCESS) {
                    tboxSn = tboxsn;
                } else {
                    tboxSn = null;
                }
                break;
            case CMD_UP_HEARTBEAT: // 心跳
                outData = ByteUtil.CreateDateTimeBytes(Calendar.getInstance());
                break;
            case CMD_UP_LOGOUT: // 登出
                // 记录登出时间
                kafkaService.transferOTAData(tboxSn, KafkaOtaDataCommand.KAFKA_PARAM_LOGOUT,
                        ByteUtil.bytesToDataTime(requestMsg.getParam()), receivetime);
                dataProcessing.logoutTboxUpdate(tboxSn);
                if (tboxSn != null) {
                    tboxLoginInfoRemove(tboxSn);
                    logger.info("TBox(SN: {}): 当前TBox10秒后与TSP断开连接： ", tboxSn);
                    tboxSn = null;
                }
                outData = new byte[]{OTAConstant.COMMON_RESULT_SUCCESS};
                break;
            case CMD_UP_REGISTER: // 注册报文
                outData = tspServiceProc.checkDataRegisterUp(requestMsg);
                break;
            case CMD_UP_REGISTER_CERT:   //OTA升级 注册报文
                outData = tspServiceProc.checkDataRegisterUpCert(requestMsg);
                break;
            case CMD_UP_UPDATE_PUBKEY:// 更新TSP公钥的响应报文
            	tspServiceProc.checkDataUpdatePubKey(requestMsg);
                return null;
            case CMD_UP_QCPASSED: // 验证通过注册
                outData = tspServiceProc.checkDataQCPassedUp(requestMsg);
                break;
            case CMD_UP_REMOTECTRL: // 远程控制车辆回复
                logger.debug("TBox(SN:{})开始判断远控类型:组合/单一", requestMsg.getSerialNumber());
                if (existRmtGroupTopic(requestMsg.getSerialNumber(), OperationConstant.RM_GROUP_CTRL)) {
                    logger.info("TBox(SN:{})为组合远控指令上行回复!", requestMsg.getSerialNumber());
                    tspServiceProc.checkRmtGroupControl(requestMsg);
                } else {
                    logger.info("TBox(SN:{})为单个指令上行回复", requestMsg.getSerialNumber());
                    tspServiceProc.checkDataRemoteControl(requestMsg);
                }
                // 不需要再下发响应报文
                outData = null;
                break;
            case CMD_UP_REPORTCAN: // 上报车况数据
            	TimeLogUtil.log(logName+"上报车况开始");
                outData = procReportCan.checkDataReportCan(requestMsg);
                TimeLogUtil.log(logName+"上报车况结束");
                break;
            case CMD_UP_REPORTPOS: // 上报批量车辆位置
                outData = tspServiceProc.checkDataReportPos(requestMsg);
                break;
            case CMD_UP_GETPOS: // 上报当前车辆实时位置
                outData = tspServiceProc.checkDataGetPos(requestMsg);
                break;
            case CMD_UP_REMOTE_UPDATE: // 远程升级Tbox回复
            	tspServiceProc.checkDataRemoteUpdate(requestMsg);
                // 不需要再下发响应报文
                outData = null;
                break;
            case CMD_UP_WARNING: // 上报报警信息
                outData = tspServiceProc.checkDataAlarm(requestMsg);
                break;
            case CMD_UP_FORWARD_4IVI: // 透传请求上行
                outData = tspServiceProc.checkDataForward(requestMsg);
                break;
            case CMD_UP_ADD_BT_KEY: { // 添加蓝牙钥匙结果
                ProcBlueTooth procBlueTooth = new ProcBlueTooth(kafkaService, tboxService);
                procBlueTooth.checkDataAddBTKey(requestMsg);
                outData = null;
            }
            break;
            case CMD_UP_DEL_BT_KEY: { // 删除蓝牙钥匙结果
                ProcBlueTooth procBlueTooth = new ProcBlueTooth(kafkaService, tboxService);
                procBlueTooth.checkDataDelBTKey(requestMsg);
                outData = null;
            }
            break;
            case CMD_UP_GET_BT_KEY: {// 获取蓝牙钥匙结果
                ProcBlueTooth procBlueTooth = new ProcBlueTooth(kafkaService, tboxService);
                procBlueTooth.checkDataGetBTKey(requestMsg);
                outData = null;
            }
            break;
            case CMD_UP_VERIFICATION_CODE: {// 蓝牙验证结果通知checkDataVerification
                ProcBlueTooth procBlueTooth = new ProcBlueTooth(kafkaService, tboxService);
                procBlueTooth.checkDataVerification(requestMsg);
                outData = null;
            }
            break;
            case CMD_UP_REALTIME_DATA: // 实时数据上报
                if (isGBLogined) {
                    outData = tspServiceProc.checkDataRealTime(requestMsg);
                } else {
                    logger.warn("TBox(SN:{})没有国标登录!", requestMsg.getSerialNumber());
                    outData = null;
                }
                break;
            case CMD_UP_GB_LOGIN: // 国标登录
                outData = tspServiceProc.checkDataGBloginlogout(requestMsg, commandByCode);
                if (outData != null && outData[0] == OTAConstant.COMMON_RESULT_SUCCESS) {
                    isGBLogined = true;
                    kafkaService.transferOTAData(tboxSn, KafkaOtaDataCommand.KAFKA_PARAM_GBLOGIN,
                            ByteUtil.bytesToDataTime(requestMsg.getParam()), receivetime);
                }
                break;
            case CMD_UP_GB_LOGOUT: // 国标登出
                outData = tspServiceProc.checkDataGBloginlogout(requestMsg, commandByCode);
                if (outData != null && outData[0] == OTAConstant.COMMON_RESULT_SUCCESS) {
                    isGBLogined = false;
                    kafkaService.transferOTAData(tboxSn, KafkaOtaDataCommand.KAFKA_PARAM_GBLOGOUT,
                            ByteUtil.bytesToDataTime(requestMsg.getParam()), receivetime);
                }
                break;
            case CMD_UP_FAULT: // 上传故障信息
                outData = tspServiceProc.checkDataFault(requestMsg);
                break;
            case CMD_UP_REMOTE_CONFIG:// 远程配置上行
            	tspServiceProc.checkDataRmtConfig(requestMsg);
                outData = null;
                break;
            case CMD_UP_BIG_DATA:// 上传大数据
                outData = tspServiceProc.checkDataBigData(requestMsg);
                break;
            case CMD_UP_GET_CONFIG:// 上行远程获取Tbox配置
            	tspServiceProc.checkGetRmtConfig(requestMsg);
                // outData = ByteUtil.CreateDateTimeBytes(Calendar.getInstance());
                outData = null;
                break;
            case CMD_UP_START_DIRECT_REPORT:// 开始往国家平台发数据 指令执行结果
            	tspServiceProc.checkDataStartDrtRep(requestMsg);
                outData = null;
                break;
            case CMD_UP_STOP_DIRECT_REPORT:// 停止往国家平台发数据 指令执行结果
            	tspServiceProc.checkDataStopDrtRep(requestMsg);
                outData = null;
                break;
            case CMD_UP_GET_VEHICLE_STATUS:// 获取车辆状态指令
            	tspServiceProc.checkGetVehicleStatus(requestMsg);
                outData = null;
                break;
            case CMD_UP_ENG_DATA:// 工程数据上传指令
                outData = tspServiceProc.checkUpEngineData(requestMsg);
                break;
            case CMD_UP_DOWNLOAD_FILE:// 文件下载指令反馈
            	tspServiceProc.checkDownLoadFile(requestMsg);
                outData = null;
                break;
            case CMD_UP_UPLOAD_FILE:// 文件上传指令反馈
            	tspServiceProc.checkUpLoadFile(requestMsg);
                outData = null;
                break;
            case CMD_UP_EARLY_WARNING:// 危险行为预警上报
                //判断当前sn对应的版本是否支持危险行为预警上报
                if (OtaVersionFeature.EARLYWARNING.isSupported(requestMsg.getSerialNumber())) {
                    outData = tspServiceProc.checkEarlyWarning(requestMsg);
                } else {
                    logger.warn("TBox(SN:{})当前版本不支持危险行为预警上报!", requestMsg.getSerialNumber());
                    outData = null;
                }
                break;
            case CMD_UP_HOME_CTRL:// 智能家居控制
            	tspServiceProc.checkHomeCtrl(requestMsg);
                outData = null;
                break;
            case CMD_UP_GB_EMISSION:// 国六B排放数据上报
                // 国六B未登录不对排放数据进行解析
                if (isGB6BLogined) {
                    ProcGB6B procGB6B = new ProcGB6B(kafkaService, tboxService, receivetime);
                    outData = procGB6B.checkGBEmission(requestMsg);
                } else {
                    if (logger.isWarnEnabled()) {
                        logger.warn("当前设备({})国标6B未登录，请先登录国标6B！", requestMsg.getSerialNumber());
                    }
                    outData = null;
                }
                break;
            case CMD_UP_GB_LOGIN_EMSN:// 国六B功能的登入
                ProcGB6B procGB6B = new ProcGB6B(kafkaService, tboxService, receivetime);
                outData = procGB6B.checkGBLoginEmission(requestMsg);
                if (outData != null && outData[0] == OTAConstant.COMMON_RESULT_SUCCESS) {
                    if (logger.isDebugEnabled()) {
                        logger.info("当前设备({})国标6B登陆成功！", requestMsg.getSerialNumber());
                    }
                    isGB6BLogined = true;// 国六B登入成功
                }
                break;
            case CMD_UP_GB_LOGOUT_EMSN:// 国六B功能的登出
                ProcGB6B procGB6B1 = new ProcGB6B(kafkaService, tboxService, receivetime);
                outData = procGB6B1.checkGBLogoutEmission(requestMsg);
                if (outData != null && outData[0] == OTAConstant.COMMON_RESULT_SUCCESS) {
                    logger.info("当前设备({})国标6B登出成功！", requestMsg.getSerialNumber());
                    isGB6BLogined = false;// 国六B登出成功
                }
                break;
            case CMD_UP_GET_ECU_LIST:// 获取ECU列表信息
                ProcGetEcu procGetEcu = new ProcGetEcu(kafkaService, tboxService);
                fotaRespParam = procGetEcu.checkGetEcuList(requestMsg);
                fotaMessage = true;
                break;
            case CMD_UP_OTA_QUERY_NOTIFY://收到版本查询通知的回复
                ProcVersionQuery procVersionQuery = new ProcVersionQuery(kafkaService, tboxService);
                procVersionQuery.checkVersionQuery(requestMsg);
                outData = null;
                break;
            case CMD_UP_UPGRADE_REQ://收到车主答复的应答
                ProcVersionUpgrade procVersionUpgrade = new ProcVersionUpgrade(kafkaService, tboxService);
                procVersionUpgrade.checkVersionUpgrade(requestMsg);
                outData = null;
                break;
//            case CMD_UP_UPGRADE_RESUME://上报继续升级结果
//                ProcUpgradeResume procUpgradeResume = new ProcUpgradeResume(kafkaService, tboxService);
//                procUpgradeResume.checkUpgradeResume(requestMsg);
//                outData = null;
//                break;
            case CMD_UP_REPORT_VERSION:// 请求版本更新
                ProcReportVersion procReportVersion = new ProcReportVersion(kafkaService, tboxService);
                fotaRespParam = procReportVersion.ReportVersionList(requestMsg);
                fotaMessage = true;
                break;
//            case CMD_UP_UPD_GW_CERT://收到证书更新的应答,PKI信息安全专用
//                ProcCertificationUpgrade procCertificationUpgrade = new ProcCertificationUpgrade(kafkaService, tboxService);
//                procCertificationUpgrade.checkCertificationUpgrade(requestMsg);
//                outData = null;
//                break;
//            case CMD_UP_AVN_UPD_GW_CERT://AVN收到证书更新的应答,PKI信息安全专用
//                ProcCertificationUpgrade procCertificationUpgrade1 = new ProcCertificationUpgrade(kafkaService, tboxService);
//                procCertificationUpgrade1.checkCertificationUpgrade(requestMsg);
//                outData = null;
//                break;
            case CMD_UP_AVN_UPGRADE_NOTIFY://车主AVN端是否同意升级
                ProcAgreeUpgrade procAgreeUpgrade = new ProcAgreeUpgrade(kafkaService, tboxService);
                fotaRespParam = procAgreeUpgrade.checkAgreeUpgrade(requestMsg);
                fotaMessage = true;
                break;
            case CMD_UP_DOWNLOAD_PROGRESS://下载进度上报
                ProcProgressReport procDownloadProgressReport = new ProcProgressReport(kafkaService, tboxService);
                fotaRespParam = procDownloadProgressReport.checkProgressReport(requestMsg, FOTAConstant.DOWNLOAD_PROGRESS_CMD);
                fotaMessage = true;
                break;
            case CMD_UP_UPGRADE_RESULT://升级进度上报
                ProcProgressReport procUpgradeProgressReport = new ProcProgressReport(kafkaService, tboxService);
                fotaRespParam = procUpgradeProgressReport.checkProgressReport(requestMsg, FOTAConstant.UPGRADE_RESULT_CMD);
                fotaMessage = true;
                break;
            case CMD_UP_REMOTECTRL_EXT://远程控制指令的执行结果（多指令）
            	tspServiceProc.checkRmtGroupControlExt(requestMsg);
                outData = null;
                break;
            default:
                break;
        }
        
        TimeLogUtil.log(logName+"指令解析完成");
        //String retHex = null;
        byte[] otaMsg2bytes = null;
        //Fota业务TBox主动请求模块
        if (fotaMessage) {
            otaMsg2bytes = (fotaRespParam != null) ? dataProcessing.otaMsg2bytes(requestMsg, fotaRespParam) : null;
        } else {//一般回复模块
            otaMsg2bytes = (outData != null) ? dataProcessing.otaMsg2bytes(requestMsg, outData) : null;
        }
//        if (otaMsg2bytes != null) {
//            // 有回包，则设置回包
//            retHex = ByteUtil.byteToHex(otaMsg2bytes);
//        }
        TimeLogUtil.log(logName+"方法结束");
        return otaMsg2bytes;

    }
    
    /**
     * @return void
     * @Description 判断redis中是否存在远程组合控制指令topic
     * @Date 2019/1/30 8:39
     * @Param []
     **/
    public boolean existRmtGroupTopic(String serialNumber, String operationName) {
        try {
            logger.debug("TBox(SN:{})查询redis中是否存在组合远控topic Key:{}", serialNumber, operationName + serialNumber);
            boolean result = redisAPI.hasKey(operationName + serialNumber);
            logger.info("TBox(SN:{})查询远程控制topic结果为:{}", serialNumber, result);
            return result;
        } catch (Exception e) {
            logger.error("TBox(SN:{})查询Redis中远程组合控制topic发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
            return false;
        }
    }

}
