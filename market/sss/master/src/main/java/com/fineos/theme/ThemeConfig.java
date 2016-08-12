package com.fineos.theme;

import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.ThemeLog;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.util.Log;


public class ThemeConfig {
	
	private static ThemeConfig mInstance;
	private static Context mContext;
	private static final String UMENG_TEST_APP_KEY = "55da83a3e0f55a5bb60054f1";
	private static final String UMENG_TEST_MESSAGE_SECRET = "1158fd71858c663f15271517824a564a";
	
	private static final String UMENG_OFFICIAL_APP_KEY = "55e3b125e0f55a478c0057b8";
	private static final String UMENG_OFFICIAL_MESSAGE_SECRET = "8e7b3135906c0598207465811035bfbf";
	
	private static final String BAIDU_TEST_APP_KEY = "bad23c50c3";
	private static final String BAIDU_OFFICIAL_APP_KEY = "7943987bb2";
	
	private static final String DOMESTIC_ROOT_URI = "http://theme.fineos.cn:8080/fineos-theme-api";
	private static final String NETWORK_ROOT_URL = "http://theme.enjoyui.com:8080/fineos-theme-api";
        //国内测试
	//private static final String TEST_ROOT_URL = "http://weather.huaqin.com:8080/fineos-theme-api";
	//海外测试
	public static final String TEST_ROOT_URL = "http://rom.enjoyui.com:8080/fineos-theme-api-test";
	private static final String TAG = "ThemeConfig";
	
	private boolean isOffcial = false;
	private String mRootUri = DOMESTIC_ROOT_URI;
	private String mUmengChannel = "FineOS";
	private String mROMCenterAndOtherChannel = "FineOS";
	private String mUmengKey = UMENG_TEST_APP_KEY;
	private String mUmengMessageSecret = UMENG_TEST_MESSAGE_SECRET;
	private String mBaiduAppKey = BAIDU_TEST_APP_KEY;
	
	private ThemeConfig() {
		init();
	}
	
	private void init() {
		int isOffcialId = mContext.getResources().getIdentifier("fineostheme_offcial", "bool", "com.fineos");
		   if(isOffcialId <= 0){
			   isOffcialId = com.fineos.internal.R.bool.fineostheme_offcial;
		}
		isOffcial = mContext.getResources().getBoolean(com.fineos.internal.R.bool.fineostheme_offcial);
		isOffcial = false;
		if (isOffcial) {
			mUmengKey = UMENG_OFFICIAL_APP_KEY;
			mUmengMessageSecret = UMENG_OFFICIAL_MESSAGE_SECRET;
			mBaiduAppKey = BAIDU_OFFICIAL_APP_KEY;
	        int uriId = mContext.getResources().getIdentifier("fineostheme_root_uri", "string", "com.fineos");
	        ThemeLog.v(TAG, "sss uriId="+uriId);
			   if(uriId <= 0){
				   uriId = com.fineos.internal.R.string.fineostheme_root_uri;
			}
			   ThemeLog.v(TAG, "sss imageId2="+uriId);
			mRootUri = mContext.getResources().getString(uriId);
		} else {
			mUmengKey = UMENG_TEST_APP_KEY;
			mUmengMessageSecret = UMENG_TEST_MESSAGE_SECRET;
			mBaiduAppKey = BAIDU_TEST_APP_KEY;
		}
		int UmengChannelId = mContext.getResources().getIdentifier("fineostheme_channel_info", "string", "com.fineos");
		   if(UmengChannelId <= 0){
			   UmengChannelId = com.fineos.internal.R.string.fineostheme_channel_info;
		}
		mUmengChannel = mContext.getResources().getString(UmengChannelId);
		int ROMCenterId = mContext.getResources().getIdentifier("fineostheme_otherchannel_info", "string", "com.fineos");
		   if(ROMCenterId <= 0){
			   ROMCenterId = com.fineos.internal.R.string.fineostheme_otherchannel_info;
		}
		mROMCenterAndOtherChannel = mContext.getResources().getString(ROMCenterId);
		ThemeLog.w(TAG, "isOffcial :" + isOffcial + "mRootUri :" + mRootUri + "mUmengChannel :" + mUmengChannel + "  mROMCenterAndOtherChannel :" + mROMCenterAndOtherChannel);
	}
	
	public static void setContext(Context context) {
		mContext = context;
	}
	
	public int getRomCenterType() {
		int type = Constant.Theme_Network.TYPE_TEST;
		if (isOffcial) {
			if (mRootUri.equals(DOMESTIC_ROOT_URI)) {
				type = Constant.Theme_Network.TYPE_DOMESTIC;
			} else if (mRootUri.equals(TEST_ROOT_URL)) {
				type = Constant.Theme_Network.TYPE_TEST;
			} else {
				type = Constant.Theme_Network.TYPE_ABROAD_OROTHER;
			}
		}
		ThemeLog.w(TAG, "RomType :" + type);
		return type;
	}
	
	public static ThemeConfig getInstance() {
		if (mInstance == null) {
			mInstance = new ThemeConfig();
		}
		return mInstance;
	}
	
	public String getRootUri() {
		ThemeLog.w(TAG, "getRootUri() mRootUri : " + mRootUri);
		return mRootUri;
	}
	
	public String getUmengChannel() {
		ThemeLog.w(TAG, "getUmengChannel :  " + mUmengChannel);
		return mUmengChannel;
	}
	
	public String getUmengKey() {
		ThemeLog.w(TAG, "mUmengKey :  " + mUmengKey);
		return mUmengKey;
	}
	
	public String getUmengMessageSecret() {
		ThemeLog.w(TAG, "getUmengMessageSecret() mUmengMessageSecret : " + mUmengMessageSecret);
		return mUmengMessageSecret;
	}
	
	public String getBaiduAppKey() {
		return mBaiduAppKey;
	}
	
	public String getROMCenterAndOtherChannel() {
		return mROMCenterAndOtherChannel;
	}
	
}
