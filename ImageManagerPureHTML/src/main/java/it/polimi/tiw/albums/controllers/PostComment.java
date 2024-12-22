package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;

import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.daos.AlbumDAO;
import it.polimi.tiw.albums.daos.CommentDAO;
import it.polimi.tiw.albums.daos.PictureDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/PostComment")
public class PostComment extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
private Connection conn;

	
	// CONSTRUCTOR
	public PostComment() {
		super();
	}

	
	
	// SERVLET METHODS
	@Override
	public void init() throws ServletException {		
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);

		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
	}
	
	
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");
			int author = user.getId();
			
			int albumId = validateAndRetrieveAlbumId(request, response);
			if(albumId == -1) return;
			
			int pictureId = validateAndRetrievePictureId(request, response, albumId);
			if(pictureId == -1) return;
			
			String comment = request.getParameter("comment");
			String error = validateComment(request, response, comment);
			if(error!=null) {
				redirectToImageWithError(request, response, albumId, pictureId, error);
				return;
			}
			
			createComment(pictureId, user.getId(), comment);
			redirectToImage(request, response, albumId, pictureId);
		}
		catch (SQLException e) {
			e.printStackTrace(); // for debugging
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
	}
	
	
	
	//HELPER METHODS
	
	private int validateAndRetrievePictureId(HttpServletRequest request, HttpServletResponse response, int albumId) throws IOException, SQLException {
        String pictureIdString = request.getParameter("pictureId");
        if (!InputSanitizer.isValidId(pictureIdString)) {
            returnHome(request, response);
            return -1;
        }

        int pictureId = Integer.parseInt(pictureIdString);
        PictureDAO pictureDao = new PictureDAO(conn);
        
        //Check if picture exists and if picture belongs to album
        if (!pictureDao.pictureExists(pictureId) || !pictureDao.pictureBelongsToAlbum(pictureId, albumId)) {
            returnHome(request, response);
            return -1;
        }

        return pictureId;
    }
	
	private int validateAndRetrieveAlbumId(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		 String albumIdString = request.getParameter("albumId");
	        if (!InputSanitizer.isValidId(albumIdString)) {
	            returnHome(request, response);
	            return -1;
	        }

	        int albumId = Integer.parseInt(albumIdString);
	        AlbumDAO albumDao = new AlbumDAO(conn);
	        
	        //Check if album exists
	        if (!albumDao.albumExists(albumId)) {
	            returnHome(request, response);
	            return -1;
	        }

	        return albumId;
	}
	
	private String validateComment(HttpServletRequest request, HttpServletResponse response, String comment){
		if(!InputSanitizer.isValidCommentBody(comment)) {
			return "Missing or malformed comment body.";
		}
		
		return null;
	}
	
	private void createComment(int pictureId, int authorId, String comment) throws SQLException {
		CommentDAO commentDao = new CommentDAO(conn);
		commentDao.createComment(pictureId, authorId, new Date(), comment);
	}
	
	private void redirectToImageWithError(HttpServletRequest request, HttpServletResponse response, int albumId, int pictureId, String error) throws IOException {
        String paramString = String.format("?error=%s&albumId=%d&pictureId=%d",
                                           error, albumId, pictureId);
        String path = request.getServletContext().getContextPath() + "/Image" + paramString;
        response.sendRedirect(path);
    }
	
	private void redirectToImage(HttpServletRequest request, HttpServletResponse response, int albumId, int pictureId) throws IOException {
		String paramString = String.format("?albumId=%d&pictureId=%d",
               albumId, pictureId);
		String path = request.getServletContext().getContextPath() + "/Image" + paramString;
		response.sendRedirect(path);
	}
	
	private void returnHome(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String homePath = request.getServletContext().getContextPath() + "/Home";
		response.sendRedirect(homePath);
	}
}








