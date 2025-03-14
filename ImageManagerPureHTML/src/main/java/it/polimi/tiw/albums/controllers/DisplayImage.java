package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.albums.beans.Album;
import it.polimi.tiw.albums.beans.Comment;
import it.polimi.tiw.albums.beans.Picture;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.controllers.helpers.ConfigManager;
import it.polimi.tiw.albums.controllers.helpers.TemplateEngineBuilder;
import it.polimi.tiw.albums.daos.AlbumDAO;
import it.polimi.tiw.albums.daos.CommentDAO;
import it.polimi.tiw.albums.daos.PictureDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/Image")
public class DisplayImage extends DBServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private int defaultPageSize;
	private int serverPort;
	private ITemplateEngine templateEngine;
	private JakartaServletWebApplication application;
	
	
	// CONSTRUCTOR
	public DisplayImage() {
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
		serverPort = Integer.parseInt(config.getProperty("externalPort"));

	}
	
	

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
        	String path = "/WEB-INF/Image.html";
        	
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            int userId = user.getId();
            String error = request.getParameter("error");
            
            //PARSE INPUTS
            int albumId = validateAndRetrieveAlbumId(request, response, userId);
			if(albumId == -1) return;
			Album album = getAlbumById(albumId);
			
            boolean isOwner = ownsAlbum(userId, albumId);

            int pictureId = validateAndRetrievePictureId(request, response, albumId);
            if(pictureId == -1) return;
            Picture picture = getPictureById(pictureId);
            
            int albumPage = validateAndRetrieveAlbumPage(request, response, albumId);
            
            List<Comment> comments = getComments(pictureId);

            String imageHost = buildImageHost(request);

            prepareContextAndRender(request, response, path, error, isOwner, picture, album, albumPage, imageHost, comments);
        }
        catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
        }
    }
	
	
	//HELPER METHODS
	private int validateAndRetrieveAlbumId(HttpServletRequest request, HttpServletResponse response, int uploader) throws IOException, SQLException {
        String albumIdString = request.getParameter("albumId");
        if (!InputSanitizer.isValidId(albumIdString)) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid parameter albumId");
            return -1;
        }

        int albumId = Integer.parseInt(albumIdString);
        AlbumDAO albumDao = new AlbumDAO(conn);
        
        //Check if album exists
        if (!albumDao.albumExists(albumId)) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No album found with provided id.");
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
	
	private boolean ownsAlbum(int userId, int albumId) throws SQLException {
        AlbumDAO albumDao = new AlbumDAO(conn);
        return albumDao.albumBelongsToUser(albumId, userId);
	}
	
	private List<Comment> getComments(int pictureId) throws SQLException{
		CommentDAO commentDao = new CommentDAO(conn);
		return commentDao.getCommentsByPicture(pictureId);
	}
	
	private String buildImageHost(HttpServletRequest request) {
        String serverDomain = request.getServerName();
        ServletContext context = getServletContext();
        if ("localhost".equals(serverDomain)) {
            return "http://" + serverDomain + ":" + request.getServerPort() + context.getInitParameter("ImageHost");
        }
        else {
        	return "http://" + serverDomain + ":" + serverPort + context.getInitParameter("ImageHost");
        }
	}

	private void prepareContextAndRender(HttpServletRequest request, HttpServletResponse response,
			String path, String error, boolean isOwner, Picture picture, Album album, int albumPage,
			String imageHost, List<Comment> comments) throws IOException {
		
		IWebExchange webExchange = this.application.buildExchange(request, response);
		WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
		ctx.setVariable("error", error);
		ctx.setVariable("isOwner", isOwner);
		ctx.setVariable("picture", picture);
		ctx.setVariable("album", album);
		ctx.setVariable("albumPage", albumPage);
		ctx.setVariable("imageHost", imageHost);
		ctx.setVariable("comments", comments);
		

		templateEngine.process(path, ctx, response.getWriter());
	}

}






