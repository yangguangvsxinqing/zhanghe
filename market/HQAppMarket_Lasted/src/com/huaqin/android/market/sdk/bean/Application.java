package com.huaqin.android.market.sdk.bean;

import java.util.Date;

public class Application {
	
	private Integer appId;
	
	private String appName;
	
	private String appDesc;
	
	private String keywords;
	
	private Integer cateId;
	
	private Integer sortId;
	
	private String appPackage;
	
	private String author;
	
	private Integer versionCode;
	
	private String versionName;
	
	private String permissions;//comma separated
	
	private Integer feeType;
	
	private Integer feeAmount;
	
	private String iconUrl;
	
	private String previewUrl;//comma separated
	
	private String downloadUrl;
	
	private Date updateTime;
	
	private Integer size;
	
	private Integer stars;
	
/*	private Integer top;//顶

	private Integer tread;//踩
*/	
	private Integer comments;
	
	private Integer weeklyDownloads;
	
	private Integer monthlyDownloads;
	
	private Integer totalDownloads;
	
	private Integer mark;
	
	public Integer getAppId() {
		return appId;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppDesc() {
		return appDesc;
	}

	public void setAppDesc(String appDesc) {
		this.appDesc = appDesc;
	}

	public String getAppPackage() {
		return appPackage;
	}

	public void setAppPackage(String appPackage) {
		this.appPackage = appPackage;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public Integer getCateId() {
		return cateId;
	}

	public void setCateId(Integer cateId) {
		this.cateId = cateId;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Integer getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(Integer versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	public Integer getFeeType() {
		return feeType;
	}

	public void setFeeType(Integer feeType) {
		this.feeType = feeType;
	}

	public Integer getFeeAmount() {
		return feeAmount;
	}

	public void setFeeAmount(Integer feeAmount) {
		this.feeAmount = feeAmount;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getPreviewUrl() {
		return previewUrl;
	}

	public void setPreviewUrl(String previewUrl) {
		this.previewUrl = previewUrl;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Integer getStars() {
		return stars;
	}

	public void setStars(Integer stars) {
		this.stars = stars;
	}

	public Integer getComments() {
		return comments;
	}

	public void setComments(Integer comments) {
		this.comments = comments;
	}

	public Integer getWeeklyDownloads() {
		return weeklyDownloads;
	}

	public void setWeeklyDownloads(Integer weeklyDownloads) {
		this.weeklyDownloads = weeklyDownloads;
	}

	public Integer getMonthlyDownloads() {
		return monthlyDownloads;
	}

	public void setMonthlyDownloads(Integer monthlyDownloads) {
		this.monthlyDownloads = monthlyDownloads;
	}

	public Integer getTotalDownloads() {
		return totalDownloads;
	}

	public void setTotalDownloads(Integer totalDownloads) {
		this.totalDownloads = totalDownloads;
	}

	public Integer getSortId() {
		return sortId;
	}

	public void setSortId(Integer sortId) {
		this.sortId = sortId;
	}

	public Integer getMark() {
		return mark;
	}

	public void setMark(Integer mark) {
		this.mark = mark;
	}	
}
