package com.huaqin.market.download;

import java.io.IOException;

import org.apache.http.HttpException;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import com.huaqin.android.market.sdk.ReportProvider;
import com.huaqin.market.R;
import com.huaqin.market.model.Application2;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

public class DownloadManager {

	private static final String TAG = "DownloadManager";
	private static IMarketService mMarketService;
	private static String[] mCols = new String [] {
            Downloads._ID,
            Downloads.COLUMN_TITLE,
            Downloads.COLUMN_CURRENT_BYTES,
            Downloads.COLUMN_TOTAL_BYTES,
            Downloads._DATA,
            Downloads.COLUMN_APP_ID,
            Downloads.COLUMN_CONTROL
    };
	
	private final int idColumn = 0;
	private final int titleColumn = 1;
	private final static int currentBytesColumn = 2;
	private final static int totalBytesColumn = 3;
	private final int dataColumn = 4;
	private final int appIdColumn = 5;
	private final int controlColumn = 6;
	public static void startDownloadAPK(Context context, Application2 appInfo, String fromWhere) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("Report", 0);
		String userId = sharedPreferences.getString("userId", null);
	
//		Log.v(TAG,"JimmyJin DownloadAPK_userId="+userId);
		ContentValues values = new ContentValues();
		String url = appInfo.getDownloadUrl();
		String fileName = url.substring(url.lastIndexOf("/") + 1);
		Log.v("DownloadManager", "url ="+url);
		values.put(Downloads.COLUMN_URI, url);
		values.put(Downloads.COLUMN_VISIBILITY, Downloads.VISIBILITY_VISIBLE);
		values.put(Downloads.COLUMN_DESTINATION, Downloads.DESTINATION_EXTERNAL);
		values.put(Downloads.COLUMN_TITLE, appInfo.getAppName());
		values.put(Downloads.COLUMN_FILE_NAME_HINT, fileName);
		values.put(Downloads.COLUMN_MIME_TYPE, Constants.MIMETYPE_APK);
		values.put(Downloads.COLUMN_NOTIFICATION_PACKAGE, context.getPackageName());
		values.put(Downloads.COLUMN_NOTIFICATION_CLASS, DownloadReceiver.class.getName());
		values.put(Downloads.COLUMN_APP_ID, appInfo.getAppId());
		values.put(Downloads.COLUMN_PACKAGE_NAME, appInfo.getAppPackage());
		values.put(Downloads.COLUMN_TOTAL_BYTES, appInfo.getSize());
		values.put(Downloads.COLUMN_CONTROL, Downloads.CONTROL_RUN);
		/*************Added-s by JimmyJin for Pudding Project**************/
		values.put(Downloads.COLUMN_APP_TYPE, appInfo.getPuddingType());
		values.put(Downloads.COLUMN_FROM_WHERE, fromWhere);
		/*************Added-s by JimmyJin for Pudding Project**************/
		
		context.getContentResolver().insert(Downloads.CONTENT_URI, values);
		
		/*************Added-s by JimmyJin**************/
		//下载开始时间接口
//		Log.v(TAG,"JimmyJin appInfo.getPuddingType()="+appInfo.getPuddingType());
//		//fromWhere 0:装机精灵 ; 1:布丁控 ; 2:一键安装
//		Log.v(TAG,"JimmyJin DownloadManager_mFromWhere="+fromWhere);
		
