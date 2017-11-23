package com.talytica.integration;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.talytica.integration.service.IntegrationPartnerDetailsService;


public class BearerLoginFilter extends AbstractAuthenticationProcessingFilter {

	static final String TOKEN_PREFIX = "Bearer ";
	static final String HEADER_STRING = "Authorization";

	@Autowired
	IntegrationPartnerDetailsService partnerService;


	public BearerLoginFilter(String url, AuthenticationManager authManager) {
	    super(new AntPathRequestMatcher(url));
	    setAuthenticationManager(authManager);
	}


  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest req, HttpServletResponse res)
      throws AuthenticationException, IOException, ServletException {

		String header = req.getHeader(HEADER_STRING);
		String login = null;
	  if ((header != null) && (header.indexOf(TOKEN_PREFIX) >= 0)) {
	    	login = header.replace(TOKEN_PREFIX, "");
	  }
    
    return getAuthenticationManager().authenticate(
        new UsernamePasswordAuthenticationToken(
            login,
            "",
            Collections.emptyList()
        )
    );
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest req,
      HttpServletResponse res, FilterChain chain,
      Authentication auth) throws IOException, ServletException {

  }
}
	
	