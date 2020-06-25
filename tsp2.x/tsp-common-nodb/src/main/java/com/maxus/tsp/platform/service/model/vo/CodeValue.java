package com.maxus.tsp.platform.service.model.vo;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CodeValue implements Serializable, Cloneable{

	
	private static Logger logger = LogManager.getLogger(CodeValue.class);
	private static final long serialVersionUID = 4888660694318723566L;
	String code;
	Object value;
	
	public CodeValue() {
		
	}
	
	public CodeValue (String code, Object value) {
		this.code = code;
		this.value = value;
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}

	public static CodeValue copyValueOf(CodeValue inputCodeValueInfo) {
		try {
			return (CodeValue) inputCodeValueInfo.clone();
		} catch (CloneNotSupportedException ex) {
			logger.error("CodeValue clone failed.", ex);
			return null;
		}
	}

}
