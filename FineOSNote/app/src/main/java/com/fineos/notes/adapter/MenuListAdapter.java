package com.fineos.notes.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

import com.fineos.notes.R;
import com.fineos.notes.constant.Constant;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MenuListAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater; 
	private List<HashMap<String, Object>> listItems;
    private int selectedPosition;
	
	public MenuListAdapter(Context context,List<HashMap<String, Object>> listItems) {
		this.context = context;
		this.listItems = listItems;
		inflater = LayoutInflater.from(context);
	}

	static class ViewHolder {
		RelativeLayout itemLayout;
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
		return listItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			Log.i("dpc", "convertView----11111");
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.slidingmenu_item, null);
			// 获取控件
			holder.itemLayout = (RelativeLayout) convertView.findViewById(R.id.item_layout);
			holder.itemIcon = (ImageView) (convertView)
					.findViewById(R.id.itemIcon);
			holder.itemName = (TextView) (convertView)
					.findViewById(R.id.itemName);
            holder.itemLayout.setBackgroundResource(R.drawable.group_choose_green);
			// 将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
			convertView.setTag(holder);
		} else {
			Log.i("dpc", "convertView----22222");
			holder = (ViewHolder) convertView.getTag();
		}

        if(position == selectedPosition && selectedPosition == 0){
            holder.itemLayout.setBackgroundResource(R.drawable.group_choose_green);
        }
//        //tempt remove callrecode
//        else if(position == selectedPosition && selectedPosition == 1){
//            holder.itemLayout.setBackgroundResource(R.drawable.group_choose_violet);
//        }
        else if(position == selectedPosition ){
            holder.itemLayout.setBackgroundResource(R.drawable.group_choose_blue);
        }else{
            holder.itemLayout.setBackgroundColor(Color.TRANSPARENT);
        }
		holder.itemIcon.setBackgroundResource((Integer) listItems.get(position)
				.get("icon"));
		holder.itemName.setText(listItems.get(position).get("folderName")
				.toString());
		Log.w(Constant.TAG, "getView" +convertView);
		return convertView;
	}
}
