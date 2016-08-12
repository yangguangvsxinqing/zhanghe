package com.huaqin.android.market.sdk.bean;

public class BatchInstall {
	
	private Application application;
	
	private Integer btachTypeId;
	
	private Integer batchInstallOrder;
	
	private Integer batchOnline;

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public Integer getBtachTypeId() {
		return btachTypeId;
	}

	public void setBtachTypeId(Integer btachTypeId) {
		this.btachTypeId = btachTypeId;
	}

	public Integer getBatchInstallOrder() {
		return batchInstallOrder;
	}

	public void setBatchInstallOrder(Integer batchInstallOrder) {
		this.batchInstallOrder = batchInstallOrder;
	}

	public Integer getBatchOnline() {
		return batchOnline;
	}

	public void setBatchOnline(Integer batchOnline) {
		this.batchOnline = batchOnline;
	}
	
}
