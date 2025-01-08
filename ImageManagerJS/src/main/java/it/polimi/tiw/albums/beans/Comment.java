package it.polimi.tiw.albums.beans;

import java.util.Date;

public class Comment{
	//ATTRIBUTES
	private int id;
	private String body;
	private int pictureId;
	private String author;
	private Date postDate;
	
	
	
	//GETTERS
	public int getId() {
		return id;
	}
	public String getBody() {
		return body;
	}
	public int getPictureId() {
		return pictureId;
	}
	public String getAuthor() {
		return author;
	}
	public Date getPostDate() {
		return postDate;
	}
	
	
	
	//SETTERS
	public void setId(int id) {
		this.id = id;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public void setPictureId(int pictureId) {
		this.pictureId = pictureId;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}
	
}