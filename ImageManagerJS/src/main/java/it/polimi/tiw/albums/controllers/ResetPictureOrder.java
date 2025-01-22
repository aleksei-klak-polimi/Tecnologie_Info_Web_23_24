package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.SQLException;

import com.google.gson.Gson;

import it.polimi.tiw.albums.CommunicationAPI.ApiResponse;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.controllers.helpers.DBConnector;
import it.polimi.tiw.albums.daos.AlbumDAO;
import it.polimi.tiw.albums.daos.PictureDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ResetPictureOrder")
@MultipartConfig(
		fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
		maxFileSize = 1024 * 1024 * 10,      // 10 MB
		maxRequestSize = 1024 * 1024 * 100   // 100 MB
)
public class ResetPictureOrder extends DBServlet{
	// ATTRIBUTES
	private static final long serialVersionUID = 1L;

	
	
	
	
	// CONSTRUCTOR
	public ResetPictureOrder() {
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
			if (albumId == -1)
				return;

			resetPicturePositions(albumId, user.getId());
			
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

	private void resetPicturePositions(int albumId, int userId) throws SQLException {
		PictureDAO pictureDao = new PictureDAO(conn);
		pictureDao.resetPictureOrderPreferenceInAlbumByUser(userId, albumId);
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
