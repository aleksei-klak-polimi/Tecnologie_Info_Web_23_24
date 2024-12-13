package it.polimi.tiw.galleries.beans;

public class Comment{
	//ATTRIBUTES
	private int id;
	private String body;
	private int pictureId;
	private int authorId;
	
	
	
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
	public int getAuthorId() {
		return authorId;
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
	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}
	
}