package com.huaqin.market;

import com.huaqin.market.download.Downloads;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

public class PuddingBroadcastReceiver extends BroadcastReceiver{
	private Context mContext;
	
	private final int column_app_name = 1;
	private final int column_app_id = 3;
	private final int column_app_type = 4;
	private final int column_package_name = 5;
	private final int column_from_where = 6;
	
	public static String INSTALLED_APP_MSG = "com.huaqin.intent.action.PACKAGE_APPSPRITE_INSTALL";
	
	private String packageName = null;
	
	private static IMarketService mMarketService;
	
    private final static String TAG = "PuddingBroadcastReceiver";
	
	private String[] mCols = new String [] {
            Downloads._ID,
            Downloads.COLUMN_TITLE,
            Downloads._DATA,
            Downloads.COLUMN_APP_ID,
            Downloads.COLUMN_APP_TYPE,
            Downloads.COLUMN_PACKAGE_NAME,
            Downloads.COLUMN_FROM_WHERE
    };

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "Received broadcast : " + intent.getAction());
		if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
			mContext = context;
			final String pkgName = intent.getDataString().replace("package:", "");
			packageName = pkgName;
    		Log.v(TAG, "JimmyJin packageName =" + packageName); 
			sendBroadcastToPudding();
		}
	}
	
	private void sendBroadcastToPudding() {
		Log.v(TAG, "JimmyJin sendBroadcastToPudding");
		Cursor cursor = mContext.getContentResolver().query(
                Downloads.CONTENT_URI, mCols,
                Downloads.WHERE_COMPLETED, null, null);
        if (cursor == null) {
    		Log.v(TAG, "JimmyJin cursor == null");
            return;
        }
        
        try{
	        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
	        	String appPackageName = cursor.getString(column_package_name);
 
	    		Log.v(TAG, "JimmyJin appPackageName =" + appPackageName);  
	    		if(packageName.equals(appPackageName)){
		            String puddingType =cursor.getString(column_app_type);
		        	String appId =cursor.getString(column_app_id);
		        	
		        	String mFromWhere = cursor.getString(column_from_where);
		        	
		        	String mAppName = cursor.getString(column_app_name);
		        	
		    		Log.v(TAG, "JimmyJin appId =" + appId);   
		    		Log.v(TAG, "JimmyJin puddingType =" + puddingType);   
		    		
		    		
	                Intent intent = new Intent(INSTALLED_APP_MSG);    
	                intent.putExtra("packageName", packageName);    
	                intent.putExtra("type", puddingType);   
	                intent.putExtra("appName", mAppName);
	                mContext.sendBroadcast(intent); 
	                
            		/***********Added-s by JimmyJin 20120509***********/
	                  //安装时间接口
        			SharedPreferences sharedPreferences = mContext.getSharedPreferences("Report", 0);
        			String userId = sharedPreferences.getString("userId", null);

        			Log.v(TAG,"JimmyJin DownloadAPK_userId="+userId);
        			
        			//fromWhere 0:装机精灵 ; 1:布丁控 ; 2:一键安装
        			Log.v(TAG,"JimmyJin PuddingBroadcastReceiver_mFromWhere="+mFromWhere);
	            		if(userId != null){
	            			mMarketService = MarketService.getServiceInstance(mContext);
	            			Request request = new Request(0, Constant.TYPE_INSTALL_INFO);
	            			Object[] params = new Object[4];
	            			
	            			params[0] = userId;
	            			params[1] = "99999999";
	            			params[2] = packageName;
	            			params[3] = mFromWhere;
	            			
	            			request.setData(params);			
	            			mMarketService.postInstallInfo(request);
	            		}			
	            		/***********Added-e by JimmyJin 20120509***********/
	                
	                
					/*************Added-s by JimmyJin**************/
            		//安装时间接口
//            		try {
//            			SharedPreferences sharedPreferences = mContext.getSharedPreferences("Report", 0);
//            			String userId = sharedPreferences.getString("userId", null);
//
//            			Log.v(TAG,"JimmyJin DownloadAPK_userId="+userId);
//            			
//            			//fromWhere 0:装机精灵 ; 1:布丁控 ; 2:一键安装
//            			Log.v(TAG,"JimmyJin PuddingBroadcastReceiver_mFromWhere="+mFromWhere);
//            			if(userId != null){
//            	    		Log.v(TAG, "JimmyJin ReportProvider.postInstallInfo=" + 
//            	    		ReportProvider.postInstallInfo(userId,"99999999",packageName ,mFromWhere)); 
//            			}   
//            		} catch (IOException e) {
//            			// TODO Auto-generated catch block
//            			e.printStackTrace();
//            		} catch (HttpException e) {
//            			// TODO Auto-generated catch block
//            			e.printStackTrace();
//            		}
            		/*************Added-e by JimmyJin**************/    
	                
		    		break;
	    		}
	        }
        }finally {
    		Log.v(TAG, "JimmyJin finally");  
        	cursor.close();
        }
	}
}

