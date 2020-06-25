/**        
 * ReportPos.java Create on 2017年7月18日      
 * Copyright (c) 2017年7月18日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.platform.service.model.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.maxus.tsp.common.constant.OTARealTimeConstants;
import com.maxus.tsp.common.util.ByteUtil;

/**
 * @ClassName: ReportPos.java
 * @Description:  
 * @author 任怀宇
 * @version V1.0
 * @Date 2017年7月18日 下午1:53:04
 */
public class ReportPos implements Serializable {

	/**
	 * 
	 */
	private static Logger logger = LogManager.getLogger(ReportPos.class);
	private static final long serialVersionUID = -8910492614623865227L;
	@JSONField(serialize = false)
	public static int byteDataLength = 16; // 单条位置信息报文的长度
	@JSONField(serialize = false)
	private byte[] oriReportPosBytes = new byte[byteDataLength];
	@JSONField(serialize = false)
	private byte[] gbDataPos;
	
	public String collectDate = "";
	private int posValid = 0;				//定位状态
	private long latitude = 0;				//纬度
	private long longitude = 0;				//经度
	private long collectTimeStamp;
	@JSONField(serialize = false)
	private boolean isDataFitGB = false;
	
	public static int divNumber = 1000000; // 单位计算
	private boolean PosDataCorrect = false;

	public ReportPos() {

	}
	@JSONField(serialize = false)
	public byte[] getGBdatagramBytes() {
		if(isDataFitGB) {
			return gbDataPos;
		} else {
			return null;	
		}
	}
	@JSONField(serialize = false)
	public boolean isAnalysisOk() {
		return PosDataCorrect;
	}

	public ReportPos(byte[] input) {
		this(input, true);
	}

