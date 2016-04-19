package com.fineos.fileexplorer.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.adapters.FileViewListAdapter;
import com.fineos.fileexplorer.bussiness.FileViewActivityBussiness;
import com.fineos.fileexplorer.bussiness.FileViewActivityBussiness.SelectionState;
import com.fineos.fileexplorer.bussiness.StorageInfoLoader;
import com.fineos.fileexplorer.entity.FileInfo;
import com.fineos.fileexplorer.entity.StorageInfo;
import com.fineos.fileexplorer.fragments.FileStatusFragment;
import com.fineos.fileexplorer.util.FolderObserver;
import com.fineos.fileexplorer.util.StringUtils;
import com.fineos.fileexplorer.views.ChoiceSortDialog;
import com.fineos.fileexplorer.views.MenuDialog;
import com.fineos.fileexplorer.views.PathButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class FileViewActivity extends Activity implements LoaderCallbacks<List<StorageInfo>>,FolderObserver.FolderObserverListener, MenuDialog.MenuDialogListener, OnClickListener {

    protected static final int STORAGE_INFO_LOADER = 1;
    public static final int CLOSE_REASON_NO_STORAGE_PATH = 7;
    private static final String CURRENT_DIR = "current_dir";
    protected FileViewActivityBussiness mBussiness;
    protected String mDirectoryPath;
    protected StorageInfo currentStorageInfo;
    protected ArrayList<String> showPathList;
    private LinearLayout layoutPathBar;
    private LinearLayout.LayoutParams pathLayoutParams;

    private View.OnClickListener pathButtonClickListener;
    private HorizontalScrollView pathBarScrollView;
    protected ArrayList<FileInfo> mFileList;
    protected FileViewListAdapter fileListAdapter;
    private ListView fileListView;
    private Context context = this;
    private ChoiceSortDialog mSortDialog;
    protected Menu mMainMenu;
    private ImageButton mFileOperationMenu;
    protected LinearLayout mLoadingLayout;
    protected ListView mFileListView;
    private TextView mLoadingTextView;
    IntentFilter mTimeChangedIntentFilter;
    private FolderObserver mFolderObserver;

    BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (fileListAdapter != null) {
                fileListAdapter.updateTime();
            }
        }
    };
    private boolean mContentChanged;

    private List<Integer> OperationsMap;
    private MenuDialog mOperationsDialog;
    private Handler mHandler = new Handler();

    /**
     * We do steps to create File View Activity:
     * 1.Get the path and currentstorage information passed by other activity
     * (If there is no storage information passed in, just open system default storage path.)
     * 2.If path is valid than analyze the path to build a string list which will fill the path bar.
     * 3.Create path buttons and arrows on path bar and set their listeners, add
     * all the button and arrows to path bar. 4.Show files under that directory.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_file_view);
        initViews();
        setActionBar();
        mBussiness = new FileViewActivityBussiness(this, this);
        initExploreState(this.getIntent());
        mDirectoryPath = getmDirectoryPath();
        getStorageInfo(savedInstanceState);
        registerTimeObserver();
        initOperationsMap();
    }

    private void initOperationsMap() {
        OperationsMap = new ArrayList<>();

        OperationsMap.add(R.string.refresh);
        OperationsMap.add(R.string.create_folder);
        OperationsMap.add(R.string.show_hidden_files);
        OperationsMap.add(R.string.hide_hidden_files);
        OperationsMap.add(R.string.sort);

        OperationsMap.add(R.string.send);
        OperationsMap.add(R.string.file_info);
        OperationsMap.add(R.string.rename);
        OperationsMap.add(R.string.hotknot);

    }

    private void registerTimeObserver() {
        // Register intent filter to listen to time changed event.
        mTimeChangedIntentFilter = new IntentFilter();
        mTimeChangedIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        mTimeChangedIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        registerReceiver(mTimeChangedReceiver, mTimeChangedIntentFilter);
    }

    private void initViews() {
        mLoadingLayout = (LinearLayout) findViewById(R.id.llayout_loading_or_no_file);
        mLoadingTextView = (TextView) findViewById(R.id.textview_loading);
        mFileListView = (ListView) findViewById(R.id.listview_file_list);
    }


    private void initExploreState(Intent intent) {
        String action = intent.getAction();
        if (action == null || action.equals("")) {
            mBussiness.setCurrentSelectionState(SelectionState.EXPLORE);
        } else if (action.equals(Intent.ACTION_PICK) || action.equals(Intent.ACTION_GET_CONTENT)
                || action.equals("com.android.fileexplorer.action.FILE_SINGLE_SEL")||
                action.equals("com.mediatek.filemanager.ADD_FILE")) {
            mBussiness.setCurrentSelectionState(SelectionState.PICK);
        }
    }


    private void setActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.actionbar_fileview);
        mFileOperationMenu = (ImageButton) findViewById(R.id.file_operation_menu);
        registerForContextMenu(mFileOperationMenu);
        mFileOperationMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                closeOtherMenusAndOpenOptionsDialog();
            }
        });
        actionBar.show();

    }

    private void openOptionsDialog() {
        mOperationsDialog = new MenuDialog(this, getOptionsMenuOperations());
        mOperationsDialog.setItemClickedListener(this);
        mOperationsDialog.show();
    }

    private String[] getOptionsMenuOperations() {
        ArrayList<String> mOptionsList = new ArrayList<>();
        for (int operation : OperationsMap) {
            if (canOptionDisplay(operation)) {
                mOptionsList.add(getString(operation));
            }
        }
        String[] optionsArray = new String[mOptionsList.size()];
        return mOptionsList.toArray(optionsArray);
    }

    protected boolean canOptionDisplay(int optionID) {
        switch (optionID) {
            case R.string.show_hidden_files:
                return !mBussiness.isHiddenFileShowing();
            case R.string.hide_hidden_files:
                return mBussiness.isHiddenFileShowing();
            case R.string.refresh:
                return true;
            case R.string.create_folder:
                return true;
            case R.string.sort:
                return true;
        }
        return false;
    }


    protected String getmDirectoryPath() {
        Intent intent = getIntent();
        return intent
                .getStringExtra(FileStatusFragment.DIRECTORY_PATH_MESSAGE);
    }

    protected void getStorageInfo(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mDirectoryPath = savedInstanceState.getString(CURRENT_DIR);
        }
        if (!mBussiness.isStringADirectoryPath(mDirectoryPath)) {
            mDirectoryPath = StorageInfo.getDefaultStorageInfo(this).path;
        }

        getLoaderManager().initLoader(STORAGE_INFO_LOADER, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_DIR, mBussiness.getCurrentDirectoryPath());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mDirectoryPath = savedInstanceState.getString(CURRENT_DIR);
     }

    protected void setupOtherInteration() {
        mBussiness.showCountNum(0);
        mBussiness.setUpCancelButton();
        setUpCopyButton();
        setUpPasteButton();
        setUpDeleteButton();
        setUpCutButton();
        setUpExteralMenuButton();
        setUpSelectAllButton();
        setUpCancelPasteButton();
        // setUpSelectAllButton();
        // ...setUp all other buttons.

    }

    private void setUpCancelPasteButton() {
        ImageButton cancelPasteButton = (ImageButton) findViewById(R.id.imagebutton_cancel_paste);
        cancelPasteButton.setOnClickListener(mBussiness
                .getCancelPasteButtonListener());

    }

    private void setUpSelectAllButton() {
        Button selectAllButton = (Button) findViewById(R.id.button_select_all);
        selectAllButton.setOnClickListener(mBussiness
                .getSelectAllButtonListener());

    }

    private void setUpExteralMenuButton() {
        ImageButton moreButton = (ImageButton) findViewById(R.id.imagebutton_more_option);
        moreButton.setOnClickListener(this);
    }

    private int getSelectedFilesCount() {
        ArrayList<File> fileList = fileListAdapter.getSelectedFiles();
        if (fileList == null) return 0;
        return fileList.size();
    }

    private String[] getExtraOperationsArray() {
        ArrayList<String> enabledOperationsList = new ArrayList<String>();
        for (int operationName : OperationsMap) {
            if (operationShouldDisplay(operationName)) {
                enabledOperationsList.add(getString(operationName));
            }
        }
        String[] operationsArray = new String[enabledOperationsList.size()];
        return enabledOperationsList.toArray(operationsArray);
    }

    private boolean operationShouldDisplay(int operationName) {
        int selectedItems = getSelectedFilesCount();
        switch (operationName) {
            case R.string.rename:
            case R.string.file_info:
                return selectedItems == 1;
            case R.string.hotknot:
                return mBussiness.showHotknotButton();
            case R.string.send:
                return true;
        }
        return false;
    }

    private void setUpCutButton() {
        ImageButton cutButton = (ImageButton) findViewById(R.id.imagebutton_cut_file);
        cutButton.setOnClickListener(mBussiness.getCutButtonClickedListener());
    }

    private void setUpDeleteButton() {
        ImageButton deleteButton = (ImageButton) findViewById(R.id.imagebutton_delete_file);
        deleteButton.setOnClickListener(mBussiness
                .getDeleteButtonClickedListener());

    }

    private void setUpPasteButton() {
        ImageButton pasteButton = (ImageButton) findViewById(R.id.imagebutton_paste_file);
        pasteButton.setOnClickListener(mBussiness.getPasteButtonListener());

    }

    private void setUpCopyButton() {
        ImageButton copyButton = (ImageButton) findViewById(R.id.imagebutton_copy_file);
        copyButton.setOnClickListener(mBussiness.getCopyButtonListener());
    }

    /**
     * Fill the folder list of files under that specified directory. Before :
     * already get a directory path to show and the path is valid. After : file
     * list items are shown on list view.
     */
    protected void showFileList(String directoryPath) {
        mFileList = new ArrayList<FileInfo>();
        initListView();
        new GetFileListTask().execute(directoryPath);
    }


    public synchronized void setFileListToListView(ArrayList<FileInfo> fileList, String path) {
        if(fileList == null || fileList.size() < 1){
            mBussiness.showNoFileView(true);
        }else{
            mBussiness.showNoFileView(false);
            showFileListView();
        }
        if (path.equals(mDirectoryPath)) {
            fileListAdapter.clear();
            fileListAdapter.addAll(fileList);
            fileListAdapter.notifyDataSetChanged();
        }
        mFolderObserver = new FolderObserver(path);
        mFolderObserver.setmListener(this);
        mFolderObserver.startWatching();
    }

    /**
     * Set adapter on file list view and set listeners to file list view.
     */
    protected void initListView() {
        fileListAdapter = new FileViewListAdapter(this,
                android.R.layout.simple_list_item_1, mFileList, mBussiness);
        fileListView = (ListView) findViewById(R.id.listview_file_list);
        mBussiness.setFileListAdapter(fileListAdapter);
        mBussiness.setFileListView(fileListView);
        fileListView.setAdapter(fileListAdapter);
        fileListView.setOnItemClickListener(mBussiness.getItemClickListener());
        fileListView.setOnItemLongClickListener(mBussiness
                .getItemLongClickListener());
        // maybe more listeners.......
    }

    /**
     * Get a string list instead of a File list from business layer.
     */
    private ArrayList<FileInfo> getFileInfoList(String directoryPath) {
        return mBussiness.buildFileList(directoryPath);
    }

    /**
     * Before: Get a list of path need to show after root directory on screen
     * with right small arrows follow. After : Storage name is correct
     * set,buttons and arrows is showing on screen. Also event listener is set
     * on each path.
     */
    protected void showPathBar(ArrayList<String> showPathList) {
        ArrayList<Button> pathButtonList = new ArrayList<Button>();
        layoutPathBar = (LinearLayout) findViewById(R.id.llayout_file_path_bar);
        Button rootPathButton = setPathBarRootName();
        pathButtonList.add(rootPathButton);
        createPathButtonsToPathBar(showPathList, getPathButtonLayoutParams());
        pathButtonClickListener = getPathButtonClickListener();
        setupBackButton();
        setupPathButtons(pathButtonList, pathButtonClickListener);
    }

    private void setupBackButton() {
        ImageButton backArrowButton = (ImageButton) findViewById(R.id.button_back_to_mainpage);
        backArrowButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FileViewActivity.this.finish();
                showActivityGoneAnime();
            }
        });

    }

    private void setupPathButtons(ArrayList<Button> pathButtonList,
                                  OnClickListener pathButtonClickListener) {
        for (Button button : pathButtonList) {
            button.setOnClickListener(pathButtonClickListener);
        }
    }

    protected OnClickListener getPathButtonClickListener() {
        if (pathButtonClickListener == null) {
            return mBussiness.createPathButtonOnClickListener();
        }
        return this.pathButtonClickListener;
    }

    private void createPathButtonsToPathBar(ArrayList<String> showPathList,
                                            LinearLayout.LayoutParams layoutParams) {
        pathBarScrollView = (HorizontalScrollView) findViewById(R.id.scrollview_file_path_bar);
        Button currentDirectoryButton = (Button) findViewById(R.id.button_root_path);
        int listLength = showPathList.size();
        if (listLength > 0) {
            for (String path : showPathList) {
                currentDirectoryButton = createNewPathButtonToPathBar(path, layoutParams);
                currentDirectoryButton.setOnClickListener(getPathButtonClickListener());
            }
        }
        currentDirectoryButton.setTextColor(getResources().getColor(R.color.focused_pathBar_color));
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (int i = 0; i < pathBarScrollView.getRight(); i = i + 1) {
                    // StringUtils.printLog("FileExplorerFileViewActivity",
                    // "current i is : " + i);
                    pathBarScrollView.smoothScrollTo(i, 0);
                }
                // pathBarScrollView.smoothScrollTo(
                // pathBarScrollView.getRight(),
                // 0);
                // pathBarScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 200);

        // new CountDownTimer(3000, 40) {
        // public void onTick(long millisUntilFinished) {
        // pathBarScrollView.smoothScrollTo((int) (3000 - millisUntilFinished),
        // 0);
        // }
        // public void onFinish() {
        // }
        // }.start();
    }

    /**
     * Before : We have a path string to print on a path button, and layout
     * parameters will be used on that button. After : Add a path button on path
     * bar, with correct directory name and a right arrow.
     */
    public PathButton createNewPathButtonToPathBar(String path,
                                                   LinearLayout.LayoutParams layoutParams) {
        PathButton pathButton = new PathButton(this);// This custom button will
        // add its arrow in its
        // init part.
        pathButton.setDirectoryName(path);
        layoutPathBar.addView(pathButton, layoutParams);
        pathBarScrollView.post(new Runnable() {
            @Override
            public void run() {
                pathBarScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });
        return pathButton;
    }

    /**
     * Get paramaters for path button on path bar.
     */
    public LayoutParams getPathButtonLayoutParams() {
        if (pathLayoutParams == null) {
            pathLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            pathLayoutParams.setMargins(
                    Math.round(StringUtils.convertDpToPixel(-5, this)), 0,
                    Math.round(StringUtils.convertDpToPixel(-10, this)), 0);
        }
        return pathLayoutParams;
    }

    /**
     * Set root name of path bar to the current storage name.
     */
    protected Button setPathBarRootName() {
        Button rootPathButton = (Button) findViewById(R.id.button_root_path);
        rootPathButton.setText(currentStorageInfo.storageName);
        return rootPathButton;
    }

    protected ArrayList<String> analyzeDirectoryPath(String directoryPath,
                                                     StorageInfo storageInfo) {
        return mBussiness.analyzeDirectoryPath(directoryPath, storageInfo);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        return super.onMenuItemSelected(featureId, item);
    }

    private void closePasteViewAndOperationView() {
        SelectionState currentState = mBussiness.getCurrentSelectionState();
        if (currentState == SelectionState.PASTE) {
            mBussiness.clearSelection();
            mBussiness.hidePasteView();
        }
        if (currentState == SelectionState.OPERATION) {
            mBussiness.clearSelection();
            mBussiness.hideOperationView();
        }
    }

    protected void createSortDialog(boolean isCategoryView) {
        mSortDialog = new ChoiceSortDialog(this, isCategoryView, mBussiness.getSortItemSelectedListener());
        mSortDialog.show();
    }

    @Override
    public void onBackPressed() {
        SelectionState currentState = mBussiness.getCurrentSelectionState();
        switch (currentState) {
            case OPERATION:
                mBussiness.clearSelection();
                mBussiness.hideOperationView();
                break;
            case EXPLORE:
            case PASTE:
            case PICK:
                if (!mBussiness.goParentDirectory()) {
                    super.onBackPressed();
                    showActivityGoneAnime();
                }
                break;
            default:
                break;
        }
    }

    public void showActivityGoneAnime() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    public ChoiceSortDialog getSortDialog() {
        return this.mSortDialog;
    }

    public void setCurrentDirectoryPath(String path) {
        mDirectoryPath = path;
    }

    public void setCurrentStorageInfo(StorageInfo currentStorageInfo) {
        this.currentStorageInfo = currentStorageInfo;
    }

    @Override
    public void onContentChange() {
        Log.d("FileViewActivity", "onContentChange: on content change is called.");
        mContentChanged = true;
    }

    @Override
    public void onDialogMenuItemSelected(String item) {
        onStringOperation(item);
    }

    @Override
    public void onDialogDismiss() {
        dialogIsDissmissed();
    }

    private void dialogIsDissmissed() {
        if(mOperationsDialog == null) return;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mOperationsDialog = null;
            }
        }, 300);
    }

    private void onStringOperation(String itemName) {
        int itemID = getItemID(itemName);
        switch (itemID) {
            case R.string.refresh:
                mBussiness.refresh();
                break;
            case R.string.create_folder:
                mBussiness.createNewFolder();
                break;
            case R.string.show_hidden_files:
                mBussiness.setShowHiddenFiles(true);
                mBussiness.refresh();
                break;
            case R.string.hide_hidden_files:
                mBussiness.setShowHiddenFiles(false);
                mBussiness.refresh();
                break;
            case R.string.sort:
                if (haveEnoughFilesInDir()) {
                    createSortDialog(false);
                }
                break;
            case R.string.send:
                mBussiness.onOperationSendFiles();
                break;
            case R.string.file_info:
                mBussiness.onOperationShowFileInfo();
                break;
            case R.string.rename:
                mBussiness.onOperationRenameFile();
                break;
            case R.string.hotknot:
                mBussiness.onOperationHotknot();
                break;
        }
    }

    private int getItemID(String itemName) {
        for (Integer id : OperationsMap) {
            if (getString(id).equals(itemName)) {
                return id;
            }
        }
        return 0;
    }

    private boolean haveEnoughFilesInDir() {
        if(mFileList.size() > 1) return true;
        if(mFileList.size() == 1) {
            Toast.makeText(this, getString(R.string.no_need_sort), Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, getString(R.string.no_file_in_directory), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.imagebutton_more_option:
                openExtraMenu();
                break;
        }
    }

    private void openExtraMenu() {
        MenuDialog dialog = new MenuDialog(context, getExtraOperationsArray());
        dialog.setItemClickedListener(FileViewActivity.this);
        dialog.show();
    }

    private class GetFileListTask extends AsyncTask<String, Integer, ArrayList<FileInfo>> {

        @Override
        protected ArrayList<FileInfo> doInBackground(String... directoryPath) {
            showLoadingView();
            return getFileInfoList(directoryPath[0]);
        }

        protected void onPostExecute(ArrayList<FileInfo> fileList) {
            showFileListView();
            setFileListToListView(fileList, mDirectoryPath);
            checkPasteViewRemain();
        }

    }

    public void showLoadingView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoadingLayout.setVisibility(View.VISIBLE);
                mLoadingTextView.setVisibility(View.VISIBLE);
                mFileListView.setVisibility(View.GONE);
            }
        });
    }

    public void showFileListView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFileListView.setVisibility(View.VISIBLE);
                mLoadingLayout.setVisibility(View.GONE);
                mLoadingTextView.setVisibility(View.GONE);
            }
        });
    }

    public HorizontalScrollView getPathBarScrollView() {
        return pathBarScrollView;
    }


    @Override
    public Loader<List<StorageInfo>> onCreateLoader(int id, Bundle args) {
        return new StorageInfoLoader(this);
    }


    @Override
    public void onLoadFinished(Loader<List<StorageInfo>> loader,
                               List<StorageInfo> storageList) {
        currentStorageInfo = null;
        mBussiness.setStorageList((ArrayList<StorageInfo>) storageList);
        if (mBussiness.getCurrentSelectionState() == SelectionState.PICK) {
            mBussiness.createStorageChooseDialog();
        }else{
            for (StorageInfo storageInfo : storageList) {
                if (mDirectoryPath.contains(storageInfo.path)) {
                    currentStorageInfo = storageInfo;
                }
            }
            //analyze directory, show path bar and files under the directory.
            if (currentStorageInfo != null) {
                showFileView();
            } else {
                //Finally we find there is no storage at all, finish this activity.
                this.setResult(CLOSE_REASON_NO_STORAGE_PATH);
                this.finish();
            }
        }
    }

    public void showFileView() {
        showPathList = analyzeDirectoryPath(mDirectoryPath,
                currentStorageInfo);
        showPathBar(showPathList);
        showFileList(mDirectoryPath);
        setupOtherInteration();
    }


    private void checkPasteViewRemain() {
        mBussiness.checkPasteViewRemain();
    }


    @Override
    public void onLoaderReset(Loader<List<StorageInfo>> arg0) {
    }


    @Override
    protected void onDestroy() {
        mBussiness.onDestroy();
        try {
            mFolderObserver.stopWatching();
            unregisterReceiver(mTimeChangedReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        initViews();
        if (mBussiness == null) {
            mBussiness = new FileViewActivityBussiness(this, this);
        } else {
            if(mContentChanged) {
                mBussiness.refresh();
                mContentChanged = false;
            }
            mBussiness.registerTaskCompleteReceiver();
            mBussiness.scrollToLastPosition();
            mBussiness.onResume();
        }
       super.onResume();
    }

    @Override
    protected void onStart() {
        checkPasteViewRemain();
        super.onStart();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            closeOtherMenusAndOpenOptionsDialog();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void closeOtherMenusAndOpenOptionsDialog() {
        closePasteViewAndOperationView();
        if(dialogExists()) return;
        openOptionsDialog();
    }

    private boolean dialogExists() {
        return mOperationsDialog != null;
    }
}
