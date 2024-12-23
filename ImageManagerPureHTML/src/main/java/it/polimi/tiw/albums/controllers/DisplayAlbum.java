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

import it.polimi.tiw.albums.beans.Picture;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.beans.Album;
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

@WebServlet("/Album")
public class DisplayAlbum extends HttpServlet {
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_PAGE_SIZE = 5;
	
	private ITemplateEngine templateEngine;
	private JakartaServletWebApplication application;
	private Connection conn;
	
	
	// CONSTRUCTOR
	public DisplayAlbum() {
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
        String path = "/WEB-INF/Album.html";

        try {
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            String error = request.getParameter("error");
            
            //PARSE INPUTS
            int albumId = parseInt(request.getParameter("albumId"), -1);
            if (albumId == -1) {
                returnHome(request, response);
                return;
            }

            AlbumDAO albumDao = new AlbumDAO(conn);
            if (!albumDao.albumExists(albumId)) {
                returnHome(request, response);
                return;
            }

            int albumPage = parseInt(request.getParameter("albumPage"), 1);
            int pictureCount = albumDao.getAmountOfPicturesByAlbum(albumId);
            //Checks if provided page actually exists in the album
            int maxAlbumPage = Math.max(1, (int) Math.ceil((double) pictureCount / DEFAULT_PAGE_SIZE));

            if (albumPage > maxAlbumPage) {
                albumPage = maxAlbumPage;
            }

            Album album = albumDao.getAlbumById(albumId);
            boolean isOwner = albumDao.albumBelongsToUser(albumId, user.getId());

            PictureDAO pictureDao = new PictureDAO(conn);
            List<Picture> pictures = pictureDao.getPicturesFromAlbumByPage(albumId, albumPage, DEFAULT_PAGE_SIZE);

            String imageHost = buildImageHost(request);

            prepareContextAndRender(request, response, path, error, isOwner, pictures, album, imageHost, 
                                     albumPage, maxAlbumPage, albumId);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
        }
    }

	
	
	
	//HELPER METHODS
	private int parseInt(String param, int defaultValue) {
        return InputSanitizer.isValidId(param) ? Integer.parseInt(param) : defaultValue;
    }
	
	
	private String buildImageHost(HttpServletRequest request) {
        String serverDomain = request.getServerName();
        if ("localhost".equals(serverDomain)) {
            serverDomain += ":" + request.getServerPort();
        }

        ServletContext context = getServletContext();
        return "http://" + serverDomain + context.getInitParameter("ImageHost");
    }
	
	
	private void prepareContextAndRender(HttpServletRequest request, HttpServletResponse response, String path,
			String error, boolean isOwner, List<Picture> pictures, Album album, String imageHost, int albumPage,
			int maxAlbumPage, int albumId) throws IOException {
		boolean hasNextPictures = albumPage < maxAlbumPage;
		boolean hasPrevPictures = albumPage > 1;

		String nextPicturesPath = hasNextPictures ? albumPagePath(albumId, albumPage + 1) : "";
		String prevPicturesPath = hasPrevPictures ? albumPagePath(albumId, albumPage - 1) : "";

		IWebExchange webExchange = this.application.buildExchange(request, response);
		WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
		ctx.setVariable("error", error);
		ctx.setVariable("isOwner", isOwner);
		ctx.setVariable("pictures", pictures);
		ctx.setVariable("album", album);
		ctx.setVariable("albumPage", albumPage);
		ctx.setVariable("imageHost", imageHost);
		ctx.setVariable("hasNextPictures", hasNextPictures);
		ctx.setVariable("hasPrevPictures", hasPrevPictures);
		ctx.setVariable("pageSize", DEFAULT_PAGE_SIZE);
		ctx.setVariable("nextPicturesPath", nextPicturesPath);
		ctx.setVariable("prevPicturesPath", prevPicturesPath);

		templateEngine.process(path, ctx, response.getWriter());
	}
	
	private String albumPagePath(int albumId, int page) {
        return "/Album?albumId=" + albumId + "&albumPage=" + page;
    }
	
	private void returnHome(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String homePath = request.getServletContext().getContextPath() + "/Home";
		response.sendRedirect(homePath);
	}

}







