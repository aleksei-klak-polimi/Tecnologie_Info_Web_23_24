package it.polimi.tiw.albums.beans;

import java.util.Date;

public class Picture{
	//ATTRIBUTES
	private int id;
	private String path;
	private String thumbnailPath;
	private String title;
	private String description;
	private Date uploadDate;
	private int uploaderId;
	
	
	
	//GETTERS
	public int getId() {
		return id;
	}
	public String getPath() {
		return path;
	}
	public String getThumbnailPath() {
		return thumbnailPath;
	}
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}
	public Date getUploadDate() {
		return uploadDate;
	}
	public int getUploaderId() {
		return uploaderId;
	}
	
	
	
	//SETTERS
	public void setId(int id) {
		this.id = id;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public void setThumbnailPath(String path) {
		this.thumbnailPath = path;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}
	public void setUploaderId(int uploaderId) {
		this.uploaderId = uploaderId;
	}
	
}