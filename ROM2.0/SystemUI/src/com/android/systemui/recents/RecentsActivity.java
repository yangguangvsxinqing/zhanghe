/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

package com.android.systemui.recents;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.SearchManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
//import android.widget.Toast;

import com.android.systemui.R;
import com.android.systemui.recents.misc.DebugTrigger;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.SpaceNode;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.DebugOverlayView;
import com.android.systemui.recents.views.RecentsView;
import com.android.systemui.recents.views.SystemBarScrimViews;
import com.android.systemui.recents.views.ViewAnimation;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.SystemUIApplication;
import com.mediatek.xlog.Xlog;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
///M:For Multi window 
import com.mediatek.multiwindow.MultiWindowProxy;
///@}

///M[HQ_REMOVE_ALLTASK_RECENTS] add remove all task in RECENTS @{
import android.widget.ImageView;
import android.os.AsyncTask;
///@}
//[FINEOS_REMOVE_ALLTASK_RECENTS] fineos add start 
import android.os.SystemProperties;
import android.widget.ImageView;
import android.view.View.OnClickListener; 
import android.util.Log;
import android.animation.LayoutTransition;
import android.animation.TimeInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import android.text.format.Formatter;
import android.widget.PopupWindow;
import android.view.LayoutInflater;
import fineos.widget.Toast;
import android.widget.TextView;
import android.app.ActivityManager;
import android.view.Gravity;
import android.widget.LinearLayout;

import android.view.MotionEvent;
import android.widget.Button;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.view.animation.TranslateAnimation;

// fineos add end
//fineos FINEOS_INFO_APP start
import android.content.ComponentName;
import android.content.pm.PackageManager;
//fineos FINEOS_INFO_APP end

//fineos RECENTS_CLEAN_ANIMATION start
import android.os.Handler;
import android.os.Message;
import android.graphics.drawable.AnimationDrawable;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
//fineos RECENTS_CLEAN_ANIMATION end

/**
 * The main Recents activity that is started from AlternateRecentsComponent.
 */
