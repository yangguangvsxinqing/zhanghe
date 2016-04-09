package com.fineos.fileexplorer.service;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;

import com.fineos.fileexplorer.entity.FileInfo.FileCategory;

import java.util.List;

public interface IFileSearchHelper {
	
	/**
	 * this method get all the media file path in the given storage:
	 */
	public List<String> getMediaFileInfoListByCategory(FileCategory fileCategory);
	
	/**
	 * this method do a fuzzy search by given string need to query, startPoint starts from 0,
	 * and step need to be defined in implement class:
	 */
	public List<String> fuzzyQuery(String queryString, int startPoint, int searchStep);
	
	/**
	 * 
	 * @param queryString String need to be queried.
	 * @return Database cursor by fuzzy query.
	 */
	public CursorLoader fuzzyQuery(String queryString);
	
	
	/**
	 * this method return the count num of latest fuzzy query.
	 */
	public int getResultCount();
	
	public Loader<Cursor> queryCategory(FileCategory mFileCategory);

	CursorLoader fuzzyQuery(String queryString, FileCategory mFileCategory);
}
