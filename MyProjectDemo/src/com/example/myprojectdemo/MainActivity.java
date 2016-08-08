package com.example.myprojectdemo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.example.myprojectdemo.FlowAdapter.OnItemClickLitener;

public class MainActivity extends Activity {
	private RequestQueue mQueue;
	public static String RESOURCE_ROOT_URL_TEST = "http://weather.huaqin.com:8080/fineos-rom-api/service/apkupgrade";
	private RecyclerView mRecyclerView;
	public static List<ImageData> mList;
	private FlowAdapter mAdapter;
	public static LruCache<String,Bitmap> mCache;
	private int screenWidth;
//	private TestImageView img;
	private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
	private boolean flag = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mList = new ArrayList<ImageData>();
		setImgData();
		mQueue = Volley.newRequestQueue(this);  
		ImageLoader();
		mAdapter = new FlowAdapter(this, mList);
		mRecyclerView = (RecyclerView)findViewById(R.id.id_recyclerview);
//		img = (TestImageView)findViewById(R.id.img_large);
		//设置布局管理器
		mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
		mRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);
		//设置adapter
		mRecyclerView.setAdapter(mAdapter);
		mAdapter.setOnItemClickLitener(new OnItemClickLitener()
		{
			@Override
			public void onItemClick(View view, int position)
			{
//				Toast.makeText(MainActivity.this,
//						position + " click", Toast.LENGTH_SHORT).show();
//				img.setImageBitmap(mCache.get(mList.get(position).getImgUrl()));
//				img.setVisibility(View.VISIBLE);
//				flag = true;
//				Intent intent = new Intent();
//				intent.setClass(MainActivity.this, LargeImageViewActivity.class);
//				startActivity(intent);
			}

			public void onItemLongClick(View view, int position)
			{
				Toast.makeText(MainActivity.this,
						position + " long click", Toast.LENGTH_SHORT).show();
//				Intent intent = new Intent();
//				intent.setClass(MainActivity.this, HscrollViewActivity.class);
//				startActivity(intent);
			}
		});
		//设置Item增加、移除动画
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);

		screenWidth = wm.getDefaultDisplay().getWidth();
		requestTest(mList.get(0).getImgUrl(),mList.get(0).getImgId());
	}
	
	public Bitmap ImageDownload(ImageData imageData){
		Bitmap btm = null;
		btm = mCache.get(imageData.getImgUrl());
		if(mCache.get(imageData.getImgUrl())==null){
			requestTest(imageData.getImgUrl(),imageData.getImgId());
		}
		return btm;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 
        if (keyCode == KeyEvent.KEYCODE_BACK
                  && event.getRepeatCount() == 0) {
             //do something...
        	if(flag){
//        		img.setVisibility(View.GONE);
        		flag = false;
        	}else{
        		finish();
        	}
              return true;
          }
          return super.onKeyDown(keyCode, event);
      }
	
	private void ImageLoader() {  
        // 获取应用程序最大可用内存  
        int maxMemory = (int) Runtime.getRuntime().maxMemory();  
        int cacheSize = maxMemory / 2;  
        // 设置图片缓存大小为程序最大可用内存的1/8  
        mCache = new LruCache<String, Bitmap>(cacheSize) {  
            @Override  
            protected int sizeOf(String key, Bitmap bitmap) {  
                return bitmap.getByteCount();  
            }  
        };  
    }  
	
	private void setImgData(){
		for(int i=0;i<imageUrls.length;i++){
			ImageData tmp = new ImageData();
			tmp.setImgId(i);
			tmp.setImgUrl(imageUrls[i]);
			mList.add(tmp);
		}
	}
	
	private void requestTest(final String url,final int id){
		
		ImageRequest imageRequest = new ImageRequest(url, 
				new Listener<Bitmap>(){
					@Override
					public void onResponse(Bitmap response) {
						// TODO Auto-generated method stub
						if (response != null) {
							mCache.put(url, response);	
							int btmW = response.getWidth();
							int btmH = response.getHeight();
							int inSampleSize = btmW*3/(screenWidth-50);
							mList.get(id).setImgHeight(btmH/inSampleSize);
							
							mAdapter.notifyItemChanged(id);
						}
					}}, 
				0, 0, Config.ARGB_8888, 
				new ErrorListener(){
					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						
					}});
		mQueue.add(imageRequest);
	}
	public final static String[] imageUrls = new String[] {  
        "http://img.my.csdn.net/uploads/201309/01/1378037235_3453.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037235_7476.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037235_9280.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037234_3539.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037234_6318.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037194_2965.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037193_1687.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037193_1286.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037192_8379.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037178_9374.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037177_1254.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037177_6203.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037152_6352.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037151_9565.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037151_7904.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037148_7104.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037129_8825.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037128_5291.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037128_3531.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037127_1085.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037095_7515.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037094_8001.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037093_7168.jpg",  
        "http://img.my.csdn.net/uploads/201309/01/1378037091_4950.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949643_6410.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949642_6939.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949630_4505.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949630_4593.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949629_7309.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949629_8247.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949615_1986.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949614_8482.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949614_3743.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949614_4199.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949599_3416.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949599_5269.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949598_7858.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949598_9982.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949578_2770.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949578_8744.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949577_5210.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949577_1998.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949482_8813.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949481_6577.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949480_4490.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949455_6792.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949455_6345.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949442_4553.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949441_8987.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949441_5454.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949454_6367.jpg",  
        "http://img.my.csdn.net/uploads/201308/31/1377949442_4562.jpg" };
}
