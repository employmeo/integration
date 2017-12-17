package com.talytica.integration;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Security;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.internal.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import com.talytica.integration.service.IntegrationPartnerDetailsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BearerLoginFilter extends GenericFilterBean {

	static final String TOKEN_PREFIX = "Bearer ";
	static final String AUTH_HEADER = "Authorization";
	
	private AuthenticationManager authenticationManager;
	
	public BearerLoginFilter(AuthenticationManager mgr) {
		authenticationManager = mgr;
	}
	
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    	
    	HttpServletRequest req = (HttpServletRequest) request;
		String header = req.getHeader(AUTH_HEADER);
		String login = null;
	    if ((header != null) && (header.indexOf(TOKEN_PREFIX) >= 0)) {
	    	login = header.replace(TOKEN_PREFIX, "");
	    	log.debug("Attempted login with Bearer login: {}", login);
    	
	    	try {
	    		Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login,"",Collections.emptyList()));
		    	SecurityContextHolder.getContext().setAuthentication(auth);
	    	} catch (AuthenticationException ae) {
	    		((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
	    		return;
	    	}
	    	
	    }   	        
	    filterChain.doFilter(request,response);

    }
}