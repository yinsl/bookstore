package com.maxus.tsp.gateway.ota.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.alibaba.fastjson.JSONObject;
import com.maxus.tsp.common.constant.OTARealTimeConstants;
import com.maxus.tsp.common.enums.CarCanStatusEnum;
import com.maxus.tsp.common.enums.CarcanPowerBattStatusEnum;
import com.maxus.tsp.common.enums.CarcanSystemPowerModeEnum;
import com.maxus.tsp.common.enums.ReportCarCanEnum;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.DateUtil;
import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.gateway.common.constant.OTAConstant;
import com.maxus.tsp.gateway.common.constant.OtaVersionFeature;
import com.maxus.tsp.gateway.common.ota.OTAMessage;
import com.maxus.tsp.gateway.common.ota.TimeLogUtil;
import com.maxus.tsp.gateway.service.DataProcessing;
import com.maxus.tsp.platform.service.model.vo.BossCanStreamVo;
import com.maxus.tsp.platform.service.model.vo.CodeValue;
import com.maxus.tsp.platform.service.model.vo.ReportCan;
import com.maxus.tsp.platform.service.model.vo.ReportCanExtData;

/**
 * @Title TspServiceProc_ReportCan
 * @Description 车况数据上报
 * @author 汪亚军
 * @date 2018年11月29日
 */
@Component
public class ProcReportCan extends BaseOtaProc {

	// 日志
	private static Logger logger = LogManager.getLogger(ProcReportCan.class);
	// 网关接收报文时间
	private long gatewayTimeIn;
	
	@Autowired
    private DataProcessing dataProcessing;

	/**
	 * @Title: checkDataReportCan
	 * @Description: 检查车况
	 * @param requestMsg
	 * @return
	 */
	public byte[] checkDataReportCan(OTAMessage requestMsg) {
		return inDataGPSCAN(requestMsg.getParam(), requestMsg.getSerialNumber());
	}

