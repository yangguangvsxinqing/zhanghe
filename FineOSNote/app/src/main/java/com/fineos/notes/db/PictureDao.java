package com.fineos.notes.db;

import android.content.Context;

import com.fineos.notes.bean.NotePicture;
import com.fineos.notes.constant.Constant;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import java.sql.SQLException;
import java.util.List;

public class PictureDao {
	private Context context;
    private Dao<NotePicture, Integer> pictureDaoOpe;
    private DatabaseHelper helper;
	 public PictureDao(Context context)
	    {  
	        this.context = context;  
	        try  
	        {  
	            helper = DatabaseHelper.getHelper(context);  
	            pictureDaoOpe = helper.getDao(NotePicture.class);
	        } catch (SQLException e)  
	        {  
	            e.printStackTrace();  
	        }  
	    }  
	 
	 /** 
	     * 增加一个便签 
	     * @param notePicture
	     */  
	    public void add(NotePicture notePicture)
	    {  
	        try  
	        {  
	            pictureDaoOpe.create(notePicture);
	        } catch (SQLException e)  
	        {  
	            e.printStackTrace();  
	        }  
	  
	    }//...other operat

    /**
     * 查询所有便签
     */
    public List<NotePicture> selectAll(){
        List<NotePicture> notePictures = null;
        try {
            notePictures = pictureDaoOpe.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notePictures;
    }
    /**
     * 根据noteId查询便签中图片
     * @param noteId
     * @return
     */
    public List<NotePicture> selectPictureByNoteId(int noteId){
        List<NotePicture> notePictures = null;
        try {
            notePictures = pictureDaoOpe.queryBuilder().where().eq(Constant.TABLE_NOTE_PICTURE_NOTEID, noteId).query();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return notePictures;
    }

    /**
     * 根据picture查询便签中图片
     */
    public  List<NotePicture> selectPictureIdByName(String pictureName){
        String path = "/mnt/sdcard/myPicture/"+pictureName+Constant.PICTURETYPE;
        List<NotePicture> notePictures = null;
        try {
            notePictures = pictureDaoOpe.queryBuilder().where().eq(Constant.TABLE_NOTE_PICTURE_PATH, path).query();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return notePictures;
    }

    /**
     * 根据content查询便签中图片
     */
    public  List<NotePicture> selectPictureIdByContent(String content){
        List<NotePicture> notePictures = null;
        try {
            notePictures = pictureDaoOpe.queryBuilder().where().eq(Constant.TABLE_NOTE_PICTURE_CONTENT, content).query();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return notePictures;
    }
    public List<NotePicture> selectByKeyWorld(String keyworld){
        List<NotePicture> notePictures = null;
        try {
            QueryBuilder<NotePicture, Integer> queryBuilder = pictureDaoOpe.queryBuilder();
            queryBuilder.where().like(Constant.TABLE_NOTE_PICTURE_CONTENT, "%"+keyworld+"%");
            notePictures = queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notePictures;
    }
	    /**
	     * 删除一个便签
	     */
	    public void delete(NotePicture notePicture){
	    	try {
				pictureDaoOpe.delete(notePicture);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }

    /**
     * 根据便签id删除便签中的图片
     * @param noteId
     */
    public void deletePictureByNoteId(int noteId){
        try {
            DeleteBuilder<NotePicture,Integer> deleteBuilder = pictureDaoOpe.deleteBuilder();
            deleteBuilder.where().eq(Constant.TABLE_NOTE_PICTURE_NOTEID,noteId);
            deleteBuilder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据图片名删除该图片,实际上是图片更新地址为空
     * @param pictureId
     */
    public  void deletePictureById(int pictureId){
        try {
            UpdateBuilder<NotePicture,Integer> updateBuilder = pictureDaoOpe.updateBuilder();
            updateBuilder.where().eq(Constant.TABLE_NOTE_PICTURE_ID,pictureId);
            updateBuilder.updateColumnValue(Constant.TABLE_NOTE_PICTURE_PATH, null);
            updateBuilder.update();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据更新内容
     * @param pictureId
     * @param newContent
     */
    public  void updateContentByPictureId(int pictureId,String newContent) {
        try {
            UpdateBuilder<NotePicture, Integer> updateBuilder = pictureDaoOpe.updateBuilder();
            updateBuilder.where().eq(Constant.TABLE_NOTE_PICTURE_ID, pictureId);
            updateBuilder.updateColumnValue(Constant.TABLE_NOTE_PICTURE_CONTENT, newContent);
            updateBuilder.update();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 根据imagePath更新
     * @param notePicture
     * @param imagePath
     */
    public void updateByPictureId(NotePicture notePicture,String imagePath){
        try {
            UpdateBuilder<NotePicture, Integer> updateBuilder = pictureDaoOpe.updateBuilder();
            updateBuilder.where().eq(Constant.TABLE_NOTE_PICTURE_PATH, imagePath);
            updateBuilder.updateColumnValue(Constant.TABLE_NOTE_PICTURE_CONTENT, notePicture.getContent());
            updateBuilder.update();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * 根据note_id更新
     * @param defautId
     * @param newNoteId
     */
    public void updateByNoteId(int defautId,int newNoteId){
        try {
            UpdateBuilder<NotePicture, Integer> updateBuilder = pictureDaoOpe.updateBuilder();
            updateBuilder.where().eq(Constant.TABLE_NOTE_PICTURE_NOTEID, defautId);
            updateBuilder.updateColumnValue(Constant.TABLE_NOTE_PICTURE_NOTEID, newNoteId);
            updateBuilder.update();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
	    
}
