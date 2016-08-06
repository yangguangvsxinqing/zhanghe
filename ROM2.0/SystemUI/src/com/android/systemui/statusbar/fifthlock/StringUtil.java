package com.android.systemui.statusbar.fifthlock;

/**
 * @ClassName:StringUtil
 * @Description:字符串工具类，用于字符串常用操作逻辑处理{modify for [HQ_BOWAY_KEYGUARD]}
 * @author:BingWu.Lee
 * @date:2013-12-31
 */
public final class StringUtil {

	/**
	 * <p>
	 * Description:判断字符串是否为空
	 * <p>
	 * 
	 * @date:2013-12-31
	 * @param strTarget 目标字符串
	 * @return true:表示该字符串为空;false:表示字符串不空
	 */
	public static boolean isEmpty(String strTarget) {
		return ((null == strTarget) || (0 == strTarget.length()));
	}

	/**
	 * 隐藏构造方法
	 */
	private StringUtil() {

	}
}
