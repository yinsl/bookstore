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
import com.maxus.tsp.common.enums.MotorStatusEnum;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;

/**
 * RealTimeDataMotor数据（CMD_UP_REALTIME_DATA_MOTOR）
 * 
 * @author lzgea
 *
 */
public class RealTimeDataMotor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7066397723535722560L;
	private static Logger logger = LogManager.getLogger(RealTimeDataMotor.class);
	@JSONField(serialize = false)
	public int byteDatalength = 12;// 字节长度为12
	@JSONField(serialize = false)
	private byte[] oriMotorInfoBytes = new byte[byteDatalength];
	@JSONField(serialize = false)
	private boolean dataCorrect = true;
	private float floatNum = 0.1f;
	private int motorControllerOffSetTemp = 40;//驱动电机控制器温度偏移量
	private int rpmOffSet = 20000;//转速偏移量
	private int motorTorqueOffSet = 2000;//转矩偏移量
	private int motorTempOffSet = 40;//电机温度偏移量
	private int controllerDCBusCurrentOffSet = 1000;//电机控制器直流母线电流偏移量

	private int motorId;//驱动电机序号
	private int motorStatus;//驱动电机状态
	private int motorControllerTemp;//驱动电机控制器温度
	private int rpm;//驱动电机转速
	private float motorTorque;//驱动电机转矩
	private int motorTemp;//驱动电机温度
	private float controllerInVolt;//电机控制器输入电压
	private float controllerDCBusCurrent;//电机控制器直流母线电流
	@JSONField(serialize = false)
	private List<CodeValue> realTimeDataMotorList;
	@JSONField(serialize = false)
	public String getRealTimeDataMotorJSONInfo() {
	//	if (isAnalysisOk()) {
			return JSONObject.toJSONString(realTimeDataMotorList, SerializerFeature.WriteMapNullValue);
	//	}
	//	return OTARealTimeConstants.INVALID_JSNINFO;
	}
	
	public RealTimeDataMotor() {
		dataCorrect = false;
	}

	
	public RealTimeDataMotor(byte[] datagramBytes, String tboxsn) {
		if (datagramBytes.length == byteDatalength) {
			oriMotorInfoBytes = datagramBytes;
			byte[] _motorId = new byte[2];
			byte[] _motorStatus = new byte[2];
			byte[] _motorControllerTemp = new byte[1];
			byte[] _rpm = new byte[2];
			byte[] _motorTorque = new byte[2];
			byte[] _motorTemp = new byte[1];
			byte[] _controllerInVolt = new byte[2];
			byte[] _controllerDCBusCurrent = new byte[2];
			DecimalFormat df1 = new DecimalFormat("#.0");
			realTimeDataMotorList = new ArrayList<>();

			try {
				//解析motorId
				System.arraycopy(oriMotorInfoBytes, 0, _motorId, 1, OTARealTimeConstants.UINT8_OFFSET);
				motorId = ByteUtil.getUnsignedInt(_motorId);
				if (motorId == OTARealTimeConstants.INVALID_ZERO_VALUE) {
					//0无效值
					logger.debug("TBox(SN:{}):国标驱动电机数据中驱动电机序号为 0", tboxsn);
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_ID, OTARealTimeConstants.INVALID_BYTE_STRING));
				} else {
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_ID, motorId));
				}

				//解析MotorStatus
				System.arraycopy(oriMotorInfoBytes, 1, _motorStatus, 1, OTARealTimeConstants.UINT8_OFFSET);
				motorStatus = ByteUtil.getUnsignedInt(_motorStatus);
				if (_motorStatus[1] == OTARealTimeConstants.INVALID_BYTE_VALUE) {
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_STATUS,
							MotorStatusEnum.INVALID.getValue()));
				} else {
					switch (motorStatus) {
						case OTARealTimeConstants.MOTOR_STATUS_CONSUMING_POWER:
							realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_STATUS,
									MotorStatusEnum.CONSUMING_POWER.getValue()));
							break;
						case OTARealTimeConstants.MOTOR_STATUS_GENERATING_POWER:
							realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_STATUS,
									MotorStatusEnum.GENERATING_POWER.getValue()));
							break;
						case OTARealTimeConstants.MOTOR_STATUS_CLOSED:
							realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_STATUS,
									MotorStatusEnum.CLOSED.getValue()));
							break;
						case OTARealTimeConstants.MOTOR_STATUS_PREPARING:
							realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_STATUS,
									MotorStatusEnum.PREPARING.getValue()));
							break;
						case OTARealTimeConstants.INVALID_BYTE_ERROR:
							logger.debug("TBox(SN:{}):国标驱动电机数据中驱动电机状态为{}", tboxsn, MotorStatusEnum.ERROR.getValue());
							realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_STATUS,
									MotorStatusEnum.ERROR.getValue()));
							break;
						default:
							//取值范围外的值，原值上传
							logger.debug("TBox(SN:{}):国标驱动电机数据中驱动电机状态为{}", tboxsn, motorStatus);
							realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_STATUS,
									motorStatus));
							break;
					}
				}

				//解析motorControllerTemp
				System.arraycopy(oriMotorInfoBytes, 2, _motorControllerTemp, 0, OTARealTimeConstants.UINT8_OFFSET);
				logger.debug("TBox(SN:{}):国标驱动电机数据中驱动电机控制器温度为{}", tboxsn, ByteUtil.byteToHex(_motorControllerTemp[0]));
				;
				if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_motorControllerTemp[0])) == OTARealTimeConstants.INVALID_BYTE_STRING) {
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_CONTROLLER_TEMP, OTARealTimeConstants.INVALID_BYTE_STRING));
				} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_motorControllerTemp[0])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_CONTROLLER_TEMP, OTARealTimeConstants.INVALID_BYTE_ERROR));
				} else {
					motorControllerTemp = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_motorControllerTemp[0])) - motorControllerOffSetTemp;
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_CONTROLLER_TEMP,
							motorControllerTemp));
				}

				//解析rpm
				System.arraycopy(oriMotorInfoBytes, 3, _rpm, 0, OTARealTimeConstants.UINT16_OFFSET);
				logger.debug("TBox(SN:{}):国标驱动电机数据中驱动电机转速为{}", tboxsn, ByteUtil.getUnsignedInt(_rpm));
				if (ByteUtil.getUnsignedInt(_rpm) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.RPM, OTARealTimeConstants.INVALID_UINT16_STRING));
				} else if (ByteUtil.getUnsignedInt(_rpm) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.RPM, OTARealTimeConstants.INVALID_UINT16_ERROR));
				} else {
					rpm = ByteUtil.getUnsignedInt(_rpm) - rpmOffSet;
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.RPM, rpm));
				}

				//解析motorTorque
				System.arraycopy(oriMotorInfoBytes, 5, _motorTorque, 0, OTARealTimeConstants.UINT16_OFFSET);
				logger.debug("TBox(SN:{}):国标驱动电机数据中驱动电机转矩为{}", tboxsn, ByteUtil.getUnsignedInt(_motorTorque));
				if (ByteUtil.getUnsignedInt(_motorTorque) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_TORQUE, OTARealTimeConstants.INVALID_UINT16_STRING));
				} else if (ByteUtil.getUnsignedInt(_motorTorque) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_TORQUE, OTARealTimeConstants.INVALID_UINT16_ERROR));
				} else {
					motorTorque = Float
							.parseFloat(df1.format(ByteUtil.getUnsignedInt(_motorTorque) * floatNum - motorTorqueOffSet));
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_TORQUE, motorTorque));
				}

				//解析motorTemp
				System.arraycopy(oriMotorInfoBytes, 7, _motorTemp, 0, OTARealTimeConstants.UINT8_OFFSET);
				logger.debug("TBox(SN:{}):国标驱动电机数据中驱动电机温度为{}", tboxsn, ByteUtil.byteToHex(_motorTemp[0]));
				if (_motorTemp[0] == OTARealTimeConstants.INVALID_BYTE_VALUE) {
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_TEMP, OTARealTimeConstants.INVALID_BYTE_STRING));
				} else if (Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_motorTemp[0])) == OTARealTimeConstants.INVALID_BYTE_ERROR) {
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_TEMP, OTARealTimeConstants.INVALID_BYTE_ERROR));
				} else {
					motorTemp = Integer.parseUnsignedInt(ByteUtil.getUnsignedChar(_motorTemp[0])) - motorTempOffSet;
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.MOTOR_TEMP, motorTemp));
				}

				//解析controllerInVolt
				System.arraycopy(oriMotorInfoBytes, 8, _controllerInVolt, 0, OTARealTimeConstants.UINT16_OFFSET);
				logger.debug("TBox(SN:{}):国标驱动电机数据中电机控制器输入电压为{}", tboxsn, ByteUtil.getUnsignedInt(_controllerInVolt));
				if (ByteUtil.getUnsignedInt(_controllerInVolt) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.CONTROLLER_IN_VOLT, OTARealTimeConstants.INVALID_UINT16_STRING));
				} else if (ByteUtil.getUnsignedInt(_controllerInVolt) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.CONTROLLER_IN_VOLT, OTARealTimeConstants.INVALID_UINT16_ERROR));
				} else {
					controllerInVolt = Float
							.parseFloat(df1.format(ByteUtil.getUnsignedInt(_controllerInVolt) * floatNum));
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.CONTROLLER_IN_VOLT,
							controllerInVolt));
				}

				//解析controllerDCBusCurrent
				System.arraycopy(oriMotorInfoBytes, 10, _controllerDCBusCurrent, 0, OTARealTimeConstants.UINT16_OFFSET);
				logger.debug("TBox(SN:{}):国标驱动电机数据中电机控制器直流母线电流为{}", tboxsn, ByteUtil.getUnsignedInt(_controllerDCBusCurrent));				;
				if (ByteUtil.getUnsignedInt(_controllerDCBusCurrent) == OTARealTimeConstants.INVALID_UINT16_VALUE) {
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.CONTROLLER_DC_BUSCURRENT, OTARealTimeConstants.INVALID_UINT16_STRING));
				} else if (ByteUtil.getUnsignedInt(_controllerDCBusCurrent) == OTARealTimeConstants.INVALID_UINT16_ERROR) {
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.CONTROLLER_DC_BUSCURRENT, OTARealTimeConstants.INVALID_UINT16_ERROR));
				} else {
					controllerDCBusCurrent = Float.parseFloat(df1.format(
							ByteUtil.getUnsignedInt(_controllerDCBusCurrent) * floatNum - controllerDCBusCurrentOffSet));
					realTimeDataMotorList.add(new CodeValue(OTARealTimeConstants.CONTROLLER_DC_BUSCURRENT,
							controllerDCBusCurrent));
				}
			} catch (Exception e) {
				logger.error("TBox(SN:{}):RealTimeDataMotor 解析报文出错，原因:{}", tboxsn,
						ThrowableUtil.getErrorInfoFromThrowable(e));
				dataCorrect = false;
			}
		} else {
			dataCorrect = false;
			logger.warn("TBox(SN:{}):RealTimeDataMotor 报文长度错误", tboxsn);
		}

	}

	public int getMotorId() {
		return motorId;
	}

	public void setMotorId(int motorId) {
		this.motorId = motorId;
	}

	public int getMotorStatus() {
		return motorStatus;
	}

	public void setMotorStatus(int motorStatus) {
		this.motorStatus = motorStatus;
	}

	public int getMotorControllerTemp() {
		return motorControllerTemp;
	}

	public void setMotorControllerTemp(int motorControllerTemp) {
		this.motorControllerTemp = motorControllerTemp;
	}

	public int getRpm() {
		return rpm;
	}

	public void setRpm(int rpm) {
		this.rpm = rpm;
	}

	public float getMotorTorque() {
		return motorTorque;
	}

	public void setMotorTorque(float motorTorque) {
		this.motorTorque = motorTorque;
	}

	public int getMotorTemp() {
		return motorTemp;
	}

	public void setMotorTemp(int motorTemp) {
		this.motorTemp = motorTemp;
	}

	public float getControllerInVolt() {
		return controllerInVolt;
	}

	public void setControllerInVolt(float controllerInVolt) {
		this.controllerInVolt = controllerInVolt;
	}

	public float getControllerDCBusCurrent() {
		return controllerDCBusCurrent;
	}

	public void setControllerDCBusCurrent(float controllerDCBusCurrent) {
		this.controllerDCBusCurrent = controllerDCBusCurrent;
	}
	@JSONField(serialize = false)
	public boolean isAnalysisOk() {
		return dataCorrect;
	}

	// public String getInfo () {
	//
	// String info= "motorId: "+ motorId +"\n"+
	// "motorStatus: "+ motorStatus +"\n"+
	// "motorControllerTemp: "+ motorControllerTemp +"\n"+
	// "rpm: "+ rpm +"\n"+
	// "motorTorque: "+ motorTorque +"\n"+
	// "motorTemp: "+ motorTemp +"\n"+
	// "controllerInVolt: "+ controllerInVolt +"\n"+
	// "controllerDCBusCurrent: "+ controllerDCBusCurrent +"\n";
	// return info;
	// }
}
