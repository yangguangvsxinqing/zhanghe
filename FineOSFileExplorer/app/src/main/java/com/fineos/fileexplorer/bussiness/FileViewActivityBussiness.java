package com.fineos.fileexplorer.bussiness;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.activity.CategoryViewActivity;
import com.fineos.fileexplorer.activity.FileViewActivity;
import com.fineos.fileexplorer.activity.HotKnotDialogActivity;
import com.fineos.fileexplorer.adapters.FileViewListAdapter;
import com.fineos.fileexplorer.adapters.IFileViewBussiness;
import com.fineos.fileexplorer.entity.FileInfo;
import com.fineos.fileexplorer.entity.StorageInfo;
import com.fineos.fileexplorer.fragments.FileStatusFragment;
import com.fineos.fileexplorer.operations.OperationResult;
import com.fineos.fileexplorer.service.FileOperationService;
import com.fineos.fileexplorer.service.FileOperationService.OperationType;
import com.fineos.fileexplorer.service.FileSortHelper;
import com.fineos.fileexplorer.service.FileSortHelper.SortMethod;
import com.fineos.fileexplorer.service.IntentBuilder;
import com.fineos.fileexplorer.util.FileUtils;
import com.fineos.fileexplorer.util.StringUtils;
import com.fineos.fileexplorer.views.FileItemView;
import com.fineos.fileexplorer.views.InformationDialog;
import com.fineos.fileexplorer.views.PathButton;
import com.fineos.fileexplorer.views.StorageChooseDialog;
import com.fineos.fileexplorer.views.TextInputDialog;
import com.fineos.fileexplorer.views.TextInputDialog.OnFinishListener;
import com.fineos.fineossupportlibrary.hotknot.HotKnotAdapterWrapper;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import de.greenrobot.event.EventBus;
import fineos.app.AlertDialog;
import fineos.app.ProgressDialog;

public class FileViewActivityBussiness implements IFileViewBussiness{

	private static final String FILE_VIEW_PREF = "fileViewPref";
	private static final String SAVED_SORT_METHOD = "sortMethod";
	public static final String SAVED_SHOW_HIDDEN_FILES = "showHiddenFiles";
    private final int mTextUnFocusedColor;
    private final int mTextFocusedColor;

    private EventBus mEventBus = EventBus.getDefault();
	private Boolean showHiddenFiles = false;
	private String rootPath = "";
	private String currentDirectoryPath = "";
	private StorageInfo currentStorageInfo;
	private ArrayList<File> selectedFileList;
	private ArrayList<FileInfo> currentFileList;
	private Context context;
	private FileViewActivity mActivity;
	public ProgressDialog mProcessingDialog;
	private TextView selectCountText;
	private LinearLayout noFileLinearLayout;
	private ListView fileListView;
    private ImageView mNoFileImageView;
    private TextView mNoFileTextView;
    private TextView mLoadingTextView;
    private Stack<Integer> mPositionRecords = new Stack<Integer>();

    private SortMethod mSortMethod = SortMethod.name;
	private SelectFileList mFileListObject;


	private FileSortHelper mSortHelper;
	private InformationDialog fileInfoDialog;
	FileViewListAdapter fileListAdapter;
	BroadcastReceiver mFileOperationReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
//			mServiceIsComplete = true;
			String action = intent.getAction();
			clearSelection();
            refresh();

