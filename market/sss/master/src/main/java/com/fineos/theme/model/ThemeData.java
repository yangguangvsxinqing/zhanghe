package com.fineos.theme.model;

import java.io.Serializable;

import android.content.Context;

import com.fineos.android.rom.sdk.bean.Theme;
import com.fineos.theme.download.Downloads;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.utils.Util;

public class ThemeData implements Serializable {

	private static final long serialVersionUID = -2387824253271681301L;

	public static final int THEME_ELEMENT_TYPE_ICONS = 0;
	public static final int THEME_ELEMENT_TYPE_SYSTEMUI = 1;
	public static final int THEME_ELEMENT_TYPE_FRAMEWORK = 2;
	public static final int THEME_ELEMENT_TYPE_CONTACTS = 3;
	public static final int THEME_ELEMENT_TYPE_DIALER = 4;
	public static final int THEME_ELEMENT_TYPE_MMS = 5;
	public static final int THEME_ELEMENT_TYPE_WALLPAPER = 6;
	public static final int THEME_ELEMENT_TYPE_LOCK_WALLPAPER = 7;
	public static final int THEME_ELEMENT_TYPE_RINGTONES = 8;
	public static final int THEME_ELEMENT_TYPE_BOOTANIMATION = 9;
	public static final int THEME_ELEMENT_TYPE_FONT = 10;
	public static final int THEME_ELEMENT_TYPE_LOCKSCREEN = 11;
	public static final int THEME_ELEMENT_TYPE_ALL_THEME = 13;
	public static final int THEME_ELEMENT_TYPE_COMPLETE_THEME = 14;
	public static final int THEME_ELEMENT_TYPE_DYNAMIC_WALLPAPER = 15;
	
	public static final int THEME_REPORT_SORT_CLICK = 0;
	public static final int THEME_REPORT_SORT_DOWNLOAD_S = 1;
	public static final int THEME_REPORT_SORT_DOWNLOAD_E = 2;
	public static final int THEME_REPORT_SORT_USE = 3;
	
	public static final int THEME_APPLY = 0;
	public static final int ICON_APPLY = 5;
	public static final int WALLPAPER_APPLY = 6;
	public static final int LOCKSCREEN_WALLPAPER_APPLY = 9;
	public static final int LOCKSCREEN_APPLY = 18;
	public static final int DESKTOP_WALLPAPER_APPLY = 19;
	public static final int RESET_LOCKSCREEN_WALLPAPER_APPLY = 24;

	
	public static final int THEME_PACKAGE_TYPE_FREE = 0;
	public static final int THEME_PACKAGE_TYPE_FOR_SALE = 1;
	public static final int THEME_PACKAGE_TYPE_DISCOUNT = 2;
	
	private int id;
	private String title;
	private String sprice;
	private Float price;
	private Float discount;//
	private String iconUrl;
	private String[] previewUrl;// 预览图URL
	private String downloadUrl;
	private Long lastUpdate; // ?
	private long size;
	private int rating; // START1 ,START2
	private int downLoadCounts;
	private int type;// 0 免费(price ==0) 1 收费(discount==price&&!=0) 2
						// 活动discount<price
	private String description;// 描述
	private String version;
	private String uiVersion;
	private String designer;// 设计师
	private String author;// 作者
	private int ringtoneDuration;
	private boolean isUpdateAvailable;
	private boolean isUsing; // is using
	private boolean isUsing_icons;
	private boolean isUsing_lockscreen;
	private boolean isUsing_lockwallpaper;
	private boolean isUsing_wallpaper;
	private boolean isUsing_fonts;
	private boolean isUsing_ringtone; // ringtone
	private boolean isDownLoad;

	private int downloadingFlag;//下载标识  0 未下载  1 下载ing 2 下载完成
	// private int isDownloading;
	private boolean isLocale;
	private int verifyFlag = 0;// 认证标识 0 未认证 1 认证成功 2 认证失败 3 下载ing
	/**************** extral *******************/
	private String fileName;
	private String themePath;
	private String previewsList;
	/**************** extral *******************/
	private String ringtonePath;
	private String wallpaperPath;
	private String screenWallpaperPath;
	/********************* NEW ADD *************************/

	private boolean isDefaultTheme;
	private boolean hasWallpaper;
	private boolean hasLockscreenWallpaper;

	private boolean hasJpgWallpaper;
	private boolean hasJpgLockscreenWallpaper;

	private boolean hasIcons;
	private boolean haslock;
	private boolean hasContacts;
	private boolean hasDialer;
	private boolean hasSystemUI;
	private boolean hasFramework;
	private boolean hasRingtone;
	private boolean hasNotification;
	private boolean hasBootanimation;
	private boolean hasMms;
	private boolean hasFont;
	private boolean isComplete;
	private long lastModified;
	private String packageName;
	/********************* NEW ADD *************************/

	public ThemeData() {
		price = new Float(0);
	}

