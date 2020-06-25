/**
 * TspServiceProc.java Create on 2017年7月17日
 * Copyright (c) 2017年7月17日 by 上汽集团商用车技术中心
 *
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>
 * @version 1.0
 */
package com.maxus.tsp.gateway.ota.process;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.maxus.tsp.gateway.service.KafkaService;
import com.maxus.tsp.gateway.service.TboxService;

public class BaseOtaProc  {
    // // 日志
    // private static Logger logger = LogManager.getLogger(BaseOtaProc.class);
    // kafka服务
	@Autowired
    protected  KafkaService kafkaService;
	
	@Autowired
    // Tbox服务
    protected TboxService tboxService;
	
	public BaseOtaProc(){
		
	}

	public BaseOtaProc(KafkaService kafkaService, TboxService tboxService) {
		this.kafkaService = kafkaService;
		this.tboxService = tboxService;
	}


	public KafkaService getKafkaService() {
        return kafkaService;
    }


    public TboxService getTboxService() {
        return tboxService;
    }

    public long getEventTime(){
	    return System.currentTimeMillis();
    }

    //获取请求序列号
    public String getSeqNo(){
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");//设置日期格式
        String date = df.format(new Date());//
        String random = String.valueOf((int)(Math.random()*9+1)*10000);
        return date + random;
    }

}