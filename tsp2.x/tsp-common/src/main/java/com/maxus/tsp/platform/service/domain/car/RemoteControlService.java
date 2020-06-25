package com.maxus.tsp.platform.service.domain.car;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.maxus.tsp.platform.service.dao.car.RemoteControlDao;
import com.maxus.tsp.platform.service.model.vo.RemoteControlHistoryVo;

/**
 * @ClassName: RemoteControlService.java
 * @Description: 车辆基本服务
 * @author 赵伟阳
 * @version V1.0
 * @Date 2017年7月6日 下午4:11:41
 */
@Service
@Transactional
public class RemoteControlService {
	

	@Autowired
	private RemoteControlDao remoteCtrlMapper;

	public int recordRemoteCtrl(String vin,String cmd,String cmdVal)
	{
		RemoteControlHistoryVo remoteCtrlVo = new RemoteControlHistoryVo();
		remoteCtrlVo.setVin(vin);
		remoteCtrlVo.setCmd(cmd);
		remoteCtrlVo.setCmdVal(cmdVal);
		remoteCtrlMapper.recordRemoteCtrl(remoteCtrlVo);
		return remoteCtrlVo.getId();
	}
	
	
	public boolean addRemoteCtrlDownStatus(int remoteCtrlID,String downDate)
	{
		remoteCtrlMapper.addRemoteCtrlDownStatus(remoteCtrlID, downDate);
		return true;
	}
	
	@Transactional(readOnly = true)
	public int getCountForOperRmtCtrl(String vin, String limitTime) {
		return remoteCtrlMapper.getCountForOperRmtCtrl(vin, limitTime);
	}
}
