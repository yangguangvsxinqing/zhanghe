package com.huaqin.market.webservice;

public abstract interface IMarketService {

	// Application 
	public abstract void getAppList(Request request);
	public abstract void getAppDetail(Request request);
	//Added by JimmyJin for PV
	public abstract void getTopicAppDetail(Request request);
	public abstract void getAppIcon(Request request);
	public abstract void getAppPreviews(Request request);
	public abstract void getAppComments(Request request);
	public abstract void addAppComment(Request request);
	public abstract void updateAppComment(Request request);
	public abstract void getNewAppList(Request request);
	public abstract void getSortAppList(Request request);
	public abstract void getSortGameList(Request request);
	public abstract void getRelatedAppList(Request request);
	public abstract void getRankingAppList(Request request);
	public abstract void getCategoryAppList(Request request);
	/*************Added-s by JimmyJin for Pudding Project**************/
	public abstract void getTypeList(Request request);
	/*************Added-e by JimmyJin for Pudding Project**************/

	// Category List
	public abstract void getCategoriesList(Request request);
	public abstract void getCategoryIcon(Request request);

	public abstract void getSortsList(Request request);
	// Subject List
	public abstract void getSubjectList(Request request);
	public abstract void getSubjectDetail(Request request);
	
	public abstract void getSubjectIcon(Request request);
	public abstract void getTopicList(Request request);
	public abstract void getTopicImage(Request request);
	public abstract void getTopicApp(Request request);

	// Top Application
	public abstract void getTopApp(Request request);
	public abstract void getTopAppIcon(Request request);

	// Search List
	public abstract void getSearchHotwords(Request request);
	public abstract void getSearchDatabase(Request request);
	public abstract void getAppListByKeyword(Request request);

	// Clear thumb request
	public abstract void clearThumbRequest(int threadId);

	// Check update
	public abstract void checkSelfUpdate(Request request);
	public abstract void checkAppUpdate(Request request);
	public abstract void getNotice(Request request);
	
	/*************Added-s by JimmyJin
	 * @return **************/
	//Report
	public abstract void PostPV(Request request);
	public abstract void getUserId(Request request);
	
	public abstract void postDownloadBegin(Request request);
	
	public abstract void postDownloadEnd(Request request);
	
	public abstract void postInstallInfo(Request request);
	/*************Added-e by JimmyJin**************/
	public abstract void getNewSubject(Request request);
	public abstract void postRebuildContext(Request request);
}