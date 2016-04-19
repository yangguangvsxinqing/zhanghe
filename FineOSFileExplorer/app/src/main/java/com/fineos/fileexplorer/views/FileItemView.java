package com.fineos.fileexplorer.views;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.adapters.IFileListAdapter;
import com.fineos.fileexplorer.entity.FileInfo;
import com.fineos.fileexplorer.entity.FileInfo.FileCategory;
import com.fineos.fileexplorer.util.FileExplorerSettings;
import com.fineos.fileexplorer.util.ImageLoaderUtil;
import com.fineos.fileexplorer.util.StringUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.lang.ref.WeakReference;

public class FileItemView extends RelativeLayout {

	private FileInfo mFile;
	private Context mContext;
	private IFileListAdapter mAdapter;
	private OnCheckedChangeListener checkBoxListener;

	private CheckBox checkbox;
	private ImageView fileIconView;
	private TextView fileNameTextView;
	private TextView fileLastModifiedTimeTextView;
	private TextView subFileCountTextView;
	private APKIconLoadTask mAPKIconLoadTask;

	@Override
	protected void onFinishInflate() {
		this.fileIconView = (ImageView) this
				.findViewById(R.id.image_file_icon);
		this.fileNameTextView = (TextView) this
				.findViewById(R.id.textview_file_title);
		this.fileLastModifiedTimeTextView = (TextView) this
				.findViewById(R.id.textview_file_date);
		this.subFileCountTextView = (TextView) this
				.findViewById(R.id.textview_file_count);
		this.checkbox = (CheckBox)this.findViewById(R.id.checkbox_file_selected);
		this.checkbox.setOnCheckedChangeListener(getCheckChangedListener());
		super.onFinishInflate();
	}
	
	public OnCheckedChangeListener getCheckChangedListener() {
		if(checkBoxListener == null){
			return new OnCheckedChangeListener() {	
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d("acmllaugh1", "onCheckedChanged: check changed is called.");
					mAdapter.changeFileToSelectedList(
                            new File(mFile.getFilePath()), isChecked);
				}
			};
		}
		return checkBoxListener;
	}

	public void setFile(FileInfo file, IFileListAdapter adapter, Context context) {
		this.mFile = file;
		this.mAdapter = adapter;
		mContext = context.getApplicationContext();
		bindModel();
	}
	
	public FileInfo getFile() {
		return mFile;
	}

	private void bindModel() {
		this.fileNameTextView.setText(StringUtils.getFileShortName(mFile
				.getFileName()));
		this.fileLastModifiedTimeTextView.setText(StringUtils
				.formatDateString(mContext, mFile.getLastModified()));
		int subFilesCount = mFile.getSubFilesCount();
		if(subFilesCount >= 0){
			if(subFilesCount <= 1 && FileExplorerSettings.currentLanguageIsEnglish()){
				this.subFileCountTextView.setText(subFilesCount + getResources().getString(R.string.show_word_item_single));
			}else {
				this.subFileCountTextView.setText(subFilesCount + getResources().getString(R.string.show_word_item));
			}
		}else{
			this.subFileCountTextView.setText(StringUtils.getProperStorageSizeString(mFile.getLength(), getContext()));
		}
		if(mAPKIconLoadTask != null) mAPKIconLoadTask.cancel();
        if (mFile.getFileCategory() == FileCategory.APK) {
			mAPKIconLoadTask = new APKIconLoadTask(mFile.getFilePath());
			mAPKIconLoadTask.start();
            return;
        }
		this.fileIconView.setImageResource(FileCategory
				.getImageResourceByFile(mFile));
		if(mFile.getFileCategory() == FileCategory.PIC){
			String imageUri = "file://" + mFile.getFilePath();
			DisplayImageOptions options = ImageLoaderUtil.getOption();
			int sideLength = mContext.getResources().getDimensionPixelSize(R.dimen.file_view_image_side_length);
			ImageSize targetSize = new ImageSize(sideLength
					, sideLength);
			ImageLoader.getInstance().loadImage(imageUri, targetSize, options, new ImageDisplayListener(mFile.getFilePath()));
		}
    }

	private void showAPKIcon() {
		PackageManager pm = mContext.getPackageManager();
		PackageInfo pi = pm.getPackageArchiveInfo(mFile.getFilePath(), 0);
		if (pi == null || pi.applicationInfo == null) {
			this.fileIconView.setImageResource(R.drawable.ic_apks);
		}else{
			// the secret are these two lines....
			pi.applicationInfo.sourceDir       = mFile.getFilePath();
			pi.applicationInfo.publicSourceDir = mFile.getFilePath();
			Drawable apkIcon = pi.applicationInfo.loadIcon(pm);
			fileIconView.setImageDrawable(apkIcon);
		}
	}


	public FileItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FileItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FileItemView(Context context) {
		super(context);
	}

	public void setCheckBoxVisibility(boolean isOperationState) {
		if(isOperationState == true){
			this.checkbox.setVisibility(View.VISIBLE);
		}else{
			this.checkbox.setVisibility(View.GONE);
		}
		
	}
	
	public void setCheckBoxChecked(boolean ischecked){
		this.checkbox.setChecked(ischecked);
		mFile.setFileSelected(ischecked);
	}

	public void dropCheckBoxListener() {
		this.checkbox.setOnCheckedChangeListener(null);
	}

	public void addCheckBoxListener() {
		this.checkbox.setOnCheckedChangeListener(getCheckChangedListener());	
	}
	
	public boolean isCheckBoxChecked(){
		return this.checkbox.isChecked();
	}


	public void onLoadAPKIconComplete(final Drawable icon, final String filepath) {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (mFile.getFilePath().equals(filepath)) {
					fileIconView.setImageDrawable(icon);
				}
			}
		});
	}


	private  class ImageDisplayListener extends SimpleImageLoadingListener {

		private String mFilePath;

		public ImageDisplayListener(String filePath) {
			mFilePath = filePath;
		}

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (mFilePath.equals(mFile.getFilePath())) {
				if(fileIconView != null) fileIconView.setImageBitmap(loadedImage);
			}
		}
	}


	private class APKIconLoadTask extends Thread {
		String apkPath;
		boolean cancelFlag;

		public APKIconLoadTask(String filepath) {
			apkPath = filepath;
		}

		@Override
		public void run() {
			super.run();
			try {
				PackageManager pm = mContext.getPackageManager();
				PackageInfo pi = pm.getPackageArchiveInfo(mFile.getFilePath(), 0);
				if(cancelFlag) return;
				if (pi == null || pi.applicationInfo == null) {
                    onLoadAPKIconComplete(getResources().getDrawable(R.drawable.ic_apks, null), apkPath);
                }else{
                    // the secret are these two lines....
                    pi.applicationInfo.sourceDir       = mFile.getFilePath();
                    pi.applicationInfo.publicSourceDir = mFile.getFilePath();
                    Drawable apkIcon = pi.applicationInfo.loadIcon(pm);
					if(cancelFlag) return;
                    onLoadAPKIconComplete(apkIcon, apkPath);
                }
			} catch (Resources.NotFoundException e) {
				e.printStackTrace();
				onLoadAPKIconComplete(getResources().getDrawable(R.drawable.ic_apks, null), apkPath);
			}
		}

		public void cancel() {
			cancelFlag = true;
		}
	}


}
