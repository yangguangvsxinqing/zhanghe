/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.systemui.settings;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
///FineOS Notificaiton qs start
import android.os.SystemProperties;
///FineOS Notificaiton qs ends
///fineos add by zhanghe for seekbar begin
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import fineos.app.ColorHelper;

///fineos add by zhanghe for seekbar end
public class ToggleSlider extends RelativeLayout {
    public interface Listener {
        public void onInit(ToggleSlider v);
        public void onChanged(ToggleSlider v, boolean tracking, boolean checked, int value);
    }

    private Listener mListener;
    private boolean mTracking;

    private CompoundButton mToggle;
    private SeekBar mSlider;
    private TextView mLabel;
///fineos add by zhanghe for seekbar begin
   private final H mHandler = new H();
   private static final String ACTION_THEME_APPLIED     = "com.android.server.ThemeManager.action.THEME_APPLIED";////fineos add by zhanghe
///fineos add by zhanghe for seekbar end
    private ToggleSlider mMirror;
    private BrightnessMirrorController mMirrorController;

    public ToggleSlider(Context context) {
        this(context, null);
    }

    public ToggleSlider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToggleSlider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
///FineOS Notificaiton qs start
		if(SystemProperties.get("ro.fineos.framework", "no").equals("yes"))
		{
		        View.inflate(context, R.layout.fineos_status_bar_toggle_slider, this);
		}else{
		        View.inflate(context, R.layout.status_bar_toggle_slider, this);
		}
///FineOS Notificaiton qs end
        final Resources res = context.getResources();
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.ToggleSlider, defStyle, 0);

        mToggle = (CompoundButton) findViewById(R.id.toggle);
      ///fineos add by zhanghe for checkBox begin
       Drawable mToggleBackground = mToggle.getBackground();
       ColorHelper.colorStateListDrawable(getResources(), mToggleBackground, R.color.systemui_qs_checkbox);
       ///fineos add by zhanghe for checkBox end
        
        mToggle.setOnCheckedChangeListener(mCheckListener);

        mSlider = (SeekBar) findViewById(R.id.slider);
        ///fineos add by zhanghe for seekbar begin
        int themeColor = ColorHelper.getColor(getContext().getResources(),R.color.systemui_qs_color);
        Log.i("zhanghe", "in ToggleSlider themeColor = "+Integer.toHexString(themeColor));
		ColorStateList tint = ColorStateList.valueOf(themeColor);
		mSlider.setProgressTintList(tint);
		///fineos add by zhanghe for seekbar end
        mSlider.setOnSeekBarChangeListener(mSeekListener);

        mLabel = (TextView) findViewById(R.id.label);
        mLabel.setText(a.getString(R.styleable.ToggleSlider_text));

        a.recycle();
    }
    
    public void hideToggle() {
        mToggle.setVisibility(View.GONE);
        mLabel.setVisibility(View.GONE);        
    }


    public void setMirror(ToggleSlider toggleSlider) {
        mMirror = toggleSlider;
        if (mMirror != null) {
            mMirror.setChecked(mToggle.isChecked());
            mMirror.setMax(mSlider.getMax());
            mMirror.setValue(mSlider.getProgress());
        }
    }

    public void setMirrorController(BrightnessMirrorController c) {
        mMirrorController = c;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mListener != null) {
            mListener.onInit(this);
        }
		///fineos add by zhanghe for seekbar begin
        IntentFilter filter = new IntentFilter();
    	filter.addAction(ACTION_THEME_APPLIED);
    	getContext().registerReceiver(mReceiver, filter);
		///fineos add by zhanghe for seekbar end
    }
   ///fineos add by zhanghe for seekbar begin 
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context content, Intent intent) {
			// TODO Auto-generated method stub
			
			if (intent.getAction().equals(ACTION_THEME_APPLIED)) {
			Log.i("zhanghe", "onReceive intent ACTION_THEME_APPLIED");	
				
	        
//	        int themeColor = ColorHelper.getColor(mContext.getResources(),
//					R.color.systemui_qs_color);
//	        Log.i("zhanghe", "in ToggleSlider themeColor = "+Integer.toHexString(themeColor));
////			SeekBar bar = (SeekBar) findViewById(R.id.seekbar);
//			ColorStateList tint = ColorStateList.valueOf(themeColor);
//			mSlider.setProgressTintList(tint);
			
			Message msg = new Message();
			msg.what = H.STATE_CHANGED;
			mHandler.removeMessages(H.STATE_CHANGED);
			mHandler.sendMessageDelayed(msg, 200);
			}
			
		}
	};
	
    @Override
    protected void onDetachedFromWindow() {
    	// TODO Auto-generated method stub
    	super.onDetachedFromWindow();
    	getContext().unregisterReceiver(mReceiver);
    }
	
    private class H extends Handler {
        private static final int STATE_CHANGED = 1;
        public H() {
            super(Looper.getMainLooper());
        }
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == STATE_CHANGED) {
            	Log.i("zhanghe", "handleMessage ");
                handleStateChanged();

            }

        }
		private void handleStateChanged() {
	        int themeColor = ColorHelper.getColor(getContext().getResources(),
					R.color.systemui_qs_color);
	        Log.i("zhanghe", "in ToggleSlider themeColor = "+Integer.toHexString(themeColor));
			ColorStateList tint = ColorStateList.valueOf(themeColor);
			mSlider.setProgressTintList(tint);
			
			   ///fineos add by zhanghe for checkBox begin
		       Drawable mToggleBackground = mToggle.getBackground();
		       ColorHelper.colorStateListDrawable(getResources(), mToggleBackground, R.color.systemui_qs_checkbox);
		       ///fineos add by zhanghe for checkBox end
		    
		}
    }
///fineos add by zhanghe for seekbar end
    public void setOnChangedListener(Listener l) {
        mListener = l;
    }

    public void setChecked(boolean checked) {
        mToggle.setChecked(checked);
    }

    public boolean isChecked() {
        return mToggle.isChecked();
    }

    public void setMax(int max) {
        mSlider.setMax(max);
        if (mMirror != null) {
            mMirror.setMax(max);
        }
    }

    public void setValue(int value) {
        mSlider.setProgress(value);
        if (mMirror != null) {
            mMirror.setValue(value);
        }
    }

    private final OnCheckedChangeListener mCheckListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton toggle, boolean checked) {
            mSlider.setEnabled(!checked);

            if (mListener != null) {
                mListener.onChanged(
                        ToggleSlider.this, mTracking, checked, mSlider.getProgress());
            }

            if (mMirror != null) {
                mMirror.mToggle.setChecked(checked);
            }
        }
    };

    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mListener != null) {
                mListener.onChanged(
                        ToggleSlider.this, mTracking, mToggle.isChecked(), progress);
            }

            if (mMirror != null) {
                mMirror.setValue(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mTracking = true;

            if (mListener != null) {
                mListener.onChanged(
                        ToggleSlider.this, mTracking, mToggle.isChecked(), mSlider.getProgress());
            }

            mToggle.setChecked(false);

            if (mMirror != null) {
                mMirror.mSlider.setPressed(true);
            }

            if (mMirrorController != null) {
              //FineOS Notificaiton qs  mMirrorController.showMirror();
                mMirrorController.setLocation((View) getParent());
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mTracking = false;

            if (mListener != null) {
                mListener.onChanged(
                        ToggleSlider.this, mTracking, mToggle.isChecked(), mSlider.getProgress());
            }

            if (mMirror != null) {
                mMirror.mSlider.setPressed(false);
            }

            if (mMirrorController != null) {
                mMirrorController.hideMirror();
            }
        }
    };
}

