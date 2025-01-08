package it.polimi.tiw.albums.controllers.helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import jakarta.servlet.ServletContext;

public class DBConnector {
	public static Connection getConnection(ServletContext context) throws ClassNotFoundException, SQLException {
		String driver = context.getInitParameter("dbDriver");
		String url = context.getInitParameter("dbUrl");
		String user = context.getInitParameter("dbUser");
		String password = context.getInitParameter("dbPassword");
		Class.forName(driver);
		return DriverManager.getConnection(url, user, password);
	}
}
