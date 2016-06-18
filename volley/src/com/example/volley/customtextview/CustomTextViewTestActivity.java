package com.example.volley.customtextview;

import java.util.ArrayList;
import java.util.List;

import com.example.volley.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

public class CustomTextViewTestActivity extends FragmentActivity{
	private String[] mTitles = new String[] { "test1", "test2", "test3" };  
	private ViewPager mViewPager;
	private FragmentPagerAdapter mAdapter;
	private TabFragment[] mFragments = new TabFragment[mTitles.length];  
    private List<CustomTextView> mTabs = new ArrayList<CustomTextView>();  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text_test);
		initView();
		initDatas();
	}
	
	private void initView(){
		mViewPager = (ViewPager)findViewById(R.id.id_viewpager);
		mTabs.add((CustomTextView)findViewById(R.id.test1));
		mTabs.add((CustomTextView)findViewById(R.id.test2));
		mTabs.add((CustomTextView)findViewById(R.id.test3));
		mTabs.get(0).setTextColor(0xffff0000);
	}
	
	@SuppressWarnings("deprecation")
	private void initDatas()  
    {  
  
        for (int i = 0; i < mTitles.length; i++)  
        {  
            mFragments[i] = (TabFragment) TabFragment.newInstance(mTitles[i]);  
        }  
  
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager())  
        {  
            @Override  
            public int getCount()  
            {  
                return mTitles.length;  
            }  
  
            @Override  
            public Fragment getItem(int position)  
            {  
                return mFragments[position];  
            }  
  
        };  
  
        mViewPager.setAdapter(mAdapter);  
        mViewPager.setCurrentItem(0); 
        
        mViewPager.setOnPageChangeListener(new OnPageChangeListener()  
        {  
            @Override  
            public void onPageSelected(int position)  
            {  
            	Log.v("", "sss onPageSelected position="+position);
            }  
  
            @Override  
            public void onPageScrolled(int position, float positionOffset,  
                    int positionOffsetPixels)  
            {  
                if (positionOffset > 0)    
                {    
                	CustomTextView left = mTabs.get(position);    
                	CustomTextView right = mTabs.get(position + 1);    
                      
                    left.setDirection(1);  
                    right.setDirection(0);  
                    Log.e("TAG", positionOffset+"");  
                    left.setProgress( 1-positionOffset);    
                    right.setProgress(positionOffset);    
                }    
            }  
  
            @Override  
            public void onPageScrollStateChanged(int state)  
            {  
            	Log.v("", "sss onPageScrollStateChanged state="+state);
            }  
        });  
    } 
	
}