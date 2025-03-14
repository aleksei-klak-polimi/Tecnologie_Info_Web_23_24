package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;

import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.controllers.helpers.TemplateEngineBuilder;
import it.polimi.tiw.albums.daos.UserDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/LogIn")
public class LogIn extends DBServlet{
	// ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private ITemplateEngine templateEngine;
	private JakartaServletWebApplication application;

	
	
	// CONSTRUCTOR
	public LogIn() {
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
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String path = "/WEB-INF/Log-In.html";
		String error = request.getParameter("error");
		String username = request.getParameter("username");
		
		final IWebExchange webExchange = this.application.buildExchange(request, response);
		WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
		ctx.setVariable("error", error);
		ctx.setVariable("username", username);
		
		final Writer writer = response.getWriter();
		
		templateEngine.process(path, ctx, writer);
	}
	
	
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		boolean validCredentials = true;
		
		//Retrieve inputs
		String username = request.getParameter("username");
		String password = request.getParameter("password");
				
		String error = null;
		String paramString = "";
		
		
		// Sanitize Inputs
		if (!InputSanitizer.isValidUsername(username)) {
			error = "Missing or wrong username.";
			validCredentials = false;
		} 
		else if (!InputSanitizer.isValidPassword(password)) {
			error = "Missing or malformed password.";
			paramString = paramString.concat("&username=").concat(username);
			validCredentials = false;
		}
		else {
			// Attempt to retrieve user from database
			UserDAO userDao = new UserDAO(conn);
			User user = null;
			try {
				user = userDao.getUserByCredentials(username, password);
			}
			catch (SQLException e) {
				e.printStackTrace(); // for debugging
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
			}
			
			if(user != null) {
				//Add user information to html session
				HttpSession s = request.getSession();
				s.setAttribute("user", user);
			}
			else {
				error = "Wrong username or password.";
				validCredentials = false;
			}
		}
		
		
		//Redirect user to appropriate page
		if(validCredentials == false) {
			paramString = "?error=".concat(error).concat(paramString);
			String logInPath = request.getServletContext().getContextPath() + "/LogIn";
			logInPath = logInPath.concat(paramString);
			response.sendRedirect(logInPath);
		}
		else {
			String homePath = request.getServletContext().getContextPath() + "/Home";
			response.sendRedirect(homePath);
		}
	}
}