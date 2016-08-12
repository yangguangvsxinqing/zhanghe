package com.huaqin.market.download;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DownloadTaskManager {
    private final static String TAG = "DownloadTaskManager";
    
    private final static int TASK_STATE_DOWNLOADING = 1;
    private final static int TASK_STATE_PAUSE = 2;
    
    private final static int MAX_TASK_PROCESSING_SLOTS = 2;
    
    private Context mContext;
    private DownloadProcessor mProcessor = null;
    
    private HashMap<DownloadInfo, DownloadItem> mTaskMap = new HashMap<DownloadInfo, DownloadItem>();
    private LinkedList<Object> mTasksWaitingQueue = new LinkedList<Object>();
    private LinkedList<Object> mTasksPauseQueue = new LinkedList<Object>();
    private DownloadTask[] mTaskProcessingSlots = new DownloadTask[MAX_TASK_PROCESSING_SLOTS];
    private int mUsedThread = 0;
    
    private Handler mHandler = null;
    private final static int HANDLER_FINISHDOWNLOAD = 1;
    
    private class DownloadItem {
        DownloadTask mTask;
        int mState;
        DownloadItem(DownloadTask task, int state) {
            mTask = task;
            mState = state;
        }
    }
    
	DownloadTaskManager(Context context){
		mContext = context;
        mProcessor = new DownloadProcessor();
        mProcessor.start();
        
        initHandler();
	}
	
	private void initHandler() {
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case HANDLER_FINISHDOWNLOAD:
					int status = (int)msg.arg1;
					DownloadInfo info = (DownloadInfo)msg.obj;
						
					if (info != null) {
						finishDownload(info, status);
						
		                // by Jacob, 2012.02.11
						if ((mContext != null) && Helpers.isNetworkAvailable(mContext)) {
							Log.w(TAG, "finishDownload, update status from RUN_PAUSED or HTTP_ERROR to PENDING and start service...");
			        		ContentValues values = new ContentValues();
			        		values.put(Downloads.COLUMN_STATUS, Downloads.STATUS_PENDING);

			        		StringBuilder sb = new StringBuilder();
			        		sb.append(Downloads.COLUMN_STATUS+"="+Downloads.STATUS_RUNNING_PAUSED);
			        		sb.append(" OR ");
			        		sb.append(Downloads.COLUMN_STATUS+"="+Downloads.STATUS_HTTP_DATA_ERROR);
			        		String where = sb.toString();

			                mContext.getContentResolver().update(Downloads.CONTENT_URI, values, where, null);
			                mContext.startService(new Intent(mContext, DownloadService.class));
						}
					}
					break;
					
				default:
					break;
				}
			}
			
		};
	}
	
	private boolean isContainTask(DownloadInfo info) {
	    return mTaskMap.containsKey(info);
	}
	
	private DownloadItem GetContainerItem(DownloadInfo info) {
//		Log.v(TAG,"JimmyJin info8989="+info);
//		Log.v(TAG,"JimmyJin mTaskMap.containsKey(info)="+mTaskMap.containsKey(info));
	    if(!mTaskMap.containsKey(info)) {
	        return null;
	    }
	    
	    DownloadItem item = mTaskMap.get(info);
	    if(item == null) {
	        mTaskMap.remove(item);
	    }
	    return item;
	}
	
    public synchronized boolean startDownload(DownloadInfo info) {
        DownloadItem item = GetContainerItem(info);
        Log.d(TAG, "startDownload:" + info.mFileTitle);
        if(item == null) {
            startNewDownload(info);
        }else {
            if(item.mState == TASK_STATE_PAUSE) {
                resumDownloadInternal(item.mTask);
                item.mState = TASK_STATE_DOWNLOADING;
                notify();
            }
        }
        return true;
    }
    
    public synchronized boolean resumDownload(DownloadInfo info) {
        DownloadItem item = GetContainerItem(info);
        Log.d(TAG, "resumDownload:" + info.mFileTitle);
        if(item == null) {
            return false;
        }
        if(item.mState == TASK_STATE_PAUSE) {
            resumDownloadInternal(item.mTask);
            item.mState = TASK_STATE_DOWNLOADING;
            notify();
        }
        return true;
    }
    
    public synchronized boolean pauseDownload(DownloadInfo info) {
        DownloadItem item = GetContainerItem(info);
        Log.d(TAG, "pauseDownload:" + info.mFileTitle);
        if(item == null) {
            return false;
        }
        
        if(item.mState == TASK_STATE_DOWNLOADING) {
            if(mTasksWaitingQueue.remove(item.mTask)) {
//            	Log.v(TAG,"JimmyJin mTasksWaitingQueue.remove(item.mTask)");
                mTasksPauseQueue.addLast(item.mTask);
            }else if(removeTaskFromProcessing(item.mTask)) {
//            	Log.v(TAG,"JimmyJin removeTaskFromProcessing(item.mTask)");
                item.mTask.pause();
                mTasksPauseQueue.addLast(item.mTask);
                notify();
            }
            item.mState = TASK_STATE_PAUSE;
        }
        return true;
    }
    
    public synchronized boolean cancelDownload(DownloadInfo info) {
//    	Log.v(TAG,"JimmyJin cancelDownload");
//    	Log.v(TAG,"JimmyJin info.mAppId="+info.mAppId);
        if(removeTask(info)) {
            notify();
        }
        return true;
    }   
    
    private boolean removeTask(DownloadInfo info) {
        DownloadItem item = GetContainerItem(info);
        
        if(item == null) {
            return false;
        }
        
        if(removeTaskFromProcessing(item.mTask)) {
            item.mTask.cancel();
        }else if(!mTasksWaitingQueue.remove(item.mTask)){
            mTasksPauseQueue.remove(item.mTask);
        }
        mTaskMap.remove(info);
        return true;
    }
    
    private boolean resumDownloadInternal(DownloadTask task) {
        if(mTasksPauseQueue.remove(task)) {
            mTasksWaitingQueue.addLast(task);
            return true;
        }
        return false;
    }
    
    private boolean addTaskToProcessing(DownloadTask task) {
        //slots is full
        if(mUsedThread == mTaskProcessingSlots.length) {
            return false;
        }
        
        for(int i = 0; i < mTaskProcessingSlots.length; i++) {
            if(mTaskProcessingSlots[i] == null ){
                mTaskProcessingSlots[i] = task;
                mUsedThread++;
                return true;
            }
        }
        return false;
    }
    
    private boolean removeTaskFromProcessing(DownloadTask task) {
        if(mUsedThread == 0) {
            return false;
        }
        Log.d(TAG, "remove task");
        for(int i = 0; i < mTaskProcessingSlots.length; i++) {
            if(mTaskProcessingSlots[i] != null  &&
                    mTaskProcessingSlots[i].equals(task)){
                mTaskProcessingSlots[i] = null;
//                Log.d(TAG, "find & remove task");
                mUsedThread--;
                return true;
            }
        }
        return false;
    }
    
    private synchronized void finishDownload(DownloadInfo info, int state) {
        Log.d(TAG, "finishDownload:"+info.mFileTitle);
        if(removeTask(info)) {
            notify();
            Log.d(TAG, "finishDownload=>schedule next");
        }
    }
    
    private void startNewDownload(final DownloadInfo info) {
        Log.d(TAG, "control:start new download:"+info.mFileTitle);
        DownloadTask task = new DownloadTask(mContext, info, new DownloadTask.DownloadCallback() {
            @Override
            public void callbackFinish(final int status) {
            	Message msg = mHandler.obtainMessage(HANDLER_FINISHDOWNLOAD, status, 0, info);
            	msg.sendToTarget();
            }
        });
//        Log.v(TAG,"JimmyJin startNewDownload123");
        DownloadItem item = new DownloadItem(task, TASK_STATE_DOWNLOADING);
        mTaskMap.put(info, item);
        mTasksWaitingQueue.addLast(task);
        notify();
        
        // notify UI list
        if (mContext != null) {
    		mContext.sendBroadcast(new Intent(Downloads.ACTION_DOWNLOAD_UPDATEUILIST));
        }
    }

    int getDownloadState(DownloadInfo info) {
        return 0;
    }
    
    @SuppressWarnings("finally")
    private boolean schedule() {
        boolean scheduled = true;
//        Log.v(TAG,"JimmyJin 111");
        synchronized (this) {
//            Log.v(TAG,"JimmyJin 222");
            try {
//                Log.v(TAG,"JimmyJin 333");
                wait();
                Log.d(TAG, "mUsedThread:" + mUsedThread+" waitingsize:"+mTasksWaitingQueue.size());
                while(hasEmptyProcessingSlot() && !isWaitingQueueEmpty()) {
                    DownloadTask task = popupTaskFromWaitingQueue();
                    if(task != null) {
                        addTaskToProcessing(task);
                        task.start();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                scheduled = false;
            }finally {
                return scheduled;
            }
        } 
    }
    
    private boolean hasEmptyProcessingSlot() {
        return mUsedThread < mTaskProcessingSlots.length;
    }
    
    private boolean isWaitingQueueEmpty() {
        return mTasksWaitingQueue.size() == 0;
    }
    
    private DownloadTask popupTaskFromWaitingQueue() {
        DownloadTask task = (DownloadTask) mTasksWaitingQueue.getFirst();
        if(task != null) {
            mTasksWaitingQueue.removeFirst();
        }
        return task;
    }
    
    private class DownloadProcessor extends Thread {
        public DownloadProcessor() {
            super("Download Process");
        }
        
        @Override
		public void run() {
            Log.d(TAG, "download control thread wait");
        	while(schedule());
        	Log.d(TAG, "download control thread end");
        }
    }
}