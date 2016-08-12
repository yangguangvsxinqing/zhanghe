package com.huaqin.android.market.sdk.bean;

import java.util.Date;

public class InstallInfo {
	
	private Integer installId;
	
	private String userId;
	
	private Integer appId;
	
	private String appPackage;
	
	private Date installDate;

	private String fromWhere;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Integer getAppId() {
		return appId;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
	}

	public String getAppPackage() {
		return appPackage;
	}

	public void setAppPackage(String appPackage) {
		this.appPackage = appPackage;
	}

	public Integer getInstallId() {
		return installId;
	}

	public void setInstallId(Integer installId) {
		this.installId = installId;
	}

	public Date getInstallDate() {
		return installDate;
	}

	public void setInstallDate(Date installDate) {
		this.installDate = installDate;
	}

	public String getFromWhere() {
		return fromWhere;
	}

	public void setFromWhere(String fromWhere) {
		this.fromWhere = fromWhere;
	}

	
	
	
}
