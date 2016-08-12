package com.fineos.android.rom.sdk.bean;

public class RingTone {

	/**
	 * 铃声
	 */
	private int ringId;
	private String title;// 标题
	private Integer size;// 大小
	private Float price;// 价格默认为0.0即免费
	private String downloadUrl;// 下载地址
	private Integer timeLength;// 时长

	public int getRingId() {
		return ringId;
	}

	public void setRingId(int ringId) {
		this.ringId = ringId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public Integer getTimeLength() {
		return timeLength;
	}

	public void setTimeLength(Integer timeLength) {
		this.timeLength = timeLength;
	}

}
