package com.huaqin.android.market.sdk.bean;

import java.util.Date;

public class DownloadBegin {
	
	private Integer beginId;	

	private String userId;
	
	private Integer appId;
	
	private String appPackage;
	
	private Date beginDate;
	
	private String fromWhere;
	
	public Integer getBeginId() {
		return beginId;
	}

	public void setBeginId(Integer beginId) {
		this.beginId = beginId;
	}

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

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public String getFromWhere() {
		return fromWhere;
	}

	public void setFromWhere(String fromWhere) {
		this.fromWhere = fromWhere;
	}
	
	
}
