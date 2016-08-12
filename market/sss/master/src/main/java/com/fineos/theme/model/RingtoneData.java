package com.fineos.theme.model;

import java.io.Serializable;

import android.net.Uri;

public class RingtoneData implements Serializable {

	private static final long serialVersionUID = -2387824253271681301L;

	private long id;
	private String title;
	private Float price;
	private Float discount;//
	private Long lastUpdate; // ?
	private Long size;
	private int rating; // START1 ,START2
	private int downLoadCounts;
	private int type;// 0 免费(price ==0) 1 收费(discount==price&&!=0) 2
						// 活动discount<price
	private String version;
	private boolean isUpdateAvailable;
	private int downloadingFlag;
	private boolean isLocale;

	private boolean isPhone;
	private boolean isSMS;
	private boolean isAlarm;
	/**************** extral *******************/
	private String fileName;
	private Uri themePath;
	private boolean isUsing_ringtone; // ringtone
	private int ringtoneDuration;

	/**************** extral *******************/
	public RingtoneData() {

		this.isUpdateAvailable = false;
		this.isPhone = false;
		this.isSMS = false;
		this.isAlarm = false;
		this.downloadingFlag = 0;
		this.isLocale = false;

	}

	public RingtoneData(ThemeData theme) {

		if (theme != null) {
			this.id = theme.getId();
			this.title = theme.getTitle();
			if (theme.getThemePath() != null) {
				this.themePath = Uri.parse(theme.getThemePath());
			}
			this.price = theme.getPrice() == 0 ? 0 : theme.getPrice();
			this.size = theme.getSize();
			this.rating = theme.getRating();
			this.downLoadCounts = theme.getDownLoadCounts() == 0 ? 0 : theme.getDownLoadCounts();
			this.type = theme.getType() == 0 ? 0 : theme.getType();
			this.version = theme.getVersion() == null ? null : theme.getVersion();
		}

		this.isUpdateAvailable = false;
		this.isPhone = false;
		this.isSMS = false;
		this.isAlarm = false;
		this.downloadingFlag = 0;
		this.isLocale = false;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public float getPrice() {
		return price;
	}

	public Long getSize() {
		return size;
	}

	public int getRating() {
		return rating;
	}

	public int getDownLoadCounts() {
		return downLoadCounts;
	}

	public int getType() {
		return type;
	}

	public void setlastUpdate(Long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Long getlastUpdate() {
		return lastUpdate;
	}

	public boolean getUpdateAvailable() {
		return isUpdateAvailable;
	}

	public int getDownloadingFlag() {
		return downloadingFlag;
	}

	public boolean getLocale() {
		return isLocale;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setPhone(boolean isPhone) {
		this.isPhone = isPhone;
	}

	public boolean getPhone() {
		return isPhone;
	}

	public void setSMS(boolean isSMS) {
		this.isSMS = isSMS;
	}

	public boolean getSMS() {
		return isSMS;
	}

	public void setAlarm(boolean isAlarm) {
		this.isAlarm = isAlarm;
	}

	public boolean getAlarm() {
		return isAlarm;
	}

	public void setThemePath(Uri themePath) {
		this.themePath = themePath;
	}

	public Uri getThemePath() {
		return themePath;
	}

	public void setIsUsing_ringtone(boolean isUsing) { // ringtone
		isUsing_ringtone = isUsing;
	}

	public boolean getIsUsing_ringtone() {
		return isUsing_ringtone;
	}

	public void setRingtoneDuration(int ringtoneDuration) {
		this.ringtoneDuration = ringtoneDuration;
	}

	public int getRingtoneDuration() {
		return ringtoneDuration;
	}

}