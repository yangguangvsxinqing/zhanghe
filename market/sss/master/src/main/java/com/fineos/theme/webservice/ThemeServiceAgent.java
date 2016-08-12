package com.fineos.theme.webservice;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
//
//import org.apache.http.HttpException;
import org.w3c.dom.Comment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

//import com.fineos.android.rom.sdk.RingToneProvider;
//import com.fineos.android.rom.sdk.ServiceProvider;
//import com.fineos.android.rom.sdk.ThemeProvider;
//import com.fineos.android.rom.sdk.WallPaperProvider;
import com.fineos.android.rom.sdk.bean.RingToneList;
import com.fineos.android.rom.sdk.bean.SubjectAdList;
import com.fineos.android.rom.sdk.bean.ThemeList;
import com.fineos.android.rom.sdk.bean.WallPaperList;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.utils.Constant;
import com.fineos.theme.utils.GlobalUtil;
import com.fineos.theme.utils.ThemeLog;

public class ThemeServiceAgent {

//	// 阿里云服务器
//	public static String RESOURCE_ROOT_URL = "http://theme.fineos.cn:8080/fineos-theme-api";

//	public static String RESOURCE_ROOT_URL = "http://theme.enjoyui.com:8080/fineos-theme-api";
	
	// 测试服务器
	public static String RESOURCE_ROOT_URL = "http://weather.huaqin.com:8080/fineos-theme-api";

	private static ThemeServiceAgent mInstance;
	private int nPageSize;
	private Context mContext;
	private boolean bIsLogin;
	// private String mClientId;
	private String mDeviceId;
	private String mSubsId;

	private ThemeServiceAgent(Context context) {

		this.mContext = context;
		// nRecommandAppListPageSize = Constant.RECOMMANDLIST_COUNT_PER_TIME;
		bIsLogin = false;

	}

