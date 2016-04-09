package com.fineos.notes.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fineos.notes.R;
import com.fineos.notes.constant.Constant;

import java.util.HashMap;
import java.util.List;

public class MoveListAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<HashMap<String, Object>> listItems;
	private int selectedPosition;
	public MoveListAdapter(Context context, List<HashMap<String, Object>> listItems) {
		this.context = context;
		this.listItems = listItems;
		inflater = LayoutInflater.from(context);
	}

	static class ViewHolder {
		LinearLayout itemLayout;
		ImageView itemIcon;
		TextView itemName;
	}

	public void setSelectedPosition(int position) {
		selectedPosition = position;
	}


	@Override
	public int getCount() {
		return listItems.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return listItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.move_menu_item, null);
			//获取控件
			holder.itemLayout =  (LinearLayout) (convertView).findViewById(R.id.item_layout);
			holder.itemName = (TextView) (convertView).findViewById(R.id.itemName);
			//将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		Log.w(Constant.TAG, "MoveListAdapter listItems:" + listItems);
		Log.w(Constant.TAG, "MoveListAdapter folderName:" + (String) listItems.get(position).get("folderName"));
		if (position != listItems.size()-1) {
			holder.itemName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}else {
			holder.itemName.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.add_icon), null, null, null);
		}

		holder.itemName.setText((String)listItems.get(position).get("folderName"));
		return convertView;
	}
}
