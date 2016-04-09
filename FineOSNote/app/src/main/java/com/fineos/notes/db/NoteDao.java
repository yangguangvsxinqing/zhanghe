package com.fineos.notes.db;

import android.content.Context;
import android.util.Log;

import com.fineos.notes.bean.Note;
import com.fineos.notes.constant.Constant;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import java.sql.SQLException;
import java.util.List;

public class NoteDao {
	private Context context;  
    private Dao<Note, Integer> noteDaoOpe;  
    private DatabaseHelper helper;  
	 public NoteDao(Context context)  
	    {  
	        this.context = context;  
	        try  
	        {  
	            helper = DatabaseHelper.getHelper(context);  
	            noteDaoOpe = helper.getDao(Note.class);  
	        } catch (SQLException e)  
	        {  
	            e.printStackTrace();  
	        }  
	    }  
	 
	 /** 
	     * 增加一个便签 
	     * @param note 
	     */  
	    public void add(Note note)  
	    {  
	        try  
	        {  
	            noteDaoOpe.create(note);  
	        } catch (SQLException e)  
	        {  
	            e.printStackTrace();  
	        }  
	  
	    }//...other operat
	    
	    /**
	     * 删除一个便签
	     */
	    public void delete(Note note){
	    	try {
				noteDaoOpe.delete(note);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }

    /**
     * 根据文件夹名称删除便签
     * @param folderName
     */
        public void deleteNoteByFolderName(String folderName){
            DeleteBuilder<Note,Integer> deleteBuilder = noteDaoOpe.deleteBuilder();
            try {
                deleteBuilder.where().eq(Constant.TABLE_NOTE_FOLDER,folderName);
                deleteBuilder.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
	    
	    /**
	     * 根据id更新
	     * @param note
	     * @param id
	     */
	    public void updateById(Note note,int id){
	    	try {
                Log.w(Constant.TAG, "NoteDao updateById note:" + note);
	    		UpdateBuilder<Note, Integer> updateBuilder = noteDaoOpe.updateBuilder();
				updateBuilder.where().eq(Constant.TABLE_NOTE_ID, id);
				updateBuilder.updateColumnValue(Constant.TABLE_NOTE_DETAIL, note.getDetail());
				updateBuilder.updateColumnValue(Constant.TABLE_NOTE_BG, note.getBackground());
				updateBuilder.updateColumnValue(Constant.TABLE_NOTE_FOLDER, note.getFolder());
				updateBuilder.updateColumnValue(Constant.TABLE_NOTE_DATA, note.getData());
				updateBuilder.update();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
                Log.w(Constant.TAG,"NoteDao updateById SQLException:"+e);

                e.printStackTrace();
			}
	    }
    /**
     * 根据data查询id
     * @param data
     */
    public  List<Note>  selectByData(Long data){
        List<Note> listNotes = null;
        try {
            QueryBuilder<Note, Integer> queryBuilder = noteDaoOpe.queryBuilder();
            queryBuilder.where().eq(Constant.TABLE_NOTE_DATA, data);
            queryBuilder.selectColumns(Constant.TABLE_NOTE_ID);
            listNotes =queryBuilder.query();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return listNotes;
    }

    /**
     * 根据id移动到文件夹，其实就是根据id更新文件夹名
     */
    public void updateByFolder(int id,String newFolderName){
        UpdateBuilder<Note,Integer> updateBuilder = noteDaoOpe.updateBuilder();
        try {
            updateBuilder.where().eq(Constant.TABLE_NOTE_ID,id);
            updateBuilder.updateColumnValue(Constant.TABLE_NOTE_FOLDER, newFolderName);
            updateBuilder.update();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据文件夹名称，更新文件夹名称
     * @param oldFolderName
     * @param newFolderName
     */
    public  void updateNoteFolderByFolder(String oldFolderName,String newFolderName){
        try {
            UpdateBuilder<Note,Integer> updateBuilder = noteDaoOpe.updateBuilder();
            updateBuilder.where().eq(Constant.TABLE_NOTE_FOLDER,oldFolderName);
            updateBuilder.updateColumnValue(Constant.TABLE_NOTE_FOLDER,newFolderName);
            updateBuilder.update();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
	    /**
	     * 查询所有便签 按时间降序
	     */
	    public List<Note> selectAll(){
	    	List<Note> listNotes = null;
	    	try {
                QueryBuilder<Note,Integer> queryBuilder = noteDaoOpe.queryBuilder();
                queryBuilder.orderBy(Constant.TABLE_NOTE_DATA,false);
                listNotes = queryBuilder.query();
//	    		listNotes = noteDaoOpe.queryForAll();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    	return listNotes;
	    }
	    /**
	     * 过滤相同文件夹查询
	     */
	    public List<Note> selectById(int id){
	    	List<Note> listNotes = null;
	    	try {
	    		QueryBuilder<Note, Integer> queryBuilder = noteDaoOpe.queryBuilder();
	    		queryBuilder.where().eq(Constant.TABLE_NOTE_ID, id);
	    		listNotes = queryBuilder.query();
	    		
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    	return listNotes;
	    }
	    
	    /**
	     * 根据id删除便签
	     */
	    public void deleteById(String id){
	    	try {
	    		DeleteBuilder<Note, Integer> deleteBuilder = noteDaoOpe.deleteBuilder();
				deleteBuilder.where().eq(Constant.TABLE_NOTE_ID, id);
				deleteBuilder.delete();
				Log.w(Constant.TAG,"0删除失败，1删除成功 deleteFolder"+ deleteBuilder.delete());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    /**
	     * 根据文件夹名称查询便签
	     * @param forderName
	     * @return
	     */
	    public List<Note> selectByFolder(String forderName){
	    	List<Note> listNotes = null;
	    	try {
                Log.w(Constant.TAG, "NoteDao selectByFolder forderName:" + forderName);
                QueryBuilder<Note, Integer> queryBuilder = noteDaoOpe.queryBuilder();
                queryBuilder.orderBy(Constant.TABLE_NOTE_DATA, false).where().eq(Constant.TABLE_NOTE_FOLDER, forderName).query();
                listNotes = queryBuilder.query();
                Log.w(Constant.TAG, "NoteDao selectByFolder listNotes:" + listNotes);
            } catch (SQLException e) {
                Log.w(Constant.TAG, "NoteDao selectByFolder SQLException:" + e);
				e.printStackTrace();
			}
	    	return listNotes;
	    }
	    
	    /**
	     * 根据关键字搜索到所有符合便签(HQ01687841 change to ids)
	     * @param keyworld
	     * @return
	     */
	    public List<Note> selectByKeyWorld(String keyworld){
	    	List<Note> listNotes = null;
	    	try {
	    		QueryBuilder<Note, Integer> queryBuilder = noteDaoOpe.queryBuilder();
				//dpc HQ01687841 @{
				queryBuilder.orderBy(Constant.TABLE_NOTE_DATA,false).where().like(Constant.TABLE_NOTE_DETAIL, "%"+keyworld+"%");
				//queryBuilder.orderBy(Constant.TABLE_NOTE_DATA,false).where().like(Constant.TABLE_NOTE_DETAIL, "%"+keyworld+"%");
				//@}
				listNotes = queryBuilder.query();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    	return listNotes;
	    }
}
