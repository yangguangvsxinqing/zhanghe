package com.huaqin.market.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.SharedPreferences;

import com.huaqin.market.R;
import com.huaqin.market.utils.Constant;
import com.huaqin.market.webservice.IMarketService;
import com.huaqin.market.webservice.MarketService;
import com.huaqin.market.webservice.Request;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Process;
import android.util.Config;
import android.util.Log;

import android.content.Intent;

class DownloadTask {  
    public final static int STATE_DOWNLOADING = 1;
    public final static int STATE_PAUSE = 2;
    public final static int STATE_FINISH = 3;
    
    private final static String TAG = "DownloadTask";
    private final static String TAG_1 = "DownlaodTaskStatus";
//    private final static boolean DEBUG_ENABLE = true;
    
    private Context mContext;
    private DownloadInfo mInfo;
    private long mFileLength = -1;
    private volatile int mCurrentState = STATE_PAUSE;
    
    private DownloadCallback mCallback;  // by Jacob, 2012.01.31
//    boolean mCountRetry = false;
//    int mRetryAfter = 0;
//    int mRedirectCount = 0;
//    boolean mGotData = false;
    private String mMimeType = null;
    
    private boolean mDownloadWillStop = false;
    private boolean mDownloadWillRestart = false;
    private Thread mPrepareDownload = null;
    
    private  boolean mStop = false;
    
//    private FileBlockInfo[] mFileBlocksInfo = 
//        new FileBlockInfo[Constants.THREAD_COUNT];
//    
//    private BlockDownload[] mBlockDownload = 
//        new BlockDownload[Constants.THREAD_COUNT];
    
    private static IMarketService mMarketService;
    
//    boolean mPauseToRestart = false;
//    boolean mRestartToPause = true;
//    boolean mParentRunningState = true;
//    boolean mRetry = false;
//    private NotifyEnd mNotifyEnd;
    
    public interface DownloadCallback {
        public void callbackFinish(int status);
    }
     
    public DownloadTask(Context context, DownloadInfo info, DownloadCallback callbak) {
        mContext = context;
        mInfo = info;
        mCallback = callbak; // by Jacob, 2012.01.31
        
        Uri.parse(Downloads.CONTENT_URI + "/" + mInfo.mId);
        mMimeType = sanitizeMimeType(mInfo.mMimeType);
//        Log.v(TAG,"JimmyJin DownloadTask123");
//        mRedirectCount = mInfo.mRedirectCount;
        
    }

    int getState() {
        return mCurrentState;
    }
    
    public synchronized boolean start() {
        Log.d(TAG_1, "start:" + mInfo.mFileTitle + " curr:" + mCurrentState);
        
        if(mCurrentState == STATE_PAUSE) {
            mDownloadWillStop = false;
            mDownloadWillRestart = false;
            startPrepareDownload();
            return true;
        }else if(mCurrentState == STATE_DOWNLOADING) {
            if(mDownloadWillStop || mDownloadWillRestart) {
                mDownloadWillStop = false;
                mDownloadWillRestart = true;
                return true;
            }
        }
        return false;
    }
    
    public synchronized boolean pause() {
        Log.d(TAG_1, "pause:" + mInfo.mFileTitle + " curr:" + mCurrentState);
        
        mDownloadWillRestart = false;
        mDownloadWillStop = false;
        
//        if(mCurrentState == STATE_DOWNLOADING) {
            mDownloadWillStop = true;
//            cancelFileBlocksDownload();
//            return true;
//        }
        return true;
    }
    
    public synchronized boolean cancel() {
        Log.d(TAG, "cancel:" + mInfo.mFileTitle + " curr:" + mCurrentState);
        
        if (mContext != null) {
        	mContext.sendBroadcast(new Intent(Downloads.ACTION_DOWNLOAD_UPDATEUILIST));
        }
        
        mDownloadWillRestart = false;
        mDownloadWillStop = false;
        
        if(mCurrentState == STATE_DOWNLOADING) {
            mDownloadWillStop = true;
            cancelFileBlocksDownload();
        }
        
        if(mCurrentState == STATE_FINISH) {
            mCurrentState = STATE_PAUSE;
            cleanDownloadContext();
        }

        return true;
    }
    
    private void cleanDownloadContext() {
        
    }
    
