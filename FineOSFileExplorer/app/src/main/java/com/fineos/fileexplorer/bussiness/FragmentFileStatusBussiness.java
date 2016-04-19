package com.fineos.fileexplorer.bussiness;

import android.content.Context;

import com.fineos.fileexplorer.entity.FileInfo.FileCategory;
import com.fineos.fileexplorer.entity.StorageInfo;
import com.fineos.fileexplorer.service.IMediaFileInfoHelper;
import com.fineos.fileexplorer.service.IMediaFileInfoHelper.MediaCategoryInfo;
import com.fineos.fileexplorer.service.MediaFileInfoHelper;
import com.fineos.fileexplorer.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author xiaoyue
 * 本类职责是提供各种方法来处理FileStatusFragment中的业务逻辑，调用下层接口实现系统功能，
 * 将业务逻辑和页面布局分离。
 */

public class FragmentFileStatusBussiness {
	public static final String TAG = "FragmentFileStatusBussiness";
	public ArrayList<MediaCategoryInfo> mediaFileInfoList;//查找到的各类媒体文件信息列表
	private StorageInfo storageInfo;
	
	public List<MediaCategoryInfo> searchMediaFileInfo(Context context){
		//获取内部存储中 某一类 媒体文件信息的数量和占用空间		
		ArrayList<MediaCategoryInfo> categoryInfoList = new ArrayList<MediaCategoryInfo>();
		for(FileCategory fileCategory : FileCategory.values()){
			if(!fileCategory.name().equals("OTHER")){
				MediaCategoryInfo mediaFileInfo;
				IMediaFileInfoHelper mediaInfoHelper = MediaFileInfoHelper.getInstance(context);
				mediaFileInfo = mediaInfoHelper.getMediaFileInfo(fileCategory);
				if(mediaFileInfo != null){
					categoryInfoList.add(mediaFileInfo);
				}
			}
		}	
		return categoryInfoList;		
	}
	
	public void setStorageInfo(StorageInfo storageInfo){
		this.storageInfo = storageInfo;
	}
	

	public ArrayList<MediaCategoryInfo> getMediaFileInfoList() {
		return mediaFileInfoList;
	}


	
}
