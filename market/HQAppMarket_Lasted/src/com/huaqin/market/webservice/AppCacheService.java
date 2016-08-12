package com.huaqin.market.webservice;

import android.content.Context;

public class AppCacheService {
	
	public static final int THREAD_COUNT = 2;
	// Thread ID must be defined from 0
	public static final int THREAD_LIST = 0;	
	public static final int THREAD_AD = 1;
	
	private static final int MAX_PRIORITY = 10;
	private static final int NORM_PRIORITY = 5;
	
	private static AppCacheService mInstance;
	private AppCacheServiceAgent mAgent;
//	private int[] nHandlerIDs;
	private RequestQueue[] mRequestQueues;
	private AppCacheEventHandler[] mRequestHandlers;	

	public AppCacheService(Context context) {

//		nHandlerIDs = new int[THREAD_COUNT];
		mRequestQueues = new RequestQueue[THREAD_COUNT];
		mRequestHandlers = new AppCacheEventHandler[THREAD_COUNT];
		mAgent = AppCacheServiceAgent.getInstance(context);
		
		initHandler();
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		// only initialize when handlers are null
		if (mRequestHandlers[0] == null) {
			for (int i = 0; i < THREAD_COUNT; i++) {
//				nHandlerIDs[i] = i;
				mRequestQueues[i] = new RequestQueue();
				mRequestHandlers[i] = new AppCacheEventHandler(this, i);
				if(i == 0){
					mRequestHandlers[i].setPriority(MAX_PRIORITY);
				}else{
					mRequestHandlers[i].setPriority(NORM_PRIORITY);
				}
				mRequestHandlers[i].start();
			}
		}
	}

	public static AppCacheService getServiceInstance(Context context) {

		if (mInstance == null) {
			mInstance = new AppCacheService(context);
		}
		return mInstance;
	}

	public AppCacheServiceAgent getAgentInstance() {
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

	public void getTopAppIcon(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_AD].pushRequest(request);
	}
	
	public void getTopLayout(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_AD].pushRequest(request);
	}

	public void getTopAppList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	public void getAppList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}
}