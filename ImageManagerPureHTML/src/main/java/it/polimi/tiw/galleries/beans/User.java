package it.polimi.tiw.galleries.beans;

public class User{
	//ATTRIBUTES
	private int id;
	private String username;
	private String email;
	
	
	
	//GETTERS
	public int getId() {
		return id;
	}
	public String getUsername() {
		return username;
	}
	public String getEmail() {
		return email;
	}
	
	
	
	//SETTERS
	public void setId(int id) {
		this.id = id;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
}