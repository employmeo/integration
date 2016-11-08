package com.employmeo.util;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

@WebFilter(filterName = "hstsFilter",urlPatterns = {"*"})
public class HSTSFilter implements Filter {

	public static boolean FORCE_SECURE = false;
	private static final Logger log = LoggerFactory.getLogger(HSTSFilter.class);
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
    		HttpServletResponse resp = (HttpServletResponse) res;
    		if (req.isSecure() && FORCE_SECURE) 
            	resp.setHeader("Strict-Transport-Security", "max-age=31622400; includeSubDomains");
            chain.doFilter(req, resp);
    }

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		FORCE_SECURE = Boolean.valueOf(System.getenv("FORCE_SECURE"));
		if (FORCE_SECURE) log.debug("Using HSTS to force traffic through https");
	}

}
