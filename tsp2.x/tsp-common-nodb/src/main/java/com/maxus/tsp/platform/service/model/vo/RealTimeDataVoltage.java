package com.maxus.tsp.platform.service.model.vo;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.maxus.tsp.common.constant.OTARealTimeConstants;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;

/**
 * RealTimeDataVoltage数据CMD_UP_REALTIME_DATA_VOLTAGE
 * 
 * @author lzgea
 *
 */
public class RealTimeDataVoltage implements Serializable {

	private static final long serialVersionUID = -6760396278250961279L;
	private static Logger logger = LogManager.getLogger(RealTimeDataVoltage.class);
	@JSONField(serialize = false)
	private int byteDatalength = 7;
	@JSONField(serialize = false)
	private byte[] oriVoltageInfoBytes;
	@JSONField(serialize = false)
	private boolean dataCorrect = true;
	@JSONField(serialize = false)
	private int currentOffset = 1000;

	private float totalVoltageFloatNum = 0.1f;
	private float totalCurrentFloatNum = 0.1f;
	private float voltagesFloatNum = 0.001f;

	private int subSystemId;//可充电储能子系统号
	private float totalVoltage;//可充电储能装置电压
	private float totalCurrent;//可充电储能装置电流
	private int cellCount;//单体电池总数
	private Object[] voltages;//电压值列表
	@JSONField(serialize = false)
	private Map<String,Object> realTimeDataVoltageMap;
	@JSONField(serialize = false)
	public String getRealTimeDataVoltageJSONInfo() {
		//if (isAnalysisOk()) {
			return JSONObject.toJSONString(realTimeDataVoltageMap, SerializerFeature.WriteMapNullValue);
		//}
		//return OTARealTimeConstants.INVALID_JSNINFO;
	}

	public RealTimeDataVoltage() {
		dataCorrect = false;
	}

