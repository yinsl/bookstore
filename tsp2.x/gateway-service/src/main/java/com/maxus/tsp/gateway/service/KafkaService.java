/**
 * Kafka相关业务类
 * Copyright (c) 2018年7月14日 by 上汽集团商用车技术中心
 *
 * @author 任怀宇
 * @version 1.0
 */
package com.maxus.tsp.gateway.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.DatePatternConstant;
import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.constant.KafkaMsgConstantFota;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.DateUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.model.BaseRmtCtrlItReq;
import com.maxus.tsp.gateway.common.model.BlueToothCtrlItRespInfo;
import com.maxus.tsp.gateway.common.model.BlueToothRequestInfo;
import com.maxus.tsp.gateway.common.model.DownLoadFileMo;
import com.maxus.tsp.gateway.common.model.EarlyWarningInfo;
import com.maxus.tsp.gateway.common.model.HomeCtrlItRequest;
import com.maxus.tsp.gateway.common.model.Kafka_BigData;
import com.maxus.tsp.gateway.common.model.Kafka_EngData;
import com.maxus.tsp.gateway.common.model.Kafka_LocationData;
import com.maxus.tsp.gateway.common.model.Kafka_OTAData;
import com.maxus.tsp.gateway.common.model.Kafka_RegisterData;
import com.maxus.tsp.gateway.common.model.RegisterResultData;
import com.maxus.tsp.gateway.common.model.RemoteControlItRequest;
import com.maxus.tsp.gateway.common.model.RemoteCtrlItResponse;
import com.maxus.tsp.gateway.common.model.TakePhotoItRequest;
import com.maxus.tsp.gateway.common.model.TboxUpdateRvmReq;
import com.maxus.tsp.gateway.common.model.TboxUpdateRvmResponse;
import com.maxus.tsp.gateway.common.model.UpLoadFileMo;
import com.maxus.tsp.gateway.mq.kafka.KafkaItProducer;
import com.maxus.tsp.gateway.mq.kafka.KafkaProducer;
import com.maxus.tsp.gateway.ota.TspServiceProc;
import com.maxus.tsp.platform.service.model.vo.CodeValue;

@Service
public class KafkaService {

    private Logger logger = LogManager.getLogger(KafkaService.class);

    @Autowired
    private DataProcessing dataProcessing;
    
    // 数据库服务
    @Autowired
    private  TboxService tboxService;
    // kafka服务,用于与it进行交互
    @Autowired
    @Qualifier("kafkaItProducer")
    private KafkaItProducer kafkaItService;
    // kafka服务,用于网关节点间的交互
    @Autowired
    // 网关本身使用的kafka
    private KafkaProducer kafkaGatewayService;

    //
    private int wakeUpDelayTime;

