package com.huaqin.market.download;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Process;
import android.os.ServiceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.RemoteViews;

import com.huaqin.market.R;
import com.huaqin.market.SplashActivity;

class DownloadNotification {
	
	static final String LOGTAG = "HQDownloadNotification";
	static final String WHERE_RUNNING = "(status >= '100') AND (status <= '199') AND (visibility IS NULL OR visibility == '0' OR visibility == '1')";
	static final String WHERE_COMPLETED = "status >= '200' AND visibility == '1'";
	
	Context mContext;
	NotificationManager mNotificationMgr;
	HashMap<String, NotificationItem> mNotifications;
	private String[] mCols = new String [] {
            Downloads._ID,
            Downloads.COLUMN_TITLE,
            Downloads.COLUMN_DESCRIPTION,
            Downloads.COLUMN_NOTIFICATION_PACKAGE,
            Downloads.COLUMN_NOTIFICATION_CLASS,
            Downloads.COLUMN_CURRENT_BYTES,
            Downloads.COLUMN_TOTAL_BYTES,
            Downloads.COLUMN_STATUS,
            Downloads.COLUMN_LAST_MODIFICATION,
            Downloads.COLUMN_DESTINATION
    };
	
	private final int idColumn = 0;
	private final int titleColumn = 1;
	private final int descColumn = 2;
	private final int ownerColumn = 3;
//	private final int classOwnerColumn = 4;
	private final int currentBytesColumn = 5;
	private final int totalBytesColumn = 6;
	private final int statusColumn = 7;
	private final int lastModColumnId = 8;
	private final int destinationColumnId = 9;

	class NotificationItem {
		
		int mId;
		int mTotalCurrent = 0;
		int mTotalTotal = 0;
		int mTitleCount = 0;
		String mPackageName;
		String mDescription;
		String[] mTitles = new String[2];

		void addItem(String title, int currentBytes, int totalBytes) {
			mTotalCurrent += currentBytes;
            if (totalBytes <= 0 || mTotalTotal == -1) {
                mTotalTotal = -1;
            } else {
                mTotalTotal += totalBytes;
            }
            if (mTitleCount < 2) {
                mTitles[mTitleCount] = title;
            }
            mTitleCount++;
		}
	}

