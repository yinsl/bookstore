package com.maxus.tsp.platform.service.model.vo;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.maxus.tsp.common.constant.OTARealTimeConstants;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;

/**
 * RealTimeDataExtermum数据
 * 
 * @author lzgea
 *
 */
public class RealTimeDataExtermum implements Serializable {

	private static final long serialVersionUID = -1435560621556767957L;
	private static Logger logger = LogManager.getLogger(RealTimeDataExtermum.class);
	@JSONField(serialize = false)
	private int byteDatalength = 14;//报文基础长度
	@JSONField(serialize = false)
	private byte[] oriExtremumInfoBytes = null;
	@JSONField(serialize = false)
	private byte[] gbDataExtermum;
	private float voltOfCellBaseNum = 0.001f;//电压单位
	@JSONField(serialize = false)
	private boolean dataCorrect = true;
	@JSONField(serialize = false)
	private boolean isDataFitGB = false;
	private int maxTempOffSet = 40;//温度偏移量
	private int minTempOffSet = 40;//温度偏移量

	private int maxVoltSubsystemId;//最高电压电池子系统号
	private int maxVoltCellId;     //最高电压电池单体代号
	private float maxVoltOfCell;   //电池单体电压最高值
	private int minVoltSubsystemId;//最低电压电池子系统号
	private int minVoltCellId;	   //最低电压电池单体代号
	private float minVoltOfCell;   //电池单体电压最低值
	private int maxTempSubsystemId;//最高温度子系统号
	private int maxTempProbeId;    //最高温度探针序号
	private int maxTemp;		   //最高温度值
	private int minTempSubsystemId;//最低温度子系统号
	private int minTempProbeId;    //最低温度探针序号
	private int minTemp;           //最低温度值
	@JSONField(serialize = false)
	private List<CodeValue> realTimeDataExtermumList;
	@JSONField(serialize = false)
	public String getRealTimeDataExtermumJSONInfo() {
		//if (isAnalysisOk()) {
			return JSONObject.toJSONString(realTimeDataExtermumList);
		//}
		//return OTARealTimeConstants.INVALID_JSNINFO;
	}

	public RealTimeDataExtermum() {
		dataCorrect = false;
	}

	@JSONField(serialize = false)
	public byte[] getGBdatagramBytes() {
		if(isDataFitGB) {
			return gbDataExtermum;
		} else {
			return null;	
		}
	}

