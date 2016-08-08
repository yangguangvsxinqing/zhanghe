package com.fineos.themecoloreditor;

import android.R.integer;
import android.content.Context;
//import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import com.fineos.themecoloreditor.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by ubuntu on 15-11-12.
 */
public class RecycleViewAdapter extends
		RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {
	private Context mcontext;
	private LayoutInflater mInflater;
	private List<int[]> mDatas;

	public void selectall() {
		for (int i = 0; i < 6; i++) {
			ArrayHelper.setChecked(i, true);
		}
	}

	public void selectnone() {
		for (int i = 0; i < 6; i++) {
			ArrayHelper.setChecked(i, false);
		}
	}

	public interface OnItemClickLitener {
		void onItemselected(View view, int position, boolean checked)
				throws JSONException;
	}

	private OnItemClickLitener mOnItemClickLitener;

	public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
		this.mOnItemClickLitener = mOnItemClickLitener;
	}

	public RecycleViewAdapter(Context context, List<int[]> datats) {
		mcontext = context;
		mInflater = LayoutInflater.from(context);
		mDatas = datats;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public ViewHolder(View arg0) {
			super(arg0);
		}
		ImageView mImg1;
		ImageView mImg2;
		ImageView mImg3;
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
		View view = mInflater.inflate(R.layout.applicationimage, viewGroup,	false);
		ViewHolder viewHolder = new ViewHolder(view);
		viewHolder.mImg1 = (ImageView) view.findViewById(R.id.appimage1);
		viewHolder.mImg2 = (ImageView) view.findViewById(R.id.appimage2);
		viewHolder.mImg3 = (ImageView) view.findViewById(R.id.appimage3);
		return viewHolder;
	}

	/**
	 * 设置值
	 */
	
	private int dialDrawble[] = {R.drawable.dial1,  R.drawable.dial2};
	private int calendarDrawable[] = {R.drawable.calendar1, R.drawable.calendar2};
	private int mmsDrawable[] = {R.drawable.mms1, R.drawable.mms2, R.drawable.mms3};
	private int settingsDrawable[] = {R.drawable.setting};
	private int SystemUiDrawable[] = {R.drawable.systemui};

	@Override
	public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
		int[] drawbles = mDatas.get(i);
		ThemeColors color = ArrayHelper.getColor();
		viewHolder.mImg1.setImageResource(drawbles[0]);
		if (color != null) {
			viewHolder.mImg1.setBackgroundColor(color.getTitleColor());
		}
		if (i == 0) {
			
			viewHolder.mImg2.setImageResource(drawbles[1]);
			if (color != null) {
				viewHolder.mImg2.setBackgroundColor(color.getBottomButtonColor());
			}			
		} else if (i == 1) {
			viewHolder.mImg2.setImageResource(drawbles[1]);
			if (color != null) {
				viewHolder.mImg2.setBackgroundColor(color.getBottomButtonColor());
			}
		} else if (i == 2) {
			viewHolder.mImg2.setImageResource(drawbles[1]);
			if (color != null) {
				viewHolder.mImg2.setBackgroundColor(color.getContext1Color());
				viewHolder.mImg3.setBackgroundColor(color.getContext2Color());
			}
			viewHolder.mImg3.setImageResource(drawbles[2]);
		}
	}
}
