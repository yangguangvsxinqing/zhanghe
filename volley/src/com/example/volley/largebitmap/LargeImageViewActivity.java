package com.example.volley.largebitmap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.example.volley.R;
import com.example.volley.customtextview.CustomTextViewTestActivity;
import com.example.volley.customview.CustomTestActivity;

public class LargeImageViewActivity extends Activity
{
    private LargeImageView mLargeImageView;
    private RecyclerView mRecyclerView;
    private List<WindowData> mList;
    private WindowViewAdapter mAdapter;
    public static TextView img;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_large_image_view);
        mList = new ArrayList<WindowData>();
        mLargeImageView = (LargeImageView) findViewById(R.id.id_largetImageview);
        try
        {
            InputStream inputStream = getAssets().open("qm.jpg");
            mLargeImageView.setInputStream(inputStream);

        } catch (IOException e)
        {
            e.printStackTrace();
        }
        mRecyclerView = (RecyclerView)findViewById(R.id.test_view);
        setListData();
        mAdapter = new WindowViewAdapter(this,mList,getStatusBarHeight());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        img = (TextView)findViewById(R.id.anim_view);
        img.setVisibility(View.INVISIBLE);
        Button btn = (Button)findViewById(R.id.test);
        btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(LargeImageViewActivity.this,CustomTextViewTestActivity.class);
				startActivity(intent);
			}
		});
        
      //初始化一个空对象  
        StateListDrawable stalistDrawable = new StateListDrawable();  
        //获取对应的属性值 Android框架自带的属性 attr  
        int pressed = android.R.attr.state_pressed;  
        int window_focused = android.R.attr.state_window_focused;  
        int focused = android.R.attr.state_focused;  
        int selected = android.R.attr.state_selected;  
          
        stalistDrawable.addState(new int []{pressed , window_focused}, getResources().getDrawable(R.drawable.pic1));  
        stalistDrawable.addState(new int []{android.R.attr.enabled}, getResources().getDrawable(R.drawable.pic2));  
        stalistDrawable.addState(new int []{selected }, getResources().getDrawable(R.drawable.pic3));  
        stalistDrawable.addState(new int []{focused }, getResources().getDrawable(R.drawable.pic4));  
        //没有任何状态时显示的图片，我们给它设置我空集合  
        stalistDrawable.addState(new int []{}, getResources().getDrawable(R.drawable.pic5)); 
        
        btn.setBackground(stalistDrawable);
    }
    
    public int getStatusBarHeight() {
    	  int result = 0;
    	  int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
    	  if (resourceId > 0) {
    	      result = getResources().getDimensionPixelSize(resourceId);
    	  }
    	  return result;
    	}
    
    private void setListData(){
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
    }
}
