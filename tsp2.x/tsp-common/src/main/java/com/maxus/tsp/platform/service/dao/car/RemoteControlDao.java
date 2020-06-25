package com.maxus.tsp.platform.service.dao.car;

import org.apache.ibatis.annotations.Param;

import com.maxus.tsp.platform.service.model.vo.RemoteControlHistoryVo;

/**
 * 
 * @ClassName: RemoteControlDao.java
 * @Description: dao about remote control
 * @author 余佶
 * @version V1.0
 * @Date 2017年11月13日 上午10:18:36
 */
public interface RemoteControlDao {

	/**
	 * 
	* @Title:        recordRemoteCtrl    
	* @Description:  记录 远程控制指令历史
	* @param:        @param vin
	* @param:        @param cmd
	* @param:        @param cmdVal 
	* @param:        @return       
	* @return:       int      
	* @throws    
	* @author        yuji   
	* @Date          2017年11月10日 上午8:59:18
	 */
	int recordRemoteCtrl(RemoteControlHistoryVo remoteCtrlVo);
	
	/**
	 * 
	* @Title:        recordRemoteCtrl    
	* @Description:  更新 远程控制指令历史的下发时间
	* @param:        @param remoteCtrlID
	* @param:        @param downDate
	* @param:        @return       
	* @return:       void      
	* @throws    
	* @author        yuji   
	* @Date          2017年11月10日 上午8:59:18
	 */
	void addRemoteCtrlDownStatus(@Param("remoteCtrlID")int remoteCtrlID,@Param("downDate")String downDate);
	
	/**
	 * 
	* @Title:        getCountForOperRmtCtrl    
	* @Description:  获取在限制时间之后，存在的没有执行下发的远程控制指令
	* @param:        @param vin
	* @param:        @param limitTime
	* @param:        @return       
	* @return:       void      
	* @throws    
	* @author        yuji   
	* @Date          2018年02月26日 上午10:59:18
	 */
	int getCountForOperRmtCtrl(@Param(value="vin") String vin, @Param(value="limitTime") String limitTime);
}
