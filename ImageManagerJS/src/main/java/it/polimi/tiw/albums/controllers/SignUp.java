package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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
import it.polimi.tiw.albums.daos.UserDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;

@WebServlet("/SignUp")
@MultipartConfig(
		fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
		maxFileSize = 1024 * 1024 * 10,      // 10 MB
		maxRequestSize = 1024 * 1024 * 100   // 100 MB
)
public class SignUp extends HttpServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private Connection conn;
	
	
	
	//CONSTRUCTOR
	public SignUp() {
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
		String username = null;
		String password = null;
		String repeatPassword = null;
		String email = null;
		
		for (Part part : request.getParts()) {
            if ("username".equals(part.getName())) {
                username = new String(part.getInputStream().readAllBytes());
            } else if ("password".equals(part.getName())) {
                password = new String(part.getInputStream().readAllBytes());
            } else if ("repeatPassword".equals(part.getName())) {
            	repeatPassword = new String(part.getInputStream().readAllBytes());
            } else if ("email".equals(part.getName())) {
            	email = new String(part.getInputStream().readAllBytes());
            }
        }
		
		try {
			UserDAO userDao = new UserDAO(conn);
			// Sanitize Inputs
			if (!InputSanitizer.isValidUsername(username)) {
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing or wrong username.");
				return;
			}
			else if (!InputSanitizer.isVaildEmail(email)) {
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing or wrong email.");
				return;
			}
			else if (!InputSanitizer.isValidPassword(password)) {
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing or malformed password.");
				return;
			}
			else if (repeatPassword == null || repeatPassword.isBlank() || repeatPassword.isEmpty()) {
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing repeat password.");
				return;
			}
			else if (!password.equals(repeatPassword)) {
				sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Passwords don't match.");
				return;
			}
			else {
				// Check if database already contains provided username or email
				if (!userDao.isUsernameAvailable(username)) {
					sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Username is not available.");
					return;
				} else if (!userDao.isEmailAvailable(email)) {
					sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Email is not available.");
					return;
				}

			}

			// Credentials are valid, create user and redirect to home page
			userDao.createUser(username, email, password);
			User user = userDao.getUserByCredentials(username, password);
			// Add user information to html session
			HttpSession s = request.getSession();
			s.setAttribute("user", user);
			sendResponse(response, HttpServletResponse.SC_OK, new Gson().toJson(user));
			
		} catch (SQLException e) {
			e.printStackTrace(); // for debugging
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed.");
		}

		return;
	}
	
	
	// HELPER METHODS
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
		case 500:
		case 400:
			responseObj.setError(content);
			break;

		}
		response.getWriter().write(new Gson().toJson(responseObj));
	}
	
	
}






