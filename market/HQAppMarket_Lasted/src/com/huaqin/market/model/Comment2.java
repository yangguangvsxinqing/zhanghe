package com.huaqin.market.model;

import java.io.Serializable;
import java.util.Date;

import com.huaqin.android.market.sdk.bean.Comment;

public class Comment2 implements Serializable {

	private static final long serialVersionUID = -4131438731966594787L;
	
	private int commentId;
	private int appId;
	private String clientId;
	private String content;
	private Date createTime;
	private String deviceId;
	private String memberId;
	private String nickName;
	private String subscribeId;
	private Integer stars;
	
	public Comment2(Comment comment) {
		this.appId = comment.getAppId();
		this.clientId = comment.getClientId();
		this.commentId = comment.getCommentId();
		this.content = comment.getContent();
		this.createTime = comment.getCreateTime();
		this.deviceId = comment.getDeviceId();
		this.memberId = comment.getMemberId();
		this.nickName = comment.getNickname();
		this.stars = comment.getStars();
		this.subscribeId = comment.getSubscriberId();
	}
	public int getCommentId() {
		return commentId;
	}
	public void setCommentId(int commentId) {
		this.commentId = commentId;
	}
	public int getAppId() {
		return appId;
	}
	public void setAppId(int appId) {
		this.appId = appId;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getSubscribeId() {
		return subscribeId;
	}
	public void setSubscribeId(String subscribeId) {
		this.subscribeId = subscribeId;
	}
	public Integer getStars() {
		return stars;
	}
	public void setStars(Integer stars) {
		this.stars = stars;
	}
	
}
