package com.huaqin.market.download;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Performs the background downloads requested by applications that use the Downloads provider.
 */
public class DownloadService extends Service {

    /* ------------ Members ------------ */

    /** Observer to get notified when the content observer's data changes */
    private DownloadManagerContentObserver mObserver;
    
    /** Class to handle Notification Manager updates */
    private DownloadNotification mNotifier;

    /**
     * The Service's view of the list of downloads. This is kept independently
     * from the content provider, and the Service only initiates downloads
     * based on this data, so that it can deal with situation where the data
     * in the content provider changes or disappears.
     */
    private ArrayList<DownloadInfo> mDownloads;

    /**
     * The thread that updates the internal download list from the content
     * provider.
     */
    private UpdateThread mUpdateThread;

    /**
     * Whether the internal download list should be updated from the content
     * provider.
     */
    private boolean mPendingUpdate;

    /**
     * Array used when extracting strings from content provider
     */
    private CharArrayBuffer oldChars;

    /**
     * Array used when extracting strings from content provider
     */
    private CharArrayBuffer mNewChars;
    private DownloadTaskManager mDownloadControl;

    /* ------------ Inner Classes ------------ */

    /**
     * Receives notifications when the data in the content provider changes
     */
    private class DownloadManagerContentObserver extends ContentObserver {

        public DownloadManagerContentObserver() {
            super(new Handler());
        }

        /**
         * Receives notification when the data in the observed content
         * provider changes.
         */
        @Override
		public void onChange(final boolean selfChange) {
            updateFromProvider();
        }

    }

    /* ------------ Methods ------------ */

    /**
     * Returns an IBinder instance when someone wants to connect to this
     * service. Binding to this service is not allowed.
     *
     * @throws UnsupportedOperationException
     */
    @Override
	public IBinder onBind(Intent i) {
        throw new UnsupportedOperationException("Cannot bind to Download Manager Service");
    }

    /**
     * Initializes the service when it is first created
     */
    @Override
	public void onCreate() {
        super.onCreate();
        Log.v("asd", "DownloadService onCreate");
        mDownloads = new ArrayList<DownloadInfo>();;
        
        mObserver = new DownloadManagerContentObserver();
        getContentResolver().registerContentObserver(Downloads.CONTENT_URI,
                true, mObserver);
        
        mNotifier = new DownloadNotification(this);
        mNotifier.mNotificationMgr.cancelAll();
        mNotifier.updateNotification();

        trimDatabase();
        removeSpuriousFiles();
        updateFromProvider();
        
        mDownloadControl = new DownloadTaskManager(this);
    }

    /**
     * Responds to a call to startService
     */
    @Override
	public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        
        updateFromProvider();
        
