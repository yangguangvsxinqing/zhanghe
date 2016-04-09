package com.fineos.fileexplorer.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import com.fineos.fileexplorer.R;
import com.fineos.fineossupportlibrary.hotknot.HotKnotAdapterWrapper;

public class HotKnotDialogActivity extends Activity implements HotKnotAdapterWrapper.OnHotKnotCompleteListener {

    public static final String TYPE = "type";
    public static final String TAG = "HotKnotDialogActivity";
    public static final String SINGLE_ITEM = "single_item";
    public static final String MULTIPLE_ITEMS = "multiple_items";
    private HotKnotAdapterWrapper mHotKnotAdapter;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj != null && msg.obj instanceof Uri[]) {
                Uri[] uris = (Uri[]) msg.obj;
                mHotKnotAdapter.setHotKnotBeamUris(uris, HotKnotDialogActivity.this);
            }
        }
    };


    private enum ShareType{
        NONE, SINGLE, MULTIPLE;

        public static ShareType formString(String typeString) {
            if(typeString.equals(SINGLE_ITEM)) return SINGLE;
            if(typeString.equals(MULTIPLE_ITEMS)) return MULTIPLE;
            return NONE;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_knot_dialog);
        setTitle(R.string.hotknot);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }else {
            initHotKnot();
            String type = getShareType(intent);
            switch (ShareType.formString(type)) {
                case NONE:
                    finish();
                    break;
                case SINGLE:
                    String filepath = intent.getStringExtra("filepath");
                    Log.d(TAG, "onCreate: file path : " + filepath);
                    if (filepath == null || filepath.isEmpty()) {
                        finish();
                        break;
                    }
                    Uri uri = Uri.fromFile(new File(filepath));
                    mHotKnotAdapter.setHotKnotBeamUris(new Uri[]{uri}, this);
                    break;
                case MULTIPLE:
                    final String[] filePathArray = intent.getStringArrayExtra("filepaths");
                    if (filePathArray == null || filePathArray.length < 0) {
                        finish();
                        break;
                    }
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int size = filePathArray.length;
                            Uri[] uris = new Uri[filePathArray.length];
                            for (int i = 0; i < size; i++) {
                                File file = new File(filePathArray[i]);
                                uris[i] = Uri.fromFile(file);
                            }
                            Message msg = new Message();
                            msg.obj = uris;
                            mHandler.sendMessage(msg);
                        }
                    });
                    thread.start();
                    break;
            }
        }
    }

    private String getShareType(Intent intent) {
        return intent.getStringExtra("type");
    }

    private void initHotKnot() {
        mHotKnotAdapter = new HotKnotAdapterWrapper(this.getApplicationContext());
        mHotKnotAdapter.init();
        if (!mHotKnotAdapter.isSupportedBySystem() || !mHotKnotAdapter.isEnabled()) {
            Toast.makeText(this, getString(R.string.need_turn_on_hotknot), Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        mHotKnotAdapter.setOnHotKnotCompleteCallback(this, this);
    }


    @Override
    public void onHotKnotComplete(int i) {
        this.finish();
    }
}
