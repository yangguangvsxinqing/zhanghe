/*
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */

package com.fineos.fileexplorer.views;

import fineos.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.entity.FileInfo;
import com.fineos.fileexplorer.util.StringUtils;

import java.io.File;

public class InformationDialog extends AlertDialog {
    protected static final int ID_USER = 100;
    private File mFile;
    private Context mContext;
    private View mView;

    public InformationDialog(Context context, File f) {
        super(context);
        mFile = f;
        mContext = context;
    }

    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.information_dialog, null);

        if (mFile.isDirectory()) {
            setIcon(R.drawable.ic_file);
            asyncGetSize();
        } else {
            setIcon(FileInfo.FileCategory.getImageResourceByFile(mFile));
        }
        setTitle(mFile.getName());

        ((TextView) mView.findViewById(R.id.information_size))
                .setText(StringUtils.getProperStorageSizeString(mFile.length(), getContext()));
        ((TextView) mView.findViewById(R.id.information_location))
                .setText(StringUtils.getProperPathString(mFile.getPath(), getContext()));
        ((TextView) mView.findViewById(R.id.information_modified)).setText(StringUtils
                .formatDateString(mContext, mFile.lastModified()));
        ((TextView) mView.findViewById(R.id.information_canread))
                .setText(mFile.canRead() ? R.string.yes : R.string.no);
        ((TextView) mView.findViewById(R.id.information_canwrite))
                .setText(mFile.canWrite()? R.string.yes : R.string.no);
        ((TextView) mView.findViewById(R.id.information_ishidden))
                .setText(mFile.isHidden() ? R.string.yes : R.string.no);

        setView(mView);
        setButton(BUTTON_NEGATIVE, mContext.getString(R.string.confirm_know), (DialogInterface.OnClickListener) null);

        super.onCreate(savedInstanceState);
    }

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ID_USER:
                    Bundle data = msg.getData();
                    long size = data.getLong("SIZE");
                    ((TextView) mView.findViewById(R.id.information_size)).setText(formatFileSizeString(size));
            }
        };
    };

    private AsyncTask task;

    @SuppressWarnings("unchecked")
    private void asyncGetSize() {
        task = new AsyncTask() {
            private long size;

            @Override
            protected Object doInBackground(Object... params) {
                String path = (String) params[0];
                size = 0;
                getSize(path);
                task = null;
                return null;
            }

            private void getSize(String path) {
                if (isCancelled())
                    return;
                File file = new File(path);
                if (file.isDirectory()) {
                    File[] listFiles = file.listFiles();
                    if (listFiles == null)
                        return;

                    for (File f : listFiles) {
                        if (isCancelled())
                            return;

                        getSize(f.getPath());
                    }
                } else {
                    size += file.length();
                    onSize(size);
                }
            }

        }.execute(mFile.getPath());
    }

    private void onSize(final long size) {
        Message msg = new Message();
        msg.what = ID_USER;
        Bundle bd = new Bundle();
        bd.putLong("SIZE", size);
        msg.setData(bd);
        mHandler.sendMessage(msg); // 向Handler发送消息,更新UI
    }

    private String formatFileSizeString(long size) {
        String ret = "";
        if (size >= 1024) {
            ret = StringUtils.getProperStorageSizeString(size, getContext());
            ret += (" (" + mContext.getResources().getString(R.string.file_size, size) + ")");
        } else {
            ret = mContext.getResources().getString(R.string.file_size, size);
        }

        return ret;
    }
}
