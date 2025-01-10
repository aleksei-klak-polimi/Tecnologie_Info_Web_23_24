package it.polimi.tiw.albums.filters;

import java.io.IOException;

import com.google.gson.Gson;

import it.polimi.tiw.albums.CommunicationAPI.ApiResponse;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class NotLoggedInFilterAPI implements Filter{

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String loginpath = req.getServletContext().getContextPath() + "/LogIn";

		HttpSession s = req.getSession();
		if (s.isNew() || s.getAttribute("user") == null) {
			sendRedirect(res, loginpath);
			return;
		}
		// pass the request along the filter chain
		chain.doFilter(request, response);
	}
	
	private void sendRedirect(HttpServletResponse response, String redirect) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		ApiResponse responseObj = new ApiResponse();

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		responseObj.setError("User is not logged in!");
		responseObj.setRedirect(redirect);
		
		response.getWriter().write(new Gson().toJson(responseObj));
	}
	
}