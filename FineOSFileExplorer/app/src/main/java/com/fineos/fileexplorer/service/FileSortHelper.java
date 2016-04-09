/*
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */

package com.fineos.fileexplorer.service;

import android.util.Log;

import com.fineos.fileexplorer.entity.FileInfo;
import com.fineos.fileexplorer.util.FileExplorerSettings;
import com.fineos.fileexplorer.util.HanziToPinyin;
import com.fineos.fileexplorer.util.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSortHelper {

    //sort
    public static final int SORT_DATE = 0;
    public static final int SORT_NAME = 1;
    public static final int SORT_SIZE = 2;
    public static final int SORT_TYPE = 3;    
    public enum SortMethod {
        name, size, date, type
    }
    
    public static Map<Integer,SortMethod> sortArr;
    static{
    	sortArr = new HashMap<Integer,SortMethod>();
    	sortArr.put(SORT_DATE, SortMethod.date);
    	sortArr.put(SORT_NAME, SortMethod.name);
    	sortArr.put(SORT_SIZE, SortMethod.size);
    	sortArr.put(SORT_TYPE, SortMethod.type);
    }

    private SortMethod mSort;

    private boolean mFileFirst;

    private HashMap<SortMethod, Comparator> mComparatorList = new HashMap<SortMethod, Comparator>();

    
    public FileSortHelper() {
        mSort = SortMethod.name;
        mComparatorList.put(SortMethod.name, cmpName);
        mComparatorList.put(SortMethod.size, cmpSize);
        mComparatorList.put(SortMethod.date, cmpDate);
        mComparatorList.put(SortMethod.type, cmpType);
    }

    public FileSortHelper setSortMethod(SortMethod s) {
        mSort = s;
        return this;
    }

    public SortMethod getSortMethod() {
        return mSort;
    }

    public void setFileFirst(boolean f) {
        mFileFirst = f;
    }

    public Comparator<FileInfo> getComparator() {
        return mComparatorList.get(mSort);
    }

    private abstract class FileComparator implements Comparator<FileInfo> {

        @Override
        public int compare(FileInfo object1, FileInfo object2) {
            if (object1.isDirectory() == object2.isDirectory()) {
                return doCompare(object1, object2);
            }

            if (mFileFirst) {
                // the files are listed before the dirs
                return (object1.isDirectory() ?1 : -1);
            } else {
                // the dir-s are listed before the files
                return object1.isDirectory() ? -1 : 1;
            }
        }

        protected abstract int doCompare(FileInfo object1, FileInfo object2);
    }

    private Comparator cmpName = new FileComparator() {
        @Override
        public int doCompare(FileInfo object1, FileInfo object2) {
            boolean filename1ContainsChinese = hasChineseCharacters(object1.getFileName());
            boolean filename2ContainsChinese = hasChineseCharacters(object2.getFileName());
            String filename1 = object1.getFileName();
            String filename2 = object2.getFileName();
            if (filename1ContainsChinese || filename2ContainsChinese) {
                if (filename1ContainsChinese && !filename2ContainsChinese) {
                    return -1;
                }
                if (!filename1ContainsChinese && filename2ContainsChinese) {
                    return 1;
                }
                filename1 = HanziToPinyin.getPingYinFormString(filename1);
                filename2 = HanziToPinyin.getPingYinFormString(filename2);
            }
            if (FileExplorerSettings.compareFileIgnoreCase()) {
                char firstChar1 = filename1.charAt(0);
                char firstChar2 = filename2.charAt(0);
                if (firstChar1 != firstChar2 && Character.toUpperCase(firstChar1) == Character.toUpperCase(firstChar2)) {
                    return filename1.compareTo(filename2);
                }
                return filename1.compareToIgnoreCase(filename2);
            }else{
                return filename1.compareTo(filename2);
            }
        }

    };

    public static boolean hasChineseCharacters(String str){
        int length = str.length();
        for (int i = 0; i < length; i++){
            char ch = str.charAt(i);
            Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
            if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)||
                    Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block)||
                    Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block)){
                return true;
            }
        }
        return false;
    }

    private Comparator cmpSize = new FileComparator() {
        @Override
        public int doCompare(FileInfo object1, FileInfo object2) {
            return longToCompareSize(object1.getLength()- object2.getLength());
        }
    };

    private Comparator cmpDate = new FileComparator() {
        @Override
        public int doCompare(FileInfo object1, FileInfo object2) {
            return longToCompareInt(object2.getLastModified() - object1.getLastModified());
        }
    };

    private int longToCompareInt(long result) {
        return result > 0 ? 1 : (result < 0 ? -1 : 0);
    }
    /*排序问题*/
    private int longToCompareSize(long result) {
        return result > 0 ? -1 : (result < 0 ? 1 : 0);
    }

    private Comparator cmpType = new FileComparator() {
        @Override
        public int doCompare(FileInfo object1, FileInfo object2) {

            if (object1.isDirectory()) {
                return -1;
            }
            if (object2.isDirectory()) {
                return 1;
            }

            int result = StringUtils.getExtFromFilename(object1.getFileName()).compareToIgnoreCase(
            		StringUtils.getExtFromFilename(object2.getFileName()));
            if (result != 0)
                return result;

            return StringUtils.getNameFromFilename(object1.getFileName()).compareToIgnoreCase(
            		StringUtils.getNameFromFilename(object2.getFileName()));
        }
    };
    
    //sort file list with the comparator already set to a FileSortHelper object
    public List<FileInfo> sortFileList(List<FileInfo> fileList) {
        if (fileList == null) {
            return null;
        }
        try {
            Collections.sort(fileList, this.getComparator());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("acmllaugh1", "sortFileList: sort failed.");
        }
        return fileList;
    }
    

    
}
