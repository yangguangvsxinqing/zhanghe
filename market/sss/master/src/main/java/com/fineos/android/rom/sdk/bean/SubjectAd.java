package com.fineos.android.rom.sdk.bean;

public class SubjectAd {
	private int id;
	private int type;//0.主题 1.广点通 2.web
	private Theme theme;
	private String adId;
	private String picUrl;
	private String webUrl;
	private String adName;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Theme getTheme() {
		return theme;
	}
	public void setTheme(Theme theme) {
		this.theme = theme;
	}
	public String getAdId() {
		return adId;
	}
	public void setAdId(String adId) {
		this.adId = adId;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public String getWebUrl() {
		return webUrl;
	}
	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
	}
	public String getADName() {
		return adName;
	}
	public void setADName(String adName) {
		this.adName = adName;
	}
	
}
