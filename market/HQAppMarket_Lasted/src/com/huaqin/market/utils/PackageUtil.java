package com.huaqin.market.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.huaqin.android.market.sdk.bean.Application;
import com.huaqin.android.market.sdk.bean.InstalledApp;
import com.huaqin.market.SlideViewPager;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.Request;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemProperties;
import android.util.Log;

public class PackageUtil {

	public static final String ACTION_PACKAGE_UPDATED = "com.hauqin.intent.action.PACKAGE_UPDATED";
	public static final String ACTION_PACKAGE_NOT_UPDATED = "com.hauqin.intent.action.PACKAGE_NOT_UPDATED";
	
	public static final int PACKAGE_NOT_INSTALLED = 0;
	public static final int PACKAGE_INSTALLED = 1;
	public static final int PACKAGE_UPDATE_AVAILABLE = 2;
	
	private static final String TAG = "PackageUtil";
	
	private static ArrayList<Application> mUpdateApps = new ArrayList<Application>();
	private static Context mContext;

	/*
	 * Refresh static update applications list
	 */
	public static void refreshUpdateApps(Context context, IMarketService marketService) {

		ArrayList<PackageInfo> pkgInfos = getInstalledPackages(context);
		
		if (pkgInfos.size() == 0) {
    		return;
    	}
		
		mContext = context;
		
		InstalledApp[] mInstalledApps = new InstalledApp[pkgInfos.size()];
    	for (int i = 0; i < pkgInfos.size(); i++) {
    		PackageInfo pkgInfo = pkgInfos.get(i);
    		InstalledApp app = new InstalledApp();
    		
    		app.setAppPackage(pkgInfo.packageName);
    		app.setVersionCode(pkgInfo.versionCode);
    		mInstalledApps[i] = app;
    	}
		
		Request request = new Request(0, Constant.TYPE_CHECK_APP_UPDATE);
		request.setData(mInstalledApps);
		request.addObserver(new Observer() {

			@SuppressWarnings("unchecked")
			@Override
			public void update(Observable observable, Object data) {
				// TODO Auto-generated method stub
				
				if (data != null) {
					ArrayList<Application> list = (ArrayList<Application>) data;
					if(list.size() > 0) {
//						mUpdateApps.clear();
						mUpdateApps = (ArrayList<Application>) data;
						if (mContext != null) {
							Intent intent = new Intent(ACTION_PACKAGE_UPDATED);
							mContext.sendBroadcast(intent);
							Intent intent2 = new Intent(SlideViewPager.MANAGE_FLAG_REFRESH);
							mContext.sendBroadcast(intent2);

						}
					} else {
						if (mContext != null) {
							Intent intent = new Intent(ACTION_PACKAGE_NOT_UPDATED);
							mContext.sendBroadcast(intent);
						}
					}
				} 
			}
		});
		marketService.checkAppUpdate(request);
	}

	/*
	 * Get static update applications list
	 */
	public static ArrayList<Application> getUpdateApps() {
		return mUpdateApps;
	}

	/*
	 * Remove an application from update list by it's package name
	 */
	public static void removeUpdateApp(String pkgName) {
		for (int i = 0; i < mUpdateApps.size(); i++) {
			Application appInfo = mUpdateApps.get(i);
			if (pkgName.equals(appInfo.getAppPackage())) {
				mUpdateApps.remove(i);
				return;
			}
		}
	}

	/*
	 * Get Application status
	 */
	public static int getApplicationStatus(PackageManager pkgManager, String pkgName) {
		PackageInfo pkgInfo = PackageUtil.getPackageInfo(pkgManager, pkgName);
//		Log.v("getApplicationStatus", "pkgInfo="+pkgName);
		if (pkgInfo == null) {
//			Log.v("asd", "PACKAGE_NOT_INSTALLED");
			return PACKAGE_NOT_INSTALLED;
		} else {
			for (int i = 0; i < mUpdateApps.size(); i++) {
				Application appInfo = mUpdateApps.get(i);
//				Log.v("asd", "i="+i);
//				Log.v("asd", "appInfo.getAppPackage="+appInfo.getAppPackage());
				if (pkgName.equals(appInfo.getAppPackage())) {
//					Log.v("asd", "PACKAGE_UPDATE_AVAILABLE");
					return PACKAGE_UPDATE_AVAILABLE;
				}
			}
//			Log.v("asd", "PACKAGE_INSTALLED");
			return PACKAGE_INSTALLED;
		}
	}

	/*
	 * Get the application's download update url by package name
	 */
	public static Application getUpdateApplication(String pkgName) {
		for (int i = 0; i < mUpdateApps.size(); i++) {
			Application appInfo = mUpdateApps.get(i);
			if (pkgName.equals(appInfo.getAppPackage())) {
				return appInfo;
			}
		}
		return null;
	}

