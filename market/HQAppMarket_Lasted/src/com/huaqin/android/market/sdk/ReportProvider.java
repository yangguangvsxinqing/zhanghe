package com.huaqin.android.market.sdk;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import com.huaqin.android.market.sdk.rest.BaseResource;

public class ReportProvider extends BaseResource {
	 /**
     * @param fromWhere 0:装机精灵 ; 1:布丁控 ; 2:一键安装;3AngelPush

     * @return
     */
	//增强报告无返回值
	public static void postDownloadBeginEx(String userId,String appId,String appPackage,String fromWhere) throws IOException, HttpException{	
		
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("user_id", userId));
        formParams.add(new BasicNameValuePair("app_id", appId));
        formParams.add(new BasicNameValuePair("app_package", appPackage));
        formParams.add(new BasicNameValuePair("from_where", fromWhere));
        
	  post(ClientInfo.RESOURCE_ROOT_URL + "/reports/downloadbeginex",formParams);
	}
	
	public static void postDownloadEndEx(String userId,String appId,String appPackage,String fromWhere) throws IOException, HttpException{	
		
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("user_id", userId));
        formParams.add(new BasicNameValuePair("app_id", appId));
        formParams.add(new BasicNameValuePair("app_package", appPackage));
        formParams.add(new BasicNameValuePair("from_where", fromWhere));
        post(ClientInfo.RESOURCE_ROOT_URL + "/reports/downloadendex",formParams);
	}
	public static void postInstallInfoEx(String userId,String appId,String appPackage,String fromWhere) throws IOException, HttpException{	
		
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("user_id", userId));
        formParams.add(new BasicNameValuePair("app_id", appId));
        formParams.add(new BasicNameValuePair("app_package", appPackage));
        formParams.add(new BasicNameValuePair("from_where", fromWhere));
		post(ClientInfo.RESOURCE_ROOT_URL + "/reports/installinfoex",formParams);
	}
	
	//老的有返回值
	public static String postDownloadBegin(String userId,String appId,String appPackage,String fromWhere) throws IOException, HttpException{	
		
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("user_id", userId));
        formParams.add(new BasicNameValuePair("app_id", appId));
        formParams.add(new BasicNameValuePair("app_package", appPackage));
        formParams.add(new BasicNameValuePair("from_where", fromWhere));
        
        return post(ClientInfo.RESOURCE_ROOT_URL + "/reports/downloadbegin",formParams,String.class);
	}
	
	public static String postDownloadEnd(String userId,String appId,String appPackage,String fromWhere) throws IOException, HttpException{	
		
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("user_id", userId));
        formParams.add(new BasicNameValuePair("app_id", appId));
        formParams.add(new BasicNameValuePair("app_package", appPackage));
        formParams.add(new BasicNameValuePair("from_where", fromWhere));
        return post(ClientInfo.RESOURCE_ROOT_URL + "/reports/downloadend",formParams,String.class);
	}
	public static String postInstallInfo(String userId,String appId,String appPackage,String fromWhere) throws IOException, HttpException{	
		
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("user_id", userId));
        formParams.add(new BasicNameValuePair("app_id", appId));
        formParams.add(new BasicNameValuePair("app_package", appPackage));
        formParams.add(new BasicNameValuePair("from_where", fromWhere));
        return post(ClientInfo.RESOURCE_ROOT_URL + "/reports/installinfo",formParams,String.class);
	}
	
	
	/*public static String postActivateInfo(String userId,String appId,String appPackage) throws IOException, HttpException{	
		
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("user_id", userId));
        formParams.add(new BasicNameValuePair("app_id", appId));
        formParams.add(new BasicNameValuePair("app_package", appPackage));
        
	return post(ClientInfo.RESOURCE_ROOT_URL + "/reports/activateinfo",formParams,String.class);
	}*/
	/*public static void main(String[] args) {
		try {
			postDownloadBegin("ddddd","22","222","1");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (HttpException e) {
			e.printStackTrace();
		}
	}*/
}
