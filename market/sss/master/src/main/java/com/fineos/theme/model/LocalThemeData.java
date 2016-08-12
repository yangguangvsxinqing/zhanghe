package com.fineos.theme.model;

import java.io.Serializable;

import com.fineos.android.rom.sdk.bean.Theme;

public class LocalThemeData implements Serializable {

	private static final long serialVersionUID = -2387824253271681301L;

	private int id;
	private String title;
	private Float discount;//
	private long size;
	private String discrition;// 描述
	private String version;
	private String uiVersion;
	private boolean isUpdateAvailable;
	private boolean isUsing;
	// private int isDownloading;
	private boolean isLocale;

	public LocalThemeData(Theme theme) {

		this.id = theme.getThemeId();
		this.title = theme.getTitle();
		this.discrition = theme.getThemeDesc() == null ? "" : theme.getThemeDesc();
		this.discount = theme.getHdPrice() == 0 ? 0 : theme.getHdPrice();
		this.size = theme.getSize();
		this.version = theme.getVersion() == null ? "" : theme.getVersion();
		this.uiVersion = theme.getUiversion() == null ? "" : theme.getUiversion();

		this.isUsing = false;
		this.isLocale = false;
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getThemeDesc() {
		return discrition;
	}

	public long getSize() {
		return size;
	}

	public String getVersion() {
		return version;
	}

	public String getUiVersion() {
		return uiVersion;
	}

	public boolean getUsing() {
		return isUsing;
	}

	public boolean getLocale() {
		return isLocale;
	}
}