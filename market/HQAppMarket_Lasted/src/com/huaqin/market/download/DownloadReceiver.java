package com.huaqin.market.download;

import java.io.File;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.IPackageInstallObserver;

import com.huaqin.android.market.sdk.rest.BaseResource;
import com.huaqin.market.MarketBackgroundService;
import com.huaqin.market.MarketBrowser;
import com.huaqin.market.R;
import com.huaqin.market.SlideViewPager;
import com.huaqin.market.ui.UninstallHintActivity;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.DeviceUtil;
import com.huaqin.market.utils.PackageUtil;
import com.huaqin.market.utils.TimeUtil;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

/**
 * Receives system broadcasts (boot, network connectivity)
 */
public class DownloadReceiver extends BroadcastReceiver {
    private static final String TAG = "DownloadReceiver";
    Timer initTimer = null;
    Timer nextTimer = null;
    Timer timechangeTimer = null;
    public static final String ACTION_APP_INSTALLING = "com.hauqin.intent.action.APP_INSTALLING";
    boolean initTimerIsRunning = false;
    boolean nextTimerIsRunning = false;
    boolean timechangeTimerIsRunning = false;
    private static IMarketService mMarketService;
    private Context mContext;
    @Override
	public void onReceive(Context context, Intent intent) {
    	mContext = context;
    	
    	final String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, DownloadService.class));
            timerCancel();
            long delay = TimeUtil.getRemainTime(72000);
            Log.d(TAG,"delay =" + delay );
            if(delay > 0 || delay == 0){
    			initTimer = new Timer();
    			initTimer.schedule(new MyRemindTask(context), delay);
    			initTimerIsRunning = true;
    			
    		}else if(delay > -( 60 * 60 * 1000) && delay < 0){
    			initTimer = new Timer();
    			initTimer.schedule(new MyRemindTask(context), 0);
    			initTimerIsRunning = true;
    		}else{
    			long mdelay = TimeUtil.getRemainTime(44 * 3600);
    			initTimer = new Timer();
    			initTimer.schedule(new MyRemindTask(context), mdelay);
    			initTimerIsRunning = true;
    		}
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo info = (NetworkInfo)
                    intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            boolean isNetworkAvailable = Helpers.isNetworkAvailable(context);
            Log.d(TAG,"network changes info.isConnected="+((info!=null)?info.isConnected():false)+" isNetworkAvailable="+isNetworkAvailable);
            if ((info != null && info.isConnected()) || isNetworkAvailable) {
                context.startService(new Intent(context, DownloadService.class));

                // by Jacob, 2012.02.03
                Log.w(TAG,"rebuild context about ApplicationProvider");
//                ResourceHelper.getInstance().rebuildContext();
                //BaseResource.rebuildContext();//Added by JimmyJin
                try {
					rebuildContext();
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

                // update status from STATUS_HTTP_DATA_ERROR OR STATUS_RUNNING_PAUSED to STATUS_PENDING
                // by Jacob, 2012.02.04
        		ContentValues values = new ContentValues();
        		values.put(Downloads.COLUMN_STATUS, Downloads.STATUS_PENDING);

        		StringBuilder sb = new StringBuilder();
        		sb.append(Downloads.COLUMN_STATUS+"="+Downloads.STATUS_RUNNING_PAUSED);
        		sb.append(" OR ");
        		sb.append(Downloads.COLUMN_STATUS+"="+Downloads.STATUS_HTTP_DATA_ERROR);
        		String where = sb.toString();

                context.getContentResolver().update(Downloads.CONTENT_URI, values, where, null);
                
                timerCancel();
                long delay = TimeUtil.getRemainTime(100);
                if(delay > 0 || delay == 0){
                	timechangeTimer = new Timer();
                	timechangeTimer.schedule(new MyRemindTask(context), delay);
                	timechangeTimerIsRunning = true;
        			
        		}else if(delay > -( 60 * 60 * 1000) && delay < 0 ){
        			timechangeTimer = new Timer();
        			timechangeTimer.schedule(new MyRemindTask(context), 0);
        			timechangeTimerIsRunning = true;
        		}else{
        			timechangeTimer = new Timer();
                	timechangeTimer.schedule(new MyRemindTask(context), 0);
                	timechangeTimerIsRunning = true;
        		}
        			
            }
        } else if (action.equals(Downloads.ACTION_DOWNLOAD_COMPLETED)) {
        	Uri uri = intent.getData();
        	
        	if (uri == null) {
        		return;
        	}
        	
        	String[] cols = new String[]{
        			Downloads.COLUMN_STATUS,
        			Downloads._DATA,
        			Downloads.COLUMN_PACKAGE_NAME,
        			Downloads.COLUMN_TITLE
        	};
        	Cursor c = context.getContentResolver()
        						.query(uri, cols, null, null, null);
        	
        	if (c != null && c.moveToFirst()) {
        		if (Downloads.isStatusSuccess(c.getInt(0))) {
        			String filePath = c.getString(1);
        			String currentPackageName = c.getString(2);
        			String fileTitle = c.getString(3);
        			int appStatus = PackageUtil.getApplicationStatus(context.getPackageManager(),currentPackageName);
        			boolean checkSystemVersion = PackageUtil.checkingSystemVersion();
        			if(appStatus == PackageUtil.PACKAGE_UPDATE_AVAILABLE && checkSystemVersion){
        				boolean signatureConflict = PackageUtil.isCertificatesConfilctedWithInstalledApk(context,filePath);
        		    	Log.v("ad", "signatureConflict = "+signatureConflict);
        				if(true == signatureConflict){
        					Intent uninstallActivity = new Intent(context,UninstallHintActivity.class);
        					Bundle bundle = new Bundle();
        					bundle.putString("package", currentPackageName);
        					bundle.putString("filepath", filePath);
        					bundle.putString("filetitle", fileTitle);
        					uninstallActivity.putExtras(bundle);
        					uninstallActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        					context.startActivity(uninstallActivity);
        				}else{
        					File file = new File(filePath);
//            		        if(checkAppType("com.huaqin.market")) {//如果是系统ROM自带APP就静默安装	
//         	       			   //Added-s by JimmyJin for 静默安装
//         	          		   String  pkgName = c.getString(2);
//         	          		   Log.v("DownLoadReceiver","JimmyJin pkgName="+pkgName);
//         	       			   int installFlags = 0;
//         	       			   PackageManager pm = context.getPackageManager();
//         	       			   try {
//         	       	                PackageInfo pi = pm.getPackageInfo(pkgName,
//         	       	                        PackageManager.GET_UNINSTALLED_PACKAGES);
//         	       	                if(pi != null) {
//         	       	                    installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
//         	       	                }
//         	       	            } catch (NameNotFoundException e) {
//         	       	            }
//         	       			   PackageInstallObserver observer = new PackageInstallObserver();
//         	       			   Intent installingIntent = new Intent(ACTION_APP_INSTALLING);
//         	       			   mContext.sendBroadcast(installingIntent);
//         	       	           pm.installPackage(Uri.fromFile(file), observer, installFlags, pkgName);
//         	       			   //Added-e by JimmyJin for 静默安装	
//         		        }else{
        					installAPK(context,filePath);         		        	
//         		        	}        					        					
        				}
        			}else{
        				File file = new File(filePath);
//        		        if(checkAppType("com.huaqin.market")) {//如果是系统ROM自带APP就静默安装	
//      	       			   //Added-s by JimmyJin for 静默安装
//      	          		   String  pkgName = c.getString(2);
//      	          		   Log.v("DownLoadReceiver","JimmyJin pkgName="+pkgName);
//      	       			   int installFlags = 0;
//      	       			   PackageManager pm = context.getPackageManager();
//      	       			   try {
//      	       	                PackageInfo pi = pm.getPackageInfo(pkgName,
//      	       	                        PackageManager.GET_UNINSTALLED_PACKAGES);
//      	       	                if(pi != null) {
//      	       	                    installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
//      	       	                }
//      	       	            } catch (NameNotFoundException e) {
//      	       	            }
//      	       			   PackageInstallObserver observer = new PackageInstallObserver();
//     	       			   Intent installingIntent = new Intent(ACTION_APP_INSTALLING);
//     	       			   mContext.sendBroadcast(installingIntent);
//      	       	           pm.installPackage(Uri.fromFile(file), observer, installFlags, pkgName);
//      	       			   //Added-e by JimmyJin for 静默安装	
//      		        }else{
     					installAPK(context,filePath);         		        	
//      		        	}    
     					
//            			File file = new File(filePath);
//            			Intent install = new Intent(Intent.ACTION_VIEW);
//            			install.setDataAndType(Uri.fromFile(file), Constants.MIMETYPE_APK);
//            			install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            			context.startActivity(install);
        			}
        		}
        		c.close();
        	}
        } else if (action.equals(Constants.ACTION_RETRY)) {
            context.startService(new Intent(context, DownloadService.class));
        } else if (action.equals(Constants.ACTION_OPEN)
                || action.equals(Constants.ACTION_LIST)) {
            Cursor cursor = context.getContentResolver().query(
                    intent.getData(), null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int statusColumn = cursor.getColumnIndexOrThrow(Downloads.COLUMN_STATUS);
                    int status = cursor.getInt(statusColumn);
                    int visibilityColumn =
                            cursor.getColumnIndexOrThrow(Downloads.COLUMN_VISIBILITY);
                    int visibility = cursor.getInt(visibilityColumn);
                    if (Downloads.isStatusCompleted(status)
                            && visibility == Downloads.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) {
                        ContentValues values = new ContentValues();
                        values.put(Downloads.COLUMN_VISIBILITY,
                                Downloads.VISIBILITY_VISIBLE);
                        context.getContentResolver().update(intent.getData(), values, null, null);
                    }

                    if (action.equals(Constants.ACTION_OPEN)) {
                        int filenameColumn = cursor.getColumnIndexOrThrow(Downloads._DATA);
                        int mimetypeColumn =
                                cursor.getColumnIndexOrThrow(Downloads.COLUMN_MIME_TYPE);
                        String filename = cursor.getString(filenameColumn);
                        String mimetype = cursor.getString(mimetypeColumn);
                        Uri path = Uri.parse(filename);
                        // If there is no scheme, then it must be a file
                        if (path.getScheme() == null) {
                            path = Uri.fromFile(new File(filename));
                        }
                        Intent activityIntent = new Intent(Intent.ACTION_VIEW);
                        activityIntent.setDataAndType(path, mimetype);
                        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            context.startActivity(activityIntent);
                        } catch (ActivityNotFoundException ex) {
                            // nothing anyone can do about this, but we're in a clean state,
                            //     swallow the exception entirely
                        }
                    } else {
                        int packageColumn = cursor.getColumnIndexOrThrow(
                                Downloads.COLUMN_NOTIFICATION_PACKAGE);
                        int classColumn = cursor.getColumnIndexOrThrow(
                                Downloads.COLUMN_NOTIFICATION_CLASS);
                        String pckg = cursor.getString(packageColumn);
                        String clazz = cursor.getString(classColumn);
                        if (pckg != null && clazz != null) {
                            Intent appIntent =
                                    new Intent(Downloads.ACTION_NOTIFICATION_CLICKED);
                            appIntent.setClassName(pckg, clazz);
                            if (intent.getBooleanExtra("multiple", true)) {
                                appIntent.setData(Downloads.CONTENT_URI);
                            } else {
                                appIntent.setData(intent.getData());
                            }
                            context.sendBroadcast(appIntent);
                        }
                    }
                }
                cursor.close();
            }
            NotificationManager notMgr = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            if (notMgr != null) {
                notMgr.cancel((int) ContentUris.parseId(intent.getData()));
            }
        } else if (action.equals(Constants.ACTION_HIDE)) {
            Cursor cursor = context.getContentResolver().query(
                    intent.getData(), null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int statusColumn = cursor.getColumnIndexOrThrow(Downloads.COLUMN_STATUS);
                    int status = cursor.getInt(statusColumn);
                    int visibilityColumn =
                            cursor.getColumnIndexOrThrow(Downloads.COLUMN_VISIBILITY);
                    int visibility = cursor.getInt(visibilityColumn);
                    if (Downloads.isStatusCompleted(status)
                            && visibility == Downloads.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) {
                        ContentValues values = new ContentValues();
                        values.put(Downloads.COLUMN_VISIBILITY,
                                Downloads.VISIBILITY_VISIBLE);
                        context.getContentResolver().update(intent.getData(), values, null, null);
                    }
                }
                cursor.close();
            }
        }else if(action.equals("android.intent.action.TIME_SET")||action.equals(Intent.ACTION_USER_PRESENT)){
        	Log.v(TAG, "action.TIME_SET");
        	timerCancel();
            long delay = TimeUtil.getRemainTime(100);
            Log.v(TAG, "delay ="+delay);
            if(delay > 0 || delay == 0){
            	timechangeTimer = new Timer();
            	timechangeTimer.schedule(new MyRemindTask(context), delay);
            	timechangeTimerIsRunning = true;
    			
    		}else if(delay > -( 60 * 60 * 1000) && delay < 0 ){
    			timechangeTimer = new Timer();
    			timechangeTimer.schedule(new MyRemindTask(context), 0);
    			timechangeTimerIsRunning = true;
    		}else{
    			timechangeTimer = new Timer();
            	timechangeTimer.schedule(new MyRemindTask(context), 0);
            	timechangeTimerIsRunning = true;
    		}
        }
        
    }
    
    
    private void installAPK(Context context,String filepath){
    	File file = new File(filepath);
		Intent install = new Intent(Intent.ACTION_VIEW);
		install.setDataAndType(Uri.fromFile(file), Constants.MIMETYPE_APK);
		install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(install);
    }
    
    private void initTimerCancel(){
    	if(initTimerIsRunning){
    		initTimer.cancel();
			initTimerIsRunning = false;
    	}
    }
    
    private void nextTimerCancel(){
    	if(nextTimerIsRunning){
			nextTimer.cancel();
			nextTimerIsRunning = false;
		}
    }
    
    private void timechangeTimerCancel(){
    	if(timechangeTimerIsRunning){
			timechangeTimer.cancel();
			timechangeTimerIsRunning = false;
		}
    }
    
    private void timerCancel(){
    	initTimerCancel();
    	nextTimerCancel();
    	timechangeTimerCancel();
    }
    private class MyRemindTask extends TimerTask{
    	private Context MyRemindTaskContext;
    	MyRemindTask(Context context){
    		MyRemindTaskContext = context;
    	}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			MyRemindTaskContext.startService(new Intent(MyRemindTaskContext, MarketBackgroundService.class));
			timerCancel();
			nextTimer = new Timer();
			nextTimer.schedule(new MyRemindTask(MyRemindTaskContext),86400 * 1000);
			nextTimerIsRunning = true;
		}
    	
    }
	//Added-s by JimmyJin for 静默安装
	public class PackageInstallObserver extends IPackageInstallObserver.Stub {
        public void packageInstalled(String packageName, int returnCode) {
        }
    };
	//Added-e by JimmyJin for 静默安装
    
    //Added-s by JimmyJin 判断是否为系统ROM自动App
	public  boolean checkAppType(String pname) {
		try {
			PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(pname, 0);
			// 是系统软件或者是系统软件更新
			if (isSystemApp(pInfo) || isSystemUpdateApp(pInfo)) {
				return true;
			} else {
				return false;
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 是否是系统软件或者是系统软件的更新软件
	 * 
	 */
	public boolean isSystemApp(PackageInfo pInfo) {
		return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
	}
	public boolean isSystemUpdateApp(PackageInfo pInfo) {
		return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
	}
    //Added-e by JimmyJin 判断是否为系统ROM自动App    
	
	private void rebuildContext() throws NameNotFoundException {
		// TODO Auto-generated method stub
		mMarketService = MarketService.getServiceInstance(mContext);
		Request request = new Request(0, Constant.REBUILD_CONTEXT);
			
		mMarketService.postRebuildContext(request);
	}
}