package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.SQLException;

import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.albums.beans.Album;
import it.polimi.tiw.albums.beans.Picture;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.controllers.helpers.ConfigManager;
import it.polimi.tiw.albums.controllers.helpers.TemplateEngineBuilder;
import it.polimi.tiw.albums.daos.AlbumDAO;
import it.polimi.tiw.albums.daos.PictureDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/RemoveFromLastAlbum")
public class RemoveFromLastAlbum extends DBServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private int defaultPageSize;
	private ITemplateEngine templateEngine;
	private JakartaServletWebApplication application;
	
	
	// CONSTRUCTOR
	public RemoveFromLastAlbum() {
		super();
	}
	
	
	
	// SERVLET METHODS
	@Override
	public void init() throws ServletException {
		super.init();
		
		this.application = JakartaServletWebApplication.buildApplication(getServletContext());
		this.templateEngine = TemplateEngineBuilder.buildTemplateEngine(this.application);
			
		ConfigManager config = ConfigManager.getInstance();
			
		defaultPageSize = Integer.parseInt(config.getProperty("imagesPerPage"));
	}
	
	
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		try {
			String path = "/WEB-INF/RemoveFromLastAlbum.html";
			
			HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");
			int userId = user.getId();
			
			//PARSE INPUTS
			int albumId = validateAndRetrieveAlbumId(request, response, userId);
			if(albumId == -1) return;
			Album album = getAlbumById(albumId);
			
			int pictureId = validateAndRetrievePictureId(request, response, albumId);
            if(pictureId == -1) return;
            Picture picture = getPictureById(pictureId);
            
            int albumPage = validateAndRetrieveAlbumPage(request, response, albumId);
            if(albumPage == -1) return;
            
            String imageHost = buildImageHost(request);

            prepareContextAndRender(request, response, path, picture, album, albumPage, imageHost);
		}
		catch (SQLException e) {
			e.printStackTrace(); // for debugging
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
	}



	//HELPER METHODS
	private int validateAndRetrieveAlbumId(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException, SQLException {
		String albumIdString = request.getParameter("albumId");
        if (!InputSanitizer.isValidId(albumIdString)) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid parameter albumId");
            return -1;
        }

        int albumId = Integer.parseInt(albumIdString);
        AlbumDAO albumDao = new AlbumDAO(conn);
        
        //Check if album exists and belongs to user
        if(!albumDao.albumExists(albumId)) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No album found with provided id.");
        	return -1;
        }
        if (!albumDao.albumBelongsToUser(albumId, userId)) {
        	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Album does not belong to user.");
            return -1;
        }

        return albumId;
	}
	
	private int validateAndRetrievePictureId(HttpServletRequest request, HttpServletResponse response, int albumId) throws IOException, SQLException {
		String pictureIdString = request.getParameter("pictureId");
        if (!InputSanitizer.isValidId(pictureIdString)) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid parameter pictureId");
            return -1;
        }

        int pictureId = Integer.parseInt(pictureIdString);
        PictureDAO pictureDao = new PictureDAO(conn);
        
        //Check if picture exists and belongs to current album
        if(!pictureDao.pictureExists(pictureId)) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No picture found with provided id. pictureId");
        	return -1;
        }
        if(!pictureDao.pictureBelongsToAlbum(pictureId, albumId)) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Picture does not belong to album");
        	return -1;
        }
        
        //Check that album is actually last instance of picture
        if(pictureDao.countPictureInstances(pictureId) > 1) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Not picture's last instance");
        	return -1;
        }

        return pictureId;
	}
	
	private int validateAndRetrieveAlbumPage(HttpServletRequest request, HttpServletResponse response, int albumId) throws IOException, SQLException {
	     HttpSession s = request.getSession();
	     //Checking for null pointer
	     Integer albumPageInteger = (Integer) s.getAttribute("albumPage");
	     if(albumPageInteger == null || albumPageInteger.intValue() <= 0) {
	         return 1;
	     }
	     
	     int albumPage = albumPageInteger;
	     
	     AlbumDAO albumDao = new AlbumDAO(conn);
	        
	     //Check if album page is valid page
	     int pictureCount = albumDao.getAmountOfPicturesByAlbum(albumId);
	     int maxAlbumPage = Math.max(1, (int) Math.ceil((double) pictureCount / defaultPageSize));

	     if (albumPage > maxAlbumPage) return maxAlbumPage;

	     return albumPage;
	}
	
	private Album getAlbumById(int albumId) throws SQLException {
		AlbumDAO albumDao = new AlbumDAO(conn);
		return albumDao.getAlbumById(albumId);
	}

	private Picture getPictureById(int pictureId) throws SQLException {
		PictureDAO pictureDao = new PictureDAO(conn);
		return pictureDao.getPictureById(pictureId);
	}
	
	private String buildImageHost(HttpServletRequest request) {
        String serverDomain = request.getServerName();
        if ("localhost".equals(serverDomain)) {
            serverDomain += ":" + request.getServerPort();
        }

        ServletContext context = getServletContext();
        return "http://" + serverDomain + context.getInitParameter("ImageHost");
    }

	private void prepareContextAndRender(HttpServletRequest request, HttpServletResponse response,
			String path, Picture picture, Album album, int albumPage,
			String imageHost) throws IOException, SQLException {
		
		IWebExchange webExchange = this.application.buildExchange(request, response);
		WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
		ctx.setVariable("picture", picture);
		ctx.setVariable("album", album);
		ctx.setVariable("imageHost", imageHost);
		ctx.setVariable("albumPage", albumPage);

		templateEngine.process(path, ctx, response.getWriter());
	}
}










