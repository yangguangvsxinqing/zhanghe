/*
 * Copyright (C) 2015 The Android Open Source Project
FineOS
 */

package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.database.ContentObserver;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.database.Cursor;
import com.android.systemui.statusbar.phone.MyInfo;
import java.util.ArrayList;

import com.android.systemui.R;
///FineOS Notification head
public final class MyInfoControllerFineOS {

    //private static final String TAG = "MyInfoControllerFineOS";

    private final Context mContext;
    private final ContentResolver mContentResolver;
    private final ArrayList<OnMyInfoChangedListener> mCallbacks=
		new ArrayList<OnMyInfoChangedListener>();
    private Bitmap mMyInfoBitmap;
    private static Cursor mCursor;
	
    public MyInfoControllerFineOS(Context context) {
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        mContentResolver.registerContentObserver(MyInfo.MyInfoColumns.CONTENT_URI, false,
                mMyInfoObserver);
    }
    private final ContentObserver mMyInfoObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
        		super.onChange(selfChange);
		reloadMyInfo();
	}
    };
    public void reloadMyInfo() {
	mMyInfoBitmap = getIcon();
	notifyChanged();
    }
    public void addListener(OnMyInfoChangedListener callback) {
        mCallbacks.add(callback);
    }
    private void notifyChanged() {
        for (OnMyInfoChangedListener listener : mCallbacks) {
            listener.onMyInfoChanged(mMyInfoBitmap);
        }		
    }
    public interface OnMyInfoChangedListener {
        public void onMyInfoChanged(Bitmap picture);
    }
	public  Bitmap getIcon() {
		Bitmap bmpout = null;
		mCursor = mContext.getContentResolver().query(
				MyInfo.MyInfoColumns.CONTENT_URI, MyInfo.MyInfoColumns.INFO_QUERY, null,
				null, null);
		if(mCursor==null){
			bmpout = BitmapFactory.decodeResource(mContext.getResources(),
					com.fineos.R.drawable.phone_logo);
		}else{
			if (mCursor.moveToFirst()) {
				byte[] in = mCursor.getBlob(mCursor
						.getColumnIndex(MyInfo.MyInfoColumns.ICON));
				bmpout = BitmapFactory.decodeByteArray(in, 0, in.length);
				mCursor.close();
			}
			if(bmpout == null){
				bmpout = BitmapFactory.decodeResource(mContext.getResources(),
						com.fineos.R.drawable.phone_logo);
			}
		}
		return bmpout;
	}
///FineOS Notification head end   	
}
