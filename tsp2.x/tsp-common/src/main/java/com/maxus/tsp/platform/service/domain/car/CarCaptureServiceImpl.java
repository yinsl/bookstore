package com.maxus.tsp.platform.service.domain.car;

import com.maxus.tsp.platform.service.dao.car.CarCaptureDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CarCaptureServiceImpl implements CarCaptureService {

	@Autowired
	private CarCaptureDao captureDao;

	public void insertCarCapture(String vin, String creatDate) {
		captureDao.insertCarCapture(vin, creatDate);
	}

	public void insertCarCaptureLog(String vin, String eventDate, int enventType) {
		captureDao.insertCarCaptureLog(vin, eventDate, enventType);
	}
	
	@Override
	public boolean updateCarCaptureStatus(String vin,String shootID,int status)
	{
		try{
			captureDao.updateCarCaptureStatus(vin,shootID,status);
			}
		catch(Exception ex)
		{
			return false;
		}
		return true;
	}
	
	@Transactional(readOnly = true)
	public int getCountForOperingTakePhoto(String vin, String limitTime)
	{
		return captureDao.getCountForOperingTakePhoto(vin, limitTime);
	}
}
