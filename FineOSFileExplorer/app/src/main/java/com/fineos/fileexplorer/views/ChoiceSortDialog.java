package com.fineos.fileexplorer.views;

import fineos.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.fineos.fileexplorer.R;

import java.util.ArrayList;

public class ChoiceSortDialog extends AlertDialog {
	
	private Context mContext;
	private OnItemClickListener mOnSortListClick;
	private static ArrayList<String> sortData;
    private boolean mIsCategoryView;
	
	public ChoiceSortDialog(Context context,boolean isCategoryView, OnItemClickListener onSortListClick) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		mOnSortListClick = onSortListClick;
        mIsCategoryView = isCategoryView;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
	    View mView=getLayoutInflater().inflate(R.layout.sortchoose, null);
		ListView list =(ListView)mView.findViewById(R.id.sort_setting);
    	list.setAdapter(new ArrayAdapter<String>(mContext,
    			R.layout.choose_sort_resource,
    			getSortData(mContext)));
    	list.setOnItemClickListener(mOnSortListClick);
		this.setTitle(R.string.sort);
		//this.setButton(an, null);
		setView(mView);
		super.onCreate(savedInstanceState);
	}
	
    public synchronized ArrayList<String> getSortData(Context context){
    	if(sortData == null){
    		sortData = new ArrayList<String>();
    		sortData.add(context.getString(R.string.sort_by_date));//if you modify this, you must adjust SORT_DATE
    		sortData.add(context.getString(R.string.sort_by_name));
    		sortData.add(context.getString(R.string.sort_by_size));
            if(!mIsCategoryView){
                sortData.add(context.getString(R.string.sort_by_type));
            }
    	}
    	return sortData;
    }
	
	
}
