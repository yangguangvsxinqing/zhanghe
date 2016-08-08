package com.example.myprojectdemo;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

class FlowAdapter extends
		RecyclerView.Adapter<FlowAdapter.MyViewHolder>
{

	private List<ImageData> mDatas;
	private LayoutInflater mInflater;

	private Context mContext;

	public interface OnItemClickLitener
	{
		void onItemClick(View view, int position);

		void onItemLongClick(View view, int position);
	}

	private OnItemClickLitener mOnItemClickLitener;

	public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
	{
		this.mOnItemClickLitener = mOnItemClickLitener;
	}

	public FlowAdapter(Context context, List<ImageData> datas)
	{
		mInflater = LayoutInflater.from(context);
		mDatas = datas;
		mContext = context;
//		mHeights = new ArrayList<Integer>();
//		for (int i = 0; i < mDatas.size(); i++)
//		{
//			mHeights.add( (int) (100 + Math.random() * 300));
//		}
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		MyViewHolder holder = new MyViewHolder(mInflater.inflate(
				R.layout.item_staggered_home, parent, false));
		return holder;
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position)
	{
		
		if(mDatas.get(position).getImgHeight()>0){
				LayoutParams lp = holder.tv.getLayoutParams();
				lp.height = mDatas.get(position).getImgHeight();
				holder.tv.setLayoutParams(lp);
		}
		if(mContext instanceof MainActivity){
			holder.tv.setBackground(new BitmapDrawable(((MainActivity)mContext).ImageDownload(mDatas.get(position))));
		}
		
		// 如果设置了回调，则设置点击事件
		if (mOnItemClickLitener != null)
		{
			holder.itemView.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					int pos = holder.getPosition();
					mOnItemClickLitener.onItemClick(holder.itemView, pos);
				}
			});

			holder.itemView.setOnLongClickListener(new OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View v)
				{
					int pos = holder.getPosition();
					mOnItemClickLitener.onItemLongClick(holder.itemView, pos);
					removeData(pos);
					return false;
				}
			});
		}
	}

	@Override
	public int getItemCount()
	{
		return mDatas.size();
	}

	public void addData(int position)
	{
//		mDatas.add(position, "Insert One");
//		mHeights.add( (int) (100 + Math.random() * 300));
		notifyItemInserted(position);
	}
	

	public void removeData(int position)
	{
//		mDatas.remove(position);
		notifyItemRemoved(position);
	}

	public class MyViewHolder extends ViewHolder
	{

		ImageView tv;

		public MyViewHolder(View view)
		{
			super(view);
			tv = (ImageView) view.findViewById(R.id.id_num);

		}
	}
}
