package com.fineos.notes.db;

import java.sql.SQLException;
import java.util.List;


import com.fineos.notes.bean.Folder;
import com.fineos.notes.bean.Note;
import com.fineos.notes.constant.Constant;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FolderDao {
	private Context context;  
    private Dao<Folder, Integer> folderDaoOpe;  
    private DatabaseHelper helper;

	 public FolderDao(Context context)  
	    {  
	        this.context = context;  
	        try  
	        {
	            helper = DatabaseHelper.getHelper(context);
	            folderDaoOpe = helper.getDao(Folder.class);
	        } catch (Exception e)
	        {
	            e.printStackTrace();
	        }  
	    }  
	 
	 /** 
	     * 增加一个便签 
	     * @param folder
	     */  
	    public void add(Folder folder)  
	    {  
	        try  
	        {
//	        	folderDaoOpe.createIfNotExists(folder);//android soure can be SQLException
	            folderDaoOpe.create(folder);
            } catch (SQLException e)
	        {  Log.w(Constant.TAG,"FolderDao add-----SQLException:"+e);
                e.printStackTrace();
            }
            Log.w(Constant.TAG,"FolderDao add-----sucess");

	    }
	    
	    /**
	     * 根据文件夹名称删除文件夹
	     * @param folderName
	     */
	    public void deleteFolder(String folderName){
	    	try {
	    		DeleteBuilder<Folder, Integer> deleteBuilder = folderDaoOpe.deleteBuilder();
				deleteBuilder.where().eq(Constant.TABLE_NOTE_FOLDER, folderName);
				deleteBuilder.delete();
				Log.w(Constant.TAG,"0删除失败，1删除成功 deleteFolder"+ deleteBuilder.delete());
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
	    /**
	     * 更新文件夹名称
	     * @param oldFolderName 原来文件夹名称
	     * @param newFolderName
	     */
	    public void updateFolder(String oldFolderName,String newFolderName){
	    	try {
	    		 UpdateBuilder<Folder, Integer> updateBuilder = folderDaoOpe.updateBuilder();
	             updateBuilder.where().eq(Constant.TABLE_NOTE_FOLDER, oldFolderName);
	             updateBuilder.updateColumnValue(Constant.TABLE_NOTE_FOLDER, newFolderName);
	             updateBuilder.update();   
	             Log.w(Constant.TAG,"0更新失败，1更新成功 updateBuilder.update()"+ updateBuilder.update());
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    	
	    }
	    /**
	     * 查询所有便签
	     */
	    public List<Folder> selectAll(){
	    	List<Folder> listFolders = null;
	    	try {
	    		listFolders = folderDaoOpe.queryForAll();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    	return listFolders;
	    }
	    /**
	     * 过滤相同文件夹查询
	     */
	    public List<Folder> selectAll(String folderName){
	    	List<Folder> listFolders = null;
	    	try {
	    		listFolders = folderDaoOpe.queryBuilder().distinct().where().eq(Constant.TABLE_NOTE_FOLDER, folderName).query();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    	return listFolders;
	    }
	    
	    /**
	     * 根据文件夹名称查询便签
	     * @param forderName
	     * @return
	     */
	    public List<Folder> selectFolder(String forderName){
	    	List<Folder> listFolders = null;
	    	try {
	    		listFolders = folderDaoOpe.queryBuilder().where().eq(Constant.TABLE_NOTE_FOLDER, forderName).query();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	return listFolders;
	    }


    /**
     * 根据文件夹名称查询便签
     * @param forlderId
     * @return
     */
    public List<Folder> selectByfolderId(int forlderId){
        List<Folder> listFolders = null;
        try {
            Log.w(Constant.TAG,"selectByfolderId");
            listFolders = folderDaoOpe.queryBuilder().where().eq(Constant.TABLE_FOLDER_ID, forlderId).query();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return listFolders;
    }
}