	public RealTimeDataVoltage(byte[] datagramBytes, String tboxsn) {
		// check lenght
		if (datagramBytes.length < byteDatalength) {
			dataCorrect = false;
			logger.warn("TBox(SN:{}):RealTimeDataVoltage 初始报文长度错误", tboxsn);
		} else {
			oriVoltageInfoBytes = datagramBytes;
			byte[] _CellCount = new byte[2];
			System.arraycopy(oriVoltageInfoBytes, 5, _CellCount, 0, OTARealTimeConstants.UINT16_OFFSET);
			cellCount = ByteUtil.getUnsignedInt(_CellCount);
			byteDatalength += cellCount * 2;
			if (byteDatalength == datagramBytes.length) {
				byte[] _SubSystemId = new byte[2];
				byte[] _TotalVoltage = new byte[2];
				byte[] _TotalCurrent = new byte[2];
				byte[] _Voltages = new byte[cellCount * 2];
				DecimalFormat df1 = new DecimalFormat("#.0");
				DecimalFormat df3 = new DecimalFormat("#.000");
				realTimeDataVoltageMap = new HashMap<>();

				try {

					System.arraycopy(oriVoltageInfoBytes, 0, _SubSystemId, 1, OTARealTimeConstants.UINT8_OFFSET);
					subSystemId =ByteUtil.getUnsignedInt(_SubSystemId);
					if (subSystemId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
						//0无效值给0
						realTimeDataVoltageMap.put(OTARealTimeConstants.SUBSYSTEMID_VOLTAGE_8, OTARealTimeConstants.INVALID_ZERO_VALUE);
					} else {
						realTimeDataVoltageMap.put(OTARealTimeConstants.SUBSYSTEMID_VOLTAGE_8, subSystemId);
					}

					System.arraycopy(oriVoltageInfoBytes, 1, _TotalVoltage, 0, OTARealTimeConstants.UINT16_OFFSET);
					if (ByteUtil.getUnsignedInt(_TotalVoltage) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
						realTimeDataVoltageMap.put(OTARealTimeConstants.TOTAL_VOLTAGE_8, OTARealTimeConstants.INVALID_UINT16_STRING);
					}else if (ByteUtil.getUnsignedInt(_TotalVoltage) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
						realTimeDataVoltageMap.put(OTARealTimeConstants.TOTAL_VOLTAGE_8, OTARealTimeConstants.INVALID_UINT16_ERROR);
					} else {
						totalVoltage = Float
								.parseFloat(df1.format(ByteUtil.getUnsignedInt(_TotalVoltage) * totalVoltageFloatNum));
						realTimeDataVoltageMap.put(OTARealTimeConstants.TOTAL_VOLTAGE_8, totalVoltage);
					}

					System.arraycopy(oriVoltageInfoBytes, 3, _TotalCurrent, 0, OTARealTimeConstants.UINT16_OFFSET);
					if (ByteUtil.getUnsignedInt(_TotalCurrent) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
						realTimeDataVoltageMap.put(OTARealTimeConstants.TOTAL_CURRENT_8, OTARealTimeConstants.INVALID_UINT16_STRING);
					}else if (ByteUtil.getUnsignedInt(_TotalCurrent) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
						realTimeDataVoltageMap.put(OTARealTimeConstants.TOTAL_CURRENT_8, OTARealTimeConstants.INVALID_UINT16_ERROR);
					} else {
						totalCurrent = Float
								.parseFloat(df1.format(ByteUtil.getUnsignedInt(_TotalCurrent) * totalCurrentFloatNum - currentOffset));
						realTimeDataVoltageMap.put(OTARealTimeConstants.TOTAL_CURRENT_8, totalCurrent);
					}
					
					realTimeDataVoltageMap.put(OTARealTimeConstants.CELL_COUNT, cellCount);

					System.arraycopy(oriVoltageInfoBytes, 7, _Voltages, 0, cellCount * 2);
					byte[] _VoltagesTemp = new byte[2];
					voltages = new Object[cellCount];

					for (int i = 0; i < cellCount; i++) {
						_VoltagesTemp[0] = _Voltages[i * 2];
						_VoltagesTemp[1] = _Voltages[i * 2 + 1];
						if (ByteUtil.getUnsignedInt(_VoltagesTemp) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
							//无效
							voltages[i] = OTARealTimeConstants.INVALID_UINT16_STRING;
						} else if (ByteUtil.getUnsignedInt(_VoltagesTemp) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
							//异常
							voltages[i] = OTARealTimeConstants.INVALID_UINT16_ERROR;
						} else {
							voltages[i] = Float.parseFloat(df3.format(ByteUtil.getUnsignedInt(_VoltagesTemp) * voltagesFloatNum));
						}
					}
					
					realTimeDataVoltageMap.put(OTARealTimeConstants.VOLTAGES, voltages);
					
				} catch (Exception e) {
					logger.error("TBox(SN:{}):RealTimeDataVoltage 解析报文出错，原因:{}", tboxsn,
							ThrowableUtil.getErrorInfoFromThrowable(e));
					dataCorrect = false;
				}
			} else {
				dataCorrect = false;
				logger.warn("TBox(SN:{}):RealTimeDataVoltage 报文长度错误", tboxsn);
			}
		}

	}

	public String converToString(float[] data) {
		String converdata = "";
		for (int i = 0; i < data.length; i++) {
			if (i == 0) {
				converdata += data[i];
			} else {
				converdata += ("," + data[i]);
			}
		}
		return converdata;
	}

	public int getSubSystemId() {
		return subSystemId;
	}

	public void setSubSystemId(int subSystemId) {
		this.subSystemId = subSystemId;
	}

	public float getTotalVoltage() {
		return totalVoltage;
	}

	public void setTotalVoltage(float totalVoltage) {
		this.totalVoltage = totalVoltage;
	}

	public float getTotalCurrent() {
		return totalCurrent;
	}

	public void setTotalCurrent(float totalCurrent) {
		this.totalCurrent = totalCurrent;
	}

	public int getCellCount() {
		return cellCount;
	}

	public void setCellCount(int cellCount) {
		this.cellCount = cellCount;
	}

	public Object[] getVoltages() {
		return voltages;
	}

	public void setVoltages(Object[] voltages) {
		this.voltages = voltages;
	}
	@JSONField(serialize = false)
	public boolean isAnalysisOk() {
		return dataCorrect;
	}

}