	public RealTimeDataExtermum(byte[] datagramBytes, String tboxsn, boolean isExtended) {
		isDataFitGB = false;
		if (isExtended) {
			byteDatalength = 18;	
		}
		oriExtremumInfoBytes = new byte[byteDatalength];
		if (datagramBytes.length == byteDatalength) {
			oriExtremumInfoBytes = datagramBytes;
			//长度符合封装国标
			isDataFitGB = true;
			gbDataExtermum = new byte[OTARealTimeConstants.GB_DATA_EXTERMUM_LENTH + 1];
			gbDataExtermum[0] = OTARealTimeConstants.GB_DATA_EXTERMUM_MARK;
			System.arraycopy(oriExtremumInfoBytes, 0, gbDataExtermum, 1, OTARealTimeConstants.GB_DATA_EXTERMUM_LENTH);

			try {

				//按照非扩展的序号解析
				byte[] _MaxVoltSubsystemId = new byte[1];

				byte[] _MaxVoltCellId = null;
				if (isExtended) {
					_MaxVoltCellId = new byte[2];
				} else {
					_MaxVoltCellId = new byte[1];
				}
				byte[] _MaxVoltOfCell = new byte[2];
				byte[] _MinVoltSubsystemId = new byte[1];
				byte[] _MinVoltCellId = null;
				if (isExtended) {
					_MinVoltCellId = new byte[2];
				} else {
					_MinVoltCellId = new byte[1];
				}
				byte[] _MinVoltOfCell = new byte[2];
				byte[] _MaxTempSubsystemId = new byte[1];
				byte[] _MaxTempProbeId = null;
				if (isExtended) {
					_MaxTempProbeId = new byte[2];
				} else {
					_MaxTempProbeId = new byte[1];
				}
				byte[] _MaxTemp = new byte[1];
				byte[] _MinTempSubsystemId = new byte[1];
				byte[] _MinTempProbeId = null;
				if (isExtended) {
					_MinTempProbeId = new byte[2];
				} else {
					_MinTempProbeId = new byte[1];
				}
				byte[] _MinTemp = new byte[1];
				DecimalFormat df3 = new DecimalFormat("#.000");
				realTimeDataExtermumList = new ArrayList<>();
				int analysisOffset = 0; //初始化解析的偏移位置

				//解析MaxVoltSubsystemId
				System.arraycopy(oriExtremumInfoBytes, analysisOffset, _MaxVoltSubsystemId, 0, OTARealTimeConstants.UINT8_OFFSET);
				logger.debug("TBox(SN:{}):国标极值数据中最高电压电池子系统号为{}", tboxsn, ByteUtil.byteToHex(_MaxVoltSubsystemId[0]));
				maxVoltSubsystemId = _MaxVoltSubsystemId[0] & 0xff;
				if (maxVoltSubsystemId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_VOLT_SUBSYSTEMID,
							OTARealTimeConstants.INVALID_BYTE_STRING));
				} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MaxVoltSubsystemId[0])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_VOLT_SUBSYSTEMID, OTARealTimeConstants.INVALID_BYTE_STRING));
				} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MaxVoltSubsystemId[0])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_VOLT_SUBSYSTEMID, OTARealTimeConstants.INVALID_BYTE_ERROR));
				} else {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_VOLT_SUBSYSTEMID,
							maxVoltSubsystemId));
				}

				//解析MaxVoltCellId
				analysisOffset = analysisOffset + OTARealTimeConstants.UINT8_OFFSET;
				System.arraycopy(oriExtremumInfoBytes, analysisOffset, _MaxVoltCellId, 0, _MaxVoltCellId.length);
				if (isExtended) {
					maxVoltCellId = ByteUtil.getUnsignedInt(_MaxVoltCellId);
					if (maxVoltCellId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
						logger.warn(String.format("Tbox %s的国标极值数据中最高电压电池单体代号为0", tboxsn));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_VOLT_CELLID,
								OTARealTimeConstants.INVALID_UINT16_STRING));
					} else if (maxVoltCellId == OTARealTimeConstants.INVALID_UINT16_STRING) {
						logger.warn(String.format("Tbox %s的国标极值数据中最高电压电池单体代号为 %s", tboxsn,maxVoltCellId));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_VOLT_CELLID, OTARealTimeConstants.INVALID_UINT16_STRING));
					} else if (maxVoltCellId == OTARealTimeConstants.INVALID_UINT16_ERROR) {
						logger.warn(String.format("Tbox %s的国标极值数据中最高电压电池单体代号为 %s", tboxsn,maxVoltCellId));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_VOLT_CELLID, OTARealTimeConstants.INVALID_UINT16_ERROR));
					} else {
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_VOLT_CELLID,
								maxVoltCellId));
					}
				} else {
					maxVoltCellId = _MaxVoltCellId[0] & 0xff;
					if (maxVoltCellId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
						logger.warn(String.format("Tbox %s的国标极值数据中最高电压电池单体代号为0", tboxsn));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_VOLT_CELLID,
								OTARealTimeConstants.INVALID_BYTE_STRING));
					} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MaxVoltCellId[0])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
						logger.warn(String.format("Tbox %s的国标极值数据中最高电压电池单体代号为 %s", tboxsn,ByteUtil.byteToHex(_MaxVoltCellId[0])));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_VOLT_CELLID, OTARealTimeConstants.INVALID_BYTE_STRING));
					} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MaxVoltCellId[0])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
						logger.warn(String.format("Tbox %s的国标极值数据中最高电压电池单体代号为 %s", tboxsn,ByteUtil.byteToHex(_MaxVoltCellId[0])));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_VOLT_CELLID, OTARealTimeConstants.INVALID_BYTE_ERROR));
					} else {
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_VOLT_CELLID,
								maxVoltCellId));
					}
				}

				//解析MaxVoltOfCell
				analysisOffset = analysisOffset + _MaxVoltCellId.length;
				System.arraycopy(oriExtremumInfoBytes, analysisOffset, _MaxVoltOfCell, 0, OTARealTimeConstants.UINT16_OFFSET);
				logger.debug("TBox(SN:{}):国标极值数据中电池单体电压最高值为{}", tboxsn, ByteUtil.byteToHex(_MaxVoltOfCell[0]));
				if (ByteUtil.getUnsignedInt(_MaxVoltOfCell) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_VOLT_OF_CELL,
							OTARealTimeConstants.INVALID_UINT16_STRING));
				} else if (ByteUtil.getUnsignedInt(_MaxVoltOfCell) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_VOLT_OF_CELL,
							OTARealTimeConstants.INVALID_UINT16_ERROR));
				} else {
					maxVoltOfCell = Float
							.parseFloat(df3.format(ByteUtil.getUnsignedInt(_MaxVoltOfCell) * voltOfCellBaseNum));
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_VOLT_OF_CELL,
							maxVoltOfCell));
				}

				//解析MinVoltSubsystemId
				analysisOffset = analysisOffset + OTARealTimeConstants.UINT16_OFFSET;
				System.arraycopy(oriExtremumInfoBytes, analysisOffset, _MinVoltSubsystemId, 0, OTARealTimeConstants.UINT8_OFFSET);
				logger.debug("TBox(SN:{}):国标极值数据中最低电压电池子系统号为{}", tboxsn, ByteUtil.byteToHex(_MinVoltSubsystemId[0]));
				minVoltSubsystemId = _MinVoltSubsystemId[0] & 0xff;
				if (minVoltSubsystemId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_VOLT_SUBSYSTEMID,
							OTARealTimeConstants.INVALID_BYTE_STRING));
				} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MinVoltSubsystemId[0])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_VOLT_SUBSYSTEMID,
							OTARealTimeConstants.INVALID_BYTE_STRING));
				} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MinVoltSubsystemId[0])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_VOLT_SUBSYSTEMID,
							OTARealTimeConstants.INVALID_BYTE_ERROR));
				} else {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_VOLT_SUBSYSTEMID,
							minVoltSubsystemId));
				}

				//解析MinVoltCellId
				analysisOffset = analysisOffset + OTARealTimeConstants.UINT8_OFFSET;
				System.arraycopy(oriExtremumInfoBytes, analysisOffset, _MinVoltCellId, 0, _MinVoltCellId.length);
				if (isExtended) {
					minVoltCellId = ByteUtil.getUnsignedInt(_MinVoltCellId);
					if (minVoltCellId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
						logger.warn(String.format("Tbox %s的国标极值数据中最低电压电池单体代号为0", tboxsn));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_VOLT_CELLID,
								OTARealTimeConstants.INVALID_UINT16_STRING));
					} else if (minVoltCellId == OTARealTimeConstants.INVALID_UINT16_STRING) {
						logger.warn(String.format("Tbox %s的国标极值数据中最低电压电池单体代号为 %s", tboxsn,minVoltCellId));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_VOLT_CELLID,
								OTARealTimeConstants.INVALID_UINT16_STRING));
					} else if (minVoltCellId == OTARealTimeConstants.INVALID_UINT16_ERROR) {
						logger.warn(String.format("Tbox %s的国标极值数据中最低电压电池单体代号为 %s", tboxsn,minVoltCellId));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_VOLT_CELLID,
								OTARealTimeConstants.INVALID_UINT16_ERROR));
					} else {
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_VOLT_CELLID,
								minVoltCellId));
					}
				} else {
					minVoltCellId = _MinVoltCellId[0] & 0xff;
					if (minVoltCellId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
						logger.warn(String.format("Tbox %s的国标极值数据中最低电压电池单体代号为0", tboxsn));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_VOLT_CELLID,
								OTARealTimeConstants.INVALID_BYTE_STRING));
					} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MinVoltCellId[0])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
						logger.warn(String.format("Tbox %s的国标极值数据中最低电压电池单体代号为 %s", tboxsn,ByteUtil.byteToHex(_MinVoltCellId[0])));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_VOLT_CELLID,
								OTARealTimeConstants.INVALID_BYTE_STRING));
					} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MinVoltCellId[0])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
						logger.warn(String.format("Tbox %s的国标极值数据中最低电压电池单体代号为 %s", tboxsn,ByteUtil.byteToHex(_MinVoltCellId[0])));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_VOLT_CELLID,
								OTARealTimeConstants.INVALID_BYTE_ERROR));
					} else {
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_VOLT_CELLID,
								minVoltCellId));
					}
				}

				//解析MinVoltOfCell
				analysisOffset = analysisOffset + _MinVoltCellId.length;
				System.arraycopy(oriExtremumInfoBytes, analysisOffset, _MinVoltOfCell, 0, OTARealTimeConstants.UINT16_OFFSET);
				logger.debug("TBox(SN:{}):国标极值数据中电池单体电压最低值为{}", tboxsn, ByteUtil.byteToHex(_MinVoltOfCell));
				if (ByteUtil.getUnsignedInt(_MinVoltOfCell) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_VOLT_OF_CELL,
							OTARealTimeConstants.INVALID_UINT16_STRING));
				} else if (ByteUtil.getUnsignedInt(_MinVoltOfCell) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_VOLT_OF_CELL,
							OTARealTimeConstants.INVALID_UINT16_ERROR));
				} else {
					minVoltOfCell = Float
							.parseFloat(df3.format(ByteUtil.getUnsignedInt(_MinVoltOfCell) * voltOfCellBaseNum));
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_VOLT_OF_CELL,
							minVoltOfCell));
				}

				//解析MaxTempSubsystemId
				analysisOffset = analysisOffset + OTARealTimeConstants.UINT16_OFFSET;
				System.arraycopy(oriExtremumInfoBytes, analysisOffset, _MaxTempSubsystemId, 0, OTARealTimeConstants.UINT8_OFFSET);
				logger.debug("TBox(SN:{}):国标极值数据中最高温度子系统代号为{}", tboxsn, ByteUtil.byteToHex(_MaxTempSubsystemId[0]));
				maxTempSubsystemId = _MaxTempSubsystemId[0] & 0xff;
				if (maxTempSubsystemId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_TEMP_SUBSYSTEMID,
							OTARealTimeConstants.INVALID_BYTE_STRING));
				} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MaxTempSubsystemId[0])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_TEMP_SUBSYSTEMID,
							OTARealTimeConstants.INVALID_BYTE_STRING));
				} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MaxTempSubsystemId[0])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_TEMP_SUBSYSTEMID,
							OTARealTimeConstants.INVALID_BYTE_ERROR));
				} else {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_TEMP_SUBSYSTEMID,
							maxTempSubsystemId));
				}

				//解析MaxTempProbeId
				analysisOffset = analysisOffset + OTARealTimeConstants.UINT8_OFFSET;
				System.arraycopy(oriExtremumInfoBytes, analysisOffset, _MaxTempProbeId, 0, _MaxTempProbeId.length);
				if (isExtended) {
					maxTempProbeId = ByteUtil.getUnsignedInt(_MaxTempProbeId);
					if (maxTempProbeId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
						logger.warn(String.format("Tbox %s的国标极值数据中最高温度探针序号为0", tboxsn));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_TEMP_PROBEID,
								OTARealTimeConstants.INVALID_UINT16_STRING));
					} else if (maxTempProbeId == OTARealTimeConstants.INVALID_UINT16_STRING) {
						logger.warn(String.format("Tbox %s的国标极值数据中最高温度探针序号为 %s", tboxsn,maxTempProbeId));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_TEMP_PROBEID,
								OTARealTimeConstants.INVALID_UINT16_STRING));
					} else if (maxTempProbeId == OTARealTimeConstants.INVALID_UINT16_ERROR) {
						logger.warn(String.format("Tbox %s的国标极值数据中最高温度探针序号为 %s",tboxsn, maxTempProbeId));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_TEMP_PROBEID,
								OTARealTimeConstants.INVALID_UINT16_ERROR));
					} else {
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_TEMP_PROBEID,
								maxTempProbeId));
					}
				} else {
					maxTempProbeId = _MaxTempProbeId[0] & 0xff;
					if (maxTempProbeId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
						logger.warn(String.format("Tbox %s的国标极值数据中最高温度探针序号为0", tboxsn));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_TEMP_PROBEID,
								OTARealTimeConstants.INVALID_BYTE_STRING));
					} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MaxTempProbeId[0])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
						logger.warn(String.format("Tbox %s的国标极值数据中最高温度探针序号为 %s", tboxsn,ByteUtil.byteToHex(_MaxTempProbeId[0])));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_TEMP_PROBEID,
								OTARealTimeConstants.INVALID_BYTE_STRING));
					} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MaxTempProbeId[0])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
						logger.warn(String.format("Tbox %s的国标极值数据中最高温度探针序号为 %s", tboxsn,ByteUtil.byteToHex(_MaxTempProbeId[0])));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_TEMP_PROBEID,
								OTARealTimeConstants.INVALID_BYTE_ERROR));
					} else {
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_TEMP_PROBEID,
								maxTempProbeId));
					}
				}

				//解析MaxTemp
				analysisOffset = analysisOffset + _MaxTempProbeId.length;
				System.arraycopy(oriExtremumInfoBytes, analysisOffset, _MaxTemp, 0, OTARealTimeConstants.UINT8_OFFSET);
				logger.debug("TBox(SN:{}):国标极值数据中最高温度值为{}", tboxsn, ByteUtil.byteToHex(_MaxTemp[0]));
				if (_MaxTemp[0] == OTARealTimeConstants.INVALID_BYTE_VALUE) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_TEMP, OTARealTimeConstants.INVALID_BYTE_STRING));
				} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MaxTemp[0])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_TEMP, OTARealTimeConstants.INVALID_BYTE_ERROR));
				} else {
					maxTemp = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MaxTemp[0])) - maxTempOffSet;
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MAX_TEMP, maxTemp));
				}

				//解析MinTempSubsystemId
				analysisOffset = analysisOffset + OTARealTimeConstants.UINT8_OFFSET;
				System.arraycopy(oriExtremumInfoBytes, analysisOffset, _MinTempSubsystemId, 0, OTARealTimeConstants.UINT8_OFFSET);
				logger.debug("TBox(SN:{}):国标极值数据中最低温度子系统代号为{}", tboxsn, ByteUtil.byteToHex(_MinTempSubsystemId[0]));
				minTempSubsystemId = _MinTempSubsystemId[0] & 0xff;
				if (minTempSubsystemId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_TEMP_SUBSYSTEMID,
							OTARealTimeConstants.INVALID_BYTE_STRING));
				} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MinTempSubsystemId[0])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_TEMP_SUBSYSTEMID,
							OTARealTimeConstants.INVALID_BYTE_STRING));
				} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MinTempSubsystemId[0])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_TEMP_SUBSYSTEMID,
							OTARealTimeConstants.INVALID_BYTE_ERROR));
				} else {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_TEMP_SUBSYSTEMID,
							minTempSubsystemId));
				}

				//解析MinTempProbeId
				analysisOffset = analysisOffset + OTARealTimeConstants.UINT8_OFFSET;
				System.arraycopy(oriExtremumInfoBytes, analysisOffset, _MinTempProbeId, 0, _MinTempProbeId.length);
				if (isExtended) {
					minTempProbeId = ByteUtil.getUnsignedInt(_MinTempProbeId);
					if (minTempProbeId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
						logger.warn(String.format("Tbox %s的国标极值数据中最低温度探针序号为0", tboxsn));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_TEMP_PROBEID,
								OTARealTimeConstants.INVALID_UINT16_STRING));
					} else if (minTempProbeId == OTARealTimeConstants.INVALID_UINT16_STRING) {
						logger.warn(String.format("Tbox %s的国标极值数据中最低温度探针序号为 %s", tboxsn,minTempProbeId));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_TEMP_PROBEID,
								OTARealTimeConstants.INVALID_UINT16_STRING));
					} else if (minTempProbeId == OTARealTimeConstants.INVALID_UINT16_ERROR) {
						logger.warn(String.format("Tbox %s的国标极值数据中最低温度探针序号为 %s", tboxsn, minTempProbeId));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_TEMP_PROBEID,
								OTARealTimeConstants.INVALID_UINT16_ERROR));
					} else {
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_TEMP_PROBEID,
								minTempProbeId));
					}
				} else {
					minTempProbeId = _MinTempProbeId[0] & 0xff;
					if (minTempProbeId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
						logger.warn(String.format("Tbox %s的国标极值数据中最低温度探针序号为0", tboxsn));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_TEMP_PROBEID,
								OTARealTimeConstants.INVALID_BYTE_STRING));
					} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MinTempProbeId[0])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
						logger.warn(String.format("Tbox %s的国标极值数据中最低温度探针序号为 %s", tboxsn,ByteUtil.byteToHex(_MinTempProbeId[0])));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_TEMP_PROBEID,
								OTARealTimeConstants.INVALID_BYTE_STRING));
					} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MinTempProbeId[0])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
						logger.warn(String.format("Tbox %s的国标极值数据中最低温度探针序号为 %s", tboxsn, ByteUtil.byteToHex(_MinTempProbeId[0])));
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_TEMP_PROBEID,
								OTARealTimeConstants.INVALID_BYTE_ERROR));
					} else {
						realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_TEMP_PROBEID,
								minTempProbeId));
					}
				}

				//解析MinTemp
				analysisOffset = analysisOffset + _MinTempProbeId.length;
				System.arraycopy(oriExtremumInfoBytes, analysisOffset, _MinTemp, 0, OTARealTimeConstants.UINT8_OFFSET);
				logger.debug("TBox(SN:{}):国标极值数据中最低温度值为{}", tboxsn, ByteUtil.byteToHex(_MinTemp[0]));
				if (_MinTemp[0] == OTARealTimeConstants.INVALID_BYTE_VALUE) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_TEMP, OTARealTimeConstants.INVALID_BYTE_STRING));
				} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MinTemp[0])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_TEMP, OTARealTimeConstants.INVALID_BYTE_ERROR));
				} else {
					minTemp = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_MinTemp[0])) - minTempOffSet;
					realTimeDataExtermumList.add(new CodeValue(OTARealTimeConstants.MIN_TEMP,
							minTemp));
				}
			} catch (Exception e) {
				logger.error("TBox(SN:{}):RealTimeDataExtermum 解析报文出错，原因:{}", tboxsn,
						ThrowableUtil.getErrorInfoFromThrowable(e));
				dataCorrect = false;
			}
		} else {
			logger.warn("TBox(SN:{}):RealTimeDataExtermum 报文长度错误", tboxsn);
			dataCorrect = false;
		}
	}
	
	@JSONField(serialize = false)
	public boolean isAnalysisOk() {
		return dataCorrect;
	}

	public int getMaxVoltSubsystemId() {
		return maxVoltSubsystemId;
	}

	public void setMaxVoltSubsystemId(int maxVoltSubsystemId) {
		this.maxVoltSubsystemId = maxVoltSubsystemId;
	}

	public int getMaxVoltCellId() {
		return maxVoltCellId;
	}

	public void setMaxVoltCellId(int maxVoltCellId) {
		this.maxVoltCellId = maxVoltCellId;
	}

	public double getMaxVoltOfCell() {
		return maxVoltOfCell;
	}

	public void setMaxVoltOfCell(float maxVoltOfCell) {
		this.maxVoltOfCell = maxVoltOfCell;
	}

	public int getMinVoltSubsystemId() {
		return minVoltSubsystemId;
	}

	public void setMinVoltSubsystemId(int minVoltSubsystemId) {
		this.minVoltSubsystemId = minVoltSubsystemId;
	}

	public int getMinVoltCellId() {
		return minVoltCellId;
	}

	public void setMinVoltCellId(int minVoltCellId) {
		this.minVoltCellId = minVoltCellId;
	}

	public double getMinVoltOfCell() {
		return minVoltOfCell;
	}

	public void setMinVoltOfCell(float minVoltOfCell) {
		this.minVoltOfCell = minVoltOfCell;
	}

	public int getMaxTempSubsystemId() {
		return maxTempSubsystemId;
	}

	public void setMaxTempSubsystemId(int maxTempSubsystemId) {
		this.maxTempSubsystemId = maxTempSubsystemId;
	}

	public int getMaxTempProbeId() {
		return maxTempProbeId;
	}

	public void setMaxTempProbeId(int maxTempProbeId) {
		this.maxTempProbeId = maxTempProbeId;
	}

	public int getMaxTemp() {
		return maxTemp;
	}

	public void setMaxTemp(int maxTemp) {
		this.maxTemp = maxTemp;
	}

	public int getMinTempSubsystemId() {
		return minTempSubsystemId;
	}

	public void setMinTempSubsystemId(int minTempSubsystemId) {
		this.minTempSubsystemId = minTempSubsystemId;
	}

	public int getMinTempProbeId() {
		return minTempProbeId;
	}

	public void setMinTempProbeId(int minTempProbeId) {
		this.minTempProbeId = minTempProbeId;
	}

	public int getMinTemp() {
		return minTemp;
	}

	public void setMinTemp(int minTemp) {
		this.minTemp = minTemp;
	}

}
