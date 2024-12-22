package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.albums.beans.Album;
import it.polimi.tiw.albums.beans.Comment;
import it.polimi.tiw.albums.beans.Picture;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.daos.AlbumDAO;
import it.polimi.tiw.albums.daos.CommentDAO;
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

@WebServlet("/Image")
public class DisplayImage extends HttpServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private Connection conn;
	private ITemplateEngine templateEngine;
	private JakartaServletWebApplication application;
	
	
	// CONSTRUCTOR
	public DisplayImage() {
			super();
		}

	// SERVLET METHODS
	@Override
	public void init() throws ServletException {
		this.application = JakartaServletWebApplication.buildApplication(getServletContext());
		this.templateEngine = buildTemplateEngine(this.application);

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

	// HELPER METHOD
	private ITemplateEngine buildTemplateEngine(final IWebApplication application) {
		final WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(application);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setSuffix(".html");
		final TemplateEngine templateEngine = new TemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);

		return templateEngine;
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
            
            List<Comment> comments = getComments(pictureId);

            String imageHost = buildImageHost(request);

            prepareContextAndRender(request, response, path, error, isOwner, picture, album, imageHost, comments);
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
            returnHome(request, response);
            return -1;
        }

        int albumId = Integer.parseInt(albumIdString);
        AlbumDAO albumDao = new AlbumDAO(conn);
        
        //Check if album exists
        if (!albumDao.albumExists(albumId)) {
            returnHome(request, response);
            return -1;
        }

        return albumId;
    }
	
	private int validateAndRetrievePictureId(HttpServletRequest request, HttpServletResponse response, int albumId) throws IOException, SQLException {
		String pictureIdString = request.getParameter("pictureId");
        if (!InputSanitizer.isValidId(pictureIdString)) {
            returnHome(request, response);
            return -1;
        }

        int pictureId = Integer.parseInt(pictureIdString);
        PictureDAO pictureDao = new PictureDAO(conn);
        
        //Check if picture exists and belongs to current album
        if (!pictureDao.pictureExists(pictureId) || !pictureDao.pictureBelongsToAlbum(pictureId, albumId)) {
            returnHome(request, response);
            return -1;
        }

        return pictureId;
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
        if ("localhost".equals(serverDomain)) {
            serverDomain += ":" + request.getServerPort();
        }

        ServletContext context = getServletContext();
        return "http://" + serverDomain + context.getInitParameter("ImageHost");
    }
	
	private void returnHome(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String homePath = request.getServletContext().getContextPath() + "/Home";
		response.sendRedirect(homePath);
	}

	private void prepareContextAndRender(HttpServletRequest request, HttpServletResponse response,
			String path, String error, boolean isOwner, Picture picture, Album album,
			String imageHost, List<Comment> comments) throws IOException {
		
		IWebExchange webExchange = this.application.buildExchange(request, response);
		WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
		ctx.setVariable("error", error);
		ctx.setVariable("isOwner", isOwner);
		ctx.setVariable("picture", picture);
		ctx.setVariable("album", album);
		ctx.setVariable("imageHost", imageHost);
		ctx.setVariable("comments", comments);
		

		templateEngine.process(path, ctx, response.getWriter());
	}

}






