package com.huaqin.market.webservice;

import java.net.SocketException;
import java.util.ArrayList;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.huaqin.android.market.sdk.bean.Application;
import com.huaqin.market.model.TopAppDetial;
import com.huaqin.market.utils.Constant;

public class AppCacheEventHandler extends Thread {

	private static final String THREAD_NAME = "AppCacheEventHandler";
	
	public boolean bIsRunning;
	
	private int nThreadId;
	private AppCacheService mService;

	public AppCacheEventHandler(AppCacheService service, int threadId) {
		// set thread name according to thread id
		super((String) (THREAD_NAME + threadId));
		
		this.nThreadId = threadId;
		this.mService = service;
		this.bIsRunning = true;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			if (!bIsRunning) {
				continue;
			}
			Request request = null;		
			try {
				request = (Request) mService.popRequest(nThreadId);
				if (request == null) {
					continue;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
			switch (request.getType()) {
			case Constant.TYPE_TOP_APP_DETAIL:
				try {
					ArrayList<TopAppDetial> appList = new ArrayList<TopAppDetial>(5);
					Log.v("asd", "TYPE_TOP_APP_DETAIL=");
					appList = 
						mService.getAgentInstance().getTopAppList(Constant.TYPE_TOP_APP_DETAIL);
					Log.v("asd", "appList"+appList.size());
					request.setStatus(Constant.STATUS_SUCCESS);
					request.notifyObservers(appList);
				} catch (SocketException e) {
					request.setStatus(Constant.STATUS_ERROR);
					request.notifyObservers(null);
				}
				break;
			case Constant.TYPE_APP_RECOMMAND_LIST:
				try {
					ArrayList<Application> appList = null;
					Object[] params = (Object[]) request.getData();
					int startIndex = ((Integer)(params[1])).intValue();
					
					appList = mService.getAgentInstance().getAppList(
								Constant.TYPE_APP_RECOMMAND_LIST,
								startIndex);
					request.setStatus(Constant.STATUS_SUCCESS);
					request.notifyObservers(appList);
				} catch (Exception e) {
					request.setStatus(Constant.STATUS_ERROR);
					request.notifyObservers(null);
				}
				break;
			case Constant.TYPE_TOP_APP_ICON:
				try {
					Drawable[] drawable = mService.getAgentInstance().getTopApp();
					request.setStatus(Constant.STATUS_SUCCESS);
					request.notifyObservers(drawable);
				} catch (Exception e) {
					e.printStackTrace();
					request.setStatus(Constant.STATUS_ERROR);
					request.notifyObservers(null);
				}				
				break;
			case Constant.TYPE_TOP_LAYOUT_DETIAL:
				try {
					int[] layout = mService.getAgentInstance().getTopLayout();
					request.setStatus(Constant.STATUS_SUCCESS);
					request.notifyObservers(layout);
				} catch (Exception e) {
					e.printStackTrace();
					request.setStatus(Constant.STATUS_ERROR);
					request.notifyObservers(null);
				}				
				break;
			default:
				break;
			}
		}
	}
}