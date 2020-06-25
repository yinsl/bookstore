package com.maxus.tsp.gateway.mq.kafka;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.enums.ResultStatus;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.platform.service.model.AppJsonResult;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @ClassName  BaseListener
 * @Description 
 * @author zhuna
 * @date 2018年12月4日
 */
public class BaseListener {
	
	private static final Logger logger = LogManager.getLogger(BaseListener.class);

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
                    logger.error("网关channel发送同步过程中发生异常:{}，本次TBox(SN:{})下发{}失败:{}",
                            ThrowableUtil.getErrorInfoFromThrowable(ex), tboxsn, operName, message);
                }
            }
        } catch (Exception ex) {
            //发送的时候不活跃，则失败
            logger.error("本次TBox(SN:{})下发{}失败:{}，发生异常:{}", tboxsn, operName, message,
                    ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }
    // 判断传入的long型时间与当前时间相比
    public static boolean timeDifference(long longTime) throws ParseException {
        long end = System.currentTimeMillis();
        if ((end - longTime) / OperationConstant.MS_IN_A_SECOND < OperationConstant.differ) {
            return true;
        }
        return false;
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

    protected AppJsonResult getReturnAppJsonResultRVM(String tboxresult)
    {
        String status = modifiedStatusByPrefix(tboxresult);
        AppJsonResult curResult = JSONObject.parseObject(tboxresult, AppJsonResult.class);
        Object data = null;
        if (curResult != null) {
            data = curResult.getData();
            if(data != null && data.toString().equals(""))
            {
                data = null;
            }
        }
        if (!ResultStatus.SUCCESS.getCode().equals(status)) {
            return new AppJsonResult(ResultStatus.getResultStatus(status), data);
        }
        return JSONObject.parseObject(tboxresult, AppJsonResult.class);
    }

    // 获取返回结果JSON中的status值，并且修正其对应结果
    private String modifiedStatusByPrefix(String tboxresult) {
        AppJsonResult curresult = JSONObject.parseObject(tboxresult, AppJsonResult.class);
        String status = curresult.getStatus();

        if (ResultStatus.TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG.getCode().equals(status)) {
            // 当老status code =2，则直接修正为12090
            status = ResultStatus.RM_TBOX_NOT_LOGIN_AFTER_SEND_WAKEUP_MSG.getCode();
        } else if (!ResultStatus.SUCCESS.getCode().equals(status) && status.length() == 1) {
            // 当非成功情况，且返回长度为1的，重新构造新版本远程控制返回的status值
            status = OperationConstant.PREFIX_REMOTE_CTRL_STATUS_FOR_ASYN_INTERFACE + status;
        }
        return status;
    }

}
