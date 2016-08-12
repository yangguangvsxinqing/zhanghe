package com.fineos.theme.webservice;

public abstract interface IThemeService {

	public abstract void postUserInfo(Request request);

	public abstract void getThemeList(Request request);

	public abstract void getThemeIcon(Request request);

	// Clear thumb request
	public abstract void clearThumbRequest(int threadId);

	public abstract void getIconList(Request request);

	public abstract void getIconImg(Request request);

	public abstract void getRingtoneList(Request request);

	public abstract void getThemePreviews(Request request);

	public abstract void getVerify(Request request);

	public abstract void getThemeADList(Request request);
	
	public abstract void getStartGoogleBilling(Request request);
	
	public abstract void getPrice(Request request);
	
	public abstract void writeData(Request request);
}