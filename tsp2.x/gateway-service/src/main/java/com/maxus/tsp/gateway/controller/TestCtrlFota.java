package com.maxus.tsp.gateway.controller;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.KafkaMsgConstantFota;
import com.maxus.tsp.gateway.common.model.BaseRmtCtrlItReq;
import com.maxus.tsp.gateway.common.model.fota.*;
import com.maxus.tsp.gateway.service.KafkaService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;

/**
 * @ClassName TestCtrlFota
 * @Description OTA升级功能测试接口
 * @Author wqgzf
 * @Data 2019年2月11日下午1:58:45
 * @Version 1.0
 */
@RestController
public class TestCtrlFota {

    private final Logger logger = LogManager.getLogger(TestCtrlFota.class);

    @Autowired
    KafkaService kafkaService; // 网关本身使用的kafka

    /**
     * @param
     * @return
     * @method FOTA测试接口 模拟IT回复获取ECU列表
     * @description
     * @author zhuna
     * @date 2019/1/29 16:18
     */
    @RequestMapping(value = "/testEcuList", method = RequestMethod.GET)
    public String testRegisterRt(String sn, int isUpdate, int JsonLength, String ECUList, @RequestParam(value = "eventTime", required = false) Long eventTime) {
        EcuVersionItResultInfo rt = new EcuVersionItResultInfo();
        Calendar calendar = Calendar.getInstance();
        if (isUpdate == 0) {
            if (!(JsonLength == 0 && StringUtils.isBlank(ECUList))) {
                rt.setSn(sn);
                rt.setCmd("GET_ECU_LIST");
                rt.setValue(isUpdate);
                rt.setParamSize(JsonLength);
                rt.setParam(ECUList);
                rt.setSeqNo("2018032216343098712345");
                //没传时间戳或时间戳没到毫秒
                if (null == eventTime || String.valueOf(eventTime).length() != 13) {
                    rt.setEventTime(calendar.getTime().getTime());
                } else {
                    rt.setEventTime(eventTime);
                }
                kafkaService.transferItData(KafkaMsgConstantFota.TOPIC_IT_INFORMATION_DATA_RESULT, rt, sn);
            } else {
                logger.error("IT返回的获取ECU列表信息格式错误");
            }
        } else {
            if (2 * JsonLength == ECUList.length()) {
                rt.setSn(sn);
                rt.setCmd("GET_ECU_LIST");
                rt.setValue(isUpdate);
                rt.setParamSize(JsonLength);
                rt.setParam(ECUList);
                rt.setSeqNo("2018032216343098712345");
                //没传时间戳或时间戳没到毫秒
                if (null == eventTime || String.valueOf(eventTime).length() != 13) {
                    rt.setEventTime(calendar.getTime().getTime());
                } else {
                    rt.setEventTime(eventTime);
                }
                kafkaService.transferItData(KafkaMsgConstantFota.TOPIC_IT_INFORMATION_DATA_RESULT, rt, sn);
            } else {
                logger.warn("IT返回的获取ECU列表信息与内容不匹配");
            }
        }
        return JSONObject.toJSONString(rt);
    }

    /**
     * @param sn
     * @param id
     * @param fileListLength
     * @param fileList
     * @param seqNo
     * @return
     * @Description 请求版本更新接口测试
     * @Data 2019年2月13日上午10:30:12
     */
    @RequestMapping(value = "/testReportVersion", method = RequestMethod.GET)
    public String testReportVersion(String sn, int id, int fileListLength, String fileList, String seqNo, @RequestParam(value = "eventTime", required = false) Long eventTime) {
        EcuVersionItResultInfo rv = new EcuVersionItResultInfo();
        Calendar calendar = Calendar.getInstance();
        if (id == 0) {
            if (fileListLength == 0 && StringUtils.isBlank(fileList)) {
                rv.setSn(sn);
                rv.setCmd("REPORT_VERSION");
                rv.setValue(id);
                rv.setParamSize(fileListLength);
                rv.setParam(fileList);
                rv.setSeqNo(seqNo);
                //没传时间戳或时间戳没到毫秒
                if (null == eventTime || String.valueOf(eventTime).length() != 13) {
                    rv.setEventTime(calendar.getTime().getTime());
                } else {
                    rv.setEventTime(eventTime);
                }
                kafkaService.transferItData(KafkaMsgConstantFota.TOPIC_IT_INFORMATION_DATA_RESULT, rv, sn);
            } else {
                logger.error("IT返回的请求版本更新信息格式错误！");
            }
        } else {
            if (2 * fileListLength == fileList.length()) {
                rv.setSn(sn);
                rv.setCmd("REPORT_VERSION");
                rv.setValue(id);
                rv.setParamSize(fileListLength);
                rv.setParam(fileList);
                rv.setSeqNo(seqNo);
                //没传时间戳或时间戳没到毫秒
                if (null == eventTime || String.valueOf(eventTime).length() != 13) {
                    rv.setEventTime(calendar.getTime().getTime());
                } else {
                    rv.setEventTime(eventTime);
                }
                kafkaService.transferItData(KafkaMsgConstantFota.TOPIC_IT_INFORMATION_DATA_RESULT, rv, sn);
            } else {
                logger.error("IT返回的请求版本更新信息长度和内容不匹配");
            }
        }
        return JSONObject.toJSONString(rv);
    }

