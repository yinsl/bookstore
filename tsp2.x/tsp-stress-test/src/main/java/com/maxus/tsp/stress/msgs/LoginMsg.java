package com.maxus.tsp.stress.msgs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.RSAUtil;
import com.maxus.tsp.common.util.StringUtil;
import com.maxus.tsp.stress.BusinessSuper;
import com.maxus.tsp.stress.GroupPackage;
import com.maxus.tsp.stress.TboxCommand;

@Component
public class LoginMsg extends BusinessSuper {
	Logger log = LoggerFactory.getLogger(LoginMsg.class);

	public String getLoginParam(String secureKey, String majorVer,
			String minorVer) {
		secureKey = StringUtil.dataLengthsCheck(secureKey.trim(), 32);
		majorVer = StringUtil.dataLengthsCheck(majorVer.trim(), 2);
		minorVer = StringUtil.dataLengthsCheck(minorVer.trim(), 2);
		String currentDate = StringUtil.getHexDateString();

		String mgs = secureKey + majorVer + minorVer + currentDate;

		return mgs;
	}

	public String getLoginPackage(String sn, String encyptMode, String seqNum,
			String secureKey, String majorVer, String minorVer) {
		String command = TboxCommand.CMD_UP_LOGIN.getCode();
		GroupPackage gp = new GroupPackage();
		secureKey = StringUtil.dataLengthsCheck(secureKey, 16);
		secureKey = ByteUtil.getAscii(secureKey);
		String param = getLoginParam(secureKey, majorVer, minorVer);
		String message = gp.getMessagess(sn, encyptMode, command, seqNum, param);
		log.info("Raw message ："+ message );
		String em = StringUtil.dataLengthsCheck(encyptMode, 2);
		String res = null;
		switch(em){
		case "00": 
			res = message;
			break;
		case "01":
			String context = message.substring(42);
			log.info("需要加密的内容 = " +context);
			log.info("需要加密的内容长度 = " +context.length());
			String rsacontext = RSAUtil.getTboxRSA(context, "d:/test/0801/pkcs1-private.pem", "d:/test/0801/pkcs1-public.pem");
			//log.info("加密后的内容 = " + rsacontext);
			//log.info("加密后内容长度 = " + rsacontext.length());			
			res = gp.getEncryptMessage(sn, em, rsacontext);
		
			break;
		case "02" :
			break;
			
		case "03" :
			break;
		}
		if(("01").equals(encyptMode)){}
		
		return res;
	}

	@Override
	public String resolveMessage(String message) {
		String secureKey;
		String majorVer;
		String minorVer;
		String currentTime;

		String result = null;
		if (message.length() != 8) {
			// 上行
			secureKey = message.substring(0, 32);
			secureKey = ByteUtil.parseAscii(secureKey);
			majorVer = message.substring(32, 34);
			minorVer = message.substring(34, 36);
			currentTime = message.substring(36, 50);

			majorVer = StringUtil.hextoString(majorVer);
			minorVer = StringUtil.hextoString(minorVer);
			currentTime = StringUtil.hextoDateString(currentTime);

			result = StringUtil.logFormat("密钥", secureKey)
					+ StringUtil.logFormat("主版本号", majorVer)
					+ StringUtil.logFormat("副版本号", minorVer)
					+ StringUtil.logFormat("时间", currentTime);

		} else {
			// 下行
			switch (message) {
			case "00000000":
				result = "登录成功";
				break;
			case "00000001":
				result = "T-Box公钥不存在";
				break;

			case "00000002":
				result = "协议版本不正确";
				break;
			case "00000004":
				result = "给定的TBox序列号不存在";
				break;
			default:
				result = "其它错误";
				break;
			}

			result = StringUtil.logFormat("状态", result);
		}

		return result;
	}
	
}	