    /**
     * 发送网关节点内部的通知，关于收到RVM的注册返回结果
     *
     * @param tboxSn
     */
	public void notifyReturnRegisterRtToTbox(String tboxSn) {
		try {
				TspServiceProc proc = TspServiceProc.getRegistProc(tboxSn);
				logger.info("收到网关自身广播的tbox({})在RVM的注册成功结果,准备确认tbox是否在当前节点通信", tboxSn);
				if (proc != null) {
					try {
						synchronized (proc) {
							proc.notifyAll();
						}
					} catch (Exception ex) {
						logger.error("TBox({})的注册结果返回发生异常:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
					}
					logger.info("当前网关有tbox({})的注册等待线程, 通知返回注册成功结果。", tboxSn);
				} else {
					logger.info("当前网关没有tbox({})的注册等待线程, 通知被丢弃。", tboxSn);
				}
			
			} catch (Exception e) {
				logger.error("消息消费异常！ 异常信息:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
			}
		
	}

    /**
     * 发送唤醒消息
     * @param eventTime
     * @param curTboxSn
     */
    public void sndWakeUp(String eventTime, String curTboxSn) {
        kafkaGatewayService.sndMesForTemplate(KafkaMsgConstant.TOPIC_REMOTECTRL_WAKEUP,
                curTboxSn + "_" + eventTime, curTboxSn);
    }

    /**
     * 发送远控响应消息
     * @param value
     * @param curTboxSn
     */
    public void sndRmtCtrlRep(String value, String curTboxSn) {
        kafkaGatewayService.sndMesForTemplate(KafkaMsgConstant.TOPIC_REMOTECTRL_RES,
                value, curTboxSn);
    }

    /**
     * 发送透传响应消息
     * @param value
     * @param curTboxSn
     */
    public void sndForwardRep(String value, String curTboxSn) {
        kafkaGatewayService.sndMesForTemplate(KafkaMsgConstant.TOPIC_FORWARD_UP,
                value, curTboxSn);
    }

    /**
     * 发送蓝牙钥匙新增响应消息
     * @param value
     * @param curTboxSn
     */
    public void sndAddBtKeyResp(String value, String curTboxSn) {
        kafkaGatewayService.sndMesForTemplate(KafkaMsgConstant.TOPIC_UP_BLUETOOTH_ADD_BTKEY, value, curTboxSn);
    }

    /**
     * 发送蓝牙钥匙删除响应消息
     * @param value
     * @param curTboxSn
     */
    public void sndDelBtKeyRep(String value, String curTboxSn) {
        kafkaGatewayService.sndMesForTemplate(KafkaMsgConstant.TOPIC_UP_BLUETOOTH_DEL_BTKEY,
                value, curTboxSn);
    }

    /**
     * 发送获取蓝牙钥匙响应消息
     * @param value
     * @param curTboxSn
     */
    public void sndGetBtKeyRep(String value, String curTboxSn) {
        kafkaGatewayService.sndMesForTemplate(KafkaMsgConstant.TOPIC_UP_BLUETOOTH_GET_BTKEY,
                value, curTboxSn);
    }

    /**
     * 发送验证蓝牙响应消息
     * @param value
     * @param curTboxSn
     */
    public void sndValidBtKeyRep(String value, String curTboxSn) {
        kafkaGatewayService.sndMesForTemplate(KafkaMsgConstant.TOPIC_UP_BLUETOOTH_VERIFICATION_CODE,
                value, curTboxSn);
    }

    /**
     * 发送远程配置响应消息
     * @param value
     * @param curTboxSn
     */
    public void sndRmtConfigRep(String value, String curTboxSn) {
        kafkaGatewayService.sndMesForTemplate(KafkaMsgConstant.TOPIC_UP_REMOTE_CONFIG_RES,
                value, curTboxSn);
    }

    /**
     * 发送获取远程配置响应消息
     * @param value
     * @param curTboxSn
     */
    public void sndGetRmtConfigRep(String value, String curTboxSn) {
        kafkaGatewayService.sndMesForTemplate(KafkaMsgConstant.TOPIC_UP_GET_REMOTE_CONFIG_RES,
                value, curTboxSn);
    }

    //向IT kafka 投递注册信息
    public boolean transferRegisterData(Kafka_RegisterData senderInfo, String tboxSn) {
        boolean transferResult = true;
        try {
            kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_REGISTER_DATA, senderInfo, tboxSn);
        } catch (Exception ex) {
            logger.error("TBox(SN:{})投递register_data信息出错:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            transferResult = false;
        }
        return transferResult;
    }

    public boolean transferLocationData(Kafka_LocationData senderInfo, String tboxSn) {
        boolean transferResult = true;
        try {
            kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_LOCATON_DATA, senderInfo, tboxSn);
        } catch (Exception ex) {
            logger.error("TBox(SN:{})投递location_data信息出错:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            transferResult = false;
        }
        return transferResult;
    }

    /**
     * @Title: transferOTAData
     * @Description: 数据转换
     * @param tboxSn
     * @param command
     * @param time
     * @return transferResult
     */
    public boolean transferOTAData(String tboxSn, String command, String time, long receiveTime) {
        boolean transferResult = true;
        try {
            Kafka_OTAData otaData = new Kafka_OTAData();
            if (dataProcessing.isTBoxDataNeedTransfer(tboxSn)) {
                otaData.setSn(tboxSn);
                otaData.setCollectTime(DateUtil.stringToLong(time, DatePatternConstant.SECOND));
                otaData.setCommand(command);
                otaData.setGatewayTimeIn(receiveTime);
                otaData.setGatewayTimeOut(System.currentTimeMillis());
                kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_OTA_DATA, otaData, tboxSn);
            }
        } catch (Exception ex) {
            logger.error("TBox(SN:{})投递ota_data信息出错:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            transferResult = false;
        }
        return transferResult;
    }

    public boolean transferOTAData(String tboxSn, String command, long collectTime, CodeValue[] items, long gatewayTimeIn) {
        boolean transferResult = true;
        try {
            Kafka_OTAData otaData = new Kafka_OTAData();
            if (dataProcessing.isTBoxDataNeedTransfer(tboxSn)) {
                otaData.setSn(tboxSn);
                otaData.setCollectTime(collectTime);
                otaData.setCommand(command);
                otaData.setItems(items);
                String curvin = tboxService.getVINForTbox(tboxSn);
                if (!StringUtils.isEmpty(curvin)) {
                    otaData.setVin(tboxService.getVINForTbox(tboxSn));
                }
                otaData.setGatewayTimeIn(gatewayTimeIn);
                otaData.setGatewayTimeOut(System.currentTimeMillis());
                kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_OTA_DATA, otaData, tboxSn, true);
            }
        } catch (Exception ex) {
            logger.error("TBox(SN:{})投递ota_data信息出错:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            transferResult = false;
        }
        return transferResult;
    }

    public boolean transferOTAData(Kafka_OTAData otaData, String command, long collectTime) {
        boolean transferResult = true;
        String tboxSn = otaData.getSn();
        try {
            if (dataProcessing.isTBoxDataNeedTransfer(tboxSn)) {
                kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_OTA_DATA, otaData, tboxSn, false);
            }
        } catch (Exception ex) {
            logger.error("TBox(SN:{})投递ota_data信息出错:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            transferResult = false;
        }
        return transferResult;
    }

    public boolean transferOTAData(String tboxSn, long collectTime, int compressStatus, byte[] items) {
        boolean transferResult = true;
        try {
            Kafka_BigData bigData = new Kafka_BigData();
            if (dataProcessing.isTBoxDataNeedTransfer(tboxSn)) {
                bigData.setSn(tboxSn);
                bigData.setCollectTime(collectTime);
                bigData.setCompressStatus(compressStatus);
                bigData.setItems(items);
                String curvin = tboxService.getVINForTbox(tboxSn);
                if (!StringUtils.isEmpty(curvin)) {
                    bigData.setVin(tboxService.getVINForTbox(tboxSn));
                }
                kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_BIG_DATA, bigData, tboxSn, true);
            }
        } catch (Exception ex) {
            logger.error("TBox(SN:{})投递big_data信息出错:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            transferResult = false;
        }
        return transferResult;
    }


    public boolean transferENGData(String tboxSn, long collectTime, int compressStatus, byte[] items) {
        boolean transferResult = true;
        try {
            Kafka_EngData engData = new Kafka_EngData();
            if (dataProcessing.isTBoxDataNeedTransfer(tboxSn)) {
                engData.setSn(tboxSn);
                engData.setCollectTime(collectTime);
                engData.setCompressStatus(compressStatus);
                engData.setItems(ByteUtil.byteToHex(items));
                String curvin = tboxService.getVINForTbox(tboxSn);
                if (!StringUtils.isEmpty(curvin)) {
                    engData.setVin(tboxService.getVINForTbox(tboxSn));
                }
                kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_ENG_DATA, engData, tboxSn, true);
            }
        } catch (Exception ex) {
            logger.error("TBox(SN:{})投递eng_data信息出错:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            transferResult = false;
        }
        return transferResult;
    }

    public boolean transferRmtResponse(RemoteCtrlItResponse rmtItResponse) {
        boolean transferResult = true;
        if (rmtItResponse != null) {
            String tboxSn = rmtItResponse.getSn();
            try {
                kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_IT_REMOTECTRL_RESPONSE, rmtItResponse, tboxSn,
                        true);
            } catch (Exception ex) {
                logger.error("TBox(SN:{})投递远程控制结果信息出错:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
                transferResult = false;
            }
        } else {
            logger.warn("TBox投递远程控制结果数据为空!");
            transferResult = false;
        }
        return transferResult;
    }

    /**
     * 蓝牙远程控制 回复it处理结果
     * @param blueToothCtrlItRespInfo
     * @return
     */
    public boolean transferBlueToothResponse(BlueToothCtrlItRespInfo blueToothCtrlItRespInfo){
        boolean transferResult = true;
        if (blueToothCtrlItRespInfo != null) {
            String tboxSn = blueToothCtrlItRespInfo.getSn();
            try {
                kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_IT_BT_RESPONSE, blueToothCtrlItRespInfo, tboxSn,
                        true);
            } catch (Exception ex) {
                logger.error("TBox(SN:{})投递蓝牙远程控制结果信息出错:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
                transferResult = false;
            }
        } else {
            logger.warn("TBox投递蓝牙远程控制结果数据为空!");
            transferResult = false;
        }
        return transferResult;
    }

    /**
     * 房车家居远控 回复it处理结果
     * @param rmtItResponse
     * @return
     * @author zhuna
     * @date 2018年11月8日
     */
    public boolean transferHomeCtrlResponse(RemoteCtrlItResponse rmtItResponse) {
        boolean transferResult = true;
        if (rmtItResponse != null) {
            String tboxSn = rmtItResponse.getSn();
            try {
                logger.info("房车家居远控指令返回结果{}",JSONObject.toJSONString(rmtItResponse));
                kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_IT_HOME_CTRL_RESPONSE, rmtItResponse, tboxSn,
                        true);
            } catch (Exception ex) {
                logger.error("TBox(SN:{})投递房车家居远程控制结果信息出错:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
                transferResult = false;
            }
        } else {
            logger.warn("TBox投递房车家居远程控制结果数据为空!");
            transferResult = false;
        }
        return transferResult;
    }

    public boolean transferTakePhotoResponse(RemoteCtrlItResponse rmtItResponse) {
        boolean transferResult = true;
        if (rmtItResponse != null) {
            String tboxSn = rmtItResponse.getSn();
            try {
                kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_IT_TAKEPHOTO_RESPONSE, rmtItResponse, tboxSn,
                        true);
            } catch (Exception ex) {
                logger.error("TBox(SN:{})投递拍照结果信息出错:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
                transferResult = false;
            }
        } else {
            logger.warn("TBox投递拍照结果数据为空!");
            transferResult = false;
        }
        return transferResult;
    }

    public boolean transferTboxFOTAResponse(TboxUpdateRvmResponse tboxUpdateRvmResponse) {
        boolean transferResult = true;
        if (tboxUpdateRvmResponse != null) {
            String tboxSn = tboxUpdateRvmResponse.getSn();
            try {
                kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_IT_REMOTEUPDATE_RESPONSE, tboxUpdateRvmResponse, tboxSn,
                        true);
            } catch (Exception ex) {
                logger.error("TBox(SN:{})投递远程升级结果信息出错:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
                transferResult = false;
            }
        } else {
            logger.warn("TBox投递远程升级结果数据为空!");
            transferResult = false;
        }
        return transferResult;
    }

    public boolean transferTboxFileLoadResponse(TboxUpdateRvmResponse tboxUpdateRvmResponse, String topic) {
        boolean transferResult = true;
        if (tboxUpdateRvmResponse != null) {
            String tboxSn = tboxUpdateRvmResponse.getSn();
            try {
                kafkaItService.sndMesForTemplate(topic, tboxUpdateRvmResponse, tboxSn, true);
            } catch (Exception ex) {
                logger.error("TBox(SN:{})投递({})结果信息出错:{}", tboxSn, topic, ThrowableUtil.getErrorInfoFromThrowable(ex));                ;
                transferResult = false;
            }
        } else {
            logger.warn("TBox投递结果数据为空!");
            transferResult = false;
        }
        return transferResult;
    }

    //Tbox投递国六B排放数据信息结果
    public boolean transferGBEmissionResponse(Kafka_OTAData kafka_otaData, String topic) {
    	boolean transferResult = true;
        if (kafka_otaData != null) {
            String tboxSn = kafka_otaData.getSn();
            try {
                kafkaItService.sndMesForTemplate(topic, kafka_otaData, tboxSn, true);
            } catch (Exception ex) {
                logger.error("TBox(SN:{})投递({})结果信息出错:{}", tboxSn, topic, ThrowableUtil.getErrorInfoFromThrowable(ex));                ;
                transferResult = false;
            }
        } else {
            logger.warn("TBox投递结果数据为空!");
            transferResult = false;
        }
        return transferResult;
    }
    
    //Tbox投递国六B功能的登入数据信息结果
    public boolean transferGBLoginEmissionResponse(Kafka_OTAData loginEmissionInfo, String topic) {
    	boolean transferResult = true;
        if (loginEmissionInfo != null) {
            String tboxSn = loginEmissionInfo.getSn();
            try {
                kafkaItService.sndMesForTemplate(topic, loginEmissionInfo, tboxSn, true);
            } catch (Exception ex) {
                logger.error("TBox(SN:{})投递({})结果信息出错:{}", tboxSn, topic, ThrowableUtil.getErrorInfoFromThrowable(ex));                ;
                transferResult = false;
            }
        } else {
            logger.warn("TBox投递结果数据为空!");
            transferResult = false;
        }
        return transferResult;
    }
    
    //Tbox投递国六B功能的登出数据信息结果
    public boolean transferGBLogoutEmissionResponse(Kafka_OTAData logoutEmissionInfo, String topic) {
    	boolean transferResult = true;
        if (logoutEmissionInfo != null) {
            String tboxSn = logoutEmissionInfo.getSn();
            try {
                kafkaItService.sndMesForTemplate(topic, logoutEmissionInfo, tboxSn, true);
            } catch (Exception ex) {
                logger.error("TBox(SN:{})投递({})结果信息出错:{}", tboxSn, topic, ThrowableUtil.getErrorInfoFromThrowable(ex));                ;
                transferResult = false;
            }
        } else {
            logger.warn("TBox投递结果数据为空!");
            transferResult = false;
        }
        return transferResult;
    }
    
    //Tbox投递预警信息结果
    public boolean transferEarlyWarningResponse(EarlyWarningInfo earlyWarningInfo, String topic) {
        boolean transferResult = true;
        if (earlyWarningInfo != null) {
            String tboxSn = earlyWarningInfo.getSn();
            try {
                kafkaItService.sndMesForTemplate(topic, earlyWarningInfo, tboxSn, true);
            } catch (Exception ex) {
                logger.error("TBox(SN:{})投递({})结果信息出错:{}", tboxSn, topic, ThrowableUtil.getErrorInfoFromThrowable(ex));                ;
                transferResult = false;
            }
        } else {
            logger.warn("TBox投递结果数据为空!");
            transferResult = false;
        }
        return transferResult;
    }

    public void sendTestForRegisterResult(RegisterResultData rt) {
        kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_REGISTER_RESULT_DATA, rt, rt.getSn());
    }

    public void sendTestForRemoteCtrl(RemoteControlItRequest rt) {
        kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_IT_REMOTECTRL_REQUEST, rt, rt.getSn());
    }

