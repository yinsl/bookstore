/**        
 * ByteUtil.java Create on 2017年6月5日      
 * Copyright (c) 2017年6月5日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */

package com.maxus.tsp.common.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.util.StringUtils;

public class ByteUtil {

	public static String byteToHex(byte b) {
		String hex = Integer.toHexString(b & 0xff);
		if (hex.length() == 1)
			hex = (new StringBuilder(String.valueOf('0'))).append(hex).toString();
		return hex.toUpperCase();
	}
	
	/**
	 *
	 * @Title: byteToHex
	 * @Description: 字节类型转16进制
	 * @param: @param
	 *             bs[]
	 * @param: @return
	 * @return: String
	 */
	// public static String byteToHex(byte bs[]) {
	// 	StringBuffer sb = new StringBuffer();
	// 	if (bs != null && bs.length > 0) {
	// 		for (int i = 0; i < bs.length; i++)
	// 			sb.append(byteToHex(bs[i]));

	// 	}
	// 	return sb.toString();
	// }
	/**
	 *
	 * @Title: byteToHex
	 * @Description: 字节类型转16进制
	 * @param: @param
	 *             bs[]
	 * @param: @return
	 * @return: String
	 */
	public static String byteToHex(byte bs[]) {
		StringBuffer sb = new StringBuffer();
		if (bs != null && bs.length > 0) {
			for (int i = 0; i < bs.length; i++) {
				//sb.append(byteToHex(bs[i]));
				String hex = Integer.toHexString(bs[i] & 0xFF);
				if (hex.length() == 1) {
					sb.append(String.valueOf('0'));
				}
				sb.append(hex);
			}
		}
		return sb.toString().toUpperCase();
	}

	public static byte hexToByte(int n) {
		return (byte) n;
	}

