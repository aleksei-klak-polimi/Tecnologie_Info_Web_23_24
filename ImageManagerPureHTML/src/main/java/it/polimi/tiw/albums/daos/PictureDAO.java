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

public class PictureDAO{
private Connection con;
	
	
	
	//CONSTRUCTOR
	public PictureDAO(Connection con) {
		this.con = con;
	}
	
	
	
	
	//QUERIES
	public List<Picture> getPicturesFromAlbumByPage(int albumId, int albumPage, int pageSize) throws SQLException{
		List<Picture> pictures = new ArrayList<Picture>();
		String query ="SELECT P.id, P.path, P.thumbnailPath, P.title, P.uploadDate FROM Picture P JOIN Album_Picture AP ON AP.pictureId = P.id WHERE AP.albumId = ? ORDER BY P.uploadDate DESC, P.id DESC LIMIT ? OFFSET ?;";
		
		
		int imageOffset = (albumPage -1) * pageSize;
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, albumId);
			pstat.setInt(2, pageSize);
			pstat.setInt(3, imageOffset);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()) {
					Picture picture = new Picture();
					picture.setId(qres.getInt("id"));
					picture.setPath(qres.getString("path"));
					picture.setThumbnailPath(qres.getString("thumbnailPath"));
					picture.setTitle(qres.getString("title"));
					
					pictures.add(picture);
				}
			}
		}
		return pictures;
	}
	
	
	
	
}




