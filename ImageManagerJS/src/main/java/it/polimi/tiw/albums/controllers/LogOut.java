package it.polimi.tiw.albums.controllers;

import java.io.IOException;

import com.google.gson.Gson;

import it.polimi.tiw.albums.CommunicationAPI.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/LogOut")
public class LogOut extends HttpServlet{
	//ATTRIBUTES
	private static final long serialVersionUID = 1L;
	
	
	// CONSTRUCTOR
	public LogOut() {
		super();
	}
	
	
	
	// SERVLET METHODS
	@Override
	public void init() throws ServletException {
		super.init();
	}
	
	
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		HttpSession s = request.getSession();
		s.setAttribute("user", null);

		String logInPath = request.getServletContext().getContextPath() + "/LogIn";
		sendResponse(response, logInPath);
	}
	
	
	
	private void sendResponse(HttpServletResponse response, String redirect) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		ApiResponse responseObj = new ApiResponse();

		response.setStatus(HttpServletResponse.SC_OK);
		responseObj.setRedirect(redirect);
		response.getWriter().write(new Gson().toJson(responseObj));
	}
}





