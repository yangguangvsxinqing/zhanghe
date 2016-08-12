package com.fineos.theme.utils;

import java.io.File;

import com.baidu.kirin.StatUpdateAgent;

import android.os.Environment;

public class Constant {

	public static final int THEME_LOCAL_LIST_TYPE = 0;
	public static final int THEME_ONLINE_LIST_TYPE = 1;
	
	public static final int THEME_LIST_TYPE = 0;
	public static final int THEME_LOCKSCREEN_TYPE = 1;
	
	public static final int THEME_FONT_TYPE = 3;
	public static final int THEME_FONT_ICON = 4;
	public static final int ONLINE_THEME_WALLPAPER_TYPE = 5;
	public static final int THEME_DYNAMIC_WALLPAPER_TYPE = 2;
	public static final int THEME_WALLPAPER_TYPE = 0;
	public static final int THEME_LOCKSCREEN_WALLPAPER_TYPE = 1;
	public static final int THEME_CACHE_MAX_SIZE = 80; // cache Max Size is 80M, if Cache Size > 80M file will be clean, when the application relaunch.
	
	
	public static final String THEME_AD_APPID = "1104880933";
	/** Request */
	/** Request����״̬ */
	public static final int STATUS_PENDING = 0x30000;
	public static final int STATUS_FAIL = 0x30001;
	public static final int STATUS_SUCCESS = 0x30002;
	public static final int STATUS_CANCELED = 0x30003;
	public static final int STATUS_ERROR = 0x30004;

	public static final int THEME_LIST_COUNT_PER_TIME = 30;
	public static final int WALLPAPER_LIST_COUNT_PER_TIME = 20;

	public static final int TYPE_POST_VERSION_REGISTER = 268501006;
	public static final int TYPE_THEME_LIST = 268501007;
	public static final int TYPE_THEME_ICON = 268501008;
	public static final int TYPE_LOCAL_THEME_PRIVIEW = 268501009;
	public static final int TYPE_ICON_LIST = 268501010;
	public static final int TYPE_ICON_IMG = 268501011;
	public static final int TYPE_FONTS_LIST = 268501012;
	public static final int TYPE_FONTS_IMG = 268501013;
	public static final int TYPE_RINGTONE_LOCAL_LIST = 268501014;

	public static final int TYPE_RINGTONE_DOWNLOAD_LIST = 268501015;

	public static final int TYPE_THEME_PREVIEW = 268501016;

	public static final int TYPE_IMAGE_LIST = 268501017;
	public static final int TYPE_IMAGE_ICON = 268501018;
	public static final int TYPE_RINGTONE_LIST = 268501019;
	public static final int TYPE_WALLPAPER_PREVIEW = 268501020;

	public static final int TYPE_THEME_VERIFY = 268501021;

	public static final int TYPE_RINGTONE_SDCARD_LIST = 268501022;

	public static final int TYPE_LOCAL_ICON_IMG = 268501023;

	public static final int TYPE_ONLINE_THEME_LIST = 268501024;
	public static final int TYPE_ONLINE_THEME_ICON = 268501025;
	
	public static final int TYPE_ONLINE_THEME_AD = 268501026;
	public static final int TYPE_ONLINE_THEME_AD_IMG = 268501027;
	
	public static final int TYPE_SPLASH_AD_ID = 268501028;

	public static final int TYPE_START_GOOGLE_BILLING = 268501029;
	
	public static final int TYPE_GET_PRICE = 268501030;
	
	public static final int TYPE_WRITE_THEME_DATA = 268501031;
	
	public static final String THEME_PATH = "/FineOS/.theme/.themes";
	public static final String THEME_CLING = "theme_cling";
	public static final String DEFAULT_THEME_PATH = Environment.getExternalStorageDirectory() + THEME_PATH;
	public static final String DEFAULT_LIVEWALLPAPER_THEME_PATH = Environment.getExternalStorageDirectory() + "/FineOS/.theme/";
	public static final String CACHE_DIR = DEFAULT_THEME_PATH + "/.cache";
	public static final String SYSTEM_THEME_PATH = "/system/media/theme";
	public static final String SYSTEM_WALLPAPER_PATH = "/system/media/theme/wallpaper";
	public static final String DEFAULT_CACHE_DIR = DEFAULT_THEME_PATH + "/.cache";
	public static final String DATA_THEME_PATH = "/data/system/theme";
	public static final String SYSTEM_FONT_PATH = "/data/fonts";
	public static final String RINGTONES_PATH = DATA_THEME_PATH + File.separator + "ringtones";
	public static final String DEFAULT_SYSTEM_THEME = SYSTEM_THEME_PATH + "/default_fineos.ftz";
	public static final String SELF_PACKAGE_NAME = "com.fineos.theme";
	public static final String ACTION_THEME_APPLIED = "com.android.server.ThemeManager.action.THEME_APPLIED";
	public static final String ACTION_THEME_NOT_APPLIED = "com.android.server.ThemeManager.action.THEME_NOT_APPLIED";
	public static final String ACTION_DATACHANGE = "com.fineos.data.change";
	public static final String ACTION_INFLATE_LIST = "com.fineos.inflatelist";
	
	public static final String TAG_ONLINE_THEME_LIST = "themelist";
	public static final String TAG_ONLINE_THEME_AD = "themeadlist";
	public static final String TAG_ONLINE_THEME_LIKE = "themelike";
	public static final String TAG_ONLINE_THEME_ICON = "themeicon";
	public static final String TAG_ONLINE_THEME_PREVIEW = "themepreview";
	
	public static final class ThemeSwitchKey {		
		public static final String VALUE_ON = "on";
		public static final String VALUE_OFF = "off";
		
		public static final String ONLINE_FONTS = "online_fonts";  // value is on or off
		public static final String ONLINE_THIRDFONTS = "online_thirdfonts";  // value is on or off

		public static final String ONLINE_THIRDLOCKSCREEN = "online_lockscreen"; // value is on or off
		
	}
	
	public static final class Theme_Network {		
		public static final int TYPE_TEST = 0;
		public static final int TYPE_DOMESTIC = 1;
		public static final int TYPE_ABROAD_OROTHER = 2;
	}
	
	public static final class ThemeSwitchKeyDefaultValue {
		public static final String DEFAULT_VALUE = ThemeSwitchKey.VALUE_ON;
		public static final String ONLINE_FONTS = DEFAULT_VALUE;
		public static final String ONLINE_THIRDFONTS = DEFAULT_VALUE;
		public static final String ONLINE_THIRDLOCKSCREEN = DEFAULT_VALUE;		
	}
	
}