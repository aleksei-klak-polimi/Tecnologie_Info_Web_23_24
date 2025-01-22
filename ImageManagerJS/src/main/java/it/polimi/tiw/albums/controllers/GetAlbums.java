package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.albums.CommunicationAPI.ApiResponse;
import it.polimi.tiw.albums.beans.Album;
import it.polimi.tiw.albums.daos.AlbumDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/GetAlbums")
public class GetAlbums extends DBServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	
	
	
	
	
	
	// SERVLET METHODS
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {			
			AlbumDAO albumDao = new AlbumDAO(conn);
			List<Album> albums = albumDao.getAllAlbums();
			
			Gson gson = new GsonBuilder().setDateFormat("yyyy/MM/dd").create();
			String json = gson.toJson(albums);
			
			sendResponse(response, HttpServletResponse.SC_OK, json);
		}
		catch (SQLException e) {
			e.printStackTrace(); // for debugging
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
	}
	
	
	//HELPER METHODS
		private void sendResponse(HttpServletResponse response, int status, String content) throws IOException {
			response.setContentType("application/json");
		    response.setCharacterEncoding("UTF-8");
		    ApiResponse responseObj = new ApiResponse();
			
			response.setStatus(status);
			switch(status) {
				case 200: 
					responseObj.setData(content);
					break;
				
				//Following cases share same logic
				case 500:
					responseObj.setError(content);
					break;
				
			}
			response.getWriter().write(new Gson().toJson(responseObj));
		}
}








