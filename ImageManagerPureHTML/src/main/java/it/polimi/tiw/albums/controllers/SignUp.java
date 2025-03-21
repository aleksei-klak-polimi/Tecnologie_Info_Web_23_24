package it.polimi.tiw.albums.controllers;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import it.polimi.tiw.albums.beans.User;
import it.polimi.tiw.albums.controllers.helpers.TemplateEngineBuilder;
import it.polimi.tiw.albums.daos.UserDAO;
import it.polimi.tiw.albums.utils.InputSanitizer;

@WebServlet("/SignUp")
public class SignUp extends DBServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	private ITemplateEngine templateEngine;
	private JakartaServletWebApplication application;
	
	
	
	//CONSTRUCTOR
	public SignUp() {
		super();
	}
	
	
	
	//SERVLET METHODS
	@Override
	public void init() throws ServletException {
		super.init();
		this.application = JakartaServletWebApplication.buildApplication(getServletContext());
		this.templateEngine = TemplateEngineBuilder.buildTemplateEngine(this.application);	
	}
	
	
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String path = "/WEB-INF/Sign-Up.html";
		String username = request.getParameter("username");
		String email = request.getParameter("email");
		String error = request.getParameter("error");
		
		final IWebExchange webExchange = this.application.buildExchange(request, response);
		WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
		ctx.setVariable("username", username);
		ctx.setVariable("email", email);
		ctx.setVariable("error", error);
		
		final Writer writer = response.getWriter();
		
		templateEngine.process(path, ctx, writer);
	}
	
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		boolean validCredentials = true;
		String paramString = "";
		
		//Retrieve inputs
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String repeatPassword = request.getParameter("repeatPassword");
		String email = request.getParameter("email");
		
		String error = null;
		
		
		//Sanitize Inputs
		if(!InputSanitizer.isValidUsername(username)) {
			error = "Missing or wrong username.";
			validCredentials = false;
		}
		else if(!InputSanitizer.isVaildEmail(email)) {
			error = "Missing or wrong email.";
			paramString = paramString.concat("&username=").concat(username);
			validCredentials = false;
		}
		else if(!InputSanitizer.isValidPassword(password)) {
			error = "Missing or malformed password.";
			paramString = paramString.concat("&username=").concat(username).concat("&email=").concat(email);
			validCredentials = false;
		}
		else if(repeatPassword == null || repeatPassword.isBlank() || repeatPassword.isEmpty()) {
			error = "Missing repeat password.";
			paramString = paramString.concat("&username=").concat(username).concat("&email=").concat(email);
			validCredentials = false;
		}
		else if(!password.equals(repeatPassword)) {
			error = "Passwords don't match.";
			paramString = paramString.concat("&username=").concat(username).concat("&email=").concat(email);
			validCredentials = false;
		}
		
		
		
		else {
			// Check if database already contains provided username or email
			UserDAO userDao = new UserDAO(conn);
			try {
				if (!userDao.isUsernameAvailable(username)) {
					error = "Username is not available.";
					validCredentials = false;
				}

				else if (!userDao.isEmailAvailable(email)) {
					error = "Email is not available.";
					validCredentials = false;
				}
			} 
			catch (SQLException e) {
				e.printStackTrace(); // for debugging
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
			}
		}
		
		
		
		if(validCredentials == false) {
			paramString = "?error=".concat(error).concat(paramString);
			String signUpPath = request.getServletContext().getContextPath() + "/SignUp";
			signUpPath = signUpPath.concat(paramString);
			response.sendRedirect(signUpPath);
		}
		else {
			//Credentials are valid, create user and redirect to home page
			UserDAO userDao = new UserDAO(conn);
			try {
				userDao.createUser(username, email, password);
				User user = userDao.getUserByCredentials(username, password);
				//Add user information to html session
				HttpSession s = request.getSession();
				s.setAttribute("user", user);
				
				
				String homePath = request.getServletContext().getContextPath() + "/Home";
				response.sendRedirect(homePath);	
			}
			catch (SQLException e) {
				e.printStackTrace(); // for debugging
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
			}
		}
		
		return;
	}

	
	
}