public class RecentsActivity extends Activity implements RecentsView.RecentsViewCallbacks,
        RecentsAppWidgetHost.RecentsAppWidgetHostCallbacks,
        DebugOverlayView.DebugOverlayViewCallbacks {
    /// M: For Debug
    static final String TAG = "recents.RecentsActivity";
    static final boolean DEBUG = true;

    RecentsConfiguration mConfig;
    long mLastTabKeyEventTime;

    // Top level views
    RecentsView mRecentsView;
    SystemBarScrimViews mScrimViews;
    ViewStub mEmptyViewStub;
    ViewStub mDebugOverlayStub;
    View mEmptyView;
    DebugOverlayView mDebugOverlay;
	// [FINEOS_REMOVE_ALLTASK_RECENTS]fineos add start
	private ImageView mclearAllRecent;
	private LinearLayout mMemInfoLayout;
	
	private Animation operatingAnim;
	private ImageView mDelTweenImg;
	private long mOldMemAvail;
    private long mNewmemAvail;
	private boolean mIsClickCleanALLBtn = false;
	private FrameLayout mRecentsRootLayout;
	private static final String PREFERENCES_RECENT_NAME = "FineosRecentsApp";
	public static final String FIRST_TIME_IN_RECENTS = "firstTimeInRecents";
	// fineos add end
	
    // Search AppWidget
    RecentsAppWidgetHost mAppWidgetHost;
    AppWidgetProviderInfo mSearchAppWidgetInfo;
    AppWidgetHostView mSearchAppWidgetHostView;

    // Runnables to finish the Recents activity
    FinishRecentsRunnable mFinishLaunchHomeRunnable;

    private PhoneStatusBar mStatusBar;

    /// M: add for multi window @{
    public static final boolean FLOAT_WINDOW_SUPPORT = MultiWindowProxy.isFeatureSupport();
    /// @}

    ///M[HQ_REMOVE_ALLTASK_RECENTS] add remove all task in RECENTS @{
    private ImageView mRemoveAllTask;
    ///@}
    
	//fineos RECENTS_CLEAN_ANIMATION start
	private static final int MSG_ID_START_ANIMATION_1 = 1003;
	private static final int MSG_ID_START_ANIMATION_2 = 1004;
	private static final int MSG_ID_STOP_ANIMATION_3 = 1005;
	private static final int MSG_ID_START_ANIMATION_1_2 = 1006;
	
	private static final int START_ANIMATION_1_TIME = 600;
	private static final int START_ANIMATION_2_TIME = 500;
	private static final int STOP_ANIMATION_3_TIME = 1000;
	private static final int START_ANIMATION_1_2_TIME = 300;

	private ImageView mCleanView;
	private ImageView mCleanBgView;
	private CircleBackground mCircleBackground;
	private final MyHandler mHandler = new MyHandler();

	private long mMemTotal;

	//fineos RECENTS_CLEAN_ANIMATION end

    /**
     * A common Runnable to finish Recents either by calling finish() (with a custom animation) or
     * launching Home with some ActivityOptions.  Generally we always launch home when we exit
     * Recents rather than just finishing the activity since we don't know what is behind Recents in
     * the task stack.  The only case where we finish() directly is when we are cancelling the full
     * screen transition from the app.
     */
    class FinishRecentsRunnable implements Runnable {
        Intent mLaunchIntent;
        ActivityOptions mLaunchOpts;

        /**
         * Creates a finish runnable that starts the specified intent, using the given
         * ActivityOptions.
         */
        public FinishRecentsRunnable(Intent launchIntent, ActivityOptions opts) {
            mLaunchIntent = launchIntent;
            mLaunchOpts = opts;
        }

        @Override
        public void run() {
            // Finish Recents
            if (mLaunchIntent != null) {
                if (DEBUG) {
                    Xlog.d(TAG, "FinishRecentsRunnable: Start Activity by LaunchIntent : "
                           + mLaunchIntent);
                }
                if (mLaunchOpts != null) {
                    startActivityAsUser(mLaunchIntent, mLaunchOpts.toBundle(), UserHandle.CURRENT);
                } else {
                    startActivityAsUser(mLaunchIntent, UserHandle.CURRENT);
                }
				/// M: add for multi window @{
				if(FLOAT_WINDOW_SUPPORT){						
				    finish();
				    overridePendingTransition(R.anim.recents_to_launcher_enter,
				        R.anim.recents_to_launcher_exit);		
				}
						/// @}
            } else {
                if (DEBUG) {
                    Xlog.d(TAG, "FinishRecentsRunnable: Finish myself");
                }
                finish();
                overridePendingTransition(R.anim.recents_to_launcher_enter,
                        R.anim.recents_to_launcher_exit);
            }
        }
    }

    /**
     * Broadcast receiver to handle messages from AlternateRecentsComponent.
     */
    final BroadcastReceiver mServiceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(AlternateRecentsComponent.ACTION_HIDE_RECENTS_ACTIVITY)) {
                if (intent.getBooleanExtra(AlternateRecentsComponent.EXTRA_TRIGGERED_FROM_ALT_TAB, false)) {
                    // If we are hiding from releasing Alt-Tab, dismiss Recents to the focused app
                    dismissRecentsToFocusedTaskOrHome(false);
                } else if (intent.getBooleanExtra(AlternateRecentsComponent.EXTRA_TRIGGERED_FROM_HOME_KEY, false)) {
                    // Otherwise, dismiss Recents to Home
                    dismissRecentsToHome(true);
                } else {
                    // Do nothing, another activity is being launched on top of Recents
                }
            } else if (action.equals(AlternateRecentsComponent.ACTION_TOGGLE_RECENTS_ACTIVITY)) {
                // If we are toggling Recents, then first unfilter any filtered stacks first
                dismissRecentsToFocusedTaskOrHome(true);
            } else if (action.equals(AlternateRecentsComponent.ACTION_START_ENTER_ANIMATION)) {
                // Trigger the enter animation
                onEnterAnimationTriggered();
                // Notify the fallback receiver that we have successfully got the broadcast
                // See AlternateRecentsComponent.onAnimationStarted()
                setResultCode(Activity.RESULT_OK);
            }
        }
    };

    /**
     * Broadcast receiver to handle messages from the system
     */
    final BroadcastReceiver mSystemBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                // When the screen turns off, dismiss Recents to Home
                dismissRecentsToHome(false);
            } else if (action.equals(SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED)) {
                // When the search activity changes, update the Search widget
                refreshSearchWidget();
            }
        }
    };

    /**
     * A custom debug trigger to listen for a debug key chord.
     */
    final DebugTrigger mDebugTrigger = new DebugTrigger(new Runnable() {
        @Override
        public void run() {
            onDebugModeTriggered();
        }
    });

    /** Updates the set of recent tasks */
    void updateRecentsTasks(Intent launchIntent) {
        // If AlternateRecentsComponent has preloaded a load plan, then use that to prevent
        // reconstructing the task stack
        RecentsTaskLoader loader = RecentsTaskLoader.getInstance();
        RecentsTaskLoadPlan plan = AlternateRecentsComponent.consumeInstanceLoadPlan();
        if (plan == null) {
            plan = loader.createLoadPlan(this);
        }

        // Start loading tasks according to the load plan
        if (plan.getTaskStack() == null) {
            loader.preloadTasks(plan, mConfig.launchedFromHome);
        }
        RecentsTaskLoadPlan.Options loadOpts = new RecentsTaskLoadPlan.Options();
        loadOpts.runningTaskId = mConfig.launchedToTaskId;
        loadOpts.numVisibleTasks = mConfig.launchedNumVisibleTasks;
        loadOpts.numVisibleTaskThumbnails = mConfig.launchedNumVisibleThumbnails;
        loader.loadTasks(this, plan, loadOpts);

        SpaceNode root = plan.getSpaceNode();
        ArrayList<TaskStack> stacks = root.getStacks();
        boolean hasTasks = root.hasTasks();
        if (hasTasks) {
            mRecentsView.setTaskStacks(stacks);
        }
        mConfig.launchedWithNoRecentTasks = !hasTasks;

        // Create the home intent runnable
        Intent homeIntent = new Intent(Intent.ACTION_MAIN, null);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        mFinishLaunchHomeRunnable = new FinishRecentsRunnable(homeIntent,
            ActivityOptions.makeCustomAnimation(this,
                mConfig.launchedFromSearchHome ? R.anim.recents_to_search_launcher_enter :
                        R.anim.recents_to_launcher_enter,
                    mConfig.launchedFromSearchHome ? R.anim.recents_to_search_launcher_exit :
                        R.anim.recents_to_launcher_exit));

        // Mark the task that is the launch target
        int taskStackCount = stacks.size();
        if (DEBUG) {
            Xlog.d(TAG, "updateRecentsTasks:stacks size = " + taskStackCount);
        }
        if (mConfig.launchedToTaskId != -1) {
            for (int i = 0; i < taskStackCount; i++) {
                TaskStack stack = stacks.get(i);
                ArrayList<Task> tasks = stack.getTasks();
                int taskCount = tasks.size();
                if (DEBUG) {
                    Xlog.d(TAG, "updateRecentsTasks:task size = " + taskCount);
                }

                for (int j = 0; j < taskCount; j++) {
                    Task t = tasks.get(j);
                    if (t.key.id == mConfig.launchedToTaskId) {
                        t.isLaunchTarget = true;
                        break;
                    }
                }
            }
        }

        // Update the top level view's visibilities
        if (mConfig.launchedWithNoRecentTasks) {
            if (mEmptyView == null) {
                mEmptyView = mEmptyViewStub.inflate();
            }
            mEmptyView.setVisibility(View.VISIBLE);
            mRecentsView.setSearchBarVisibility(View.GONE);
	    ///M[HQ_REMOVE_ALLTASK_RECENTS] add remove all task in RECENTS @{
            if(mRemoveAllTask != null)mRemoveAllTask.setVisibility(View.GONE);
            /// }@
        } else {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.GONE);
            }
            /*if (mRecentsView.hasSearchBar()) {
                mRecentsView.setSearchBarVisibility(View.VISIBLE);
            } else {
                addSearchBarAppWidgetView();
            }*/
            ///M [HQ_REMOVE_ALLTASK_RECENTS] add remove all task in RECENTS @{
}
	// fineos add start
		if(SystemProperties.get("ro.fineos.framework", "no").equals("yes")){
			//fineos RECENTS_CLEAN_ANIMATION start
   			mCleanView.setVisibility(View.GONE);
   			mCleanBgView.setVisibility(View.GONE);
   			mCircleBackground.setVisibility(View.GONE);
			//fineos RECENTS_CLEAN_ANIMATION end
			if(mRecentsView.getTaskCount() <= 0){
				mclearAllRecent.setVisibility(View.GONE);
				mMemInfoLayout.setVisibility(View.GONE);
			}else{
				//fineos RECENTS_CLEAN_ANIMATION start
	   			//mclearAllRecent.setVisibility(View.VISIBLE);
	   			mclearAllRecent.setVisibility(View.GONE);
				mMemInfoLayout.setVisibility(View.VISIBLE);
				mHandler.sendEmptyMessageDelayed(MSG_ID_START_ANIMATION_1, START_ANIMATION_1_TIME);
				//fineos RECENTS_CLEAN_ANIMATION end
			}
		}
        // Animate the SystemUI scrims into view
        mScrimViews.prepareEnterRecentsAnimation();
    }

    /** Attempts to allocate and bind the search bar app widget */
    void bindSearchBarAppWidget() {
        if (Constants.DebugFlags.App.EnableSearchLayout) {
            SystemServicesProxy ssp = RecentsTaskLoader.getInstance().getSystemServicesProxy();

            // Reset the host view and widget info
            mSearchAppWidgetHostView = null;
            mSearchAppWidgetInfo = null;

            // Try and load the app widget id from the settings
            int appWidgetId = mConfig.searchBarAppWidgetId;
            if (appWidgetId >= 0) {
                mSearchAppWidgetInfo = ssp.getAppWidgetInfo(appWidgetId);
                if (mSearchAppWidgetInfo == null) {
                    // If there is no actual widget associated with that id, then delete it and
                    // prepare to bind another app widget in its place
                    ssp.unbindSearchAppWidget(mAppWidgetHost, appWidgetId);
                    appWidgetId = -1;
                }
            }

            // If there is no id, then bind a new search app widget
            if (appWidgetId < 0) {
                Pair<Integer, AppWidgetProviderInfo> widgetInfo =
                        ssp.bindSearchAppWidget(mAppWidgetHost);
                if (widgetInfo != null) {
                    // Save the app widget id into the settings
                    mConfig.updateSearchBarAppWidgetId(this, widgetInfo.first);
                    mSearchAppWidgetInfo = widgetInfo.second;
                }
            }
        }
    }

    /** Creates the search bar app widget view */
    void addSearchBarAppWidgetView() {
        if (Constants.DebugFlags.App.EnableSearchLayout) {
            int appWidgetId = mConfig.searchBarAppWidgetId;
            if (appWidgetId >= 0) {
                mSearchAppWidgetHostView = mAppWidgetHost.createView(this, appWidgetId,
                        mSearchAppWidgetInfo);
                Bundle opts = new Bundle();
                opts.putInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY,
                        AppWidgetProviderInfo.WIDGET_CATEGORY_SEARCHBOX);
                mSearchAppWidgetHostView.updateAppWidgetOptions(opts);
                // Set the padding to 0 for this search widget
                mSearchAppWidgetHostView.setPadding(0, 0, 0, 0);
                mRecentsView.setSearchBar(mSearchAppWidgetHostView);
            } else {
                mRecentsView.setSearchBar(null);
            }
        }
    }

    /** Dismisses recents if we are already visible and the intent is to toggle the recents view */
    boolean dismissRecentsToFocusedTaskOrHome(boolean checkFilteredStackState) {
        SystemServicesProxy ssp = RecentsTaskLoader.getInstance().getSystemServicesProxy();
        if (ssp.isRecentsTopMost(ssp.getTopMostTask(), null)) {
            // If we currently have filtered stacks, then unfilter those first
            if (checkFilteredStackState &&
                mRecentsView.unfilterFilteredStacks()) return true;
            // If we have a focused Task, launch that Task now
            if (mRecentsView.launchFocusedTask()) return true;
            // If we launched from Home, then return to Home
            if (mConfig.launchedFromHome) {
                dismissRecentsToHomeRaw(true);
                return true;
            }
            // Otherwise, try and return to the Task that Recents was launched from
            if (mRecentsView.launchPreviousTask()) return true;
            // If none of the other cases apply, then just go Home
            dismissRecentsToHomeRaw(true);
            return true;
        }
        else {
            Xlog.d(TAG, "dismissRecentsToFocusedTaskOrHome : invisible");
        }
        return false;
    }

    /** Dismisses Recents directly to Home. */
    void dismissRecentsToHomeRaw(boolean animated) {
        if (animated) {
            ReferenceCountedTrigger exitTrigger = new ReferenceCountedTrigger(this,
                    null, mFinishLaunchHomeRunnable, null);
            mRecentsView.startExitToHomeAnimation(
                    new ViewAnimation.TaskViewExitContext(exitTrigger));
        } else {
            mFinishLaunchHomeRunnable.run();
        }
    }

    /** Dismisses Recents directly to Home if we currently aren't transitioning. */
    boolean dismissRecentsToHome(boolean animated) {
        SystemServicesProxy ssp = RecentsTaskLoader.getInstance().getSystemServicesProxy();
        if (ssp.isRecentsTopMost(ssp.getTopMostTask(), null)) {
            // Return to Home
            dismissRecentsToHomeRaw(animated);
            return true;
        }
        return false;
    }

    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Xlog.d(TAG, "RecentsActivity: onCreate");
        }

        super.onCreate(savedInstanceState);
        // For the non-primary user, ensure that the SystemServicesProxy and configuration is
        // initialized
        RecentsTaskLoader.initialize(this);
        SystemServicesProxy ssp = RecentsTaskLoader.getInstance().getSystemServicesProxy();
        mConfig = RecentsConfiguration.reinitialize(this, ssp);

        // Initialize the widget host (the host id is static and does not change)
        mAppWidgetHost = new RecentsAppWidgetHost(this, Constants.Values.App.AppWidgetHostId);

        // Set the Recents layout
        // fineos add start
         if (SystemProperties.get("ro.fineos.framework", "no").equals("yes")) {
        		setContentView(R.layout.fineos_recents);
         	}else{
         	    setContentView(R.layout.recents);
         	}
		// fineos add end
        mRecentsView = (RecentsView) findViewById(R.id.recents_view);
        mRecentsView.setCallbacks(this);
        mRecentsView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mEmptyViewStub = (ViewStub) findViewById(R.id.empty_view_stub);
        mDebugOverlayStub = (ViewStub) findViewById(R.id.debug_overlay_stub);
