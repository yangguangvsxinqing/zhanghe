package com.fineos.android.rom.sdk.bean;

public class Theme {

	/**
	 * 主题
	 */
	private int themeId;
	private Integer themeType;// 0.主题 1.锁屏 2.动态壁纸 3.字体 4.图标 5.壁纸
	private Integer cpId;// cp
	private String title;// 标题
	private String designer;// 设计师
	private String author;// 作者
	private long size;// 大小
	private String version;// 版本
	private String uiversion;// ui版本
	private Integer feeType;// 免费0，优惠1，收费2
	private Integer ranking;// 排行、指数
	private Float price;// 价格
	private String sprice;  
	private Integer stars;// 星级
	private Integer downloads;// 下载次数
	private Float hdPrice;// 活动价格
	private String themeDesc;// 描述
	private String iconUrl;
	private String previewUrl;//
	private String downloadUrl;
	private String packageName;
	public int getThemeId() {
		return themeId;
	}
	public void setThemeId(int themeId) {
		this.themeId = themeId;
	}
	
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public Integer getThemeType() {
		return themeType;
	}

	public void setThemeType(Integer themeType) {
		this.themeType = themeType;
	}

	public Integer getCpId() {
		return cpId;
	}

	public void setCpId(Integer cpId) {
		this.cpId = cpId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesigner() {
		return designer;
	}

	public void setDesigner(String designer) {
		this.designer = designer;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUiversion() {
		return uiversion;
	}

	public void setUiversion(String uiversion) {
		this.uiversion = uiversion;
	}

	public Integer getFeeType() {
		return feeType;
	}

	public void setFeeType(Integer feeType) {
		this.feeType = feeType;
	}

	public Integer getRanking() {
		return ranking;
	}

	public void setRanking(Integer ranking) {
		this.ranking = ranking;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}
	
	public String getsPrice() {
		return sprice;
	}

	public void setsPrice(String price) {
		this.sprice = price;
	}
	
	
	

	public Integer getStars() {
		return stars;
	}

	public void setStars(Integer stars) {
		this.stars = stars;
	}

	public Integer getDownloads() {
		return downloads;
	}

	public void setDownloads(Integer downloads) {
		this.downloads = downloads;
	}

	public Float getHdPrice() {
		return hdPrice;
	}

	public void setHdPrice(Float hdPrice) {
		this.hdPrice = hdPrice;
	}

	public String getThemeDesc() {
		return themeDesc;
	}

	public void setThemeDesc(String themeDesc) {
		this.themeDesc = themeDesc;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getPreviewUrl() {
		return previewUrl;
	}

	public void setPreviewUrl(String previewUrl) {
		this.previewUrl = previewUrl;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

}
