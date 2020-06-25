package com.maxus.tsp.gateway.common.model;

public class TakePhotoRespInfo {
	//拍照序列号
	private String takePhotoID;
	//摄像头结果数组
	private CameraRespInfo[] cameraResult;
	//车辆电源模式
	private String powerMode;

	public String getTakePhotoID() {
		return takePhotoID;
	}
	public void setTakePhotoID(String takePhotoID) {
		this.takePhotoID = takePhotoID;
	}
	public CameraRespInfo[] getCameraResult() {
		return cameraResult;
	}
	public void setCameraResult(CameraRespInfo[] cameraResult) {
		this.cameraResult = cameraResult;
	}
	public String getPowerMode() {
		return powerMode;
	}
	public void setPowerMode(String powerMode) {
		this.powerMode = powerMode;
	}
}


