/**
 * TBoxRemoteCtrl.java Create on 2017年6月5日
 * Copyright (c) 2017年6月5日 by 上汽集团商用车技术中心
 *
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>
 * @version 1.0
 */
package com.maxus.tsp.gateway.controller;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.maxus.tsp.common.constant.KafkaMsgConstant;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.model.BlueToothRequestInfo;
import com.maxus.tsp.gateway.common.model.DownLoadFileMo;
import com.maxus.tsp.gateway.common.model.HomeCtrlItRequest;
import com.maxus.tsp.gateway.common.model.RegisterResultData;
import com.maxus.tsp.gateway.common.model.RemoteControlItRequest;
import com.maxus.tsp.gateway.common.model.RmtGroupRequestInfo;
import com.maxus.tsp.gateway.common.model.TakePhotoItRequest;
import com.maxus.tsp.gateway.common.model.TboxUpdateRvmReq;
import com.maxus.tsp.gateway.common.model.UpLoadFileMo;
import com.maxus.tsp.gateway.gb.GBDataProcessing;
import com.maxus.tsp.gateway.gb.GBNettyClient;
import com.maxus.tsp.gateway.mq.kafka.KafkaItProducer;
import com.maxus.tsp.gateway.service.DataProcessing;
import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.service.MqttService;
import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
import com.maxus.tsp.platform.service.model.car.Tbox;
import com.maxus.tsp.platform.service.model.vo.ItRedisInfo;
import com.maxus.tsp.platform.service.model.vo.TboxVo;

/**
 * @author 任怀宇
 * @version V1.0
 * @ClassName: TBoxController.java
 * @Description: 远程控制API
 * @Date 2017年7月18日 下午5:10:42
 */
@RestController
// @RequestMapping(value = "/tbox")
public class TestCtrl {

    @Value("${test.flag:true}")
    private boolean testFlag;
    
    @Autowired
    private DataProcessing dataProcessing;
    
    private final Logger logger = LogManager.getLogger(TestCtrl.class);

    @Autowired
    GBNettyClient gbNettyClient;

    @Autowired
    GBDataProcessing gbDataProcessing;
    
    @Autowired
    MqttService mqttService;

    // @Autowired
    // StreamSender StreamSender;
    // @Autowired
    // TboxRemoteConfigCtrl configCtrl;

    // 网关本身使用的kafka
    @Autowired
    KafkaService kafkaService;

    @Autowired
    RedisAPI redisAPI;

    // itservice
    // @Autowired
    // private ItServiceClient itServiceClient;

    @Autowired
    TspPlatformClient tspPlatformClient;

    @Autowired
    private GBDataProcessing gbdProcessing;

    // 网关与it平台交互的kafka
    @Autowired
    @Qualifier("kafkaItProducer")
    KafkaItProducer kafkaItService;

    /**
     * @Description: 发送短信测试接口.
     * @param {type} {type}
     * @return: boolean ret 短信是否发送成功
     */
    @RequestMapping(value = "/testSms", method = RequestMethod.GET)
    @Pointcut()
    public boolean testSms(String sn) {
        boolean ret = true;
        ret = dataProcessing.isSendingMessageSucceed(sn);
        return ret;
    }
    
    /**
     * @Description: 获取平台激活操作处理结果.
     * @param {type} {type}
     * @return: RegisterResultData rt
     */
    @RequestMapping(value = "/testRegRt", method = RequestMethod.GET)
    public String testRegisterRt(String sn, String status) {
        RegisterResultData rt = new RegisterResultData();
        rt.setSn(sn);
        rt.setStatus(status);
        kafkaService.sendTestForRegisterResult(rt);
        return JSONObject.toJSONString(rt);

    }

    /**
     * @Description: 单条远程控制接口. (远程配置/国家平台直连操作/SendPOI/获取整车状态/寻车/限速/远程开门/空调温度调节)
     * @param {type} {type}
     * @return: "test receive remote control request from it"
     */
    @RequestMapping(value = "/testRmtCtrl", method = RequestMethod.GET)
    public String testRmtCtrl(String cmd, String value, String sn,
            @RequestParam(value = "eventTime", required = false) Long eventTime) {
        logger.info("test Remote Ctrl");
        Calendar calendar = Calendar.getInstance();
        RemoteControlItRequest rt = new RemoteControlItRequest();
        rt.setComd(cmd);
        rt.setValue(value);
        rt.setSn(sn);
        rt.setSeqNo("2018032216343098712345");
        // 没传时间戳或时间戳没到毫秒
        if (null == eventTime || String.valueOf(eventTime).length() != 13) {
            rt.setEventTime(calendar.getTime().getTime());
        } else {
            rt.setEventTime(eventTime);
        }
        kafkaService.sendTestForRemoteCtrl(rt);
        return "test receive remote control request from it";

    }