	@SuppressWarnings("null")
    public static byte hexToByte(String hexString) {
		byte b[] = hexStringToBytes(hexString);
		if (b != null || b.length != 1)
			// throw new CommandException((new
			// StringBuilder()).append(hexString).append("ת�����ֽ��쳣").toString());
			return b[0];
		else
			return b[0];

	}

	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals(""))
			return null;
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char hexChars[] = hexString.toCharArray();
		byte d[] = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}

		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static String parseAscii(byte bs[]) {
		StringBuffer str = new StringBuffer("");
		for (int i = 0; i < bs.length; i++) {
			char a = (char) Integer.parseInt(String.valueOf(bs[i]));
			str.append(a);
		}

		return str.toString();
	}

	public static String getAscii(String str) {
		if (StringUtils.isEmpty(str))
			return null;
		char ch[] = str.toCharArray();
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < ch.length; i++)
			sb.append(Integer.toHexString(Integer.valueOf(Integer.toString(ch[i])).intValue()));

		return sb.toString();
	}

	public static String byteToStr(byte bs[]) {
		StringBuffer sb = new StringBuffer();
		if (bs != null && bs.length > 0) {
			for (int i = 0; i < bs.length; i++) {
				String str = String.valueOf(bs[i] & 0xff);
				if (str.length() == 1)
					str = (new StringBuilder(String.valueOf('0'))).append(str).toString();
				sb.append(str);
			}

		}
		return sb.toString();
	}

	public static byte[] stringToBytes(String str) throws UnsupportedEncodingException {
		byte bytes[] = str.getBytes("GBK");
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {
			sb.append("0123456789ABCDEF".charAt((bytes[i] & 0xf0) >> 4));
			sb.append("0123456789ABCDEF".charAt((bytes[i] & 0xf) >> 0));
		}

		return hexStringToBytes(sb.toString());
	}
	public static byte[] stringUTF8ToBytes(String str) throws UnsupportedEncodingException {
		byte bytes[] = str.getBytes("UTF-8");
		return hexStringToBytes(byteToHex(bytes));
	}

	public static String bytesToString(byte bts[]) {
		String bytes = byteToHex(bts);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
		for (int i = 0; i < bytes.length(); i += 2)
			baos.write(
					"0123456789ABCDEF".indexOf(bytes.charAt(i)) << 4 | "0123456789ABCDEF".indexOf(bytes.charAt(i + 1)));

		try {
	        return new String(baos.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
        }
		return null;
	}

	public static String byteToBinary(byte bytes[]) {
		StringBuffer sb = new StringBuffer();
		byte abyte0[];
		int j = (abyte0 = bytes).length;
		for (int i = 0; i < j; i++) {
			byte b = abyte0[i];
			sb.append(byteToBinary(b));
		}

		return sb.toString();
	}

	public static String byteToBinary(byte bye) {
		String status1 = Integer.toBinaryString(bye & 0xff);
		int length = 8 - status1.length();
		for (int i = 0; i < length; i++)
			status1 = (new StringBuilder("0")).append(status1).toString();

		return status1;
	}

	public static String byteToBinary(int num) {
		String status1 = Integer.toBinaryString(num);
		int length = 8 - status1.length();
		for (int i = 0; i < length; i++)
			status1 = (new StringBuilder("0")).append(status1).toString();

		return status1;
	}

	public static byte[] long2Byte(long x) {
		byte bb[] = new byte[8];
		bb[0] = (byte) (int) (x >> 56);
		bb[1] = (byte) (int) (x >> 48);
		bb[2] = (byte) (int) (x >> 40);
		bb[3] = (byte) (int) (x >> 32);
		bb[4] = (byte) (int) (x >> 24);
		bb[5] = (byte) (int) (x >> 16);
		bb[6] = (byte) (int) (x >> 8);
		bb[7] = (byte) (int) (x >> 0);
		return bb;
	}

	public static Long byte2Long(byte bb[]) {
		if (bb != null && bb.length == 8) {
			ByteBuffer aa = ByteBuffer.wrap(bb);
			return Long.valueOf(aa.getLong());
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @Title: int2Byte
	 * @Description: int转化为byte[4]
	 * @param: @param
	 *             x
	 * @param: @return
	 * @return: byte[]
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月13日 下午2:38:58
	 */
	public static byte[] int2Byte(int x) {
		byte bb[] = new byte[4];
		bb[0] = (byte) (x >> 24);
		bb[1] = (byte) (x >> 16);
		bb[2] = (byte) (x >> 8);
		bb[3] = (byte) (x >> 0);
		return bb;
	}

	public static byte[] short2Byte(short x) {
		byte bb[] = new byte[2];
		bb[0] = (byte) (x >> 8);
		bb[1] = (byte) (x >> 0);
		return bb;
	}

	public static Short byte2Short(byte bb[]) {
		if (bb != null && bb.length == 2) {
			ByteBuffer aa = ByteBuffer.wrap(bb);
			return aa.getShort();
		} else {
			return null;
		}
	}

	public static Character byte2Char(byte bb[]) {
		if (bb != null && bb.length == 2) {
			ByteBuffer aa = ByteBuffer.wrap(bb);
			return Character.valueOf(aa.getChar());
		} else {
			return null;
		}
	}

	public static float byte2float(byte b[], int index) {
		int l = b[index + 0];
		l &= 0xff;
		l = (int) ((long) l | (long) b[index + 1] << 8);
		l &= 0xffff;
		l = (int) ((long) l | (long) b[index + 2] << 16);
		l &= 0xffffff;
		l = (int) ((long) l | (long) b[index + 3] << 24);
		return Float.intBitsToFloat(l);
	}

	/**
	 * 计算CRC校验码
	 * 
	 * @Title: getCRC
	 * @Description: 获取校验和(异或计算)
	 * @param: @param
	 *             data
	 * @param: @return
	 * @return: int
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月14日 上午11:01:15
	 */
	public static int getCRC(List<Short> data) {
		short crc = data.get(0);
		for (int i = 1; i < data.size(); i++) {
			crc ^= data.get(i);
		}
		return crc;
	}

	/**
	 * 计算CRC校验码
	 * 
	 * @Title: getCRC
	 * @Description: 计算CRC校验码
	 * @param: @param
	 *             data
	 * @param: @return
	 * @return: int
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月14日 上午11:01:15
	 */
	public static int getCRC(byte[] data, int len) {
		// byte b = data[0];
		// Integer.toHexString(b & 0xff);
		short crc = (short) (data[0] & 0xFF);
		for (int i = 1; i < len; i++) {
			crc ^= data[i] & 0xFF;
		}
		return crc;
	}

	public static byte getCS(byte data[]) throws Exception {
		int sum = 0;
		for (int i = 0; i < data.length; i++)
			sum += Integer.valueOf(byteToHex(data[i]), 16).intValue();

		String sumHex = Integer.toHexString(sum);
		sumHex = sumHex.substring(sumHex.length() - 2);
		return hexToByte(sumHex);
	}

	public static short getShort(byte bytes[]) {
		return (short) (0xff & bytes[0] | 0xff00 & bytes[1] << 8);
	}

	public static char getChar(byte bytes[]) {
		return (char) (0xff & bytes[0] | 0xff00 & bytes[1] << 8);
	}

	public static int getUnsignedInt(byte bytes[]) {
		return byte2Short(bytes).shortValue() & 0xffff;
	}

	public static long getUnsignedLong(byte bytes[]) {
		return (long) byte2Int(bytes) & 0xffffffffL;
	}

	/**
	 * @Title: byte2Int
	 * @Description: byte[4]转化为int
	 * @param: @param
	 *             bytes
	 * @param: @return
	 * @return: Integer
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月19日 下午3:14:37
	 */
	public static int byte2Int(byte[] bytes) {
		if (bytes == null || bytes.length < 4)
			return -1;
		return (int) (0xff & bytes[3] | 0xff00 & (bytes[2] << 8) | 0xff0000 & (bytes[1] << 16)
			| 0xff000000 & (bytes[0] << 24));
	}

	public static String getUnsignedChar(byte bytes) {
		return String.valueOf(bytes & 0xff);
	}

	// public static byte[] toByte(int x)
	// {
	// byte bb[] = new byte[4];
	// bb[0] = (byte)(x >> 24);
	// bb[1] = (byte)(x >> 16);
	// bb[2] = (byte)(x >> 8);
	// bb[3] = (byte)(x >> 0);
	// return bb;
	// }
	/**
	 * @Title: CreateDateTimeBytes
	 * @Description: 心跳连接
	 * @param datetime
	 * @return Reply
	 */
	public static byte[] CreateDateTimeBytes(Calendar datetime) {

		byte[] Reply = new byte[7];
		Reply[0] = (byte) ((datetime.get(Calendar.YEAR) & 0xFF00) >> 8);
		Reply[1] = (byte) (datetime.get(Calendar.YEAR) & 0xFF);
		Reply[2] = (byte) (datetime.get(Calendar.MONTH)+1);
		Reply[3] = (byte) (datetime.get(Calendar.DAY_OF_MONTH));
		Reply[4] = (byte) (datetime.get(Calendar.HOUR_OF_DAY));
		Reply[5] = (byte) (datetime.get(Calendar.MINUTE));
		Reply[6] = (byte) (datetime.get(Calendar.SECOND));
		return Reply;
	}

	public static int byte2int(byte res, byte res1) {
	       int targets = (res & 0xff) | ((res1 << 8) & 0xff00);
	       return targets;
	      }

	
	/// <summary>
	/// 十六进制时间字节数组转换成时间字符串
	/// </summary>
	/// <param name="dataTimebytes"></param>
	/// <returns></returns>
	public static String bytesToDataTime(byte[] dataTimebytes) {
		Calendar dataTime = Calendar.getInstance();
		// 获取时间
		byte[] year = { 0x00, 0x00 };
		byte[] month = { 0x00 };
		byte[] day = { 0x00 };
		byte[] hour = { 0x00 };
		byte[] min = { 0x00 };
		byte[] second = { 0x00 };
		System.arraycopy(dataTimebytes, 0, year, 0, 2);
		System.arraycopy(dataTimebytes, 2, month, 0, 1);
		System.arraycopy(dataTimebytes, 3, day, 0, 1);
		System.arraycopy(dataTimebytes, 4, hour, 0, 1);
		System.arraycopy(dataTimebytes, 5, min, 0, 1);
		System.arraycopy(dataTimebytes, 6, second, 0, 1);
		// datatime default year,month and day are all equal to 1
		//short curYear =ByteUtil.byte2Short(year);
		dataTime.set(Calendar.YEAR, ByteUtil.byte2int(year[1],year[0]));
		dataTime.set(Calendar.MONTH, (month[0]&0xff)-1);
		dataTime.set(Calendar.DAY_OF_MONTH, (day[0]&0xff));
		dataTime.set(Calendar.HOUR_OF_DAY, (hour[0]&0xff));
		dataTime.set(Calendar.MINUTE, (min[0]&0xff));
		dataTime.set(Calendar.SECOND,(second[0]&0xff));

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// "yyyy-MM-dd
		// HH:mm:ss"
		return format.format(dataTime.getTime());
	}
	
	public static Date bytesToDT(byte[] dataTimebytes) {
		Calendar dataTime = Calendar.getInstance();
		// 获取时间
		byte[] year = { 0x00, 0x00 };
		byte[] month = { 0x00 };
		byte[] day = { 0x00 };
		byte[] hour = { 0x00 };
		byte[] min = { 0x00 };
		byte[] second = { 0x00 };
		System.arraycopy(dataTimebytes, 0, year, 0, 2);
		System.arraycopy(dataTimebytes, 2, month, 0, 1);
		System.arraycopy(dataTimebytes, 3, day, 0, 1);
		System.arraycopy(dataTimebytes, 4, hour, 0, 1);
		System.arraycopy(dataTimebytes, 5, min, 0, 1);
		System.arraycopy(dataTimebytes, 6, second, 0, 1);
		// datatime default year,month and day are all equal to 1
		//short curYear =ByteUtil.byte2Short(year);
		dataTime.set(Calendar.YEAR, ByteUtil.byte2int(year[1],year[0]));
		dataTime.set(Calendar.MONTH, (month[0]&0xff)-1);
		dataTime.set(Calendar.DAY_OF_MONTH, (day[0]&0xff));
		dataTime.set(Calendar.HOUR_OF_DAY, (hour[0]&0xff));
		dataTime.set(Calendar.MINUTE, (min[0]&0xff));
		dataTime.set(Calendar.SECOND,(second[0]&0xff));
		return dataTime.getTime();
	}
	
	//转成国标时间
	public static byte[] bytesToGBDataTime(byte[] dataTimebytes) {
	
		//国标时间0.year 1.month 2.day 3.hour 4.min 5.second
		byte[] gbTime = new byte[6];
		byte[] yearByte = { 0x00, 0x00 };
		int yearInt = 0;
		if(dataTimebytes.length==7) {
			System.arraycopy(dataTimebytes, 0, yearByte, 0, 2);
			yearInt = ByteUtil.getUnsignedInt(yearByte)-2000;
			yearByte = ByteUtil.short2Byte((short)yearInt);
			gbTime[0] = yearByte[1];
			System.arraycopy(dataTimebytes, 2, gbTime, 1, 1);
			System.arraycopy(dataTimebytes, 3, gbTime, 2, 1);
			System.arraycopy(dataTimebytes, 4, gbTime, 3, 1);
			System.arraycopy(dataTimebytes, 5, gbTime, 4, 1);
			System.arraycopy(dataTimebytes, 6, gbTime, 5, 1);
			return gbTime;
		} else{
			return null;
		}
	}
	
	/**
	 * String类型的OTA版本号，转换为hex类型的OTA版本号
	 * @param version
	 * @return
	 */
	public static String versionStringToHex(String version) {
		if (version != null) {
			String[] versionitem = version.split("\\.");
			if(versionitem.length != 2) return null;
			String curVersion = byteToHex(short2Byte(Short.valueOf(versionitem[0]))[1])
					+ byteToHex(short2Byte(Short.valueOf(versionitem[1]))[1]);
			return curVersion;
		} else {
			return null;
		}
	}
	
	public static String parseAscii(String hexString)
	{  StringBuffer str = new StringBuffer("");
		if (hexString == null || hexString.equals(""))
			return null;
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char hexChars[] = hexString.toCharArray();
		byte d[] = new byte[length];
		for (int i = 0; i < length; i++)
		{
			int pos = i * 2;
			d[i] = (byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
			char a = (char)Integer.parseInt(String.valueOf(d[i]));
			str.append(a);
		}			
		return str.toString();
	}
	
	// public static void main(String[] args) {
// 		byte[] dataTimebytes = {0x07,(byte) 0xE1,0x05,0x03,0x0F,0x19,0x1E};
// 		for(byte bt:ByteUtil.bytesToGBDataTime(dataTimebytes)) {
// 			System.out.println(bt);
// 		}
// 	73 61 69 63 e5 a4 a7 e9 80 9a
// String test = "金海路2505弄宝龙城市广场F3层";
// byte[] code = test.getBytes("UTF-8");
// System.out.println(ByteUtils.toHexString(code));

// System.out.println(ByteUtil.bytesToString((stringUTF8ToBytes(test))));
	// }
}
