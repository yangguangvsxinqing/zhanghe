package com.huaqin.market.webservice;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;

import org.apache.http.HttpException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.huaqin.android.market.sdk.ClientInfo;
import com.huaqin.android.market.sdk.Constants;
import com.huaqin.android.market.sdk.bean.AdInfoList;
import com.huaqin.android.market.sdk.bean.Application;
import com.huaqin.android.market.sdk.bean.ApplicationList;
import com.huaqin.android.market.sdk.bean.Comment;
import com.huaqin.android.market.sdk.bean.CommentList;
import com.huaqin.android.market.sdk.bean.HotWordList;
import com.huaqin.android.market.sdk.bean.InstalledApp;
import com.huaqin.android.market.sdk.bean.NewSubject;
import com.huaqin.android.market.sdk.bean.NewSubjectList;
import com.huaqin.android.market.sdk.bean.NewTopic;
import com.huaqin.android.market.sdk.bean.NewTopicList;
import com.huaqin.android.market.sdk.bean.Partner;
import com.huaqin.android.market.sdk.bean.Sort;
import com.huaqin.android.market.sdk.bean.SortList;
import com.huaqin.android.market.sdk.bean.UpdateStates;
import com.huaqin.android.market.sdk.bean.appNameList;
import com.huaqin.android.market.sdk.rest.BaseResource;
import com.huaqin.android.market.sdk.ApplicationProvider;
import com.huaqin.android.market.sdk.ClientPvProvider;
import com.huaqin.android.market.sdk.NewSubjectProvider;
import com.huaqin.android.market.sdk.ReportProvider;
import com.huaqin.android.market.sdk.ServiceProvider;
import com.huaqin.android.market.sdk.SortProvider;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.DeviceUtil;
import com.huaqin.market.utils.FileManager;
import com.huaqin.market.utils.GlobalUtil;

public class MarketServiceAgent {

	public static String[] mTopAppIcon=new String[5];
	private AdInfoList mAdInfo;
	
//	private static final String MARKET_ADDRESS = "http://192.168.33.223:8080/app_market_api/";
//	private static final String MARKET_ADDRESS = "http://61.152.133.244:8080/app_market_api/";
//	private static final String MARKET_ADDRESS = "http://222.73.237.12/app_market_api/";
//	private static final String MARKET_ADDRESS = "http://app.ursns.com/app_market_api";
	
	//生产服务器
	private static final String MARKET_ADDRESS = "http://app.ursns.com/app_market_api";

	//测试服务器
//	private static final String MARKET_ADDRESS = "http://s.ursns.com:8080/app_market_api";

	//周永本机
//	private static final String MARKET_ADDRESS = "http://192.168.38.58:8080/app-market-api-webapp";

	
	private static MarketServiceAgent mInstance;
	
	private Context mContext; 
	private int nPageSize;
	private int nRecommandAppListPageSize;
	private boolean bIsLogin;
//	private String mClientId;
	private String mDeviceId;
	private String mSubsId;

	public MarketServiceAgent(Context context) {

		this.mContext = context;
		nPageSize = Constant.LIST_COUNT_PER_TIME;
		nRecommandAppListPageSize = Constant.RECOMMANDLIST_COUNT_PER_TIME;
		bIsLogin = false;
		
		initLogin();
	}

	private void initLogin() {
		// TODO Auto-generated method stub
		SharedPreferences sharedPreferences = mContext.getSharedPreferences("Report", 0);
		String userId = sharedPreferences.getString("userId", null);

		if (!bIsLogin) {
			ClientInfo.setRootResourceUrl(MARKET_ADDRESS);
			
			mDeviceId = DeviceUtil.getIMEI(mContext);
			mSubsId = DeviceUtil.getIMSI(mContext);
//			mClientId = DeviceUtil.getClientId();
			
			ClientInfo.setDeviceId(mDeviceId);
			ClientInfo.setSubscriberId(mSubsId);
			if(userId != null)
				ClientInfo.setUserId(userId);
			
//			ClientInfo.setClientId(mClientId);
			
			bIsLogin = true;
		}
	}

