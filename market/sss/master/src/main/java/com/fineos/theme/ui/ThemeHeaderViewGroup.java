package com.fineos.theme.ui;

import java.util.ArrayList;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.fineos.theme.R;
import com.fineos.theme.activity.ThemeLocalHomeActivity;
import com.fineos.theme.activity.ThemeOnlineHomeActivity;
import com.fineos.theme.model.CustomData;

/**created by huchenbo ,this ViewGroup is for ThemeView,it can make m*n ViewGroup,
 *     but you must setchildLayoutP...when you use it*/
public class ThemeHeaderViewGroup extends ViewGroup{

	private static final String tag = "OverContainThemeViewGroup";
	private int mColumns = 5;
	private int mRows = 1;
	private int mMargin = 0;
	private int mHorizontalGap = 0;
	private int mVerticalGap = 0;
	private int mCount = 5;
	private int mMaxChildWidth = 0;
	private int mMaxChildHeight = 0;
	private final String TAG = "OverContainThemeViewGroup";
	public ThemeHeaderViewGroup(Context context, AttributeSet attrs, int defStyle) {
		
		super(context, attrs, defStyle);
	}

	public ThemeHeaderViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}

	public ThemeHeaderViewGroup(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		mMaxChildWidth = 0;
		mMaxChildHeight = 0;
		mCount = getChildCount();
		if (mCount == 0) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		mRows = mCount % mColumns == 0 ? mCount / mColumns : mCount / mColumns + 1;
		int top[] = new int[mColumns];
		for (int i = 0; i < mRows; i++) {
			for (int j = 0; j < mColumns; j++) {
				View child = this.getChildAt(i * mColumns + j);
				if (child == null)
					break;
				ViewGroup.LayoutParams lp = child.getLayoutParams();
				if (child.getVisibility() == GONE) {
					continue;
				}
				child.measure(MeasureSpec.makeMeasureSpec(
						MeasureSpec.getSize(widthMeasureSpec),
						MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
						lp.height, MeasureSpec.AT_MOST));
				top[j] += lp.height + mVerticalGap;
				mMaxChildWidth = Math.max(mMaxChildWidth,
						child.getMeasuredWidth());
			}

		}
		setMeasuredDimension(resolveSize(mMaxChildWidth, widthMeasureSpec),
				resolveSize(getMax(top) + mVerticalGap, heightMeasureSpec));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int height = b - t;
		int width = r - l;
		if (mCount == 0)
			return;
		int gridW = (width - this.getPaddingLeft() - this.getPaddingRight() - mHorizontalGap * (mColumns + 1)) / mColumns;
		int gridH = 0;
		int left = 0;
		int top[] = new int[mColumns];

		for (int i = 0; i < mRows; i++) {
			for (int j = 0; j < mColumns; j++) {
				View child = this.getChildAt(i * mColumns + j);
				if (child == null)
					return;
				ViewGroup.LayoutParams lp = child.getLayoutParams();

				child.measure(MeasureSpec.makeMeasureSpec(gridW,
						MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
						lp.height, MeasureSpec.AT_MOST));

				if (child.getTag() != null && child.getTag().equals(tag)) {
					child.measure(MeasureSpec.makeMeasureSpec(gridW,
							MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
							getMax(top) - top[j], MeasureSpec.EXACTLY));
					gridH = getMax(top) - top[j];
					left = j * gridW + mHorizontalGap * (j + 1);
					child.layout(left, top[j] + mVerticalGap, left + gridW, top[j]
							+ gridH);
					break;
				}

				gridH = lp.height;
				left = j * gridW + mHorizontalGap * (j + 1);
				top[j] += mVerticalGap;
				child.layout(left, top[j], left + gridW, top[j] + gridH);
				top[j] += gridH;
			}

		}
	}

	private int getMax(int array[]) {
		int max = array[0];
		for (int i = 0; i < array.length; i++) {
			if (max < array[i])
				max = array[i];
		}
		return max;
	}
	
	public void setLayoutLp(int Columns, int count) {
		if (mColumns != Columns || mCount != count) {
			mColumns = Columns;
			mCount = count;
			this.requestLayout();
		}
	}
	
	public void setgap(int horizontalgap, int verticalGap) {
		if (mHorizontalGap != horizontalgap || mVerticalGap != verticalGap ) {
			mHorizontalGap = horizontalgap;
			mVerticalGap = verticalGap;
			this.requestLayout();
		}
	}
	
	public void addLocalThemeHeaderChildView(ArrayList<CustomData> list, OnClickListener listener, int childresourceId) {
		int count = list.size();
		for (int i = 0; i < count; i++) {
			CustomData customData = list.get(i);
			View view = View.inflate(getContext(), childresourceId, null);
			android.view.ViewGroup.LayoutParams lp = (android.view.ViewGroup.LayoutParams)
					new android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, 
					(int)getContext().getResources().getDimension(R.dimen.local_theme_child_view_height));
			view.setLayoutParams(lp);
			Drawable drawable = getContext().getResources().getDrawable(customData.getImgId());
			ImageView iv = (ImageView)view.findViewById(R.id.custom_img);
			iv.setTag(Integer.valueOf(customData.getCustomId()));
			iv.setImageDrawable(drawable);
			TextView titleTextView = (TextView)view.findViewById(R.id.custom_title);
			titleTextView.setText(customData.getTitle());
			view.setTag(customData.getCustomId());
			view.setOnClickListener(listener);
			this.addView(view);
		}
	}
	
	public void addThemeDrawableChildView(ArrayList<CustomData> list, TypedArray iconArray, OnClickListener listener) {
		int drawableNum = Math.min(iconArray.length(), list.size());
		if (drawableNum > 0) {
			this.removeAllViews();
			for (int i = 0 ; i < drawableNum; i++) {
				CustomData cd = list.get(i);
				DrawableCenterButton view = (DrawableCenterButton) View.inflate(getContext(), R.layout.theme_drawable_button, null);
				view.setCompoundDrawablesWithIntrinsicBounds(null, 
						iconArray.getDrawable(i), null, null);
				android.view.ViewGroup.LayoutParams lp = (android.view.ViewGroup.LayoutParams)
						new android.view.ViewGroup.LayoutParams((int)getContext().getResources().getDimension(R.dimen.drawble_icon_item_height), 
						(int)getContext().getResources().getDimension(R.dimen.drawble_icon_item_height));
				view.setLayoutParams(lp);
				view.setText(cd.getTitle());
				view.setOnClickListener(listener);
				view.setTag(getTagByType(cd.getCustomType()));
				view.setCompoundDrawablePadding((int)getContext().getResources().getDimension(R.dimen.drawble_icon_padding_top));
				this.addView(view);
			}
			setLayoutLp(drawableNum, drawableNum);
		}
	}
	
	public void addThemeDrawableChildView(ArrayList<CustomData> list, OnClickListener listener) {
		int drawableNum = list.size();
		if (drawableNum > 0) {
			this.removeAllViews();
			for (int i = 0 ; i < drawableNum; i++) {
				CustomData cd = list.get(i);
				DrawableCenterButton view = (DrawableCenterButton) View.inflate(getContext(), R.layout.theme_drawable_button, null);
				view.setCompoundDrawablesWithIntrinsicBounds(null, 
						getContext().getResources().getDrawable(cd.getImgId()), null, null);
				android.view.ViewGroup.LayoutParams lp = (android.view.ViewGroup.LayoutParams)
						new android.view.ViewGroup.LayoutParams((int)getContext().getResources().getDimension(R.dimen.drawble_icon_item_height), 
						(int)getContext().getResources().getDimension(R.dimen.drawble_icon_item_height));
				view.setLayoutParams(lp);
				view.setText(cd.getTitle());
				view.setOnClickListener(listener);
				view.setTag(cd.getCustomId());
				view.setCompoundDrawablePadding((int)getContext().getResources().getDimension(R.dimen.drawble_icon_padding_top));
				this.addView(view);
			}
			setLayoutLp(drawableNum, drawableNum);
		}
	}
	
	private int getTagByType(String type) {
		int tag = -1;
		if (type != null && type.equals("font")) {
			tag = CustomData.CUSTOM_ITEM_FONT;
		} else if (type != null && type.equals("wallpaper")) {
			tag = CustomData.CUSTOM_WALLPAPER;
		} else if (type != null && type.equals("livewallpaper")) {
			tag = CustomData.CUSTOM_ITEM_DYNAMIC_WALLPAPER;
		} else if (type != null && type.equals("lockscreen")) {
			tag = CustomData.CUSTOM_ITEM_LOCKSCREEN;
		} else if (type != null && type.equals("localtheme")) {
			tag = CustomData.CUSTOM_ITEM_LOCAL_THEME;
		} else {
			tag = -1;
		}
		return tag;
	}
	
	public class ViewInfo {
		private String type = null;
	}

}
