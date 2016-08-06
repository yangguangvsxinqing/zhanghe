/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs.tiles;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings.System;
import android.provider.Settings;

import com.android.systemui.R;
import com.android.systemui.qs.FineOSSetting;
import com.android.systemui.qs.QSTile;
import android.util.Log;
/** Quick settings tile: ScreenRecordTitle mode **/


public class ScreenRecordTitle extends QSTile<QSTile.BooleanState> {
    private final FineOSSetting mSetting;

    public static final int NOTIFICATION_ID = 1;

    public ScreenRecordTitle(Host host) {
        super(host);
        Log.w("ScreenRecordTitle", "ScreenRecordTile: ");
        mSetting = new FineOSSetting(mContext, mHandler, "screen_record_is_show") {
            @Override
            protected void handleValueChanged(int value) {
                // we have refreshState in function handleClick(), so here we needn't to refreshState ageain.
                //handleRefreshState(value);
            }
        };
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void handleClick() {
        boolean enableScreenRecord = Settings.System.getInt(mContext.getContentResolver(), "screen_record_is_show", 0) != 1;
        StatusBarManager statusBar = (StatusBarManager) mContext.getSystemService(Context.STATUS_BAR_SERVICE);
        statusBar.collapsePanels();
        Intent intent = new Intent();
        if(enableScreenRecord){         
            intent.setAction("android.fineos.screenrecorder.action.START_RECORD");
            sendScreenRecordNotify(mContext, NOTIFICATION_ID);
        }else{
            intent.setAction("android.fineos.screenrecorder.action.STOP_RECORD"); 
            cancelNotification(mContext, NOTIFICATION_ID);
        }
        mContext.sendBroadcast(intent);
        setEnabled(!mState.value);
        refreshState();
    }

    @Override
    protected void handleLongClick() {
    }

    private void setEnabled(boolean enabled) {   
 	    Settings.System.putInt(mContext.getContentResolver(), "screen_record_is_show", enabled ? 1:0);  				
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        Log.w("ScreenRecordTitle", "ScreenRecord  :handleUpdateState: arg = " + arg);    
        final int value = arg instanceof Integer ? (Integer)arg : mSetting.getValue();
        final boolean powersaving = value != 0;
        state.value = powersaving;
        // png is visible or not.
        state.visible = true;
        state.label = mContext.getString(R.string.quick_settings_screenrecord_label);
        if (powersaving) {
            state.icon =  ResourceIcon.get(R.drawable.fineos_ic_qs_screen_record_on);
            state.contentDescription =  mContext.getString(
                    R.string.quick_settings_screenrecord_on);
        } else {
            state.icon = ResourceIcon.get(R.drawable.fineos_ic_qs_screen_record_off);
            state.contentDescription =  mContext.getString(
                    R.string.quick_settings_screenrecord_off);
        }
    }

    public void setListening(boolean listening) {
        mSetting.setListening(listening);
    }

    private void sendScreenRecordNotify(Context context, int notificationId){
        final NotificationManager mNotificationManager =
			                         (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification.Builder builder = new Notification.Builder(context);

		 builder.setSmallIcon(R.drawable.fineos_ic_qs_screen_record_on)
            .setContentTitle(context.getString(R.string.quick_settings_screen_recording))
            .setContentText(context.getString(R.string.click_screenrecord_to_stop));

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
		 mNotificationManager.notify(notificationId, notification);
	}

	private void cancelNotification(Context context, int notificationId) {
        Log.d("ScreenRecordTitle", "cancelNotification(), notificationId:" + notificationId);
        NotificationManager nm = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);

        nm.cancel(notificationId);
    }
}

