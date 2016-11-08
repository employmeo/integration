package com.talytica.services;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import com.employmeo.objects.Partner;

public class PartnerAuthorizer implements SecurityContext {

	private Partner partner = null;
	
	public PartnerAuthorizer(Partner partner) {
		this.partner = partner;
	}

	@Override
	public String getAuthenticationScheme() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		return partner;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return true;
	}

}
