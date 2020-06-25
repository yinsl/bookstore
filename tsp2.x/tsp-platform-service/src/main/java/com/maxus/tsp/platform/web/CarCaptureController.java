package com.maxus.tsp.platform.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.maxus.tsp.platform.service.domain.car.CarCaptureServiceImpl;

/**
 * 
 * @ClassName: CarCaptureController.java
 * @Description: 远程抓拍图片查看相关的controller
 * @author 张涛
 * @version V1.0
 * @Date 2017年10月9日 下午3:20:51
 */
@RestController
@RequestMapping(value={"/tsp/carCapture","tsp/appapi"})
public class CarCaptureController {

	private static final Logger logger = LogManager.getLogger(CarCaptureController.class);
	@Autowired
	private CarCaptureServiceImpl ccServiceImpl;

	/**
	 *
	 * @Title: insertCarCapture
	 * @Description: 根据vin和抓拍日期插入抓拍数据
	 * @param: @param
	 *             vin
	 * @param: @param
	 *             creatDate
	 * @param: @return
	 * @return: void
	 * @throws @author
	 *             yuji
	 * @Date 2017年10月10日 下午4:49:02
	 */
	@RequestMapping(value = "/insertCarCapture", method = { RequestMethod.POST, RequestMethod.GET })
	public boolean insertCarCapture(@RequestParam("vin") String vin, @RequestParam("creatDate") String creatDate) {
		logger.info("Enter insertCarCapture:" + vin);
		try {
			ccServiceImpl.insertCarCapture(vin, creatDate);
			return true;
		} catch (Exception ex) {
			logger.error("Insert into car capture Exception: " + ex);
			return false;
		}
	}

	/**
	 *
	 * @Title: insertCarCaptureLog
	 * @Description: 根据vin和抓拍日期插入抓拍数据
	 * @param: @param
	 *             vin
	 * @param: @param
	 *             creatDate
	 * @param: @return
	 * @return: void
	 * @throws @author
	 *             yuji
	 * @Date 2017年10月10日 下午4:49:02
	 */
	@RequestMapping(value = "/insertCarCaptureLog", method = { RequestMethod.POST, RequestMethod.GET })
	public boolean insertCarCaptureLog(@RequestParam("vin") String vin, @RequestParam("eventDate") String eventDate,
			@RequestParam(value = "enventType") int enventType) {
		logger.info("Enter insertCarCaptureLog:" + vin );
		try {
			ccServiceImpl.insertCarCaptureLog(vin, eventDate, enventType);
			return true;
		} catch (Exception ex) {
			logger.error("Insert into car capture Exception: " + ex);
			return false;
		}
	}

	/**
	 * 
	 * @Title: updateCarCaptureStatus
	 * @Description: 根据vin和抓拍序列号，更新状态
	 * @param: @param
	 *             vin
	 * @param: @param
	 *             creatDate
	 * @param: @return
	 * @return: void
	 * @throws @author
	 *             yuji
	 * @Date 2017年10月10日 下午4:49:02
	 */
	@RequestMapping(value = "/updateCarCaptureStatus", method = RequestMethod.GET)
	public boolean updateCarCaptureStatus(@RequestParam(value="vin") String vin,@RequestParam(value="shootID")String shootID,@RequestParam(value="status")int status)
	{
		logger.info("Enter updateCarCaptureStatus:" + vin);
		try{
		return ccServiceImpl.updateCarCaptureStatus(vin,shootID,status);
		}
		catch(Exception ex)
		{
			logger.info("update car capture status Exception "+ex);
			return false;
		}
	}

	@RequestMapping(value = "/getCountForOperingTakePhoto", method = RequestMethod.GET)
	public int getCountForOperingTakePhoto(@RequestParam(value="vin") String vin, @RequestParam(value="limitTime") String limitTime)
	{
		logger.info("Enter getCountForOperingTakePhoto");
		return ccServiceImpl.getCountForOperingTakePhoto(vin, limitTime);
	}

}
