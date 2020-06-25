package com.maxus.tsp.platform.service.domain.car;

import com.maxus.tsp.common.util.ThrowableUtil;
import com.maxus.tsp.platform.service.dao.car.RedisBackUpDao;
import com.maxus.tsp.platform.service.model.car.RedisTboxInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * 
 * @author guhaowei
 *
 */
@Service
public class RedisBackUpService {

	public final Logger logger = LogManager.getLogger(getClass());
	//无记录
	final int haveNoRecord = 0;
	@Autowired
	private RedisBackUpDao redisBackupMapper;
	
	//操作数据库表格(更新或者插入数据)
	//rds_car_remote_ctrl_resp
	//获取对应记录
	@Transactional (readOnly = true)
	public String getValueIndb_rds_car_remote_ctrl_resp(String tboxSn) {
		try {
			//查询是否存在记录
			if (redisBackupMapper.checkTboxInfoIndb_rds_car_remote_ctrl_resp(tboxSn) == haveNoRecord) {
				//没有记录则返回null
			return null;
			} else {
				RedisTboxInfo rdsMap = redisBackupMapper.getRecordIndb_rds_car_remote_ctrl_resp(tboxSn);
				return rdsMap.getValue();
			}
		} catch (Exception e) {
			logger.error("Tbox(sn: {})获取rds_car_remote_ctrl_resp表格记录出错，错误原因：{}",
					tboxSn, ThrowableUtil.getErrorInfoFromThrowable(e));
			return null;
		}
	}

	
	//操作数据库表格(更新或者插入数据)
	@Transactional
	public boolean updateRecordIndb_rds_car_remote_ctrl_resp(String tboxSn, String value) {
		try {
			//查询是否存在记录
			if (redisBackupMapper.checkTboxInfoIndb_rds_car_remote_ctrl_resp(tboxSn) == haveNoRecord) {
				//没有记录则插入记录
				return redisBackupMapper.insertTboxInfodb_rds_car_remote_ctrl_resp(tboxSn, value);
			} else {
				//记录存在则更新数据
				return redisBackupMapper.updateTboxInfodb_rds_car_remote_ctrl_resp(tboxSn, value);
			}
		} catch(Exception e) {
			logger.error("Tbox(sn: {})更新rds_car_remote_ctrl_resp表格记录出错，错误原因：{}",
					tboxSn, ThrowableUtil.getErrorInfoFromThrowable(e));
			return false;
		}
	}
	
	//删除数据库表格(查询并删除数据)
	@Transactional
	public boolean deleteRecordIndb_rds_car_remote_ctrl_resp(String tboxSn) {
		try {
			//查询记录是否存在
			if (redisBackupMapper.checkTboxInfoIndb_rds_car_remote_ctrl_resp(tboxSn) == haveNoRecord) {
				//没有记录则无法删除
				return false;
			} else {
				//删除记录
				return redisBackupMapper.deleteTboxInfodb_rds_car_remote_ctrl_resp(tboxSn);
			}
		} catch(Exception e) {
			logger.error("Tbox(sn: {})删除“rds_car_remote_ctrl_resp”表格记录出错，错误原因：{}",
					tboxSn, ThrowableUtil.getErrorInfoFromThrowable(e));
			return false;
			}
	}
	
	//操作数据库表格(更新或者插入数据)
	//rds_online_tbox
	@Transactional (readOnly = true)
	public String getValueIndb_rds_online_tbox(String tboxSn) {
		try {
			//查询是否存在记录
			if (redisBackupMapper.checkTboxInfoIndb_rds_online_tbox(tboxSn) == haveNoRecord) {
				//没有记录则返回null
			return null;
			} else {
				RedisTboxInfo rdsMap = redisBackupMapper.getRecordIndb_rds_online_tbox(tboxSn);
				return rdsMap.getValue();
			}
		} catch (Exception e) {
			logger.error("Tbox(sn: {})获取rds_online_tbox表格记录出错，错误原因：{}",
					tboxSn, ThrowableUtil.getErrorInfoFromThrowable(e));
			return null;
		}
	}
	@Transactional
	public boolean updateRecordIndb_rds_online_tbox(String tboxSn, String value) {
		try {
			//查询是否存在记录
			if (redisBackupMapper.checkTboxInfoIndb_rds_online_tbox(tboxSn) == haveNoRecord) {
				//没有记录则插入记录
				return redisBackupMapper.insertTboxInfodb_rds_online_tbox(tboxSn, value);
			} else {
				//记录存在则更新数据
				return redisBackupMapper.updateTboxInfodb_rds_online_tbox(tboxSn, value);
			}
		} catch(Exception e) {
			logger.error("Tbox(sn: {})更新rds_online_tbox表格记录出错，错误原因：{}",
					tboxSn, ThrowableUtil.getErrorInfoFromThrowable(e));
			return false;
		}
	}
	
