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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings.System;
import android.provider.Settings;
import android.app.StatusBarManager;

import com.android.systemui.R;
import com.android.systemui.qs.FineOSSetting;
import com.android.systemui.qs.QSTile;
///FineOS Notificaiton qs start
import android.util.Log;
import android.content.ComponentName;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
///FineOS Notificaiton qs end
/** Quick settings tile: ScreenshotTile mode **/
public class ScreenshotTile extends QSTile<QSTile.BooleanState> {
    private final FineOSSetting mSetting;

    private boolean mListening;
    private boolean processFlag = true;
    private final KeyguardMonitor mKeyguard;
    //add by liweixin for observer keyguard
    private final Callback mCallback = new Callback();

    public ScreenshotTile(Host host) {
        super(host);
        Log.w("ganggang", "ScreenshotTile: ");
        mKeyguard = host.getKeyguardMonitor();
        mSetting = new FineOSSetting(mContext, mHandler, "super_screen_shot_is_show") {
            @Override
            protected void handleValueChanged(int value) {
                handleRefreshState(value);
            }
        };
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void handleClick() {
    //add by huangjie
     boolean enableSuperSreenshot = Settings.System.getInt(mContext.getContentResolver(),"super_screen_shot_is_show",0)!=1;
      StatusBarManager statusBar = (StatusBarManager) mContext.getSystemService(Context.STATUS_BAR_SERVICE);
      statusBar.collapsePanels();
	 Intent intent = new Intent();
	  if(enableSuperSreenshot){         
               intent.setAction("broad.homescreenshot.show");                
	  	}else{
               intent.setAction("broad.homescreenshot.dismiss"); 
	  		}
		if(processFlag){
		mContext.sendBroadcast(intent);
		processFlag = false;
			}
		new TimeThread().start();
     /*   setEnabled(!mState.value);*/
        refreshState();
    }
///FineOS Notificaiton qs start
    @Override
    protected void handleLongClick() {
           /*     Intent intent = new Intent();
                intent.setComponent(new ComponentName(
                                "com.android.settings",
                                "com.android.settings.Settings$GestureSettingsActivity"));
        mHost.startSettingsActivity(intent);*/
//        Intent intent = new Intent("com.huaqin.gesture.settings");
     //   mHost.startSettingsActivity(intent);
    }
///FineOS Notificaiton qs end
    private void setEnabled(boolean enabled) {
              //  int bgState = enabled ? 1 : 0;		
        Log.w("ganggang", "ScreenshotTile  :setEnabled: front ");    
 	       Settings.System.putInt(mContext.getContentResolver(),Settings.System.GESTURE_SWITCH,enabled ? 1:0);
	       Intent mIntent= new Intent("com.huaqin.gesture.protectservice.change");
                mContext.sendBroadcast(mIntent);
        Log.w("ganggang", "ScreenshotTile  :setEnabled: end :enabled"+enabled);    				
 }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        Log.w("ganggang", "ScreenshotTile  :handleUpdateState: ");    
        final int value = arg instanceof Integer ? (Integer)arg : mSetting.getValue();
        final boolean powersaving = value != 0;
        state.visible = !mKeyguard.isShowing();
        state.value = powersaving;
        //state.visible = true;
        state.label = mContext.getString(R.string.quick_settings_Screenshot_label); //FineOS Notificaiton qs  quick_settings_airplane_mode_label
        if (powersaving) {
            state.icon =  ResourceIcon.get(R.drawable.fineos_ic_qs_super_screen_shot_on);
            state.contentDescription =  mContext.getString(
                    R.string.quick_settings_Screenshot_on);
        } else {
            state.icon = ResourceIcon.get(R.drawable.fineos_ic_qs_super_screen_shot_off);
            state.contentDescription =  mContext.getString(
                    R.string.quick_settings_Screenshot_off);
        }
    }
    //add by liweixin for observer keyguard begin
    public void setListening(boolean listening) {
	//nothing
        if (listening) {
            mKeyguard.addCallback(mCallback);
        } else {
            mKeyguard.removeCallback(mCallback);
        }
    }

    private final class Callback implements KeyguardMonitor.Callback {
        @Override
        public void onKeyguardChanged() {
            refreshState();
        }
    };
    //add by liweixin for observer keyguard end

	 private class TimeThread extends Thread {
           public void run() {
                  try {
                           sleep(1000);
                          processFlag = true;
                  } catch (Exception e) {
                          e.printStackTrace();
                  }
           }
        }
}
