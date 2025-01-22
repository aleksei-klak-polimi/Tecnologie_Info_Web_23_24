package it.polimi.tiw.albums.controllers;

import java.io.IOException;

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
	}
}





