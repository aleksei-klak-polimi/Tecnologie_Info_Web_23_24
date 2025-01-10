package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.google.gson.Gson;

import it.polimi.tiw.albums.CommunicationAPI.ApiResponse;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.controllers.helpers.DBConnector;
import it.polimi.tiw.albums.daos.UserDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet("/LogIn")
@MultipartConfig(
		fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
		maxFileSize = 1024 * 1024 * 10,      // 10 MB
		maxRequestSize = 1024 * 1024 * 100   // 100 MB
)
public class LogIn extends HttpServlet {
	// ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private Connection conn;

	
	
	// CONSTRUCTOR
	public LogIn() {
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
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String path = "/WEB-INF/Log-In.html";
		ServletContext sc = getServletContext();
		sc.getRequestDispatcher(path).forward(request, response);
	}
	
	
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		//Retrieve inputs
		String username = null;
		String password = null;
		
		for (Part part : request.getParts()) {
            if ("username".equals(part.getName())) {
                username = new String(part.getInputStream().readAllBytes());
            } else if ("password".equals(part.getName())) {
                password = new String(part.getInputStream().readAllBytes());
            }
        }
				
		// Sanitize Inputs
		if (!InputSanitizer.isValidUsername(username)) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing or wrong username.");
			return;
		}
		else if (!InputSanitizer.isValidPassword(password)) {
			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing or wrong password.");
			return;
		}
		else {
			// Attempt to retrieve user from database
			UserDAO userDao = new UserDAO(conn);
			User user = null;
			try {
				if(userDao.isUsernameAvailable(username)) {
					//username available means no existing user with that username
					sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Username not found.");
					return;
				}
				
				user = userDao.getUserByCredentials(username, password);
			}
			catch (SQLException e) {
				e.printStackTrace(); // for debugging
				sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed.");
				return;
			}
			
			if(user != null) {
				//Add user information to html session
				HttpSession s = request.getSession();
				s.setAttribute("user", user);
				
				String homePath = request.getServletContext().getContextPath() + "/Home";
				sendResponse(response, HttpServletResponse.SC_OK, homePath);
			}
			else {
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Password and username do not match.");
			}
		}
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





