/**
 * TspRealTimeDataServiceProc.java Create on 2018年2月26日
 * Copyright (c) 2018年2月26日 by 上汽集团商用车技术中心
 * 用于解析新能源数据，并传递相应给it
 * @author 余佶
 * @version 1.0
 */
package com.maxus.tsp.gateway.ota;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import com.maxus.tsp.gateway.common.constant.*;
import com.maxus.tsp.gateway.service.KafkaService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.maxus.tsp.common.constant.OTARealTimeConstants;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.platform.service.model.vo.CodeValue;
import com.maxus.tsp.platform.service.model.vo.RealTimeDataAlarm;
import com.maxus.tsp.platform.service.model.vo.RealTimeDataEngine;
import com.maxus.tsp.platform.service.model.vo.RealTimeDataExtermum;
import com.maxus.tsp.platform.service.model.vo.RealTimeDataFuelCell;
import com.maxus.tsp.platform.service.model.vo.RealTimeDataMotor;
import com.maxus.tsp.platform.service.model.vo.RealTimeDataTemp;
import com.maxus.tsp.platform.service.model.vo.RealTimeDataVehicle;
import com.maxus.tsp.platform.service.model.vo.RealTimeDataVoltage;
import com.maxus.tsp.platform.service.model.vo.ReportPos;

public class TspRealTimeDataServiceProc {

	// 记录日志
	private static Logger logger = LogManager.getLogger(TspRealTimeDataServiceProc.class);

	private KafkaService kafkaService;
	private long gatewayTimeIn;

	/**
	 * 构造方法
	 *
	 * @param kafkaService
	 */
	public TspRealTimeDataServiceProc(KafkaService kafkaService, long gatewayTimeIn) {
		this.gatewayTimeIn = gatewayTimeIn;
		this.kafkaService = kafkaService;
	}

