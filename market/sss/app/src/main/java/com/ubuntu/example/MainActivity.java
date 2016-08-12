package com.ubuntu.example;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView mRecyclerView;
    private TextView img;
    private boolean animFlag = false;
    private List<WindowData> mList;
    private RecyclerViewAdapter mAdapter;
    private Context mContext;
    private Toolbar mToolbar;
    private FloatingActionButton fab;
    private TextView indexView;
    private int index = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mList = new ArrayList<WindowData>();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Check it", Snackbar.LENGTH_LONG)
                        .setAction("intent", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setClass(mContext,CollapsingToolbarActivity.class);
                                startActivity(intent);
                            }
                        }).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        setListData();
        mAdapter = new RecyclerViewAdapter(mContext,mList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext,3));
        img = (TextView)findViewById(R.id.anim_view);
        img.setVisibility(View.INVISIBLE);

        indexView = (TextView) findViewById(R.id.index);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        StateListDrawable d = new StateListDrawable();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void imgAnim(int viewW, int ViewH, int translateW, int translateH, String text, Drawable drawable){
        Log.v("","sss fab.top="+fab.getTop());
        Log.v("","sss fab.left="+fab.getLeft());
        Log.v("","sss fab.Width="+fab.getMeasuredWidth());
        Log.v("","sss fab.Height="+fab.getMeasuredHeight());
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
        if(!animFlag){
        animFlag = true;
        img.setText(text);
        img.setBackground(drawable);
        RelativeLayout.LayoutParams lytp = new RelativeLayout.LayoutParams(viewW,ViewH);
        img.setLayoutParams(lytp);
//				img.startAnimation(set);
//				img.setVisibility(View.VISIBLE);
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
        img.setVisibility(View.VISIBLE);
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator obj = ObjectAnimator.ofFloat(img, "scaleX", 1f, 0.2f);
        ObjectAnimator obj2 = ObjectAnimator.ofFloat(img, "scaleY", 1f, 0.2f);
        ObjectAnimator obj3 = ObjectAnimator.ofFloat(img, "translationX", translateW - getPaddingWidth(), 500);
        ObjectAnimator obj4 = ObjectAnimator.ofFloat(img, "translationY", translateH - getStatusBarHeight() -
                getPaddingHeight() - mToolbar.getHeight(), 900);
        obj4.setInterpolator(new AccelerateInterpolator(2f));
        animatorSet.play(obj).with(obj2).with(obj3).with(obj4);
        animatorSet.setDuration(1500);
        animatorSet.addListener(new Animator.AnimatorListener() {

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
                img.setVisibility(View.GONE);
                animFlag = false;
                setIndex();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub

            }
        });
        animatorSet.start();
    }
    }
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int getPaddingWidth(){
        int result = getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
        return result;
    }

    private int getPaddingHeight(){
        int result = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        return result;
    }

    private void setIndex(){
        indexView.setText("" + ++index);
    }

    private void setListData(){
        Log.v("","sss setListData");
        WindowData tmp = new WindowData();
        tmp.setWinName("name1");
        tmp.setWinType(0);
        tmp.setWinColor(0);
        mList.add(tmp);

        tmp = new WindowData();
        tmp.setWinTitle1("title1");
        tmp.setWinTitle2("title2");
        tmp.setWinType(1);
        tmp.setWinColor(0);
        mList.add(tmp);

        tmp = new WindowData();
        tmp.setWinTitle1("title3");
        tmp.setWinTitle2("title4");
        tmp.setWinType(1);
        tmp.setWinColor(0);
        mList.add(tmp);

        tmp = new WindowData();
        tmp.setWinName("name2");
        tmp.setWinType(0);
        tmp.setWinColor(1);
        mList.add(tmp);

        tmp = new WindowData();
        tmp.setWinTitle1("title1");
        tmp.setWinTitle2("title2");
        tmp.setWinType(1);
        tmp.setWinColor(1);
        mList.add(tmp);

        tmp = new WindowData();
        tmp.setWinTitle1("title3");
        tmp.setWinTitle2("title4");
        tmp.setWinType(1);
        tmp.setWinColor(1);
        mList.add(tmp);

        tmp = new WindowData();
        tmp.setWinName("name3");
        tmp.setWinType(0);
        tmp.setWinColor(2);
        mList.add(tmp);

        tmp = new WindowData();
        tmp.setWinTitle1("title1");
        tmp.setWinTitle2("title2");
        tmp.setWinType(1);
        tmp.setWinColor(2);
        mList.add(tmp);

        tmp = new WindowData();
        tmp.setWinName("name4");
        tmp.setWinType(0);
        tmp.setWinColor(2);
        mList.add(tmp);
//
//        tmp = new WindowData();
//        tmp.setWinName("name1");
//        tmp.setWinType(0);
//        tmp.setWinColor(0);
//        mList.add(tmp);
//
//        tmp = new WindowData();
//        tmp.setWinTitle1("title1");
//        tmp.setWinTitle2("title2");
//        tmp.setWinType(1);
//        tmp.setWinColor(0);
//        mList.add(tmp);
//
//        tmp = new WindowData();
//        tmp.setWinTitle1("title3");
//        tmp.setWinTitle2("title4");
//        tmp.setWinType(1);
//        tmp.setWinColor(0);
//        mList.add(tmp);
//
//        tmp.setWinName("name1");
//        tmp.setWinType(0);
//        tmp.setWinColor(0);
//        mList.add(tmp);
//
//        tmp = new WindowData();
//        tmp.setWinTitle1("title1");
//        tmp.setWinTitle2("title2");
//        tmp.setWinType(1);
//        tmp.setWinColor(0);
//        mList.add(tmp);
//
//        tmp = new WindowData();
//        tmp.setWinTitle1("title3");
//        tmp.setWinTitle2("title4");
//        tmp.setWinType(1);
//        tmp.setWinColor(0);
//        mList.add(tmp);
//
//        tmp.setWinName("name1");
//        tmp.setWinType(0);
//        tmp.setWinColor(0);
//        mList.add(tmp);
//
//        tmp = new WindowData();
//        tmp.setWinTitle1("title1");
//        tmp.setWinTitle2("title2");
//        tmp.setWinType(1);
//        tmp.setWinColor(0);
//        mList.add(tmp);
//
//        tmp = new WindowData();
//        tmp.setWinTitle1("title3");
//        tmp.setWinTitle2("title4");
//        tmp.setWinType(1);
//        tmp.setWinColor(0);
//        mList.add(tmp);
    }
}
