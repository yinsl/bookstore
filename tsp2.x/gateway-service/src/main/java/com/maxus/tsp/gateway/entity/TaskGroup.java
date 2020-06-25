package com.maxus.tsp.gateway.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name="task_group")
public class TaskGroup {
	
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
	
	@Column
	private Date createTime;
	
	@Column
	private int groupNum;
	
	@Column
	private String groupName;
	
	@OneToMany(cascade = {CascadeType.ALL}, fetch=FetchType.EAGER,orphanRemoval=true)
	@JoinColumn(name = "group_id")
	private List<TaskDetails> detailsList;

	public List<TaskDetails> getDetailsList() {
		if(detailsList==null) {
			detailsList = new ArrayList<>();
		}
		return detailsList;
	}

	public void setDetailsList(List<TaskDetails> detailsList) {
		this.detailsList = detailsList;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public TaskGroup(String groupName,int groupNum,Date createTime) {
		this.groupName = groupName;
		this.createTime= createTime;
		this.groupNum =groupNum;
	}

	public int getGroupNum() {
		return groupNum;
	}

	public void setGroupNum(int groupNum) {
		this.groupNum = groupNum;
	}

	public TaskGroup() {
	}

	@Override
	public String toString() {
		return "TaskGroup [id=" + id + ", createTime=" + createTime + ", groupNum=" + groupNum + ", groupName="
				+ groupName +  "]";
	}

}
