package com.maxus.tsp.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.util.StringUtils;

public class StringUtil {

	/*
	 * 十进制的字符串转成16进行的字符串
	 */
	public static String toHexString(String s) {
		String str = "";
		str = Integer.toHexString(Integer.parseInt(s)).toUpperCase();
		return str;
	}

	public static String toHexString(String s, int length) {
		int num = length;
		String str = StringUtil.dataLengthsCheck(s, num);
		s = StringUtil.toHexString(str);
		s = String.format("%1$" + num + "s", s).replaceAll(" ", "0");
		return s;
	}

	public static String addXString(String s) {
		String str = "";
		String regex = "(.{2})";
		str = s.replaceAll(regex, "\\\\\\\\x$1") + "\r\n";
		return str;
	}

	/**
	 * 16进制转10进制字符串
	 * 
	 * @param s
	 * @return
	 */
	public static String hextoString(String s) {
		String res = Integer.toString(Integer.parseInt(s, 16)).toUpperCase();

		return res;
	}

	public static String hexLongtoString(String s) {

		return Long.toString(Long.parseLong(s, 16)).toUpperCase();
	}

	public static String rideString(String s, double b) {
		String res = String.valueOf((Double.valueOf(s) * b));
		return res;
	}

	/**
	 * 日期格式转换成16进制的字符串
	 * 
	 * @param s
	 *            (yyyyMMdd HHmmss)
	 * @return(yyyy-mm-dd hh:mm:ss)
	 */
	public static String toDateHexString(String s) {
		String str = "";

		String year = s.substring(0, 4);

		year = dataLengthsCheck(toHexString(year), 4);
		String month = s.substring(4, 6);

		month = dataLengthsCheck(toHexString(month), 2);
		String day = s.substring(6, 8);

		day = dataLengthsCheck(toHexString(day), 2);
		str = year + month + day;
		return str;
	}

	/**
	 * 16进制时日期时间字符串
	 * 
	 * @return
	 */

	public static String getHexDateString() {
		Date nowTime = new Date();
		SimpleDateFormat time = new SimpleDateFormat("yyyyMMdd HHmmss");
		String timeStr = time.format(nowTime);
		String currentDate = timeStr.split(" ")[0];
		currentDate = StringUtil.toDateHexString(currentDate);

		String currentTime = timeStr.split(" ")[1];
		currentTime = StringUtil.toTimeHexString(currentTime);

		return currentDate + currentTime;
	}

	/**
	 * 16进制的时间字符串转10进制的时间字符串
	 * 
	 * @param s
	 * @return
	 */
	public static String hextoDateString(String s) {

		if (s.replaceAll(" ", "").length() != 14) {
			return null;

		}
		String year = dataLengthsCheck(hextoString(s.substring(0, 4)), 4);
		String month = dataLengthsCheck(hextoString(s.substring(4, 6)), 2);
		String day = dataLengthsCheck(hextoString(s.substring(6, 8)), 2);
		String hour = dataLengthsCheck(hextoString(s.substring(8, 10)), 2);
		String min = dataLengthsCheck(hextoString(s.substring(10, 12)), 2);
		String sec = dataLengthsCheck(hextoString(s.substring(12, 14)), 2);
		String res = year + "-" + month + "-" + day + " " + hour + ":" + min + ":" + sec;
		return res;
	}

	/**
	 * 时间字符串转换成16进制字符串
	 * 
	 * @param s
	 * @return
	 */
	public static String toTimeHexString(String s) {
		String str = "";

		String hour = s.substring(0, 2);
		// System.out.println("hour" +hour);
		hour = dataLengthsCheck(toHexString(hour), 2);
		String min = s.substring(2, 4);
		// System.out.println("min" +min);
		min = dataLengthsCheck(toHexString(min), 2);
		String sec = s.substring(4, 6);
		// System.out.println("sec"+sec);
		sec = dataLengthsCheck(toHexString(sec), 2);
		str = hour + min + sec;
		return str;
	}

	public static String dataLengthsCheck(String str, int num) {
		String result = "";
		str = str.trim().toUpperCase();
		if (str.length() < num) {
			int strLen = str.length();
			StringBuffer sb = null;
			while (strLen < num) {
				sb = new StringBuffer();
				sb.append("0").append(str);

				str = sb.toString();
				strLen = str.length();
			}
			result = str;

		} else if (str.length() > num) {
			result = str.substring(str.length() - num, str.length());

		} else {
			result = str;
		}

		return result;
	}

	public static String addBlank(String str) {
		String input = str;

		String regex = "(.{2})";

		input = input.replaceAll(regex, "$1 ");

		// System.out.println (input);
		return input;
	}

	/**
	 * 日志格式化
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static String logFormat(String key, String value) {

		return "\n\t " + key + " : " + value;
	}

	public static String logFormat2t(String key, String value) {

		return "\n\t\t " + key + " : " + value;
	}
	
	public static byte[] hexStringToBytes(String hexString) {
		if (StringUtils.isEmpty(hexString))
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

}
