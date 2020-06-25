/**
 * 处理数据，包括报文数据，及redis失效与数据库数据库进行同步的方案
 * Copyright (c) 2017年7月14日 by 上汽集团商用车技术中心
 * @author 余佶
 * @version 1.0
 */
package com.maxus.tsp.gateway.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.maxus.tsp.common.constant.DatePatternConstant;
import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.ChinaMobileSMSUtil;
import com.maxus.tsp.common.util.LianTongSMSUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTACommand;
import com.maxus.tsp.gateway.common.constant.OTAConstant;
import com.maxus.tsp.gateway.common.constant.OTAEncrptMode;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.RemoteCtrlRespInfo;
import com.maxus.tsp.gateway.common.ota.OTAMessage;
import com.maxus.tsp.gateway.common.ota.TimeLogUtil;
import com.maxus.tsp.gateway.serviceclient.TspPlatformClient;
import com.maxus.tsp.platform.service.model.car.Tbox;
import com.maxus.tsp.platform.service.model.vo.ItRedisInfo;
import com.maxus.tsp.platform.service.model.vo.TboxVo;

@Component
public class DataProcessing {

	private static Logger logger = LogManager.getLogger(DataProcessing.class);

	@Autowired
	private RedisAPI redisAPI;
	
	@Autowired
	private OTAMessage responseMsg;
	
	// 数据库服务
	@Autowired
	private TspPlatformClient tspPlatformClient;
	
	// 数据库服务
	@Autowired
	private TboxService tboxService;

	private static int wakeUpDelayTime;
	
	@Autowired
	private LianTongSMSUtil lianTongSMSUtil;

	public static void setwakeUpDelayTime(int inputWakeUpDelayTime) {
		wakeUpDelayTime = inputWakeUpDelayTime;
	}

