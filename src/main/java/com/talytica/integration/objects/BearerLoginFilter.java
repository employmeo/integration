package com.talytica.integration.objects;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.filter.GenericFilterBean;

public class BearerLoginFilter extends GenericFilterBean {

	  static final String TOKEN_PREFIX = "Bearer";
	  static final String HEADER_STRING = "Authorization";
	  
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		
		HttpServletRequest rqst = (HttpServletRequest) request;
		
		String header = rqst.getHeader(HEADER_STRING);
		
		
	    filterChain.doFilter(rqst,response);

	}

}
