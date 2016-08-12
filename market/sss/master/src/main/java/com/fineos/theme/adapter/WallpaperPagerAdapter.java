package com.fineos.theme.adapter;


import java.util.ArrayList;
import java.util.List;

import com.fineos.theme.activity.ThemeDetailBaseNewActivity;
import com.fineos.theme.jazzyviewpager.JazzyViewPager;
import com.fineos.theme.model.ThemeData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.fineos.theme.R;
import com.fineos.theme.ThemeDataCache;

public class WallpaperPagerAdapter extends PagerAdapter{

	private List<ThemeData> mThemedata;
	private Context mContext;
	private int mMixType;
	private JazzyViewPager mJazzyViewPager;
	
	public WallpaperPagerAdapter(List<ThemeData> mThemeListSave, Context context, int mixType, JazzyViewPager jazzyViewPager) {
		mThemedata = new ArrayList<ThemeData>(mThemeListSave);
		mContext = context;
		mMixType = mixType;
		mJazzyViewPager = jazzyViewPager;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mThemedata.size();
	}
	
	@Override
	public int getItemPosition(Object object) {
		// TODO Auto-generated method stub
		return POSITION_NONE;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		View view = mJazzyViewPager.findViewFromObject(position);
		container.removeView(view);
		if (view != null) {
			mJazzyViewPager.removeViewFromObject(position);
		}
	}

    @Override  
    public Object instantiateItem(ViewGroup container, int position) {
    	ViewGroup contentView = (ViewGroup) View.inflate(mContext, R.layout.wallpaperitem, null);
    	ImageView iView = (ImageView)contentView.findViewById(R.id.themeimage);
//    	ProgressBar pb = (ProgressBar)contentView.findViewById(R.id.progressbar);
    	ImageView imageView = (ImageView)contentView.findViewById(R.id.progressimage);
		AnimationDrawable ad = (AnimationDrawable)imageView.getBackground();
    	ThemeData themeData = mThemedata.get(position);
    	if (themeData.getDownloadUrl() != null) {
    		Drawable drawable = ((ThemeDetailBaseNewActivity)(mContext)).getPreviewDrawable(themeData);
        	if (drawable != null) {
        		imageView.setVisibility(View.INVISIBLE);
        		iView.setImageDrawable(drawable);
        		ad.stop();
        	} else {
        		imageView.setVisibility(View.VISIBLE);
        		ad.start();
        		Drawable thumbsDrawble = ThemeDataCache.getThumbnail(mContext, Integer.toString(themeData.getId()), mMixType);
        		if (thumbsDrawble != null) {
        			iView.setImageDrawable(thumbsDrawble);
        		}
        	}
    	} else {
    		imageView.setVisibility(View.INVISIBLE);
    		ad.stop();
    		Drawable d = new BitmapDrawable(null, ThemeDataCache.getLocalPreviewDrawable(mContext, themeData.getTitle(), themeData, mMixType));
    		iView.setImageDrawable(d);
    	}
    	mJazzyViewPager.setObjectForPosition(contentView, position);
    	container.addView(contentView);
        return contentView;  
    }

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == (arg1);
	}
	
	public void replaceThemeData(ArrayList<ThemeData> list) {
//		for (ThemeData listthemedata : list) {
//			boolean isexist = false;
//			for (ThemeData themedata : mThemedata) {
//				if (listthemedata.getId() == themedata.getId()) {
//					isexist = true;
//					break;
//				}
//			}
//			if (!isexist) {
//				mThemedata.add(listthemedata);
//			}
//		}
		mThemedata.clear();
		mThemedata.addAll(list);
		notifyDataSetChanged();
	}

	
}