    private boolean startPrepareDownload() {
    	//Marked-s by JimmyJin 20120514
        if(mPrepareDownload == null) {
            mCurrentState = STATE_DOWNLOADING;
            mPrepareDownload = new PrepareDownload();        
            mPrepareDownload.start();
            return true;
        }

        mCurrentState = STATE_DOWNLOADING;
        return true;
    }
    
    private boolean cancelFileBlocksDownload() {
        boolean workding = false;
//        for(BlockDownload thread :mBlockDownload) {
//            if(mPrepareDownload != null) {
            	PrepareDownload a = new PrepareDownload();
//            	Log.v(TAG,"JimmyJin cancelFileBlocksDownload!");
            	a.stopDownload();

                workding = true;
//            }
//        }
        return workding;
    }
    
    

    
    private void notifyDownloadCompleted(
            int status, boolean countRetry, int retryAfter, int redirectCount, boolean gotData,
            String filename, String uri, String mimeType) {
        Log.d(TAG, "come into notifyDownloadCompleted() status = " + status + "countRetry = " + countRetry);
        notifyThroughDatabase(
                status, countRetry, retryAfter, redirectCount, gotData, filename, uri, mimeType);
        if (Downloads.isStatusCompleted(status)) {
            notifyThroughIntent();
            
    		/***********Added-s by JimmyJin 20120509***********/
          //下载完成时间接口
			SharedPreferences sharedPreferences = mContext.getSharedPreferences("Report", 0);
			String userId = sharedPreferences.getString("userId", null);
//			Log.v(TAG,"JimmyJin DownloadAPK_userId="+userId);
//			Log.v(TAG,"JimmyJin String.valueOf(mInfo.mAppId)="+String.valueOf(mInfo.mAppId));
//			Log.v(TAG,"JimmyJin mInfo.mPkgName="+mInfo.mPkgName);
			//fromWhere 0:装机精灵 ; 1:布丁控 ; 2:一键安装
			String mFromWhere = String.valueOf(mInfo.mFromWhere);
//			Log.v(TAG,"JimmyJin DownloadTask_mFromWhere="+mFromWhere);
    		if(userId != null){
    			mMarketService = MarketService.getServiceInstance(mContext);
    			Request request = new Request(0, Constant.TYPE_DOWNLOAD_END);
    			Object[] params = new Object[4];
    			
    			params[0] = userId;
    			params[1] = String.valueOf(mInfo.mAppId);
    			params[2] = mInfo.mPkgName;
    			params[3] = mFromWhere;
    			
    			request.setData(params);			
    			mMarketService.postDownloadEnd(request);
    		}			
    		/***********Added-e by JimmyJin 20120509***********/
            
        }
        
        // by Jacob, 2012.01.31
        if (mCallback != null) {
        	mCallback.callbackFinish(status);
        }
        
		notifyThroughPrompt(status, filename);
    }

	private void notifyThroughPrompt(int status, String filename) {
        // by Jacob, 2012.02.08
        if (status == Downloads.STATUS_HTTP_DATA_ERROR) {
        	if (mContext != null) {
        		Log.w(TAG, "STATUS_HTTP_DATA_ERROR so prompt content...");
            	Intent intent = new Intent(Downloads.ACTION_DOWNLOAD_RESULTPROMT);
            	
            	String strDownloadNetError = mContext.getResources().getString(R.string.download_net_error);
            	String strDownloadStop = mContext.getResources().getString(R.string.download_stop);
            	StringBuilder builder = new StringBuilder();
            	builder.append((strDownloadNetError!=null)?strDownloadNetError:"");
            	builder.append((filename!=null)?filename:"");
            	builder.append((strDownloadStop!=null)?strDownloadStop:"");
            	intent.putExtra(Downloads.RESULTPROMPT, builder.toString());
            	
            	mContext.sendBroadcast(intent);
        	}
        }
    }

    private void notifyThroughDatabase(
            int status, boolean countRetry, int retryAfter, int redirectCount, boolean gotData,
            String filename, String uri, String mimeType) {
        ContentValues values = new ContentValues();
        values.put(Downloads.COLUMN_STATUS, status);
        values.put(Downloads._DATA, filename);
        if (uri != null) {
            values.put(Downloads.COLUMN_URI, uri);
        }
        values.put(Downloads.COLUMN_MIME_TYPE, mimeType);
        values.put(Downloads.COLUMN_LAST_MODIFICATION, System.currentTimeMillis());
        values.put(Constants.RETRY_AFTER_X_REDIRECT_COUNT, retryAfter + (redirectCount << 28));
        if (!countRetry) {
            values.put(Constants.FAILED_CONNECTIONS, 0);
        } else if (gotData) {
            values.put(Constants.FAILED_CONNECTIONS, 1);
        } else {
            values.put(Constants.FAILED_CONNECTIONS, mInfo.mNumFailed + 1);
        }
        mContext.getContentResolver().update(ContentUris.withAppendedId(
                Downloads.CONTENT_URI, mInfo.mId), values, null, null);
    }

