/**        
 * BaseCarClient.java Create on 2017年6月5日      
 * Copyright (c) 2017年6月5日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.gateway.serviceclient;

import com.maxus.tsp.platform.service.model.Platform;
import com.maxus.tsp.platform.service.model.car.Tbox;
import com.maxus.tsp.platform.service.model.vo.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 与数据库进行交互的接口
 * @author uwczo
 *
 */
@FeignClient("tsp-platform-service")
public interface TspPlatformClient {

	@RequestMapping(value = "/app/lockTbox", method = RequestMethod.GET)
	public boolean lockTbox(@RequestParam(value = "pKey") String pKey,
							@RequestParam(value = "tboxSN") String tboxSN,
							@RequestParam(value = "major") String major,
							@RequestParam(value = "minor") String minor,
							@RequestParam(value = "tboxStat") int tboxStat);

	@RequestMapping(value = "/app/lockTboxComplete", method = RequestMethod.GET)
	public boolean lockTboxComplete(@RequestParam(value = "tboxSN") String tboxSN);

	@RequestMapping(value ="/app/tboxDataSave", method = RequestMethod.POST )
	public boolean tboxDataSave(@RequestBody Tbox tbox);

	@RequestMapping(value = "/app/updateVerStatus", method = RequestMethod.GET)
	public boolean updateVerStatus(@RequestParam(value = "tboxSN") String tboxSN,
                                   @RequestParam(value = "status") int status);

	@RequestMapping(value = "/app/updatePkeyStatus", method = RequestMethod.GET)
	public boolean updatePkeyStatus(@RequestParam(value = "tboxSN") String tboxSN,
									@RequestParam(value = "status") int status);

	@RequestMapping(value = "/app/getTboxUpdateInfo", method = RequestMethod.GET)
	public TboxUpdateInfoVo getTboxUpdateInfo(@RequestParam(value = "id") int id);

	@RequestMapping(value = "/app/getAllTboxInfo", method = RequestMethod.GET)
	public TboxVo[] getAllTboxInfo();

	@RequestMapping(value = "/app/inActiveTbox", method = RequestMethod.GET)
	public boolean inActiveTbox(@RequestParam(value = "sn") String sn);

	@RequestMapping(value = "/app/resetTboxStatus", method = RequestMethod.GET)
	public boolean resetTboxStatus(@RequestParam(value = "sn") String sn);

	@RequestMapping(value = "/app/updateTboxBySn", method = RequestMethod.GET)
	public boolean updateTboxBySn(@RequestBody Tbox tbox);

	//baseCarController
	//将tbox及sn与vin对应关系初始化至redis
	@RequestMapping(value = "/api/getAllVinForTbox", method = RequestMethod.GET)
	public OwnerBusNichNamesVO[] getAllVinForTbox();

	@RequestMapping(value = "/api/selectBaseCarByVIN", method = RequestMethod.GET)
	public BaseCar selectBaseCarByVIN(@RequestParam(value = "vin") String vin);

	@RequestMapping(value = "/api/checkCamera", method = RequestMethod.GET)
	public int checkCamera(@RequestParam(value = "vin") String vin, @RequestParam(value = "cameraList") String cameraList);


	@RequestMapping(value = "/app/getTboxVo", method = RequestMethod.GET)
	public TboxVo getTboxVo(@RequestParam(value = "tboxSN") String tboxSN);


	//CarCaptureController
	@RequestMapping(value = "/tsp/carCapture/updateCarCaptureStatus", method = RequestMethod.GET)
	public boolean updateCarCaptureStatus(@RequestParam(value = "vin") String vin, @RequestParam(value = "shootID") String shootID, @RequestParam(value = "status") int status);

	@RequestMapping(value = "/tsp/carCapture/insertCarCapture", method = RequestMethod.GET)
	public boolean insertCarCapture(@RequestParam(value = "vin") String vin, @RequestParam(value = "creatDate") String creatDate);

	@RequestMapping(value = "/tsp/carCapture/insertCarCaptureLog", method = RequestMethod.GET)
	public boolean insertCarCaptureLog(@RequestParam(value = "vin") String vin, @RequestParam(value = "eventDate") String eventDate, @RequestParam(value = "enventType") int enventType);

