package com.fineos.fileexplorer.activity;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;

import com.fineos.fileexplorer.R;

public class DialogActivity extends Activity {

    Dialog mLoadingStorageInfoDialog;
    Handler mDialogHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DialogActivity.this.finish();
            super.handleMessage(msg);
        }
    };

    protected void onStop() {
        super.onStop();

        try {
            if (mLoadingStorageInfoDialog != null) {
                mLoadingStorageInfoDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoadingStorageInfoDialog = ProgressDialog.show(this,
                getString(R.string.loading_storage),
                getString(R.string.now_loading), true);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    mDialogHandler.sendEmptyMessage(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}
