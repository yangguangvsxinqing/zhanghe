package com.fineos.fileexplorer.service;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Xiao Yue
 * Find storage list by using reflection method of StorageManager which
 * using system service to find storage list.
 * */
public class StorageInfoFinderSystemService extends AbstractStorageInfoFinder{
	
	private static final String TAG = "StorageFinderService";
	
	public StorageInfoFinderSystemService(Context context) {
		super(context);
	}
	
	@Override
	public ArrayList<String> getStorageInfoList() {
		/**
		 * Try to use StorageManager's method getVolumePaths() to get a list of storage path
		 * then see if everyone of them is a directory and can be read
		 * return a List of class StorageInfo.
		 * */
		StorageManager storageManager = (StorageManager)mContext.getSystemService(Context.STORAGE_SERVICE);
		ArrayList<String> pathList = new ArrayList<String>();
		
		try{
			String[] paths = (String[]) storageManager.getClass().getMethod("getVolumePaths", new Class[0]).invoke(storageManager, new Object[0]);
			for(String path : paths){
				Log.d(TAG, "path is  : " + path);
				File file = new File(path);
				if (file.isDirectory() && file.canRead() && getStorageTotalSize(path) > 0){
						pathList.add(path);
						Log.d(TAG, "accepted path: " + path);
				}else{
					Log.d(TAG, "canRead : " + file.canRead());
					Log.d(TAG, "isDirectory" + file.isDirectory());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return pathList;
	}

}