	public static MarketServiceAgent getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new MarketServiceAgent(context);
		}
		return mInstance;
	}

	public Drawable[] getTopAppIcon() throws SocketException {
		BitmapDrawable[] imageTop = new BitmapDrawable[5];
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
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
			for(int i=0;i<mAdInfo.getAdInfos().size();i++){
				mTopAppIcon[i] = mAdInfo.getAdInfos().get(i).getImageUrl();
			}
			if (mTopAppIcon != null) {
				try {
					for(int i=0;i<mAdInfo.getAdInfos().size();i++){
					byte[] rawData = ServiceProvider.getImage(mTopAppIcon[i]);
					if (rawData != null ) {
						Bitmap bmp = 
							BitmapFactory.decodeByteArray(rawData, 0, rawData.length);
						imageTop[i]=new BitmapDrawable(null, bmp);
						}
					}
					return imageTop;
				} catch (Exception e) {
					e.printStackTrace();
					throw new SocketException();
				}
			}
			return null;
		}
	}

	public Drawable getAppIcon(String imgUrl) throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			byte[] rawData =null;
			Bitmap bmp = null;		
			try {
				rawData = ServiceProvider.getImage(imgUrl);
				if (rawData != null) {
//					bmp = 
//						BitmapFactory.decodeByteArray(rawData, 0, rawData.length);
//					return new BitmapDrawable(null, bmp);					
					try
					{
						BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.inJustDecodeBounds = true;
						BitmapFactory.decodeByteArray(rawData, 0, rawData.length,opts);
//						opts.inSampleSize = 2;
			            opts.inJustDecodeBounds = false;
			            opts.inInputShareable = true;
			            opts.inPurgeable = true;	
						bmp = BitmapFactory.decodeByteArray(rawData, 0, rawData.length,opts);	
						
						return new BitmapDrawable(null, bmp);
					}
					catch(OutOfMemoryError e)
					{
						e.printStackTrace();	
						Log.v("MarketServiceAgent","JimmyJin getAppIcon OutOfMemoryError:e="+e);
						return null;
					}	
				}
				return null;
			}catch(OutOfMemoryError e){
				e.printStackTrace();	
				Log.v("MarketServiceAgent","JimmyJin getAppIcon OutOfMemoryError:e1="+e);		
				return null;
			} 
			catch (Exception e) {
				throw new SocketException();
			}
		}
	}

	public Application getAppDetail(int appId) throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			try {
				Log.v("getAppDetail", "appId ="+appId);
				Application app = ApplicationProvider.getAppDetail(appId);
				Log.v("getAppDetail", "app ="+app);
				return app;
			} catch (Exception e) {
				throw new SocketException();
			}
		}
	}
	
	//Added by JimmyJin for PV
	public Application getTopicAppDetail(int appId) throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			try {
				return ApplicationProvider.getTopicAppDetail(appId);
			} catch (Exception e) {
				throw new SocketException();
			}
		}
	}

	public ArrayList<Drawable> getAppPreviews(String[] urls) throws SocketException 
	{
		
		/****************modified by daniel_whj,2012-02-03,for bug HQ00078802&HQ00078723,begin******************/		
		/*
		 these bugs were caused by function decodeByteArray,bitmap size too large,but not handle OutOfMemoryError
		 exception throwed by andriond system.
		*/
		
		ArrayList<Drawable> previews = new ArrayList<Drawable>();
		
		if(!GlobalUtil.checkNetworkState(mContext)) 
		{
			throw new SocketException();
		} 
		else 
		{
					
			byte[] rawData =null;
			Bitmap bmp = null;						
			previews.clear();//Added by JimmyJin

			for (int i = 0; i < urls.length; i++) 
			{
				try 
				{
					rawData = ServiceProvider.getImage(urls[i]);					
					if (rawData != null) 
					{						
						try
						{
							BitmapFactory.Options opts = new BitmapFactory.Options();
							opts.inJustDecodeBounds = true;
							BitmapFactory.decodeByteArray(rawData, 0, rawData.length,opts);
//							opts.inSampleSize = 2;
				            opts.inJustDecodeBounds = false;
				            opts.inInputShareable = true;
				            opts.inPurgeable = true;							
							bmp = BitmapFactory.decodeByteArray(rawData, 0, rawData.length,opts);	
							previews.add(new BitmapDrawable(null, bmp));
						}
						catch(OutOfMemoryError e)
						{
							e.printStackTrace();	
//							bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_applist_sadface);		
							return null;
					
							//previews.add(new BitmapDrawable(null, bmp));
						}																
					}
				}
				catch (Exception e) //Exception
				{	
					e.printStackTrace();	
					Log.v("MarketServiceAgent","JimmyJin Exception:e="+e);
					throw new SocketException();					
				}		
			}	
		}
		return previews;
		/****************modified by daniel_whj,2012-02-03,for bug HQ00078802&HQ00078385,end******************/
	}

	public ArrayList<Application> getAppList(int listType, int startIndex)
		throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			ArrayList<Application> appList = new ArrayList<Application>();
			ApplicationList list = null;
			// default start page is 1 not 0
			Log.v("getAppList", "startIndex ="+startIndex);
			int page = (startIndex / nPageSize) + 1;
			
			try {
				switch (listType) {
				case Constant.TYPE_APP_RECOMMAND_LIST:
					page = (startIndex / nRecommandAppListPageSize) + 1;
					Log.v("getAppList", "page ="+page);
					list = ApplicationProvider.getRecommendedList(page);
					break;
				case Constant.TYPE_APP_NEW_LIST:
					Log.v("getAppList", "page ="+page);
					list = ApplicationProvider.getNewShelvingList(page);
					break;
				default:
					break;
				}
				if (list != null && list.getApplications() != null) {
					int pageSize = Math.min(nPageSize, list.getApplications().size());
					if(listType == Constant.TYPE_APP_RECOMMAND_LIST){
						pageSize = Math.min(nRecommandAppListPageSize, list.getApplications().size());
					}
					for (int i = 0; i < pageSize; i++) {
						Log.v("getAppList", "list.getApplications().get(i) ="+list.getApplications().get(i).getAppName());
						appList.add(list.getApplications().get(i));
					}
				}
				
				return appList;
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}

	public ArrayList<Comment> getCommentList(int appId, int startIndex) throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			ArrayList<Comment> appList = new ArrayList<Comment>();
			CommentList list = null;
			// default start page is 1 not 0
			int page = (startIndex / nPageSize) + 1;
			
			try {
				list = ApplicationProvider.getComments(appId, page);
				
				if (list != null && list.getComments() != null) {
					int pageSize = Math.min(nPageSize, list.getComments().size());
					for (int i = 0; i < pageSize; i++) {
						appList.add(list.getComments().get(i));
					}
				}
				return appList;
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}

	public Comment addComment(int appId, String content, float stars, String clientId) throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			Comment comment = new Comment();
			comment.setAppId(appId);
			comment.setContent(content);
			comment.setDeviceId(mDeviceId);
			comment.setSubscriberId(mSubsId);
			comment.setClientId(clientId);
			comment.setStars((int)stars);
			try {
				return ApplicationProvider.putComment(appId, comment);
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}
	
	public Comment updateComment(Comment comment) throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			try {
				return ApplicationProvider.postComment(comment.getAppId(), comment);
				
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}

	public ArrayList<Application> getRelatedAppList(int appId, int startIndex)
		throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			ArrayList<Application> appList = new ArrayList<Application>();
			ApplicationList list = null;
			// default start page is 1 not 0
			int page = (startIndex / nPageSize) + 1;
			
			try {
				list = ApplicationProvider.getRelatedList(appId, page);
				
				if (list != null && list.getApplications() != null) {
					int pageSize = Math.min(nPageSize, list.getApplications().size());
					for (int i = 0; i < pageSize; i++) {
						appList.add(list.getApplications().get(i));
					}
				}
				return appList;
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}
	public ArrayList<Application> getAroundAppList(int totalDownloads)
			throws SocketException {
			if(!GlobalUtil.checkNetworkState(mContext)) {
				throw new SocketException();
			} else {
				ArrayList<Application> appList = new ArrayList<Application>();
				ApplicationList list = null;

				try {
					list = ApplicationProvider.getAroundList(totalDownloads);
					Log.v("asd", "getAroundList = "+list.getApplications().size());
					if (list != null && list.getApplications() != null) {
			//			int pageSize = Math.min(nPageSize, list.getApplications().size());
						for (int i = 0; i < list.getApplications().size(); i++) {
							appList.add(list.getApplications().get(i));
						}
					}
					return appList;
				} catch (Exception e) {
					e.printStackTrace();
					throw new SocketException();
				}
			}
		}

	public ArrayList<Application> getRankingAppList(int rankType, int startIndex)
		throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			ArrayList<Application> appList = new ArrayList<Application>();
			ApplicationList list = null;
			// default start page is 1 not 0
			int page = (startIndex / nPageSize) + 1;
			
			try {
				switch (rankType) {
				case Constants.RANK_TYPE_WEEK:
				case Constants.RANK_TYPE_MONTH:
				case Constants.RANK_TYPE_TOTAL:
					list = ApplicationProvider.getRankingList(rankType, page);
					break;
				default:
					break;
				}
				
				if (list != null && list.getApplications() != null) {
					int pageSize = Math.min(nPageSize, list.getApplications().size());
					for (int i = 0; i < pageSize; i++) {
						appList.add(list.getApplications().get(i));
					}
				}
				return appList;
			}catch(OutOfMemoryError e){
				e.printStackTrace();	
				Log.v("MarketServiceAgent","JimmyJin getRankingAppList OutOfMemoryError:e="+e);
				return null;
			}		 
			catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}

	public ArrayList<Application> getSortAppList(int startIndex)
			throws SocketException {
			if(!GlobalUtil.checkNetworkState(mContext)) {
				throw new SocketException();
			} else {
				ArrayList<Application> appList = new ArrayList<Application>();
				ApplicationList list = null;
				// default start page is 1 not 0
				int page = (startIndex / nPageSize) + 1;
				
				try {
					Log.v("getSortAppList", "list");
					list = ApplicationProvider.getSortAppList(page);
					Log.v("getSortAppList", "list ="+list);
					if (list != null && list.getApplications() != null) {
						int pageSize = Math.min(nPageSize, list.getApplications().size());
						for (int i = 0; i < pageSize; i++) {
							appList.add(list.getApplications().get(i));
						}
					}
					return appList;
				}catch(OutOfMemoryError e){
					e.printStackTrace();
					return null;
				}		 
				catch (Exception e) {
					e.printStackTrace();
					throw new SocketException();
				}
			}
		}
	
	public ArrayList<Application> getSortGameList(int startIndex)
			throws SocketException {
			if(!GlobalUtil.checkNetworkState(mContext)) {
				throw new SocketException();
			} else {
				ArrayList<Application> appList = new ArrayList<Application>();
				ApplicationList list = null;
				// default start page is 1 not 0
				int page = (startIndex / nPageSize) + 1;
				
				try {
					
					list = ApplicationProvider.getSortGameList(page);
					if (list != null && list.getApplications() != null) {
						int pageSize = Math.min(nPageSize, list.getApplications().size());
						for (int i = 0; i < pageSize; i++) {
							appList.add(list.getApplications().get(i));
						}
					}
					return appList;
				}catch(OutOfMemoryError e){
					e.printStackTrace();
					return null;
				}		 
				catch (Exception e) {
					e.printStackTrace();
					throw new SocketException();
				}
			}
		}
	
	public ArrayList<Application> getTypeList(int type,int categoryId,
			int orderType, int startIndex) throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			ArrayList<Application> appList = new ArrayList<Application>();
			ApplicationList list = null;
			// default start page is 1 not 0
			int page = (startIndex / nPageSize) + 1;
			
			try {
				// check order type
				if (orderType >= Constants.ORDER_TYPE_DOWNLOAD &&
						orderType <= Constants.ORDER_TYPE_DATE) {
//					list = ApplicationProvider.getCategoricalList(categoryId, orderType, page);
					Log.v("MarketServiceAgent","JimmyJin type="+type);
					list = ApplicationProvider.getTypeList(type, categoryId, orderType, page);
				}
				
				if (list != null && list.getApplications() != null) {
					int pageSize = Math.min(nPageSize, list.getApplications().size());
					for (int i = 0; i < pageSize; i++) {
						appList.add(list.getApplications().get(i));
					}
				}
				return appList;
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}

	public String getNewSubFlag() throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			try {
				return NewSubjectProvider.getNewSubject();
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}
	
	public HotWordList getSearchHotWords() throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			try {
				return ApplicationProvider.getSearchHotWords();
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}
	public appNameList getDatabase() throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			try {
				return ApplicationProvider.getAllAppNames();
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}
	 
	public ArrayList<Application> getSearchResultList(String keyWord, int startIndex)
		throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			ArrayList<Application> appList = new ArrayList<Application>();
			ApplicationList list = null;
			// default start page is 1 not 0
			int page = (startIndex / nPageSize) + 1;
			
			try {
				list = ApplicationProvider.getSearchResultList(keyWord, page);
				
				if (list != null && list.getApplications() != null) {
					int pageSize = list.getApplications().size();
					for (int i = 0; i < pageSize; i++) {
						appList.add(list.getApplications().get(i));
					}
				}
				return appList;
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}

	public ArrayList<NewSubject> getSubjectsList(int startIndex)
		throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			ArrayList<NewSubject> subList = new ArrayList<NewSubject>();
			NewSubjectList list = null;
			// default start page is 1 not 0
			int page = (startIndex / nPageSize) + 1;
			
			try {
				list = NewSubjectProvider.getNewSubjectList(page);
				
				if (list != null && list.getNewSubjects() != null) {
					int pageSize = Math.min(nPageSize, list.getNewSubjects().size());
					for (int i = 0; i < pageSize; i++) {
						subList.add(list.getNewSubjects().get(i));
					}
				}
				
				return subList;
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}
	
	public NewSubject getSubjectDetail(int startIndex)
			throws SocketException {
			if(!GlobalUtil.checkNetworkState(mContext)) {
				throw new SocketException();
			} else {
				NewSubject sub = new NewSubject();
				// default start page is 1 not 0
				
				try {
					sub = NewSubjectProvider.getNewSubjectDetail(startIndex);
					return sub;
				} catch (Exception e) {
					e.printStackTrace();
					throw new SocketException();
				}
			}
		}
	
	public String getNewSubject()
			throws SocketException {
			if(!GlobalUtil.checkNetworkState(mContext)) {
				throw new SocketException();
			} else {
				String sub = new String();
				// default start page is 1 not 0
				
				try {
					sub = NewSubjectProvider.getNewSubject();
					return sub;
				} catch (Exception e) {
					e.printStackTrace();
					throw new SocketException();
				}
			}
		}
	
	public ArrayList<NewTopic> getTopicListOfSubject(int subjectId, int startIndex)
		throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			ArrayList<NewTopic> topicList = new ArrayList<NewTopic>();
			NewTopicList list = null;
			// default start page is 1 not 0
			//int page = (startIndex / nPageSize) + 1;
			int page = 1;
			try {
				list = NewSubjectProvider.getNewTopicListOfSubject(subjectId, page);
				
				if (list != null && list.getNewTopics() != null) {
//					int pageSize = Math.min(nPageSize, list.getNewTopics().size());
					int pageSize = list.getNewTopics().size();
					for (int i = 0; i < pageSize; i++) {
						topicList.add(list.getNewTopics().get(i));
					}
				}
				Log.v("asd", "getTopicListOfSubject list = "+list);
				Log.v("asd", "getTopicListOfSubject page = "+page);
				Log.v("asd", "getTopicListOfSubject topicList = "+topicList.size());
				return topicList;
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}

	public ArrayList<Sort> getCategoriesList(int categoryId)
		throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			ArrayList<Sort> cateList = new ArrayList<Sort>();
			SortList list = null;
			
			try {
				list = SortProvider.getSortsList(categoryId);
				if (list != null && list.getSorts() != null) {
	//				int pageSize = Math.min(nPageSize, list.getCategories().size());
	//				for (int i = 0; i < pageSize; i++) {
	//					cateList.add(list.getCategories().get(i));
	//				}
					return (ArrayList<Sort>)list.getSorts();
				}
				
				return cateList;
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}
	
	public ArrayList<Sort> getSortsList(int categoryId)
			throws SocketException {
			if(!GlobalUtil.checkNetworkState(mContext)) {
				throw new SocketException();
			} else {
				ArrayList<Sort> cateList = new ArrayList<Sort>();
				SortList list = null;
				Log.v("getSortsList", "categoryId ="+categoryId);
				try {
					list = SortProvider.getSortsList(categoryId);
					if (list != null && list.getSorts() != null) {
		//				int pageSize = Math.min(nPageSize, list.getCategories().size());
		//				for (int i = 0; i < pageSize; i++) {
		//					cateList.add(list.getCategories().get(i));
		//				}
						return (ArrayList<Sort>)list.getSorts();
					}
					
					return cateList;
				} catch (Exception e) {
					e.printStackTrace();
					throw new SocketException();
				}
			}
		}

	public Drawable getTopicAppIcon(int appId, String imgUrl) throws SocketException {
		// TODO Auto-generated method stub
		Drawable drawable = FileManager.readAppIconFromFile(mContext, appId);
		
		if (drawable == null) {
			drawable = getAppIcon(imgUrl);
		}
		return drawable;
	}

	public UpdateStates checkSelfUpdate(int version) throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			UpdateStates update = null;
			
			try {
				update = ServiceProvider.checkUpdate(version);
				
				return update;
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}
	
	public String getNotice() throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			String notice = null;
			
			try {
				notice = ServiceProvider.getMarketNotice();
				Log.v("getNotice", "notice ="+notice);
				return notice;
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}
	
	public ArrayList<Application> checkAppUpdate(InstalledApp[] installedApps)
		throws SocketException {
		if(!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			ArrayList<Application> appList = new ArrayList<Application>();
			ApplicationList list = null;
			Log.v("asd", "checkAppUpdate");
			try {
				list = ApplicationProvider.getUpdateListNew(installedApps);
				
				if (list != null && list.getApplications() != null) {
					int pageSize = list.getApplications().size();
					for (int i = 0; i < pageSize; i++) {
						appList.add(list.getApplications().get(i));
					}
				}
				return appList;
			} catch (Exception e) {
				e.printStackTrace();
				throw new SocketException();
			}
		}
	}

	public Comment postComment(int appId, String content) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String postMarketComment(String userId,String content) throws SocketException {
		// TODO Auto-generated method stub
		try {
			ServiceProvider.postMarketComment(userId,content);
		}  catch (Exception e) {
			e.printStackTrace();
			throw new SocketException();
		}
		return content;
	}
	public String postVersion(String userId,String version)throws SocketException {
		try {
			ServiceProvider.postVersion(userId,version);
		}  catch (Exception e) {
			e.printStackTrace();
			throw new SocketException();
		}
		return version;
	}
	public Partner postPartner(String phoneModel,String version)throws SocketException {
		Partner partner = null;
		try {
			Log.v("postPartner", "phoneModel ="+phoneModel);
			Log.v("postPartner", "version ="+version);
			partner = ServiceProvider.postPartner(phoneModel,version);
			Log.v("postPartner ", "partner ="+partner.getName());
			Log.v("postPartner ", "partner ="+partner.getPattern());
			Log.v("postPartner ", "partner ="+partner.isHavePush());
		}  catch (Exception e) {
			e.printStackTrace();
			throw new SocketException();
		}
		Log.v("postPartner ", "partner2 ="+partner);
		
		return partner;
	}
	
	public String postPV(String phoneModel,String version,String pageName)throws SocketException {
		try {
			Log.v("postPV", "phoneModel ="+phoneModel);
			Log.v("postPV", "version ="+version);
			Log.v("postPV", "pageName ="+pageName);
			ClientPvProvider.clientPv(phoneModel,version,pageName);
		}  catch (Exception e) {
			e.printStackTrace();
			throw new SocketException();
		}
		return pageName;
	}
	
	public String postSubjComment(String subjId,String up,String down) throws SocketException {
		// TODO Auto-generated method stub
		try {
			NewSubjectProvider.postNewSubjComment(subjId,up,down);
		}  catch (Exception e) {
			e.printStackTrace();
			throw new SocketException();
		}
		return subjId;
	}	
	
	/*************Added-s by JimmyJin**************/
	public String userRegister(String deviceId, 
			String subscriberId,
			String softId, 
			String messageId,
			String phoneModel) throws SocketException{
		// TODO Auto-generated method stub	
		try {
			return ServiceProvider.postUserInfo(deviceId, subscriberId, softId, messageId, phoneModel);
		}  catch (Exception e) {
			e.printStackTrace();
			throw new SocketException();
		}
	}
	/*************Added-e by JimmyJin**************/	
	
	/****************Added-s by JimmyJin 20120509*****************/
	public void postDownloadBegin(String userId, 
			String appId,
			String appPackage, 
			String fromWhere) throws SocketException{
		// TODO Auto-generated method stub	
		try {
			ReportProvider.postDownloadBeginEx(userId, appId, appPackage, fromWhere);
		}  catch (Exception e) {
			e.printStackTrace();
			throw new SocketException();
		}
	}
	
	
	public void postDownloadEnd(String userId, 
			String appId,
			String appPackage, 
			String fromWhere) throws SocketException{
		// TODO Auto-generated method stub	
		try {
			ReportProvider.postDownloadEndEx(userId, appId, appPackage, fromWhere);
		}  catch (Exception e) {
			e.printStackTrace();
			throw new SocketException();
		}
	}
	
	public void postInstallInfo(String userId, 
			String appId,
			String appPackage, 
			String fromWhere) throws SocketException{
		// TODO Auto-generated method stub	
		try {
			ReportProvider.postInstallInfoEx(userId, appId, appPackage, fromWhere);
		}  catch (Exception e) {
			e.printStackTrace();
			throw new SocketException();
		}
	}
	public void postRebuildContext() throws SocketException{
		// TODO Auto-generated method stub	
		try {
			BaseResource.rebuildContext();
			Log.v("RequestHandler","JimmyJin BaseResource.rebuildContext()!");
		}  catch (Exception e) {
			e.printStackTrace();
			throw new SocketException();
		}
	}
	/****************Added-e by JimmyJin 20120509*****************/
}