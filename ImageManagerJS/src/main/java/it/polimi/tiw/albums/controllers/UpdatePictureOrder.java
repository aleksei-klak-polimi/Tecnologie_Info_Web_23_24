package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;

import it.polimi.tiw.albums.CommunicationAPI.ApiResponse;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.beans.UserAlbumOrdering;
import it.polimi.tiw.albums.daos.AlbumDAO;
import it.polimi.tiw.albums.daos.PictureDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet("/UpdatePictureOrder")
@MultipartConfig(
		fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
		maxFileSize = 1024 * 1024 * 10,      // 10 MB
		maxRequestSize = 1024 * 1024 * 100   // 100 MB
)
public class UpdatePictureOrder extends DBServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;

	// CONSTRUCTOR
	public UpdatePictureOrder() {
			super();
		}

	// SERVLET METHODS
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			HttpSession s = request.getSession();
			User user = (User) s.getAttribute("user");

			int albumId = validateAndRetrieveAlbumId(request, response, user.getId());
			if (albumId == -1)
				return;

			List<UserAlbumOrdering> picturePositions = validateAndRetrievePicturePositions(request, response, albumId);
			if (picturePositions == null)
				return;

			// Add pictures to new album
			updatePicturePositions(picturePositions, albumId, user.getId());
		} catch (SQLException e) {
			e.printStackTrace();// for debugging
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
	}

	
	
	// HELPER METHODS
	private int validateAndRetrieveAlbumId(HttpServletRequest request, HttpServletResponse response, int userId)
			throws IOException, SQLException, ServletException {
		String albumIdString = request.getParameter("albumId");

		if (!InputSanitizer.isValidId(albumIdString)) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid parameter albumId");
			return -1;
		}

		int albumId = Integer.parseInt(albumIdString);
		AlbumDAO albumDao = new AlbumDAO(conn);

		// Check if album exists and if user is the owner
		if (!albumDao.albumExists(albumId)) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No album found with provided id.");
			return -1;
		}

		return albumId;
	}

	private List<UserAlbumOrdering> validateAndRetrievePicturePositions(HttpServletRequest request, HttpServletResponse response,
			int albumId) throws SQLException, IOException, ServletException {
		Set<Integer> pictureIds = new HashSet<>();
		Set<Integer> picturePositions = new HashSet<>();
		List<UserAlbumOrdering> albumOrdering = new ArrayList<>();
		PictureDAO pictureDao = new PictureDAO(conn);
		
		for(Part part : request.getParts()) {
			//Check that each string is a valid picture id and that they belong to the album
			String pictureIdString = part.getName();
			String pictureOrderString = new String(part.getInputStream().readAllBytes());
			
			if (!InputSanitizer.isValidId(pictureIdString)){
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid value for pictureId");
				return null;
			}
			
			if (!InputSanitizer.isValidId(pictureOrderString)){
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid value for picture order");
				return null;
			}
			
			//Check for duplicates
			if (pictureIds.contains(Integer.valueOf(pictureIdString))) {
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Duplicate pictureId value");
				return null;
			}
			
			//Check for duplicates
			if (picturePositions.contains(Integer.valueOf(pictureOrderString))) {
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Duplicate picture order value");
				return null;
			}
			
			//Verify that each pictureId belongs to the album
			if(!pictureDao.pictureBelongsToAlbum(Integer.valueOf(pictureIdString), albumId)) {
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "One or more pictures do not belong to the specified album.");
				return null;
			}
			
			//If all checks passed then add to albumOrdering list
			
			UserAlbumOrdering picturePosition = new UserAlbumOrdering();
			picturePosition.setPictureId(Integer.valueOf(pictureIdString));
			picturePosition.setPictureOrder(Integer.valueOf(pictureOrderString));
			albumOrdering.add(picturePosition);
		}
		
		if(albumOrdering.isEmpty()) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No parameters were submitted in the request.");
			return null;
		}

		return albumOrdering;
	}

	private void updatePicturePositions(List<UserAlbumOrdering> orders, int albumId, int userId) throws SQLException {
		PictureDAO pictureDao = new PictureDAO(conn);
		pictureDao.changePictureOrderPreferenceInAlbum(orders, albumId, userId);
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
