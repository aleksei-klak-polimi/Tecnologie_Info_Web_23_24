package it.polimi.tiw.albums.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.albums.beans.Album;

public class AlbumDAO{
private Connection con;
	
	
	
	//CONSTRUCTOR
	public AlbumDAO(Connection con) {
		this.con = con;
	}
	
	
	
	
	//QUERIES
	public boolean albumExists(int albumId) throws SQLException{
		String query ="SELECT id FROM Album WHERE id = ?";
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, albumId);
			
			try(ResultSet qres = pstat.executeQuery()){
				if(qres.isBeforeFirst()) {
					//If isBeforeFirst() = true, result set is not empty
					//and album exists
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	public boolean albumBelongsToUser(int albumId, int userId) throws SQLException{
		String query ="SELECT id FROM Album WHERE id = ? AND owner = ?";
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, albumId);
			pstat.setInt(2, userId);
			
			try(ResultSet qres = pstat.executeQuery()){
				if(qres.isBeforeFirst()) {
					//If isBeforeFirst() = true, result set is not empty
					//and album-owner combo exists
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	public int getAmountOfPicturesByAlbum(int albumId) throws SQLException{
		String query = "SELECT COUNT(DISTINCT pictureId) FROM Album_Picture WHERE albumId = ?";
		int amount = -1;
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, albumId);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()){
					amount = qres.getInt("COUNT(DISTINCT pictureId)");
				}
			}
		}
		
		return amount;
	}
	
	
	public Album getAlbumById(int albumId) throws SQLException{
		Album album = null;
		String query = "SELECT id, title, owner, creationDate FROM Album WHERE id = ? ;";
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, albumId);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()) {
					album = new Album();
					album.setId(qres.getInt("id"));
					album.setOwner(qres.getString("owner"));
					album.setTitle(qres.getString("title"));
					album.setCreationDate(qres.getDate("creationDate"));
				}
			}
		}
		
		return album;
	}
	
	
	public List<Album> getAlbumsByUser(int userId) throws SQLException{
		List<Album> albums = new ArrayList<Album>();
		String query = "SELECT Album.id, Album.title, Album.creationDate, User.username FROM Album JOIN User ON Album.owner = User.id WHERE User.id = ? ORDER BY creationDate DESC;";
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, userId);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()) {
					Album album = new Album();
					album.setId(qres.getInt("id"));
					album.setOwner(qres.getString("username"));
					album.setTitle(qres.getString("title"));
					album.setCreationDate(qres.getDate("creationDate"));
					
					albums.add(album);
				}
			}
		}
		
		return albums;
	}
	
	
	
	public List<Album> getAlbumsByOthers(int userIdToExclude) throws SQLException{
		List<Album> albums = new ArrayList<Album>();
		String query = "SELECT Album.id, Album.title, Album.creationDate, User.username FROM Album JOIN User ON Album.owner = User.id WHERE User.id != ? ORDER BY creationDate DESC;";
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, userIdToExclude);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()) {
					Album album = new Album();
					album.setId(qres.getInt("id"));
					album.setOwner(qres.getString("username"));
					album.setTitle(qres.getString("title"));
					album.setCreationDate(qres.getDate("creationDate"));
					
					albums.add(album);
				}
			}
		}
		
		return albums;
	}
	
	
	
	public void createAlbum(int owner, String title, Date creationDate) throws SQLException {
		String query = "INSERT INTO Album (title, owner, creationDate) VALUES (?, ?, ?);";
	
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setString(1, title);
			pstat.setInt(2, owner);
			pstat.setObject(3, creationDate.toInstant().atZone(ZoneId.of("Europe/Rome")).toLocalDate());
			pstat.executeUpdate();
		}
	}
	
	
	
	public void deleteAlbum(int albumId) throws SQLException {
		String query = "DELETE FROM Album WHERE id = ?";
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, albumId);
			pstat.executeUpdate();
		}
		//TODO check if is necessary to manually delete entries from Album_Picture table
	}
	
	public int getLatestAlbumByUser(int userId) throws SQLException{
		int albumId = -1;
		
		String query = "SELECT MAX(id) FROM Album WHERE owner = ?;";
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, userId);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()){
					albumId = qres.getInt("MAX(id)");
				}
			}
		}
		
		return albumId;
	}
	

}



