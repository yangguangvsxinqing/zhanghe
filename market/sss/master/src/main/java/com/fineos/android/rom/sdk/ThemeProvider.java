//package com.fineos.android.rom.sdk;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
////
////import org.apache.http.HttpException;
////import org.apache.http.NameValuePair;
////import org.apache.http.message.BasicNameValuePair;
////
//
//import com.fineos.android.rom.sdk.bean.SubjectAdList;
//import com.fineos.android.rom.sdk.bean.Theme;
//import com.fineos.android.rom.sdk.bean.ThemeList;
//import com.fineos.android.rom.sdk.rest.BaseResource;
//
///**
// * 主题客户端信息
// * 
// * @author zhouyong
// * 
// */
//
//public class ThemeProvider extends BaseResource {
//	/**
//	 * 获取ID单个主题详细信息
//	 * 
//	 * @param appId
//	 * @return
//	 */
////	public static Theme getThemeDetail(int themeId) throws IOException, HttpException {
////		return get(ClientInfo.RESOURCE_ROOT_URL + "/theme/" + themeId, Theme.class);
////	}
////
////	/**
////	 * 获取主题列表
////	 * 
////	 * @param type
////	 *            主题类型：主题0 锁屏1 动态壁纸2 字体3 图标4 壁纸5
////	 * @param page
////	 * @return ThemeList
////	 */
////	public static ThemeList getTopThemeList(int themeType, int page) throws IOException, HttpException {
////		return get(ClientInfo.RESOURCE_ROOT_URL + "/theme/" + themeType + "/topthemes/" + page, ThemeList.class);
////	}
////	 /**
////     * 获取主题列表
////     * @param type 主题类型：0.主题  1.锁屏  2.动态壁纸  3.字体 4.图标 5.壁纸
////     * @param page
////     * @param pageSize
////     * @return ThemeList
////     */
////    public static ThemeList getThemeList(int themeType,int page,int pageSize) throws IOException,HttpException{
////        return get(ClientInfo.RESOURCE_ROOT_URL+"/theme/"+themeType+"/themes/"+page+"/"+pageSize,ThemeList.class);
////    }
////	/**
////	 * 主题包验证 param title标题 param version版本号 param size 大小 return "0或1"
////	 */
////	public static String verifyTheme(String title, String version, String size) throws IOException, HttpException {
////		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
////		formParams.add(new BasicNameValuePair("title", title));
////		formParams.add(new BasicNameValuePair("version", version));
////		formParams.add(new BasicNameValuePair("size", size));
////		return post(ClientInfo.RESOURCE_ROOT_URL + "/theme/verifytheme", formParams, String.class);
////	}
////
////	public static SubjectAdList getSubjectAd() throws IOException, HttpException {
////		  return get(ClientInfo.RESOURCE_ROOT_URL+"/theme/ad",SubjectAdList.class);
////	}
////	
////	public static SubjectAdList postSubjectAd(String appPackage,String phoneModel,String countryCode,String channelInfo) throws IOException, HttpException {
////		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
////		 formParams.add(new BasicNameValuePair("app_package", appPackage));
////		 formParams.add(new BasicNameValuePair("phone_model", phoneModel));
////		 formParams.add(new BasicNameValuePair("country_code", countryCode));
////	     formParams.add(new BasicNameValuePair("channel_info", channelInfo));
////		 return post(ClientInfo.RESOURCE_ROOT_URL + "/theme/ad",formParams,SubjectAdList.class);
////	}
////
////	
////	public static String getSplashAd() throws IOException, HttpException {
////		  return get(ClientInfo.RESOURCE_ROOT_URL+"/theme/ad/splash",String.class);
////	}
////	
////	/**
////     * 主题点赞接口
////     */
////    public static void postDianZan(String themeId) throws IOException,HttpException{
////    	List<NameValuePair> formParams = new ArrayList<NameValuePair>();
////        formParams.add(new BasicNameValuePair("theme_id", themeId));
////        post(ClientInfo.RESOURCE_ROOT_URL + "/theme/dianzan",formParams);
////    }
//}
