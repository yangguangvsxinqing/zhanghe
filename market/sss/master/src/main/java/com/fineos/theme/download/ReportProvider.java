package com.fineos.theme.download;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.fineos.android.rom.sdk.ClientInfo;
import com.fineos.android.rom.sdk.bean.SubjectAdList;
import com.fineos.theme.baidusdk.ThemeApplication;
import com.fineos.theme.utils.Constant;
import com.fineos.volley.GsonPostRequest;
import com.fineos.volley.VolleyURLBuilder;

public class ReportProvider{

	public synchronized static void postUserTheme(final String packageName, final int sort) {
//		new Thread() {
//			@Override
//			public void run() {
//				try {
//					ThemeLog.e("ReportProvider", "postUserTheme: " + packageName);
			postUserTheme(ClientInfo.getUserId(), "" + packageName, sort);
					
//				} catch (Exception e) {
//
//				}
//			}
//		}.start();
	}
	
	

	/**
	 * 主题用户数据接口
	 * 
	 * @param packageName
	 *            主题包名
	 * @param sort
	 *            分类 0：点击 1：开始下载 2：下载完成 3：使用
	 * @return
	 */

	private static void postUserTheme(String userId, String packageName, int sort){
		GsonPostRequest<SubjectAdList> mGsonPostRequest = new GsonPostRequest<SubjectAdList>(
				VolleyURLBuilder.postUserTheme(), 
				VolleyURLBuilder.userThemeRequestData(userId,packageName,sort), 
				SubjectAdList.class, 
				new Listener<SubjectAdList>(){
					@Override
					public void onResponse(SubjectAdList response) {
						// TODO Auto-generated method stub
						
					}
				}, 
				new ErrorListener(){
					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
					}
				});
		ThemeApplication.getInstance().addToRequestQueue(mGsonPostRequest,Constant.TAG_ONLINE_THEME_AD);
	}

}
