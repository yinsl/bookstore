package com.maxus.tsp.stress;

import com.maxus.tsp.common.util.ByteUtil;
import com.maxus.tsp.common.util.CRC2Util;
import com.maxus.tsp.common.util.StringUtil;

public class GroupPackage {
	MessageFormat mf = new MessageFormat();

	/**
	 * 设定报文的各个参数
	 */
	private void setPackageParamater(String beginSign, String msgSize, String sn, String encyptMode, String command,
	        String seqNum, String paramSize, String param, String checksum) {
		mf.setBeginSign("2323");
		mf.setMsgSize(msgSize);
		mf.setSn(sn);
		mf.setEncyptMode(encyptMode);
		mf.setCommand(command);
		mf.setSeqNum(seqNum);
		mf.setParamSize(paramSize);
		mf.setParam(param);
		mf.setChecksum(checksum);
	}

	/**
	 * 报文的组装
	 * 
	 * @param mf
	 * @return 报文
	 */
	private String getPackageParamater(MessageFormat mf) {
		String mgs = "";
		String a[] = new String[10];
		StringBuffer sb = new StringBuffer();
		a[0] = mf.getBeginSign();
		a[1] = mf.getMsgSize();
		a[2] = mf.getSn();
		a[3] = mf.getEncyptMode();
		a[4] = mf.getCommand();
		a[5] = mf.getSeqNum();
		a[6] = mf.getParamSize();
		a[7] = mf.getParam();
		a[8] = mf.getChecksum();

		for (int i = 0; i < a.length; i++) {
			if (a[i] != null) {
				sb.append(a[i]);
				// System.out.println(a[i]);
			}

		}

		mgs = sb.toString();

		return mgs;

	}

	/**
	 * 格式化各个参数；得到报文
	 * 
	 * @param sn
	 * @param encyptMode
	 * @param command
	 * @param seqNum
	 * @param param
	 * @return
	 */
	public String getMessagess(String sn, String encyptMode, String command, String seqNum, String param) {
		String mgs = "";
		String beginSign = StringUtil.dataLengthsCheck("2323", 4);
		String msgSize = Integer.toHexString(30 + param.length() / 2);
		msgSize = StringUtil.dataLengthsCheck(msgSize, 4);
		sn = sn.trim();

		if (sn.length() == 15) {
			sn = " " + sn;
		} else {
			sn = StringUtil.dataLengthsCheck(sn, 16);
		}
		sn = ByteUtil.getAscii(sn);
		encyptMode = StringUtil.dataLengthsCheck(encyptMode, 2);
		command = StringUtil.dataLengthsCheck(command, 4);
		seqNum = StringUtil.dataLengthsCheck(seqNum, 8);

		String paramSize = Integer.toHexString(param.length() / 2);
		paramSize = StringUtil.dataLengthsCheck(paramSize, 4);

		String crcStr = command + seqNum + paramSize + param;
		String checksum = CRC2Util.getCRC(crcStr);
		checksum = StringUtil.dataLengthsCheck(checksum, 2);

		setPackageParamater(beginSign, msgSize, sn, encyptMode, command, seqNum, paramSize, param, checksum);
		mgs = getPackageParamater(mf);
		return mgs.toUpperCase();
	}

	/**
	 * 加密后的组包
	 * 
	 * @param sn
	 * @param em
	 * @param etxt
	 * @return
	 */
	public String getEncryptMessage(String sn, String em, String etxt) {

		if (sn.length() == 15) {
			sn = " " + sn;
		} else {
			sn = StringUtil.dataLengthsCheck(sn, 16);
		}
		String sns = ByteUtil.getAscii(sn);

		String a[] = new String[10];
		StringBuffer sb = new StringBuffer();
		a[0] = "2323";
		a[1] = StringUtil.dataLengthsCheck(Integer.toHexString(21 + etxt.length() / 2), 4);
		a[2] = sns;
		a[3] = em;
		a[4] = etxt;

		for (int i = 0; i < a.length; i++) {
			if (a[i] != null) {
				sb.append(a[i]);
				// System.out.println(a[i]);
			}

		}

		String mgs = sb.toString();
		return mgs;

	}
}
