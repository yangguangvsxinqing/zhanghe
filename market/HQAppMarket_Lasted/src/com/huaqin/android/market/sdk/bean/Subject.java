package com.huaqin.android.market.sdk.bean;

import java.util.Date;

public class Subject {
	
	private Integer subjId;
	
	private String title;
	
	private String description;
	
	private String icon;
	
	private Integer up;
	
	private Integer down;
	
	
	private String reads;
	
	private Date createTime;

	public Integer getSubjId() {
		return subjId;
	}

	public void setSubjId(Integer subjId) {
		this.subjId = subjId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Integer getUp() {
		return up;
	}

	public void setUp(Integer up) {
		this.up = up;
	}

	public Integer getDown() {
		return down;
	}

	public void setDown(Integer down) {
		this.down = down;
	}

	public String getReads() {
		return reads;
	}

	public void setReads(String reads) {
		this.reads = reads;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}