    /**
     * @Description: 房车家居远控接口
     * @param sn
     * @param {type} {type}
     * @return: "test receive home control request from it"
     */
    @RequestMapping(value = "/testHomeCtrl", method = RequestMethod.GET)
    public String testHomeCtrl(String sn, int paramSize, String param,
            @RequestParam(value = "eventTime", required = false) Long eventTime) {
        logger.info("test Remote Ctrl");
        Calendar calendar = Calendar.getInstance();

        HomeCtrlItRequest homeCtrl = new HomeCtrlItRequest();
        homeCtrl.setSn(sn);
        homeCtrl.setParamSize(paramSize);
        homeCtrl.setParam(param);
        homeCtrl.setSeqNo("2018032216343098712345");
        // 没传时间戳或时间戳没到毫秒
        if (null == eventTime || String.valueOf(eventTime).length() != 13) {
            homeCtrl.setEventTime(calendar.getTime().getTime());
        } else {
            homeCtrl.setEventTime(eventTime);
        }
        kafkaService.sendTestForHomeCtrl(homeCtrl);
        /*
         * List<HomeCtrlResultMo> ctrlResultList = new ArrayList<>(); HomeCtrlResultMo
         * aa = new HomeCtrlResultMo(1,1); HomeCtrlResultMo bb = new
         * HomeCtrlResultMo(2,2); ctrlResultList.add(aa); ctrlResultList.add(bb);
         * System.out.println(JSONObject.toJSONString(ctrlResultList));
         * System.out.println(JSONArray.toJSONString(ctrlResultList));
         * 
         * OTARemoteCommand comd = OTARemoteCommand.valueOf("HomeEquipmentCtrl");
         * OTARemoteCommand co = OTARemoteCommand.HomeEquipmentCtrl; boolean a =
         * comd.equals(OTARemoteCommand.HomeEquipmentCtrl);
         */
        logger.info("传入参数值：{},sn[{}]", JSONObject.toJSONString(homeCtrl), sn);
        return "test receive home control request from it";

    }

    /**
     * @Description: 蓝牙远程控制接口.
     * @param {type} {type}
     * @return: "test receive bluetooth control request from it"
     */
    @RequestMapping(value = "/testBlueToothCtrl", method = RequestMethod.POST)
    public String testRmtCtrl(@RequestBody BlueToothRequestInfo btReqInfo) {
        logger.info("test bluetooth Ctrl");
        Calendar calendar = Calendar.getInstance();
        // 没传时间戳或时间戳没到毫秒
        if (btReqInfo.getEventTime() == 0) {
            btReqInfo.setEventTime(calendar.getTime().getTime());
        }
        kafkaService.sendTestForBlueTooth(btReqInfo);
        return "test receive bluetooth control request from it";
    }

    /**
     * @Description: 透传-拍照远程控制接口.
     * @param {type} {type}
     * @return: "test receive take Photo request from it"
     */
    @RequestMapping(value = "/testTakePhoto", method = RequestMethod.GET)
    public String testTakePhoto(String cameraList, String sn,
            @RequestParam(value = "eventTime", required = false) Long eventTime) {
        logger.info("test take Photo");
        Calendar calendar = Calendar.getInstance();
        logger.info(calendar.getTime());
        calendar.add(Calendar.SECOND, -9);
        logger.info(calendar.getTime());
        TakePhotoItRequest rt = new TakePhotoItRequest();
        rt.setCameraList(cameraList);
        rt.setSn(sn);
        // 没传时间戳或时间戳没到毫秒
        if (null == eventTime || String.valueOf(eventTime).length() != 13) {
            rt.setEventTime(calendar.getTime().getTime());
        } else {
            rt.setEventTime(eventTime);
        }
        kafkaService.sendTestForTakePhoto(rt);
        return "test receive take Photo request from it";
    }

    // 平台登录
    @RequestMapping(value = "/GbPlatformLogin", method = RequestMethod.GET)
    public Object gbPlatformLogin() {
        return JSONObject
                .toJSON(gbdProcessing.sendGBmsg(gbDataProcessing.getGBPlatformLoginMessage("SQ12345SQ99900000")));
    }

