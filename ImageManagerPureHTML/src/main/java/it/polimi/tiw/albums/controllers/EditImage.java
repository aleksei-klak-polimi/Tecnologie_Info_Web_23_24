package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

@WebServlet("/EditImage")
public class EditImage extends DBServlet{
	// ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private int defaultPageSize;
	private ITemplateEngine templateEngine;
	private JakartaServletWebApplication application;

	// CONSTRUCTOR
	public EditImage() {
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
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String path = "/WEB-INF/EditImage.html";
			String error = request.getParameter("error");

			HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");
			int userId = user.getId();

			// PARSE INPUTS
			int albumId = validateAndRetrieveAlbumId(request, response, userId);
			if (albumId == -1)
				return;
			Album album = getAlbumById(albumId);

			int pictureId = validateAndRetrievePictureId(request, response, albumId);
			if (pictureId == -1)
				return;
			Picture picture = getPictureById(pictureId);
			
			int albumPage = validateAndRetrieveAlbumPage(request, response, albumId);

			String imageHost = buildImageHost(request);

			prepareContextAndRender(request, response, path, picture, album, albumPage, imageHost, error);
		} catch (SQLException e) {
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

			// PARSE INPUTS
			int albumId = validateAndRetrieveAlbumId(request, response, userId);
			if (albumId == -1)
				return;

			int pictureId = validateAndRetrievePictureId(request, response, albumId);
			if (pictureId == -1) return;
			
			String title = request.getParameter("title").trim();
			String description = request.getParameter("description").trim();
			String dateString = request.getParameter("date");

			String error = validateInputs(title, description, dateString);
			if (error != null) {
				redirectToPageWithError(request, response, albumId, pictureId, error);
				return;
			}
			
			Date date = parseDate(request, response, dateString, albumId, pictureId);
			if(date == null) return;

			// Update the database
			editImage(pictureId, title, description, date);
			redirectToImage(request, response, albumId, pictureId);
		} catch (SQLException e) {
			e.printStackTrace(); // for debugging
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
	}
	
	
	
	// HELPER METHODS
	private int validateAndRetrieveAlbumId(HttpServletRequest request, HttpServletResponse response, int userId)
			throws IOException, SQLException {
		String albumIdString = request.getParameter("albumId");
        if (!InputSanitizer.isValidId(albumIdString)) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid parameter albumId");
            return -1;
        }

        int albumId = Integer.parseInt(albumIdString);
        AlbumDAO albumDao = new AlbumDAO(conn);
        
        //Check if album exists and if user is the owner
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

	private int validateAndRetrievePictureId(HttpServletRequest request, HttpServletResponse response, int albumId)
			throws IOException, SQLException {
		String pictureIdString = request.getParameter("pictureId");
		if (!InputSanitizer.isValidId(pictureIdString)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid parameter pictureId");
            return -1;
		}

		int pictureId = Integer.parseInt(pictureIdString);
		PictureDAO pictureDao = new PictureDAO(conn);

		// Check if picture exists and belongs to current album
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
	
	private String validateInputs(String title, String description, String date) {
        if (!InputSanitizer.isValidTitle(title)) {
            return "Missing or malformed Image Title.";
        }

        if (!InputSanitizer.isValidImageDescription(description)) {
            return "Malformed Image description.";
        }

        if (!InputSanitizer.isValidDate(date)) {
            return "Malformed Image date.";
        }

        return null;
    }
	
	private Album getAlbumById(int albumId) throws SQLException {
		AlbumDAO albumDao = new AlbumDAO(conn);
		return albumDao.getAlbumById(albumId);
	}

	private Picture getPictureById(int pictureId) throws SQLException {
		PictureDAO pictureDao = new PictureDAO(conn);
		return pictureDao.getPictureById(pictureId);
	}

	private Date parseDate(HttpServletRequest request, HttpServletResponse response, String dateString, int albumId, int pictureId) throws IOException {
		String datePattern = "yyyy-MM-dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
        dateFormat.setLenient(false);
        
        try {
			return dateFormat.parse(dateString);
		} catch (ParseException e) {
			String error ="Malformed Image date.";
			redirectToPageWithError(request, response, albumId, pictureId, error);
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private void editImage(int pictureId, String title, String description, Date date) throws SQLException {
		PictureDAO pictureDao = new PictureDAO(conn);
		pictureDao.updatePicture(pictureId, title, description, date);
	}
	
 	private String buildImageHost(HttpServletRequest request) {
		String serverDomain = request.getServerName();
		if ("localhost".equals(serverDomain)) {
			serverDomain += ":" + request.getServerPort();
		}

		ServletContext context = getServletContext();
		return "http://" + serverDomain + context.getInitParameter("ImageHost");
	}
	
 	private void redirectToPageWithError(HttpServletRequest request, HttpServletResponse response, int albumId, int pictureId, String error) throws IOException {
        String paramString = String.format("?error=%s&albumId=%d&pictureId=%d",
                                           error, albumId, pictureId);
        String albumPath = request.getServletContext().getContextPath() + "/EditImage" + paramString;
        response.sendRedirect(albumPath);
    }

	private void prepareContextAndRender(HttpServletRequest request, HttpServletResponse response,
			String path, Picture picture, Album album, int albumPage,
			String imageHost, String error) throws IOException {
		
		IWebExchange webExchange = this.application.buildExchange(request, response);
		WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
		ctx.setVariable("picture", picture);
		ctx.setVariable("error", error);
		ctx.setVariable("album", album);
		ctx.setVariable("albumPage", albumPage);
		ctx.setVariable("imageHost", imageHost);

		templateEngine.process(path, ctx, response.getWriter());
	}

	private void redirectToImage(HttpServletRequest request, HttpServletResponse response, int albumId, int pictureId) throws IOException {
		String paramString = String.format("?albumId=%d&pictureId=%d", albumId, pictureId);
		String path = request.getServletContext().getContextPath() + "/Image" + paramString;
		response.sendRedirect(path);
	}
}