	/**
	 * @Title: inDataGPSCAN
	 * @Description: 车况数据上报
	 * @param datagramBytes
	 * @param serialNumber
	 * @return result
	 */
	private byte[] inDataGPSCAN(byte[] datagramBytes, String serialNumber) {
		String logName ="OTAResolveServerHandler.resolve -> procOTA -> ProcReportCan.inDataGPSCAN: ";
		TimeLogUtil.log(logName+"start");
		byte[] recCount = new byte[1];
		System.arraycopy(datagramBytes, 0, recCount, 0, 1);
		// 子车况个数
		int gpsCanLength = (short) (recCount[0]);
		int offset = 1;
		
		// 基础车况40字节
		boolean extData = false;
		int singleDataLength = OTARealTimeConstants.REPORT_CARCAN_LENGTH;
		// 版本判断顺序：由大至小（且注意特殊版本）
		// 200.18蔚来版本车况
		if (OtaVersionFeature.NIODATA.isSupported(serialNumber)) {
			singleDataLength = (datagramBytes.length - 1) / gpsCanLength;
		} else if (OtaVersionFeature.EXTDATA.isSupported(serialNumber) && !OtaVersionFeature.getVersion(serialNumber).equalsIgnoreCase("C812")) {
			// 0.32版本新增ExtData
			extData = true;
		} else if (OtaVersionFeature.SPEEDLIMITCMD.isSupported(serialNumber)) {
			// 0.23版本添加跛行支持；
			singleDataLength = OTARealTimeConstants.REPORT_CARCAN_ADD_SPEEDLIMIT_LENGTH;
		} else if (OtaVersionFeature.POWERBATTTEMP.isSupported(serialNumber)) {
			// 0.20版本是否支持PowerBattStatus、SOC、PowerBattTemp车况调整长度
			singleDataLength = OTARealTimeConstants.REPORT_CARCAN_ADD_POWERBATTERY_LENGTH;
		}
		
		TimeLogUtil.log(logName+"基础车况");
		
		byte[] curGPSCANbytes = new byte[singleDataLength];// 基础40字节的解析
		byte[] result = { OTAConstant.COMMON_RESULT_SUCCESS }; // 记录结果,初始化为成功
		String currentVIN = tboxService.getVINForTbox(serialNumber);
		
		TimeLogUtil.log(logName+"tboxService.getVINForTbox完成");
		// if (currentVIN == null) {
		// 	logger.warn("TBox(SN:{}): 从Redis获取VIN号异常", serialNumber);
		// 	currentVIN = "";
		// }
		ReportCan[] poses = new ReportCan[gpsCanLength];
		try {
			// 数据长度正确
			if (!extData) {
				if ((gpsCanLength * singleDataLength + 1) == datagramBytes.length) {
					// 长度一致时，即数据格式正确
					// 分析每一组can数据，进行
					for (int i = 1; i <= gpsCanLength; i++) {
						// 获取当前组GPSCAN数据
						System.arraycopy(datagramBytes, offset, curGPSCANbytes, 0, singleDataLength);
						ReportCan rpSingleCan = new ReportCan(curGPSCANbytes,
								OtaVersionFeature.getVersion(serialNumber));
						if (!rpSingleCan.isReportCanDataCorrect()) {
							// 数据解析出错
							result[0] = OTAConstant.COMMON_RESULT_FAILED;
							return result;
						}
						poses[i - 1] = rpSingleCan;
						// 计算下一组数据偏移
						offset += singleDataLength;
					}
				} else {
					logger.warn("TBox(SN:{}): CAN数据异常，原因：参数长度错误", serialNumber);
					result[0] = OTAConstant.COMMON_RESULT_FAILED;
				}
			} else {
				// 对于带ExtData非定长车况解析
				int totalLength = 1;// 总参数长度
				byte[] extSize = new byte[2];
				int[] singleExtCanLength = new int[gpsCanLength];
				for (int i = 1; i <= gpsCanLength; i++) {
					totalLength += OTARealTimeConstants.REPORT_CARCAN_ADD_SPEEDLIMIT_LENGTH;
					System.arraycopy(datagramBytes, totalLength, extSize, 0, 2);
					singleExtCanLength[i - 1] = ByteUtil.getUnsignedInt(extSize);
					totalLength += (singleExtCanLength[i - 1] + 2);
				}
				if (totalLength == datagramBytes.length) {
					byte[] basecan = new byte[OTARealTimeConstants.REPORT_CARCAN_ADD_SPEEDLIMIT_LENGTH];
					totalLength = 1;
					for (int i = 1; i <= gpsCanLength; i++) {
						System.arraycopy(datagramBytes, totalLength, basecan, 0,
								OTARealTimeConstants.REPORT_CARCAN_ADD_SPEEDLIMIT_LENGTH);
						byte[] extDataCan = new byte[singleExtCanLength[i - 1] + 2];
						totalLength += OTARealTimeConstants.REPORT_CARCAN_ADD_SPEEDLIMIT_LENGTH;
						System.arraycopy(datagramBytes, totalLength, extDataCan, 0, singleExtCanLength[i - 1] + 2);
						totalLength += singleExtCanLength[i - 1] + 2;
						ReportCanExtData extDataCanRec = new ReportCanExtData(basecan, extDataCan,
								OtaVersionFeature.getVersion(serialNumber));

						if (extDataCanRec.isReportCanDataCorrect()) {
							poses[i - 1] = extDataCanRec;
						} else {
							result[0] = OTAConstant.COMMON_RESULT_FAILED;
							return result;
						}
					}
					TimeLogUtil.log(logName+"line162  车况分析");
				} else {
					logger.warn("TBox(SN:{}): CAN(ExtData)数据异常，原因：参数长度错误", serialNumber);
					result[0] = OTAConstant.COMMON_RESULT_FAILED;
					return result;
				}
			}
			// 插入数据库的操作，投递至kafka进行
			// 将参数封装成json对象进行传递
			BossCanStreamVo bossCanStream = new BossCanStreamVo(poses, currentVIN, serialNumber);
			if (dataProcessing.isTBoxDataNeedTransfer(serialNumber)) {
				logger.info("TBox(SN:{})已查询到TBox信息，开始向RVM投递车况！", serialNumber);
				transferReportCanData(bossCanStream);
				TimeLogUtil.log(logName+"RVM投递车况完成");
			}
			result[0] = OTAConstant.COMMON_RESULT_SUCCESS;
			
		} catch (Exception ex) {
			logger.error("TBox(SN:{}): CAN数据报文处理异常，原因:{}", serialNumber,
					ThrowableUtil.getErrorInfoFromThrowable(ex));
			result[0] = OTAConstant.COMMON_RESULT_FAILED;
		}
		TimeLogUtil.log(logName+"报文解析完成");
		return result;
	}

