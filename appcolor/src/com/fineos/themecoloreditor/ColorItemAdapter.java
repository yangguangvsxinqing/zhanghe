package com.fineos.themecoloreditor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.SubtitleTrack.RenderingWidget.OnChangedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONException;

import com.fineos.themecoloreditor.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ubuntu on 15-11-12.
 */
public class ColorItemAdapter extends
		RecyclerView.Adapter<ColorItemAdapter.ViewHolder> {
	private static final String TAG = "ColorItemAdapter";
	private LayoutInflater mInflater;
	private List<ThemeColors> mDatas;
	private ImageView temp;
	private int select;
   
 
	public interface OnItemClickLitener {
		void onItemselected(ThemeColors colors, int position) throws JSONException;
	}

	private OnItemClickLitener mOnItemClickLitener;

	public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
		this.mOnItemClickLitener = mOnItemClickLitener;
		
	}

	public ColorItemAdapter(Context context, List<ThemeColors> datats) {
		mInflater = LayoutInflater.from(context);
		mDatas = datats;	
	}
	
	public  class ViewHolder extends RecyclerView.ViewHolder {
		public ViewHolder(View arg0) {
			super(arg0);
		}

		ImageView mImg;
		ImageView mChoose;
		ImageView mContext1Img;
		ImageView mContext2Img;
		ImageView mAddButtonImg;
	}

	@Override
	public int getItemCount() {
		return mDatas.size();
	}

	/**
	 * 创建ViewHolder
	 */
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View view = mInflater.inflate(R.layout.newcoloritem, viewGroup, false);
		ViewHolder viewHolder = new ViewHolder(view);
		viewHolder.mImg = (ImageView) view.findViewById(R.id.coloricon);
		viewHolder.mChoose = (ImageView) view.findViewById(R.id.ic_choose);
		viewHolder.mContext1Img = (ImageView) view.findViewById(R.id.item_context1);
		viewHolder.mContext2Img = (ImageView) view.findViewById(R.id.item_context2);
		viewHolder.mAddButtonImg = (ImageView) view.findViewById(R.id.item_add_button);
		return viewHolder;
	}

	private int getColorInt(int color) {
		int colorInt = 0;

		int alpha = (color >> 24) & 0xFF;
		int red = (color >> 16) & 0xFF;
		int green = (color >> 8) & 0XFF;
		int blue = color & 0XFF;
		colorInt = Color.argb(alpha, red, green, blue);
		return colorInt;

	}

	public void setbeginposition(int positon) {
		this.select = positon;
	}

	/**
	 * 设置值
	 */
	@Override
	public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
		viewHolder.mImg.setTag(i);
		final GradientDrawable myGrad = (GradientDrawable) viewHolder.mImg.getBackground();
		myGrad.setColor(getColorInt(mDatas.get(i).getTitleColor()));
		
		final GradientDrawable context1GD = (GradientDrawable) viewHolder.mContext1Img.getBackground();
		context1GD.setColor(getColorInt(mDatas.get(i).getContext1Color()));
		
		final GradientDrawable context2GD = (GradientDrawable) viewHolder.mContext2Img.getBackground();
		context2GD.setColor(getColorInt(mDatas.get(i).getContext2Color()));
		
		final GradientDrawable bottomButtonGD = (GradientDrawable) viewHolder.mAddButtonImg.getBackground();
		bottomButtonGD.setColor(getColorInt(mDatas.get(i).getBottomButtonColor()));

		final GradientDrawable mChooseGD = (GradientDrawable) viewHolder.mChoose.getBackground();
		
		if (i == select) {
			temp = viewHolder.mImg;
			viewHolder.mChoose.setVisibility(View.VISIBLE);
//			myGrad.setStroke(3, Color.WHITE);
			try {
				mOnItemClickLitener.onItemselected(mDatas.get(i), i);								
			} catch (JSONException e) {
				e.printStackTrace();
			}

		} else {
//			myGrad.setStroke(0, Color.WHITE);
			viewHolder.mChoose.setVisibility(View.GONE);
		}
		
		
		viewHolder.mImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				select = (Integer) viewHolder.mImg.getTag();
				if (temp != null && temp != v) {
//					myGrad.setStroke(3, Color.WHITE);
					GradientDrawable grad = (GradientDrawable) temp
							.getBackground();
//					grad.setStroke(0, Color.WHITE);
					temp = viewHolder.mImg;			
				} else {
//					myGrad.setStroke(3, Color.WHITE);
					temp = viewHolder.mImg;
				}
				try {
					mOnItemClickLitener.onItemselected(mDatas.get(i), i);
				
					viewHolder.mChoose.setVisibility(View.VISIBLE);
					viewHolder.mImg.getTag(i);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				notifyDataSetChanged();
			}
		});
		


	}
	

}
