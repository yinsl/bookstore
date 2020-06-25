/**
 * OpCarRemoteControl.java Create on 2017年7月14日
 * Copyright (c) 2017年7月14日 by 上汽集团商用车技术中心
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>
 * @version 1.0
 */
package com.maxus.tsp.gateway.common.ota;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
//import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

//import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
//import com.maxus.tsp.common.redis.SIRedisAPI;
import com.maxus.tsp.common.util.ByteUtil;
//import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTACommand;
import com.maxus.tsp.gateway.common.constant.OTADrtRepMessagePartSize;
import com.maxus.tsp.gateway.common.constant.OTAEncrptMode;
import com.maxus.tsp.gateway.common.constant.OTAForwardCommand;
import com.maxus.tsp.gateway.common.constant.OTAForwardMessageOffset;
import com.maxus.tsp.gateway.common.constant.OTAForwardMessagePartSize;
import com.maxus.tsp.gateway.common.constant.OTAMessageOffset;
import com.maxus.tsp.gateway.common.constant.OTAMessagePartSize;
import com.maxus.tsp.gateway.common.constant.OTARemoteCommand;
import com.maxus.tsp.gateway.common.constant.OperationConstant;
import com.maxus.tsp.gateway.common.model.PoiData;


/**
 * @ClassName: OpCarRemoteControl.java
 * @Description: 构造远程控制、透传指令等下行报文
 * @author 任怀宇 余佶 梁潇
 * @version V1.0
 * @Date 2017年7月14日 上午10:07:00
 */
public class OpCarRemoteControl {
	static Logger logger = LogManager.getLogger(OpCarRemoteControl.class);

	/**
	 * 根据拍照事件的时间生成序列号
	 * @param eventTime
	 * @return
	 */
	public static String getShootIDByEventTime(String eventTime)
	{
		String shootID = null;
		if (!StringUtils.isEmpty(eventTime)) {
			shootID = eventTime.replace(" ", "").replace("-", "").replace(":", "");
		}
		return shootID;
	}

	private static AtomicInteger _serianlNo = new AtomicInteger(0);
	// 获取当前的报文的seq no
	public static byte[] getCurrentSeqNo() {
		byte[] curSeqNo;
		// 线程安全加1
		curSeqNo = ByteUtil.int2Byte(_serianlNo.incrementAndGet());
		if (_serianlNo.incrementAndGet() == Integer.MAX_VALUE - 1) {
			_serianlNo = new AtomicInteger(0);
		}
		return curSeqNo;
	}

