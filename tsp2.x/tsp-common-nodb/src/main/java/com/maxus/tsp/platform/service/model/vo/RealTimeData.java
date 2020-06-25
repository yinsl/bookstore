package com.maxus.tsp.platform.service.model.vo;

public class RealTimeData {
	//数据类型. 实时为true,补发为false
	private final boolean isRealTime;
	//收集时间
	private final String collectDateTime;
	//整车数据
	private final RealTimeDataVehicle dataVehicle;
	//驱动电机数据
	private final RealTimeDataMotor[] dataMotor;
	//燃料电池数据
	private final RealTimeDataFuelCell dataFuelCell;
	//发动机数据
	private final RealTimeDataEngine dataEngine;
	//车辆位置数据
	private final ReportPos dataPos;
	//极值数据
	private final RealTimeDataExtermum dataExtermum;
	//报警数据
	private final RealTimeDataAlarm dataAlarm;
	//可充电储能装置电压数据
	private final RealTimeDataVoltage[] dataVoltage;
	//可充电储能装置温度数据
	private final RealTimeDataTemp[] dataTemp;
	
	//静态成员构建类
	public static class Builder{
		// requireed parameter
		private final boolean isRealTime;
		private final String collectDateTime;
		
		// optional parameter - initialied to default values;
		//整车数据
		private RealTimeDataVehicle dataVehicle = null;
		//驱动电机数据
		private RealTimeDataMotor[] dataMotor = null;
		//燃料电池数据
		private RealTimeDataFuelCell dataFuelCell = null;
		//发动机数据
		private RealTimeDataEngine dataEngine = null;
		//车辆位置数据
		private ReportPos dataPos = null;
		//极值数据
		private RealTimeDataExtermum dataExtermum = null;
		//报警数据
		private RealTimeDataAlarm dataAlarm = null;
		//可充电储能装置电压数据
		private RealTimeDataVoltage[] dataVoltage = null;
		//可充电储能装置温度数据
		private RealTimeDataTemp[] dataTemp = null;
		
		
		public Builder(boolean isRealTime, String collectDateTime) {
			this.isRealTime = isRealTime;
			this.collectDateTime = collectDateTime;
		}
	
		public Builder dataVehicle(RealTimeDataVehicle dataVehicle)
		{
			this.dataVehicle = dataVehicle;
			return this;
		}
		
		public Builder dataMotor(RealTimeDataMotor[] dataMotor)
		{
			this.dataMotor = dataMotor;
			return this;
		}
		
		public Builder dataFuelCell(RealTimeDataFuelCell dataFuelCell)
		{
			this.dataFuelCell = dataFuelCell;
			return this;
		}
		
		public Builder dataEngine(RealTimeDataEngine dataEngine)
		{
			this.dataEngine = dataEngine;
			return this;
		}
		
		public Builder dataPos(ReportPos dataPos)
		{
			this.dataPos = dataPos;
			return this;
		}
		
		public Builder dataExtermum(RealTimeDataExtermum dataExtermum)
		{
			this.dataExtermum = dataExtermum;
			return this;
		}
		
		public Builder dataAlarm(RealTimeDataAlarm dataAlarm)
		{
			this.dataAlarm = dataAlarm;
			return this;
		}
		
		public Builder dataVoltage(RealTimeDataVoltage[] dataVoltage)
		{
			this.dataVoltage = dataVoltage;
			return this;
		}
		
		public Builder dataTemp(RealTimeDataTemp[] dataTemp)
		{
			this.dataTemp = dataTemp;
			return this;
		}
		
		public RealTimeData build() {
			return new RealTimeData(this);
		}
	}
	
	private RealTimeData(Builder builder)
	{
		isRealTime = builder.isRealTime;
		collectDateTime = builder.collectDateTime;
		dataVehicle = builder.dataVehicle;
		dataMotor = builder.dataMotor;
		dataFuelCell = builder.dataFuelCell;
		dataEngine = builder.dataEngine;
		dataPos = builder.dataPos;
		dataExtermum = builder.dataExtermum;
		dataAlarm = builder.dataAlarm;
		dataVoltage = builder.dataVoltage;
		dataTemp = builder.dataTemp;
	}

	public boolean isRealTime() {
		return isRealTime;
	}

	public String getCollectDateTime() {
		return collectDateTime;
	}

	public RealTimeDataVehicle getDataVehicle() {
		return dataVehicle;
	}

	public RealTimeDataMotor[] getDataMotor() {
		return dataMotor;
	}

	public RealTimeDataFuelCell getDataFuelCell() {
		return dataFuelCell;
	}

	public RealTimeDataEngine getDataEngine() {
		return dataEngine;
	}

	public ReportPos getDataPos() {
		return dataPos;
	}

	public RealTimeDataExtermum getDataExtermum() {
		return dataExtermum;
	}

	public RealTimeDataAlarm getDataAlarm() {
		return dataAlarm;
	}

	public RealTimeDataVoltage[] getDataVoltage() {
		return dataVoltage;
	}

	public RealTimeDataTemp[] getDataTemp() {
		return dataTemp;
	}
	
	
}
