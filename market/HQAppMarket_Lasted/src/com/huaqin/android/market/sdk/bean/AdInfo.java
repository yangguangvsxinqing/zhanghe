package com.huaqin.android.market.sdk.bean;


public class AdInfo {
	
	private Integer adId;
	
	private String imageUrl;
	
	private Integer adRows;
	
	private Integer adCols;
	
	private Integer adType;//0:apk,1:subject,2:webview
	
	private String appLinks;
	
	private Integer showTime;
	
	

	public Integer getAdId() {
		return adId;
	}

	public void setAdId(Integer adId) {
		this.adId = adId;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Integer getAdRows() {
		return adRows;
	}

	public void setAdRows(Integer adRows) {
		this.adRows = adRows;
	}

	public Integer getAdCols() {
		return adCols;
	}

	public void setAdCols(Integer adCols) {
		this.adCols = adCols;
	}

	public String getAppLinks() {
		return appLinks;
	}

	public void setAppLinks(String appLinks) {
		this.appLinks = appLinks;
	}

	public Integer getShowTime() {
		return showTime;
	}

	public void setShowTime(Integer showTime) {
		this.showTime = showTime;
	}

	public Integer getAdType() {
		return adType;
	}

	public void setAdType(Integer adType) {
		this.adType = adType;
	}
	
	
}
