package com.maxus.tsp.gateway.common.model;

public class UpLoadFileMo{
	// sn
	private String sn;
	// 当前日期
	private long currentTime;
	// 文件类型
	private int fileType;
	// tbox文件路径
	private String localPath;
	// 序列号
	private String seqNo;

	
	public String getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(String seqNo) {
		this.seqNo = seqNo;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public long getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(long currentTime) {
		this.currentTime = currentTime;
	}

	public int getFileType() {
		return fileType;
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}


	public synchronized UpLoadFileMo copyUpLoadFileMo(){
		UpLoadFileMo bean = new UpLoadFileMo();
		bean.setCurrentTime(this.currentTime);
		bean.setFileType(this.fileType);
		bean.setLocalPath(this.localPath);
		bean.setSeqNo(this.seqNo);
		bean.setSn(this.sn);
		return bean;
	}
}
