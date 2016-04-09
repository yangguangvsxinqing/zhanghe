package com.fineos.fileexplorer.bussiness;

import android.app.Dialog;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.fineos.fileexplorer.entity.StorageInfo;
import com.fineos.fileexplorer.service.AbstractStorageInfoFinder;
import com.fineos.fileexplorer.service.StorageInfoFinderSystemService;

import java.util.List;

public class StorageInfoLoader extends AsyncTaskLoader<List<StorageInfo>>{

	 // We hold a reference to the Loader's data here.
	private int LOAD_TIME_DELAY = 2000;
	private List<StorageInfo> mStorageInfoList;
	private StorageObserver mStorageObserver;
	private AbstractStorageInfoFinder mStorageInfoFinder;
	private Boolean mChangeFlag = false;
    private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			forceLoad();
		}
	};
	
	
	public StorageInfoLoader(Context context) {
		 // Loaders may be used across multiple Activitys (assuming they aren't
	    // bound to the LoaderManager), so NEVER hold a reference to the context
	    // directly. Doing so will cause you to leak an entire Activity's context.
	    // The superclass constructor will store a reference to the Application
	    // Context instead, and can be retrieved with a call to getContext().
		super(context);
	}

	/**
	 * This method asynchronously load current mounted storage info and return a list of them.
	 * Concrete storage information loading method may change over time, so we use an abstract method to 
	 * get storage information list. 
	 * */
	@Override
	public List<StorageInfo> loadInBackground() {
		if(mStorageInfoFinder == null){
			mStorageInfoFinder = new StorageInfoFinderSystemService(getContext());
		}	
		return mStorageInfoFinder.buildStorageInfoList();
	}
	
	/**
	   * Called when there is new data to deliver to the client. The superclass will
	   * deliver it to the registered listener (i.e. the LoaderManager), which will
	   * forward the results to the client through a call to onLoadFinished.
	   */
	@Override
	public void deliverResult(List<StorageInfo> storageInfoList) {
		if (isReset()) {
			if (storageInfoList != null) {
				releaseResources(storageInfoList);
			}
		}
		List<StorageInfo> oldStorageInfoList = mStorageInfoList;
		mStorageInfoList = storageInfoList;

		if (isStarted()) {
			// If the Loader is in a started state, have the superclass deliver
			// the results to the client.
			super.deliverResult(storageInfoList);
		}

		// Invalidate the old data as we don't need it any more.
		if (oldStorageInfoList != null && oldStorageInfoList != storageInfoList) {
			releaseResources(oldStorageInfoList);
		}
	}
	
	@Override
	protected void onStartLoading() {
		if (mStorageInfoList != null) {
			// Deliver any previously loaded data immediately.
			deliverResult(mStorageInfoList);
		}

		if (mStorageObserver == null) {
			mStorageObserver = StorageObserver.getInstance(this);
		}

		if (takeContentChanged()) {
			forceLoad();
		} else if (mStorageInfoList == null) {
			forceLoad();
		}
	}
	
	@Override
	protected void onStopLoading() {
		cancelLoad();
	}
	
	@Override
	protected void onReset() {
		// Ensure the loader is stopped.
		onStopLoading();
		
		// At this point we can release the resources associated with 'storageinfolist'.
	    if (mStorageInfoList != null) {
	      releaseResources(mStorageInfoList);
	      mStorageInfoList = null;
	    }
	    
	    if(mStorageObserver != null){
	    	this.getContext().unregisterReceiver(mStorageObserver);
	    	mStorageObserver = null;
	    }
	    
	    if(mStorageInfoFinder != null){
	    	mStorageInfoFinder = null;
	    }
	}
	
	@Override
	public void onCanceled(List<StorageInfo> data) {
		 // Attempt to cancel the current asynchronous load.
		super.onCanceled(data);
		
		// The load has been canceled, so we should release the resources
	    // associated with 'mStorageInfoList'.
	    releaseResources(mStorageInfoList);
	}

	/**
	   * Helper method to take care of releasing resources associated with an
	   * actively loaded data set.
	   */
	private void releaseResources(List<StorageInfo> storageInfoList) {
		// For a simple List, there is nothing to do. For something like a Cursor,
	    // we would close it in this method. All resources associated with the
	    // Loader should be released here.
	}
	
	@Override
	public void onContentChanged() {
		if(isStarted()){
			new Thread(new Runnable() {			
				@Override
				public void run() {
					try {
						Thread.sleep(LOAD_TIME_DELAY);
						mHandler.sendEmptyMessage(0);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}else{
			mChangeFlag = true;
		}
	}

	@Override
	public boolean takeContentChanged() {
		if(mChangeFlag){
			mChangeFlag = !mChangeFlag;
			return true;
		}
		return false;
	}
	
}
