package com.maxus.tsp.gateway.common.ota;

import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.gateway.common.constant.OTACommand;
import com.maxus.tsp.gateway.common.constant.OTAEncrptMode;
import com.maxus.tsp.gateway.common.constant.OTAMessageOffset;
import com.maxus.tsp.gateway.common.constant.OTAMessagePartSize;
import com.maxus.tsp.gateway.common.model.HomeCtrlItRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;
import java.util.Date;

/**
 * 房车家居远控  oat报文组装类
 * @ClassName HomeCtrlData.java
 * @Description 
 * @author zhuna
 * @date 2018年11月9日
 */
public class HomeCtrlData {
	
	private static Logger logger = LogManager.getLogger(HomeCtrlData.class);
	
	/**
	 * 房车家居远控 报文构造
	 * @return
	 * @author zhuna
	 * @date 2018年11月11日
	 */
	public static OTAMessage getSendDataForHomeCtrl(String tboxSN,HomeCtrlItRequest homeCtrl) {
		
		OTAMessage requestMsg = new OTAMessage();		
		try {
			//it给的报文长度
			int size = homeCtrl.getParamSize();	
			// 创建报文头
			System.arraycopy(OTAMessage.BeginSign, 0, requestMsg.getMsgHeader(),
					OTAMessageOffset.BEGIN_SIGN_OFFSET.value(), OTAMessagePartSize.BEGIN_SIGN_SIZE.value());
			byte[] curSN = ByteUtil.stringToBytes(tboxSN.trim());
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
			// 设置加密方式  AES加密
			if (GlobalSessionChannel.getAESKey(tboxSN) != null) {
				requestMsg.setEncryptType(OTAEncrptMode.AES);
			}
			// 设置Command
			requestMsg.setCommand(OTACommand.CMD_DOWN_HOME_CTRL.value());
			// 报文序号
			requestMsg.setSeqNum(OpCarRemoteControl.getCurrentSeqNo());
				
			//**********    参数构造       **********
			//房车家居远控参数  内部参数字节长度（2）+报文长度（size）+ CurrentDate、CurrentTime(7)
			byte[] homeCtrlParam = new byte[9 + size];			
			//设置内部参数长度字节 paramInnerSize（2字节）			
			byte[] paramInnerSize = ByteUtil.short2Byte((short) size);
			System.arraycopy(paramInnerSize, 0, homeCtrlParam, 0, 2);
			//param
			byte[] param = ByteUtil.hexStringToBytes(homeCtrl.getParam());
			System.arraycopy(param, 0, homeCtrlParam, 2, size);
			//时间
			//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//Date dateTime = sdf.parse(eventTime);
			Date dateTime = new Date(homeCtrl.getEventTime());			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dateTime);
			System.arraycopy(ByteUtil.CreateDateTimeBytes(calendar), 0, homeCtrlParam, 2 + size, 7);
			
			// 设置参数长度
			requestMsg.setParamSize(ByteUtil.short2Byte((short) homeCtrlParam.length));
			requestMsg.setParam(homeCtrlParam);			
			
			// 创建下发报文
			requestMsg.createMessageFromParts();		
			logger.info("房车家居远控 DownSendData: " + ByteUtil.byteToHex(requestMsg.curMessage));
		} catch (Exception e) {
			logger.error("组装房车家居远控报文失败，原因：{}", e);
			return null;
		}
		return requestMsg;
	}
	
	
}
