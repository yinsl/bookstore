/**        
 * TboxService.java Create on 2017年7月6日      
 * Copyright (c) 2017年7月6日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.platform.service.domain.car;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maxus.tsp.platform.service.dao.car.TboxDao;
import com.maxus.tsp.platform.service.model.car.Tbox;
import com.maxus.tsp.platform.service.model.vo.TboxUpdateInfoVo;
import com.maxus.tsp.platform.service.model.vo.TboxVo;

/**
 * @ClassName: TboxService.java
 * @Description: 设置PIN值
 * @author 赵伟阳
 * @version V1.0
 * @Date 2017年7月6日 下午4:11:41
 */
@Service
@Transactional
public class TboxService {

	@Autowired
	private TboxDao tboxMapper;

	/**
	 * 
	 * @Title: updatePkeyVerStatus
	 * @Description: 更新版本更新状态
	 * @param: @param
	 *             serialNumber
	 * @param: @return
	 * @return: boolean
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月19日 上午9:52:31
	 */
	public boolean updatePkeyVerStatus(String serialNumber, int result) {

		try {
			tboxMapper.updatePkeyVerStatus(serialNumber, result);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @Title: getTboxVo
	 * @Description: 
	 * @param: @param
	 *             tboxSN
	 * @param: @return
	 * @return: TboxVo
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月19日 上午10:38:28
	 */
	@Transactional(readOnly = true)
	public TboxVo getTboxVo(String tboxSN) {

		return tboxMapper.getTboxVo(tboxSN);
	}

	/**
	 * @Title: getAllTboxVo
	 * @Description: 
	 * @param: @return
	 * @return: TboxVo
	 * @throws @author
	 *             yuji
	 * @Date 2017年11月2日 上午10:38:28
	 */
	@Transactional(readOnly = true)
	public TboxVo[] getAllTboxInfo() {

		return tboxMapper.getAllTboxInfo();
	}
	
	/**
	 * @Title: getTboxUpdateInfo
	 * @Description: 
	 * @param: @param
	 *             tboxSN
	 * @param: @return
	 * @return: TboxVo
	 * @throws @author
	 *             yuji
	 * @Date 2017年10月16日 上午10:38:28
	 */
	@Transactional(readOnly = true)
	public TboxUpdateInfoVo getTboxUpdateInfo(int id) {

		return tboxMapper.getTboxUpdateInfo(id);
	}

	/**
	 * @Title: lockTbox
	 * @Description: 
	 * @param: @param
	 *             tboxSN
	 * @param: @return
	 * @return: boolean
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月19日 上午10:41:02
	 */
	public boolean lockTbox(String tboxSN, String pkey,String major,String minor, int tboxStat) {

		try {
			tboxMapper.lockTbox(tboxSN, pkey,major,minor, tboxStat);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param tboxSN
	 * @return
	 */
	public boolean lockTboxComplete(String tboxSN) {

		try {
			tboxMapper.lockTboxComplete(tboxSN);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

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
	 * @Date 2017年7月19日 上午10:59:12
	 */
	public boolean updateTboxSWVerStatus(String tboxSN, int status) {
		try {
			tboxMapper.updateTboxSWVerStatus(tboxSN, status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * @Title: updatePkeyStatus
	 * @Description: 
	 * @param: @param
	 *             tboxSN
	 * @param: @param
	 *             status
	 * @param: @return
	 * @return: boolean
	 * @throws @author
	 *             fogmk
	 * @Date 2017年7月19日 上午11:04:23
	 */
	public boolean updatePkeyStatus(String tboxSN, int status) {
		try {
			tboxMapper.updatePkeyVerStatus(tboxSN, status);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	/**
	 * Tbox数据插入
	 * 
	 * @param tbox
	 * @author zhuna
	 * @date 2017年9月23日
	 */
	public void insertTBOX(Tbox tbox) {
		Date date = new Date();
		tbox.setCreatDate(date);
		tboxMapper.insertTBOX(tbox);

	}
	
	/**
	 * 
	 * @param tboxSN
	 */
	public void deleteTboxBySn(String tboxSN){
		tboxMapper.deleteTboxBySn(tboxSN);
	}

	/**
	 *
	 * @param tboxSN
	 */
	public void resetTboxStatus(String tboxSN){
		tboxMapper.resetTboxStatus(tboxSN);
	}

	public void updateTboxBySn(Tbox tbox){
        tboxMapper.updateTboxBySn(tbox);
    }

}
