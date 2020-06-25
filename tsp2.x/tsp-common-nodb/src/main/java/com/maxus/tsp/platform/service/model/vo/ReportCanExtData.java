/**
 * 
 */
package com.maxus.tsp.platform.service.model.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.maxus.tsp.common.constant.OTARealTimeConstants;
import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.ThrowableUtil;

/**
 * @author lzgea
 *
 */
public class ReportCanExtData extends ReportCan {
	public static final String BASECAN_VERSION = "0017";
	private static Logger logger = LogManager.getLogger(ReportCanExtData.class);
	int ExtDataSize;
	Map<Short, Object> ExtData = new HashMap<>();
	boolean correctAnlysis;

	
	
	public int getExtDataSize() {
		return ExtDataSize;
	}

	public void setExtDataSize(int extDataSize) {
		ExtDataSize = extDataSize;
	}

	public Map<Short, Object> getExtData() {
		return ExtData;
	}

	public void setExtData(Map<Short, Object> extData) {
		ExtData = extData;
	}

	public ReportCanExtData(byte[] baseCan, byte[] extCan, String version) {
		super(baseCan, ReportCanExtData.BASECAN_VERSION);
		correctAnlysis = false;
		if (super.isReportCanDataCorrect()) {
			byte[] _ExtDataSize = new byte[2];//StrAgCnt
			byte[] _ExtData = new byte[extCan.length - 2];//StrAgList
			// 基础解析没问题再继续解析拓展字段；
			try {
				System.arraycopy(extCan, 0, _ExtDataSize, 0, OTARealTimeConstants.REPORT_CARCAN_ADD_EXTDATASIZE_LENGTH);
				System.arraycopy(extCan, 2, _ExtData, 0, extCan.length - 2);

				ExtDataSize = ByteUtil.getUnsignedInt(_ExtDataSize);
				//判断扩展车况报文长度是否正确
				if (ExtDataSize == _ExtData.length) {
					byte[] _id = new byte[2];
					byte[] _numU8 = new byte[1];
					byte[] _numU16 = new byte[2];
					int index = 2;
					int key = 0;
					while (index < ExtDataSize) {
						System.arraycopy(extCan, index, _id, 0, 2);
						key = ByteUtil.getUnsignedInt(_id);
						index += 2;
						if (key == 2) {
							System.arraycopy(extCan, index, _numU16, 0, 2);
							int value = ByteUtil.getUnsignedInt(_numU16);
							index += 2;
							ExtData.put((short) key,  value);
						}else if (key == 6) {
							List<Integer> value = new ArrayList<>();
							System.arraycopy(extCan, index, _numU8, 0, 1);
							int cnt = _numU8[0];
							value.add(cnt);
							index += 1;
							
							for (int i = 0; i < cnt; i++) {
								System.arraycopy(extCan, index, _numU16, 0, 2);
								value.add(ByteUtil.getUnsignedInt(_numU16));
								index += 2;
							}
							ExtData.put((short) key,  value);
							
						}else if(key == 7) {
							List<Integer> value = new ArrayList<>();
							System.arraycopy(extCan, index, _numU8, 0, 1);
							int cnt = _numU8[0];
							value.add(cnt);
							index += 1;
							
							for (int i = 0; i < cnt; i++) {
								System.arraycopy(extCan, index, _numU8, 0, 1);
								value.add(Integer.parseInt(ByteUtil.getUnsignedChar(_numU8[0])));
								index += 1;
							}
							ExtData.put((short) key,  value);
						}else {
							System.arraycopy(extCan, index, _numU8, 0, 1);
							int value = Integer.parseInt(ByteUtil.getUnsignedChar(_numU8[0]));
							index += 1;
							ExtData.put((short) key,  value);
						}
					}
					correctAnlysis = true;
				} else {
					logger.warn("解析ExtData拓展车况长度错误");
				}
			} catch (Exception ex) {
				logger.error("解析ExtData拓展车况出错,拓展车况报文:{}，原因:{}", ByteUtil.byteToHex(extCan),
						ThrowableUtil.getErrorInfoFromThrowable(ex));
			}
		}

	}

	@Override
	public boolean isReportCanDataCorrect() {
		return correctAnlysis & super.isReportCanDataCorrect();
	}
}
