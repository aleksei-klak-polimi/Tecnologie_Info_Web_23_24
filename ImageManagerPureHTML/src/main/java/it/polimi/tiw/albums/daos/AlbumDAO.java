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
	public List<Album> getAlbumsByUser(int userId) throws SQLException{
		List<Album> albums = new ArrayList<Album>();
		String query = "SELECT * FROM Album WHERE owner = ? ORDER BY creationDate DESC;";
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, userId);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()) {
					Album album = new Album();
					album.setId(qres.getInt("id"));
					album.setOwner(qres.getInt("owner"));
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
		String query = "SELECT * FROM Album WHERE owner != ? ORDER BY creationDate DESC;";
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, userIdToExclude);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()) {
					Album album = new Album();
					album.setId(qres.getInt("id"));
					album.setOwner(qres.getInt("owner"));
					album.setTitle(qres.getString("title"));
					album.setCreationDate(qres.getDate("creationDate"));
					
					albums.add(album);
				}
			}
		}
		
		return albums;
	}
	
	
	
	public void createAlbum(int owner, String title, Date creationDate) throws SQLException {
		String query = "INSERT INTO Album (title, owner, creationDate) VALUES (?, ?, ?)";
	
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
}