	//删除数据库表格(查询并删除数据)
	@Transactional
	public boolean deleteRecordIndb_rds_online_tbox(String tboxSn) {
		try {
			//查询记录是否存在
			if (redisBackupMapper.checkTboxInfoIndb_rds_online_tbox(tboxSn) == haveNoRecord) {
				//没有记录则无法删除
				return false;
			} else {
				//删除记录
				return redisBackupMapper.deleteTboxInfodb_rds_online_tbox(tboxSn);
			}
		} catch(Exception e) {
			logger.error("Tbox(sn: {})删除“rds_online_tbox”表格记录出错，错误原因：{}",
					tboxSn, ThrowableUtil.getErrorInfoFromThrowable(e));
			return false;
			}
	}
	
	//操作数据库表格(更新或者插入数据)
	//rds_tbox_channel
	//获取对应记录
	@Transactional (readOnly = true)
	public String getValueIndb_rds_tbox_channel(String tboxSn) {
		try {
			//查询是否存在记录
			if (redisBackupMapper.checkTboxInfoIndb_rds_tbox_channel(tboxSn) == haveNoRecord) {
				//没有记录则返回null
			return null;
			} else {
				RedisTboxInfo rdsMap = redisBackupMapper.getRecordIndb_rds_tbox_channel(tboxSn);
				return rdsMap.getValue();
			}
		} catch (Exception e) {
			logger.error("Tbox(sn: {})获取rds_tbox_channel表格记录出错，错误原因：{}",
					tboxSn, ThrowableUtil.getErrorInfoFromThrowable(e));
			return null;
		}
	}
	@Transactional
	public boolean updateRecordIndb_rds_tbox_channel(String tboxSn, String value) {
		try {
			//查询是否存在记录
			if (redisBackupMapper.checkTboxInfoIndb_rds_tbox_channel(tboxSn) == haveNoRecord) {
				//没有记录则插入记录
				return redisBackupMapper.insertTboxInfodb_rds_tbox_channel(tboxSn, value);
			} else {
				//记录存在则更新数据
				return redisBackupMapper.updateTboxInfodb_rds_tbox_channel(tboxSn, value);
			}
		} catch(Exception e) {
			logger.error("Tbox(sn: {})更新rds_tbox_channel表格记录出错，错误原因：{}",
					tboxSn, ThrowableUtil.getErrorInfoFromThrowable(e));
			return false;
		}
	}
	
	//删除数据库表格(查询并删除数据)
	@Transactional
	public boolean deleteRecordIndb_rds_tbox_channel(String tboxSn) {
		try {
			//查询记录是否存在
			if (redisBackupMapper.checkTboxInfoIndb_rds_tbox_channel(tboxSn) == haveNoRecord) {
				//没有记录则无法删除
				return false;
			} else {
				//删除记录
				return redisBackupMapper.deleteTboxInfodb_rds_tbox_channel(tboxSn);
			}
		} catch(Exception e) {
			logger.error("Tbox(sn: {})删除“rds_tbox_channel”表格记录出错，错误原因：{}",
					tboxSn, ThrowableUtil.getErrorInfoFromThrowable(e));
			return false;
			}
	}

	//删除数据库表格(查询并删除数据)
	//rds_tbox_logout_time
	//获取对应记录
	@Transactional (readOnly = true)
	public String getValueIndb_rds_tbox_logout_time(String tboxSn) {
		try {
			//查询是否存在记录
			if (redisBackupMapper.checkTboxInfoIndb_rds_tbox_logout_time(tboxSn) == haveNoRecord) {
				//没有记录则返回null
			return null;
			} else {
				RedisTboxInfo rdsMap = redisBackupMapper.getRecordIndb_rds_tbox_logout_time(tboxSn);
				return rdsMap.getValue();
			}
		} catch (Exception e) {
			logger.error("Tbox(sn: {})获取rds_tbox_logout_time表格记录出错，错误原因：{}",
					tboxSn, ThrowableUtil.getErrorInfoFromThrowable(e));
			return null;
		}
	}
	@Transactional
	public boolean updateRecordIndb_rds_tbox_logout_time(String tboxSn, String value) {
		try {
			//查询记录是否存在
			if (redisBackupMapper.checkTboxInfoIndb_rds_tbox_logout_time(tboxSn) == haveNoRecord) {
				//没有记录则插入记录
				return redisBackupMapper.insertTboxInfodb_rds_tbox_logout_time(tboxSn, value);
			} else {
				//更新记录
				return redisBackupMapper.updateTboxInfodb_rds_tbox_logout_time(tboxSn, value);
			}
		} catch(Exception e) {
			logger.error("Tbox(sn: {})更新“rds_tbox_logout_time”表格记录出错，错误原因：{}",
					tboxSn, ThrowableUtil.getErrorInfoFromThrowable(e));
			return false;
			}
	}
	
}
