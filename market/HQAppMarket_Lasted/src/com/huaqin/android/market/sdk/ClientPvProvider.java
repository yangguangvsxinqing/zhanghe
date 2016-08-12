package com.huaqin.android.market.sdk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import com.huaqin.android.market.sdk.rest.BaseResource;

/**
 * 新版分类信息获取
 * @author zhouyong
 *
 */
public class ClientPvProvider extends BaseResource{
    
    /**
     * 客户传pv
     * @param phoneModel:机型;
     * @param  version 客户端版本
     * @param pageName 访问页面
     */
	
	
public static void clientPv(String phoneModel,String version,String pageName) throws IOException, HttpException{	
		
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("phone_model", phoneModel));
        formParams.add(new BasicNameValuePair("version", version));
        formParams.add(new BasicNameValuePair("page_name", pageName));
	  post(ClientInfo.RESOURCE_ROOT_URL + "/pv/clientpv",formParams);
	}
}