			dismissProcessDialog();
			if(currentSelectionState == SelectionState.OPERATION){
				hideOperationView();
			}
			if(action.equals(FileOperationService.TASK_NOT_COMPLETED)){
				Toast.makeText(mActivity, context.getString(R.string.op_did_not_completed), Toast.LENGTH_SHORT).show();
			}else if(action.equals(FileOperationService.TAST_NAME_DUPLICATION)){
				new AlertDialog.Builder(context).setMessage(context.getString(R.string.create_file_fail))
				.setPositiveButton(context.getString(R.string.confirm), null).create().show();
			}
		}
	};
    private ArrayList<StorageInfo> mStorageList;
    private StorageChooseDialog mStorageChooseDialog;
    private LinearLayout mButtonsLayout;
    public boolean mIsRefreshing;
    private Runnable mScrollListViewRunnable;
	private HotKnotAdapterWrapper mHotKnotAdapter;
	private Button mSelectAllButton;

	public void refresh() {
        if (mIsRefreshing) {
            return; // Last refreshing is not finished.
        }
        if (getTypeOfActivity()) {
            // Category view activity does not need to refresh. The loader of category view activity will automatically load new results.
        } else {
            openDirectoryInFileList(currentDirectoryPath);
        }
    }

    public void createStorageChooseDialog() {
        if (mStorageList != null && mStorageList.size() == 1) {
            showChosenStorage(0);
        } else {
            mStorageChooseDialog = new StorageChooseDialog(mActivity, mStorageList,
                    new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            mStorageChooseDialog.dismiss();
                            showChosenStorage(position);
                        }
                    });
            mStorageChooseDialog.setCancelable(false);
            mStorageChooseDialog.show();
            mStorageChooseDialog.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						mStorageChooseDialog.dismiss();
						mActivity.finish();
					}
					return true;
				}
			});
        }
    }

    private void showChosenStorage(int position) {
        StorageInfo storageInfo = mStorageList.get(position);
        mActivity.setCurrentStorageInfo(storageInfo);
        mActivity.setCurrentDirectoryPath(storageInfo.path);
        mActivity.showFileView();
    }

    public void onResume() {
        if (mEventBus == null) {
            mEventBus = EventBus.getDefault();
        }
        if (!mEventBus.isRegistered(this)) {
            try {
                mEventBus.register(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean showHotknotButton() {
        if (mHotKnotAdapter.isSupportedBySystem()) {
            return true;
        }
        return false;
    }

    public void showHotKnotSystemSettings() {
		mHotKnotAdapter.tryToOpenHotKnotSettingsPage();
    }

	public void onOperationSendFiles() {
		if (buildSelectedFileList()) {
			onOperationSend();
		} else {
			showNeedSelectHint();
		}
	}

	public void onOperationShowFileInfo() {
		ArrayList<File> fileList = fileListAdapter.getSelectedFiles();
		if(fileList != null &&fileList.size() == 1){
			fileInfoDialog = new InformationDialog(context, fileList.get(0));
			fileInfoDialog.show();
		}
		clearSelection();
		hideOperationView();
	}

	public void onOperationRenameFile() {
		if(buildSelectedFileList()){
			renameFile();
		}
	}

	public static enum Animations{
		SELECTION_BAR_IN, SELECTION_BAR_OUT,
		OPERATION_BAR_IN, OPERATION_BAR_OUT
	}
	private Animation selectionBarInAnimation;
	private Animation selectionBarOutAnimation;
	private Animation operationBarInAnimation;
	private Animation operationBarOutAnimation;

	public static enum SelectionState {
		EXPLORE, OPERATION, PASTE, PICK
	};

	private SelectionState currentSelectionState = SelectionState.EXPLORE;

	private boolean isCopyFile = false;
	private boolean isCutFile = false;

	private SharedPreferences mPrefs;

	public FileViewActivityBussiness(Context context,
			FileViewActivity fileViewActivity) {
		this.context = context;
		this.mActivity = fileViewActivity;
		this.selectCountText = (TextView) fileViewActivity
				.findViewById(R.id.txtview_selection_count);
		this.noFileLinearLayout = (LinearLayout) fileViewActivity.findViewById(R.id.llayout_loading_or_no_file);
        mNoFileImageView = (ImageView) fileViewActivity.findViewById(R.id.imageview_no_file);
        mNoFileTextView = (TextView) fileViewActivity.findViewById(R.id.textview_no_file);
        mLoadingTextView = (TextView) fileViewActivity.findViewById(R.id.textview_loading);
        loadSavedStates();
		registerTaskCompleteReceiver();
        if(!getTypeOfActivity()){
		    mEventBus.register(this);
        }
        mTextUnFocusedColor = context.getResources().getColor(R.color.unfocused_pathBar_color);
        mTextFocusedColor = context.getResources().getColor(R.color.focused_pathBar_color);
        mButtonsLayout = (LinearLayout) mActivity.findViewById(R.id.llayout_file_path_bar);
		initHotKnot();
	}

	private void initHotKnot() {
		mHotKnotAdapter = new HotKnotAdapterWrapper(mActivity.getApplicationContext());
		mHotKnotAdapter.init();
	}

	private boolean getTypeOfActivity() {
        return (mActivity instanceof CategoryViewActivity);
    }

    public void registerTaskCompleteReceiver() {
		try {
			IntentFilter filter = new IntentFilter(
					FileOperationService.TASK_COMPLETED);
			filter.addAction(FileOperationService.TASK_NOT_COMPLETED);
			filter.addAction(FileOperationService.TAST_NAME_DUPLICATION);
			mActivity.registerReceiver(
                    mFileOperationReceiver, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadSavedStates() {
		mPrefs = context.getSharedPreferences(FILE_VIEW_PREF, Activity.MODE_PRIVATE);
		String savedSortMethod = mPrefs.getString(SAVED_SORT_METHOD, "name");
		boolean savedShowHiddenFiles = mPrefs.getBoolean(SAVED_SHOW_HIDDEN_FILES, false);
		for(SortMethod sortMethod : SortMethod.values()){
			if(sortMethod.toString().equals(savedSortMethod)){
				mSortMethod = sortMethod;
				break;
			}
		}
		if(savedShowHiddenFiles){
			showHiddenFiles = true;
		}else{
			showHiddenFiles = false;
		}
	}

    // This method is used by category view.
    public SortMethod getSortMethod() {
        return mSortMethod;
    }

	private void saveSortMethod(SortMethod sortMethod){
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(SAVED_SORT_METHOD, sortMethod.toString());
        editor.commit();
	}

	/**
	 * Judge a string is a directory or not. if it is a directory, then
	 * separates the string with "/" and returns a string array. For example a
	 * string like "/abc/def" will return ArrayList<String>[]{"abc", "def"} if
	 * "/abc/def" is a directory.
	 *
	 * before : Already get directoryPath as a string to analyze. after : Return
	 * array list of string that contains strings to display on screen.
	 * storageInfo is set to current storage information.
	 *
	 * */
	public ArrayList<String> analyzeDirectoryPath(String path,
			StorageInfo storageInfo) {
		setCurrentDirectoryPath(path);
		this.rootPath = storageInfo.path;
		this.currentStorageInfo = storageInfo;
		path = dropRootPath(path);
		if (!path.isEmpty()) {
			String[] separatedPathArray = path.split("/");
			ArrayList<String> showPathList = new ArrayList<String>();
			for (String dirName : separatedPathArray) {
				if (dirName.isEmpty())
					continue;
				showPathList.add(dirName);
			}
			return showPathList;
		} else {
			return new ArrayList<String>();
		}
	}

    public String getCurrentDirectoryPath() {
        return currentDirectoryPath;
    }

    private void setCurrentDirectoryPath(String path) {
		this.currentDirectoryPath = path;
        mActivity.setCurrentDirectoryPath(path);
    }

	/**
	 * Drop root directory which is already represented by word "Phone" on
	 * screen.
	 * */
	private String dropRootPath(String path) {
		if (path.equals(rootPath)) {
			return "";
		} else {
			path = path.replaceFirst(rootPath, "");
		}
		return path;
	}

	/**
	 * Judge a string is a directory or not. This method is used by
	 * separateDirectory(String path).
	 *
	 * Before : given a string to judge. After : the judgment boolean result (is
	 * this path a directory or not).
	 *
	 * */
	public boolean isStringADirectoryPath(String path) {
		boolean result = false;
		if (path == null || path.isEmpty()) {
			StringUtils.printLog("FileViewActivityBussiness",
					"string is null or empty.");
			return result;
		}
		File file = new File(path);
		if (file != null && file.isDirectory() && file.canRead()) {
			result = true;
			return result;
		} else {
			StringUtils.printLog("FileViewActivityBussiness",
					"string is not a vaild directory!");
			return result;
		}
	}

	/**
	 * Create path button onclick listener. It will find out which path to jump
	 * in when user click on a path button.
	 * */
	public OnClickListener createPathButtonOnClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				Button clickedPathButton = (Button) v;
				clickedPathButton.setTextColor(mTextFocusedColor);
                if(mActivity instanceof CategoryViewActivity){
                    mActivity.finish();
                    mActivity.showActivityGoneAnime();
                }else if(clickedPathButton.getText().equals(currentStorageInfo.storageName)){//Root path clicked.
                    if(mButtonsLayout.getChildCount() < 1){
                        mActivity.finish();
                        mActivity.showActivityGoneAnime();
                    }
                    openDirectoryInFileList(currentStorageInfo.path);
                    mButtonsLayout.removeAllViews();
                } else{
					LinearLayout buttonsLayout = (LinearLayout)clickedPathButton.getParent();
					int childCount = buttonsLayout.getChildCount();
					String newPath = currentStorageInfo.path;
					for(int i = 0; i < childCount; ++i){
						Button pathButton = (Button) buttonsLayout.getChildAt(i);
						if(pathButton == clickedPathButton){
							buttonsLayout.removeViewsInLayout(i+1, childCount - i - 1);
                            buttonsLayout.requestLayout();
							//buttonsLayout.invalidate();//Without invalidate, the button path view may not update button change.
							break;
						}else{
							newPath += File.separator + pathButton.getText();
						}
					}
                    scrollToLastButton();
                    newPath += File.separator + clickedPathButton.getText();
					openDirectoryInFileList(newPath);
				}
			}

		};
	}

    private void scrollToLastButton() {
        final HorizontalScrollView scrollView = (HorizontalScrollView) mActivity.findViewById(R.id.scrollview_file_path_bar);
        scrollView.clearAnimation();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Log.d("com.fineos.fileexplorer.mBussiness.FileViewActivityBussiness", "run (line 367): right : " + scrollView.getRight());
                scrollView.smoothScrollTo(scrollView.getRight(), 0);
            }
        });
    }

    // Set current file list, used by category file view.
    public void setCurrentFileList(ArrayList<FileInfo> currentFileList) {
        this.currentFileList = currentFileList;
    }

    /**
	 * Get String list of the files under given directory path.
	 * */
	public ArrayList<FileInfo> getFileList(String directoryPath) {
		File[] files = (new File(directoryPath)).listFiles();
		currentFileList = new ArrayList<FileInfo>();
		if(files == null) return currentFileList;
		if(showHiddenFiles){
			for (File file : files) {
				currentFileList.add(new FileInfo(file, showHiddenFiles));
			}
		}else{
			for(File file : files){
				if(!file.isHidden()){
					currentFileList.add(new FileInfo(file, showHiddenFiles));
				}
			}
		}
		currentFileList = (ArrayList<FileInfo>) onSortMethodChanged(mSortMethod);
		return currentFileList;
	}

	/**
	 * Build click listener to every file item showed in the file list. When
	 * user click one item, if it is an directory, then open it in file list; if
	 * it is a file, open it with intent builder.
	 * */
	public OnItemClickListener getItemClickListener() {
		return new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				FileItemView fileItemView = (FileItemView) view;
				FileInfo file = fileItemView.getFile();
				String path = file.getFilePath();
				switch (currentSelectionState) {
				case OPERATION:
					onSelectItem((FileItemView) view);
                    break;
				case EXPLORE:
				case PASTE:
                    recordCurrentPosition();
                    if (file.isDirectory()) {
						setCurrentDirectoryPath(path);
						openDirectoryInFileList(path);
						addPathToPathBar(file.getFileName());
					} else {
						openFile(path);
					}
					break;
				case PICK:
                    recordCurrentPosition();
					if (file.isDirectory()) {
						setCurrentDirectoryPath(path);
						openDirectoryInFileList(path);
						addPathToPathBar(file.getFileName());
					} else {
						onPick(file);
					}
					break;
				default:
					break;
				}
			}
		};
	}

	private void onSelectItem(FileItemView view) {
		FileItemView fileItem = view;
		if (fileItem.isCheckBoxChecked()) {
            fileItem.setCheckBoxChecked(false);
        } else {
            fileItem.setCheckBoxChecked(true);
        }
	}

    private void recordCurrentPosition() {
        int position = fileListView.getFirstVisiblePosition();
        Log.d("acmllaugh1", "recordCurrentPosition (line 509): push position : " + position);
        if (mPositionRecords != null) {
            mPositionRecords.push(position);
        }
    }

    public void setStorageList(ArrayList<StorageInfo> storageList) {
        this.mStorageList = storageList;
    }

    public void scrollToLastPosition() {
        int position = 0;
        try {
            if (mPositionRecords != null && !mPositionRecords.isEmpty()) {
				position = mPositionRecords.pop();
			}
			Log.d("acmllaugh1", "scrollToLastPosition (line 477): popup position : " + position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (position > 0) {
            Handler handler = new Handler();
            final int finalPosition = position;

            if(mScrollListViewRunnable != null) {
                handler.removeCallbacks(mScrollListViewRunnable);
            }
            mScrollListViewRunnable = new Runnable() {
                @Override
                public void run() {
                    fileListView.setSelection(finalPosition);
                }
            };
            handler.postDelayed(mScrollListViewRunnable, 300);
        }
    }

    public void onPick(FileInfo f) {
        try {
        	String uriString = Uri.fromFile(new File(f.getFilePath())).toString();
            Intent intent = Intent.parseUri(uriString, 0);
            //Log.d("FileViewActivityBussiness", "uri " + uriString);
            mActivity.setResult(Activity.RESULT_OK, intent);
            mActivity.finish();
            return;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

	private void addPathToPathBar(String filename) {
		PathButton pathButton = mActivity
				.createNewPathButtonToPathBar(filename,
						mActivity
								.getPathButtonLayoutParams());

		pathButton
				.setOnClickListener(createPathButtonOnClickListener());
		pathButton.setCurrentDirectory(true);
        if (mButtonsLayout.getChildCount() < 2) {
            Button mRootButton = (Button) mActivity.findViewById(R.id.button_root_path);
            mRootButton.setTextColor(mTextUnFocusedColor);
        }else {
            View buttonView = mButtonsLayout.getChildAt(mButtonsLayout.getChildCount() - 2);
            ((PathButton) buttonView).setTextColor(mTextUnFocusedColor);
        }
	}

	private void openDirectoryInFileList(final String path) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mIsRefreshing = true;
                mActivity.showLoadingView();
                setCurrentDirectoryPath(path);
                File newPath = new File(path);
                if(newPath.canRead()){
                    currentFileList = getFileList(path);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                          mActivity.setFileListToListView(currentFileList, path);
                        }
                    });
                }else{
                    mActivity.setResult(FileViewActivity.CLOSE_REASON_NO_STORAGE_PATH);
                    mActivity.finish();
                    mActivity.showActivityGoneAnime();
                }
                mIsRefreshing = false;
			}
		});
		thread.start();
	}


	public void showNoFileView(final boolean b) {
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (b) {
					fileListView.setVisibility(View.GONE);
					noFileLinearLayout.setVisibility(View.VISIBLE);
					mLoadingTextView.setVisibility(View.GONE);
					mNoFileImageView.setVisibility(View.VISIBLE);
					mNoFileTextView.setVisibility(View.VISIBLE);
				} else {
					fileListView.setVisibility(View.VISIBLE);
					mLoadingTextView.setVisibility(View.GONE);
					noFileLinearLayout.setVisibility(View.GONE);
					mNoFileImageView.setVisibility(View.GONE);
					mNoFileTextView.setVisibility(View.GONE);
				}
			}
		});
	}

	private void openFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(mActivity, context.getString(R.string.file_not_exist), Toast.LENGTH_SHORT).show();
            refresh();
            return;
        }
        IntentBuilder.viewFile(mActivity, filePath);
    }

    public OnItemLongClickListener getItemLongClickListener() {
		return new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(currentSelectionState == SelectionState.EXPLORE){
					addClickedFileToSelectedList(position);
					showCountNum(1);
					showOperationView();
					scrollToSelectedItem(view, position);
				}
				return true;// return true so that it will not trigger click event.
			}
		};
	}

	private void scrollToSelectedItem(View view, final int position) {
		StringUtils.printLog("FileViewActivityBussiness", "scroll to position" + position);
		final int h1 = fileListView.getHeight();
		final int h2 = view.getHeight();
		final int firstVisiblePosition = fileListView.getFirstVisiblePosition();
		final int lastVisiblePosition = fileListView.getLastVisiblePosition();
		final int bestAdjustNumber = h1/h2;
		final int actualNumber = lastVisiblePosition - firstVisiblePosition + 1;
		if(position >= lastVisiblePosition - 1){
			fileListView.post(new Runnable() {
                @Override
                public void run() {
                    StringUtils.printLog("FileViewActivityBussiness", "position : " + position);
                    if (actualNumber <= bestAdjustNumber || position == (lastVisiblePosition - 1)) {
                        fileListView.smoothScrollBy(h2, 500);
                    } else {
                        fileListView.smoothScrollBy(h2 * 2, 500);
                    }
                }
            });
		}
	}


	private void addClickedFileToSelectedList(int position) {
			fileListAdapter.addFileToSelectedList(position);
	}

	private void showOperationView() {
		currentSelectionState = SelectionState.OPERATION;
		mSelectAllButton = (Button) mActivity.findViewById(R.id.button_select_all);
		if (currentFileList != null && currentFileList.size() > 1) {
			showSelectAll();
		} else {
			showDeSelectAll();
		}
		mActivity.findViewById(
                R.id.linearlayout_outer_path_bar).setVisibility(View.INVISIBLE);
		selectionBarInAnimation = getAnimation(Animations.SELECTION_BAR_IN);
		mActivity.findViewById(R.id.rlayout_selection_bar)
				.startAnimation(selectionBarInAnimation);
		mActivity.findViewById(R.id.rlayout_selection_bar)
				.setVisibility(View.VISIBLE);
		operationBarInAnimation = getAnimation(Animations.OPERATION_BAR_IN);
		mActivity.findViewById(R.id.llyout_operation_bar)
				.startAnimation(operationBarInAnimation);
		mActivity.findViewById(R.id.llyout_operation_bar)
				.setVisibility(View.VISIBLE);
		ListView fileList = (ListView) mActivity
				.findViewById(R.id.listview_file_list);
		fileList.invalidateViews();
	}

	public void showDeSelectAll() {
		mSelectAllButton.setText(mActivity.getString(R.string.un_select_all));
	}

	public void showSelectAll() {
		mSelectAllButton.setText(mActivity.getString(R.string.select_all));
	}

    private Animation getAnimation(Animations animationNeeded) {
		switch (animationNeeded) {
		case SELECTION_BAR_IN: {
			if (selectionBarInAnimation == null)
				return AnimationUtils.loadAnimation(context,
						R.anim.slide_in_top);
			return selectionBarInAnimation;
		}
		case SELECTION_BAR_OUT: {
			if (selectionBarOutAnimation == null){
				selectionBarOutAnimation = AnimationUtils.loadAnimation(context,
						R.anim.slide_out_top);
				selectionBarOutAnimation.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
						mActivity.findViewById(R.id.linearlayout_outer_path_bar).setVisibility(View.VISIBLE);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationStart(Animation animation) {
					}
				});
			}
			return selectionBarOutAnimation;
		}
		case OPERATION_BAR_IN: {
			if (operationBarInAnimation == null)
				return AnimationUtils.loadAnimation(context,
						R.anim.slide_in_bottom);
			return operationBarInAnimation;
		}
		case OPERATION_BAR_OUT: {
			if (operationBarOutAnimation == null)
				return AnimationUtils.loadAnimation(context,
						R.anim.slide_out_bottom);
			return operationBarOutAnimation;
		}
		}
		return null;
	}

	public SelectionState getCurrentSelectionState() {
		return currentSelectionState;
	}

	public void setUpCancelButton() {
		Button cancelButton = (Button) mActivity
				.findViewById(R.id.button_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				clearSelection();
				hideOperationView();
			}

		});

	}

	public void hideOperationView() {
		currentSelectionState = SelectionState.EXPLORE;
		selectionBarOutAnimation = getAnimation(Animations.SELECTION_BAR_OUT);
		View selectionBar = mActivity.findViewById(R.id.rlayout_selection_bar);
		selectionBar.setVisibility(View.GONE);
		selectionBar.startAnimation(selectionBarOutAnimation);
		View operationBar = mActivity.findViewById(R.id.llyout_operation_bar);
		operationBarOutAnimation = getAnimation(Animations.OPERATION_BAR_OUT);
		operationBar.setVisibility(View.GONE);
		operationBar.startAnimation(operationBarOutAnimation);
		fileListView.invalidateViews();
	}

	public OnClickListener getCopyButtonListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				buildSelectedFileList();
                sendStartPasteViewEvent(false,true);
			}
		};
	}

    private void startFileViewActivity() {
        if(mStorageList == null || mStorageList.size() <= 1){
            Intent intent = new Intent(mActivity,FileViewActivity.class);
            intent.putExtra(FileStatusFragment.DIRECTORY_PATH_MESSAGE, Environment.getExternalStorageDirectory().getAbsolutePath());
            mActivity.startActivityForResult(intent, 1);
            mActivity.finish();
            //use slide animation:
            mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }else{
            mStorageChooseDialog = new StorageChooseDialog(mActivity, mStorageList, getStorageListItemSelectedListener());
            mStorageChooseDialog.show();
        }

    }

    public void onEvent(SelectFileList fileList) {
        if (mActivity instanceof CategoryViewActivity) {
			return; // Not show paste view in category view activity.
        }
        showPasteView();
    }

    public void onEvent(final OperationResult result) {
		refresh();
		dismissProcessDialog();
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				clearSelection();
				if(currentSelectionState == SelectionState.OPERATION){
					hideOperationView();
				}
				if (result.getResult() == OperationResult.OperationResultType.CANNOT_COPY_FILE_INTO_SELF) {
					Toast.makeText(mActivity, mActivity.getString(R.string.cannot_copy_to_self), Toast.LENGTH_SHORT).show();
				}
				if (result.getResult() == OperationResult.OperationResultType.TARGET_STORAGE_IS_FULL) {
					Toast.makeText(mActivity, R.string.target_storage_is_full, Toast.LENGTH_SHORT).show();
				}
			}
		});
    }

	public OnClickListener getCutButtonClickedListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				buildSelectedFileList();
                sendStartPasteViewEvent(true, false);
			}
		};
	}

    private void sendStartPasteViewEvent(boolean isCutFile, boolean isCopyFile) {
		Log.d("acmllaugh1", "sendStartPasteViewEvent: is cut : " + isCutFile + " is copy file : " + isCopyFile);
        if (selectedFileList != null && selectedFileList.size() > 0) {
            mFileListObject = new SelectFileList();
            mFileListObject.selectedFileList = selectedFileList;
            mFileListObject.pasteIsDown = false;
            mFileListObject.isCutFile = isCutFile;
            this.isCutFile = isCutFile;
            mFileListObject.isCopyFile = isCopyFile;
            this.isCopyFile = isCopyFile;
            mEventBus.postSticky(mFileListObject);
            if(getTypeOfActivity()) {
                startFileViewActivity();
            }
        } else {
            showNeedSelectHint();
        }
    }


    private void showNeedSelectHint() {
		Toast.makeText(context, context.getString(R.string.select_hint), Toast.LENGTH_SHORT)
				.show();
		clearSelection();
		hideOperationView();
	}

	public OnClickListener getDeleteButtonClickedListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (buildSelectedFileList()) {
					confirmDelete();
				} else {
					showNeedSelectHint();
				}
			}
		};
	}

	private boolean confirmDelete() {
		new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(context.getString(R.string.confirm_delete_msg))
				.setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						showProcessingDialog();
						deleteFiles();
						hideOperationView();
					}

				})
				.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						clearSelection();
						hideOperationView();
					}
				}).show();
		return false;
	}

	private boolean buildSelectedFileList() {
		Log.d("acmllaugh1", "buildSelectedFileList: build selected file list.");
		selectedFileList = new ArrayList<>(fileListAdapter.getSelectedFiles());
		if (selectedFileList.size() == 0) {
			Log.d("acmllaugh1", "buildSelectedFileList: selected file list is empty : " + selectedFileList);
			return false;
		}
		Log.d("acmllaugh1", "buildSelectedFileList: size : " + selectedFileList.size());
		return true;
	}

	private void showPasteView() {
		mActivity.findViewById(R.id.llyout_paste_bar)
				.setVisibility(View.VISIBLE);
		if(isOperationViewShowing()){
			hideOperationView();
		}
		currentSelectionState = SelectionState.PASTE;
	}

	private boolean isOperationViewShowing(){
		View operationBar = mActivity.findViewById(R.id.llyout_operation_bar);
		if(currentSelectionState == SelectionState.OPERATION &&
				operationBar != null && operationBar.getVisibility() == View.VISIBLE){
			return true;
		}else{
			return false;
		}
	}

	public OnClickListener getPasteButtonListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("acmllaugh1", "onClick: paste button is clicked.");
				if (selectedFileList != null && selectedFileList.size() > 0) {
					hidePasteView();
					showProcessingDialog();
					if (isCopyingFile()) {
						Log.d("acmllaugh1", "onClick: copy file button is pressed.");
						copyFile();// Should clear selection, set copyFile to
									// false after copy files.
						return;
					}
					if (isCutFile()) {
						if (fileCanMove(currentDirectoryPath)) {
							StringUtils.printLog("FileViewActivityBussiness",
									"file can be moved.");
							moveFile();
						} else {
							Toast.makeText(context, context.getString(R.string.cannot_move),
									Toast.LENGTH_SHORT).show();
							dismissProcessDialog();
						}
					} else {
						StringUtils.printLog("FileViewActivityBussiness",
								"no files be selected.");
					}
				}else{
					Log.d("acmllaugh1", "onClick: selected file list is null or empty : " + selectedFileList);
					if (selectedFileList != null) {
						Log.d("acmllaugh1", "onClick: size : " + selectedFileList.size());
					}
				}
			}
		};
	}

	private void moveFile() {
		if(mFileListObject != null){
			mFileListObject.pasteIsDown = true;
		}
		startPasteService(selectedFileList);
	}

	private void startPasteService(ArrayList<File> selectedFileList) {
		ArrayList<String> sourceList = new ArrayList<String>();
		for(File file : selectedFileList){
			sourceList.add(file.getAbsolutePath());
		}
//		Intent i = new Intent(context, FileOperationService.class);
//		i.putExtra(FileOperationService.INTENT_TYPE, OperationType.PASTE.name());
//		i.putExtra(FileOperationService.PASTE_SOURCE_LIST, sourceList);
//		i.putExtra(FileOperationService.PASTE_DEST_PATH, currentDirectoryPath);
//		context.startService(i);
		Intent intent = new Intent(mActivity.getApplicationContext(), com.fineos.fileexplorer.operations.FileOperationService.class);
		intent.setAction(com.fineos.fileexplorer.operations.FileOperationService.MOVE_ACTION);
		intent.putExtra(com.fineos.fileexplorer.operations.FileOperationService.MOVE_SOURCE_LIST, sourceList);
		intent.putExtra(com.fineos.fileexplorer.operations.FileOperationService.MOVE_DESTATION_DIR, currentDirectoryPath);
		mActivity.startService(intent);
	}

	private boolean isCutFile() {
		return isCutFile;
	}

	public void hidePasteView() {
		currentSelectionState = SelectionState.EXPLORE;
		mActivity.findViewById(R.id.llyout_paste_bar)
				.setVisibility(View.GONE);
	}

	private boolean isCopyingFile() {
		return isCopyFile;
	}

	private void showProcessingDialog() {
		mProcessingDialog = new ProgressDialog(context);
		mProcessingDialog.setMessage(mActivity.getString(R.string.please_wait));
		mProcessingDialog.setCancelable(false);
        mProcessingDialog.show();
	}

	public void dismissProcessDialog() {
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mProcessingDialog != null) {
					mProcessingDialog.dismiss();
				}
			}
		});
    }

	private void copyFile() {
		 //If storage available is not enough
		if (currentStorageInfo != null && currentStorageInfo.availableSize <= 0) {
			Log.d("acmllaugh1", "copyFile: no enough space");
			dismissProcessDialog();
			Toast.makeText(context,
					context.getString(R.string.not_enough_space),
					Toast.LENGTH_SHORT).show();
			clearSelection();
			refresh();
		} else {
			Log.d("acmllaugh1", "copyFile: start copy file service.");
			if(mFileListObject != null){
				mFileListObject.pasteIsDown = true;
			}
			startCopyService(selectedFileList);
		}
	}

	private void startCopyService(ArrayList<File> selectedFileList) {
        Log.d("acmllaugh1", "startCopyService (line 988): start copy service.");
		ArrayList<String> sourceList = new ArrayList<String>();
		for(File file : selectedFileList){
			sourceList.add(file.getAbsolutePath());
		}
        Intent intent = new Intent(mActivity.getApplicationContext(), com.fineos.fileexplorer.operations.FileOperationService.class);
        intent.setAction(com.fineos.fileexplorer.operations.FileOperationService.COPY_ACTION);
        intent.putExtra(com.fineos.fileexplorer.operations.FileOperationService.COPY_SOURCE_LIST, sourceList);
        intent.putExtra(com.fineos.fileexplorer.operations.FileOperationService.COPY_DESTATION_DIR, currentDirectoryPath);
        mActivity.startService(intent);
    }

	private void deleteFiles() {
		startDeleteFileService();
	}

	private void startDeleteFileService() {
		ArrayList<String> deleteList = new ArrayList<String>();
		for(File file : selectedFileList){
			deleteList.add(file.getAbsolutePath());
		}
//		Intent i = new Intent(context, FileOperationService.class);
//		i.putExtra(FileOperationService.INTENT_TYPE, OperationType.DELETE.name());
//		i.putExtra(FileOperationService.DELETE_LIST, deleteList);
//		context.startService(i);
		Intent intent = new Intent(mActivity.getApplicationContext(), com.fineos.fileexplorer.operations.FileOperationService.class);
		intent.setAction(com.fineos.fileexplorer.operations.FileOperationService.DELETE_ACTION);
		intent.putExtra(com.fineos.fileexplorer.operations.FileOperationService.DELETE_PATH_LIST, deleteList);
		mActivity.startService(intent);
	}

	public synchronized void clearSelection() {
        Log.d("acmllaugh1", "clearSelection (line 1031): clear selection.");
		if (selectedFileList != null) {
			selectedFileList.clear();
		}
		fileListAdapter.clearSelectedList();
		for(FileInfo file : currentFileList){
			file.setFileSelected(false);
		}
		isCopyFile = false;
		isCutFile = false;
		if(mFileListObject != null){
			mFileListObject.pasteIsDown = true;
		}
		showCountNum(0);
	}

	public boolean fileCanMove(String path) {
		for (File f : selectedFileList) {
			if (!f.isDirectory())
				continue;
			if (FileUtils.containsPath(f.getPath(), path))
				return false;
		}
		return true;
	}


	public void onOperationSend() {
		buildSelectedFileList();
		for (File f : selectedFileList) {
			if (f.isDirectory()) {
				AlertDialog dialog = new AlertDialog.Builder(context)
						.setMessage(context.getString(R.string.cannot_send_dir)).setPositiveButton(context.getString(R.string.confirm), null)
						.create();
				dialog.show();
				return;
			}
		}
		Intent intent = IntentBuilder.buildSendFile(selectedFileList);
		if (intent != null) {
			try {
				mActivity.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				StringUtils.printLog("FileViewActivityBussiness",
						"fail to view file: " + e.toString());
			}
		}
		clearSelection();
        refresh();
		hideOperationView();
	}

    public void onOperationHotknot() {
		if(!mHotKnotAdapter.isEnabled()) {
			Toast.makeText(mActivity, mActivity.getString(R.string.need_turn_on_hotknot), Toast.LENGTH_SHORT).show();
			showHotKnotSystemSettings();
			return;
		}
        buildSelectedFileList();
        int size = selectedFileList.size();
        if(size <= 0) return;
        String[] paths = new String[size];
        for (int i = 0; i < size; i++) {
            File file = selectedFileList.get(i);
            if (file.isDirectory()) {
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setMessage(context.getString(R.string.cannot_send_dir)).setPositiveButton(context.getString(R.string.confirm), null)
                        .create();
                dialog.show();
                return;
            }
			paths[i] = file.getAbsolutePath();
//            Uri uri = Uri.fromFile(selectedFileList.get(i));
//            uris[i] = uri;
        }
		Intent intent = new Intent(mActivity, HotKnotDialogActivity.class);
		intent.putExtra("type", 2);
		intent.putExtra("filepaths", paths);
		mActivity.startActivity(intent);
//        mHotKnotAdapter.setHotKnotBeamUrisCallback(null, mActivity);
//        mHotKnotAdapter.setHotKnotBeamUris(uris, mActivity);
        clearSelection();
        refresh();
        hideOperationView();
    }

	public OnClickListener getSelectAllButtonListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
                Button button = (Button) v;
                CharSequence action = button.getText();
                if(action.equals(context.getString(R.string.select_all))){
                    fileListAdapter.selectAllFiles();
                    button.setText(context.getString(R.string.un_select_all));
                }else if (action.equals(context.getString(R.string.un_select_all))) {
                    fileListAdapter.clearSelectedList();
                    button.setText(context.getString(R.string.select_all));
                }
                fileListView.invalidateViews();
            }
		};
	}

	public void createNewFolder() {
		if(currentStorageInfo != null && currentStorageInfo.availableSize <= 0){
			Toast.makeText(context, context.getString(R.string.not_enough_space), Toast.LENGTH_SHORT).show();
		}else{
			TextInputDialog dialog = new TextInputDialog(context,
					context.getString(R.string.create_folder),
					context.getString(R.string.create_folder_message),
					context.getString(R.string.new_folder_name),
					new OnFinishListener() {
						@Override
						public boolean onFinish(String text) {
							return doCreateFolder(text);
						}
					}, currentDirectoryPath);
			dialog.show();
		}
	}

	private boolean doCreateFolder(String text) {
		Log.d("acmllaugh1", "doCreateFolder (line 1134): new folder string : " + text);
		if (StringUtils.isVaildFileName(text)){
			startCreateFolderService(text);
//			if (FileUtils.CreateFolder(currentDirectoryPath, text, context)) {
//				openDirectoryInFileList(currentDirectoryPath);
//			} else {
//				new AlertDialog.Builder(context).setMessage(context.getString(R.string.create_file_fail))
//						.setPositiveButton(context.getString(R.string.confirm), null).create().show();
//				return false;
//			}
//			return true;
		}else{
			new AlertDialog.Builder(context).setMessage(context.getString(R.string.invalid_file_name))
			.setPositiveButton(context.getString(R.string.confirm), null).create().show();
			return false;
		}
		return false;
	}

	private void startCreateFolderService(String text) {
		Intent i = new Intent(context, FileOperationService.class);
		i.putExtra(FileOperationService.INTENT_TYPE, OperationType.CREATE_FOLDER.name());
		i.putExtra(FileOperationService.NEW_FOLDER_PATH, currentDirectoryPath + File.separator + text);
		context.startService(i);
	}

	public void setCurrentSelectionState(SelectionState currentSelectionState) {
		this.currentSelectionState = currentSelectionState;
	}

	public void showCountNum(final int i) {
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				selectCountText.setText(context.getString(R.string.show_word_selected)
						+ " " + i + " " + context.getString(R.string.show_word_item));
			}
		});
	}

	public OnClickListener getCancelPasteButtonListener() {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				clearSelection();
				hidePasteView();
			}
		};
	}

	public boolean goParentDirectory() {
        if (currentStorageInfo == null) {
            return false;
        }
        File storagePath = new File(currentStorageInfo.path);
		if(!storagePath.canRead()){
			mActivity.setResult(FileViewActivity.CLOSE_REASON_NO_STORAGE_PATH);
			mActivity.finish();
            mActivity.showActivityGoneAnime();
		}
		String parentPath = StringUtils.getParentPath(currentDirectoryPath,rootPath);
		if(parentPath.isEmpty()){
			return false;
		}else{
			openDirectoryInFileList(parentPath);
			dropLastPathButton();
            scrollToLastPosition();
			return true;
		}
	}

	private void dropLastPathButton() {
		int buttonsCount = mButtonsLayout.getChildCount();
		Button lastButton = (Button) mButtonsLayout.getChildAt(buttonsCount - 1);
		//Set next to last button color (If exist).
		if(buttonsCount > 1){
			Button nextLastButton = (Button)mButtonsLayout.getChildAt(buttonsCount-2);
			nextLastButton.setTextColor(mTextFocusedColor);
		}else{
			Button rootPathButton = (Button) mActivity.findViewById(R.id.button_root_path);
			rootPathButton.setTextColor(mTextFocusedColor);
		}
		mButtonsLayout.removeView(lastButton);
		mButtonsLayout.invalidate();
	}

	public OnKeyListener setDialogOnKeyPressedListener() {
		return new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_MENU
						&& event.getAction() == KeyEvent.ACTION_UP){
					StringUtils
							.printLog(
									"FileExplorerFileViewActivity",
									"menu pressed.");
					clearSelection();
					hideOperationView();
					mActivity.openOptionsMenu();
					return false;
				}else{
					if(keyCode == KeyEvent.KEYCODE_BACK){
						dialog.dismiss();
					}
					return true;
				}
			}
		};
	}

	public void setShowHiddenFiles(boolean b) {
		showHiddenFiles = b;
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putBoolean(SAVED_SHOW_HIDDEN_FILES, b);
		editor.commit();
	}

	public OnItemClickListener getSortItemSelectedListener() {
		return new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				mSortMethod = FileSortHelper.sortArr.get(position);
				saveSortMethod(mSortMethod);
				currentFileList = (ArrayList<FileInfo>) onSortMethodChanged(mSortMethod);
				mActivity.setFileListToListView(currentFileList, currentDirectoryPath);
				mActivity.getSortDialog().dismiss();
			}
		};
	}

    public OnItemClickListener getStorageListItemSelectedListener() {
        return new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                StorageInfo storageInfo = mStorageList.get(position);
                mStorageChooseDialog.dismiss();
                Intent intent = new Intent(mActivity,FileViewActivity.class);
                intent.putExtra(FileStatusFragment.DIRECTORY_PATH_MESSAGE, storageInfo.path);
                mActivity.startActivityForResult(intent, 1);
                mActivity.finish();
                //use slide animation:
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        };
    }


	public List onSortMethodChanged(SortMethod sortMethod) {
		if(mSortHelper == null){
			mSortHelper = new FileSortHelper();
		}
//		StringUtils.printLog("FileViewActivityBussiness", "sortMethod " + sortMethod.name());
		currentFileList = (ArrayList<FileInfo>)mSortHelper
					.setSortMethod(sortMethod)
					.sortFileList(currentFileList);
		return currentFileList;
	}

	public OnClickListener getInformationButtonListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		};
	}


	private void renameFile() {
		if (currentDirectoryPath == null || currentDirectoryPath.isEmpty()) {
			currentDirectoryPath = selectedFileList.get(0).getParentFile().getAbsolutePath();
		}
		Log.d("acmllaugh1", "renameFile: current directory path : " + currentDirectoryPath);
		TextInputDialog dialog = new TextInputDialog(context,
				context.getString(R.string.rename_file),
				context.getString(R.string.rename_file_message),
				selectedFileList.get(0).getName(),
				new OnFinishListener() {
					@Override
					public boolean onFinish(String text) {
						if (selectedFileList.get(0).isDirectory()) {
							doRenameFile(text);
						} else {
							checkIfExtensionChanged(text);
						}
						return true;
					}
				}, currentDirectoryPath);
		dialog.show();
	}

    private void checkIfExtensionChanged(String newFileName) {
        String oldFileName = selectedFileList.get(0).getName();
        int oldExtensionIndex = oldFileName.lastIndexOf('.');
        int newExtensionIndex = newFileName.lastIndexOf('.');
        if(oldExtensionIndex != -1) {
            // Old file has an extension.
            String oldExtension = oldFileName.substring(oldExtensionIndex + 1);
            if (newExtensionIndex != -1) {
                String newExtension = newFileName.substring(newExtensionIndex + 1);
                if (oldExtension.equals(newExtension)) {
                    doRenameFile(newFileName);
                }else {
                    createExtensionAlertDialog(newFileName);
                }
            }else{
                createExtensionAlertDialog(newFileName);
            }
        }else{
           if(newExtensionIndex != -1) {
               createExtensionAlertDialog(newFileName);
           }else{
               doRenameFile(newFileName);
           }
        }
    }

    private void createExtensionAlertDialog(final String newFileName) {
       new AlertDialog.Builder(context)
               .setTitle(context.getString(R.string.extension_change_alert_title))
               .setMessage(context.getString(R.string.extension_change_alert_msg))
               .setPositiveButton(context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       doRenameFile(newFileName);
                   }
               })
               .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       clearSelection();
                       hideOperationView();
                       refresh();
                   }
               })
               .create().show();

    }

    private void doRenameFile(final String text) {
        final File file = selectedFileList.get(0);

        if (text == null || text.isEmpty()) {
            Toast.makeText(mActivity, mActivity.getString(com.fineos.fileexplorer.R.string.rename_empty_string), Toast.LENGTH_SHORT).show();
            return;
        }
		int indexOfDot = text.lastIndexOf(".");
		String filename = text;
		if (indexOfDot != -1) {
			if(file.isDirectory()){
				indexOfDot = text.length();
				Log.i("zhanghe","text.length="+text.length());
			}
		 	filename = text.substring(0, indexOfDot);
		}
		if (filename.isEmpty()) {
			Toast.makeText(mActivity, mActivity.getString(com.fineos.fileexplorer.R.string.rename_empty_string), Toast.LENGTH_SHORT).show();
			return;
		}
		if (text.equals(file.getName())) {
            clearSelection();
            hideOperationView();
            return;
        }
        if (StringUtils.isVaildFileName(text)) {
            clearSelection();
            hideOperationView();
            showProcessingDialog();
            String newFilePath = file.getParent() + File.separator + text;
            startRenameService(file.getAbsolutePath(), newFilePath);
        }
    }

    private void startRenameService(String sourcePath, String newFilePath) {
        Intent intent = new Intent(mActivity.getApplicationContext(), com.fineos.fileexplorer.operations.FileOperationService.class);
        intent.setAction(com.fineos.fileexplorer.operations.FileOperationService.RENAME_FILE);
        intent.putExtra(com.fineos.fileexplorer.operations.FileOperationService.RENAME_SOURCE_FILE, sourcePath);
        intent.putExtra(com.fineos.fileexplorer.operations.FileOperationService.RENAME_TO_FILE, newFilePath);
        mActivity.startService(intent);
    }

    public void setFileListAdapter(FileViewListAdapter fileListAdapter) {
		this.fileListAdapter = fileListAdapter;
	}
	
	public void setFileListView(ListView fileListView) {
		this.fileListView = fileListView;
	}

	public boolean isHiddenFileShowing() {
		return showHiddenFiles;
	}
	
	/**
	 *  Check if service is running.
	 * */
	public boolean isServiceRunning(String serviceClassName){
        final ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
     }

	public void onDestroy() {
		try{
			mActivity.unregisterReceiver(mFileOperationReceiver);
            if (mEventBus.isRegistered(this)) {
                mEventBus.unregister(this);
            }
		}catch(Exception e){
			// Do nothing.
		}
//        try {
//            ImageLoader.getInstance().clearMemoryCache();
//            ImageLoader.getInstance().clearDiskCache();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

	public void checkPasteViewRemain() {
        if (mActivity instanceof CategoryViewActivity) {
            return;
        }
        mFileListObject = mEventBus.getStickyEvent(SelectFileList.class);
        if (mFileListObject != null) {
			Log.d("acmllaugh1", "checkPasteViewRemain: mFileListObject : " + mFileListObject);
			if(mFileListObject.selectedFileList != null)Log.d("acmllaugh1", "checkPasteViewRemain: size : " + mFileListObject.selectedFileList.size());
            if (!mFileListObject.pasteIsDown) {
                selectedFileList = mFileListObject.selectedFileList;
                if (fileListView != null) {
                    showPasteView();
                }
                isCopyFile = mFileListObject.isCopyFile;
                isCutFile = mFileListObject.isCutFile;
            }
        }
    }
}

class SelectFileList{
	public ArrayList<File> selectedFileList;
	public boolean pasteIsDown = false;
	public boolean isCopyFile = false;
	public boolean isCutFile = false;
}

