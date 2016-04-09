package com.fineos.fileexplorer.entity;

import android.content.Context;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

public final class StorageInfo implements Parcelable {

	public String path = "";
	public String storageName = "";
	public boolean isInternal = false;
	public boolean isReadonly = true;
	public Long totalSize = 0l;
	public Long availableSize = 0l;

	public StorageInfo(String path) {
		this.path = path;
	}
	
	public StorageInfo(String path, String storageName, boolean isInternal,
			boolean isReadonly, long totalSize, long availableSize) {
		this.path = path;
		this.storageName = storageName;
		this.isInternal = isInternal;
		this.isReadonly = isReadonly;
		this.totalSize = totalSize;
		this.availableSize = availableSize;
	}
	
	public StorageInfo(Parcel in) {
		String[] data = new String[6];
		in.readStringArray(data);
		this.path = data[0];
		this.storageName = data[1];
		this.isInternal = Boolean.getBoolean(data[2]);
		this.isReadonly = Boolean.getBoolean(data[3]);
		this.totalSize = Long.valueOf(data[4]);
		this.availableSize = Long.valueOf(data[5]);
	}


	public static StorageInfo getEmptyStorageInfo() {
		// Return an empty instance of StorageInfo. It is not used by now.
		return new StorageInfo("empty path", "empty name", false, false,
				Long.valueOf(0), Long.valueOf(0));
	}
	
	public static StorageInfo getDefaultStorageInfo(Context context) {
		// Return an empty instance of StorageInfo. It is not used by now.
		return new StorageInfo(Environment.getExternalStorageDirectory().getPath(),
				"inner_storage_name", false, false,
				0, 0);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(new String[] { this.path, this.storageName,
				String.valueOf(this.isInternal),
				String.valueOf(this.isReadonly),
				String.valueOf(this.totalSize),
				String.valueOf(this.availableSize) });
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public StorageInfo createFromParcel(Parcel in) {
			return new StorageInfo(in);
		}

		public StorageInfo[] newArray(int size) {
			return new StorageInfo[size];
		}
	};

	public String getStorageName() {
		return storageName;
	}

	public void setStorageName(String storageName) {
		this.storageName = storageName;
	}

	public boolean isInternal() {
		return isInternal;
	}

	public void setInternal(boolean isInternal) {
		this.isInternal = isInternal;
	}

	public boolean isReadonly() {
		return isReadonly;
	}

	public void setReadonly(boolean isReadonly) {
		this.isReadonly = isReadonly;
	}

	public Long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(Long totalSize) {
		this.totalSize = totalSize;
	}

	public Long getAvailableSize() {
		return availableSize;
	}

	public void setAvailableSize(Long availableSize) {
		this.availableSize = availableSize;
	}
}