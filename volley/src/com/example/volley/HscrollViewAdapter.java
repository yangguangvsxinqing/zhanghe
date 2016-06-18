package com.example.volley;
import com.example.volley.FlowAdapter.MyViewHolder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class HscrollViewAdapter extends RecyclerView.Adapter<HscrollViewAdapter.MyViewHolder>{
		private LayoutInflater mInflater;
		public HscrollViewAdapter(Context context){
			mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getItemCount() {
			// TODO Auto-generated method stub
			return MainActivity.mList.size();
		}

		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			// TODO Auto-generated method stub
			MyViewHolder holder = new MyViewHolder(mInflater.inflate(
					R.layout.hscroll_item, parent, false));
			
			return holder;
		}
		
		public class MyViewHolder extends ViewHolder{
			public ImageView itemView;

			public MyViewHolder(View view) {
				super(view);
				// TODO Auto-generated constructor stub
				itemView = (ImageView)(view.findViewById(R.id.img));
			}
			
		}

		@Override
		public void onBindViewHolder(MyViewHolder viewHolder, int position) {
			// TODO Auto-generated method stub
			viewHolder.itemView.setImageBitmap(MainActivity.mCache.get(MainActivity.mList.get(position).getImgUrl()));
		}
	}