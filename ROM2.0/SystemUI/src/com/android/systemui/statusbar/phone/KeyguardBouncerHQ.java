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
 * limitations under the License
 */

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.Log;
import android.view.Choreographer;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardViewBase;
import com.android.keyguard.R;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.keyguard.KeyguardViewMediator;

import com.mediatek.keyguard.PowerOffAlarm.PowerOffAlarmManager ;

import static com.android.keyguard.KeyguardHostView.OnDismissAction;
import static com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.systemui.statusbar.phone.FineOSUtilities;

  
import android.util.Log;
import android.widget.FrameLayout;
import com.fineos.keyguard.KeyguardHostViewHQ;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUpdateMonitor;

import android.os.SystemProperties;
import android.graphics.Color;
/**
 * A class which manages the bouncer on the lockscreen.
 */
public class KeyguardBouncerHQ extends KeyguardBouncer {

    private final String TAG = "KeyguardBouncerHQ" ;
    private final boolean DEBUG = true ;

    private static final int  KEYGUARD_INDEX =  4 ;
    private Context mContext;
    private ViewMediatorCallback mCallback;
    private LockPatternUtils mLockPatternUtils;
    private ViewGroup mContainer;
    private StatusBarWindowManager mWindowManager;
    private KeyguardViewBase mKeyguardView;
    private ViewGroup mRoot;
    private boolean mShowingSoon;
    private Choreographer mChoreographer = Choreographer.getInstance();

    /// M: use securityModel to check if it needs to show full screen mode
    private static KeyguardSecurityModel mSecurityModel;
    private static boolean mIsSecure = false ;
    private ViewGroup mNotificationPanel ;
    private View mOverLay =null;
    

    public KeyguardBouncerHQ(Context context, ViewMediatorCallback callback,
            LockPatternUtils lockPatternUtils, StatusBarWindowManager windowManager,
            ViewGroup container) {
        super(context,callback,lockPatternUtils,windowManager,container);
        mContext = context;
        mCallback = callback;
        mLockPatternUtils = lockPatternUtils;
        mContainer = container;
        mWindowManager = windowManager;
        mNotificationPanel = (ViewGroup) mContainer.findViewById(R.id.notification_panel);
        
       
        mSecurityModel = new KeyguardSecurityModel(mContext);
        
    }
	 public static boolean isKeyguardSecure(){
    	
    	return mSecurityModel!=null?(mSecurityModel.getSecurityMode()!= SecurityMode.None):false  ;
    }

    public void show(boolean resetSecuritySelection) {
        show(resetSecuritySelection, false) ;
    }

    /**
     * show bouncer.
     * @param resetSecuritySelection need to reset.
     * @param authenticated authenticated or not.
     */
    public void show(boolean resetSecuritySelection, boolean authenticated) {
        if (DEBUG) {
            Log.d(TAG, "show(resetSecuritySelection = " + resetSecuritySelection
                + "authenticated = " + authenticated + ") is called.") ;
        }

        if (PowerOffAlarmManager.isAlarmBoot()) {
            Log.d(TAG, "show() - this is alarm boot, just re-inflate.") ;
            /// M: fix ALPS01865324, we should call KeyguardPasswordView.onPause() to hide IME
            ///    before the KeyguardPasswordView is gone.
            if (mKeyguardView != null && mRoot != null) {
                Log.d(TAG, "show() - before re-inflate, we should pause current view.") ;
                mKeyguardView.onPause();
            }
            // force to remove views.
            inflateView() ;
        } else {
		
        	
        	 
		    if(resetSecuritySelection){
			
			    inflateView() ;
				
			}else{
			
			    ensureView();
			
			}
            
        }

        if (resetSecuritySelection) {
            // showPrimarySecurityScreen() updates the current security method. This is needed in
            // case we are already showing and the current security method changed.
            mKeyguardView.showPrimarySecurityScreen();
        }
        if (mRoot.getVisibility() == View.VISIBLE || mShowingSoon) {
            return;
        }

        // Try to dismiss the Keyguard. If no security pattern is set, this will dismiss the whole
        // Keyguard. If we need to authenticate, show the bouncer.
        if ((mKeyguardView!=null)&&(!mKeyguardView.dismiss(authenticated))) {
            if (DEBUG) {
                Log.d(TAG, "show() - try to dismiss \"Bouncer\" directly.") ;
            }

            mShowingSoon = true;

            // Split up the work over multiple frames.
            mChoreographer.postCallbackDelayed(Choreographer.CALLBACK_ANIMATION, mShowRunnable,
                    null, 16);
        }
    }

