package it.polimi.tiw.albums.filters;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AlreadyLoggedInFilter implements Filter{

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String Homepath = req.getServletContext().getContextPath() + "/Home";

		HttpSession s = req.getSession();
		if (s.getAttribute("user") != null) {
			res.sendRedirect(Homepath);
			return;
		}
		// pass the request along the filter chain
		chain.doFilter(request, response);
		
	}
	
}