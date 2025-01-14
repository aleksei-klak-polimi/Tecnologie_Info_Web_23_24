package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.albums.beans.Picture;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.controllers.helpers.DBConnector;
import it.polimi.tiw.albums.CommunicationAPI.ApiResponse;
import it.polimi.tiw.albums.beans.Comment;
import it.polimi.tiw.albums.daos.AlbumDAO;
import it.polimi.tiw.albums.daos.CommentDAO;
import it.polimi.tiw.albums.daos.PictureDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/GetImagesByAlbum")
public class GetImagesByAlbum extends HttpServlet {
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private Connection conn;
	
	
	// CONSTRUCTOR
	public GetImagesByAlbum() {
		super();
	}
	
	
	
	// SERVLET METHODS
	@Override
	public void init() throws ServletException {
		try {
			conn = DBConnector.getConnection(getServletContext());
		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
	}
	
	
	
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			HttpSession s = request.getSession();
			User user = (User) s.getAttribute("user");
			
            //PARSE INPUTS
            int albumId = validateAndRetrieveAlbumId(request, response);
            if (albumId == -1) return;
            
            PictureDAO pictureDao = new PictureDAO(conn);
            CommentDAO commentDao = new CommentDAO(conn);
            
            List<Picture> pictures = pictureDao.getPicturesFromAlbum(albumId);
            List<Picture> otherPictures = pictureDao.getPicturesNotInAlbum(albumId, user.getId());
            List<Comment> comments = new ArrayList<>();
            
            for(Picture picture : pictures) {
            	comments.addAll(commentDao.getCommentsByPicture(picture.getId()));
            }
            
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            String pitcuresJson = gson.toJson(pictures);
            String otherPicturesJson = gson.toJson(otherPictures);
            String commentsJson = gson.toJson(comments);
            List<String> jsonList = new ArrayList<>();
            
            jsonList.add(pitcuresJson);
            jsonList.add(otherPicturesJson);
            jsonList.add(commentsJson);
            
            sendResponse(response, HttpServletResponse.SC_OK, gson.toJson(jsonList));
            
        } catch (SQLException e) {
            e.printStackTrace();
        	sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
        }
    }

	
	
	
	//HELPER METHODS
	private int validateAndRetrieveAlbumId(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		String albumIdString = request.getParameter("albumId");
        if (!InputSanitizer.isValidId(albumIdString)) {
        	sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid parameter albumId");
            return -1;
        }

        int albumId = Integer.parseInt(albumIdString);
        AlbumDAO albumDao = new AlbumDAO(conn);
        
        //Check if album exists and belongs to user
        if(!albumDao.albumExists(albumId)) {
        	sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No album found with provided id.");
        	return -1;
        }
        return albumId;
	}

	private void sendResponse(HttpServletResponse response, int status, String content) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		ApiResponse responseObj = new ApiResponse();

		response.setStatus(status);
		switch (status) {
		case 200:
			responseObj.setData(content);
			break;

		// Following cases share same logic
		default:
			responseObj.setError(content);
			break;

		}
		response.getWriter().write(new Gson().toJson(responseObj));
	}
}







