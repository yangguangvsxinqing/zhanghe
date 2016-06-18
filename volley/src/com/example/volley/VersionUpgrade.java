package com.example.volley;

public class VersionUpgrade {

	private Integer id;
	private String version;
	private Integer sortId;
	private String downUrl;
	private String upgradeNote;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDownUrl() {
		return downUrl;
	}

	public void setDownUrl(String downUrl) {
		this.downUrl = downUrl;
	}

	public Integer getSortId() {
		return sortId;
	}

	public void setSortId(Integer sortId) {
		this.sortId = sortId;
	}

	public String getUpgradeNote() {
		return upgradeNote;
	}

	public void setUpgradeNote(String upgradeNote) {
		this.upgradeNote = upgradeNote;
	}
	
}
