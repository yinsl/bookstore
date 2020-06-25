/**        
 * KafkaConsumerConfig.java Create on 2017年8月2日      
 * Copyright (c) 2017年8月2日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.gateway.mq.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: BeanConfig.java
 * @Description: 创建kafka监听Bean
 * @author 任怀宇
 * @version V1.0
 * @Date 2017年8月2日 下午1:34:18
 */
@Configuration
public class BeanConfig {

//	@Bean
//	public RemoteCtrlListener listener() {
//		return new RemoteCtrlListener();
//	}
	
//	@Bean
//	public HomeCtrlListener homeCtrlListener(){
//		return new HomeCtrlListener();
//	}
	
	/*@Bean
	public TransferCtrlInnerListener transInnerlistener() {
		return new TransferCtrlInnerListener();
	}*/

//	@Bean
//	public RemoteConfigCtrlListener remoteConfigCtrlListener() {
//		return new RemoteConfigCtrlListener();
//	}

//	@Bean
//	public DirectReportListener directReportListener() {
//		return new DirectReportListener();
//	}

//	@Bean
//	public BlueToothListener blueToothListener(){
//		return new BlueToothListener();
//	}

//	@Bean
//	public TBoxBlueToothListener tBoxBlueToothListener() {
//		return new TBoxBlueToothListener();
//	}

//	@Bean
//	public VersionUpgradeListener versionUpgradeListener() {
//		return new VersionUpgradeListener();
//	}
	
//	@Bean
//	public UpgradeResumeListener upgradeResumeListener() {
//		return new UpgradeResumeListener();
//	}
	
//	@Bean
//	public VersionQueryListener versionQueryListener(){
//		return new VersionQueryListener();
//	}

	@Bean
	public RmtGroupListener rmtGroupListener() {
		return new RmtGroupListener();
	}

//	@Bean
//	public TboxGetVehicleStatusListener tboxGetVehicleStatusListener(){
//		return new TboxGetVehicleStatusListener();
//	}

//	@Bean
//	public FileCtrlListener fileCtrlListener(){
//		return new FileCtrlListener();
//	}

//	@Bean
//	public GetEcuVersionUpgradeLlistener getEcuVersionUpgradeLlistener() {
//		return new GetEcuVersionUpgradeLlistener();
//	}

	@Bean
	public TransferCtrlOutterListener transOutterlistener() {
		return new TransferCtrlOutterListener();
	}
	
	

//	@Bean
//	public CertificateUpgradeListener certificateUpgradeListener() {
//		return new CertificateUpgradeListener();
//	}



//	@Bean
//	public AgreeUpgradeListener agreeUpgradeListener() {
//		return new AgreeUpgradeListener();
//	}
	
//	@Bean
//	public ProgressReportListener progressReportListener() {
//		return new ProgressReportListener();
//	}
	
}