    // 平台登出
    @RequestMapping(value = "/GbPlatformLogout", method = RequestMethod.GET)
    public Object gbPlatformLoginout() {
        return JSONObject
                .toJSON(gbdProcessing.sendGBmsg(gbDataProcessing.getGBPlatformLogoutMessage("SQ12345SQ99900000")));

    }

    // 车辆登录
    @RequestMapping(value = "/gbCarLogin", method = RequestMethod.GET)
    public Object gbCarLogin(String sn) {
        return JSONObject.toJSON(
                gbdProcessing.sendGBmsg(gbDataProcessing.getGBCarloginMessage(" J14400SI1080001", "1", 1, 1, "1")));

    }

    // 车辆登出
    @RequestMapping(value = "/gbCarLogout", method = RequestMethod.GET)
    public Object gbCarLogout(String sn) {
        return JSONObject.toJSON(gbdProcessing.sendGBmsg(gbDataProcessing.getGBCarLogoutMessage(" J14400SI1080001")));

    }

    // // 远程配置测试
    // @RequestMapping(value = "/remoteConfig", method = RequestMethod.GET)
    // public String getRemoteConfig(@RequestParam(value = "eventTime", required =
    // false) Long eventTime) {
    // // doRemoteConfig(String comd, String value, String serialNum, String
    // // eventTime, String seqNo)
    // String comd = "CmdRemoteStallConfig";
    // String value = "01";
    // String sn = " J14400SI1080001";
    // //没传时间戳或时间戳没到毫秒
    // if (null == eventTime || String.valueOf(eventTime).length() != 13) {
    // eventTime = System.currentTimeMillis();
    // }
    // String seqNo = "2018032216343098712345";
    // return configCtrl.doRemoteConfig(comd, value, sn, eventTime, seqNo, null);
    //
    // }

    /**
     * @Description: 国家平台直连操作(回调远程控制接口,设定cmd为DirectReport).
     * @param {type} {type}
     * @return:"test direct report"
     */
    @RequestMapping(value = "/directReport", method = RequestMethod.GET)
    public String getDirectReport(String sn, String value,
            @RequestParam(value = "eventTime", required = false) Long eventTime) {
        RemoteControlItRequest rt = new RemoteControlItRequest();
        rt.setComd("DirectReport");
        rt.setValue(value);
        rt.setSn(sn);
        rt.setSeqNo("2018032216343098712345");
        // 没传时间戳或时间戳没到毫秒
        if (null == eventTime || String.valueOf(eventTime).length() != 13) {
            rt.setEventTime(System.currentTimeMillis());
        } else {
            rt.setEventTime(eventTime);
        }
        kafkaService.sendTestForRemoteCtrl(rt);
        return "test direct report";

    }

    /**
     * @Description: Fota功能接口-TBox升级.
     * @param {type} {type}
     * @return: "test receive testFOTA request from it"
     */
    @RequestMapping(value = "/testFOTA", method = RequestMethod.GET)
    public String getFOTA(String sn, String version, String url, String md5, String seqNo,
            @RequestParam(value = "eventTime", required = false) Long eventTime) {
        logger.info("test FOTA Ctrl");
        Calendar calendar = Calendar.getInstance();
        logger.info(calendar.getTime());
        calendar.add(Calendar.SECOND, -9);
        logger.info(calendar.getTime());
        TboxUpdateRvmReq tbox = new TboxUpdateRvmReq();
        tbox.setMd5(md5);
        tbox.setSn(sn);
        tbox.setUrl(url);
        tbox.setVersion(version);
        tbox.setSeqNo(seqNo);
        // 没传时间戳或时间戳没到毫秒
        if (null == eventTime || String.valueOf(eventTime).length() != 13) {
            tbox.setEventTime(calendar.getTime().getTime());
        } else {
            tbox.setEventTime(eventTime);
        }
        kafkaService.senTestForFOTA(tbox);
        return "test receive testFOTA request from it";

    }

