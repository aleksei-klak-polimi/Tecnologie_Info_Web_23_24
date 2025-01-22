package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;

import it.polimi.tiw.albums.CommunicationAPI.ApiResponse;
import it.polimi.tiw.albums.beans.Picture;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.daos.PictureDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet("/EditImage")
@MultipartConfig(
 fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
 maxFileSize = 1024 * 1024 * 10, // 10 MB
 maxRequestSize = 1024 * 1024 * 100 // 100 MB
)
public class EditImage extends DBServlet{
	// ATTRIBUTES
	private static final long serialVersionUID = 1L;

	
	
	
	
	// CONSTRUCTOR
	public EditImage() {
		super();
	}

	// SERVLET METHODS
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");
			int userId = user.getId();

			// PARSE INPUTS
			int pictureId = validateAndRetrievePictureId(request, response, userId);
			if (pictureId == -1) return;
			
			
			String title = null;
			String description = null;
			String dateString = null;
			
			for (Part part : request.getParts()) {
	            if ("title".equals(part.getName())) {
	            	title = new String(part.getInputStream().readAllBytes());
	            }
	            else if ("description".equals(part.getName())) {
	            	description = new String(part.getInputStream().readAllBytes());
	            }
	            else if ("date".equals(part.getName())) {
	            	dateString = new String(part.getInputStream().readAllBytes());
	            }
	        }

			String error = validateInputs(title, description, dateString);
			if (error != null) {
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, error);
				return;
			}
			
			Date date = parseDate(request, response, dateString);
			if(date == null) return;

			// Update the database
			editImage(pictureId, title, description, date);
		} catch (SQLException e) {
			e.printStackTrace(); // for debugging
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
	}
	
	
	
	// HELPER METHODS
	private int validateAndRetrievePictureId(HttpServletRequest request, HttpServletResponse response, int userId)
			throws IOException, SQLException {
		String pictureIdString = request.getParameter("pictureId");
		if (!InputSanitizer.isValidId(pictureIdString)) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid parameter pictureId");
            return -1;
		}

		int pictureId = Integer.parseInt(pictureIdString);
		PictureDAO pictureDao = new PictureDAO(conn);

		// Check if picture exists and belongs to current album
		if(!pictureDao.pictureExists(pictureId)) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No picture found with provided id. pictureId");
        	return -1;
        }
		
		Picture picture = pictureDao.getPictureById(pictureId);
        if(picture.getUploaderId() != userId) {
			sendResponse(response, HttpServletResponse.SC_FORBIDDEN, "Picture does not belong to user");
        	return -1;
        }
		return pictureId;
	}
	
	private String validateInputs(String title, String description, String date) {
        if (!InputSanitizer.isValidTitle(title)) {
            return "Missing or malformed Image Title.";
        }

        if (!InputSanitizer.isValidImageDescription(description)) {
            return "Malformed Image description.";
        }

        if (!InputSanitizer.isValidDate(date)) {
            return "Malformed Image date.";
        }

        return null;
    }

	private Date parseDate(HttpServletRequest request, HttpServletResponse response, String dateString) throws IOException {
		String datePattern = "yyyy-MM-dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
        dateFormat.setLenient(false);
        
        try {
			return dateFormat.parse(dateString);
		}
        catch (ParseException e) {
			String error ="Malformed Image date.";
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, error);
			return null;
		}
	}
	
	private void editImage(int pictureId, String title, String description, Date date) throws SQLException {
		PictureDAO pictureDao = new PictureDAO(conn);
		pictureDao.updatePicture(pictureId, title, description, date);
	}
 	
	private void sendResponse(HttpServletResponse response, int status, String content) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		ApiResponse responseObj = new ApiResponse();
		responseObj.setError(content);
		response.setStatus(status);
		
		response.getWriter().write(new Gson().toJson(responseObj));
	}
}










