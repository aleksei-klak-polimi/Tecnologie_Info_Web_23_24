package it.polimi.tiw.albums.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.albums.beans.Picture;

public class PictureDAO{
private Connection con;
	
	
	
	//CONSTRUCTOR
	public PictureDAO(Connection con) {
		this.con = con;
	}
	
	
	
	
	//QUERIES
	public List<Picture> getPicturesFromAlbumByPage(int albumId, int albumPage, int pageSize) throws SQLException{
		List<Picture> pictures = new ArrayList<Picture>();
		String query ="SELECT P.id, P.path, P.title FROM Picture P JOIN Album_Picture AP ON AP.pictureId = P.id WHERE AP.albumId = ? LIMIT ? OFFSET ? ORDER BY P.uploadDate DESC";
		
		
		int imageOffset = albumPage * pageSize;
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, albumId);
			pstat.setInt(2, pageSize);
			pstat.setInt(3, imageOffset);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()) {
					Picture picture = new Picture();
					picture.setId(qres.getInt("id"));
					picture.setPath(qres.getString("path"));
					picture.setTitle(qres.getString("title"));
					
					pictures.add(picture);
				}
			}
		}
		
		return pictures;
	}
}