		/***********Added-s by JimmyJin 20120509***********/
		if(userId != null){
			mMarketService = MarketService.getServiceInstance(context);
			Request request = new Request(0, Constant.TYPE_DOWNLOAD_BEGIN);
			Object[] params = new Object[4];
			
			params[0] = userId;
			params[1] = String.valueOf(appInfo.getAppId());
			params[2] = appInfo.getAppPackage();
			params[3] = fromWhere;
			
			request.setData(params);			
			mMarketService.postDownloadBegin(request);
		}			
		/***********Added-e by JimmyJin 20120509***********/	
	}
	
	public static void deleteAPK(Context context, Application2 appInfo) {
		String where = Downloads.WHERE_APP_ID;
		context.getContentResolver().delete(Downloads.CONTENT_URI, where, new String[]{"" + appInfo.getAppId()});
	}

	public static void startDownloadSelf(Context context, String releaseURL, int fileSize) {
		
		ContentValues values = new ContentValues();
		Log.i("DownloadManager", "url=" + releaseURL);
		String fileName = releaseURL.substring(releaseURL.lastIndexOf("/") + 1);
		
		values.put(Downloads.COLUMN_URI, releaseURL);
		values.put(Downloads.COLUMN_VISIBILITY, Downloads.VISIBILITY_VISIBLE);
		values.put(Downloads.COLUMN_DESTINATION, Downloads.DESTINATION_EXTERNAL);
		values.put(Downloads.COLUMN_TITLE, context.getString(R.string.app_name));
		values.put(Downloads.COLUMN_FILE_NAME_HINT, fileName);
		values.put(Downloads.COLUMN_MIME_TYPE, Constants.MIMETYPE_APK);
		values.put(Downloads.COLUMN_NOTIFICATION_PACKAGE, context.getPackageName());
		values.put(Downloads.COLUMN_NOTIFICATION_CLASS, DownloadReceiver.class.getName());
		values.put(Downloads.COLUMN_APP_ID, -65536);
		values.put(Downloads.COLUMN_TOTAL_BYTES, fileSize);
		values.put(Downloads.COLUMN_PACKAGE_NAME, Constant.MARKET_SELF_PACKAGE_NAME);
		values.put(Downloads.COLUMN_CONTROL, Downloads.CONTROL_RUN);
		
		context.getContentResolver().insert(Downloads.CONTENT_URI, values);
	}

	public static void pauseDownload(Context context, long[] appIds) {
		// TODO Auto-generated method stub
		if (appIds == null || appIds.length == 0) {
			Log.e(TAG, "input appIds is null");
			return;
		}
		
		ContentValues values = new ContentValues();
		values.put(Downloads.COLUMN_CONTROL, Downloads.CONTROL_PAUSED);
		Log.i(TAG, "pause()");
		context.getContentResolver().update(Downloads.CONTENT_URI, values,
				getWhereClauseForIds(appIds), null);
		
		// by Jacob, 2012.02.02
		Intent intent = new Intent(context, DownloadService.class);
		intent.putExtra("control", "pause");
		String strAppIds = Helpers.getStringForAppIds(appIds);
		intent.putExtra("appids", strAppIds);
		context.startService(intent);
	}

	public static void resumeDownload(Context context, long[] appIds) {
		// TODO Auto-generated method stub
		if (appIds == null || appIds.length == 0) {
			Log.e(TAG, "input appIds is null");
			return;
		}
		
		ContentValues values = new ContentValues();
		values.put(Downloads.COLUMN_CONTROL, Downloads.CONTROL_RUN);
		context.getContentResolver().update(Downloads.CONTENT_URI, values,
				getWhereClauseForIds(appIds), null);
		
		// by Jacob, 2012.02.02
		Intent intent = new Intent(context, DownloadService.class);
		intent.putExtra("control", "start");
		String strAppIds = Helpers.getStringForAppIds(appIds);
		intent.putExtra("appids", strAppIds);
		context.startService(intent);
	}

	public static void deleteCompletedTasks(Context context) {
		context.getContentResolver().delete(Downloads.CONTENT_URI,
				Downloads.WHERE_COMPLETED, null);
	}
	
	public static void deleteCompletedTask(Context context, long id) {
		context.getContentResolver().delete(Downloads.CONTENT_URI,
				BaseColumns._ID + "=" + id, null);
	}

	private static String getWhereClauseForIds(long[] appIds) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder(Downloads.COLUMN_APP_ID);
		
		sb.append(" IN (");
		for (int i = 0; i < appIds.length; i++) {
			sb.append(appIds[i]);
			if(i < (appIds.length - 1)) {
				sb.append(",");
			}
		}
		sb.append(")");
		
		return sb.toString();
	}
	
	public static boolean queryDownloadingURL(Context context, Application2 appInfo) {
		String url = appInfo.getDownloadUrl();
        boolean result = false;
        Log.v("queryDownloadingURL", "url ="+url);
		String selection = new StringBuilder().append(Downloads.WHERE_RUNNING)
			.append(" AND uri like '").append(url).append("%'").toString();
		Log.v("queryDownloadingURL", "selection ="+selection);
		Cursor cursor = null;
		Log.v("queryDownloadingURL1", "cursor ="+cursor);
		cursor = context.getContentResolver().query(
				Downloads.CONTENT_URI, new String[]{Downloads.COLUMN_URI}, selection, null, null);
		Log.v("queryDownloadingURL2", "cursor ="+cursor);
		if(null == cursor){
			return false;
		}
		try{
			result = cursor.moveToFirst();
			Log.v("queryDownloadingURL", "result ="+result);
		}finally{
			if(cursor!=null&&!cursor.isClosed()){
				cursor.close();
				Log.v("queryDownloadingURL", "cursor close");
			}
		}
		return result;
		
	}
	public static String queryDownloadedURL(Context context, Application2 appInfo) {
		String url = appInfo.getDownloadUrl();
		Log.v("queryDownloadedURL", "url ="+url);
		String selection = new StringBuilder().append(Downloads.WHERE_COMPLETED)
			.append(" AND uri like '").append(url).append("%'").toString();
		Log.v("queryDownloadedURL", "selection ="+selection);
		Cursor cursor = context.getContentResolver().query(
				Downloads.CONTENT_URI, new String[]{Downloads._DATA}, selection, null, null);
		String result = null;
		if(null == cursor)
			return null;
		try{
			if(cursor.moveToFirst()){
				int data_column = cursor.getColumnIndex(Downloads._DATA);
				result = cursor.getString(data_column);
			}
		}finally{
			cursor.close();
		}
		return result;
	}
	public static String queryDownloadedORDownloadingAPKEx(Context context, Application2 appInfo) {
		String pkgName = appInfo.getAppPackage();
		String selection = new StringBuilder().append(Downloads.WHERE_RUNNING_OR_COMPLETED)
				.append(" AND pkgName like '").append(pkgName).append("%'").toString();
		Log.v("queryDownloadedORDownloadingAPKEx", "selection ="+selection);
			Cursor cursor = context.getContentResolver().query(
					Downloads.CONTENT_URI, new String[]{Downloads._DATA,Downloads.COLUMN_STATUS}, selection, null, null);
			String result = null;
			String status = null;
			Log.v("queryDownloadedORDownloadingAPKEx", "cursor ="+cursor);
			if(null == cursor)
				return null;
			try{
				if(cursor.moveToFirst()){
					int data_column = cursor.getColumnIndex(Downloads._DATA);
					int status_column = cursor.getColumnIndex(Downloads.COLUMN_STATUS);
					status = cursor.getString(status_column);
					if(!status.equals("200"))
						result = "Downloading";
					else
				result = cursor.getString(data_column);
				if (cursor != null) {
					cursor.close();
				}
			}
		}finally{
			cursor.close();
		}
		return result;
	}
	
	@SuppressWarnings("unused")
	public static int queryDownloadingProgressURL(Context context, Application2 appInfo) {
		String url = appInfo.getDownloadUrl();
        int result = 0;
		String selection = new StringBuilder().append(Downloads.WHERE_RUNNING)
			.append(" AND pkgName like '").append(appInfo.getAppPackage()).append("%'").toString();
		Cursor cursor = context.getContentResolver().query(
                Downloads.CONTENT_URI, mCols,selection, null, BaseColumns._ID);
		Log.v("queryDownloadingProgressURL", "cursor ="+cursor);
		Log.v("queryDownloadingProgressURL", "getAppName ="+appInfo.getAppName());
		Log.v("queryDownloadingProgressURL", "getAppId ="+appInfo.getAppId());
		if(null == cursor){
			return 0;
		}
		try{
			if(cursor.moveToFirst()){
				int totalBytes = cursor.getInt(totalBytesColumn);
				int currentBytes = cursor.getInt(currentBytesColumn);
				Log.v("queryDownloadingProgressURL", "totalBytes ="+totalBytes);
				Log.v("queryDownloadingProgressURL", "currentBytes ="+currentBytes);
				result = (int)(currentBytes * 100 / totalBytes);
			}
		}finally{
			cursor.close();
		}
		Log.v("queryDownloadingProgressURL", "result ="+result);
		return result;
		
	}
}