/**
 * OTAMessage.java Create on 2017年7月14日
 * Copyright (c) 2017年7月14日 by 上汽集团商用车技术中心
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>
 * @version 1.0
 */
package com.maxus.tsp.gateway.common.ota;

import java.io.File;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.maxus.tsp.common.constant.RedisConstant;
import com.maxus.tsp.common.redis.allredis.RedisAPI;
import com.maxus.tsp.common.util.AESUtil;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.MinioClientUtils;
import com.maxus.tsp.common.util.PKICAUtil;
import com.maxus.tsp.common.util.RSAUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
//import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTAEncrptMode;
import com.maxus.tsp.gateway.common.constant.OTAMessageOffset;
import com.maxus.tsp.gateway.common.constant.OTAMessagePartSize;

/**
 * @ClassName: OTAMessage.java
 * @Description:OTA指令解析
 * @author 任怀宇 余佶
 * @version V1.0
 * @Date 2017年7月14日 上午10:08:59
 */
@Component
public class OTAMessage {
	
	@Autowired
	private MinioClientUtils minioClientUtils;
	
	@Autowired
	private RedisAPI redisAPI;
	
	/**
	 */
	private static final String ETC_RASTSP_PKCS1_PRIVATE_PEM = "/etc/rsatsp/pkcs1-private.pem";
	//private static final String ETC_RASTSP_PKCS1_PRIVATE_PEM = "D:/etc/rsatsp/pkcs1-private.pem";
	//报文头标志
	public static final byte[] BeginSign = {0x23, 0x23};
	//网关证书标识
	private static String certId = "test";
	static Logger logger = LogManager.getLogger(OTAMessage.class);

	private String tboxSN = null;

	public static void initCertificationId(String cert){
		certId = cert;
	}

	/// <summary>
	/// default construct method
	/// </summary>
	public OTAMessage() {
		byte[] empty = {};
		curMessage = empty;
	}

	/**
	 * 根据接收到的报文进行构造
	 * @param oriMessage
	 */
	public OTAMessage(byte[] oriMessage) {
		curMessage = oriMessage;
		// 只解析消息通用格式，没有解析、解密命令报文
		analysisCommonMSG();
	}

	/**
	 * @param tboxPKey
	 *            the tboxPKey to set
	 */
	public void setTboxPKey(String tboxPKey) {
		//this.tboxPKey = tboxPKey;
	}

	/**
	 * 根据返回参数来获取对应的返回报文对象
	 * @param param
	 */
	public OTAMessage getResOTAMessage(byte[] param) {
		OTAMessage responseMsg = new OTAMessage();
		// 设置回包报文头部,复制请求报文头部
		responseMsg.otaHeader = this.otaHeader;

		responseMsg.tboxSerialNumber = this.tboxSerialNumber;
		responseMsg.command = this.command;
		responseMsg.command[0] = (byte) 0x80;

		// 设置回包参数长度和参数内容和报文序号
		responseMsg.otaSeqNum = otaSeqNum;
		responseMsg.otaParamSize = ByteUtil.short2Byte((short) (param.length));
		responseMsg.otaParam = param;
		return responseMsg;
	}
	
	/**
	 * 根据返回参数来获取对应的返回报文对象
	 * @param param
	 */
	public OTAMessage getResOTAMessage(OTAMessage responseMsg, byte[] param) {
		// 设置回包报文头部,复制请求报文头部
		responseMsg.otaHeader = this.otaHeader;

		responseMsg.tboxSerialNumber = this.tboxSerialNumber;
		responseMsg.command = this.command;
		responseMsg.command[0] = (byte) 0x80;

		// 设置回包参数长度和参数内容和报文序号
		responseMsg.otaSeqNum = otaSeqNum;
		responseMsg.otaParamSize = ByteUtil.short2Byte((short) (param.length));
		responseMsg.otaParam = param;
		return responseMsg;
	}

	private boolean isAnalysed = false;
	/// 起始符号 0x2323
	private static byte[] otaBeginSign = {(byte) 0x23, (byte) 0x23};

