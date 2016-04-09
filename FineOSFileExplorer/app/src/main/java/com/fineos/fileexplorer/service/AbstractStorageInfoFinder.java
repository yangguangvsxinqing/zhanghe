package com.fineos.fileexplorer.service;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.entity.StorageInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class AbstractStorageInfoFinder {

	private static final String TAG = "StorageInfoFinder";
	private static final String SECONDARY_STORAGE = "SECONDARY_STORAGE";
	private static final String INTERNAL_STORAGE = "INTERNAL_STORAGE";
	private static final int PHONE_STORAGE = 0;
	private static final int EXTERNAL_SD_CARD = 1;
	private static final int USB_STORAGE = 2;
	public static final int FIRST_PLACE = 0;

	private static Map<String, String> mEnvMap = System.getenv();
	private ArrayList<String> mStoragePathList;
	protected String mDefaultPath = Environment.getExternalStorageDirectory()
			.getAbsolutePath();
	protected Context mContext;

	public AbstractStorageInfoFinder(Context context) {
		mContext = context.getApplicationContext();
	}

	abstract public ArrayList<String> getStorageInfoList();

	public ArrayList<StorageInfo> buildStorageInfoList() {
		mStoragePathList = getStorageInfoList();
		return buildStorageInfo();
	}

    protected ArrayList<StorageInfo> buildStorageInfo() {
        ArrayList<StorageInfo> storageInfos= new ArrayList<StorageInfo>();
        Iterator iterator = mStoragePathList.iterator();
        while (iterator.hasNext()) {
            String path = (String) iterator.next();
			Log.d(TAG, "buildStorageInfo: storage path : " + path);
			StorageInfo storageInfo = createStorageInfo(path);
            if (storageInfo.isInternal()) {
				storageInfos.add(FIRST_PLACE, storageInfo);
            }else {
				storageInfos.add(storageInfo);
            }
        }
        return storageInfos;
    }

    protected int getStorageType(String path) {
        if (isUSBStorage(path)) {
            return USB_STORAGE;
        }
        if (isInternalStorage(path)) {
            return PHONE_STORAGE;
        }
        if (isSecondaryStorage(path)) {
            return EXTERNAL_SD_CARD;
        }
        return EXTERNAL_SD_CARD;
    }

    private StorageInfo createStorageInfo(String path) {
		int storageType = getStorageType(path);
        StorageInfo storageInfo = new StorageInfo(path);
        storageInfo.setTotalSize(getStorageTotalSize(path));
        storageInfo.setAvailableSize(getStorageFreeSize(path));
        storageInfo.setReadonly(false);
		storageInfo.setInternal(isInternalByType(storageType));
		storageInfo.setStorageName(getStorageNameByType(storageType));
        return storageInfo;
    }

	private String getStorageNameByType(int storageType) {
		switch (storageType) {
			case USB_STORAGE:
				return mContext.getResources().getString(R.string.usb_storage_name);
			case PHONE_STORAGE:
				return mContext.getResources().getString(R.string.inner_storage_name);
			case EXTERNAL_SD_CARD:
				return mContext.getResources().getString(R.string.sd_card_name);
		}
		return "";
	}

	protected boolean isInternalByType(int storageType) {
		return storageType == PHONE_STORAGE;
	}

	public Long getStorageTotalSize(String path) {
		try {
			StatFs stat = new StatFs(path);
			long singleBlockSize = stat.getBlockSizeLong();
			long blockCount = stat.getBlockCountLong();
			return blockCount * singleBlockSize;
		} catch (Exception e) {
			e.printStackTrace();
			return 0L;
		}
	}

	public Long getStorageFreeSize(String path) {
		StatFs stat;
		try{
			stat = new StatFs(path);
			long singleBlockSize = stat.getBlockSizeLong();
			long freeBlockCount = stat.getAvailableBlocksLong();
			return freeBlockCount * singleBlockSize;
		}catch(Exception e){
			e.printStackTrace();
			return 0L;
		}
	}


	protected boolean isInternalStorage(String path) {
        if (path.equals(mDefaultPath) && Environment.isExternalStorageRemovable()) {
            return false;
        }
        if (!path.equals(mDefaultPath) && !Environment.isExternalStorageRemovable()) {
            return false;
        }
        return true;
    }


	protected boolean isSecondaryStorage(String path){
		String secondaryPathA = System.getenv(SECONDARY_STORAGE);
		String secondaryPathB = mEnvMap.get(SECONDARY_STORAGE);
		if(secondaryPathA != null && secondaryPathA.contains(path)){
			return true;
		}
		if(secondaryPathB != null && secondaryPathB.contains(path)){
			return true;
		}
		return false;
	}
	

	protected boolean isUSBStorage(String path) {
		if(path.toLowerCase(Locale.ENGLISH).contains("usb")){
			return true;
		}
		Set<String> set = mEnvMap.keySet();
		Iterator<String> keys = set.iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			String value = mEnvMap.get(key);
			if (value.contains(path) && key.toLowerCase(Locale.ENGLISH).contains("usb")) {
				return true;
			}
		}
		return false;
	}
	
	

}
