package com.maxus.tsp.gateway.common.model;

/**
 * 文件下载model类
 * @author lzgea
 *
 */
public class DownLoadFileMo {

	// sn
	private String sn;
	// 当前日期
	private long currentTime;
	// 文件类型
	private int fileType;
	// 文件大小
	private int fileInfoSize;
	// 文件内容
	private String fileInfo;
	// 下载url路径
	private String url;
	// MD5校验
	private String md5Data;
	// 序列号
	private String seqNo;
	
	
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

	public int getFileInfoSize() {
		return fileInfoSize;
	}

	public void setFileInfoSize(int fileInfoSize) {
		this.fileInfoSize = fileInfoSize;
	}

	public String getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(String fileInfo) {
		this.fileInfo = fileInfo;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMd5Data() {
		return md5Data;
	}

	public void setMd5Data(String md5Data) {
		this.md5Data = md5Data;
	}

	public String getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(String seqNo) {
		this.seqNo = seqNo;
	}

	public synchronized DownLoadFileMo copyDownLoadFileMo(){
		DownLoadFileMo bean = new DownLoadFileMo();
		bean.setCurrentTime(this.currentTime);
		bean.setFileInfo(this.fileInfo);
		bean.setFileInfoSize(this.fileInfoSize);
		bean.setFileType(this.fileType);
		bean.setMd5Data(this.md5Data);
		bean.setSeqNo(this.seqNo);
		bean.setSn(this.sn);
		bean.setUrl(this.url);
		return bean;
	}
}