// FINEOS_REMOVE_ALLTASK_RECENTS add by fineos start
		if(SystemProperties.get("ro.fineos.framework", "no").equals("yes")){
			mRecentsRootLayout = (FrameLayout) findViewById(R.id.recents_root);
			
			mclearAllRecent = (ImageView)findViewById(R.id.removeAll);
			mMemInfoLayout = (LinearLayout)findViewById(R.id.memInfoLayout);
			
				
			mclearAllRecent.setOnClickListener( new OnClickListener() {
						public void onClick(View v) {
							startTweenAnimation();
							mRecentsView.dismissAllTaskView();
						  }
					});
			mDelTweenImg = (ImageView)findViewById(R.id.removeAllTween); 
			//fineos RECENTS_CLEAN_ANIMATION start
			mCleanView = (ImageView) findViewById(R.id.clean_icon);
			mCleanBgView = (ImageView) findViewById(R.id.clean_icon_bg);
			mCircleBackground = (CircleBackground) findViewById(R.id.circle_background);
			//fineos RECENTS_CLEAN_ANIMATION end

		}
// FINEOS_REMOVE_ALLTASK_RECENTS add by fineos end
		
        mScrimViews = new SystemBarScrimViews(this, mConfig);
        mStatusBar = ((SystemUIApplication) getApplication())
                .getComponent(PhoneStatusBar.class);
        inflateDebugOverlay();

        // Bind the search app widget when we first start up
        bindSearchBarAppWidget();

        // Register the broadcast receiver to handle messages when the screen is turned off
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED);
        registerReceiver(mSystemBroadcastReceiver, filter);

        // Private API calls to make the shadows look better
        try {
            Utilities.setShadowProperty("ambientRatio", String.valueOf(1.5f));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        ///M[HQ_REMOVE_ALLTASK_RECENTS] add remove all task in RECENTS @{
		if(com.huaqin.common.featureoption.FeatureOption.HQ_REMOVE_ALLTASK_RECENTS){
			mRemoveAllTask = (ImageView) findViewById(R.id.remove_all_task);
        	mRemoveAllTask.setOnClickListener(mRemoveAllTaskListener);
           mRemoveAllTask.setVisibility(View.VISIBLE);
        }
        /// }@	

		// FINEOS_REMOVE_ALLTASK_RECENTS add by fineos start
		if(needPrivateAlbumGuide()){
			openPrivateGuideView();
		}
		// FINEOS_REMOVE_ALLTASK_RECENTS add by fineos end
		
    }


    private class RemoveAllTask extends AsyncTask<Void, Void, Void> {
    
            @Override
            protected Void doInBackground(Void... params) {
                             mRecentsView.removeAllTask();
                             return null;
            }
    
            @Override
            protected void onPostExecute(Void result) {
                    onAllTaskViewsDismissed();
            }
    }
    /// }@	
	
	
		private void startTweenAnimation(){
		   mIsClickCleanALLBtn = true;
		   
			operatingAnim = AnimationUtils.loadAnimation(this, R.anim.fineos_recnts_del_tween);  
			LinearInterpolator lin = new LinearInterpolator();  
			operatingAnim.setInterpolator(lin); 
			if (operatingAnim != null) {  
			     mDelTweenImg.setVisibility(View.VISIBLE);
			     mDelTweenImg.startAnimation(operatingAnim);  
			}  
	 	}

	private void StopTweenAnimation(){
			if (operatingAnim != null) {  
				mDelTweenImg.setVisibility(View.GONE);
			    mDelTweenImg.clearAnimation();  
			}  
	 	}

    /** Inflates the debug overlay if debug mode is enabled. */
    void inflateDebugOverlay() {
        if (!Constants.DebugFlags.App.EnableDebugMode) return;

        if (mConfig.debugModeEnabled && mDebugOverlay == null) {
            // Inflate the overlay and seek bars
            mDebugOverlay = (DebugOverlayView) mDebugOverlayStub.inflate();
            mDebugOverlay.setCallbacks(this);
            mRecentsView.setDebugOverlay(mDebugOverlay);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        // Clear any debug rects
        if (mDebugOverlay != null) {
            mDebugOverlay.clear();
        }
    }

    @Override
    protected void onStart() {
        if (DEBUG) {
            Xlog.d(TAG, "RecentsActivity: onStart");
        }

        super.onStart();
        RecentsTaskLoader loader = RecentsTaskLoader.getInstance();
        SystemServicesProxy ssp = loader.getSystemServicesProxy();
        AlternateRecentsComponent.notifyVisibilityChanged(this, ssp, true);

        // Register the broadcast receiver to handle messages from our service
        IntentFilter filter = new IntentFilter();
        filter.addAction(AlternateRecentsComponent.ACTION_HIDE_RECENTS_ACTIVITY);
        filter.addAction(AlternateRecentsComponent.ACTION_TOGGLE_RECENTS_ACTIVITY);
        filter.addAction(AlternateRecentsComponent.ACTION_START_ENTER_ANIMATION);
        registerReceiver(mServiceBroadcastReceiver, filter);

        // Register any broadcast receivers for the task loader
        loader.registerReceivers(this, mRecentsView);

        // Update the recent tasks
        updateRecentsTasks(getIntent());

        // If this is a new instance from a configuration change, then we have to manually trigger
        // the enter animation state
        if (mConfig.launchedHasConfigurationChanged) {
            onEnterAnimationTriggered();
        }
    }

    @Override
    protected void onStop() {
        if (DEBUG) {
            Xlog.d(TAG, "RecentsActivity: onStop");
        }
		 if (SystemProperties.get("ro.fineos.framework", "no").equals("yes")) {
				 StopTweenAnimation();
				//fineos RECENTS_CLEAN_ANIMATION start
				 stopAnimation3();
				//fineos RECENTS_CLEAN_ANIMATION end
				 dismissAndGoBack(mIsClickCleanALLBtn,false);
				 
		 }
		 
        super.onStop();
        RecentsTaskLoader loader = RecentsTaskLoader.getInstance();
        SystemServicesProxy ssp = loader.getSystemServicesProxy();
        AlternateRecentsComponent.notifyVisibilityChanged(this, ssp, false);

        // Notify the views that we are no longer visible
        mRecentsView.onRecentsHidden();

        // Unregister the RecentsService receiver
        unregisterReceiver(mServiceBroadcastReceiver);

        // Unregister any broadcast receivers for the task loader
        loader.unregisterReceivers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            Xlog.d(TAG, "RecentsActivity: onDestroy");
        }

        // Unregister the system broadcast receivers
        unregisterReceiver(mSystemBroadcastReceiver);

        // Stop listening for widget package changes if there was one bound
        mAppWidgetHost.stopListening();
    }

    public void onEnterAnimationTriggered() {
        // Try and start the enter animation (or restart it on configuration changed)
        ReferenceCountedTrigger t = new ReferenceCountedTrigger(this, null, null, null);
        ViewAnimation.TaskViewEnterContext ctx = new ViewAnimation.TaskViewEnterContext(t);
        mRecentsView.startEnterRecentsAnimation(ctx);
        if (mConfig.searchBarAppWidgetId >= 0) {
            final WeakReference<RecentsAppWidgetHost.RecentsAppWidgetHostCallbacks> cbRef =
                    new WeakReference<RecentsAppWidgetHost.RecentsAppWidgetHostCallbacks>(
                            RecentsActivity.this);
            ctx.postAnimationTrigger.addLastDecrementRunnable(new Runnable() {
                @Override
                public void run() {
                    // Start listening for widget package changes if there is one bound
                    RecentsAppWidgetHost.RecentsAppWidgetHostCallbacks cb = cbRef.get();
                    if (cb != null) {
                        mAppWidgetHost.startListening(cb);
                    }
                }
            });
        }

        // Animate the SystemUI scrim views
        mScrimViews.startEnterRecentsAnimation();
    }

    @Override
    public void onTrimMemory(int level) {
        RecentsTaskLoader loader = RecentsTaskLoader.getInstance();
        if (loader != null) {
            loader.onTrimMemory(level);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_TAB: {
                boolean hasRepKeyTimeElapsed = (SystemClock.elapsedRealtime() -
                        mLastTabKeyEventTime) > mConfig.altTabKeyDelay;
                if (event.getRepeatCount() <= 0 || hasRepKeyTimeElapsed) {
                    // Focus the next task in the stack
                    final boolean backward = event.isShiftPressed();
                    mRecentsView.focusNextTask(!backward);
                    mLastTabKeyEventTime = SystemClock.elapsedRealtime();
                }
                return true;
            }
            case KeyEvent.KEYCODE_DPAD_UP: {
                mRecentsView.focusNextTask(true);
                return true;
            }
            case KeyEvent.KEYCODE_DPAD_DOWN: {
                mRecentsView.focusNextTask(false);
                return true;
            }
            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_FORWARD_DEL: {
                mRecentsView.dismissFocusedTask();
                return true;
            }
            default:
                break;
        }
        // Pass through the debug trigger
        mDebugTrigger.onKeyEvent(keyCode);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onUserInteraction() {
        mRecentsView.onUserInteraction();
    }

    @Override
    public void onBackPressed() {
        // Test mode where back does not do anything
        if (mConfig.debugModeEnabled) return;
		dismissRecentsToHome(true);
        // Dismiss Recents to the focused Task or Home
       // dismissRecentsToFocusedTaskOrHome(true);
    }

    /** Called when debug mode is triggered */
    public void onDebugModeTriggered() {
        if (mConfig.developerOptionsEnabled) {
            SharedPreferences settings = getSharedPreferences(getPackageName(), 0);
            if (settings.getBoolean(Constants.Values.App.Key_DebugModeEnabled, false)) {
                // Disable the debug mode
                settings.edit().remove(Constants.Values.App.Key_DebugModeEnabled).apply();
                mConfig.debugModeEnabled = false;
                inflateDebugOverlay();
                if (mDebugOverlay != null) {
                    mDebugOverlay.disable();
                }
            } else {
                // Enable the debug mode
                settings.edit().putBoolean(Constants.Values.App.Key_DebugModeEnabled, true).apply();
                mConfig.debugModeEnabled = true;
                inflateDebugOverlay();
                if (mDebugOverlay != null) {
                    mDebugOverlay.enable();
                }
            }
            Toast.makeText(this, "Debug mode (" + Constants.Values.App.DebugModeVersion + ") " +
                (mConfig.debugModeEnabled ? "Enabled" : "Disabled") + ", please restart Recents now",
                Toast.LENGTH_SHORT).show();
        }
    }

    /**** RecentsView.RecentsViewCallbacks Implementation ****/

    @Override
    public void onExitToHomeAnimationTriggered() {
        // Animate the SystemUI scrim views out
        mScrimViews.startExitRecentsAnimation();
    }

    @Override
    public void onTaskViewClicked() {
		/// M: add for multi window @{
	    if(FLOAT_WINDOW_SUPPORT){
		    if (mFinishLaunchHomeRunnable != null) {
	            mFinishLaunchHomeRunnable.run();
	         } else {
		        Intent homeIntent = new Intent(Intent.ACTION_MAIN, null);
		        homeIntent.addCategory(Intent.CATEGORY_HOME);
		        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
		                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		        startActivityAsUser(homeIntent, new UserHandle(UserHandle.USER_CURRENT));
			    finish();
                overridePendingTransition(R.anim.recents_to_launcher_enter,
                        R.anim.recents_to_launcher_exit);
	         }
	     }
        /// @}
    }

    @Override
    public void onTaskLaunchFailed() {
        if (DEBUG) {
            Xlog.d(TAG, "RecentsActivity: onTaskLaunchFailed");
        }

        // Return to Home
        dismissRecentsToHomeRaw(true);
    }

    @Override
    public void onAllTaskViewsDismissed() {
        /// M : Check if there is runnable or start Activity to Home directly @{
        if (mFinishLaunchHomeRunnable != null) {
            mFinishLaunchHomeRunnable.run();
       } else {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN, null);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            if (DEBUG) {
                Xlog.d(TAG, "onAllTaskViewsDismissed : " +
                       "start Activity to Home by intent if there is no runnable");
            }
            startActivityAsUser(homeIntent, new UserHandle(UserHandle.USER_CURRENT));
        }
        /// M : Check if there is runnable or start Activity to Home directly @}
    }

    @Override
    public void onScreenPinningRequest() {
        if (mStatusBar != null) {
            mStatusBar.showScreenPinningRequest(false);
        }
    }

    /**** RecentsAppWidgetHost.RecentsAppWidgetHostCallbacks Implementation ****/

    @Override
    public void refreshSearchWidget() {
        bindSearchBarAppWidget();
       // addSearchBarAppWidgetView();
    }

    /**** DebugOverlayView.DebugOverlayViewCallbacks ****/

    @Override
    public void onPrimarySeekBarChanged(float progress) {
        // Do nothing
    }

    @Override
    public void onSecondarySeekBarChanged(float progress) {
        // Do nothing
    }


	 @Override
    protected void onResume() {
        super.onResume();
        setMemoryInfo();
    }