	/// <summary>
	/// 远程控制下行指令构造
	/// </summary>
	/// <param name="code"></param>
	/// <param name="comd"></param>
	/// <param name="Cmvalue"></param>
	/// <param name="Encrypt"></param>
	/// <returns></returns>
	public static OTAMessage getSendData(String code, OTARemoteCommand comd, String Cmvalue, String Encrypt) {
		byte[] _SendData = new byte[24]; // Enumerable.Repeat((byte)0x00,
										// 24).ToArray();
		OTAMessage requestMsg = new OTAMessage();
		try {

			// 创建新的Header
			System.arraycopy(OTAMessage.BeginSign, 0, requestMsg.getMsgHeader(),
					OTAMessageOffset.BEGIN_SIGN_OFFSET.value(), OTAMessagePartSize.BEGIN_SIGN_SIZE.value());
			// code= serial number
			// ascii encode
			byte[] curSN = ByteUtil.stringToBytes(code.trim());
			if (curSN.length == 15) {
				byte[] fullSN = new byte[16];
				fullSN[0] = 0x20;
				System.arraycopy(curSN, 0, fullSN, 1, 15);
				System.arraycopy(fullSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else if (curSN.length == 16) {
				System.arraycopy(curSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else {
				return requestMsg;
			}
			// 设置加密方式
			if (GlobalSessionChannel.getAESKey(code) != null) {
				requestMsg.setEncryptType(OTAEncrptMode.AES);
			}
			// 设置CMD字
			requestMsg.setCommand(OTACommand.CMD_DOWN_REMOTECTRL.value());
			// 创建远程控制的命令内容
			byte[] RemoteCtrlParam = new byte[12];
			byte[] RemoteCtrl_Command = {0x00, 0x00};
			byte[] RemoteCtrl_Flg = {0x00, 0x00};

			switch (comd) {
			case Search: // 控制鸣笛 bit 0: 1：鸣笛；0：无效;控制闪灯 bit 1: 1：闪灯；0：无效
				if (Cmvalue.equals("11")) { // 11打开，EE失效
					RemoteCtrl_Command[1] |= 0x03;
				}
				break;
			case ControlDoor: // 远程控车门 bit 2: 1：上锁解锁；0：无效
				RemoteCtrl_Command[1] |= 0x04; // bit 0:
												// 1：上锁；0：解锁。Command的bit2为1时，此位才有意义
				if (Cmvalue.equals("00")) { // 关闭，上锁
					RemoteCtrl_Flg[1] |= 0x01;
				} else if (Cmvalue.equals("11")) { // 打开，解锁
					RemoteCtrl_Flg[1] |= 0x00;
					//Flag bit 11~12 3：所有门和后备箱 (其余走组合远控)
					RemoteCtrl_Flg[0] |= 0x18;
				}
				break;
			case AirConditioning: // 远程空调 bit 3: 1：开关空调；0：无效
				RemoteCtrl_Command[1] |= 0x08; // bit 1:
												// 1：开空调；0：关空调。Command的bit3为1时，此位才有意义
				if (Cmvalue.equals("00")) { // 关闭
					RemoteCtrl_Flg[1] |= 0x00;
				} else if (Cmvalue.equals("11")) { // 打开
					RemoteCtrl_Flg[1] |= 0x02;
				}
				break;
			case TemperatureSetting: // 远程温度设置 bit 4: 1：设置空调温度；0：无效
				RemoteCtrl_Command[1] |= 0x10; // 空调温度。Command的bit4为1时，此数据项才有意义
				System.arraycopy(ByteUtil.short2Byte(Short.valueOf(Cmvalue)), 1, RemoteCtrlParam, 2, 1);
				break;
			case SeatheatingFrontLeft: // 远程座椅加热（左前） bit 5: 1：座椅加热；0：无效
				RemoteCtrl_Command[1] |= 0x20;
				if (!Cmvalue.equals("00")) { // bit 2:
										// 1：左前座椅加热；0：左前座椅停止加热。Command的bit5为1时，此位才有意义
					RemoteCtrl_Flg[1] |= 0x04;
				}
				break;
			case SeatheatingRearLeft: // 远程座椅加热（左后） bit 5: 1：座椅加热；0：无效
				RemoteCtrl_Command[1] |= 0x20;
				if (!Cmvalue.equals("00")) {// bit 3:
										// 1：左后座椅加热；0：左后座椅停止加热。Command的bit5为1时，此位才有意义
					RemoteCtrl_Flg[1] |= 0x08;
				}
				break;
			case SeatheatingFrontRight: // 远程座椅加热（右前） bit 5: 1：座椅加热；0：无效
				RemoteCtrl_Command[1] |= 0x20;
				if (!Cmvalue.equals("00")) { // bit 4:
										// 1：右前座椅加热；0：右前座椅停止加热。Command的bit5为1时，此位才有意义
					RemoteCtrl_Flg[1] |= 0x10;
				}
				break;
			case SeatheatingRearRight: // 远程座椅加热（右后） bit 5: 1：座椅加热；0：无效
				RemoteCtrl_Command[1] |= 0x20;
				if (!Cmvalue.equals("00")) { // bit 5:
										// 1：右后座椅加热；0：右后座椅停止加热。Command的bit5为1时，此位才有意义
					RemoteCtrl_Flg[1] |= 0x20;
				}
				break;
			case LimitSpeed: // 远程车辆跛行  bit 6: 1：限速；0：无效
				RemoteCtrl_Command[1] |= 0x40;
				if (!Cmvalue.equals("00")) { // bit 6:
										// 1：进行限速；0：解除限速。Command的bit6为1时，此位才有意义
					RemoteCtrl_Flg[1] |= 0x40;
				}
				break;
			default:
				logger.info("Http Request Command Error:" + comd);
				return requestMsg;
			}
			// 拼接远程控制的参数
			System.arraycopy(RemoteCtrl_Command, 0, RemoteCtrlParam, 0, 2);
			System.arraycopy(RemoteCtrl_Flg, 0, RemoteCtrlParam, 3, 2);
			Calendar now = Calendar.getInstance();
			System.arraycopy(ByteUtil.CreateDateTimeBytes(now), 0, RemoteCtrlParam, 5, 7);

			requestMsg.setParamSize(ByteUtil.short2Byte((short) RemoteCtrlParam.length));
			requestMsg.setParam(RemoteCtrlParam);
			// 报文序号
			requestMsg.setSeqNum(getCurrentSeqNo());
			// 创建下发报文
			requestMsg.createMessageFromParts();
			_SendData = requestMsg.curMessage;
			// String b = BitConverter.ToString(_SendData).Replace("-"," ");
			logger.info("DownSendData: " + ByteUtil.byteToHex(_SendData));
		} catch (Exception e) {
			logger.error("远程控制指令构造发生异常 " + "Tbox=" + code + "comd=" + comd + "Cmvalue=" + Cmvalue + "Encrypt"
					+ Encrypt + "异常：", e);
		}
		return requestMsg;
	}

	/**
	* @Title:        getSendData
	* @Description:远程抓拍命令生成工具类
	* @param:        @param code
	* @param:        @param comd
	* @param:        @param Encrypt
	* @param:        @return
	* @return:       OTAMessage
	* @throws
	* @author        Administrator
	* @Date          2017年8月21日 下午4:16:49
	 */
	public static OTAMessage getSendData(String code, OTARemoteCommand comd, String Encrypt) {
		byte[] _SendData = new byte[24];// Enumerable.Repeat((byte)0x00,
										// 24).ToArray();
		OTAMessage requestMsg = new OTAMessage();
		try {

			// 创建新的Header
			System.arraycopy(OTAMessage.BeginSign, 0, requestMsg.getMsgHeader(),
					OTAMessageOffset.BEGIN_SIGN_OFFSET.value(), OTAMessagePartSize.BEGIN_SIGN_SIZE.value());
			// code= serial number
			// ascii encode
			byte[] curSN = ByteUtil.stringToBytes(code.trim());
			if (curSN.length == 15) {
				byte[] fullSN = new byte[16];
				fullSN[0] = 0x20;
				System.arraycopy(curSN, 0, fullSN, 1, 15);
				System.arraycopy(fullSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else if (curSN.length == 16) {
				System.arraycopy(curSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else {
				return requestMsg;
			}
			/*// 设置加密方式
			if(GlobalSessionChannel.getAESKey(code)!=null)
				requestMsg.getMsgHeader()[OTAMessageOffset.ENCRYPT_MODE_OFFSET.value()] = 0x02;*/
			// 设置CMD字:远程抓拍
			//requestMsg.setCommand(OTACommand.CMD_DOWN_CAPTURE.value());

			requestMsg.setParamSize(ByteUtil.short2Byte((short) 0));
			// 报文序号
			requestMsg.setSeqNum(getCurrentSeqNo());
			// 创建下发报文
			requestMsg.createMessageFromParts();
			_SendData = requestMsg.curMessage;
			// String b = BitConverter.ToString(_SendData).Replace("-"," ");
			logger.info("DownSendData: " + ByteUtil.byteToHex(_SendData));
			// OpRedisRecord.saveRecord<String>(OpCarRemoteControl.OpRemoteCtrlRequestHeader
			// + code, comd); //记录操作
			// RedisHelper rd = new RedisHelper();
			// rd.StringSet(OpCarRemoteControl.OpRemoteCtrlRequestHeader + code,
			// comd);

			logger.info(" DownSendData Complete");

		} catch (Exception e) {
		//	e.printStackTrace();
			logger.error("Percy DownSendData " + "code=" + code + "comd=" + comd + "Encrypt="
					+ Encrypt + "E=", e);
			// throw E;
		}
		return requestMsg;
	}

	private static byte[] creatTakePhoteParam(String cameraList, String shootID) {
		String[] cameraNoList = cameraList.split(",");
		int count = cameraNoList.length;
		int messageLength = OTAForwardMessagePartSize.COMMAND_SIZE.value()
				+ OTAForwardMessagePartSize.PARAM_SIZE_SIZE.value()
				+ OTAForwardMessagePartSize.SHOOT_ID_SIZE.value()
				+ OTAForwardMessagePartSize.CAMERA_NUM_SIZE.value() + count;
		int parameterLength = OTAForwardMessagePartSize.SHOOT_ID_SIZE.value()
				+ OTAForwardMessagePartSize.CAMERA_NUM_SIZE.value() + count;
		try {
			byte[] forwardMsg = new byte[messageLength];
			//拍照下行指令
			System.arraycopy(ByteUtil.short2Byte(OTAForwardCommand.CMD_DOWN_TAKE_PHOTO.value()), 0, forwardMsg,
					OTAForwardMessageOffset.COMMAND_OFFSET.value(), OTAForwardMessagePartSize.COMMAND_SIZE.value());
			//拍照下行指令参数长度
			System.arraycopy(ByteUtil.short2Byte((short) parameterLength), 0, forwardMsg,
			OTAForwardMessageOffset.PARAM_SIZE_OFFSET.value(), OTAForwardMessagePartSize.PARAM_SIZE_SIZE.value());
			//拍照序列号
			byte[] shootIDByte = new byte[OTAForwardMessagePartSize.SHOOT_ID_SIZE.value()];
			byte[] tempIDByte = new byte[OTAForwardMessagePartSize.SHOOT_ID_SIZE.value() - 2];
			shootIDByte[0]=0x00;
			shootIDByte[1]=0x0E;

			tempIDByte = ByteUtil.stringToBytes(shootID);

			System.arraycopy(tempIDByte, 0, shootIDByte, 2, OTAForwardMessagePartSize.SHOOT_ID_SIZE.value() - 2);
			logger.info(ByteUtil.byteToHex(shootIDByte));
			System.arraycopy(shootIDByte, 0, forwardMsg, OTAForwardMessageOffset.PARAM_OFFSET.value(),
					OTAForwardMessagePartSize.SHOOT_ID_SIZE.value());
			//设置摄像头个数
			//System.arraycopy((byte)count, 0, forwardMsg, OTAForwardMessageOffset.CAMERA_NUM_DOWN_OFFSET.value(),
			//OTAForwardMessagePartSize.CAMERA_NUM_SIZE.value());
			forwardMsg[OTAForwardMessageOffset.CAMERA_NUM_DOWN_OFFSET.value()] = (byte) count;
			for (int i = 0; i < count; i++) {
				int cameraNo = Integer.parseInt(cameraNoList[i]);
				forwardMsg[ OTAForwardMessageOffset.CAMERA_LIST_OFFSET.value() + i] = (byte) (cameraNo);
				//System.arraycopy(Integer.valueOf(cameraNoList[i]), 0, forwardMsg,
				//OTAForwardMessageOffset.CAMERA_LIST_OFFSET.value()+i, 1);
			}
			return forwardMsg;
		} catch (Exception ex) {
			logger.error("Create Take Photo Parameter Message Error: ", ex);
			return null;
		}
	}

	/**
	* @Title:        getSendData
	* @Description:远程抓拍命令生成工具类
	* @param:        @param code
	* @param:        @param comd
	* @param:        @param Encrypt
	* @param:        @return
	* @return:       OTAMessage
	* @throws
	* @author        Administrator
	* @Date          2017年8月21日 下午4:16:49
	 */
	public static OTAMessage getSendData(String cmd, String serialNumber, String parameter, String happenTime) {
		byte[] _SendData = new byte[24];
		OTAMessage requestMsg = new OTAMessage();
		try {

			// 创建新的Header
			System.arraycopy(OTAMessage.BeginSign, 0, requestMsg.getMsgHeader(),
					OTAMessageOffset.BEGIN_SIGN_OFFSET.value(), OTAMessagePartSize.BEGIN_SIGN_SIZE.value());
			// code= serial number
			// ascii encode
			byte[] curSN = ByteUtil.stringToBytes(serialNumber.trim());
			if (curSN.length == 15) {
				byte[] fullSN = new byte[16];
				fullSN[0] = 0x20;
				System.arraycopy(curSN, 0, fullSN, 1, 15);
				System.arraycopy(fullSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else if (curSN.length == 16) {
				System.arraycopy(curSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else {
				return requestMsg;
			}
			// 设置加密方式
			if (GlobalSessionChannel.getAESKey(serialNumber) != null) {
				requestMsg.setEncryptType(OTAEncrptMode.AES);
			}

			requestMsg.setCommand(OTACommand.CMD_DOWN_FORWARD_4IVI.value());

			requestMsg.setParamSize(ByteUtil.short2Byte((short) 0));

			byte[] param = {};
			//创建指令
			switch (cmd) {
				case OperationConstant.FORWARD_TAKEPHOTO:
					//创建拍照序列号
					String shootID = getShootIDByEventTime(happenTime);
					param = creatTakePhoteParam(parameter, shootID);
					break;
				case OperationConstant.FORWARD_POI:
					param = creatPoiParam(parameter, happenTime);
					break;
				default:
					param = null;
					break;
			}
			if (param == null) { //没有对应的参数时，返回null
				return null;
			}
			requestMsg.setParam(param);

			requestMsg.setParamSize(ByteUtil.short2Byte((short) (param.length)));
			// 报文序号
			requestMsg.setSeqNum(getCurrentSeqNo());
			// 创建下发报文
			requestMsg.createMessageFromParts();
			_SendData = requestMsg.curMessage;

			logger.info("DownSendData: " + ByteUtil.byteToHex(_SendData));
		} catch (Exception e) {
			logger.error("远程抓拍指令构造发生异常。 " + "serial number" + serialNumber + "异常：", e);
		}
		return requestMsg;
	}

	private static byte[] creatPoiParam(String parameter, String happenTime) {
		 PoiData poiData = JSON.parseObject(parameter, PoiData.class);
		 int longitude = Math.abs(poiData.getLongitude());
		 int latitude = Math.abs(poiData.getLatitude());
		 String address = poiData.getAddress();
		 int gPSType = poiData.getgPSType();
		 int posType = poiData.getPosType();
		 try {
		 // String类型实际传输数据长度：数据长度+实际数据长度
		 int count = ByteUtil.stringUTF8ToBytes(address).length + 2;
		 int messageLength = OTAForwardMessagePartSize.COMMAND_SIZE.value()
					+ OTAForwardMessagePartSize.PARAM_SIZE_SIZE.value()
					+ OTAForwardMessagePartSize.LONGITUDE_SIZE.value()
					+ OTAForwardMessagePartSize.LATITUDE_SIZE.value()
					+ OTAForwardMessagePartSize.DATE_TIME_SIZE.value() + count// string长度
					+ OTAForwardMessagePartSize.GPSTYPE_SIZE.value()
					+ OTAForwardMessagePartSize.POSTYPE_SIZE.value();
		 int parameterLength = OTAForwardMessagePartSize.LONGITUDE_SIZE.value()
				 	+ OTAForwardMessagePartSize.LATITUDE_SIZE.value()
				 	+ OTAForwardMessagePartSize.DATE_TIME_SIZE.value() + count
				 	+ OTAForwardMessagePartSize.GPSTYPE_SIZE.value()
					+ OTAForwardMessagePartSize.POSTYPE_SIZE.value();		 
			 byte[] forwardMsg = new byte[messageLength];
			 // poi下行指令
		     System.arraycopy(ByteUtil.short2Byte(OTAForwardCommand.CMD_DOWN_POI.value()),
		    		 0, forwardMsg, OTAForwardMessageOffset.COMMAND_OFFSET.value(), OTAForwardMessagePartSize.COMMAND_SIZE.value());
			 // poi下行指令参数长度
		     System.arraycopy(ByteUtil.short2Byte((short) parameterLength), 0, forwardMsg,
		 			OTAForwardMessageOffset.PARAM_SIZE_OFFSET.value(), OTAForwardMessagePartSize.PARAM_SIZE_SIZE.value());
			 // longitude参数
		     System.arraycopy(ByteUtil.int2Byte(longitude), 0, forwardMsg,
			 		OTAForwardMessageOffset.LONGITUDE_OFFSET.value(), OTAForwardMessagePartSize.LONGITUDE_SIZE.value());
		     // latitude参数
		     System.arraycopy(ByteUtil.int2Byte(latitude), 0, forwardMsg,
			 		OTAForwardMessageOffset.LATITUDE_OFFSET.value(), OTAForwardMessagePartSize.LATITUDE_SIZE.value());
		     // address参数
		     byte addressByte[] = new byte[count];
		     System.arraycopy(ByteUtil.short2Byte((short)ByteUtil.stringUTF8ToBytes(address).length), 0, addressByte, 0, 2);
		     
		     System.arraycopy(ByteUtil.stringUTF8ToBytes(address), 0, addressByte, 2, ByteUtil.stringUTF8ToBytes(address).length);
		     System.arraycopy(addressByte, 0, forwardMsg, OTAForwardMessageOffset.ADDRESS_OFFSET.value(), count);
		     // date time
		     SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		     Date date =sdf.parse(happenTime);
		     Calendar datetime = Calendar.getInstance();
		     datetime.setTime(date);
		     byte[] datetimeBytes = ByteUtil.CreateDateTimeBytes(datetime);
		     System.arraycopy(datetimeBytes, 0, forwardMsg, OTAForwardMessageOffset.ADDRESS_OFFSET.value() + count,
		    		 OTAForwardMessagePartSize.DATE_TIME_SIZE.value());
		     
		     // postype
		     byte[] _postype = new byte[1];
		     _postype[0] = (byte) posType;
		     System.arraycopy(_postype, 0, forwardMsg,
		    		 OTAForwardMessageOffset.ADDRESS_OFFSET.value() + count + OTAForwardMessagePartSize.DATE_TIME_SIZE.value(), OTAForwardMessagePartSize.POSTYPE_SIZE.value());
		     // gPSType
		     byte[] _gPSType = new byte[1];
		     _gPSType[0] = (byte) gPSType;
		     System.arraycopy(_gPSType, 0, forwardMsg,
		    		 OTAForwardMessageOffset.ADDRESS_OFFSET.value() + count + OTAForwardMessagePartSize.DATE_TIME_SIZE.value() + OTAForwardMessagePartSize.POSTYPE_SIZE.value(), OTAForwardMessagePartSize.GPSTYPE_SIZE.value());
		     
		     return forwardMsg;
		 } catch (Exception ex) {
			logger.error("Create Poi Parameter Message Error: ", ex);
			return null;
		 }
	}

	/**
	* @Title:        getSendDataForUpdate
	* @Description:  远程升级命令生成工具类
	* @param:        @param serialNumber
	* @param:        @param version
	* @param:        @param url
	* @param:        @param md5
	* @param:        @return
	* @return:       OTAMessage
	* @throws
	* @author        Administrator
	* @Date          2017年8月21日 下午4:16:49
	 */
	public static OTAMessage getSendDataForUpdate(String serialNumber, String version, String url, String md5) {
		byte[] _SendData = new byte[24];

		OTAMessage requestMsg = new OTAMessage();
		try {

			// 创建新的Header
			System.arraycopy(OTAMessage.BeginSign, 0, requestMsg.getMsgHeader(),
					OTAMessageOffset.BEGIN_SIGN_OFFSET.value(), OTAMessagePartSize.BEGIN_SIGN_SIZE.value());
			// code= serial number
			// ascii encode
			byte[] curSN = ByteUtil.stringToBytes(serialNumber.trim());
			if (curSN.length == 15) {
				byte[] fullSN = new byte[16];
				fullSN[0] = 0x20;
				System.arraycopy(curSN, 0, fullSN, 1, 15);
				System.arraycopy(fullSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else if (curSN.length == 16) {
				System.arraycopy(curSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else {
				return requestMsg;
			}

			requestMsg.setCommand(OTACommand.CMD_DOWN_REMOTE_UPDATE.value());

			requestMsg.setParamSize(ByteUtil.short2Byte((short) 0));

			//创建指令
			int versionLen = version.length();
			int urlLen = url.length();
			if(md5.length() != 32 || versionLen <0 || urlLen <0) {
				logger.error("Update information Error. Please check version, url and md5 format in database.");
				return null;
			}
			byte[] param  = new byte[7 + 2 + versionLen + 2 + urlLen + 32];
			Calendar current = Calendar.getInstance();
			System.arraycopy(ByteUtil.CreateDateTimeBytes(current), 0, param, 0, 7);

			System.arraycopy(ByteUtil.short2Byte((short) (versionLen)), 0, param, 7, 2);
			System.arraycopy(ByteUtil.stringToBytes(version), 0, param, 9, versionLen);
			System.arraycopy(ByteUtil.short2Byte((short) (urlLen)), 0, param, 9 + versionLen, 2);
			System.arraycopy(ByteUtil.stringToBytes(url), 0, param, 11 + versionLen, urlLen);
			System.arraycopy(ByteUtil.stringToBytes(md5), 0, param, 11 + versionLen + urlLen, 32);

			requestMsg.setParam(param);

			requestMsg.setParamSize(ByteUtil.short2Byte((short) (param.length)));
			// 报文序号
			requestMsg.setSeqNum(getCurrentSeqNo());
			// 创建下发报文
			requestMsg.createMessageFromParts();
			_SendData = requestMsg.curMessage;
			logger.info("DownSendData: " + ByteUtil.byteToHex(_SendData));
		} catch (Exception e) {
			//e.printStackTrace();
			logger.error("getSendDataForUpdate " + "serial number" + serialNumber + "E=", e);
			// throw E;
		}
		return requestMsg;
	}

	/**
	* @Title:        getSendDataForAddBTKey
	* @Description:  新增蓝牙命令生成工具类
	* @param:        @param code
	* @param:        @param comd
	* @param:        @param Encrypt
	* @param:        @return
	* @return:       OTAMessage
	* @throws
	* @author        yuji
	* @Date          2017年11月16日 下午4:16:49
	 */
	public static OTAMessage getSendDataForAddBTKey(String serialNumber, String btKey, String startTime, String endTime) {
		byte[] _SendData = new byte[24];

		OTAMessage requestMsg = new OTAMessage();
		try {

			// 创建新的Header
			System.arraycopy(OTAMessage.BeginSign, 0, requestMsg.getMsgHeader(),
					OTAMessageOffset.BEGIN_SIGN_OFFSET.value(), OTAMessagePartSize.BEGIN_SIGN_SIZE.value());
			// code= serial number
			// ascii encode
			byte[] curSN = ByteUtil.stringToBytes(serialNumber.trim());
			if (curSN.length == 15) {
				byte[] fullSN = new byte[16];
				fullSN[0] = 0x20;
				System.arraycopy(curSN, 0, fullSN, 1, 15);
				System.arraycopy(fullSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else if (curSN.length == 16) {
				System.arraycopy(curSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else {
				return requestMsg;
			}

			requestMsg.setCommand(OTACommand.CMD_DOWN_ADD_BT_KEY.value());

			requestMsg.setParamSize(ByteUtil.short2Byte((short) 0));

			//创建指令
			int bklen = btKey.length();
			if(bklen != 16 || StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)){
				return null;
			}			
			byte[] param = new byte[7+7+bklen];
			SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
			Date startdate =sdf.parse(startTime);
			Date enddate =sdf.parse(endTime); 
			Calendar startcalendar = Calendar.getInstance(); 
			Calendar endcalendar = Calendar.getInstance(); 
			startcalendar.setTime(startdate);
			endcalendar.setTime(enddate);
			System.arraycopy(ByteUtil.CreateDateTimeBytes(startcalendar), 0, param, 0, 7);
			System.arraycopy(ByteUtil.CreateDateTimeBytes(endcalendar), 0, param, 7, 7);
			System.arraycopy(ByteUtil.stringToBytes(btKey), 0, param, 14, bklen);
			requestMsg.setParam(param);
			requestMsg.setParamSize(ByteUtil.short2Byte((short) (param.length)));
			// 报文序号
			requestMsg.setSeqNum(getCurrentSeqNo());
			// 创建下发报文
			requestMsg.createMessageFromParts();
			_SendData = requestMsg.curMessage;

			logger.info("DownSendData: " + ByteUtil.byteToHex(_SendData));
		} catch (Exception e) {
			logger.error("新增蓝牙指令构造发生异常。 " + "serial number" + serialNumber + "异常：", e);
		}
		return requestMsg;
	}

	/**
	* @Title:        getSendDataForDelBTKey
	* @Description:  删除蓝牙命令生成工具类
	* @param:        @param serialNumber
	* @param:        @param btKey
	* @param:        @return
	* @return:       OTAMessage
	* @throws
	* @author        yuji
	* @Date          2017年11月16日 下午4:16:49
	 */
	public static OTAMessage getSendDataForDelBTKey(String serialNumber, String btKey) {
		byte[] _SendData = new byte[24];
		OTAMessage requestMsg = new OTAMessage();
		try {

			// 创建新的Header
			System.arraycopy(OTAMessage.BeginSign, 0, requestMsg.getMsgHeader(),
					OTAMessageOffset.BEGIN_SIGN_OFFSET.value(), OTAMessagePartSize.BEGIN_SIGN_SIZE.value());
			// code= serial number
			// ascii encode
			byte[] curSN = ByteUtil.stringToBytes(serialNumber.trim());
			if (curSN.length == 15) {
				byte[] fullSN = new byte[16];
				fullSN[0] = 0x20;
				System.arraycopy(curSN, 0, fullSN, 1, 15);
				System.arraycopy(fullSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else if (curSN.length == 16) {
				System.arraycopy(curSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else {
				return requestMsg;
			}

			requestMsg.setCommand(OTACommand.CMD_DOWN_DEL_BT_KEY.value());

			requestMsg.setParamSize(ByteUtil.short2Byte((short) 0));

			//创建指令
			int bklen = btKey.length();
			if(bklen != 16){
				return null;
			}			
			byte[] param = new byte[bklen];
			System.arraycopy(ByteUtil.stringToBytes(btKey), 0, param, 0, bklen);
			requestMsg.setParam(param);
			requestMsg.setParamSize(ByteUtil.short2Byte((short) (param.length)));
			// 报文序号
			requestMsg.setSeqNum(getCurrentSeqNo());
			// 创建下发报文
			requestMsg.createMessageFromParts();
			_SendData = requestMsg.curMessage;

			logger.info("DownSendData: " + ByteUtil.byteToHex(_SendData));
		} catch (Exception e) {
			logger.error("蓝牙删除指令构造发生异常。 " + "serial number" + serialNumber + "异常：", e);
		}
		return requestMsg;
	}
	/**
	* @Title:        getSendDataForUpBTKey
	* @Description:  蓝牙更新命令生成工具类
	* @param:        @param serialNumber
	* @param:        @param oldBtKey
	* @param:        @param newBtKey
	* @param:        @param newStartTime
	* @param:        @param newEndTime
	* @param:        @return
	* @return:       OTAMessage
	* @throws
	* @author        Administrator
	* @Date          2017年8月21日 下午4:16:49
	 */
	public static OTAMessage getSendDataForUpBTKey(String serialNumber, String oldBtKey,
			String newBtKey, String newStartTime, String newEndTime) {
		byte[] _SendData = new byte[24];

		OTAMessage requestMsg = new OTAMessage();
		try {

			// 创建新的Header
			System.arraycopy(OTAMessage.BeginSign, 0, requestMsg.getMsgHeader(),
					OTAMessageOffset.BEGIN_SIGN_OFFSET.value(), OTAMessagePartSize.BEGIN_SIGN_SIZE.value());
			// code= serial number
			// ascii encode
			byte[] curSN = ByteUtil.stringToBytes(serialNumber.trim());
			if (curSN.length == 15) {
				byte[] fullSN = new byte[16];
				fullSN[0] = 0x20;
				System.arraycopy(curSN, 0, fullSN, 1, 15);
				System.arraycopy(fullSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else if (curSN.length == 16) {
				System.arraycopy(curSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else {
				return requestMsg;
			}

			requestMsg.setCommand(OTACommand.CMD_UP_GET_BT_KEY.value());

			requestMsg.setParamSize(ByteUtil.short2Byte((short) 0));

			//创建指令
//			int versionLen = version.length();
//			int urlLen = url.length();
//			if(md5.length() !=32 || versionLen <0 || urlLen <0)
//			{
//				logger.error("Update information Error. Please check version, url and md5 format in database.");
//				return null;
//			}
//			byte[] param  = new byte[7+2+versionLen+2+urlLen+32];
//			Calendar current = Calendar.getInstance();
//			System.arraycopy(ByteUtil.CreateDateTimeBytes(current), 0, param, 0, 7);

//			System.arraycopy(ByteUtil.short2Byte((short) (versionLen)), 0, param, 7, 2);
//			System.arraycopy(ByteUtil.stringToBytes(version), 0, param, 9, versionLen);
//			System.arraycopy(ByteUtil.short2Byte((short) (urlLen)), 0, param, 9+versionLen, 2);
//			System.arraycopy(ByteUtil.stringToBytes(url), 0, param, 11+versionLen, urlLen);
//			System.arraycopy(ByteUtil.stringToBytes(md5), 0, param, 11+versionLen+urlLen, 32);

//			requestMsg.getParam(param);

//			requestMsg.setParamSize(ByteUtil.short2Byte((short) (param.length)));
			// 报文序号
			requestMsg.setSeqNum(getCurrentSeqNo());
			// 创建下发报文
			requestMsg.createMessageFromParts();
			_SendData = requestMsg.curMessage;

			logger.info("DownSendData: " + ByteUtil.byteToHex(_SendData));

			logger.info(" DownSendData Complete");

		} catch (Exception e) {
			logger.error("蓝牙更新指令构造发生异常。 " + "serial number" + serialNumber + "异常：", e);
		}
		return requestMsg;
	}
	
	/**
	* @Title:        getSendDataForStartDrtRep
	* @Description:  开始往国家平台发数据报文生成工具类
	* @param:        @param serialNumber
	* @param:        @param ip
	* @param:        @param port
	* @param:        @return
	* @return:       OTAMessage
	* @throws
	* @author        Administrator
	* @Date          2018年6月7日 下午1:07:06
	 */
	public static OTAMessage getSendDataForStartDrtRep(String serialNumber, String ip, int port) {
		byte[] _SendData = new byte[24];
		OTAMessage requestMsg = new OTAMessage();
		try {
			// 创建新的Header
			System.arraycopy(OTAMessage.BeginSign, 0, requestMsg.getMsgHeader(),
					OTAMessageOffset.BEGIN_SIGN_OFFSET.value(), OTAMessagePartSize.BEGIN_SIGN_SIZE.value());
			byte[] curSN = ByteUtil.stringToBytes(serialNumber.trim());
			if (curSN.length == 15) {
				byte[] fullSN = new byte[16];
				fullSN[0] = 0x20;
				System.arraycopy(curSN, 0, fullSN, 1, 15);
				System.arraycopy(fullSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else if (curSN.length == 16) {
				System.arraycopy(curSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else {
				return requestMsg;
			}

			// 设置加密方式
			if (GlobalSessionChannel.getAESKey(serialNumber) != null) {
				requestMsg.setEncryptType(OTAEncrptMode.AES);
			}
			
			requestMsg.setCommand(OTACommand.CMD_DOWN_START_DIRECT_REPORT.value());

			requestMsg.setParamSize(ByteUtil.short2Byte((short) 0));

			//创建指令
			if (ip == null || ip.length() < 0 || port < 0 || port == 0xFFFF) {
				logger.error("开始往国家平台发数据指令参数错误");
				return null;
			}
			int ipLen = 2 + ip.length();
			byte[] param = new byte[ipLen + OTADrtRepMessagePartSize.PORT_SIZE.value() + OTADrtRepMessagePartSize.DATE_TIME_SIZE.value()];
			System.arraycopy(ByteUtil.short2Byte((short) (ip.length())), 0, param, 0, 2);
			System.arraycopy(ByteUtil.stringToBytes(ip), 0, param, 2, ip.length());
			System.arraycopy(ByteUtil.short2Byte((short) (port)), 0, param, ipLen, OTADrtRepMessagePartSize.PORT_SIZE.value());
			Calendar current = Calendar.getInstance();
			System.arraycopy(ByteUtil.CreateDateTimeBytes(current), 0, param, ipLen + 2, OTADrtRepMessagePartSize.DATE_TIME_SIZE.value());
			
			requestMsg.setParam(param);
			requestMsg.setParamSize(ByteUtil.short2Byte((short) (param.length)));
			// 报文序号
			requestMsg.setSeqNum(getCurrentSeqNo());
			// 创建下发报文
			requestMsg.createMessageFromParts();
			_SendData = requestMsg.curMessage;

			logger.info("DownSendData: " + ByteUtil.byteToHex(_SendData));
		} catch (Exception e) {
			logger.error("开始往国家平台发数据指令构造发生异常。 " + "serial number" + serialNumber + "异常：", e);
		}
		return requestMsg;
	}
	
	/**
	* @Title:        getSendDataForEndDrtRep
	* @Description:  停止往国家平台发数据报文生成工具类
	* @param:        @param serialNumber
	* @param:        @return
	* @return:       OTAMessage
	* @throws
	* @author        Administrator
	* @Date          2018年6月7日 下午1:44:16
	 */
	public static OTAMessage getSendDataForEndDrtRep(String serialNumber) {
		byte[] _SendData = new byte[24];
		OTAMessage requestMsg = new OTAMessage();
		try {
			// 创建新的Header
			System.arraycopy(OTAMessage.BeginSign, 0, requestMsg.getMsgHeader(),
					OTAMessageOffset.BEGIN_SIGN_OFFSET.value(), OTAMessagePartSize.BEGIN_SIGN_SIZE.value());
			byte[] curSN = ByteUtil.stringToBytes(serialNumber.trim());
			if (curSN.length == 15) {
				byte[] fullSN = new byte[16];
				fullSN[0] = 0x20;
				System.arraycopy(curSN, 0, fullSN, 1, 15);
				System.arraycopy(fullSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else if (curSN.length == 16) {
				System.arraycopy(curSN, 0, requestMsg.getMsgHeader(), OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(),
						OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
			} else {
				return requestMsg;
			}
			
			// 设置加密方式
			if (GlobalSessionChannel.getAESKey(serialNumber) != null) {
				requestMsg.setEncryptType(OTAEncrptMode.AES);
			}

			requestMsg.setCommand(OTACommand.CMD_DOWN_STOP_DIRECT_REPORT.value());

			requestMsg.setParamSize(ByteUtil.short2Byte((short) 0));

			//创建指令
			byte[] param = new byte[OTADrtRepMessagePartSize.DATE_TIME_SIZE.value()];
			Calendar current = Calendar.getInstance();
			System.arraycopy(ByteUtil.CreateDateTimeBytes(current), 0, param, 0, OTADrtRepMessagePartSize.DATE_TIME_SIZE.value());
			
			requestMsg.setParam(param);
			requestMsg.setParamSize(ByteUtil.short2Byte((short) (param.length)));
			// 报文序号
			requestMsg.setSeqNum(getCurrentSeqNo());
			// 创建下发报文
			requestMsg.createMessageFromParts();
			_SendData = requestMsg.curMessage;

			logger.info("DownSendData: " + ByteUtil.byteToHex(_SendData));
		} catch (Exception e) {
			logger.error("停止往国家平台发数据指令构造发生异常。 " + "serial number" + serialNumber + "异常：", e);
		}
		return requestMsg;
	}
}


