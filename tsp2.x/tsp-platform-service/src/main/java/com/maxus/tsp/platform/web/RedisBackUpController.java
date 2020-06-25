package com.maxus.tsp.platform.web;

import com.maxus.tsp.platform.service.domain.car.RedisBackUpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author guhaowei
 *
 */
@RestController
public class RedisBackUpController {
	public final Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	private RedisBackUpService redisBackUpService;
	
	
	//更新rds_car_remote_ctrl_resp表格数据
	@RequestMapping(value = "/api/updateDataInRdsCarRemoteCtrlResp", method = RequestMethod.GET)
	public boolean updateDataInRdsCarRemoteCtrlResp(@RequestParam(value="tboxSn") String tboxSn, @RequestParam(value="value") String value) {
		logger.info("Enter updateDataInRdsCarRemoteCtrlResp。TBox {}", tboxSn);
		return redisBackUpService.updateRecordIndb_rds_car_remote_ctrl_resp(tboxSn, value);
	}

	//根据SN号删除rds_car_remote_ctrl_resp表格数据
	@RequestMapping(value = "/api/deleteDataInRdsCarRemoteCtrlResp", method = RequestMethod.GET)
	public boolean deleteDataInRdsCarRemoteCtrlResp(@RequestParam(value="tboxSn") String tboxSn) {
		logger.info("Enter deleteDataInRdsCarRemoteCtrlResp。TBox {}", tboxSn);
		return redisBackUpService.deleteRecordIndb_rds_car_remote_ctrl_resp(tboxSn);
	}

	//根据Sn号获取rds_car_remote_ctrl_resp表格数据
	@RequestMapping(value = "/api/getValueInRdsCarRemoteCtrlResp", method = RequestMethod.GET)
	public String getValueInRdsCarRemoteCtrlResp(@RequestParam(value="tboxSn") String tboxSn) {
		logger.info("Enter getValueInRdsCarRemoteCtrlResp。TBox {}", tboxSn);
		return redisBackUpService.getValueIndb_rds_car_remote_ctrl_resp(tboxSn);
	}

	//更新rds_online_tbox表格数据
	@RequestMapping(value = "/api/updateDataInRdsOnlineTbox", method = RequestMethod.GET)
	public boolean updateDataInRdsOnlineTbox(@RequestParam(value="tboxSn") String tboxSn, @RequestParam(value="value") String value) {
		logger.info("Enter updateDataInRdsOnlineTbox。TBox {}", tboxSn);
		return redisBackUpService.updateRecordIndb_rds_online_tbox(tboxSn, value);
	}

	//删除rds_online_tbox表格数据
	@RequestMapping(value = "/api/deleteDataInRdsOnlineTbox", method = RequestMethod.GET)
	public boolean deleteDataInRdsOnlineTbox(@RequestParam(value="tboxSn") String tboxSn) {
		logger.info("Enter deleteDataInRdsOnlineTbox。TBox {}", tboxSn);
		return redisBackUpService.deleteRecordIndb_rds_online_tbox(tboxSn);
	}

	//获取rds_online_tbox表格数据
	@RequestMapping(value = "/api/getValueInRdsOnlineTbox", method = RequestMethod.GET)
	public String getValueInRdsOnlineTbox(@RequestParam(value="tboxSn") String tboxSn) {
		logger.info("Enter getValueInRdsOnlineTbox。TBox {}", tboxSn);
		return redisBackUpService.getValueIndb_rds_online_tbox(tboxSn);
	}

	//更新rds_tbox_channel表格数据
	@RequestMapping(value = "/api/updateDateInRdsTboxChannel", method = RequestMethod.GET)
	public boolean updateDateInRdsTboxChannel(@RequestParam(value="tboxSn") String tboxSn, @RequestParam(value="value") String value) {
		logger.info("Enter updateDateInRdsTboxChannel。TBox {}", tboxSn);
		return redisBackUpService.updateRecordIndb_rds_tbox_channel(tboxSn, value);
	}

	//删除rds_tbox_channel表格数据
	@RequestMapping(value = "/api/deleteDataInRdsTboxChannel", method = RequestMethod.GET)
	public boolean deleteDataInRdsTboxChannel(@RequestParam(value="tboxSn") String tboxSn) {
		logger.info("Enter deleteDataInRdsTboxChannel。TBox {}", tboxSn);
		return redisBackUpService.deleteRecordIndb_rds_tbox_channel(tboxSn);
	}

	//获取rds_tbox_channel表格数据
	@RequestMapping(value = "/api/getValueInRdsTboxChannel", method = RequestMethod.GET)
	public String getValueInRdsTboxChannel(@RequestParam(value="tboxSn") String tboxSn) {
		logger.info("Enter getValueInRdsTboxChannel。TBox {}", tboxSn);
		return redisBackUpService.getValueIndb_rds_tbox_channel(tboxSn);
	}

	//删除rds_tbox_logout_time表格数据
	@RequestMapping(value = "/api/updateDataInRdsTboxLogOutTime", method = RequestMethod.GET)
	public boolean updateDataInRdsTboxLogOutTime(@RequestParam(value="tboxSn") String tboxSn, @RequestParam(value="value") String value) {
		logger.info("Enter updateDataInRdsTboxLogOutTime。TBox {}", tboxSn);
		return redisBackUpService.updateRecordIndb_rds_tbox_logout_time(tboxSn,value);
	}

	//获取rds_tbox_logout_time表格数据
	@RequestMapping(value = "/api/getValueInRdsTboxLogOutTime", method = RequestMethod.GET)
	public String getValueInRdsTboxLogOutTime(@RequestParam(value="tboxSn") String tboxSn) {
		logger.info("Enter getValueInRdsTboxLogOutTime。TBox {}", tboxSn);
		return redisBackUpService.getValueIndb_rds_tbox_logout_time(tboxSn);
	}
}
