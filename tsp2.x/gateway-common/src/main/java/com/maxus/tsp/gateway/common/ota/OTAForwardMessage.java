package com.maxus.tsp.gateway.common.ota;

import com.maxus.tsp.common.util.ThrowableUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.maxus.tsp.common.util.ByteUtil;
//import com.maxus.tsp.common.util.ThrowableUtil;
//import com.maxus.tsp.gateway.common.constant.OTAMessageOffset;
import com.maxus.tsp.gateway.common.constant.OTAMessagePartSize;

/**
 * 透传指令解析
 * @author uwczo
 *
 */
public class OTAForwardMessage {
	private boolean isAnalysed = false;
	Logger logger = LogManager.getLogger(OTAForwardMessage.class);
	/// 指令
	private byte[] command = new byte[OTAMessagePartSize.CMD_SIZE.value()];
	/// 指令长度
	private byte[] _ParamSize = new byte[OTAMessagePartSize.PARAM_LENGTH_SIZE.value()];
	/// 指令
	private byte[] _Param = {};

	/// 记录当前完整报文
	public byte[] CurMessage;


		public OTAForwardMessage() {
			byte[] empty = {};
			CurMessage = empty;
		}

		/**
		 * 根据接收到的报文进行构造
		 * @param oriMessage
		 */
		public OTAForwardMessage(byte[] oriMessage) {
			CurMessage = oriMessage;
			// 只解析消息通用格式，没有解析、解密命令报文
			AnalysisCommonMSG();
		}
	/**
	 * @Title: setCommand
	 * @Description: 设置命令值
	 * @param: @param
	 *             command
	 * @return: void
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月14日 上午8:41:30
	 */
	public void setCommand(Short command) {
		this.command = ByteUtil.short2Byte(command);
	}

	/**
	 * @Title: getCommand
	 * @Description: 获取协议的命令值
	 * @param: @return
	 * @return: short
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月13日 上午11:15:56
	 */
	public short getCommand() {
		return ByteUtil.byte2Short(command);
	}

	/**
	 * @Title: getParam
	 * @Description: 获取param
	 * @param: @return
	 * @return: byte[]
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月17日 下午4:34:47
	 */
	public byte[] getParam() {
		return _Param;
	}

	/// <summary>
	/// 获取当前报文长度大小
	/// </summary>
	/// <returns></returns>
	private int getParamSize() {
		return ByteUtil.byte2Short(_ParamSize);
	}
	private boolean AnalysisCommonMSG() {
		isAnalysed = false;
		// 获取报文前两个字节是否与规范一致，如果不一致，则不进行解析
		try {
			if (CurMessage.length <= 4) {
				logger.warn("Forward Message format error:{} ", ByteUtil.byteToHex(CurMessage));
			}
			// 获取指令
			System.arraycopy(CurMessage, 0, command, 0,
					OTAMessagePartSize.CMD_SIZE.value());
			// 参数长度大小
			System.arraycopy(CurMessage, 2, _ParamSize, 0,
					OTAMessagePartSize.PARAM_LENGTH_SIZE.value());
			// 判断实际参数长度大小一致
			int paramLen = getParamSize();
			if(CurMessage.length == 4 + paramLen) {
				// 获取实际参数
				_Param = new byte[paramLen];
				System.arraycopy(CurMessage, 4, _Param, 0, paramLen);
				isAnalysed = true;
			} else {
				logger.warn("Forward Message Parameter Length error:{} ", ByteUtil.byteToHex(CurMessage));
			}
		} catch (Exception e) {
			//logger.info("Analysis Forward Message Failed: " + e.getMessage());
			logger.error("Analysis Forward Message Failed:{} ", ThrowableUtil.getErrorInfoFromThrowable(e));
			isAnalysed = false;
		}
		// Console.WriteLine("Analysis Result: "+ isAnalysed);
		return isAnalysed;
	}

	/**
	 * @return the isAnalysed
	 */
	public boolean isAnalysed() {
		return isAnalysed;
	}
}
