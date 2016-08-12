package com.huaqin.android.market.sdk.bean;

import java.util.Date;

public class UpdateStates {
	
	private boolean hasNewRelease;
	
	private String releaseNote;
	
	private Date releaseDate;
	
	private String releaseVersion;
	
	private String releaseURL;
	
	public UpdateStates() {
	}

	public UpdateStates(boolean hasNewRelease, String releaseNote,
			Date releaseDate, String releaseVersion, String releaseURL) {
		super();
		this.hasNewRelease = hasNewRelease;
		this.releaseNote = releaseNote;
		this.releaseDate = releaseDate;
		this.releaseVersion = releaseVersion;
		this.releaseURL = releaseURL;
	}

	public boolean isHasNewRelease() {
		return hasNewRelease;
	}

	public void setHasNewRelease(boolean hasNewRelease) {
		this.hasNewRelease = hasNewRelease;
	}

	public String getReleaseNote() {
		return releaseNote;
	}

	public void setReleaseNote(String releaseNote) {
		this.releaseNote = releaseNote;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getReleaseVersion() {
		return releaseVersion;
	}

	public void setReleaseVersion(String releaseVersion) {
		this.releaseVersion = releaseVersion;
	}

	public String getReleaseURL() {
		return releaseURL;
	}

	public void setReleaseURL(String releaseURL) {
		this.releaseURL = releaseURL;
	}

}
