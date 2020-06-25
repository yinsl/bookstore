package com.maxus.tsp.platform.service.dao.car;


import org.apache.ibatis.annotations.Param;

import com.maxus.tsp.platform.service.model.car.Tbox;
import com.maxus.tsp.platform.service.model.vo.TboxUpdateInfoVo;
import com.maxus.tsp.platform.service.model.vo.TboxVo;

/**
 * 
 * @ClassName: BaseCarDao.java
 * @Description: 车辆的dao
 * @author 赵伟阳
 * @version V1.0
 * @Date 2017年7月4日 上午10:18:36
 */
public interface TboxDao {

	/**
	 * 查询指定Tbox信息
	 * 
	 * @Title: getTboxInfo
	 * @Description:  
	 * @param: @param
	 *             tboxSN
	 * @param: @return
	 * @return: Tbox
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月19日 上午9:32:35
	 */
	//Tbox getTboxInfo(String tboxSN);

	/**
	 * 
	 * @Title: getTboxVo
	 * @Description: 获取TboxVo信息
	 * @param: @param
	 *             tboxSN
	 * @param: @return
	 * @return: TboxVo
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月19日 上午10:37:15
	 */
	TboxVo getTboxVo(@Param("tboxSN") String tboxSN);

	
	/**
	 * 
	 * @Title: getAllTboxVo
	 * @Description: 获取全部TboxVo信息
	 * @param: @return
	 * @return: TboxVo[]
	 * @throws @author
	 *             yuji
	 * @Date 2017年11月2日 上午10:37:15
	 */
	TboxVo[] getAllTboxInfo();
			
	/**
	 * 
	 * @Title: getTboxUpdateInfo
	 * @Description: 获取TboxVo信息
	 * @param: @param
	 *             tboxSN
	 * @param: @return
	 * @return: TboxVo
	 * @throws @author
	 *             yuji
	 * @Date 2017年10月16日 上午10:37:15
	 */
	TboxUpdateInfoVo getTboxUpdateInfo(@Param("id") int id);

	/**
	 * 
	 * @Title: updatePkeyVerStatus
	 * @Description: 更新版本状态
	 * @param: @param
	 *             tboxSN
	 * @param: @return
	 * @return: boolean
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月19日 上午9:50:40
	 */
	void updatePkeyVerStatus(String tboxSN, int status);

	/**
	 * @Title: lockTbox
	 * @Description: 锁定tbox，并且将pkey等信息写入
	 * @param: @param
	 *             tboxSN
	 * @param: @return
	 * @return: boolean
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月19日 上午10:41:22
	 */
	void lockTbox(@Param("tboxSN") String tboxSN, @Param("pkey") String pkey,
			@Param("major") String major, @Param("minor") String minor, @Param("tboxStat") int tboxStat);

	/**
	 * @Title: lockTboxComplete
	 * @Description: 根据rvm的成功反馈，彻底锁定tbox
	 * @param: @param
	 *             tboxSN
	 * @param: @return
	 * @return: boolean
	 * @throws @author
	 *             uwczo
	 * @Date 2018年5月3日 上午10:41:22
	 */
	void lockTboxComplete(@Param("tboxSN") String tboxSN);
	
	/**
	 * @Title: updateTboxSWVerStatus
	 * @Description:  
	 * @param: @param
	 *             tboxSN
	 * @param: @param
	 *             status
	 * @param: @return
	 * @return: boolean
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月19日 上午10:59:47
	 */
	void updateTboxSWVerStatus(@Param("tboxSN")String tboxSN,@Param("status")  int status);

	/**
	 * 
	 * @Title: insertTBOX
	 * @Description: 
	 * @param: @param
	 *             tbox
	 * @return: void
	 * @throws @author
	 *             zhuna
	 * @Date 2017年8月2日 下午5:03:43
	 */
	void insertTBOX(Tbox tbox);
	
	
	/**
	 * 
	 * @param tboxSN
	 * @return 
	 * @throws @author
	 *             guhaowei
	 */
	void deleteTboxBySn(@Param("tboxSN") String tboxSN);


	void resetTboxStatus(@Param("tboxSN") String tboxSN);

    void updateTboxBySn(Tbox tbox);
}
