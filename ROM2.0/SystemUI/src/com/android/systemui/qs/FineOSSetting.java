/*
 * Copyright (C) 2015 The Android Open Source Project
FineOS Notificaiton qs
 */

package com.android.systemui.qs;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

import com.android.systemui.statusbar.policy.Listenable;

/** Helper for managing a global setting. **/
public abstract class FineOSSetting extends ContentObserver implements Listenable {
    private final Context mContext;
    private final String mSettingName;

    protected abstract void handleValueChanged(int value);

    public FineOSSetting(Context context, Handler handler, String settingName) {
        super(handler);
        mContext = context;
        mSettingName = settingName;
    }

    public int getValue() {
        return Settings.System.getInt(mContext.getContentResolver(), mSettingName, 0);
    }

    public void setValue(int value) {
        Settings.System.putInt(mContext.getContentResolver(), mSettingName, value);
    }

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            mContext.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(mSettingName), false, this);
        } else {
            mContext.getContentResolver().unregisterContentObserver(this);
        }
    }

    @Override
    public void onChange(boolean selfChange) {
        handleValueChanged(getValue());
    }
}

