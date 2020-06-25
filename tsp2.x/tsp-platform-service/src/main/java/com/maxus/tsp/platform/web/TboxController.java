package com.maxus.tsp.platform.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.platform.service.domain.car.TboxService;
import com.maxus.tsp.platform.service.model.car.Tbox;
import com.maxus.tsp.platform.service.model.vo.TboxUpdateInfoVo;
import com.maxus.tsp.platform.service.model.vo.TboxVo;
/**
 * @ClassName     TboxController.java
* @Description:    
* @Author:         zhuna
* @CreateDate:     2018/12/14 10:52
* @Version:        1.0
*/
@RestController
public class TboxController {
	private final Logger logger = LogManager.getLogger(getClass());
	@Autowired
	private TboxService tboxService;
   /* @Autowired
    RedisAPI redisAPI;*/

	@RequestMapping(value = "/app/lockTbox", method = RequestMethod.GET)
	public boolean lockTbox(@RequestParam(value = "pKey") String pKey, @RequestParam(value = "tboxSN") String tboxSN,
							@RequestParam(value = "major") String major, @RequestParam(value = "minor") String minor,
							@RequestParam(value = "tboxStat") int tboxStat) {
		logger.info("lockTbox start");
		return tboxService.lockTbox(tboxSN, pKey, major, minor, tboxStat);
	}

	@RequestMapping(value = "/app/lockTboxComplete", method = RequestMethod.GET)
	public boolean lockTboxComplete(@RequestParam(value = "tboxSN") String tboxSN) {
		logger.info("lockTboxComplete start");
		return tboxService.lockTboxComplete(tboxSN);
	}

	/**
	 * TBbox数据新增、编辑
	 * @param tbox
	 * @author zhuna
	 * @date 2017年9月28日
	 */
	@RequestMapping(value = "/app/tboxDataSave", method = RequestMethod.POST)
	public boolean tboxDataSave(@RequestBody Tbox tbox) {
		try {
			// 获得sn号
			String sn = tbox.getSn();
			if (sn == null) {
				logger.warn("TBox序列号为空!");
				return false;
			} else {
				TboxVo tboxExist = tboxService.getTboxVo(sn);
				if (tboxExist != null) {
					logger.warn("TBox数据" + sn + "已经存在");
					return false;
				} else {
					tboxService.insertTBOX(tbox);
				}
			}
			return true;
		} catch (Exception e) {
			logger.error(ThrowableUtil.getErrorInfoFromThrowable(e));
			return false;
		}

	}

	@RequestMapping(value = "/app/updatePkeyStatus", method = RequestMethod.GET)
	public boolean updatePkeyStatus(@RequestParam String tboxSN, @RequestParam int status) {
		logger.info("updatePkeyStatus start" + tboxSN);
		return tboxService.updatePkeyVerStatus(tboxSN, status);
	}

	@RequestMapping(value = "/app/updateVerStatus", method = RequestMethod.GET)
	public boolean updateTboxSWVerStatus(@RequestParam String tboxSN, @RequestParam int status) {
		logger.info("updateTboxSWVerStatus start" + tboxSN);
		return tboxService.updateTboxSWVerStatus(tboxSN, status);
	}

	@RequestMapping(value = "/app/getTboxUpdateInfo", method = RequestMethod.GET)
	public TboxUpdateInfoVo getTboxUpdateInfo(@RequestParam(value = "id") int id) {
		logger.info("getTboxVo start" + id);
		TboxUpdateInfoVo tboxUpdateInfoVo = tboxService.getTboxUpdateInfo(id);
		return tboxUpdateInfoVo;
	}

	@RequestMapping(value = "/app/getTboxVo", method = RequestMethod.GET)
	public TboxVo getTboxVo(@RequestParam(value = "tboxSN") String tboxSN) {
		logger.info("getTboxVo start" + tboxSN);
		TboxVo tboxVo = tboxService.getTboxVo(tboxSN);
		return tboxVo;
	}

	@RequestMapping(value = "/app/getAllTboxInfo", method = RequestMethod.GET)
	public TboxVo[] getAllTboxInfo() {
		logger.info("getAllTboxVo start");
		TboxVo[] allTboxInfo = tboxService.getAllTboxInfo();
		return allTboxInfo;
	}

	/**
	 * 删除数据空中该sn对应的数据
	 * @param sn
	 * @return
	 */
	@RequestMapping(value = "/app/deleteTboxBySn", method = RequestMethod.GET)
	public boolean deleteTboxBySn(String sn) {
		boolean result = false;
		try {
			if (null != sn) {
				tboxService.deleteTboxBySn(sn);
				result = true;
			}
		} catch (Exception e) {
			logger.error(ThrowableUtil.getErrorInfoFromThrowable(e));
		}
		return result;
	}

	/**
	 * 数据库修改激活状态
	 * @param sn
	 * @return
	 */
	@RequestMapping(value = "/app/resetTboxStatus", method = RequestMethod.GET)
	public boolean resetTboxStatus(String sn) {
		boolean result = false;
		try {
			if (null != sn) {
				tboxService.resetTboxStatus(sn);
				result = true;
			}
		} catch (Exception e) {
			logger.error(ThrowableUtil.getErrorInfoFromThrowable(e));
		}
		return result;
	}

	@RequestMapping(value = "/app/updateTboxBySn", method = RequestMethod.POST)
	public boolean updateTboxBySn(@RequestBody Tbox tbox) {
		boolean result = false;
		try {
			String sn = tbox.getSn();
			if(StringUtils.isNotBlank(sn)){
				tboxService.updateTboxBySn(tbox);
				result = true;
			}
		} catch (Exception e) {
			logger.error(ThrowableUtil.getErrorInfoFromThrowable(e));
		}
		return result;
	}

    /**
     * 重置Tbox信息
     *
     * @param sn
     * @return
     */
   /* @RequestMapping(value = "/inactiveTboxStatus", method = RequestMethod.GET)
    public String inactiveTboxStatus(String sn, String user) {
        logger.info("TBox({}) request  inactiveTboxStatus by {}", sn, user);
        if (null == sn) {
            return "sn 不合法！";
        }
        if (null == user || user.length() <= 0) {
            return "用户不合法！";
        }
        try {
            // 重置TBox status
            // 1。检查数据库中是否存在该SN
            TboxVo tboxInfo = getTboxVo(sn);
            if (tboxInfo != null) {
                boolean inActiveResult = resetTboxStatus(sn);
                if (!inActiveResult) {
                    return "error:--modify mysql error!";
                }
            }
            // 2。判断Redis是否有这个sn记录
            String info = redisAPI.getHash(RedisConstant.TBOX_INFO, sn);
            if (info != null) {
                // 有则删除记录
                redisAPI.removeHash(RedisConstant.TBOX_INFO, sn);
            }

        } catch (Exception e) {
            logger.error("{}", ThrowableUtil.getErrorInfoFromThrowable(e));
            return "error:--Failed!";
        }
        return "Modify Success!";
    }*/

}
