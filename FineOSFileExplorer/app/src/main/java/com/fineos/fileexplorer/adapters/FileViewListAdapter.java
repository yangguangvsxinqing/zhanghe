package com.fineos.fileexplorer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.fineos.fileexplorer.R;
import com.fineos.fileexplorer.bussiness.FileViewActivityBussiness;
import com.fineos.fileexplorer.bussiness.FileViewActivityBussiness.SelectionState;
import com.fineos.fileexplorer.entity.FileInfo;
import com.fineos.fileexplorer.util.ImageLoaderUtil;
import com.fineos.fileexplorer.views.FileItemView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class FileViewListAdapter extends ArrayAdapter<FileInfo> implements IFileListAdapter{
	private final Context context;
	private ArrayList<FileInfo> fileList;
	private ArrayList<File> selectedFileList;

	private IFileViewBussiness mBussiness;

	public FileViewListAdapter(Context context,
			int textViewResourceId, ArrayList<FileInfo> fileList,
			IFileViewBussiness bussiness) {
		super(context, textViewResourceId, fileList);
		this.context = context.getApplicationContext();
		this.fileList = fileList;
		this.mBussiness = bussiness;
		ImageLoaderUtil.initImageLoader(context);
	}

	public ArrayList<FileInfo> getFileList(){
		return this.fileList;
	}


	@Override
	public int getCount() {
		if (fileList == null || fileList.size() == 0) {
			return 0;
		}
		return fileList.size();
	}

	/*show files on the file list view. */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(position >= fileList.size()){
			return convertView;
		}
		FileInfo file = fileList.get(position);
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView  = layoutInflater.inflate(
					R.layout.file_item, parent, false);
		}
		FileItemView fileItemView = (FileItemView)convertView;
		fileItemView.setFile(file, this, context);
		//See if check box is need to show on screen.
		if(mBussiness.getCurrentSelectionState() == SelectionState.OPERATION){
			fileItemView.setCheckBoxVisibility(true);
		}else{
			fileItemView.setCheckBoxVisibility(false);
		}
		//Set correct state of check box while not trigger the check changed event.
		fileItemView.dropCheckBoxListener();
		if(file.isFileSelected()){
			fileItemView.setCheckBoxChecked(true);
		}else{
			fileItemView.setCheckBoxChecked(false);
		}
		fileItemView.addCheckBoxListener();

		return convertView;
	}

	@Override
	public void clear() {
		super.clear();
		if(this.selectedFileList != null){
			selectedFileList.clear();
		}

	}

	@Override
	public void add(FileInfo object) {
		super.add(object);
	}

	@Override
	public void addAll(Collection<? extends FileInfo> collection) {
		super.addAll(collection);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	public void changeFileToSelectedList(File file, boolean isChecked) {
		if(this.selectedFileList == null){
			selectedFileList = new ArrayList<File>();
		}
		if(isChecked && !isInSelectedList(file)){
			selectedFileList.add(file);
		}else{
			removeFileFromSelectedList(file);
			selectedFileList.remove(file);
		}
		updateSelectAllTextView();
		mBussiness.showCountNum(selectedFileList.size());
	}

	private void updateSelectAllTextView() {
		if (selectedFileList.size() == getCount()) {
			mBussiness.showDeSelectAll();
		}else{
			mBussiness.showSelectAll();
		}
	}

	private void removeFileFromSelectedList(File file) {
		if(selectedFileList == null){
			return;
		}
		ArrayList<File> removeList = new ArrayList<File>();
		for(File selectedfile : selectedFileList){
			if(selectedfile.getPath().equals(file.getPath())){
				removeList.add(selectedfile);
			}
		}
		selectedFileList.removeAll(removeList);
	}

	public boolean isInSelectedList(File file) {
		if(selectedFileList == null){
			return false;
		}
		for(File selectedfile : selectedFileList){
			if(selectedfile.getPath().equals(file.getPath())){
				return true;
			}
		}
		return false;
	}

	public void clearSelectedList(){
		if(selectedFileList != null){
			this.selectedFileList.clear();
		}
        if (fileList != null) {
            for (FileInfo fileInfo : fileList) {
                fileInfo.setFileSelected(false);
            }
        }
        mBussiness.showCountNum(0);
    }

	public void selectAllFiles(){
        if (fileList == null) {
            return;
        }
        if (selectedFileList == null) {
            selectedFileList = new ArrayList<File>();
        }
        selectedFileList.clear();
        for (FileInfo fileInfo : fileList) {
            fileInfo.setFileSelected(true);
            File file = new File(fileInfo.getFilePath());
            changeFileToSelectedList(file, true);
        }
	}

	public ArrayList<File> getSelectedFiles() {
		return selectedFileList;
	}

	public void addFileToSelectedList(int position) {
		if(selectedFileList == null){
			selectedFileList = new ArrayList<File>();
		}
		selectedFileList.add(new File(fileList.get(position).getFilePath()));
		fileList.get(position).setFileSelected(true);
	}


    public void updateTime() {
        this.notifyDataSetInvalidated();
    }
}
