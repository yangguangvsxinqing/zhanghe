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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Process;
import android.util.Config;
import android.util.Log;

/**
 * Runs an actual download
 */
public class DownloadThread extends Thread {

    private Context mContext;
    private DownloadInfo mInfo;

    public DownloadThread(Context context, DownloadInfo info) {
        mContext = context;
        mInfo = info;
    }

    /**
     * Returns the user agent provided by the initiating app, or use the default one
     */
//    private String userAgent() {
//        String userAgent = mInfo.mUserAgent;
//        if (userAgent != null) {
//        }
//        if (userAgent == null) {
//            userAgent = Constants.DEFAULT_USER_AGENT;
//        }
//        return userAgent;
//    }

    /**
     * Executes the download in a separate thread
     */
    @Override
	public void run() {
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

        try {
            boolean continuingDownload = false;
//            String headerAcceptRanges = null;
            String headerContentDisposition = null;
            String headerContentLength = null;
            String headerContentLocation = null;
            String headerETag = null;
            String headerTransferEncoding = null;

            byte data[] = new byte[Constants.BUFFER_SIZE];

            int bytesSoFar = 0;

            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.TAG);
            wakeLock.acquire();

            filename = mInfo.mFileName;
            if (filename != null) {
                // We're resuming a download that got interrupted
                File f = new File(filename);
                if (f.exists()) {
                    long fileLength = f.length();
                    if (fileLength == 0) {
                        // The download hadn't actually started, we can restart from scratch
                        f.delete();
                        filename = null;
                    } else if (mInfo.mETag == null && !mInfo.mNoIntegrity) {
                        // Tough luck, that's not a resumable download
                        if (Config.LOGD) {
                            Log.d(Constants.TAG,
                                    "can't resume interrupted non-resumable download"); 
                        }
                        f.delete();
                        finalStatus = Downloads.STATUS_PRECONDITION_FAILED;
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
                        headerETag = mInfo.mETag;
                        continuingDownload = true;
                    }
                }
            }

            int bytesNotified = bytesSoFar;
            // starting with MIN_VALUE means that the first write will commit
            //     progress to the database
            long timeLastNotification = 0;

            client = new DefaultHttpClient();

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
                // Prepares the request and fires it.
                HttpGet request = new HttpGet(mInfo.mUri);

                if (mInfo.mCookies != null) {
                    request.addHeader("Cookie", mInfo.mCookies);
                }
                if (mInfo.mReferer != null) {
                    request.addHeader("Referer", mInfo.mReferer);
                }
                if (continuingDownload) {
                    if (headerETag != null) {
                        request.addHeader("If-Match", headerETag);
                    }
                    request.addHeader("Range", "bytes=" + bytesSoFar + "-");
                }

                HttpResponse response;
                try {
                    response = client.execute(request);
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
                    // Handles the response, saves the file
                    if (!continuingDownload) {
                        Header header = response.getFirstHeader("Accept-Ranges");
//                        if (header != null) {
//                            headerAcceptRanges = header.getValue();
//                        }
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
                        header = response.getFirstHeader("ETag");
                        if (header != null) {
                            headerETag = header.getValue();
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
                        DownloadFileInfo fileInfo = null;
//                        DownloadFileInfo fileInfo = Helpers.generateSaveFile(
//                                mContext,
//                                mInfo.mUri,
//                                mInfo.mHint,
//                                headerContentDisposition,
//                                headerContentLocation,
//                                mimeType,
//                                mInfo.mDestination,
//                                (headerContentLength != null) ?
//                                        Integer.parseInt(headerContentLength) : 0);
                        if (fileInfo.mFileName == null) {
                            finalStatus = fileInfo.mStatus;
                            request.abort();
                            break http_request_loop;
                        }
                        filename = fileInfo.mFileName;
                        stream = fileInfo.mStream;
                        
                        ContentValues values = new ContentValues();
                        values.put(Downloads._DATA, filename);
                        if (headerETag != null) {
                            values.put(Constants.ETAG, headerETag);
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
                    for (;;) {
                        int bytesRead;
                        try {
                            bytesRead = entityStream.read(data);
                        } catch (IOException ex) {
                            ContentValues values = new ContentValues();
                            values.put(Downloads.COLUMN_CURRENT_BYTES, bytesSoFar);
                            mContext.getContentResolver().update(contentUri, values, null, null);
                            if (!mInfo.mNoIntegrity && headerETag == null) {
                                finalStatus = Downloads.STATUS_PRECONDITION_FAILED;
                            } else if (!Helpers.isNetworkAvailable(mContext)) {
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
                        if (bytesRead == -1) { // success
                            ContentValues values = new ContentValues();
                            values.put(Downloads.COLUMN_CURRENT_BYTES, bytesSoFar);
                            if (headerContentLength == null) {
                                values.put(Downloads.COLUMN_TOTAL_BYTES, bytesSoFar);
                            }
                            mContext.getContentResolver().update(contentUri, values, null, null);
                            if ((headerContentLength != null)
                                    && (bytesSoFar
                                            != Integer.parseInt(headerContentLength))) {
                                if (!mInfo.mNoIntegrity && headerETag == null) {
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
                                    stream = new FileOutputStream(filename, true);
                                }
                                stream.write(data, 0, bytesRead);
                                if (mInfo.mDestination == Downloads.DESTINATION_EXTERNAL) {
                                    try {
                                        stream.close();
                                        stream = null;
                                    } catch (IOException ex) {
                                        // nothing can really be done if the file can't be closed
                                    }
                                }
                                break;
                            } catch (IOException ex) {
                                if (!Helpers.discardPurgeableFiles(
                                        mContext, Constants.BUFFER_SIZE)) {
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
            notifyDownloadCompleted(finalStatus, countRetry, retryAfter, redirectCount,
                    gotData, filename, newUri, mimeType);
        }
    }

    /**
     * Stores information about the completed download, and notifies the initiating application.
     */
    private void notifyDownloadCompleted(
            int status, boolean countRetry, int retryAfter, int redirectCount, boolean gotData,
            String filename, String uri, String mimeType) {
        notifyThroughDatabase(
                status, countRetry, retryAfter, redirectCount, gotData, filename, uri, mimeType);
        if (Downloads.isStatusCompleted(status)) {
            notifyThroughIntent();
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

    /**
     * Clean up a mimeType string so it can be used to dispatch an intent to
     * view a downloaded asset.
     * @param mimeType either null or one or more mime types (semi colon separated).
     * @return null if mimeType was null. Otherwise a string which represents a
     * single mimetype in lowercase and with surrounding whitespaces trimmed.
     */
    private String sanitizeMimeType(String mimeType) {
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
