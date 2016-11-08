package com.employmeo.admin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.employmeo.objects.User;
import com.employmeo.util.SecurityUtil;
import com.talytica.services.EmpAutoLoginFilter;

@Path("login")
public class Login {

	private final Response LOGIN_FAILED = Response.status(Response.Status.UNAUTHORIZED)
			.entity("{ message: 'Login failed' }").build();
	private static final Logger log = LoggerFactory.getLogger(Login.class);

	@POST
	@PermitAll
	public Response doPost(@Context final HttpServletRequest reqt, @Context final HttpServletResponse resp,
			@FormParam("email") String email, @FormParam("password") String password,
			@DefaultValue("false") @FormParam("rememberme") boolean persistLogin) {
		// Collect Expected Input Fields From Form:
		// Validate required fields

		// Execute business logic (lookup the user by email and password)
		User user = SecurityUtil.login(email, password);
		if (user.getUserId() != null) {

			EmpAutoLoginFilter.login(user, reqt);
			ResponseBuilder rb = Response.status(Response.Status.ACCEPTED).entity(user.getJSONString());

			if (persistLogin) {
				String hashword = user.getUserPassword();
				String encodedHash = hashword;
				String encodedEmail = email;
				try {
					encodedEmail = URLEncoder.encode(user.getUserEmail(), "UTF-8");
					encodedHash = URLEncoder.encode(hashword, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					log.warn("UTF-8 unsupported");
				}
				Cookie uCookie = new Cookie("email", encodedEmail, "/", reqt.getServerName());
				Cookie pCookie = new Cookie("hashword", encodedHash, "/", reqt.getServerName());
				rb.cookie(new NewCookie(uCookie, "email", 60 * 60 * 24 * 90, false));
				rb.cookie(new NewCookie(pCookie, "hashword", 60 * 60 * 24 * 90, false));
			} else {
				Cookie uCookie = new Cookie("email", null, "/", reqt.getServerName());
				Cookie pCookie = new Cookie("hashword", null, "/", reqt.getServerName());
				rb.cookie(new NewCookie(uCookie, "email", 0, false));
				rb.cookie(new NewCookie(pCookie, "hashword", 0, false));
			}

			Cookie nCookie = new Cookie("user_fname", user.getUserFname(), "/", reqt.getServerName());
			rb.cookie(new NewCookie(nCookie, "user_fname", 60 * 60 * 24 * 90, false));
log.debug("Put Cookies under: " + reqt.getServerName());
			return rb.build();
		}

		return LOGIN_FAILED;

	}

}