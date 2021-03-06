package com.huaqin.market.download;

import java.security.acl.LastOwnerException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Stores information about an individual download.
 */
public class DownloadInfo {
	
    public int mId;
    public String mUri;
    public boolean mNoIntegrity;
    public String mHint;
    public String mFileName;
    public String mFileTitle;
    public String mMimeType;
    public int mDestination;
    public int mVisibility;
    public int mControl;
    public int mStatus;
    public int mNumFailed;
    public int mRetryAfter;
    public int mRedirectCount;
    public long mLastMod;
    public String mPackage;
    public String mClass;
    public String mExtras;
    public String mCookies;
    public String mUserAgent;
    public String mReferer;
    public int mTotalBytes;
    public int mCurrentBytes;
    public String mETag;
    public String mLastMofiyAtServer;

    public int mFuzz;

    public volatile boolean mHasActiveThread;
    
    public int mAppId;
    public String mPkgName;
    
	/*************Added-s by JimmyJin for Pudding Project**************/
	public String mAppType;
	public String mFromWhere;
	/*************Added-e by JimmyJin for Pudding Project**************/

    public DownloadInfo(int id, String uri, boolean noIntegrity,
            String hint, String fileName,String fileTitle,
            String mimeType, int destination, int visibility, int control,
            int status, int numFailed, int retryAfter, int redirectCount, long lastMod,
            String pckg, String clazz, String extras, String cookies,
            String userAgent, String referer, int totalBytes, int currentBytes, String eTag, 
            int appId, String pkgName,String appType,String fromWhere,String lastModifyAtServer) {
        mId = id;
        mUri = uri;
        mNoIntegrity = noIntegrity;
        mHint = hint;
        mFileName = fileName;
        mFileTitle = fileTitle;
        mMimeType = mimeType;
        mDestination = destination;
        mVisibility = visibility;
        mControl = control;
        mStatus = status;
        mNumFailed = numFailed;
        mRetryAfter = retryAfter;
        mRedirectCount = redirectCount;
        mLastMod = lastMod;
        mPackage = pckg;
        mClass = clazz;
        mExtras = extras;
        mCookies = cookies;
        mUserAgent = userAgent;
        mReferer = referer;
        mTotalBytes = totalBytes;
        mCurrentBytes = currentBytes;
        mETag = eTag;
        mFuzz = Helpers.sRandom.nextInt(1001); 
        mAppId = appId;
        mPkgName = pkgName;
        mAppType = appType;
        mFromWhere = fromWhere;
        mLastMofiyAtServer = lastModifyAtServer;
    }

    public void sendIntentIfRequested(Uri contentUri, Context context) {
    	Log.v("", "ssss mPackage="+mPackage);
    	Log.v("", "ssss mClass="+mClass);
    	
        if (mPackage != null && mClass != null) {
            Intent intent = new Intent(Downloads.ACTION_DOWNLOAD_COMPLETED);
            intent.setClassName(mPackage, mClass);
            Log.v("", "ssss mExtras="+mExtras);
            if (mExtras != null) {
                intent.putExtra(Downloads.COLUMN_NOTIFICATION_EXTRAS, mExtras);
            }
            // We only send the content: URI, for security reasons. Otherwise, malicious
            //     applications would have an easier time spoofing download results by
            //     sending spoofed intents.
            intent.setData(contentUri);
            context.sendBroadcast(intent);
        }
    }

    /**
     * Returns the time when a download should be restarted. Must only
     * be called when numFailed > 0.
     */
    public long restartTime() {
        if (mRetryAfter > 0) {
            return mLastMod + mRetryAfter;
        }
        return mLastMod +
                Constants.RETRY_FIRST_DELAY *
                    (1000 + mFuzz) * (1 << (mNumFailed - 1));
    }

    /**
     * Returns whether this download (which the download manager hasn't seen yet)
     * should be started.
     */
    public boolean isReadyToStart(long now) {
        if (mControl == Downloads.CONTROL_PAUSED) {
            // the download is paused, so it's not going to start
            return false;
        }
        if (mStatus == 0) {
            // status hasn't been initialized yet, this is a new download
            return true;
        }
        if (mStatus == Downloads.STATUS_PENDING) {
            // download is explicit marked as ready to start
            return true;
        }
        if (mStatus == Downloads.STATUS_RUNNING) {
            // download was interrupted (process killed, loss of power) while it was running,
            //     without a chance to update the database
            return true;
        }
        if (mStatus == Downloads.STATUS_RUNNING_PAUSED) {
            if (mNumFailed == 0) {
                // download is waiting for network connectivity to return before it can resume
                return true;
            }
            if (restartTime() < now) {
                // download was waiting for a delayed restart, and the delay has expired
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether this download (which the download manager has already seen
     * and therefore potentially started) should be restarted.
     *
     * In a nutshell, this returns true if the download isn't already running
     * but should be, and it can know whether the download is already running
     * by checking the status.
     */
    public boolean isReadyToRestart(long now) {
        if (mControl == Downloads.CONTROL_PAUSED) {
            // the download is paused, so it's not going to restart
            return false;
        }
        if (mStatus == 0) {
            // download hadn't been initialized yet
            return true;
        }
        if (mStatus == Downloads.STATUS_PENDING) {
            // download is explicit marked as ready to start
            return true;
        }
        if (mStatus == Downloads.STATUS_RUNNING_PAUSED) {
            if (mNumFailed == 0) {
                // download is waiting for network connectivity to return before it can resume
                return true;
            }
            if (restartTime() < now) {
                // download was waiting for a delayed restart, and the delay has expired
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether this download has a visible notification after
     * completion.
     */
    public boolean hasCompletionNotification() {
        if (!Downloads.isStatusCompleted(mStatus)) {
            return false;
        }
        if (mVisibility == Downloads.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) {
            return true;
        }
        return false;
    }

    /**
     * Returns whether this download is allowed to use the network.
     */
    public boolean canUseNetwork(boolean available, boolean roaming) {
        if (!available) {
            return false;
        }
        if (mDestination == Downloads.DESTINATION_CACHE_PARTITION_NOROAMING) {
            return !roaming;
        } else {
            return true;
        }
    }
}