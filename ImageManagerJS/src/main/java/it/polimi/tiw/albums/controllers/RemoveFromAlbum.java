package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.SQLException;

import com.google.gson.Gson;

import it.polimi.tiw.albums.CommunicationAPI.ApiResponse;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.daos.AlbumDAO;
import it.polimi.tiw.albums.daos.PictureDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/RemoveFromAlbum")
public class RemoveFromAlbum extends DBServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;

	
	// CONSTRUCTOR
	public RemoveFromAlbum() {
		super();
	}

	
	
	// SERVLET METHODS
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");
			
			int albumId = validateAndRetrieveAlbumId(request, response, user.getId());
			if(albumId == -1) return;
			
			int pictureId = validateAndRetrievePictureId(request, response, albumId);
			if(pictureId == -1) return;
			
			//Picture is in no other albums, redirect to delete picture
			if(lastPictureInstance(request, response, pictureId))
				sendResponse(response, 422, "LastInstace.");
			else {
				//If picture is present in more than one album then simply remove from current one
				removePicture(albumId, pictureId);
			}
		}
		catch (SQLException e) {
			e.printStackTrace(); // for debugging
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
	}
	
	
	
	//HELPER METHODS
	private int validateAndRetrievePictureId(HttpServletRequest request, HttpServletResponse response, int albumId) throws IOException, SQLException {
        String pictureIdString = request.getParameter("pictureId");
        String error = null;
        if (!InputSanitizer.isValidId(pictureIdString))
        	error = "Missing or invalid parameter pictureId";

        int pictureId = Integer.parseInt(pictureIdString);
        PictureDAO pictureDao = new PictureDAO(conn);
        
        //Check if picture exists and if picture belongs to album
        if(!pictureDao.pictureExists(pictureId))
        	error = "No picture found with provided id. pictureId";
        if(!pictureDao.pictureBelongsToAlbum(pictureId, albumId))
        	error = "Picture does not belong to album";
        
        if(error != null){
        	sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, error);
        	return -1;
        }
        else
        	return pictureId;
    }
	
	private int validateAndRetrieveAlbumId(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException, SQLException {
		 String albumIdString = request.getParameter("albumId");
	        if (!InputSanitizer.isValidId(albumIdString)) {
	        	sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid parameter albumId");
	            return -1;
	        }

	        int albumId = Integer.parseInt(albumIdString);
	        AlbumDAO albumDao = new AlbumDAO(conn);
	        
	      //Check if album exists and if user is the owner
	        if(!albumDao.albumExists(albumId)) {
	        	sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No album found with provided id.");
	        	return -1;
	        }
	        if (!albumDao.albumBelongsToUser(albumId, userId)) {
	        	sendResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Album does not belong to user.");
	            return -1;
	        }

	        return albumId;
	}
	
	private boolean lastPictureInstance(HttpServletRequest request, HttpServletResponse response, int pictureId) throws SQLException {
		PictureDAO pictureDao = new PictureDAO(conn);
		if(pictureDao.countPictureInstances(pictureId) == 1)
			return true;
		
		return false;
	}
	
	private void removePicture(int albumId, int pictureId) throws SQLException {
		AlbumDAO albumDao = new AlbumDAO(conn);
		albumDao.removePictureFromAlbum(albumId, pictureId);
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









