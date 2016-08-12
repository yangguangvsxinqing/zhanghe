package com.fineos.volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;

import com.fineos.android.rom.sdk.ClientInfo;
import com.fineos.theme.model.ThemeData;
import com.fineos.theme.utils.Constant;

public class VolleyURLBuilder{
	
	/**
	 * 获取ID单个主题详细信息
	 * 
	 * @param appId
	 * @return
	 */
	public static String getThemeDetail(int themeId){
		return ClientInfo.RESOURCE_ROOT_URL + "/theme/" + themeId;
	} 
	
	/**
	 * 获取主题列表
	 * 
	 * @param type
	 *            主题类型：主题0 锁屏1 动态壁纸2 字体3 图标4 壁纸5
	 * @param page
	 * @return ThemeList
	 */
	public static String getTopThemeList(int themeType, int page){
		return ClientInfo.RESOURCE_ROOT_URL + "/theme/" + themeType + "/topthemes/" + page;
	}
	/**
     * 获取主题列表
     * @param type 主题类型：0.主题  1.锁屏  2.动态壁纸  3.字体 4.图标 5.壁纸
     * @param startIndex
     */
    public static String getThemeList(int themeType,int startIndex){
    	
    	int pageSize = 0;
    	if(themeType == Constant.ONLINE_THEME_WALLPAPER_TYPE || themeType == ThemeData.THEME_ELEMENT_TYPE_WALLPAPER){
    		pageSize = Constant.WALLPAPER_LIST_COUNT_PER_TIME;
		}else{
			pageSize = Constant.THEME_LIST_COUNT_PER_TIME;
		}
    	int page = (startIndex / pageSize) + 1;
        return ClientInfo.RESOURCE_ROOT_URL+"/theme/"+themeType+"/themes/"+page+"/"+pageSize;
    }
	/**
	 * 主题包验证 param title标题 param version版本号 param size 大小 return "0或1"
	 */
	public static String verifyTheme(String title, String version, String size){
		return ClientInfo.RESOURCE_ROOT_URL + "/theme/verifytheme";
	}

	public static String getSubjectAd(){
		  return ClientInfo.RESOURCE_ROOT_URL+"/theme/ad";
	}
	
	public static String postSubjectAd(){
		return ClientInfo.RESOURCE_ROOT_URL + "/theme/ad";
	}
	
	public static String subjectAdRequestData(String appPackage,String phoneModel,String countryCode,String channelInfo){
		HashMap<String, String> map = new HashMap<String, String>();
	        map.put("app_package", appPackage);  
	        map.put("phone_model", phoneModel);  
	        map.put("country_code", countryCode);
	        map.put("channel_info", channelInfo);
	     return mapEntryBuild(map);
	}
	
	public static String postUserTheme(){
		return ClientInfo.RESOURCE_ROOT_URL + "/reports/usertheme";
	}
	public static String userThemeRequestData(String userId, String packageName, int sort){
		HashMap<String, String> map = new HashMap<String, String>();
		
	     map.put("user_id", userId);  
	     map.put("package_name", packageName);  
	     map.put("sort", sort + "");
	     return mapEntryBuild(map);
	}

	//获取开屏广告
	public static String getSplashAd(){
		  return ClientInfo.RESOURCE_ROOT_URL+"/theme/ad/splash";
	}
	
	/**
     * 主题点赞接口
     */
    public static String postDianZan(){
    	return ClientInfo.RESOURCE_ROOT_URL + "/theme/dianzan";
    }
    
    public static String postLikeRequestData(String themeId){
		String str = "theme_id=";
	     return str+themeId;
	}
    
    private static String mapEntryBuild(HashMap<String,String> map){
    	String dataBody = null;
    	if (map != null) {
            Uri.Builder builder = new Uri.Builder();
            for (HashMap.Entry<String, String> entry : map.entrySet()) {
              builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
            dataBody = builder.build().getQuery();
        }
    	return dataBody;
    }
}