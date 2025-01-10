package it.polimi.tiw.albums.CommunicationAPI;

public class ApiResponse{
	private String redirect;
	private String error;
	private String data;
	
	
	
	public String getRedirect() {
		return redirect;
	}
	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}
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