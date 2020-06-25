package com.maxus.tsp.gateway.common.ota;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


public class TimeLogUtil {
	
	static ThreadLocal<List<TimeLog>> swLocal = new ThreadLocal<List<TimeLog>>();
	public static boolean enable = true;
	public static void log(String logName) {
		if(!enable) {
			return;
		}
		if(swLocal.get()==null) {
			swLocal.set(new ArrayList<>());
		}
		swLocal.get().add(new TimeLog((swLocal.get().size()+1)+". "+logName));
	}
	
	static class TimeLog{
		String logName;
		Long time;
		
		public TimeLog(String logName) {
			this.logName=logName;
			this.time = System.nanoTime();
		}

		@Override
		public String toString() {
			return logName;
		}
	}
	
	public static String printLog() {
		if(!enable) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		long allCost =0;
		String n = System.getProperty("line.separator");
		SortedMap<Long,String> sortedMap = new TreeMap<Long,String>(Comparator.reverseOrder());
		for(int i=0;i<swLocal.get().size();i++) {
			TimeLog currentLog = swLocal.get().get(i);
			sb.append(currentLog);
			if(i+1 < swLocal.get().size()) {
				TimeLog nextLog = swLocal.get().get(i+1);
				long cost = nextLog.time-currentLog.time;
				allCost+=cost;
				sortedMap.put(cost,nextLog.logName);
				sb.append(n+"cost: ").append(formatNum(cost)).append(n);
			}
		}
		sb.append(n+"All cost: ").append(formatNum(allCost));
		sb.append(n+"Top 10:");
		int order = 1;
		for(Long num:sortedMap.keySet()) {
			sb.append(n+order+": "+formatNum(num)+"--"+sortedMap.get(num));
			order++;
			if (order >10){
				break;
			}
		}
		swLocal.set(new ArrayList<>());
		return sb.toString();
	}
	
	private static String formatNum(Long num) {
		StringBuffer ac = new StringBuffer(Long.toString(num));
		if(num > 1000000) {
			ac.insert(0, "【").insert(ac.length()-6, ",").append(" ms").append("】");
		}
		return ac.toString();
	}

}