    /**
     * Notifies the initiating app if it requested it. That way, it can know that the
     * download completed even if it's not actively watching the cursor.
     */
    private void notifyThroughIntent() {
        Uri uri = Uri.parse(Downloads.CONTENT_URI + "/" + mInfo.mId);
        mInfo.sendIntentIfRequested(uri, mContext);
    }
    
    private class PrepareDownload extends Thread {
    	   	
        @Override
        public void run(){
//            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
//            int finalyStatus = prepareFile();
//            notifyPrepareDownloadFinish(finalyStatus);
        	
//Added-s by JimmyJin 20120515
        	

            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            int finalStatus = Downloads.STATUS_UNKNOWN_ERROR;
            boolean countRetry = false;
            int retryAfter = 0;
            int redirectCount = mInfo.mRedirectCount;
            String newUri = null;
            boolean gotData = false;
            String filename = null;
            String mimeType = sanitizeMimeType(mInfo.mMimeType);
            FileOutputStream stream = null;
            HttpClient client = null;
            PowerManager.WakeLock wakeLock = null;
            Uri contentUri = Uri.parse(Downloads.CONTENT_URI + "/" + mInfo.mId);
//            Log.v("DownloadTask","JImmyJin 899!");
            try {
                boolean continuingDownload = false;
//                String headerAcceptRanges = null;
                String headerContentDisposition = null;
                String headerContentLength = null;
                String headerContentLocation = null;
//                String headerETag = null;
                String headerLastModify = null;
                String headerTransferEncoding = null;
                boolean fileNotChanged = false;

                byte data[] = new byte[Constants.BUFFER_SIZE];

                int bytesSoFar = 0;

                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.TAG);
                wakeLock.acquire();

                filename = mInfo.mFileName;
                Log.d("DownloadThread", "mInfo.mLastMofiyAtServer:"+mInfo.mLastMofiyAtServer);
//                Log.v("DownloadTask","JImmyJin filename888999="+filename);
                
            if (filename != null) {
                // We're resuming a download that got interrupted
                File f = new File(filename);
//                    Log.v("DownloadTask","JImmyJin f.exists()="+f.exists());
//                    Log.v("DownloadTask","JImmyJin mInfo.mETag="+mInfo.mETag);
//                    Log.v("DownloadTask","JImmyJin mInfo.mNoIntegrity="+mInfo.mNoIntegrity);
//                    
                    if (f.exists()) {
                        long fileLength = f.length();
                        if (fileLength == 0) {
                            // The download hadn't actually started, we can restart from scratch
                            f.delete();
                            filename = null;
                        } else if (mInfo.mLastMofiyAtServer == null && !mInfo.mNoIntegrity) {
                            // Tough luck, that's not a resumable download
                            if (Config.LOGD) {
                                Log.d(Constants.TAG,
                                        "can't resume interrupted non-resumable download"); 
                            }
                            f.delete();
                            finalStatus = Downloads.STATUS_PRECONDITION_FAILED;
//                            Log.v("DownloadTask","JImmyJin finalStatus666="+finalStatus);
                            notifyDownloadCompleted(
                                    finalStatus, false, 0, 0, false, filename, null, mInfo.mMimeType);
                            return;
                        } else {
                            // All right, we'll be able to resume this download
                            stream = new FileOutputStream(filename, true);
                            bytesSoFar = (int) fileLength;
                            if (mInfo.mTotalBytes != -1) {
                                headerContentLength = Integer.toString(mInfo.mTotalBytes);
                            }
//                            headerETag = mInfo.mETag;
                            headerLastModify = mInfo.mLastMofiyAtServer;
                            continuingDownload = true;
                        }
                    }
                }

                int bytesNotified = bytesSoFar;
                // starting with MIN_VALUE means that the first write will commit
                //     progress to the database
                long timeLastNotification = 0;

                client = new DefaultHttpClient();
//                Log.v("DownloadTask","JImmyJin client="+client);

                if (stream != null && mInfo.mDestination == Downloads.DESTINATION_EXTERNAL) {
                    try {
                        stream.close();
                        stream = null;
                    } catch (IOException ex) {
                        // nothing can really be done if the file can't be closed
                    }
                }

                /*
                 * This loop is run once for every individual HTTP request that gets sent.
                 * The very first HTTP request is a "virgin" request, while every subsequent
                 * request is done with the original ETag and a byte-range.
                 */
    http_request_loop:
    			
                while (true) {
//                	Log.v("DownloadTask","JImmyJin while (true)!!!");
                	                	
                    // Prepares the request and fires it.
                    HttpGet request = new HttpGet(mInfo.mUri);

                    if (mInfo.mCookies != null) {
//                    	Log.v("DownloadTask","JImmyJin mInfo.mCookies="+mInfo.mCookies);
                        request.addHeader("Cookie", mInfo.mCookies);
                    }
                    if (mInfo.mReferer != null) {
//                    	Log.v("DownloadTask","JImmyJin mInfo.mReferer="+mInfo.mReferer);
                        request.addHeader("Referer", mInfo.mReferer);
                    }
                    if (continuingDownload) {
                        if (headerLastModify != null&&!fileNotChanged) {
                            request.addHeader("If-Modified-Since", headerLastModify);
//                            request.addHeader("If-Match", headerLastModify);
                        }
                        if(fileNotChanged){
                        	request.addHeader("Range", "bytes=" + bytesSoFar + "-");
                        }
                    }


                    HttpResponse response;
                    
                    Header[] hs = request.getAllHeaders();
                    for(Header h:hs){
                    	Log.d("DownloadThread", "header:"+h.getName()+":"+h.getValue());
                    }
                    try {
                        response = client.execute(request);
//                        Log.v("DownloadTask","JImmyJin response="+response);
                    } catch (IllegalArgumentException ex) {
                        finalStatus = Downloads.STATUS_BAD_REQUEST;
                        request.abort();
                        break http_request_loop;
                    } catch (IOException ex) {
                        if (!Helpers.isNetworkAvailable(mContext)) {
                            finalStatus = Downloads.STATUS_RUNNING_PAUSED;
                        } else if (mInfo.mNumFailed < Constants.MAX_RETRIES) {
                            finalStatus = Downloads.STATUS_RUNNING_PAUSED;
                            countRetry = true;
                        } else {
                            finalStatus = Downloads.STATUS_HTTP_DATA_ERROR;
                        }
                        request.abort();
                        break http_request_loop;
                    }
                                                       
                    int statusCode = response.getStatusLine().getStatusCode();
                    Log.v(TAG,"JImmyJin statusCode="+statusCode);
                    if (statusCode == 503 && mInfo.mNumFailed < Constants.MAX_RETRIES) {
                        finalStatus = Downloads.STATUS_RUNNING_PAUSED;
                        countRetry = true;
                        Header header = response.getFirstHeader("Retry-After");
                        if (header != null) {
                           try {
                               retryAfter = Integer.parseInt(header.getValue());
                               if (retryAfter < 0) {
                                   retryAfter = 0;
                               } else {
                                   if (retryAfter < Constants.MIN_RETRY_AFTER) {
                                       retryAfter = Constants.MIN_RETRY_AFTER;
                                   } else if (retryAfter > Constants.MAX_RETRY_AFTER) {
                                       retryAfter = Constants.MAX_RETRY_AFTER;
                                   }
                                   retryAfter += Helpers.sRandom.nextInt(Constants.MIN_RETRY_AFTER + 1);
                                   retryAfter *= 1000;
                               }
                           } catch (NumberFormatException ex) {
                               // ignored - retryAfter stays 0 in this case.
                           }
                        }
                        request.abort();
                        break http_request_loop;
                    }
                    if (statusCode == 304){//file not change 
                    	
                    	fileNotChanged = true;
                    	request.abort();
                    	continue;
                    }
                    if (statusCode == 301 ||
                            statusCode == 302 ||
                            statusCode == 303 ||
                            statusCode == 307) {
                        if (redirectCount >= Constants.MAX_REDIRECTS) {
                            finalStatus = Downloads.STATUS_TOO_MANY_REDIRECTS;
                            request.abort();
                            break http_request_loop;
                        }
                        Header header = response.getFirstHeader("Location");
                        if (header != null) {
                            try {
                                newUri = new URI(mInfo.mUri).
                                        resolve(new URI(header.getValue())).
                                        toString();
                            } catch(URISyntaxException ex) {
                                finalStatus = Downloads.STATUS_BAD_REQUEST;
                                request.abort();
                                break http_request_loop;
                            }
                            ++redirectCount;
                            finalStatus = Downloads.STATUS_RUNNING_PAUSED;
                            request.abort();
                            break http_request_loop;
                        }
                    }
                    if ((!continuingDownload && statusCode != Downloads.STATUS_SUCCESS)
                            || (continuingDownload && statusCode != 206)) {
                        if (Downloads.isStatusError(statusCode)) {
                            finalStatus = statusCode;
                        } else if (statusCode >= 300 && statusCode < 400) {
                            finalStatus = Downloads.STATUS_UNHANDLED_REDIRECT;
                        } else if (continuingDownload && statusCode == Downloads.STATUS_SUCCESS) {
                            finalStatus = Downloads.STATUS_PRECONDITION_FAILED;
                        } else {
                            finalStatus = Downloads.STATUS_UNHANDLED_HTTP_CODE;
                        }
                        request.abort();
                        break http_request_loop;
                    } else {
//                    	 Log.v(TAG,"JImmyJin continuingDownload="+continuingDownload);
                        // Handles the response, saves the file
                        if (!continuingDownload) {
                            Header header = response.getFirstHeader("Accept-Ranges");
//                            if (header != null) {
//                                headerAcceptRanges = header.getValue();
//                            }
                            header = response.getFirstHeader("Content-Disposition");
                            if (header != null) {
                                headerContentDisposition = header.getValue();
                            }
                            header = response.getFirstHeader("Content-Location");
                            if (header != null) {
                                headerContentLocation = header.getValue();
                            }
                            if (mimeType == null) {
                                header = response.getFirstHeader("Content-Type");
                                if (header != null) {
                                    mimeType = sanitizeMimeType(header.getValue()); 
                                }
                            }
//                            header = response.getFirstHeader("ETag");
                            header = response.getFirstHeader("Last-Modified");                            
//                            if (header != null) {
//                                headerETag = header.getValue();
//                            }
                            if (header != null) {
                            	headerLastModify = header.getValue();
                            	mInfo.mLastMofiyAtServer = headerLastModify;
                            	Log.d("DownloadThread", "headerLastModify:"+headerLastModify);
                            }                            
                            header = response.getFirstHeader("Transfer-Encoding");
                            if (header != null) {
                                headerTransferEncoding = header.getValue();
                            }
                            if (headerTransferEncoding == null) {
                                header = response.getFirstHeader("Content-Length");
                                if (header != null) {
                                    headerContentLength = header.getValue();
                                }
                            } else {
                                // Ignore content-length with transfer-encoding - 2616 4.4 3
                            }

                            if (!mInfo.mNoIntegrity && headerContentLength == null &&
                                    (headerTransferEncoding == null
                                            || !headerTransferEncoding.equalsIgnoreCase("chunked"))
                                    ) {
                                finalStatus = Downloads.STATUS_LENGTH_REQUIRED;
                                request.abort();
                                break http_request_loop;
                            }

                        DownloadFileInfo fileInfo = Helpers.generateSaveFile(
                                mContext,
                                mInfo.mUri,
                                mInfo.mHint,
                                headerContentDisposition,
                                headerContentLocation,
                                mimeType,
                                mInfo.mDestination,
                                (headerContentLength != null) ?
                                        Integer.parseInt(headerContentLength) : 0);
                            

                            if (fileInfo.mFileName == null) {
                                finalStatus = fileInfo.mStatus;
                                request.abort();
                                break http_request_loop;
                            }
                            filename = fileInfo.mFileName;
                            stream = fileInfo.mStream;
                            
                            ContentValues values = new ContentValues();
                            values.put(Downloads._DATA, filename);
//                            if (headerETag != null) {
//                                values.put(Constants.ETAG, headerETag);
//                            }
                            if (headerLastModify != null) {
                                values.put(Constants.LAST_MOFIFY_AT_SERVER, headerLastModify);
                            }                            
                            if (mimeType != null) {
                                values.put(Downloads.COLUMN_MIME_TYPE, mimeType);
                            }
                            int contentLength = -1;
                            if (headerContentLength != null) {
                                contentLength = Integer.parseInt(headerContentLength);
                            }
                            values.put(Downloads.COLUMN_TOTAL_BYTES, contentLength);
                            mContext.getContentResolver().update(contentUri, values, null, null);
                        }

                        InputStream entityStream;
                        try {
                            entityStream = response.getEntity().getContent();
                        } catch (IOException ex) {
                            if (!Helpers.isNetworkAvailable(mContext)) {
//                            	Log.v(TAG,"JImmyJin !Helpers.isNetworkAvailable(mContext)");
                                finalStatus = Downloads.STATUS_RUNNING_PAUSED;
                            } else if (mInfo.mNumFailed < Constants.MAX_RETRIES) {
//                            	Log.v(TAG,"JImmyJin mInfo.mNumFailed="+mInfo.mNumFailed);
                                finalStatus = Downloads.STATUS_RUNNING_PAUSED;
                                countRetry = true;
                            } else {
                                finalStatus = Downloads.STATUS_HTTP_DATA_ERROR;
                            }
                            request.abort();
                            break http_request_loop;
                        }
                        for (;;) {
//                            Log.v(TAG,"JImmyJin mStop="+mStop);
                            if (mStop) {
//                                Log.v(TAG,"JImmyJin mStop!");
                                request.abort();
                                break http_request_loop;
                            }
                        	
                            int bytesRead;
                            try {
//                            	Log.v(TAG,"JimmyJin data="+data); 
                                bytesRead = entityStream.read(data);
//                                Log.v(TAG,"JimmyJin bytesRead="+bytesRead); 
                            } catch (IOException ex) {
                                ContentValues values = new ContentValues();
                                values.put(Downloads.COLUMN_CURRENT_BYTES, bytesSoFar);
                                mContext.getContentResolver().update(contentUri, values, null, null);
                                if (!mInfo.mNoIntegrity && headerLastModify == null) {
//                                	Log.v(TAG,"JimmyJin !mInfo.mNoIntegrity && headerETag == null"); 
                                    finalStatus = Downloads.STATUS_PRECONDITION_FAILED;
                                } else if (!Helpers.isNetworkAvailable(mContext)) {
//                                	Log.v(TAG,"JImmyJin !Helpers.isNetworkAvailable(mContext)6666");
                                    finalStatus = Downloads.STATUS_RUNNING_PAUSED;
                                } else if (mInfo.mNumFailed < Constants.MAX_RETRIES) {
//                                	Log.v(TAG,"JImmyJin mInfo.mNumFailed6666="+mInfo.mNumFailed);
                                    finalStatus = Downloads.STATUS_RUNNING_PAUSED;
                                    countRetry = true;
                                } else {
                                    finalStatus = Downloads.STATUS_HTTP_DATA_ERROR;
                                }
                                request.abort();
                                break http_request_loop;
                            }
                            if (bytesRead == -1) { // success
                                ContentValues values = new ContentValues();
//                                Log.v(TAG,"JimmyJin bytesSoFar="+bytesSoFar); 
                               
                                values.put(Downloads.COLUMN_CURRENT_BYTES, bytesSoFar);
                                if (headerContentLength == null) {
//                                	Log.v(TAG,"JimmyJin 234234234"); 
                                    values.put(Downloads.COLUMN_TOTAL_BYTES, bytesSoFar);
                                }
                                mContext.getContentResolver().update(contentUri, values, null, null);
                                if ((headerContentLength != null)
                                        && (bytesSoFar
                                                != Integer.parseInt(headerContentLength))) {
                                    if (!mInfo.mNoIntegrity && headerLastModify == null) {
                                        finalStatus = Downloads.STATUS_LENGTH_REQUIRED;
                                    } else if (!Helpers.isNetworkAvailable(mContext)) {
                                        finalStatus = Downloads.STATUS_RUNNING_PAUSED;
                                    } else if (mInfo.mNumFailed < Constants.MAX_RETRIES) {
                                        finalStatus = Downloads.STATUS_RUNNING_PAUSED;
                                        countRetry = true;
                                    } else {
                                        finalStatus = Downloads.STATUS_HTTP_DATA_ERROR;
                                    }
                                    break http_request_loop;
                                }
                                break;
                            }
                            gotData = true;
                            for (;;) {
                                try {
                                    if (stream == null) {
//                                    	Log.v(TAG,"JimmyJin filename90="+filename); 
                                        stream = new FileOutputStream(filename, true);
                                    }
//                                    Log.v(TAG,"JimmyJin 345"); 
                                    stream.write(data, 0, bytesRead);
//                                    Log.v(TAG,"JimmyJin 789"); 
                                    if (mInfo.mDestination == Downloads.DESTINATION_EXTERNAL) {
                                        try {
                                            stream.close();
                                            stream = null;
                                        } catch (IOException ex) {
//                                        	Log.v(TAG,"JimmyJin 123456789"); 
                                            // nothing can really be done if the file can't be closed
                                        }
                                    }
                                    break;
                                } catch (IOException ex) {
                                    if (!Helpers.discardPurgeableFiles(
                                            mContext, Constants.BUFFER_SIZE)) {
//                                    	Log.v(TAG,"JimmyJin Helpers.discardPurgeableFiles="+ex);      	
                                        finalStatus = Downloads.STATUS_FILE_ERROR;
                                        break http_request_loop;
                                    }
                                }
                            }
                            bytesSoFar += bytesRead;
                            long now = System.currentTimeMillis();
                            if (bytesSoFar - bytesNotified > Constants.MIN_PROGRESS_STEP
                                    && now - timeLastNotification
                                            > Constants.MIN_PROGRESS_TIME) {
                                ContentValues values = new ContentValues();
                                values.put(Downloads.COLUMN_CURRENT_BYTES, bytesSoFar);
                                mContext.getContentResolver().update(
                                        contentUri, values, null, null);
                                bytesNotified = bytesSoFar;
                                timeLastNotification = now;
                            }

                            synchronized (mInfo) {
                                if (mInfo.mControl == Downloads.CONTROL_PAUSED) {
//                                	Log.v(TAG,"JimmyJin mInfo.mControl == Downloads.CONTROL_PAUSED!!!");
                                    finalStatus = Downloads.STATUS_RUNNING_PAUSED;
                                    request.abort();
                                    break http_request_loop;
                                }
                            }
                            if (mInfo.mStatus == Downloads.STATUS_CANCELED) {
                                finalStatus = Downloads.STATUS_CANCELED;
                                break http_request_loop;
                            }
                        }
                        finalStatus = Downloads.STATUS_SUCCESS;
                    }
                    break;
                }
            } catch (FileNotFoundException ex) {
//            	Log.v(TAG,"JimmyJin catch (FileNotFoundException ex)");  
                finalStatus = Downloads.STATUS_FILE_ERROR;
                // falls through to the code that reports an error
            } catch (RuntimeException ex) { //sometimes the socket code throws unchecked exceptions
                finalStatus = Downloads.STATUS_UNKNOWN_ERROR;
                // falls through to the code that reports an error
            } finally {
                mInfo.mHasActiveThread = false;
                if (wakeLock != null) {
                    wakeLock.release();
                    wakeLock = null;
                }
                if (client != null) {
                    //client.close();
                    client = null;
                }
                try {
                    // close the file
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException ex) {
                    // nothing can really be done if the file can't be closed
                }
                if (filename != null) {
                    // if the download wasn't successful, delete the file
                    if (Downloads.isStatusError(finalStatus)) {
                        new File(filename).delete();
                        filename = null;
                    }
                }
//                Log.v("DownloadTask","JImmyJin finalStatus="+finalStatus);
                notifyDownloadCompleted(finalStatus, countRetry, retryAfter, redirectCount,
                        gotData, filename, newUri, mimeType);
            }       	             	
//Added-e by JimmyJin 20120515        	
        }
        
        void stopDownload() {
//            Log.v(TAG,"JImmyJin stopDownload()!");
            mStop = true;
        }
        
        @Override
        public void destroy() {
            super.destroy();
//            Log.v(TAG,"JImmyJin destroy!");
        }
    }
    
    private static String sanitizeMimeType(String mimeType) {
        try {
            mimeType = mimeType.trim().toLowerCase(Locale.ENGLISH);

            final int semicolonIndex = mimeType.indexOf(';');
            if (semicolonIndex != -1) {
                mimeType = mimeType.substring(0, semicolonIndex);
            }
            return mimeType;
        } catch (NullPointerException npe) {
            return null;
        }
    }
}
