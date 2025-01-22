package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.daos.AlbumDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;

@WebServlet("/CreateAlbum")
public class CreateAlbum extends DBServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	
	
	
	//CONSTRUCTOR
	public CreateAlbum() {
		super();
	}
	
	
	
	//SERVLET METHODS
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
				
				String homePath = request.getServletContext().getContextPath() + "/Album";
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