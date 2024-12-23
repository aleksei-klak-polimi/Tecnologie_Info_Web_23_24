package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.daos.AlbumDAO;
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

@WebServlet("/RemoveFromAlbum")
public class RemoveFromAlbum extends HttpServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_PAGE_SIZE = 5;
	private Connection conn;

	
	// CONSTRUCTOR
	public RemoveFromAlbum() {
		super();
	}

	
	
	// SERVLET METHODS
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
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");
			
			int albumId = validateAndRetrieveAlbumId(request, response, user.getId());
			if(albumId == -1) return;
			
			int pictureId = validateAndRetrievePictureId(request, response, albumId);
			if(pictureId == -1) return;
			
			int albumPage = validateAndRetrieveAlbumPage(request, response, albumId);
			if(albumPage == -1) return;
			
			//Picture is in no other albums, redirect to delete picture
			if(lastPictureInstance(request, response, pictureId))
				redirectToDelete(request, response, albumId, albumPage, pictureId);
			else {
				//If picture is present in more than one album then simply remove from current one
				removePicture(albumId, pictureId);
				redirectToAlbum(request, response, albumId, albumPage);
			}
		}
		catch (SQLException e) {
			e.printStackTrace(); // for debugging
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
	}
	
	
	
	//HELPER METHODS
	private int validateAndRetrievePictureId(HttpServletRequest request, HttpServletResponse response, int albumId) throws IOException, SQLException {
        String pictureIdString = request.getParameter("pictureId");
        if (!InputSanitizer.isValidId(pictureIdString)) {
            returnHome(request, response);
            return -1;
        }

        int pictureId = Integer.parseInt(pictureIdString);
        PictureDAO pictureDao = new PictureDAO(conn);
        
        //Check if picture exists and if picture belongs to album
        if (!pictureDao.pictureExists(pictureId) || !pictureDao.pictureBelongsToAlbum(pictureId, albumId)) {
            returnHome(request, response);
            return -1;
        }

        return pictureId;
    }
	
	private int validateAndRetrieveAlbumId(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException, SQLException {
		 String albumIdString = request.getParameter("albumId");
	        if (!InputSanitizer.isValidId(albumIdString)) {
	            returnHome(request, response);
	            return -1;
	        }

	        int albumId = Integer.parseInt(albumIdString);
	        AlbumDAO albumDao = new AlbumDAO(conn);
	        
	        //Check if album exists
	        if (!albumDao.albumExists(albumId) || !albumDao.albumBelongsToUser(albumId, userId)) {
	            returnHome(request, response);
	            return -1;
	        }

	        return albumId;
	}
	
	private int validateAndRetrieveAlbumPage(HttpServletRequest request, HttpServletResponse response, int albumId) throws IOException, SQLException {
		 String albumPageString = request.getParameter("albumPage");
	        if (!InputSanitizer.isValidAlbumPage(albumPageString)) {
	            returnHome(request, response);
	            return -1;
	        }

	        int albumPage = Integer.parseInt(albumPageString);
	        AlbumDAO albumDao = new AlbumDAO(conn);
	        
	        //Check if album page is valid page
            int pictureCount = albumDao.getAmountOfPicturesByAlbum(albumId);
            int maxAlbumPage = Math.max(1, (int) Math.ceil((double) pictureCount / DEFAULT_PAGE_SIZE));

            if (albumPage > maxAlbumPage) {
            	redirectToAlbum(request, response, albumId, maxAlbumPage);
                return -1;
            }

	        return albumPage;
	}
	
	private boolean lastPictureInstance(HttpServletRequest request, HttpServletResponse response, int pictureId) throws SQLException {
		PictureDAO pictureDao = new PictureDAO(conn);
		if(pictureDao.countPictureInstances(pictureId) == 1)
			return true;
		
		return false;
	}
	
	private void removePicture(int albumId, int pictureId) throws SQLException {
		AlbumDAO albumDao = new AlbumDAO(conn);
		albumDao.removePictureFromAlbum(albumId, pictureId);
	}
	
	private void returnHome(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String homePath = request.getServletContext().getContextPath() + "/Home";
		response.sendRedirect(homePath);
	}

	private void redirectToDelete(HttpServletRequest request, HttpServletResponse response, int albumId, int albumPage, int pictureId) throws IOException {
		String paramString = String.format("?albumId=%d&albumPage=%d&pictureId=%d&returnToAlbum=true", albumId, albumPage, pictureId);
		String path = request.getServletContext().getContextPath() + "/DeleteImage" + paramString;
		response.sendRedirect(path);
	}
	
	private void redirectToAlbum(HttpServletRequest request, HttpServletResponse response, int albumId, int albumPage) throws IOException{
		String paramString = String.format("?albumId=%d&albumPage=%d", albumId, albumPage);
		String path = request.getServletContext().getContextPath() + "/Album" + paramString;
		response.sendRedirect(path);
	}
}









