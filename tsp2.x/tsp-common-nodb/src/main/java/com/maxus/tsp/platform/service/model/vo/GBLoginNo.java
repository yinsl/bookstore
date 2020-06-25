package com.maxus.tsp.platform.service.model.vo;

/**
 * 国标登录流水号对象
 * @author uwczo
 *
 */
public class GBLoginNo {
	//最新登陆流水号， 数值从1开始，最大值65531
	int no;
	//最新登录时间
	String date;

	public int getNo() {
		return no;
	}
	public void setNo(int no) {
		this.no = no;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public GBLoginNo(int no, String date) {
		super();
		this.no = no;
		this.date = date;
	}
	public GBLoginNo() {
		super();
	}
	
}