    /**
     * @Description: 文件下载接口.
     * @param {type} {type}
     * @return: "test downloadfile"
     */
    @RequestMapping(value = "/testDownLoadFile", method = RequestMethod.GET)
    public String getDownLoadFile(String sn, String seqNo, String fileType, String fileInfoSize, String fileInfo,
            String url, String md5Data, @RequestParam(value = "currentTime", required = false) Long currentTime) {
        System.out.println("testDownLoadFile-1");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -9);
        DownLoadFileMo test = new DownLoadFileMo();
        test.setSn(sn);
        test.setUrl(url);
        // 没传时间戳或时间戳没到毫秒
        if (null == currentTime || String.valueOf(currentTime).length() != 13) {
            test.setCurrentTime(calendar.getTime().getTime());
        } else {
            test.setCurrentTime(currentTime);
        }
        test.setSeqNo(seqNo);
        test.setMd5Data(md5Data);
        test.setFileInfo(fileInfo);
        test.setFileInfoSize(Integer.parseInt(fileInfoSize));
        test.setFileType(Integer.parseInt(fileType));
        kafkaService.sendTestForDownLoadFile(test);
        return "test downloadfile";
    }

    /**
     * @Description: 文件上传接口.
     * @param {type} {type}
     * @return: "test UPloadfile"
     */
    @RequestMapping(value = "/testUpLoadFile", method = RequestMethod.GET)
    public String getUpLoadFile(String sn, String seqNo, String fileType, String localPath,
            @RequestParam(value = "currentTime", required = false) Long currentTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -9);
        UpLoadFileMo test = new UpLoadFileMo();
        test.setSn(sn);
        // 没传时间戳或时间戳没到毫秒
        if (null == currentTime || String.valueOf(currentTime).length() != 13) {
            test.setCurrentTime(calendar.getTime().getTime());
        } else {
            test.setCurrentTime(currentTime);
        }
        test.setSeqNo(seqNo);
        test.setFileType(Integer.parseInt(fileType));
        test.setLocalPath(localPath);
        kafkaService.sendTestForUpLoadFile(test);
        return "test UPloadfile";
    }

    /**
     * @Description: 组合远控接口(包含车窗EXT控制).
     * @param {type} {type}
     * @return: "test receive remote group control request from it"
     */
    @RequestMapping(value = "/testRmtGroup", method = RequestMethod.POST)
    public String testRmtGroup(@RequestBody RmtGroupRequestInfo rmtGroupRequestInfo) {
        // System.out.println(rmtGroupRequestInfo.getComd());
        logger.info("test remote group Ctrl!");
        String serialNumber = rmtGroupRequestInfo.getSn();
        Calendar calendar = Calendar.getInstance();
        if (rmtGroupRequestInfo.getEventTime() == 0) {
            rmtGroupRequestInfo.setEventTime(calendar.getTime().getTime());
        }
        kafkaService.sndMesToITForTemplate(KafkaMsgConstant.TOPIC_IT_RM_GROUP_REQ, rmtGroupRequestInfo, serialNumber);
        return "test receive remote group control request from it";
    }

    /**
     * @Description: 添加TBox接口.
     * @param {type} {type}
     * @return: 
     */
    @RequestMapping(value = "/insertNewTbox", method = RequestMethod.GET)
    public String insertInitTboxInfo(String sn, String iccid, String uuid, String mno, String simNumber) {
        logger.info("TBox({}) request  insertInitTboxInfo", sn);
        if (!testFlag)
            return "You can not do such operation in P envirenment!";

        ItRedisInfo itInfo = new ItRedisInfo();
        if (sn == null) {
            return "error:sn is null!";
        }
        if (iccid == null) {
            return "error:iccid is null!";
        }
        if (uuid == null) {
            return "error:uuid is null!";
        }
        // 新加入Tbox检查数据一致性问题：1.网关mysql是否有这个Tbox; 2.网关redis是否有这个Tbox； 3.IT
        // redis是否有这个redis；
        try {
            // 1. check gateway mysql
            TboxVo tboxInfo = tspPlatformClient.getTboxVo(sn);
            if (tboxInfo != null) {
                return "error: This Tbox is not new! MySQL of gateway has it!";
            }

            // 2.判断Redis是否有这个sn记录
            String info = redisAPI.getHash(RedisConstant.TBOX_INFO, sn);
            if (info != null) {
                // 有则删除记录
                return "error:This Tbox is not new! Redis of gateway has it! But MySQL of gateway doesnot have it!";
            }
            itInfo.setSn(sn);
            itInfo.setIccid(iccid);
            itInfo.setUuid(uuid);
            itInfo.setMno(mno);
            itInfo.setSimNumber(simNumber);
            ItRedisInfo ittboxInfo = new ItRedisInfo();
            ittboxInfo = redisAPI.getItValue(itInfo.getSn());
            if (ittboxInfo != null) {
                return "error:--IT redis already has such sn!" + JSONObject.toJSONString(ittboxInfo);
            }
            redisAPI.setItValue(itInfo.getSn(), itInfo);
            return "Insert success!";
        } catch (Exception e) {
            logger.error("insertNewTBox--TBox(SN:{}): error: {} ", sn, ThrowableUtil.getErrorInfoFromThrowable(e));
        }
        return "error:--request param is null!";
    }

    /**
     * @Description: 重置TBox状态.
     * @param {type} {type}
     * @return: 
     */
    @RequestMapping(value = "/inactiveTboxStatus", method = RequestMethod.GET)
    public String inactiveTboxStatus(String sn, String user) {
        logger.info("TBox({}) request  inactiveTboxStatus by {}", sn, user);
        if (!testFlag)
            return "You can not do such operation in P envirenment!";
        if (null == sn) {
            return "sn 不合法！";
        }
        if (null == user || user.length() <= 0) {
            return "用户不合法！";
        }
        try {

            // 删除网关 redis 及 mysql中对应的数据
            // 1。检查数据库中是否存在该SN
            TboxVo tboxInfo = tspPlatformClient.getTboxVo(sn);
            if (tboxInfo != null) {
                boolean inActiveResult = tspPlatformClient.resetTboxStatus(sn);
                if (!inActiveResult) {
                    return "error:--modify mysql error!";
                }
            }
            // 2。判断Redis是否有这个sn记录
            String info = redisAPI.getHash(RedisConstant.TBOX_INFO, sn);
            if (info != null) {
                // 有则删除记录
                redisAPI.removeHash(RedisConstant.TBOX_INFO, sn);
            }

        } catch (Exception e) {
            logger.error("inactiveTboxStatus Tbox({}) error:{}", sn, ThrowableUtil.getErrorInfoFromThrowable(e));
            return "error:--Failed!";
        }
        return "inactiveTboxStatus Success!";
    }

    /**
     * @Description: 修改TBox信息.
     * @param {type} {type}
     * @return: 
     */
    @RequestMapping(value = "/updateTboxInfo", method = RequestMethod.POST)
    public String updateTboxInfo(@RequestBody Tbox tbox) {
        try {
            String sn = tbox.getSn();
            if (StringUtils.isBlank(sn)) {
                return "sn 不合法！";
            }
            boolean updateTboxResult = tspPlatformClient.updateTboxBySn(tbox);
            if (!updateTboxResult) {
                return "error:--modify mysql error!";
            }
            // tboxService.updateTboxBySn(tbox);
            // 判断Redis是否有这个sn记录
            TboxVo info = JSONObject.parseObject(redisAPI.getHash(RedisConstant.TBOX_INFO, sn), TboxVo.class);
            if (info != null) {
                String iccid = tbox.getIccid();
                String simno = tbox.getSimno();
                String uuid = tbox.getUuid();
                if (StringUtils.isNotBlank(iccid)) {
                    info.setIccid(iccid);
                }
                if (StringUtils.isNotBlank(simno)) {
                    info.setSimno(simno);
                }
                if (StringUtils.isNotBlank(uuid)) {
                    info.setUuid(uuid);
                }
                redisAPI.setHash(RedisConstant.TBOX_INFO, sn,
                        JSONObject.toJSONString(info, SerializerFeature.WriteMapNullValue));
            }
            return "update TBox success";
        } catch (Exception e) {
            logger.error("updateTbox error:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
            return "updateTbox error";
        }
    }

    /**
     * @Description: 检查TBox激活状态.
     * @param {type} {type}
     * @return: 
     */
    @RequestMapping(value = "/checkTboxStatus", method = RequestMethod.GET)
    public String checkTboxStatus(String sn) {
        logger.info("TBox({}) request  checkTboxStatus", sn);
        String result = "Tbox在数据库中的状态：\n";
        if (sn.isEmpty()) {
            return "Sn 为空！";
        }
        // 查询IT状态
        try {
            ItRedisInfo itInfo = redisAPI.getItValue(sn);
            if (itInfo == null) {
                result += "IT:null\n";
            } else {
                result += "IT:" + itInfo.getDeviceLockStatus() + "\n";
            }
            TboxVo tboxInfo = tspPlatformClient.getTboxVo(sn);
            if (tboxInfo == null) {
                result += "Mysql:null\n";
            } else {
                result += "Mysql:" + tboxInfo.getStatus() + "\n";
            }
            String info = redisAPI.getHash(RedisConstant.TBOX_INFO, sn);
            if (info == null) {
                // 有则删除记录
                result += "Redis缓存：null\n";
            } else {
                int index = info.indexOf("status");
                result += "Redis缓存：" + info.substring(index + 8, index + 9) + "\n";
            }
        } catch (Exception e) {
            logger.error("TBox(SN:{}),获取激活状态异常~", sn);
            return result += " 异常！\n";
        }
        return result;
    }

}
