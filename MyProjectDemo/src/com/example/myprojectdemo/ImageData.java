package com.example.myprojectdemo;

class ImageData{
	private String imgUrl;
	private int imgId;
	private int imgHeight=0;
	
	public void setImgUrl(String url){
		imgUrl = url;
	}
	
	public String getImgUrl(){
		return imgUrl;
	}
	
	public void setImgId(int id){
		imgId = id;
	}
	
	public int getImgId(){
		return imgId;
	}
	
	public void setImgHeight(int h){
		imgHeight = h;
	}
	
	public int getImgHeight(){
		return imgHeight;
	}
}
