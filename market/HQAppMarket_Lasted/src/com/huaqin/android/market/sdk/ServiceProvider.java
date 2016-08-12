package com.huaqin.android.market.sdk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import com.huaqin.android.market.sdk.bean.AdInfo;
import com.huaqin.android.market.sdk.bean.AdInfoList;
import com.huaqin.android.market.sdk.bean.AuthStates;
import com.huaqin.android.market.sdk.bean.Partner;
import com.huaqin.android.market.sdk.bean.UpdateStates;
import com.huaqin.android.market.sdk.rest.BaseResource;

/**
 * 公共服务信息获取
 * @author duanweiming
 *
 */
public class ServiceProvider extends BaseResource{
    
    /**
     * 用户登陆鉴权
     * @param userId
     * @param password
     * @return
     */
    public static AuthStates authenticate(String memberId,String password) throws IOException,HttpException{
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("member_id", memberId));
        formParams.add(new BasicNameValuePair("password", password));
        return post(ClientInfo.RESOURCE_ROOT_URL+"/service/auth",formParams,AuthStates.class);
    }
    

    
    /**
     * 客户端版本更新
     *      
     */
	public static void postVersion(String userId,String version) throws IOException, HttpException{	
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("user_id", userId));
        formParams.add(new BasicNameValuePair("version", version));
	  post(ClientInfo.RESOURCE_ROOT_URL + "/service/version",formParams);
	}
    
    /**
     * 第三方合作接口(邮乐)
     *   @param version  装机精灵版本号
     *   @param  phoneModel 设备型号
     *   @return Partner(name,havaPush,pattern,urls)返回类Partner包含合作第三方名称、和是否有push,合作方式、webview url 
     */
	public static Partner postPartner(String phoneModel,String version) throws IOException, HttpException{	
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("phone_model", phoneModel));
        formParams.add(new BasicNameValuePair("version", version));
	  return post(ClientInfo.RESOURCE_ROOT_URL + "/service/partner",formParams,Partner.class);
	}
	
    /**
     * 获取首页广告信息
     * @return
     *      AdInfo
     */
    public static AdInfo  getAdInfo() throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/service/ad",AdInfo.class);
    }
    /**
     * 获取多个广告信息
     * 
     * @return
     *      AdInfoList
     */
    public static AdInfoList  getAdInfoList() throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/service/ads",AdInfoList.class);
    }
    /**
     * 根据adId获取某个广告信息
     * @param adId
     * @return
     *      AdInfoList
     */
    public static AdInfo  getOneAdInfo(int adId) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/service/ad/"+adId,AdInfo.class);
    }
    /**
     * 获取多个广告信息（新3.1）
     * 
     * @return
     *      AdInfoList
     */
    public static AdInfoList  getAdInfoListNew() throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/service/adsnew",AdInfoList.class);
    }
    
    /**
     * 检查客户端更新
     * @return
     *      UpdateStates
     */
    public static UpdateStates checkUpdate(int clientVersionCode) throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/service/update?client_version="+clientVersionCode,UpdateStates.class);
    }
    
    /**
     * 系统通知
     * @return
     */
    public static String getMarketNotice() throws IOException,HttpException{
        return get(ClientInfo.RESOURCE_ROOT_URL+"/service/marketnotice",String.class);
    }
    /**
     * 获取图片
     * @param url
     * @return
     */
    public static byte[] getImage(String url)throws IOException,HttpException{
        return getRawData(url, "image/*");
    }

	public static String postUserInfo(String deviceId, 
			String subscriberId,
			String softId, 
			String messageId,
			String phoneModel)
			throws Exception, HttpException {
		 	List<NameValuePair> formParams = new ArrayList<NameValuePair>();
	        formParams.add(new BasicNameValuePair("device_id", deviceId));
	        formParams.add(new BasicNameValuePair("subscriber_id", subscriberId));
	        formParams.add(new BasicNameValuePair("soft_id", softId));
	        formParams.add(new BasicNameValuePair("message_id", messageId));
	        formParams.add(new BasicNameValuePair("phone_model", phoneModel));     
		return post(ClientInfo.RESOURCE_ROOT_URL + "/service/register",formParams,String.class);
	}
	/**
     * 新注册postUserInfoEx，增加fromWhere
     * @param fromWhere 0:装机精灵 ; 1:布丁控 ; 2:一键安装;3AngelPush
     * @return
     */
	public static String postUserInfoEx(String deviceId, 
			String subscriberId,
			String softId, 
			String messageId,
			String phoneModel,
			String fromWhere)
			throws Exception, HttpException {
		 	List<NameValuePair> formParams = new ArrayList<NameValuePair>();
	        formParams.add(new BasicNameValuePair("device_id", deviceId));
	        formParams.add(new BasicNameValuePair("subscriber_id", subscriberId));
	        formParams.add(new BasicNameValuePair("soft_id", softId));
	        formParams.add(new BasicNameValuePair("message_id", messageId));
	        formParams.add(new BasicNameValuePair("phone_model", phoneModel));     
	        formParams.add(new BasicNameValuePair("from_where", fromWhere));     
		return post(ClientInfo.RESOURCE_ROOT_URL + "/service/registerex",formParams,String.class);
	}
	/**
     * Market Comment 装机精灵意见反馈接口
     * @param userId 用户注册userId
     * @param content 内容
     * 
     */
	public static void postMarketComment(String userId,String content) throws IOException, HttpException{	
		
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("user_id", userId));
        formParams.add(new BasicNameValuePair("content", content));
        
	  post(ClientInfo.RESOURCE_ROOT_URL + "/service/marketcomment",formParams);
	}
}
