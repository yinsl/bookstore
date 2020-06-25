package com.maxus.tsp.gateway.common.ota;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.maxus.tsp.common.util.ByteUtil;

@Component
public class OtaVersion {
	
	@Value("${rpcServer.otaVersion}")
	private String otaVersion;
	
	private Set<String> supportedVersions = new HashSet<>();
	
	public void setOtaVersion(String otaVersion) {
		this.otaVersion = otaVersion;
		StringUtils.commaDelimitedListToSet(otaVersion).forEach((version)->supportedVersions.add(ByteUtil.versionStringToHex(version)));
	}
	
	@PostConstruct
	public void initVersions() {
		StringUtils.commaDelimitedListToSet(otaVersion).forEach((version)->supportedVersions.add(ByteUtil.versionStringToHex(version)));
	}

	public boolean isVersionSupported(String versionNo) {
		return supportedVersions.contains(versionNo);
	}
	
	public int getSupportedNumber() {
		return supportedVersions.size();
	}

}