	public ThemeData(Theme theme, Context context) {

		if (theme != null) {

			this.id = theme.getThemeId();
			this.title = theme.getTitle();
			this.description = theme.getThemeDesc() == null ? "" : theme.getThemeDesc();
			this.price = theme.getPrice() == null ? 0 : theme.getPrice();
			this.sprice = theme.getsPrice() == null ? "" : theme.getsPrice();
			this.discount = theme.getHdPrice() == null ? 0 : theme.getHdPrice();
			this.iconUrl = theme.getIconUrl();
			this.downloadUrl = theme.getDownloadUrl() == null ? "" : theme.getDownloadUrl();
			this.size = theme.getSize();
			this.packageName = theme.getPackageName() == null ? "" : theme.getPackageName();
			
			ThemeLog.e("ThemeData", "title: " + title + " theme.getStars() == null: " + (theme.getStars() == null) + " theme.getStars(): " + (theme.getStars() == null ? 0 : theme.getStars()));

			this.rating = theme.getStars() == null?0:theme.getStars();
			this.downLoadCounts = theme.getDownloads() == null ? 0 : theme.getDownloads();
			this.type = theme.getThemeType() == null ? 0 : theme.getThemeType();
			this.version = theme.getVersion() == null ? "" : theme.getVersion();
			this.uiVersion = theme.getUiversion() == null ? "" : theme.getUiversion();
			this.designer = theme.getDesigner() == null ? "" : theme.getDesigner();
			this.author = theme.getAuthor() == null ? "" : theme.getAuthor();
			if(theme.getPreviewUrl()!=null){
				if(theme.getPreviewUrl().contains(",")){
					this.previewUrl = theme.getPreviewUrl() != null ? theme.getPreviewUrl().split(",") : new String[] {};
				}else{
					this.previewUrl = new String[]{theme.getPreviewUrl()};
				}
			}
			
			this.isDownLoad = Util.checkDownload(context, theme.getPackageName());
		}

		// this.lastUpdate = theme.getUpdateDate();

		this.isUpdateAvailable = false;
		this.isUsing = false;
		this.downloadingFlag = Downloads.STATUS_UNDOWNLOAD;
		this.isLocale = false;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public float getPrice() {
		return price;
	}
	public void setPrice(Float price) {
		this.price = price;
	}
	
	public String getsPrice() {
		return sprice;
	}
	public void setsPrice(String price) {
		this.sprice = price;
	}
	
	
	
	public String getIconUrl() {
		return iconUrl;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public long getSize() {
		return size;
	}

	public int getRating() {
		return rating;
	}
	
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getPackageName() {
		return packageName;
	}
	
	public int getDownLoadCounts() {
		return downLoadCounts;
	}

	public int getType() {
		return type;
	}
    public void setType(int type){
    	this.type = type ;
    }
	
	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public void setUiVersion(String uiVersion) {
		this.uiVersion = uiVersion;
	}

	public String getUiVersion() {
		return uiVersion;
	}

	public void setDesigner(String designer) {
		this.designer = designer;
	}

	public String getdesigner() {
		return designer;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getauthor() {
		return author;
	}

	public String[] getPreviewUrl() {
		return previewUrl;
	}

	public void setlastUpdate(Long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Long getlastUpdate() {
		return lastUpdate;
	}

	public boolean getUpdateAvailable() {
		return isUpdateAvailable;
	}

	public boolean getUsing() {
		return isUsing;
	}

	public void setDownloadingFlag(int flag) {
		this.downloadingFlag = flag;
	}
	
	public int getDownloadingFlag() {
		return downloadingFlag;
	}

	public boolean getLocale() {
		return isLocale;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setRingtoneDuration(int ringtoneDuration) {
		this.ringtoneDuration = ringtoneDuration;
	}

	public int getRingtoneDuration() {
		return ringtoneDuration;
	}

	public void setThemePath(String themePath) {
		this.themePath = themePath;
	}

	public String getThemePath() {
		return themePath;
	}

	public void setRingtonePath(String ringtonePath) {
		this.ringtonePath = ringtonePath;
	}

	public String getRingtonePath() {
		return ringtonePath;
	}

	public void setWallpaperPath(String wallpaperPath) {
		this.wallpaperPath = wallpaperPath;
	}

	public String getWallpaperPath() {
		return wallpaperPath;
	}

	public void setScreenWallpaperPath(String screenWallpaperPath) {
		this.screenWallpaperPath = screenWallpaperPath;
	}

	public String getScreenWallpaperPath() {
		return screenWallpaperPath;
	}

	public String getPreviewsList() {
		return previewsList;
	}

	public void setPreviewsList(String previewsList) {
		this.previewsList = previewsList;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/***************************** NEW ADD ****************************/

	public boolean getIsDefaultTheme() {
		return isDefaultTheme;
	}

	public void setIsDefaultTheme(boolean isDefaultTheme) {
		this.isDefaultTheme = isDefaultTheme;
	}

	public boolean getHasWallpaper() {
		return hasWallpaper;
	}

	public boolean getHasJpgWallpaper() {
		return hasJpgWallpaper;
	}

	public void setHasWallpaper(boolean hasWallpaper) {
		this.hasWallpaper = hasWallpaper;
	}

	public void setHasJpgWallpaper(boolean hasJpgWallpaper) {
		this.hasJpgWallpaper = hasJpgWallpaper;
	}

	public boolean getHasIcons() {
		return hasIcons;
	}

	public void setHasIcons(boolean hasIcons) {
		this.hasIcons = hasIcons;
	}

	public boolean getHasLock() {
		return haslock;
	}

	public void setHasLock(boolean haslock) {
		this.haslock = haslock;
	}

	public boolean getHasLockscreenWallpaper() {
		return hasLockscreenWallpaper;
	}

	public boolean getHasJpgLockscreenWallpaper() {
		return hasJpgLockscreenWallpaper;
	}

	public void setHasLockscreenWallpaper(boolean hasLockscreenWallpaper) {
		this.hasLockscreenWallpaper = hasLockscreenWallpaper;
	}

	public void setHasJpgLockscreenWallpaper(boolean hasJpgLockscreenWallpaper) {
		this.hasJpgLockscreenWallpaper = hasJpgLockscreenWallpaper;
	}

	public boolean getHasContacts() {
		return hasContacts;
	}

	public void setHasContacts(boolean hasContacts) {
		this.hasContacts = hasContacts;
	}

	public boolean getHasDialer() {
		return hasDialer;
	}

	public void setHasDialer(boolean hasDialer) {
		this.hasDialer = hasDialer;
	}

	public boolean getHasSystemUI() {
		return hasSystemUI;
	}

	public void setHasSystemUI(boolean hasSystemUI) {
		this.hasSystemUI = hasSystemUI;
	}

	public boolean getHasFramework() {
		return hasFramework;
	}

	public void setHasFramework(boolean hasFramework) {
		this.hasFramework = hasFramework;
	}

	public boolean getHasRingtone() {
		return hasRingtone;
	}

	public void setHasRingtone(boolean hasRingtone) {
		this.hasRingtone = hasRingtone;
	}

	public boolean getHasNotification() {
		return hasNotification;
	}

	public void setHasNotification(boolean hasNotification) {
		this.hasNotification = hasNotification;
	}

	public boolean getHasBootanimation() {
		return hasBootanimation;
	}

	public void setHasBootanimation(boolean hasBootanimation) {
		this.hasBootanimation = hasBootanimation;
	}

	public boolean getHasMms() {
		return hasMms;
	}

	public void setHasMms(boolean hasMms) {
		this.hasMms = hasMms;
	}

	public boolean getHasFont() {
		return hasFont;
	}

	public void setHasFont(boolean hasFont) {
		this.hasFont = hasFont;
	}

	public boolean getIsComplete() {
		return isComplete;
	}

	public void setIsComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	public boolean getIsUsing() {
		return isUsing;
	}

	public void setIsUsing(boolean isUsing) {
		this.isUsing = isUsing;
	}

	public boolean getIsUsing_icons() { // icons
		return isUsing_icons;
	}

	public void setIsUsing_icons(boolean isUsing) {
		this.isUsing_icons = isUsing;
	}
	
	public boolean getIsDownLoaded() {
		return isDownLoad;
	}

	public void setIsDownLoaded(boolean download) {
		this.isDownLoad = download;
	}

	public void setIsUsing_lockscreen(boolean isUsing) { // lockscreen
		isUsing_lockscreen = isUsing;
	}

	public boolean getIsUsing_lockscreen() {
		return isUsing_lockscreen;
	}

	public void setIsUsing_lockwallpaper(boolean isUsing) { // lockwallpaper
		isUsing_lockwallpaper = isUsing;
	}

	public boolean getIsUsing_lockwallpaper() {
		return isUsing_lockwallpaper;
	}

	public void setIsUsing_wallpaper(boolean isUsing) { // wallpaper
		isUsing_wallpaper = isUsing;
	}

	public boolean getIsUsing_wallpaper() {
		return isUsing_wallpaper;
	}

	public void setIsUsing_fonts(boolean isUsing) { // fonts
		isUsing_fonts = isUsing;
	}

	public boolean getIsUsing_fonts() {
		return isUsing_fonts;
	}

	public void setIsUsing_ringtone(boolean isUsing) { // ringtone
		isUsing_ringtone = isUsing;
	}

	public boolean getIsUsing_ringtone() {
		return isUsing_ringtone;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	/***************************** NEW ADD ****************************/

	public int getVerifyFlag() {
		return verifyFlag;
	}

	public void setVerifyFlag(int verifyFlag) {
		this.verifyFlag = verifyFlag;
	}

}