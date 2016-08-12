package com.fineos.android.rom.sdk;

//import org.apache.http.protocol.HTTP;

//import com.fineos.android.rom.sdk.rest.BaseResource;
import com.fineos.theme.ThemeConfig;

/**
 * 客户端信息类，用于设置请求交互中的客户端信息
 * 
 * @author zhouyong
 * 
 */
public class ClientInfo {

	//阿里云服务器地址
//	public static String RESOURCE_ROOT_URL = "http://theme.fineos.cn:8080/fineos-theme-api";
	
	//国外服务器
//	public static String RESOURCE_ROOT_URL = "http://theme.enjoyui.com:8080/fineos-theme-api";
	
	//测试服务器地址
	public static String RESOURCE_ROOT_URL = "http://weather.huaqin.com:8080/fineos-theme-api";

	public static boolean GZIP_ENCODING = true;

	public static final String USER_AGENT = "User-Agent";//HTTP.USER_AGENT;

	public static final String API_Version = "0.1b";

	public static final String USER_ID = "User-ID";

	public static final String CLIENT_ID = "Client-ID";

	public static final String DEVICE_ID = "Device-ID";

	public static final String SUBSCRIBER_ID = "Subscriber-ID";

	public static final String ACCESS_TOKEN = "Access_Token";


	static {
		setHeader(USER_AGENT, "App-Rom/" + API_Version);
		setHeader(USER_ID, "");
		setHeader("API-Version", API_Version);
		setHeader("Channel-Info", ThemeConfig.getInstance().getROMCenterAndOtherChannel());
		RESOURCE_ROOT_URL = ThemeConfig.getInstance().getRootUri();
	}

	public static void setHeader(String name, String value) {
	//	BaseResource.customHeaders.put(name, value);
	}

	public static String getApiVersion() {
		return API_Version;
	}

//	public static String getHeaderValue(String name) {
//		return BaseResource.customHeaders.get(name);
//	}

	public static void setClientId(String value) {
		setHeader(CLIENT_ID, value);
	}

	public static void setDeviceId(String value) {
		setHeader(DEVICE_ID, value);
	}

	public static void setSubscriberId(String value) {
		setHeader(SUBSCRIBER_ID, value);
	}

	public static void setUserId(String value) {
		setHeader(USER_ID, value);
	}

	public static String getUserId() {
		return USER_ID;
	}

	public static void setAccessToken(String value) {
		setHeader(ACCESS_TOKEN, value);
	}

	public static void setRootResourceUrl(String rootResourceUrl) {
		RESOURCE_ROOT_URL = rootResourceUrl;
	}

	public static void setGzipEncoding(boolean gzip) {
		GZIP_ENCODING = gzip;
	}

}