    private final Runnable mShowRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "mShowRunnable.run() is called.") ;
            mRoot.setVisibility(View.VISIBLE);
            if(mKeyguardView!=null){
            	 mKeyguardView.onResume();
               mKeyguardView.startAppearAnimation();
            }
           
            mShowingSoon = false;
            mKeyguardView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        }
    };

    private void cancelShowRunnable() {
        mChoreographer.removeCallbacks(Choreographer.CALLBACK_ANIMATION, mShowRunnable, null);
        mShowingSoon = false;
    }

    public void showWithDismissAction(OnDismissAction r) {
        if (DEBUG) Log.d(TAG, "showWithDismissAction() is called.") ;
        ensureView();
        if(mKeyguardView!=null){
        	 mKeyguardView.setOnDismissAction(r);
         }
       
        show(false /* resetSecuritySelection */);
    }

    public void hide(boolean destroyView) {
        if (DEBUG) {
            Log.d(TAG, "hide() is called, destroyView = " + destroyView) ;
        }

        cancelShowRunnable();
        if (mKeyguardView != null) {
            mKeyguardView.setOnDismissAction(null);
            mKeyguardView.cleanUp();
            FineOSUtilities fineUtilities = FineOSUtilities.getInstance(mContext);
            if(fineUtilities.useFineOSlideKeyguard()&&destroyView){  //xuqian add 
            	mKeyguardView.cleanUpHQ();   
				
            }
            
            if(mOverLay!=null){
            	//mOverLay.setBackgroundDrawable(null);
            	mNotificationPanel.removeView(mOverLay);
            }
            
        }

        if (destroyView) {
            if (DEBUG) Log.d(TAG, "call removeView()") ;
            removeView();
        } else if (mRoot != null) {
            if (DEBUG) Log.d(TAG, "just set keyguard Invisible.") ;
            mRoot.setVisibility(View.INVISIBLE);
        }

        /// M: [ALPS01748966] true place that user has left keyguard.
        // If the alternate unlock was suppressed, it can now be safely
        // enabled because the user has left keyguard.
        Log.d(TAG, "hide() - user has left keyguard, setAlternateUnlockEnabled(true)") ;
        KeyguardUpdateMonitor.getInstance(mContext).setAlternateUnlockEnabled(true);
    }


    /**
     * See {@link StatusBarKeyguardViewManager#startPreHideAnimation}.
     */
    public void startPreHideAnimation(Runnable runnable) {
        if (mKeyguardView != null) {
            mKeyguardView.startDisappearAnimation(runnable);
        } else if (runnable != null) {
            runnable.run();
        }
    }


    /**
     * Reset the state of the view.
     */
    public void reset() {
        cancelShowRunnable();
        inflateView();
    }

    public void onScreenTurnedOff() {
        if (mKeyguardView != null && mRoot != null && mRoot.getVisibility() == View.VISIBLE) {
            mKeyguardView.onScreenTurnedOff();
            mKeyguardView.onPause();
        }
    }

    public long getUserActivityTimeout() {
        if (mKeyguardView != null) {
            long timeout = mKeyguardView.getUserActivityTimeout();
            if (timeout >= 0) {
                return timeout;
            }
        }
        return KeyguardViewMediator.AWAKE_INTERVAL_DEFAULT_MS;
    }

    public boolean isShowing() {
        return mShowingSoon || (mRoot != null && mRoot.getVisibility() == View.VISIBLE);
    }

    public void prepare() {
        boolean wasInitialized = mRoot != null;
        ensureView();
        if (wasInitialized) {
            mKeyguardView.showPrimarySecurityScreen();
        }
    }

    private void ensureView() {
    	
    	Log.v(TAG,"ensureView,mRoot= "+mRoot);
        if (mRoot == null) {
            inflateView();
        }
    }

    private void inflateView() {
    	
        if (DEBUG) Log.d(TAG, "inflateView() is called, we force to re-inflate the \"Bouncer\" view.") ;
        Log.d(TAG, "inflateView(),mKeyguardView: "+mKeyguardView +"isSecure() :"+isSecure()+",isKeyguardSecure(): "+isKeyguardSecure()) ;
        mIsSecure =  mSecurityModel.getSecurityMode()!= SecurityMode.None ;
        
        Log.v(TAG," mOverLay :"+ mOverLay+",KeyguardBouncerHQ.isKeyguardSecure():"+KeyguardBouncerHQ.isKeyguardSecure());
       if(mIsSecure){

        
       	if(mOverLay!=null){
       		mOverLay.setVisibility(View.GONE);
       		//mOverLay.setBackgroundDrawable(null);
       	    }
        }else {
        	if(mOverLay!=null){ 
           		mOverLay.setVisibility(View.VISIBLE);
			//mOverLay.setBackgroundDrawable(null);
           		//mOverLay.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.ic_camera_loading));
           	}
         }
        FineOSUtilities fineUtilities = FineOSUtilities.getInstance(mContext);

        if(fineUtilities.useFineOSlideKeyguardNew()){
        	 
                Boolean ret = inflateViewHQ(); 
                Log.v(TAG,"inflateHQ success ? : "+ret);	
        	
        	 
        }else{
        	 
              inflateOtherView();
        }
        
    }
    
    private boolean inflateViewHQ() {
        if (DEBUG) Log.d(TAG, "inflateViewHQ() is called, we force to re-inflate the \"Bouncer\" view.") ;

        removeView();
        mRoot = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.keyguard_bouncer, null);
       
        boolean useHQLock = false;
		 KeyguardHostViewHQ hostView = null;

		if (hostView == null) {
			hostView = new KeyguardHostViewHQ(mContext);
			useHQLock = hostView.createSecurityView();
		}
        Log.v(TAG,"hostView= "+hostView+", useHQLock="+useHQLock);
        mNotificationPanel.setVisibility(View.VISIBLE);
		if (useHQLock) {
			mKeyguardView = hostView;
			mKeyguardView.setLockPatternUtils(mLockPatternUtils);
	       mKeyguardView.setViewMediatorCallback(mCallback);
	       mKeyguardView.setNotificationPanelView(mNotificationPanel) ;
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.FILL_PARENT,
					FrameLayout.LayoutParams.FILL_PARENT);
			
			for(int i=0;i<mNotificationPanel.getChildCount();i++){
				Log.v(TAG,"mNotificationPanel1: "+mNotificationPanel.getChildAt(i)+"i = "+i);
			}
			
	    View carrier = mNotificationPanel.findViewById(R.id.carrier_label);
		View status = mNotificationPanel.findViewById(R.id.keyguard_status_view);
		View emergency = mNotificationPanel.findViewById(R.id.emergency_calls_only);
		View bottom_area = mNotificationPanel.findViewById(R.id.keyguard_bottom_area);
		carrier.setVisibility(View.GONE);
		status.setVisibility(View.GONE);
		emergency.setVisibility(View.GONE);
		bottom_area.setVisibility(View.GONE);
			
			
			
			
			//View lastView = mNotificationPanel.findViewById(R.id.qs_navbar_scrim);
			//View lastlastView = mNotificationPanel.findViewById(R.id.header);
			//View lastfourView = mNotificationPanel.findViewById(R.id.notification_container_parent);
			
			View lastView = mNotificationPanel.findViewById(R.id.qs_navbar_scrim);
			View lastlastView = mNotificationPanel.findViewById(R.id.header);
			View lastthreeView = mNotificationPanel.findViewById(R.id.keyguard_bottom_area);
			View lastfourView = mNotificationPanel.findViewById(R.id.notification_container_parent);
			
			
			
			
			if(mNotificationPanel.getChildCount()<9){                                           // -3 NotificationsQuickSettingsContainer
				
				mNotificationPanel.addView(mKeyguardView,mNotificationPanel.getChildCount(),lp);
				mNotificationPanel.bringChildToFront(lastfourView);
				mNotificationPanel.bringChildToFront(lastthreeView);
				mNotificationPanel.bringChildToFront(lastlastView);
				mNotificationPanel.bringChildToFront(lastView);
			}else{        //>= 8 already contain keyguarHostViewHQ ,just replace
				
				mNotificationPanel.removeViewAt(KEYGUARD_INDEX);         //replce with new keyguard
				mNotificationPanel.addView(mKeyguardView,KEYGUARD_INDEX,lp);
				
			}
			for(int i=0;i<mNotificationPanel.getChildCount();i++){
				Log.v(TAG,"mNotificationPanel2: "+mNotificationPanel.getChildAt(i)+"i = "+i);
			}
			mContainer.addView(mRoot, mContainer.getChildCount());
			mRoot.setVisibility(View.INVISIBLE);
		    mRoot.setSystemUiVisibility(View.STATUS_BAR_DISABLE_HOME);
			return true;
		} else {
			Log.e(TAG, "inflateViewHQ fail");
			hostView.cleanUp();
			return false;
		}
        
    }
   
	 private void inflateOtherView() {
   	
       if (DEBUG) Log.d(TAG, "inflateViewOther() is called, we force to re-inflate the \"Bouncer\" view.") ;

       removeView();
//       if(mNotificationPanel!=null){
//    	   mNotificationPanel.removeAllViews();
//    	   //mNotificationPanel = null ;
//        }
       //mNotificationPanel = (ViewGroup) mContainer.findViewById(R.id.notification_panel);
       Log.v(TAG,"mKeyguardView :"+mKeyguardView);
       //mNotificationPanel.setVisibility(View.GONE);
       if(mKeyguardView!=null&& mKeyguardView instanceof KeyguardHostViewHQ){
    	   mNotificationPanel.removeView(mKeyguardView);
        }
        
       Log.d(TAG, "inflateViewOther, mNotificationPanel: "+mNotificationPanel) ;
       mRoot = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.keyguard_bouncer, null);
       mKeyguardView = (KeyguardViewBase) mRoot.findViewById(R.id.keyguard_host_view);
       mKeyguardView.setLockPatternUtils(mLockPatternUtils);
       mKeyguardView.setViewMediatorCallback(mCallback);
       mKeyguardView.setNotificationPanelView(mNotificationPanel) ;
       mContainer.addView(mRoot, mContainer.getChildCount());
       mRoot.setVisibility(View.INVISIBLE);
       mRoot.setSystemUiVisibility(View.STATUS_BAR_DISABLE_HOME);
       
	   /*View carrier = mNotificationPanel.findViewById(R.id.carrier_label);
		View status = mNotificationPanel.findViewById(R.id.keyguard_status_view);
		View emergency = mNotificationPanel.findViewById(R.id.emergency_calls_only);
		View bottom_area = mNotificationPanel.findViewById(R.id.keyguard_bottom_area);
		carrier.setVisibility(View.VISIBLE);
		status.setVisibility(View.VISIBLE);
		emergency.setVisibility(View.VISIBLE);
		bottom_area.setVisibility(View.VISIBLE);*/
	   
       for(int i=0;i<mNotificationPanel.getChildCount();i++){
			Log.v(TAG,"inflateOtherView(),mNotificationPanel1: "+mNotificationPanel.getChildAt(i)+"i = "+i);
		}
       
       
   }
	

    private void removeView() {
        if (mRoot != null && mRoot.getParent() == mContainer) {

            Log.d(TAG, "removeView() - really remove all views.") ;

            mContainer.removeView(mRoot);
            mRoot = null;
        }
    }

    public boolean onBackPressed() {
        return mKeyguardView != null && mKeyguardView.handleBackKey();
    }

    /**
     * @return True if and only if the security method should be shown before showing the
     * notifications on Keyguard, like SIM PIN/PUK.
     */
    public boolean needsFullscreenBouncer() {
        SecurityMode mode = mSecurityModel.getSecurityMode();
        return mode == SecurityMode.SimPinPukMe1
                || mode == SecurityMode.SimPinPukMe2
                || mode == SecurityMode.SimPinPukMe3
                || mode == SecurityMode.SimPinPukMe4
                || mode == SecurityMode.AntiTheft
                || mode == SecurityMode.AlarmBoot;
    }

    /**
     * Like {@link #needsFullscreenBouncer}, but uses the currently visible security method, which
     * makes this method much faster.
     */
    public boolean isFullscreenBouncer() {
        if (mKeyguardView != null) {
            SecurityMode mode = mKeyguardView.getCurrentSecurityMode();
            return mode == SecurityMode.SimPinPukMe1
                || mode == SecurityMode.SimPinPukMe2
                || mode == SecurityMode.SimPinPukMe3
                || mode == SecurityMode.SimPinPukMe4
                || mode == SecurityMode.AntiTheft
                || mode == SecurityMode.AlarmBoot;
        }

        return false ;
    }

    /**
     * WARNING: This method might cause Binder calls.
     */
    public boolean isSecure() {
        return mKeyguardView == null || mKeyguardView.getSecurityMode() != SecurityMode.None;
    }

    public boolean onMenuPressed() {
        ensureView();
        if (mKeyguardView.handleMenuKey()) {

            // We need to show it in case it is secure. If not, it will get dismissed in any case.
            mRoot.setVisibility(View.VISIBLE);
            mKeyguardView.requestFocus();
            mKeyguardView.onResume();
            return true;
        } else {
            return false;
        }
    }

    public boolean interceptMediaKey(KeyEvent event) {
        ensureView();
        return mKeyguardView.interceptMediaKey(event);
    }

    /**
     * @return True if mRoot view container is inflated.
     */
    public boolean isContainerInflated() {
        Log.d(TAG, "isContainerInflated() - ans is " + (mRoot != null)) ;
        return mRoot != null ;
    }
}
