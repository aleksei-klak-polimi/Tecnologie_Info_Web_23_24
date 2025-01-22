package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.albums.beans.Picture;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.controllers.helpers.ConfigManager;
import it.polimi.tiw.albums.controllers.helpers.TemplateEngineBuilder;
import it.polimi.tiw.albums.beans.Album;
import it.polimi.tiw.albums.daos.AlbumDAO;
import it.polimi.tiw.albums.daos.PictureDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/Album")
public class DisplayAlbum extends DBServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private int defaultPageSize;
	private int serverPort;
	private ITemplateEngine templateEngine;
	private JakartaServletWebApplication application;
	
	
	// CONSTRUCTOR
	public DisplayAlbum() {
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
        String path = "/WEB-INF/Album.html";

        try {
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            String error = request.getParameter("error");
            
            //PARSE INPUTS
            int albumId = validateAndRetrieveAlbumId(request, response);
            if (albumId == -1) return;

            int albumPage = validateAndRetrieveAlbumPage(request, response, albumId);
            if(albumPage == -1) return;
            
            //Add albumPage to session
            HttpSession s = request.getSession();
			s.setAttribute("albumPage", albumPage);
			
			AlbumDAO albumDao = new AlbumDAO(conn);
            Album album = albumDao.getAlbumById(albumId);
            boolean isOwner = albumDao.albumBelongsToUser(albumId, user.getId());

            PictureDAO pictureDao = new PictureDAO(conn);
            List<Picture> pictures = pictureDao.getPicturesFromAlbumByPage(albumId, albumPage, defaultPageSize);

            String imageHost = buildImageHost(request);

            prepareContextAndRender(request, response, path, error, isOwner, pictures, album, imageHost, 
                                     albumPage, albumId);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
        }
    }

	
	
	
	//HELPER METHODS
	private int validateAndRetrieveAlbumId(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
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
        return albumId;
	}
	
	private int validateAndRetrieveAlbumPage(HttpServletRequest request, HttpServletResponse response, int albumId) throws IOException, SQLException {
	     String albumPageString = request.getParameter("albumPage");
	     
	     if(albumPageString == null) {
	    	 //If no page is provided then default to first page
	    	 return 1;
	     }    
	     else if (!InputSanitizer.isValidId(albumPageString)) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter albumPage");
	        return -1;
	     }
	     
	     int albumPage = Integer.parseInt(albumPageString);
	     
	     AlbumDAO albumDao = new AlbumDAO(conn);
	        
	     //Check if album page is valid page
	     int pictureCount = albumDao.getAmountOfPicturesByAlbum(albumId);
	     int maxAlbumPage = Math.max(1, (int) Math.ceil((double) pictureCount / defaultPageSize));

	     if (albumPage > maxAlbumPage) return maxAlbumPage;

	     return albumPage;
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
	
	private boolean hasMorePages(int albumId, int albumPage) throws SQLException {
		AlbumDAO albumDao = new AlbumDAO(conn);
		int pictureCount = albumDao.getAmountOfPicturesByAlbum(albumId);
		int maxAlbumPage = Math.max(1, (int) Math.ceil((double) pictureCount / defaultPageSize));
		
		return albumPage < maxAlbumPage;
	}
	
	private void prepareContextAndRender(HttpServletRequest request, HttpServletResponse response, String path,
			String error, boolean isOwner, List<Picture> pictures, Album album, String imageHost, int albumPage,
			int albumId) throws IOException, SQLException {
		boolean hasNextPictures = hasMorePages(albumId, albumPage);
		boolean hasPrevPictures = albumPage > 1;

		String nextPicturesPath = hasNextPictures ? albumPagePath(albumId, albumPage + 1) : "";
		String prevPicturesPath = hasPrevPictures ? albumPagePath(albumId, albumPage - 1) : "";

		IWebExchange webExchange = this.application.buildExchange(request, response);
		WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
		ctx.setVariable("error", error);
		ctx.setVariable("isOwner", isOwner);
		ctx.setVariable("pictures", pictures);
		ctx.setVariable("album", album);
		ctx.setVariable("imageHost", imageHost);
		ctx.setVariable("hasNextPictures", hasNextPictures);
		ctx.setVariable("hasPrevPictures", hasPrevPictures);
		ctx.setVariable("pageSize", defaultPageSize);
		ctx.setVariable("nextPicturesPath", nextPicturesPath);
		ctx.setVariable("prevPicturesPath", prevPicturesPath);

		templateEngine.process(path, ctx, response.getWriter());
	}
	
	private String albumPagePath(int albumId, int page) {
        return "/Album?albumId=" + albumId + "&albumPage=" + page;
    }
	
}







