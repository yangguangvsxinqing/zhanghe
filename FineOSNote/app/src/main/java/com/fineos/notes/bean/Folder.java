package com.fineos.notes.bean;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "tb_folder")
public class Folder {
    @DatabaseField(generatedId = true)
    private int folder_id;
    @DatabaseField(columnName = "folder")
    private String folder;
    @DatabaseField(columnName = "data")
    private Long data;
    public int getId() {
        return folder_id;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public Long getData() {
        return data;
    }

    public void setData(Long data) {
        this.data = data;
    }

    public void setId(int folder_id) {

        this.folder_id = folder_id;
    }

}
