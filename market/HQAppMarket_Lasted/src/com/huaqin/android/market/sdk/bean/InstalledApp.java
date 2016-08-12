package com.huaqin.android.market.sdk.bean;

public class InstalledApp {
	
	private String appPackage;
	
	private int versionCode;

	public InstalledApp() {
	}
	
	public InstalledApp(String appPackage, int versionCode) {
		super();
		this.appPackage = appPackage;
		this.versionCode = versionCode;
	}
	
	public String getAppPackage() {
		return appPackage;
	}
	
	public void setAppPackage(String appPackage) {
		this.appPackage = appPackage;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

}
