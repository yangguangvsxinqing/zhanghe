package com.huaqin.android.market.sdk.bean;


public class Partner {
	
	private int id;
	
	private String name;//第三方名称
	
	private boolean havePush;//是否展示push
	
	private String pattern;//合作方式 0:sdk,1:webweiw
	
	private String urls;//webwiew url
	
	
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isHavePush() {
		return havePush;
	}

	public void setHavePush(boolean havePush) {
		this.havePush = havePush;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getUrls() {
		return urls;
	}

	public void setUrls(String urls) {
		this.urls = urls;
	}
	
}
