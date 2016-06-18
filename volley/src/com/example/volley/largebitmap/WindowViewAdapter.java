package com.example.volley.largebitmap;

import java.util.List;

import com.example.volley.R;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("ResourceAsColor")
public class WindowViewAdapter extends RecyclerView.Adapter<WindowViewAdapter.MyViewHolder>{
	private List<WindowData> mList;
	private LayoutInflater mInflater;
	private View.OnClickListener mOnClickListener;
	private boolean animFlag = false;
	private int actionBarHeight;
	public WindowViewAdapter(Context context,List<WindowData> mList,int size){
		this.mList = mList;
		actionBarHeight = size;
		mInflater = LayoutInflater.from(context);
		mOnClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!animFlag){
					
				animFlag = true;
				int tmp[] = new int[2];
				v.getLocationInWindow(tmp);
				Log.v("", "sss tmp="+tmp[0]);
				Log.v("", "sss tmp="+tmp[1]);
				Log.v("", "sss actionBarHeight ="+actionBarHeight);
//				AnimationSet set =new AnimationSet(false);
//		        ScaleAnimation scaleAnim = new ScaleAnimation (1.0f,0.2f,1.0f,0.2f,0.5f,0.5f);
//		        set.addAnimation(scaleAnim);
//				TranslateAnimation translateAnimationX = new TranslateAnimation(tmp[0], 360, 0, 0);
//				translateAnimationX.setInterpolator(new LinearInterpolator());
//				translateAnimationX.setRepeatCount(0);
//				TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0,  tmp[1] - v.getMeasuredHeight()/2, 1000);
//				translateAnimationY.setInterpolator(new AccelerateInterpolator());
//				translateAnimationY.setRepeatCount(0);
//				set.addAnimation(translateAnimationX);
//				set.addAnimation(translateAnimationY);
//				set.setDuration(1500);
				Log.v("", "sss v.getMeasuredWidth()="+v.getMeasuredWidth());
				Log.v("", "sss v.getMeasuredHeight()="+v.getMeasuredHeight());
				LargeImageViewActivity.img.setText((String)v.getTag());
				LargeImageViewActivity.img.setBackground(v.getBackground());
				RelativeLayout.LayoutParams lytp = new RelativeLayout.LayoutParams(v.getMeasuredWidth(),v.getMeasuredHeight());
				LargeImageViewActivity.img.setLayoutParams(lytp);
//				LargeImageViewActivity.img.startAnimation(set);
//				LargeImageViewActivity.img.setVisibility(View.VISIBLE);
//				set.setAnimationListener(new AnimationListener(){
//
//					@Override
//					public void onAnimationStart(Animation animation) {
//						// TODO Auto-generated method stub
//						
//					}
//
//					@Override
//					public void onAnimationEnd(Animation animation) {
//						// TODO Auto-generated method stub
//						LargeImageViewActivity.img.setVisibility(View.GONE);
//					}
//
//					@Override
//					public void onAnimationRepeat(Animation animation) {
//						// TODO Auto-generated method stub
//						
//					}
//					
//				});
				LargeImageViewActivity.img.setVisibility(View.VISIBLE);
				AnimatorSet animatorSet = new AnimatorSet();
				
				ObjectAnimator obj = ObjectAnimator.ofFloat(LargeImageViewActivity.img, "scaleX", 1f, 0.2f);
				ObjectAnimator obj2 = ObjectAnimator.ofFloat(LargeImageViewActivity.img, "scaleY", 1f, 0.2f);
				ObjectAnimator obj3 = ObjectAnimator.ofFloat(LargeImageViewActivity.img, "translationX", tmp[0], 500);
				ObjectAnimator obj4 = ObjectAnimator.ofFloat(LargeImageViewActivity.img, "translationY", tmp[1] - actionBarHeight, 900);
				obj4.setInterpolator(new AccelerateInterpolator(2f));
				animatorSet.play(obj).with(obj2).with(obj3).with(obj4);
				animatorSet.setDuration(1500);
				animatorSet.addListener(new AnimatorListener() {
					
					@Override
					public void onAnimationStart(Animator animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationRepeat(Animator animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationEnd(Animator animation) {
						// TODO Auto-generated method stub
						LargeImageViewActivity.img.setVisibility(View.GONE);
						animFlag = false;
					}
					
					@Override
					public void onAnimationCancel(Animator animation) {
						// TODO Auto-generated method stub
						
					}
				});
				animatorSet.start();
				
			}

			}
		}; 
	}
	
	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@SuppressLint("ResourceAsColor")
	@Override
	public void onBindViewHolder(MyViewHolder viewHolder, int postion) {
		// TODO Auto-generated method stub
		WindowData tmp = mList.get(postion);
		if(tmp.getWinType() == 0){
			viewHolder.nameView.setVisibility(View.VISIBLE);
			viewHolder.title1View.setVisibility(View.GONE);
			viewHolder.title2View.setVisibility(View.GONE);
			viewHolder.nameView.setText(tmp.getWinName());
			viewHolder.nameView.setTag(tmp.getWinName());
		}else{
			viewHolder.nameView.setVisibility(View.GONE);
			viewHolder.title1View.setVisibility(View.VISIBLE);
			viewHolder.title2View.setVisibility(View.VISIBLE);
			viewHolder.title1View.setText(tmp.getWinTitle1());
			viewHolder.title2View.setText(tmp.getWinTitle2());
			viewHolder.title1View.setTag(tmp.getWinTitle1());
			viewHolder.title2View.setTag(tmp.getWinTitle2());
		}
		if(tmp.getWinColor() == 0){
			viewHolder.nameView.setBackgroundResource(R.drawable.color1);
			viewHolder.title1View.setBackgroundResource(R.drawable.color1);
			viewHolder.title2View.setBackgroundResource(R.drawable.color1);
		}else if(tmp.getWinColor() == 1){
			viewHolder.nameView.setBackgroundResource(R.drawable.color2);
			viewHolder.title1View.setBackgroundResource(R.drawable.color2);
			viewHolder.title2View.setBackgroundResource(R.drawable.color2);
		}else{
			viewHolder.nameView.setBackgroundResource(R.drawable.color3);
			viewHolder.title1View.setBackgroundResource(R.drawable.color3);
			viewHolder.title2View.setBackgroundResource(R.drawable.color3);
		}
		viewHolder.nameView.setOnClickListener(mOnClickListener);
		viewHolder.title1View.setOnClickListener(mOnClickListener);
		viewHolder.title2View.setOnClickListener(mOnClickListener);
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// TODO Auto-generated method stub
		MyViewHolder holder = null;
	//	if(viewType == 0){
			holder = new MyViewHolder(mInflater.inflate(
					R.layout.window_item1, parent, false));
	//	}
		
		return holder;
	}
	
	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return mList.get(position).getWinType();
	}

	public class MyViewHolder extends ViewHolder{
		public TextView nameView;
		public TextView title1View;
		public TextView title2View;

		public MyViewHolder(View view) {
			super(view);
			// TODO Auto-generated constructor stub
			nameView = (TextView)(view.findViewById(R.id.name));
			title1View = (TextView)(view.findViewById(R.id.title1));
			title2View = (TextView)(view.findViewById(R.id.title2));
			
		}
	}
	
	public class AnimData{
		public String title;
		public int res;
	}
	
}