package com.fineos.theme.model;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.SyncStateContract.Constants;

import com.fineos.theme.R;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.Constant.ThemeSwitchKey;

public class CustomData {
	private int customId;
	private String title;
	private int imgId;
	private String type;

	public int getCustomId() {
		return customId;
	}

	public void setCustomId(int customId) {
		this.customId = customId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getImgId() {
		return imgId;
	}
	
	public String getCustomType() {
		return type;
	}

	public void setImgId(int imgId) {
		this.imgId = imgId;
	}

	public final static int CUSTOM_ITEM_ICON = 0;
	public final static int CUSTOM_ITEM_LOCKSCREEN = 1;
	public final static int CUSTOM_ITEM_LOCKSCREEN_WALLPAPER = 2;
	public final static int CUSTOM_ITEM_WALLPAPER = 3;
	public final static int CUSTOM_ITEM_DYNAMIC_WALLPAPER = 4;
	public final static int CUSTOM_ITEM_FONT = 5;
	public final static int CUSTOM_ITEM_RINGTONE = 6;
	public final static int CUSTOM_WALLPAPER = 7;
	public final static int CUSTOM_ITEM_REMINDER_WALLPAPER = 8;
	public final static int CUSTOM_ITEM_LOCAL_THEME = 9;
	

	public static ArrayList<CustomData> loadMixerOnlineIcon(Context mContext) {
		ArrayList<CustomData> mList = new ArrayList<CustomData>();

//		CustomData icon = new CustomData();
//		icon.customId = CUSTOM_ITEM_ICON;
//		icon.title = mContext.getResources().getString(R.string.custom_icon);
//		icon.imgId = R.drawable.ic_icon;
//		mList.add(icon);
		
		// //桌面壁纸
		CustomData wallpaper = new CustomData();
		wallpaper.customId = CUSTOM_WALLPAPER;
		wallpaper.title = mContext.getResources().getString(R.string.wallpaper);
		wallpaper.imgId = R.drawable.ic_lockwallpaper_big;
		mList.add(wallpaper);
		
		// 动态壁纸
		CustomData customDynamicWallpaper = new CustomData();
		customDynamicWallpaper.customId = CUSTOM_ITEM_DYNAMIC_WALLPAPER;
		customDynamicWallpaper.title = mContext.getResources().getString(R.string.custom_dynamic_wallpaper);
		customDynamicWallpaper.imgId = R.drawable.ic_dynamicwallpaper_big;
		mList.add(customDynamicWallpaper);
				
		CustomData lockscreen = new CustomData();
		lockscreen.customId = CUSTOM_ITEM_LOCKSCREEN;
		lockscreen.title = mContext.getResources().getString(R.string.custom_lockscreen);
		lockscreen.imgId = R.drawable.ic_lockscreen_big;
		mList.add(lockscreen);		
		
		// //字体
		if (isThridFontsSwitchOn(mContext)) {
			CustomData fonts = new CustomData();
			fonts.customId = CUSTOM_ITEM_FONT;
			fonts.title = mContext.getResources().getString(R.string.tab_title_font);
			fonts.imgId = R.drawable.ic_fonts_big;
			mList.add(fonts);
		}
		
		// //local
		CustomData localTheme = new CustomData();
		localTheme.customId = CUSTOM_ITEM_LOCAL_THEME;
		localTheme.title = mContext.getResources().getString(R.string.tab_local_theme);
		localTheme.imgId = R.drawable.ic_local_big;
		mList.add(localTheme);

		return mList;
	}
	
	private static String getSwitchValue(Context context, String key) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
		String onlineflag = sharedPreferences.getString(key, Constant.ThemeSwitchKeyDefaultValue.DEFAULT_VALUE);
		return onlineflag;
	}
	
	public static boolean isThridFontsSwitchOn(Context context) {
		String value = getSwitchValue(context, Constant.ThemeSwitchKey.ONLINE_THIRDFONTS);
		if (value == null) {
			return (!Constant.ThemeSwitchKeyDefaultValue.DEFAULT_VALUE.equals(Constant.ThemeSwitchKey.VALUE_OFF));
		}
		return (!value.equals(Constant.ThemeSwitchKey.VALUE_OFF));
	}
	
	public static ArrayList<CustomData> loadMixerOnlineIconFromXml(Context context) {
		ArrayList<CustomData> mList = new ArrayList<CustomData>();
		String[] mixOnlineDescribesStrings = context.getResources().getStringArray(R.array.drawable_center_describe);
		String[] mixOnlineType = context.getResources().getStringArray(R.array.drawable_center_type);
		int count = Math.min(mixOnlineDescribesStrings.length, mixOnlineType.length);
		for (int i = 0; i < count; i++) {
			CustomData cd = new CustomData();
			cd.type = mixOnlineType[i];
			cd.title = mixOnlineDescribesStrings[i];
			mList.add(cd);
		}
		return mList;
	}

	public static ArrayList<CustomData> loadMixerLocalIcon(Context mContext) {
		ArrayList<CustomData> mList = new ArrayList<CustomData>();

//		CustomData icon = new CustomData();
//		icon.customId = CUSTOM_ITEM_ICON;
//		icon.title = mContext.getResources().getString(R.string.custom_icon);
//		icon.imgId = R.drawable.ic_icon_big;
//		mList.add(icon);


		
		// 动态壁纸
		CustomData customDynamicWallpaper = new CustomData();
		customDynamicWallpaper.customId = CUSTOM_ITEM_DYNAMIC_WALLPAPER;
		customDynamicWallpaper.title = mContext.getResources().getString(R.string.custom_dynamic_wallpaper);
		customDynamicWallpaper.imgId = R.drawable.ic_dynamicwallpaper;
		mList.add(customDynamicWallpaper);
		
		// //桌面壁纸
		CustomData wallpaper = new CustomData();
		wallpaper.customId = CUSTOM_ITEM_WALLPAPER;
		wallpaper.title = mContext.getResources().getString(R.string.custom_wallpaper);
		wallpaper.imgId = R.drawable.ic_wallpaper;
		mList.add(wallpaper);

		// //锁屏
		CustomData lockscreen = new CustomData();
		lockscreen.customId = CUSTOM_ITEM_LOCKSCREEN;
		lockscreen.title = mContext.getResources().getString(R.string.custom_lockscreen);
		lockscreen.imgId = R.drawable.ic_lockscreen;
		mList.add(lockscreen);
		

		// //字体
		CustomData fonts = new CustomData();
		fonts.customId = CUSTOM_ITEM_FONT;
		fonts.title = mContext.getResources().getString(R.string.custom_fonts);
		fonts.imgId = R.drawable.ic_fonts;
		mList.add(fonts);

//		// //锁屏壁纸
//		CustomData lockWallpaper = new CustomData();
//		lockWallpaper.customId = CUSTOM_ITEM_LOCKSCREEN_WALLPAPER;
//		lockWallpaper.title = mContext.getResources().getString(R.string.custom_lock_wallpaper);
//		lockWallpaper.imgId = R.drawable.ic_lockwallpaper_big;
//		mList.add(lockWallpaper);
//
//		
//
//		
//
//		// //铃声
//		CustomData customRingtone = new CustomData();
//		customRingtone.customId = CUSTOM_ITEM_RINGTONE;
//		customRingtone.title = mContext.getResources().getString(R.string.custom_ringtone);
//		customRingtone.imgId = R.drawable.ic_ringtone_big;
//		mList.add(customRingtone);

		return mList;
	}
}
