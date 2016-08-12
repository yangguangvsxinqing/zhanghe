package com.fineos.android.rom.sdk.bean;

public class WallPaper {

	/**
	 * 壁纸
	 */
	private int paperId;// id
	private String title;// 标题
	private int type;// 类型：0壁纸1锁屏壁纸
	private Integer size;// 大小
	private String downUrl;// 下载地址
	private String iconUrl;//

	public int getPaperId() {
		return paperId;
	}

	public void setPaperId(int paperId) {
		this.paperId = paperId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getDownUrl() {
		return downUrl;
	}

	public void setDownUrl(String downUrl) {
		this.downUrl = downUrl;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
}
