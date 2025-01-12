package it.polimi.tiw.albums.controllers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.io.FilenameUtils;

import com.google.gson.Gson;

import it.polimi.tiw.albums.CommunicationAPI.ApiResponse;
import it.polimi.tiw.albums.beans.Picture;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.controllers.helpers.DBConnector;
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

@WebServlet("/DeleteImage")
public class DeleteImage extends HttpServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private Connection conn;
	
	
	// CONSTRUCTOR
	public DeleteImage() {
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
			//PARSE INPUTS	
			int pictureId = validateAndRetrievePictureId(request, response);
            if(pictureId == -1) return;
            Picture picture = getPictureById(pictureId);
            
            deleteImage(request, picture);
		}
		catch (SQLException e) {
			e.printStackTrace(); // for debugging
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
	}



	//HELPER METHODS
	private int validateAndRetrievePictureId(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		String pictureIdString = request.getParameter("pictureId");
		String error = null;
		
        if (!InputSanitizer.isValidId(pictureIdString))
        	error = "Missing or invalid parameter pictureId";

        int pictureId = Integer.parseInt(pictureIdString);
        PictureDAO pictureDao = new PictureDAO(conn);
        
        //Check if picture exists and belongs to current album
        if(!pictureDao.pictureExists(pictureId)) 
        	error = "No image found with provided id.";
        
        HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
        Picture picture = getPictureById(pictureId);
        
        if (picture.getUploaderId() != user.getId()) {
        	sendResponse(response, HttpServletResponse.SC_FORBIDDEN, "Provided picture id does not belong to User.");
            return -1;
        }
        
        if(error != null) {
        	sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, error);
            return -1;
        }

        return pictureId;
	}

	private Picture getPictureById(int pictureId) throws SQLException {
		PictureDAO pictureDao = new PictureDAO(conn);
		return pictureDao.getPictureById(pictureId);
	}

	private void deleteImage(HttpServletRequest request, Picture picture) throws SQLException {
		ServletContext context = getServletContext();
		String uploadImagePath = context.getInitParameter("uploadImagePath");
		String uploadThumbnailPath = context.getInitParameter("uploadThumbnailPath");
		
		String pictureName = FilenameUtils.getName(picture.getPath());
		String thumbNailName = FilenameUtils.getName(picture.getThumbnailPath());
		
		File pictureFile = new File(request.getServletContext().getRealPath("/").concat(uploadImagePath).concat(pictureName));
		File thumbNailFile = new File(request.getServletContext().getRealPath("/").concat(uploadThumbnailPath).concat(thumbNailName));
		
		//Remove image from database
		PictureDAO pictureDao = new PictureDAO(conn);
		pictureDao.deletePictureById(picture.getId());
		
		//Delete image from system
		if(!pictureFile.delete()) {
			System.out.println("Couldn't deleted the image: " + pictureFile.getName());
			System.out.println("Full file path: " + request.getServletContext().getRealPath("/").concat(uploadImagePath).concat(pictureName));
		}
		if(!thumbNailFile.delete()) {
			System.out.println("Couldn't deleted the thumbnail: " + pictureFile.getName());
			System.out.println("Full file path: " + request.getServletContext().getRealPath("/").concat(uploadThumbnailPath).concat(thumbNailName));
		}
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










