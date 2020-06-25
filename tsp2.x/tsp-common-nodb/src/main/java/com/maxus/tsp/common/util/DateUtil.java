package com.maxus.tsp.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @ClassName: DateUtil.java
 * @Description:  
 * @author 胡宗明
 * @version V1.0
 * @Date 2018年3月23日 下午3:45:48
 */
public class DateUtil {
	private static final Logger logger = LogManager.getLogger(DateUtil.class);
	/**
	 * 
	 * @Title: stringToLong
	 * @Description: string类型转换为long类型 strTime要转换的String类型的时间 formatType时间格式
	 *               strTime的时间格式和formatType的时间格式必须相同
	 * @param: @param
	 *             strTime
	 * @param: @param
	 *             formatType
	 * @param: @return
	 * @return: long
	 * @throws @author
	 *             胡宗明
	 * @Date 2018年3月23日 下午15:49:31
	 */

	private final static long MS_IN_A_SECOND = 1000;// long型时间和秒之间转换的乘除数
	private final static long differ = 10;// 用于判断时间时间是否小于10秒，

	public static long stringToLong(String strTime, String formatType) throws ParseException {
		Date date = stringToDate(strTime, formatType); // String类型转成date类型
		if (date == null) {
			return 0;
		} else {
			long currentTime = dateToLong(date); // date类型转成long类型
			return currentTime;
		}
	}

	// string类型转换为date类型
	// strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
	// HH时mm分ss秒，
	// strTime的时间格式必须要与formatType的时间格式相同
	public static Date stringToDate(String strTime, String formatType) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(formatType);
		Date date = null;
		date = formatter.parse(strTime);
		return date;
	}

	// date类型转换为long类型
	// date要转换的date类型的时间
	public static long dateToLong(Date date) {
		return date.getTime();
	}

	// 判断传入的long型时间与当前时间相比
	public static boolean timeDifference(long longTime) throws ParseException {
		long end = System.currentTimeMillis();
		if ((end - longTime) / MS_IN_A_SECOND < differ) {
			return true;
		}

		return false;
	}

	//根据传入的long时间戳转换为真实时间
	public static Date longToDate(long time)
	{
		try {
			 SimpleDateFormat format =   new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );   
	         String d = format.format(time);  
	         Date date= format.parse(d);  
	         return date;
		} catch(Exception ex) {
			logger.error("时间戳转换发生异常:{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
			return null;
		}
	}
	
	//根据传入的long时间戳转换为真实时间
	public static String longToDateStr(long time)
	{
		try {
			 SimpleDateFormat format =   new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );   
	         String date = format.format(time);  
	         return date;
		} catch(Exception ex) {
			logger.error("时间戳转换发生异常:{}", ThrowableUtil.getErrorInfoFromThrowable(ex));
			return null;
		}
	}

//	public static void main(String args[]) {
//		try {
//		 SimpleDateFormat format =   new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
//         long time=Long.parseLong("1525941247234");
//         String d = format.format(time);
//         Date date=format.parse(d);
//         System.out.println("Format To String(Date):"+d);
//         System.out.println(DateUtil.stringToLong("2018-05-09 16:44:13","yyyy-MM-dd HH:mm:ss"));
//		} catch (Exception ex) {
//
//		}
//	}
}
