package com.huaqin.market.list;

import java.util.ArrayList;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huaqin.android.market.sdk.bean.Application;
import com.huaqin.market.R;
import com.huaqin.market.utils.PackageUtil;

public class UpgradeAppListAdapter extends ArrayAdapter<Application> {
		
	private Context mContext;
	
	private LayoutInflater mLayoutInflater;
	
	public UpgradeAppListAdapter(Context context, ArrayList<Application> objects) {
		
		super(context, 0, objects);
		
		mContext = context;
		
		mLayoutInflater = 
			(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return getItem(position).getAppId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		PackageManager pkgManager = mContext.getPackageManager();
		Application appInfo = getItem(position);
		PackageInfo pkgInfo = PackageUtil.getPackageInfo(pkgManager, appInfo.getAppPackage());
		ViewHolder viewHolder = null;
		
		if (convertView == null || 
				!(convertView.getTag() instanceof ViewHolder)) {
			convertView = mLayoutInflater.inflate(R.layout.app_list_update_item, null);
			viewHolder = new ViewHolder();
			
			viewHolder.mListItem =
				(RelativeLayout) convertView.findViewById(R.id.app_listitem_group);
			viewHolder.mListItemGroupAll = 
				(RelativeLayout) convertView.findViewById(R.id.app_listitem_upgrade);
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
		
//		viewHolder.mListItem.setTag(appInfo.getAppId());
		viewHolder.mListItem.setTag(appInfo);
		viewHolder.mListItem.setOnClickListener((View.OnClickListener)mContext);
		viewHolder.mOperate.setTag(Integer.valueOf(position));
		viewHolder.mOperate.setOnClickListener((View.OnClickListener)mContext);		
		
/****************begin,modified by daniel_whj,2012-02-13,for bug HQ00079989******************/		
		/*
		 when entering "upgradeapplist" activity,the function "onresume" execute "initListData",but 
		 not handle msg to update installed packeges infomation in time,so remain last list in here codes.
		*/		
	
		if(pkgInfo!=null)
		{
			viewHolder.mIcon.setImageDrawable(pkgInfo.applicationInfo.loadIcon(pkgManager));
			
			viewHolder.mName.setText(pkgInfo.applicationInfo.loadLabel(pkgManager));
			viewHolder.mCurrentVersion.setText(
					mContext.getString(R.string.app_current_version) + 
						(pkgInfo.versionName == null ? "" : pkgInfo.versionName));
		}		
		
/****************end,modified by daniel_whj,2012-02-06,for bug HQ00079989&HQ00079704&HQ00079109******************/

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