		// by Jacob, 2012.02.02
        if(intent != null){
            controlDownloadTask(intent);
        }

    }

    /**
     * Cleans up when the service is destroyed
     */
    @Override
	public void onDestroy() {
        getContentResolver().unregisterContentObserver(mObserver);
        Log.w("DownloadService", "onDestroy begin");
        mNotifier.clearAllNotification();
        for (DownloadInfo info : mDownloads) {
        	mDownloadControl.cancelDownload(info);
        }
        mDownloads.clear();
        Log.w("DownloadService", "onDestroy end");
        super.onDestroy();
    }

    /**
     * Parses data from the content provider into private array
     */
    private void updateFromProvider() {
        synchronized (this) {
            mPendingUpdate = true;
            if (mUpdateThread == null) {
                mUpdateThread = new UpdateThread();
                mUpdateThread.start();
            }
        }
    }

    private class UpdateThread extends Thread {
        public UpdateThread() {
            super("Download Service");
        }
        
        @Override
		public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            
            boolean keepService = false;
            // for each update from the database, remember which download is
            // supposed to get restarted soonest in the future
            long wakeUp = Long.MAX_VALUE;
            for (;;) {
                synchronized (DownloadService.this) {
                    if (mUpdateThread != this) {
                        throw new IllegalStateException(
                                "multiple UpdateThreads in DownloadService");
                    }
                    if (!mPendingUpdate) {
                        mUpdateThread = null;
                        if (!keepService) {
                        	Log.w("downloadservice", "stop service...");
                            stopSelf();
                        }
                        if (wakeUp != Long.MAX_VALUE) {
                            AlarmManager alarms =
                                    (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            if (alarms == null) {
                                Log.e(Constants.TAG, "couldn't get alarm manager");
                            } else {
                                Intent intent = new Intent(Constants.ACTION_RETRY);
                                intent.setClassName("com.android.providers.downloads",
                                        DownloadReceiver.class.getName());
                                alarms.set(
                                        AlarmManager.RTC_WAKEUP,
                                        System.currentTimeMillis() + wakeUp,
                                        PendingIntent.getBroadcast(DownloadService.this, 0, intent,
                                                PendingIntent.FLAG_ONE_SHOT));
                            }
                        }
                        oldChars = null;
                        mNewChars = null;
                        return;
                    }
                    mPendingUpdate = false;
                }
                boolean networkAvailable = Helpers.isNetworkAvailable(DownloadService.this);
                Log.v(Constants.TAG,"JimmyJin networkAvailable="+networkAvailable);
                boolean networkRoaming = Helpers.isNetworkRoaming(DownloadService.this);
                long now = System.currentTimeMillis();
                
                Cursor cursor = getContentResolver().query(Downloads.CONTENT_URI,
                        null, null, null, BaseColumns._ID);
                
                if (cursor == null) {
                    // TODO: this doesn't look right, it'd leave the loop in an inconsistent state
                    return;
                }
                
                cursor.moveToFirst();
                
                int arrayPos = 0;
                
                keepService = false;
                wakeUp = Long.MAX_VALUE;
                
                boolean isAfterLast = cursor.isAfterLast();
                
                int idColumn = cursor.getColumnIndexOrThrow(BaseColumns._ID);
                
                /*
                 * Walk the cursor and the local array to keep them in sync. The key
                 *     to the algorithm is that the ids are unique and sorted both in
                 *     the cursor and in the array, so that they can be processed in
                 *     order in both sources at the same time: at each step, both
                 *     sources point to the lowest id that hasn't been processed from
                 *     that source, and the algorithm processes the lowest id from
                 *     those two possibilities.
                 * At each step:
                 * -If the array contains an entry that's not in the cursor, remove the
                 *     entry, move to next entry in the array.
                 * -If the array contains an entry that's in the cursor, nothing to do,
                 *     move to next cursor row and next array entry.
                 * -If the cursor contains an entry that's not in the array, insert
                 *     a new entry in the array, move to next cursor row and next
                 *     array entry.
                 */
                while (!isAfterLast || arrayPos < mDownloads.size()) {
                    if (isAfterLast) {
                      	Log.w("test", "JimmyJin isAfterLast!");
                        // We're beyond the end of the cursor but there's still some
                        //     stuff in the local array, which can only be junk
                        deleteDownload(arrayPos); // this advances in the array
                    } else {
                        int id = cursor.getInt(idColumn);
                        
                        if (arrayPos == mDownloads.size()) {
                        	Log.w("test", "arrayPos == mDownloads.size");
                            insertDownload(cursor, arrayPos, networkAvailable, networkRoaming, now);
                            if (visibleNotification(arrayPos)) {
                                keepService = true;
                            }
                            long next = nextAction(arrayPos, now);
                            if (next == 0) {
                                keepService = true;
                            } else if (next > 0 && next < wakeUp) {
                                wakeUp = next;
                            }
                            ++arrayPos;
                            cursor.moveToNext();
                            isAfterLast = cursor.isAfterLast();
                        } else {
                            int arrayId = mDownloads.get(arrayPos).mId;
                            
                            if (arrayId < id) {
                                // The array entry isn't in the cursor
                              	Log.w("test", "JimmyJin arrayId="+arrayId);
                              	Log.w("test", "JimmyJin id="+id);
                                deleteDownload(arrayPos); // this advances in the array
                            } else if (arrayId == id) {
                                // This cursor row already exists in the stored array
                                updateDownload(
                                        cursor, arrayPos,
                                        networkAvailable, networkRoaming, now);
                                if (visibleNotification(arrayPos)) {
                                    keepService = true;
                                }
                                long next = nextAction(arrayPos, now);
                                if (next == 0) {
                                    keepService = true;
                                } else if (next > 0 && next < wakeUp) {
                                    wakeUp = next;
                                }
                                ++arrayPos;
                                cursor.moveToNext();
                                isAfterLast = cursor.isAfterLast();
                            } else {
                                // This cursor entry didn't exist in the stored array
                            	Log.w("test", "arrayId > id");
                                insertDownload(
                                        cursor, arrayPos,
                                        networkAvailable, networkRoaming, now);
                                if (visibleNotification(arrayPos)) {
                                    keepService = true;
                                }
                                long next = nextAction(arrayPos, now);
                                if (next == 0) {
                                    keepService = true;
                                } else if (next > 0 && next < wakeUp) {
                                    wakeUp = next;
                                }
                                ++arrayPos;
                                cursor.moveToNext();
                                isAfterLast = cursor.isAfterLast();
                            }
                        }
                    }
                }
                
                mNotifier.updateNotification();
                
                cursor.close();
            }
        }
    }

    /**
     * Removes files that may have been left behind in the cache directory
     */
    private void removeSpuriousFiles() {
        File[] files = Environment.getDownloadCacheDirectory().listFiles();
        if (files == null) {
            // The cache folder doesn't appear to exist (this is likely the case
            // when running the simulator).
            return;
        }
        HashSet<String> fileSet = new HashSet<String>();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(Constants.KNOWN_SPURIOUS_FILENAME)) {
                continue;
            }
            if (files[i].getName().equalsIgnoreCase(Constants.RECOVERY_DIRECTORY)) {
                continue;
            }
            fileSet.add(files[i].getPath());
        }

        Cursor cursor = getContentResolver().query(Downloads.CONTENT_URI,
                new String[] { Downloads._DATA }, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    fileSet.remove(cursor.getString(0));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        Iterator<String> iterator = fileSet.iterator();
        while (iterator.hasNext()) {
            String filename = iterator.next();
            
            new File(filename).delete();
        }
    }

    /**
     * Drops old rows from the database to prevent it from growing too large
     */
    private void trimDatabase() {
        Cursor cursor = getContentResolver().query(Downloads.CONTENT_URI,
                new String[] { BaseColumns._ID },
                Downloads.COLUMN_STATUS + " >= '200'", null,
                Downloads.COLUMN_LAST_MODIFICATION);
        if (cursor == null) {
            // This isn't good - if we can't do basic queries in our database, nothing's gonna work
            Log.e(Constants.TAG, "null cursor in trimDatabase");
            return;
        }
        if (cursor.moveToFirst()) {
            int numDelete = cursor.getCount() - Constants.MAX_DOWNLOADS;
            int columnId = cursor.getColumnIndexOrThrow(BaseColumns._ID);
            while (numDelete > 0) {
                getContentResolver().delete(
                        ContentUris.withAppendedId(Downloads.CONTENT_URI,
                        cursor.getLong(columnId)), null, null);
                if (!cursor.moveToNext()) {
                    break;
                }
                numDelete--;
            }
        }
        cursor.close();
    }

    /**
     * Keeps a local copy of the info about a download, and initiates the
     * download if appropriate.
     */
    private void insertDownload(
            Cursor cursor, int arrayPos,
            boolean networkAvailable, boolean networkRoaming, long now) {
    	
        int statusColumn = cursor.getColumnIndexOrThrow(Downloads.COLUMN_STATUS);
        int failedColumn = cursor.getColumnIndexOrThrow(Constants.FAILED_CONNECTIONS);
        int retryRedirect =
                cursor.getInt(cursor.getColumnIndexOrThrow(Constants.RETRY_AFTER_X_REDIRECT_COUNT));
        
        DownloadInfo info = new DownloadInfo(
                cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(Downloads.COLUMN_URI)),
                cursor.getInt(cursor.getColumnIndexOrThrow(
                        Downloads.COLUMN_NO_INTEGRITY)) == 1,
                cursor.getString(cursor.getColumnIndexOrThrow(
                        Downloads.COLUMN_FILE_NAME_HINT)),
                cursor.getString(cursor.getColumnIndexOrThrow(Downloads._DATA)),
                cursor.getString(cursor.getColumnIndexOrThrow(Downloads.COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(Downloads.COLUMN_MIME_TYPE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.COLUMN_DESTINATION)),
                cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.COLUMN_VISIBILITY)),
                cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.COLUMN_CONTROL)),
                cursor.getInt(statusColumn),
                cursor.getInt(failedColumn),
                retryRedirect & 0xfffffff,
                retryRedirect >> 28,
                cursor.getLong(cursor.getColumnIndexOrThrow(
                        Downloads.COLUMN_LAST_MODIFICATION)),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        Downloads.COLUMN_NOTIFICATION_PACKAGE)),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        Downloads.COLUMN_NOTIFICATION_CLASS)),
                cursor.getString(cursor.getColumnIndexOrThrow(
                        Downloads.COLUMN_NOTIFICATION_EXTRAS)),
                cursor.getString(cursor.getColumnIndexOrThrow(Downloads.COLUMN_COOKIE_DATA)),
                cursor.getString(cursor.getColumnIndexOrThrow(Downloads.COLUMN_USER_AGENT)),
                cursor.getString(cursor.getColumnIndexOrThrow(Downloads.COLUMN_REFERER)),
                cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.COLUMN_TOTAL_BYTES)),
                cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.COLUMN_CURRENT_BYTES)),
                cursor.getString(cursor.getColumnIndexOrThrow(Constants.ETAG)),
                cursor.getInt(cursor.getColumnIndexOrThrow(Downloads.COLUMN_APP_ID)),
        		cursor.getString(cursor.getColumnIndexOrThrow(Downloads.COLUMN_PACKAGE_NAME)),
        		cursor.getString(cursor.getColumnIndexOrThrow(Downloads.COLUMN_APP_TYPE)),
        		cursor.getString(cursor.getColumnIndexOrThrow(Downloads.COLUMN_FROM_WHERE)),
        		cursor.getString(cursor.getColumnIndexOrThrow(Constants.LAST_MOFIFY_AT_SERVER))
        		);      
        mDownloads.add(arrayPos, info);
        
