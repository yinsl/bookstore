package com.maxus.tsp.stress.msgs;

import com.maxus.tsp.common.util.StringUtil;
import com.maxus.tsp.stress.BusinessSuper;
import com.maxus.tsp.stress.GroupPackage;
import com.maxus.tsp.stress.TboxCommand;

public class LogoutMsg extends BusinessSuper {

	public String getLogoutPackage(String sn, String encyptMode, 
			String seqNum){
		String command = TboxCommand.CMD_UP_LOGOUT.getCode();
		GroupPackage gp = new GroupPackage();
		String param = StringUtil.getHexDateString();
		String result = gp.getMessagess(sn, encyptMode, command, seqNum, param);
		return result;		
		
	}

	@Override
	public String resolveMessage(String message) {
		String result;
		if(message.length() == 14){
			//上行
			result = StringUtil.hextoDateString(message);
			result = StringUtil.logFormat("时间", result);
		}else{
			//下行
			
			if(("1").equals(message)){
				result = StringUtil.logFormat("登出", "失败");
			}else{
				result = StringUtil.logFormat("登出", "成功");
			}
			
			
		}
		return result;
	}
	

}
