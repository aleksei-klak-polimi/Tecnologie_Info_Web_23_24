package it.polimi.tiw.albums.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.albums.beans.User;

public class UserDAO{
	//ATTRIBUTES
	private Connection con;
	
	
	
	//CONSTRUCTOR
	public UserDAO(Connection con) {
		this.con = con;
	}
	
	
	
	
	//QUERIES
	public boolean isUsernameAvailable(String username) throws SQLException{
		String query = "SELECT id FROM User WHERE username = ?";
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setString(1, username);
			
			try(ResultSet qres = pstat.executeQuery()){
				if(!qres.isBeforeFirst()) {
					//If isBeforeFirst() = false, result set is empty
					//and username is available
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	public boolean isEmailAvailable(String email) throws SQLException{
		String query = "SELECT id FROM User WHERE email = ?";
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setString(1, email);
			
			try(ResultSet qres = pstat.executeQuery()){
				if(!qres.isBeforeFirst()) {
					//If isBeforeFirst() = false, result set is empty
					//and username is available
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	public User getUserByCredentials(String username, String pwd) throws SQLException{
		User user = null;
		
		String query = "SELECT id, username, email FROM User WHERE username = ? AND password = ?";
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setString(1, username);
			pstat.setString(2, pwd);
			
			try(ResultSet qres = pstat.executeQuery()){
				user = new User();
				while(qres.next()) {
					user.setId(qres.getInt("id"));
					user.setUsername(qres.getString("username"));
					user.setEmail(qres.getString("email"));
				}
			}
		}
		
		return user;
	}
	
	
	public void createUser(String username, String email, String pwd) throws SQLException{
		String query = "INSERT INTO User (username, email, password) VALUES ?, ?, ?";
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setString(1, username);
			pstat.setString(2, email);
			pstat.setString(3, pwd);
			pstat.executeUpdate();
		}
	}
}