package com.maxus.tsp.gateway.common.model;

import java.io.Serializable;

public class CameraRespInfo  implements Serializable {
	private static final long serialVersionUID = -8910492614623865227L;
	//摄像头编号
	private String cameraID;
	//摄像头结果
	private String result;

	public String getCameraID() {
		return cameraID;
	}
	public void setCameraID(String cameraID) {
		this.cameraID = cameraID;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
}
