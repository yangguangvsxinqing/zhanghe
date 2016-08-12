package com.huaqin.market.webservice;

import android.content.Context;

public class MarketService implements IMarketService {

	public static final int THREAD_COUNT = 3;
	// Thread ID must be defined from 0
	public static final int THREAD_LIST = 0;
	public static final int THREAD_THUMB = 1;
//	public static final int THREAD_PAYMENT = 2;
	public static final int THREAD_OTHER = 2;
	
	private static final int MAX_PRIORITY = 10;
	private static final int NORM_PRIORITY = 5;
	private static final int MIN_PRIORITY = 1;
	private static MarketService mInstance;
	
	private MarketServiceAgent mAgent;
	private int[] nHandlerIDs;
	private RequestQueue[] mRequestQueues;
	private RequestHandler[] mRequestHandlers;	

	private MarketService(Context context) {

		nHandlerIDs = new int[THREAD_COUNT];
		mRequestQueues = new RequestQueue[THREAD_COUNT];
		mRequestHandlers = new RequestHandler[THREAD_COUNT];
		mAgent = MarketServiceAgent.getInstance(context);
		
		initHandler();
	}
 
	@Override
	public void finalize(){
		// TODO Auto-generated method stub
		destroy();
		try {
			super.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void destroy() {
		// TODO Auto-generated method stub
		for (int i = 0; i < THREAD_COUNT; i++) {
			mRequestHandlers[i].bIsRunning = true;
			mRequestHandlers[i].interrupt();
		}
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		// only initialize when handlers are null
		if (mRequestHandlers[0] == null) {
			for (int i = 0; i < THREAD_COUNT; i++) {
				nHandlerIDs[i] = i;
				mRequestQueues[i] = new RequestQueue();
				mRequestHandlers[i] = new RequestHandler(this, i);
				if(i == THREAD_LIST){
					mRequestHandlers[i].setPriority(MAX_PRIORITY);
				}else if(i == THREAD_THUMB){
					mRequestHandlers[i].setPriority(NORM_PRIORITY);
				}else{
					mRequestHandlers[i].setPriority(MIN_PRIORITY);
				}
				mRequestHandlers[i].start();
			}
		}
	}

	public static MarketService getServiceInstance(Context context) {
		if (mInstance == null) {
			mInstance = new MarketService(context);
		}
		return mInstance;
	}

	public MarketServiceAgent getAgentInstance() {
		return mAgent;
	}

	public Object popRequest(int threadId) throws InterruptedException {
		
		Object obj = null;
		if (threadId < THREAD_COUNT) {
			obj = mRequestQueues[threadId].popRequest();
		}
		return obj;
	}

	@Override
	public void getAppList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void getAppDetail(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}
	
	//Added by JimmyJin for PV
	@Override
	public void getTopicAppDetail(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}
	
	@Override
	public void getAppIcon(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_THUMB].pushRequest(request);
	}

	@Override
	public void getAppPreviews(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_THUMB].pushRequest(request);
	}

	@Override
	public void getAppComments(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void addAppComment(Request request) {
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void updateAppComment(Request request) {
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void getNewAppList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void getSortAppList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}
	
	@Override
	public void getSortGameList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}
	
	@Override
	public void getRelatedAppList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void getRankingAppList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void getCategoryAppList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	/*************Added-s by JimmyJin for Pudding Project**************/
	@Override
	public void getTypeList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}
	/*************Added-e by JimmyJin for Pudding Project**************/
	@Override
	public void getCategoriesList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}
	
	@Override
	public void getSortsList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void getCategoryIcon(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_THUMB].pushRequest(request);
	}

	@Override
	public void getSubjectList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}
	
	@Override
	public void getSubjectDetail(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_THUMB].pushRequest(request);
	}

	@Override
	public void getSubjectIcon(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_THUMB].pushRequest(request);
	}
	
	@Override
	public void getNewSubject(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_THUMB].pushRequest(request);
	}
	
	@Override
	public void getTopicList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void getTopicImage(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_THUMB].pushRequest(request);
	}

	@Override
	public void getTopicApp(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void getTopApp(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void getTopAppIcon(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_THUMB].pushRequest(request);
	}

	@Override
	public void clearThumbRequest(int threadId) {
		// TODO Auto-generated method stub
		if (threadId == THREAD_THUMB
				&& !mRequestQueues[threadId].isEmpty()) {
			mRequestQueues[threadId].clearPendingRequest();
		}
	}

	@Override
	public void getSearchHotwords(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void getAppListByKeyword(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void checkSelfUpdate(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_OTHER].pushRequest(request);
	}

	@Override
	public void checkAppUpdate(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_OTHER].pushRequest(request);
	}
	
	@Override
	public void getNotice(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_OTHER].pushRequest(request);
	}
	
	/*************Added-s by JimmyJin**************/
	@Override
	public void getUserId(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_OTHER].pushRequest(request);
	}
	
	@Override
	public void postDownloadBegin(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_OTHER].pushRequest(request);
	}
	
	@Override
	public void postDownloadEnd(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_OTHER].pushRequest(request);
	}
	
	@Override
	public void postInstallInfo(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_OTHER].pushRequest(request);
	}
	/*************Added-e by JimmyJin**************/

	@Override
	public void getSearchDatabase(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void PostPV(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_OTHER].pushRequest(request);
	}
	
	@Override
	public void postRebuildContext(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_OTHER].pushRequest(request);
	}
}