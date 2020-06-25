/**        
 * ReportCan.java Create on 2017年7月18日      
 * Copyright (c) 2017年7月18日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.platform.service.model.vo;

import java.io.Serializable;
import java.text.NumberFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.maxus.tsp.common.constant.OTARealTimeConstants;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;

/**
 * @ClassName: ReportCan.java
 * @Description:  
 * @author 任怀宇
 * @version V1.0
 * @Date 2017年7月18日 下午1:42:05
 */
public class ReportCan implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 488866069431877555L;

	public static final String VERSION_23 = "0017";
	public static final String VERSION_20 = "0014";
	public static final String VERSION_200_18 = "C812";//蔚来版本200.18

	public static int bytesLength = 40; // 单条CAN数据的长度
	public static final String INVALID_VALUE = "-255";
	private int strgAglListOffset = 1080;
	private static Logger logger = LogManager.getLogger(ReportCan.class);
	private boolean ReportCanDataCorrect = false;
	private String collectDateTime = "";
	private String averageFuelConsumption = ""; // 平均油耗
	private String remainingDistance = ""; // 剩余里程
	private String frontLeftTirePressure = ""; // 左前胎压
	private String frontRightTirePressure = ""; // 右前胎压
	private String rearLeftTirePressure = ""; // 左后胎压
	private String rearRightTirePressure = ""; // 右后胎压
	private String fuelLevel = ""; // 油量（百分比）
	private String inCarTemp = ""; // 车内温度
	private String vehicleSpeed = ""; // 车速
	private String engineSpeed = ""; // 发动机速度
	private String odo = ""; // 总里程
	private String systemPowerMode = ""; // 系统电源模式
	private String doorStatus = ""; // 车门状态
	private String fuelTankLevel = ""; // 剩余油量（升）

	// 0.20新增车况状态
	private String powerBattStatus = "";// 动力电池状态
	private String soc = "";// 动力电池剩余电量
	private String powerBattTemp = "";// 动力电池温度

	// 0.23新增车况跛行
	private String speedLimitCmd = "";// 限速指令状态
	private String speedLimitActive = "";// 限速执行状态

	// 200.18蔚来版本车况
	private String strgAglCnt = "";// 方向盘转角数据的个数
	private String[] strgAglList;// 方向盘转角数据的列表
	private String acclPosCnt = "";// 加速踏板位置数据的个数
	private String[] acclPosList;// 加速踏板位置数据的列表

	public ReportCan() {
		this.ReportCanDataCorrect = false;
	}

	public ReportCan(byte[] input, String otaVersion) {
		ReportCanDataCorrect = false;
		// 构造函数报文长度与版本的初步检查
		// 基础车况为40字节
		if (input.length >= OTARealTimeConstants.REPORT_CARCAN_LENGTH) {
				// 正常解析40个字节的车况
				byte[] _CollectDataTime = new byte[7]; // 收集时间
				byte[] _AverageFuelConsumption = new byte[2]; // 平均油耗
				byte[] _RemainingDistance = new byte[4]; // 续航里程
				byte[] _FrontLeftTirePressure = new byte[2]; // 左前胎压
				byte[] _FrontRightTirePressure = new byte[2]; // 右前胎压
				byte[] _RearLeftTirePressure = new byte[2]; // 左后胎压
				byte[] _RearRightTirePressure = new byte[2]; // 右后胎压
				byte[] _FuelLevel = new byte[2]; // 油量（百分比）
				byte[] _InCarTemp = new byte[2]; // 车内温度
				byte[] _VehicleSpeed = new byte[2]; // 车速
				byte[] _EngineSpeed = new byte[2]; // 发动机速度
				byte[] _ODO = new byte[4]; // 总里程
				byte[] _SystemPowerMode = new byte[1]; // 系统电源模式
				byte[] _DoorStatus = new byte[4]; // 车门状态
				byte[] _FuelTankLevel = new byte[2]; // 剩余油量（升）

				try {
					System.arraycopy(input, 0, _CollectDataTime, 0, 7);
					collectDateTime = ByteUtil.bytesToDataTime(_CollectDataTime);

					System.arraycopy(input, 7, _AverageFuelConsumption, 0, 2);
					if (ByteUtil.byteToHex(_AverageFuelConsumption).equals("FFFF"))
						averageFuelConsumption = INVALID_VALUE;
					else
						averageFuelConsumption = String.valueOf(ByteUtil.getUnsignedInt(_AverageFuelConsumption) / 10f);

					System.arraycopy(input, 9, _RemainingDistance, 0, 4);
					if (ByteUtil.byteToHex(_RemainingDistance).equals("FFFFFFFF"))
						remainingDistance = INVALID_VALUE;
					else
						remainingDistance = String.valueOf(ByteUtil.getUnsignedLong(_RemainingDistance) / 10f);

					System.arraycopy(input, 13, _FrontLeftTirePressure, 0, 2);
					if (ByteUtil.byteToHex(_FrontLeftTirePressure).equals("FFFF"))
						frontLeftTirePressure = INVALID_VALUE;
					else
						frontLeftTirePressure = String
								.valueOf(ByteUtil.getUnsignedInt(_FrontLeftTirePressure) / 10.00f);

					System.arraycopy(input, 15, _FrontRightTirePressure, 0, 2);
					if (ByteUtil.byteToHex(_FrontRightTirePressure).equals("FFFF"))
						frontRightTirePressure = INVALID_VALUE;
					else
						frontRightTirePressure = String
								.valueOf(ByteUtil.getUnsignedInt(_FrontRightTirePressure) / 10.00f);

					System.arraycopy(input, 17, _RearLeftTirePressure, 0, 2);
					if (ByteUtil.byteToHex(_RearLeftTirePressure).equals("FFFF"))
						rearLeftTirePressure = INVALID_VALUE;
					else
						rearLeftTirePressure = String.valueOf(ByteUtil.getUnsignedInt(_RearLeftTirePressure) / 10.00f);

					System.arraycopy(input, 19, _RearRightTirePressure, 0, 2);
					if (ByteUtil.byteToHex(_RearRightTirePressure).equals("FFFF"))
						rearRightTirePressure = INVALID_VALUE;
					else
						rearRightTirePressure = String
								.valueOf(ByteUtil.getUnsignedInt(_RearRightTirePressure) / 10.00f);

					System.arraycopy(input, 21, _FuelLevel, 0, 2);
					if (ByteUtil.byteToHex(_FuelLevel).equals("FFFF"))
						fuelLevel = INVALID_VALUE;
					else
						fuelLevel = String.valueOf(ByteUtil.getUnsignedInt(_FuelLevel) / 10f);

					System.arraycopy(input, 23, _InCarTemp, 0, 2);
					if (ByteUtil.byteToHex(_InCarTemp).equals("FFFF"))
						inCarTemp = INVALID_VALUE;
					else
						inCarTemp = String.valueOf(ByteUtil.getUnsignedInt(_InCarTemp) / 10f - 40);

					System.arraycopy(input, 25, _VehicleSpeed, 0, 2);
					if (ByteUtil.byteToHex(_VehicleSpeed).equals("FFFF"))
						vehicleSpeed = INVALID_VALUE;
					else
						vehicleSpeed = String.valueOf(ByteUtil.getUnsignedInt(_VehicleSpeed) / 10f);

					System.arraycopy(input, 27, _EngineSpeed, 0, 2);
					if (ByteUtil.byteToHex(_EngineSpeed).equals("FFFF"))
						engineSpeed = INVALID_VALUE;
					else
						engineSpeed = String.valueOf(ByteUtil.getUnsignedInt(_EngineSpeed) / 4f);

					System.arraycopy(input, 29, _ODO, 0, 4);
					if (ByteUtil.byteToHex(_ODO).equals("FFFFFFFF")){
						odo = INVALID_VALUE;
					}else{
						NumberFormat numberFormat = NumberFormat.getInstance();
						numberFormat.setGroupingUsed(false);
						double temp = ByteUtil.getUnsignedLong(_ODO) * 0.1;
						odo = numberFormat.format(temp);
					}

					System.arraycopy(input, 33, _SystemPowerMode, 0, 1);
					if (ByteUtil.byteToHex(_SystemPowerMode).equals("FF"))
						systemPowerMode = INVALID_VALUE;
					else
						systemPowerMode = String.valueOf(_SystemPowerMode[0]);

					System.arraycopy(input, 34, _DoorStatus, 0, 4);
					// if (ByteUtil.byteToHex(_DoorStatus).equals("FFFFFFFF"))
					_DoorStatus[0] = 0x00;
					_DoorStatus[1] = (byte) (_DoorStatus[1] & 0x03);
					doorStatus = String.valueOf(ByteUtil.getUnsignedLong(_DoorStatus));

					System.arraycopy(input, 38, _FuelTankLevel, 0, 2);
					if (ByteUtil.byteToHex(_FuelTankLevel).equals("FFFF"))
						fuelTankLevel = INVALID_VALUE;
					else
						fuelTankLevel = String.valueOf(ByteUtil.getUnsignedInt(_FuelTankLevel) / 10f);

					this.ReportCanDataCorrect = true;
				} catch (Exception ex) {
					this.ReportCanDataCorrect = false;
					logger.error("解析{}车况发生异常，40字节车况错误，异常原因:{}", otaVersion, ThrowableUtil.getErrorInfoFromThrowable(ex));
				}

			// 首先检查蔚来版本
			if (ReportCanDataCorrect && otaVersion.equals(VERSION_200_18)) {
				// System.out.println("开始解析蔚来的车况报文！");
				byte[] _strgAglCnt = new byte[1]; // 方向盘转角数据的个数
				byte[] _strgAglList; // 方向盘转角数据的列表
				byte[] _acclPosCnt = new byte[1]; // 加速踏板位置数据的个数
				byte[] _acclPosList; // 加速踏板位置数据的列表
				int inputLength = 0;
				int strAglCntInt = 0;
				int acclPosCntInt = 0;
				try {
					System.arraycopy(input, 40, _strgAglCnt, 0, 1);
					strgAglCnt = ByteUtil.getUnsignedChar(_strgAglCnt[0]);
					strAglCntInt = Integer.parseUnsignedInt(strgAglCnt);

					System.arraycopy(input, 41 + strAglCntInt * 2, _acclPosCnt, 0, 1);
					acclPosCnt = ByteUtil.getUnsignedChar(_acclPosCnt[0]);
					acclPosCntInt = Integer.parseUnsignedInt(acclPosCnt);

					inputLength = OTARealTimeConstants.REPORT_CARCAN_LENGTH + strAglCntInt* 2 + acclPosCntInt + 2;

					if (inputLength == input.length) {
						int index = 0;
						// 方向盘转角数据的个数
						strgAglList = new String[strAglCntInt];
						_strgAglList = new byte[strAglCntInt * 2];
						byte[] _strgAgl = new byte[2];
						System.arraycopy(input, 41, _strgAglList, 0, strAglCntInt * 2);
						while (index < strAglCntInt * 2) {
							_strgAgl[0] = _strgAglList[index++];
							_strgAgl[1] = _strgAglList[index++];
							if (ByteUtil.byteToHex(_strgAgl).equals("FFFF"))
								strgAglList[(index - 2) / 2] = null;
							else
								strgAglList[(index - 2) / 2] = String.valueOf(ByteUtil.getUnsignedInt(_strgAgl) / 10f - strgAglListOffset);
						}
						index = 0;
						// 加速踏板位置数据的个数
						acclPosList = new String[acclPosCntInt];
						_acclPosList = new byte[acclPosCntInt];
						byte[] _acclPos = new byte[1];
						System.arraycopy(input, 42 + strAglCntInt * 2, _acclPosList, 0, acclPosCntInt);
						while (index < acclPosCntInt) {
							_acclPos[0] = _acclPosList[index];
							if (ByteUtil.byteToHex(_acclPos).equals("FF"))
								acclPosList[index] = null;
							else
								acclPosList[index] = String.valueOf(ByteUtil.getUnsignedChar(_acclPos[0]));
							index++;
						}

						this.ReportCanDataCorrect = true;
					} else {
						this.ReportCanDataCorrect = false;
						logger.warn("当前解析车况报文长度错误!");
					}
				} catch (Exception ex) {
					logger.error("当前{}版本车况解析出错，原因为:{}", otaVersion,
							ThrowableUtil.getErrorInfoFromThrowable(ex));
					ReportCanDataCorrect = false;
				}

			} else {
				// 0.20
				if (ReportCanDataCorrect && otaVersion.compareTo(VERSION_20) >= 0) {
					// System.out.println("开始解析0.20-0.23之间的车况报文！");
					byte[] _powerBattStatus = new byte[1];// 动力电池状态
					byte[] _soc = new byte[1];// 动力电池剩余电量
					byte[] _powerBattTemp = new byte[1];// 动力电池温度
					try {
						System.arraycopy(input, 40, _powerBattStatus, 0, 1);
						if (ByteUtil.byteToHex(_powerBattStatus).equals("FF")) {
							powerBattStatus = INVALID_VALUE;
						} else {
							powerBattStatus = ByteUtil.getUnsignedChar(_powerBattStatus[0]);
						}

						System.arraycopy(input, 41, _soc, 0, 1);
						if (ByteUtil.byteToHex(_soc).equals("FF")) {
							logger.debug("soc:{}", ByteUtil.byteToHex(_soc));
							soc = INVALID_VALUE;
						} else {
							soc = String.valueOf(Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_soc[0])));
						}

						System.arraycopy(input, 42, _powerBattTemp, 0, 1);
						if (ByteUtil.byteToHex(_powerBattTemp).equals("FF")) {
							powerBattTemp = INVALID_VALUE;
						} else {
							powerBattTemp = String.valueOf(
									Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_powerBattTemp[0])) - 40);
						}

						this.ReportCanDataCorrect = true;
					} catch (Exception ex) {
						logger.error("解析{}版本车况报文出错，原因:{}", otaVersion, ThrowableUtil.getErrorInfoFromThrowable(ex));
						this.ReportCanDataCorrect = false;
					}
				}
				// 0.23 0.20解析成功才继续解析
				if (ReportCanDataCorrect && otaVersion.compareTo(VERSION_23) >= 0) {
					// System.out.println("开始解析0.23之后的车况报文！");
					byte[] _speedLimitCmd = new byte[1];// 限速指令
					byte[] _speedLimitActive = new byte[1];// 限速执行状态
					try {
						System.arraycopy(input, 43, _speedLimitCmd, 0, 1);
						if (ByteUtil.byteToHex(_speedLimitCmd).equals("FF")) {
							speedLimitCmd = INVALID_VALUE;
						} else {
							switch (_speedLimitCmd[0]) {
							case 0:
								speedLimitCmd = "0";
								break;
							case 1:
								speedLimitCmd = "1";
								break;
							default:
								speedLimitCmd = "-255";
								break;
							}
						}
						System.arraycopy(input, 44, _speedLimitActive, 0, 1);
						if (ByteUtil.byteToHex(_speedLimitActive).equals("FF")) {
							speedLimitActive = INVALID_VALUE;
						} else {
							switch (_speedLimitActive[0]) {
							case 0:
								speedLimitActive = "0";
								break;
							case 1:
								speedLimitActive = "1";
								break;
							default:
								speedLimitActive = "-255";
								break;
							}
						}
						this.ReportCanDataCorrect = true;
					} catch (Exception ex) {
						logger.error("解析{}版本之后车况报文出错，原因:{}", otaVersion, ThrowableUtil.getErrorInfoFromThrowable(ex));
						this.ReportCanDataCorrect = false;
					}
				}
			}
		} else {
			this.ReportCanDataCorrect = false;
			logger.warn("解析车况出错，原因为：报文长度错误");
		}

	}

	public boolean isReportCanDataCorrect() {
		return ReportCanDataCorrect;
	}

	public void setCollectDataTime(String inputCollectDateTime) {
		collectDateTime = inputCollectDateTime;
	}

	public String getCollectDataTime() {
		return collectDateTime;
	}

	public void setAverageFuelConsumption(String inputAverageFuelConsumption) {
		averageFuelConsumption = inputAverageFuelConsumption;
	}

	public String getAverageFuelConsumption() {
		return averageFuelConsumption;
	}

	public void setRemainingDistance(String inputRemainingDistance) {
		remainingDistance = inputRemainingDistance;
	}

	public String getRemainingDistance() {
		return remainingDistance;
	}

	public void setFrontLeftTirePressure(String inputFrontLeftTirePressure) {
		frontLeftTirePressure = inputFrontLeftTirePressure;
	}

	public String getFrontLeftTirePressure() {
		return frontLeftTirePressure;
	}

	public void setFrontRightTirePressure(String inputFrontRightTirePressure) {
		frontRightTirePressure = inputFrontRightTirePressure;
	}

	public String getFrontRightTirePressure() {
		return frontRightTirePressure;
	}

	public void setRearLeftTirePressure(String inputRearLeftTirePressure) {
		rearLeftTirePressure = inputRearLeftTirePressure;
	}

	public String getRearLeftTirePressure() {
		return rearLeftTirePressure;
	}

	public void setRearRightTirePressure(String inputRearRightTirePressure) {
		rearRightTirePressure = inputRearRightTirePressure;
	}

	public String getRearRightTirePressure() {
		return rearRightTirePressure;
	}

	public void setFuelLevel(String inputFuelLevel) {
		fuelLevel = inputFuelLevel;
	}

	public String getFuelLevel() {
		return fuelLevel;
	}

	public void setInCarTemp(String inputInCarTemp) {
		inCarTemp = inputInCarTemp;
	}

	public String getInCarTemp() {
		return inCarTemp;
	}

	public void setVehicleSpeed(String inputVehicleSpeed) {
		vehicleSpeed = inputVehicleSpeed;
	}

	public String getVehicleSpeed() {
		return vehicleSpeed;
	}

	public void setEngineSpeed(String inputEngineSpeed) {
		engineSpeed = inputEngineSpeed;
	}

	public String getEngineSpeed() {
		return engineSpeed;
	}

	public void setOdo(String inputODO) {
		odo = inputODO;
	}

	public String getOdo() {
		return odo;
	}

	public void setSystemPowerMode(String inputSystemPowerMode) {
		systemPowerMode = inputSystemPowerMode;
	}

	public String getSystemPowerMode() {
		return systemPowerMode;
	}

	public void setDoorStatus(String inputDoorStatus) {
		doorStatus = inputDoorStatus;
	}

	public String getDoorStatus() {
		return doorStatus;
	}

	public void setFuelTankLevel(String inputFuelTankLevel) {
		fuelTankLevel = inputFuelTankLevel;
	}

	public String getFuelTankLevel() {
		return fuelTankLevel;
	}

	public String getPowerBattStatus() {
		return powerBattStatus !=null?powerBattStatus:" ";
	}

	public void setPowerBattStatus(String powerBattStatus) {
		this.powerBattStatus = powerBattStatus;
	}

	public String getSoc() {
		return soc != null ? soc:" ";
	}

	public void setSoc(String soc) {
		this.soc = soc;
	}

	public String getPowerBattTemp() {
		return powerBattTemp != null ? powerBattTemp:" ";
	}

	public void setPowerBattTemp(String powerBattTemp) {
		this.powerBattTemp = powerBattTemp;
	}

	public String getSpeedLimitCmd() {
		return speedLimitCmd;
	}

	public void setSpeedLimitCmd(String speedLimitCmd) {
		this.speedLimitCmd = speedLimitCmd;
	}

	public String getSpeedLimitActive() {
		return speedLimitActive;
	}

	public void setSpeedLimitActive(String speedLimitActive) {
		this.speedLimitActive = speedLimitActive;
	}

	public String getStrgAglCnt() {
		return strgAglCnt;
	}

	public void setStrgAglCnt(String strgAglCnt) {
		this.strgAglCnt = strgAglCnt;
	}

	public String[] getStrgAglList() {
		return strgAglList;
	}

	public void setStrgAglList(String[] strgAglList) {
		this.strgAglList = strgAglList;
	}

	public String getAcclPosCnt() {
		return acclPosCnt;
	}

	public void setAcclPosCnt(String acclPosCnt) {
		this.acclPosCnt = acclPosCnt;
	}

	public String[] getAcclPosList() {
		return acclPosList;
	}

	public void setAcclPosList(String[] acclPosList) {
		this.acclPosList = acclPosList;
	}

	// public static void main(String[] args) {
	// byte[] test = new byte[2];
	// test[0]=1;
	// test[1]=(byte)4;
	// test[1]=(byte)(test[1]&0x03);
	// System.out.println(ByteUtil.byteToHex(test));
	// }
}
