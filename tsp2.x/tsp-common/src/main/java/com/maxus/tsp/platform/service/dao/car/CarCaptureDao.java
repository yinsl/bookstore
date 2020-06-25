package com.maxus.tsp.platform.service.dao.car;

import org.apache.ibatis.annotations.Param;

/**
 * 
 * @ClassName: CarCaptureDao.java
 * @Description: 远程抓拍dao
 * @author 张涛
 * @version V1.0
 * @Date 2017年10月9日 下午3:25:16
 */
public interface CarCaptureDao {

	/**
	 *
	 * @Title: insertCarCapture
	 * @Description: 根据vin号和抓拍日期插入数据库
	 * @param: @param
	 *             vin
	 * @param: @param
	 *             creatDate
	 * @param: @return
	 * @return: void
	 * @throws @author
	 *             oeqgn
	 * @Date 2017年10月10日 下午4:43:41
	 */
	void insertCarCapture(@Param("vin") String vin, @Param("creatDate") String creatDate);

	/**
	 * 
	 * @Title: insertCarCaptureLog
	 * @Description: 根据vin号和抓拍日期插入数据库
	 * @param: @param
	 *             vin
	 * @param: @param
	 *             creatDate
	 * @param: @return
	 * @return: void
	 * @throws @author
	 *             oeqgn
	 * @Date 2017年10月10日 下午4:43:41
	 */
	void insertCarCaptureLog(@Param("vin") String vin, @Param("eventDate") String eventDate,
			@Param(value = "enventType") int enventType);

	/**
	 * 
	 * @Title: updateCarCaptureStatus
	 * @Description: 根据vin号和抓拍序列号，更新抓拍状态
	 * @param: @param
	 *             vin
	 * @param: @param
	 *             shootID
	 * @param: @param
	 *             status
	 * @param: @return
	 * @return: void
	 * @throws @author
	 *             yuji
	 * @Date 2017年10月10日 下午4:43:41
	 */
	void updateCarCaptureStatus(@Param(value="vin") String vin,@Param(value="shootID")String shootID,@Param(value="status")int status);
	
	/**
	 * 
	 * @Title: getCountForOperingTakePhoto
	 * @Description: 查询限制时间之后，正在执行的拍照操作个数
	 * @param: @param
	 *             vin
	 * @param: @param
	 *             limitTime
	 * @param: @return
	 * @return: int
	 * @throws @author
	 *             yuji
	 * @Date 2018年02月26日 上午11:23:41
	 */
	int getCountForOperingTakePhoto(@Param(value="vin") String vin, @Param(value="limitTime") String limitTime);
}
