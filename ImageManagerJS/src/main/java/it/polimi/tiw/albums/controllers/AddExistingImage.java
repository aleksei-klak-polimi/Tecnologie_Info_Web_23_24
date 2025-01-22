package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import it.polimi.tiw.albums.CommunicationAPI.ApiResponse;
import it.polimi.tiw.albums.beans.Picture;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.controllers.helpers.DBConnector;
import it.polimi.tiw.albums.daos.AlbumDAO;
import it.polimi.tiw.albums.daos.PictureDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet("/AddToAlbum")
@MultipartConfig(
		fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
		maxFileSize = 1024 * 1024 * 10,      // 10 MB
		maxRequestSize = 1024 * 1024 * 100   // 100 MB
)
public class AddExistingImage extends HttpServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private Connection conn;

	
	
	// CONSTRUCTOR
	public AddExistingImage() {
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
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			HttpSession s = request.getSession();
			User user = (User) s.getAttribute("user");
			
			int albumId = validateAndRetrieveAlbumId(request, response, user.getId());
			if(albumId == -1) return;
			
			List<Integer> pictureIds = validateAndRetrievePictureIds(request, response, user.getId(), albumId);
			if(pictureIds == null) return;
			
			//Add pictures to new album
			addPicturesToAlbum(pictureIds, albumId);
		}
		catch (SQLException e) {
			e.printStackTrace();// for debugging
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
	}
	
	
	
	//HELPER METHODS
	private int validateAndRetrieveAlbumId(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException, SQLException, ServletException {
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
        	sendResponse(response, HttpServletResponse.SC_FORBIDDEN, "Album does not belong to user.");
            return -1;
        }

        return albumId;
    }
	
	private List<Integer> validateAndRetrievePictureIds(HttpServletRequest request, HttpServletResponse response, int userId, int albumId) throws SQLException, IOException, ServletException {
		Set<Integer> pictureIds = new HashSet<Integer>();
		List<String> pictureIdStrings = new ArrayList<>();
		
		for (Part part : request.getParts()) {
            if ("newPictures".equals(part.getName())) {
            	pictureIdStrings.add(new String(part.getInputStream().readAllBytes()));
            }
        }
		
		
		
		if(pictureIdStrings == null || pictureIdStrings.size() == 0) {
			String error = "No pictures were selected, selection must contain at least one picture.";
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, error);
			return null;
		}
		
		//Validate that each entry is a valid id
		for(String id:pictureIdStrings) {
			if (!InputSanitizer.isValidId(id)) {
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid value for pictureId in newPictures");
	            return null;
	        }
			//Check if id is duplicate
			if (pictureIds.contains(Integer.valueOf(id))) {
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Duplicate pictureId value");
	            return null;
	        }
			pictureIds.add(Integer.valueOf(id));
		}
		
		//Validate that each picture actually belongs to user
		PictureDAO pictureDao = new PictureDAO(conn);
		List<Picture> allowedPictures = pictureDao.getPicturesNotInAlbum(albumId, userId);
		Set<Integer> allowedPictureIds = allowedPictures.stream()
				.map(Picture::getId)
				.collect(Collectors.toCollection(HashSet::new));
		
		for(int pictureId:pictureIds) {
			if (!allowedPictureIds.contains(pictureId)) {
				sendResponse(response, HttpServletResponse.SC_FORBIDDEN, "One or more pictures do not belong to user.");
	            return null;
	        }
		}
		
		return pictureIds.stream().toList();
	}
	
	private void addPicturesToAlbum(List<Integer> pictureIds, int albumId) throws SQLException {
		PictureDAO pictureDao = new PictureDAO(conn);
		pictureDao.addExistingPicturesToAlbum(pictureIds, albumId);
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








