package com.android.systemui.statusbar.fifthlock;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.telecom.TelecomManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.android.systemui.R;
import com.android.systemui.statusbar.phone.ActivityStarter;
import com.android.systemui.statusbar.policy.FlashlightController;


/**
 * @ClassName:KeyguardBottomAreaViewHQ
 * @Description:锁屏未读事件视图{modify for [HQ_BOWAY_KEYGUARD]}
 * @author:BingWu.Lee
 * @date:2015-6-10
 */
public class KeyguardBottomAreaViewHQ extends RelativeLayout {

    private static final Intent PHONE_INTENT = new Intent(Intent.ACTION_DIAL);
	private static final Intent INSECURE_CAMERA_INTENT =
            new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);

	private KeyguardUnreadView mKeyguardUreadViewPhone;
	private ImageView mKeyguardUreadVieCamera;

	private ActivityStarter mActivityStarter;
	private FlashlightController mFlashlightController;

	private GestureDetector mGestureDetector;
    private View mTouchView;

	public KeyguardBottomAreaViewHQ(Context context) {
		this(context, null);
	}

	public KeyguardBottomAreaViewHQ(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public KeyguardBottomAreaViewHQ(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mKeyguardUreadViewPhone = (KeyguardUnreadView) findViewById(R.id.keyguard_iv_phone);
		mKeyguardUreadVieCamera = (ImageView) findViewById(R.id.keyguard_iv_camera);
		mKeyguardUreadViewPhone.init(R.drawable.keyguard_bottom_phone,KeyguardUnreadView.KEY_PHONE);

		mKeyguardUreadViewPhone.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                mTouchView = view;
                return mGestureDetector.onTouchEvent(ev);
            }
        });
        mKeyguardUreadVieCamera.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                mTouchView = view;
                return mGestureDetector.onTouchEvent(ev);
            }
        });

        mGestureDetector = new GestureDetector(new DoubleTapGestureListener(),
                new Handler());
	}

	public void setActivityStarter(ActivityStarter activityStarter) {
        mActivityStarter = activityStarter;
    }

	public void setFlashlightController(FlashlightController flashlightController) {
        mFlashlightController = flashlightController;
    }

	public void launchPhone() {

        final TelecomManager tm = TelecomManager.from(getContext());

        if (tm.isInCall()) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    tm.showInCallScreen(false /* showDialpad */);
                }
            });
        } else {
            mActivityStarter.startActivity(PHONE_INTENT, false /* dismissShade */);
        }
    }
    
    private void launchCamera(){
		mFlashlightController.killFlashlight();
        mActivityStarter.startActivity(INSECURE_CAMERA_INTENT, false /* dismissShade */);
    }

    class DoubleTapGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mTouchView == mKeyguardUreadViewPhone) {
                launchPhone();
            } else if (mTouchView == mKeyguardUreadVieCamera) {
				launchCamera();
            }
            return true;
        }
    }

}
