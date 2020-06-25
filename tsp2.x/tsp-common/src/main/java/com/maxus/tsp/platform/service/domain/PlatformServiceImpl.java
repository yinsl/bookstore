package com.maxus.tsp.platform.service.domain;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maxus.tsp.platform.service.dao.PlatformDao;
import com.maxus.tsp.platform.service.model.Platform;

@Service
@Transactional
public class PlatformServiceImpl implements PlatformService{
	@Autowired
	private PlatformDao platformDao;

	@Override
	public List<Platform> platformAll() {
		return platformDao.platformAll();
	}

}