	/*
	 * Get current installed packages(applications)
	 */
	public static ArrayList<PackageInfo> getInstalledPackages(Context context) {
		return getInstalledPackages(context, false);
	}

	/*
	 * Get current installed packages(applications)
	 * bContainSystemApp - whether including system applications
	 */
	private static ArrayList<PackageInfo> getInstalledPackages(Context context,
			boolean bContainSystemApp) {
		// TODO Auto-generated method stub
		PackageManager packageManager = context.getPackageManager();
		ArrayList<PackageInfo> arrayList = new ArrayList<PackageInfo>();
		List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
		String selfPkgName = context.getPackageName();
		Log.v("", "");
		for (int i = 0; i < installedPackages.size(); i++) {
			PackageInfo pkgInfo = installedPackages.get(i);
			
			if (!selfPkgName.equals(pkgInfo.packageName) && !bContainSystemApp && isSystemApp(packageManager, pkgInfo)) {
				continue;
			} 
//			else if (selfPkgName.equals(pkgInfo.packageName)) {
//				// do not display app self
//				continue;
//			} 
			else {
				arrayList.add(pkgInfo);
//				Log.i("PackageUtil", "name=" + pkgInfo.applicationInfo.name);
			}
		}
		
		return arrayList;
	}

	/*
	 * Check one application to see whether it is System application by ApplicationInfo
	 */
	public static boolean isSystemApp(PackageManager packageManager,
			ApplicationInfo appInfo) {
		// TODO Auto-generated method stub
		if (appInfo == null) {
			return false;
		}
		if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM)
				== ApplicationInfo.FLAG_SYSTEM) {
			return true;
		}
		return false;
	}
    public static boolean checkingSystemVersion(){
    	String singatureVersion = "2.3";
    	String fullSystemVersion = android.os.Build.VERSION.RELEASE;
    	String subSystemVersion = fullSystemVersion.substring(0, 3);
    	if(subSystemVersion.equals(singatureVersion)){
    		return true;
    	}
    	return false;
    }
	/*
	 * Check one application to see whether it is System application by PackageName
	 */
	public static boolean isSystemApp(PackageManager packageManager,
			String packageName) {
		// TODO Auto-generated method stub		
		return isSystemApp(packageManager, getPackageInfo(packageManager, packageName));
	}

	/*
	 * Check one application to see whether it is System application by PackageInfo
	 */
	public static boolean isSystemApp(PackageManager packageManager,
			PackageInfo pkgInfo) {
		// TODO Auto-generated method stub		
		if (pkgInfo == null || pkgInfo.applicationInfo == null) {
			return false;
		}
		if ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)
				== ApplicationInfo.FLAG_SYSTEM) {
			return true;
		}
		return false;
	}

	/*
	 * Get PackageInfo by PackageName
	 */
	public static PackageInfo getPackageInfo(PackageManager packageManager,
			String packageName) {
		// TODO Auto-generated method stub
		PackageInfo packageInfo = null;
		try {
			packageInfo = packageManager.getPackageInfo(packageName,
							PackageManager.PERMISSION_GRANTED);
		} catch (PackageManager.NameNotFoundException e) {
//			e.printStackTrace();
		}
		return packageInfo;
	}
    public static Drawable getApplicationIcon(PackageManager packageManager,
	       String archiveFilePath) {
    	PackageInfo packageInfo = null;
    	ApplicationInfo appInfo = null;
    	Drawable icon = null;
		packageInfo = packageManager.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
		if(packageInfo != null){
			appInfo = packageInfo.applicationInfo;
			icon = packageManager.getApplicationIcon(appInfo);
		}
		return icon;
	}
	/*
	 * Get a package's version code
	 */
	public static int getPackageVersionCode(PackageManager packageManager,
			String packageName) {
		// TODO Auto-generated method stub
		PackageInfo packageInfo = getPackageInfo(packageManager, packageName);
		
		if (packageInfo != null) {
			return packageInfo.versionCode;
		}
		return 0;
	}

	/*
	 * Get a package's version name
	 */
	public static String getPackageVersionName(PackageManager packageManager,
			String packageName) {
		// TODO Auto-generated method stub
		PackageInfo packageInfo = getPackageInfo(packageManager, packageName);
		
		if (packageInfo != null) {
			return packageInfo.versionName;
		}
		return "";
	}

	/*
	 * uninstall a package by packageName
	 */
	public static void uninstallPackage(Context context, String pkgName) {
		// TODO Auto-generated method stub
		Uri uri = Uri.parse("package:" + pkgName);
		Intent intent = new Intent(Intent.ACTION_DELETE, uri);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		context.startActivity(intent);
	}
	
	public static boolean isCertificatesConfilctedWithInstalledApk(Context context,String archiveFilePath) {
		if(null == archiveFilePath){
			return false;
		}
		//Marked by JimmyJin
//        PackageInfo packageinfo = getPackageArchiveSignatures(archiveFilePath);
		PackageManager pm = context.getPackageManager(); 
		PackageInfo packageinfo =pm.getPackageArchiveInfo(archiveFilePath,PackageManager.GET_ACTIVITIES);
        Log.d(TAG, "packageinfo = " + packageinfo);
        if(packageinfo == null || packageinfo.packageName == null ||
                packageinfo.signatures == null || packageinfo.signatures.length == 0) {
            return false;
        }
        Signature[] signsInstall = getPackageSignatures(context,packageinfo.packageName);
        if(signsInstall != null && signsInstall.length != 0) {
            if(!signsInstall[0].equals(packageinfo.signatures[0])) {
              return true;
          }
        }
        return false;
    }
	
	public static Signature[] getPackageSignatures(Context context,String packageName) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
