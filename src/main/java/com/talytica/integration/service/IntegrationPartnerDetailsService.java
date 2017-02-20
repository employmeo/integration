package com.talytica.integration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.employmeo.data.model.Partner;
import com.employmeo.data.repository.PartnerRepository;
import com.talytica.integration.objects.PartnerPrincipal;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IntegrationPartnerDetailsService implements UserDetailsService {
	
	@Autowired
	private PartnerRepository partnerRepository;

    @Override
	public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
		Partner partner = partnerRepository.findByLogin(login);
		if (partner != null) {
			return new PartnerPrincipal(partner);
		} else {
		    throw new UsernameNotFoundException(login);
		}
	}
    
}
