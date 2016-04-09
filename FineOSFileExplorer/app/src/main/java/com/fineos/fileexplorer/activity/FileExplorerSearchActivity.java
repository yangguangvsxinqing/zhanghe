package com.fineos.fileexplorer.activity;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.adapters.SearchCursorAdapter;
import com.fineos.fileexplorer.entity.FileInfo.FileCategory;
import com.fineos.fileexplorer.fragments.FileStatusFragment;
import com.fineos.fileexplorer.service.FileSearchLoader;
import com.fineos.fileexplorer.service.IFileSearchHelper;
import com.fineos.fileexplorer.service.IntentBuilder;
import com.fineos.fileexplorer.util.StringUtils;
import com.fineos.fileexplorer.views.CustomSearchView;
import com.fineos.fileexplorer.views.SearchResultView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

public class FileExplorerSearchActivity extends Activity implements
        LoaderCallbacks<Cursor>,
        SearchView.OnQueryTextListener,
        CustomSearchView.CategorySelectedListener, View.OnClickListener {

    private static final int SEARCH_LOADER = 1;
    public static final String NOT_SHOW_DIALOG = "not_show_dialog";
    private final static String TAG = FileExplorerSearchActivity.class
            .getSimpleName();
    private SearchView mSearchView;
    private FileCategory mFileCategory = null;
    private ListView listViewSearchResultList;
    private SearchCursorAdapter listAdapter;
    private IFileSearchHelper mFileSearchHelper;
    private String mSearchString = null;
    private Cursor mResultCursor;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj != null) {
                try {
                    String searchString = (String) msg.obj;
                    if (mSearchString == null || mSearchString.equals(searchString)) {
                        listAdapter.swapCursor(mResultCursor);
                        showNoResultView(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private ImageButton mBackButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransition();
        setContentView(R.layout.activity_search);
        sendScanDiskBoardCast();
        setResultListView();
        setActionBar();
        mSearchView.setOnQueryTextListener(this);
        ((CustomSearchView) mSearchView).setCategorySelectedListener(this);
    }

    private void initActivityTransition() {
        requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        TransitionSet set = new TransitionSet();
        set.addTransition(new Fade());
//        set.addTransition(new Slide(Gravity.BOTTOM));
        set.setDuration(300);
        set.addListener(new Transition.TransitionListener() {
                            @Override
                            public void onTransitionStart(Transition transition) {

                            }

                            @Override
                            public void onTransitionEnd(Transition transition) {
                                initLoaderManager();
                            }

                            @Override
                            public void onTransitionCancel(Transition transition) {

                            }

                            @Override
                            public void onTransitionPause(Transition transition) {

                            }

                            @Override
                            public void onTransitionResume(Transition transition) {

                            }
                        }
        );
        getWindow().setEnterTransition(set);
        Fade fade = new Fade();
        fade.setStartDelay(500);
        getWindow().setReturnTransition(fade);
        getWindow().setSharedElementEnterTransition(new TransitionSet());
        getWindow().setSharedElementReturnTransition(null);
    }

    private void sendScanDiskBoardCast() {
        try {
            Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                    + Environment.getExternalStorageDirectory()));
            intent.putExtra(NOT_SHOW_DIALOG, true);
            sendBroadcast(intent);
        } catch (Exception e) {
            //e.printStackTrace();
            Log.d("FileExplorerSearchActivity", "扫描广播失败，请检查应用是否已安装到/system/priv-app目录下（4.4系统以上）");
        }
    }

    private void setResultListView() {
        listViewSearchResultList = (ListView) findViewById(R.id.listview_search_result);
        listAdapter = new SearchCursorAdapter(this, null, 0);
        listViewSearchResultList.setAdapter(listAdapter);
        listViewSearchResultList
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent,
                                            final View view, int position, long id) {
                        SearchResultView resultView = (SearchResultView) view;
                        File file = resultView.getFile();
                        if (file.isDirectory()) {
                            Intent intent = new Intent(FileExplorerSearchActivity.this, FileViewActivity.class);
                            intent.putExtra(FileStatusFragment.DIRECTORY_PATH_MESSAGE, file.getPath());
                            startActivity(intent);
                            FileExplorerSearchActivity.this.finish();
                            //use slide animation:
                            FileExplorerSearchActivity.this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        } else {
                            String filePath = file.getPath();
                            Log.d(TAG, "need to open file : " + filePath);
                            IntentBuilder.viewFile(
                                    FileExplorerSearchActivity.this, filePath);
                        }
                    }
                });
    }

    private void initLoaderManager() {
        mFileSearchHelper = new FileSearchLoader(this);
        getLoaderManager().initLoader(SEARCH_LOADER, null, this);
    }

    private void setActionBar() {
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View menuLayoutView = layoutInflater.inflate(R.layout.actionbar_search_activity,
                null);
        getActionBar().setCustomView(
                menuLayoutView,
                new ActionBar.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT));
        mSearchView = (SearchView) findViewById(R.id.searchview);
        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mBackButton.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.search, menu);
        setupSearchViewToThisActivity(menu);
        return false;
    }

    @Override
    public void onBackPressed() {
        hideKeyboard(this, mSearchView);
        super.onBackPressed();
    }

    public static void hideKeyboard(Context mContext, View v) {
        // TODO Auto-generated method stub
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                StringUtils.printLog("FileExplorerSearchActivity", "Back is pressed.");
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public boolean onSearchRequested() {
        //Log.d(TAG, "search button is pressed");
        return super.onSearchRequested();
    }

    private void setupSearchViewToThisActivity(Menu menu) {
        // Locate the search view widget
        // as indicated by the menu item.
        if (mSearchView == null) {
            Log.d(TAG, "Failed to get search view");
            return;
        }
        // setup searchview
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName cn = new ComponentName(this,
                FileExplorerSearchActivity.class);
        SearchableInfo info = searchManager.getSearchableInfo(cn);
        if (info == null) {
            Log.d(TAG, "Failed to get search info");
            return;
        }
        mSearchView.setSearchableInfo(info);
        // Do not iconify the widget; expand it by default
        mSearchView.setIconifiedByDefault(false);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        //StringUtils.printLog("FileExplorerSearchActivity", "now judge whether to show no result.");
        if (listAdapter.getCount() < 1) {
            showNoResultView(true);
        } else {
            showNoResultView(false);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Place to do some search!
        mSearchString = newText;
        Log.d("acmllaugh1", "onQueryTextChange (line 248): mSearchString : " + mSearchString);
        doSearch();
        return false;
    }


    private void doSearch() {
        if(mFileSearchHelper == null) {
            initLoaderManager(); // The loader may not be initialized after language change and back to fileexplorer.
        }else {
            getLoaderManager().restartLoader(SEARCH_LOADER, null, this);
        }
    }


    @Override
    public void onCategorySelected(FileCategory fileCategory) {
        mFileCategory = fileCategory;
        getLoaderManager().restartLoader(SEARCH_LOADER, null, this);
        return;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("FileExplorerSearchActivity", "start loader...");
        return mFileSearchHelper.fuzzyQuery(mSearchString, mFileCategory);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mResultCursor = cursor;
        String currentSearchString = mSearchString == null? "":mSearchString;
        Message msg = new Message();
        msg.obj = currentSearchString;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        listAdapter.swapCursor(null);
        listAdapter.notifyDataSetChanged();
    }

    private void showNoResultView(boolean showNoSearchResult) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.llayout_no_result);
        if (showNoSearchResult) {
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageLoader.getInstance().clearMemoryCache();
        ImageLoader.getInstance().clearDiskCache();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_back) {
           onBackPressed();
        }
    }
}
