package com.maxus.tsp.platform.service.dao.car;

import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.maxus.tsp.platform.service.model.car.RedisTboxInfo;

public interface RedisBackUpDao {

	//表格1.rds_car_remote_ctrl_resp
	// 查询Redis数据
	public int checkTboxInfoIndb_rds_car_remote_ctrl_resp(@Param("tboxSn")String tboxSn);

	// 根据Redis更新数据库对应表格数据
	public boolean insertTboxInfodb_rds_car_remote_ctrl_resp(@Param("tboxSn")String tboxSn, @Param("value")String value);

	//根据Redis更新数据库对应表格数据
	public boolean updateTboxInfodb_rds_car_remote_ctrl_resp(@Param("tboxSn")String tboxSn, @Param("value")String value);

	// 删除Redis数据
	public boolean deleteTboxInfodb_rds_car_remote_ctrl_resp(@Param("tboxSn")String tboxSn);
	
	//根据sn获取value数据
	public RedisTboxInfo getRecordIndb_rds_car_remote_ctrl_resp(@Param("tboxSn")String tboxSn);
	
	//获取表格取全部数据
	public RedisTboxInfo[] getAllRecordsIndb_rds_car_remote_ctrl_resp();
	
	//插入所有数据
	public boolean insertAllRecordsIndb_rds_car_remote_ctrl_resp(@Param("currentrecords") Map<String,String> currentrecords);
	
	//表格2.rds_online_tbox
	// 查询Redis数据
	public int checkTboxInfoIndb_rds_online_tbox(@Param("tboxSn")String tboxSn);

	// 根据Redis更新数据库对应表格数据
	public boolean insertTboxInfodb_rds_online_tbox(@Param("tboxSn")String tboxSn, @Param("value")String value);

	//根据Redis更新数据库对应表格数据
	public boolean updateTboxInfodb_rds_online_tbox(@Param("tboxSn")String tboxSn, @Param("value")String value);

	// 删除Redis数据
	public boolean deleteTboxInfodb_rds_online_tbox(@Param("tboxSn")String tboxSn);
	
	//获取value数据
	public RedisTboxInfo getRecordIndb_rds_online_tbox(@Param("tboxSn")String tboxSn);
	
	//插入所有数据
	public boolean insertAllRecordsIndb_rds_online_tbox(@Param("currentrecords") Map<String,String> currentrecords);

	//表格3.rds_tbox_channel
	// 查询Redis数据
	public int checkTboxInfoIndb_rds_tbox_channel(@Param("tboxSn")String tboxSn);

	// 根据Redis更新数据库对应表格数据
	public boolean insertTboxInfodb_rds_tbox_channel(@Param("tboxSn")String tboxSn, @Param("value")String value);

	//根据Redis更新数据库对应表格数据
	public boolean updateTboxInfodb_rds_tbox_channel(@Param("tboxSn")String tboxSn, @Param("value")String value);

	// 删除Redis数据
	public boolean deleteTboxInfodb_rds_tbox_channel(@Param("tboxSn")String tboxSn);
	
	//获取value数据
	public RedisTboxInfo getRecordIndb_rds_tbox_channel(@Param("tboxSn")String tboxSn);
	

	//表格4.rds_tbox_logout_time
	// 查询Redis数据
	public int checkTboxInfoIndb_rds_tbox_logout_time(@Param("tboxSn")String tboxSn);
	
	//插入Redis数据
	public boolean insertTboxInfodb_rds_tbox_logout_time(@Param("tboxSn")String tboxSn, @Param("value")String value);
	
	// 更新Redis数据
	public boolean updateTboxInfodb_rds_tbox_logout_time(@Param("tboxSn")String tboxSn, @Param("value")String value);
	
	//获取value数据
	public RedisTboxInfo getRecordIndb_rds_tbox_logout_time(@Param("tboxSn")String tboxSn);

}
