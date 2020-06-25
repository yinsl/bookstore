package com.maxus.tsp.platform.service.model.vo;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.maxus.tsp.common.constant.OTARealTimeConstants;
import com.maxus.tsp.common.enums.EngineStatusEnum;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;

/**
 * RealTimeDataEngine数据
 * 
 * @author lzgea
 *
 */
public class RealTimeDataEngine implements Serializable {

	private static final long serialVersionUID = -6056526051278174703L;
	private static Logger logger = LogManager.getLogger(RealTimeDataEngine.class);
	@JSONField(serialize = false)
	public int byteDatalength = 5;// 报文基础字节长度为5
	@JSONField(serialize = false)
	private byte[] oriEngineInfoBytes = new byte[byteDatalength];
	@JSONField(serialize = false)
	private byte[] gbDataEngine = new byte[byteDatalength+1];
	private boolean dataCorrect = true;
	private float fuelConsumePercentBaseNum = 0.01f;//燃料消耗率单位
	private int engineStatus;//发动机状态
	private int crankshaftSpeed;//曲轴转速
	private float fuelConsumePercent;//燃料消耗率
	@JSONField(serialize = false)
	private List<CodeValue> realTimeDataEngineList;
	@JSONField(serialize = false)
	private boolean isDataFitGB = false;
	@JSONField(serialize = false)
	public String getRealTimeDataEngineJSONInfo() {
		//if (isAnalysisOk()) {
			return JSONObject.toJSONString(realTimeDataEngineList,SerializerFeature.WriteMapNullValue);
		//}
		//return OTARealTimeConstants.INVALID_JSNINFO;
	}
	@JSONField(serialize = false)
	public byte[] getGBdatagramBytes() {
		if(isDataFitGB) {
			return gbDataEngine;
		} else {
			return null;	
		}
	}
	
	public RealTimeDataEngine() {
		dataCorrect = false;
	}

