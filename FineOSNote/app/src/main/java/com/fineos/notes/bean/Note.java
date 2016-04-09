package com.fineos.notes.bean;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "tb_note")
public class Note {
	@DatabaseField(generatedId = true) 
	private int note_id;
	@DatabaseField(columnName = "folder")
	private String folder;
	@DatabaseField(columnName = "title")
	private String title ;
	@DatabaseField(columnName = "detail")
	private String detail;
	@DatabaseField(columnName = "background")
	private int background; 
	@DatabaseField(columnName = "data")
	private Long data;
	@DatabaseField(columnName = "picture")
	private String picture;
	public int getId() {
		return note_id;
	}
	public void setId(int note_id) {
		this.note_id = note_id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public int getBackground() {
		return background;
	}
	public void setBackground(int background) {
		this.background = background;
	}
	public Long getData() {
		return data;
	}
	public void setData(Long data) {
		this.data = data;
	}
	public String getPicture() {
		return picture;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	public String getFolder() {
		return folder;
	}
	public void setFolder(String folder) {
		this.folder = folder;
	}
	public boolean isValid(){
		//HQ01567567
		if( detail == null & data == null){
			return false;
		}
		if(detail !=null & detail.equals("null")){
		  	if(data != null & data.equals("null")){
				return false;
			}
		}
		//if(( detail == null && data == null) || (detail.equals("null")&&data.equals("null"))){
		//	return false;
		//}
		return true;
	}
	@Override
	public String toString() {
		return "Note [note_id=" + note_id + ", title=" + title + ", detail=" + detail
				+ ", background=" + background + ", data=" + data
				+ ", picture=" + picture + ", folder=" + folder + "]";
	}
	
}