	@RequestMapping(value = "/tsp/carCapture/getCountForOperingTakePhoto", method = RequestMethod.GET)
	public int getCountForOperingTakePhoto(@RequestParam(value = "vin") String vin, @RequestParam(value = "limitTime") String limitTime);


	//------Redis backup----供DataProcessing使用----调用platform-service(RedisBackUpController)
	//rds_car_remote_ctrl_resp//
	@RequestMapping(value = "/api/updateDataInRdsCarRemoteCtrlResp", method = RequestMethod.GET)
	public boolean updateDataInRdsCarRemoteCtrlResp(@RequestParam(value = "tboxSn") String tboxSn, @RequestParam(value = "value") String value);

	@RequestMapping(value = "/api/deleteDataInRdsCarRemoteCtrlResp", method = RequestMethod.GET)
	public boolean deleteDataInRdsCarRemoteCtrlResp(@RequestParam(value = "tboxSn") String tboxSn);

	@RequestMapping(value = "/api/getValueInRdsCarRemoteCtrlResp", method = RequestMethod.GET)
	public String getValueInRdsCarRemoteCtrlResp(@RequestParam(value = "tboxSn") String tboxSn);

	//rds_online_tbox//
	@RequestMapping(value = "/api/updateDataInRdsOnlineTbox", method = RequestMethod.GET)
	public boolean updateDataInRdsOnlineTbox(@RequestParam(value = "tboxSn") String tboxSn, @RequestParam(value = "value") String value);

	@RequestMapping(value = "/api/deleteDataInRdsOnlineTbox", method = RequestMethod.GET)
	public boolean deleteDataInRdsOnlineTbox(@RequestParam(value = "tboxSn") String tboxSn);

	@RequestMapping(value = "/api/getValueInRdsOnlineTbox", method = RequestMethod.GET)
	public String getValueInRdsOnlineTbox(@RequestParam(value = "tboxSn") String tboxSn);

	//rds_tbox_channel//
	@RequestMapping(value = "/api/updateDateInRdsTboxChannel", method = RequestMethod.GET)
	public boolean updateDateInRdsTboxChannel(@RequestParam(value = "tboxSn") String tboxSn, @RequestParam(value = "value") String value);

	@RequestMapping(value = "/api/deleteDataInRdsTboxChannel", method = RequestMethod.GET)
	public boolean deleteDataInRdsTboxChannel(@RequestParam(value = "tboxSn") String tboxSn);

	@RequestMapping(value = "/api/getValueInRdsTboxChannel", method = RequestMethod.GET)
	public String getValueInRdsTboxChannel(@RequestParam(value = "tboxSn") String tboxSn);

	//rds_tbox_logout_time//
	@RequestMapping(value = "/api/updateDataInRdsTboxLogOutTime", method = RequestMethod.GET)
	public boolean updateDataInRdsTboxLogOutTime(@RequestParam(value = "tboxSn") String tboxSn, @RequestParam(value = "value") String value);

	@RequestMapping(value = "/api/getValueInRdsTboxLogOutTime", method = RequestMethod.GET)
	public String getValueInRdsTboxLogOutTime(@RequestParam(value = "tboxSn") String tboxSn);

	////////////////////////////RemoteCtrlController
	@RequestMapping(value = "/app/recordRemoteCtrl", method = RequestMethod.GET)
	public int recordRemoteCtrl(@RequestParam("vin") String vin, @RequestParam("cmd") String cmd, @RequestParam("cmdVal") String cmdVal);

	@RequestMapping(value = "/app/updateRemoteCtrlStatus", method = RequestMethod.GET)
	public boolean addRemoteCtrlDownStatus(@RequestParam("remoteCtrlID") int remoteCtrlID, @RequestParam("downDate") String downDate);

	@RequestMapping(value = "/app/getCountForOperingRmtCtrl", method = RequestMethod.GET)
	public int getCountForOperRmtCtrl(@RequestParam(value = "vin") String vin, @RequestParam(value = "limitTime") String limitTime);


	@RequestMapping(value = "/getPlatformList", method = RequestMethod.GET)
	public List<Platform> getPlatformList();

}