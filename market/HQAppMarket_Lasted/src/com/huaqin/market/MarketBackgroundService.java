package com.huaqin.market;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import com.huaqin.android.market.sdk.bean.Application;
import com.huaqin.android.market.sdk.bean.InstalledApp;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.utils.PackageUtil;
import com.huaqin.market.utils.TimeUtil;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;

public class MarketBackgroundService extends Service{
    private static final String TAG = "MarketBackgroundService";
    private Timer nineTimer = null;
    private boolean nineTimerIsRunning = false;
    private BroadcastReceiver mNetworkStateReceiver;
    private boolean mNetworkStateReceiverIsRunning = false;
    private IMarketService mMarketService;
    private Context mContext;
    private int day = 0;
    private int year = 0;
    private int serviceDay = 0;
    private int serviceYear = 0;
//    private PackageUtil mPackageUtil;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mContext = this;
		mMarketService = MarketService.getServiceInstance(mContext);
		mNetworkStateReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				Log.d(TAG,"intent.getAction() ="+intent.getAction());
				if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
					Log.d(TAG,"CONNECTIVITY_ACTION");
					Time t=new Time(); 
					t.setToNow();
			        day = t.yearDay;
			        year = t.year;
			        SharedPreferences sharedPreferences = getSharedPreferences("Report", 0);
					serviceDay = sharedPreferences.getInt("serviceDay", 0);
					serviceYear = sharedPreferences.getInt("serviceYear", 0);
					Log.v("CONNECTIVITY_ACTION", "day ="+day);
					Log.v("CONNECTIVITY_ACTION", "year ="+year);
					Log.v("CONNECTIVITY_ACTION", "serviceDay ="+serviceDay);
					Log.v("CONNECTIVITY_ACTION", "serviceYear ="+serviceYear);
					if(((year-serviceYear)*365+(day-serviceDay)) > 2){
					boolean netIsAvailable = false;
					ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
					State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState(); 
                    if(State.CONNECTED == state){
                    	netIsAvailable = true;
                    }
                    state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState(); 
                    if(State.CONNECTED == state){
                    	netIsAvailable = true;
                    }
                    
                    if(netIsAvailable){
                    	
                    		Log.v("asd", "MarketService getUpdateAppsCount");
                    		SharedPreferences.Editor serviceEditor = getSharedPreferences("Report", MODE_PRIVATE).edit();
                			serviceEditor.putInt("serviceDay", day);
                			serviceEditor.putInt("serviceYear", year);
                			serviceEditor.commit();
                    		getUpdateAppsCount(mMarketService);
                    	}
                    }
				}
			}
    		
    	};
    	SharedPreferences sharedPreferences = getSharedPreferences("Report", 0);
		serviceDay = sharedPreferences.getInt("serviceDay", 0);
		serviceYear = sharedPreferences.getInt("serviceYear", 0);
		Time t=new Time(); 
		t.setToNow();
        day = t.yearDay;
        year = t.year;
        
		long setTime = (24*3) * 3600 * 1000;
		Log.v(TAG, "serviceDay ="+serviceDay);
		
//		int remainTime = TimeUtil.getRemainTime(serviceDay,serviceYear);
		if(((year-serviceYear)*365-(day-serviceDay)) > 2){
			Log.v(TAG, "serviceYear>2");
			nineTimer = new Timer();
			nineTimer.schedule(new BackgroundServiceRemindTask(), setTime);
			nineTimerIsRunning = true;
			
		}else{
//			onDestroy();
			Log.v(TAG, "stopSelf");
			stopSelf();
		}
		
	}
    private void nineTimerCancel(){
    	if(nineTimerIsRunning){
    		nineTimer.cancel();
    		nineTimerIsRunning = false;
    	}
    }
    private void unregisterNetStateReceiver(){
    	if(mNetworkStateReceiverIsRunning){
    		unregisterReceiver(mNetworkStateReceiver);
    	}
    }
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		nineTimerCancel();
		unregisterNetStateReceiver();
		Log.d(TAG,"MarketBackgroundService destory");
	}
	
    private void registerNetStateReceiver(){
    	IntentFilter filter = new IntentFilter();  
    	filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    	registerReceiver(mNetworkStateReceiver, filter);
    	mNetworkStateReceiverIsRunning = true;
    }
    
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		Log.d(TAG,"MarketBackgroundService start()");
		registerNetStateReceiver();
		boolean netIsWorked = checkNetworkState();
		if(netIsWorked){
			Time t=new Time(); 
			t.setToNow();
	        day = t.yearDay;
	        year = t.year;
	        SharedPreferences sharedPreferences = getSharedPreferences("Report", 0);
			serviceDay = sharedPreferences.getInt("serviceDay", 0);
			serviceYear = sharedPreferences.getInt("serviceYear", 0);
			Log.v("onStart", "day ="+day);
			Log.v("onStart", "year ="+year);
			Log.v("onStart", "serviceDay ="+serviceDay);
			Log.v("onStart", "serviceYear ="+serviceYear);
			if(((year-serviceYear)*365+(day-serviceDay)) > 2){
        		SharedPreferences.Editor serviceEditor = getSharedPreferences("Report", MODE_PRIVATE).edit();
    			serviceEditor.putInt("serviceDay", day);
    			serviceEditor.putInt("serviceYear", year);
    			serviceEditor.commit();
        		getUpdateAppsCount(mMarketService);
        	}
		}
		
	}
	
    private class BackgroundServiceRemindTask extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
//			onDestroy();
			Log.v(TAG, "BackgroundServiceRemindTask run");
			stopSelf();
		}
    	
    }
	
	

    private class UpdateAppandNotiThread extends Thread{
    	int updateAppCount = 0;
    	UpdateAppandNotiThread(int updateAppNumber){
    		updateAppCount = updateAppNumber;
    	}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
				sendNotification(updateAppCount);
//				onDestroy();
				stopSelf();
		}
    	
    }
	
	private void getUpdateAppsCount(IMarketService marketService){
		ArrayList<PackageInfo> pkgInfos = PackageUtil.getInstalledPackages(mContext);
		if (pkgInfos.size() == 0) {
    		return;
    	}
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
					int updateAppCount = list.size();
					if(updateAppCount > 0) {
						UpdateAppandNotiThread updateAppandThread = new UpdateAppandNotiThread(updateAppCount);
						updateAppandThread.start();
						
					} else {
						
					}
				} 
			}
		});
		marketService.checkAppUpdate(request);
	}
	
	private void sendNotification(int AppsCount){
		Log.d(TAG,"sendNotification() start");
		NotificationManager mNotificationManager;
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String contentTitle = mContext.getString(R.string.app_name);
		String contentText = AppsCount + mContext.getString(R.string.update_apps_hint);
		String tickerText = contentTitle + "," + contentText;
		int icon = R.drawable.logo;
		Intent notificationIntent = new Intent(this,SplashActivity.class);
		notificationIntent.putExtra("bUpdate", true);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
		
		Notification notification = new Notification(icon,tickerText,System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.flags |=  Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);
		mNotificationManager.notify(0, notification);
	}
	
	private boolean checkNetworkState() {
		// TODO Auto-generated method stub
		ConnectivityManager connectMgr =
			(ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if (connectMgr == null) {
			return false;
		}
		
		NetworkInfo nwInfo = connectMgr.getActiveNetworkInfo();
		
		if (nwInfo == null || !nwInfo.isAvailable()) {
			return false;
		}
		return true;
	}
	
}