//        Log.d("DownlaodTask", "insertDownload C:" + info.mControl);
        if (info.canUseNetwork(networkAvailable, networkRoaming)) {
            Log.d("DownlaodTask", "insertDownload C: " + info.mControl + " status:" + info.mStatus+arrayPos);
            if(info.mStatus != Downloads.STATUS_SUCCESS &&
                    info.mControl == Downloads.CONTROL_RUN) {
                mDownloadControl.startDownload(info);
            }
        }
    }

    /**
     * Updates the local copy of the info about a download.
     */
    private void updateDownload(
            Cursor cursor, int arrayPos,
            boolean networkAvailable, boolean networkRoaming, long now) {
    	Log.w("test", "updateDownload "+arrayPos);
    	if(mDownloads.size()>0){
    		DownloadInfo info = mDownloads.get(arrayPos);
            int oldControl = info.mControl;
            int oldStatus = info.mStatus;
            int statusColumn = cursor.getColumnIndexOrThrow(Downloads.COLUMN_STATUS);
            int failedColumn = cursor.getColumnIndexOrThrow(Constants.FAILED_CONNECTIONS);
            info.mId = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID));
            info.mUri = stringFromCursor(info.mUri, cursor, Downloads.COLUMN_URI);
            info.mNoIntegrity = cursor.getInt(cursor.getColumnIndexOrThrow(
                    Downloads.COLUMN_NO_INTEGRITY)) == 1;
            info.mHint = stringFromCursor(info.mHint, cursor, Downloads.COLUMN_FILE_NAME_HINT);
            info.mFileName = stringFromCursor(info.mFileName, cursor, Downloads._DATA);
            info.mMimeType = stringFromCursor(info.mMimeType, cursor, Downloads.COLUMN_MIME_TYPE);
            info.mDestination = cursor.getInt(cursor.getColumnIndexOrThrow(
                    Downloads.COLUMN_DESTINATION));
            int newVisibility = cursor.getInt(cursor.getColumnIndexOrThrow(
                    Downloads.COLUMN_VISIBILITY));
            if (info.mVisibility == Downloads.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    && newVisibility != Downloads.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    && Downloads.isStatusCompleted(info.mStatus)) {
                mNotifier.mNotificationMgr.cancel(info.mId);
            }
            info.mVisibility = newVisibility;
            synchronized (info) {
                info.mControl = cursor.getInt(cursor.getColumnIndexOrThrow(
                        Downloads.COLUMN_CONTROL));
            }
            int newStatus = cursor.getInt(statusColumn);
            if (!Downloads.isStatusCompleted(info.mStatus) &&
                        Downloads.isStatusCompleted(newStatus)) {
                mNotifier.mNotificationMgr.cancel(info.mId);
            }
            info.mStatus = newStatus;
            info.mNumFailed = cursor.getInt(failedColumn);
            int retryRedirect =
                    cursor.getInt(cursor.getColumnIndexOrThrow(Constants.RETRY_AFTER_X_REDIRECT_COUNT));
            info.mRetryAfter = retryRedirect & 0xfffffff;
            info.mRedirectCount = retryRedirect >> 28;
            info.mLastMod = cursor.getLong(cursor.getColumnIndexOrThrow(
                    Downloads.COLUMN_LAST_MODIFICATION));
            info.mPackage = stringFromCursor(
                    info.mPackage, cursor, Downloads.COLUMN_NOTIFICATION_PACKAGE);
            info.mClass = stringFromCursor(
                    info.mClass, cursor, Downloads.COLUMN_NOTIFICATION_CLASS);
            info.mCookies = stringFromCursor(info.mCookies, cursor, Downloads.COLUMN_COOKIE_DATA);
            info.mUserAgent = stringFromCursor(
                    info.mUserAgent, cursor, Downloads.COLUMN_USER_AGENT);
            info.mReferer = stringFromCursor(info.mReferer, cursor, Downloads.COLUMN_REFERER);
            info.mTotalBytes = cursor.getInt(cursor.getColumnIndexOrThrow(
                    Downloads.COLUMN_TOTAL_BYTES));
            info.mCurrentBytes = cursor.getInt(cursor.getColumnIndexOrThrow(
                    Downloads.COLUMN_CURRENT_BYTES));
            info.mETag = stringFromCursor(info.mETag, cursor, Constants.ETAG);

            Log.w("DownloadService", "status="+info.mStatus+" oldstatus="+oldStatus+" control="+info.mControl+" oldcontrol="+oldControl);
            if (info.canUseNetwork(networkAvailable, networkRoaming)) {
            	if (info.mStatus != Downloads.STATUS_SUCCESS) {
            		if (info.mControl != oldControl) {
            			if (info.mControl == Downloads.CONTROL_PAUSED) {
            				mDownloadControl.pauseDownload(info);
            			} else {
            				mDownloadControl.startDownload(info);
            			}
            		} else if (info.mStatus != oldStatus && info.mStatus == Downloads.STATUS_PENDING && info.mControl == Downloads.CONTROL_RUN) {
            			Log.w("DownloadService", "status==Downloads.STATUS_PENDING so restart Download...");
            			mDownloadControl.startDownload(info);

            		}
            	}
            }
    	}
    		
    }

    /**
     * Returns a String that holds the current value of the column,
     * optimizing for the case where the value hasn't changed.
     */
    private String stringFromCursor(String old, Cursor cursor, String column) {
        int index = cursor.getColumnIndexOrThrow(column);
        if (old == null) {
            return cursor.getString(index);
        }
        if (mNewChars == null) {
            mNewChars = new CharArrayBuffer(128);
        }
        cursor.copyStringToBuffer(index, mNewChars);
        int length = mNewChars.sizeCopied;
        if (length != old.length()) {
            return cursor.getString(index);
        }
        if (oldChars == null || oldChars.sizeCopied < length) {
            oldChars = new CharArrayBuffer(length);
        }
        char[] oldArray = oldChars.data;
        char[] newArray = mNewChars.data;
        old.getChars(0, length, oldArray, 0);
        for (int i = length - 1; i >= 0; --i) {
            if (oldArray[i] != newArray[i]) {
                return new String(newArray, 0, length);
            }
        }
        return old;
    }

    /**
     * Removes the local copy of the info about a download.
     */
    private void deleteDownload(int arrayPos) {
    	Log.w("test", "deleteDownload"+arrayPos);
        DownloadInfo info = mDownloads.get(arrayPos);
    	Log.w("test", "JimmyJin info.mStatus="+info.mStatus);
    	Log.w("test", "JimmyJin info.mDestination="+info.mDestination);
    	Log.w("test", "JimmyJin info.mFileName="+info.mFileName);
        if (info.mStatus == Downloads.STATUS_RUNNING) {
            info.mStatus = Downloads.STATUS_CANCELED;
        } else if (info.mDestination != Downloads.DESTINATION_EXTERNAL
                    && info.mFileName != null) {
            new File(info.mFileName).delete();
        }
        mNotifier.mNotificationMgr.cancel(info.mId);

        mDownloads.remove(arrayPos);
        mDownloadControl.cancelDownload(info);
    }

    /**
     * Returns the amount of time (as measured from the "now" parameter)
     * at which a download will be active.
     * 0 = immediately - service should stick around to handle this download.
     * -1 = never - service can go away without ever waking up.
     * positive value - service must wake up in the future, as specified in ms from "now"
     */
    private long nextAction(int arrayPos, long now) {
        DownloadInfo info = mDownloads.get(arrayPos);
        if (Downloads.isStatusCompleted(info.mStatus)) {
            return -1;
        }
        if (info.mStatus != Downloads.STATUS_RUNNING_PAUSED) {
            return 0;
        }
        if (info.mNumFailed == 0) {
            return 0;
        } else {
        	Log.w("test", "DownloadInfo arrayPos="+arrayPos+" status="+info.mStatus+" numfailed="+info.mNumFailed);
        }
        long when = info.restartTime();
        if (when <= now) {
            return 0;
        }
        return when - now;
    }

    /**
     * Returns whether there's a visible notification for this download
     */
    private boolean visibleNotification(int arrayPos) {
        DownloadInfo info = mDownloads.get(arrayPos);
        return info.hasCompletionNotification();
    }

    /*
     * Process UI control
     *   @param strControl : "start" or "pause"
     *   @param strAppIds : string of app ids
     *   
     * by Jacob, 2012.02.02
     */
    private void controlDownloadTask(Intent intent) {
    	Log.v("controlDownloadTask", "intent = "+intent);
        String strControl = intent.getStringExtra("control");
        String strAppIds = intent.getStringExtra("appids");
        
        if ((strControl == null) || (strAppIds == null)) {
        	return;
        }
    	
    	if (!strControl.equals("start") && !strControl.equals("pause")) {
    		return;
    	}
    	
    	long[] appIds = Helpers.getAppIdsFromString(strAppIds);
    	for (long appid : appIds) {
    		DownloadInfo downloadinfo = null;
    		for (DownloadInfo info : mDownloads) {
    			if (info.mAppId == appid) {
    				downloadinfo = info;
    				break;
    			}
    		}
    		
    		if (downloadinfo == null) {
    			continue;
    		}
    		
    		if (strControl.equals("start")) {
    			downloadinfo.mControl = Downloads.CONTROL_RUN;
    			mDownloadControl.startDownload(downloadinfo);
    		} else if (strControl.equals("pause")) {
    			downloadinfo.mControl = Downloads.CONTROL_PAUSED;
    			mDownloadControl.pauseDownload(downloadinfo);
    		}
    	}
    }
}