    /**
     * @return java.lang.String
     * @Description FOTA版本查询测试接口
     * @Date 2019/1/24 16:34
     * @Param [versionQueryRequestInfo]
     **/
    @RequestMapping(value = "/testVersionQuery", method = RequestMethod.POST)
    public String versionQuery(@RequestBody BaseRmtCtrlItReq versionQueryRequestInfo) {
        logger.info("Test FOTA Version Query!");
        if (versionQueryRequestInfo.getEventTime() == 0) {
            Calendar calendar = Calendar.getInstance();
            versionQueryRequestInfo.setEventTime(calendar.getTime().getTime());
        }
        logger.info("TBox(SN:{})版本查询测试接口入参:{}", versionQueryRequestInfo.getSn(), JSONObject.toJSONString(versionQueryRequestInfo));
        kafkaService.sendTestForVersionQuery(versionQueryRequestInfo);
        return "Test FOTA Version Query Success!";
    }

    /**
     * @return java.lang.String
     * @Description FOTA版本升级测试接口
     * @Date 2019/1/24 16:41
     * @Param [versionUpgradeReqInfo]
     **/
    @RequestMapping(value = "/testVersionUpgrade", method = RequestMethod.POST)
    public String versionUpgrade(@RequestBody VersionUpgradeReqInfo versionUpgradeReqInfo) {
        logger.info("Test FOTA Version Upgrade!");
        if (versionUpgradeReqInfo.getEventTime() == 0) {
            Calendar calendar = Calendar.getInstance();
            versionUpgradeReqInfo.setEventTime(calendar.getTime().getTime());
        }
        logger.info("TBox(SN:{})版本升级测试接口入参:{}", versionUpgradeReqInfo.getSn(), JSONObject.toJSONString(versionUpgradeReqInfo));
        kafkaService.transferItData(KafkaMsgConstantFota.TOPIC_IT_VERSION_UPGRADE_REQ, versionUpgradeReqInfo, versionUpgradeReqInfo.getSn());
        return "Test FOTA Version Upgrade Success!";
    }


    /**
     * @param upgradeResumeReqInfo
     * @return
     * @Description FOTA版本继续升级测试接口
     * @Data 2019年2月11日下午1:58:57
     */
//    @RequestMapping(value = "/testUpgradeResume", method = RequestMethod.POST)
//    public String upgradeResume(@RequestBody UpgradeResumeReqInfo upgradeResumeReqInfo) {
//        logger.info("Test FOTA Upgrade Resume!");
//        if (upgradeResumeReqInfo.getEventTime() == 0) {
//            Calendar calendar = Calendar.getInstance();
//            upgradeResumeReqInfo.setEventTime(calendar.getTime().getTime());
//        }
//        logger.info("TBox(SN:{})继续升级接口入参:{}", upgradeResumeReqInfo.getSn(), JSONObject.toJSONString(upgradeResumeReqInfo));
//        kafkaService.transferItData(KafkaMsgConstantFota.TOPIC_IT_UPGRADE_RESUME_REQ, upgradeResumeReqInfo, upgradeResumeReqInfo.getSn());
//        return "Test FOTA Upgrade Resume Success!";
//    }

    /**
     * @return java.lang.String
     * @Description FOTA证书更新测试接口
     * @Date 2019/1/31 14:47
     * @Param [certificationUpgradeReqInfo]
     **/
    @RequestMapping(value = "/testCertificationUpgrade", method = RequestMethod.POST)
    public String certificationUpgrade(@RequestBody CertificationUpgradeReqInfo certificationUpgradeReqInfo) {
        logger.info("Test FOTA Certification Upgrade!");
        if (certificationUpgradeReqInfo.getEventTime() == 0) {
            Calendar calendar = Calendar.getInstance();
            certificationUpgradeReqInfo.setEventTime(calendar.getTime().getTime());
        }
        logger.info("TBox(SN:{})证书更新测试接口入参：{}", certificationUpgradeReqInfo.getSn(), JSONObject.toJSONString(certificationUpgradeReqInfo));
        kafkaService.transferItData(KafkaMsgConstantFota.TOPIC_IT_CERTIFICATION_UPGRADE_REQ, certificationUpgradeReqInfo, certificationUpgradeReqInfo.getSn());
        return "Test FOTA Certification Upgrade Success!";
    }

    /**
     * @return java.lang.String
     * @Description FOTA车主AVN端是否同意升级测试接口
     * @Date 2019/2/14 13:20
     * @Param [agreeUpgradeReqInfo]
     **/
    @RequestMapping(value = "/testAgreeUpgrade", method = RequestMethod.POST)
    public String agreeUpgrade(@RequestBody AgreeUpgradeRespInfo agreeUpgradeRespInfo) {
        logger.info("Test FOTA Agree Upgrade!");
        if (agreeUpgradeRespInfo.getEventTime() == 0) {
            Calendar calendar = Calendar.getInstance();
            agreeUpgradeRespInfo.setEventTime(calendar.getTime().getTime());
        }
        logger.info("TBox(SN:{})车主avn端是否同意升级测试接口入参：{}", agreeUpgradeRespInfo.getSn(), JSONObject.toJSONString(agreeUpgradeRespInfo));
        kafkaService.transferItData(KafkaMsgConstantFota.TOPIC_IT_AGREE_UPGRADE_RESULT, agreeUpgradeRespInfo, agreeUpgradeRespInfo.getSn());
        return "Test FOTA agree Upgrade Success!";
    }

    @PostMapping(value = "/testProgressReport")
    public String testProgressReport(@RequestBody ProgressItRespInfo progressItRespInfo) {
        logger.info("Test Fota Progress Report!");
        if (progressItRespInfo.getEventTime()==0) {
            progressItRespInfo.setEventTime(System.currentTimeMillis());
        }
        logger.info("当前进度上报结果回复内容为:{}", JSONObject.toJSONString(progressItRespInfo));
        kafkaService.transferItData(KafkaMsgConstantFota.TOPIC_IT_PROGRESS_REPORT_RESULT, progressItRespInfo, progressItRespInfo.getSn());
        return "Test Fota Progress Report!";
    }

}
