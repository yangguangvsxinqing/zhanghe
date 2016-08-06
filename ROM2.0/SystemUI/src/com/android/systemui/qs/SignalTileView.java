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

package com.android.systemui.qs;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.systemui.R;
import com.android.systemui.qs.QSTile.SignalState;
import com.mediatek.xlog.Xlog;

//fineos add by zhanghe begin
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.systemui.qs.QSTile.State;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
//fineos add by zhanghe end 

/** View that represents a custom quick settings tile for displaying signal info (wifi/cell). **/
public final class SignalTileView extends QSTileView {
    private static final long DEFAULT_DURATION = new ValueAnimator().getDuration();
    private static final long SHORT_DURATION = DEFAULT_DURATION / 3;

    private FrameLayout mIconFrame;
    private ImageView mSignal;
    private ImageView mOverlay;
    private ImageView mIn;
    private ImageView mOut;
    
    private final H mHandler = new H();//add by zhanghe 
    private int mWideOverlayIconStartPadding;

    public SignalTileView(Context context) {
        super(context);

        mIn = addTrafficView(R.drawable.ic_qs_signal_in);
        mOut = addTrafficView(R.drawable.ic_qs_signal_out);

        mWideOverlayIconStartPadding = context.getResources().getDimensionPixelSize(
                R.dimen.wide_type_icon_start_padding_qs);
    }

    private ImageView addTrafficView(int icon) {
        final ImageView traffic = new ImageView(mContext);
        traffic.setImageResource(icon);
        traffic.setAlpha(0f);
        addView(traffic);
        return traffic;
    }

    @Override
    protected View createIcon() {
        mIconFrame = new FrameLayout(mContext);
        mSignal = new ImageView(mContext);
        mIconFrame.addView(mSignal);
        mOverlay = new ImageView(mContext);
        mIconFrame.addView(mOverlay, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        return mIconFrame;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int hs = MeasureSpec.makeMeasureSpec(mIconFrame.getMeasuredHeight(), MeasureSpec.EXACTLY);
        int ws = MeasureSpec.makeMeasureSpec(mIconFrame.getMeasuredHeight(), MeasureSpec.AT_MOST);
        mIn.measure(ws, hs);
        mOut.measure(ws, hs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        layoutIndicator(mIn);
        layoutIndicator(mOut);
    }

    private void layoutIndicator(View indicator) {
        boolean isRtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        int left, right;
        if (isRtl) {
            right = mIconFrame.getLeft();
            left = right - indicator.getMeasuredWidth();
        } else {
            left = mIconFrame.getRight();
            right = left + indicator.getMeasuredWidth();
        }
        indicator.layout(
                left,
                mIconFrame.getBottom() - indicator.getMeasuredHeight(),
                right,
                mIconFrame.getBottom());
    }

    @Override
    protected void handleStateChanged(QSTile.State state) {
        super.handleStateChanged(state);
        final SignalState s = (SignalState) state;
        setIcon(mSignal, s);
        if (s.overlayIconId > 0) {
            mOverlay.setVisibility(VISIBLE);
            mOverlay.setImageResource(s.overlayIconId);
        } else {
            mOverlay.setVisibility(GONE);
        }
        if (s.overlayIconId > 0 && s.isOverlayIconWide) {
            mSignal.setPaddingRelative(mWideOverlayIconStartPadding, 0, 0, 0);
        } else {
            mSignal.setPaddingRelative(0, 0, 0, 0);
        }
        Drawable drawable = mSignal.getDrawable();
        if (state.autoMirrorDrawable && drawable != null) {
            drawable.setAutoMirrored(true);
        }
        Xlog.d("SignalTileView", "handleStateChanged: state.connected: " + s.connected);
        final boolean shown = isShown();
        setVisibility(mIn, shown, s.activityIn);
        setVisibility(mOut, shown, s.activityOut);
    }

    private void setVisibility(View view, boolean shown, boolean visible) {
        Xlog.d("SignalTileView", "setVisibility: shown: " + shown + ", visible: " + visible);
        final float newAlpha = shown && visible ? 1 : 0;
        Xlog.d("SignalTileView", "view.getAlpha(): " + view.getAlpha() + ", newAlpha: " + newAlpha);
        if (view.getAlpha() == newAlpha) return;
        if (shown) {
            view.animate()
                .setDuration(visible ? SHORT_DURATION : DEFAULT_DURATION)
                .alpha(newAlpha)
                .start();
        } else {
            view.setAlpha(newAlpha);
        }
    }
    //fineos add by zhanghe 20150929 begin   
    private class H extends Handler {
        private static final int STATE_CHANGED_2 = 2;
        public H() {
            super(Looper.getMainLooper());
        }
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == STATE_CHANGED_2) {
            	Log.i("zhanghe", "   STATE_CHANGED_2   handleMessage ");
				handleStateChanged2((State) msg.obj);
			}

        }
    }
    

    protected void handleStateChanged2(QSTile.State state) {
         final SignalState s = (SignalState) state;
        if (mSignal instanceof ImageView) {
            setIcon2((ImageView) mSignal, state);
        }
    }
    

    @Override
    protected void onAttachedToWindow() {
    	// TODO Auto-generated method stub
    	super.onAttachedToWindow();
    	IntentFilter filter = new IntentFilter();
    	filter.addAction(ACTION_THEME_APPLIED);
    	mContext.registerReceiver(mReceiver, filter);
    }
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context content, Intent intent) {
			// TODO Auto-generated method stub
			
			if (intent.getAction().equals(ACTION_THEME_APPLIED)) {
			Log.i("zhanghe2", "onReceive intent ACTION_THEME_APPLIED");	
				
				if (mSignal instanceof ImageView) {
					Log.i("zhanghe2", "onReceive  in if ");
//					setIcon((ImageView) mIcon, state2);
					Message msg = new Message();
					msg.what = H.STATE_CHANGED_2;
					msg.obj = state2;
					mHandler.removeMessages(H.STATE_CHANGED_2);
					mHandler.sendMessageDelayed(msg, 200);
		        }
			}
			
		}
	};
	
    @Override
    protected void onDetachedFromWindow() {
    	// TODO Auto-generated method stub
    	super.onDetachedFromWindow();
    	mContext.unregisterReceiver(mReceiver);
    }
    
//fineos add by zhanghe 20150929 end
    
}
