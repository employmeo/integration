package com.talytica.services;

import java.io.IOException;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import java.lang.reflect.Method;

import com.employmeo.objects.User;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AdminAuthProvider implements ContainerRequestFilter {

	private static final Response LOGIN_REQUIRED = Response.status(Response.Status.UNAUTHORIZED)
			.entity("{ message: 'Login Required' }").build();
	// private static final Response INSUFFICIENT_PERMISSION =
	// Response.status(Response.Status.UNAUTHORIZED).entity("{ message:
	// 'Insufficient Permission' }").build();
	private static final Response ACCESS_FORBIDDEN = Response.status(Response.Status.FORBIDDEN)
			.entity("{ message: 'Access Forbidden' }").build();

	@Context
	private ResourceInfo resourceInfo;
	@Context
	HttpServletRequest reqt;

	@Override
	public void filter(ContainerRequestContext req) throws IOException {

		Method method = resourceInfo.getResourceMethod();
		// Access allowed for all
		if (!method.isAnnotationPresent(PermitAll.class)) {
			// Access denied for all
			if (method.isAnnotationPresent(DenyAll.class)) {
				req.abortWith(ACCESS_FORBIDDEN);
				return;
			}
			User user = (User) reqt.getSession().getAttribute("User");
			if (user == null) {
				req.abortWith(LOGIN_REQUIRED);
				return;
			}

			if (method.isAnnotationPresent(RolesAllowed.class)) {
				// Is role sufficient?
				// RolesAllowed rolesAnnotation =
				// method.getAnnotation(RolesAllowed.class);
				// Set<String> rolesSet = new
				// HashSet<String>(Arrays.asList(rolesAnnotation.value()));
				// if(!true) { req.abortWith(INSUFFICIENT_PERMISSION); return; }
			}
		}
	}
}