	/**
	 * @Title: checkRealTimeData
	 * @Description: 检查
	 * @param tboxsn
	 * @return
	 */
	public byte[] checkRealTimeData(byte[] datagramBytes, String tboxsn) {
		byte[] outData = { OTAConstant.COMMON_RESULT_SUCCESS };
		// 报文总长度
		int realTimeDataLength = datagramBytes.length;
		// 剩余总长度，从总长开始
		int remainAnalysisLength = realTimeDataLength;
		// 剩余分析字节在原报文中的偏移， 从0 开始
		int remainAnalysisOffset = OTAConstant.ZERO;
		// 判断类型与最少长度是否正确
		try {
			if (realTimeDataLength <= OTARealTimeMemberConstants.REALTIMEDATA_TYPE_AND_DATETIME_LENGTH) {
				logger.warn("TBox(SN:{}): 实时上报数据报文长度错误", tboxsn);
				outData = null;
			} else if (datagramBytes[OTARealTimeMemberConstants.REALTIMEDATA_MSG_TYPE_OFFSET] != OTARealTimeMemberConstants.REALTIMEDATA_ISNOT_REAL_TIME
					&& datagramBytes[OTARealTimeMemberConstants.REALTIMEDATA_MSG_TYPE_OFFSET] != OTARealTimeMemberConstants.REALTIMEDATA_IS_REAL_TIME) {
				logger.warn("TBox(SN:{}): 实时上报数据报文类型错误", tboxsn);
				outData = null;
			} else {
				// 解析类型与收集时间
				byte[] dataTimebytes = new byte[OTAConstant.DATETIME_BYTES_SIZE];
				System.arraycopy(datagramBytes, OTARealTimeMemberConstants.REALTIMEDATA_MSG_DATETIME_OFFSET,
						dataTimebytes, OTAConstant.ZERO, OTAConstant.DATETIME_BYTES_SIZE);
				boolean realTimeDataType = false;
				if (datagramBytes[OTARealTimeMemberConstants.REALTIMEDATA_MSG_TYPE_OFFSET] == OTARealTimeMemberConstants.REALTIMEDATA_IS_REAL_TIME) {
					realTimeDataType = true;
				}

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(ByteUtil.bytesToDataTime(dataTimebytes)));
				long collectTime = calendar.getTimeInMillis();

				// 计算剩余长度
				remainAnalysisLength = remainAnalysisLength
						- OTARealTimeMemberConstants.REALTIMEDATA_TYPE_AND_DATETIME_LENGTH;
				remainAnalysisOffset = remainAnalysisOffset
						+ OTARealTimeMemberConstants.REALTIMEDATA_TYPE_AND_DATETIME_LENGTH;
				if (!analysisSubMsg(datagramBytes, remainAnalysisLength, remainAnalysisOffset, realTimeDataType,
						collectTime, tboxsn, gatewayTimeIn)) {
					outData[0] = OTAConstant.COMMON_RESULT_FAILED;
				}
			}
		} catch (Exception ex) {
			outData[0] = OTAConstant.COMMON_RESULT_FAILED;
			logger.error("TBox(SN:{}):解析过程发生错误:{},当前报文:{}", tboxsn, ThrowableUtil.getErrorInfoFromThrowable(ex), ByteUtil.byteToHex(datagramBytes));
		}
		return outData;
	}

	private static boolean isValidSubMsgSize(int curMsgSize, OTARealTimeDataMember curDataMember) {
		boolean isValid = true;
		if (curMsgSize < 0 || (curDataMember.isSingle()
				&& curMsgSize > OTARealTimeMemberConstants.REALTIMEDATA_FIXED_SUBMSG_MAX_SIZE)) {
			// 仅为子报文单条数据时，需要判断是否条数不超过最大值
			isValid = false;
		}
		return isValid;
	}

	private boolean analysisSubMsg(byte[] originWholeBytes, int remainAnalysisLength, int remainAnalysisOffset,
								   boolean realTimeDataType, long collectTime, String tboxsn, long gatewayTimeIn) {
		int curSubMsgCount = 0;
		StringBuilder gbRealTimeData = new StringBuilder();
		boolean analysisResult = true;
		RealTimeDataVehicle dataVehicle = null;
		RealTimeDataMotor[] dataMotor = null; // 整车数据; 驱动电机数据
		RealTimeDataFuelCell dataFuelCell = null;
		RealTimeDataEngine dataEngine = null; // 燃料电池数据; 发动机数据
		ReportPos dataPos = null;
		RealTimeDataExtermum dataExtermum = null; // 车辆位置数据; 极值数据
		RealTimeDataAlarm dataAlarm = null;
		RealTimeDataVoltage[] dataVoltage = null; // 报警数据;可充电储能装置电压数据
		RealTimeDataTemp[] dataTemp = null; // 可充电储能装置温度数据
		JSONArray jsonArray = new JSONArray();
		int analysisMember = OTARealTimeDataMember.Vehicle.value(); // 从整车数据开始分析
		while (analysisResult && remainAnalysisLength > 0) {
			curSubMsgCount = originWholeBytes[remainAnalysisOffset]; // 获取当前待解析的子报文条数
			OTARealTimeDataMember curMember = OTARealTimeDataMember.getByCode(analysisMember);
			if (curMember == null) { // 当前子报文未在OTA中定义
				logger.warn("TBox(SN:{})当前实时数据上报报文存在OTA定义外报文字段, 分析失败!", tboxsn);
				analysisResult = false;
				break;
			} else if (!isValidSubMsgSize(curSubMsgCount, curMember)) { // 当前子报文长度非法
				analysisResult = false;
				break;
			}
			// 调整每个子报文个数所占的字节
			remainAnalysisLength = remainAnalysisLength - 1;
			remainAnalysisOffset = remainAnalysisOffset + 1;
			if (curSubMsgCount >= OTARealTimeMemberConstants.REALTIME_SUBDATA_EXIST) {
				int curSubMsgValidSize = 0;
				if (curMember.isSingle()) { // 对应成员只有单条报文,获取子报文段，进行解析
					switch (curMember) {
						case Vehicle:
							curSubMsgValidSize = OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_VEHICLE_SIZE;
							break;
						case FuelCell: // 重新计算fuelCell报文实际长度
							int tempProbeCntSize = OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_FUEL_CELL_TEMP_PROBE_CNT_SIZE;
							if (remainAnalysisLength > OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_FUEL_CELL_TEMP_PROBE_CNT_OFFSET
									+ tempProbeCntSize + 1) {
								byte[] tempProbeCntByte = new byte[tempProbeCntSize];
								System.arraycopy(originWholeBytes,
										remainAnalysisOffset
												+ OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_FUEL_CELL_TEMP_PROBE_CNT_OFFSET,
										tempProbeCntByte, 0, tempProbeCntSize);
								int tempProbeCnt = ByteUtil.byte2Short(tempProbeCntByte);
								curSubMsgValidSize = tempProbeCnt
										+ OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_FUEL_CELL_FIXPART_SIZE;
							}
							break;
						case Engine:
							curSubMsgValidSize = OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_ENGINE_SIZE;
							break;
						case Pos:
							curSubMsgValidSize = OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_GPS_SIZE;
							break;
						case Extermum:
							//curSubMsgValidSize = OtaSupported.getRealDateExtermumBytesSizeSupported(tboxsn);
							curSubMsgValidSize = OTARealTimeConstants.REALTIMEDATA_SINGLE_EXTERMUM_SIZE;
							if (OtaVersionFeature.REALDATEEXTERMUM.isSupported(tboxsn)) {
								curSubMsgValidSize = OTARealTimeConstants.REALTIMEDATA_SINGLE_EXTERMUM_OTA22_SIZE;
							}
							break;
						case Alarm: // 重新计算Alarm实际长度
							int alarmFaultCntSize = OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_ALARM_FAULT_CNT_SIZE;
							int curFaultCntOffset = OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_ALARM_FAULT_CNT_OFFSET;
							int curFaultInt;
							curSubMsgValidSize = OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_ALARM_FIXPART_SIZE;
							for (int i = 0; i < OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_ALARM_FAULT_TYPE_CNT; i++) {
								byte[] curFaultByte = new byte[alarmFaultCntSize];
								System.arraycopy(originWholeBytes, remainAnalysisOffset + curFaultCntOffset, curFaultByte,
										0, alarmFaultCntSize);
								curFaultInt = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(curFaultByte[0]));
								logger.debug("TBox(SN:{}):当前报警大小:{}", tboxsn, curFaultInt);
								if (curFaultInt > 0 && curFaultInt <= 253) {
									curSubMsgValidSize = curSubMsgValidSize + alarmFaultCntSize + curFaultInt
											* OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_ALARM_FAULT_CODE_SIZE;
									curFaultCntOffset = curFaultCntOffset + alarmFaultCntSize + curFaultInt
											* OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_ALARM_FAULT_CODE_SIZE;
								} else {
									logger.warn("TBox(SN:{})当前实时数据中的报警数据可充电储能装置故障总数不在范围[0,253]内, 可充电储能装置故障总数:{}", tboxsn, curFaultInt);
									curFaultCntOffset = curFaultCntOffset + alarmFaultCntSize;
									curSubMsgValidSize = curSubMsgValidSize + alarmFaultCntSize;
								}
								if (remainAnalysisLength < curSubMsgValidSize) { // 长度不正确，直接退出
									logger.warn("TBox(SN:{})当前实时数据中的报警数据长度错误!", tboxsn);
									curSubMsgValidSize = 0;
									break;
								}
							}
							break;
						default:
							break;
					}
					if (curSubMsgValidSize > 0 && remainAnalysisLength > curSubMsgValidSize) {
						// 截取子报文，并且分析相应对象
						byte[] analysisByte = new byte[curSubMsgValidSize];
						System.arraycopy(originWholeBytes, remainAnalysisOffset, analysisByte, OTAConstant.ZERO,
								curSubMsgValidSize);
						logger.debug("TBox(SN:{}):当前新能源报文中{}字节:{}", tboxsn, curMember.getMessage(),
								ByteUtil.byteToHex(analysisByte));
						// 调用解析整车报文方法，获得对象
						switch (curMember) {
							case Vehicle:
								dataVehicle = checkRealTimeDataVehicle(analysisByte, tboxsn);
								// GB拼接
								if (dataVehicle.getGBdatagramBytes() != null) {
									gbRealTimeData.append(ByteUtil.byteToHex(dataVehicle.getGBdatagramBytes()));
								} else {
									analysisResult = false;
								}
								jsonArray.addAll(JSONObject.parseArray(dataVehicle.getRealTimeDataVehicleJSONInfo()));
								break;
							case FuelCell:
								dataFuelCell = checkRealTimeDataFuelCell(analysisByte, tboxsn);
								// GB拼接
								if (dataFuelCell.getGBdatagramBytes() != null) {
									gbRealTimeData.append(ByteUtil.byteToHex(dataFuelCell.getGBdatagramBytes()));
								} else {
									analysisResult = false;
								}
								jsonArray.addAll(JSONObject.parseArray(dataFuelCell.getRealTimeDataFuelCellJSONInfo()));
								break;
							case Engine:
								dataEngine = checkRealTimeDataEngine(analysisByte, tboxsn);
								// GB拼接
								if (dataEngine.getGBdatagramBytes() != null) {
									gbRealTimeData.append(ByteUtil.byteToHex(dataEngine.getGBdatagramBytes()));
								} else {
									analysisResult = false;
								}
								jsonArray.addAll(JSONObject.parseArray(dataEngine.getRealTimeDataEngineJSONInfo()));
								break;
							case Pos:
								dataPos = checkRealTimeDataGPS(analysisByte, tboxsn);
								// GB拼接
								if (dataPos.getGBdatagramBytes() != null) {
									gbRealTimeData.append(ByteUtil.byteToHex(dataPos.getGBdatagramBytes()));
								} else {
									analysisResult = false;
								}
								jsonArray.addAll(JSONObject.parseArray(getRealTimeDataGPSJSONInfo(dataPos)));
								break;
							case Extermum:
								dataExtermum = checkRealTimeDataExtermum(analysisByte, tboxsn);
								// GB拼接
								if (dataExtermum.getGBdatagramBytes() != null) {
									gbRealTimeData.append(ByteUtil.byteToHex(dataExtermum.getGBdatagramBytes()));
								} else {
									analysisResult = false;
								}
								jsonArray.addAll(JSONObject.parseArray(dataExtermum.getRealTimeDataExtermumJSONInfo()));
								break;
							case Alarm:
								dataAlarm = checkRealTimeDataAlarm(analysisByte, tboxsn);
								// GB拼接
								if (dataAlarm.getGBdatagramBytes() != null) {
									gbRealTimeData.append(ByteUtil.byteToHex(dataAlarm.getGBdatagramBytes()));
								} else {
									analysisResult = false;
								}
								jsonArray.addAll(JSONObject.parseArray(dataAlarm.getRealTimeDataAlarmJSONInfo()));
								break;
							default:
								break;
						}
						remainAnalysisLength = remainAnalysisLength - curSubMsgValidSize;
						remainAnalysisOffset = remainAnalysisOffset + curSubMsgValidSize;
					} else { // 剩余报文长度不正确,失败
						analysisResult = false;
						break;
					}
				} else { // 当子报文数据为多组数据时
					switch (curMember) {
						case Motor:
							dataMotor = new RealTimeDataMotor[curSubMsgCount];
							// GB拼接
							gbRealTimeData.append((byte) 0x02);
							curSubMsgValidSize = OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_MOTOR_SIZE;
							if (curSubMsgValidSize > 0 && remainAnalysisLength >= (curSubMsgCount * curSubMsgValidSize)) {
								for (int i = 0; i < curSubMsgCount; i++) {
									byte[] analysisByte = new byte[curSubMsgValidSize];
									System.arraycopy(originWholeBytes, remainAnalysisOffset, analysisByte, OTAConstant.ZERO,
											curSubMsgValidSize);
									logger.debug("TBox(SN:{}):当前新能源报文中{}字节:{}", tboxsn,
											curMember.getMessage(), ByteUtil.byteToHex(analysisByte));
									// GB拼接
									gbRealTimeData.append(ByteUtil.byteToHex(analysisByte));
									RealTimeDataMotor curMotor = checkRealTimeDataMotor(analysisByte, tboxsn);
									jsonArray.addAll(JSONObject.parseArray(curMotor.getRealTimeDataMotorJSONInfo()));
									dataMotor[i] = curMotor;
									remainAnalysisLength = remainAnalysisLength - curSubMsgValidSize;
									remainAnalysisOffset = remainAnalysisOffset + curSubMsgValidSize;
								}
								CodeValue motorCnt = new CodeValue(OTARealTimeConstants.MOTOR_COUNT, curSubMsgCount);
								jsonArray.add(motorCnt);
							} else {
								analysisResult = false;
							}
							break;
						case Voltage:
							dataVoltage = new RealTimeDataVoltage[curSubMsgCount];
							Object[] voltageValueList = new Object[curSubMsgCount];
							for (int i = 0; i < curSubMsgCount; i++) {
								curSubMsgValidSize = OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_VOLTAGE_FIXPART_SIZE;
								int cellCntSize = OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_VOLTAGE_CELL_CNT_SIZE;
								if (remainAnalysisLength < curSubMsgValidSize) { // 长度不正确，直接退出
									analysisResult = false;
									break;
								} else { // 重新计算Voltage报文实际长度
									byte[] cellCntByte = new byte[cellCntSize];
									System.arraycopy(originWholeBytes, remainAnalysisOffset + curSubMsgValidSize,
											cellCntByte, 0, cellCntSize);
									curSubMsgValidSize = curSubMsgValidSize + cellCntSize + ByteUtil.byte2Short(cellCntByte)
											* OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_VOLTAGE_CELL_VOLTAGE_SIZE;
								}
								byte[] analysisByte = new byte[curSubMsgValidSize];
								// 计算当前可充电储能装置子系统报文长度
								if (remainAnalysisLength < curSubMsgValidSize) {
									analysisResult = false;
									break;
								} else {
									System.arraycopy(originWholeBytes, remainAnalysisOffset, analysisByte, 0,
											curSubMsgValidSize);
									logger.debug("TBox(SN:{}):当前新能源报文中{}字节:{}", tboxsn,
											curMember.getMessage(), ByteUtil.byteToHex(analysisByte));
									RealTimeDataVoltage curVoltage = checkRealTimeDataVoltage(analysisByte, tboxsn);
									Object curValue = JSONObject.parseObject(curVoltage.getRealTimeDataVoltageJSONInfo());
									if (!StringUtils.isBlank(curValue.toString())) {
										voltageValueList[i] = curValue;
									} else {
										analysisResult = false;
										break;
									}
									dataVoltage[i] = curVoltage;
									remainAnalysisLength = remainAnalysisLength - curSubMsgValidSize;
									remainAnalysisOffset = remainAnalysisOffset + curSubMsgValidSize;
								}
							}
							if (curSubMsgCount > 0 && analysisResult) {
								CodeValue voltageCodeValue = new CodeValue(OTARealTimeConstants.VOLTAGE_DATA_LIST,
										voltageValueList);
								CodeValue voltageCnt = new CodeValue(OTARealTimeConstants.VOTLTAGE_COUNT, curSubMsgCount);
								jsonArray.add(voltageCodeValue);
								jsonArray.add(voltageCnt);
							}
							break;
						case Temp:
							dataTemp = new RealTimeDataTemp[curSubMsgCount];
							Object[] tempValueList = new Object[curSubMsgCount];
							for (int i = 0; i < curSubMsgCount; i++) { // 计算长度
								curSubMsgValidSize = OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_TEMP_FIXPART_SIZE;
								int tempCntSize = OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_TEMP_TEMP_CNT_SIZE;
								if (remainAnalysisLength < curSubMsgValidSize) { // 长度不正确，直接退出
									analysisResult = false;
									break;
								} else { // 重新计算Temp报文实际长度
									byte[] cellCntByte = new byte[tempCntSize];
									System.arraycopy(originWholeBytes, remainAnalysisOffset + curSubMsgValidSize,
											cellCntByte, 0, tempCntSize);
									curSubMsgValidSize = curSubMsgValidSize + tempCntSize
											+ ByteUtil.byte2Short(cellCntByte);
								}
								byte[] analysisByte = new byte[curSubMsgValidSize];
								// 计算当前可充电储能装置子系统报文长度
								if (remainAnalysisLength < curSubMsgValidSize) {
									analysisResult = false;
									break;
								} else {
									System.arraycopy(originWholeBytes, remainAnalysisOffset, analysisByte, 0,
											curSubMsgValidSize);
									logger.debug("TBox(SN:{}):当前新能源报文中{}字节:{}", tboxsn,
											curMember.getMessage(), ByteUtil.byteToHex(analysisByte));
									RealTimeDataTemp curTemp = checkRealTimeDataTemp(analysisByte, tboxsn);
									Object curTempstr = JSONObject.parseObject(curTemp.getRealTimeDataTempJSONInfo());
									if (!StringUtils.isBlank(curTempstr.toString())) {
										tempValueList[i] = curTempstr;
									} else {
										analysisResult = false;
										break;
									}
									dataTemp[i] = curTemp;
									remainAnalysisLength = remainAnalysisLength - curSubMsgValidSize;
									remainAnalysisOffset = remainAnalysisOffset + curSubMsgValidSize;
								}
							}
							if (curSubMsgCount > 0 && analysisResult) {
								CodeValue tempCodeValue = new CodeValue(OTARealTimeConstants.TEMP_DATA_LIST,
										tempValueList);
								CodeValue tempCnt = new CodeValue(OTARealTimeConstants.TEMP_COUNT, curSubMsgCount);
								jsonArray.add(tempCodeValue);
								jsonArray.add(tempCnt);
							}
							break;
						default:
							break;
					}
				}
			} else {
				logger.debug("TBox(SN:{}):当前新能源报文中{}字节为0", tboxsn, curMember.getMessage());
			}
			analysisMember = analysisMember + 1; // 分析下一个成员数据
		}
		if (analysisResult) { // 分析成功，则传递数据
			// 传递给RVM平台
			transRealTimeData(jsonArray, tboxsn, realTimeDataType, collectTime, gatewayTimeIn);
		}
		return analysisResult;
	}

	public void transRealTimeData(JSONArray jsonArray, String tboxsn, boolean isRealTime, long collectTime, long gatewayTimeIn) {
		if (jsonArray != null) {
			HashMap<String, String> valueString = new HashMap<String, String>();
			valueString.put("items", JSONObject.toJSONString(jsonArray, SerializerFeature.WriteMapNullValue));
			logger.debug("当前待传递的数据:{}", JSONObject.toJSONString(valueString));
			List<CodeValue> curItems = JSONObject.parseArray(
					JSONObject.toJSONString(jsonArray, SerializerFeature.WriteMapNullValue), CodeValue.class);
			CodeValue[] items = new CodeValue[curItems.size()];
			curItems.toArray(items);
			// 如果无法传递过去
			if (isRealTime) {
				kafkaService.transferOTAData(tboxsn, KafkaOtaDataCommand.KAFKA_PARAM_REALTIME_DATA, collectTime,
						items, gatewayTimeIn);
			} else {
				kafkaService.transferOTAData(tboxsn, KafkaOtaDataCommand.KAFKA_PARAM_RE_REALTIME_DATA,
						collectTime, items, gatewayTimeIn);
			}
		}
	}

	public static RealTimeDataVehicle checkRealTimeDataVehicle(byte[] datagramBytes, String tboxsn) {
		RealTimeDataVehicle curObj = new RealTimeDataVehicle(datagramBytes, tboxsn);
		return curObj;
	}

	public static RealTimeDataMotor checkRealTimeDataMotor(byte[] datagramBytes, String tboxsn) {
		RealTimeDataMotor curObj = new RealTimeDataMotor(datagramBytes, tboxsn);
		return curObj;
	}

	public static RealTimeDataFuelCell checkRealTimeDataFuelCell(byte[] datagramBytes, String tboxsn) {
		RealTimeDataFuelCell curObj = new RealTimeDataFuelCell(datagramBytes, tboxsn);
		return curObj;
	}

	public static RealTimeDataEngine checkRealTimeDataEngine(byte[] datagramBytes, String tboxsn) {
		RealTimeDataEngine curObj = new RealTimeDataEngine(datagramBytes, tboxsn);
		return curObj;
	}

	public static RealTimeDataExtermum checkRealTimeDataExtermum(byte[] datagramBytes, String tboxsn) {
		boolean isExtend = OtaVersionFeature.REALDATEEXTERMUM.isSupported(tboxsn);
		RealTimeDataExtermum curObj = new RealTimeDataExtermum(datagramBytes, tboxsn, isExtend);
		return curObj;
	}

	public static RealTimeDataAlarm checkRealTimeDataAlarm(byte[] datagramBytes, String tboxsn) {
		RealTimeDataAlarm curObj = new RealTimeDataAlarm(datagramBytes, tboxsn);
		return curObj;
	}

	public static RealTimeDataVoltage checkRealTimeDataVoltage(byte[] datagramBytes, String tboxsn) {
		RealTimeDataVoltage curObj = new RealTimeDataVoltage(datagramBytes, tboxsn);
		return curObj;
	}

	public static RealTimeDataTemp checkRealTimeDataTemp(byte[] datagramBytes, String tboxsn) {
		RealTimeDataTemp curObj = new RealTimeDataTemp(datagramBytes, tboxsn);
		return curObj;
	}

	public static ReportPos checkRealTimeDataGPS(byte[] datagramBytes, String tboxsn) {
		if (datagramBytes.length != OTARealTimeMemberConstants.REALTIMEDATA_SINGLE_GPS_SIZE) {
			return null;
		}
		ReportPos curPos = new ReportPos(datagramBytes, false);
		return curPos;
	}

	private static String getRealTimeDataGPSJSONInfo(ReportPos rpPosData) {
		BigDecimal labd = new BigDecimal(rpPosData.getLatitude() / OperationConstant.GPS_PRECISION);
		BigDecimal lgbd = new BigDecimal(rpPosData.getLongitude() / OperationConstant.GPS_PRECISION);
		String validStatus = OperationConstant.INVALID; // 数据有效性
		if (rpPosData.getValidStatus() > 0) {
			validStatus = OperationConstant.VALID;
		}
		String latitude = OperationConstant.SOUTH_LATITUDE; // 南北纬
		if (rpPosData.getLatitude() > 0) {
			latitude = OperationConstant.NORTH_LATITUDE;
		}
		String logitude = OperationConstant.WEST_LONGITUDE; // 东西经
		if (rpPosData.getLongitude() > 0) {
			logitude = OperationConstant.EAST_LONGITUDE;
		}
		StringBuilder curStatus = new StringBuilder();
		curStatus.append(validStatus).append(OperationConstant.COMMA).append(latitude).append(OperationConstant.COMMA)
				.append(logitude);
		List<CodeValue> codeValue = new ArrayList<CodeValue>();
		codeValue.add(new CodeValue(OTARealTimeConstants.GPS_VALID, curStatus.toString()));
		codeValue.add(new CodeValue(OTARealTimeConstants.GPS_LATITUDE_VALUE,
				labd.setScale(OperationConstant.GPS_PRECISION_SCALE, BigDecimal.ROUND_HALF_UP).doubleValue()));
		codeValue.add(new CodeValue(OTARealTimeConstants.GPS_LONGITUDE_VALUE,
				lgbd.setScale(OperationConstant.GPS_PRECISION_SCALE, BigDecimal.ROUND_HALF_UP).doubleValue()));
		return JSONObject.toJSONString(codeValue);
	}

}
