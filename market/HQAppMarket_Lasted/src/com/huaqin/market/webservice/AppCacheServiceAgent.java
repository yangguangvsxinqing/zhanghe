package com.huaqin.market.webservice;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;

import org.apache.http.HttpException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.huaqin.android.market.sdk.bean.AdInfoList;
import com.huaqin.android.market.sdk.bean.Application;
import com.huaqin.android.market.sdk.ServiceProvider;
import com.huaqin.market.model.TopAppDetial;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.FileManager;
import com.huaqin.market.utils.Md5;

public class AppCacheServiceAgent {

	private static AppCacheServiceAgent mInstance;
	
	private Context mContext;
	private MarketServiceAgent mMarketServiceAgent;
	private ArrayList<Application> mAppList;
	private AdInfoList mAdInfo;
	private int nPageSize;
	private boolean bAppListCacheded;

	public AppCacheServiceAgent(Context context) {

		this.mContext = context;
		mMarketServiceAgent = MarketServiceAgent.getInstance(mContext);
		nPageSize = Constant.LIST_COUNT_PER_TIME;
		
		bAppListCacheded = false;
	}

	public static AppCacheServiceAgent getInstance(Context context) {

		if (mInstance == null) {
			mInstance = new AppCacheServiceAgent(context);
		}
		return mInstance;
	}

	public static void freeInstance() {
		mInstance = null;
	}

	public Drawable[] getTopApp() throws SocketException {
		// TODO Auto-generated method stub
		Log.v("getTopApp", "getTopApp in");
		try {
			mAdInfo = ServiceProvider.getAdInfoListNew();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (HttpException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (mAdInfo == null) {
			throw new SocketException();
		}
		Log.v("getTopApp", "getTopApp in2");
		Drawable[] drawable = new Drawable[5];
		String[] encodeFileName = new String[5];

		for(int i = 0;i < mAdInfo.getAdInfos().size();i++){
		encodeFileName[i] = Md5.encode(mAdInfo.getAdInfos().get(i).getImageUrl());
		drawable[i] = FileManager.readIconFromFile(mContext, encodeFileName[i]);
		Log.v("asd", "JimmyJin drawable="+drawable.length);
		if(drawable[i] == null) {
			MarketServiceAgent.mTopAppIcon[i] = mAdInfo.getAdInfos().get(i).getImageUrl();
			Log.v("asd", "JimmyJin mTopAppIcon666="+MarketServiceAgent.mTopAppIcon[i]);
			if (MarketServiceAgent.mTopAppIcon[i] != null) {
				try {
					byte[] rawData = ServiceProvider.getImage(MarketServiceAgent.mTopAppIcon[i]);
					if (rawData != null ) {
						Bitmap bmp = 
							BitmapFactory.decodeByteArray(rawData, 0, rawData.length);
						drawable[i]=new BitmapDrawable(null, bmp);
						}
					
				} catch (Exception e) {
					e.printStackTrace();
					throw new SocketException();
				}
			}
			FileManager.writeIconToFile(mContext, encodeFileName[i], drawable[i]);
		}
		}

		return drawable;
	}
	
	public int[] getTopLayout() throws SocketException {
		// TODO Auto-generated method stub
		if (mAdInfo == null) {
			try {
				mAdInfo = ServiceProvider.getAdInfoListNew();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (HttpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (mAdInfo == null) {
			throw new SocketException();
		}
		
		int[] layout = new int[2];
		layout[0] = 1;
		layout[1] = mAdInfo.getAdInfos().size();
		return layout;
	}

	public ArrayList<TopAppDetial> getTopAppList(int listType) throws SocketException {
		// TODO Auto-generated method stub
		ArrayList<TopAppDetial> topAppListIds = new ArrayList<TopAppDetial>(5);
//		if (listType == Constant.TYPE_TOP_APP_DETAIL && mAdInfo.getAdInfos() != null) {
//			String[] appIds = mAdInfo.getAdInfos().get(0).getAppLinks().split(",");
//			int nTopAppCount = appIds.length;
//			Log.v("asd", "nTopAppCount="+nTopAppCount);
//			Log.v("asd", "appIds="+appIds);
//			topAppListIds = new ArrayList<Integer>();
//			for (int i = 0; i < nTopAppCount; i++) {
//				topAppListIds.add(Integer.parseInt(appIds[i]));
//			}
//		}
		Log.v("getTopAppList","topAppListIds="+topAppListIds);
		if (listType == Constant.TYPE_TOP_APP_DETAIL && mAdInfo != null) {
			Log.v("getTopAppList","mAdInfo="+(mAdInfo.getAdInfos()));
			for (int i = 0; i < mAdInfo.getAdInfos().size(); i++) {
			topAppListIds.add(new TopAppDetial());
			String[] appIds = mAdInfo.getAdInfos().get(i).getAppLinks().split(",");
			int adType = mAdInfo.getAdInfos().get(i).getAdType();
			topAppListIds.get(i).setAdType(adType);
			
			Log.v("getTopAppList","mAdInfo="+(String)(mAdInfo.getAdInfos().get(i).getAppLinks()));
			Log.v("getTopAppList","appIds="+appIds);
			Log.v("getTopAppList","appIdssize="+appIds.length);
			for (int j = 0; j < appIds.length; j++) {
				
				if(adType == 0){
					topAppListIds.get(i).setAppId(Integer.parseInt(appIds[j]));
				}
				if(adType == 2){
					topAppListIds.get(i).setwebUrl(appIds[j]);
				}
			}
			}
		}
		Log.v("asd","topAppListIds="+topAppListIds.size());
		return topAppListIds;
	}

	public ArrayList<Application> getAppList(int listType, int startIndex)
		throws SocketException {
		// TODO Auto-generated method stub
		if (listType == Constant.TYPE_APP_RECOMMAND_LIST) {
			if (!bAppListCacheded) {
				if (mAppList == null) {
					mAppList = new ArrayList<Application>();
				}
				
				if (mAppList.size() < nPageSize) {
					mAppList.clear();
					try {
						ArrayList<Application> appList =
							mMarketServiceAgent.getAppList(listType, startIndex);
						if (appList != null) {
							mAppList.addAll(appList);
						}
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						throw e;
					}
					bAppListCacheded = true;
				}
			}
			return mAppList;
		}
		return null;
	}
}