	/// 报文大小
	private byte[] otaMsgSize = new byte[OTAMessagePartSize.MESSAGE_SIZE_SIZE.value()];

	/// TBox序列号
	private byte[] tboxSerialNumber = new byte[OTAMessagePartSize.SERIAL_NUMBER_SIZE.value()];

	/// 加密方式
	private short encryptMode = 0;

	/// 头部报文段
	private byte[] otaHeader = new byte[OTAMessagePartSize.OTA_HEADER_SIZE.value()];

	/// 加密报文段
	private byte[] otaEncryptMessage = {};

	/// 指令
	private byte[] command = new byte[OTAMessagePartSize.CMD_SIZE.value()];
	/**
	 * @Title: getEncryptType
	 * @Description:获取加密方式
	 */
	public OTAEncrptMode getEncryptType() {
		return OTAEncrptMode.getByCode(encryptMode);
	}
	/**
	 * @Title: setEncryptType
	 * @Description:设置加密方式
	 */
	public void setEncryptType(OTAEncrptMode encryptMode){
		this.encryptMode = encryptMode.value();
	}
	/**
	 * @Title: setCommand
	 * @Description:设置命令值
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
	 * @Description:获取协议的命令值
	 * @param: @return
	 * @return: short
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月13日 上午11:15:56
	 */
	public synchronized short getCommand() {
		return ByteUtil.byte2Short(command);
	}

	/**
	 * @Title: getSerialNumber
	 * @Description:获取TBox编号
	 * @param: @return
	 * @return: String
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月13日 下午1:59:11
	 */
	public String getSerialNumber() {
		if (tboxSN == null) {
			tboxSN = ByteUtil.parseAscii(tboxSerialNumber)/*.trim()*/;
		}
		return tboxSN;
	}

	/// 序列号
	private byte[] otaSeqNum = new byte[OTAMessagePartSize.SEQ_NO_SIZE.value()];

	/// 指令长度
	private byte[] otaParamSize = new byte[OTAMessagePartSize.PARAM_LENGTH_SIZE.value()];

