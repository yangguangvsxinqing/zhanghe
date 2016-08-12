package com.ubuntu.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.ui.CustomTextView;
import com.example.ui.ListFragment;
import com.example.ui.TabFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ubuntu on 16-7-11.
 */
public class CustomTextActivity extends AppCompatActivity{

    private String[] mTitles = new String[] { "test1", "test2", "test3" };
    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mFragments;
    private List<CustomTextView> mTabs = new ArrayList<CustomTextView>();
    private Toolbar mToolbar;
    public static final String TAG = "CustomTextActivity";
    HashMap<String,String> map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customtext);
        initView();
        initDatas();
    }

    private void initView(){
        mFragments = new ArrayList<Fragment>();
        mViewPager = (ViewPager)findViewById(R.id.id_viewpager);
        mTabs.add((CustomTextView)findViewById(R.id.test1));
        mTabs.add((CustomTextView)findViewById(R.id.test2));
        mTabs.add((CustomTextView)findViewById(R.id.test3));
        mTabs.get(0).setTextColor(0xffff0000);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(0xffffffff);
        mToolbar.setBackgroundColor(0xff66ccff);
        mToolbar.setTitle("asd");
        mToolbar.setNavigationIcon(R.drawable.ic_ab_back_holo_dark_am_normal);
        setSupportActionBar(mToolbar);
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG,"sss Navigation onClick");
                Intent intent = new Intent(CustomTextActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


        StatusBarCompat.compat(this, 0xff66ccff);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @SuppressWarnings("deprecation")
    private void initDatas()
    {

//        for (int i = 0; i < mTitles.length; i++)
//        {
            mFragments.add((TabFragment) TabFragment.newInstance(mTitles[0]));
            mFragments.add((ListFragment) ListFragment.newInstance());
            mFragments.add((TabFragment) TabFragment.newInstance(mTitles[2]));
//        }

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
                return mFragments.get(position);
            }

        };

        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(0);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
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
