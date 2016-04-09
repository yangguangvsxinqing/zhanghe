package com.fineos.fileexplorer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.entity.FileInfo;
import com.fineos.fileexplorer.util.ImageLoaderUtil;
import com.fineos.fileexplorer.views.SearchResultView;

import java.io.File;
import java.util.HashMap;

public class SearchCursorAdapter extends CursorAdapter {
	private LayoutInflater cursorInflater;
	private static HashMap<Long, Bitmap> mThumbnailHashSet = new HashMap<Long, Bitmap>();

	public SearchCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
		ImageLoaderUtil.initImageLoader(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		SearchResultView searchResultView = (SearchResultView)view;
		String filepath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
		FileInfo fileInfo = new FileInfo( new File(filepath), true);
		fileInfo.setDbId(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)));
		searchResultView.setFileInfo(fileInfo, context, mThumbnailHashSet);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return cursorInflater.inflate(R.layout.viewlist_search_result, parent, false);
	}

}
