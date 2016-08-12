package com.huaqin.market.model;

import java.io.Serializable;
import java.util.Date;

import com.huaqin.android.market.sdk.bean.NewSubject;

public class Subject2 implements Serializable {

	private static final long serialVersionUID = -8405904903637660663L;
	
	private int subjId;
	private String title;
	private String description;
	private String icon;
	private String reads;
	private Date releaseDate;
	private boolean bIsHot;
	private Integer up;
	private Integer down;

	public Subject2(NewSubject sub) {
		// TODO Auto-generated constructor stub
		this.subjId = sub.getSubjId();
		this.title = sub.getTitle();
		this.description = sub.getDescription();
		this.icon = sub.getIcon();
		this.reads = sub.getReads();
		this.releaseDate = new Date();
		this.bIsHot = true;
		this.up = sub.getUp();
		this.down = sub.getDown();
	}

	public int getSubjId() {
		return subjId;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getIcon() {
		return icon;
	}
	
	public Integer getUp(){
		return up;
	}
	
	public Integer getDown(){
		return down;
	}

	public String getReads() {
		return reads;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean isHot() {
		return bIsHot;
	}
}