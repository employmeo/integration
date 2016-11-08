package com.talytica.services.portal;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import io.swagger.annotations.Api;

@Path("logout")
@Api( value="/logout")
public class Logout {

	@POST
	@PermitAll
	public Response doPost(@Context final HttpServletRequest reqt, @Context final HttpServletResponse resp) {

		ResponseBuilder rb = Response.status(Response.Status.ACCEPTED).entity("{ message: 'Logged Out' }");
		Cookie uCookie = new Cookie("email", null, "/", reqt.getServerName());
		rb.cookie(new NewCookie(uCookie, "email", 1, false));
		Cookie pCookie = new Cookie("hashword", null, "/", reqt.getServerName());
		rb.cookie(new NewCookie(pCookie, "hashword", 1, false));
		Cookie nCookie = new Cookie("user_fname", null, "/", reqt.getServerName());
		rb.cookie(new NewCookie(nCookie, "user_fname", 1, false));

		reqt.getSession().invalidate();

		return rb.build();
	}

}