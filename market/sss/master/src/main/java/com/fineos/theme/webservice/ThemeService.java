package com.fineos.theme.webservice;

import android.content.Context;

public class ThemeService implements IThemeService {

	public static final int THREAD_COUNT = 3;
	// Thread ID must be defined from 0
	public static final int THREAD_LIST = 0;
	public static final int THREAD_THUMB = 1;
	// public static final int THREAD_PAYMENT = 2;
	public static final int THREAD_OTHER = 2;

	private static final int MAX_PRIORITY = 10;
	private static final int NORM_PRIORITY = 5;
	private static final int MIN_PRIORITY = 1;
	private static ThemeService mInstance;

	private ThemeServiceAgent mAgent;
	private int[] nHandlerIDs;
	private RequestQueue[] mRequestQueues;
	private RequestHandler[] mRequestHandlers;

	private ThemeService(Context context) {

		nHandlerIDs = new int[THREAD_COUNT];
		mRequestQueues = new RequestQueue[THREAD_COUNT];
		mRequestHandlers = new RequestHandler[THREAD_COUNT];
		mAgent = ThemeServiceAgent.getInstance(context);

		initHandler();
	}

	@Override
	public void finalize() {
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
				if (i == THREAD_LIST) {
					mRequestHandlers[i].setPriority(MAX_PRIORITY);
				} else if (i == THREAD_THUMB) {
					mRequestHandlers[i].setPriority(NORM_PRIORITY);
				} else {
					mRequestHandlers[i].setPriority(MIN_PRIORITY);
				}
				mRequestHandlers[i].start();
			}
		}
	}

	public static ThemeService getServiceInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ThemeService(context);
		}
		return mInstance;
	}

	public ThemeServiceAgent getAgentInstance() {
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
	public void postUserInfo(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_OTHER].pushRequest(request);
	}

	@Override
	public void getThemeList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}
	
	@Override
	public void getThemeADList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void getThemeIcon(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_THUMB].pushRequest(request);
	}

	@Override
	public void getIconImg(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_THUMB].pushRequest(request);
	}

	@Override
	public void clearThumbRequest(int threadId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getIconList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void getRingtoneList(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_LIST].pushRequest(request);
	}

	@Override
	public void getThemePreviews(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_THUMB].pushRequest(request);
	}

	@Override
	public void getVerify(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_OTHER].pushRequest(request);
	}

	@Override
	public void getStartGoogleBilling(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_OTHER].pushRequest(request);
	}

	@Override
	public void getPrice(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_OTHER].pushRequest(request);
	}
	
	@Override
	public void writeData(Request request) {
		// TODO Auto-generated method stub
		mRequestQueues[THREAD_OTHER].pushRequest(request);
	}

}