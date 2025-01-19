package it.polimi.tiw.albums.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.albums.beans.Picture;
import it.polimi.tiw.albums.beans.UserAlbumOrdering;

public class PictureDAO{
private Connection con;
	
	
	
	//CONSTRUCTOR
	public PictureDAO(Connection con) {
		this.con = con;
	}
	
	
	
	
	//QUERIES
	public Picture getPictureById(int pictureId) throws SQLException {
		Picture picture = null;
		String query ="SELECT * FROM Picture WHERE id = ?;";
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, pictureId);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()) {
					picture = new Picture();
					picture.setId(qres.getInt("id"));
					picture.setPath(qres.getString("path"));
					picture.setThumbnailPath(qres.getString("thumbnailPath"));
					picture.setTitle(qres.getString("title"));
					picture.setDescription(qres.getString("description"));
					picture.setUploadDate(qres.getDate("uploadDate"));
					picture.setUploaderId(qres.getInt("uploader"));
				}
			}
		}
		
		return picture;
	}
	
	public void deletePictureById(int pictureId) throws SQLException {
		String query ="DELETE FROM Picture WHERE id = ?;";
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, pictureId);
			pstat.executeUpdate();
		}
	}
	
	public void updatePicture(int pictureId, String title, String description, Date date) throws SQLException {
		String query ="UPDATE Picture SET title = ?, description = ?,  uploadDate = ? WHERE id = ?;";
		
		//Add picture to database
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setString(1, title);
			pstat.setString(2, description);
			pstat.setObject(3, date.toInstant().atZone(ZoneId.of("Europe/Rome")).toLocalDate());
			pstat.setInt(4, pictureId);
			pstat.executeUpdate();
		}
	}
	
	
	public List<Picture> getPicturesFromAlbum(int albumId) throws SQLException{
		List<Picture> pictures = new ArrayList<Picture>();
		String query ="SELECT P.id, P.path, P.thumbnailPath, P.title, P.uploadDate, P.description FROM Picture P JOIN Album_Picture AP ON AP.pictureId = P.id WHERE AP.albumId = ? ORDER BY P.uploadDate DESC, P.id DESC;";
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, albumId);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()) {
					Picture picture = new Picture();
					picture.setId(qres.getInt("id"));
					picture.setPath(qres.getString("path"));
					picture.setThumbnailPath(qres.getString("thumbnailPath"));
					picture.setTitle(qres.getString("title"));
					picture.setDescription(qres.getString("description"));
					picture.setUploadDate(qres.getDate("uploadDate"));

					pictures.add(picture);
				}
			}
		}
		return pictures;
	}
	
	
	public boolean pictureExists(int pictureId) throws SQLException{
		String query ="SELECT id FROM Picture WHERE id = ?";
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, pictureId);
			
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
	
	
	public boolean pictureBelongsToAlbum(int pictureId, int albumId) throws SQLException{
		String query ="SELECT pictureid FROM Album_Picture WHERE pictureid = ? AND albumId = ?";
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, pictureId);
			pstat.setInt(2, albumId);
			
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
	
	
	public int countPictureInstances(int pictureId) throws SQLException {
		String query ="SELECT DISTINCT COUNT(albumId) AS totalCount FROM Album_Picture WHERE pictureId = ?;";
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, pictureId);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()) {
					return qres.getInt("totalCount");
				}
			}
		}
		
		return -1;
	}
	
	
	public List<Picture> getPicturesNotInAlbum(int albumId, int uploader) throws SQLException{
		List<Picture> pictures = new ArrayList<Picture>();
		String query ="SELECT P.id, P.path, P.thumbnailPath, P.title, P.uploadDate, P.description FROM Picture P WHERE P.uploader = ? AND P.id NOT IN (SELECT pictureId FROM Album_Picture WHERE albumId = ?);";
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, uploader);
			pstat.setInt(2, albumId);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()) {
					Picture picture = new Picture();
					picture.setId(qres.getInt("id"));
					picture.setPath(qres.getString("path"));
					picture.setThumbnailPath(qres.getString("thumbnailPath"));
					picture.setTitle(qres.getString("title"));
					picture.setDescription(qres.getString("description"));
					picture.setUploadDate(qres.getDate("uploadDate"));
					
					pictures.add(picture);
				}
			}
		}
		return pictures;
	}
	
	
	
	public int createPicture(int albumId, int uploader, String path, String thumbnailPath, String title, String description, Date date)throws SQLException{
		int pictureId = -1;
		String query ="INSERT INTO Picture (path, thumbnailPath, title, description, uploadDate, uploader) VALUES (?, ?, ?, ?, ?, ?);";
		
		//Add picture to database
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setString(1, path);
			pstat.setString(2, thumbnailPath);
			pstat.setString(3, title);
			pstat.setString(4, description);
			pstat.setObject(5, date.toInstant().atZone(ZoneId.of("Europe/Rome")).toLocalDate());
			pstat.setInt(6, uploader);
			pstat.executeUpdate();
		}
		
		//Retrieve database-generated id
		query = "SELECT MAX(id) FROM Picture WHERE uploader = ?;";
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, uploader);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()){
					pictureId = qres.getInt("MAX(id)");
				}
			}
		}
		
		//Link picture to album
		query = "INSERT INTO Album_Picture (pictureId, albumId) VALUES (?, ?)";
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, pictureId);
			pstat.setInt(2, albumId);
			pstat.executeUpdate();
		}
		
		return pictureId;
	}
	
	
	
	public void addExistingPicturesToAlbum(List<Integer> pictureIds, int albumId) throws SQLException {
		String query ="INSERT INTO Album_Picture (pictureId, albumId) VALUES (?, ?)";
		try (PreparedStatement pstat = con.prepareStatement(query)){
			
			for(int pictureId:pictureIds) {
				pstat.setInt(1, pictureId);
				pstat.setInt(2, albumId);
				pstat.addBatch();
			}
			pstat.executeBatch();
		}
	}
	
	
	
	public void changePictureOrderPreferenceInAlbum(List<UserAlbumOrdering> orders, int albumId, int userId)throws SQLException{
		String query ="INSERT INTO User_Album_Ordering (userId, albumId, pictureId, pictureOrder) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE pictureOrder = ?";
		try(PreparedStatement pstat = con.prepareStatement(query)){
			
			for(UserAlbumOrdering order : orders) {
				pstat.setInt(1, userId);
				pstat.setInt(2, albumId);
				pstat.setInt(3, order.getPictureId());
				pstat.setInt(4, order.getPictureOrder());
				pstat.setInt(5, order.getPictureOrder());
				pstat.addBatch();
			}
			pstat.executeBatch();
		}
	}
	
	public List<UserAlbumOrdering> getPictureOrderPreferenceInAlbumByUser(int userId, int albumId) throws SQLException{
		List<UserAlbumOrdering> orders = new ArrayList<>();
		String query="SELECT pictureId, pictureOrder FROM User_Album_Ordering WHERE userId = ? AND albumId = ?";
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, userId);
			pstat.setInt(2, albumId);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()) {
					UserAlbumOrdering order = new UserAlbumOrdering();
					order.setPictureId(qres.getInt("pictureId"));
					order.setPictureOrder(qres.getInt("pictureOrder"));
					
					orders.add(order);
				}
			}
		}
		return orders;
	}
	
	public void resetPictureOrderPreferenceInAlbumByUser(int userId, int albumId) throws SQLException{
		String query="DELETE FROM User_Album_Ordering WHERE userId = ? AND albumId = ?";
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, userId);
			pstat.setInt(2, albumId);
			pstat.executeUpdate();
		}
	}
}