public void dismissAndGoBack(boolean isDisToast, boolean isAllLocked) {
       
	if(isDisToast){
		 mNewmemAvail = getTotalMemory();
	     mNewmemAvail = mNewmemAvail >>10;
	     long clearMem = mNewmemAvail - mOldMemAvail ;
		
		String cleartxt;
		if(clearMem <= 0){
			 cleartxt = this.getString(R.string.fineos_clear_memrey_all_locked);
		}else{
		       cleartxt = this.getString(R.string.fineos_clear_memrey_toast,
		                            clearMem);
		}
		//fineos FINEOS_INFO_APP start
		if(isInfoAppExist()){
			if(clearMem <= 0){
				 clearMem = 2;
			}
		       cleartxt = String.format("%sM",clearMem);
			Intent intent = new Intent(Intent.ACTION_MAIN); 
			intent.addCategory(Intent.CATEGORY_LAUNCHER);             
			ComponentName cn = new ComponentName("com.fineos.info", "com.fineos.info.RecentInfoActivity");             
			intent.setComponent(cn); 
			intent.putExtra("clear_text", cleartxt);
			startActivity(intent); 
		}else{
			Toast toast = Toast.makeText(getApplicationContext(), cleartxt, 1000);
			toast.setGravity(Gravity.CENTER, 0, 360);
			toast.show();
		}
		//fineos FINEOS_INFO_APP end

	}
		mIsClickCleanALLBtn = false;
       // finish();
    }


