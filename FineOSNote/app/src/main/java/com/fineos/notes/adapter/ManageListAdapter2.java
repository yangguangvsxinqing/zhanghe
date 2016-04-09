package com.fineos.notes.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fineos.notes.R;
import com.fineos.notes.bean.Folder;
import com.fineos.notes.constant.Constant;
import com.fineos.notes.db.FolderDao;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ManageListAdapter2 extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater; 
	private List<HashMap<String, Object>> listItems;
	private ArrayList<Integer> idList = new ArrayList<Integer>();
	public ManageListAdapter2(Context context,List<HashMap<String, Object>> listItems) {
		this.context = context;
		this.listItems = listItems;

		inflater = LayoutInflater.from(context);
	}

	static class ViewHolder {
		RelativeLayout itemLayout;
		CheckBox itemIcon;
		TextView itemName;
	}
	
	public ArrayList<Integer> getChooseID(){
		return idList;
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
		if(convertView == null){
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.manage_delete_item, null);
			//获取控件
			holder.itemLayout =  (RelativeLayout) (convertView).findViewById(R.id.item_layout);
			holder.itemIcon = (CheckBox) (convertView).findViewById(R.id.itemIcon);
			holder.itemName = (TextView) (convertView).findViewById(R.id.itemName);
			//将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		String folderName = (String) listItems.get(position).get("folderName");
			if(!folderName.equals(Constant.ADD_FOLDER)){
			if (folderName.equals(Constant.All_NOTE) || folderName.equals(Constant.CALL_RECORDING)) {
				holder.itemIcon.setVisibility(View.GONE);
			}
				holder.itemName.setTextColor(Color.BLACK);
				holder.itemName.setText(folderName);
				
				holder.itemName.setOnClickListener(new ItemNameListener(position,folderName));
				holder.itemIcon.setOnCheckedChangeListener(new ItemIconListener(position));
			}
			
		
		return convertView;
	}
	
	class ItemNameListener implements OnClickListener{
		private int position;
		private String oldFolderName;
		public  ItemNameListener(int position,String oldFolderName) {
			this.position = position;
			this.oldFolderName = oldFolderName;
		}
		@Override
		public void onClick(View v) {
//			CommonUtil.addFolder(context, listItems, null);
			addFolder(context,position,oldFolderName, listItems, ManageListAdapter2.this);
			
		}
		
	}
	
	class ItemIconListener implements OnCheckedChangeListener{
		private int position;
		public  ItemIconListener(int position) {
			this.position = position;
		}
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Log.w(Constant.TAG, "onCheckedChanged :"  + "isChecked :" + isChecked + "position :" + position);
			if (isChecked && (!idList.contains(position))) {
                idList.add(position);
            } else {
                if (idList.contains(position)) {
                    idList.remove(position);
                }
            }
		}
		
	}
	public static  void addFolder(final Context context,final int position,final String oldFolderName,final List<HashMap<String, Object>> list,
			final ManageListAdapter2 adapter) {
		final HashMap<String, Object> map = new HashMap<String, Object>();
		final EditText editText = new EditText(context);

		new AlertDialog.Builder(context).setTitle("请输入")
				.setIcon(android.R.drawable.ic_dialog_info).setView(editText)
				.setCancelable(false)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(!editText.getText().toString().isEmpty()){
							String newFolderName = editText.getText().toString();
							map.put("folderName", newFolderName);
							list.set(position, map);
							FolderDao folderDao = new FolderDao(context);
							Folder folder = new Folder();
							folder.setFolder(newFolderName);
							folderDao.updateFolder(oldFolderName,newFolderName);
							adapter.notifyDataSetChanged();
							dialog.dismiss();
						}else{
							Toast.makeText(context, context.getString(R.string.toast), Toast.LENGTH_SHORT).show();
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}
}
