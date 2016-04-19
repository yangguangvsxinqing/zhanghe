package com.fineos.fileexplorer.fragments;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.activity.CategoryViewActivity;
import com.fineos.fileexplorer.activity.FileViewActivity;
import com.fineos.fileexplorer.bussiness.FragmentFileStatusBussiness;
import com.fineos.fileexplorer.entity.FileInfo;
import com.fineos.fileexplorer.entity.StorageInfo;
import com.fineos.fileexplorer.service.AbstractStorageInfoFinder;
import com.fineos.fileexplorer.service.IMediaFileInfoHelper.MediaCategoryInfo;
import com.fineos.fileexplorer.service.StorageInfoFinderSystemService;
import com.fineos.fileexplorer.util.StringUtils;
import com.fineos.fileexplorer.views.CategoryBar;
import com.fineos.fileexplorer.views.CategoryItemView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class FileStatusFragment extends Fragment implements OnClickListener {

    private static final String TAG = "FileStatusFragment";

    private static final String KEY_CONTENT = "TestFragment:Content";
    private static final String EMPTY_PATH = "empty path";
    public static final String DIRECTORY_PATH_MESSAGE = "directoryPath";
    public static final String STORAGE_INFO_MESSAGE = "mStorageInfoList";
//    private static final String CLEANING_TOOLS_PACKAGE_NAME = "com.fineos.cleanningtools";
    private static final String CLEANING_TOOLS_PACKAGE_NAME = "com.fineos.systemmanager";

    public static final String CATEGORY_FLAG = "VIEW_CATEGORY";
    public static final String OPEN_DIRECTORY_ACTION = "com.fineos.fileexplorer.OPEN_DIRECTORY";

    protected static final String SCAN_TYPE = "scan_type";

    protected static final String DIRECTLY_SCAN = "directly_scan";

    private View mRootView;
    private FragmentFileStatusBussiness fileStatusBussiness;
    private ArrayList<MediaCategoryInfo> mMediaInfoList = new ArrayList<MediaCategoryInfo>();
    private ArrayList<StorageInfo> mStorageInfoList;
    private AbstractStorageInfoFinder storageInfoFinder;
    private int mResultCode = 0;
    private PackageManager mPackageManager;
    private CategoryBar mCategoryBar;
    private boolean mCleanToolsInstalled;
    private ImageButton mCleanButton;
    private ListView mStorageManageButtonListView;
    private StorageListAdapter mStorageAdapter;
    private InitFileStatusTask mFileStatusTask;
    private TextView mStorageStateTextView;
    private HashMap<FileInfo.FileCategory, TextView> mCategoryInfoMap;
    private LinearLayout mCenterLinearLayout;


    public static FileStatusFragment newInstance(ArrayList<StorageInfo> storageInfoList) {
        FileStatusFragment fragment = new FileStatusFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("storageInfoList", storageInfoList);
        fragment.setArguments(args);
        return fragment;
    }

    private String mContent = "???";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_file_status, container,
                false);
        initViews();
        if (getStorageInfoFromMainActivity()) {
            initCategoryBar();
            storageInfoFinder = new StorageInfoFinderSystemService(getActivity());
            setUpFragmentLayout();
            mStorageAdapter.setStorageList(mStorageInfoList);
            setUpCleanImageButton();
            startInitFileStatus();
        }
        return mRootView;
    }

    private void initViews() {
        mCleanButton = (ImageButton) (mRootView.findViewById(R.id.button_clean_files));
        mStorageManageButtonListView = (ListView) mRootView.findViewById(R.id.listview_storage_list);
        mStorageAdapter = new StorageListAdapter(getActivity().getApplicationContext());
        mStorageManageButtonListView.setAdapter(mStorageAdapter);
        mStorageStateTextView = (TextView) mRootView
                .findViewById(R.id.textview_available_space);
        mCenterLinearLayout = (LinearLayout) mRootView.findViewById(R.id.llayout_center);
        fillCategoryHashMap();
    }

    private void fillCategoryHashMap() {
        mCategoryInfoMap = new HashMap<FileInfo.FileCategory, TextView>();
        mCategoryInfoMap.put(FileInfo.FileCategory.PIC, (TextView) mRootView.findViewById(R.id.textview_pic_info));
        mCategoryInfoMap.put(FileInfo.FileCategory.MUSIC, (TextView) mRootView.findViewById(R.id.textview_music_info));
        mCategoryInfoMap.put(FileInfo.FileCategory.VIDEO, (TextView) mRootView.findViewById(R.id.textview_videos_info));
        mCategoryInfoMap.put(FileInfo.FileCategory.APK, (TextView) mRootView.findViewById(R.id.textview_apk_info));
        mCategoryInfoMap.put(FileInfo.FileCategory.DOC, (TextView) mRootView.findViewById(R.id.textview_doc_info));
        mCategoryInfoMap.put(FileInfo.FileCategory.ZIP, (TextView) mRootView.findViewById(R.id.textview_zip_info));
        mCategoryInfoMap.put(FileInfo.FileCategory.OTHER, (TextView) mRootView.findViewById(R.id.textview_other_info));
    }

    private void setUpFragmentLayout() {
        //TODO: set view_holder and button_clean_files to let layout comfortable.
        layoutCleanButton();
//        layoutStorageInfo();
    }

    private void layoutCleanButton() {
        mPackageManager = getActivity().getPackageManager();
        ApplicationInfo cleaningToolsAppInfo = null;
        try {
            cleaningToolsAppInfo = mPackageManager.getApplicationInfo(
                    CLEANING_TOOLS_PACKAGE_NAME, PackageManager.GET_META_DATA);
            if (cleaningToolsAppInfo != null) {
                mCleanToolsInstalled = true;
            } else {
                mCleanToolsInstalled = false;
            }
        } catch (NameNotFoundException e) {
            //Log.d("FileStatusFragment", "垃圾清理没有安装...");
            mCleanButton.setVisibility(View.GONE);
            mCleanToolsInstalled = false;
            //e.printStackTrace();
        }
    }

    public void startInitFileStatus() {
        if (mFileStatusTask != null) {
            mFileStatusTask.cancel(true);
        }
        mFileStatusTask = new InitFileStatusTask();
        mFileStatusTask.execute();
    }

    public void stopTask() {
        if (mFileStatusTask != null) {
            mFileStatusTask.cancel(true);
        }
    }

    /**
     * Get storage info that is passed by main activity.
     * Before: Main activity has already got storage information and put it in Bundle.
     * After : Local variable mStorageInfoList gets its information and is available to be used.
     */

    private boolean getStorageInfoFromMainActivity() {
        Bundle args = getArguments();
        if (args != null) {
            mStorageInfoList = args.getParcelableArrayList("storageInfoList");
            return true;
        }
        return false;
    }

    private void setUpCleanImageButton() {
        if (mCleanToolsInstalled) {
            double usedPersentDouble = getUsedPersentDouble();
            setDrawableForCleanButton(usedPersentDouble, mCleanButton);
            mCleanButton.setVisibility(View.VISIBLE);
            mCleanButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(CLEANING_TOOLS_PACKAGE_NAME, "com.fineos.systemmanager.cleanningtools.CleanningToolsActivity"));
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }
    }

    private double getUsedPersentDouble() {
        double usedPersentDouble = 1 - ((float) getStorageAvailableSpace() / (float) getStorageTotalSpace());
        return usedPersentDouble;
    }

    private void setDrawableForCleanButton(Double usedPersentDouble, ImageButton cleanButton) {
        //Log.d(TAG, "current usedPersentDouble is : " + usedPersentDouble);
        if (usedPersentDouble > 0.3 && usedPersentDouble < 0.7) {
            cleanButton.setImageDrawable(getResources().getDrawable(R.drawable.selector_yellow_button));
        } else if (usedPersentDouble >= 0.7) {
            cleanButton.setImageDrawable(getResources().getDrawable(R.drawable.selector_red_button));
        } else {
            cleanButton.setImageDrawable(getResources().getDrawable(R.drawable.selector_green_button));
        }
    }

    private void setCategoriesViews() {
        setStorageStateTextView();
        setCategorySizeTextViews();
//        LinearLayout layout = (LinearLayout) mRootView.findViewById(R.id.linearlayout_category_spec);
//        layout.setVisibility(View.VISIBLE);

    }

    private void setCategorySizeTextViews() {
        for (MediaCategoryInfo mediaCategoryInfo : mMediaInfoList) {
            FileInfo.FileCategory category = mediaCategoryInfo.getMediaCategory();
            TextView categoryCountTextView = mCategoryInfoMap.get(category);
            String categoryCountTextString = getCategoryCountTextString(mediaCategoryInfo);
            if (isRightToLeft()) {
                categoryCountTextView.setTextDirection(View.TEXT_DIRECTION_RTL);
                categoryCountTextView.setGravity(Gravity.RIGHT);
                categoryCountTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, getCategoryDrawable(category), null);
                categoryCountTextView.setMinLines(2);
            }
            categoryCountTextView.setText(categoryCountTextString);
        }
        setOthersCategoryTextView();
    }

    private Drawable getCategoryDrawable(FileInfo.FileCategory category) {
        switch (category) {
            case PIC:
                return getResources().getDrawable(R.drawable.color_picture, null);
            case MUSIC:
                return getResources().getDrawable(R.drawable.color_music, null);
            case VIDEO:
                return getResources().getDrawable(R.drawable.color_video, null);
            case APK:
                return getResources().getDrawable(R.drawable.color_apks, null);
            case DOC:
                return getResources().getDrawable(R.drawable.color_doc, null);
            case ZIP:
                return getResources().getDrawable(R.drawable.color_zip, null);
        }
        return getResources().getDrawable(R.drawable.color_other, null);
    }

    private boolean isRightToLeft() {
        Configuration config = getResources().getConfiguration();
        return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private void setStorageStateTextView() {
        String storageStateTitleString = getStateTitleString();
        mStorageStateTextView.setText(storageStateTitleString);
    }

    private void setOthersCategoryTextView() {
        TextView othersCategorySizeTextView = mCategoryInfoMap.get(FileInfo.FileCategory.OTHER);
        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.other_text))
                .append(" : ")
                .append(StringUtils.getProperStorageSizeString(getOtherFilesLength(), getActivity()));
        if (isRightToLeft()) {
            othersCategorySizeTextView.setTextDirection(View.TEXT_DIRECTION_RTL);
            othersCategorySizeTextView.setGravity(Gravity.RIGHT);
            othersCategorySizeTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, getCategoryDrawable(FileInfo.FileCategory.OTHER), null);
        }
        othersCategorySizeTextView.setText(builder);
    }

    private String getCategoryCountTextString(MediaCategoryInfo mediaCategoryInfo) {
        StringBuilder infoTextBuilder = new StringBuilder();
        FileInfo.FileCategory category = mediaCategoryInfo.getMediaCategory();
        switch (category) {
            case PIC:
                infoTextBuilder.append(getString(R.string.picture_text));
                break;
            case MUSIC:
                infoTextBuilder.append(getString(R.string.music_text));
                break;
            case VIDEO:
                infoTextBuilder.append(getString(R.string.video_text));
                break;
            case APK:
                infoTextBuilder.append(getString(R.string.apk_text));
                break;
            case DOC:
                infoTextBuilder.append(getString(R.string.doc_text));
                break;
            case ZIP:
                infoTextBuilder.append(getString(R.string.zip_text));
                break;
        }
        infoTextBuilder.append(": ").append(StringUtils.getProperStorageSizeString(mediaCategoryInfo.getMediaSize(), getActivity()));
        return infoTextBuilder.toString();
    }


    private String getStateTitleString() {
        String textBase = getString(R.string.storage_info_title);
        String storageName = getString(R.string.storage_name);
        String availableSpace = StringUtils.getProperStorageSizeString(getStorageAvailableSpace(), getActivity());
        String totalSize = StringUtils.getProperStorageSizeString(getStorageTotalSpace(), getActivity());
        Log.d("acmllaugh1", "getStateTitleString (line 269): total size text : " + totalSize);
        return String.format(textBase, storageName, availableSpace, totalSize);
    }

    private Long getStorageTotalSpace() {
        long total = 0l;
        for (StorageInfo storageInfo : mStorageInfoList) {
            total += storageInfo.getTotalSize();
        }
        return total;
    }

    private long getStorageAvailableSpace() {
        long total = 0l;
        for (StorageInfo storageInfo : mStorageInfoList) {
            total += storageInfo.getAvailableSize();
        }
        return total;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }

    private ArrayList<MediaCategoryInfo> getFileCategoryInfo() {
        fileStatusBussiness = new FragmentFileStatusBussiness();
        mMediaInfoList = (ArrayList<MediaCategoryInfo>)
                fileStatusBussiness.searchMediaFileInfo(getActivity());
        return mMediaInfoList;
    }

    @Override
    public void onClick(View v) {
        //open category view
        CategoryItemView categoryItemView = (CategoryItemView) v;
        Intent intent = new Intent(getActivity(), CategoryViewActivity.class);
        intent.putExtra(CATEGORY_FLAG, categoryItemView.getmCategoryID());
        startActivityForResult(intent, 1);
        //use slide animation:
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

    }

    public void updateStorageInfoList(ArrayList<StorageInfo> storageInfoList) {
        mStorageInfoList = storageInfoList;
        mStorageAdapter.setStorageList(storageInfoList);
        startInitFileStatus();
    }

    public class InitFileStatusTask extends
            AsyncTask<Void, Integer, ArrayList<MediaCategoryInfo>> {

        protected ArrayList<MediaCategoryInfo> doInBackground(Void... params) {
            return getFileCategoryInfo();// 获取各类型文件的信息
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(ArrayList<MediaCategoryInfo> resultList) {
            mMediaInfoList = resultList;
            if (!isAdded()) return; // The fragment is not attached to activity, so do nothing.
            if (mMediaInfoList == null || mMediaInfoList.size() == 0) {
                mRootView.setVisibility(View.INVISIBLE);
                getActivity().findViewById(R.id.llayout_no_sd).setVisibility(View.VISIBLE);
            } else {
                mRootView.setVisibility(View.VISIBLE);
                setupCategoryIcon();
                setCategoriesViews();
                setUpCategoryBar();
                determineCenterLayoutHeight();
            }
        }
    }

    private void determineCenterLayoutHeight() {
        ViewGroup.LayoutParams params = mCenterLinearLayout.getLayoutParams();
        float layoutStartPoint = mCenterLinearLayout.getY();
        float layoutEndPoint = mStorageManageButtonListView.getY();
        params.height = Math.round(layoutEndPoint - layoutStartPoint);
        mCenterLinearLayout.setLayoutParams(params);
    }

    private void setUpCategoryBar() {
        if (mCategoryBar == null) {
            mCategoryBar = (CategoryBar) mRootView.findViewById(R.id.category_bar);
        }
        mCategoryBar.setFullValue(getStorageTotalSpace());
        int count = mMediaInfoList.size();
        for (int i = 0; i < count; i++) {
            MediaCategoryInfo categoryInfo = mMediaInfoList.get(i);
            mCategoryBar.setCategoryValue(i, categoryInfo.getMediaSize());
        }
        mCategoryBar.setCategoryValue(6, getOtherFilesLength());
        mCategoryBar.startAnimation();
    }

    private long getOtherFilesLength() {
        long total = getStorageTotalSpace();
        long mediaFilesLength = 0;
        for (MediaCategoryInfo categoryInfo : mMediaInfoList) {
            mediaFilesLength += categoryInfo.getMediaSize();
        }
        return total - getStorageAvailableSpace() - mediaFilesLength;
    }

    private void initCategoryBar() {
        mCategoryBar = (CategoryBar) mRootView.findViewById(R.id.category_bar);
        mCategoryBar.setSaveEnabled(true);//save configs for mCategoryBar. by acmllaugh
        int[] imgs = new int[]{
                R.drawable.category_bar_picture, R.drawable.category_bar_music,
                R.drawable.category_bar_video, R.drawable.category_bar_document,
                R.drawable.category_bar_apk, R.drawable.category_bar_zip, R.drawable.category_bar_other
        };
        for (int i = 0; i < imgs.length; i++) {
            mCategoryBar.addCategory(imgs[i]);
        }
    }

    private void setupCategoryIcon() {
        TableLayout categoryTable = (TableLayout) mRootView.findViewById(R.id.table_categories);
        int tableChildCount = categoryTable.getChildCount();
        for (int i = 0; i < tableChildCount; i++) {
            TableRow row = (TableRow) categoryTable.getChildAt(i);
            int rowCount = row.getChildCount();
            if (rowCount == 3) {// 3 means this row is a row of category items. Otherwise this row may be a divider.
                for (int j = 0; j < rowCount; j++) {
                    CategoryItemView categoryItem = (CategoryItemView) row.getChildAt(j);
                    for (MediaCategoryInfo categoryInfo : mMediaInfoList) {
                        String name = categoryInfo.getMediaCategory().name();
                        if (categoryItem.getmCategoryID().equals(name)) {
                            categoryItem.setCategoryCount(categoryInfo.getMediaCount());
                        }
                    }
                    categoryItem.setOnClickListener(this);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mResultCode = resultCode;
        super.onActivityResult(requestCode, resultCode, data);
    }
//
//	private void refreshAvailableSpace() {
//		File file = new File(mStorageInfoList.path);
//		if(file.canRead()){
//			this.mStorageInfoList.availableSize = storageInfoFinder.getStorageFreeSize(
//					mStorageInfoList.path);
//		}
//	}

    @Override
    public void onResume() {
        super.onResume();
        if (mResultCode == FileViewActivity.CLOSE_REASON_NO_STORAGE_PATH) {
            return;
        }
        refreshStorageSpaceInfo();
        startInitFileStatus();
    }

    private void refreshStorageSpaceInfo() {
        if (mStorageInfoList != null && mStorageInfoList.size() > 0) {
            for (StorageInfo storageInfo : mStorageInfoList) {
                File file = new File(storageInfo.path);
                if (file.exists() && file.canRead()) {
                    storageInfo.availableSize = storageInfoFinder.getStorageFreeSize(storageInfo.path);
                }
            }
        }
    }


    private class StorageListAdapter extends BaseAdapter{

        private List<StorageInfo> adapterStorageList;
        private Context context;

        public StorageListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            if(adapterStorageList == null) return 0;
            return adapterStorageList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(position >= adapterStorageList.size()){
                return convertView;
            }
            final StorageInfo info = adapterStorageList.get(position);
            Holder holder;
            if (convertView == null) {
               LayoutInflater layoutInflater = (LayoutInflater) context
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
               convertView  = layoutInflater.inflate(
                            R.layout.item_storage, null);
               holder = new Holder();
               holder.storageButton = (Button) convertView.findViewById(R.id.button_storage);
               convertView.setTag(holder);
            }else{
               holder = (Holder) convertView.getTag();
            }
            holder.storageButton.setText(info.getStorageName());
            if (adapterStorageList.size() > 1) {
                holder.storageButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.getResources().getDimensionPixelSize(R.dimen.storage_button_height_small)));
                holder.storageButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.storage_text_size_small));
            }else{
                holder.storageButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.getResources().getDimensionPixelSize(R.dimen.storage_button_height_big)));
                holder.storageButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.storage_text_size_big));
            }
            holder.storageButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!checkStorageAvailable(info.path))return;
                    stopTask();
                    Intent intent = new Intent(getActivity(), FileViewActivity.class);
                    intent.setAction(OPEN_DIRECTORY_ACTION);
                    intent.putExtra(DIRECTORY_PATH_MESSAGE, info.path);
                    intent.putExtra(STORAGE_INFO_MESSAGE, mStorageInfoList);
                    startActivityForResult(intent, 1);
                    //use slide animation:
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
            return convertView;
        }

        public void setStorageList(List<StorageInfo> infos) {
            adapterStorageList = infos;
            notifyDataSetChanged();
        }

    }

    private boolean checkStorageAvailable(String path) {
        File file = new File(path);
        if(file.exists() && file.canRead() && file.canWrite()) return true;
        return false;
    }


    private class Holder{
        private Button storageButton;
    }

}
