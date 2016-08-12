package com.fineos.theme.utils;

import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.telephony.TelephonyManager;

public class DeviceUtil {
	public static final String CLIENT_ID = "46as4d6f46a4sd6f";

	/*
	 * Get UE model name
	 */
	public static String getDeviceModel() {
		// TODO Auto-generated method stub
		String model = Build.MODEL;

		if (model == null) {
			return "";
		} else {
			return model;
		}
	}

	public static String getUserId(Context context) {
		SharedPreferences settingPreference = context.getSharedPreferences("Report", Context.MODE_PRIVATE);
		String userid = settingPreference.getString("userId", "");
		return userid;
	}

	/*
	 * Get IMEI information
	 */
	public static String getIMEI(Context context) {
		// TODO Auto-generated method stub
		return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
	}

	/*
	 * Get IMSI information
	 */
	public static String getIMSI(Context context) {
		// TODO Auto-generated method stub
		return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId();
	}

	public static String getBuildModel() {
		return android.os.Build.MODEL;
	}

	public static String getBuildVersionRelease() {
		return android.os.Build.VERSION.RELEASE;
	}

	/*
	 * Get UE SDK Version
	 */
	public static String getSDKVersion() {
		// TODO Auto-generated method stub
		return Build.VERSION.SDK;
	}

	/*
	 * Get UE SDK Version as integer value
	 */
	public static int getSDKVersionInt() {
		// TODO Auto-generated method stub
		int nVer = 0;

		try {
			nVer = Integer.parseInt(Build.VERSION.SDK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nVer;
	}

	/**
	 * GET Client Id
	 * 
	 * @return
	 */
	public static String getClientId() {
		StringBuilder builder = new StringBuilder();
		builder.append(System.currentTimeMillis());
		char[] num = new char[32 - builder.length()];
		Random ran = new Random();
		int temp;
		char cur;
		for (int i = 0; i < num.length; i++) {
			temp = ran.nextInt(10);
			cur = (char) ('0' + temp);
			num[i] = cur;
		}
		String tmp = new String(num);
		builder.append(tmp);
		return builder.toString();
	}

	public static String getChannelID(Context context) {
		ApplicationInfo appInfo = null;
		try {
			appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String channelId = appInfo.metaData.getString("ChannelID");

		return channelId;
	}
}