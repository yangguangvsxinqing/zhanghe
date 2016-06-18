package com.example.volley;

import com.example.volley.HscrollRecyclerView.OnItemScrollChangeListener;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class HscrollViewActivity extends Activity{
	private ImageView img;
	private HscrollRecyclerView mHscrollRecyclerView;
	private HscrollViewAdapter mAdapter;
	private LinearLayoutManager mLinearLayoutManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hscroll);
		img = (ImageView)findViewById(R.id.img);
		mHscrollRecyclerView = (HscrollRecyclerView)findViewById(R.id.mView);
		mLinearLayoutManager = new LinearLayoutManager(this);
		mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		mHscrollRecyclerView.setLayoutManager(mLinearLayoutManager);
		mAdapter = new HscrollViewAdapter(this);
		mHscrollRecyclerView.setAdapter(mAdapter);
		mHscrollRecyclerView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView,
					int newState) {
				// TODO Auto-generated method stub
				super.onScrollStateChanged(mHscrollRecyclerView, newState);
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				// TODO Auto-generated method stub
				super.onScrolled(mHscrollRecyclerView, dx, dy);
				View newView = mHscrollRecyclerView.getChildAt(0);

				if (mHscrollRecyclerView.mItemScrollChangeListener != null)
				{
					if (newView != null && newView != mHscrollRecyclerView.mCurrentView)
					{
						mHscrollRecyclerView.mCurrentView = newView ;
						mHscrollRecyclerView.mItemScrollChangeListener.onChange(mHscrollRecyclerView.mCurrentView,
								mHscrollRecyclerView.getChildPosition(mHscrollRecyclerView.mCurrentView));

					}
				}
			}
			
		});
		mHscrollRecyclerView.setOnItemScrollChangeListener(new OnItemScrollChangeListener(){

			@Override
			public void onChange(View view, int position) {
				// TODO Auto-generated method stub
				Log.v("", "sss onChange position="+position);
				if(position>=0){
					img.setImageBitmap(MainActivity.mCache.get(MainActivity.mList.get(position).getImgUrl()));
				}
			}});
	}
	
}