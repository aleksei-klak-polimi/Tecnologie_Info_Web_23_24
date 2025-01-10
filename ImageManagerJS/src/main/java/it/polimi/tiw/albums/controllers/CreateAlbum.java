package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import it.polimi.tiw.albums.CommunicationAPI.ApiResponse;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.controllers.helpers.DBConnector;
import it.polimi.tiw.albums.daos.AlbumDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;

@WebServlet("/CreateAlbum")
@MultipartConfig(
		fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
		maxFileSize = 1024 * 1024 * 10,      // 10 MB
		maxRequestSize = 1024 * 1024 * 100   // 100 MB
)
public class CreateAlbum extends HttpServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private Connection conn;
	
	
	
	//CONSTRUCTOR
	public CreateAlbum() {
		super();
	}
	
	
	
	//SERVLET METHODS
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
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		//Retrieve inputs
		String albumTitle = request.getParameter("albumTitle");
		
		for (Part part : request.getParts()) {
            if ("albumTitle".equals(part.getName())) {
            	albumTitle = new String(part.getInputStream().readAllBytes());
            }
        }
		
		//Sanitize Inputs
		if(!InputSanitizer.isValidTitle(albumTitle))
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing or malformed album Title.");
		
		
		//Create new album
		else {
			AlbumDAO albumDao = new AlbumDAO(conn);
			try {
				HttpSession s = request.getSession();
				User user = (User) s.getAttribute("user");
				Date creationDate = new Date();
				albumDao.createAlbum(user.getId(), albumTitle, creationDate);	
			}
			catch (SQLException e) {
				e.printStackTrace(); // for debugging
				sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
			}
		}
		
		return;
	}
	
	
	
	//HELPER METHODS
		private void sendResponse(HttpServletResponse response, int status, String content) throws IOException {
			response.setContentType("application/json");
		    response.setCharacterEncoding("UTF-8");
		    ApiResponse responseObj = new ApiResponse();
			
			response.setStatus(status);
			switch(status) {
				case 200 : 
					responseObj.setRedirect(content);
					break;
				
				//Following cases share same logic
				case 500:
				case 400:
					responseObj.setError(content);
					break;
				
			}
			response.getWriter().write(new Gson().toJson(responseObj));
		}
}












