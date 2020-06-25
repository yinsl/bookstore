/**
 * GlobalSessionChannl.java Create on 2017年7月13日
 * Copyright (c) 2017年7月13日 by 上汽集团商用车技术中心
 *
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>
 * @version 1.0
 */
package com.maxus.tsp.gateway.common.ota;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.gateway.common.constant.OTARemoteCommand;


/**
 * @ClassName: GlobalSessionChannl.java
 * @Description:定义网关访问自身redis的业务接口
 * @author 任怀宇
 * @version V1.0
 * @Date 2017年7月13日 下午3:31:27
 */
public class GlobalSessionChannel {

    private static Logger logger = LogManager.getLogger(GlobalSessionChannel.class);

    /**
     * 保存当前会话aes key信息
     */
    private static Map<String, String> aesKeyMap = new ConcurrentHashMap<>();

    /**
     * 保存当前网关节点的编号
     */
    private static String localGatewayInstanceUUID = UUID.randomUUID().toString();

    public static RedisAPI redisAPI;
  
    public static String getUUIDforGatewayInstance() {
        return localGatewayInstanceUUID;
    }
    
    public static boolean existCurrentChannelInfo(String tboxsn, String curChannelInfo) {
        boolean result = false;
        if (redisAPI.hasKey(RedisConstant.TBOX_CHANNEL, tboxsn)) {
            result = redisAPI.getHash(RedisConstant.TBOX_CHANNEL, tboxsn)
                    .equalsIgnoreCase(getUUIDforGatewayInstance() + curChannelInfo);
        }
        logger.info(
                "This tbox({}) have channel:{}", tboxsn, getUUIDforGatewayInstance() + curChannelInfo + ":" + result);
        return result;
    }

    /**
     * @Title: getRemoteCtrl
     * @Description: 根据tboxsn查找远程控制指令
     * @param: @param
     *             tboxSn
     * @param: @return
     * @return: int
     * @throws @author
     *             fogmk
     * @Date 2017年7月18日 上午11:23:15
     */
    public static RemoteCtrlInfo getRemoteCtrl(String tboxSn) {
        // return remoteCtrlMap.get(tboxSn);
        RemoteCtrlInfo remoteCtrl = new RemoteCtrlInfo();
        //if (existTboxRemoteCtrl(tboxSn)) {
        if (redisAPI.hasKey(RedisConstant.COMMAND_SEND + "_" + tboxSn)) {
            //根据key获取远控排他value值
            String value = redisAPI.getValue(RedisConstant.COMMAND_SEND + "_" + tboxSn);
            //获取key值不为空
            if (StringUtils.isNoneBlank(value)) {
                String[] values = value.split("_");
                try {
                    remoteCtrl.remoteCmd = OTARemoteCommand.valueOf(values[0]);
                }catch (Exception e){
                    logger.info("TBox({})未在Redis中找到8005远控指令,目前存在指令为：{}", tboxSn, values[0]);
                    return null;
                }

                return remoteCtrl;
            } else {
                return null;
            }
            //remoteCtrl.remoteCmd = OTARemoteCommand.valueOf(redisAPI.getHash(RedisConstant.CAR_REMOTE_CTRL_REQ, tboxSn));
        } else {
            return null;
        }
    }

    /**
     * @Title: getAESKey
     * @Description: 获取当前会话AESkey
     * @param: @param
     *             tboxSn
     * @param: @return
     * @return: String
     * @throws @author
     *             余佶
     * @Date 2017年8月3日 上午10:48:37
     */
    public static String getAESKey(String tboxsn) {
    	//redisAPI.getHash(RedisConstant.SECURITY_KEY, tboxsn);
        return aesKeyMap.get(tboxsn);
    }

}
