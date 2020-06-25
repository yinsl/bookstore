package com.maxus.tsp.gateway.gb;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.enums.CommandIdentifierEnum;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTAEncrptMode;
import com.maxus.tsp.gateway.service.TboxService;
import com.maxus.tsp.platform.service.model.vo.GBLoginNo;
import com.maxus.tsp.platform.service.model.vo.RealTimeDataOperation;
@Component
public class GBDataProcessing {
	// 记录日志
	private static Logger logger = LogManager.getLogger(GBDataProcessing.class);
	// static TspPlatformClient tspPlatformClient;

	@Autowired
	 GBNettyClient gbNettyClient;
	@Autowired
	TboxService tboxService;
	static String recvMsg = "";
	
	@Autowired
	private RedisAPI redisAPI;
	
	/**
     * @Title: setGbCarLoginNo
     * @Description: set车辆登录流水号及日期
     * @param: @param
     * @return:
     * @throws @author
     *             胡宗明
     * @Date 2018年4月13日 上午11:22:43
     */
    public void setGbCarLoginNo(String gbno, Map<String, String> map) {
        try {
            redisAPI.setHashAll(gbno, map);
        } catch (Exception ex) {
            logger.error("Redis connection error, can't do getGbCarLoginNo:{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
    }
    
    /**
     * @Title: getGbCarLoginNo
     * @Description: 获取车辆登录流水号及日期
     * @param: @param
     * @return: Map<String ,   String>
     * @throws @author
     *             胡宗明
     * @Date 2018年4月13日 上午11:22:43
     */
    public GBLoginNo getGbCarLoginNo(String sn) {
        try {
            GBLoginNo carLoginNo = JSONObject.parseObject(
                    (String) redisAPI.getHash(RedisConstant.GB_CAR_LOGIN_NO, sn), GBLoginNo.class);

            return carLoginNo;
        } catch (Exception ex) {
            logger.error("Redis connection error, can't do getGbCarLoginNo:{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
        }
        return null;
    }
	
	/**
	 * 存储每辆电动车登陆国家平台时的流水号及日期
	 *
	 * @param
	 * @return
	 */
	public void setGbCarLoginNo(String sn) {
		try {
			GBLoginNo gBLoginNo = new GBLoginNo();
			Map<String, String> gbCarLoginNomap1 = new HashMap<String, String>();
			Map<String, String> gbCarLoginNomap2 = new HashMap<String, String>();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
			String now = df.format(new Date());// new Date()为获取当前系统时间
			gBLoginNo =  getGbCarLoginNo(sn);
			
			gbCarLoginNomap1.put(sn, JSON.toJSONString(new GBLoginNo(1,now)));

			if (gBLoginNo != null) {

				if (gBLoginNo.getDate().equals(now) && gBLoginNo.getNo() < RedisConstant.KING_NO) {
					logger.info("车辆登入日期相同，no小于" + RedisConstant.KING_NO);
					gbCarLoginNomap2.put(sn, JSON.toJSONString(new GBLoginNo(gBLoginNo.getNo()+1,now)));
					setGbCarLoginNo(RedisConstant.GB_CAR_LOGIN_NO, gbCarLoginNomap2);

				} else if (!gBLoginNo.getDate().equals(now)) {
					logger.info("车辆登入日期不同");
					setGbCarLoginNo(RedisConstant.GB_CAR_LOGIN_NO, gbCarLoginNomap1);
				} else if (gBLoginNo.getNo() >= RedisConstant.KING_NO) {
					logger.info("车辆登入日期不在规定范围内" + gBLoginNo.getNo());
					setGbCarLoginNo(RedisConstant.GB_CAR_LOGIN_NO, gbCarLoginNomap1);
				}
			} else {
				logger.info("gBLoginNo为null");
				setGbCarLoginNo(RedisConstant.GB_CAR_LOGIN_NO, gbCarLoginNomap1);
			}
		} catch (NumberFormatException e) {
			logger.error("存储每辆电动车登陆国家平台时因发生异常失败，原因:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
		}
	}
	
	/**
     * @Title: getGbCarLoginNo
     * @Description: set流水号及日期
     * @param: @param
     * @return: GBLoginNo
     * @throws @author
     *             胡宗明
     * @Date 2018年4月13日 上午11:22:43
     */
    public boolean setGbPlatformLoginNo(String gbno, GBLoginNo gBLoginNo) {

        try {
            redisAPI.setGbValue(gbno, gBLoginNo);
        } catch (Exception ex) {
            logger.error("Redis connection error, can't do getGbCarLoginNo:{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
            return false;
        }
        return true;
    }
	
	/**
	 * 存储平台登陆国家平台时的流水号及日期
	 *
	 * @param
	 * @return
	 */
	public void setGbPlatformLoginNo() {
		GBLoginNo gBLoginNo = new GBLoginNo();
		
			gBLoginNo = redisAPI.getGbValue(RedisConstant.GB_PLATFORM_LOGIN_NO);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
			String now = df.format(new Date());// new Date()为获取当前系统时间
			if (gBLoginNo == null) {
				logger.info("gBLoginNo为null");
				setGbPlatformLoginNo(RedisConstant.GB_PLATFORM_LOGIN_NO, new GBLoginNo(1, now));
			} else {
				if (gBLoginNo.getDate().equals(now) && gBLoginNo.getNo() < RedisConstant.KING_NO) {
					logger.info("平台登入日期相同，no小于" + RedisConstant.KING_NO);
					setGbPlatformLoginNo(RedisConstant.GB_PLATFORM_LOGIN_NO, new GBLoginNo(gBLoginNo.getNo() + 1, now));

				} else if (!gBLoginNo.getDate().equals(now)) {
					logger.info("平台登日期不同");
					setGbPlatformLoginNo(RedisConstant.GB_PLATFORM_LOGIN_NO, new GBLoginNo(1, now));
				} else if (gBLoginNo.getNo() >= RedisConstant.KING_NO) {
					logger.info("平台登入日期不在规定范围内" + gBLoginNo.getDate());
					setGbPlatformLoginNo(RedisConstant.GB_PLATFORM_LOGIN_NO, new GBLoginNo(1, now));
				}
			}
		

	}

	/**
	 * 国标报文生成：按照实际情况生成实时信息上报报文或补发信息上报报文
	 * 
	 * @param realTimeDataType
	 * @param realTImeDataUnitBytes
	 * @return
	 */
	public String getRealTimeMessage(boolean realTimeDataType, String tboxsn, byte[] realTImeDataUnitBytes) {
		String sentMessage = null;
		String vin = tboxService.getVINForTbox(tboxsn);
		try {
			RealTimeDataOperation rdOp = new RealTimeDataOperation();
			rdOp.setUniqueID(vin);
			if (realTimeDataType) {
				rdOp.setCommandIdFlag(CommandIdentifierEnum.REALTIME_DATA_REPORT_UP.getCode());
			} else {
				rdOp.setCommandIdFlag(CommandIdentifierEnum.RESEND_DATA_REPORT_UP.getCode());
			}
			rdOp.setRealTimeDateUintDecry(realTImeDataUnitBytes);
			rdOp.createMessageFromParts();
			sentMessage = ByteUtil.byteToHex(rdOp.getOriginalMessage());
			//logger.info(String.format("本次TBox(%s) VIN(%s)发送给国家平台的信息上报报文：%s", tboxsn, vin, sentMessage));
		} catch (Exception ex) {
			logger.error("创建报文出错：{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
			sentMessage = null;
		}
		return sentMessage;
	}

	/**
	 * 补发信息上报报文构造
	 * 
	 * @param vin
	 * @param realTImeDataUnitBytes
	 * @return
	 */
	public static String getResendMessage(String vin, byte[] realTImeDataUnitBytes) {
		String sentMessage = null;
		try {
			RealTimeDataOperation rdOp = new RealTimeDataOperation();
			rdOp.setUniqueID(vin);
			rdOp.setCommandIdFlag(CommandIdentifierEnum.RESEND_DATA_REPORT_UP.getCode());
			rdOp.setRealTimeDateUintDecry(realTImeDataUnitBytes);
			rdOp.createMessageFromParts();
			sentMessage = ByteUtil.byteToHex(rdOp.getOriginalMessage());
			logger.info("创建的补发信息上报报文：{}", sentMessage);
		} catch (Exception ex) {
			logger.error("创建报文出错：{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
			sentMessage = null;
		}
		return sentMessage;
	}

	/**
	 * 构造默认的指令
	 * 
	 * @param commandId
	 * @param realTImeDataUnitBytes
	 * @return
	 */
	public static String getCommonGBMessage(CommandIdentifierEnum commandId, String uniqueID, byte[] realTImeDataUnitBytes) {
		String sentMessage = null;
		try {
			RealTimeDataOperation rdOp = new RealTimeDataOperation();
			rdOp.setUniqueID(uniqueID);
			rdOp.setCommandIdFlag(commandId.getCode());
			rdOp.setRealTimeDateUintDecry(realTImeDataUnitBytes);
			rdOp.createMessageFromParts();
			sentMessage = ByteUtil.byteToHex(rdOp.getOriginalMessage());
			logger.info("创建的国标[{}]报文：{}", commandId.getValue(), sentMessage);
		} catch (Exception ex) {
			logger.error("创建报文出错：{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
			sentMessage = null;
		}
		return sentMessage;
	}

	/**
	 * 构造平台登录报文 *
	 * 
	 */
	public String getGBPlatformLoginMessage(String platformUniqueCode) {
		//登录前设置流水号
		setGbPlatformLoginNo();
		String sentMessage = null;
		try {
			// 构造平台登录报文内容
			String platformLoginDataStr = "";
			
			String platformUserName = GBNettyClient.userName;
			String platfromPwd = GBNettyClient.userPassword;

			// 不加密
			byte encodetype = (byte) OTAEncrptMode.NONE.value();
			// 登录时间
			platformLoginDataStr += ByteUtil
					.byteToStr(ByteUtil.bytesToGBDataTime(ByteUtil.CreateDateTimeBytes(Calendar.getInstance())));
			logger.info("平台登录时间： " + platformLoginDataStr);
	
			// 设置国标登录号
			platformLoginDataStr += ByteUtil.byteToStr(ByteUtil
					.int2Byte(redisAPI.getGbValue(RedisConstant.GB_PLATFORM_LOGIN_NO).getNo()));
			logger.info("平台登录流水号： " + platformLoginDataStr);
			// 用户名
			platformLoginDataStr += ByteUtil.getAscii(platformUserName);
			logger.info("用户名： " + platformLoginDataStr);
			// 密码
			platformLoginDataStr += ByteUtil.getAscii(platfromPwd);
			logger.info("密码： " + platformLoginDataStr);
			// 加密方式
			platformLoginDataStr += encodetype;

			byte[] platformLoginDataUnit = ByteUtil.hexStringToBytes(platformLoginDataStr);
			logger.info("平台登录拼装内容： " + platformLoginDataStr);

			// 暂时不考虑国标长度
			if (platformLoginDataUnit != null) {
				sentMessage = getCommonGBMessage(CommandIdentifierEnum.PLATFORM_LOGIN_IN_UP, platformUniqueCode,
						platformLoginDataUnit);
			} else {
				logger.warn("创建【平台登录】报文出错：内容为空或组装错误");
			}
		} catch (Exception ex) {
			logger.error("创建【平台登录】报文出错：{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
			sentMessage = null;
		}
		return sentMessage;
	}

	/**
	 * 构造平台登出报文 *
	 * 
	 * @param platformUniqueCode
	 */
	public String getGBPlatformLogoutMessage(String platformUniqueCode) {
		String sentMessage = null;
		try {
			String platformLogOutDataStr = "";
			platformLogOutDataStr += ByteUtil
					.byteToStr(ByteUtil.bytesToGBDataTime(ByteUtil.CreateDateTimeBytes(Calendar.getInstance())));
			logger.info("平台登出时间： " + platformLogOutDataStr);
			// 设置国标登录号
			platformLogOutDataStr += ByteUtil.byteToStr(ByteUtil.int2Byte(
					(short) redisAPI.getGbValue(RedisConstant.GB_PLATFORM_LOGIN_NO).getNo()));
			logger.info("平台登出流水号： " + platformLogOutDataStr);
			byte[] platformLogOutDataUnit = ByteUtil.hexStringToBytes(platformLogOutDataStr);

			if (platformLogOutDataUnit != null) {
				sentMessage = getCommonGBMessage(CommandIdentifierEnum.PLATFORM_LOGIN_OUT_UP, platformUniqueCode,
						platformLogOutDataUnit);
			} else {
				logger.error("创建【平台登出】报文出错：内容为空或组装错误");
			}

		} catch (Exception ex) {
			logger.error("创建【平台登出】报文出错：{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
			sentMessage = null;
		}
		return sentMessage;
	}

	/**
	 * 构造车辆登入报文
	 * @param tboxSn
	 * @param iccid
	 * @param subSystemNum
	 * @param subSystemLength
	 * @param code
	 * @return
	 */
	public String getGBCarloginMessage(String tboxSn, String iccid, int subSystemNum, int subSystemLength,
			String code) {
		//登录前设置流水号
		setGbCarLoginNo(tboxSn);
		String sentMessage = null;
		String vin = tboxService.getVINForTbox(tboxSn);
		try {
			String carLoginDataStr = "";
			// 采集时间
			carLoginDataStr += ByteUtil
					.byteToStr(ByteUtil.bytesToGBDataTime(ByteUtil.CreateDateTimeBytes(Calendar.getInstance())));
			logger.info("车辆登录时间： " + carLoginDataStr);
			// 流水号
			carLoginDataStr += ByteUtil.byteToStr(ByteUtil
					.int2Byte(getGbCarLoginNo(tboxSn).getNo()));
			logger.info("车辆登录流水号： " + carLoginDataStr);
			// iccid
			carLoginDataStr += ByteUtil.getAscii(iccid);
			// 可充电子系统个数
			carLoginDataStr += subSystemNum;
			// 编码长度
			carLoginDataStr += subSystemLength;
			// 编码
			carLoginDataStr += ByteUtil.getAscii(code);
			logger.info("车辆登录拼装报文： " + carLoginDataStr);
			byte[] carLoginDataUnit = ByteUtil.hexStringToBytes(carLoginDataStr);

			if (carLoginDataUnit != null) {
				sentMessage = getCommonGBMessage(CommandIdentifierEnum.VEHICLE_LOGIN_IN_UP, vin, carLoginDataUnit);
			} else {
				logger.error("创建【车辆登入】报文出错：内容为空或组装错误");
			}
		} catch (Exception ex) {
			logger.error("创建【车辆登入】报文出错：{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
			sentMessage = null;
		}
		return sentMessage;
	}

	/**
	 * 构造车辆登出报文
	 * 
	 * @param tboxsn
	 */
	public String getGBCarLogoutMessage(String tboxsn) {
		String sentMessage = null;
		String vin = tboxService.getVINForTbox(tboxsn);
		try {
			String carLogOutDataStr = "";
			carLogOutDataStr += ByteUtil
					.byteToStr(ByteUtil.bytesToGBDataTime(ByteUtil.CreateDateTimeBytes(Calendar.getInstance())));
			logger.info("车辆登出时间： " + carLogOutDataStr);
			
			carLogOutDataStr += ByteUtil.byteToStr(ByteUtil
					.int2Byte(getGbCarLoginNo(tboxsn).getNo()));
			logger.info("车辆登出流水号： " + carLogOutDataStr);
			
			byte[] carLogOutDataUnit = ByteUtil.hexStringToBytes(carLogOutDataStr);
			logger.info("车辆登出拼装报文： " + carLogOutDataStr);
			
			if (carLogOutDataUnit != null) {
				sentMessage = getCommonGBMessage(CommandIdentifierEnum.VEHICLE_LOGIN_OUT_UP, vin, carLogOutDataUnit);
			} else {
				logger.error("创建【车辆登出】报文出错：内容为空或组装错误");
			}

		} catch (Exception ex) {
			logger.error("创建【车辆登出】报文出错:{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
			sentMessage = null;
		}
		return sentMessage;
	}

	// 构造完报文之后向国家平台发送消息
	public RealTimeDataOperation sendGBmsg(String msg) {
		if (msg != null) {
			String serverMsg = sendRealMsg(msg);
			if (serverMsg != null) {
				RealTimeDataOperation result = new RealTimeDataOperation(ByteUtil.hexStringToBytes(serverMsg));
				return result;
			} else {
				return null;
			}
		} else {
			logger.error("发送报文错误：报文为空！");
			return null;
		}
	}

	//一直等服务器回包，后续可改相关逻辑
	private String sendRealMsg(String msg) {
		logger.info("send message : {}", msg);
		recvMsg = "";
		int sendNum = 0;
		while (!GBNettyClient.isConnected || (gbNettyClient.getSendNum() < gbNettyClient.getRecvNum())) {
			try {
				if(GBNettyClient.isConnected){
				   gbNettyClient.getNCClose();
				} 
				gbNettyClient.initGBPlatformInfo();
				Thread.sleep(1000);
			} catch (Exception e) {
				logger.error("重连国标平台失败！原因：{}", ThrowableUtil.getErrorInfoFromThrowable(e));
			}
		}
		// 发送消息
		try {
			gbNettyClient.sendMessage(msg);
			sendNum = gbNettyClient.getSendNum();
		} catch (Exception e) {
			logger.error("向国标平台发送数据失败！原因：{}", ThrowableUtil.getErrorInfoFromThrowable(e));
		}
		while (gbNettyClient.getSendNum() > gbNettyClient.getRecvNum()) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				logger.error("向国标平台发送数据失败！原因：{}", ThrowableUtil.getErrorInfoFromThrowable(e));
			}
		}
		recvMsg = gbNettyClient.getServerMsgBySendNum(sendNum);
		logger.info("从服务器收到回包：" + recvMsg);
		return recvMsg;

	}
}