    public void sendTestForVersionQuery(BaseRmtCtrlItReq versionQueryRequestInfo) {
        kafkaItService.sndMesForTemplate(KafkaMsgConstantFota.TOPIC_IT_VERSION_QUERY_REQ, versionQueryRequestInfo, versionQueryRequestInfo.getSn());
    }
    
    public void sendTestForHomeCtrl(HomeCtrlItRequest rt) {
        kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_IT_HOME_CTRL_REQUEST, rt, rt.getSn());
    }

    public void sendTestForTakePhoto(TakePhotoItRequest rt) {
        kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_IT_TAKEPHOTO_REQUEST, rt, rt.getSn());
    }

    public void sendTestForBlueTooth(BlueToothRequestInfo rt) {
        kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_IT_BT_REQUEST, rt, rt.getSn());
    }

    public void senTestForFOTA(TboxUpdateRvmReq rt) {
        kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_IT_REMOTEUPDATE_REQUEST, rt, rt.getSn());
    }

    public void sendTestForDownLoadFile(DownLoadFileMo rt) {
        kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_IT_DOWNLOAD_FILE_REQUEST, rt, rt.getSn());
    }

    public void sendTestForUpLoadFile(UpLoadFileMo rt) {
        kafkaItService.sndMesForTemplate(KafkaMsgConstant.TOPIC_IT_UPLOAD_FILE_REQUEST, rt, rt.getSn());
    }


    /**
     *  根据登出报文判断是否需要延迟唤醒线程至距离登出为10s后执行
     * @param tboxsn
     */
    public void checkTBoxLogOutForWakeupWait(String tboxsn) {
        // 获取tbox最新的LOGOUT报文形式登出时间
        String lastLogoutTime = dataProcessing.logoutTboxGet(
                tboxsn)/* GlobalSessionChannel.getTboxLogout(tboxsn) */;
        Date lastLogoutDT;
        if (lastLogoutTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 计算时间是否相差5s，是则退出，否则sleep相应时间差
            try {
                lastLogoutDT = sdf.parse(lastLogoutTime);
                Calendar curCalendar = Calendar.getInstance();
                logger.info("当前操控请求的原始时间:{}", sdf.format(curCalendar.getTime()));
                curCalendar.add(Calendar.SECOND, -wakeUpDelayTime);
                logger.info("请求时间计算偏移后结果为:{}", sdf.format(curCalendar.getTime()));
                if (lastLogoutDT.compareTo(curCalendar.getTime()) > 0) {
                    // 需要计算差值时间进行sleep
                    long diff = lastLogoutDT.getTime() - curCalendar.getTime().getTime();
                    logger.warn("由于TBox(SN:{})刚发送登出报文，唤醒请求将延迟{}毫秒", tboxsn, diff);
                    Thread.sleep(diff);
                    logger.info("TBox(SN:{})结束唤醒请求延迟等待，将执行唤醒。", tboxsn);
                }

            } catch (Exception ex) {
                logger.error("确认延迟唤醒流程发生异常:{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
                return;
            }
        }
        return;
    }

    /**
     * @Description 给自身kafka 指定topic发送消息 公共方法
     * @param value
     * @param curTboxSn
     */
    public void sndMesForTemplate(String topic, String value, String curTboxSn) {
        kafkaGatewayService.sndMesForTemplate(topic,
                value, curTboxSn);
    }

    /**
     * @Description 重载方法，用于自身通信避免json解析出错
     * @Date 2019/2/21 19:18
     * @Param [topic, value, curTboxSn]
     * @return void
     **/
    public void sndMesForTemplate(String topic, Object value, String curTboxSn) {
        kafkaGatewayService.sndMesForTemplate(topic,
                value, curTboxSn);
    }

    /**
     * @Description 给It kafka 指定的topic发送消息 公共方法
     * @Date 2019/1/16 14:24
     * @Param [topic, value, curTboxSn]
     * @return void
     **/
    public void sndMesToITForTemplate(String topic, Object value, String curTboxSn) {
        kafkaItService.sndMesForTemplate(topic, value, curTboxSn);
    }
    
    //向IT kafka投递信息 主要用于OTA升级模块的数据投递 公共方法
    public boolean transferItData(String topic, Object value, String tboxSn){
        boolean transferResult = true;
        try {
            kafkaItService.sndMesForTemplate(topic, value, tboxSn);
        } catch (Exception ex) {
            logger.error("TBox(SN:{})Topic({})投递信息({})出错:{}", tboxSn, topic, JSONObject.toJSONString(value), ThrowableUtil.getErrorInfoFromThrowable(ex));
            transferResult = false;
        }
        return transferResult;
    }

}
