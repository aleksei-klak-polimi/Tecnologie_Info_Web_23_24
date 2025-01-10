package it.polimi.tiw.albums.controllers;

import java.io.IOException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/Home")
public class Home extends HttpServlet {
	// ATTRIBUTES
	private static final long serialVersionUID = 1L;

	
	
	// CONSTRUCTOR
	public Home() {
		super();
	}

	
	
	// SERVLET METHODS
	@Override
	public void init() throws ServletException {
		super.init();
	}

	
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = "/WEB-INF/Home.html";
		
		ServletContext sc = getServletContext();
		sc.getRequestDispatcher(path).forward(request, response);
	}
}