	/**
	 * @Title: transferReportCanData
	 * @Description:解析并封装转投到IT的can数据
	 * @param bossCanStream
	 */
	private void transferReportCanData(BossCanStreamVo bossCanStream) {
		ReportCan[] reportCanGroup = bossCanStream.getReportCanGroup();

		String serialNum = bossCanStream.getSerialNumber();

		for (ReportCan reportcandata : reportCanGroup) {
			doAnalysisReportCan(reportcandata, serialNum, reportcandata.getCollectDataTime());
		}
	}

	/**
	 * @Title: doAnalysisReportCan
	 * @Description:封装IT车况消息
	 * @param reportCanData
	 * @param serialNum
	 * @param collectiontime
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public void doAnalysisReportCan(ReportCan reportCanData, String serialNum, String collectiontime) {
		try {
			Map<String, Object> reportCanRecrMap = new HashMap<String, Object>();
			// CARACN_AVERG_FUEl_CONSUMPTION 1
			if (!reportCanData.getAverageFuelConsumption().equals(OTARealTimeConstants.CARCAN_INVALID_DATA)) {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_AVERG_FUEl_CONSUMPTION.getCode(),
						Float.parseFloat(reportCanData.getAverageFuelConsumption()));
			} else {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_AVERG_FUEl_CONSUMPTION.getCode(),
						OTARealTimeConstants.ILLEGAL_NUM);
			}
			// CARACN_REMIANING_DISTANCE 2
			if (!reportCanData.getRemainingDistance().equals(OTARealTimeConstants.CARCAN_INVALID_DATA)) {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_REMIANING_DISTANCE.getCode(),
						Float.parseFloat(reportCanData.getRemainingDistance()));
			} else {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_REMIANING_DISTANCE.getCode(),
						OTARealTimeConstants.ILLEGAL_NUM);
			}
			// CARACN_FRONT_LEFT_TIER_PRESSURE 3
			if (!reportCanData.getFrontLeftTirePressure().equals(OTARealTimeConstants.CARCAN_INVALID_DATA)) {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_FRONT_LEFT_TIER_PRESSURE.getCode(),
						Float.parseFloat(reportCanData.getFrontLeftTirePressure()));
			} else {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_FRONT_LEFT_TIER_PRESSURE.getCode(),
						OTARealTimeConstants.ILLEGAL_NUM);
			}
			// CARACN_FRONT_RIGHT_TIER_PRESSURE 4
			if (!reportCanData.getFrontRightTirePressure().equals(OTARealTimeConstants.CARCAN_INVALID_DATA)) {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_FRONT_RIGHT_TIER_PRESSURE.getCode(),
						Float.parseFloat(reportCanData.getFrontRightTirePressure()));
			} else {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_FRONT_RIGHT_TIER_PRESSURE.getCode(),
						OTARealTimeConstants.ILLEGAL_NUM);
			}
			// CARCAN_REAR_LEFT_TIER_PRESSURE 5
			if (!reportCanData.getRearLeftTirePressure().equals(OTARealTimeConstants.CARCAN_INVALID_DATA)) {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_REAR_LEFT_TIER_PRESSURE.getCode(),
						Float.parseFloat(reportCanData.getRearLeftTirePressure()));
			} else {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_REAR_LEFT_TIER_PRESSURE.getCode(),
						OTARealTimeConstants.ILLEGAL_NUM);
			}
			// CARCAN_REAR_RIGHT_TIER_PRESSURE 6
			if (!reportCanData.getRearRightTirePressure().equals(OTARealTimeConstants.CARCAN_INVALID_DATA)) {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_REAR_RIGHT_TIER_PRESSURE.getCode(),
						Float.parseFloat(reportCanData.getRearRightTirePressure()));
			} else {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_REAR_RIGHT_TIER_PRESSURE.getCode(),
						OTARealTimeConstants.ILLEGAL_NUM);
			}
			// CARCAN_FUEL_LEVEL 7
			if (!reportCanData.getFuelLevel().equals(OTARealTimeConstants.CARCAN_INVALID_DATA)) {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_FUEL_LEVEL.getCode(),
						Float.parseFloat(reportCanData.getFuelLevel()));
			} else {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_FUEL_LEVEL.getCode(), OTARealTimeConstants.ILLEGAL_NUM);
			}
			// CARCAN_IN_CAR_TEMP 8
			if (!reportCanData.getInCarTemp().equals(OTARealTimeConstants.CARCAN_INVALID_DATA)) {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_IN_CAR_TEMP.getCode(),
						Float.parseFloat(reportCanData.getInCarTemp()));
			} else {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_IN_CAR_TEMP.getCode(), OTARealTimeConstants.ILLEGAL_NUM);
			}
			// CARCAN_VEHICLE_SPEED 9
			if (!reportCanData.getVehicleSpeed().equals(OTARealTimeConstants.CARCAN_INVALID_DATA)) {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_VEHICLE_SPEED.getCode(),
						Float.parseFloat(reportCanData.getVehicleSpeed()));
			} else {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_VEHICLE_SPEED.getCode(), OTARealTimeConstants.ILLEGAL_NUM);
			}
			// CARCAN_ENGINGE_SPEED 10
			if (!reportCanData.getEngineSpeed().equals(OTARealTimeConstants.CARCAN_INVALID_DATA)) {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_ENGINGE_SPEED.getCode(),
						Float.parseFloat(reportCanData.getEngineSpeed()));
			} else {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_ENGINGE_SPEED.getCode(), OTARealTimeConstants.ILLEGAL_NUM);
			}
			// CARCAN_ODO 11
			if (!reportCanData.getOdo().equals(OTARealTimeConstants.CARCAN_INVALID_DATA)) {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_ODO.getCode(), Double.parseDouble(reportCanData.getOdo()));
			} else {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_ODO.getCode(), OTARealTimeConstants.ILLEGAL_NUM);
			}
			// CARCAN_SYSTEM_POWER_MODE 12
			// 确认当前tbox是否支持某一OTA版本的Up Reportcan Rec SystemPowerMode数据
			if (OtaVersionFeature.POWERBATTSTATUS.isSupported(serialNum)) {
				reportCanRecrMap.put(CarCanStatusEnum.CARCAN_SYSTEM_POWER_MODE.getCode(),
						OTARealTimeConstants.CARCAN_INVALID_STATUS);
				for (CarcanSystemPowerModeEnum powermode : CarcanSystemPowerModeEnum.values()) {
					if (reportCanData.getSystemPowerMode().equals(powermode.getCode())) {
						reportCanRecrMap.put(CarCanStatusEnum.CARCAN_SYSTEM_POWER_MODE.getCode(), powermode.getValue());
						break;
					}
				}
			}
			// CARCAN_COLLECTION_TIME 22
			reportCanRecrMap.put(CarCanStatusEnum.CARCAN_COLLECTION_TIME.getCode(),
					DateUtil.stringToLong(collectiontime, "yyyy-MM-dd HH:mm:ss"));
			// CARCAN_POWERBATTSTATUS 23
			// 确认当前tbox是否支持ReportCan的PowerBattStatus车况
			if (!OtaVersionFeature.NIODATA.isSupported(serialNum)
					&& OtaVersionFeature.POWERBATTTEMP.isSupported(serialNum)) {
				if (!reportCanData.getPowerBattStatus().equals(OTARealTimeConstants.CARCAN_INVALID_DATA)) {
					for (CarcanPowerBattStatusEnum powerBattStatus : CarcanPowerBattStatusEnum.values()) {
						if (reportCanData.getPowerBattStatus().equals(powerBattStatus.getCode())) {
							reportCanRecrMap.put(CarCanStatusEnum.CARCAN_POWERBATTSTATUS.getCode(),
									powerBattStatus.getValue());
						}
					}
				} else {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_POWERBATTSTATUS.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
				}
				// CARCAN_SOC 24
				// 确认当前tbox是否支持ReportCan的SOC车况
				if (!reportCanData.getSoc().equals(OTARealTimeConstants.CARCAN_INVALID_DATA)) {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_SOC.getCode(),
							Integer.parseInt(reportCanData.getSoc()));
				} else {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_SOC.getCode(), OTARealTimeConstants.ILLEGAL_NUM);
				}
				// CARCAN_POWERBATTTEMP 25
				// 确认当前tbox是否支持ReportCan的CARCAN_POWERBATTTEMP车况
				if (!reportCanData.getPowerBattTemp().equals(OTARealTimeConstants.CARCAN_INVALID_DATA)) {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_POWERBATTTEMP.getCode(),
							Integer.parseInt(reportCanData.getPowerBattTemp()));
				} else {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_POWERBATTTEMP.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
				}
				// 判断CARCAN_SPEEDLIMIT_CMD 26
				if (OtaVersionFeature.SPEEDLIMITCMD.isSupported(serialNum)) {
					// CARCAN_SPEEDLIMIT_CMD
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_SPEEDLIMIT_CMD.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_SPEEDLIMIT_ACTIVE.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
					for (ReportCarCanEnum reportCarCanEnum : ReportCarCanEnum.values()) {

						if (reportCanData.getSpeedLimitCmd() != null && reportCanData.getSpeedLimitActive() != null) {
							if (reportCanData.getSpeedLimitCmd().equals(reportCarCanEnum.getCode())) {
								reportCanRecrMap.put(CarCanStatusEnum.CARCAN_SPEEDLIMIT_CMD.getCode(),
										reportCarCanEnum.getValue());
							}
							if (reportCanData.getSpeedLimitActive().equals(reportCarCanEnum.getCode())) {
								reportCanRecrMap.put(CarCanStatusEnum.CARCAN_SPEEDLIMIT_ACTIVE.getCode(),
										reportCarCanEnum.getValue());
							}
						} else {
							reportCanRecrMap.put(CarCanStatusEnum.CARCAN_SPEEDLIMIT_CMD.getCode(), null);
							reportCanRecrMap.put(CarCanStatusEnum.CARCAN_SPEEDLIMIT_ACTIVE.getCode(), null);

						}
					}
				}
			}
			// 确认当前tbox是否支持某一OTA版本的Up Reportcan Rec Status数据 262132
			if (OtaVersionFeature.POWERBATTSTATUS.isSupported(serialNum)) {

				long doorStatus = Long.parseLong(reportCanData.getDoorStatus());
				// CARCAN_DLCK_COCKPIT 13
				if ((doorStatus
						& OTARealTimeConstants.CARCAN_DLOCK_COKPIT_CHECK) == OTARealTimeConstants.CARCAN_DLCK_COCKPIT_LOCK) {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DLCK_COCKPIT.getCode(),
							OTARealTimeConstants.CARCAN_DOOR_LOCK);
				} else {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DLCK_COCKPIT.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
					if ((doorStatus
							& OTARealTimeConstants.CARCAN_DLOCK_COKPIT_CHECK) == OTARealTimeConstants.CARCAN_DOOR_UNLOCK_CLOSE_NUM) {
						reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DLCK_COCKPIT.getCode(),
								OTARealTimeConstants.CARCAN_DOOR_UNLOCK);
					}
				}
				// CARCAN_DLCK_COPILOT14
				if ((doorStatus
						& OTARealTimeConstants.CARCAN_DLOCK_COPILOT_CHECK) == OTARealTimeConstants.CARCAN_DLCK_COPILOT_LOCK) {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DLCK_COPILOT.getCode(),
							OTARealTimeConstants.CARCAN_DOOR_LOCK);
				} else {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DLCK_COPILOT.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
					if ((doorStatus
							& OTARealTimeConstants.CARCAN_DLOCK_COPILOT_CHECK) == OTARealTimeConstants.CARCAN_DOOR_UNLOCK_CLOSE_NUM) {
						reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DLCK_COPILOT.getCode(),
								OTARealTimeConstants.CARCAN_DOOR_UNLOCK);
					}
				}
				// CARCAN_DLCK_RTBCKSEAT 15
				if ((doorStatus
						& OTARealTimeConstants.CARCAN_DLOCK_RTBCKSEAT_CHECK) == OTARealTimeConstants.CARCAN_DLCK_RTBCKSEAT_LOCK) {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DLCK_RTBCKSEAT.getCode(),
							OTARealTimeConstants.CARCAN_DOOR_LOCK);
				} else {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DLCK_RTBCKSEAT.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
					if ((doorStatus
							& OTARealTimeConstants.CARCAN_DLOCK_RTBCKSEAT_CHECK) == OTARealTimeConstants.CARCAN_DOOR_UNLOCK_CLOSE_NUM) {
						reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DLCK_RTBCKSEAT.getCode(),
								OTARealTimeConstants.CARCAN_DOOR_UNLOCK);
					}
				}
				// CARCAN_DLCK_LFTBCKSEAT 16
				if ((doorStatus
						& OTARealTimeConstants.CARCAN_DLOCK_LFTBCKSEAT_CHECK) == OTARealTimeConstants.CARCAN_DLCK_LFTBCKSEAT_LOCK) {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DLCK_LFTBCKSEAT.getCode(),
							OTARealTimeConstants.CARCAN_DOOR_LOCK);
				} else {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DLCK_LFTBCKSEAT.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
					if ((doorStatus
							& OTARealTimeConstants.CARCAN_DLOCK_LFTBCKSEAT_CHECK) == OTARealTimeConstants.CARCAN_DOOR_UNLOCK_CLOSE_NUM) {
						reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DLCK_LFTBCKSEAT.getCode(),
								OTARealTimeConstants.CARCAN_DOOR_UNLOCK);
					}
				}
				// CARCAN_DSTATUS_COCKPIT 17
				if ((doorStatus
						& OTARealTimeConstants.CARCAN_DSTATUS_COCKPIT_CHECK) == OTARealTimeConstants.CARCAN_DSTATUS_COCKPIT_OPEN) {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DSTATUS_COCKPIT.getCode(),
							OTARealTimeConstants.CARCAN_DOOR_OPEN);
				} else {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DSTATUS_COCKPIT.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
					if ((doorStatus
							& OTARealTimeConstants.CARCAN_DSTATUS_COCKPIT_CHECK) == OTARealTimeConstants.CARCAN_DOOR_UNLOCK_CLOSE_NUM) {
						reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DSTATUS_COCKPIT.getCode(),
								OTARealTimeConstants.CARCAN_DOOR_CLOSE);
					}
				}
				// CARCAN_DSTATUS_COPILOT 18
				if ((doorStatus
						& OTARealTimeConstants.CARCAN_DSTATUS_COPILOT_CHECK) == OTARealTimeConstants.CARCAN_DSTATUS_COPILOT_OPEN) {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DSTATUS_COPILOT.getCode(),
							OTARealTimeConstants.CARCAN_DOOR_OPEN);
				} else {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DSTATUS_COPILOT.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
					if ((doorStatus
							& OTARealTimeConstants.CARCAN_DSTATUS_COPILOT_CHECK) == OTARealTimeConstants.CARCAN_DOOR_UNLOCK_CLOSE_NUM) {
						reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DSTATUS_COPILOT.getCode(),
								OTARealTimeConstants.CARCAN_DOOR_CLOSE);
					}
				}
				// CARCAN_DSTATUS_RGTBCKSEAT 19
				if ((doorStatus
						& OTARealTimeConstants.CARCAN_DSTATUS_RGTBCKSEAT_CHECK) == OTARealTimeConstants.CARCAN_DSTATUS_RGTBCKSEAT_OPEN) {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DSTATUS_RGTBCKSEAT.getCode(),
							OTARealTimeConstants.CARCAN_DOOR_OPEN);
				} else {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DSTATUS_RGTBCKSEAT.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
					if ((doorStatus
							& OTARealTimeConstants.CARCAN_DSTATUS_RGTBCKSEAT_CHECK) == OTARealTimeConstants.CARCAN_DOOR_UNLOCK_CLOSE_NUM) {
						reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DSTATUS_RGTBCKSEAT.getCode(),
								OTARealTimeConstants.CARCAN_DOOR_CLOSE);
					}
				}
				// CARCAN_DSTATUS_LFTBCKSEAT 20
				if ((doorStatus
						& OTARealTimeConstants.CARCAN_DSTATUS_LFTBCKSEAT_CHECK) == OTARealTimeConstants.CARCAN_DSTATUS_LFTBCKSEAT_OPEN) {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DSTATUS_LFTBCKSEAT.getCode(),
							OTARealTimeConstants.CARCAN_DOOR_OPEN);
				} else {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DSTATUS_LFTBCKSEAT.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
					if ((doorStatus
							& OTARealTimeConstants.CARCAN_DSTATUS_LFTBCKSEAT_CHECK) == OTARealTimeConstants.CARCAN_DOOR_UNLOCK_CLOSE_NUM) {
						reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DSTATUS_LFTBCKSEAT.getCode(),
								OTARealTimeConstants.CARCAN_DOOR_CLOSE);
					}
				}
				// CARCAN_DSTATUS_BCK 21
				// 确认当前tbox是否支持某一OTA版本的Up Reportcan Rec DoorStatus 尾门开关状态数据
				if (OtaVersionFeature.STATUS_TAIL_DOOR.isSupported(serialNum)) {
					if ((doorStatus
							& OTARealTimeConstants.CARCAN_DSTATUS_BCK_CHECK) == OTARealTimeConstants.CARCAN_DSTATUS_BCK_OPEN) {
						reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DSTATUS_BCK.getCode(),
								OTARealTimeConstants.CARCAN_DOOR_OPEN);
					} else {
						reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DSTATUS_BCK.getCode(),
								OTARealTimeConstants.ILLEGAL_NUM);
						if ((doorStatus
								& OTARealTimeConstants.CARCAN_DSTATUS_BCK_CHECK) == OTARealTimeConstants.CARCAN_DOOR_UNLOCK_CLOSE_NUM) {
							reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DSTATUS_BCK.getCode(),
									OTARealTimeConstants.CARCAN_DOOR_CLOSE);
						}
					}
				}
			}

			// 是否是蔚来的版本
			if (OtaVersionFeature.NIODATA.isSupported(serialNum)) {
				// 判断是否为空
				if (StringUtils.isEmpty(reportCanData.getStrgAglCnt())) {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_STRGAGL_CNT.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_STRGAGL_LIST.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
				} else {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_STRGAGL_CNT.getCode(),
							Integer.parseUnsignedInt(reportCanData.getStrgAglCnt()));
					List<Float> strgAglList = new ArrayList<Float>();
					for (String agl : reportCanData.getStrgAglList()) {
						if (agl != null) {
							strgAglList.add(Float.parseFloat(agl));
						} else {
							strgAglList.add(null);
						}
					}
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_STRGAGL_LIST.getCode(), strgAglList);
				}
				if (StringUtils.isEmpty(reportCanData.getAcclPosCnt())) {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_ACCLPOS_CNT.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_ACCLPOS_LIST.getCode(),
							OTARealTimeConstants.ILLEGAL_NUM);
				} else {
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_ACCLPOS_CNT.getCode(),
							Integer.parseUnsignedInt(reportCanData.getAcclPosCnt()));
					List<Integer> acclPosList = new ArrayList<Integer>();
					for (String acclPos : reportCanData.getAcclPosList()) {
						if (acclPos != null) {
							acclPosList.add(Integer.parseUnsignedInt(acclPos));
						} else {
							acclPosList.add(null);
						}
					}
					reportCanRecrMap.put(CarCanStatusEnum.CARCAN_ACCLPOS_LIST.getCode(), acclPosList);
				}
			}
			// 车况扩展字段
			if (OtaVersionFeature.EXTDATA.isSupported(serialNum)) {
				// 0.32版本新增ExtData(0.35版本新增的扩展字段在0.32版本后面追加)
				if (ReportCanExtData.class.isInstance(reportCanData)) {
					ReportCanExtData ReportCanExtData = (ReportCanExtData) reportCanData;
					Map<Short, Object> ExtData = ReportCanExtData.getExtData();
					if (ExtData.size() > 0) {
						logger.debug("TBox({})车况数据发送rvm做数据封装，车况扩展字段map集合:{}", serialNum,
								JSONObject.toJSONString(ExtData));
						for (Map.Entry<Short, Object> entry : ExtData.entrySet()) {
							// System.out.println("Key: "+ entry.getKey()+ " Value: "+entry.getValue());
							Short key = entry.getKey();
							Object value1 = entry.getValue();
							if (key == 6) {
								List<Integer> value = (List<Integer>) value1;
								reportCanRecrMap.put(CarCanStatusEnum.CARCAN_STRGAGL_CNT.getCode(), value.get(0));
								value.remove(0);
								for (int i = 0; i < value.size(); i++) {
									if (OTARealTimeConstants.INVALID_2BYTE_VALUE == value.get(i)) {
										value.set(i, null);
									}
								}
								reportCanRecrMap.put(CarCanStatusEnum.CARCAN_STRGAGL_LIST.getCode(), value);
							}else if(key == 7){
								List<Integer> value = (List<Integer>) value1;
								reportCanRecrMap.put(CarCanStatusEnum.CARCAN_ACCLPOS_CNT.getCode(), value.get(0));
								value.remove(0);
								for (int i = 0; i < value.size(); i++) {
									if (OTARealTimeConstants.INVALID_BYTE_STRING == value.get(i)) {
										value.set(i, null);
									}
								}
								reportCanRecrMap.put(CarCanStatusEnum.CARCAN_ACCLPOS_LIST.getCode(), value);
								
							}else {
								int value = Integer.parseInt(value1 == null ? "" : value1.toString());
								switch (key) {
								case 1: // 空调状态
									switch (value) {
										case 0:
											reportCanRecrMap.put(CarCanStatusEnum.CARCAN_AIRCONDITION_STATUS.getCode(), OTARealTimeConstants.REPORT_CARCAN_AIRCONDITION_STATUS_0);
											break;
										case 1:
											reportCanRecrMap.put(CarCanStatusEnum.CARCAN_AIRCONDITION_STATUS.getCode(), OTARealTimeConstants.REPORT_CARCAN_AIRCONDITION_STATUS_1);
											break;
										case 2:
											reportCanRecrMap.put(CarCanStatusEnum.CARCAN_AIRCONDITION_STATUS.getCode(), OTARealTimeConstants.REPORT_CARCAN_AIRCONDITION_STATUS_2);
											break;
										case 3:
											reportCanRecrMap.put(CarCanStatusEnum.CARCAN_AIRCONDITION_STATUS.getCode(), OTARealTimeConstants.REPORT_CARCAN_AIRCONDITION_STATUS_3);
											break;
										case 4:
											reportCanRecrMap.put(CarCanStatusEnum.CARCAN_AIRCONDITION_STATUS.getCode(), OTARealTimeConstants.REPORT_CARCAN_AIRCONDITION_STATUS_4);
											break;
										default:
											reportCanRecrMap.put(CarCanStatusEnum.CARCAN_AIRCONDITION_STATUS.getCode(), OTARealTimeConstants.ILLEGAL_NUM);
											break;
									}
									break;
								case 2: // 车外温度
									if (!(OTARealTimeConstants.INVALID_2BYTE_VALUE == value)) {
										reportCanRecrMap.put(CarCanStatusEnum.CARCAN_OUT_CAR_TEMP.getCode(), value);
									} else {
										reportCanRecrMap.put(CarCanStatusEnum.CARCAN_OUT_CAR_TEMP.getCode(),
												OTARealTimeConstants.ILLEGAL_NUM);
									}
									break;
								case 3: // 净水百分比
									if (!(OTARealTimeConstants.INVALID_BYTE_STRING == value)) {
										reportCanRecrMap.put(CarCanStatusEnum.CARCAN_CLEAN_WATER.getCode(), value);
									} else {
										reportCanRecrMap.put(CarCanStatusEnum.CARCAN_CLEAN_WATER.getCode(),
												OTARealTimeConstants.ILLEGAL_NUM);
									}
									break;
								case 4: // 灰水百分比
									if (!(OTARealTimeConstants.INVALID_BYTE_STRING == value)) {
										reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DIRTY_WATER.getCode(), value);
									} else {
										reportCanRecrMap.put(CarCanStatusEnum.CARCAN_DIRTY_WATER.getCode(),
												OTARealTimeConstants.ILLEGAL_NUM);
									}
									break;
								case 5: // 生活区剩余电量
									if (!(OTARealTimeConstants.INVALID_BYTE_STRING == value)) {
										reportCanRecrMap.put(CarCanStatusEnum.CARCAN_LIFE_POWER.getCode(), value);
									} else {
										reportCanRecrMap.put(CarCanStatusEnum.CARCAN_LIFE_POWER.getCode(),
												OTARealTimeConstants.ILLEGAL_NUM);
									}
									break;
								default:
									break;
								}
							}
						}
					} else {
						logger.debug("TBox({})车况数据发送rvm做数据封装，车况扩展字段map集合为空", serialNum);
					}
				} else {
					logger.warn("TBox({})车况数据发送rvm做数据封装，车况扩展类实例化异常，不能从父类实例化子类或为null", serialNum);
				}

			}
			CodeValue[] reportCanRecr = new CodeValue[1];
			List<Map> reportCanRecrList = new ArrayList<>();
			reportCanRecrList.add(reportCanRecrMap);
			reportCanRecr[0] = new CodeValue(CarCanStatusEnum.CARCAN_DATA.getCode(), reportCanRecrList);
			// 调用发送kafka消息函数
			/*
			 * kafkaService.transferOTAData(serialNum,
			 * KafkaOtaDataCommand.KAFKA_PARAM_STATUS, DateUtil.stringToLong(collectiontime,
			 * "yyyy-MM-dd HH:mm:ss"), reportCanRecr, gatewayTimeIn);
			 */
			
			logger.debug("Mock kafka 发送消息");
		} catch (Exception e) {
			logger.error("解析车况数据出错，原因:{}", ThrowableUtil.getErrorInfoFromThrowable(e));
		}
	}
}
