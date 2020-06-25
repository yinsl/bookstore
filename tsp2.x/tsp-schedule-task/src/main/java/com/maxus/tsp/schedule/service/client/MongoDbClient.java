package com.maxus.tsp.schedule.service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.maxus.tsp.nosql.mongodb.model.CarCan;
import com.maxus.tsp.nosql.mongodb.model.CarGPS;


@FeignClient("tsp-nosql-service")
public interface MongoDbClient {

	/**
	 *插入车辆的基本信息
	 */
	@RequestMapping(method=RequestMethod.GET,value="/nosql/mongodb/insertCarCan")
	Boolean insertCarCan(@RequestBody CarCan carCan);
	
	/**
	 *插入车辆的GPS信息
	 */
	@RequestMapping(method=RequestMethod.GET,value="/nosql/mongodb/insertCarGPS")
	Boolean insertCarGPS(@RequestBody CarGPS carGPS);
	
	/**
	 *返回指定车辆的基本信息
	 */
	@RequestMapping(method=RequestMethod.GET,value="/nosql/mongodb/findCarCan/{vin}")
	CarCan findCarCan(@PathVariable("vin")String vin);
	
	/**
	 *返回指定车辆的最近5条GPS信息
	 */
	@RequestMapping(method=RequestMethod.GET,value="/nosql/mongodb/findCarGPS/{vin}")
	List<CarGPS> findCarGPS(@PathVariable("vin")String vin);
}
