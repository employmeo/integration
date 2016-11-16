package com.talytica.integration.objects;


import java.util.Collection;

import javax.persistence.Transient;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.employmeo.data.model.Partner;

public class PartnerPrincipal implements  UserDetails {

	@Transient
	private static final long serialVersionUID = 2345276473565624543L;
	
	private Partner partner;
	
	public PartnerPrincipal(Partner partner) {
		this.partner = partner;
	}

	public Partner getPartner() {
		return this.partner;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return partner.getPassword();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return partner.getLogin();
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

}
