package com.fineos.fileexplorer.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.bussiness.StorageInfoLoader;
import com.fineos.fileexplorer.entity.StorageInfo;
import com.fineos.fileexplorer.fragments.FileStatusFragment;

import java.util.ArrayList;
import java.util.List;

public class FileExplorerMainActivity extends Activity implements
        LoaderCallbacks<List<StorageInfo>> {

    private static final String TAG = "FileExplorerMain";
    public static final String STORAGE_INFO_LIST = "storageInfoList";

    private ArrayList<StorageInfo> mStorageInfoList = new ArrayList<StorageInfo>();
    public LinearLayout mNoSdcardLayout;
    private Fragment mFragment;
    private View mActionBarCustomView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getActionBar().hide();
        setContentView(R.layout.activity_file_explorer_main);
        setUpActionBar();
        setUpSearchButton();
        loadStorageInfo();
    }


    private void loadStorageInfo() {
        getLoaderManager().initLoader(1, null, this);
    }

    private void setUpFragment() {
        mNoSdcardLayout = (LinearLayout) findViewById(R.id.llayout_no_sd);
        try {
            if (mFragment != null) {
                if (mFragment instanceof FileStatusFragment && mStorageInfoList.size() > 0) {
                    FileStatusFragment fragment = (FileStatusFragment) mFragment;
                    fragment.updateStorageInfoList(mStorageInfoList);
                }
            }else{
                if (mStorageInfoList != null && mStorageInfoList.size() > 0) {
                    mFragment = FileStatusFragment.newInstance(mStorageInfoList);
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.add(android.R.id.content, mFragment).commitAllowingStateLoss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActionBarCustomView = layoutInflater.inflate(
                R.layout.actionbar_main_page, null);
        actionBar.setCustomView(mActionBarCustomView,
                new ActionBar.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
        actionBar.show();
    }

    private void setUpSearchButton() {
        // set up search button's on click listener to create SearchActivity.
        final ImageButton searchButton = (ImageButton) findViewById(R.id.imagebutton_search);
        TextView appNameText = (TextView) findViewById(R.id.textview_app_name);
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStorageInfoList.size() < 1) {
                    Toast.makeText(FileExplorerMainActivity.this,
                            getString(R.string.no_storage_message),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(FileExplorerMainActivity.this,
                            FileExplorerSearchActivity.class);
                    intent.putExtra(FileExplorerMainActivity.STORAGE_INFO_LIST,
                            mStorageInfoList);
                    Pair transitionSearch = Pair.create(mActionBarCustomView, "transition_search");
                    Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(FileExplorerMainActivity.this, transitionSearch).toBundle();
                    startActivity(intent, bundle);
                }
            }

        };
        searchButton.setOnClickListener(onClickListener);
        appNameText.setOnClickListener(onClickListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false; // Not show options menu.
    }

    @Override
    public Loader<List<StorageInfo>> onCreateLoader(int id, Bundle args) {
        return new StorageInfoLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<StorageInfo>> loader,
                               List<StorageInfo> storageInfoList) {
        mStorageInfoList = (ArrayList<StorageInfo>) storageInfoList;
        Log.d(TAG, "onLoadFinished: on finish finding storage, list size : " + storageInfoList.size());
        updateUI();
    }

    private void updateUI() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                setUpFragment();
                setLayoutVisibility();
            }
        });
    }

    private void setLayoutVisibility() {
        if (mStorageInfoList.size() < 1) {
            mNoSdcardLayout.setVisibility(View.VISIBLE);
            setUpSearchButton();
        } else {
            mNoSdcardLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<StorageInfo>> arg0) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStorageInfo();
    }
}
