package it.polimi.tiw.albums.daos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.albums.beans.Comment;

public class CommentDAO {
	//ATTRIBUTES
	private Connection con;
	
	
	//CONSTRUCTOR
	public CommentDAO(Connection con) {
		this.con = con;
	}
	
	
	
	
	public List<Comment> getCommentsByPicture(int pictureId) throws SQLException {
		List<Comment> comments = new ArrayList<Comment>();
		String query ="SELECT C.id, C.body, C.pictureId, C.postDate, U.username FROM Comment C JOIN User U ON C.author = U.id WHERE pictureId = ?";
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, pictureId);
			
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()) {
					Comment comment = new Comment();
					comment.setId(qres.getInt("id"));
					comment.setBody(qres.getString("body"));
					comment.setPictureId(qres.getInt("pictureId"));
					comment.setAuthor(qres.getString("username"));
					comment.setPostDate(qres.getDate("postDate"));
					
					comments.add(comment);
				}
			}
		}
		
		return comments;
	}
	
	public int createComment(int pictureId, int authorId, Date date, String body) throws SQLException {
		int commentId = -1;
		String query ="INSERT INTO Comment (body, pictureId, author, postDate) VALUES (?, ?, ?, ?);";
		
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setString(1, body);
			pstat.setInt(2, authorId);
			pstat.setInt(3, authorId);
			pstat.setObject(4, date.toInstant().atZone(ZoneId.of("Europe/Rome")).toLocalDate());
			pstat.executeUpdate();
		}
		
		//Retrieve database-generated id
		query = "SELECT MAX(id) FROM Comment WHERE author = ?;";
		try(PreparedStatement pstat = con.prepareStatement(query)){
			pstat.setInt(1, authorId);
					
			try(ResultSet qres = pstat.executeQuery()){
				while(qres.next()){
					commentId = qres.getInt("MAX(id)");
				}
			}
		}
		return commentId;
	}
	
	
	
}