	/**
	 * 公共方法用于获取tbox信息,主要用于公钥、锁定等信息获取，RSA加解密
	 *
	 * @param tboxSn
	 * @return
	 */
	public TboxVo getTboxInfo(String tboxSn) {
		logger.info("TBox(SN:{}): 开始从Redis中搜索当前TBox信息", tboxSn);
		TboxVo curTboxVo = null;
		try {
			// 当Redis可用时，先从Redis查询tbox信息，否则直接掠过此步骤
			curTboxVo = null;
			if (redisAPI.hasKey(RedisConstant.TBOX_INFO, tboxSn)) {
				curTboxVo = JSONObject.parseObject(redisAPI.getHash(RedisConstant.TBOX_INFO, tboxSn), TboxVo.class);
            }
			if (curTboxVo == null) {
				// 获取失败，则前往数据库获取
				logger.info("TBox(SN:{}):Redis中无当前TBox信息，将前往数据库查询", tboxSn);
				curTboxVo = tspPlatformClient.getTboxVo(tboxSn);
				if (curTboxVo != null) {
					// 获取成功，更新redis
					logger.info("TBox(SN:{}): 将当前TBox信息从数据库同步到Redis中", tboxSn);
					redisAPI.setHash(RedisConstant.TBOX_INFO, curTboxVo.getSn(),
		                    JSONObject.toJSONString(curTboxVo, SerializerFeature.WriteMapNullValue));
				} else {
					// 数据库获取失败，从it的redis中获取
					logger.info("TBox(SN:{}): 网关无当前TBox信息，将前往RVM Redis查询", tboxSn);
					ItRedisInfo itTboxVo = getITTboxInfo(tboxSn);
					if (itTboxVo != null) {
						// 从it redis获取的数据，仅需要写入数据库表tbox，未来再进一步获取时，会再从数据库读出，同步至本地Redis
						itTboxVo.setSn(tboxSn);
						curTboxVo = new TboxVo();
						curTboxVo.setSn(itTboxVo.getSn());
						curTboxVo.setIccid(itTboxVo.getIccid());
						curTboxVo.setUuid(itTboxVo.getUuid());
						if (!OperationConstant.DEVICE_LOCK_SUCCESS.equals(itTboxVo.getDeviceLockStatus())) {
							// 如果tbox在it处没有被注册激活
							logger.info("TBox(SN:{})需要注册，注册信息根据rvm redis写入数据库", tboxSn);
							curTboxVo.setStatus(OperationConstant.REGISTER_TBOXSTATUS_INITIAL);
							tboxSave(itTboxVo);
						} else {
							curTboxVo.setStatus(OperationConstant.REGISTER_COMPLETE_SUCCESS);
							logger.info("TBox(SN:{})需要注册，但注册信息根据rvm redis为已注册", tboxSn);
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("TBox(SN:{})获取TBox信息发生异常:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
			curTboxVo = null;
		}
		return curTboxVo;
	}

	/**
	 * 查询tbox信息是否存在，是否是合法tbox
	 *
	 * @param tboxSn
	 * @return
	 */
	public boolean existTboxInfo(String tboxSn) {
		logger.info("TBox(SN:{}): 从Redis中搜索当前TBox信息", tboxSn);
		boolean existTbox = false;
		logger.info("redisAPI is null? " + (redisAPI == null));
		existTbox = redisAPI.hasKey(RedisConstant.TBOX_INFO, tboxSn);
		if (!existTbox) {
			// 查看rvm redis是否存在该tbox
			logger.info("TBox(SN:{}): 网关本地Redis中无当前TBox信息，将前往RVM redis查询", tboxSn);
			existTbox = redisAPI.ithasKey(tboxSn);
			// 获取失败，则前往数据库获取
			if (!existTbox) {
				logger.info("TBox(SN:{}): RVM redis中无当前TBox信息，将前往数据库查询", tboxSn);
				if (tspPlatformClient.getTboxVo(tboxSn) != null) {
					existTbox = true;
					logger.info("Tbox 数据库中存在 {}",tboxSn);
				}
			}
		}
		return existTbox;
	}

	public boolean loginVarible(String tboxSn){
		boolean loginVarible = true;
		/*ItRedisInfo itRedisInfo = itredisAPI.getItValue(tboxSn);
		String itStatus = itRedisInfo.getDeviceLockStatus();*/
		if(tspPlatformClient.getTboxVo(tboxSn) == null){
			logger.info("TBox(SN:{}): 网关数据库中无当前TBox信息，将登录不成功", tboxSn);
			loginVarible = false;
		}
		return loginVarible;
	}



	/**
	 * 调用oms的接口，将tbox信息报存至mysql
	 *
	 * @param itTboxVo
	 * @return
	 */
	public boolean tboxSave(ItRedisInfo itTboxVo) {
		boolean ret = false;
		try {
			Tbox tboxSave = new Tbox();
			tboxSave.setSn(itTboxVo.getSn());
			tboxSave.setIccid(itTboxVo.getIccid());
			tboxSave.setUuid(itTboxVo.getUuid());
			tboxSave.setStatus(0);
			ret = tspPlatformClient.tboxDataSave(tboxSave);
		} catch (Exception ex) {
			logger.error("TBox({})数据从it Redis插入数据库发送异常:{}", itTboxVo.getSn(),
					ThrowableUtil.getErrorInfoFromThrowable(ex));
			ret = false;
		}
		return ret;
	}

	/**
	 * 检查tbox是否合法
	 *
	 * @param serialNumber
	 * @return
	 */
	public boolean isTboxValid(String serialNumber) {
		// 确认code是合法的tbox编号,如果是，则返回空
		boolean checkResult = false;
		// 检查自身redis中是否有数据
		checkResult = redisAPI.hasKey(RedisConstant.VALID_PRE + serialNumber);
		// 如果自建redis查询失败, 检查it的redis中是否有数据
		if (!checkResult) {
			checkResult = isTBoxDataNeedTransfer(serialNumber);
		}
		// 如果it redis也查询失败，从数据库获取
		if (!checkResult) {
			TboxVo tboxVo = tspPlatformClient.getTboxVo(serialNumber);
			if (tboxVo == null) {
				logger.warn("控制指令不能执行，非法的TBox(SN:{})", serialNumber);
			} else {
				checkResult = true;
				redisAPI.setSet(RedisConstant.VALID_PRE + serialNumber, "");
			}
		}
		return checkResult;
	}

	/**
	 * 准备下行回复报文，如为登录报文，使用AES加密
	 *
	 * @param requestMsg
	 * @param outData
	 * @return
	 */
	public byte[] otaMsg2bytes(OTAMessage requestMsg, byte[] outData) {
		if (outData == null) {
			outData = OTAConstant.DEFAULT_RETURN_BYTES;
		}
		String logName ="OTAResolveServerHandler.resolve -> procOTA -> DataProcessing.otaMsg2bytes: ";
		TimeLogUtil.log(logName+"方法开始");
		// 设置回包报文头部,复制请求报文头部
		responseMsg = requestMsg.getResOTAMessage(responseMsg,outData);
		OTACommand commandByCode = OTACommand.getByCode(responseMsg.getCommand());
		TimeLogUtil.log(logName+"设置头部");
		if (commandByCode.equals(OTACommand.CMD_DOWN_LOGIN)) {
			String tboxWithFota = redisAPI.getHash(RedisConstant.TBOX_WITH_FOTA, requestMsg.getSerialNumber());
			
			TimeLogUtil.log(logName+"redis 获得 tboxWithFota");
			//带fota功能的TBox  登录回复
			if(StringUtils.isNotBlank(tboxWithFota)){
				// PKI/CA  04加密
				responseMsg.setEncryptType(OTAEncrptMode.PKI);
			}else {//没有fota功能的  登录回复
				// AES 02加密
				responseMsg.setEncryptType(OTAEncrptMode.AES);
			}
		}
		
		if (commandByCode.equals(OTACommand.CMD_UP_GB_LOGIN_EMSN)) {
			//国六B登陆回复须加密
			responseMsg.setEncryptType(OTAEncrptMode.AES);
		}
		TimeLogUtil.log(logName+"createMessageFromParts 开始");
		responseMsg.createMessageFromParts();
		
		TimeLogUtil.log(logName+"方法结束");
		return responseMsg.curMessage;
	}

	public byte[] otaMsg2bytesWithPKICA(OTAMessage requestMsg, byte[] outData) {
		if (outData == null) {
			outData = OTAConstant.DEFAULT_RETURN_BYTES;
		}
		// 设置回包报文头部,复制请求报文头部
		responseMsg = requestMsg.getResOTAMessage(responseMsg, outData);
		OTACommand commandByCode = OTACommand.getByCode(responseMsg.getCommand());
		if (commandByCode.equals(OTACommand.CMD_DOWN_LOGIN)) {
			responseMsg.setEncryptType(OTAEncrptMode.AES);
		}
		responseMsg.createMessageFromParts();

		return responseMsg.curMessage;
	}
	
	public boolean existTboxOnline(String tboxsn) {
        // 查询是否在线，不进行Redis连接异常捕捉，由外部调用进行捕捉
        return redisAPI.hasKey(RedisConstant.ONLINE_TBOX, tboxsn);
    }

	/**
	 * 判断Redis中tbox在线情况，如果Redis无法正常访问，则使用数据库进行查询
	 *
	 * @param serialNumber
	 * @return boolean
	 */
	public boolean onlineTboxExistCheck(String serialNumber) {
		boolean checkResult = false;
		boolean isRedisConnected = true;
		if (isRedisConnected) {
			// 当Redis连接有效时，查询Redis
			try {
				checkResult = existTboxOnline(serialNumber);
			} catch (Exception ex) {
				logger.error("Redis查询TBox(SN:{})在线信息发生异常:{}", serialNumber,
						ThrowableUtil.getErrorInfoFromThrowable(ex));
				// checkResult = false;
				isRedisConnected = false;
			}
		}
		if (!isRedisConnected) {
			// 如果Redis连接失败，查询不到，从数据库获取
			try {
				if (tspPlatformClient.getValueInRdsOnlineTbox(serialNumber) == null) {
					checkResult = false;
				} else {
					checkResult = true;
				}
			} catch (Exception ex) {
				logger.error("数据库查询TBox(SN:{})在线信息发生异常:{}", serialNumber,
						ThrowableUtil.getErrorInfoFromThrowable(ex));
				checkResult = false;
			}
		}
		return checkResult;
	}
	
	/**
     * @Title: addTboxSignIn
     * @Description: 记录登陆信息hash数据，field为tboxsn,value为登录时间
     * @param: @param
     *             tboxsn
     * @return: void
     * @throws @author
     *             余佶
     * @Date 2017年8月12日 下午1:52:37
     */
    public boolean addTboxSignIn(String tboxsn, String eventTime) {
        try {
            redisAPI.setHash(RedisConstant.ONLINE_TBOX, tboxsn, eventTime);
            return true;
        } catch (Exception ex) {
            logger.error("Redis connection error, TBox({}) can't do addTboxSignIn:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return false;
        }
    }

	/**
	 * 向Redis增加tbox在线信息，如果Redis坏了，更新至数据库
	 *
	 * @param serialNumber
	 */
	public boolean onlineTboxAdd(String serialNumber) {
		// 记录当前登陆时间
		Date curTime = new Date();
		String eventTime = DateFormatUtils.format(curTime, DatePatternConstant.SECOND);
		boolean retResult = false;
		retResult = addTboxSignIn(serialNumber, eventTime);
		if (!retResult) {
			try {
				tspPlatformClient.updateDataInRdsOnlineTbox(serialNumber, eventTime);
				logger.info("TBox(SN:{}): 当前TBox在线信息更新至mysql", serialNumber);
				retResult = true;
			} catch (Exception ex) {
				logger.error("TBox(SN:{}): 当前TBox在线信息没有被存储到mysql中，异常:{}", serialNumber,
						ThrowableUtil.getErrorInfoFromThrowable(ex));
				retResult = false;
			}
		} else {
			logger.info("TBox(SN:{}): 当前TBox在线信息更新至Redis", serialNumber);
		}
		return retResult;
	}

	/**
	 * 删除tbox在线信息，redis正常时，清除redis，否则清楚mysql
	 *
	 * @param serialNumber
	 */
	public void onlineTboxDelete(String serialNumber) {
		boolean isRedisConnected = true;
		if (isRedisConnected) {
			// Redis可用，删除Redis的online数据
			try {
				redisAPI.removeHash(RedisConstant.ONLINE_TBOX, serialNumber);
			} catch (Exception ex) {
				logger.error("TBox(SN:{}):Redis在线信息不能执行删除,发生异常:{}", serialNumber,
						ThrowableUtil.getErrorInfoFromThrowable(ex));
				isRedisConnected = false;
			}
		}
		if (!isRedisConnected) {
			// Redis不可用，向数据库删除online数据
			try {
				tspPlatformClient.deleteDataInRdsOnlineTbox(serialNumber);
			} catch (Exception ex) {
				logger.error("TBox(SN:{})mysql在线信息不能执行删除,发生异常:{}", serialNumber,
						ThrowableUtil.getErrorInfoFromThrowable(ex));
			}
		}
	}
	
	public boolean setTboxLogout(String tboxsn, String eventTime) {
        try {
            redisAPI.setHash(RedisConstant.TBOX_LOGOUT_TIME, tboxsn, eventTime);
            logger.info("Save Tbox Logout time into Redis. tbox:{} Logout Time:{}", tboxsn, eventTime);
            return true;
        } catch (Exception ex) {
            logger.error("Redis connection error, TBox({}) can't do setTboxLogout:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return false;
        }
    }

	/**
	 * @Title: logoutTboxUpdate
	 * @Description: tbox发送登出报文时间更新
	 * @param serialNumber
	 */
	public void logoutTboxUpdate(String serialNumber) {
		// 记录当前登陆时间
		Date curTime = new Date();
		String eventTime = DateFormatUtils.format(curTime, DatePatternConstant.SECOND);
		boolean isRedisConnected = true;
		if (isRedisConnected) {
			// Redis可用，更新Redis中的登出报文接收时间
			if (setTboxLogout(serialNumber, eventTime)) {
				logger.info("TBox(SN:{}): 当前TBox发送LOGOUT报文时间已更新至Redis中", serialNumber);
			} else {
				logger.warn("TBox(SN:{}): Redis异常，当前TBox发送LOGOUT报文时间未能更新至Redis中", serialNumber);
				isRedisConnected = false;
			}
		}
		if (!isRedisConnected) {
			// Redis不可用，更新数据库中的登出报文接收时间
			try {
				tspPlatformClient.updateDataInRdsTboxLogOutTime(serialNumber, eventTime);
				logger.info("TBox(SN:{}): 当前TBox发送LOGOUT报文时间已更新至mysql", serialNumber);
			} catch (Exception ex) {
				logger.error("TBox(SN:{}): TBox发送LOGOUT报文时间未能更新至mysql.异常:{}", serialNumber,
						ThrowableUtil.getErrorInfoFromThrowable(ex));
			}
		}
	}
	
	public String getTboxLogout(String tboxsn) {
        String eventTime = null;
        if (redisAPI.hasKey(RedisConstant.TBOX_LOGOUT_TIME, tboxsn)) {
            eventTime = redisAPI.getHash(RedisConstant.TBOX_LOGOUT_TIME, tboxsn);
        }
        logger.info("Get Tbox Logout time from Redis. tbox:{} Logout Time:{}", tboxsn, eventTime);
        return eventTime;
    }

	/**
	 * tbox发送登出报文时间获取
	 *
	 * @param serialNumber
	 */
	public String logoutTboxGet(String serialNumber) {
		// 记录当前登陆时间
		String lastLogoutTime = null;
		boolean isRedisConnected = true;
		if (isRedisConnected) {
			// Redis可用，从Redis中获取登出报文接收时间
			try {
				lastLogoutTime = getTboxLogout(serialNumber);
			} catch (Exception ex) {
				isRedisConnected = false;
			}
		}
		if (!isRedisConnected) {
			// Redis不可用，更新数据库中的登出报文接收时间
			try {
				lastLogoutTime = tspPlatformClient.getValueInRdsTboxLogOutTime(serialNumber);
			} catch (Exception ex) {
				logger.error("TBox(SN:{}): TBoxLOGOUT报文时间未能从mysql获取.异常:{}", serialNumber,
						ThrowableUtil.getErrorInfoFromThrowable(ex));
				lastLogoutTime = null;
			}
		}
		return lastLogoutTime;
	}
	
	/**
     * @Title: addRemoteCtrlRes
     * @Description: 添加一条远程控制返回结果
     * @param: @param
     *             tboxsn
     * @param: @param
     *             remoteCtrlCommand
     * @return: void
     * @throws @author
     *             yuji
     * @Date 2017年7月18日 上午11:22:43
     */
    public boolean addRemoteCtrlRes(String tboxsn, String remoteCtrlResp) {
        try {
            redisAPI.setHash(RedisConstant.CAR_REMOTE_CTRL_RESP, tboxsn, remoteCtrlResp);
            return true;
        } catch (Exception ex) {
            logger.error("Redis connection error, TBox({}) can't do addRemoteCtrlRes:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return false;
        }
    }

	/**
	 * 记录远程控制或者拍照结果于redis和数据库中
	 *
	 * @param serialNumber
	 * @param remoteRespInfo
	 */
	public void remoteCtrlRespAdd(String serialNumber, RemoteCtrlRespInfo remoteRespInfo) {
		// 记录远程控制结果至SVN，同时也记录于数据库
		String savedValue = JSONObject.toJSONString(remoteRespInfo, SerializerFeature.WriteMapNullValue);
		boolean isRedisConnected = true;
		if (isRedisConnected) {
			// Redis可用，向Redis插入远程控制结果
			if (addRemoteCtrlRes(serialNumber, savedValue)) {
				logger.info("TBox(SN:{}): 当前TBox添加远程控制结果已更新至Redis中", serialNumber);
			} else {
				logger.warn("TBox(SN:{}): Redis异常，当前TBox添加远程控制结果未能更新至Redis中", serialNumber);
				isRedisConnected = false;
			}
		}
		if (!isRedisConnected) {
			try {
				// Redis不可用，向数据库插入远程控制结果
				tspPlatformClient.updateDataInRdsCarRemoteCtrlResp(serialNumber, savedValue);
				logger.info("TBox(SN:{}): 当前TBox添加远程控制结果已更新至Mysql中", serialNumber);
			} catch (Exception ex) {
				logger.error("TBox(SN:{}): 当前TBox添加远程控制结果至Mysql失败，异常:{}", serialNumber,
						ThrowableUtil.getErrorInfoFromThrowable(ex));
			}
		}
	}

	/**
	 * 删除redis和数据库中的远程控制或者拍照结果
	 *
	 * @param serialNumber
	 */
	public boolean remoteCtrlRespDelete(String serialNumber) {
		boolean delResult = false;
		// Redis可用，向Redis删除远程控制结果
		delResult = redisAPI.removeHash(RedisConstant.CAR_REMOTE_CTRL_RESP, serialNumber);
		if (!delResult) {
			// Redis不可用，向删除远程控制结果
			try {
				delResult = tspPlatformClient.deleteDataInRdsCarRemoteCtrlResp(serialNumber);
			} catch (Exception ex) {
				delResult = false;
				logger.error("TBox(SN:{}): 当前TBox删除远程控制结果至Mysql失败，异常:{}", serialNumber,
						ThrowableUtil.getErrorInfoFromThrowable(ex));
			}
		}
		return delResult;
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
    public String getRemoteCtrlRes(String tboxSn) {
        if (redisAPI.hasKey(RedisConstant.CAR_REMOTE_CTRL_RESP, tboxSn)) {
            return redisAPI.getHash(RedisConstant.CAR_REMOTE_CTRL_RESP, tboxSn);
        } else {
            return null;
        }
    }

	/**
	 * 获取redis和数据库中的远程控制或者拍照结果
	 *
	 * @param serialNumber
	 */
	public String remoteCtrlRespGet(String serialNumber) {
		String getResult = null;
		boolean isRedisConnected = true;
		if (isRedisConnected) {
			// Redis可用，向Redis获取远程控制结果
			try {
				getResult = getRemoteCtrlRes(serialNumber);
			} catch (Exception ex) {
				logger.error("TBox(SN:{}): Redis获取TBox远程控制或拍照结果发生异常:{}", serialNumber,
						ThrowableUtil.getErrorInfoFromThrowable(ex));
				isRedisConnected = false;
			}
		}
		if (!isRedisConnected) {
			// Redis不可用，向数据库获取远程控制结果
			try {
				getResult = tspPlatformClient.getValueInRdsCarRemoteCtrlResp(serialNumber);
			} catch (Exception ex) {
				getResult = null;
				logger.error("TBox(SN:{}): Mysql获取TBox远程控制或拍照结果发生异常:{}", serialNumber,
						ThrowableUtil.getErrorInfoFromThrowable(ex));
			}
		}
		return getResult;
	}
	
	public boolean existChannelInfo(String tboxsn) {
        boolean result = redisAPI.hasKey(RedisConstant.TBOX_CHANNEL, tboxsn);
        return result;
    }

	/**
	 * 检查tbox是否已经存在channel信息(tbox新建一个socket)
	 *
	 * @param serialNumber
	 * @return
	 */
	public boolean tboxChannelExistCheck(String serialNumber) {
		boolean existResult = false;
		boolean isRedisConnected = true;
		if (isRedisConnected) {
			// Redis可用，向Redis查询Tbox channel是否存在
			try {
				existResult = existChannelInfo(serialNumber);
			} catch (Exception ex) {
				logger.error("TBox(SN:{}): Redis查询TBox channel是否存在发生异常:{}", serialNumber,
						ThrowableUtil.getErrorInfoFromThrowable(ex));
				isRedisConnected = false;
			}
		}
		if (!isRedisConnected) {
			try {
				// Redis访问失败，向数据库查询Tbox channel是否存在
				if (tspPlatformClient.getValueInRdsTboxChannel(serialNumber) != null) {
					existResult = true;
				} else {
					existResult = false;
				}
			} catch (Exception ex) {
				existResult = false;
				logger.error("TBox(SN:{}): 数据库查询TBox channel是否存在发生异常:{}", serialNumber,
						ThrowableUtil.getErrorInfoFromThrowable(ex));
			}
		}
		return existResult;
	}

	/**
	 * @Title: isTBoxDataNeedTransfer
	 * @Description: 检查tbox是否需要转发
	 * @param tboxSn
	 * @return
	 */
	public boolean isTBoxDataNeedTransfer(String tboxSn) {
		// 确认tbox信息存在于it的redis中
		try {
			return redisAPI.ithasKey(tboxSn);
		} catch (Exception e) {
			logger.error("确认TBox(SN:{})信息存在于it的redis中发生异常:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(e));
			return false;
		}

	}

	/**
	 * 返回封装完的ItInfo
	 *
	 * @param tboxSn
	 * @return
	 */
	public ItRedisInfo getITTboxInfo(String tboxSn) {
		ItRedisInfo itInfo = new ItRedisInfo();
		itInfo = redisAPI.getItValue(tboxSn);
		if (itInfo == null) {
			logger.warn("无法根据TBox序列号{}在RVM redis中查找到数据", tboxSn);
			return null;
		}
		logger.debug("TBox(SN:{})的RVM数据为:{}", tboxSn, JSONObject.toJSONString(itInfo));
		try {
			return itInfo;
		} catch (NumberFormatException e) {
			logger.error("TBox(SN:{})封装itInfo发生异常:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(e));
			return null;
		}
	}

	/**
	 * 根据tbox所属的短信运营商，判断发送唤醒短信是否成功
	 * @param serialNumber
	 * @return
	 */
	public boolean isSendingMessageSucceed(String serialNumber) {
		boolean sendWakeUpMSM = true;
		String simNumber = "";
		String currentMno = "";
		String iccid = "";
		try {
			// 获取当前Tbox使用的短信服务运行商
			ItRedisInfo itRedisInfo = getITTboxInfo(serialNumber);
			if (itRedisInfo == null) {
				logger.warn("TBox(sn:{})从RVM redis获取该TBox信息失败,默认发送联通唤醒短信！", serialNumber);
				currentMno = OperationConstant.MESSAGE_SERVICE_CU;
				iccid = tboxService.getIccid(serialNumber);
			} else {
				currentMno = itRedisInfo.getMno();
				simNumber = itRedisInfo.getSimNumber();
				iccid = itRedisInfo.getIccid();
				if (iccid == null || StringUtils.isBlank(iccid)) {
					logger.warn("TBox(sn:{})从RVM redis获取该TBox的iccid信息失败,尝试从内存或数据获取信息！", serialNumber);
					iccid = tboxService.getIccid(serialNumber);
				}
				//运营商为移动的YM，改为默认的移动CM
				if (currentMno.equals(OperationConstant.MESSAGE_SERVICE_YM)){
					currentMno = OperationConstant.MESSAGE_SERVICE_CM;
				}
			}
			if (StringUtils.isBlank(currentMno)) {
				logger.warn("TBox(sn:{}):未获取到当前TBox短信服务运营商消息,将切换成默认的联通运营商！", serialNumber);
				currentMno = OperationConstant.MESSAGE_SERVICE_CU;
			}
			switch (currentMno) {
				case OperationConstant.MESSAGE_SERVICE_CU:
					if (iccid == null || StringUtils.isBlank(iccid)) {
						logger.warn("TBox(sn:{}):当前TBox使用的联通iccid为空或有误！", serialNumber);
						sendWakeUpMSM = false;
					} else {
						logger.info("TBox(sn:{} ,iccid:{}):尝试请求联通服务运营商发送唤醒Tbox短信...", serialNumber, iccid);
						// 联通短信服务
						lianTongSMSUtil.callWebService(iccid, OperationConstant.WAKE_UP_MSG_CONTENT);
					}
					break;
				case OperationConstant.MESSAGE_SERVICE_CM:
					logger.info("TBox(sn:{} ,SIM:{}):尝试请求移动服务运营商发送唤醒TBox短信...",
							serialNumber, itRedisInfo.getSimNumber());
					// 移动短信服务
					if (StringUtils.isBlank(simNumber)) {
						logger.warn("TBox(sn:{}):当前TBox使用的移动sim卡号为空或有误！", serialNumber);
						sendWakeUpMSM = false;
					}
					boolean result = ChinaMobileSMSUtil.sendSmsMessage(simNumber, OperationConstant.WAKE_UP_MSG_CONTENT);
					if (!result) {
						logger.warn("TBox(sn:{}):唤醒短信发送失败！", serialNumber);
						sendWakeUpMSM = false;
					}
					break;
				default:
					logger.warn("TBox(sn:{}):当前TBox使用的短信服务运营商有误", serialNumber);
					sendWakeUpMSM = false;
					break;
			}
		} catch (Exception e) {
			sendWakeUpMSM = false;
			logger.error("短信唤醒失败TBox:{}，异常:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
		}
		return sendWakeUpMSM;
	}
	
	/**
     * @Title: 根据操作指令确认是否有该指令引起的唤醒操作
     * @Description: 获取实时位置信息
     * @param: @param
     *             tboxSn
     * @param: @return
     * @return: ReportPos
     * @throws @author
     *             fogmk
     * @Date 2017年7月18日 下午3:04:37
     */
    public boolean existWakeUp(String tboxSn, String operName) {
        return redisAPI.hasKey(RedisConstant.WAKE_UP + "_" + operName + "_" + tboxSn);
    }
    
    /**
     * @Title: existTboxRemoteCtrl
     * @Description: tbox是否已经存在一条远程控制指令了
     * @param: @param
     *             tboxsn
     * @param: @return
     * @return: boolean
     * @throws @author
     *             fogmk
     * @Date 2017年7月18日 下午2:01:24
     */
    public boolean existTboxRemoteCtrl(String tboxsn) {
        return redisAPI.hasKey(RedisConstant.CAR_REMOTE_CTRL_REQ, tboxsn);
    }

	/**
	 * 远程控制操作时，需要进行排它，故需判断是否已经存在一条指令
	 *
	 * @param serialNumber
	 * @param vin
	 * @return
	 */
	public boolean isRequestRmtExist(String serialNumber, String vin) {
		// 判断tbox是否已经存在正在执行的请求
		boolean existRequest = false;
		boolean redisStatus = true;
		if (redisStatus) {
			// 如果Redis可以访问，使用redis进行查询
			try {
				boolean wakeUpStatus = existWakeUp(serialNumber, OperationConstant.REMOTE_CONTROL);
				boolean remoteCtrlStatus = existTboxRemoteCtrl(serialNumber);
				existRequest = wakeUpStatus || remoteCtrlStatus;
				logger.info("TBox(sn:{})的Redis会话唤醒状态:{},远程控制指令存在状态:{}", serialNumber,
				wakeUpStatus,remoteCtrlStatus);
			} catch (Exception e) {
				redisStatus = false;
				logger.error("Redis error, can't find remote data for TBox(sn:{}): {}",
				serialNumber,ThrowableUtil.getErrorInfoFromThrowable(e));
			}
		}
		if (!redisStatus) {
			// Redis不可用，使用数据库查询，根据当前时间节点推算间隔时间内是否存在相应请求
			Calendar calendarForRmtCtrl = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				// 计算偏移多少秒前的时间
				calendarForRmtCtrl.add(Calendar.SECOND, -OperationConstant.REMOTECONTROL_REQ_EXPIRED_TIME);
				String limitTimeForRmtCtrl = sdf.format(calendarForRmtCtrl.getTime());
				if (tspPlatformClient.getCountForOperRmtCtrl(vin, limitTimeForRmtCtrl) > 0) {
					existRequest = true;
				}
			} catch (Exception ex) {
				logger.error("数据库查询TBox:{}是否已经存在正在执行的请求失败,原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));				;
				throw ex;
			}
		}
		return existRequest;
	}

	/**
	 * TBox指令下发排他判断
	 *
	 * @param serialNumber
	 * @return
	 */
	/*public static boolean isTboxWakeUpOrCommandExist(String serialNumber) {
		boolean existRequst = false;
		// 如果Redis可以访问，使用redis进行查询
		boolean existWakeUp = GlobalSessionChannel.existWakeUp(serialNumber, OperationConstant.HOME_CONTROL);
		boolean existTBoxCommandInRedis = GlobalSessionChannel.existTBoxCommandInRedis(serialNumber);
		logger.warn("TBox:{}的Redis会话唤醒状态:{}, 指令存在状态:{}", serialNumber,
		existWakeUp, existTBoxCommandInRedis);
		existRequst = existWakeUp || existTBoxCommandInRedis;
		return existRequst;
	}*/

	/**
     * @Title: 根据操作指令确认是否有该指令引起的唤醒操作
     * @param tboxSn
     * @return
     * @author zhuna
     * @date 2018年12月4日
     */
    public boolean existWakeUp(String tboxSn) {
        return redisAPI.hasKey(RedisConstant.WAKE_UP + "_" + tboxSn);
    }
    
	/**
	 * 蓝牙操作排他判断
	 *
	 * @param serialNumber
	 * @return
	 */
	public boolean isBlueToothExist(String serialNumber) {
		boolean existRequst = false;
			// 如果Redis可以访问，使用redis进行查询
			boolean existWakeUp = existWakeUp(serialNumber);
			//boolean existBlueToothCtrl = GlobalSessionChannel.existBlueToothCtrl(serialNumber, OperationConstant.BLUE_TOOTH_CTRL);
			boolean existBlueToothCtrl = redisAPI.hasKey(RedisConstant.COMMAND_SEND + "_" + serialNumber);
			logger.warn("TBox:{}的Redis会话唤醒状态:{}, 远程配置控制指令存在状态:{}", serialNumber,
			existWakeUp, existBlueToothCtrl);
			existRequst = existWakeUp || existBlueToothCtrl;
		return existRequst;
	}

	/**
	 * @Description 判断当前TBox是否存在控制指令或者唤醒指令
	 * @Date 2019/1/22 18:40
	 * @Param [serialNumber]
	 * @return boolean
	 **/
	public boolean isVersionQueryExist(String serialNumber) {
		boolean existRequest = false;
		//redis可以使用，在redis中查询
		boolean existWakeUp = existWakeUp(serialNumber, OperationConstant.FOTA_VERSION_QUERY);
		boolean existFotaVersionQuery = existFotaVersionQuery(serialNumber, OperationConstant.FOTA_VERSION_QUERY);
		logger.warn("TBox(SN:{})的Redis会话唤醒状态:{}, 版本查询控制指令存在状态:{}", serialNumber, existWakeUp, existFotaVersionQuery);
		existRequest = existWakeUp || existFotaVersionQuery;
		return existRequest;
	}

	/**
	 * 获取车况配置排他性
	 * @param sn
	 * @return
	 */
	public boolean isGetVehicleStatusExist(String sn){
		boolean existReq = false;
		logger.warn("TBox:{}的Redis会话唤醒状态:{}, 获取车况指令存在状态:{}", sn,
				existWakeUp(sn),
				redisAPI.hasKey(RedisConstant.COMMAND_SEND + "_" + sn));
		existReq = existWakeUp(sn) ||
				redisAPI.hasKey(RedisConstant.COMMAND_SEND + "_" + sn);
		return existReq;
	}
	
	/**
	 * 远程配置排他判断
	 *
	 * @param serialNumber
	 * @param vin
	 * @param operName
	 * @return
	 */
	public boolean isRmtConfigExist(String serialNumber, String vin, String operName) {
		// 判断tbox是否已经存在正在执行的poi请求
		boolean existRequest = false;
		// 如果Redis可以访问，使用redis进行查询
		logger.warn("TBox:{}的Redis会话唤醒状态:{}, 远程配置控制指令存在状态:{}", serialNumber,
				existWakeUp(serialNumber),
				redisAPI.hasKey(RedisConstant.COMMAND_SEND + "_" + serialNumber));
		existRequest = existWakeUp(serialNumber)
				|| redisAPI.hasKey(RedisConstant.COMMAND_SEND + "_" + serialNumber);
		return existRequest;
	}

	/**
	 * 远程控制异步方案操作时，需要进行排它，故需判断是否已经存在一条指令，由于异步方案中拍照和远控轮询使用同一个接口，故需要两者都进行排它
	 *
	 * @param serialNumber
	 * @param vin
	 * @return
	 */
	public boolean isRequestRmtAsynExist(String serialNumber, String vin) {
		// 判断tbox是否已经存在正在执行的请求
		boolean existRequest = false;
		boolean redisStatus = true;
		if (redisStatus) {
			try {
				// 如果Redis可以访问，使用redis进行查询
				logger.warn("TBox:{}的Redis会话唤醒状态:{},远程控制指令存在状态:{}, 透传控制指令存在状态:{}", serialNumber,
						existWakeUp(serialNumber),
						redisAPI.hasKey(RedisConstant.COMMAND_SEND + "_" + serialNumber));
				existRequest = existWakeUp(serialNumber)
						|| redisAPI.hasKey(RedisConstant.COMMAND_SEND + "_" + serialNumber);
			} catch (Exception e) {
				redisStatus = false;
				logger.error("redis remote controller status query failed for sn[{}]:{}",
				serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
			}
		} 
		if (!redisStatus) {
			// Redis不可用，使用数据库查询，根据当前时间节点推算间隔时间内是否存在相应请求
			Calendar calendarForRmtCtrl = Calendar.getInstance(), calendarForEtRmtCtrl = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				// 计算偏移多少秒前的时间
				calendarForRmtCtrl.add(Calendar.SECOND, -OperationConstant.REMOTECONTROL_REQ_EXPIRED_TIME);
				String limitTimeForRmtCtrl = sdf.format(calendarForRmtCtrl.getTime());
				calendarForEtRmtCtrl.add(Calendar.SECOND, -OperationConstant.TAKEPHOTO_REQ_EXPIRED_TIME);
				String limitTimeForEtRmtCtrl = sdf.format(calendarForEtRmtCtrl.getTime());
				if (tspPlatformClient.getCountForOperRmtCtrl(vin, limitTimeForRmtCtrl) > 0
						|| tspPlatformClient.getCountForOperingTakePhoto(vin, limitTimeForEtRmtCtrl) > 0) {
					existRequest = true;
				}
			} catch (Exception ex) {
				logger.error("数据库查询TBox:{}是否已经存在正在执行的请求失败,原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));				;
				throw ex;
			}
		}
		return existRequest;
	}
	
	/**
     * @Title: existEntertainmentRemoteCtrl
     * @Description: 娱乐主机是否已经存在一条远程控制指令了
     * @param: @param
     *             tboxsn
     * @param: @return
     * @return: boolean
     * @throws @author
     *             yuji
     * @Date 2017年8月30日 下午1:01:24
     */
    public boolean existTakePhotoRemoteCtrl(String tboxsn) {
        try {
            return redisAPI.hasKey(RedisConstant.ENTERTAINMENT_REMOTE_CTRL_REQ, OperationConstant.FORWARD_TAKEPHOTO + tboxsn);
        } catch (Exception ex) {
            logger.error("Redis connection error, TBox({}) can't do existEntertainmentRemoteCtrl:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return false;
        }
    }

	/**
	 * 远程控制操作时，需要进行排它，故需判断是否已经存在一条指令
	 *
	 * @param serialNumber
	 * @param vin
	 * @return
	 */
	public boolean isRequestTakePhotoExist(String serialNumber, String vin) {
		// 判断tbox是否已经存在正在执行的请求
		boolean existRequest = false;
		boolean redisStatus = true;
		// 如果Redis可以访问，使用redis进行查询
		try {
			logger.warn("TBox:{}的Redis会话唤醒状态:{},透传控制指令存在状态:{}", serialNumber,
					existWakeUp(serialNumber, OperationConstant.FORWARD_TAKEPHOTO),
					existTakePhotoRemoteCtrl(serialNumber));
			existRequest = existWakeUp(serialNumber, OperationConstant.FORWARD_TAKEPHOTO)
					|| existTakePhotoRemoteCtrl(serialNumber);
		} catch (Exception e) {
			redisStatus = false;
			logger.error("redis TakePhotoRequest status query failed for sn[{}]:{}",
			serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
		}
		if (!redisStatus) {
			// Redis不可用，使用数据库查询，根据当前时间节点推算间隔时间内是否存在相应请求
			Calendar calendarForEtRmtCtrl = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				// 计算偏移多少秒前的时间
				calendarForEtRmtCtrl.add(Calendar.SECOND, -OperationConstant.TAKEPHOTO_REQ_EXPIRED_TIME);
				String limitTimeForEtRmtCtrl = sdf.format(calendarForEtRmtCtrl.getTime());
				if (tspPlatformClient.getCountForOperingTakePhoto(vin, limitTimeForEtRmtCtrl) > 0) {
					existRequest = true;
				}
			} catch (Exception ex) {
				logger.error("数据库查询TBox:{}是否已经存在正在执行的请求失败,原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));				;
				throw ex;
			}
		}
		return existRequest;
	}

	/**
	 * 与娱乐主机有关的项目，根据透传操作指令，判断是否已经存在一个请求
	 *
	 * @param serialNumber
	 * @return
	 */
	public boolean isEntertainmentRequestExist(String serialNumber) {
		// 判断tbox是否已经存在正在执行的poi请求
		boolean existRequest;
		try{
			// 如果Redis可以访问，使用redis进行查询
			boolean wakeUpStatus = existWakeUp(serialNumber);
			boolean commandStatus = redisAPI.hasKey(RedisConstant.COMMAND_SEND + "_" + serialNumber);
			logger.warn("TBox:{}的Redis会话唤醒状态:{}, 透传控制指令存在状态:{}", serialNumber, wakeUpStatus, commandStatus);
			existRequest = wakeUpStatus || commandStatus;
		} catch (Exception e) {
			logger.error("TBox(SN:{})查询redis中是否存在唤醒以及控制指令过程发生异常, 异常原因:{}",
					serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
			existRequest = false;
		}
		return existRequest;
	}

	/**
	 *  根据登出报文判断是否需要延迟唤醒线程至距离登出为10s后执行
	 * @param tboxsn
	 */
	public void checkTBoxLogOutForWakeupWait(String tboxsn) {
		// 获取tbox最新的LOGOUT报文形式登出时间
		String lastLogoutTime = logoutTboxGet(tboxsn);
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
					logger.info("由于TBox:{}刚发送登出报文，唤醒请求将延迟{}毫秒", tboxsn, diff);
					Thread.sleep(diff);
					logger.info("TBox:{}结束唤醒请求延迟等待，将执行唤醒。", tboxsn);
				}

			} catch (Exception ex) {
				logger.error("确认延迟唤醒流程发生异常:{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
				return;
			}
		}
		return;
	}


	/**
	 * 更新tbox状态
	 * @param curPublicKey
	 * @param tboxSn
	 * @param major（该参数为原61蓝牙方案，目前不使用）
	 * @param minor（该参数为原61蓝牙方案，目前不使用）
	 * @return
	 */
	public boolean lockTbox(String curPublicKey, String tboxSn, String major, String minor) {
		boolean operate = false;
		try {
			if (isTBoxDataNeedTransfer(tboxSn)) {  //如果RVM redis存在该tbox信息，则更新为网关已经激活完成
				tspPlatformClient.lockTbox(curPublicKey, tboxSn, major, minor,
						OperationConstant.REGISTER_GATEWAY_SUCCESS);
			} else { //如果RVM redis不存在该tbox信息，则认为非RVM项目，直接设为注册激活完成
				tspPlatformClient.lockTbox(curPublicKey, tboxSn, major, minor,
						OperationConstant.REGISTER_COMPLETE_SUCCESS);
			}
			operate = true;
		} catch (Exception ex) {
			logger.error("TBox:{}数据库锁定发生异常:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
			operate = false;
		}
		return operate;
	}

	/**
	 * 正式锁定tbox，锁定后的tbox，如不解锁不再接受注册
	 * @param tboxSn
	 * @return
	 */
	public boolean lockTbox(String tboxSn) {
		boolean operate = false;
		try {
			tspPlatformClient.lockTboxComplete(tboxSn);
			logger.info("TBox:{}在数据库中正式锁定", tboxSn);
			operate = true;
		} catch (Exception ex) {
			logger.error("TBox:{}数据库锁定发生异常:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
			operate = false;
		}
		return operate;
	}

	/**
	 * 检查tbox对应的摄像头列表中是否包含当前摄像头编号
	 * @param vin
	 * @param tboxSn
	 * @param cameraNo
	 * @return
	 */
	public boolean checkCamera(String vin, String tboxSn, int cameraNo) {
		if (isTBoxDataNeedTransfer(tboxSn)) {
			// tbox 存在于RVM 的redis中
			ItRedisInfo itTboxInfo = getITTboxInfo(tboxSn);
			if (itTboxInfo == null) {
				logger.warn("RVM Redis TBox(SN:{}) Data invalid.", tboxSn);
				return true;
			} else {
				List<Integer> cameraSupportList = itTboxInfo.getCameraNumList();
				if (cameraSupportList == null) {
					logger.warn("RVM Redis TBox(SN:{}) CameraList Data is null", tboxSn);
					return true;
				} else {
					return !cameraSupportList.contains(cameraNo);
				}
			}
		} else {
			return (tspPlatformClient.checkCamera(vin, String.valueOf(cameraNo)) != 1);
		}
	}
	
	/**
     * @return boolean
     * @Description 判断Redis中是否存在同一条Fota控制指令
     * 				通用方法
     * @Date 2019/1/22 18:01
     * @Param [serialNumber, operationName]
     **/
    public boolean existFotaCtrl(String serialNumber, String operationName) {
        try {
            return redisAPI.hasKey(RedisConstant.FOTA_REMOTE_CTRL_REQ, operationName + serialNumber);
        } catch (Exception ex) {
            logger.error("Redis连接异常, TBox(SN:{})判断Redis中是否存在FOTA控制指令因异常失败:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return false;
        }
    }

	/**
	 * @return boolean
	 * @Description 判断当前TBox是否存在FOTA对应控制指令或唤醒指令
	 * 				FOTA通用方法
	 * 				key:FOTA_REMOTE_CTRL
	 * @Date 2019/1/25 15:56
	 * @Param serialNumber
	 * @Param operationName 对应的控制操作名称
	 **/
	public boolean isFotaCtrlExist(String serialNumber, String operationName) {
		try {
			boolean wakeUp = existWakeUp(serialNumber, operationName);
			boolean existCtrl = existFotaCtrl(serialNumber, operationName);
			logger.warn("TBox(SN:{})的Redis会话唤醒状态:{}, 控制指令存在状态:{}", serialNumber, wakeUp, existCtrl);
			return wakeUp || existCtrl;
		} catch (Exception e) {
			logger.error("TBox(SN:{})查询对应FOTA指令是否存在或是否存在唤醒指令因发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
			return false;
		}
	}

	/**
	 * 组合远控的排他判断
	 *
	 * @param serialNumber
	 * @return
	 */
	public boolean isRmtGroupCtrlExist(String serialNumber) {
		boolean existRequst = false;
		// 如果Redis可以访问，使用redis进行查询
		boolean wakeUpStatus = existWakeUp(serialNumber, OperationConstant.RM_GROUP_CTRL);
		boolean rmtGroupCtrlStatus = existTboxRemoteCtrl(serialNumber);
		logger.warn("TBox:{}的Redis会话唤醒状态:{}, 远程组合控制指令存在状态:{}", serialNumber, wakeUpStatus, rmtGroupCtrlStatus);
		existRequst = wakeUpStatus || rmtGroupCtrlStatus;
		return existRequst;
	}
	
	/**
     * @return boolean
     * @Description 判断是否存在同一条Fota版本查询指令
     * @Date 2019/1/22 18:01
     * @Param [serialNumber, versionQueryCommand]
     **/
    public boolean existFotaVersionQuery(String serialNumber, String versionQueryCommand) {
        try {
            return redisAPI.hasKey(RedisConstant.FOTA_VERSION_QUERY_REQ, versionQueryCommand + serialNumber);
        } catch (Exception ex) {
            logger.error("Redis connection error,TBox({}) can't do existFotaVersionQuery:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));
            return false;
        }
    }

	/**
	 * @return boolean
	 * @Description 判断redis中是否存在wakeUp指令或远程控制指令
	 * 适用范围:
	 *       适用于所有远控
	 * 适用条件:
	 *       WakeUp命令保存格式 :		key : WAKE_UP_SerialNumber			value : CurrentTime
	 *       Command命令保存格式 :	key : COMMAND_SEND_SerialNumber		value : OperationName_CurrentTime
	 * @Date 2019/3/1 16:11
	 * @Param [serialNumber]
	 **/
	public boolean isWakeUpOrCommandExist(String serialNumber) {
		boolean resultStatus;
		try {
			boolean wakeUpStatus = existWakeUp(serialNumber);
			boolean commandStatus = redisAPI.hasKey(RedisConstant.COMMAND_SEND + "_" + serialNumber);
			resultStatus = wakeUpStatus || commandStatus;
			logger.info("TBox(SN:{})当前远控请求命令存在状态:{}, 唤醒指令存在状态:{}, 控制指令存在状态:{}", resultStatus, wakeUpStatus, commandStatus);
		} catch (Exception e) {
			logger.error("TBox(SN:{})当前远控指令排他操作查询redis过程发生异常失败, 异常原因:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(e));
			resultStatus = false;
		}
		return resultStatus;
	}
}
