package com.fineos.fileexplorer.service;

import com.fineos.fileexplorer.entity.FileInfo.FileCategory;

/**
 * 
 * @author Xiao Yue
 * This interface defines functionality of class which returns information list of a media type
 * (or FileCategory type).
 * 
 * */
public interface IMediaFileInfoHelper {
	//this method get one particular category file statistic info and return it:
	public MediaCategoryInfo getMediaFileInfo(FileCategory fileCategory);
		
    public class MediaCategoryInfo{
    	private FileCategory mediaCategory;
		private long mediaCount;
    	private long mediaSize;
    	
    	public MediaCategoryInfo(FileCategory mediaCategory, long mediaCount, long mediaSize) {
			this.mediaCategory = mediaCategory;
			this.mediaCount = mediaCount;
			this.mediaSize = mediaSize;
		}
    	
    	public MediaCategoryInfo(){ 		
    	}
    	
    	public FileCategory getMediaCategory() {
			return mediaCategory;
		}

		public void setMediaCategory(FileCategory mediaCategory) {
			this.mediaCategory = mediaCategory;
		}

		public long getMediaCount() {
			return mediaCount;
		}

		public void setMediaCount(long mediaCount) {
			this.mediaCount = mediaCount;
		}

		public long getMediaSize() {
			return mediaSize;
		}

		public void setMediaSize(long mediaSize) {
			this.mediaSize = mediaSize;
		}
    	
    }
}
