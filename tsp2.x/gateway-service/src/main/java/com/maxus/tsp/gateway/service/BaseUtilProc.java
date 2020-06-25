/**
 * TspServiceProc.java Create on 2018年11月22日
 * Copyright (c) 2017年7月17日 by 上汽集团商用车技术中心
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>
 * @version 1.0
 */
package com.maxus.tsp.gateway.service;

import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Description 所有IT下发控制指令业务流程处理基类
 * @Date 2019/1/22 14:14
 **/
public class BaseUtilProc {

    private static final Logger logger = LogManager.getLogger(BaseUtilProc.class);

    protected String key =  RedisConstant.CAR_REMOTE_CTRL_REQ;

    protected RedisAPI redisAPI ;

//    /**
//     * 传入本次远控请求
//     */
    public BaseUtilProc(RedisAPI redisAPI){
        this.redisAPI = redisAPI;
    }
    
    /**
     * @Title: 根据操作指令确认是否有该指令引起的唤醒操作
     * @param tboxSn
     * @return
     * @author zhuna
     * @date 2018年12月4日
     */
    public boolean existWakeUp(String tboxSn) {
		return redisAPI.hasKey(RedisConstant.WAKE_UP +  "_" + tboxSn);
	}
    
    /**
     * redis添加  正在唤醒记录
     * @param tboxsn
	 * @param operateName 引发唤醒的远程操作名称
     * @author zhuna
     * @date 2018年12月4日
     */
    public void addWakeUp(String tboxsn, String operateName) {
		// 设置75s超时,清楚redis wake up记录
		try {
			Date date = new Date();
			String currentTime = DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
			redisAPI.setValue(RedisConstant.WAKE_UP + "_" + tboxsn, operateName + "_" +currentTime, OperationConstant.WAKEUP_WAIT_TIME_SEC, TimeUnit.SECONDS);
		} catch (Exception ex) {
			logger.error("Redis connection error, TBox(SN:{}) can't do addWakeUp! The error:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
		}		
	}
    
    /**
     * redis清除  正在唤醒记录
     * @param tboxsn
     * @author zhuna
     * @date 2018年12月4日
     */
    public void removeWakeUp(String tboxsn) {		
		try {
			redisAPI.delete(RedisConstant.WAKE_UP + "_" + tboxsn);
		} catch (Exception ex) {
			logger.error("Redis connection error, TBox(SN:{}) can't do removeWakeUp! The error:{} ", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
		}		
	}
	/**
	 * @method      existCommandSend
	 * @description   是否存在一条正在下发的指令
	 * @param serialNumber
	 * @return
	 * @author      zhuna
	 * @date        2019/2/15 15:49
	 */
	public boolean existCommandSend(String serialNumber){
    	try{
			return redisAPI.hasKey(RedisConstant.COMMAND_SEND + "_" + serialNumber);
		}catch (Exception ex){
			logger.error("Redis hasKey error, TBox(SN:{}) can't do existCommandSend! The error:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));
    		return false;
		}
	}

	/**
	 * @method      addCommandSend
	 * @description   添加一条正在下发的指令
	 * @param serialNumber
	 * @param value
	 * @return
	 * @author      zhuna
	 * @date        2019/2/15 15:49
	 */
	public boolean addCommandSend(String serialNumber, String value){
    	try {
			Date date = new Date();
			String currentTime = DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
    		return redisAPI.setValueWithEspireTime(RedisConstant.COMMAND_SEND + "_" + serialNumber, value + "_" + currentTime,  OperationConstant.REMOTECONTROL_RESP_EXPIRED_TIME, TimeUnit.SECONDS);
		}catch (Exception ex){
    		logger.error("Redis setValue error, TBox({}) can't do addCommandSend! The error:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));
    		return false;
		}
	}

	/**
	 * @method      removeCommandSend
	 * @description   下发完成，删除指令
	 * @param serialNumber
	 * @return
	 * @author      zhuna
	 * @date        2019/2/15 15:49
	 */
	public boolean removeCommandSend(String serialNumber){
		try {
			return redisAPI.removeKey(RedisConstant.COMMAND_SEND + "_" + serialNumber);
		}catch (Exception ex){
			logger.error("Redis removeValue error, TBox({}) can't do addCommandSend! The error:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));
			return false;
		}
	}

    /**
     * @Title:tbox是否已经存在一条远程控制指令了
     * @param tboxsn
     * @return
     * @author zhuna
     * @date 2018年12月4日
     */
    public  boolean existTboxRemoteCtrl(String tboxsn) {
		return redisAPI.hasKey(key, tboxsn);
	}
    
    
    /**
     * 向tbox发送报文
     * @param channel 通信socket
     * @param sendMessage 报文
     * @param tboxsn 序列号
     * @param message 对应kafka信息
     * @param operName 操作名称
     */
    protected void sendMsgToChannel(Channel channel, byte[] sendMessage, String tboxsn, String message, String operName) {
        try {
            if (channel.isWritable()) {
                //若状态为可写入说明待发送数据量并未堆积超限
                ChannelFuture future = channel.writeAndFlush(sendMessage);
				if (logger.isInfoEnabled()) {
					future.addListener(new ChannelFutureListener() {
						public void operationComplete(final ChannelFuture future)
								throws Exception {
							if (future.isSuccess()) {
								logger.info("本次TBox(SN:{})下发{}成功:{}", tboxsn, operName, message);
							} else {
								logger.info("本次TBox(SN:{})下发{}失败:{}", tboxsn, operName, message);
							}
						}
					});
				}
            } else {
                //如果Channel不是可写入状态，说明待发送数据量超过堆积超限，需要进行同步，否则内存溢出
                try {
					channel.writeAndFlush(sendMessage).sync();
					logger.info("网关channel发送进行同步，本次TBox(SN:{})下发{}推送成功:{}", tboxsn, operName, message);
				} catch (InterruptedException ex) {
					logger.error("网关channel发送同步过程中发生异常:{}，本次TBox(SN:{})下发:{}失败:{}",
							ThrowableUtil.getErrorInfoFromThrowable(ex), tboxsn, operName, message);
				}
            }
        } catch (Exception ex) {
			//发送的时候不活跃，则失败
			logger.error("本次TBox(SN:{})下发{}失败:{}，发生异常:{}", tboxsn, operName, message,
					ThrowableUtil.getErrorInfoFromThrowable(ex));
		}
    }

    /**
     * 判断远程配置消息是否过期
     *
     * @param offsetTime
     * @return
     */
    protected String getExpiredTimeByCurTimeAndOffset(int offsetTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 获取当前时间
        Calendar calendar = Calendar.getInstance();
        String limitTime = "";
        try {
            // 计算偏移多少秒前的时间
            calendar.add(Calendar.SECOND, -offsetTime);
            limitTime = sdf.format(calendar.getTime());
        } catch (Exception ex) {
			logger.error("获取过期时间异常:{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
			return "";
		}
        return limitTime;
    }
}
