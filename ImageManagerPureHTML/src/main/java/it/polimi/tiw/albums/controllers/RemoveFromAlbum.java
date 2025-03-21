package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.SQLException;

import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.controllers.helpers.ConfigManager;
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
	private int defaultPageSize;

	
	// CONSTRUCTOR
	public RemoveFromAlbum() {
		super();
	}

	
	
	// SERVLET METHODS
	@Override
	public void init() throws ServletException {
		super.init();
		
		ConfigManager config = ConfigManager.getInstance();
			
		defaultPageSize = Integer.parseInt(config.getProperty("imagesPerPage"));
	}
	
	
	
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
				redirectToDelete(request, response, albumId, pictureId);
			else {
				int albumPage = validateAndRetrieveAlbumPage(request, response, albumId);
				//If picture is present in more than one album then simply remove from current one
				removePicture(albumId, pictureId);
				redirectToAlbum(request, response, albumId, albumPage);
			}
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
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid parameter pictureId");
            return -1;
        }

        int pictureId = Integer.parseInt(pictureIdString);
        PictureDAO pictureDao = new PictureDAO(conn);
        
        //Check if picture exists and if picture belongs to album
        if(!pictureDao.pictureExists(pictureId)) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No picture found with provided id. pictureId");
        	return -1;
        }
        if(!pictureDao.pictureBelongsToAlbum(pictureId, albumId)) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Picture does not belong to album");
        	return -1;
        }

        return pictureId;
    }
	
	private int validateAndRetrieveAlbumId(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException, SQLException {
		 String albumIdString = request.getParameter("albumId");
	        if (!InputSanitizer.isValidId(albumIdString)) {
	        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid parameter albumId");
	            return -1;
	        }

	        int albumId = Integer.parseInt(albumIdString);
	        AlbumDAO albumDao = new AlbumDAO(conn);
	        
	      //Check if album exists and if user is the owner
	        if(!albumDao.albumExists(albumId)) {
	        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No album found with provided id.");
	        	return -1;
	        }
	        if (!albumDao.albumBelongsToUser(albumId, userId)) {
	        	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Album does not belong to user.");
	            return -1;
	        }

	        return albumId;
	}
	
	private int validateAndRetrieveAlbumPage(HttpServletRequest request, HttpServletResponse response, int albumId) throws IOException, SQLException {
		HttpSession s = request.getSession();
	     //Checking for null pointer
	     Integer albumPageInteger = (Integer) s.getAttribute("albumPage");
	     if(albumPageInteger == null || albumPageInteger.intValue() <= 0) {
	         return 1;
	     }
	     
	     int albumPage = albumPageInteger;
	     
	     AlbumDAO albumDao = new AlbumDAO(conn);
	        
	     //Check if album page is valid page
	     int pictureCount = albumDao.getAmountOfPicturesByAlbum(albumId);
	     int maxAlbumPage = Math.max(1, (int) Math.ceil((double) pictureCount / defaultPageSize));

	     if (albumPage > maxAlbumPage) return maxAlbumPage;

	     return albumPage;
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

	private void redirectToDelete(HttpServletRequest request, HttpServletResponse response, int albumId, int pictureId) throws IOException {
		String paramString = String.format("?albumId=%d&pictureId=%d", albumId, pictureId);
		String path = request.getServletContext().getContextPath() + "/RemoveFromLastAlbum" + paramString;
		response.sendRedirect(path);
	}
	
	private void redirectToAlbum(HttpServletRequest request, HttpServletResponse response, int albumId, int albumPage) throws IOException{
		String paramString = String.format("?albumId=%d&albumPage=%d", albumId, albumPage);
		String path = request.getServletContext().getContextPath() + "/Album" + paramString;
		response.sendRedirect(path);
	}
}