	DownloadNotification(Context context) {
		mContext = context;
		mNotificationMgr =
			(NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifications = new HashMap<String, NotificationItem>();
	}

	/*
     * Update the notification ui. 
     */
    public void updateNotification() {
        updateActiveNotification();
        updateCompletedNotification();
    }

    private void updateActiveNotification() {
        // Active downloads
    	Log.v("asd", "updateActiveNotification");
        Cursor c = mContext.getContentResolver().query(
                Downloads.CONTENT_URI, mCols,
                WHERE_RUNNING, null, BaseColumns._ID);
        
        if (c == null) {
            return;
        }
        
        // Collate the notifications
        mNotifications.clear();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            String packageName = c.getString(ownerColumn);
            int max = c.getInt(totalBytesColumn);
            int progress = c.getInt(currentBytesColumn);
            long id = c.getLong(idColumn);
            String title = c.getString(titleColumn);
            if (title == null || title.length() == 0) {
                title = mContext.getResources().getString(
                        R.string.download_unknown_title);
            }
            if (mNotifications.containsKey(packageName)) {
                mNotifications.get(packageName).addItem(title, progress, max);
            } else {
                NotificationItem item = new NotificationItem();
                item.mId = (int) id;
                item.mPackageName = packageName;
                item.mDescription = c.getString(descColumn);
                item.addItem(title, progress, max);
                mNotifications.put(packageName, item);
            }
        }
		if (c != null) {
			c.close();
		}

        // Add the notifications
        for (NotificationItem item : mNotifications.values()) {
            // Build the notification object
            Notification n = new Notification();
            
            n.icon = android.R.drawable.stat_sys_download;
            n.flags |= Notification.FLAG_ONGOING_EVENT;
            
            // Build the RemoteView object
            RemoteViews expandedView = new RemoteViews(
            		mContext.getPackageName(),
                    R.layout.status_bar_ongoing_event_progress_bar);
            StringBuilder title = new StringBuilder(item.mTitles[0]);
            if (item.mTitleCount > 1) {
                title.append(mContext.getString(R.string.notification_filename_separator));
                title.append(item.mTitles[1]);
                n.number = item.mTitleCount;
                if (item.mTitleCount > 2) {
                    title.append(mContext.getString(R.string.notification_filename_extras,
                            new Object[] { Integer.valueOf(item.mTitleCount - 2) }));
                }
            } else {
                expandedView.setTextViewText(R.id.statusbar_description, 
                        item.mDescription);
            }
            expandedView.setTextViewText(R.id.statusbar_title, title);
            expandedView.setProgressBar(R.id.statusbar_progress_bar, 
                    item.mTotalTotal,
                    item.mTotalCurrent,
                    item.mTotalTotal == -1);
            expandedView.setTextViewText(R.id.statusbar_progress_text, 
                    getDownloadingText(item.mTotalTotal, item.mTotalCurrent));
            expandedView.setImageViewResource(R.id.statusbar_appIcon,
                    android.R.drawable.stat_sys_download);
            n.contentView = expandedView;
            Log.v("asd", "start intent");
            Intent intent = new Intent(Constants.ACTION_LIST);
            intent.setClassName("com.android.providers.downloads",
                    DownloadReceiver.class.getName());
            intent.setData(Uri.parse(Downloads.CONTENT_URI + "/" + item.mId));
            intent.putExtra("multiple", item.mTitleCount > 1);
            Intent showActivity = new Intent(mContext, SplashActivity.class);
            Log.v("asd", "notify myPid = "+Process.myPid());
            if(Process.myPid() > 0) {
            } else {
            	showActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            	
            }
            showActivity.putExtra("bDownload", true);
            Log.v("asd", "notify showActivity = "+showActivity);
            n.contentIntent = PendingIntent.getActivity(mContext, 0, showActivity, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationMgr.notify(item.mId, n);
            
        }
    }

    private void updateCompletedNotification() {
        // Completed downloads
        Cursor c = mContext.getContentResolver().query(
                Downloads.CONTENT_URI, mCols,
                WHERE_COMPLETED, null, BaseColumns._ID);
        
        if (c == null) {
            return;
        }
        
        // Columns match projection in query above
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            // Add the notifications
            Notification n = new Notification();
            n.icon = android.R.drawable.stat_sys_download_done;

            long id = c.getLong(idColumn);
            String title = c.getString(titleColumn);
            if (title == null || title.length() == 0) {
                title = mContext.getResources().getString(
                        R.string.download_unknown_title);
            }
            Uri contentUri = Uri.parse(Downloads.CONTENT_URI + "/" + id);
            String caption;
            Intent intent;
            if (Downloads.isStatusError(c.getInt(statusColumn))) {
                caption = mContext.getResources()
                        .getString(R.string.notification_download_failed);
                intent = new Intent(Constants.ACTION_LIST);
            } else {
                caption = mContext.getResources()
                        .getString(R.string.notification_download_complete);
                if (c.getInt(destinationColumnId) == Downloads.DESTINATION_EXTERNAL) {
                    intent = new Intent(Constants.ACTION_OPEN);
                } else {
                    intent = new Intent(Constants.ACTION_LIST);
                }
            }
            intent.setClassName("com.android.providers.downloads",
                    DownloadReceiver.class.getName());
            intent.setData(contentUri);

            n.when = c.getLong(lastModColumnId);
            n.setLatestEventInfo(mContext, title, caption,
                    PendingIntent.getBroadcast(mContext, 0, intent, 0));

            intent = new Intent(Constants.ACTION_HIDE);
            intent.setClassName("com.android.providers.downloads",
                    DownloadReceiver.class.getName());
            intent.setData(contentUri);
            n.deleteIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

            mNotificationMgr.notify(c.getInt(idColumn), n);
        }
        c.close();
    }

    /*
     * Helper function to build the downloading text.
     */
    private String getDownloadingText(long totalBytes, long currentBytes) {
        if (totalBytes <= 0) {
            return "";
        }
        long progress = currentBytes * 100 / totalBytes;
        StringBuilder sb = new StringBuilder();
        sb.append(progress);
        sb.append('%');
        return sb.toString();
    }

    public void cancelNotification(long id) {
    	if (mNotificationMgr != null) {
    		mNotificationMgr.cancel((int) id);
    	}
    }

    public void clearAllNotification() {
		// by Jacob, 2012.02.10
    	if (mNotificationMgr != null && mNotifications != null) {
    		for (Iterator iter = mNotifications.entrySet().iterator(); iter.hasNext();) {
    			Map.Entry element = (Map.Entry)iter.next();
    			if (element != null) {
        			String strKey = (String)element.getKey();
        			NotificationItem item = (NotificationItem)element.getValue();
        			if (strKey != null && item != null) {
        				mNotificationMgr.cancel((int) item.mId);
        			}
    			}
    		}
    		mNotifications.clear();
    	}
    }
}