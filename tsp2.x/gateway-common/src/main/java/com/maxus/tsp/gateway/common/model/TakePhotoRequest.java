package com.maxus.tsp.gateway.common.model;

public class TakePhotoRequest {
		//车架号
		private String vin;
		//摄像头编号列表
		private String cameraList;
		public String getVin() {
			return vin;
		}
		public void setVin(String vin) {
			this.vin = vin;
		}
		public String getCameraList() {
			return cameraList;
		}
		public void setCameraList(String cameraList) {
			this.cameraList = cameraList;
		}
}
