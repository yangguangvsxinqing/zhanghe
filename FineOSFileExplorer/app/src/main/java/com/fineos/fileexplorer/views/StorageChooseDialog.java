package com.fineos.fileexplorer.views;

import fineos.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.entity.StorageInfo;

import java.util.ArrayList;

public class StorageChooseDialog extends AlertDialog {

    private final OnItemClickListener mOnStorageListListener;
    private Context mContext;
    private static ArrayList<String> sortData;
    private ArrayList<StorageInfo> mStorageList;

    public StorageChooseDialog(Context context, ArrayList<StorageInfo> storageList, OnItemClickListener onStorageListClick) {
        super(context);
        mContext = context;
        mStorageList = storageList;
        mOnStorageListListener = onStorageListClick;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View mView = getLayoutInflater().inflate(R.layout.sortchoose, null);
        ListView list = (ListView) mView.findViewById(R.id.sort_setting);
        list.setAdapter(new ArrayAdapter<String>(mContext,
                R.layout.choose_sort_resource,
                getStorageListData(mContext)));
        list.setOnItemClickListener(mOnStorageListListener);
        this.setTitle(mContext.getString(R.string.choose_storage));
        //this.setButton(an, null);
        setView(mView);
        super.onCreate(savedInstanceState);
    }

    public synchronized ArrayList<String> getStorageListData(Context context) {
        if (sortData == null) {
            sortData = new ArrayList<String>();
            for (StorageInfo storageInfo : mStorageList) {
                sortData.add(storageInfo.getStorageName());
            }
        }
        return sortData;
    }


}
