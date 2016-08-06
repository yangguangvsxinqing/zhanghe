package com.android.systemui.statusbar.fifthlock;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName:TimeUtil
 * @Description:时间工具类，用于获取本地时间、格式化时间等逻辑{modify for [HQ_BOWAY_KEYGUARD]}
 * @author:BingWu.Lee
 * @date:2013-12-31
 */
public final class TimeUtil {

	/** 时间格式 HH:mm */
	public static final String STR_FORMAT_DATE_TIME = "HH:mm";
	/** 时间格式 MM.dd */
	public static final String STR_FORMAT_DATE = "yyyy M d E";

	/**
	 * <p>
	 * Description:根据给定字符串格式获取本地时间
	 * <p>
	 * 
	 * @date:2013-12-31
	 * @param strDesTimeFormat 时间格式，如"yyyy.MM.dd"
	 * @return 指定格式的本地时间
	 */
	public static String getLocalTime(String strDesTimeFormat) {
		if (StringUtil.isEmpty(strDesTimeFormat)) {
			return null;
		}
		Date date = new Date();
		SimpleDateFormat sdfDateFormat = new SimpleDateFormat(strDesTimeFormat);
		return sdfDateFormat.format(date);
	}

	/**
	 * <p>
	 * Description:根据指定时间格式解析出日期
	 * <p>
	 * 
	 * @date:2014-1-4
	 * @param strDate String类型的日期信息,如"2012-2-27 09:12:32"
	 * @param strFormatType 待解析的日期格式 如："yyyy-MM-dd HH:mm:ss"
	 * @return 日期信息
	 * @throws ParseException 解析异常，两者格式不符抛出异常
	 */
	public static Date getDateByStrDate(String strDate, String strFormatType)
			throws ParseException {
		if (StringUtil.isEmpty(strDate) || StringUtil.isEmpty(strFormatType)) {
			return null;
		}

		if (!checkTimeFormatType(strDate, strFormatType)) {
			return null;
		}
		SimpleDateFormat sdfDateFormat = new SimpleDateFormat(strFormatType);
		return sdfDateFormat.parse(strDate);
	}

	/**
	 * 
	 * <p>
	 * Description: 判断时间是否为指定格式
	 * <p>
	 * 
	 * @date 2012-4-19
	 * @param strDate 时间串
	 * @param strTimeType 待识别时间格式
	 * @return 格式相符返回true，错误或异常返回false
	 */
	public static boolean checkTimeFormatType(String strDate, String strTimeType) {
		if (StringUtil.isEmpty(strDate) || StringUtil.isEmpty(strTimeType)) {
			return false;
		}

		@SuppressWarnings("unused")
		Date date = null;
		DateFormat dateFormat = null;
		try {
			dateFormat = new SimpleDateFormat(strTimeType);
			date = dateFormat.parse(strDate);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 不允许创建实例，隐藏构造函数
	 */
	private TimeUtil() {

	}
}
