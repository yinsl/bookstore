package com.maxus.tsp.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MajorMinorUtil {

	private static Logger logger = LogManager.getLogger(MajorMinorUtil.class);
	// private byte[] originSN = new byte[16];
	private String major="";
	private String minor="";
	private boolean isAnalyseOK = false;
	
	public MajorMinorUtil(String serialNumber)
	{
		if(serialNumber.length()!=16)
		{
			logger.error("This serial number length is error，please check: "+serialNumber);
			isAnalyseOK = false;
		}
		else
		{
//			根据制定SN的规则： 生产序号为 0001： 转换为二进制为    0000 0000 0000 0001           （每字节数字最大为9，所以我们只要低4bit，高4bit舍弃不用）
//			生产日期为 19          转换为二进制为     0000 0001 0000  1001         （高字节最大值为3，低字节最大值为9，所以取高字节 bit 0和bit1， 低字节的 bit0~bit3）
//			生产月份为9             转换为二进制为      0000 1001                              （最大值为12， 所以我们取bit0 ~ bit3）
//			生产年份为H            代表2017的意思   减去2017 为   0000 00          (最大值为0b111111)
//			Major（16位）=   0000   0000   0000    0001          （ SN号每个字节低4bit ）
//			Minor（16位）=   0000   0010    0101   1001          （ 01   1001    生产日期 ，10    01 生产月份， 0000   00生产年份   ）
			try{
			//product no: bytes 12~15
			// String temp = serialNumber.substring(12, 13);
			
			String major1 = Integer.toBinaryString(Integer.valueOf(serialNumber.substring(12, 13)));
			String major2 = Integer.toBinaryString(Integer.valueOf(serialNumber.substring(13, 14)));
			String major3 = Integer.toBinaryString(Integer.valueOf(serialNumber.substring(14, 15)));
			String major4 = Integer.toBinaryString(Integer.valueOf(serialNumber.substring(15, 16)));
			
			major =(major1.length()<4?"0000".substring(major1.length())+major1:major1)
					+(major2.length()<4?"0000".substring(major2.length())+major2:major2)
					+(major3.length()<4?"0000".substring(major3.length())+major3:major3)
					+(major4.length()<4?"0000".substring(major4.length())+major4:major4);
			
			logger.info("major: "+major);
			//product year: byte 8
			String productYear = Integer.toBinaryString(Integer.valueOf(ByteUtil.getAscii(serialNumber.substring(8, 9)),16)-72);
			if(productYear.length()>6) {
				logger.warn("Cauculate Minor Product Year Error for tbox: " + serialNumber + ", productYear: " + productYear);
				productYear = productYear.substring(0,6);
			}
			//product month: byte 9
			String productMonth = Integer.toBinaryString(Integer.valueOf(serialNumber.substring(9, 10),16));
			//product day: byte 10~11
			String productDay1 = Integer.toBinaryString(Integer.valueOf(serialNumber.substring(10, 11)));
			String productDay2 = Integer.toBinaryString(Integer.valueOf(serialNumber.substring(11, 12)));
			
			minor = (productYear.length()<6?"000000".substring(productYear.length())+productYear:productYear)
					+(productMonth.length()<4?"0000".substring(productMonth.length())+productMonth:productMonth)
					+ (productDay1.length()<2?"00".substring(productDay1.length())+productDay1:productDay1)
					+ (productDay2.length()<4?"0000".substring(productDay2.length())+productDay2:productDay2);
			logger.info("minor: "+minor);
			isAnalyseOK = true;
			}
			catch(Exception ex)
			{
				logger.error("Caculate major minor exception: "+ex);
				major = "";
				minor = "";
				isAnalyseOK = false;
			}
			
		}
	}
	
	public boolean isAnalysedOK()
	{
		return isAnalyseOK;
	}
	
	public String getMajor()
	{
		return major;
	}
	
	public String getMinor()
	{
		return minor;
	}
	
}
