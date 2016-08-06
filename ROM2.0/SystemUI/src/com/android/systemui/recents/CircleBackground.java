package com.android.systemui.recents;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import com.android.systemui.R;
import android.util.Log;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.animation.LinearInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;


/**
 * Created by fyu on 11/3/14.
 */
//fineos RECENTS_CLEAN_ANIMATION start
public class CircleBackground extends RelativeLayout{

	private Paint paint;
	private boolean animationRunning=false;
	private LayoutParams rippleParams;
	RippleView mRippleView;
	ValueAnimator mAnimator = ValueAnimator.ofFloat(0.0F, 1.0F);  

	public CircleBackground(Context context) {
		super(context);
	}

	public CircleBackground(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public CircleBackground(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(final Context context, final AttributeSet attrs) {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);

		mRippleView=new RippleView(getContext());
		addView(mRippleView);


		mAnimator = ValueAnimator.ofFloat(0.0F, 1.0F);  
		mAnimator.setTarget(mRippleView);  
		mAnimator.setDuration(1500);
		mAnimator.setRepeatCount(ObjectAnimator.INFINITE);
		mAnimator.setRepeatMode(ObjectAnimator.RESTART);
		Interpolator interpolator = new DecelerateInterpolator();  
		mAnimator.setInterpolator(interpolator);

		mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){  
			@Override  
			public void onAnimationUpdate(ValueAnimator animation)  
			{  
				float progress =  (float) animation.getAnimatedValue();
				mRippleView.setProgress(progress);
				mRippleView.invalidate();
			}  
		}); 

	}

	private class RippleView extends View{
		private float mProgress;

		public RippleView(Context context) {
			super(context);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			int fromRadius = 57;
			int toRadius = 68;
			int radius = (int)((toRadius - fromRadius)*mProgress + fromRadius);
			int fromStrokeWidth = 6;
			int toStrokeWidth = 2;
			int strokeWidth = (int)((toStrokeWidth - fromStrokeWidth)*mProgress + fromStrokeWidth);
			float fromAlpha = 1.0F;
			float toAlpha = 0.0F;
			float alpha = (toAlpha - fromAlpha)*mProgress + fromAlpha;
			int intAlpha = (int)(255 * alpha);

			int center=(Math.min(getWidth(),getHeight()))/2;
			paint.setStrokeWidth(strokeWidth);
			int color = Color.argb( intAlpha, 0xFF, 0xFF, 0xFF) ;
			paint.setColor(color);

			canvas.drawCircle(center,center,radius,paint);
		}
		public void setProgress(float progress){
			mProgress = progress;
		}
	}

	public void startRippleAnimation(){
		if(!isRippleAnimationRunning()){
			mAnimator.start();
			animationRunning=true;
		}
	}

	public void stopRippleAnimation(){
		if(isRippleAnimationRunning()){
			animationRunning=false;
		}
	}

	public boolean isRippleAnimationRunning(){
		return animationRunning;
	}
}
//fineos RECENTS_CLEAN_ANIMATION end

