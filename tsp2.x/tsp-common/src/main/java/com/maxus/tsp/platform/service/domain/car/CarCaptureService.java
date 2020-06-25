package com.maxus.tsp.platform.service.domain.car;

/**
 * 车辆救援信息
 * 
 * @ClassName: Car
 * @Description: 
 * @author 张涛
 * @version V1.0
 * @Date 2017年7月31日 下午4:08:53
 */
public interface CarCaptureService {
	boolean updateCarCaptureStatus(String vin,String shootID,int status);
}
