package com.fineos.theme.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fineos.theme.R;
import com.fineos.theme.model.CustomData;

public class CustomAdapter extends ArrayAdapter<CustomData> {

	private LayoutInflater mInflater;
	private OnClickListener mOnClickListener;
	private Context mContext;
	private int mResourceId;

	public CustomAdapter(Context context, ArrayList<CustomData> objects, OnClickListener listener, int resourceId) {

		super(context, 0, objects);
		// TODO Auto-generated constructor stub
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mOnClickListener = listener;
		mResourceId = resourceId;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return ((long) getItem(position).getCustomId());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		CustomData customData = getItem(position);
		ViewHolder viewHolder = null;

		if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
			convertView = mInflater.inflate(mResourceId, parent, false);
			viewHolder = new ViewHolder();

			viewHolder.mListItem = (RelativeLayout) convertView.findViewById(R.id.custom_item);
			viewHolder.mCategotyThumb = (ImageView) convertView.findViewById(R.id.custom_img);
			viewHolder.mCategotyTitle = (TextView) convertView.findViewById(R.id.custom_title);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Drawable drawable = mContext.getResources().getDrawable(customData.getImgId());
		viewHolder.mCategotyThumb.setTag(Integer.valueOf(customData.getCustomId()));
		viewHolder.mCategotyThumb.setImageDrawable(drawable);

		viewHolder.mCategotyTitle.setText(customData.getTitle());
		viewHolder.mListItem.setTag(customData.getCustomId());
		viewHolder.mListItem.setOnClickListener(mOnClickListener);

		return convertView;
	}

	class ViewHolder {
		RelativeLayout mListItem;
		ImageView mCategotyThumb;
		TextView mCategotyTitle;
	}
}
