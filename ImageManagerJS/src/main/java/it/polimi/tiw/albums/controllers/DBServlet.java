package it.polimi.tiw.albums.controllers;

import java.sql.Connection;
import java.sql.SQLException;

import it.polimi.tiw.albums.controllers.helpers.DBConnector;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServlet;

public abstract class DBServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	protected Connection conn;
	
	
	@Override
	public void init() throws ServletException {
		try {
			conn = DBConnector.getConnection(getServletContext());
		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
	}
	
	
	
	@Override
	public void destroy() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e){
				
			}
		}
	}
}