	public RealTimeDataEngine(byte[] datagramBytes, String tboxsn) {
		// check length
		isDataFitGB = false;
		if (datagramBytes.length == byteDatalength) {
			isDataFitGB = true;
			oriEngineInfoBytes = datagramBytes;
			//封装国标数据
			System.arraycopy(oriEngineInfoBytes, 0, gbDataEngine, 1, byteDatalength);
			gbDataEngine[0] = OTARealTimeConstants.GB_DATA_ENGIN_MARK;
			
			byte[] _EngineStatus = new byte[2];
			byte[] _CrankshaftSpeed = new byte[2];
			byte[] _FuelConsumePercent = new byte[2];
			// BigDecimal fuelConsumePercentbd;
			DecimalFormat df4 = new DecimalFormat("#.00");
			realTimeDataEngineList = new ArrayList<>();

			//解析EngineStatus
			try {
				System.arraycopy(oriEngineInfoBytes, 0, _EngineStatus, 1, OTARealTimeConstants.UINT8_OFFSET);
				if (_EngineStatus[1] == OTARealTimeConstants.INVALID_BYTE_VALUE) {
					realTimeDataEngineList.add(new CodeValue(OTARealTimeConstants.ENGINE_STATUS, 
							EngineStatusEnum.INVALID.getValue()));
				} else {
					engineStatus =ByteUtil.getUnsignedInt(_EngineStatus);
					switch (engineStatus) {
					case OTARealTimeConstants.ENGINE_ON:
						realTimeDataEngineList.add(new CodeValue(OTARealTimeConstants.ENGINE_STATUS, 
								EngineStatusEnum.ON.getValue()));
						break;
					case OTARealTimeConstants.ENGINE_OFF:
						realTimeDataEngineList.add(new CodeValue(OTARealTimeConstants.ENGINE_STATUS, 
								EngineStatusEnum.OFF.getValue()));
						break;
					case OTARealTimeConstants.INVALID_BYTE_ERROR:
						logger.debug("TBox(SN:{}):国标发动机数据中发动机状态为{}", tboxsn, EngineStatusEnum.ERROR.getValue());
						realTimeDataEngineList.add(new CodeValue(OTARealTimeConstants.ENGINE_STATUS, EngineStatusEnum.ERROR.getValue()));
							break;
					default:
						//国标范围外的值，原值上传
						logger.debug("TBox(SN:{}):国标发动机数据中发动机状态为{}", tboxsn, engineStatus);
						realTimeDataEngineList.add(new CodeValue(OTARealTimeConstants.ENGINE_STATUS, engineStatus));
						break;
					}
				}

				//解析CrankshaftSpeed
				System.arraycopy(oriEngineInfoBytes, 1, _CrankshaftSpeed, 0, OTARealTimeConstants.UINT16_OFFSET);
				logger.debug("TBox(SN:{}):国标发动机数据中曲轴转速为{}", tboxsn, ByteUtil.getUnsignedInt(_CrankshaftSpeed));
				if (ByteUtil.getUnsignedInt(_CrankshaftSpeed) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
					realTimeDataEngineList.add(new CodeValue(OTARealTimeConstants.CRANK_SHAFT_SPEED, OTARealTimeConstants.INVALID_UINT16_STRING));
				} else if (ByteUtil.getUnsignedInt(_CrankshaftSpeed) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
					realTimeDataEngineList.add(new CodeValue(OTARealTimeConstants.CRANK_SHAFT_SPEED, OTARealTimeConstants.INVALID_UINT16_ERROR));
				} else {
					crankshaftSpeed = ByteUtil.getUnsignedInt(_CrankshaftSpeed);
					realTimeDataEngineList.add(new CodeValue(OTARealTimeConstants.CRANK_SHAFT_SPEED, crankshaftSpeed));
				}

				//解析FuelConsumePercent
				System.arraycopy(oriEngineInfoBytes, 3, _FuelConsumePercent, 0, OTARealTimeConstants.UINT16_OFFSET);
				logger.debug("TBox(SN:{}):国标发动机数据中燃料消耗率为{}", tboxsn, ByteUtil.getUnsignedInt(_FuelConsumePercent));
				if (ByteUtil.getUnsignedInt(_FuelConsumePercent) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
					realTimeDataEngineList.add(new CodeValue(OTARealTimeConstants.FUEL_CONSUME_PERCENT, OTARealTimeConstants.INVALID_UINT16_STRING));
				} else if (ByteUtil.getUnsignedInt(_FuelConsumePercent) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
					realTimeDataEngineList.add(new CodeValue(OTARealTimeConstants.FUEL_CONSUME_PERCENT, OTARealTimeConstants.INVALID_UINT16_ERROR));
				} else {
//					fuelConsumePercentbd = new BigDecimal(ByteUtil.getUnsignedInt(_FuelConsumePercent)/10000.0f);
//					fuelConsumePercent = fuelConsumePercentbd.setScale(4, BigDecimal.ROUND_HALF_UP).floatValue();
					fuelConsumePercent =Float.parseFloat(df4.format(
							ByteUtil.getUnsignedInt(_FuelConsumePercent) * fuelConsumePercentBaseNum));
					realTimeDataEngineList.add(new CodeValue(OTARealTimeConstants.FUEL_CONSUME_PERCENT, fuelConsumePercent));
				}
			} catch (Exception e) {
				logger.error("TBox(SN:{}):RealTimeDataEngine 解析报文出错，原因:{}", tboxsn,
						ThrowableUtil.getErrorInfoFromThrowable(e));
				dataCorrect = false;
			}
		} else {
			logger.warn("TBox(SN:{})RealTimeDataEngine 报文长度错误", tboxsn);
			dataCorrect = false;
		}
	}

	public int getEngineStatus() {
		return engineStatus;
	}

	public void setEngineStatus(int engineStatus) {
		this.engineStatus = engineStatus;
	}

	public int getCrankshaftSpeed() {
		return crankshaftSpeed;
	}

	public void setCrankshaftSpeed(int crankshaftSpeed) {
		this.crankshaftSpeed = crankshaftSpeed;
	}

	public float getFuelConsumePercent() {
		return fuelConsumePercent;
	}

	public void setFuelConsumePercent(float fuelConsumePercent) {
		this.fuelConsumePercent = fuelConsumePercent;
	}
	@JSONField(serialize = false)
	public boolean isAnalysisOk() {
		return dataCorrect;
	}
}
