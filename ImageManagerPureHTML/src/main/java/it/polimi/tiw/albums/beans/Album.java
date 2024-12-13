package it.polimi.tiw.albums.beans;

import java.util.Date;

public class Album{
	//ATTRIBUTES
	private int id;
	private String title;
	private int owner;
	private Date creationDate;
	
	
	
	//GETTERS
	public int getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public int getOwner() {
		return owner;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	
	
	
	//SETTERS
	public void setId(int id) {
		this.id = id;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setOwner(int owner) {
		this.owner = owner;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

}