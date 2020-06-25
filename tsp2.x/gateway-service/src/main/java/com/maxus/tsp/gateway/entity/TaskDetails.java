package com.maxus.tsp.gateway.entity;


import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="task_details")
public class TaskDetails {
	
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
	
	private String category;//机器类别
	
	private String address;//ip地址
	
	private long number;//条数
	
	private Date startTime;//开始时间
	
	private Date endTime;//结束时间
	
	private String tps;
	
	private String avgCost;//平均响应时间

	public TaskDetails(String category, long number, long startTime,long endTime) {
		this.category = category;
		this.number = number;
		this.startTime = new Date(startTime);
		this.endTime = new Date(endTime);
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public TaskDetails() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public String getTps() {
		return tps;
	}

	public void setTps(String tps) {
		this.tps = tps;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAvgCost() {
		return avgCost;
	}

	public void setAvgCost(String avgCost) {
		this.avgCost = avgCost;
	}

	@Override
	public String toString() {
		return "TaskDetails [id=" + id + ", category=" + category + ", address=" + address + ", number=" + number
				+ ", startTime=" + startTime + ", endTime=" + endTime + ", tps=" + tps + ", avgCost=" + avgCost + "]";
	}

}