	/// 指令
	private byte[] otaParam = {};

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
		return otaParam;
	}

	/// 记录当前完整报文
	public byte[] curMessage;

	public byte[] getMsgHeader() {
		return this.otaHeader;
	}

	/// <summary>
	/// 获取当前报文长度大小
	/// </summary>
	/// <returns></returns>
	private int GetMsgSize() {
		return ByteUtil.getUnsignedInt(otaMsgSize);
	}

	/// <summary>
	/// RSA 加密部分，仅供测试使用
	/// </summary>
	/// <param name="oir"></param>
	/// <param name="publickey"></param>
	/// <returns></returns>
	// public byte[] EncryptBytesRSA(String oir, RSACryptoServiceProvider
	// publickey) {
	// byte[] encryptStr = RSAFromPkcs8.encryptData(oir, publickey, "ascii");
	// Console.WriteLine("Encrypt Result: " +
	// BitConverter.ToString(encryptStr));
	// return encryptStr;
	// }

	/**
	 * @Title: DecryptBytesRSA
	 * @Description: 该方法只能由登录报文调用， RSA 解密部分，由于原始加密字节为32个字节，在2048证书的加密解密单个block最大范围内，故直接解密,
	 * @param: @param
	 *             pKey
	 * @param: @return
	 * @return: byte[]
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月20日 上午10:56:48
	 */
	public byte[] DecryptBytesRSA(String pKey, String tboxSn) {
		// String tboxPubkey = "";
		byte[] result = new byte[] {};
		try {
			//先用TSP的私钥解密
			byte[] tempResult = RSAUtil.privateKeyDecrypt(new File(ETC_RASTSP_PKCS1_PRIVATE_PEM), otaEncryptMessage);
			logger.info("TBox(SN:{}) Decrypt Data:{}", tboxSn, ByteUtil.byteToHex(tempResult));

			if (pKey != null) {
				// 再用TBox的公钥解密
				result = RSAUtil.publicKeyDecrypt(pKey, tempResult);
				logger.info("TBox(SN:{}) Decrypt Message:{}", tboxSn, ByteUtil.byteToHex(result));
			} else {
				logger.warn("请先设置TBox公钥");
			}
		} catch (Exception ex) {
			logger.error("TBox(SN:{})DecryptBytesRSA Error:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
		}
		return result;
	}

	/**
	 * @method      DecryptBytesPKICA
	 * @description   pki/server 解密、验签
	 * @param certification TBox证书
	 * @return
	 * @author      zhuna
	 * @date        2019/3/4 10:29
	 */
	public byte[] DecryptBytesPKICA(String certification, String tboxSn){
		byte[] result = new byte[] {};
		try{
			logger.info("TBox(SN:{})开始TSP私钥解密，TBox公钥验签过程", tboxSn);
			//先用网关私钥解密
			byte[] tempResult = PKICAUtil.privateKeyDecrypt(otaEncryptMessage);
			logger.debug("TBox(SN:{}) 网关私钥解密 Data:{}", tboxSn, ByteUtil.byteToHex(tempResult));
			//再用TBox证书验签
			result = PKICAUtil.verifyAttachedSign(tempResult);
			logger.debug("TBox(SN:{}) TBox证书验签 Data:{}", tboxSn, ByteUtil.byteToHex(result));
		}catch (Exception ex){
			logger.error("TBox({}) DecryptBytes PKI/CA Error:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
		}
		return result;
	}
	
	/**
	 * @method      EncryptBytesPKICA
	 * @description   pki/server 加密、签名
	 * @param serialNumber
	 * @param certId  			网关证书标志符
	 * @param certification    TBox证书
	 * @return      
	 * @author      zhuna
	 * @date        2019/3/12 18:32
	 */
	public byte[] EncryptBytesPKICA(String serialNumber, String certId, String certification) {
		byte[] result = new byte[]{};
		try {
			logger.debug("TBox(SN:{}) 网关登录回复04加密原始报文段 Data:{},网关证书标识：{}，TBox的证书：{}", serialNumber, ByteUtil.byteToHex(otaEncryptMessage),certId, certification);
			//先用网关私钥签名
			byte[] tempResult = PKICAUtil.attachSign(certId, otaEncryptMessage);
			logger.debug("TBox(SN:{}) 网关私钥签名 Data:{}", serialNumber, ByteUtil.byteToHex(tempResult));
			//再用TBox公钥加密
			result = PKICAUtil.publicKeyEncrypt(certification, tempResult);
			logger.debug("TBox(SN:{}) TBox公钥加密 Data:{}", serialNumber, ByteUtil.byteToHex(result));
		} catch (Exception ex) {
			logger.error("TBox({}) EncryptBytes PKI/CA Error:{}", serialNumber, ThrowableUtil.getErrorInfoFromThrowable(ex));
		}
		return result;
	}


	/**
	 * @Title: DecryptBytesAES
	 * @Description: AES解密
	 * @param aesKey
	 * @return
	 */
	public byte[] DecryptBytesAES(String aesKey, String tboxSn) {
		byte[] result = new byte[] {};
		try {
			AESUtil aes = new AESUtil();
			result = aes.decrypt(otaEncryptMessage, ByteUtil.hexStringToBytes(aesKey));
			logger.info("TBox({}) Decrypt AES Message:{}", tboxSn, ByteUtil.byteToHex(result));

		} catch (Exception ex) {
			logger.error("TBox({}) DecryptBytesAES Error:{}", tboxSn, ThrowableUtil.getErrorInfoFromThrowable(ex));
		}
		return result;
	}

	/**
	 * @Title: EncryptBytesAES
	 * @Description: AES加密
	 * @param aesKey
	 * @return result
	 */
	public byte[] EncryptBytesAES(String aesKey) {
		byte[] result = new byte[] {};
		try {
			AESUtil aes = new AESUtil();
			result = aes.encrypt(otaEncryptMessage, ByteUtil.hexStringToBytes(aesKey));
			//logger.info("Encrypt Message:" + ByteUtil.byteToHex(result));

		} catch (Exception ex) {
			logger.error("EncryptBytesAES Error:{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
		}
		return result;
	}

	/// <summary>
	/// 根据加密方式，加密字节段内容
	/// </summary>
	/// <param name="encryptMode"></param>
	/// <param name="encryptBytes"></param>
	/// <returns></returns>
	private byte[] EncryptBytes(short encryptMode, byte[] originalBytes, String serialNumber) throws Exception {
		//logger.info("网关向TBox(SN:{})发送报文中的原始字节:{}", serialNumber, ByteUtil.byteToHex(originalBytes)));
		//System.out.println("Original Bytes:" + ByteUtil.byteToHex(originalBytes));
		byte[] encryptBytes = new byte[] {};
		String logName ="OTAResolveServerHandler.resolve -> procOTA -> DataProcessing.otaMsg2bytes -> OTAMessage.createMessageFromParts -> EncryptBytes: ";
		switch (OTAEncrptMode.getByCode(encryptMode)) {
		case AES:
			String aeskey = null;
			//获得aes key
			aeskey = redisAPI.getHash(RedisConstant.SECURITY_KEY, serialNumber);
			//得到AES加密密文
			if (aeskey != null && aeskey.length() == 32) {
				encryptBytes = EncryptBytesAES(aeskey);
			} else {
				logger.warn("AES Key empty or length error!");
			}
			TimeLogUtil.log(logName+" line 399 redis get aes key");
			break;
		case RC4:
			break;
		case PKI:
			//从证书服务器获取TBox证书
			String tboxCertfication = minioClientUtils.getBase64CertData(serialNumber);
			encryptBytes = EncryptBytesPKICA(serialNumber, certId, tboxCertfication);
			TimeLogUtil.log(logName+" line 407");
			break;
		case NONE:
		default:
			encryptBytes = originalBytes;
			break;
		}
		logger.info("网关向TBox(SN:{})发送报文中的原始字节:{},加密后的字节:{}", serialNumber, ByteUtil.byteToHex(originalBytes), ByteUtil.byteToHex(encryptBytes));
		//System.out.println("Encrypt Bytes:" + ByteUtil.byteToHex(encryptBytes));
		TimeLogUtil.log(logName+" line 416");
		return encryptBytes;

	}

	/// <summary>
	/// 根据 CurMessage 的内容解析各部分子数据的字节内容,不包含解密过程。
	/// 返回结果成功true, 失败false
	/// </summary>
	/// <returns></returns>
	/**
	 * @Title: AnalysisCommonMSG
	 * @Description: 初步解析报文
	 * @param: @return
	 * @return: isAnalysed
	 */
	public boolean analysisCommonMSG() {
		isAnalysed = false;
		// 获取报文前两个字节是否与规范一致，如果不一致，则不进行解析
		try {
			byte[] messageBegin = new byte[OTAMessagePartSize.BEGIN_SIGN_SIZE.value()];
			System.arraycopy(curMessage, OTAMessageOffset.BEGIN_SIGN_OFFSET.value(), messageBegin, 0,
					OTAMessagePartSize.BEGIN_SIGN_SIZE.value());
			if (ByteUtil.byteToHex(messageBegin).equals(ByteUtil.byteToHex(otaBeginSign))) { // 获取报文相关各子数据内容
				if (curMessage.length > OTAMessagePartSize.OTA_HEADER_SIZE.value()) {
					// 报文头部信息
					System.arraycopy(curMessage, OTAMessageOffset.BEGIN_SIGN_OFFSET.value(), otaHeader, 0,
							OTAMessagePartSize.OTA_HEADER_SIZE.value());
					// 报文大小
					System.arraycopy(curMessage, OTAMessageOffset.MESSAGE_SIZE_OFFSET.value(), otaMsgSize, 0,
							OTAMessagePartSize.MESSAGE_SIZE_SIZE.value());
					// TBox 序列号
					System.arraycopy(curMessage, OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(), tboxSerialNumber, 0,
							OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
					// 加密模式
					this.encryptMode = curMessage[OTAMessageOffset.ENCRYPT_MODE_OFFSET.value()];

					// 加密报文段
					int encryptMsgSize = this.GetMsgSize() - OTAMessagePartSize.OTA_HEADER_SIZE.value();
					otaEncryptMessage = new byte[encryptMsgSize];
					System.arraycopy(curMessage, OTAMessageOffset.ENCRYPT_FIELD_OFFSET.value(), otaEncryptMessage, 0,
							encryptMsgSize);
					isAnalysed = true;
				}
			}
		} catch (Exception e) {
			logger.error("AnalysisCommonMSG Failed:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
			isAnalysed = false;
		}
		// Console.WriteLine("Analysis Result: "+ isAnalysed);
		return isAnalysed;
	}

	/**
	 * @return isAnalysed
	 */
	public boolean isAnalysed() {
		return isAnalysed;
	}

	/**
	 * @Title: analysisCommand
	 * @Description: 解析加密数据段
	 * @param:
	 * @return: void
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月20日 上午9:53:53
	 */
	public void analysisCommand(byte[] decryptMessage) {
		// 按照加密方式，解析加密数据段
		// byte[] DecryptMessage = DecryptBytes(_EncryptMode[0],
		// ByteUtil.byteToHex(_SerialNumber).replaceAll("-", ""));
		// 没有传入密文，则直接使用原有原版密文（明文）
		if (decryptMessage == null) {
			decryptMessage = this.otaEncryptMessage;
		}
		//int encryptMsgSize = _EncryptMessage.length;
		if (ByteUtil.getCRC(decryptMessage,
				decryptMessage.length - 1) == (decryptMessage[decryptMessage.length - 1] & 0xFF)) { //
		// CRC校验对比，如果错误，则不进行下一步解析
            // 指令
            System.arraycopy(decryptMessage, 0, command, 0, OTAMessagePartSize.CMD_SIZE.value());
            // Message 序列号
            System.arraycopy(decryptMessage, 2, otaSeqNum, 0, OTAMessagePartSize.SEQ_NO_SIZE.value());
            // 参数长度
            System.arraycopy(decryptMessage, 6, otaParamSize, 0, OTAMessagePartSize.PARAM_LENGTH_SIZE.value());
            // 判断参数长度是否正确,正确，则获取参数内容，错误则表示解析错误
            int paramLength = ByteUtil.getUnsignedInt(otaParamSize);
            
            if ((9 + paramLength) != decryptMessage.length) {
				logger.warn("当前报文paramSize与msgSize不匹配, 报文分析错误!");
                this.isAnalysed = false;
            } else {
                otaParam = new byte[paramLength];
                System.arraycopy(decryptMessage, 8, otaParam, 0, paramLength);
                // 重新拼接新的CurMessage
                byte[] ResultMessage = new byte[otaHeader.length + decryptMessage.length];
                System.arraycopy(otaHeader, 0, ResultMessage, 0, otaHeader.length);
                System.arraycopy(decryptMessage, 0, ResultMessage, OTAMessageOffset.ENCRYPT_FIELD_OFFSET.value(), decryptMessage.length);
                short messageLenth = (short) (otaHeader.length + decryptMessage.length);
                System.arraycopy(ByteUtil.short2Byte(messageLenth), 0, ResultMessage, OTAMessageOffset.MESSAGE_SIZE_OFFSET.value(),
                		OTAMessagePartSize.MESSAGE_SIZE_SIZE.value());
                curMessage = ResultMessage;
                this.isAnalysed = true;
            }
		} else {
			logger.warn("CRC校验出错！原始报文：{}", ByteUtil.byteToHex(decryptMessage));
			this.isAnalysed = false;
		}
		
	}

	/// <summary>
	/// 根据现有各部分子数据的字节内容生成数据报文 CurMessage
	/// 返回结果成功true, 失败false
	/// </summary>
	/// <returns></returns>
	public boolean createMessageFromParts() {
		boolean creatRet = false;
		String logName ="OTAResolveServerHandler.resolve -> procOTA -> DataProcessing.otaMsg2bytes -> OTAMessage.createMessageFromParts: ";
		TimeLogUtil.log(logName+"方法开始");
		// 拼接报文
		// 如果设置的子数据根据规范有误，则认为拼接失败
		try {
			//根据加密方式进行加密
            int encryptMsgSize = command.length + otaParam.length + otaParamSize.length + otaSeqNum.length + OTAMessagePartSize.CRC_SIZE.value();
            otaEncryptMessage = new byte[encryptMsgSize];
            //得到密文字段
            //复制明文指令
            System.arraycopy(command, 0, otaEncryptMessage, 0, OTAMessagePartSize.CMD_SIZE.value());

            //复制明文序列号
            System.arraycopy(otaSeqNum, 0, otaEncryptMessage, 2, OTAMessagePartSize.SEQ_NO_SIZE.value());

            //复制参数长度
            System.arraycopy(otaParamSize, 0, otaEncryptMessage, 6, OTAMessagePartSize.PARAM_LENGTH_SIZE.value());

            //复制参数内容
            System.arraycopy(otaParam, 0, otaEncryptMessage, 8, otaParam.length);
            //明文最后一个字节设置为CRC校验值
            otaEncryptMessage[encryptMsgSize - 1] = (byte) ByteUtil.getCRC(otaEncryptMessage, encryptMsgSize - 1);
            //对加密段进行加密
            System.arraycopy(otaHeader, OTAMessageOffset.SERIAL_NUMBER_OFFSET.value(), tboxSerialNumber, 0,
            		OTAMessagePartSize.SERIAL_NUMBER_SIZE.value());
            //得到加密后字段
            byte[] EncryptMessage = EncryptBytes(encryptMode, otaEncryptMessage, this.getSerialNumber());
            if (EncryptMessage.length > 0) {
                //计算加密后全报文长度
                short curMsgLength = (short) (otaHeader.length + EncryptMessage.length);
                curMessage = new byte[curMsgLength];

                //加密方式需要根据指令情况进行设置，待开发
                //复制原有的header
                System.arraycopy(otaHeader, 0, curMessage, OTAMessageOffset.BEGIN_SIGN_OFFSET.value(),
                		OTAMessagePartSize.OTA_HEADER_SIZE.value());
                //设置加密模式
                System.arraycopy(ByteUtil.short2Byte((short) encryptMode), 1, curMessage, OTAMessageOffset.ENCRYPT_MODE_OFFSET.value(),
                		OTAMessagePartSize.ENCYPT_MODE_SIZE.value());
                //复制加密段内容
                System.arraycopy(EncryptMessage, 0, curMessage, OTAMessageOffset.ENCRYPT_FIELD_OFFSET.value(), EncryptMessage.length);

                //修改报文的长度字节
                otaMsgSize = ByteUtil.short2Byte(curMsgLength);

                //修改长度
                System.arraycopy(otaMsgSize, 0, curMessage, OTAMessageOffset.MESSAGE_SIZE_OFFSET.value(),
                		OTAMessagePartSize.MESSAGE_SIZE_SIZE.value());

                creatRet = true;
                TimeLogUtil.log(logName+"line 586");
            }
		} catch (Exception e) {
        	logger.error("Create Message Failed:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
        	creatRet = false;
        }
		return creatRet;
	}

	public void setParamSize(byte[] short2Byte) {
		this.otaParamSize = short2Byte;

	}

	public void setParam(byte[] remoteCtrlParam) {
		this.otaParam = remoteCtrlParam;

	}

	public void setSeqNum(byte[] currentSeqNo) {
		this.otaSeqNum = currentSeqNo;

	}
}
