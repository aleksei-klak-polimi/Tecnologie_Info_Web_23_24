package it.polimi.tiw.albums.daos;

import java.sql.Connection;

public class PictureDAO{
private Connection con;
	
	
	
	//CONSTRUCTOR
	public PictureDAO(Connection con) {
		this.con = con;
	}
}