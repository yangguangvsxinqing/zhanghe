//package com.fineos.android.rom.sdk;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.http.HttpException;
//import org.apache.http.NameValuePair;
//import org.apache.http.message.BasicNameValuePair;
//
//import com.fineos.android.rom.sdk.rest.BaseResource;
//
//public class ServiceProvider extends BaseResource {
//	/**
//	 * 获取图片
//	 * 
//	 * @param url
//	 * @return
//	 */
////	public static byte[] getImage(String url) throws IOException, HttpException {
////		return getRawData(url, "image/*");
////	}
////
////	/**
////	 * 用户评论
////	 */
////	public static void postUserComment(String userId, String loginId, String appId, String phoneModel, String stars, String content, String channel) throws IOException, HttpException {
////		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
////		formParams.add(new BasicNameValuePair("userId", userId));
////		formParams.add(new BasicNameValuePair("loginId", loginId));
////		formParams.add(new BasicNameValuePair("appId", appId));
////		formParams.add(new BasicNameValuePair("stars", stars));
////		formParams.add(new BasicNameValuePair("phoneModel", phoneModel));
////		formParams.add(new BasicNameValuePair("content", content));
////		formParams.add(new BasicNameValuePair("channel", channel));
////		post(ClientInfo.RESOURCE_ROOT_URL + "/service/usercomment", formParams);
////	}
////
////	/**
////	 * 用户反馈
////	 */
////	public static void postUserFeedback(String userId, String loginId, String appId, String phoneModel, String content, String channel) throws IOException, HttpException {
////		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
////		formParams.add(new BasicNameValuePair("userId", userId));
////		formParams.add(new BasicNameValuePair("loginId", loginId));
////		formParams.add(new BasicNameValuePair("appId", appId));
////		formParams.add(new BasicNameValuePair("phoneModel", phoneModel));
////		formParams.add(new BasicNameValuePair("content", content));
////		formParams.add(new BasicNameValuePair("channel", channel));
////		post(ClientInfo.RESOURCE_ROOT_URL + "/service/userfeedback", formParams);
////	}
////
////	/**
////	 * 用户log上传
////	 */
////	public static void postUserLog(String userId, String loginId, String appId, String phoneModel, String content, String appVersion, String osVersion, String channel) throws IOException,
////			HttpException {
////		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
////		formParams.add(new BasicNameValuePair("userId", userId));
////		formParams.add(new BasicNameValuePair("loginId", loginId));
////		formParams.add(new BasicNameValuePair("appId", appId));
////		formParams.add(new BasicNameValuePair("phoneModel", phoneModel));
////		formParams.add(new BasicNameValuePair("content", content));
////		formParams.add(new BasicNameValuePair("appVersion", appVersion));
////		formParams.add(new BasicNameValuePair("osVersion", osVersion));
////		formParams.add(new BasicNameValuePair("channel", channel));
////		post(ClientInfo.RESOURCE_ROOT_URL + "/service/userlog", formParams);
////	}
//}
