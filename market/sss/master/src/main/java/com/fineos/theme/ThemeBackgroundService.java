package com.fineos.theme;

import java.util.Timer;
import java.util.TimerTask;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.fineos.theme.activity.ThemeOnlineHomeActivity;
import com.fineos.theme.utils.FileManager;
import com.fineos.theme.utils.ThemeLog;
import com.fineos.theme.webservice.IThemeService;
import com.fineos.theme.webservice.ThemeService;
import com.huaqin.android.rom.sdk.bean.VersionUpgrade;
import com.huaqin.romcenter.utils.UpdateRequestAgent;

public class ThemeBackgroundService extends Service {
	private static final String TAG = "MarketBackgroundService";
	private Timer nineTimer = null;
	private boolean nineTimerIsRunning = false;
	private BroadcastReceiver mNetworkStateReceiver;
	private boolean mNetworkStateReceiverIsRunning = false;
	private IThemeService mThemeService;
	private DownloadManager downloadManager;
	private Context mContext;
	private int day = 0;
	private int year = 0;
	private int serviceDay = 0;
	private int serviceYear = 0;

	// private PackageUtil mPackageUtil;
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
		mThemeService = ThemeService.getServiceInstance(mContext);
		mNetworkStateReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				ThemeLog.i(TAG, "intent.getAction() =" + intent.getAction());
				if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
					Time t = new Time();
					t.setToNow();
					day = t.yearDay;
					year = t.year;
					SharedPreferences sharedPreferences = getSharedPreferences("Report", 0);
					serviceDay = sharedPreferences.getInt("serviceDay", 0);
					serviceYear = sharedPreferences.getInt("serviceYear", 0);
					ThemeLog.i("CONNECTIVITY_ACTION", "day =" + day);
					ThemeLog.i("CONNECTIVITY_ACTION", "year =" + year);
					ThemeLog.i("CONNECTIVITY_ACTION", "serviceDay =" + serviceDay);
					ThemeLog.i("CONNECTIVITY_ACTION", "serviceYear =" + serviceYear);
					if (((year - serviceYear) * 365 + (day - serviceDay)) > 6) {
						boolean netIsAvailable = false;
						ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
						State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
						if (State.CONNECTED == state) {
							netIsAvailable = true;
						}
						state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
						if (State.CONNECTED == state) {
							netIsAvailable = true;
						}

						if (netIsAvailable) {
							SharedPreferences.Editor serviceEditor = getSharedPreferences("Report", MODE_PRIVATE).edit();
							serviceEditor.putInt("serviceDay", day);
							serviceEditor.putInt("serviceYear", year);
							serviceEditor.commit();
							// getUpdateAppsCount(mThemeService);
						}
					}
				}
			}

		};
		SharedPreferences sharedPreferences = getSharedPreferences("Report", 0);
		serviceDay = sharedPreferences.getInt("serviceDay", 0);
		serviceYear = sharedPreferences.getInt("serviceYear", 0);
		Time t = new Time();
		t.setToNow();
		day = t.yearDay;
		year = t.year;

		long setTime = (24 * 3) * 3600 * 1000;

		if (((year - serviceYear) * 365 - (day - serviceDay)) > 6) {
			nineTimer = new Timer();
			nineTimer.schedule(new BackgroundServiceRemindTask(), setTime);
			nineTimerIsRunning = true;
			// ROMCenter.postUpdateInfo(new ThemeUpdateRequest());
		} else {
			stopSelf();
		}

	}

	private void nineTimerCancel() {
		if (nineTimerIsRunning) {
			nineTimer.cancel();
			nineTimerIsRunning = false;
		}
	}

	private void unregisterNetStateReceiver() {
		if (mNetworkStateReceiverIsRunning) {
			unregisterReceiver(mNetworkStateReceiver);
		}
	}

	public class ThemeUpdateRequest implements UpdateRequestAgent {

		@Override
		public void updateRequest(VersionUpgrade vp) {
			// TODO Auto-generated method stub
			if (vp != null) {
				startDownload(mContext, vp.getDownUrl());
			}
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		nineTimerCancel();
		unregisterNetStateReceiver();
	}

	private void registerNetStateReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mNetworkStateReceiver, filter);
		mNetworkStateReceiverIsRunning = true;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		registerNetStateReceiver();
		boolean netIsWorked = checkNetworkState();
		if (netIsWorked) {
			Time t = new Time();
			t.setToNow();
			day = t.yearDay;
			year = t.year;
			SharedPreferences sharedPreferences = getSharedPreferences("Report", 0);
			serviceDay = sharedPreferences.getInt("serviceDay", 0);
			serviceYear = sharedPreferences.getInt("serviceYear", 0);
			if (((year - serviceYear) * 365 + (day - serviceDay)) > 6) {
				SharedPreferences.Editor serviceEditor = getSharedPreferences("Report", MODE_PRIVATE).edit();
				serviceEditor.putInt("serviceDay", day);
				serviceEditor.putInt("serviceYear", year);
				serviceEditor.commit();
				getUpdate();
			}
		}

	}

	private class BackgroundServiceRemindTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// onDestroy();
			stopSelf();
		}

	}

	private class UpdateAppandNotiThread extends Thread {
		int updateAppCount = 0;

		UpdateAppandNotiThread() {
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			sendNotification();
			// onDestroy();
			stopSelf();
		}

	}

	private void startDownload(Context context, String downloadUrl) {
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Toast.makeText(mContext, getResources().getString(R.string.sd_unmounted), Toast.LENGTH_SHORT);
			return;
		}
		
		String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);

		// 开始下载
		Uri resource = Uri.parse(downloadUrl);
		DownloadManager.Request request = new DownloadManager.Request(resource);
		// request.setAllowedNetworkTypes(Request.NETWORK_MOBILE |
		// Request.NETWORK_WIFI);
		// request.setAllowedOverRoaming(false);
		// 设置文件类型
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(downloadUrl));
		request.setMimeType(mimeString);
		// 在通知栏中显示
		request.setShowRunningNotification(true);
		request.setVisibleInDownloadsUi(true);
		// sdcard的目录下的download文件夹
		request.setDestinationInExternalPublicDir(FileManager.THEME_DIR_PATH, fileName);
		request.setTitle(fileName);
	}

	private void getUpdate() {
		UpdateAppandNotiThread updateAppandThread = new UpdateAppandNotiThread();
		updateAppandThread.start();
	}

	private void sendNotification() {
		NotificationManager mNotificationManager;
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String contentTitle = mContext.getString(R.string.app_name);
		String contentText = mContext.getString(R.string.update_theme_hint);
		String tickerText = contentTitle + "," + contentText;
		int icon = R.drawable.ic_launcher;
		Intent notificationIntent = new Intent(this, ThemeOnlineHomeActivity.class);
		notificationIntent.putExtra("bUpdate", true);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

		Notification notification = new Notification(icon, tickerText, System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);
		mNotificationManager.notify(0, notification);
	}

	private boolean checkNetworkState() {
		// TODO Auto-generated method stub
		ConnectivityManager connectMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

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
