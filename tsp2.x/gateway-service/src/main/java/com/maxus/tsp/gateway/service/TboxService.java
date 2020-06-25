/**
 * 处理数据，包括报文数据，及redis失效与数据库数据库进行同步的方案
 * Copyright (c) 2017年7月14日 by 上汽集团商用车技术中心
 *
 * @author 余佶
 * @version 1.0
 */
package com.maxus.tsp.gateway.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.ota.TimeLogUtil;
import com.maxus.tsp.gateway.ota.TspServiceProc;
import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
import com.maxus.tsp.platform.service.model.vo.ItRedisInfo;

@Service
public class TboxService {

    private Logger logger = LogManager.getLogger(TboxService.class);
    // redis服务，用于与it进行交互
    @Autowired
    private RedisAPI redisAPI;
    // 数据库服务
    @Autowired
    private TspPlatformClient tspPlatformClient;

    /**
     * 公共方法用于获取tbox对应的vin信息
     *
     * @param tboxSn
     * @return
     */
    public String getVINForTbox(String tboxSn) {
        // 从redis中获取tbox对应VIN号，redis无法连接或查询不到时，从数据库中读取
    	
    	String logName ="OTAResolveServerHandler.resolve -> procOTA -> ProcReportCan.inDataGPSCAN -> TboxService.getVINForTbox: ";
    	TimeLogUtil.log(logName+"方法开始");
        String tboxBindCarVIN = null;
        // 访问内存中的tbox与vin对应关系(69系列，点检登陆时会提供,优先使用该信息)
        tboxBindCarVIN = TspServiceProc.loginCarInfo.get(tboxSn);
        if (tboxBindCarVIN != null && tboxBindCarVIN.length() == OperationConstant.VIN_LENGTH) {
            redisAPI.setHash(RedisConstant.VIN_FOR_SN, tboxSn, tboxBindCarVIN);
        }
        // 如果内存没有提供，则先访问it的redis
        if (tboxBindCarVIN == null || tboxBindCarVIN.length() < OperationConstant.VIN_LENGTH) {
            tboxBindCarVIN = getVinForITTbox(tboxSn);
            // 如果获取到vin，将该tbox对应vin信息更新至自建的redis
            if (tboxBindCarVIN != null && tboxBindCarVIN.length() == OperationConstant.VIN_LENGTH) {
                redisAPI.setHash(RedisConstant.VIN_FOR_SN, tboxSn, tboxBindCarVIN);
            }
        }
        TimeLogUtil.log(logName+" line 62 it redis");
        // 本地redis ok
        if (StringUtils.isBlank(tboxBindCarVIN)) {
            tboxBindCarVIN = redisAPI.getHash(RedisConstant.VIN_FOR_SN, tboxSn);
        }
        TimeLogUtil.log(logName+" line 67 redis");
        if (StringUtils.isBlank(tboxBindCarVIN) || tboxBindCarVIN.length() < OperationConstant.VIN_LENGTH) {
            logger.warn("TBox(SN:{}): 无法获取当前TBox对应车架号（VIN）", tboxSn);
            tboxBindCarVIN = "";
            redisAPI.setHash(RedisConstant.VIN_FOR_SN, tboxSn, tboxBindCarVIN);
        } else {
            logger.info("TBox(SN:{}): 当前TBox绑定的对应车架号（VIN）为:{}", tboxSn, tboxBindCarVIN);
        }
        TimeLogUtil.log(logName+"方法结束  set redis");
        return tboxBindCarVIN;
    }

    /**
     * 从tbox登记的数据库中的信息里获得iccid
     *
     * @param tboxSn
     * @return
     */
    public String getIccid(String tboxSn) {
        String iccid = "";
        try {
            iccid = TspServiceProc.loginICCIDInfo.get(tboxSn);
            if (iccid == null || StringUtils.isBlank(iccid.trim())) {
                logger.info("无法从内存中获取Tbox({})到iccid信息,尝试在数据库中获取", tboxSn);
                iccid = tspPlatformClient.getTboxVo(tboxSn).getIccid();
            }
        } catch (Exception ex) {
            logger.error("无法正常获取Tbox({})到iccid信息,可能影响发短信,异常为:{}",
                    tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
        return iccid;
    }

    /**
     * @param uuid
     */
    public boolean setRedisRMTTimeoutKey(String uuid) {
        //开启定时器，并设置redis全局标志位
        return redisAPI.setHash(RedisConstant.RMT_TIMEOUT, uuid, "1");
    }

    /**
     * 移除掉超时标志位
     *
     * @param uuid
     */
    public boolean rmRedisRMTTimeoutKey(String uuid) {
        //移除掉超时标志位
        return redisAPI.removeHash(RedisConstant.RMT_TIMEOUT, uuid);
    }

    /**
     * 检查是否存在超时定时任务需要处理
     *
     * @param uuid
     * @return
     */
    public boolean existTimeoutKey(String uuid) {
        return redisAPI.hasKey(RedisConstant.RMT_TIMEOUT, uuid);
    }

    /**
     * 返回车架号
     *
     * @param tboxSn
     * @return
     */
    public String getVinForITTbox(String tboxSn) {
        ItRedisInfo itInfo = new ItRedisInfo();
        itInfo = redisAPI.getItValue(tboxSn);
        if (itInfo == null) {
            logger.warn("无法根据tbox序列号{}在RVM redis中查找到数据", tboxSn);
            return null;
        }
        try {
            return itInfo.getVin();
        } catch (Exception e) {
            logger.error("根据SN:{}查询VIN号发生异常，原因为:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(e));
            return null;
        }
    }

    /**
     * @param tokenId
     * @return
     * @Description 返回token
     */
    public String getTokenForITTbox(String tokenId) {
        String token = null;
        try {
            token = redisAPI.getItValueString(tokenId);
        } catch (Exception e) {
            logger.error("根据tokenId:{}在RVM redis中查找到数据出现异常:{}", tokenId, ThrowableUtil.getErrorInfoFromThrowable(e));
        }
        if (token == null) {
            logger.warn("无法根据tokenId:{}在RVM redis中查找到数据", tokenId);
            return "";
        }
        return token;
    }


}
