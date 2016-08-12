package com.fineos.theme.ui;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
//import android.animation.ObjectAnimator;
//import android.view.animation.DecelerateInterpolator;

public class FlipImageView extends ImageView implements Animator.AnimatorListener {
	private static final int STATE_NEUTRAL = 0;
	private static final int STATE_ROTATE_OUT = 1;
	private static final int STATE_ROTATE_IN = 2;
	private static final long ANIMATION_TIME = 500;
	private int mState = STATE_NEUTRAL;
	private int mNewResourceId = 0;
	private Drawable mNewDrawable = null;

	public FlipImageView(Context context) {
		this(context, null);
	}

	public FlipImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FlipImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setImageResourceAnimated(int resId) {
		setImageResourceAnimated(resId, 0);
	}

	public void setImageResourceAnimated(int resId, long startDelay) {
		mNewResourceId = resId;
		mNewDrawable = null;
		ObjectAnimator rotateOut = ObjectAnimator.ofFloat(this, "rotationY", 90);
		rotateOut.setInterpolator(new AccelerateInterpolator(1.0f));
		rotateOut.setDuration(ANIMATION_TIME);
		rotateOut.setStartDelay(startDelay);
		rotateOut.addListener((AnimatorListener) this);
		rotateOut.start();
		mState = STATE_ROTATE_OUT;
	}

	public void setImageDrawableAnimated(Drawable drawable) {
		setImageDrawableAnimated(drawable, 0);
	}

	public void setImageDrawableAnimated(Drawable drawable, long startDelay) {
		mNewDrawable = drawable;
		mNewResourceId = 0;
		ObjectAnimator rotateOut = ObjectAnimator.ofFloat(this, "rotationY", 90);
		rotateOut.setInterpolator(new AccelerateInterpolator(1.0f));
		rotateOut.setDuration(ANIMATION_TIME);
		rotateOut.setStartDelay(startDelay);
		rotateOut.addListener((AnimatorListener) this);
		rotateOut.start();
		mState = STATE_ROTATE_OUT;
	}

	public void onAnimationStart(Animator animator) {
	}

	public void onAnimationEnd(Animator animator) {
		if (mState == STATE_ROTATE_OUT) {
			if (mNewDrawable != null) {
				setImageDrawable(mNewDrawable);
				mNewDrawable = null;
			} else
				setImageResource(mNewResourceId);
			ObjectAnimator rotateIn = ObjectAnimator.ofFloat(this, "rotationY", 270, 360);
			rotateIn.setInterpolator(new DecelerateInterpolator(1.0f));
			rotateIn.setDuration(ANIMATION_TIME);
			rotateIn.addListener((AnimatorListener) this);
			rotateIn.start();
			mState = STATE_ROTATE_IN;
		} else if (mState == STATE_ROTATE_IN) {
			mState = STATE_NEUTRAL;
		}
	}

	public void onAnimationCancel(Animator animator) {
	}

	public void onAnimationRepeat(Animator animator) {
	}
}