	public ReportPos(byte[] input, boolean withCollectDataTime)
	{
		isDataFitGB = false;
		if (withCollectDataTime == true) {
			if (input.length == byteDataLength) {
				// 数据长度相等,解析数据
				oriReportPosBytes = input;			
				
				byte[] _CollectDataTime = new byte[7]; // 收集时间
				byte[] _PosStatus = new byte[1]; // 定位状态 经纬度用南纬是负,北纬是正,东经是正,西经是负
				byte[] _Latitude = new byte[4]; // 纬度，根据定位状态，设定正负值
				byte[] _Longitude = new byte[4]; // 经度，根据定位状态，设定正负值
				//获取收集时间
				System.arraycopy(oriReportPosBytes, 0, _CollectDataTime, 0, 7);
				collectDate = ByteUtil.bytesToDataTime(_CollectDataTime);
				collectTimeStamp = ByteUtil.bytesToDT(_CollectDataTime).getTime();
				// 第0位：0表示有效定位；1表示无效定位，后面的经纬度的值为最后一次有效的经纬度。
				// 第1位：0表示北纬；1表示南纬
				// 第2位：0表示东经；1表示西经
				if(ByteUtil.byte2int(_CollectDataTime[1],_CollectDataTime[0])<2017) {
					PosDataCorrect = false;
					logger.warn("该位置信息的收集时间错误:{},原始报文:{} ", ByteUtil.bytesToDataTime(_CollectDataTime), ByteUtil.byteToHex(input));					;
					return;
				}
				// 第3~7位：保留
				//获取是否有效
				System.arraycopy(oriReportPosBytes, 7, _PosStatus, 0, 1);
				if ((_PosStatus[0] & 0x01) == 0x00) {
					posValid = 1; //有效
				}
				//获得纬度
				System.arraycopy(oriReportPosBytes, 8, _Latitude, 0, 4);
			
				latitude = ByteUtil.getUnsignedLong(_Latitude);
				if ((_PosStatus[0] & 0x02) == 0x02) {
					// 如果是南纬，则是负数
					latitude = -1 * latitude;
				}
				//获得经度
				System.arraycopy(oriReportPosBytes, 12, _Longitude, 0, 4);
				longitude = ByteUtil.getUnsignedLong(_Longitude);
				if ((_PosStatus[0] & 0x04) == 0x04) {
					// 如果是西经，则是负数
					longitude = -1 * longitude;
				}
				PosDataCorrect = true;
			} else
				PosDataCorrect = false;
		} else {
			if (input.length == (byteDataLength - 7)) {
				// 数据长度相等,解析数据
				oriReportPosBytes = input;
				
				//封装国标数据
				isDataFitGB = true;
				gbDataPos = new byte[OTARealTimeConstants.GB_DATA_VEHICLE_POS_LENTH + 1];
				//纬度
				System.arraycopy(oriReportPosBytes, 1, gbDataPos, 6, 4);
				//经度
				System.arraycopy(oriReportPosBytes, 5, gbDataPos, 2, 4);
				//定位状态
				System.arraycopy(oriReportPosBytes, 0, gbDataPos, 1, 1);
				gbDataPos[0] = OTARealTimeConstants.GB_DATA_VEHICLE_POS_MARK;
				
				byte[] _PosStatus = new byte[1]; // 定位状态 经纬度用南纬是负,北纬是正,东经是正,西经是负
				byte[] _Latitude = new byte[4]; // 纬度，根据定位状态，设定正负值
				byte[] _Longitude = new byte[4]; // 经度，根据定位状态，设定正负值
				// 第0位：0表示有效定位；1表示无效定位，后面的经纬度的值为最后一次有效的经纬度。
				// 第1位：0表示北纬；1表示南纬
				// 第2位：0表示东经；1表示西经
				// 第3~7位：保留
				//获取是否有效
				System.arraycopy(oriReportPosBytes, 0, _PosStatus, 0, 1);
				if ((_PosStatus[0] & 0x01) == 0x00) {
					posValid = 1; //有效
				}
				//获得纬度
				System.arraycopy(oriReportPosBytes, 1, _Latitude, 0, 4);
			
				latitude = ByteUtil.getUnsignedLong(_Latitude);
				if ((_PosStatus[0] & 0x02) == 0x02) {
					// 如果是南纬，则是负数
					latitude = -1 * latitude;
				}
				//获得经度
				System.arraycopy(oriReportPosBytes, 5, _Longitude, 0, 4);
				longitude = ByteUtil.getUnsignedLong(_Longitude);
				if ((_PosStatus[0] & 0x04) == 0x04) {
					// 如果是西经，则是负数
					longitude = -1 * longitude;
				}
				PosDataCorrect = true;
			} else
				PosDataCorrect = false;
		}
	}
	
	public void setCollectDate(String inputCollectDateTime) {
		collectDate= inputCollectDateTime;
	}
	
	public String getCollectDate() {
		return collectDate;
	}

	public void setLatitude(long curlatitude)
	{
		latitude = curlatitude;
	}
	
	public long getLatitude() // 获取当前纬度
	{
		return latitude;
	}

	public void setLongitude(long curlongitude)
	{
		longitude = curlongitude;
	}
	
	public long getLongitude() // 获取当前经度
	{
		return longitude;
	}

	public void setValidStatus(int inputValid)
	{
		posValid=inputValid;
	}
	
	public int getValidStatus() // 获取该定位是否有效
	{
		return posValid;
	}
	@JSONField(serialize = false)
	public String getRealTimeDataGPSJSONInfo() // 获取该定位是否有效
	{
		List<CodeValue> codeValue = new ArrayList<CodeValue>();
		if (isAnalysisOk()) {
			codeValue.add(new CodeValue(OTARealTimeConstants.GPS_VALID, String.valueOf(posValid)));
			codeValue.add(new CodeValue(OTARealTimeConstants.GPS_LATITUDE_VALUE, String.valueOf(latitude)));
			codeValue.add(new CodeValue(OTARealTimeConstants.GPS_LONGITUDE_VALUE, String.valueOf(longitude)));
			return JSONObject.toJSONString(codeValue);
		}
		return OTARealTimeConstants.INVALID_JSNINFO;
	}
	
	public long getCollectTimeStamp()
	{
		return collectTimeStamp;
	}
		
}
