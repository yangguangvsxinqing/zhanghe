package com.fineos.fileexplorer.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.util.Common;

/**
 * Created by xiaoyue on 14-10-13.
 * This is the single category item on main activity/page.
 */
public class CategoryItemView extends RelativeLayout {


    private String mCategoryID;
    private TextView mCategoryName;
    private String mCategoryNameString;
    private Drawable mCategoryIcon;
    private TextView mCategoryCount;


    public CategoryItemView(Context context) {
        super(context);
    }

    public CategoryItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getProperties(context, attrs);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setBackgroundColor(getResources().getColor(android.R.color.transparent));
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.item_category, this, true);
    }

    private void getProperties(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.MainPageCategory);
        CharSequence s = a.getString(R.styleable.MainPageCategory_category_name);
        mCategoryIcon = a.getDrawable(R.styleable.MainPageCategory_category_icon);
        CharSequence s2 = a.getString(R.styleable.MainPageCategory_category_id);

        if (s != null) {
            mCategoryNameString = s.toString();
        }
        if (s2 != null) {
            mCategoryID = s2.toString();
        }
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCategoryName = (TextView) this.findViewById(R.id.txt_cate_name);
        Log.d("CategoryItemView", "category name : " + mCategoryNameString);
        mCategoryName.setText(mCategoryNameString);
//        mCategoryName.setTextSize(getResources().getDimensionPixelSize(R.dimen.category_name_size));
        mCategoryName.setCompoundDrawablesWithIntrinsicBounds(null, mCategoryIcon, null, null);
        mCategoryCount = (TextView) findViewById(R.id.txt_cate_count);
    }

    public void setCategoryCount(long countNum) {
        if (mCategoryCount != null) {
            mCategoryCount.setText(String.format(getResources().getString(R.string.file_count), Long.toString(countNum)));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int deviceWidth = Common.getDeviceWidth(getContext());
        super.onMeasure(MeasureSpec.makeMeasureSpec(deviceWidth / 3, MeasureSpec.EXACTLY), heightMeasureSpec);
    }

    public String getmCategoryID() {
        return mCategoryID;
    }
}
