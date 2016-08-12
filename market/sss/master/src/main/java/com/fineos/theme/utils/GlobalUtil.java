package com.fineos.theme.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.Secure;

public class GlobalUtil {

	public static boolean bAutoCheckUpdate;
	public static boolean bAutoCreateShortcut;

	/*
	 * Check system setting for whether allow to install 3rd party applications
	 */
	public static boolean allowInstallNonMarketApps(Context context) {
		// TODO Auto-generated method stub
		ContentResolver cr = context.getContentResolver();

		if (Secure.getInt(cr, Secure.INSTALL_NON_MARKET_APPS, 0) == 0) {
			return false;
		} else {
			return true;
		}
	}

	/*
	 * Get Date string with raw Date by specified format
	 */
	public static String getDateByFormat(Date date, String format) {
		// TODO Auto-generated method stub
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);

		return simpleDateFormat.format(date);
	}

	public static boolean checkNetworkState(Context context) {
		// TODO Auto-generated method stub
		ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectMgr == null) {
			return false;
		}

		NetworkInfo nwInfo = connectMgr.getActiveNetworkInfo();

		if (nwInfo == null || !nwInfo.isAvailable()) {
			return false;
		}
		return true;
	}
}