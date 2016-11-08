package com.employmeo.admin;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import com.employmeo.objects.User;
import com.employmeo.util.SecurityUtil;
import com.talytica.services.EmpAutoLoginFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("changepass")
public class ChangePassword {

	private static final Logger log = LoggerFactory.getLogger(ChangePassword.class);
	
	@POST
	@PermitAll
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String doMethod(@Context final HttpServletRequest reqt, String json) {
		log.debug("Change Password Requested");
		try {
			JSONObject jUser = new JSONObject(json);
			User user = null;
			if (jUser.has("hash")) {
				user = SecurityUtil.loginHashword(jUser.getString("email"), jUser.getString("hash"));
			} else if (jUser.has("password")) {
				user = SecurityUtil.login(jUser.getString("email"), jUser.getString("password"));			
			}
	
			if (user != null) {
				user.refreshMe();
				String pass = jUser.getString("newpass");
				user.setUserPassword(SecurityUtil.hashPassword(pass));
				user.mergeMe();
				EmpAutoLoginFilter.login(user, reqt);
				json = user.getJSONString();
			}
		} catch (Exception e) {
			log.warn("Failed to reset password: " + e.getMessage());
		}
		return json;
	}


}	