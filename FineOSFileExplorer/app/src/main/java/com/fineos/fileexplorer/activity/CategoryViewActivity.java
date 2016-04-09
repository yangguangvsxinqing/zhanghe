package com.fineos.fileexplorer.activity;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.bussiness.FileViewActivityBussiness;
import com.fineos.fileexplorer.entity.FileInfo;
import com.fineos.fileexplorer.entity.StorageInfo;
import com.fineos.fileexplorer.fragments.FileStatusFragment;
import com.fineos.fileexplorer.operations.OperationResult;
import com.fineos.fileexplorer.service.FileSearchLoader;
import com.fineos.fileexplorer.service.IFileSearchHelper;
import com.fineos.fileexplorer.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by acmllaugh on 14-10-9.
 */
public class CategoryViewActivity extends FileViewActivity {


    public static final int CATEGORY_FILES_LOADER = 2;
    private FileInfo.FileCategory mFileCategory;
    private EventBus mEventBus = EventBus.getDefault();

    private CategoryFilesLoaderCallBack mLoaderCallBack = new CategoryFilesLoaderCallBack();
    private IFileSearchHelper mFileSearchHelper;
    private List<StorageInfo> mStorageList;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null && msg.obj instanceof ArrayList) {
                ArrayList<FileInfo> fileList = (ArrayList<FileInfo>) msg.obj;
                mBussiness.setCurrentFileList(fileList);
                fileList = (ArrayList<FileInfo>) mBussiness.onSortMethodChanged(mBussiness.getSortMethod());
                setFileListToListView(fileList, mDirectoryPath);
                showFileListView();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerToEventBus();
    }

    private void registerToEventBus() {
        if (!mEventBus.isRegistered(this)) {
            try {
                mEventBus.register(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void unRegisterToEventBus() {
        if (mEventBus.isRegistered(this)) {
            try {
                mEventBus.unregister(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void getStorageInfo(Bundle savedInstanceState) {

        getLoaderManager().initLoader(STORAGE_INFO_LOADER, null, this);
    }

    @Override
    protected String getmDirectoryPath() {
        // This activity is used for showing specific category file, so no need to know directory path.
        return "";
    }

    public CategoryFilesLoaderCallBack getLoaderCallBack() {
        return mLoaderCallBack;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category_view, menu);
        mMainMenu = menu;
        return true;
    }

    @Override
    public void onLoadFinished(Loader<List<StorageInfo>> loader, List<StorageInfo> storageList) {
        if (loader.getId() == STORAGE_INFO_LOADER) {
            if (storageList.size() <= 0) {
                showNoAvailableStorageView();
            } else {
                mStorageList = storageList;
                mBussiness.setStorageList((ArrayList<StorageInfo>)mStorageList);
                getCategory();
                showPathBar(new ArrayList<String>());
                showFileList(mFileCategory);
                setupOtherInteration();
            }
            //analyze directory, show path bar and files under the directory.
//            if(currentStorageInfo != null){
//                showPathList = analyzeDirectoryPath(mDirectoryPath,
//                        currentStorageInfo);
//                showPathBar(new ArrayList<String>());

//                setupOtherInteration();
//            }else{
//
//
//            }
        }
    }

    @Override
    public Loader<List<StorageInfo>> onCreateLoader(int id, Bundle args) {
        if (id == STORAGE_INFO_LOADER) {
            return super.onCreateLoader(id, args);
        }
        return null;
    }


    private void showFileList(FileInfo.FileCategory category) {
        mFileList = new ArrayList<FileInfo>();
        initListView();
        mFileSearchHelper = new FileSearchLoader(this);
        getLoaderManager().restartLoader(CATEGORY_FILES_LOADER, null, mLoaderCallBack);
    }

    private void getCategory() {
        mFileCategory = FileInfo.FileCategory.getCategoryByName(getIntent().getStringExtra(FileStatusFragment.CATEGORY_FLAG));
        Log.d("CategoryViewActivity", "received category : " + mFileCategory.name());
    }


    private void showNoAvailableStorageView() {
        //We find there is no storage at all, finish this activity.
        this.setResult(CLOSE_REASON_NO_STORAGE_PATH);
        this.finish();
    }

    @Override
    protected Button setPathBarRootName() {
        Button rootPathButton = (Button) findViewById(R.id.button_root_path);
        rootPathButton.setText(getString(FileInfo.FileCategory.getCategoryStringResource(mFileCategory)));
        return rootPathButton;
    }

    @Override
    public void onBackPressed() {
        FileViewActivityBussiness.SelectionState currentState = mBussiness.getCurrentSelectionState();
        switch (currentState) {
            case OPERATION:
                mBussiness.clearSelection();
                mBussiness.hideOperationView();
                break;
            case EXPLORE:
                this.finish();
                showActivityGoneAnime();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        String itemTitle = (String) item.getTitle();
        if (itemTitle == null) return super.onMenuItemSelected(featureId, item);
        if (itemTitle.equals(getString(R.string.refresh))) {
            refresh();
        } else if (itemTitle.equals(getString(R.string.sort))) {
            if (mFileList.size() > 1) {
                createSortDialog(true);
            }else if(mFileList.size() == 1) {
                StringUtils.makeToast(this, getString(R.string.no_need_sort));
            }else{
                Toast.makeText(this, getString(R.string.no_file_in_directory), Toast.LENGTH_SHORT).show();
            }
        }
        FileViewActivityBussiness.SelectionState currentState = mBussiness.getCurrentSelectionState();
        if (currentState == FileViewActivityBussiness.SelectionState.OPERATION) {
            mBussiness.clearSelection();
            mBussiness.hideOperationView();
        }
        return true;// Return true to finish processing of selection.
    }

    public void refresh() {
        getLoaderManager().restartLoader(CATEGORY_FILES_LOADER, null, mLoaderCallBack);
    }

    @Override
    protected boolean canOptionDisplay(int optionID) {
        switch (optionID) {
            case R.string.show_hidden_files:
                return false;
            case R.string.hide_hidden_files:
                return false;
            case R.string.create_folder:
                return false;
        }
        return super.canOptionDisplay(optionID);
    }

    @Override
    protected void onResume() {
        //TODO: super onresume.
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unRegisterToEventBus();
    }

    public void onEvent(final OperationResult result) {
        mBussiness.clearSelection();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refresh();
                mBussiness.dismissProcessDialog();
                if(mBussiness.getCurrentSelectionState() == FileViewActivityBussiness.SelectionState.OPERATION){
                    mBussiness.hideOperationView();
                }
                Log.d("acmllaugh1", "onEvent (line 784): result : " + result);
            }
        });
    }

    class CategoryFilesLoaderCallBack implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            showLoadingView();
            return mFileSearchHelper.fuzzyQuery(null, mFileCategory);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // Create mFileList<FileInfo> and clear it if it already exists.
            Log.d("acmllaugh1", "onLoadFinished: on finish load category files.");
            if (data!= null && data.moveToFirst()) {
                final Cursor dataCursor = data;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<FileInfo> fileList = new ArrayList<FileInfo>();
                        if (dataCursor.moveToFirst()) {
                            mBussiness.showNoFileView(false);
                            int idColumn = dataCursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
                            int pathColumn = dataCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                            do {
                                String filePath = dataCursor.getString(pathColumn);
                                int fileID = dataCursor.getInt(idColumn);
                                File file = new File(filePath);
                                if (file.exists() && file.length() >= 0 && !file.isHidden()) {
                                    FileInfo newFileInfo = new FileInfo(file, mFileCategory, false);
                                    newFileInfo.setDbId(fileID);
                                    fileList.add(newFileInfo);
                                }
                            } while (!dataCursor.isClosed() && dataCursor.moveToNext());
                            Message msg = new Message();
                            msg.obj = fileList;
                            mHandler.sendMessage(msg);
                        }
                    }
                });
                thread.start();
            } else {
                mBussiness.showNoFileView(true);
            }


        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if (mFileList != null) {
                mFileList.clear();
                fileListAdapter.notifyDataSetChanged();
            }
        }
    }


}
