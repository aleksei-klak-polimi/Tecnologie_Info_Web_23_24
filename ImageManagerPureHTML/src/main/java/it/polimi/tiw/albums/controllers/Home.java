package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.List;

import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.albums.beans.Album;
import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.controllers.helpers.TemplateEngineBuilder;
import it.polimi.tiw.albums.daos.AlbumDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/Home")
public class Home extends DBServlet{
	// ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private ITemplateEngine templateEngine;
	private JakartaServletWebApplication application;

	
	
	// CONSTRUCTOR
	public Home() {
		super();
	}

	
	
	// SERVLET METHODS
	@Override
	public void init() throws ServletException {
		super.init();
		
		this.application = JakartaServletWebApplication.buildApplication(getServletContext());
		this.templateEngine = TemplateEngineBuilder.buildTemplateEngine(this.application);
	}

	
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = "/WEB-INF/Home.html";
		
		HttpSession s = request.getSession();
		User user = (User) s.getAttribute("user");
		String error = request.getParameter("error");
		AlbumDAO albumDao = new AlbumDAO(conn);
		List<Album> myAlbums = null;
		List<Album> otherAlbums = null;
		
		try {
			myAlbums = albumDao.getAlbumsByUser(user.getId());
			otherAlbums = albumDao.getAlbumsByOthers(user.getId());
		}
		catch (SQLException e) {
			e.printStackTrace(); // for debugging
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
		
		
		
		final IWebExchange webExchange = this.application.buildExchange(request, response);
		WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
		ctx.setVariable("myAlbums", myAlbums);
		ctx.setVariable("otherAlbums", otherAlbums);
		ctx.setVariable("error", error);
		
		final Writer writer = response.getWriter();
		
		templateEngine.process(path, ctx, writer);
	}
}