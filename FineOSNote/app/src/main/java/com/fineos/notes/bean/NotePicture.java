package com.fineos.notes.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by ubuntu on 15-7-14.
 */
@DatabaseTable(tableName = "tb_picture")
public class NotePicture {
    @DatabaseField(generatedId = true)
    private  int image_id;
    @DatabaseField(columnName = "imagePath")
    private  String imagePath;
    @DatabaseField(columnName = "content")
    private  String content;
    @DatabaseField(columnName = "note_id")
    private  int note_id;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return "NotePicture{" +
                "image_id=" + image_id +
                ", imagePath='" + imagePath + '\'' +
                ", content='" + content + '\'' +
                ", note_id=" + note_id +
                '}';
    }

    public int getNoteId() {
        return note_id;
    }

    public void setNoteId(int note_id) {
        this.note_id = note_id;
    }

    public int getImageId() {
        return image_id;
    }

    public void setImageId(int image_id) {
        this.image_id = image_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
