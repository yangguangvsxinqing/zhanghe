package com.huaqin.android.market.sdk.bean;

import java.util.Date;

public class NewTopic {
	
	private Integer topicId;
	
	private Integer subjId;
	
	private Integer appId;
	
	private String title;
	
	private String content;
	
	private String illustrations;
	
	private Date createTime;

	public Integer getTopicId() {
		return topicId;
	}

	public void setTopicId(Integer topicId) {
		this.topicId = topicId;
	}

	public Integer getSubjId() {
		return subjId;
	}

	public void setSubjId(Integer subjId) {
		this.subjId = subjId;
	}

	public Integer getAppId() {
		return appId;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getIllustrations() {
		return illustrations;
	}

	public void setIllustrations(String illustrations) {
		this.illustrations = illustrations;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}