//            Log.d(TAG, "info = " + info);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(info == null) {
//            Log.d(TAG, "not find:" + packageName);
            return null;
        }
        return info.signatures;
    }
	
//	public static PackageInfo getPackageArchiveSignatures(String archiveFilePath) {
//        PackageParser packageParser = new PackageParser(archiveFilePath);
//        
//        DisplayMetrics metrics = new DisplayMetrics();
//        metrics.setToDefaults();
//        final File sourceFile = new File(archiveFilePath);
//        PackageParser.Package pkg = packageParser.parsePackage(
//                 sourceFile, archiveFilePath, metrics, 0);
//        if (pkg == null) {
//           return null;
//        }
//       
//        packageParser.collectCertificates(pkg, 0); 
//       
//        return PackageParser.generatePackageInfo(pkg, null, PackageManager.GET_SIGNATURES, 0, 0);
//    }
	
	public static String getAppSelfVersionName(Context context){
    	String appVersionName = null;
    	try{
    		PackageManager manager = context.getPackageManager();
    		PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
    		appVersionName = info.versionName;
    	}catch(NameNotFoundException e){
    		e.printStackTrace();
    	}
    	return appVersionName;
    }
	
//	public static String getAppFirstInstallTime(Context context,String PackageName)
//	{		
//		String strDateTime="";
//		
//		PackageInfo pkgInfo=getPackageInfo(context.getPackageManager(),PackageName);	
//		
//		if(pkgInfo!=null)
//		{
//			Date date = new Date(pkgInfo.firstInstallTime);
//			strDateTime=DateFormat.getDateFormat(context).format(date)+" "+DateFormat.getTimeFormat(context).format(date);
//		}
//		return strDateTime;
//    }
//
//
//	public static String getMessageCenterNumber(Context context)
//	{
//		String StrMsgServiceCenter="";
//    	try
//		{						    
//			Log.v(TAG,"JimmyJin 1");
//			TelephonyManagerEx telephonyManager = TelephonyManagerEx.getDefault();
//			Log.v(TAG,"JimmyJin telephonyManager="+telephonyManager);
//			if(telephonyManager!=null)
//			{				
//				List <SIMInfo> simList=SIMInfo.getInsertedSIMList(context);				
//				
//				for(int i=0;i<simList.size();i++)
//				{						
//					StrMsgServiceCenter=telephonyManager.getScAddress(simList.get(i).mSlot);					
//					if(!StrMsgServiceCenter.equalsIgnoreCase(""))
//					{
//						Log.e("whj","**************getMessageCenterNumber=:" + StrMsgServiceCenter);
//						break;
//					}
//				}
//			}				
//			else
//			{
//				Log.e("whj","**************getMessageCenterNumber ,TelephonyManager is null");
//			}	
//		}
//		catch(NoClassDefFoundError e)
//		{
//			e.printStackTrace();			
//			Log.e(TAG,"**************getMessageCenterNumber,TelephonyManager class not find");
//		}
//    	
//    	return StrMsgServiceCenter;
//    }	
	
    public static String getSystemVersionName(Context context) {
        String strSystemVersionName = null;

        try {
            if(!SystemProperties.get("ro.build.hq.version.release", "Unknown").equals("Unknown"))
                strSystemVersionName = SystemProperties.get("ro.build.hq.version.release");
            else if(!SystemProperties.get("ro.build.hq.sw.version", "Unknown").equals("Unknown"))
                strSystemVersionName = SystemProperties.get("ro.build.hq.sw.version");
            else strSystemVersionName = "Unknown";
        } catch(Exception e) {
            Log.w(TAG, "getSystemVersionName catch exception!");
            return null;
        }
        
        if (strSystemVersionName != null) {
            Log.w(TAG, strSystemVersionName);
        }
        
        return strSystemVersionName;
    }
	
//	public static String getAPKname(String filename){
//		if(null == filename){
//			return null;
//		}
//		String downloadDir = Environment.getExternalStorageDirectory() + "/hqappmarket/apks/";
//		File Dir = new File(downloadDir);
//		File[] files = Dir.listFiles();
//		if()
//		return packageinfo.packageName;
//	}
	
}