package it.polimi.tiw.albums.CommunicationAPI;

public class ApiResponse{
	private String error;
	private String data;
	
	
	
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
	
}