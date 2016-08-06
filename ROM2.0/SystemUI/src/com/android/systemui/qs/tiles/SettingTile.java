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

import android.content.res.Configuration;
import android.content.res.Resources;

import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import android.content.Intent;
import android.util.Log;
import android.content.ComponentName;

/** Quick settings tile: SettingTile modify with [HQ_BOWAY_SYSTEMUI]**/
public class SettingTile extends QSTile<QSTile.BooleanState> {
    private Host mHost;

    public SettingTile(Host host) {
        super(host);
        mHost = host;
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
        Intent i = new Intent();
        i.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings"));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mHost.getContext().startActivity(i);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        return;
    }

    public void setListening(boolean listening) {
    }
 
}