	public static ThemeServiceAgent getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ThemeServiceAgent(context);
		}
		return mInstance;
	}

	// public Comment addComment(int appId, String content, float stars, String
	// clientId) throws SocketException {
	// if(!GlobalUtil.checkNetworkState(mContext)) {
	// throw new SocketException();
	// } else {
	// Comment comment = new Comment();
	// comment.setAppId(appId);
	// comment.setContent(content);
	// comment.setDeviceId(mDeviceId);
	// comment.setSubscriberId(mSubsId);
	// comment.setClientId(clientId);
	// comment.setStars((int)stars);
	// try {
	// return ServiceProvider.postUserComment(appId, comment);
	// } catch (Exception e) {
	// e.printStackTrace();
	// throw new SocketException();
	// }
	// }
	// }

	public Comment postComment(int appId, String content) {
		// TODO Auto-generated method stub
		return null;
	}

	public ThemeList getThemeList(int listType, int startIndex) throws SocketException {
		if(listType == Constant.ONLINE_THEME_WALLPAPER_TYPE || listType == ThemeData.THEME_ELEMENT_TYPE_WALLPAPER){
			nPageSize = Constant.WALLPAPER_LIST_COUNT_PER_TIME;
		}else{
			nPageSize = Constant.THEME_LIST_COUNT_PER_TIME;
		}
		if (!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			ThemeList themeList = new ThemeList();

			ThemeLog.i("getThemeList", "startIndex =" + startIndex);
			int page = (startIndex / nPageSize) + 1;

			ThemeLog.i("getThemeList", "page =" + page);
			try {
	//			themeList = ThemeProvider.getThemeList(listType, page,nPageSize);
			} catch (Exception e) {

				throw new SocketException();
			}
			return themeList;
		}
	}
	
	public SubjectAdList getThemeADList() throws SocketException {
		    SubjectAdList subjectAdList = null;
			try {
				ThemeLog.v("getThemeADList","getThemeADList");
		//		subjectAdList = ThemeProvider.getSubjectAd();
		//		ThemeLog.v("getThemeADList","getThemeADList ="+subjectAdList.getSubjectAds().size());
			} catch (Exception e) {

				throw new SocketException();
			}
			return subjectAdList;
	}

	public SubjectAdList postSubjectAd(String pageName, String phoneModel, String countryCode,String channelId) throws SocketException {
		SubjectAdList subjectAdList = null;
		try {
	//		subjectAdList = ThemeProvider.postSubjectAd(pageName,phoneModel,countryCode,channelId);
		} catch (Exception e) {

			throw new SocketException();
		} catch (NoSuchMethodError e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new SocketException();
		}
		return subjectAdList;
	}

	public RingToneList getRingToneList(int startIndex) throws SocketException {
		nPageSize = Constant.THEME_LIST_COUNT_PER_TIME;
		if (!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			RingToneList ringToneList = new RingToneList();
			// default start page is 1 not 0
			int page = (startIndex / nPageSize) + 1;

//			try {
//				ringToneList = RingToneProvider.getRingToneList(page);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (HttpException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			return ringToneList;
		}
	}

	public String getVerify(String[] verifyData) throws SocketException {
		String verifyType = "0";
//		if (!GlobalUtil.checkNetworkState(mContext)) {
//			throw new SocketException();
//		} else {
//			try {
//				verifyType = ThemeProvider.verifyTheme(verifyData[0], verifyData[1], verifyData[2]);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (HttpException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			return verifyType;
	//	}
	}
	
	public void postDianZan(String themeId) throws SocketException {
		if (!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
//			try {
//				Log.v("", "ssss postDianZan themeId ="+themeId);
//				ThemeProvider.postDianZan(themeId);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (HttpException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}
	
	public WallPaperList getImageList(int listType, int startIndex) throws SocketException {
		nPageSize = Constant.THEME_LIST_COUNT_PER_TIME;
		if (!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			WallPaperList imageList = new WallPaperList();
			// default start page is 1 not 0
			ThemeLog.i("getThemeList", "startIndex =" + startIndex);
			int page = (startIndex / nPageSize) + 1;

			ThemeLog.i("getThemeList", "page =" + page);
//			try {
//				imageList = WallPaperProvider.getWallPaperList(listType, page);
//				ThemeLog.i("TYPE_IMAGE_LIST", "imageList id=" + imageList.getWallPaperInfos().get(0).getPaperId());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (HttpException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			return imageList;
		}
	}

	public Bitmap getAppIcon(String imgUrl) throws SocketException {
		if (!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {
			byte[] rawData = null;
			Bitmap bmp = null;
//			try {
//				rawData = ServiceProvider.getImage(imgUrl);
//				ThemeLog.i("getAppIcon", "rawData =" + rawData + "imgUrl: " + imgUrl);
//				if (rawData != null) {
//					// bmp =
//					// BitmapFactory.decodeByteArray(rawData, 0,
//					// rawData.length);
//					// return new BitmapDrawable(null, bmp);
//					try {
//						BitmapFactory.Options opts = new BitmapFactory.Options();
//						opts.inJustDecodeBounds = true;
//						BitmapFactory.decodeByteArray(rawData, 0, rawData.length, opts);
//						// opts.inSampleSize = 2;
//						opts.inJustDecodeBounds = false;
//						opts.inInputShareable = true;
//						opts.inPurgeable = true;
//						bmp = BitmapFactory.decodeByteArray(rawData, 0, rawData.length, opts);
//
//						return bmp;
//					} catch (OutOfMemoryError e) {
//						e.printStackTrace();
//						return null;
//					}
//				}
//				return null;
//			} catch (OutOfMemoryError e) {
//				e.printStackTrace();
//				return null;
//			} catch (Exception e) {
//				throw new SocketException();
//			}
		}
		////add 
		return null;
	}

	public ArrayList<Bitmap> getThemePreviews(String[] urls) throws SocketException {
		ArrayList<Bitmap> previews = new ArrayList<Bitmap>();

		if (!GlobalUtil.checkNetworkState(mContext)) {
			throw new SocketException();
		} else {

			byte[] rawData = null;
			Bitmap bmp = null;
			previews.clear();

			for (int i = 0; i < urls.length; i++) {
				try {
//					rawData = ServiceProvider.getImage(urls[i]);
//					if (rawData != null) {
//						try {
//							BitmapFactory.Options opts = new BitmapFactory.Options();
//							opts.inJustDecodeBounds = true;
//							BitmapFactory.decodeByteArray(rawData, 0, rawData.length, opts);
////							opts.inSampleSize = 2;
//							opts.inJustDecodeBounds = false;
//							opts.inInputShareable = true;
//							opts.inPurgeable = true;
//							bmp = BitmapFactory.decodeByteArray(rawData, 0, rawData.length, opts);
//							previews.add(bmp);
//						} catch (OutOfMemoryError e) {
//							e.printStackTrace();
////							bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_applist_sadface);		
//							return null;
//
//							// previews.add(new BitmapDrawable(null, bmp));
//						}
//					}
				} catch (Exception e) // Exception
				{
					e.printStackTrace();
					throw new SocketException();
				}
			}
		}
		return previews;
	}
}
