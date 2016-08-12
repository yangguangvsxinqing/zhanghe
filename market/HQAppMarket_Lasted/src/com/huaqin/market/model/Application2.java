package com.huaqin.market.model;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import android.util.Log;

import com.huaqin.android.market.sdk.bean.Application;

public class Application2 implements Serializable{

	private static final long serialVersionUID = -2387824253271681301L;

	private int appId;
	private String appName;
	private String appDesc;
	private String appPackage;
	private String keywords;
	private String author;
	private int versionCode;
	private String versionName;
	private Date releaseDate;
	private String[] permissions;
	private int feeType;
	private int feeAmount;
	private String iconUrl;
	private String[] previewUrl;
	private String downloadUrl;
	private Date updateTime;
	private int size;
	private int stars;
	private int downloads;
    public boolean bUpdateAvailable;
    public boolean bInstalled;
    public int bDownloadingFlag;
    public boolean bDownloadNotInstalled;
    public File downloadedAppFile;
    private int hotType;
	/*************Added-s by JimmyJin for Pudding Project**************/
    private int puddingType;
	/*************Added-e by JimmyJin for Pudding Project**************/
	public Application2(Application app) {

		this.appId = app.getAppId();
		this.appName = app.getAppName();
		this.appDesc = app.getAppDesc() == null ? "" : app.getAppDesc();
		this.appPackage = app.getAppPackage();
		this.author = app.getAuthor();
		this.downloads = app.getTotalDownloads() == null ? 0 : app.getTotalDownloads();
		this.downloadUrl = app.getDownloadUrl();
		//this.feeAmount = app.getFeeAmount();
		//this.feeType = app.getFeeType();
		this.iconUrl = app.getIconUrl();
		this.keywords = app.getKeywords() == null ? "" : app.getKeywords();
		this.permissions = app.getPermissions() != null ? app.getPermissions().split(",") : new String[]{};
		this.previewUrl = app.getPreviewUrl() != null ? app.getPreviewUrl().split(",") : new String[]{};
		this.releaseDate = app.getUpdateTime();
		this.size = app.getSize();
		this.stars = app.getStars() == null ? 0 : app.getStars();
		this.updateTime = app.getUpdateTime();
		this.versionCode = app.getVersionCode();
		this.versionName = app.getVersionName();
		this.bUpdateAvailable = false;
		this.bInstalled = false;
		this.hotType = app.getMark();
		this.bDownloadingFlag = 0;
		this.bDownloadNotInstalled = false;		
		/*************Added-s by JimmyJin for Pudding Project**************/
		this.puddingType = app.getSortId();
		/*************Added-s by JimmyJin for Pudding Project**************/
	}

	public int getAppId() {
		return appId;
	}

	public String getAppName() {
		return appName;
	}

	public String getAppDesc() {
		return appDesc;
	}

	public String getAppPackage() {
		return appPackage;
	}

	public String getKeywords() {
		return keywords;
	}

	public String getAuthor() {
		return author;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public String[] getPermissions() {
		return permissions;
	}

	public int getFeeType() {
		return feeType;
	}

	public int getFeeAmount() {
		return feeAmount;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public String[] getPreviewUrl() {
		return previewUrl;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public int getSize() {
		return size;
	}

	public int getStars() {
		return stars;
	}

	public int getDownloads() {
		return downloads;
	}
	public int getHotType() {
		return hotType;
	}
	
	/*************Added-s by JimmyJin for Pudding Project**************/
	public int getPuddingType() {
		return puddingType;
	}
	/*************Added-e by JimmyJin for Pudding Project**************/
}