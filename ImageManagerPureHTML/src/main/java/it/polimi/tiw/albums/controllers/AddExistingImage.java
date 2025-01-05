package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.albums.beans.Picture;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.controllers.helpers.DBConnector;
import it.polimi.tiw.albums.controllers.helpers.TemplateEngineBuilder;
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

@WebServlet("/AddToAlbum")
public class AddExistingImage extends HttpServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_BATCH_SIZE = 5;
	private Connection conn;
	private ITemplateEngine templateEngine;
	private JakartaServletWebApplication application;

	
	
	// CONSTRUCTOR
	public AddExistingImage() {
		super();
	}

	
	
	// SERVLET METHODS
	@Override
	public void init() throws ServletException {
		this.application = JakartaServletWebApplication.buildApplication(getServletContext());
		this.templateEngine = TemplateEngineBuilder.buildTemplateEngine(this.application);

		try {
			conn = DBConnector.getConnection(getServletContext());

		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}

	}
	
	
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String path = "/WEB-INF/AddExistingToAlbum.html";
			
			HttpSession s = request.getSession();
			User user = (User) s.getAttribute("user");
			String error = request.getParameter("error");
		
			int userId = user.getId();
			int albumId = validateAndRetrieveAlbumId(request, response, userId);
			if(albumId == -1) return;
		
			PictureDAO pictureDao = new PictureDAO(conn);
			List<Picture> pictures = pictureDao.getPicturesNotInAlbum(albumId, userId);
			
			String imageHost = buildImageHost(request);
			prepareContextAndRender(request, response, path, error, pictures, imageHost, albumId);
		}
		catch (SQLException e) {
			e.printStackTrace(); // for debugging
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
	}
	
	
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			HttpSession s = request.getSession();
			User user = (User) s.getAttribute("user");
			
			int albumId = validateAndRetrieveAlbumId(request, response, user.getId());
			if(albumId == -1) return;
			
			List<Integer> pictureIds = validateAndRetrievePictureIds(request, response, user.getId(), albumId);
			if(pictureIds == null) return;
			
			//Add pictures to new album
			addPicturesToAlbum(pictureIds, albumId);
			redirectToAlbum(request, response, albumId);
		}
		catch (SQLException e) {
			e.printStackTrace(); // for debugging
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
        
        //Check if album exists and if user is the owner
        if (!albumDao.albumExists(albumId) || !albumDao.albumBelongsToUser(albumId, uploader)) {
            returnHome(request, response);
            return -1;
        }

        return albumId;
    }
	
	private List<Integer> validateAndRetrievePictureIds(HttpServletRequest request, HttpServletResponse response, int userId, int albumId) throws SQLException, IOException {
		Set<Integer> pictureIds = new HashSet<Integer>();	
		String[] pictureIdStrings = request.getParameterValues("newPictures");
		
		if(pictureIdStrings == null || pictureIdStrings.length == 0) {
			String error = "No pictures were selected, selection must contain at least one picture.";
			redirectToPageWithError(request, response, albumId, error);
			return null;
		}
		
		//Validate that each entry is a valid id
		for(String id:pictureIdStrings) {
			if(!InputSanitizer.isValidId(id)) {
				returnHome(request, response);
				return null;
			}
			//Check if id is duplicate
			if(pictureIds.contains(Integer.valueOf(id))) {
				returnHome(request, response);
				return null;
			}
				
			pictureIds.add(Integer.valueOf(id));
		}
		
		//Validate that each picture actually belongs to user
		PictureDAO pictureDao = new PictureDAO(conn);
		List<Picture> allowedPictures = pictureDao.getPicturesNotInAlbum(albumId, userId);
		Set<Integer> allowedPictureIds = allowedPictures.stream()
				.map(Picture::getId)
				.collect(Collectors.toCollection(HashSet::new));
		
		for(int pictureId:pictureIds) {
			if(!allowedPictureIds.contains(pictureId)) {
				returnHome(request, response);
				return null;
			}
		}
		
		return pictureIds.stream().toList();
	}
	
	private void addPicturesToAlbum(List<Integer> pictureIds, int albumId) throws SQLException {
		PictureDAO pictureDao = new PictureDAO(conn);
		
		//If the input is very large
		//to avoid overloading the database connection driver
		//send inputs in chunks of max size
		for(int i = 0; i < pictureIds.size(); i+=DEFAULT_BATCH_SIZE) {
			List<Integer> chunk = pictureIds.subList(i, Math.min(pictureIds.size(), i + DEFAULT_BATCH_SIZE));
			pictureDao.addExistingPicturesToAlbum(chunk, albumId);
		}
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
			String path, String error, List<Picture> pictures, String imageHost,
			int albumId) throws IOException{
		
		final IWebExchange webExchange = this.application.buildExchange(request, response);
		WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
		ctx.setVariable("pictures", pictures);
		ctx.setVariable("albumId", albumId);
		ctx.setVariable("error", error);
		ctx.setVariable("imageHost", imageHost);
	
		templateEngine.process(path, ctx, response.getWriter());
	}
	
	private void redirectToPageWithError(HttpServletRequest request, HttpServletResponse response, int albumId, String error) throws IOException {
        String paramString = String.format("?error=%s&albumId=%d", error, albumId);
        String path = request.getServletContext().getContextPath() + "/AddToAlbum" + paramString;
        response.sendRedirect(path);
	}
	
	private void returnHome(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String homePath = request.getServletContext().getContextPath() + "/Home";
		response.sendRedirect(homePath);
	}

	private void redirectToAlbum(HttpServletRequest request, HttpServletResponse response, int albumId) throws IOException {
        String albumPath = request.getServletContext().getContextPath() + "/Album?albumId=" + albumId;
        response.sendRedirect(albumPath);
    }
}








