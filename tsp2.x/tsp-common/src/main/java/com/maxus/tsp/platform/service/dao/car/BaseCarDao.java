package com.maxus.tsp.platform.service.dao.car;

import org.apache.ibatis.annotations.Param;

import com.maxus.tsp.platform.service.model.car.BaseCar;
import com.maxus.tsp.platform.service.model.vo.OwnerBusNichNamesVO;

/**
 * 
 * @ClassName: BaseCarDao.java
 * @Description: 车辆的dao
 * @author 赵伟阳
 * @version V1.0
 * @Date 2017年7月4日 上午10:18:36
 */
public interface BaseCarDao {

	/**
	 * @Title: selectBaseCarByVIN
	 * @Description: 根据发动机和车架号来查询车辆
	 * @param: @param
	 *             engineNo
	 * @param: @param
	 *             vin
	 * @param: @return
	 * @return: Long
	 * @throws @author
	 *             赵伟阳
	 * @Date 2017年7月24日 下午1:21:18
	 */
	BaseCar selectBaseCarByVIN(String vin);


	/**
	 * @Title: checkCamera
	 * @Description: 根据vin和cameralist检查摄像头编号合法性
	 * @param: @param
	 *             vin cameraList
	 * @param: @return
	 * @return: integer
	 * @throws @author
	 *             余佶
	 * @Date 2017年10月10日 下午16:17:13
	 */
	public int checkCamera(@Param("vin") String vin, @Param("cameraNo") String cameraNo);

	/**
	 * 
	* @Title:        getAllVinForTbox    
	* @Description:  获取所有tbox对应车架号信息   
	* @param:        @return       
	* @return:       OwnerBusNichNamesVO[]      
	* @throws    
	* @author        yuji   
	* @Date          2017年11月14日 上午8:59:18
	 */
	OwnerBusNichNamesVO[] getAllVinForTbox();

}
