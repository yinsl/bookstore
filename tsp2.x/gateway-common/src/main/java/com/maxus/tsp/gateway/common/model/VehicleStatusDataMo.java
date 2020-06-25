package com.maxus.tsp.gateway.common.model;

public class VehicleStatusDataMo {

	private Integer id;
	private Object value;
	private String type;

	public VehicleStatusDataMo(){
		super();
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}
