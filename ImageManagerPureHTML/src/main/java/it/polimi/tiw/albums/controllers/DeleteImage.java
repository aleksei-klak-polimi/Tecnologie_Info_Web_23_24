package it.polimi.tiw.albums.controllers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.io.FilenameUtils;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.albums.beans.Album;
import it.polimi.tiw.albums.beans.Picture;
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

@WebServlet("/DeleteImage")
public class DeleteImage extends HttpServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private Connection conn;
	private ITemplateEngine templateEngine;
	private JakartaServletWebApplication application;
	
	
	// CONSTRUCTOR
	public DeleteImage() {
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
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		try {
			String path = "/WEB-INF/DeleteImage.html";
		
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
            
            String imageHost = buildImageHost(request);

            prepareContextAndRender(request, response, path, picture, album, imageHost);
		}
		catch (SQLException e) {
			e.printStackTrace(); // for debugging
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
	}
	
	
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
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
            
            if(request.getParameter("Delete") != null) {
            	//User confirmed they want to delete image
            	//Remove from database
            	deleteImage(request, picture);
            	redirectToAlbum(request, response, album.getId());
            }
            else {
            	//User does not want to delete image
            	redirectToImage(request, response, album.getId(), picture.getId());
            }
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
            returnHome(request, response);
            return -1;
        }

        int albumId = Integer.parseInt(albumIdString);
        AlbumDAO albumDao = new AlbumDAO(conn);
        
        //Check if album exists and belongs to user
        if (!albumDao.albumExists(albumId) || !albumDao.albumBelongsToUser(albumId, userId)) {
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
	
	private String buildImageHost(HttpServletRequest request) {
        String serverDomain = request.getServerName();
        if ("localhost".equals(serverDomain)) {
            serverDomain += ":" + request.getServerPort();
        }

        ServletContext context = getServletContext();
        return "http://" + serverDomain + context.getInitParameter("ImageHost");
    }
	
	private void deleteImage(HttpServletRequest request, Picture picture) throws SQLException {
		ServletContext context = getServletContext();
		String uploadImagePath = context.getInitParameter("uploadImagePath");
		String uploadThumbnailPath = context.getInitParameter("uploadThumbnailPath");
		
		String pictureName = FilenameUtils.getName(picture.getPath());
		String thumbNailName = FilenameUtils.getName(picture.getThumbnailPath());
		
		File pictureFile = new File(request.getServletContext().getRealPath("/").concat(uploadImagePath).concat(pictureName));
		File thumbNailFile = new File(request.getServletContext().getRealPath("/").concat(uploadThumbnailPath).concat(thumbNailName));
		
		//Remove image from database
		PictureDAO pictureDao = new PictureDAO(conn);
		pictureDao.deletePictureById(picture.getId());
		
		//Delete image from system
		if(!pictureFile.delete()) {
			System.out.println("Couldn't deleted the image: " + pictureFile.getName());
			System.out.println("Full file path: " + request.getServletContext().getRealPath("/").concat(uploadImagePath).concat(pictureName));
		}
		if(!thumbNailFile.delete()) {
			System.out.println("Couldn't deleted the thumbnail: " + pictureFile.getName());
			System.out.println("Full file path: " + request.getServletContext().getRealPath("/").concat(uploadThumbnailPath).concat(thumbNailName));
		}
	}
	
	private void redirectToAlbum(HttpServletRequest request, HttpServletResponse response, int albumId) throws IOException {
		String paramString = String.format("?albumId=%d", albumId);
		String path = request.getServletContext().getContextPath() + "/Album" + paramString;
		response.sendRedirect(path);
	}
	
	private void redirectToImage(HttpServletRequest request, HttpServletResponse response, int albumId, int pictureId) throws IOException {
		String paramString = String.format("?albumId=%d&pictureId=%d", albumId, pictureId);
		String path = request.getServletContext().getContextPath() + "/Image" + paramString;
		response.sendRedirect(path);
	}
	
	private void returnHome(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String homePath = request.getServletContext().getContextPath() + "/Home";
		response.sendRedirect(homePath);
	}

	private void prepareContextAndRender(HttpServletRequest request, HttpServletResponse response,
			String path, Picture picture, Album album,
			String imageHost) throws IOException {
		
		IWebExchange webExchange = this.application.buildExchange(request, response);
		WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
		ctx.setVariable("picture", picture);
		ctx.setVariable("album", album);
		ctx.setVariable("imageHost", imageHost);

		templateEngine.process(path, ctx, response.getWriter());
	}
}










