package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import com.google.gson.Gson;

import it.polimi.tiw.albums.CommunicationAPI.ApiResponse;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.daos.CommentDAO;
import it.polimi.tiw.albums.daos.PictureDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet("/PostComment")
@MultipartConfig(
 fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
 maxFileSize = 1024 * 1024 * 10, // 10 MB
 maxRequestSize = 1024 * 1024 * 100 // 100 MB
)
public class PostComment extends DBServlet{
	private static final long serialVersionUID = 1L;
	


	
	// CONSTRUCTOR
	public PostComment() {
		super();
	}

	
	
	// SERVLET METHODS
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");
			
			int pictureId = validateAndRetrievePictureId(request, response, user.getId());
			if(pictureId == -1) return;
			
			String comment = validateAndRetrieveComment(request, response);
			if(comment == null) return;
			
			createComment(pictureId, user.getId(), comment);
		}
		catch (SQLException e) {
			e.printStackTrace(); // for debugging
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
	}
	
	
	
	//HELPER METHODS
	private int validateAndRetrievePictureId(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException, SQLException, ServletException {
		String pictureIdString = request.getParameter("pictureId");
        
        if (!InputSanitizer.isValidId(pictureIdString)) {
        	sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid parameter pictureId");
            return -1;
        }

        int pictureId = Integer.parseInt(pictureIdString);
        PictureDAO pictureDao = new PictureDAO(conn);
        
        //Check if picture exists and if picture belongs to user
        if(!pictureDao.pictureExists(pictureId)) {
        	sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No picture found with provided id. pictureId");
        	return -1;
        }
        return pictureId;
    }
	
	private String validateAndRetrieveComment(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
		String comment = null;
        Part part = request.getPart("comment");
        
        if(part == null) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid comment");
			return null;
        }
		else
			comment = new String(part.getInputStream().readAllBytes());
		
		
		if(!InputSanitizer.isValidCommentBody(comment)) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing or malformed comment body.");
			return null;
		}
		
		return comment;
	}
	
	private void createComment(int pictureId, int authorId, String comment) throws SQLException {
		CommentDAO commentDao = new CommentDAO(conn);
		commentDao.createComment(pictureId, authorId, new Date(), comment);
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








