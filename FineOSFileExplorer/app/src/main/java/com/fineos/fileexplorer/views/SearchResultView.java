package com.fineos.fileexplorer.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.entity.FileInfo;
import com.fineos.fileexplorer.entity.FileInfo.FileCategory;
import com.fineos.fileexplorer.util.ImageLoaderUtil;
import com.fineos.fileexplorer.util.StringUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.HashMap;

public class SearchResultView extends RelativeLayout {

	private FileInfo mFileInfo;
	private TextView resultFileNameTextView;
	private TextView resultFilePathTextView;
	private ImageView resultImageView;
	private Context mContext;
	public static final String LOAD_THUMBNAIL = "load_thumbnail";
	private SimpleImageLoadingListener mImageLoadListener = new ImageDisplayListener();
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		this.resultFilePathTextView = (TextView) this
				.findViewById(R.id.textview_file_path);
		this.resultFileNameTextView = (TextView) this
				.findViewById(R.id.textview_file_name);
		this.resultImageView = (ImageView) this
				.findViewById(R.id.imageview_search_result_file_icon);
	}

	public void setFileInfo(FileInfo file, Context context, HashMap<Long, Bitmap> thumbnailHashMap) {
		this.mFileInfo = file;
		mContext = context.getApplicationContext();
		bindModel();
	}
	
	public File getFile() {
		return new File(mFileInfo.getFilePath());
	}

	private void bindModel() {
		//Log.d("SearchResultView", "start bind view : " + ++mBoundCount);
		this.resultFileNameTextView.setText(StringUtils.getFileShortName(mFileInfo.getFileName()));
        String pathForShown = StringUtils.getProperPathString(mFileInfo.getFilePath(), getContext());
        this.resultFilePathTextView.setText(StringUtils.getFileShortPath(pathForShown));
		if(mFileInfo.getFileCategory() == FileCategory.PIC){
			String imageUri = "file://" + mFileInfo.getFilePath();
			DisplayImageOptions options = ImageLoaderUtil.getOption();
			ImageLoader.getInstance().displayImage(imageUri, resultImageView, options, mImageLoadListener);
		}else{	
			resultImageView
			.setImageResource(FileCategory.getImageResourceByFile(mFileInfo));
		}
	}
	
	public SearchResultView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SearchResultView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SearchResultView(Context context) {
		super(context);
	}
	
	private  class ImageDisplayListener extends SimpleImageLoadingListener {

//		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());
		
		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		   if(mFileInfo.getFileCategory() != FileCategory.PIC){
			   ((ImageView)view)
				.setImageResource(FileCategory.getImageResourceByFile(mFileInfo));
		   }
		}
	}
}
