package com.vanchu.libs.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
	
	/**
	 * 获取下一天凌晨的时间戳
	 * @return
	 */
	public static long nextDayTimestamp() {
		long currentTime	= System.currentTimeMillis();
		
		Calendar calendar	= Calendar.getInstance();
		calendar.setTimeInMillis(currentTime);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTimeInMillis();
	}
	
	/**
	 * 时间戳转换成日期时间字符串，格式为2013-07-03 03:22:00
	 * 
	 * @param timestampString
	 * @return
	 */
	public static String timestampToDateStr(long timestamp) {
		String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
		return dateStr;
	}
}