// [FINEOS_REMOVE_ALLTASK_RECENTS] add by fineos start		
	 public void setMemoryInfo(){
	final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	long memAvail;
	long memTotal;
	
	 ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memInfo);
		
	 TextView mFreeMemreyInfo = (TextView) findViewById(R.id.freeMemreyInfoId);

	 mOldMemAvail = getTotalMemory();
	 mOldMemAvail = mOldMemAvail >>10;
	   
    mFreeMemreyInfo.setText(Long.toString(mOldMemAvail) + "M");
	 	 
	TextView mTotalFreeMemreyInfo = (TextView) findViewById(R.id.totalMemreyInfoId);
	
	memTotal = memInfo.totalMem >>20;
	mTotalFreeMemreyInfo.setText("/ " + Long.toString(memTotal) + "M");

  // [FINEOS_REMOVE_ALLTASK_RECENTS] add by fineos end

	//fineos RECENTS_CLEAN_ANIMATION start
	mMemTotal = memTotal;
	//fineos RECENTS_CLEAN_ANIMATION end

}

  // [FINEOS_REMOVE_ALLTASK_RECENTS] add by fineos start	
private long getTotalMemory() {
		String str1 = "/proc/meminfo";
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			 while ((str2 = localBufferedReader.readLine())!=null){
				arrayOfString = str2.split("\\s+");
				if("MemFree:".equals(arrayOfString[0]) || 
					"Buffers:".equals(arrayOfString[0]) ||
					"Cached:".equals(arrayOfString[0])){
					if(null != arrayOfString[1])
						initial_memory += Integer.valueOf(arrayOfString[1]).intValue();
					}
			 }
			 
			localBufferedReader.close();
			
		} catch (IOException e) {
		}
		return initial_memory;
	}



 private boolean needPrivateAlbumGuide(){

	SharedPreferences preferences = getSharedPreferences(PREFERENCES_RECENT_NAME, Activity.MODE_PRIVATE);
        boolean isFirstTimeVisitRecents = preferences.getBoolean(FIRST_TIME_IN_RECENTS, true);
        if (isFirstTimeVisitRecents) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(FIRST_TIME_IN_RECENTS, false);
            editor.commit();
        }
        return isFirstTimeVisitRecents;
}


 
private void openPrivateGuideView() {
        final View privateGuideView = getLayoutInflater().inflate(R.layout.fineos_recents_guide_layout, null);
        mRecentsRootLayout.addView(privateGuideView);
        privateGuideView.setVisibility(View.VISIBLE);
        privateGuideView.animate().alpha(1).setDuration(400).start();
        privateGuideView.setFocusable(true);
        privateGuideView.requestFocus();
        privateGuideView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        Button gotItButton = (Button) findViewById(R.id.recents_guide_dismiss_button);
        gotItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                privateGuideView.setVisibility(View.GONE);
                mRecentsRootLayout.removeView(privateGuideView);
            }
        });
        ImageView fingerImage = (ImageView) findViewById(R.id.imageview_finger);
        
		final TranslateAnimation animation = new TranslateAnimation(0, -150,0, 0); 
		animation.setDuration(1500);
		animation.setRepeatCount(1);
		fingerImage.setAnimation(animation); 
		animation.startNow();
		
    }

  // [FINEOS_REMOVE_ALLTASK_RECENTS] add by fineos end


    ///M[HQ_REMOVE_ALLTASK_RECENTS] add remove all task in RECENTS @{
    private View.OnClickListener mRemoveAllTaskListener = new View.OnClickListener(){
            @Override
                public void onClick(View v){
                    new RemoveAllTask().execute();
                }
    };
	//fineos FINEOS_INFO_APP start
	public boolean isInfoAppExist(){
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.fineos.info", "com.fineos.info.RecentInfoActivity"));
		if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() == 0) {
			return false;
		}
		return true;
	}
	//fineos FINEOS_INFO_APP end

	//fineos RECENTS_CLEAN_ANIMATION start
	private class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_ID_START_ANIMATION_1:
					startAnimation1();
					break;
				case MSG_ID_START_ANIMATION_1_2:
					startAnimation1_2();
					break;
				case MSG_ID_START_ANIMATION_2:
					startAnimation2();
					break;
				case MSG_ID_STOP_ANIMATION_3:
					stopAnimation3();
					break;
			}
		}
	}
	
	private void startAnimation1(){
		mCleanView.setVisibility(View.VISIBLE);
		mCleanView.setImageResource(R.anim.clean_gif1);
		AnimationDrawable animaition = (AnimationDrawable)mCleanView.getDrawable();  
		animaition.setOneShot(true);  
		animaition.start();
		mHandler.sendEmptyMessageDelayed(MSG_ID_START_ANIMATION_1_2, START_ANIMATION_1_2_TIME);
	}
	
	private void startAnimation1_2(){
		mCleanBgView.setVisibility(View.VISIBLE);
		int color = getCleanBgColor();
		mCleanBgView.setColorFilter(color);
		mCleanView.setImageResource(R.anim.clean_gif1_2);
		AnimationDrawable animaition = (AnimationDrawable)mCleanView.getDrawable();  
		animaition.setOneShot(true);  
		animaition.start();
		mHandler.sendEmptyMessageDelayed(MSG_ID_START_ANIMATION_2, START_ANIMATION_2_TIME);
	}
	
	private void startAnimation2(){
		mCleanView.setImageResource(R.drawable.clean_focus);
		mCleanView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsClickCleanALLBtn = true;
				mRecentsView.dismissAllTaskView();
				startAnimation3();
			}
		});

		mCircleBackground.setVisibility(View.VISIBLE);
		mCircleBackground.startRippleAnimation();
	}
	
	private void startAnimation3(){
		mCircleBackground.setVisibility(View.GONE);
		mCleanView.setImageResource(R.drawable.clean_rotate);
		Animation animaition3 = AnimationUtils.loadAnimation(this, R.anim.clean_gif3);  
		LinearInterpolator lin = new LinearInterpolator();  
		animaition3.setInterpolator(lin); 
		mCleanView.startAnimation(animaition3);  
	}
	
	private void stopAnimation3(){
		mCleanView.clearAnimation();  
	}

	private int getCleanBgColor(){
		int green = 0xFF05C600;
		int yellow = 0xFFF3D400;
		int red = 0xFFE20B00;
		int mem1, mem2;
		Log.e("yfm", "mMemTotal = "+mMemTotal+", mOldMemAvail = "+mOldMemAvail);

		if(mMemTotal >= 1800){
			mem1 = 800;
			mem2 = 400;
		}else{
			mem1 = 400;
			mem2 = 250;
		}
		
		int color = green;
		if(mOldMemAvail > mem1){
			color = green;
		}else if(mOldMemAvail > mem2){
			color = yellow;
		}else{
			color = red;
		}
		return color;
	}

	//fineos RECENTS_CLEAN_ANIMATION end

}
