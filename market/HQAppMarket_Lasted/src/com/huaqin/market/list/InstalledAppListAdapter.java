package com.huaqin.market.list;

import java.util.ArrayList;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huaqin.market.R;
import com.huaqin.market.utils.PackageUtil;

public class InstalledAppListAdapter extends ArrayAdapter<PackageInfo> {
	
	private Context mContext;
	
	private LayoutInflater mLayoutInflater;
	private View.OnClickListener mOnClickListener;
	
	public InstalledAppListAdapter(Context context, ArrayList<PackageInfo> objects) {
		
		super(context, 0, objects);
		
		mContext = context;
		
		mLayoutInflater = 
			(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mOnClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()) {
				case R.id.app_listitem_operate:
					onOperateClick(v);
					break;
				case R.id.app_listitem_group:
					Log.i("InstalledAppListAdapter", "###InstalledAppListAdapter");
					break;
				default:
					break;
				}
			}
		};
	}

	private void onOperateClick(View v) {
		// TODO Auto-generated method stub
		PackageInfo pkgInfo = (PackageInfo) v.getTag();
		
		PackageUtil.uninstallPackage(mContext, pkgInfo.packageName);
	}
	

	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return super.getItemId(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		PackageManager pkgManager = mContext.getPackageManager();
		PackageInfo pkgInfo = getItem(position);
		ViewHolder viewHolder = null;
		
		if (convertView == null || 
				!(convertView.getTag() instanceof ViewHolder)) {
			convertView = mLayoutInflater.inflate(R.layout.app_list_installed_item, null);
			viewHolder = new ViewHolder();
			
			viewHolder.mParent = convertView;
			viewHolder.mListItem =
				(RelativeLayout) convertView.findViewById(R.id.app_listitem_group);
			viewHolder.mListItem.setOnClickListener(mOnClickListener);
			viewHolder.mListItemGroupAll = 
				(RelativeLayout) convertView.findViewById(R.id.app_listitem_installed);
			viewHolder.mOperate =
				(ImageButton) convertView.findViewById(R.id.app_listitem_operate);
			
			viewHolder.mIcon =
				(ImageView) convertView.findViewById(R.id.app_listitem_icon);
			viewHolder.mName =
				(TextView) convertView.findViewById(R.id.app_listitem_name);
			viewHolder.mCurrentVersion =
				(TextView) convertView.findViewById(R.id.app_listitem_currentversion);
			viewHolder.mUpdateVersion =
				(TextView) convertView.findViewById(R.id.app_listitem_updateversion);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
//		if ((position % 2) == 0) {
//			viewHolder.mListItemGroupAll.setBackgroundResource(R.drawable.bg_list_item_view);
//		} else {
//			viewHolder.mListItemGroupAll.setBackgroundResource(R.drawable.bg_list_item_grey);
//		}
		
		viewHolder.mOperate.setTag(pkgInfo);
		viewHolder.mOperate.setOnClickListener(mOnClickListener);
		viewHolder.mIcon.setImageDrawable(pkgInfo.applicationInfo.loadIcon(pkgManager));
		viewHolder.mName.setText(pkgInfo.applicationInfo.loadLabel(pkgManager));
		viewHolder.mCurrentVersion.setText(
				mContext.getString(R.string.app_current_version) + pkgInfo.versionName);
		
		return convertView;
	}
	
	class ViewHolder {
		
		View mParent;
		ImageButton mOperate;
		ImageView mIcon;
		TextView mName;
		TextView mCurrentVersion;
		TextView mUpdateVersion;
		
		RelativeLayout mListItem;
		RelativeLayout mListItemGroupAll;
	}
}