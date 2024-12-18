package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.daos.AlbumDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;

@WebServlet("/CreateAlbum")
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
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);

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
		String error = null;
		
		
		//Sanitize Inputs
		if(!InputSanitizer.isValidTitle(albumTitle)) {
			error = "Missing or malformed album Title.";
			String homePath = request.getServletContext().getContextPath() + "/Home";
			homePath = homePath.concat("?error=").concat(error);
			response.sendRedirect(homePath);
		}
		
		
		//Create new album
		else {
			AlbumDAO albumDao = new AlbumDAO(conn);
			try {
				HttpSession s = request.getSession();
				User user = (User) s.getAttribute("user");
				Date creationDate = new Date();
				albumDao.createAlbum(user.getId(), albumTitle, creationDate);
				int albumId = albumDao.getLatestAlbumByUser(user.getId());
				
				//TODO change redirect from home to album page
				String homePath = request.getServletContext().getContextPath() + "/Home";
				homePath = homePath.concat("?albumId=").concat(String.valueOf(albumId));
				response.sendRedirect(homePath);	
			}
			catch (SQLException e) {
				e.printStackTrace(); // for debugging
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
			}
		}
		
		return;
	}
}