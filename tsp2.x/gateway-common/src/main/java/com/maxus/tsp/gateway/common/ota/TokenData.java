package com.maxus.tsp.gateway.common.ota;

import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTAConstant;
import com.maxus.tsp.gateway.common.constant.OTAForwardCommand;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @ClassName TokenData
 * @Description 透传获取token结果返回TBox的组包类
 * @Author zijhm
 * @Date 2019/1/15 9:19
 * @Version 1.0
 **/
public class TokenData {
    //日志
    private static Logger logger = LogManager.getLogger(TokenData.class);

    /**
     * @return byte[]
     * @Description 根据token获取的结果组成对应的返回报文
     * @Date 2019/1/15 10:15
     * @Param [ret, token]
     **/
    public byte[] getTokenData(String ret, String token, String serialNumber) {
        byte[] outData;
        byte[] command = ByteUtil.short2Byte(OTAForwardCommand.CMD_DOWN_TOKEN.getCode());
        //定义token获取失败时的返回报文param
        byte[] errData = new byte[]{(byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00};
        try {
            if (StringUtils.isBlank(ret) && !StringUtils.isBlank(token)) {
                //获取token成功
                byte[] tokenByte = ByteUtil.stringToBytes(token);
                byte[] tokenLength = ByteUtil.int2Byte(token.length());
                byte[] paramSize = ByteUtil.int2Byte(token.length() + 2);
                outData = new byte[tokenByte.length + OTAConstant.ERROR_OUT_DATA_SIZE];
                System.arraycopy(command, 0, outData, 0, 2);
                System.arraycopy(paramSize, 2, outData, 2, 2);
                System.arraycopy(tokenLength, 2, outData, 4, 2);
                System.arraycopy(tokenByte, 0, outData, 6, tokenByte.length);
                logger.info("TBox(SN:{})获取token成功, ret:{}, token:{}, outData:{}", serialNumber, ret, token, ByteUtil.byteToHex(outData));
            } else {
                //获取token失败
                outData = new byte[OTAConstant.ERROR_OUT_DATA_SIZE];
                System.arraycopy(command, 0, outData, 0, 2);
                System.arraycopy(errData, 0, outData, 2, 4);
                logger.warn("TBox(SN:{})获取token失败, ret:{}, token:{}, outData:{}", serialNumber, ret, token, ByteUtil.byteToHex(outData));
            }
        } catch (Exception e) {
            outData = new byte[OTAConstant.ERROR_OUT_DATA_SIZE];
            System.arraycopy(command, 0, outData, 0, 2);
            System.arraycopy(errData, 0, outData, 2, 4);
            logger.error("TBox(SN:{})组包过程发生异常，异常信息:{}",serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
        }
        return outData;
    }
}
